package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;

public class SnowflakeParser extends Parser {
    private final String ALTERNATIVE_QUOTE = "$$";

    public SnowflakeParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 2);
    }

    @Override
    protected boolean isAlternativeStringLiteral(String peek) {
        if (peek.startsWith("$$")) {
            return true;
        }

        return super.isAlternativeStringLiteral(peek);
    }

    @Override
    protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        reader.swallow(ALTERNATIVE_QUOTE.length());
        reader.swallowUntilExcluding(ALTERNATIVE_QUOTE);
        reader.swallow(ALTERNATIVE_QUOTE.length());
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }
}