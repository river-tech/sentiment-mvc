package com.team.app.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * Simple standalone checker for PostgreSQL connection using application.properties
 */
public class DbConnectionTest {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (InputStream is = DbConnectionTest.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new IllegalStateException("application.properties not found on classpath");
            }
            props.load(is);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");
        String driver = props.getProperty("db.driver", "org.postgresql.Driver");

        // Ensure driver is loaded
        Class.forName(driver);

        System.out.println("Connecting to: " + url + " as " + user);
        try (Connection con = DriverManager.getConnection(url, user, pass);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1")) {
            if (rs.next()) {
                System.out.println("Connection OK. SELECT 1 -> " + rs.getInt(1));
            } else {
                System.out.println("Connection OK but SELECT 1 returned no rows");
            }
        }
    }
}


