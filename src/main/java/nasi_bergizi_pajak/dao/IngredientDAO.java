package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConfig;
import nasi_bergizi_pajak.model.Ingredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO {
    private final DatabaseConfig dbConfig;

    public IngredientDAO(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public boolean addIngredient(Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO ingredient (name, unit) VALUES (?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, ingredient.getName());
            pstmt.setString(2, ingredient.getUnit());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    ingredient.setIngredientId(generatedKeys.getInt(1));
                    return true;
                }
            }
            return false;
        }
    }

    public boolean updateIngredient(Ingredient ingredient) throws SQLException {
        String sql = "UPDATE ingredient SET name = ?, unit = ? WHERE ingredient_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, ingredient.getName());
            pstmt.setString(2, ingredient.getUnit());
            pstmt.setInt(3, ingredient.getIngredientId());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteIngredient(int ingredientId) throws SQLException {
        String sql = "DELETE FROM ingredient WHERE ingredient_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ingredientId);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    public Ingredient getIngredientById(int ingredientId) throws SQLException {
        String sql = "SELECT i.*, ip.price as current_price, ip.effective_date " +
                    "FROM ingredient i " +
                    "LEFT JOIN ingredient_price ip ON i.ingredient_id = ip.ingredient_id " +
                    "WHERE i.ingredient_id = ? " +
                    "ORDER BY ip.effective_date DESC " +
                    "LIMIT 1";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ingredientId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToIngredient(rs);
            }
        }
        return null;
    }

    public Ingredient getIngredientByName(String name) throws SQLException {
        String sql = "SELECT i.*, ip.price as current_price, ip.effective_date " +
                    "FROM ingredient i " +
                    "LEFT JOIN ingredient_price ip ON i.ingredient_id = ip.ingredient_id " +
                    "WHERE i.name = ? " +
                    "ORDER BY ip.effective_date DESC " +
                    "LIMIT 1";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToIngredient(rs);
            }
        }
        return null;
    }

    public List<Ingredient> getAllIngredients() throws SQLException {
        String sql = "SELECT i.*, ip.price as current_price, ip.effective_date " +
                    "FROM ingredient i " +
                    "LEFT JOIN ingredient_price ip ON i.ingredient_id = ip.ingredient_id " +
                    "WHERE ip.effective_date = (" +
                    "    SELECT MAX(effective_date) " +
                    "    FROM ingredient_price ip2 " +
                    "    WHERE ip2.ingredient_id = i.ingredient_id" +
                    ") OR ip.effective_date IS NULL " +
                    "ORDER BY i.name ASC";
        
        List<Ingredient> ingredients = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ingredients.add(mapResultSetToIngredient(rs));
            }
        }
        return ingredients;
    }

    public List<Ingredient> searchIngredients(String searchTerm) throws SQLException {
        String sql = "SELECT i.*, ip.price as current_price, ip.effective_date " +
                    "FROM ingredient i " +
                    "LEFT JOIN ingredient_price ip ON i.ingredient_id = ip.ingredient_id " +
                    "WHERE i.name LIKE ? " +
                    "AND (ip.effective_date = (" +
                    "    SELECT MAX(effective_date) " +
                    "    FROM ingredient_price ip2 " +
                    "    WHERE ip2.ingredient_id = i.ingredient_id" +
                    ") OR ip.effective_date IS NULL) " +
                    "ORDER BY i.name ASC";
        
        List<Ingredient> ingredients = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + searchTerm + "%");
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ingredients.add(mapResultSetToIngredient(rs));
            }
        }
        return ingredients;
    }

    private Ingredient mapResultSetToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setIngredientId(rs.getInt("ingredient_id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setUnit(rs.getString("unit"));
        
        double price = rs.getDouble("current_price");
        if (!rs.wasNull()) {
            ingredient.setCurrentPrice(price);
            String effectiveDateStr = rs.getString("effective_date");
            if (effectiveDateStr != null && !effectiveDateStr.isEmpty()) {
                ingredient.setPriceEffectiveDate(java.time.LocalDate.parse(effectiveDateStr));
            }
        }
        
        return ingredient;
    }
}
