package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.KitchenStock;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KitchenStockDAO {
    public KitchenStockDAO() {
    }

    public boolean addStock(KitchenStock stock) throws SQLException {
        String sql = "INSERT INTO kitchen_stock (user_id, ingredient_id, quantity, initial_quantity, unit, storage_location, expiry_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, stock.getUserId());
            pstmt.setInt(2, stock.getIngredientId());
            pstmt.setDouble(3, stock.getQuantity());
            pstmt.setDouble(4, stock.getQuantity()); // initial_quantity = quantity saat pertama ditambahkan
            pstmt.setString(5, stock.getUnit());
            pstmt.setString(6, stock.getStorageLocation());
            pstmt.setDate(7, stock.getExpiryDate() != null ? Date.valueOf(stock.getExpiryDate()) : null);

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateStock(KitchenStock stock) throws SQLException {
        String sql = "UPDATE kitchen_stock SET ingredient_id = ?, quantity = ?, unit = ?, storage_location = ?, expiry_date = ? " +
                    "WHERE stock_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, stock.getIngredientId());
            pstmt.setDouble(2, stock.getQuantity());
            pstmt.setString(3, stock.getUnit());
            pstmt.setString(4, stock.getStorageLocation());
            pstmt.setDate(5, stock.getExpiryDate() != null ? Date.valueOf(stock.getExpiryDate()) : null);
            pstmt.setInt(6, stock.getStockId());
            pstmt.setInt(7, stock.getUserId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteStock(int stockId, int userId) throws SQLException {
        String sql = "DELETE FROM kitchen_stock WHERE stock_id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, stockId);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    public KitchenStock getStockById(int stockId, int userId) throws SQLException {
        String sql = "SELECT ks.*, i.name as ingredient_name " +
                    "FROM kitchen_stock ks " +
                    "JOIN ingredient i ON ks.ingredient_id = i.ingredient_id " +
                    "WHERE ks.stock_id = ? AND ks.user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, stockId);
            pstmt.setInt(2, userId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStock(rs);
            }
        }
        return null;
    }

    public List<KitchenStock> getAllStockByUser(int userId) throws SQLException {
        String sql = "SELECT ks.*, i.name as ingredient_name " +
                    "FROM kitchen_stock ks " +
                    "JOIN ingredient i ON ks.ingredient_id = i.ingredient_id " +
                    "WHERE ks.user_id = ? " +
                    "ORDER BY ks.expiry_date ASC";
        
        List<KitchenStock> stocks = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                stocks.add(mapResultSetToStock(rs));
            }
        }
        return stocks;
    }

    public List<KitchenStock> getExpiringStock(int userId, int daysThreshold) throws SQLException {
        String sql = "SELECT ks.*, i.name as ingredient_name " +
                    "FROM kitchen_stock ks " +
                    "JOIN ingredient i ON ks.ingredient_id = i.ingredient_id " +
                    "WHERE ks.user_id = ? " +
                    "AND ks.expiry_date IS NOT NULL " +
                    "AND ks.expiry_date >= CURDATE() " +
                    "AND ks.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                    "ORDER BY ks.expiry_date ASC";
        
        List<KitchenStock> stocks = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, daysThreshold);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                stocks.add(mapResultSetToStock(rs));
            }
        }
        return stocks;
    }

    public List<KitchenStock> getExpiredStock(int userId) throws SQLException {
        String sql = "SELECT ks.*, i.name as ingredient_name " +
                    "FROM kitchen_stock ks " +
                    "JOIN ingredient i ON ks.ingredient_id = i.ingredient_id " +
                    "WHERE ks.user_id = ? " +
                    "AND ks.expiry_date IS NOT NULL " +
                    "AND ks.expiry_date < CURDATE() " +
                    "ORDER BY ks.expiry_date ASC";
        
        List<KitchenStock> stocks = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                stocks.add(mapResultSetToStock(rs));
            }
        }
        return stocks;
    }

    public List<KitchenStock> getLowStock(int userId, double threshold) throws SQLException {
        // Stok rendah = quantity sudah mencapai <= 20% dari initial_quantity
        String sql = "SELECT ks.*, i.name as ingredient_name " +
                    "FROM kitchen_stock ks " +
                    "JOIN ingredient i ON ks.ingredient_id = i.ingredient_id " +
                    "WHERE ks.user_id = ? " +
                    "AND ks.initial_quantity > 0 " +
                    "AND ks.quantity <= ks.initial_quantity * 0.20 " +
                    "ORDER BY ks.quantity ASC";
        
        List<KitchenStock> stocks = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                stocks.add(mapResultSetToStock(rs));
            }
        }
        return stocks;
    }

    public KitchenStock getStockByUserAndIngredient(int userId, int ingredientId) throws SQLException {
        String sql = "SELECT ks.*, i.name as ingredient_name " +
                    "FROM kitchen_stock ks " +
                    "JOIN ingredient i ON ks.ingredient_id = i.ingredient_id " +
                    "WHERE ks.user_id = ? AND ks.ingredient_id = ? " +
                    "LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, ingredientId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStock(rs);
            }
        }
        return null;
    }

    public boolean updateQuantity(int stockId, int userId, double newQuantity) throws SQLException {
        String sql = "UPDATE kitchen_stock SET quantity = ? WHERE stock_id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, newQuantity);
            pstmt.setInt(2, stockId);
            pstmt.setInt(3, userId);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    private KitchenStock mapResultSetToStock(ResultSet rs) throws SQLException {
        KitchenStock stock = new KitchenStock();
        stock.setStockId(rs.getInt("stock_id"));
        stock.setUserId(rs.getInt("user_id"));
        stock.setIngredientId(rs.getInt("ingredient_id"));
        stock.setQuantity(rs.getDouble("quantity"));
        stock.setInitialQuantity(rs.getDouble("initial_quantity"));
        stock.setUnit(rs.getString("unit"));
        stock.setStorageLocation(rs.getString("storage_location"));
        
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            stock.setExpiryDate(expiryDate.toLocalDate());
        }
        
        stock.setIngredientName(rs.getString("ingredient_name"));
        
        return stock;
    }
}
