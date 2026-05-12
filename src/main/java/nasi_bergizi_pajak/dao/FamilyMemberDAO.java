package nasi_bergizi_pajak.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.FamilyMember;

public class FamilyMemberDAO {
    public List<FamilyMember> listByUserId(int userId) {
        List<FamilyMember> members = new ArrayList<>();
        String sql = """
                SELECT member_id, user_id, name, relationship, birth_date, height, weight, allergy
                FROM family_member
                WHERE user_id = ?
                ORDER BY member_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    members.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat profil keluarga.", e);
        }

        return members;
    }

    public int insert(FamilyMember member) {
        String sql = """
                INSERT INTO family_member (user_id, name, relationship, birth_date, height, weight, allergy)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, member);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    member.setMemberId(id);
                    return id;
                }
            }

            throw new SQLException("Gagal mengambil ID anggota keluarga baru.");
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan anggota keluarga.", e);
        }
    }

    public void update(FamilyMember member) {
        String sql = """
                UPDATE family_member
                SET user_id = ?, name = ?, relationship = ?, birth_date = ?, height = ?, weight = ?, allergy = ?
                WHERE member_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, member);
            statement.setInt(8, member.getMemberId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memperbarui anggota keluarga.", e);
        }
    }

    public void delete(int memberId, int userId) {
        String sql = "DELETE FROM family_member WHERE member_id = ? AND user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghapus anggota keluarga.", e);
        }
    }

    private void fillStatement(PreparedStatement statement, FamilyMember member) throws SQLException {
        statement.setInt(1, member.getUserId());
        statement.setString(2, member.getName());
        statement.setString(3, normalizeNullable(member.getRelationship()));
        statement.setDate(4, Date.valueOf(member.getBirthDate()));
        statement.setDouble(5, member.getHeight());
        statement.setDouble(6, member.getWeight());
        statement.setString(7, normalizeNullable(member.getAllergy()));
    }

    private FamilyMember mapRow(ResultSet resultSet) throws SQLException {
        Date birthDate = resultSet.getDate("birth_date");
        return new FamilyMember(
                resultSet.getInt("member_id"),
                resultSet.getInt("user_id"),
                resultSet.getString("name"),
                resultSet.getString("relationship"),
                birthDate == null ? null : birthDate.toLocalDate(),
                resultSet.getDouble("height"),
                resultSet.getDouble("weight"),
                resultSet.getString("allergy")
        );
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
