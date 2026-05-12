package nasi_bergizi_pajak.config;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface DatabaseConfig {
    Connection getConnection() throws SQLException;
}
