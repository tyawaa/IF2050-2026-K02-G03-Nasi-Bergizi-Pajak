package nasi_bergizi_pajak.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nasi_bergizi_pajak.util.PasswordUtil;

public class DatabaseInitializer {
    private static final Path DATABASE_DIR = Path.of("database");
    private static final Path DATABASE_FILE = DATABASE_DIR.resolve("nasi_bergizi_pajak.db");
    private static final Path INIT_SQL = DATABASE_DIR.resolve("init.sql");
    private static final String DEFAULT_ADMIN_EMAIL = "admin@nasibergizipajak.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin12345";

    private DatabaseInitializer() {
    }

    public static void initializeIfNeeded() throws IOException, SQLException {
        Files.createDirectories(DATABASE_DIR);

        if (Files.exists(DATABASE_FILE)) {
            ensureAdminColumnExists();
            ensureDefaultAdminExists();
            return;
        }

        runInitScript();
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

    private static void ensureAdminColumnExists() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            if (!columnExists(statement, "user_account", "tipe_admin")) {
                statement.execute("""
                        ALTER TABLE user_account
                        ADD COLUMN tipe_admin INTEGER NOT NULL DEFAULT 0 CHECK (tipe_admin IN (0, 1))
                        """);
            }
        }
    }

    private static boolean columnExists(Statement statement, String tableName, String columnName) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (resultSet.next()) {
                if (columnName.equals(resultSet.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
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
