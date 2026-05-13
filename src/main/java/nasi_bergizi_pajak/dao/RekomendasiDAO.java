package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.KebutuhanGizi;
import nasi_bergizi_pajak.model.Recipe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RekomendasiDAO {

    public List<RecommendationData> listDataRekomendasi(int userId, int targetServings, Set<String> allergyKeywords)
            throws SQLException {

        String sql = """
                SELECT
                    r.recipe_id,
                    r.name,
                    r.description,
                    r.serving_size,
                    r.status,
                    ri.ingredient_id,
                    ri.amount,
                    ri.unit AS recipe_unit,
                    i.name AS ingredient_name,
                    i.unit AS ingredient_unit,
                    n.calories,
                    n.protein,
                    n.carbohydrate,
                    n.fat,
                    n.fibre,
                    n.unit AS nutrition_unit,
                    ip.price AS latest_price
                FROM recipe r
                JOIN recipe_ingredient ri
                    ON r.recipe_id = ri.recipe_id
                JOIN ingredient i
                    ON ri.ingredient_id = i.ingredient_id
                LEFT JOIN ingredient_nutrition n
                    ON ri.ingredient_id = n.ingredient_id
                LEFT JOIN ingredient_price ip
                    ON ip.price_id = (
                        SELECT ip2.price_id
                        FROM ingredient_price ip2
                        WHERE ip2.ingredient_id = ri.ingredient_id
                          AND ip2.effective_date <= CURDATE()
                        ORDER BY ip2.effective_date DESC, ip2.price_id DESC
                        LIMIT 1
                    )
                WHERE UPPER(r.status) = 'ACTIVE'
                ORDER BY r.recipe_id, ri.recipe_ingredient_id
                """;

        Map<Integer, RecipeAccumulator> groupedRecipes = new LinkedHashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int recipeId = rs.getInt("recipe_id");

                RecipeAccumulator accumulator = groupedRecipes.computeIfAbsent(recipeId, id -> {
                    Recipe recipe = new Recipe();
                    try {
                        recipe.setRecipeId(rs.getInt("recipe_id"));
                        recipe.setName(rs.getString("name"));
                        recipe.setDescription(rs.getString("description"));
                        recipe.setServingSize(rs.getInt("serving_size"));
                        recipe.setStatus(rs.getString("status"));
                    } catch (SQLException e) {
                        throw new IllegalStateException("Gagal memetakan data resep.", e);
                    }
                    return new RecipeAccumulator(recipe);
                });

                IngredientSnapshot snapshot = new IngredientSnapshot();
                snapshot.ingredientId = rs.getInt("ingredient_id");
                snapshot.ingredientName = rs.getString("ingredient_name");
                snapshot.recipeAmount = rs.getDouble("amount");
                snapshot.recipeUnit = rs.getString("recipe_unit");
                snapshot.ingredientUnit = rs.getString("ingredient_unit");

                snapshot.calories = rs.getObject("calories") == null ? null : rs.getDouble("calories");
                snapshot.protein = rs.getObject("protein") == null ? null : rs.getDouble("protein");
                snapshot.carbohydrate = rs.getObject("carbohydrate") == null ? null : rs.getDouble("carbohydrate");
                snapshot.fat = rs.getObject("fat") == null ? null : rs.getDouble("fat");
                snapshot.fibre = rs.getObject("fibre") == null ? null : rs.getDouble("fibre");
                snapshot.nutritionUnit = rs.getString("nutrition_unit");

                snapshot.latestPrice = rs.getObject("latest_price") == null ? null : rs.getDouble("latest_price");

                accumulator.ingredients.add(snapshot);
            }
        }

        Map<Integer, List<StockSnapshot>> stockByIngredient = loadKitchenStock(userId);
        List<RecommendationData> results = new ArrayList<>();

        for (RecipeAccumulator accumulator : groupedRecipes.values()) {
            Recipe recipe = accumulator.recipe;

            if (recipe.getServingSize() <= 0 || accumulator.ingredients.isEmpty()) {
                continue;
            }

            double scaleFactor = (double) Math.max(1, targetServings) / recipe.getServingSize();

            KebutuhanGizi totalNutrition = new KebutuhanGizi();
            double totalPrice = 0.0;
            boolean completeData = true;
            boolean safeForAllergy = true;
            double minimumStockCoverage = 1.0;

            for (IngredientSnapshot ingredient : accumulator.ingredients) {
                if (!isNutritionComplete(ingredient) || ingredient.latestPrice == null) {
                    completeData = false;
                    break;
                }

                double scaledRecipeAmount = ingredient.recipeAmount * scaleFactor;

                double amountForNutrition = convertQuantity(
                        scaledRecipeAmount,
                        ingredient.recipeUnit,
                        ingredient.nutritionUnit
                );

                double amountForPrice = convertQuantity(
                        scaledRecipeAmount,
                        ingredient.recipeUnit,
                        ingredient.ingredientUnit
                );

                if (Double.isNaN(amountForNutrition) || Double.isNaN(amountForPrice)) {
                    completeData = false;
                    break;
                }

                totalNutrition.setKalori(totalNutrition.getKalori() + ingredient.calories * amountForNutrition);
                totalNutrition.setProtein(totalNutrition.getProtein() + ingredient.protein * amountForNutrition);
                totalNutrition.setKarbohidrat(totalNutrition.getKarbohidrat() + ingredient.carbohydrate * amountForNutrition);
                totalNutrition.setLemak(totalNutrition.getLemak() + ingredient.fat * amountForNutrition);
                totalNutrition.setSerat(totalNutrition.getSerat() + ingredient.fibre * amountForNutrition);

                totalPrice += ingredient.latestPrice * amountForPrice;

                if (containsAllergen(ingredient.ingredientName, allergyKeywords)) {
                    safeForAllergy = false;
                }

                double availableStock = sumConvertedStock(
                        stockByIngredient.get(ingredient.ingredientId),
                        ingredient.recipeUnit
                );

                double coverage = scaledRecipeAmount <= 0
                        ? 1.0
                        : Math.min(1.0, availableStock / scaledRecipeAmount);

                minimumStockCoverage = Math.min(minimumStockCoverage, coverage);
            }

            if (!completeData || !safeForAllergy) {
                continue;
            }

            boolean stockSufficient = minimumStockCoverage >= 0.999;

            results.add(new RecommendationData(
                    recipe,
                    totalNutrition,
                    totalPrice,
                    stockSufficient,
                    minimumStockCoverage
            ));
        }

        return results;
    }

    public Double findActiveBudgetAmount(int userId) throws SQLException {
        String sql = """
                SELECT amount
                FROM budget
                WHERE user_id = ?
                  AND LOWER(status) = 'active'
                ORDER BY period_start DESC, budget_id DESC
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("amount");
                }
            }
        }

        return null;
    }

    private Map<Integer, List<StockSnapshot>> loadKitchenStock(int userId) throws SQLException {
        String sql = """
                SELECT ingredient_id, quantity, unit
                FROM kitchen_stock
                WHERE user_id = ?
                  AND quantity > 0
                  AND (expiry_date IS NULL OR expiry_date >= CURDATE())
                """;

        Map<Integer, List<StockSnapshot>> stockByIngredient = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StockSnapshot snapshot = new StockSnapshot();
                    snapshot.ingredientId = rs.getInt("ingredient_id");
                    snapshot.quantity = rs.getDouble("quantity");
                    snapshot.unit = rs.getString("unit");

                    stockByIngredient
                            .computeIfAbsent(snapshot.ingredientId, key -> new ArrayList<>())
                            .add(snapshot);
                }
            }
        }

        return stockByIngredient;
    }

    private double sumConvertedStock(List<StockSnapshot> stocks, String targetUnit) {
        if (stocks == null || stocks.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;

        for (StockSnapshot stock : stocks) {
            double converted = convertQuantity(stock.quantity, stock.unit, targetUnit);
            if (!Double.isNaN(converted)) {
                total += converted;
            }
        }

        return total;
    }

    private boolean isNutritionComplete(IngredientSnapshot ingredient) {
        return ingredient.calories != null
                && ingredient.protein != null
                && ingredient.carbohydrate != null
                && ingredient.fat != null
                && ingredient.fibre != null
                && ingredient.nutritionUnit != null
                && !ingredient.nutritionUnit.isBlank();
    }

    private boolean containsAllergen(String ingredientName, Set<String> allergyKeywords) {
        if (ingredientName == null || allergyKeywords == null || allergyKeywords.isEmpty()) {
            return false;
        }

        String normalizedIngredient = ingredientName.trim().toLowerCase(Locale.ROOT);

        for (String keyword : allergyKeywords) {
            if (keyword == null || keyword.isBlank()) {
                continue;
            }

            String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
            if (normalizedIngredient.contains(normalizedKeyword)
                    || normalizedKeyword.contains(normalizedIngredient)) {
                return true;
            }
        }

        return false;
    }

    private double convertQuantity(double quantity, String fromUnit, String toUnit) {
        String from = normalizeUnit(fromUnit);
        String to = normalizeUnit(toUnit);

        if (from.isEmpty() || to.isEmpty()) {
            return Double.NaN;
        }

        if (to.equals("100g")) {
            double grams = convertQuantity(quantity, from, "gram");
            return Double.isNaN(grams) ? Double.NaN : grams / 100.0;
        }

        if (to.equals("100ml")) {
            double milliliters = convertQuantity(quantity, from, "ml");
            return Double.isNaN(milliliters) ? Double.NaN : milliliters / 100.0;
        }

        if (from.equals(to)) {
            return quantity;
        }

        if (from.equals("kg") && to.equals("gram")) {
            return quantity * 1000.0;
        }

        if (from.equals("gram") && to.equals("kg")) {
            return quantity / 1000.0;
        }

        if (from.equals("liter") && to.equals("ml")) {
            return quantity * 1000.0;
        }

        if (from.equals("ml") && to.equals("liter")) {
            return quantity / 1000.0;
        }

        return Double.NaN;
    }

    private String normalizeUnit(String unit) {
        if (unit == null) {
            return "";
        }

        String normalized = unit.trim().toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "100g", "100 g", "100 gram", "100 grams", "per 100g", "per 100 gram" -> "100g";
            case "100ml", "100 ml", "100 milliliter", "100 milliliters", "per 100ml", "per 100 ml" -> "100ml";
            case "g", "gr", "gram", "grams" -> "gram";
            case "kg", "kilogram", "kilograms" -> "kg";
            case "ml", "milliliter", "milliliters" -> "ml";
            case "l", "lt", "liter", "litre", "liters", "litres" -> "liter";
            case "pcs", "pc", "piece", "pieces" -> "pcs";
            default -> normalized;
        };
    }

    public static class RecommendationData {
        private final Recipe recipe;
        private final KebutuhanGizi nutrition;
        private final double estimatedPrice;
        private final boolean stockSufficient;
        private final double stockCoverage;

        public RecommendationData(Recipe recipe, KebutuhanGizi nutrition, double estimatedPrice,
                                  boolean stockSufficient, double stockCoverage) {
            this.recipe = recipe;
            this.nutrition = nutrition;
            this.estimatedPrice = estimatedPrice;
            this.stockSufficient = stockSufficient;
            this.stockCoverage = stockCoverage;
        }

        public Recipe getRecipe() {
            return recipe;
        }

        public KebutuhanGizi getNutrition() {
            return nutrition;
        }

        public double getEstimatedPrice() {
            return estimatedPrice;
        }

        public boolean isStockSufficient() {
            return stockSufficient;
        }

        public double getStockCoverage() {
            return stockCoverage;
        }
    }

    private static class RecipeAccumulator {
        private final Recipe recipe;
        private final List<IngredientSnapshot> ingredients = new ArrayList<>();

        private RecipeAccumulator(Recipe recipe) {
            this.recipe = recipe;
        }
    }

    private static class IngredientSnapshot {
        private int ingredientId;
        private String ingredientName;
        private double recipeAmount;
        private String recipeUnit;
        private String ingredientUnit;
        private Double calories;
        private Double protein;
        private Double carbohydrate;
        private Double fat;
        private Double fibre;
        private String nutritionUnit;
        private Double latestPrice;
    }

    private static class StockSnapshot {
        private int ingredientId;
        private double quantity;
        private String unit;
    }
}
