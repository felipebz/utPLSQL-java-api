package org.utplsql.api.reporter;

import org.utplsql.api.CustomTypes;

import java.sql.SQLException;

public class CoverallsReporter extends OutputReporter {

    @Override
    public String getSQLTypeName() throws SQLException {
        return CustomTypes.UT_COVERALLS_REPORTER;
    }
    
}
