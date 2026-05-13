package nasi_bergizi_pajak.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.IngredientPrice;

public class IngredientPriceDAO {

    public List<IngredientPrice> listAllPriceHistory() {
        List<IngredientPrice> histories = new ArrayList<>();
        String query = """
                SELECT price_id, ingredient_id, price, effective_date
                FROM ingredient_price
                ORDER BY ingredient_id, effective_date DESC, price_id DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                histories.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat riwayat harga.", e);
        }
        return histories;
    }

    public List<IngredientPrice> listPriceHistoryByIngredientId(int ingredientId) {
        List<IngredientPrice> histories = new ArrayList<>();
        String query = """
                SELECT price_id, ingredient_id, price, effective_date
                FROM ingredient_price
                WHERE ingredient_id = ?
                ORDER BY effective_date DESC, price_id DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    histories.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat riwayat harga bahan.", e);
        }
        return histories;
    }

    public void insertPrice(int ingredientId, double price, LocalDate effectiveDate) {
        String query = "INSERT INTO ingredient_price (ingredient_id, price, effective_date) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            stmt.setDouble(2, price);
            stmt.setDate(3, Date.valueOf(effectiveDate));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan harga bahan.", e);
        }
    }

    private IngredientPrice mapRow(ResultSet rs) throws SQLException {
        return new IngredientPrice(
                rs.getInt("price_id"),
                rs.getInt("ingredient_id"),
                rs.getDouble("price"),
                rs.getDate("effective_date").toLocalDate()
        );
    }
}
