package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.Recipe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeDAO {

    public List<Recipe> listAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        String query = "SELECT * FROM recipe";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Recipe recipe = new Recipe();
                recipe.setRecipeId(rs.getInt("recipe_id"));
                recipe.setName(rs.getString("name"));
                recipe.setDescription(rs.getString("description"));
                recipe.setServingSize(rs.getInt("serving_size"));
                recipe.setStatus(rs.getString("status"));
                recipes.add(recipe);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat daftar resep.", e);
        }
        return recipes;
    }

    public int insertRecipe(Recipe recipe) {
        String query = "INSERT INTO recipe (name, description, serving_size, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, recipe.getName());
            stmt.setString(2, recipe.getDescription());
            stmt.setInt(3, recipe.getServingSize());
            stmt.setString(4, recipe.getStatus());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int recipeId = generatedKeys.getInt(1);
                    recipe.setRecipeId(recipeId);
                    return recipeId;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan resep.", e);
        }
        throw new IllegalStateException("Gagal mengambil ID resep baru.");
    }

    public void updateRecipe(Recipe recipe) {
        String query = "UPDATE recipe SET name = ?, description = ?, serving_size = ?, status = ? WHERE recipe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, recipe.getName());
            stmt.setString(2, recipe.getDescription());
            stmt.setInt(3, recipe.getServingSize());
            stmt.setString(4, recipe.getStatus());
            stmt.setInt(5, recipe.getRecipeId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memperbarui resep.", e);
        }
    }

    public int saveRecipeWithIngredients(Recipe recipe, List<nasi_bergizi_pajak.model.RecipeIngredient> ingredients) {
        String insertRecipe = "INSERT INTO recipe (name, description, serving_size, status) VALUES (?, ?, ?, ?)";
        String updateRecipe = "UPDATE recipe SET name = ?, description = ?, serving_size = ?, status = ? WHERE recipe_id = ?";
        String deleteIngredients = "DELETE FROM recipe_ingredient WHERE recipe_id = ?";
        String insertIngredient = "INSERT INTO recipe_ingredient (recipe_id, ingredient_id, amount, unit) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int recipeId = recipe.getRecipeId();
                if (recipeId > 0) {
                    try (PreparedStatement stmt = conn.prepareStatement(updateRecipe)) {
                        stmt.setString(1, recipe.getName());
                        stmt.setString(2, recipe.getDescription());
                        stmt.setInt(3, recipe.getServingSize());
                        stmt.setString(4, recipe.getStatus());
                        stmt.setInt(5, recipeId);
                        stmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement stmt = conn.prepareStatement(insertRecipe, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, recipe.getName());
                        stmt.setString(2, recipe.getDescription());
                        stmt.setInt(3, recipe.getServingSize());
                        stmt.setString(4, recipe.getStatus());
                        stmt.executeUpdate();
                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException("Gagal mengambil ID resep baru.");
                            }
                            recipeId = keys.getInt(1);
                            recipe.setRecipeId(recipeId);
                        }
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(deleteIngredients)) {
                    stmt.setInt(1, recipeId);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(insertIngredient)) {
                    for (nasi_bergizi_pajak.model.RecipeIngredient ingredient : ingredients) {
                        stmt.setInt(1, recipeId);
                        stmt.setInt(2, ingredient.getIngredientId());
                        stmt.setDouble(3, ingredient.getQuantity());
                        stmt.setString(4, ingredient.getUnit());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                conn.commit();
                return recipeId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan resep dan bahan resep.", e);
        }
    }

    public void deleteRecipe(int recipeId) {
        String query = "DELETE FROM recipe WHERE recipe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, recipeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghapus resep.", e);
        }
    }

    public Recipe getRecipeById(int recipeId) {
        String query = "SELECT * FROM recipe WHERE recipe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, recipeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Recipe recipe = new Recipe();
                    recipe.setRecipeId(rs.getInt("recipe_id"));
                    recipe.setName(rs.getString("name"));
                    recipe.setDescription(rs.getString("description"));
                    recipe.setServingSize(rs.getInt("serving_size"));
                    recipe.setStatus(rs.getString("status"));
                    return recipe;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memuat resep.", e);
        }
        return null;
    }
}
