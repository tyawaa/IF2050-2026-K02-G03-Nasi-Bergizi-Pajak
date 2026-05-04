package nasi_bergizi_pajak.util;

import nasi_bergizi_pajak.model.KitchenStock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NotificationUtil {
    
    public static class Notification {
        private final String type;
        private final String message;
        private final String severity;
        private final LocalDate date;

        public Notification(String type, String message, String severity) {
            this.type = type;
            this.message = message;
            this.severity = severity;
            this.date = LocalDate.now();
        }

        public Notification(String type, String message, String severity, LocalDate date) {
            this.type = type;
            this.message = message;
            this.severity = severity;
            this.date = date;
        }

        public String getType() { return type; }
        public String getMessage() { return message; }
        public String getSeverity() { return severity; }
        public LocalDate getDate() { return date; }

        @Override
        public String toString() {
            return "[" + severity.toUpperCase() + "] " + type + ": " + message;
        }
    }

    public static List<Notification> generateStockNotifications(List<KitchenStock> stocks) {
        List<Notification> notifications = new ArrayList<>();

        for (KitchenStock stock : stocks) {
            if (stock.isExpired()) {
                notifications.add(new Notification(
                    "STOCK_EXPIRED",
                    stock.getIngredientName() + " has expired",
                    "critical"
                ));
            } else if (stock.isExpiringSoon(3)) {
                notifications.add(new Notification(
                    "STOCK_EXPIRING",
                    stock.getIngredientName() + " expires in " + stock.getDaysUntilExpiry() + " days",
                    "warning"
                ));
            } else if (stock.getQuantity() <= 1.0) {
                notifications.add(new Notification(
                    "LOW_STOCK",
                    stock.getIngredientName() + " quantity is low (" + stock.getQuantity() + " " + stock.getUnit() + ")",
                    "info"
                ));
            }
        }

        return notifications;
    }

    public static List<Notification> filterNotificationsBySeverity(List<Notification> notifications, String severity) {
        List<Notification> filtered = new ArrayList<>();
        for (Notification notification : notifications) {
            if (notification.getSeverity().equals(severity)) {
                filtered.add(notification);
            }
        }
        return filtered;
    }

    public static List<Notification> filterNotificationsByType(List<Notification> notifications, String type) {
        List<Notification> filtered = new ArrayList<>();
        for (Notification notification : notifications) {
            if (notification.getType().equals(type)) {
                filtered.add(notification);
            }
        }
        return filtered;
    }

    public static List<Notification> getNotificationsByDate(List<Notification> notifications, LocalDate date) {
        List<Notification> filtered = new ArrayList<>();
        for (Notification notification : notifications) {
            if (notification.getDate().equals(date)) {
                filtered.add(notification);
            }
        }
        return filtered;
    }

    public static String getNotificationSummary(List<Notification> notifications) {
        int critical = 0, warning = 0, info = 0;
        
        for (Notification notification : notifications) {
            switch (notification.getSeverity()) {
                case "critical":
                    critical++;
                    break;
                case "warning":
                    warning++;
                    break;
                case "info":
                    info++;
                    break;
            }
        }

        return String.format("Critical: %d, Warning: %d, Info: %d", critical, warning, info);
    }

    public static boolean hasCriticalNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            if ("critical".equals(notification.getSeverity())) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getNotificationMessages(List<Notification> notifications) {
        List<String> messages = new ArrayList<>();
        for (Notification notification : notifications) {
            messages.add(notification.toString());
        }
        return messages;
    }

    public static Notification createCustomNotification(String type, String message, String severity) {
        return new Notification(type, message, severity);
    }

    public static Notification createCustomNotification(String type, String message, String severity, LocalDate date) {
        return new Notification(type, message, severity, date);
    }
}
