package nasi_bergizi_pajak.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nasi_bergizi_pajak.util.PasswordUtil;

public class DatabaseInitializer {
    private static final Path DATABASE_DIR = Path.of("database");
    private static final Path INIT_SQL = DATABASE_DIR.resolve("init.sql");
    private static final String DEFAULT_ADMIN_EMAIL = "admin@nasibergizipajak.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin12345";

    private DatabaseInitializer() {
    }

    public static void initializeIfNeeded() throws IOException, SQLException {
        Files.createDirectories(DATABASE_DIR);

        if (!tableExists("user_account")) {
            runInitScript();
        }

        ensureAdminColumnExists();
        ensureDefaultAdminExists();
    }

    private static void runInitScript() throws IOException, SQLException {
        if (!Files.exists(INIT_SQL)) {
            throw new IOException("File init.sql tidak ditemukan di folder database.");
        }

        String sql = Files.readString(INIT_SQL);

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            for (String query : sql.split(";")) {
                String trimmedQuery = query.trim();
                if (!trimmedQuery.isEmpty()) {
                    statement.execute(trimmedQuery);
                }
            }
        }
    }

    private static boolean tableExists(String tableName) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();

            try (ResultSet resultSet = metaData.getTables(catalog, null, tableName, new String[]{"TABLE"})) {
                return resultSet.next();
            }
        }
    }

    private static void ensureAdminColumnExists() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (!columnExists(connection, "user_account", "tipe_admin")) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("""
                            ALTER TABLE user_account
                            ADD COLUMN tipe_admin TINYINT(1) NOT NULL DEFAULT 0
                            """);
                }
            }
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();

        try (ResultSet resultSet = metaData.getColumns(catalog, null, tableName, columnName)) {
            if (resultSet.next()) {
                return true;
            }
        }

        try (ResultSet resultSet = metaData.getColumns(catalog, null, tableName, columnName.toUpperCase())) {
            if (resultSet.next()) {
                return true;
            }
        }

        try (ResultSet resultSet = metaData.getColumns(catalog, null, tableName, columnName.toLowerCase())) {
            return resultSet.next();
        }
    }

    private static void ensureDefaultAdminExists() throws SQLException {
        String selectSql = "SELECT user_id FROM user_account WHERE email = ?";
        String insertSql = """
                INSERT INTO user_account (email, password, first_name, last_name, active, profile_image_name, tipe_admin)
                VALUES (?, ?, ?, ?, 1, NULL, 1)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            selectStatement.setString(1, DEFAULT_ADMIN_EMAIL);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    return;
                }
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                insertStatement.setString(1, DEFAULT_ADMIN_EMAIL);
                insertStatement.setString(2, PasswordUtil.hashPassword(DEFAULT_ADMIN_PASSWORD));
                insertStatement.setString(3, "Admin");
                insertStatement.setString(4, "Nasi Bergizi Pajak");
                insertStatement.executeUpdate();
            }
        }
    }
}
