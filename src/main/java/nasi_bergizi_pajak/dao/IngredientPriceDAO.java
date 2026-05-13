package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.IngredientPrice;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IngredientPriceDAO {
    public boolean addPrice(IngredientPrice price) throws SQLException {
        String sql = "INSERT INTO ingredient_price (ingredient_id, price, effective_date) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, price.getIngredientId());
            stmt.setDouble(2, price.getPrice());
            stmt.setDate(3, Date.valueOf(price.getEffectiveDate()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    price.setPriceId(generatedKeys.getInt(1));
                }
            }
            return true;
        }
    }

    public boolean updatePrice(IngredientPrice price) throws SQLException {
        String sql = "UPDATE ingredient_price SET price = ?, effective_date = ? WHERE price_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, price.getPrice());
            stmt.setDate(2, Date.valueOf(price.getEffectiveDate()));
            stmt.setInt(3, price.getPriceId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletePrice(int priceId) throws SQLException {
        String sql = "DELETE FROM ingredient_price WHERE price_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, priceId);
            return stmt.executeUpdate() > 0;
        }
    }

    public IngredientPrice getPriceById(int priceId) throws SQLException {
        String sql = """
                SELECT ip.*, i.name AS ingredient_name
                FROM ingredient_price ip
                JOIN ingredient i ON ip.ingredient_id = i.ingredient_id
                WHERE ip.price_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, priceId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPrice(rs);
                }
            }
        }
        return null;
    }

    public IngredientPrice getCurrentPriceByIngredient(int ingredientId) throws SQLException {
        String sql = """
                SELECT ip.*, i.name AS ingredient_name
                FROM ingredient_price ip
                JOIN ingredient i ON ip.ingredient_id = i.ingredient_id
                WHERE ip.ingredient_id = ?
                  AND ip.effective_date <= CURDATE()
                ORDER BY ip.effective_date DESC, ip.price_id DESC
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ingredientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPrice(rs);
                }
            }
        }
        return null;
    }

    public List<IngredientPrice> getPriceHistoryByIngredient(int ingredientId) throws SQLException {
        String sql = """
                SELECT ip.*, i.name AS ingredient_name
                FROM ingredient_price ip
                JOIN ingredient i ON ip.ingredient_id = i.ingredient_id
                WHERE ip.ingredient_id = ?
                ORDER BY ip.effective_date DESC, ip.price_id DESC
                """;
        List<IngredientPrice> prices = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ingredientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prices.add(mapResultSetToPrice(rs));
                }
            }
        }
        return prices;
    }

    public List<IngredientPrice> getAllPrices() throws SQLException {
        String sql = """
                SELECT ip.*, i.name AS ingredient_name
                FROM ingredient_price ip
                JOIN ingredient i ON ip.ingredient_id = i.ingredient_id
                ORDER BY i.name ASC, ip.effective_date DESC, ip.price_id DESC
                """;
        List<IngredientPrice> prices = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                prices.add(mapResultSetToPrice(rs));
            }
        }
        return prices;
    }

    public List<IngredientPrice> getPricesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = """
                SELECT ip.*, i.name AS ingredient_name
                FROM ingredient_price ip
                JOIN ingredient i ON ip.ingredient_id = i.ingredient_id
                WHERE ip.effective_date BETWEEN ? AND ?
                ORDER BY ip.effective_date DESC, i.name ASC
                """;
        List<IngredientPrice> prices = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prices.add(mapResultSetToPrice(rs));
                }
            }
        }
        return prices;
    }

    public List<IngredientPrice> listAllPriceHistory() {
        try {
            return getAllPriceHistoryWithoutNames();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat riwayat harga.", e);
        }
    }

    public List<IngredientPrice> listPriceHistoryByIngredientId(int ingredientId) {
        try {
            return getPriceHistoryRowsWithoutNames("WHERE ingredient_id = ?", ingredientId);
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat riwayat harga bahan.", e);
        }
    }

    public void insertPrice(int ingredientId, double price, LocalDate effectiveDate) {
        try {
            addPrice(new IngredientPrice(ingredientId, price, effectiveDate));
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan harga bahan.", e);
        }
    }

    private List<IngredientPrice> getAllPriceHistoryWithoutNames() throws SQLException {
        return getPriceHistoryRowsWithoutNames("", null);
    }

    private List<IngredientPrice> getPriceHistoryRowsWithoutNames(String whereClause, Integer ingredientId)
            throws SQLException {
        String sql = """
                SELECT price_id, ingredient_id, price, effective_date
                FROM ingredient_price
                %s
                ORDER BY ingredient_id, effective_date DESC, price_id DESC
                """.formatted(whereClause);
        List<IngredientPrice> histories = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (ingredientId != null) {
                stmt.setInt(1, ingredientId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    histories.add(mapPriceHistoryRow(rs));
                }
            }
        }
        return histories;
    }

    private IngredientPrice mapResultSetToPrice(ResultSet rs) throws SQLException {
        IngredientPrice price = mapPriceHistoryRow(rs);
        price.setIngredientName(rs.getString("ingredient_name"));
        return price;
    }

    private IngredientPrice mapPriceHistoryRow(ResultSet rs) throws SQLException {
        Date effectiveDate = rs.getDate("effective_date");
        return new IngredientPrice(
                rs.getInt("price_id"),
                rs.getInt("ingredient_id"),
                rs.getDouble("price"),
                effectiveDate == null ? null : effectiveDate.toLocalDate()
        );
    }
}
