package org.utplsql.api;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.rules.DatabaseRule;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CompatibilityIT {

    @Rule
    public final DatabaseRule db = new DatabaseRule();

    @Test
    public void compatibleVersion() {
        try {
            Connection conn = db.newConnection();
            CompatibilityProxy proxy = new CompatibilityProxy(conn);
            proxy.failOnNotCompatible();
            assertEquals(true, proxy.isCompatible());
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    public void skipCompatibilityCheck()
    {
        try {
            Connection conn = db.newConnection();
            CompatibilityProxy proxy = new CompatibilityProxy(conn, true);
            proxy.failOnNotCompatible();
            assertEquals(true, proxy.isCompatible());
            assertEquals(CompatibilityProxy.UTPLSQL_API_VERSION, proxy.getDatabaseVersion().toString());
        } catch (SQLException e) {
            fail(e);
        }
    }
}