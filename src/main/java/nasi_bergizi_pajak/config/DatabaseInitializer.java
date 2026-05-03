package nasi_bergizi_pajak.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final Path DATABASE_DIR = Path.of("database");
    private static final Path DATABASE_FILE = DATABASE_DIR.resolve("nasi_bergizi_pajak.db");
    private static final Path INIT_SQL = DATABASE_DIR.resolve("init.sql");

    private DatabaseInitializer() {
    }

    public static void initializeIfNeeded() throws IOException, SQLException {
        Files.createDirectories(DATABASE_DIR);

        if (Files.exists(DATABASE_FILE)) {
            return;
        }

        runInitScript();
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
}
