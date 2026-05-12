package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.Ingredient;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO {
    private static final String SELECT_WITH_LATEST_PRICE = """
            SELECT i.ingredient_id,
                   i.name,
                   i.unit,
                   (
                       SELECT ip.price
                       FROM ingredient_price ip
                       WHERE ip.ingredient_id = i.ingredient_id
                         AND ip.effective_date <= CURDATE()
                       ORDER BY ip.effective_date DESC, ip.price_id DESC
                       LIMIT 1
                   ) AS current_price,
                   (
                       SELECT ip.effective_date
                       FROM ingredient_price ip
                       WHERE ip.ingredient_id = i.ingredient_id
                         AND ip.effective_date <= CURDATE()
                       ORDER BY ip.effective_date DESC, ip.price_id DESC
                       LIMIT 1
                   ) AS effective_date
            FROM ingredient i
            """;

    public boolean addIngredient(Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO ingredient (name, unit) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ingredient.getName());
            stmt.setString(2, ingredient.getUnit());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ingredient.setIngredientId(generatedKeys.getInt(1));
                }
            }
            return true;
        }
    }

    public int insertIngredient(Ingredient ingredient) {
        String ingredientQuery = "INSERT INTO ingredient (name, unit) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(ingredientQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, ingredient.getName());
                stmt.setString(2, ingredient.getUnit());
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int ingredientId = generatedKeys.getInt(1);
                        ingredient.setIngredientId(ingredientId);
                        insertIngredientPrice(conn, ingredientId, ingredient.getPricePerUnit(), LocalDate.now());
                        conn.commit();
                        return ingredientId;
                    }
                }

                throw new SQLException("Gagal mengambil ID bahan baru.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan bahan.", e);
        }
    }

    public boolean updateIngredient(Ingredient ingredient) throws SQLException {
        String sql = "UPDATE ingredient SET name = ?, unit = ? WHERE ingredient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ingredient.getName());
            stmt.setString(2, ingredient.getUnit());
            stmt.setInt(3, ingredient.getIngredientId());
            return stmt.executeUpdate() > 0;
        }
    }

    public void updateIngredientDetails(Ingredient ingredient) {
        try {
            updateIngredient(ingredient);
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memperbarui bahan.", e);
        }
    }

    public boolean deleteIngredient(int ingredientId) throws SQLException {
        String sql = "DELETE FROM ingredient WHERE ingredient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ingredientId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Ingredient getIngredientById(int ingredientId) throws SQLException {
        String sql = SELECT_WITH_LATEST_PRICE + " WHERE i.ingredient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ingredientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToIngredient(rs);
                }
            }
        }
        return null;
    }

    public Ingredient getIngredientByName(String name) throws SQLException {
        String sql = SELECT_WITH_LATEST_PRICE + " WHERE i.name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToIngredient(rs);
                }
            }
        }
        return null;
    }

    public List<Ingredient> getAllIngredients() throws SQLException {
        String sql = SELECT_WITH_LATEST_PRICE + " ORDER BY i.name ASC";
        List<Ingredient> ingredients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ingredients.add(mapResultSetToIngredient(rs));
            }
        }
        return ingredients;
    }

    public List<Ingredient> listAllIngredients() {
        try {
            return getAllIngredients();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat daftar bahan.", e);
        }
    }

    public List<Ingredient> searchIngredients(String searchTerm) throws SQLException {
        String sql = SELECT_WITH_LATEST_PRICE + " WHERE i.name LIKE ? ORDER BY i.name ASC";
        List<Ingredient> ingredients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ingredients.add(mapResultSetToIngredient(rs));
                }
            }
        }
        return ingredients;
    }

    public void addIngredientPrice(int ingredientId, double price, LocalDate effectiveDate) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            insertIngredientPrice(conn, ingredientId, price, effectiveDate);
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan harga bahan.", e);
        }
    }

    private void insertIngredientPrice(Connection conn, int ingredientId, double price, LocalDate effectiveDate)
            throws SQLException {
        String query = "INSERT INTO ingredient_price (ingredient_id, price, effective_date) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            stmt.setDouble(2, price);
            stmt.setDate(3, Date.valueOf(effectiveDate));
            stmt.executeUpdate();
        }
    }

    private Ingredient mapResultSetToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setIngredientId(rs.getInt("ingredient_id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setUnit(rs.getString("unit"));

        double price = rs.getDouble("current_price");
        if (!rs.wasNull()) {
            ingredient.setPricePerUnit(price);
            Date effectiveDate = rs.getDate("effective_date");
            if (effectiveDate != null) {
                ingredient.setPriceEffectiveDate(effectiveDate.toLocalDate());
            }
        }

        return ingredient;
    }
}
