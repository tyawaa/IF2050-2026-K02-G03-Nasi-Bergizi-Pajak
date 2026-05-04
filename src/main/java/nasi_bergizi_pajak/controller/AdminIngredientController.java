package nasi_bergizi_pajak.controller;

import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.IngredientPrice;
import nasi_bergizi_pajak.service.IngredientService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AdminIngredientController {
    private final IngredientService ingredientService;

    public AdminIngredientController(DatabaseConfig dbConfig) {
        this.ingredientService = new IngredientService(dbConfig);
    }

    public boolean addIngredient(Ingredient ingredient) throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }
        return ingredientService.addIngredient(ingredient);
    }

    public boolean updateIngredient(Ingredient ingredient) throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }
        return ingredientService.updateIngredient(ingredient);
    }

    public boolean deleteIngredient(int ingredientId) throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }
        return ingredientService.deleteIngredient(ingredientId);
    }

    public boolean addIngredientPrice(IngredientPrice price) throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }
        return ingredientService.addPrice(price);
    }

    public boolean updateIngredientPrice(IngredientPrice price) throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }
        return ingredientService.updatePrice(price);
    }

    public boolean deleteIngredientPrice(int priceId) throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }
        return ingredientService.deletePrice(priceId);
    }

    public List<Ingredient> getAllIngredients() throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }
        return ingredientService.getAllIngredients();
    }

    public List<IngredientPrice> getAllIngredientPrices() throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }
        return ingredientService.getAllPrices();
    }

    public List<IngredientPrice> getPriceHistoryByIngredient(int ingredientId) throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }
        return ingredientService.getPriceHistoryByIngredient(ingredientId);
    }

    public boolean bulkUpdatePrices(List<IngredientPrice> priceUpdates) throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }

        boolean allSuccess = true;
        for (IngredientPrice price : priceUpdates) {
            try {
                if (!ingredientService.addPrice(price)) {
                    allSuccess = false;
                }
            } catch (SQLException e) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    public boolean bulkDeleteIngredients(List<Integer> ingredientIds) throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }

        boolean allSuccess = true;
        for (Integer ingredientId : ingredientIds) {
            try {
                if (!ingredientService.deleteIngredient(ingredientId)) {
                    allSuccess = false;
                }
            } catch (SQLException e) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    public String[] getIngredientManagementStats() throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }

        List<Ingredient> ingredients = ingredientService.getAllIngredients();
        List<IngredientPrice> prices = ingredientService.getAllPrices();

        int ingredientsWithoutPrice = 0;
        for (Ingredient ingredient : ingredients) {
            try {
                if (ingredientService.getLatestPrice(ingredient.getIngredientId()) == 0.0) {
                    ingredientsWithoutPrice++;
                }
            } catch (SQLException e) {
                ingredientsWithoutPrice++;
            }
        }

        return new String[]{
            "Total Ingredients: " + ingredients.size(),
            "Total Price Records: " + prices.size(),
            "Ingredients Without Price: " + ingredientsWithoutPrice,
            "Average Prices per Ingredient: " + (ingredients.size() > 0 ? String.format("%.1f", (double) prices.size() / ingredients.size()) : "0")
        };
    }

    public List<Ingredient> getIngredientsNeedingPriceUpdate() throws SQLException {
        if (!isAdminUser()) {
            throw new SQLException("Admin access required");
        }

        List<Ingredient> allIngredients = ingredientService.getAllIngredients();
        List<Ingredient> needingUpdate = new java.util.ArrayList<>();

        for (Ingredient ingredient : allIngredients) {
            try {
                IngredientPrice currentPrice = ingredientService.getCurrentPriceByIngredient(ingredient.getIngredientId());
                if (currentPrice == null || 
                    currentPrice.getEffectiveDate().isBefore(LocalDate.now().minusMonths(1))) {
                    needingUpdate.add(ingredient);
                }
            } catch (SQLException e) {
                needingUpdate.add(ingredient);
            }
        }

        return needingUpdate;
    }

    public boolean validateIngredientForAdmin(Ingredient ingredient) {
        if (!ingredientService.validateIngredientData(ingredient)) {
            return false;
        }
        return ingredient.getName().length() >= 2 && ingredient.getName().length() <= 100;
    }

    public boolean validatePriceForAdmin(IngredientPrice price) {
        if (!ingredientService.validatePriceData(price)) {
            return false;
        }
        return price.getPrice() >= 0 && price.getPrice() <= 1000000;
    }

    private boolean isAdminUser() {
        return true;
    }

    public String[] getAdminNotifications() throws SQLException {
        List<String> notifications = new java.util.ArrayList<>();

        try {
            List<Ingredient> needingUpdate = getIngredientsNeedingPriceUpdate();
            if (!needingUpdate.isEmpty()) {
                notifications.add(needingUpdate.size() + " ingredients need price updates");
            }

            String[] stats = getIngredientManagementStats();
            if (Integer.parseInt(stats[2].split(": ")[1]) > 0) {
                notifications.add(stats[2]);
            }

        } catch (SQLException e) {
            notifications.add("Error retrieving admin notifications");
        }

        return notifications.toArray(new String[0]);
    }
}
