package nasi_bergizi_pajak.model;

import java.time.LocalDate;

public class KitchenStock {
    private int stockId;
    private int userId;
    private int ingredientId;
    private double quantity;
    private String unit;
    private String storageLocation;
    private LocalDate expiryDate;
    private String ingredientName;

    public KitchenStock() {}

    public KitchenStock(int userId, int ingredientId, double quantity, String unit, String storageLocation, LocalDate expiryDate) {
        this.userId = userId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unit = unit;
        this.storageLocation = storageLocation;
        this.expiryDate = expiryDate;
    }

    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public boolean isExpiringSoon(int daysThreshold) {
        if (expiryDate == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.plusDays(daysThreshold);
        return expiryDate.isBefore(thresholdDate) || expiryDate.isEqual(thresholdDate);
    }

    public boolean isExpired() {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now());
    }

    public int getDaysUntilExpiry() {
        if (expiryDate == null) return Integer.MAX_VALUE;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KitchenStock that = (KitchenStock) o;
        return stockId == that.stockId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(stockId);
    }

    @Override
    public String toString() {
        return "KitchenStock{" +
                "stockId=" + stockId +
                ", userId=" + userId +
                ", ingredientId=" + ingredientId +
                ", ingredientName='" + ingredientName + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", storageLocation='" + storageLocation + '\'' +
                ", expiryDate=" + expiryDate +
                '}';
    }
}
