package nasi_bergizi_pajak.controller;

import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.IngredientPrice;
import nasi_bergizi_pajak.service.IngredientService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class IngredientController {
    private final IngredientService ingredientService;

    public IngredientController() {
        this.ingredientService = new IngredientService();
    }

    @Deprecated
    public IngredientController(Object dbConfig) {
        this();
    }

    public boolean addIngredient(Ingredient ingredient) throws SQLException {
        try {
            return ingredientService.addIngredient(ingredient);
        } catch (SQLException e) {
            throw new SQLException("Failed to add ingredient: " + e.getMessage(), e);
        }
    }

    public boolean updateIngredient(Ingredient ingredient) throws SQLException {
        try {
            return ingredientService.updateIngredient(ingredient);
        } catch (SQLException e) {
            throw new SQLException("Failed to update ingredient: " + e.getMessage(), e);
        }
    }

    public boolean deleteIngredient(int ingredientId) throws SQLException {
        try {
            return ingredientService.deleteIngredient(ingredientId);
        } catch (SQLException e) {
            throw new SQLException("Failed to delete ingredient: " + e.getMessage(), e);
        }
    }

    public Ingredient getIngredientById(int ingredientId) throws SQLException {
        try {
            return ingredientService.getIngredientById(ingredientId);
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve ingredient: " + e.getMessage(), e);
        }
    }

    public Ingredient getIngredientByName(String name) throws SQLException {
        try {
            return ingredientService.getIngredientByName(name);
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve ingredient: " + e.getMessage(), e);
        }
    }

    public List<Ingredient> getAllIngredients() throws SQLException {
        try {
            return ingredientService.getAllIngredients();
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve all ingredients: " + e.getMessage(), e);
        }
    }

    public List<Ingredient> searchIngredients(String searchTerm) throws SQLException {
        try {
            return ingredientService.searchIngredients(searchTerm);
        } catch (SQLException e) {
            throw new SQLException("Failed to search ingredients: " + e.getMessage(), e);
        }
    }

    public boolean addPrice(IngredientPrice price) throws SQLException {
        try {
            return ingredientService.addPrice(price);
        } catch (SQLException e) {
            throw new SQLException("Failed to add price: " + e.getMessage(), e);
        }
    }

    public boolean updatePrice(IngredientPrice price) throws SQLException {
        try {
            return ingredientService.updatePrice(price);
        } catch (SQLException e) {
            throw new SQLException("Failed to update price: " + e.getMessage(), e);
        }
    }

    public boolean deletePrice(int priceId) throws SQLException {
        try {
            return ingredientService.deletePrice(priceId);
        } catch (SQLException e) {
            throw new SQLException("Failed to delete price: " + e.getMessage(), e);
        }
    }

    public IngredientPrice getCurrentPriceByIngredient(int ingredientId) throws SQLException {
        try {
            return ingredientService.getCurrentPriceByIngredient(ingredientId);
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve current price: " + e.getMessage(), e);
        }
    }

    public List<IngredientPrice> getPriceHistoryByIngredient(int ingredientId) throws SQLException {
        try {
            return ingredientService.getPriceHistoryByIngredient(ingredientId);
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve price history: " + e.getMessage(), e);
        }
    }

    public List<IngredientPrice> getAllPrices() throws SQLException {
        try {
            return ingredientService.getAllPrices();
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve all prices: " + e.getMessage(), e);
        }
    }

    public List<IngredientPrice> getPricesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        try {
            return ingredientService.getPricesByDateRange(startDate, endDate);
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve prices by date range: " + e.getMessage(), e);
        }
    }

    public boolean updateIngredientPrice(int ingredientId, double newPrice, LocalDate effectiveDate) throws SQLException {
        try {
            return ingredientService.updateIngredientPrice(ingredientId, newPrice, effectiveDate);
        } catch (SQLException e) {
            throw new SQLException("Failed to update ingredient price: " + e.getMessage(), e);
        }
    }

    public double getLatestPrice(int ingredientId) throws SQLException {
        try {
            return ingredientService.getLatestPrice(ingredientId);
        } catch (SQLException e) {
            throw new SQLException("Failed to get latest price: " + e.getMessage(), e);
        }
    }

    public List<Ingredient> getIngredientsWithCurrentPrices() throws SQLException {
        try {
            return ingredientService.getIngredientsWithCurrentPrices();
        } catch (SQLException e) {
            throw new SQLException("Failed to get ingredients with current prices: " + e.getMessage(), e);
        }
    }

    public boolean validateIngredientData(Ingredient ingredient) {
        if (ingredient == null) return false;
        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) return false;
        if (ingredient.getUnit() == null || ingredient.getUnit().trim().isEmpty()) return false;
        return true;
    }

    public boolean validatePriceData(IngredientPrice price) {
        if (price == null) return false;
        if (price.getPrice() < 0) return false;
        if (price.getEffectiveDate() == null) return false;
        if (price.getEffectiveDate().isAfter(LocalDate.now().plusDays(30))) return false;
        return true;
    }

    public String[] getIngredientPriceStatistics(int ingredientId) throws SQLException {
        List<IngredientPrice> priceHistory = getPriceHistoryByIngredient(ingredientId);
        
        if (priceHistory.isEmpty()) {
            return new String[]{"No price data available"};
        }
        
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;
        double total = 0;
        
        for (IngredientPrice price : priceHistory) {
            minPrice = Math.min(minPrice, price.getPrice());
            maxPrice = Math.max(maxPrice, price.getPrice());
            total += price.getPrice();
        }
        
        double avgPrice = total / priceHistory.size();
        IngredientPrice currentPrice = getCurrentPriceByIngredient(ingredientId);
        
        return new String[]{
            "Current Price: " + (currentPrice != null ? String.format("%.2f", currentPrice.getPrice()) : "N/A"),
            "Average Price: " + String.format("%.2f", avgPrice),
            "Min Price: " + String.format("%.2f", minPrice),
            "Max Price: " + String.format("%.2f", maxPrice),
            "Price History: " + priceHistory.size() + " records"
        };
    }
}
