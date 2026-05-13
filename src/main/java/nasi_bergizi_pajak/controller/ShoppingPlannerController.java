package nasi_bergizi_pajak.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.dao.ShoppingPlannerDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.model.ShoppingItem;
import nasi_bergizi_pajak.model.ShoppingPlanner;
import nasi_bergizi_pajak.model.WeeklyMenuOption;

public class ShoppingPlannerController {
    private final ShoppingPlannerDAO shoppingPlannerDAO = new ShoppingPlannerDAO();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

    @FXML private ComboBox<WeeklyMenuOption> menuComboBox;
    @FXML private Label topbarAvatarText;
    @FXML private Label welcomeLabel;
    @FXML private Label emailLabel;
    @FXML private Label plannerStatusLabel;
    @FXML private Label estimationLabel;
    @FXML private Label budgetLabel;
    @FXML private Label actualLabel;
    @FXML private Label messageLabel;
    @FXML private TableView<ShoppingItem> itemTable;
    @FXML private TableColumn<ShoppingItem, String> ingredientColumn;
    @FXML private TableColumn<ShoppingItem, String> quantityColumn;
    @FXML private TableColumn<ShoppingItem, String> estimationColumn;
    @FXML private TableColumn<ShoppingItem, String> actualColumn;
    @FXML private TableColumn<ShoppingItem, Boolean> boughtColumn;
    @FXML private TextField actualPriceField;
    @FXML private CheckBox boughtCheckBox;
    @FXML private Button processButton;

    private ShoppingPlanner activePlanner;

    @FXML
    private void initialize() {
        initializeUserInfo();
        setupTable();
        loadMenus();

        itemTable.getSelectionModel().selectedItemProperty().addListener((observable, oldItem, newItem) -> {
            if (newItem == null) {
                actualPriceField.clear();
                boughtCheckBox.setSelected(false);
                return;
            }

            actualPriceField.setText(formatPlain(newItem.getActualPrice()));
            boughtCheckBox.setSelected(newItem.isBought());
        });
    }

    @FXML
    private void handleGenerateShoppingList() {
        WeeklyMenuOption selectedMenu = menuComboBox.getValue();
        if (selectedMenu == null) {
            showMessage("Pilih menu mingguan terlebih dahulu.", true);
            return;
        }

        Akun akun = AppNavigator.getCurrentUser();
        try {
            activePlanner = shoppingPlannerDAO.susunDaftarBelanja(akun.getUserId(), selectedMenu.getMenuId());
            refreshItems();
            showPlannerSummary();
            showMessage("Daftar belanja berhasil disusun dari menu mingguan.", false);
        } catch (SQLException e) {
            showMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void handleApplySelectedActual() {
        ShoppingItem selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showMessage("Pilih item belanja yang ingin diperbarui.", true);
            return;
        }

        BigDecimal actualPrice = parseAmount(actualPriceField.getText());
        if (actualPrice == null || actualPrice.compareTo(BigDecimal.ZERO) < 0) {
            showMessage("Harga aktual harus berupa angka 0 atau lebih.", true);
            return;
        }

        selectedItem.setActualPrice(actualPrice);
        selectedItem.setBought(boughtCheckBox.isSelected());
        itemTable.refresh();
        updateActualPreview();
        showMessage("Perubahan item diterapkan di layar. Klik proses untuk menyimpan.", false);
    }

    @FXML
    private void handleUseEstimationAsActual() {
        for (ShoppingItem item : itemTable.getItems()) {
            item.setActualPrice(item.getEstimatedPrice());
            item.setBought(true);
        }

        itemTable.refresh();
        updateActualPreview();
        showMessage("Semua item ditandai sudah dibeli dengan harga sesuai estimasi.", false);
    }

    @FXML
    private void handleProcessShoppingResult() {
        if (activePlanner == null) {
            showMessage("Susun atau buka daftar belanja terlebih dahulu.", true);
            return;
        }
        if (itemTable.getItems().isEmpty()) {
            showMessage("Tidak ada item belanja untuk diproses.", true);
            return;
        }

        Akun akun = AppNavigator.getCurrentUser();
        try {
            activePlanner = shoppingPlannerDAO.prosesHasilBelanja(
                    akun.getUserId(),
                    activePlanner.getPlannerId(),
                    List.copyOf(itemTable.getItems())
            );
            refreshItems();
            showPlannerSummary();
            showMessage("Hasil belanja diproses dan stok dapur sudah disinkronkan.", false);
            showAlert("UC12 selesai", "Item yang dibeli berhasil masuk ke stok dapur.");
        } catch (SQLException e) {
            showMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void handleBackToDashboard() {
        AppNavigator.showDashboard(AppNavigator.getCurrentUser());
    }

    @FXML
    private void handleOpenFamilyProfile() {
        AppNavigator.showDashboard(AppNavigator.getCurrentUser(), AppNavigator.DashboardPage.FAMILY_PROFILE);
    }

    @FXML
    private void handleOpenWeeklyMenu() {
        AppNavigator.showDashboard(AppNavigator.getCurrentUser(), AppNavigator.DashboardPage.WEEKLY_MENU);
    }

    @FXML
    private void handleOpenRecommendation() {
        AppNavigator.showDashboard(AppNavigator.getCurrentUser(), AppNavigator.DashboardPage.RECOMMENDATION);
    }

    @FXML
    private void handleOpenSettings() {
        AppNavigator.showDashboard(AppNavigator.getCurrentUser(), AppNavigator.DashboardPage.SETTINGS);
    }

    @FXML
    private void handleLogout() {
        AppNavigator.showLogin();
    }

    private void initializeUserInfo() {
        Akun akun = AppNavigator.getCurrentUser();
        if (akun == null) {
            welcomeLabel.setText("User");
            emailLabel.setText("");
            topbarAvatarText.setText("N");
            return;
        }

        String name = akun.getFirstName() == null || akun.getFirstName().isBlank()
                ? "User"
                : akun.getFirstName();
        if (akun.getLastName() != null && !akun.getLastName().isBlank()) {
            name += " " + akun.getLastName();
        }

        welcomeLabel.setText(name);
        emailLabel.setText(akun.getEmail() == null ? "" : akun.getEmail());
        topbarAvatarText.setText(name.substring(0, 1).toUpperCase());
    }

    private void setupTable() {
        ingredientColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getIngredientName()));
        quantityColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                trimNumber(cell.getValue().getRequiredQty()) + " " + cell.getValue().getUnit()));
        estimationColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatCurrency(cell.getValue().getEstimatedPrice())));
        actualColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatCurrency(cell.getValue().getActualPrice())));
        boughtColumn.setCellValueFactory(cell -> {
            ShoppingItem item = cell.getValue();
            SimpleBooleanProperty property = new SimpleBooleanProperty(item.isBought());
            property.addListener((observable, oldValue, newValue) -> item.setBought(newValue));
            return property;
        });
        boughtColumn.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean checked, boolean empty) {
                super.updateItem(checked, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                ShoppingItem row = getTableView().getItems().get(getIndex());
                checkBox.setSelected(row.isBought());
                checkBox.setOnAction(event -> {
                    row.setBought(checkBox.isSelected());
                    updateActualPreview();
                });
                setGraphic(checkBox);
            }
        });
    }

    private void loadMenus() {
        Akun akun = AppNavigator.getCurrentUser();
        if (akun == null) {
            AppNavigator.showLogin();
            return;
        }

        try {
            List<WeeklyMenuOption> menus = shoppingPlannerDAO.cariMenuMingguanUser(akun.getUserId());
            menuComboBox.setItems(FXCollections.observableArrayList(menus));
            if (!menus.isEmpty()) {
                menuComboBox.getSelectionModel().selectFirst();
                loadExistingPlanner(menus.getFirst());
            } else {
                showMessage("Belum ada menu mingguan untuk akun ini.", true);
            }
        } catch (SQLException e) {
            showMessage("Gagal memuat menu: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleMenuChanged() {
        WeeklyMenuOption selectedMenu = menuComboBox.getValue();
        if (selectedMenu != null) {
            loadExistingPlanner(selectedMenu);
        }
    }

    private void loadExistingPlanner(WeeklyMenuOption menu) {
        try {
            Akun akun = AppNavigator.getCurrentUser();
            activePlanner = shoppingPlannerDAO.cariPlannerUntukMenu(akun.getUserId(), menu.getMenuId());
            if (activePlanner == null) {
                itemTable.getItems().clear();
                showEmptySummary();
                showMessage("Belum ada daftar belanja untuk menu ini. Klik susun daftar belanja.", false);
                return;
            }

            refreshItems();
            showPlannerSummary();
            showMessage("Daftar belanja yang sudah ada berhasil dimuat.", false);
        } catch (SQLException e) {
            showMessage("Gagal memuat planner: " + e.getMessage(), true);
        }
    }

    private void refreshItems() throws SQLException {
        Akun akun = AppNavigator.getCurrentUser();
        itemTable.setItems(FXCollections.observableArrayList(
                shoppingPlannerDAO.cariItems(akun.getUserId(), activePlanner.getPlannerId())
        ));
        processButton.setDisable("completed".equalsIgnoreCase(activePlanner.getStatus()));
        updateActualPreview();
    }

    private void showPlannerSummary() {
        plannerStatusLabel.setText(activePlanner.getStatus());
        estimationLabel.setText(formatCurrency(activePlanner.getTotalEstimation()));
        budgetLabel.setText(activePlanner.getBudgetAmount() == null
                ? "Belum terhubung"
                : formatCurrency(activePlanner.getBudgetAmount()));
        updateActualPreview();

        if (activePlanner.isOverBudget()) {
            showMessage("Peringatan: estimasi belanja melebihi budget menu ini.", true);
        }
    }

    private void showEmptySummary() {
        plannerStatusLabel.setText("-");
        estimationLabel.setText("-");
        budgetLabel.setText("-");
        actualLabel.setText("-");
        processButton.setDisable(true);
    }

    private void updateActualPreview() {
        BigDecimal total = BigDecimal.ZERO;
        for (ShoppingItem item : itemTable.getItems()) {
            if (item.isBought() && item.getActualPrice() != null) {
                total = total.add(item.getActualPrice());
            }
        }
        actualLabel.setText(formatCurrency(total));
    }

    private BigDecimal parseAmount(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        try {
            String normalizedValue = rawValue.trim();
            if (normalizedValue.contains(",") && normalizedValue.contains(".")) {
                normalizedValue = normalizedValue.replace(".", "").replace(",", ".");
            } else if (normalizedValue.contains(",")) {
                normalizedValue = normalizedValue.replace(",", ".");
            }
            return new BigDecimal(normalizedValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatCurrency(BigDecimal value) {
        return currencyFormat.format(value == null ? BigDecimal.ZERO : value);
    }

    private String formatPlain(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    private String trimNumber(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private void showMessage(String message, boolean error) {
        messageLabel.setText(message == null ? "" : message);
        messageLabel.getStyleClass().removeAll("message-error", "message-success");
        messageLabel.getStyleClass().add(error ? "message-error" : "message-success");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
