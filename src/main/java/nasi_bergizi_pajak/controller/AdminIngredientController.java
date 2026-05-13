package nasi_bergizi_pajak.controller;

import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.IngredientPrice;
import nasi_bergizi_pajak.service.IngredientService;

import java.time.LocalDate;
import java.util.List;

public class AdminIngredientController {
    private final IngredientService ingredientService;

    public AdminIngredientController() {
        this.ingredientService = new IngredientService();
    }

    public void addIngredient(Ingredient ingredient) {
        if (!isAdminUser()) throw new IllegalStateException("Admin access required");
        ingredientService.addIngredient(ingredient);
    }

    public void updateIngredient(Ingredient ingredient) {
        if (!isAdminUser()) throw new IllegalStateException("Admin access required");
        ingredientService.updateIngredient(ingredient);
    }

    public void deleteIngredient(int ingredientId) {
        if (!isAdminUser()) throw new IllegalStateException("Admin access required");
        ingredientService.deleteIngredient(ingredientId);
    }

    public void updateIngredientPrice(int ingredientId, double price, LocalDate effectiveDate) {
        if (!isAdminUser()) throw new IllegalStateException("Admin access required");
        ingredientService.updateIngredientPrice(ingredientId, price, effectiveDate);
    }

    public List<Ingredient> getAllIngredients() {
        if (!isAdminUser()) throw new IllegalStateException("Admin access required");
        return ingredientService.getAllIngredients();
    }

    public List<IngredientPrice> getAllIngredientPrices() {
        if (!isAdminUser()) throw new IllegalStateException("Admin access required");
        return ingredientService.getAllPrices();
    }

    public List<IngredientPrice> getPriceHistoryByIngredient(int ingredientId) {
        if (!isAdminUser()) throw new IllegalStateException("Admin access required");
        return ingredientService.getPriceHistoryByIngredient(ingredientId);
    }

    public void bulkDeleteIngredients(List<Integer> ingredientIds) {
        if (!isAdminUser()) throw new IllegalStateException("Admin access required");
        ingredientIds.forEach(ingredientService::deleteIngredient);
    }

    public String[] getIngredientManagementStats() {
        if (!isAdminUser()) throw new IllegalStateException("Admin access required");
        List<Ingredient> ingredients = ingredientService.getAllIngredients();
        List<IngredientPrice> prices = ingredientService.getAllPrices();
        long withoutPrice = ingredients.stream()
                .filter(i -> i.getPricePerUnit() == 0.0)
                .count();
        return new String[]{
            "Total Ingredients: " + ingredients.size(),
            "Total Price Records: " + prices.size(),
            "Ingredients Without Price: " + withoutPrice,
            "Average Prices per Ingredient: " + (ingredients.isEmpty() ? "0"
                    : String.format("%.1f", (double) prices.size() / ingredients.size()))
        };
    }

    public boolean validateIngredientForAdmin(Ingredient ingredient) {
        if (!ingredientService.validateIngredientData(ingredient)) return false;
        return ingredient.getName().length() >= 2 && ingredient.getName().length() <= 100;
    }

    public boolean validatePriceForAdmin(IngredientPrice price) {
        if (!ingredientService.validatePriceData(price)) return false;
        return price.getPrice() >= 0 && price.getPrice() <= 1_000_000;
    }

    private boolean isAdminUser() {
        return true;
    }
}
