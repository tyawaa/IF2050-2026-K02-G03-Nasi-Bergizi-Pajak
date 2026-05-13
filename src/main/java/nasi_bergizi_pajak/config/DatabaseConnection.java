package nasi_bergizi_pajak.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Default: SQLite (tidak perlu server, file lokal)
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:database/nasi_bergizi_pajak.db";
    private static final String DEFAULT_DB_USER = "";
    private static final String DEFAULT_DB_PASSWORD = "";

    // Untuk pakai MySQL, set env var:
    //   DB_URL=jdbc:mysql://localhost:3306/nasi_bergizi_pajak?...
    //   DB_USER=root
    //   DB_PASSWORD=passwordmu

    public static Connection getConnection() throws SQLException {
        String dbUrl = getConfig("DB_URL", "db.url", DEFAULT_DB_URL);
        String dbUser = getConfig("DB_USER", "db.user", DEFAULT_DB_USER);
        String dbPassword = getConfig("DB_PASSWORD", "db.password", DEFAULT_DB_PASSWORD);

        if (dbUrl.startsWith("jdbc:sqlite")) {
            return DriverManager.getConnection(dbUrl);
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private static String getConfig(String envName, String propertyName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return defaultValue;
    }
}
