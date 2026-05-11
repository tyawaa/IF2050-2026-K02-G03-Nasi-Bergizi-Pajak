package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.RecipeIngredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeIngredientDAO {

    public void insert(
            int recipeId,
            int ingredientId,
            double amount,
            String unit
    ) {

        String sql = """
            INSERT INTO recipe_ingredient(
                recipe_id,
                ingredient_id,
                amount,
                unit
            )
            VALUES (?, ?, ?, ?)
        """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, recipeId);
            ps.setInt(2, ingredientId);
            ps.setDouble(3, amount);
            ps.setString(4, unit);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteByRecipe(int recipeId) {

        String sql = """
            DELETE FROM recipe_ingredient
            WHERE recipe_id = ?
        """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, recipeId);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RecipeIngredient>
    getRecipeIngredientsByRecipeId(int recipeId) {

        List<RecipeIngredient> list =
                new ArrayList<>();

        String sql = """
            SELECT
                ri.recipe_ingredient_id,
                ri.recipe_id,
                ri.ingredient_id,
                ri.amount,
                ri.unit,
                i.name
            FROM recipe_ingredient ri
            JOIN ingredient i
                ON ri.ingredient_id = i.ingredient_id
            WHERE ri.recipe_id = ?
        """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, recipeId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                RecipeIngredient ri =
                        new RecipeIngredient();

                ri.setRecipeIngredientId(
                        rs.getInt("recipe_ingredient_id")
                );

                ri.setRecipeId(
                        rs.getInt("recipe_id")
                );

                ri.setIngredientId(
                        rs.getInt("ingredient_id")
                );

                ri.setIngredientName(
                        rs.getString("name")
                );

                ri.setAmount(
                        rs.getDouble("amount")
                );

                ri.setUnit(
                        rs.getString("unit")
                );

                list.add(ri);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}