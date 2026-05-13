package nasi_bergizi_pajak.service;

import nasi_bergizi_pajak.dao.IngredientDAO;
import nasi_bergizi_pajak.dao.IngredientPriceDAO;
import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.IngredientPrice;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class IngredientService {
    private final IngredientDAO ingredientDAO = new IngredientDAO();
    private final IngredientPriceDAO priceDAO = new IngredientPriceDAO();

    public List<Ingredient> getAllIngredients() {
        return ingredientDAO.listAllIngredients();
    }

    public List<Ingredient> searchIngredients(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllIngredients();
        }
        String lower = searchTerm.trim().toLowerCase();
        return getAllIngredients().stream()
                .filter(i -> i.getName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public void addIngredient(Ingredient ingredient) {
        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama bahan tidak boleh kosong.");
        }
        if (ingredient.getUnit() == null || ingredient.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Satuan tidak boleh kosong.");
        }
        ingredientDAO.insertIngredient(ingredient);
    }

    public void updateIngredient(Ingredient ingredient) {
        ingredientDAO.updateIngredientDetails(ingredient);
    }

    public void deleteIngredient(int ingredientId) {
        ingredientDAO.deleteIngredient(ingredientId);
    }

    public Ingredient getIngredientById(int ingredientId) {
        return ingredientDAO.getIngredientById(ingredientId);
    }

    public void updateIngredientPrice(int ingredientId, double newPrice, LocalDate effectiveDate) {
        if (newPrice < 0) {
            throw new IllegalArgumentException("Harga tidak boleh negatif.");
        }
        if (effectiveDate == null) {
            throw new IllegalArgumentException("Tanggal berlaku tidak boleh kosong.");
        }
        if (effectiveDate.isAfter(LocalDate.now().plusDays(30))) {
            throw new IllegalArgumentException("Tanggal berlaku maksimal 30 hari ke depan.");
        }
        ingredientDAO.addIngredientPrice(ingredientId, newPrice, effectiveDate);
    }

    public List<IngredientPrice> getPriceHistoryByIngredient(int ingredientId) {
        return priceDAO.listPriceHistoryByIngredientId(ingredientId);
    }

    public List<IngredientPrice> getAllPrices() {
        return priceDAO.listAllPriceHistory();
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
}
