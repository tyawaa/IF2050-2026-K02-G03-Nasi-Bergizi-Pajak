package nasi_bergizi_pajak.config;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final DatabaseConfig instance = new DatabaseConfig();
    
    private DatabaseConfig() {}
    
    public static DatabaseConfig getInstance() {
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
}
