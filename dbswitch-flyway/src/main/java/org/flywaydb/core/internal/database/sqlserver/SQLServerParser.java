package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;
import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SQLServerParser extends Parser {
    private static final List<String> SPROCS_INVALID_IN_TRANSACTIONS = Arrays.asList(
            "SP_ADDSUBSCRIPTION", "SP_DROPSUBSCRIPTION",
            "SP_ADDDISTRIBUTOR", "SP_DROPDISTRIBUTOR",
            "SP_ADDDISTPUBLISHER", "SP_DROPDISTPUBLISHER",
            "SP_ADDLINKEDSERVER", "SP_DROPLINKEDSERVER",
            "SP_ADDLINKEDSRVLOGIN", "SP_DROPLINKEDSRVLOGIN",
            "SP_SERVEROPTION", "SP_REPLICATIONDBOPTION");

    public SQLServerParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 3);
    }

    @Override
    protected Delimiter getDefaultDelimiter() {
        return Delimiter.GO;
    }

    @Override
    protected boolean isDelimiter(String peek, ParserContext context, int col) {
        return peek.length() >= 2
                && (peek.charAt(0) == 'G' || peek.charAt(0) == 'g')
                && (peek.charAt(1) == 'O' || peek.charAt(1) == 'o')
                && (peek.length() == 2 || Character.isWhitespace(peek.charAt(2)));
    }

    @Override
    protected String readKeyword(PeekingReader reader, Delimiter delimiter, ParserContext context) throws IOException {
        return "" + (char) reader.read() + reader.readKeywordPart(null, context);
    }

    @Override
    protected Boolean detectCanExecuteInTransaction(String simplifiedStatement, List<Token> keywords) {
        String current = keywords.get(keywords.size() - 1).getText();
        if ("BACKUP".equals(current) || "RESTORE".equals(current) || "RECONFIGURE".equals(current)) {
            return false;
        }

        if (keywords.size() < 2) {
            return null;
        }

        String previous = keywords.get(keywords.size() - 2).getText();

        if ("EXEC".equals(previous) && SPROCS_INVALID_IN_TRANSACTIONS.contains(current)) {
            return false;
        }

        if (("CREATE".equals(previous) || "ALTER".equals(previous) || "DROP".equals(previous))
                && ("DATABASE".equals(current) || "FULLTEXT".equals(current))) {
            return false;
        }

        return null;
    }

    @Override
    protected int getTransactionalDetectionCutoff() {
        return Integer.MAX_VALUE;
    }
}