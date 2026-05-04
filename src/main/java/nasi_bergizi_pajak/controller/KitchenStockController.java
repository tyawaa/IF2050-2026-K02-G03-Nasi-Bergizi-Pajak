package nasi_bergizi_pajak.controller;

import nasi_bergizi_pajak.config.DatabaseConfig;
import nasi_bergizi_pajak.model.KitchenStock;
import nasi_bergizi_pajak.service.KitchenStockService;

import java.sql.SQLException;
import java.util.List;

public class KitchenStockController {
    private final KitchenStockService stockService;

    public KitchenStockController(DatabaseConfig dbConfig) {
        this.stockService = new KitchenStockService(dbConfig);
    }

    public boolean addStock(KitchenStock stock) throws SQLException {
        try {
            return stockService.addStock(stock);
        } catch (SQLException e) {
            throw new SQLException("Failed to add stock: " + e.getMessage(), e);
        }
    }

    public boolean updateStock(KitchenStock stock) throws SQLException {
        try {
            return stockService.updateStock(stock);
        } catch (SQLException e) {
            throw new SQLException("Failed to update stock: " + e.getMessage(), e);
        }
    }

    public boolean deleteStock(int stockId, int userId) throws SQLException {
        try {
            return stockService.deleteStock(stockId, userId);
        } catch (SQLException e) {
            throw new SQLException("Failed to delete stock: " + e.getMessage(), e);
        }
    }

    public KitchenStock getStockById(int stockId, int userId) throws SQLException {
        try {
            return stockService.getStockById(stockId, userId);
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve stock: " + e.getMessage(), e);
        }
    }

    public List<KitchenStock> getAllStockByUser(int userId) throws SQLException {
        try {
            return stockService.getAllStockByUser(userId);
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve all stocks: " + e.getMessage(), e);
        }
    }

    public List<KitchenStock> getExpiringStock(int userId, int daysThreshold) throws SQLException {
        try {
            return stockService.cekKadaluarsaStok(userId, daysThreshold);
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve expiring stock: " + e.getMessage(), e);
        }
    }

    public List<KitchenStock> getExpiredStock(int userId) throws SQLException {
        try {
            return stockService.getExpiredStock(userId);
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve expired stock: " + e.getMessage(), e);
        }
    }

    public List<KitchenStock> getLowStock(int userId, double threshold) throws SQLException {
        try {
            return stockService.getLowStock(userId, threshold);
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve low stock: " + e.getMessage(), e);
        }
    }

    public boolean updateQuantity(int stockId, int userId, double newQuantity) throws SQLException {
        try {
            return stockService.updateQuantity(stockId, userId, newQuantity);
        } catch (SQLException e) {
            throw new SQLException("Failed to update quantity: " + e.getMessage(), e);
        }
    }

    public List<KitchenStock> getCriticalStock(int userId, int expiryDaysThreshold, double quantityThreshold) throws SQLException {
        try {
            return stockService.getCriticalStock(userId, expiryDaysThreshold, quantityThreshold);
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve critical stock: " + e.getMessage(), e);
        }
    }

    public String getStockStatus(KitchenStock stock) {
        return stockService.getStockStatus(stock);
    }

    public boolean hasCriticalItems(int userId) throws SQLException {
        try {
            return stockService.hasCriticalItems(userId);
        } catch (SQLException e) {
            throw new SQLException("Failed to check critical items: " + e.getMessage(), e);
        }
    }

    public int getExpiringCount(int userId, int days) throws SQLException {
        try {
            return stockService.getExpiringCount(userId, days);
        } catch (SQLException e) {
            throw new SQLException("Failed to get expiring count: " + e.getMessage(), e);
        }
    }

    public int getExpiredCount(int userId) throws SQLException {
        try {
            return stockService.getExpiredCount(userId);
        } catch (SQLException e) {
            throw new SQLException("Failed to get expired count: " + e.getMessage(), e);
        }
    }

    public int getLowStockCount(int userId, double threshold) throws SQLException {
        try {
            return stockService.getLowStockCount(userId, threshold);
        } catch (SQLException e) {
            throw new SQLException("Failed to get low stock count: " + e.getMessage(), e);
        }
    }

    public String[] getStockNotifications(int userId) throws SQLException {
        List<String> notifications = new java.util.ArrayList<>();
        
        List<KitchenStock> expired = getExpiredStock(userId);
        if (!expired.isEmpty()) {
            notifications.add(expired.size() + " item(s) expired");
        }
        
        List<KitchenStock> expiringSoon = getExpiringStock(userId, 3);
        if (!expiringSoon.isEmpty()) {
            notifications.add(expiringSoon.size() + " item(s) expiring within 3 days");
        }
        
        List<KitchenStock> lowStock = getLowStock(userId, 1.0);
        if (!lowStock.isEmpty()) {
            notifications.add(lowStock.size() + " item(s) with low quantity");
        }
        
        return notifications.toArray(new String[0]);
    }
}
