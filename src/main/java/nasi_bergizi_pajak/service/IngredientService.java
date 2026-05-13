package nasi_bergizi_pajak.service;

import nasi_bergizi_pajak.config.DatabaseConfig;
import nasi_bergizi_pajak.dao.IngredientDAO;
import nasi_bergizi_pajak.dao.IngredientPriceDAO;
import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.IngredientPrice;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class IngredientService {
    private final IngredientDAO ingredientDAO;
    private final IngredientPriceDAO priceDAO;

    public IngredientService(DatabaseConfig dbConfig) {
        this.ingredientDAO = new IngredientDAO(dbConfig);
        this.priceDAO = new IngredientPriceDAO(dbConfig);
    }

    public boolean addIngredient(Ingredient ingredient) throws SQLException {
        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }
        if (ingredient.getUnit() == null || ingredient.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Unit cannot be empty");
        }
        
        Ingredient existing = ingredientDAO.getIngredientByName(ingredient.getName());
        if (existing != null) {
            throw new IllegalArgumentException("Ingredient with this name already exists");
        }
        
        return ingredientDAO.addIngredient(ingredient);
    }

    public boolean updateIngredient(Ingredient ingredient) throws SQLException {
        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }
        if (ingredient.getUnit() == null || ingredient.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Unit cannot be empty");
        }
        
        return ingredientDAO.updateIngredient(ingredient);
    }

    public boolean deleteIngredient(int ingredientId) throws SQLException {
        return ingredientDAO.deleteIngredient(ingredientId);
    }

    public Ingredient getIngredientById(int ingredientId) throws SQLException {
        return ingredientDAO.getIngredientById(ingredientId);
    }

    public Ingredient getIngredientByName(String name) throws SQLException {
        return ingredientDAO.getIngredientByName(name);
    }

    public List<Ingredient> getAllIngredients() throws SQLException {
        return ingredientDAO.getAllIngredients();
    }

    public List<Ingredient> searchIngredients(String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllIngredients();
        }
        return ingredientDAO.searchIngredients(searchTerm);
    }

    public boolean addPrice(IngredientPrice price) throws SQLException {
        if (price.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (price.getEffectiveDate().isAfter(LocalDate.now().plusDays(30))) {
            throw new IllegalArgumentException("Effective date cannot be more than 30 days in the future");
        }
        
        return priceDAO.addPrice(price);
    }

    public boolean updatePrice(IngredientPrice price) throws SQLException {
        if (price.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (price.getEffectiveDate().isAfter(LocalDate.now().plusDays(30))) {
            throw new IllegalArgumentException("Effective date cannot be more than 30 days in the future");
        }
        
        return priceDAO.updatePrice(price);
    }

    public boolean deletePrice(int priceId) throws SQLException {
        return priceDAO.deletePrice(priceId);
    }

    public IngredientPrice getCurrentPriceByIngredient(int ingredientId) throws SQLException {
        return priceDAO.getCurrentPriceByIngredient(ingredientId);
    }

    public List<IngredientPrice> getPriceHistoryByIngredient(int ingredientId) throws SQLException {
        return priceDAO.getPriceHistoryByIngredient(ingredientId);
    }

    public List<IngredientPrice> getAllPrices() throws SQLException {
        return priceDAO.getAllPrices();
    }

    public List<IngredientPrice> getPricesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        return priceDAO.getPricesByDateRange(startDate, endDate);
    }

    public boolean updateIngredientPrice(int ingredientId, double newPrice, LocalDate effectiveDate) throws SQLException {
        Ingredient ingredient = ingredientDAO.getIngredientById(ingredientId);
        if (ingredient == null) {
            throw new IllegalArgumentException("Ingredient not found");
        }
        
        IngredientPrice newPriceRecord = new IngredientPrice(ingredientId, newPrice, effectiveDate);
        return addPrice(newPriceRecord);
    }

    public double getLatestPrice(int ingredientId) throws SQLException {
        IngredientPrice price = getCurrentPriceByIngredient(ingredientId);
        return price != null ? price.getPrice() : 0.0;
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

    public List<Ingredient> getIngredientsWithCurrentPrices() throws SQLException {
        List<Ingredient> ingredients = getAllIngredients();
        for (Ingredient ingredient : ingredients) {
            IngredientPrice currentPrice = getCurrentPriceByIngredient(ingredient.getIngredientId());
            if (currentPrice != null) {
                ingredient.setCurrentPrice(currentPrice.getPrice());
                ingredient.setPriceEffectiveDate(currentPrice.getEffectiveDate());
            }
        }
        return ingredients;
    }
}
