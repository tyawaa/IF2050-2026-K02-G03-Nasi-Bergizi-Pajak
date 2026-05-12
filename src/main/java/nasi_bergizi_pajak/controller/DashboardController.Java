package nasi_bergizi_pajak.controller;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.dao.FamilyMemberDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.model.FamilyMember;
import nasi_bergizi_pajak.model.MenuMingguan;
import nasi_bergizi_pajak.model.SlotMakan;

public class DashboardController {
    private static final List<String> MEAL_TIME_OPTIONS = List.of("breakfast", "lunch", "dinner", "snack");

    @FXML private Label welcomeLabel;
    @FXML private Label emailLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Label dashboardFamilyIconCountLabel;
    @FXML private Label dashboardFamilyCountLabel;
    @FXML private Button dashboardNavButton;
    @FXML private Button familyProfileNavButton;
    @FXML private Button weeklyMenuNavButton;
    @FXML private VBox dashboardPage;
    @FXML private VBox familyProfilePage;
    @FXML private VBox weeklyMenuPage;
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

    @FXML private ComboBox<MenuMingguan> weeklyMenuComboBox;
    @FXML private Label weeklyMenuPeriodLabel;
    @FXML private Label weeklyMenuTotalLabel;
    @FXML private Label weeklyMenuStatusLabel;
    @FXML private Label weeklyMenuSlotCountLabel;
    @FXML private GridPane weeklyMenuGrid;
    @FXML private TableView<SlotMakan> weeklySlotTable;
    @FXML private TableColumn<SlotMakan, String> weeklyDateColumn;
    @FXML private TableColumn<SlotMakan, String> weeklyTimeColumn;
    @FXML private TableColumn<SlotMakan, String> weeklyTypeColumn;
    @FXML private TableColumn<SlotMakan, String> weeklyRecipeColumn;
    @FXML private TableColumn<SlotMakan, String> weeklyCostColumn;
    @FXML private TableColumn<SlotMakan, SlotMakan> weeklyActionColumn;

    private final FamilyMemberDAO familyMemberDAO = new FamilyMemberDAO();
    private final MenuController menuController = new MenuController();
    private final ObservableList<FamilyMember> familyMembers = FXCollections.observableArrayList();
    private final ObservableList<MenuMingguan> weeklyMenus = FXCollections.observableArrayList();
    private final ObservableList<SlotMakan> weeklySlots = FXCollections.observableArrayList();
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private Akun currentUser;
    private MenuMingguan selectedWeeklyMenu;

    @FXML
    private void initialize() {
        currentUser = AppNavigator.getCurrentUser();
        initializeUserInfo();
        setupFamilyTable();
        setupWeeklyMenuComboBox();
        setupWeeklySlotTable();
        refreshFamilyMembers();
        refreshWeeklyMenus();
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
    private void showWeeklyMenuPage() {
        pageTitleLabel.setText("Menu Mingguan");
        showPage(weeklyMenuPage);
        setActiveNav(weeklyMenuNavButton);
        refreshWeeklyMenus();
    }

    private void showPage(VBox page) {
        dashboardPage.setVisible(page == dashboardPage);
        dashboardPage.setManaged(page == dashboardPage);
        familyProfilePage.setVisible(page == familyProfilePage);
        familyProfilePage.setManaged(page == familyProfilePage);
        weeklyMenuPage.setVisible(page == weeklyMenuPage);
        weeklyMenuPage.setManaged(page == weeklyMenuPage);
    }

    private void setActiveNav(Button activeButton) {
        setNavClass(dashboardNavButton, activeButton == dashboardNavButton);
        setNavClass(familyProfileNavButton, activeButton == familyProfileNavButton);
        setNavClass(weeklyMenuNavButton, activeButton == weeklyMenuNavButton);
    }

    private void setNavClass(Button button, boolean active) {
        button.getStyleClass().removeAll("userdash-nav-button", "userdash-nav-active");
        button.getStyleClass().add(active ? "userdash-nav-active" : "userdash-nav-button");
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

    private void setupWeeklyMenuComboBox() {
        weeklyMenuComboBox.setItems(weeklyMenus);
        weeklyMenuComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MenuMingguan menu) {
                if (menu == null) {
                    return "";
                }
                return menu.getWeekStartDate() + " - " + menu.getWeekEndDate();
            }

            @Override
            public MenuMingguan fromString(String text) {
                return null;
            }
        });

        weeklyMenuComboBox.setOnAction(event -> {
            MenuMingguan selected = weeklyMenuComboBox.getValue();
            if (selected != null) {
                selectedWeeklyMenu = selected;
                refreshSelectedWeeklyMenu();
            }
        });
    }

    private void setupWeeklySlotTable() {
        weeklySlotTable.setItems(weeklySlots);
        weeklySlotTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        weeklyDateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getMealDate())));
        weeklyTimeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(toTitleCase(data.getValue().getMealTime())));
        weeklyTypeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().isEatingOut() ? "Makan di luar" : "Resep"));
        weeklyRecipeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().isEatingOut() ? "-" : "Resep #" + data.getValue().getRecipeId()
        ));
        weeklyCostColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().isEatingOut()
                        ? formatRupiah(data.getValue().getOutsideCost())
                        : "Otomatis dari resep"
        ));
        weeklyActionColumn.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyObjectWrapper<>(data.getValue()));
        weeklyActionColumn.setCellFactory(column -> new WeeklySlotActionCell());
    }

    @FXML
    private void handleRefreshWeeklyMenus() {
        refreshWeeklyMenus();
    }

    private void refreshWeeklyMenus() {
        if (currentUser == null) {
            weeklyMenus.clear();
            weeklySlots.clear();
            selectedWeeklyMenu = null;
            updateWeeklySummary();
            renderWeeklyMenuGrid();
            return;
        }

        try {
            weeklyMenus.setAll(menuController.ambilMenuMingguanPengguna(currentUser.getUserId()));

            if (weeklyMenus.isEmpty()) {
                selectedWeeklyMenu = null;
                weeklyMenuComboBox.setValue(null);
                weeklySlots.clear();
                updateWeeklySummary();
                renderWeeklyMenuGrid();
                return;
            }

            MenuMingguan menuToSelect = selectedWeeklyMenu == null
                    ? weeklyMenus.get(0)
                    : weeklyMenus.stream()
                            .filter(menu -> menu.getMenuId() == selectedWeeklyMenu.getMenuId())
                            .findFirst()
                            .orElse(weeklyMenus.get(0));

            selectedWeeklyMenu = menuToSelect;
            weeklyMenuComboBox.setValue(menuToSelect);
            refreshSelectedWeeklyMenu();
        } catch (SQLException e) {
            showError("Gagal memuat menu mingguan", e.getMessage());
        }
    }

    private void refreshSelectedWeeklyMenu() {
        if (selectedWeeklyMenu == null) {
            weeklySlots.clear();
            updateWeeklySummary();
            renderWeeklyMenuGrid();
            return;
        }

        try {
            MenuMingguan refreshed = menuController.ambilMenuMingguan(selectedWeeklyMenu.getMenuId());
            if (refreshed != null) {
                selectedWeeklyMenu = refreshed;
            }

            weeklySlots.setAll(menuController.ambilSlotMakan(selectedWeeklyMenu.getMenuId()));
            updateWeeklySummary();
            renderWeeklyMenuGrid();
        } catch (SQLException e) {
            showError("Gagal memuat detail menu mingguan", e.getMessage());
        }
    }

    private void updateWeeklySummary() {
        if (selectedWeeklyMenu == null) {
            weeklyMenuPeriodLabel.setText("Belum ada menu mingguan dipilih.");
            weeklyMenuTotalLabel.setText(formatRupiah(0));
            weeklyMenuStatusLabel.setText("draft");
            weeklyMenuSlotCountLabel.setText("0 slot");
            return;
        }

        weeklyMenuPeriodLabel.setText(
                "Minggu: " + selectedWeeklyMenu.getWeekStartDate() + " - " + selectedWeeklyMenu.getWeekEndDate()
        );
        weeklyMenuTotalLabel.setText(formatRupiah(selectedWeeklyMenu.getTotalEstimation()));
        weeklyMenuStatusLabel.setText(toTitleCase(selectedWeeklyMenu.getStatusBudget()));
        weeklyMenuSlotCountLabel.setText(weeklySlots.size() + " slot");
    }

    private void renderWeeklyMenuGrid() {
        weeklyMenuGrid.getChildren().clear();

        Label corner = new Label("Waktu");
        corner.getStyleClass().add("userdash-section-subtitle");
        weeklyMenuGrid.add(corner, 0, 0);

        if (selectedWeeklyMenu == null) {
            Label empty = new Label("Buat atau pilih menu mingguan untuk melihat grid slot makan.");
            empty.setWrapText(true);
            empty.getStyleClass().add("userdash-section-subtitle");
            weeklyMenuGrid.add(empty, 1, 0);
            return;
        }

        LocalDate currentDate = selectedWeeklyMenu.getWeekStartDate();
        int dayColumn = 1;

        while (!currentDate.isAfter(selectedWeeklyMenu.getWeekEndDate())) {
            Label dayHeader = new Label(currentDate.getDayOfWeek() + "\n" + currentDate);
            dayHeader.setWrapText(true);
            dayHeader.getStyleClass().add("userdash-section-subtitle");
            weeklyMenuGrid.add(dayHeader, dayColumn, 0);
            currentDate = currentDate.plusDays(1);
            dayColumn++;
        }

        for (int row = 0; row < MEAL_TIME_OPTIONS.size(); row++) {
            String mealTime = MEAL_TIME_OPTIONS.get(row);
            Label rowLabel = new Label(toTitleCase(mealTime));
            rowLabel.getStyleClass().add("userdash-section-subtitle");
            weeklyMenuGrid.add(rowLabel, 0, row + 1);

            LocalDate dateCursor = selectedWeeklyMenu.getWeekStartDate();
            int column = 1;
            while (!dateCursor.isAfter(selectedWeeklyMenu.getWeekEndDate())) {
                VBox card = createWeeklyGridCard(dateCursor, mealTime);
                weeklyMenuGrid.add(card, column, row + 1);
                dateCursor = dateCursor.plusDays(1);
                column++;
            }
        }
    }

    private VBox createWeeklyGridCard(LocalDate date, String mealTime) {
        VBox card = new VBox(4);
        card.setMinWidth(118);
        card.setPrefWidth(128);
        card.setPadding(new Insets(8));
        card.getStyleClass().add("userdash-budget-box");

        Optional<SlotMakan> slot = weeklySlots.stream()
                .filter(item -> date.equals(item.getMealDate()) && mealTime.equalsIgnoreCase(item.getMealTime()))
                .findFirst();

        if (slot.isEmpty()) {
            Label empty = new Label("+");
            empty.getStyleClass().add("userdash-section-subtitle");
            card.getChildren().add(empty);
            return card;
        }

        SlotMakan item = slot.get();
        Label title = new Label(item.isEatingOut() ? "Makan di luar" : "Resep #" + item.getRecipeId());
        title.setWrapText(true);
        title.getStyleClass().add("userdash-stock-title");

        Label subtitle = new Label(item.isEatingOut()
                ? formatRupiah(item.getOutsideCost())
                : "Estimasi resep");
        subtitle.setWrapText(true);
        subtitle.getStyleClass().add("userdash-stock-subtitle");

        card.getChildren().addAll(title, subtitle);
        return card;
    }

    @FXML
    private void handleCreateWeeklyMenu() {
        if (currentUser == null) {
            showWarning("Belum login", "Pengguna tidak ditemukan.");
            return;
        }

        Dialog<MenuMingguan> dialog = new Dialog<>();
        dialog.setTitle("Buat Menu Mingguan");
        dialog.setHeaderText("Masukkan periode dan parameter planner.");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField parameterIdField = new TextField();
        parameterIdField.setPromptText("Contoh: 1");
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.add(new Label("Parameter ID"), 0, 0);
        form.add(parameterIdField, 1, 0);
        form.add(new Label("Tanggal Mulai"), 0, 1);
        form.add(startDatePicker, 1, 1);
        form.add(new Label("Tanggal Selesai"), 0, 2);
        form.add(endDatePicker, 1, 2);
        dialog.getDialogPane().setContent(form);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String error = validateWeeklyMenuForm(parameterIdField, startDatePicker, endDatePicker);
            if (error != null) {
                showWarning("Data belum valid", error);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != saveButtonType) {
                return null;
            }

            try {
                int parameterId = Integer.parseInt(parameterIdField.getText().trim());
                return menuController.buatMenuMingguan(
                        currentUser.getUserId(),
                        parameterId,
                        startDatePicker.getValue(),
                        endDatePicker.getValue()
                );
            } catch (Exception e) {
                showError("Gagal membuat menu mingguan", e.getMessage());
                return null;
            }
        });

        dialog.showAndWait().ifPresent(menu -> {
            selectedWeeklyMenu = menu;
            refreshWeeklyMenus();
        });
    }

    private String validateWeeklyMenuForm(TextField parameterIdField, DatePicker startDatePicker, DatePicker endDatePicker) {
        if (!isPositiveInteger(parameterIdField.getText())) {
            return "Parameter ID harus berupa angka positif.";
        }

        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            return "Tanggal mulai dan selesai wajib diisi.";
        }

        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            return "Tanggal selesai tidak boleh sebelum tanggal mulai.";
        }

        return null;
    }

    @FXML
    private void handleAddRecipeSlot() {
        if (selectedWeeklyMenu == null) {
            showWarning("Pilih menu dulu", "Pilih atau buat menu mingguan sebelum menambah slot.");
            return;
        }

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Tambah Slot Resep");
        dialog.setHeaderText("Tambahkan resep ke slot makan.");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField recipeIdField = new TextField();
        recipeIdField.setPromptText("Contoh: 1");
        DatePicker datePicker = new DatePicker(selectedWeeklyMenu.getWeekStartDate());
        ComboBox<String> mealTimeCombo = createMealTimeComboBox();

        GridPane form = createSlotForm(recipeIdField, datePicker, mealTimeCombo, null);
        dialog.getDialogPane().setContent(form);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!isPositiveInteger(recipeIdField.getText())) {
                showWarning("Data belum valid", "Recipe ID harus berupa angka positif.");
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != saveButtonType) {
                return false;
            }

            try {
                menuController.tambahSlotResep(
                        selectedWeeklyMenu.getMenuId(),
                        Integer.parseInt(recipeIdField.getText().trim()),
                        datePicker.getValue(),
                        mealTimeCombo.getValue()
                );
                return true;
            } catch (Exception e) {
                showError("Gagal menambah slot resep", e.getMessage());
                return false;
            }
        });

        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(saved -> refreshSelectedWeeklyMenu());
    }

    @FXML
    private void handleAddEatingOutSlot() {
        if (selectedWeeklyMenu == null) {
            showWarning("Pilih menu dulu", "Pilih atau buat menu mingguan sebelum menambah slot.");
            return;
        }

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Tambah Slot Makan di Luar");
        dialog.setHeaderText("Tambahkan biaya makan di luar untuk slot tertentu.");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField costField = new TextField();
        costField.setPromptText("Contoh: 25000");
        DatePicker datePicker = new DatePicker(selectedWeeklyMenu.getWeekStartDate());
        ComboBox<String> mealTimeCombo = createMealTimeComboBox();

        GridPane form = createSlotForm(null, datePicker, mealTimeCombo, costField);
        dialog.getDialogPane().setContent(form);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!isNonNegativeNumber(costField.getText())) {
                showWarning("Data belum valid", "Biaya makan di luar harus berupa angka nol atau lebih.");
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != saveButtonType) {
                return false;
            }

            try {
                menuController.tambahSlotMakanDiLuar(
                        selectedWeeklyMenu.getMenuId(),
                        datePicker.getValue(),
                        mealTimeCombo.getValue(),
                        Double.parseDouble(costField.getText().trim())
                );
                return true;
            } catch (Exception e) {
                showError("Gagal menambah slot makan di luar", e.getMessage());
                return false;
            }
        });

        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(saved -> refreshSelectedWeeklyMenu());
    }

    private GridPane createSlotForm(TextField recipeIdField, DatePicker datePicker, ComboBox<String> mealTimeCombo, TextField costField) {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        int row = 0;
        if (recipeIdField != null) {
            form.add(new Label("Recipe ID"), 0, row);
            form.add(recipeIdField, 1, row++);
        }

        if (costField != null) {
            form.add(new Label("Biaya"), 0, row);
            form.add(costField, 1, row++);
        }

        form.add(new Label("Tanggal"), 0, row);
        form.add(datePicker, 1, row++);
        form.add(new Label("Waktu"), 0, row);
        form.add(mealTimeCombo, 1, row);
        return form;
    }

    private ComboBox<String> createMealTimeComboBox() {
        ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(MEAL_TIME_OPTIONS));
        comboBox.setValue("breakfast");
        return comboBox;
    }

    private void handleEditWeeklySlot(SlotMakan slot) {
        if (slot == null || selectedWeeklyMenu == null) {
            return;
        }

        if (slot.isEatingOut()) {
            editEatingOutSlot(slot);
        } else {
            editRecipeSlot(slot);
        }
    }

    private void editRecipeSlot(SlotMakan slot) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Slot Resep");
        dialog.setHeaderText("Perbarui resep dan waktu slot.");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField recipeIdField = new TextField(slot.getRecipeId() == null ? "" : String.valueOf(slot.getRecipeId()));
        DatePicker datePicker = new DatePicker(slot.getMealDate());
        ComboBox<String> mealTimeCombo = createMealTimeComboBox();
        mealTimeCombo.setValue(slot.getMealTime());

        dialog.getDialogPane().setContent(createSlotForm(recipeIdField, datePicker, mealTimeCombo, null));

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!isPositiveInteger(recipeIdField.getText())) {
                showWarning("Data belum valid", "Recipe ID harus berupa angka positif.");
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != saveButtonType) {
                return false;
            }

            try {
                menuController.ubahSlotResep(
                        slot.getSlotId(),
                        selectedWeeklyMenu.getMenuId(),
                        Integer.parseInt(recipeIdField.getText().trim()),
                        datePicker.getValue(),
                        mealTimeCombo.getValue()
                );
                return true;
            } catch (Exception e) {
                showError("Gagal memperbarui slot resep", e.getMessage());
                return false;
            }
        });

        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(saved -> refreshSelectedWeeklyMenu());
    }

    private void editEatingOutSlot(SlotMakan slot) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Slot Makan di Luar");
        dialog.setHeaderText("Perbarui biaya dan waktu slot makan di luar.");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField costField = new TextField(String.valueOf(slot.getOutsideCost()));
        DatePicker datePicker = new DatePicker(slot.getMealDate());
        ComboBox<String> mealTimeCombo = createMealTimeComboBox();
        mealTimeCombo.setValue(slot.getMealTime());

        dialog.getDialogPane().setContent(createSlotForm(null, datePicker, mealTimeCombo, costField));

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!isNonNegativeNumber(costField.getText())) {
                showWarning("Data belum valid", "Biaya makan di luar harus berupa angka nol atau lebih.");
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != saveButtonType) {
                return false;
            }

            try {
                menuController.ubahSlotMakanDiLuar(
                        slot.getSlotId(),
                        selectedWeeklyMenu.getMenuId(),
                        datePicker.getValue(),
                        mealTimeCombo.getValue(),
                        Double.parseDouble(costField.getText().trim())
                );
                return true;
            } catch (Exception e) {
                showError("Gagal memperbarui slot makan di luar", e.getMessage());
                return false;
            }
        });

        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(saved -> refreshSelectedWeeklyMenu());
    }

    private void handleDeleteWeeklySlot(SlotMakan slot) {
        if (slot == null || selectedWeeklyMenu == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Slot Makan");
        confirm.setHeaderText("Hapus slot makan ini?");
        confirm.setContentText("Estimasi menu akan dihitung ulang setelah slot dihapus.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            menuController.hapusSlotMakan(selectedWeeklyMenu.getMenuId(), slot.getSlotId());
            refreshSelectedWeeklyMenu();
        } catch (Exception e) {
            showError("Gagal menghapus slot makan", e.getMessage());
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

    private boolean isNonNegativeNumber(String value) {
        try {
            return Double.parseDouble(value.trim()) >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value.trim()) > 0;
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

    private String formatRupiah(double value) {
        return rupiahFormat.format(value);
    }

    private String toTitleCase(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        String lower = value.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1).replace("_", " ");
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

    private class WeeklySlotActionCell extends TableCell<SlotMakan, SlotMakan> {
        private final HBox actions = new HBox(8);
        private final Button editButton = new Button("Edit");
        private final Button deleteButton = new Button("Hapus");

        private WeeklySlotActionCell() {
            setAlignment(Pos.CENTER);
            actions.setAlignment(Pos.CENTER);
            editButton.getStyleClass().add("family-edit-button");
            deleteButton.getStyleClass().add("family-delete-button");
            actions.getChildren().addAll(editButton, deleteButton);
        }

        @Override
        protected void updateItem(SlotMakan slot, boolean empty) {
            super.updateItem(slot, empty);
            if (empty || slot == null) {
                setGraphic(null);
                return;
            }

            editButton.setOnAction(event -> handleEditWeeklySlot(slot));
            deleteButton.setOnAction(event -> handleDeleteWeeklySlot(slot));
            setGraphic(actions);
        }
    }
}
