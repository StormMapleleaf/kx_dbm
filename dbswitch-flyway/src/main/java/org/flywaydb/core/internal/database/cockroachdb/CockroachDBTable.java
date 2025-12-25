package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.util.SqlCallable;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Random;

public class CockroachDBTable extends Table<CockroachDBDatabase, CockroachDBSchema> {
    private static final Log LOG = LogFactory.getLog(CockroachDBTable.class);

        private String tableLockString = RandomStringGenerator.getNextRandomString();

        CockroachDBTable(JdbcTemplate jdbcTemplate, CockroachDBDatabase database, CockroachDBSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        new CockroachDBRetryingStrategy().execute(new SqlCallable<Integer>() {
            @Override
            public Integer call() throws SQLException {
                doDropOnce();
                return null;
            }
        });
    }

    protected void doDropOnce() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return new CockroachDBRetryingStrategy().execute(new SqlCallable<Boolean>() {
            @Override
            public Boolean call() throws SQLException {
                return doExistsOnce();
            }
        });
    }

    protected boolean doExistsOnce() throws SQLException {
        if (schema.cockroachDB1) {
            return jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                    "   SELECT 1\n" +
                    "   FROM   information_schema.tables \n" +
                    "   WHERE  table_schema = ?\n" +
                    "   AND    table_name = ?\n" +
                    ")", schema.getName(), name);
        }

        return jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                "   SELECT 1\n" +
                "   FROM   information_schema.tables \n" +
                "   WHERE  table_catalog = ?\n" +
                "   AND    table_schema = 'public'\n" +
                "   AND    table_name = ?\n" +
                ")", schema.getName(), name);
    }

    @Override
    protected void doLock() throws SQLException {
        if (lockDepth > 0) {
            return;
        }

        int retryCount = 0;
        do {
            try {
                if (insertLockingRow()) {
                    return;
                }
                retryCount++;
                LOG.debug("Waiting for lock on " + this);
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        } while (retryCount < 50);

        throw new FlywayException("Unable to obtain table lock - another Flyway instance may be running");
    }

    private boolean insertLockingRow() {
        Results results = jdbcTemplate.executeStatement("INSERT INTO " + this + " VALUES (-100, '" + tableLockString + "', 'flyway-lock', '', '', 0, '', now(), 0, TRUE)");
        return (results.getResults().size() > 0
                && results.getResults().get(0).getUpdateCount() == 1
                && results.getErrors().size() == 0);
    }

    @Override
    protected void doUnlock() throws SQLException {
        if (lockDepth > 1) {
            return;
        }

        int competingLocksTaken = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + this + " WHERE version != '" + tableLockString + "' AND DESCRIPTION = 'flyway-lock'");
        if (competingLocksTaken > 0) {
            throw new FlywayException("Internal error: on unlocking, a competing lock was found");
        }

        jdbcTemplate.executeStatement("DELETE FROM " + this + " WHERE version = '" + tableLockString + "' AND DESCRIPTION = 'flyway-lock'");
    }
}

class RandomStringGenerator {
    static final Random random = new Random();

    public static String getNextRandomString(){
        BigInteger bInt = new BigInteger(128, random);
        return bInt.toString(16);
    }
}