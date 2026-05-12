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
                ri.setQuantity(rs.getDouble("amount"));
                ri.setUnit(rs.getString("unit"));
                recipeIngredients.add(ri);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat daftar bahan resep.", e);
        }
        return recipeIngredients;
    }

    public void insertRecipeIngredient(RecipeIngredient ri) {
        String query = "INSERT INTO recipe_ingredient (recipe_id, ingredient_id, amount, unit) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ri.getRecipeId());
            stmt.setInt(2, ri.getIngredientId());
            stmt.setDouble(3, ri.getQuantity());
            stmt.setString(4, ri.getUnit());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan bahan resep.", e);
        }
    }

    public void updateRecipeIngredient(RecipeIngredient ri) {
        String query = "UPDATE recipe_ingredient SET recipe_id = ?, ingredient_id = ?, amount = ?, unit = ? WHERE recipe_ingredient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ri.getRecipeId());
            stmt.setInt(2, ri.getIngredientId());
            stmt.setDouble(3, ri.getQuantity());
            stmt.setString(4, ri.getUnit());
            stmt.setInt(5, ri.getRecipeIngredientId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memperbarui bahan resep.", e);
        }
    }

    public void deleteRecipeIngredient(int recipeIngredientId) {
        String query = "DELETE FROM recipe_ingredient WHERE recipe_ingredient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, recipeIngredientId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghapus bahan resep.", e);
        }
    }

    public void deleteRecipeIngredientsByRecipeId(int recipeId) {
        String query = "DELETE FROM recipe_ingredient WHERE recipe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, recipeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghapus daftar bahan resep.", e);
        }
    }

    public void replaceRecipeIngredients(int recipeId, List<RecipeIngredient> recipeIngredients) {
        String deleteQuery = "DELETE FROM recipe_ingredient WHERE recipe_id = ?";
        String insertQuery = "INSERT INTO recipe_ingredient (recipe_id, ingredient_id, amount, unit) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, recipeId);
                deleteStmt.executeUpdate();

                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    for (RecipeIngredient ri : recipeIngredients) {
                        insertStmt.setInt(1, recipeId);
                        insertStmt.setInt(2, ri.getIngredientId());
                        insertStmt.setDouble(3, ri.getQuantity());
                        insertStmt.setString(4, ri.getUnit());
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan daftar bahan resep.", e);
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
                    ri.setQuantity(rs.getDouble("amount"));
                    ri.setUnit(rs.getString("unit"));
                    recipeIngredients.add(ri);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat daftar bahan resep.", e);
        }
        return recipeIngredients;
    }
}
