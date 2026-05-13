package nasi_bergizi_pajak.controller;

import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.IngredientPrice;
import nasi_bergizi_pajak.service.IngredientService;

import java.time.LocalDate;
import java.util.List;

public class IngredientController {
    private final IngredientService ingredientService;

    public IngredientController() {
        this.ingredientService = new IngredientService();
    }

    public void addIngredient(Ingredient ingredient) {
        ingredientService.addIngredient(ingredient);
    }

    public void updateIngredient(Ingredient ingredient) {
        ingredientService.updateIngredient(ingredient);
    }

    public void deleteIngredient(int ingredientId) {
        ingredientService.deleteIngredient(ingredientId);
    }

    public Ingredient getIngredientById(int ingredientId) {
        return ingredientService.getIngredientById(ingredientId);
    }

    public List<Ingredient> getAllIngredients() {
        return ingredientService.getAllIngredients();
    }

    public List<Ingredient> searchIngredients(String searchTerm) {
        return ingredientService.searchIngredients(searchTerm);
    }

    public void updateIngredientPrice(int ingredientId, double newPrice, LocalDate effectiveDate) {
        ingredientService.updateIngredientPrice(ingredientId, newPrice, effectiveDate);
    }

    public List<IngredientPrice> getPriceHistoryByIngredient(int ingredientId) {
        return ingredientService.getPriceHistoryByIngredient(ingredientId);
    }

    public List<IngredientPrice> getAllPrices() {
        return ingredientService.getAllPrices();
    }

    public boolean validateIngredientData(Ingredient ingredient) {
        return ingredientService.validateIngredientData(ingredient);
    }

    public boolean validatePriceData(IngredientPrice price) {
        return ingredientService.validatePriceData(price);
    }

    public String[] getIngredientPriceStatistics(int ingredientId) {
        List<IngredientPrice> priceHistory = getPriceHistoryByIngredient(ingredientId);

        if (priceHistory.isEmpty()) {
            return new String[]{"No price data available"};
        }

        double minPrice = Double.MAX_VALUE;
        double maxPrice = -Double.MAX_VALUE;
        double total = 0;

        for (IngredientPrice price : priceHistory) {
            minPrice = Math.min(minPrice, price.getPrice());
            maxPrice = Math.max(maxPrice, price.getPrice());
            total += price.getPrice();
        }

        double avgPrice = total / priceHistory.size();

        return new String[]{
            "Average Price: " + String.format("%.2f", avgPrice),
            "Min Price: " + String.format("%.2f", minPrice),
            "Max Price: " + String.format("%.2f", maxPrice),
            "Price History: " + priceHistory.size() + " records"
        };
    }
}
