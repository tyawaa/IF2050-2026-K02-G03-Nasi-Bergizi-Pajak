package nasi_bergizi_pajak.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class UnitOptions {
    private static final List<String> MASTER_UNITS = List.of(
            "gram",
            "kg",
            "ml",
            "liter",
            "pcs",
            "buah",
            "butir",
            "ikat",
            "botol",
            "porsi",
            "bungkus",
            "sendok makan",
            "sendok teh");

    private UnitOptions() {
    }

    public static List<String> masterUnits() {
        return MASTER_UNITS;
    }

    public static List<String> masterUnitsWith(String selectedUnit) {
        List<String> options = new ArrayList<>(MASTER_UNITS);
        addIfPresent(options, selectedUnit);
        return Collections.unmodifiableList(options);
    }

    public static List<String> recipeUnitsFor(String ingredientUnit) {
        List<String> options = new ArrayList<>();
        switch (normalize(ingredientUnit)) {
            case "kg", "gram" -> {
                options.add("gram");
                options.add("kg");
            }
            case "liter", "ml" -> {
                options.add("ml");
                options.add("liter");
            }
            case "botol" -> options.add("botol");
            default -> addIfPresent(options, ingredientUnit);
        }
        return Collections.unmodifiableList(options);
    }

    public static String defaultRecipeUnit(String ingredientUnit) {
        return switch (normalize(ingredientUnit)) {
            case "kg", "gram" -> "gram";
            case "liter", "ml" -> "ml";
            default -> trimToEmpty(ingredientUnit);
        };
    }

    public static double convertQuantity(double quantity, String fromUnit, String toUnit) {
        String from = normalize(fromUnit);
        String to = normalize(toUnit);
        if (from.equals(to)) {
            return quantity;
        }
        if ("gram".equals(from) && "kg".equals(to)) {
            return quantity / 1000;
        }
        if ("kg".equals(from) && "gram".equals(to)) {
            return quantity * 1000;
        }
        if ("ml".equals(from) && "liter".equals(to)) {
            return quantity / 1000;
        }
        if ("liter".equals(from) && "ml".equals(to)) {
            return quantity * 1000;
        }
        return Double.NaN;
    }

    private static void addIfPresent(List<String> options, String unit) {
        String normalizedUnit = trimToEmpty(unit);
        if (normalizedUnit.isBlank()) {
            return;
        }
        for (String option : options) {
            if (option.equalsIgnoreCase(normalizedUnit)) {
                return;
            }
        }
        options.add(normalizedUnit);
    }

    private static String normalize(String unit) {
        String normalized = trimToEmpty(unit).toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "g", "gr" -> "gram";
            case "l" -> "liter";
            case "pc", "piece", "pieces" -> "pcs";
            default -> normalized;
        };
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
