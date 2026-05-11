package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.IngredientNutrition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class IngredientNutritionDAO {

    public IngredientNutrition getNutritionByIngredientId(int ingredientId) {
        String sql = "SELECT * FROM ingredient_nutrition WHERE ingredient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ingredientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}