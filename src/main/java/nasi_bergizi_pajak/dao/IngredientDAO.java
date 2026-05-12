package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.Ingredient;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO {
    private static final String SELECT_WITH_LATEST_PRICE = """
            SELECT i.ingredient_id,
                   i.name,
                   i.unit,
                   COALESCE((
                       SELECT ip.price
                       FROM ingredient_price ip
                       WHERE ip.ingredient_id = i.ingredient_id
                          AND ip.effective_date <= CURDATE()
                       ORDER BY ip.effective_date DESC, ip.price_id DESC
                       LIMIT 1
                   ), 0) AS price_per_unit
            FROM ingredient i
            """;

    public List<Ingredient> listAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = SELECT_WITH_LATEST_PRICE + " ORDER BY i.name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setIngredientId(rs.getInt("ingredient_id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setUnit(rs.getString("unit"));
                ingredient.setPricePerUnit(rs.getDouble("price_per_unit"));
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat daftar bahan.", e);
        }
        return ingredients;
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

    public void updateIngredient(Ingredient ingredient) {
        updateIngredientDetails(ingredient);
        addIngredientPrice(ingredient.getIngredientId(), ingredient.getPricePerUnit(), LocalDate.now());
    }

    public void updateIngredientDetails(Ingredient ingredient) {
        String query = "UPDATE ingredient SET name = ?, unit = ? WHERE ingredient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, ingredient.getName());
            stmt.setString(2, ingredient.getUnit());
            stmt.setInt(3, ingredient.getIngredientId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memperbarui bahan.", e);
        }
    }

    public void deleteIngredient(int ingredientId) {
        String query = "DELETE FROM ingredient WHERE ingredient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghapus bahan. Bahan mungkin masih dipakai pada resep.", e);
        }
    }

    public Ingredient getIngredientById(int ingredientId) {
        String query = SELECT_WITH_LATEST_PRICE + " WHERE i.ingredient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setIngredientId(rs.getInt("ingredient_id"));
                    ingredient.setName(rs.getString("name"));
                    ingredient.setUnit(rs.getString("unit"));
                    ingredient.setPricePerUnit(rs.getDouble("price_per_unit"));
                    return ingredient;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat bahan.", e);
        }
        return null;
    }

    public void addIngredientPrice(int ingredientId, double price, LocalDate effectiveDate) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            insertIngredientPrice(conn, ingredientId, price, effectiveDate);
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan harga bahan.", e);
        }
    }

    private void insertIngredientPrice(Connection conn, int ingredientId, double price, LocalDate effectiveDate) throws SQLException {
        String query = "INSERT INTO ingredient_price (ingredient_id, price, effective_date) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            stmt.setDouble(2, price);
            stmt.setDate(3, Date.valueOf(effectiveDate));
            stmt.executeUpdate();
        }
    }
}
