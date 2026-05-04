package nasi_bergizi_pajak.service;

import nasi_bergizi_pajak.config.DatabaseConfig;
import nasi_bergizi_pajak.dao.KitchenStockDAO;
import nasi_bergizi_pajak.model.KitchenStock;
import nasi_bergizi_pajak.util.NotificationUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final KitchenStockDAO stockDAO;

    public NotificationService(DatabaseConfig dbConfig) {
        this.stockDAO = new KitchenStockDAO(dbConfig);
    }

    public List<NotificationUtil.Notification> getAllNotificationsForUser(int userId) throws SQLException {
        List<KitchenStock> stocks = stockDAO.getAllStockByUser(userId);
        return NotificationUtil.generateStockNotifications(stocks);
    }

    public List<NotificationUtil.Notification> getCriticalNotificationsForUser(int userId) throws SQLException {
        List<NotificationUtil.Notification> allNotifications = getAllNotificationsForUser(userId);
        return NotificationUtil.filterNotificationsBySeverity(allNotifications, "critical");
    }

    public List<NotificationUtil.Notification> getWarningNotificationsForUser(int userId) throws SQLException {
        List<NotificationUtil.Notification> allNotifications = getAllNotificationsForUser(userId);
        return NotificationUtil.filterNotificationsBySeverity(allNotifications, "warning");
    }

    public List<NotificationUtil.Notification> getInfoNotificationsForUser(int userId) throws SQLException {
        List<NotificationUtil.Notification> allNotifications = getAllNotificationsForUser(userId);
        return NotificationUtil.filterNotificationsBySeverity(allNotifications, "info");
    }

    public List<String> getNotificationMessagesForUser(int userId) throws SQLException {
        List<NotificationUtil.Notification> notifications = getAllNotificationsForUser(userId);
        return NotificationUtil.getNotificationMessages(notifications);
    }

    public String getNotificationSummaryForUser(int userId) throws SQLException {
        List<NotificationUtil.Notification> notifications = getAllNotificationsForUser(userId);
        return NotificationUtil.getNotificationSummary(notifications);
    }

    public boolean hasCriticalNotificationsForUser(int userId) throws SQLException {
        List<NotificationUtil.Notification> notifications = getAllNotificationsForUser(userId);
        return NotificationUtil.hasCriticalNotifications(notifications);
    }

    public List<NotificationUtil.Notification> getExpiringSoonNotifications(int userId, int days) throws SQLException {
        List<KitchenStock> expiringStocks = stockDAO.getExpiringStock(userId, days);
        List<NotificationUtil.Notification> notifications = new ArrayList<>();

        for (KitchenStock stock : expiringStocks) {
            if (!stock.isExpired()) {
                notifications.add(new NotificationUtil.Notification(
                    "STOCK_EXPIRING_SOON",
                    stock.getIngredientName() + " expires in " + stock.getDaysUntilExpiry() + " days",
                    "warning"
                ));
            }
        }

        return notifications;
    }

    public List<NotificationUtil.Notification> getExpiredNotifications(int userId) throws SQLException {
        List<KitchenStock> expiredStocks = stockDAO.getExpiredStock(userId);
        List<NotificationUtil.Notification> notifications = new ArrayList<>();

        for (KitchenStock stock : expiredStocks) {
            notifications.add(new NotificationUtil.Notification(
                "STOCK_EXPIRED",
                stock.getIngredientName() + " has expired",
                "critical"
            ));
        }

        return notifications;
    }

    public List<NotificationUtil.Notification> getLowStockNotifications(int userId, double threshold) throws SQLException {
        List<KitchenStock> lowStockItems = stockDAO.getLowStock(userId, threshold);
        List<NotificationUtil.Notification> notifications = new ArrayList<>();

        for (KitchenStock stock : lowStockItems) {
            notifications.add(new NotificationUtil.Notification(
                "LOW_STOCK",
                stock.getIngredientName() + " quantity is low (" + stock.getQuantity() + " " + stock.getUnit() + ")",
                "info"
            ));
        }

        return notifications;
    }

    public List<NotificationUtil.Notification> getCriticalStockNotifications(int userId, int expiryDays, double quantityThreshold) throws SQLException {
        List<KitchenStock> criticalStocks = stockDAO.getExpiringStock(userId, expiryDays);
        List<KitchenStock> lowStockItems = stockDAO.getLowStock(userId, quantityThreshold);
        
        List<NotificationUtil.Notification> notifications = new ArrayList<>();

        for (KitchenStock stock : criticalStocks) {
            if (stock.isExpired()) {
                notifications.add(new NotificationUtil.Notification(
                    "CRITICAL_STOCK_EXPIRED",
                    stock.getIngredientName() + " has expired",
                    "critical"
                ));
            } else {
                notifications.add(new NotificationUtil.Notification(
                    "CRITICAL_STOCK_EXPIRING",
                    stock.getIngredientName() + " expires in " + stock.getDaysUntilExpiry() + " days",
                    "critical"
                ));
            }
        }

        for (KitchenStock stock : lowStockItems) {
            if (!criticalStocks.contains(stock)) {
                notifications.add(new NotificationUtil.Notification(
                    "CRITICAL_LOW_STOCK",
                    stock.getIngredientName() + " quantity is critically low (" + stock.getQuantity() + " " + stock.getUnit() + ")",
                    "critical"
                ));
            }
        }

        return notifications;
    }

    public int getNotificationCountForUser(int userId) throws SQLException {
        return getAllNotificationsForUser(userId).size();
    }

    public int getCriticalNotificationCountForUser(int userId) throws SQLException {
        return getCriticalNotificationsForUser(userId).size();
    }

    public int getWarningNotificationCountForUser(int userId) throws SQLException {
        return getWarningNotificationsForUser(userId).size();
    }

    public int getInfoNotificationCountForUser(int userId) throws SQLException {
        return getInfoNotificationsForUser(userId).size();
    }

    public NotificationUtil.Notification createCustomNotification(String type, String message, String severity) {
        return NotificationUtil.createCustomNotification(type, message, severity);
    }

    public NotificationUtil.Notification createCustomNotification(String type, String message, String severity, java.time.LocalDate date) {
        return NotificationUtil.createCustomNotification(type, message, severity, date);
    }

    public List<String> getDashboardNotifications(int userId) throws SQLException {
        List<String> dashboardNotifications = new ArrayList<>();

        List<NotificationUtil.Notification> criticalNotifications = getCriticalNotificationsForUser(userId);
        if (!criticalNotifications.isEmpty()) {
            dashboardNotifications.add("🔴 " + criticalNotifications.size() + " critical alert(s)");
        }

        List<NotificationUtil.Notification> warningNotifications = getWarningNotificationsForUser(userId);
        if (!warningNotifications.isEmpty()) {
            dashboardNotifications.add("🟡 " + warningNotifications.size() + " warning(s)");
        }

        List<NotificationUtil.Notification> infoNotifications = getInfoNotificationsForUser(userId);
        if (!infoNotifications.isEmpty()) {
            dashboardNotifications.add("🔵 " + infoNotifications.size() + " info notification(s)");
        }

        if (dashboardNotifications.isEmpty()) {
            dashboardNotifications.add("✅ No notifications");
        }

        return dashboardNotifications;
    }
}
