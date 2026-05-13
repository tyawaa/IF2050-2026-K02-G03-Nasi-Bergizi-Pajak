package nasi_bergizi_pajak.controller;

import java.io.File;
import java.text.NumberFormat;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.dao.AkunDAO;
import nasi_bergizi_pajak.dao.FamilyMemberDAO;
import nasi_bergizi_pajak.dao.RecipeDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.model.Budget;
import nasi_bergizi_pajak.model.FamilyMember;
import nasi_bergizi_pajak.model.Recipe;
import nasi_bergizi_pajak.model.RekomendasiMenu;
import nasi_bergizi_pajak.model.MenuMingguan;
import nasi_bergizi_pajak.model.SlotMakan;
import nasi_bergizi_pajak.util.PasswordUtil;

public class DashboardController {
    private static final Locale INDONESIAN_LOCALE = new Locale("id", "ID");
    private static final DateTimeFormatter PICKER_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter PREVIEW_DAY_FORMATTER = DateTimeFormatter.ofPattern("d MMM", INDONESIAN_LOCALE);
    private static final DateTimeFormatter PREVIEW_FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", INDONESIAN_LOCALE);
    private static final int WEEKLY_TOTAL_SLOTS = 28;
    private static final String[] WEEKLY_DAY_LABELS = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
    private static final String[] WEEKLY_MEAL_KEYS = {"breakfast", "lunch", "dinner", "snack"};
    private static final String[] WEEKLY_MEAL_LABELS = {"Sarapan", "Makan Siang", "Makan Malam", "Snack"};
    private static final BudgetOption FALLBACK_BUDGET = new BudgetOption(
            -1,
            "Budget Mingguan April W4",
            500_000,
            LocalDate.of(2024, 4, 22),
            LocalDate.of(2024, 4, 28),
            "active"
    );

    @FXML private Label welcomeLabel;
    @FXML private Label emailLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Label budgetPeriodBadgeLabel;
    @FXML private Label topbarAvatarText;
    @FXML private Label dashboardFamilyIconCountLabel;
    @FXML private Label dashboardFamilyCountLabel;
    @FXML private Button dashboardNavButton;
    @FXML private Button familyProfileNavButton;
    @FXML private Button budgetNavButton;
    @FXML private Button parameterPlannerNavButton;
    @FXML private Button budgetResetButton;
    @FXML private Button budgetSaveButton;
    @FXML private Button settingsNavButton;
    @FXML private Button shoppingPlannerNavButton;
    @FXML private Button profileSettingsTabButton;
    @FXML private Button securitySettingsTabButton;
    @FXML private HBox userProfileMenuButton;
    @FXML private VBox dashboardPage;
    @FXML private VBox familyProfilePage;
    @FXML private VBox budgetPage;
    @FXML private VBox parameterPlannerPage;
    @FXML private VBox settingsPage;
    @FXML private VBox profileSettingsPane;
    @FXML private VBox securitySettingsPane;
    @FXML private HBox familyAlertBox;
    @FXML private Label familyAlertIconLabel;
    @FXML private Label familyAlertTitle;
    @FXML private Label familyAlertSubtitle;
    @FXML private Label familyCountSubtitle;
    @FXML private Label totalMembersLabel;
    @FXML private VBox allergySummaryBox;
    @FXML private TableView<FamilyMember> familyMemberTable;
    @FXML private TableColumn<FamilyMember, String> nameColumn;
    @FXML private TableColumn<FamilyMember, String> birthDateColumn;
    @FXML private TableColumn<FamilyMember, String> heightColumn;
    @FXML private TableColumn<FamilyMember, String> weightColumn;
    @FXML private TableColumn<FamilyMember, String> allergyColumn;
    @FXML private TableColumn<FamilyMember, FamilyMember> actionColumn;

    @FXML private TextField budgetNameField;
    @FXML private TextField budgetAmountField;
    @FXML private DatePicker budgetStartDatePicker;
    @FXML private DatePicker budgetEndDatePicker;
    @FXML private ComboBox<String> budgetStatusComboBox;
    @FXML private Label budgetFormTitleLabel;
    @FXML private Label budgetActiveNameLabel;
    @FXML private Label budgetActiveAmountLabel;
    @FXML private Label budgetActivePeriodLabel;
    @FXML private Label budgetCountLabel;
    @FXML private Label budgetStatusSummaryLabel;
    @FXML private TableView<Budget> budgetTable;
    @FXML private TableColumn<Budget, String> budgetNameColumn;
    @FXML private TableColumn<Budget, String> budgetAmountColumn;
    @FXML private TableColumn<Budget, String> budgetPeriodColumn;
    @FXML private TableColumn<Budget, String> budgetStatusColumn;
    @FXML private TableColumn<Budget, Budget> budgetActionColumn;

    @FXML private Button weeklyMenuNavButton;
    @FXML private VBox weeklyMenuPage;
    @FXML private Label weeklyBudgetBadgeLabel;
    @FXML private Label weeklyPeriodLabel;
    @FXML private Label weeklyProgressLabel;
    @FXML private GridPane weeklyPlannerGrid;
    @FXML private Label weeklyMenuTotalLabel;
    @FXML private Label weeklyAvailableBudgetLabel;
    @FXML private Label weeklyMenuStatusLabel;
    @FXML private Label weeklyCaloriesLabel;
    @FXML private Label weeklyProteinLabel;
    @FXML private Label weeklyCarbsLabel;
    @FXML private Label weeklyFatLabel;
    @FXML private Label weeklyFiberLabel;
    @FXML private ImageView settingsAvatarImage;
    @FXML private Label settingsAvatarInitial;
    @FXML private TextField settingsFamilyNameField;
    @FXML private TextField settingsEmailField;
    @FXML private TextField settingsPhoneField;
    @FXML private TextField settingsLocationField;
    @FXML private Label settingsToastLabel;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordErrorLabel;
    @FXML private Button kitchenStockNavButton;
    @FXML private VBox kitchenStockPage;
    @FXML private KitchenStockViewController kitchenStockContentController;
    @FXML private ComboBox<BudgetOption> plannerBudgetCombo;
    @FXML private Label plannerBudgetHelperLabel;
    @FXML private DatePicker plannerStartDatePicker;
    @FXML private DatePicker plannerEndDatePicker;
    @FXML private Label plannerDateErrorLabel;
    @FXML private ComboBox<String> mainMealCombo;
    @FXML private ComboBox<String> snackCombo;
    @FXML private Button savePlannerButton;
    @FXML private HBox plannerToastBox;
    @FXML private Label plannerSaveStatusLabel;
    @FXML private Button usePlannerButton;
    @FXML private Label previewPeriodLabel;
    @FXML private Label previewTotalDaysLabel;
    @FXML private Label previewMealsLabel;
    @FXML private Label previewSlotsLabel;
    @FXML private Label previewBudgetLabel;

    private final AkunDAO akunDAO = new AkunDAO();
    private final FamilyMemberDAO familyMemberDAO = new FamilyMemberDAO();
    private final ObservableList<FamilyMember> familyMembers = FXCollections.observableArrayList();
    private final ObservableList<Budget> budgets = FXCollections.observableArrayList();
    private final BudgetController budgetController = new BudgetController();
    private final ObservableList<MenuMingguan> weeklyMenus = FXCollections.observableArrayList();
    private final ObservableList<SlotMakan> weeklySlots = FXCollections.observableArrayList();
    private final MenuController menuController = new MenuController();
    private final RecipeDAO recipeDAO = new RecipeDAO();
    private final Map<Integer, Recipe> weeklyRecipeCache = new HashMap<>();
    private final Map<Integer, RecipeNutrition> weeklyNutritionCache = new HashMap<>();
    private final RekomendasiController rekomendasiController = new RekomendasiController();
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(INDONESIAN_LOCALE);
    private final DateTimeFormatter budgetDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", INDONESIAN_LOCALE);
    private final Preferences plannerPreferences = Preferences.userNodeForPackage(DashboardController.class)
            .node("parameterPlanner");
    private Akun currentUser;
    private Budget selectedBudget;
    private ContextMenu profileMenu;
    private PauseTransition settingsToastTimer;
    private PauseTransition plannerToastTimer;
    private MenuMingguan selectedWeeklyMenu;
    private boolean plannerDirty;
    private boolean plannerSaved;
    private boolean suppressPlannerDirtyTracking;

    @FXML
    private void initialize() {
        currentUser = AppNavigator.getCurrentUser();
        initializeUserInfo();
        initializeSettingsForm();
        setupProfileMenu();
        setupParameterPlanner();
        setupBudgetPage();
        setupFamilyTable();
        setupWeeklyPlanner();
        refreshFamilyMembers();
        openRequestedDashboardPage();
    }

    private void openRequestedDashboardPage() {
        switch (AppNavigator.consumeRequestedDashboardPage()) {
            case FAMILY_PROFILE -> showFamilyProfilePage();
            case WEEKLY_MENU -> showWeeklyMenuPage();
            case RECOMMENDATION -> showRecommendationPage();
            case SETTINGS -> showSettingsPage();
            case DASHBOARD -> showDashboardPage();
        }
        refreshBudgets();
    }

    private void setupParameterPlanner() {
        suppressPlannerDirtyTracking = true;
        List<BudgetOption> budgetOptions = loadActiveBudgets();
        plannerBudgetCombo.setItems(FXCollections.observableArrayList(budgetOptions));
        plannerBudgetCombo.setValue(findPreferredBudget(budgetOptions));
        mainMealCombo.setItems(FXCollections.observableArrayList("1 kali", "2 kali", "3 kali"));
        snackCombo.setItems(FXCollections.observableArrayList("0 kali", "1 kali"));
        mainMealCombo.setValue(plannerPreferences.getInt("mainMealsPerDay", 3) + " kali");
        snackCombo.setValue(clampSnackCount(plannerPreferences.getInt("snacksPerDay", 1)) + " kali");

        StringConverter<LocalDate> dateConverter = new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.format(PICKER_DATE_FORMATTER);
            }

            @Override
            public LocalDate fromString(String value) {
                if (value == null || value.isBlank()) {
                    return null;
                }

                try {
                    return LocalDate.parse(value.trim(), PICKER_DATE_FORMATTER);
                } catch (Exception e) {
                    return null;
                }
            }
        };

        plannerStartDatePicker.setConverter(dateConverter);
        plannerEndDatePicker.setConverter(dateConverter);
        BudgetOption selectedBudget = plannerBudgetCombo.getValue();
        plannerStartDatePicker.setValue(loadPlannerDate("startDate", selectedBudget.periodStart()));
        plannerEndDatePicker.setValue(loadPlannerDate("endDate", selectedBudget.periodEnd()));

        plannerBudgetCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            markPlannerDirty();
            updatePlannerPreview();
        });
        plannerStartDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            markPlannerDirty();
            updatePlannerPreview();
        });
        plannerEndDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            markPlannerDirty();
            updatePlannerPreview();
        });
        mainMealCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            markPlannerDirty();
            updatePlannerPreview();
        });
        snackCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            markPlannerDirty();
            updatePlannerPreview();
        });

        suppressPlannerDirtyTracking = false;
        plannerSaved = plannerPreferences.getBoolean("saved", false);
        plannerDirty = !plannerSaved;
        updatePlannerPreview();
    }

    private List<BudgetOption> loadActiveBudgets() {
        if (currentUser == null) {
            return List.of(FALLBACK_BUDGET);
        }

        String sql = """
                SELECT budget_id, name, amount, period_start, period_end, status
                FROM budget
                WHERE user_id = ?
                  AND LOWER(status) = 'active'
                ORDER BY period_end DESC, budget_id DESC
                """;

        List<BudgetOption> budgets = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    budgets.add(new BudgetOption(
                            rs.getInt("budget_id"),
                            rs.getString("name"),
                            rs.getDouble("amount"),
                            rs.getDate("period_start").toLocalDate(),
                            rs.getDate("period_end").toLocalDate(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat budget aktif untuk Parameter Planner: " + e.getMessage());
        }

        return budgets.isEmpty() ? List.of(FALLBACK_BUDGET) : budgets;
    }

    private BudgetOption findPreferredBudget(List<BudgetOption> budgetOptions) {
        int savedBudgetId = plannerPreferences.getInt("budgetId", -1);
        return budgetOptions.stream()
                .filter(budget -> budget.budgetId() == savedBudgetId)
                .findFirst()
                .orElse(budgetOptions.get(0));
    }

    private LocalDate loadPlannerDate(String key, LocalDate fallback) {
        try {
            return LocalDate.parse(plannerPreferences.get(key, fallback.toString()));
        } catch (Exception e) {
            return fallback;
        }
    }

    private void updatePlannerPreview() {
        BudgetOption selectedBudget = plannerBudgetCombo.getValue() == null ? FALLBACK_BUDGET : plannerBudgetCombo.getValue();
        boolean budgetMissing = selectedBudget.budgetId() <= 0;
        plannerBudgetHelperLabel.setText(budgetMissing
                ? "Belum ada budget aktif. Buat atau aktifkan budget dulu sebelum menyimpan parameter."
                : "Budget aktif : " + selectedBudget.name());
        previewBudgetLabel.setText(formatRupiahCompact(selectedBudget.amount()));

        LocalDate startDate = plannerStartDatePicker.getValue();
        LocalDate endDate = plannerEndDatePicker.getValue();
        boolean dateMissing = startDate == null || endDate == null;
        boolean dateInvalid = dateMissing || endDate.isBefore(startDate);

        if (dateInvalid || budgetMissing) {
            plannerDateErrorLabel.setText(budgetMissing
                    ? "Budget aktif wajib tersedia sebelum menyimpan parameter."
                    : dateMissing
                            ? "Tanggal mulai dan tanggal akhir wajib diisi."
                            : "Tanggal akhir tidak boleh lebih awal dari tanggal mulai.");
            plannerDateErrorLabel.setVisible(true);
            plannerDateErrorLabel.setManaged(true);
            savePlannerButton.setDisable(true);
            previewPeriodLabel.setText("-");
            previewTotalDaysLabel.setText("0 hari");
            previewSlotsLabel.setText("0 slot");
        } else {
            plannerDateErrorLabel.setVisible(false);
            plannerDateErrorLabel.setManaged(false);
            savePlannerButton.setDisable(false);

            int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
            previewPeriodLabel.setText(formatPreviewPeriod(startDate, endDate));
            previewTotalDaysLabel.setText(totalDays + " hari");
            previewSlotsLabel.setText(totalDays * (getSelectedMainMeals() + getSelectedSnacks()) + " slot");
        }

        previewMealsLabel.setText(getSelectedMainMeals() + " makan + " + getSelectedSnacks() + " snack");
        updatePlannerSaveState();
    }

    private void markPlannerDirty() {
        if (!suppressPlannerDirtyTracking) {
            plannerDirty = true;
        }
    }

    private void updatePlannerSaveState() {
        boolean dateInvalid = savePlannerButton.isDisable();
        usePlannerButton.setDisable(dateInvalid || plannerDirty || !plannerSaved);

        if (dateInvalid) {
            plannerSaveStatusLabel.setText(plannerDateErrorLabel.isVisible()
                    ? plannerDateErrorLabel.getText()
                    : "Perbaiki data sebelum menyimpan parameter.");
            return;
        }

        if (!plannerSaved) {
            plannerSaveStatusLabel.setText("Parameter belum disimpan. Tekan Simpan Parameter sebelum dipakai.");
            return;
        }

        plannerSaveStatusLabel.setText(plannerDirty
                ? "Perubahan belum disimpan. Tekan Simpan Parameter sebelum dipakai."
                : "Parameter terakhir sudah tersimpan.");
    }

    private String formatPreviewPeriod(LocalDate startDate, LocalDate endDate) {
        String start = startDate.getYear() == endDate.getYear()
                ? startDate.format(PREVIEW_DAY_FORMATTER)
                : startDate.format(PREVIEW_FULL_DATE_FORMATTER);
        return start + " - " + endDate.format(PREVIEW_FULL_DATE_FORMATTER);
    }

    private int getSelectedMainMeals() {
        return parseMealCount(mainMealCombo.getValue(), 3);
    }

    private int getSelectedSnacks() {
        return clampSnackCount(parseMealCount(snackCombo.getValue(), 1));
    }

    private int parseMealCount(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        try {
            return Integer.parseInt(value.trim().split("\\s+")[0]);
        } catch (Exception e) {
            return fallback;
        }
    }

    private int clampSnackCount(int value) {
        return Math.max(0, Math.min(1, value));
    }

    private String formatRupiahCompact(double amount) {
        return "Rp" + String.format(INDONESIAN_LOCALE, "%,.0f", amount).replace(',', '.');
    }

    private void initializeUserInfo() {
        if (currentUser == null) {
            welcomeLabel.setText("Selamat datang");
            emailLabel.setText("");
            return;
        }

        String nama = currentUser.getFirstName();
        if (currentUser.getLastName() != null && !currentUser.getLastName().isBlank()) {
            nama += " " + currentUser.getLastName();
        }

        welcomeLabel.setText(nama);
        emailLabel.setText(currentUser.getEmail());
        topbarAvatarText.setText(getInitial(nama));
    }

    private String getDisplayName() {
        if (currentUser == null) {
            return "Budi Santoso";
        }

        String firstName = currentUser.getFirstName() == null || currentUser.getFirstName().isBlank()
                ? "Budi"
                : currentUser.getFirstName();
        String lastName = currentUser.getLastName() == null ? "" : currentUser.getLastName();
        return (firstName + " " + lastName).trim();
    }

    private String getInitial(String name) {
        if (name == null || name.isBlank()) {
            return "N";
        }
        return name.trim().substring(0, 1).toUpperCase();
    }

    private void initializeSettingsForm() {
        String displayName = getDisplayName();
        settingsAvatarInitial.setText(getInitial(displayName));
        settingsFamilyNameField.setText("Keluarga " + displayName.split("\\s+")[0]);
        settingsEmailField.setText(currentUser == null ? "budi@email.com" : currentUser.getEmail());
        settingsPhoneField.setText("+62 812 3456 7890");
        settingsLocationField.setText("Jakarta");
        hidePasswordError();
    }

    private void setupProfileMenu() {
        Label nameLabel = new Label(getDisplayName());
        nameLabel.getStyleClass().add("profile-dropdown-name");

        CustomMenuItem headerItem = new CustomMenuItem(nameLabel, false);
        headerItem.getStyleClass().add("profile-dropdown-header");

        MenuItem settingsItem = new MenuItem("Pengaturan");
        settingsItem.setOnAction(event -> showSettingsPage());

        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.getStyleClass().add("profile-dropdown-logout");
        logoutItem.setOnAction(event -> {
            System.out.println("Logout clicked");
        });

        profileMenu = new ContextMenu(headerItem, new SeparatorMenuItem(), settingsItem, new SeparatorMenuItem(), logoutItem);
        profileMenu.getStyleClass().add("profile-dropdown-menu");
    }

    private void setupWeeklyPlanner() {
        renderWeeklyPlanner(null, List.of());
        refreshWeeklySummary(null, List.of());
    }

    @FXML
    private void handleRefreshWeeklyMenus() {
        refreshWeeklyMenus();
    }

    @FXML
    private void handleSaveWeeklyMenu() {
        if (selectedWeeklyMenu == null) {
            showInfo("Menu Mingguan", "Belum ada menu mingguan untuk disimpan.");
            return;
        }

        showInfo("Menu Mingguan", "Menu mingguan sudah tersimpan.");
    }

    private void refreshWeeklyMenus() {
        if (currentUser == null) {
            weeklyMenus.clear();
            weeklySlots.clear();
            selectedWeeklyMenu = null;
            renderWeeklyPlanner(null, List.of());
            refreshWeeklySummary(null, List.of());
            weeklyMenuStatusLabel.setText("User tidak ditemukan");
            return;
        }

        try {
            List<MenuMingguan> menus = menuController.ambilMenuMingguanPengguna(currentUser.getUserId());
            weeklyMenus.setAll(menus);

            if (menus.isEmpty()) {
                weeklySlots.clear();
                selectedWeeklyMenu = null;
                renderWeeklyPlanner(null, List.of());
                refreshWeeklySummary(null, List.of());
                return;
            }

            MenuMingguan plannerMenu = findMenuForCurrentPlanner(menus);
            if (plannerMenu == null) {
                weeklySlots.clear();
                selectedWeeklyMenu = null;
                renderWeeklyPlanner(null, List.of());
                refreshWeeklySummary(null, List.of());
                return;
            }

            loadWeeklySlots(plannerMenu);
        } catch (SQLException e) {
            weeklyMenus.clear();
            weeklySlots.clear();
            selectedWeeklyMenu = null;
            renderWeeklyPlanner(null, List.of());
            refreshWeeklySummary(null, List.of());
            weeklyMenuStatusLabel.setText("Gagal memuat");
            showError("Gagal memuat menu mingguan", e.getMessage());
        }
    }

    private void loadWeeklySlots(MenuMingguan menu) {
        try {
            List<SlotMakan> slots = menuController.ambilSlotMakan(menu.getMenuId());
            weeklySlots.setAll(slots);
            selectedWeeklyMenu = menu;
            weeklyRecipeCache.clear();
            weeklyNutritionCache.clear();
            renderWeeklyPlanner(menu, slots);
            refreshWeeklySummary(menu, slots);
        } catch (SQLException e) {
            weeklySlots.clear();
            selectedWeeklyMenu = menu;
            renderWeeklyPlanner(menu, List.of());
            refreshWeeklySummary(menu, List.of());
            weeklyMenuStatusLabel.setText("Gagal memuat slot");
            showError("Gagal memuat slot menu", e.getMessage());
        }
    }

    private void renderWeeklyPlanner(MenuMingguan menu, List<SlotMakan> slots) {
        weeklyPlannerGrid.getChildren().clear();
        weeklyPlannerGrid.getColumnConstraints().clear();
        weeklyPlannerGrid.getRowConstraints().clear();

        ColumnConstraints labelColumn = new ColumnConstraints(88);
        labelColumn.setMinWidth(82);
        weeklyPlannerGrid.getColumnConstraints().add(labelColumn);

        List<LocalDate> plannerDates = getPlannerDates(menu);
        List<MealSlotDefinition> mealDefinitions = getPlannerMealDefinitions();

        for (int i = 0; i < plannerDates.size(); i++) {
            ColumnConstraints dayColumn = new ColumnConstraints();
            dayColumn.setMinWidth(158);
            dayColumn.setPrefWidth(182);
            dayColumn.setHgrow(Priority.ALWAYS);
            weeklyPlannerGrid.getColumnConstraints().add(dayColumn);
        }

        RowConstraints headerRow = new RowConstraints(48);
        weeklyPlannerGrid.getRowConstraints().add(headerRow);
        for (int i = 0; i < mealDefinitions.size(); i++) {
            RowConstraints mealRow = new RowConstraints();
            mealRow.setMinHeight(128);
            mealRow.setPrefHeight(142);
            mealRow.setVgrow(Priority.ALWAYS);
            weeklyPlannerGrid.getRowConstraints().add(mealRow);
        }

        Map<String, SlotMakan> slotByCell = new HashMap<>();
        for (SlotMakan slot : slots) {
            if (slot.getMealDate() != null && slot.getMealTime() != null) {
                slotByCell.put(getSlotKey(slot.getMealDate(), slot.getMealTime()), slot);
            }
        }

        weeklyPlannerGrid.add(new Label(""), 0, 0);
        for (int dayIndex = 0; dayIndex < plannerDates.size(); dayIndex++) {
            LocalDate date = plannerDates.get(dayIndex);
            Node header = createDayHeader(formatPlannerDayName(date), date);
            weeklyPlannerGrid.add(header, dayIndex + 1, 0);
        }

        for (int mealIndex = 0; mealIndex < mealDefinitions.size(); mealIndex++) {
            MealSlotDefinition mealDefinition = mealDefinitions.get(mealIndex);
            Node rowLabel = createMealRowLabel(mealDefinition.label());
            GridPane.setFillWidth(rowLabel, true);
            GridPane.setFillHeight(rowLabel, true);
            weeklyPlannerGrid.add(rowLabel, 0, mealIndex + 1);
            for (int dayIndex = 0; dayIndex < plannerDates.size(); dayIndex++) {
                LocalDate date = plannerDates.get(dayIndex);
                SlotMakan slot = slotByCell.get(getSlotKey(date, mealDefinition.key()));
                Node cell = createMealCell(date, mealDefinition.key(), slot);
                GridPane.setHgrow(cell, Priority.ALWAYS);
                GridPane.setVgrow(cell, Priority.ALWAYS);
                weeklyPlannerGrid.add(cell, dayIndex + 1, mealIndex + 1);
            }
        }
    }

    private Node createMealCell(LocalDate mealDate, String mealTime, SlotMakan slot) {
        if (slot == null) {
            return createEmptySlotCell(mealDate, mealTime);
        }

        return createRecipeCard(slot);
    }

    private Node createRecipeCard(SlotMakan slot) {
        VBox card = new VBox(7);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(116);
        card.setPrefHeight(126);
        card.setPickOnBounds(true);
        card.setCursor(Cursor.HAND);
        card.getStyleClass().add(slot.isEatingOut() ? "weekly-outside-card" : "weekly-meal-card");

        HBox header = new HBox(8);
        header.setAlignment(Pos.TOP_LEFT);

        if (slot.isEatingOut()) {
            Label title = new Label("Makan Luar");
            configureWeeklyCardTitle(title);
            title.getStyleClass().add("weekly-meal-title");
            HBox.setHgrow(title, Priority.ALWAYS);

            Label cost = new Label(formatRupiahCompact(slot.getOutsideCost()));
            configureWeeklyCardLabel(cost);
            cost.getStyleClass().add("weekly-meal-price");

            Label note = new Label("Biaya makan luar");
            configureWeeklyCardLabel(note);
            note.getStyleClass().add("weekly-meal-meta");

            Node actions = createWeeklySlotActions(slot);
            header.getChildren().addAll(title, actions);
            card.getChildren().addAll(header, cost, note);
            card.setOnMouseClicked(event -> openWeeklySlotDialog(slot.getMealDate(), slot.getMealTime(), slot));
            return card;
        }

        Recipe recipe = resolveRecipe(slot.getRecipeId());
        Label title = new Label(recipe == null ? "Resep tidak ditemukan" : recipe.getName());
        configureWeeklyCardTitle(title);
        title.getStyleClass().add("weekly-meal-title");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label price = new Label(formatRecipeEstimate(slot.getRecipeId()));
        configureWeeklyCardLabel(price);
        price.getStyleClass().add("weekly-meal-price");

        Label stock = new Label(recipe == null ? "Stok tidak lengkap" : "Stok tersedia");
        configureWeeklyCardLabel(stock);
        stock.getStyleClass().add(recipe == null ? "weekly-stock-warning" : "weekly-stock-ok");

        Node actions = createWeeklySlotActions(slot);
        header.getChildren().addAll(title, actions);
        card.getChildren().addAll(header, price, stock);
        card.setOnMouseClicked(event -> openWeeklySlotDialog(slot.getMealDate(), slot.getMealTime(), slot));
        return card;
    }

    private void configureWeeklyCardTitle(Label label) {
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setMaxHeight(42);
    }

    private void configureWeeklyCardLabel(Label label) {
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
    }

    private Node createWeeklySlotActions(SlotMakan slot) {
        HBox actions = new HBox(4);
        actions.setAlignment(Pos.TOP_RIGHT);
        actions.setMinWidth(64);
        actions.setPrefWidth(64);
        actions.setMaxWidth(64);

        Button editButton = new Button("Ubah");
        editButton.setMinWidth(38);
        editButton.setPrefWidth(38);
        editButton.setMinHeight(24);
        editButton.setPrefHeight(24);
        editButton.getStyleClass().add("weekly-card-action-button");
        editButton.setOnAction(event -> {
            event.consume();
            openWeeklySlotDialog(slot.getMealDate(), slot.getMealTime(), slot);
        });

        Button deleteButton = new Button("X");
        deleteButton.setMinWidth(22);
        deleteButton.setPrefWidth(22);
        deleteButton.setMinHeight(24);
        deleteButton.setPrefHeight(24);
        deleteButton.getStyleClass().add("weekly-card-delete-button");
        deleteButton.setOnAction(event -> {
            event.consume();
            handleDeleteWeeklySlot(slot);
        });

        actions.getChildren().addAll(editButton, deleteButton);
        return actions;
    }

    private Node createEmptySlotCell(LocalDate mealDate, String mealTime) {
        VBox emptyCell = new VBox();
        emptyCell.setAlignment(Pos.CENTER);
        emptyCell.setMaxWidth(Double.MAX_VALUE);
        emptyCell.setMinHeight(116);
        emptyCell.setPrefHeight(126);
        emptyCell.setPickOnBounds(true);
        emptyCell.setCursor(Cursor.HAND);
        emptyCell.getStyleClass().add("weekly-empty-cell");
        emptyCell.setOnMouseClicked(event -> openWeeklySlotDialog(mealDate, mealTime, null));
        Button plusButton = new Button("+");
        plusButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        plusButton.setMinHeight(42);
        plusButton.getStyleClass().add("weekly-empty-button");
        plusButton.setOnAction(event -> {
            event.consume();
            openWeeklySlotDialog(mealDate, mealTime, null);
        });

        Label recommendationHint = new Label("Pilih rekomendasi menu");
        recommendationHint.getStyleClass().add("weekly-meal-meta");

        Button recommendationButton = new Button("Rekomendasi");
        recommendationButton.setMaxWidth(Double.MAX_VALUE);
        recommendationButton.setMinHeight(28);
        recommendationButton.getStyleClass().add("weekly-outline-button");
        recommendationButton.setOnAction(event -> {
            event.consume();
            openWeeklyRecommendationDialog(mealDate, mealTime);
        });

        VBox.setVgrow(plusButton, Priority.ALWAYS);
        emptyCell.setSpacing(6);
        emptyCell.getChildren().addAll(plusButton, recommendationHint, recommendationButton);
        return emptyCell;
    }


    private void openWeeklyRecommendationDialog(LocalDate mealDate, String mealTime) {
        openWeeklySlotDialog(mealDate, mealTime, null, true);
    }
    private void openWeeklySlotDialog(LocalDate mealDate, String mealTime, SlotMakan existingSlot) {
        openWeeklySlotDialog(mealDate, mealTime, existingSlot, false);
    }

    private void openWeeklySlotDialog(LocalDate mealDate, String mealTime, SlotMakan existingSlot, boolean preferRecommendations) {
        if (getSelectedBudgetOption().budgetId() <= 0) {
            showWarning("Budget aktif belum tersedia",
                    "Tambahkan atau aktifkan budget dulu, lalu simpan Parameter Planner sebelum menambah menu.");
            return;
        }

        boolean editMode = existingSlot != null;
        Dialog<WeeklySlotFormResult> dialog = new Dialog<>();
        dialog.setTitle(editMode ? "Edit Menu" : "Tambah Menu");
        dialog.setHeaderText((editMode ? "Edit" : "Tambah") + " slot "
                + formatWeeklySlotTitle(mealDate, mealTime));
        dialog.getDialogPane().getStyleClass().add("weekly-slot-dialog");

        ButtonType saveButtonType = new ButtonType(editMode ? "Simpan" : "Tambah", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        CheckBox eatingOutCheckBox = new CheckBox("Makan di luar");
        eatingOutCheckBox.getStyleClass().add("weekly-dialog-checkbox");

        ObservableList<Recipe> allRecipeOptions = FXCollections.observableArrayList(loadRecipeOptions());
        ObservableList<Recipe> recommendedRecipeOptions = FXCollections.observableArrayList(loadRecommendedRecipeOptions());
        Recipe existingRecipe = editMode && !existingSlot.isEatingOut()
                ? resolveRecipe(existingSlot.getRecipeId())
                : null;
        boolean useRecommendations = !recommendedRecipeOptions.isEmpty()
                && (existingRecipe == null || containsRecipeId(recommendedRecipeOptions, existingRecipe.getRecipeId()));
        List<Recipe> initialRecipeOptions = useRecommendations ? recommendedRecipeOptions : allRecipeOptions;

        ComboBox<String> recipeSourceComboBox = new ComboBox<>();
        recipeSourceComboBox.setMaxWidth(Double.MAX_VALUE);
        recipeSourceComboBox.getStyleClass().add("planner-input");
        if (recommendedRecipeOptions.isEmpty()) {
            recipeSourceComboBox.setItems(FXCollections.observableArrayList("Semua Resep"));
            recipeSourceComboBox.setValue("Semua Resep");
        } else {
            recipeSourceComboBox.setItems(FXCollections.observableArrayList("Rekomendasi Menu", "Semua Resep"));
            recipeSourceComboBox.setValue(useRecommendations ? "Rekomendasi Menu" : "Semua Resep");
        }

        ComboBox<Recipe> recipeComboBox = new ComboBox<>();
        recipeComboBox.setMaxWidth(Double.MAX_VALUE);
        recipeComboBox.getStyleClass().add("planner-input");
        recipeComboBox.setPromptText("Pilih resep");
        recipeComboBox.setItems(initialRecipeOptions == recommendedRecipeOptions ? recommendedRecipeOptions : allRecipeOptions);
        recipeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Recipe recipe) {
                return recipe == null ? "" : recipe.getName();
            }

            @Override
            public Recipe fromString(String value) {
                return null;
            }
        });

        TextField outsideCostField = new TextField();
        outsideCostField.setPromptText("Biaya makan luar");
        outsideCostField.getStyleClass().add("planner-input");

        if (editMode) {
            eatingOutCheckBox.setSelected(existingSlot.isEatingOut());
            if (existingSlot.isEatingOut()) {
                outsideCostField.setText(String.format(INDONESIAN_LOCALE, "%.0f", existingSlot.getOutsideCost()));
            } else {
                recipeComboBox.setValue(existingRecipe);
            }
        } else if (!initialRecipeOptions.isEmpty()) {
            recipeComboBox.setValue(initialRecipeOptions.get(0));
        } else {
            eatingOutCheckBox.setSelected(true);
        }

        recipeSourceComboBox.valueProperty().addListener((observable, oldValue, source) -> {
            ObservableList<Recipe> selectedOptions = "Rekomendasi Menu".equals(source)
                    ? recommendedRecipeOptions
                    : allRecipeOptions;
            Recipe previousRecipe = recipeComboBox.getValue();
            recipeComboBox.setItems(selectedOptions);
            if (previousRecipe != null && containsRecipeId(selectedOptions, previousRecipe.getRecipeId())) {
                recipeComboBox.setValue(findRecipeById(selectedOptions, previousRecipe.getRecipeId()));
            } else if (!selectedOptions.isEmpty()) {
                recipeComboBox.setValue(selectedOptions.get(0));
            } else {
                recipeComboBox.setValue(null);
            }
        });

        Button createRecipeButton = new Button("+ Buat Resep Baru");
        createRecipeButton.getStyleClass().add("weekly-outline-button");
        createRecipeButton.setMaxWidth(Double.MAX_VALUE);
        createRecipeButton.setOnAction(event -> {
            List<Integer> existingRecipeIds = allRecipeOptions.stream()
                    .map(Recipe::getRecipeId)
                    .toList();
            showRecipeFormModal(dialog.getDialogPane().getScene().getWindow());

            allRecipeOptions.setAll(loadRecipeOptions());
            recommendedRecipeOptions.setAll(loadRecommendedRecipeOptions());
            recipeSourceComboBox.setItems(recommendedRecipeOptions.isEmpty()
                    ? FXCollections.observableArrayList("Semua Resep")
                    : FXCollections.observableArrayList("Rekomendasi Menu", "Semua Resep"));
            recipeSourceComboBox.setValue("Semua Resep");
            recipeComboBox.setItems(allRecipeOptions);

            Recipe newRecipe = allRecipeOptions.stream()
                    .filter(recipe -> !existingRecipeIds.contains(recipe.getRecipeId()))
                    .findFirst()
                    .orElse(allRecipeOptions.isEmpty() ? null : allRecipeOptions.get(0));
            recipeComboBox.setValue(newRecipe);
        });

        recipeComboBox.setDisable(eatingOutCheckBox.isSelected());
        recipeSourceComboBox.setDisable(eatingOutCheckBox.isSelected());
        createRecipeButton.setDisable(eatingOutCheckBox.isSelected());
        outsideCostField.setDisable(!eatingOutCheckBox.isSelected());
        eatingOutCheckBox.selectedProperty().addListener((observable, oldValue, eatingOut) -> {
            recipeComboBox.setDisable(eatingOut);
            recipeSourceComboBox.setDisable(eatingOut);
            createRecipeButton.setDisable(eatingOut);
            outsideCostField.setDisable(!eatingOut);
        });

        Label helperLabel = new Label();
        helperLabel.setWrapText(true);
        helperLabel.getStyleClass().add("planner-error-text");

        VBox form = new VBox(12);
        form.setPadding(new Insets(8, 0, 0, 0));
        form.getChildren().addAll(
                eatingOutCheckBox,
                new Label("Sumber Menu"),
                recipeSourceComboBox,
                new Label("Pilih Resep"),
                recipeComboBox,
                createRecipeButton,
                new Label("Biaya Makan Luar"),
                outsideCostField,
                helperLabel
        );
        dialog.getDialogPane().setContent(form);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        Runnable updateSaveState = () -> {
            String error = validateWeeklySlotForm(eatingOutCheckBox.isSelected(), recipeComboBox.getValue(), outsideCostField.getText());
            saveButton.setDisable(error != null);
            helperLabel.setText(error == null ? "" : error);
            helperLabel.setVisible(error != null);
            helperLabel.setManaged(error != null);
        };
        eatingOutCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateSaveState.run());
        recipeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateSaveState.run());
        outsideCostField.textProperty().addListener((observable, oldValue, newValue) -> updateSaveState.run());
        updateSaveState.run();

        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String error = validateWeeklySlotForm(eatingOutCheckBox.isSelected(), recipeComboBox.getValue(), outsideCostField.getText());
            if (error != null) {
                showWarning("Data menu belum valid", error);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != saveButtonType) {
                return null;
            }

            boolean eatingOut = eatingOutCheckBox.isSelected();
            Recipe selectedRecipe = recipeComboBox.getValue();
            double outsideCost = eatingOut ? parseCurrencyInput(outsideCostField.getText()) : 0;
            return new WeeklySlotFormResult(eatingOut, selectedRecipe == null ? null : selectedRecipe.getRecipeId(), outsideCost);
        });

        dialog.showAndWait().ifPresent(result -> saveWeeklySlot(mealDate, mealTime, existingSlot, result));
    }

    private void saveWeeklySlot(LocalDate mealDate, String mealTime, SlotMakan existingSlot, WeeklySlotFormResult result) {
        try {
            MenuMingguan menu = ensureSelectedWeeklyMenu();
            String normalizedMealTime = normalizeMealTimeForGrid(mealTime);

            if (existingSlot == null) {
                if (result.eatingOut()) {
                    menuController.tambahSlotMakanDiLuar(menu.getMenuId(), mealDate, normalizedMealTime, result.outsideCost());
                } else {
                    menuController.tambahSlotResep(menu.getMenuId(), result.recipeId(), mealDate, normalizedMealTime);
                }
            } else if (result.eatingOut()) {
                menuController.ubahSlotMakanDiLuar(existingSlot.getSlotId(), menu.getMenuId(), mealDate, normalizedMealTime, result.outsideCost());
            } else {
                menuController.ubahSlotResep(existingSlot.getSlotId(), menu.getMenuId(), result.recipeId(), mealDate, normalizedMealTime);
            }

            reloadWeeklyMenu(menu.getMenuId());
        } catch (SQLException | IllegalArgumentException e) {
            showError("Gagal menyimpan slot menu", e.getMessage());
        }
    }

    private void handleDeleteWeeklySlot(SlotMakan slot) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Hapus Menu");
        confirmation.setHeaderText("Hapus slot menu ini?");
        confirmation.setContentText(formatWeeklySlotTitle(slot.getMealDate(), slot.getMealTime()));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            menuController.hapusSlotMakan(slot.getMenuId(), slot.getSlotId());
            reloadWeeklyMenu(slot.getMenuId());
        } catch (SQLException | IllegalArgumentException e) {
            showError("Gagal menghapus slot menu", e.getMessage());
        }
    }

    private MenuMingguan ensureSelectedWeeklyMenu() throws SQLException {
        if (selectedWeeklyMenu != null && menuMatchesCurrentPlanner(selectedWeeklyMenu)) {
            return selectedWeeklyMenu;
        }
        selectedWeeklyMenu = null;

        if (currentUser == null) {
            throw new IllegalArgumentException("User tidak ditemukan.");
        }

        BudgetOption selectedBudget = getSelectedBudgetOption();
        int parameterId = plannerPreferences.getInt("parameterId", -1);
        if (parameterId <= 0) {
            updatePlannerPreview();
            parameterId = saveParameterPlannerToDatabase(selectedBudget);
            if (parameterId > 0) {
                plannerPreferences.putInt("parameterId", parameterId);
                plannerPreferences.putInt("budgetId", selectedBudget.budgetId());
                plannerPreferences.putBoolean("saved", true);
                plannerSaved = true;
                plannerDirty = false;
            }
        }

        if (parameterId <= 0) {
            throw new IllegalArgumentException("Simpan Parameter Planner dengan budget aktif sebelum menambah menu.");
        }

        LocalDate weekStart = getPlannerStartDate();
        LocalDate weekEnd = getPlannerEndDate(weekStart);
        selectedWeeklyMenu = menuController.buatMenuMingguan(currentUser.getUserId(), parameterId, weekStart, weekEnd);
        weeklyMenus.setAll(selectedWeeklyMenu);
        return selectedWeeklyMenu;
    }

    private void reloadWeeklyMenu(int menuId) {
        try {
            selectedWeeklyMenu = menuController.ambilMenuMingguan(menuId);
            if (selectedWeeklyMenu != null) {
                loadWeeklySlots(selectedWeeklyMenu);
            } else {
                refreshWeeklyMenus();
            }
        } catch (SQLException e) {
            showError("Gagal memuat ulang menu mingguan", e.getMessage());
        }
    }

    private List<Recipe> loadRecipeOptions() {
        try {
            List<Recipe> recipes = recipeDAO.listAllRecipes();
            if (!recipes.isEmpty()) {
                return recipes;
            }
        } catch (IllegalStateException e) {
            System.err.println("Gagal memuat daftar resep via RecipeDAO: " + e.getMessage());
        }

        return loadRecipeOptionsDirectly();
    }

    private List<Recipe> loadRecommendedRecipeOptions() {
        if (currentUser == null) {
            return List.of();
        }

        try {
            List<Recipe> recipes = new ArrayList<>();
            for (RekomendasiMenu recommendation : rekomendasiController.buatRekomendasiMenu(currentUser.getUserId())) {
                Recipe recipe = recommendation.getRecipe();
                if (recipe != null && !containsRecipeId(recipes, recipe.getRecipeId())) {
                    recipes.add(recipe);
                }
            }
            return recipes;
        } catch (SQLException | IllegalStateException e) {
            return List.of();
        }
    }

    private void showRecipeFormModal(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource("/view/RecipeFormView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Form Resep");
            stage.setScene(new Scene(root, 760, 640));
            stage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.showAndWait();
        } catch (Exception e) {
            showError("Gagal membuka form resep", e.getMessage());
        }
    }

    private boolean containsRecipeId(List<Recipe> recipes, int recipeId) {
        return findRecipeById(recipes, recipeId) != null;
    }

    private Recipe findRecipeById(List<Recipe> recipes, int recipeId) {
        if (recipes == null || recipeId <= 0) {
            return null;
        }

        for (Recipe recipe : recipes) {
            if (recipe != null && recipe.getRecipeId() == recipeId) {
                return recipe;
            }
        }

        return null;
    }

    private List<Recipe> loadRecipeOptionsDirectly() {
        String sql = """
                SELECT recipe_id, name, description, serving_size, status
                FROM recipe
                ORDER BY
                    CASE WHEN UPPER(status) = 'ACTIVE' THEN 0 ELSE 1 END,
                    name ASC
                """;

        List<Recipe> recipes = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Recipe recipe = new Recipe();
                recipe.setRecipeId(rs.getInt("recipe_id"));
                recipe.setName(rs.getString("name"));
                recipe.setDescription(rs.getString("description"));
                recipe.setServingSize(rs.getInt("serving_size"));
                recipe.setStatus(rs.getString("status"));
                recipes.add(recipe);
            }
        } catch (SQLException e) {
            showError("Gagal memuat daftar resep", e.getMessage());
        }

        return recipes;
    }

    private String validateWeeklySlotForm(boolean eatingOut, Recipe selectedRecipe, String outsideCostText) {
        if (eatingOut) {
            if (!isPositiveNumber(outsideCostText)) {
                return "Isi biaya makan luar dengan angka positif.";
            }
            return null;
        }

        return selectedRecipe == null ? "Belum ada resep yang bisa dipilih. Centang Makan di luar atau tambahkan resep dulu." : null;
    }

    private String validateBudgetForm(String name, String amountText, LocalDate startDate, LocalDate endDate) {
        if (name == null || name.isBlank()) {
            return "Nama budget wajib diisi.";
        }

        if (!isPositiveNumber(amountText)) {
            return "Nominal budget harus berupa angka positif.";
        }

        if (startDate == null || endDate == null) {
            return "Tanggal mulai dan akhir wajib diisi.";
        }

        if (endDate.isBefore(startDate)) {
            return "Tanggal akhir tidak boleh lebih awal dari tanggal mulai.";
        }

        return null;
    }

    private int insertBudget(BudgetOption budget) throws SQLException {
        String sql = """
                INSERT INTO budget
                (user_id, name, amount, period_start, period_end, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, currentUser.getUserId());
            stmt.setString(2, budget.name());
            stmt.setDouble(3, budget.amount());
            stmt.setDate(4, Date.valueOf(budget.periodStart()));
            stmt.setDate(5, Date.valueOf(budget.periodEnd()));
            stmt.setString(6, budget.status());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Gagal mengambil ID budget baru.");
    }

    private double parseCurrencyInput(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }

        return Double.parseDouble(value.trim().replace(".", "").replace(",", "."));
    }

    private void refreshWeeklySummary(MenuMingguan menu, List<SlotMakan> slots) {
        LocalDate startDate = getWeeklyStartDate(menu);
        LocalDate endDate = getWeeklyEndDate(menu, startDate);
        BudgetOption selectedBudget = getSelectedBudgetOption();
        int filledSlots = countVisiblePlannerSlots(slots);
        int totalSlots = getPlannerDates(menu).size() * getPlannerMealDefinitions().size();

        weeklyBudgetBadgeLabel.setText("Budget: " + formatDateRangeShort(selectedBudget.periodStart(), selectedBudget.periodEnd()));
        weeklyPeriodLabel.setText("Minggu: " + formatPreviewPeriod(startDate, endDate));
        weeklyProgressLabel.setText(filledSlots + " dari " + totalSlots + " slot terisi");
        weeklyMenuTotalLabel.setText(formatRupiahCompact(menu == null ? 0 : menu.getTotalEstimation()));
        weeklyAvailableBudgetLabel.setText(formatRupiahCompact(selectedBudget.amount()));
        updateWeeklyBudgetStatus(menu, selectedBudget.amount());

        RecipeNutrition totalNutrition = calculateWeeklyNutrition(menu, slots);
        if (totalNutrition.hasData()) {
            weeklyCaloriesLabel.setText(formatNutritionValue(totalNutrition.calories(), "kkal"));
            weeklyProteinLabel.setText(formatNutritionValue(totalNutrition.protein(), "g"));
            weeklyCarbsLabel.setText(formatNutritionValue(totalNutrition.carbohydrate(), "g"));
            weeklyFatLabel.setText(formatNutritionValue(totalNutrition.fat(), "g"));
            weeklyFiberLabel.setText(formatNutritionValue(totalNutrition.fibre(), "g"));
        } else {
            weeklyCaloriesLabel.setText("-");
            weeklyProteinLabel.setText("-");
            weeklyCarbsLabel.setText("-");
            weeklyFatLabel.setText("-");
            weeklyFiberLabel.setText("-");
        }
    }

    private void updateWeeklyBudgetStatus(MenuMingguan menu, double availableBudget) {
        weeklyMenuStatusLabel.getStyleClass().removeAll("weekly-status-safe", "weekly-status-over", "weekly-status-empty");

        if (menu == null) {
            weeklyMenuStatusLabel.setText("Belum ada menu");
            weeklyMenuStatusLabel.getStyleClass().add("weekly-status-empty");
            return;
        }

        boolean overBudget = "overbudget".equalsIgnoreCase(menu.getStatusBudget())
                || menu.getTotalEstimation() > availableBudget;
        weeklyMenuStatusLabel.setText(overBudget ? "Melebihi Budget" : "Aman");
        weeklyMenuStatusLabel.getStyleClass().add(overBudget ? "weekly-status-over" : "weekly-status-safe");
    }

    private RecipeNutrition calculateWeeklyNutrition(MenuMingguan menu, List<SlotMakan> slots) {
        RecipeNutrition total = new RecipeNutrition(0, 0, 0, 0, 0, false);
        if (menu == null || slots == null || slots.isEmpty()) {
            return total;
        }

        List<LocalDate> plannerDates = getPlannerDates(menu);
        List<String> visibleMealKeys = getPlannerMealDefinitions().stream()
                .map(MealSlotDefinition::key)
                .toList();

        for (SlotMakan slot : slots) {
            if (slot == null || slot.isEatingOut() || slot.getRecipeId() == null
                    || slot.getMealDate() == null || slot.getMealTime() == null) {
                continue;
            }

            boolean visibleSlot = !slot.getMealDate().isBefore(plannerDates.get(0))
                    && !slot.getMealDate().isAfter(plannerDates.get(plannerDates.size() - 1))
                    && visibleMealKeys.contains(normalizeMealTimeForGrid(slot.getMealTime()));
            if (!visibleSlot) {
                continue;
            }

            total = total.plus(getRecipeNutrition(slot.getRecipeId()));
        }

        return total;
    }

    private RecipeNutrition getRecipeNutrition(Integer recipeId) {
        if (recipeId == null || recipeId <= 0) {
            return RecipeNutrition.empty();
        }

        if (!weeklyNutritionCache.containsKey(recipeId)) {
            weeklyNutritionCache.put(recipeId, loadRecipeNutrition(recipeId));
        }

        return weeklyNutritionCache.get(recipeId);
    }

    private RecipeNutrition loadRecipeNutrition(int recipeId) {
        String sql = """
                SELECT ri.amount,
                       ri.unit AS recipe_unit,
                       n.calories,
                       n.protein,
                       n.carbohydrate,
                       n.fat,
                       n.fibre,
                       n.unit AS nutrition_unit
                FROM recipe_ingredient ri
                JOIN ingredient_nutrition n
                    ON ri.ingredient_id = n.ingredient_id
                WHERE ri.recipe_id = ?
                """;

        double calories = 0;
        double protein = 0;
        double carbohydrate = 0;
        double fat = 0;
        double fibre = 0;
        boolean hasData = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recipeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double multiplier = convertQuantity(
                            rs.getDouble("amount"),
                            rs.getString("recipe_unit"),
                            rs.getString("nutrition_unit")
                    );
                    if (Double.isNaN(multiplier)) {
                        continue;
                    }

                    calories += rs.getDouble("calories") * multiplier;
                    protein += rs.getDouble("protein") * multiplier;
                    carbohydrate += rs.getDouble("carbohydrate") * multiplier;
                    fat += rs.getDouble("fat") * multiplier;
                    fibre += rs.getDouble("fibre") * multiplier;
                    hasData = true;
                }
            }
        } catch (SQLException e) {
            return RecipeNutrition.empty();
        }

        return new RecipeNutrition(calories, protein, carbohydrate, fat, fibre, hasData);
    }

    private String formatNutritionValue(double value, String unit) {
        return String.format(INDONESIAN_LOCALE, "%,.0f", value).replace(',', '.') + " " + unit;
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

    private Node createDayHeader(String dayLabel, LocalDate date) {
        VBox header = new VBox(2);
        header.setAlignment(Pos.CENTER);
        header.setMaxWidth(Double.MAX_VALUE);
        header.getStyleClass().add("weekly-day-header");

        Label day = new Label(dayLabel);
        day.getStyleClass().add("weekly-day-name");
        Label number = new Label(String.valueOf(date.getDayOfMonth()));
        number.getStyleClass().add("weekly-day-number");
        header.getChildren().addAll(day, number);
        return header;
    }

    private Node createMealRowLabel(String mealLabel) {
        Label label = new Label(mealLabel);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("weekly-meal-row-label");
        return label;
    }

    private MenuMingguan findMenuForCurrentPlanner(List<MenuMingguan> menus) {
        return menus.stream()
                .filter(this::menuMatchesCurrentPlanner)
                .findFirst()
                .orElse(null);
    }

    private boolean menuMatchesCurrentPlanner(MenuMingguan menu) {
        if (menu == null || menu.getWeekStartDate() == null || menu.getWeekEndDate() == null) {
            return false;
        }

        LocalDate plannerStart = getPlannerStartDate();
        LocalDate plannerEnd = getPlannerEndDate(plannerStart);
        return menu.getWeekStartDate().isEqual(plannerStart)
                && menu.getWeekEndDate().isEqual(plannerEnd);
    }

    private List<LocalDate> getPlannerDates(MenuMingguan menu) {
        LocalDate startDate = getWeeklyStartDate(menu);
        LocalDate endDate = getWeeklyEndDate(menu, startDate);
        int totalDays = Math.max(1, (int) ChronoUnit.DAYS.between(startDate, endDate) + 1);

        List<LocalDate> dates = new ArrayList<>();
        for (int dayOffset = 0; dayOffset < totalDays; dayOffset++) {
            dates.add(startDate.plusDays(dayOffset));
        }
        return dates;
    }

    private List<MealSlotDefinition> getPlannerMealDefinitions() {
        int mainMeals = Math.max(1, Math.min(3, getPlannerMainMeals()));
        int snacks = Math.max(0, getPlannerSnacks());

        List<MealSlotDefinition> definitions = new ArrayList<>();
        for (int i = 0; i < mainMeals; i++) {
            definitions.add(new MealSlotDefinition(WEEKLY_MEAL_KEYS[i], WEEKLY_MEAL_LABELS[i]));
        }

        if (snacks > 0) {
            definitions.add(new MealSlotDefinition("snack", "Snack"));
        }

        return definitions;
    }

    private int countVisiblePlannerSlots(List<SlotMakan> slots) {
        if (slots == null || slots.isEmpty()) {
            return 0;
        }

        List<LocalDate> plannerDates = getPlannerDates(selectedWeeklyMenu);
        List<String> visibleMealKeys = getPlannerMealDefinitions().stream()
                .map(MealSlotDefinition::key)
                .toList();

        return (int) slots.stream()
                .filter(slot -> slot.getMealDate() != null && slot.getMealTime() != null)
                .filter(slot -> !slot.getMealDate().isBefore(plannerDates.get(0))
                        && !slot.getMealDate().isAfter(plannerDates.get(plannerDates.size() - 1)))
                .filter(slot -> visibleMealKeys.contains(normalizeMealTimeForGrid(slot.getMealTime())))
                .count();
    }

    private String formatPlannerDayName(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("EEE", INDONESIAN_LOCALE));
    }

    private Recipe resolveRecipe(Integer recipeId) {
        if (recipeId == null || recipeId <= 0) {
            return null;
        }

        if (!weeklyRecipeCache.containsKey(recipeId)) {
            try {
                weeklyRecipeCache.put(recipeId, recipeDAO.getRecipeById(recipeId));
            } catch (IllegalStateException e) {
                weeklyRecipeCache.put(recipeId, null);
            }
        }

        return weeklyRecipeCache.get(recipeId);
    }

    private String formatRecipeEstimate(Integer recipeId) {
        if (recipeId == null || recipeId <= 0) {
            return "Estimasi -";
        }

        try {
            double estimate = estimateRecipeCost(recipeId);
            return estimate <= 0 ? "Estimasi -" : formatRupiahCompact(estimate);
        } catch (SQLException e) {
            return "Estimasi -";
        }
    }

    private double estimateRecipeCost(int recipeId) throws SQLException {
        String sql = """
                SELECT ri.amount, ip.price
                FROM recipe_ingredient ri
                JOIN ingredient_price ip
                    ON ri.ingredient_id = ip.ingredient_id
                WHERE ri.recipe_id = ?
                  AND ip.effective_date = (
                        SELECT MAX(ip2.effective_date)
                        FROM ingredient_price ip2
                        WHERE ip2.ingredient_id = ri.ingredient_id
                  )
                """;

        double total = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recipeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    total += (rs.getDouble("amount") / 100.0) * rs.getDouble("price");
                }
            }
        }
        return total;
    }

    private String getSlotKey(LocalDate mealDate, String mealTime) {
        return mealDate + "|" + normalizeMealTimeForGrid(mealTime);
    }

    private String normalizeMealTimeForGrid(String mealTime) {
        if (mealTime == null) {
            return "";
        }

        String normalized = mealTime.trim().toLowerCase(Locale.ROOT).replace("_", " ");
        return switch (normalized) {
            case "sarapan", "breakfast" -> "breakfast";
            case "makan siang", "lunch" -> "lunch";
            case "makan malam", "dinner" -> "dinner";
            case "snack", "camilan" -> "snack";
            default -> normalized;
        };
    }

    private String formatWeeklySlotTitle(LocalDate mealDate, String mealTime) {
        String dateText = mealDate == null
                ? "-"
                : mealDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", INDONESIAN_LOCALE));
        return dateText + " - " + formatWeeklyMealTime(mealTime);
    }

    private String formatWeeklyMealTime(String mealTime) {
        return switch (normalizeMealTimeForGrid(mealTime)) {
            case "breakfast" -> "Sarapan";
            case "lunch" -> "Makan Siang";
            case "dinner" -> "Makan Malam";
            case "snack" -> "Snack";
            default -> formatStatus(mealTime);
        };
    }

    private LocalDate getWeeklyStartDate(MenuMingguan menu) {
        if (menu != null && menuMatchesCurrentPlanner(menu) && menu.getWeekStartDate() != null) {
            return menu.getWeekStartDate();
        }

        return getPlannerStartDate();
    }

    private LocalDate getWeeklyEndDate(MenuMingguan menu, LocalDate startDate) {
        if (menu != null && menuMatchesCurrentPlanner(menu) && menu.getWeekEndDate() != null) {
            return menu.getWeekEndDate();
        }

        return getPlannerEndDate(startDate);
    }

    private BudgetOption getSelectedBudgetOption() {
        return plannerBudgetCombo != null && plannerBudgetCombo.getValue() != null
                ? plannerBudgetCombo.getValue()
                : FALLBACK_BUDGET;
    }

    private LocalDate getPlannerStartDate() {
        if (plannerStartDatePicker != null && plannerStartDatePicker.getValue() != null) {
            return plannerStartDatePicker.getValue();
        }

        try {
            return LocalDate.parse(plannerPreferences.get("startDate", getSelectedBudgetOption().periodStart().toString()));
        } catch (Exception e) {
            return getSelectedBudgetOption().periodStart();
        }
    }

    private LocalDate getPlannerEndDate(LocalDate startDate) {
        LocalDate fallbackEnd = startDate == null ? getSelectedBudgetOption().periodEnd() : startDate.plusDays(6);
        if (plannerEndDatePicker != null && plannerEndDatePicker.getValue() != null) {
            LocalDate pickerEnd = plannerEndDatePicker.getValue();
            return pickerEnd.isBefore(startDate) ? fallbackEnd : pickerEnd;
        }

        try {
            LocalDate savedEnd = LocalDate.parse(plannerPreferences.get("endDate", fallbackEnd.toString()));
            return savedEnd.isBefore(startDate) ? fallbackEnd : savedEnd;
        } catch (Exception e) {
            return fallbackEnd;
        }
    }

    private int getPlannerMainMeals() {
        return plannerPreferences.getInt("mainMealsPerDay", getSelectedMainMeals());
    }

    private int getPlannerSnacks() {
        return clampSnackCount(plannerPreferences.getInt("snacksPerDay", getSelectedSnacks()));
    }

    private String formatDateRangeShort(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "-";
        }

        return startDate.format(PREVIEW_DAY_FORMATTER) + " - " + endDate.format(PREVIEW_DAY_FORMATTER);
    }

    private void setupBudgetPage() {
        budgetStatusComboBox.setItems(FXCollections.observableArrayList("active", "inactive"));
        budgetStatusComboBox.getSelectionModel().select("active");
        budgetStartDatePicker.setValue(LocalDate.now());
        budgetEndDatePicker.setValue(LocalDate.now().plusMonths(1));

        budgetTable.setItems(budgets);
        budgetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        budgetTable.setPlaceholder(createBudgetEmptyState());

        budgetNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
        budgetAmountColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(rupiahFormat.format(data.getValue().getAmount())));
        budgetPeriodColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatBudgetPeriod(data.getValue())));
        budgetStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatStatus(data.getValue().getStatus())));
        budgetActionColumn.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyObjectWrapper<>(data.getValue()));
        budgetStatusColumn.getStyleClass().add("family-center-column");
        budgetActionColumn.getStyleClass().add("family-center-column");
        budgetStatusColumn.setCellFactory(column -> new BudgetStatusCell());
        budgetActionColumn.setCellFactory(column -> new BudgetActionCell());
    }

    private Node createBudgetEmptyState() {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("family-empty-state");

        Label title = new Label("Belum ada budget");
        title.getStyleClass().add("family-empty-title");
        Label subtitle = new Label("Tambahkan budget aktif untuk mengatur batas belanja keluarga.");
        subtitle.getStyleClass().add("userdash-section-subtitle");

        box.getChildren().addAll(title, subtitle);
        return box;
    }

    private void refreshBudgetPage() {
        refreshBudgets();
        refreshPlannerBudgetOptions();
    }

    @FXML
    private void handleSaveBudget() {
        if (currentUser == null) {
            showWarning("User tidak ditemukan", "Silakan login ulang sebelum mengelola budget.");
            return;
        }

        try {
            Budget budget = readBudgetForm();
            if (selectedBudget == null) {
                budgetController.setBudget(budget);
            } else {
                budgetController.updateBudget(selectedBudget.getBudgetId(), budget);
            }

            resetBudgetForm();
            refreshBudgetPage();
        } catch (IllegalArgumentException e) {
            showWarning("Data budget belum valid", e.getMessage());
        } catch (SQLException e) {
            showError("Gagal menyimpan budget", e.getMessage());
        }
    }

    @FXML
    private void handleResetBudgetForm() {
        resetBudgetForm();
    }

    @FXML
    private void handleRefreshBudgets() {
        refreshBudgetPage();
    }

    private Budget readBudgetForm() {
        String name = budgetNameField.getText() == null ? "" : budgetNameField.getText().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Nama budget wajib diisi.");
        }

        double amount = parseBudgetAmount(budgetAmountField.getText());
        LocalDate startDate = budgetStartDatePicker.getValue();
        LocalDate endDate = budgetEndDatePicker.getValue();
        String status = budgetStatusComboBox.getValue();

        return new Budget(currentUser.getUserId(), name, amount, startDate, endDate, status);
    }

    private double parseBudgetAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Nominal budget wajib diisi.");
        }

        String normalized = text.trim()
                .replace("Rp", "")
                .replace("rp", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".");

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Nominal budget harus berupa angka.");
        }
    }

    private void refreshBudgets() {
        if (currentUser == null) {
            budgets.clear();
            updateBudgetSummary(null);
            return;
        }

        try {
            List<Budget> loadedBudgets = budgetController.getDaftarBudget(currentUser.getUserId());
            budgets.setAll(loadedBudgets);
            updateBudgetSummary(budgetController.getBudgetAktif(currentUser.getUserId()));
        } catch (SQLException e) {
            budgets.clear();
            updateBudgetSummary(null);
            showError("Gagal memuat budget", e.getMessage());
        }
    }

    private void updateBudgetSummary(Budget activeBudget) {
        budgetCountLabel.setText(budgets.size() + " budget");

        if (activeBudget == null) {
            budgetActiveNameLabel.setText("Belum ada budget aktif");
            budgetActiveAmountLabel.setText(rupiahFormat.format(0));
            budgetActivePeriodLabel.setText("-");
            budgetStatusSummaryLabel.setText("Tidak aktif");
            budgetPeriodBadgeLabel.setText("Budget: Belum aktif");
            return;
        }

        budgetActiveNameLabel.setText(activeBudget.getName());
        budgetActiveAmountLabel.setText(rupiahFormat.format(activeBudget.getAmount()));
        budgetActivePeriodLabel.setText(formatBudgetPeriod(activeBudget));
        budgetStatusSummaryLabel.setText("Aktif");
        budgetPeriodBadgeLabel.setText("Budget: " + formatBudgetPeriod(activeBudget));
    }

    private void editBudget(Budget budget) {
        selectedBudget = budget;
        budgetFormTitleLabel.setText("Ubah Budget");
        budgetSaveButton.setText("Perbarui Budget");
        budgetResetButton.setText("Batal Edit");
        budgetNameField.setText(budget.getName());
        budgetAmountField.setText(String.valueOf(Math.round(budget.getAmount())));
        budgetStartDatePicker.setValue(budget.getPeriodStart());
        budgetEndDatePicker.setValue(budget.getPeriodEnd());
        budgetStatusComboBox.getSelectionModel().select(budget.getStatus());
    }

    private void deleteBudget(Budget budget) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Budget");
        confirm.setHeaderText("Hapus " + budget.getName() + "?");
        confirm.setContentText("Data budget ini akan dihapus dari daftar budget keluarga.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            budgetController.hapusBudget(budget.getBudgetId(), budget.getUserId());
            if (selectedBudget != null && selectedBudget.getBudgetId() == budget.getBudgetId()) {
                resetBudgetForm();
            }
            refreshBudgetPage();
        } catch (SQLException e) {
            showError("Gagal menghapus budget", e.getMessage());
        }
    }

    private void resetBudgetForm() {
        selectedBudget = null;
        budgetFormTitleLabel.setText("Tambah Budget");
        budgetSaveButton.setText("Simpan Budget");
        budgetResetButton.setText("Reset");
        budgetNameField.clear();
        budgetAmountField.clear();
        budgetStartDatePicker.setValue(LocalDate.now());
        budgetEndDatePicker.setValue(LocalDate.now().plusMonths(1));
        budgetStatusComboBox.getSelectionModel().select("active");
        budgetTable.getSelectionModel().clearSelection();
    }

    private String formatBudgetPeriod(Budget budget) {
        if (budget == null || budget.getPeriodStart() == null || budget.getPeriodEnd() == null) {
            return "-";
        }
        return formatBudgetDate(budget.getPeriodStart()) + " s.d. " + formatBudgetDate(budget.getPeriodEnd());
    }

    private String formatBudgetDate(LocalDate date) {
        return date == null ? "-" : budgetDateFormatter.format(date);
    }

    private List<BudgetOption> loadBudgets(boolean activeOnly) {
        if (currentUser == null) {
            return List.of();
        }

        String sql = """
                SELECT budget_id, name, amount, period_start, period_end, status
                FROM budget
                WHERE user_id = ?
                """;
        if (activeOnly) {
            sql += " AND LOWER(status) = 'active'";
        }
        sql += " ORDER BY period_end DESC, budget_id DESC";

        List<BudgetOption> budgets = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUser.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    budgets.add(new BudgetOption(
                            rs.getInt("budget_id"),
                            rs.getString("name"),
                            rs.getDouble("amount"),
                            rs.getDate("period_start").toLocalDate(),
                            rs.getDate("period_end").toLocalDate(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            showError("Gagal memuat budget", e.getMessage());
        }

        return budgets;
    }

    private void refreshPlannerBudgetOptions() {
        BudgetOption previousBudget = plannerBudgetCombo.getValue();
        List<BudgetOption> budgetOptions = loadActiveBudgets();
        plannerBudgetCombo.setItems(FXCollections.observableArrayList(budgetOptions));

        BudgetOption selectedBudget = previousBudget == null
                ? findPreferredBudget(budgetOptions)
                : budgetOptions.stream()
                        .filter(budget -> budget.budgetId() == previousBudget.budgetId())
                        .findFirst()
                        .orElse(findPreferredBudget(budgetOptions));

        plannerBudgetCombo.setValue(selectedBudget);
        if (selectedBudget.budgetId() > 0) {
            plannerStartDatePicker.setValue(selectedBudget.periodStart());
            plannerEndDatePicker.setValue(selectedBudget.periodEnd());
        }
        updatePlannerPreview();
    }

    private void setupFamilyTable() {
        familyMemberTable.setItems(familyMembers);
        familyMemberTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        familyMemberTable.setPlaceholder(createEmptyState());

        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDisplayName()));
        birthDateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getBirthDateAgeText()));
        heightColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getHeightText()));
        weightColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getWeightText()));
        allergyColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAllergyDisplay()));
        actionColumn.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyObjectWrapper<>(data.getValue()));

        heightColumn.getStyleClass().add("family-center-column");
        weightColumn.getStyleClass().add("family-center-column");
        allergyColumn.getStyleClass().add("family-center-column");
        actionColumn.getStyleClass().add("family-center-column");

        birthDateColumn.setCellFactory(column -> new MultilineTextCell());
        heightColumn.setCellFactory(column -> new CenteredTextCell());
        weightColumn.setCellFactory(column -> new CenteredTextCell());
        allergyColumn.setCellFactory(column -> new AllergyBadgeCell());
        actionColumn.setCellFactory(column -> new ActionButtonCell());
    }

    private Node createEmptyState() {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("family-empty-state");

        Label title = new Label("Belum ada anggota keluarga");
        title.getStyleClass().add("family-empty-title");
        Label subtitle = new Label("Tambahkan data keluarga agar rekomendasi menu bisa disesuaikan.");
        subtitle.getStyleClass().add("userdash-section-subtitle");

        box.getChildren().addAll(title, subtitle);
        return box;
    }

    @FXML
    private void showDashboardPage() {
        pageTitleLabel.setText("Dashboard");
        showPage(dashboardPage);
        setActiveNav(dashboardNavButton);
    }

    @FXML
    private void showFamilyProfilePage() {
        pageTitleLabel.setText("Profil Keluarga");
        showPage(familyProfilePage);
        setActiveNav(familyProfileNavButton);
        refreshFamilyMembers();
    }

    @FXML
    private void showBudgetPage() {
        pageTitleLabel.setText("Budget");
        showPage(budgetPage);
        setActiveNav(budgetNavButton);
        refreshBudgetPage();
    }



    @FXML
    private void handleAddBudget() {
        if (currentUser == null) {
            showWarning("User tidak ditemukan", "Login ulang sebelum menambahkan budget.");
            return;
        }

        Dialog<BudgetOption> dialog = new Dialog<>();
        dialog.setTitle("Tambah Budget");
        dialog.setHeaderText("Tambah budget aktif");

        ButtonType saveType = new ButtonType("Simpan Budget", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Contoh: Budget Mingguan Mei W2");
        nameField.getStyleClass().add("planner-input");

        TextField amountField = new TextField();
        amountField.setPromptText("Contoh: 500000");
        amountField.getStyleClass().add("planner-input");

        DatePicker startPicker = new DatePicker(LocalDate.now());
        DatePicker endPicker = new DatePicker(LocalDate.now().plusDays(6));
        startPicker.getStyleClass().add("planner-input");
        endPicker.getStyleClass().add("planner-input");

        Label errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.getStyleClass().add("planner-error-text");

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(10);
        form.add(new Label("Nama Budget"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Nominal"), 0, 1);
        form.add(amountField, 1, 1);
        form.add(new Label("Tanggal Mulai"), 0, 2);
        form.add(startPicker, 1, 2);
        form.add(new Label("Tanggal Akhir"), 0, 3);
        form.add(endPicker, 1, 3);
        form.add(errorLabel, 1, 4);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(120);
        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelColumn, inputColumn);

        dialog.getDialogPane().setContent(form);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        Runnable updateSaveState = () -> {
            String error = validateBudgetForm(nameField.getText(), amountField.getText(), startPicker.getValue(), endPicker.getValue());
            saveButton.setDisable(error != null);
            errorLabel.setText(error == null ? "" : error);
            errorLabel.setVisible(error != null);
            errorLabel.setManaged(error != null);
        };
        nameField.textProperty().addListener((observable, oldValue, newValue) -> updateSaveState.run());
        amountField.textProperty().addListener((observable, oldValue, newValue) -> updateSaveState.run());
        startPicker.valueProperty().addListener((observable, oldValue, newValue) -> updateSaveState.run());
        endPicker.valueProperty().addListener((observable, oldValue, newValue) -> updateSaveState.run());
        updateSaveState.run();

        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String error = validateBudgetForm(nameField.getText(), amountField.getText(), startPicker.getValue(), endPicker.getValue());
            if (error != null) {
                showWarning("Data budget belum valid", error);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != saveType) {
                return null;
            }

            return new BudgetOption(
                    0,
                    nameField.getText().trim(),
                    parseCurrencyInput(amountField.getText()),
                    startPicker.getValue(),
                    endPicker.getValue(),
                    "active"
            );
        });

        dialog.showAndWait().ifPresent(budget -> {
            try {
                int budgetId = insertBudget(budget);
                plannerPreferences.putInt("budgetId", budgetId);
                plannerPreferences.remove("parameterId");
                plannerPreferences.putBoolean("saved", false);
                plannerSaved = false;
                plannerDirty = true;
                refreshBudgetPage();
                showParameterPlannerPage();
            } catch (SQLException e) {
                showError("Gagal menyimpan budget", e.getMessage());
            }
        });
    }

    @FXML
    private void showParameterPlannerPage() {
        pageTitleLabel.setText("Parameter Planner");
        showPage(parameterPlannerPage);
        setActiveNav(parameterPlannerNavButton);
        updatePlannerPreview();
    }

    @FXML
    private void showSettingsPage() {
        pageTitleLabel.setText("Pengaturan");
        showPage(settingsPage);
        setActiveNav(settingsNavButton);
        showProfileSettingsTab();
    }

    @FXML
    private void showProfileSettingsTab() {
        showSettingsTab(profileSettingsPane);
        setSettingsTabClass(profileSettingsTabButton, true);
        setSettingsTabClass(securitySettingsTabButton, false);
    }

    @FXML
    private void showSecuritySettingsTab() {
        showSettingsTab(securitySettingsPane);
        setSettingsTabClass(profileSettingsTabButton, false);
        setSettingsTabClass(securitySettingsTabButton, true);
    }

    @FXML
    private void showWeeklyMenuPage() {
        pageTitleLabel.setText("Menu Mingguan");
        showPage(weeklyMenuPage);
        setActiveNav(weeklyMenuNavButton);
        refreshWeeklyMenus();
    }

    private void showRecommendationPage() {
        showWeeklyMenuPage();
    }

    @FXML
    private void handleSavePlannerParameter() {
        updatePlannerPreview();
        if (savePlannerButton.isDisable()) {
            showWarning("Parameter belum valid", plannerDateErrorLabel.getText());
            return;
        }

        BudgetOption selectedBudget = plannerBudgetCombo.getValue() == null ? FALLBACK_BUDGET : plannerBudgetCombo.getValue();
        int parameterId = saveParameterPlannerToDatabase(selectedBudget);

        plannerPreferences.putInt("budgetId", selectedBudget.budgetId());
        plannerPreferences.put("budgetName", selectedBudget.name());
        plannerPreferences.put("startDate", plannerStartDatePicker.getValue().toString());
        plannerPreferences.put("endDate", plannerEndDatePicker.getValue().toString());
        plannerPreferences.putInt("mainMealsPerDay", getSelectedMainMeals());
        plannerPreferences.putInt("snacksPerDay", getSelectedSnacks());
        plannerPreferences.putBoolean("saved", true);
        if (parameterId > 0) {
            plannerPreferences.putInt("parameterId", parameterId);
        }

        plannerSaved = true;
        plannerDirty = false;
        updatePlannerPreview();
        showPlannerToast();
    }

    private void showPlannerToast() {
        if (plannerToastTimer != null) {
            plannerToastTimer.stop();
        }

        plannerToastBox.setVisible(true);
        plannerToastBox.setManaged(true);

        plannerToastTimer = new PauseTransition(Duration.seconds(3));
        plannerToastTimer.setOnFinished(event -> {
            plannerToastBox.setVisible(false);
            plannerToastBox.setManaged(false);
        });
        plannerToastTimer.play();
    }

    private int saveParameterPlannerToDatabase(BudgetOption budget) {
        if (currentUser == null || budget.budgetId() <= 0) {
            return -1;
        }

        int savedParameterId = plannerPreferences.getInt("parameterId", -1);
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (savedParameterId > 0 && updateParameterPlanner(conn, savedParameterId, budget)) {
                return savedParameterId;
            }

            return insertParameterPlanner(conn, budget);
        } catch (SQLException e) {
            System.err.println("Gagal menyimpan Parameter Planner ke database: " + e.getMessage());
            return -1;
        }
    }

    private boolean updateParameterPlanner(Connection conn, int parameterId, BudgetOption budget) throws SQLException {
        String sql = """
                UPDATE parameter_planner
                SET budget_id = ?,
                    shopping_period_start = ?,
                    shopping_period_end = ?,
                    meals_per_day = ?,
                    snack_per_day = ?
                WHERE parameter_id = ?
                  AND user_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, budget.budgetId());
            stmt.setDate(2, Date.valueOf(plannerStartDatePicker.getValue()));
            stmt.setDate(3, Date.valueOf(plannerEndDatePicker.getValue()));
            stmt.setInt(4, getSelectedMainMeals());
            stmt.setInt(5, getSelectedSnacks());
            stmt.setInt(6, parameterId);
            stmt.setInt(7, currentUser.getUserId());
            return stmt.executeUpdate() > 0;
        }
    }

    private int insertParameterPlanner(Connection conn, BudgetOption budget) throws SQLException {
        String sql = """
                INSERT INTO parameter_planner
                (user_id, budget_id, shopping_period_start, shopping_period_end, meals_per_day, snack_per_day)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, currentUser.getUserId());
            stmt.setInt(2, budget.budgetId());
            stmt.setDate(3, Date.valueOf(plannerStartDatePicker.getValue()));
            stmt.setDate(4, Date.valueOf(plannerEndDatePicker.getValue()));
            stmt.setInt(5, getSelectedMainMeals());
            stmt.setInt(6, getSelectedSnacks());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    @FXML
    private void handleUsePlannerForWeeklyMenu() {
        updatePlannerPreview();
        if (savePlannerButton.isDisable()) {
            showWarning("Parameter belum valid", plannerDateErrorLabel.getText());
            return;
        }

        if (plannerDirty || !plannerSaved) {
            showWarning("Parameter belum disimpan", "Simpan parameter dulu sebelum digunakan untuk Menu Mingguan.");
            return;
        }

        showWeeklyMenuPage();
    }

    private void showPage(VBox page) {
        dashboardPage.setVisible(page == dashboardPage);
        dashboardPage.setManaged(page == dashboardPage);

        familyProfilePage.setVisible(page == familyProfilePage);
        familyProfilePage.setManaged(page == familyProfilePage);

        budgetPage.setVisible(page == budgetPage);
        budgetPage.setManaged(page == budgetPage);

        parameterPlannerPage.setVisible(page == parameterPlannerPage);
        parameterPlannerPage.setManaged(page == parameterPlannerPage);

        weeklyMenuPage.setVisible(page == weeklyMenuPage);
        weeklyMenuPage.setManaged(page == weeklyMenuPage);

        settingsPage.setVisible(page == settingsPage);
        settingsPage.setManaged(page == settingsPage);

        kitchenStockPage.setVisible(page == kitchenStockPage);
        kitchenStockPage.setManaged(page == kitchenStockPage);
    }

    private void setActiveNav(Button activeButton) {
        setNavClass(dashboardNavButton, activeButton == dashboardNavButton);
        setNavClass(familyProfileNavButton, activeButton == familyProfileNavButton);
        setNavClass(budgetNavButton, activeButton == budgetNavButton);
        setNavClass(parameterPlannerNavButton, activeButton == parameterPlannerNavButton);
        setNavClass(weeklyMenuNavButton, activeButton == weeklyMenuNavButton);
        setNavClass(shoppingPlannerNavButton, activeButton == shoppingPlannerNavButton);

        setNavClass(settingsNavButton, activeButton == settingsNavButton);
        setNavClass(kitchenStockNavButton, activeButton == kitchenStockNavButton);
    }

    private void setNavClass(Button button, boolean active) {
        button.getStyleClass().removeAll("userdash-nav-button", "userdash-nav-active");
        button.getStyleClass().add(active ? "userdash-nav-active" : "userdash-nav-button");
    }

    private void showSettingsTab(VBox activePane) {
        profileSettingsPane.setVisible(activePane == profileSettingsPane);
        profileSettingsPane.setManaged(activePane == profileSettingsPane);
        securitySettingsPane.setVisible(activePane == securitySettingsPane);
        securitySettingsPane.setManaged(activePane == securitySettingsPane);
    }

    private void setSettingsTabClass(Button button, boolean active) {
        button.getStyleClass().removeAll("settings-tab-button", "settings-tab-active");
        button.getStyleClass().add(active ? "settings-tab-active" : "settings-tab-button");
    }

    @FXML
    private void toggleProfileMenu() {
        if (profileMenu == null) {
            setupProfileMenu();
        }

        if (profileMenu.isShowing()) {
            profileMenu.hide();
            return;
        }

        Bounds bounds = userProfileMenuButton.localToScreen(userProfileMenuButton.getBoundsInLocal());
        if (bounds == null) {
            profileMenu.show(userProfileMenuButton, Side.BOTTOM, 0, 8);
            return;
        }
        profileMenu.show(userProfileMenuButton, bounds.getMaxX() - 280, bounds.getMaxY() + 8);
    }

    @FXML
    private void handleChooseProfilePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Gambar", "*.jpg", "*.jpeg", "*.png")
        );

        File file = fileChooser.showOpenDialog(settingsAvatarImage.getScene().getWindow());
        if (file == null) {
            return;
        }

        if (file.length() > 2 * 1024 * 1024) {
            showWarning("Ukuran foto terlalu besar", "Pilih foto JPG atau PNG dengan ukuran maksimal 2MB.");
            return;
        }

        settingsAvatarImage.setImage(new Image(file.toURI().toString()));
        settingsAvatarImage.setVisible(true);
        settingsAvatarImage.setManaged(true);
        settingsAvatarInitial.setVisible(false);
        settingsAvatarInitial.setManaged(false);
    }

    @FXML
    private void handleSaveProfileSettings() {
        showSettingsToast("Perubahan profil keluarga berhasil disimpan.", true);
    }

    @FXML
    private void handleChangePassword() {
        if (currentUser == null) {
            showPasswordError("User belum terbaca. Silakan login ulang.");
            return;
        }

        String oldPassword = oldPasswordField.getText() == null ? "" : oldPasswordField.getText();
        String newPassword = newPasswordField.getText() == null ? "" : newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            showPasswordError("Semua field password wajib diisi.");
            return;
        }

        if (!PasswordUtil.verifyPassword(oldPassword, currentUser.getPassword())) {
            showPasswordError("Password lama tidak sesuai.");
            return;
        }

        if (newPassword.length() < 8) {
            showPasswordError("Password baru minimal 8 karakter.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showPasswordError("Password baru dan konfirmasi password harus sama.");
            return;
        }

        try {
            String passwordHash = PasswordUtil.hashPassword(newPassword);
            akunDAO.updatePassword(currentUser.getUserId(), passwordHash);
            currentUser.setPassword(passwordHash);
            hidePasswordError();
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            showSettingsToast("Password berhasil diubah.", true);
        } catch (SQLException e) {
            showPasswordError("Gagal menyimpan password baru. Coba lagi.");
            e.printStackTrace();
        }
    }

    private void showSettingsToast(String message, boolean success) {
        if (settingsToastTimer != null) {
            settingsToastTimer.stop();
        }

        settingsToastLabel.setText(message);
        settingsToastLabel.getStyleClass().setAll(
                "settings-toast",
                success ? "settings-toast-success" : "settings-toast-error"
        );
        settingsToastLabel.setVisible(true);
        settingsToastLabel.setManaged(true);

        settingsToastTimer = new PauseTransition(Duration.seconds(3));
        settingsToastTimer.setOnFinished(event -> {
            settingsToastLabel.setVisible(false);
            settingsToastLabel.setManaged(false);
        });
        settingsToastTimer.play();
    }

    private void showPasswordError(String message) {
        passwordErrorLabel.setText(message);
        passwordErrorLabel.setVisible(true);
        passwordErrorLabel.setManaged(true);
    }

    private void hidePasswordError() {
        passwordErrorLabel.setText("");
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);
    }



    private String formatStatus(String status) {
        if (status == null || status.isBlank()) {
            return "-";
        }

        return status.replace("_", " ");
    }

    private void refreshFamilyMembers() {
        if (currentUser == null) {
            familyMembers.clear();
            updateFamilySummary();
            updateFamilyTableHeight();
            return;
        }

        try {
            familyMembers.setAll(familyMemberDAO.listByUserId(currentUser.getUserId()));
            updateFamilySummary();
            updateFamilyTableHeight();
        } catch (IllegalStateException e) {
            showError("Gagal memuat profil keluarga", e.getMessage());
        }
    }

    private void updateFamilyTableHeight() {
        double headerHeight = 54;
        double bottomPadding = 18;
        double rowsHeight = familyMembers.isEmpty()
                ? 150
                : familyMembers.stream()
                        .mapToDouble(this::estimateFamilyRowHeight)
                        .sum();

        familyMemberTable.setPrefHeight(headerHeight + rowsHeight + bottomPadding);
    }

    private double estimateFamilyRowHeight(FamilyMember member) {
        int allergyCount = splitAllergies(member.getAllergyDisplay()).size();
        if (allergyCount == 0) {
            return 72;
        }

        int badgeRows = (int) Math.ceil(allergyCount / 2.0);
        return Math.max(72, 44 + (badgeRows * 38));
    }

    private void updateFamilySummary() {
        int total = familyMembers.size();
        familyCountSubtitle.setText(total + " anggota terdaftar");
        totalMembersLabel.setText(String.valueOf(total));
        dashboardFamilyIconCountLabel.setText(String.valueOf(total));
        dashboardFamilyCountLabel.setText(String.valueOf(total));
        updateFamilyAlert();

        allergySummaryBox.getChildren().clear();
        List<FamilyMember> membersWithAllergy = familyMembers.stream()
                .filter(FamilyMember::hasAllergy)
                .toList();

        if (membersWithAllergy.isEmpty()) {
            Label empty = new Label("Tidak ada alergi tercatat");
            empty.getStyleClass().add("family-muted-row");
            allergySummaryBox.getChildren().add(empty);
            return;
        }

        for (FamilyMember member : membersWithAllergy) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("family-allergy-row");

            Label name = new Label(member.getDisplayName());
            name.getStyleClass().add("family-allergy-name");
            FlowPane allergyBadges = createAllergyBadges(member.getAllergyDisplay());

            row.getChildren().addAll(name, allergyBadges);
            HBox.setHgrow(allergyBadges, javafx.scene.layout.Priority.ALWAYS);
            allergySummaryBox.getChildren().add(row);
        }
    }

    private void updateFamilyAlert() {
        familyAlertBox.getStyleClass().removeAll("family-alert", "family-alert-warning");
        familyAlertBox.getStyleClass().add(familyMembers.isEmpty() ? "family-alert-warning" : "family-alert");

        if (familyMembers.isEmpty()) {
            familyAlertIconLabel.setText("!");
            familyAlertTitle.setText("Profil keluarga belum lengkap");
            familyAlertSubtitle.setText("Tambahkan anggota keluarga agar rekomendasi menu bisa disesuaikan.");
            return;
        }

        boolean allRequiredDataComplete = familyMembers.stream().allMatch(member ->
                member.getName() != null && !member.getName().isBlank()
                        && member.getBirthDate() != null
                        && member.getHeight() > 0
                        && member.getWeight() > 0
        );

        if (allRequiredDataComplete) {
            familyAlertIconLabel.setText("OK");
            familyAlertTitle.setText("Profil siap untuk rekomendasi menu");
            familyAlertSubtitle.setText("Semua data anggota keluarga sudah lengkap");
        } else {
            familyAlertIconLabel.setText("!");
            familyAlertTitle.setText("Profil keluarga belum lengkap");
            familyAlertSubtitle.setText("Lengkapi nama, tanggal lahir, tinggi, dan berat setiap anggota keluarga.");
        }
    }

    @FXML
    private void handleAddFamilyMember() {
        showFamilyMemberDialog(null);
    }

    private void handleEditFamilyMember(FamilyMember member) {
        showFamilyMemberDialog(member);
    }

    private void handleDeleteFamilyMember(FamilyMember member) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Anggota Keluarga");
        confirm.setHeaderText("Hapus " + member.getDisplayName() + "?");
        confirm.setContentText("Data anggota keluarga ini akan dihapus dari profil.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK || currentUser == null) {
            return;
        }

        try {
            familyMemberDAO.delete(member.getMemberId(), currentUser.getUserId());
            refreshFamilyMembers();
        } catch (IllegalStateException e) {
            showError("Gagal menghapus anggota keluarga", e.getMessage());
        }
    }

    private void showFamilyMemberDialog(FamilyMember existingMember) {
        Dialog<FamilyMember> dialog = new Dialog<>();
        boolean editMode = existingMember != null;
        dialog.setTitle(editMode ? "Edit Anggota Keluarga" : "Tambah Anggota Keluarga");
        dialog.setHeaderText(editMode ? "Perbarui data anggota keluarga" : "Lengkapi data anggota keluarga");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Contoh: Budi");
        TextField relationshipField = new TextField();
        relationshipField.setPromptText("Ayah, Ibu, Anak");
        DatePicker birthDatePicker = new DatePicker();
        birthDatePicker.setPromptText("Tanggal lahir");
        TextField heightField = new TextField();
        heightField.setPromptText("170");
        TextField weightField = new TextField();
        weightField.setPromptText("72");
        TextArea allergyField = new TextArea();
        allergyField.setPromptText("Contoh: Udang, Kacang, Susu. Kosongkan jika tidak ada.");
        allergyField.setPrefRowCount(2);

        if (editMode) {
            nameField.setText(existingMember.getName());
            relationshipField.setText(existingMember.getRelationship() == null ? "" : existingMember.getRelationship());
            birthDatePicker.setValue(existingMember.getBirthDate());
            heightField.setText(existingMember.getHeightText());
            weightField.setText(existingMember.getWeightText());
            allergyField.setText(existingMember.getAllergy() == null ? "" : existingMember.getAllergy());
        }

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.add(new Label("Nama"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Hubungan"), 0, 1);
        form.add(relationshipField, 1, 1);
        form.add(new Label("Tanggal Lahir"), 0, 2);
        form.add(birthDatePicker, 1, 2);
        form.add(new Label("Tinggi (cm)"), 0, 3);
        form.add(heightField, 1, 3);
        form.add(new Label("Berat (kg)"), 0, 4);
        form.add(weightField, 1, 4);
        form.add(new Label("Alergi"), 0, 5);
        form.add(allergyField, 1, 5);
        dialog.getDialogPane().setContent(form);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String error = validateFamilyForm(nameField, birthDatePicker, heightField, weightField);
            if (error != null) {
                showWarning("Data belum valid", error);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != saveButtonType || currentUser == null) {
                return null;
            }

            FamilyMember member = editMode ? existingMember : new FamilyMember();
            member.setUserId(currentUser.getUserId());
            member.setName(nameField.getText().trim());
            member.setRelationship(normalizeNullable(relationshipField.getText()));
            member.setBirthDate(birthDatePicker.getValue());
            member.setHeight(Double.parseDouble(heightField.getText().trim()));
            member.setWeight(Double.parseDouble(weightField.getText().trim()));
            member.setAllergy(normalizeNullable(allergyField.getText()));
            return member;
        });

        Optional<FamilyMember> result = dialog.showAndWait();
        result.ifPresent(member -> {
            try {
                if (editMode) {
                    familyMemberDAO.update(member);
                } else {
                    familyMemberDAO.insert(member);
                }
                refreshFamilyMembers();
            } catch (IllegalStateException e) {
                showError("Gagal menyimpan anggota keluarga", e.getMessage());
            }
        });
    }

    private String validateFamilyForm(TextField nameField, DatePicker birthDatePicker,
                                      TextField heightField, TextField weightField) {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            return "Nama wajib diisi.";
        }

        LocalDate birthDate = birthDatePicker.getValue();
        if (birthDate == null) {
            return "Tanggal lahir wajib diisi.";
        }

        if (birthDate.isAfter(LocalDate.now())) {
            return "Tanggal lahir tidak boleh melebihi tanggal hari ini.";
        }

        if (!isPositiveNumber(heightField.getText())) {
            return "Tinggi harus berupa angka positif.";
        }

        if (!isPositiveNumber(weightField.getText())) {
            return "Berat harus berupa angka positif.";
        }

        return null;
    }

    private boolean isPositiveNumber(String value) {
        try {
            return Double.parseDouble(value.trim()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private javafx.scene.layout.Region createSpacer() {
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        return spacer;
    }

    private FlowPane createAllergyBadges(String allergyText) {
        FlowPane badges = new FlowPane();
        badges.setHgap(6);
        badges.setVgap(6);
        badges.setAlignment(Pos.CENTER);

        for (String allergy : splitAllergies(allergyText)) {
            Label badge = new Label(allergy);
            badge.getStyleClass().add("family-allergy-badge");
            badge.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            badge.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            badges.getChildren().add(badge);
        }

        return badges;
    }

    private List<String> splitAllergies(String allergyText) {
        if (allergyText == null || allergyText.trim().isEmpty() || "-".equals(allergyText.trim())) {
            return List.of();
        }

        return Arrays.stream(allergyText.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "Terjadi kesalahan tidak diketahui." : message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleStokDapur() {
        pageTitleLabel.setText("Stok Dapur");
        showPage(kitchenStockPage);
        setActiveNav(kitchenStockNavButton);
        if (kitchenStockContentController != null) {
            kitchenStockContentController.refreshData();
        }
    }

    @FXML
    private void handleLogout() {
        AppNavigator.showLogin();
    }

    @FXML
    private void handleOpenShoppingPlanner() {
        AppNavigator.showShoppingPlanner();
    }

    @FXML
    private void handleOpenRecipeForm() {
        AppNavigator.showRecipeForm();
    }

    private record BudgetOption(int budgetId, String name, double amount, LocalDate periodStart,
                                LocalDate periodEnd, String status) {
        @Override
        public String toString() {
            String statusText = status == null || status.isBlank()
                    ? "Aktif"
                    : status.substring(0, 1).toUpperCase(INDONESIAN_LOCALE) + status.substring(1).toLowerCase(INDONESIAN_LOCALE);
            return name + " - " + formatAmount(amount) + " (" + statusText + ")";
        }

        private static String formatAmount(double amount) {
            return "Rp" + String.format(INDONESIAN_LOCALE, "%,.0f", amount).replace(',', '.');
        }
    }

    private record RecipeNutrition(double calories,
                                   double protein,
                                   double carbohydrate,
                                   double fat,
                                   double fibre,
                                   boolean hasData) {
        private static RecipeNutrition empty() {
            return new RecipeNutrition(0, 0, 0, 0, 0, false);
        }

        private RecipeNutrition plus(RecipeNutrition other) {
            if (other == null || !other.hasData()) {
                return this;
            }

            return new RecipeNutrition(
                    calories + other.calories(),
                    protein + other.protein(),
                    carbohydrate + other.carbohydrate(),
                    fat + other.fat(),
                    fibre + other.fibre(),
                    true
            );
        }
    }

    private record WeeklySlotFormResult(boolean eatingOut, Integer recipeId, double outsideCost) {
    }

    private record MealSlotDefinition(String key, String label) {
    }

    private static class MultilineTextCell extends TableCell<FamilyMember, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : item);
        }
    }

    private static class CenteredTextCell extends TableCell<FamilyMember, String> {
        private CenteredTextCell() {
            setAlignment(Pos.CENTER);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : item);
        }
    }

    private class AllergyBadgeCell extends TableCell<FamilyMember, String> {
        private AllergyBadgeCell() {
            setAlignment(Pos.CENTER);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            if ("-".equals(item)) {
                setText("-");
                setGraphic(null);
                return;
            }

            setText(null);
            FlowPane badges = createAllergyBadges(item);
            badges.setPrefWrapLength(Math.max(120, allergyColumn.getWidth() - 26));
            setGraphic(badges);
        }
    }

    private static class BudgetStatusCell extends TableCell<Budget, String> {
        private BudgetStatusCell() {
            setAlignment(Pos.CENTER);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label badge = new Label(item);
            badge.getStyleClass().add("active".equalsIgnoreCase(item) || "Aktif".equalsIgnoreCase(item)
                    ? "budget-status-active"
                    : "budget-status-inactive");
            setText(null);
            setGraphic(badge);
        }
    }

    private class BudgetActionCell extends TableCell<Budget, Budget> {
        private final Button editButton = new Button("Edit");
        private final Button deleteButton = new Button("Hapus");
        private final HBox actions = new HBox(8, editButton, deleteButton);

        private BudgetActionCell() {
            setAlignment(Pos.CENTER);
            actions.setAlignment(Pos.CENTER);
            editButton.getStyleClass().add("family-edit-button");
            deleteButton.getStyleClass().add("family-delete-button");
        }

        @Override
        protected void updateItem(Budget budget, boolean empty) {
            super.updateItem(budget, empty);
            if (empty || budget == null) {
                setGraphic(null);
                return;
            }

            editButton.setOnAction(event -> editBudget(budget));
            deleteButton.setOnAction(event -> deleteBudget(budget));
            setGraphic(actions);
        }
    }

    private class ActionButtonCell extends TableCell<FamilyMember, FamilyMember> {
        private final HBox actions = new HBox(8);
        private final Button editButton = new Button("Edit");
        private final Button deleteButton = new Button("Hapus");

        private ActionButtonCell() {
            setAlignment(Pos.CENTER);
            actions.setAlignment(Pos.CENTER);
            editButton.getStyleClass().add("family-edit-button");
            deleteButton.getStyleClass().add("family-delete-button");
            actions.getChildren().addAll(editButton, deleteButton);
        }

        @Override
        protected void updateItem(FamilyMember member, boolean empty) {
            super.updateItem(member, empty);
            if (empty || member == null) {
                setGraphic(null);
                return;
            }

            editButton.setOnAction(event -> handleEditFamilyMember(member));
            deleteButton.setOnAction(event -> handleDeleteFamilyMember(member));
            setGraphic(actions);
        }
    }
}
