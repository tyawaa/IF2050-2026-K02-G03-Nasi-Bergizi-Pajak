package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.RecipeIngredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeIngredientDAO {

    public List<RecipeIngredient> listAllRecipeIngredients() {
        List<RecipeIngredient> recipeIngredients = new ArrayList<>();
        String query = "SELECT * FROM recipe_ingredient";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                RecipeIngredient ri = new RecipeIngredient();
                ri.setRecipeIngredientId(rs.getInt("recipe_ingredient_id"));
                ri.setRecipeId(rs.getInt("recipe_id"));
                ri.setIngredientId(rs.getInt("ingredient_id"));
                ri.setQuantity(rs.getDouble("quantity"));
                recipeIngredients.add(ri);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recipeIngredients;
    }

    public void insertRecipeIngredient(RecipeIngredient ri) {
        String query = "INSERT INTO recipe_ingredient (recipe_id, ingredient_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ri.getRecipeId());
            stmt.setInt(2, ri.getIngredientId());
            stmt.setDouble(3, ri.getQuantity());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRecipeIngredient(RecipeIngredient ri) {
        String query = "UPDATE recipe_ingredient SET recipe_id = ?, ingredient_id = ?, quantity = ? WHERE recipe_ingredient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ri.getRecipeId());
            stmt.setInt(2, ri.getIngredientId());
            stmt.setDouble(3, ri.getQuantity());
            stmt.setInt(4, ri.getRecipeIngredientId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRecipeIngredient(int recipeIngredientId) {
        String query = "DELETE FROM recipe_ingredient WHERE recipe_ingredient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, recipeIngredientId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<RecipeIngredient> getRecipeIngredientsByRecipeId(int recipeId) {
        List<RecipeIngredient> recipeIngredients = new ArrayList<>();
        String query = "SELECT * FROM recipe_ingredient WHERE recipe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, recipeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RecipeIngredient ri = new RecipeIngredient();
                    ri.setRecipeIngredientId(rs.getInt("recipe_ingredient_id"));
                    ri.setRecipeId(rs.getInt("recipe_id"));
                    ri.setIngredientId(rs.getInt("ingredient_id"));
                    ri.setQuantity(rs.getDouble("quantity"));
                    recipeIngredients.add(ri);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recipeIngredients;
    }
}