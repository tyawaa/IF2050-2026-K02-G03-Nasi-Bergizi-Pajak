package nasi_bergizi_pajak.service;

import nasi_bergizi_pajak.dao.KitchenStockDAO;
import nasi_bergizi_pajak.model.KitchenStock;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class KitchenStockService {
    private final KitchenStockDAO stockDAO;

    public KitchenStockService() {
        this.stockDAO = new KitchenStockDAO();
    }

    public boolean addStock(KitchenStock stock) throws SQLException {
        if (stock.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        return stockDAO.addStock(stock);
    }

    public boolean updateStock(KitchenStock stock) throws SQLException {
        if (stock.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        return stockDAO.updateStock(stock);
    }

    public boolean deleteStock(int stockId, int userId) throws SQLException {
        return stockDAO.deleteStock(stockId, userId);
    }

    public KitchenStock getStockById(int stockId, int userId) throws SQLException {
        return stockDAO.getStockById(stockId, userId);
    }

    public List<KitchenStock> getAllStockByUser(int userId) throws SQLException {
        return stockDAO.getAllStockByUser(userId);
    }

    public List<KitchenStock> cekKadaluarsaStok(int userId, int daysThreshold) throws SQLException {
        return stockDAO.getExpiringStock(userId, daysThreshold);
    }

    public List<KitchenStock> getExpiredStock(int userId) throws SQLException {
        return stockDAO.getExpiredStock(userId);
    }

    public List<KitchenStock> getLowStock(int userId, double threshold) throws SQLException {
        return stockDAO.getLowStock(userId, threshold);
    }

    public boolean updateQuantity(int stockId, int userId, double newQuantity) throws SQLException {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        return stockDAO.updateQuantity(stockId, userId, newQuantity);
    }

    public List<KitchenStock> getCriticalStock(int userId, int expiryDaysThreshold, double quantityThreshold) throws SQLException {
        List<KitchenStock> expiringSoon = cekKadaluarsaStok(userId, expiryDaysThreshold);
        List<KitchenStock> lowQuantity = getLowStock(userId, quantityThreshold);
        
        for (KitchenStock stock : lowQuantity) {
            if (!expiringSoon.contains(stock)) {
                expiringSoon.add(stock);
            }
        }
        
        return expiringSoon;
    }

    public String getStockStatus(KitchenStock stock) {
        if (stock == null) return "Unknown";
        
        if (stock.isExpired()) {
            return "Expired";
        } else if (stock.isExpiringSoon(3)) {
            return "Expiring Soon";
        } else if (stock.getQuantity() <= 1.0) {
            return "Low Stock";
        } else {
            return "Good";
        }
    }

    public boolean hasCriticalItems(int userId) throws SQLException {
        List<KitchenStock> criticalItems = getCriticalStock(userId, 7, 1.0);
        return !criticalItems.isEmpty();
    }

    public int getExpiringCount(int userId, int days) throws SQLException {
        return cekKadaluarsaStok(userId, days).size();
    }

    public int getExpiredCount(int userId) throws SQLException {
        return getExpiredStock(userId).size();
    }

    public int getLowStockCount(int userId, double threshold) throws SQLException {
        return getLowStock(userId, threshold).size();
    }
}
