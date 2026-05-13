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
        ensureFamilyMemberSchema();
        ensureBudgetTableExists();
        ensureNutritionAndPriceTablesExist();
        ensureRecipeIngredientSchema();
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

    private static void ensureFamilyMemberSchema() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS family_member (
                        member_id    INT          NOT NULL AUTO_INCREMENT,
                        user_id      INT          NOT NULL,
                        name         VARCHAR(100) NOT NULL,
                        relationship VARCHAR(50),
                        birth_date   DATE,
                        height       DOUBLE,
                        weight       DOUBLE,
                        allergy      TEXT,
                        PRIMARY KEY (member_id),
                        KEY idx_family_member_user_id (user_id),
                        CONSTRAINT fk_fm_user FOREIGN KEY (user_id)
                            REFERENCES user_account(user_id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """);

            if (!columnExists(connection, "family_member", "relationship")) {
                statement.execute("ALTER TABLE family_member ADD COLUMN relationship VARCHAR(50) AFTER name");
            }
        }
    }

    private static void ensureBudgetTableExists() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS budget (
                        budget_id     INT             NOT NULL AUTO_INCREMENT,
                        user_id       INT             NOT NULL,
                        name          VARCHAR(100)    NOT NULL,
                        amount        DECIMAL(15,2)   NOT NULL,
                        period_start  DATE            NOT NULL,
                        period_end    DATE            NOT NULL,
                        status        VARCHAR(50)     NOT NULL DEFAULT 'active',
                        PRIMARY KEY (budget_id),
                        KEY idx_budget_user_id (user_id),
                        CONSTRAINT fk_budget_user FOREIGN KEY (user_id)
                            REFERENCES user_account(user_id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """);
        }
    }

    private static void ensureNutritionAndPriceTablesExist() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS ingredient_nutrition (
                        nutrition_id   INT             NOT NULL AUTO_INCREMENT,
                        ingredient_id  INT             NOT NULL,
                        calories       DECIMAL(10,2)   NOT NULL DEFAULT 0,
                        protein        DECIMAL(10,2)   NOT NULL DEFAULT 0,
                        carbohydrate   DECIMAL(10,2)   NOT NULL DEFAULT 0,
                        fat            DECIMAL(10,2)   NOT NULL DEFAULT 0,
                        fibre          DECIMAL(10,2)   NOT NULL DEFAULT 0,
                        unit           VARCHAR(50)     NOT NULL,
                        PRIMARY KEY (nutrition_id),
                        UNIQUE KEY uq_nutrition_ingredient (ingredient_id),
                        CONSTRAINT fk_in_ingredient FOREIGN KEY (ingredient_id)
                            REFERENCES ingredient(ingredient_id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS ingredient_price (
                        price_id        INT             NOT NULL AUTO_INCREMENT,
                        ingredient_id   INT             NOT NULL,
                        price           DECIMAL(15,2)   NOT NULL,
                        effective_date  DATE            NOT NULL,
                        PRIMARY KEY (price_id),
                        KEY idx_ingredient_price_ingredient_id (ingredient_id),
                        CONSTRAINT fk_ip_ingredient FOREIGN KEY (ingredient_id)
                            REFERENCES ingredient(ingredient_id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """);
        }
    }

    private static void ensureRecipeIngredientSchema() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (!tableExists(connection, "recipe_ingredient")) {
                return;
            }

            if (!columnExists(connection, "recipe_ingredient", "amount")) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("ALTER TABLE recipe_ingredient ADD COLUMN amount DOUBLE NOT NULL DEFAULT 0");
                    if (columnExists(connection, "recipe_ingredient", "quantity")) {
                        statement.execute("UPDATE recipe_ingredient SET amount = quantity");
                    }
                }
            }

            if (!columnExists(connection, "recipe_ingredient", "unit")) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("ALTER TABLE recipe_ingredient ADD COLUMN unit VARCHAR(50) NOT NULL DEFAULT ''");
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

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();

        try (ResultSet resultSet = metaData.getTables(catalog, null, tableName, new String[]{"TABLE"})) {
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
