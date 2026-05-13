package nasi_bergizi_pajak.controller;

import java.io.File;
import java.text.NumberFormat;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.dao.AkunDAO;
import nasi_bergizi_pajak.dao.FamilyMemberDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.model.Budget;
import nasi_bergizi_pajak.model.FamilyMember;
import nasi_bergizi_pajak.model.RekomendasiMenu;
import nasi_bergizi_pajak.model.MenuMingguan;
import nasi_bergizi_pajak.model.SlotMakan;
import nasi_bergizi_pajak.util.PasswordUtil;

public class DashboardController {
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
    @FXML private Button kitchenStockNavButton;
    @FXML private Button budgetResetButton;
    @FXML private Button budgetSaveButton;
    @FXML private Button settingsNavButton;
    @FXML private Button profileSettingsTabButton;
    @FXML private Button securitySettingsTabButton;
    @FXML private HBox userProfileMenuButton;
    @FXML private VBox dashboardPage;
    @FXML private VBox familyProfilePage;
    @FXML private VBox budgetPage;
    @FXML private VBox kitchenStockPage;
    @FXML private KitchenStockViewController kitchenStockContentController;
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
    @FXML private Label weeklyMenuCountLabel;
    @FXML private Label weeklySlotCountLabel;
    @FXML private Label weeklyMenuTotalLabel;
    @FXML private Label weeklyMenuStatusLabel;
    @FXML private TableView<MenuMingguan> weeklyMenuTable;
    @FXML private TableColumn<MenuMingguan, String> weeklyMenuIdColumn;
    @FXML private TableColumn<MenuMingguan, String> weeklyMenuPeriodColumn;
    @FXML private TableColumn<MenuMingguan, String> weeklyMenuEstimationColumn;
    @FXML private TableColumn<MenuMingguan, String> weeklyMenuBudgetStatusColumn;
    @FXML private TableView<SlotMakan> weeklySlotTable;
    @FXML private TableColumn<SlotMakan, String> weeklySlotDateColumn;
    @FXML private TableColumn<SlotMakan, String> weeklySlotTimeColumn;
    @FXML private TableColumn<SlotMakan, String> weeklySlotTypeColumn;
    @FXML private TableColumn<SlotMakan, String> weeklySlotCostColumn;
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
    @FXML private Button recommendationNavButton;
    @FXML private VBox recommendationPage;
    @FXML private Label recommendationCountLabel;
    @FXML private Label recommendationStatusLabel;
    @FXML private TableView<RekomendasiMenu> recommendationTable;
    @FXML private TableColumn<RekomendasiMenu, String> recommendationRecipeColumn;
    @FXML private TableColumn<RekomendasiMenu, String> recommendationServingColumn;
    @FXML private TableColumn<RekomendasiMenu, String> recommendationPriceColumn;
    @FXML private TableColumn<RekomendasiMenu, String> recommendationNutritionColumn;
    @FXML private TableColumn<RekomendasiMenu, String> recommendationBudgetColumn;
    @FXML private TableColumn<RekomendasiMenu, String> recommendationStockColumn;
    @FXML private TableColumn<RekomendasiMenu, String> recommendationScoreColumn;

    private final AkunDAO akunDAO = new AkunDAO();
    private final FamilyMemberDAO familyMemberDAO = new FamilyMemberDAO();
    private final ObservableList<FamilyMember> familyMembers = FXCollections.observableArrayList();
    private final ObservableList<Budget> budgets = FXCollections.observableArrayList();
    private final BudgetController budgetController = new BudgetController();
    private final ObservableList<MenuMingguan> weeklyMenus = FXCollections.observableArrayList();
    private final ObservableList<SlotMakan> weeklySlots = FXCollections.observableArrayList();
    private final MenuController menuController = new MenuController();
    private final ObservableList<RekomendasiMenu> recommendations = FXCollections.observableArrayList();
    private final RekomendasiController rekomendasiController = new RekomendasiController();
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
    private final DateTimeFormatter budgetDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.of("id", "ID"));
    private Akun currentUser;
    private Budget selectedBudget;
    private ContextMenu profileMenu;
    private PauseTransition settingsToastTimer;

    @FXML
    private void initialize() {
        currentUser = AppNavigator.getCurrentUser();
        initializeUserInfo();
        initializeSettingsForm();
        setupProfileMenu();
        setupFamilyTable();
        setupBudgetPage();
        setupWeeklyMenuTables();
        setupRecommendationTable();
        refreshFamilyMembers();
        refreshBudgets();
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

    private void setupBudgetPage() {
        budgetStatusComboBox.setItems(FXCollections.observableArrayList("active", "inactive"));
        budgetStatusComboBox.getSelectionModel().select("active");
        budgetStartDatePicker.setValue(LocalDate.now());
        budgetEndDatePicker.setValue(LocalDate.now().plusMonths(1));

        budgetTable.setItems(budgets);
        budgetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        budgetTable.setPlaceholder(createBudgetEmptyState());

        budgetNameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getName()));
        budgetAmountColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(rupiahFormat.format(data.getValue().getAmount())));
        budgetPeriodColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(formatBudgetPeriod(data.getValue())));
        budgetStatusColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(formatStatus(data.getValue().getStatus())));
        budgetActionColumn.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(data.getValue()));
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
            refreshBudgets();
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
        refreshBudgets();
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
            refreshBudgets();
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

    private void setupWeeklyMenuTables() {
        weeklyMenuTable.setItems(weeklyMenus);
        weeklySlotTable.setItems(weeklySlots);

        weeklyMenuTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        weeklySlotTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        weeklyMenuIdColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getMenuId())));

        weeklyMenuPeriodColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getWeekStartDate() + " s.d. " + data.getValue().getWeekEndDate()
                ));

        weeklyMenuEstimationColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(rupiahFormat.format(data.getValue().getTotalEstimation())));

        weeklyMenuBudgetStatusColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(formatStatus(data.getValue().getStatusBudget())));

        weeklySlotDateColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getMealDate())));

        weeklySlotTimeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(formatStatus(data.getValue().getMealTime())));

        weeklySlotTypeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().isEatingOut() ? "Makan di luar" : "Resep #" + data.getValue().getRecipeId()));

        weeklySlotCostColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().isEatingOut()
                                ? rupiahFormat.format(data.getValue().getOutsideCost())
                                : "-"
                ));

        weeklyMenuTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedMenu) -> {
            if (selectedMenu != null) {
                loadWeeklySlots(selectedMenu);
            }
        });
    }

    @FXML
    private void handleRefreshWeeklyMenus() {
        refreshWeeklyMenus();
    }

    private void refreshWeeklyMenus() {
        if (currentUser == null) {
            weeklyMenus.clear();
            weeklySlots.clear();
            weeklyMenuCountLabel.setText("0 menu");
            weeklySlotCountLabel.setText("0 slot");
            weeklyMenuTotalLabel.setText(rupiahFormat.format(0));
            weeklyMenuStatusLabel.setText("User tidak ditemukan");
            return;
        }

        try {
            List<MenuMingguan> menus = menuController.ambilMenuMingguanPengguna(currentUser.getUserId());
            weeklyMenus.setAll(menus);
            weeklyMenuCountLabel.setText(menus.size() + " menu");

            if (menus.isEmpty()) {
                weeklySlots.clear();
                weeklySlotCountLabel.setText("0 slot");
                weeklyMenuTotalLabel.setText(rupiahFormat.format(0));
                weeklyMenuStatusLabel.setText("Belum ada menu");
                return;
            }

            weeklyMenuTable.getSelectionModel().selectFirst();
            loadWeeklySlots(menus.get(0));
        } catch (SQLException e) {
            weeklyMenus.clear();
            weeklySlots.clear();
            weeklyMenuCountLabel.setText("0 menu");
            weeklySlotCountLabel.setText("0 slot");
            weeklyMenuTotalLabel.setText(rupiahFormat.format(0));
            weeklyMenuStatusLabel.setText("Gagal memuat");
            showError("Gagal memuat menu mingguan", e.getMessage());
        }
    }

    private void loadWeeklySlots(MenuMingguan menu) {
        try {
            List<SlotMakan> slots = menuController.ambilSlotMakan(menu.getMenuId());
            weeklySlots.setAll(slots);
            weeklySlotCountLabel.setText(slots.size() + " slot");
            weeklyMenuTotalLabel.setText(rupiahFormat.format(menu.getTotalEstimation()));
            weeklyMenuStatusLabel.setText(formatStatus(menu.getStatusBudget()));
        } catch (SQLException e) {
            weeklySlots.clear();
            weeklySlotCountLabel.setText("0 slot");
            weeklyMenuTotalLabel.setText(rupiahFormat.format(menu.getTotalEstimation()));
            weeklyMenuStatusLabel.setText("Gagal memuat slot");
            showError("Gagal memuat slot menu", e.getMessage());
        }
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
        refreshBudgets();
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
    @FXML
    private void showRecommendationPage() {
        pageTitleLabel.setText("Rekomendasi Menu");
        showPage(recommendationPage);
        setActiveNav(recommendationNavButton);
        refreshRecommendations();
    }

    private void showPage(VBox page) {
        dashboardPage.setVisible(page == dashboardPage);
        dashboardPage.setManaged(page == dashboardPage);

        familyProfilePage.setVisible(page == familyProfilePage);
        familyProfilePage.setManaged(page == familyProfilePage);

        budgetPage.setVisible(page == budgetPage);
        budgetPage.setManaged(page == budgetPage);

        kitchenStockPage.setVisible(page == kitchenStockPage);
        kitchenStockPage.setManaged(page == kitchenStockPage);

        weeklyMenuPage.setVisible(page == weeklyMenuPage);
        weeklyMenuPage.setManaged(page == weeklyMenuPage);

        settingsPage.setVisible(page == settingsPage);
        settingsPage.setManaged(page == settingsPage);

        recommendationPage.setVisible(page == recommendationPage);
        recommendationPage.setManaged(page == recommendationPage);
    }

    private void setActiveNav(Button activeButton) {
        setNavClass(dashboardNavButton, activeButton == dashboardNavButton);
        setNavClass(familyProfileNavButton, activeButton == familyProfileNavButton);
        setNavClass(budgetNavButton, activeButton == budgetNavButton);
        setNavClass(kitchenStockNavButton, activeButton == kitchenStockNavButton);
        setNavClass(weeklyMenuNavButton, activeButton == weeklyMenuNavButton);

        setNavClass(settingsNavButton, activeButton == settingsNavButton);
        setNavClass(recommendationNavButton, activeButton == recommendationNavButton);
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

    private void setupRecommendationTable() {
        recommendationTable.setItems(recommendations);
        recommendationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        recommendationRecipeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getRecipe().getName()));

        recommendationServingColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getRecipe().getServingSize())));

        recommendationPriceColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(rupiahFormat.format(data.getValue().getEstimasiHarga())));

        recommendationNutritionColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(formatStatus(data.getValue().getStatus())));

        recommendationBudgetColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(formatStatus(data.getValue().getStatusBudget())));

        recommendationStockColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        formatStatus(data.getValue().getStatusStok())
                                + " (" + String.format(Locale.US, "%.0f", data.getValue().getPersentaseStok() * 100) + "%)"
                ));

        recommendationScoreColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.format(Locale.US, "%.2f", data.getValue().getSkor())));
    }

    @FXML
    private void handleRefreshRecommendations() {
        refreshRecommendations();
    }

    private void refreshRecommendations() {
        if (currentUser == null) {
            recommendations.clear();
            recommendationCountLabel.setText("0 menu");
            recommendationStatusLabel.setText("User tidak ditemukan");
            return;
        }

        try {
            List<RekomendasiMenu> hasil = rekomendasiController.buatRekomendasiMenu(currentUser.getUserId());
            recommendations.setAll(hasil);
            recommendationCountLabel.setText(hasil.size() + " menu");
            recommendationStatusLabel.setText(hasil.isEmpty() ? "Belum ada data cocok" : "Rekomendasi siap");
        } catch (IllegalStateException e) {
            recommendations.clear();
            recommendationCountLabel.setText("0 menu");
            recommendationStatusLabel.setText("Data belum lengkap");
            showWarning("Rekomendasi belum tersedia", e.getMessage());
        } catch (SQLException e) {
            recommendations.clear();
            recommendationCountLabel.setText("0 menu");
            recommendationStatusLabel.setText("Gagal memuat");
            showError("Gagal memuat rekomendasi", e.getMessage());
        }
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

    @FXML
    private void handleLogout() {
        AppNavigator.showLogin();
    }

    @FXML
    private void handleOpenRecipeForm() {
        AppNavigator.showRecipeForm();
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
