package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.Ingredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO {

    public List<Ingredient> listAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredient";
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
            e.printStackTrace();
        }
        return ingredients;
    }

    public void insertIngredient(Ingredient ingredient) {
        String query = "INSERT INTO ingredient (name, unit, price_per_unit) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, ingredient.getName());
            stmt.setString(2, ingredient.getUnit());
            stmt.setDouble(3, ingredient.getPricePerUnit());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateIngredient(Ingredient ingredient) {
        String query = "UPDATE ingredient SET name = ?, unit = ?, price_per_unit = ? WHERE ingredient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, ingredient.getName());
            stmt.setString(2, ingredient.getUnit());
            stmt.setDouble(3, ingredient.getPricePerUnit());
            stmt.setInt(4, ingredient.getIngredientId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteIngredient(int ingredientId) {
        String query = "DELETE FROM ingredient WHERE ingredient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Ingredient getIngredientById(int ingredientId) {
        String query = "SELECT * FROM ingredient WHERE ingredient_id = ?";
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
            e.printStackTrace();
        }
        return null;
    }
}