package nasi_bergizi_pajak.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.IngredientNutrition;

public class IngredientNutritionDAO {

    public List<IngredientNutrition> listAllNutritions() {
        List<IngredientNutrition> nutritions = new ArrayList<>();
        String query = """
                SELECT nutrition_id, ingredient_id, calories, protein, carbohydrate, fat, fibre, unit
                FROM ingredient_nutrition
                ORDER BY ingredient_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                nutritions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat data nutrisi.", e);
        }
        return nutritions;
    }

    public IngredientNutrition getByIngredientId(int ingredientId) {
        String query = """
                SELECT nutrition_id, ingredient_id, calories, protein, carbohydrate, fat, fibre, unit
                FROM ingredient_nutrition
                WHERE ingredient_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat nutrisi bahan.", e);
        }
        return null;
    }

    public void upsertNutrition(IngredientNutrition nutrition) {
        String query = """
                INSERT INTO ingredient_nutrition
                    (ingredient_id, calories, protein, carbohydrate, fat, fibre, unit)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    calories = VALUES(calories),
                    protein = VALUES(protein),
                    carbohydrate = VALUES(carbohydrate),
                    fat = VALUES(fat),
                    fibre = VALUES(fibre),
                    unit = VALUES(unit)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, nutrition.getIngredientId());
            stmt.setDouble(2, nutrition.getCalories());
            stmt.setDouble(3, nutrition.getProtein());
            stmt.setDouble(4, nutrition.getCarbohydrate());
            stmt.setDouble(5, nutrition.getFat());
            stmt.setDouble(6, nutrition.getFibre());
            stmt.setString(7, nutrition.getUnit());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan nutrisi bahan.", e);
        }
    }

    private IngredientNutrition mapRow(ResultSet rs) throws SQLException {
        return new IngredientNutrition(
                rs.getInt("nutrition_id"),
                rs.getInt("ingredient_id"),
                rs.getDouble("calories"),
                rs.getDouble("protein"),
                rs.getDouble("carbohydrate"),
                rs.getDouble("fat"),
                rs.getDouble("fibre"),
                rs.getString("unit")
        );
    }
}
