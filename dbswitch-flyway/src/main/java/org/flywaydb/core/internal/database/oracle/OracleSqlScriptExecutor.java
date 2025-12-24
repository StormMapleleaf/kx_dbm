package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;

import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.sqlscript.DefaultSqlScriptExecutor;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.util.AsciiTable;
import org.flywaydb.core.internal.util.DateUtils;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("SqlResolve")
public class OracleSqlScriptExecutor extends DefaultSqlScriptExecutor {



















    public OracleSqlScriptExecutor(JdbcTemplate jdbcTemplate




    ) {
        super(jdbcTemplate



        );
    }





























































































































































































































































}