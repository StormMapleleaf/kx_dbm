package org.flywaydb.core.internal.output;

import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ErrorOutput {

    public static class ErrorOutputItem {
        public ErrorCode errorCode;
        public String message;
        public String stackTrace;

        ErrorOutputItem(ErrorCode errorCode, String message, String stackTrace) {
            this.errorCode = errorCode;
            this.message = message;
            this.stackTrace = stackTrace;
        }
    }

    public ErrorOutputItem error;

    public ErrorOutput(ErrorCode errorCode, String message, String stackTrace) {
        this.error = new ErrorOutputItem(errorCode, message, stackTrace);
    }

    public static ErrorOutput fromException(Exception exception) {
        String message = exception.getMessage();

        if (exception instanceof FlywayException) {
            FlywayException flywayException = (FlywayException)exception;

            return new ErrorOutput(
                    flywayException.getErrorCode(),
                    message == null ? "Error occurred" : message,
                    null);
        }

        return new ErrorOutput(
                ErrorCode.FAULT,
                message == null ? "Fault occurred" : message,
                getStackTrace(exception));
    }

    private static String getStackTrace(Exception exception) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream printStream;

        try {
            printStream = new PrintStream(output, true, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return "";
        }

        exception.printStackTrace(printStream);

        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}