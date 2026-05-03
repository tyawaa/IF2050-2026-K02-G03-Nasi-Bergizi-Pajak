package nasi_bergizi_pajak.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.Akun;

public class AkunDAO {
    public Akun simpanAkun(Akun akun) throws SQLException {
        String sql = """
                INSERT INTO user_account (email, password, first_name, last_name, active, profile_image_name)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, akun.getEmail());
            statement.setString(2, akun.getPassword());
            statement.setString(3, akun.getFirstName());
            statement.setString(4, akun.getLastName());
            statement.setInt(5, akun.isActive() ? 1 : 0);
            statement.setString(6, akun.getProfileImageName());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    akun.setUserId(keys.getInt(1));
                }
            }
        }

        return akun;
    }

    public boolean cekEmailTerdaftar(String email) throws SQLException {
        return cariAkunByEmail(email) != null;
    }

    public Akun cariAkunByEmail(String email) throws SQLException {
        String sql = """
                SELECT user_id, email, password, first_name, last_name, active, signup_datetime, profile_image_name
                FROM user_account
                WHERE email = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRowToAkun(resultSet);
                }
            }
        }

        return null;
    }

    public Akun cariAkunById(int userId) throws SQLException {
        String sql = """
                SELECT user_id, email, password, first_name, last_name, active, signup_datetime, profile_image_name
                FROM user_account
                WHERE user_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRowToAkun(resultSet);
                }
            }
        }

        return null;
    }

    private Akun mapRowToAkun(ResultSet resultSet) throws SQLException {
        return new Akun(
                resultSet.getInt("user_id"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getInt("active") == 1,
                resultSet.getString("signup_datetime"),
                resultSet.getString("profile_image_name")
        );
    }
}
