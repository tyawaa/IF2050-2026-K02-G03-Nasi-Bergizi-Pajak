package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.IngredientPrice;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IngredientPriceDAO {
    public IngredientPriceDAO() {
    }

    public boolean addPrice(IngredientPrice price) throws SQLException {
        String sql = "INSERT INTO ingredient_price (ingredient_id, price, effective_date) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, price.getIngredientId());
            pstmt.setDouble(2, price.getPrice());
            pstmt.setString(3, price.getEffectiveDate().toString());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    price.setPriceId(generatedKeys.getInt(1));
                    return true;
                }
            }
            return false;
        }
    }

    public boolean updatePrice(IngredientPrice price) throws SQLException {
        String sql = "UPDATE ingredient_price SET price = ?, effective_date = ? WHERE price_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, price.getPrice());
            pstmt.setString(2, price.getEffectiveDate().toString());
            pstmt.setInt(3, price.getPriceId());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deletePrice(int priceId) throws SQLException {
        String sql = "DELETE FROM ingredient_price WHERE price_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, priceId);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    public IngredientPrice getPriceById(int priceId) throws SQLException {
        String sql = "SELECT ip.*, i.name as ingredient_name " +
                    "FROM ingredient_price ip " +
                    "JOIN ingredient i ON ip.ingredient_id = i.ingredient_id " +
                    "WHERE ip.price_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, priceId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPrice(rs);
            }
        }
        return null;
    }

    public IngredientPrice getCurrentPriceByIngredient(int ingredientId) throws SQLException {
        String sql = "SELECT ip.*, i.name as ingredient_name " +
                    "FROM ingredient_price ip " +
                    "JOIN ingredient i ON ip.ingredient_id = i.ingredient_id " +
                    "WHERE ip.ingredient_id = ? " +
                    "ORDER BY ip.effective_date DESC " +
                    "LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ingredientId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPrice(rs);
            }
        }
        return null;
    }

    public List<IngredientPrice> getPriceHistoryByIngredient(int ingredientId) throws SQLException {
        String sql = "SELECT ip.*, i.name as ingredient_name " +
                    "FROM ingredient_price ip " +
                    "JOIN ingredient i ON ip.ingredient_id = i.ingredient_id " +
                    "WHERE ip.ingredient_id = ? " +
                    "ORDER BY ip.effective_date DESC";
        
        List<IngredientPrice> prices = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ingredientId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                prices.add(mapResultSetToPrice(rs));
            }
        }
        return prices;
    }

    public List<IngredientPrice> getAllPrices() throws SQLException {
        String sql = "SELECT ip.*, i.name as ingredient_name " +
                    "FROM ingredient_price ip " +
                    "JOIN ingredient i ON ip.ingredient_id = i.ingredient_id " +
                    "ORDER BY i.name ASC, ip.effective_date DESC";
        
        List<IngredientPrice> prices = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                prices.add(mapResultSetToPrice(rs));
            }
        }
        return prices;
    }

    public List<IngredientPrice> getPricesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT ip.*, i.name as ingredient_name " +
                    "FROM ingredient_price ip " +
                    "JOIN ingredient i ON ip.ingredient_id = i.ingredient_id " +
                    "WHERE ip.effective_date BETWEEN ? AND ? " +
                    "ORDER BY ip.effective_date DESC, i.name ASC";
        
        List<IngredientPrice> prices = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                prices.add(mapResultSetToPrice(rs));
            }
        }
        return prices;
    }

    private IngredientPrice mapResultSetToPrice(ResultSet rs) throws SQLException {
        IngredientPrice price = new IngredientPrice();
        price.setPriceId(rs.getInt("price_id"));
        price.setIngredientId(rs.getInt("ingredient_id"));
        price.setPrice(rs.getDouble("price"));
        price.setEffectiveDate(LocalDate.parse(rs.getString("effective_date")));
        price.setIngredientName(rs.getString("ingredient_name"));
        
        return price;
    }
}
