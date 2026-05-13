package nasi_bergizi_pajak.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.dao.IngredientDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.KitchenStock;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KitchenStockViewController {

    // ── Stat card labels ──
    @FXML private Label statTotalLabel;
    @FXML private Label statLowLabel;
    @FXML private Label statExpiringLabel;
    @FXML private Label statExpiredLabel;

    // ── Toolbar ──
    @FXML private TextField searchField;
    @FXML private Button filterAllBtn;
    @FXML private Button filterLowBtn;
    @FXML private Button filterExpiringBtn;
    @FXML private Button filterExpiredBtn;

    // ── Table ──
    @FXML private TableView<KitchenStock> stockTable;
    @FXML private TableColumn<KitchenStock, Integer> colNo;
    @FXML private TableColumn<KitchenStock, String>  colNama;
    @FXML private TableColumn<KitchenStock, Double>  colJumlah;
    @FXML private TableColumn<KitchenStock, String>  colSatuan;
    @FXML private TableColumn<KitchenStock, String>  colLokasi;
    @FXML private TableColumn<KitchenStock, String>  colKadaluarsa;
    @FXML private TableColumn<KitchenStock, String>  colStatus;
    @FXML private TableColumn<KitchenStock, Void>    colAksi;

    // ── Form ──
    @FXML private Label         formTitleLabel;
    @FXML private Label         messageLabel;
    @FXML private ComboBox<Ingredient> ingredientCombo;
    @FXML private TextField     quantityField;
    @FXML private TextField     unitField;
    @FXML private TextField     locationField;
    @FXML private DatePicker    expiryDatePicker;

    private final KitchenStockController stockController = new KitchenStockController();
    private final IngredientDAO          ingredientDAO   = new IngredientDAO();

    private ObservableList<KitchenStock> allStocks = FXCollections.observableArrayList();
    private int    editingStockId = -1;
    private String currentFilter  = "all";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        messageLabel.managedProperty().bind(messageLabel.visibleProperty());
        messageLabel.setVisible(false);

        setupTableColumns();
        loadIngredients();
        loadStockData();
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
    }

    // ═══════════════════════════════════════════════
    // TABLE SETUP
    // ═══════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        colNo.setCellValueFactory(cell -> {
            int idx = stockTable.getItems().indexOf(cell.getValue()) + 1;
            return new SimpleIntegerProperty(idx).asObject();
        });

        colNama.setCellValueFactory(new PropertyValueFactory<>("ingredientName"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colLokasi.setCellValueFactory(new PropertyValueFactory<>("storageLocation"));

        colKadaluarsa.setCellValueFactory(cell -> {
            LocalDate d = cell.getValue().getExpiryDate();
            return new SimpleStringProperty(d != null ? d.format(DATE_FMT) : "-");
        });

        colStatus.setCellValueFactory(cell ->
            new SimpleStringProperty(stockController.getStockStatus(cell.getValue()))
        );
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                setText(null);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().add("status-badge");
                badge.getStyleClass().add(switch (status) {
                    case "Good"          -> "status-good";
                    case "Low Stock"     -> "status-low";
                    case "Expiring Soon" -> "status-expiring";
                    case "Expired"       -> "status-expired";
                    default              -> "status-good";
                });
                setGraphic(badge);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn  = new Button("Hapus");
            private final HBox   box     = new HBox(6, editBtn, delBtn);

            {
                editBtn.getStyleClass().add("btn-edit");
                delBtn.getStyleClass().add("btn-danger");
                box.setAlignment(Pos.CENTER_LEFT);

                editBtn.setOnAction(e -> {
                    KitchenStock s = getTableView().getItems().get(getIndex());
                    populateForm(s);
                });
                delBtn.setOnAction(e -> {
                    KitchenStock s = getTableView().getItems().get(getIndex());
                    confirmAndDelete(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        stockTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(KitchenStock item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-expired", "row-expiring", "row-low");
                if (!empty && item != null) {
                    switch (stockController.getStockStatus(item)) {
                        case "Expired"       -> getStyleClass().add("row-expired");
                        case "Expiring Soon" -> getStyleClass().add("row-expiring");
                        case "Low Stock"     -> getStyleClass().add("row-low");
                    }
                }
            }
        });
    }

    // ═══════════════════════════════════════════════
    // DATA LOADING
    // ═══════════════════════════════════════════════

    private void loadIngredients() {
        try {
            List<Ingredient> list = ingredientDAO.getAllIngredients();
            ingredientCombo.setItems(FXCollections.observableArrayList(list));
            ingredientCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Ingredient i)      { return i == null ? "" : i.getName(); }
                @Override public Ingredient fromString(String name) {
                    return ingredientCombo.getItems().stream()
                            .filter(i -> i.getName().equalsIgnoreCase(name))
                            .findFirst().orElse(null);
                }
            });
            ingredientCombo.valueProperty().addListener((obs, old, ing) -> {
                if (ing != null && ing.getUnit() != null && !ing.getUnit().isBlank()) {
                    unitField.setText(ing.getUnit());
                }
            });
        } catch (SQLException e) {
            showMessage("Gagal memuat daftar bahan: " + e.getMessage(), false);
        }
    }

    private void loadStockData() {
        Akun user = AppNavigator.getCurrentUser();
        if (user == null) { AppNavigator.showLogin(); return; }
        try {
            allStocks = FXCollections.observableArrayList(
                stockController.getAllStockByUser(user.getUserId())
            );
            applyFilter();
            updateStats(user.getUserId());
        } catch (SQLException e) {
            showMessage("Gagal memuat data stok: " + e.getMessage(), false);
        }
    }

    private void applyFilter() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<KitchenStock> base = switch (currentFilter) {
            case "low"      -> allStocks.stream()
                    .filter(s -> "Low Stock".equals(stockController.getStockStatus(s))).toList();
            case "expiring" -> allStocks.stream()
                    .filter(s -> "Expiring Soon".equals(stockController.getStockStatus(s))).toList();
            case "expired"  -> allStocks.stream()
                    .filter(s -> "Expired".equals(stockController.getStockStatus(s))).toList();
            default         -> allStocks;
        };

        if (!search.isEmpty()) {
            base = base.stream()
                    .filter(s -> s.getIngredientName() != null
                              && s.getIngredientName().toLowerCase().contains(search))
                    .toList();
        }

        stockTable.setItems(FXCollections.observableArrayList(base));
    }

    private void updateStats(int userId) {
        statTotalLabel.setText(String.valueOf(allStocks.size()));
        try {
            statLowLabel.setText(String.valueOf(stockController.getLowStockCount(userId, 1.0)));
            statExpiringLabel.setText(String.valueOf(stockController.getExpiringCount(userId, 7)));
            statExpiredLabel.setText(String.valueOf(stockController.getExpiredCount(userId)));
        } catch (SQLException e) {
            // stats are best-effort; silently skip
        }
    }

    // ═══════════════════════════════════════════════
    // FILTER HANDLERS
    // ═══════════════════════════════════════════════

    @FXML private void handleFilterAll() {
        currentFilter = "all";
        activateFilter(filterAllBtn);
        applyFilter();
    }

    @FXML private void handleFilterLow() {
        currentFilter = "low";
        activateFilter(filterLowBtn);
        applyFilter();
    }

    @FXML private void handleFilterExpiring() {
        currentFilter = "expiring";
        activateFilter(filterExpiringBtn);
        applyFilter();
    }

    @FXML private void handleFilterExpired() {
        currentFilter = "expired";
        activateFilter(filterExpiredBtn);
        applyFilter();
    }

    private void activateFilter(Button active) {
        for (Button b : List.of(filterAllBtn, filterLowBtn, filterExpiringBtn, filterExpiredBtn)) {
            b.getStyleClass().remove("filter-tab-active");
        }
        active.getStyleClass().add("filter-tab-active");
    }

    // ═══════════════════════════════════════════════
    // FORM HANDLERS
    // ═══════════════════════════════════════════════

    @FXML
    private void handleSave() {
        Akun user = AppNavigator.getCurrentUser();
        if (user == null) { AppNavigator.showLogin(); return; }

        // Resolve ingredient (selected from list or typed)
        Ingredient ingredient = ingredientCombo.getValue();
        if (ingredient == null) {
            String typed = ingredientCombo.getEditor().getText().trim();
            if (typed.isEmpty()) { showMessage("Pilih atau masukkan nama bahan.", false); return; }
            try {
                ingredient = ingredientDAO.getIngredientByName(typed);
                if (ingredient == null) {
                    ingredient = new Ingredient(typed, unitField.getText().trim());
                    ingredientDAO.addIngredient(ingredient);
                    loadIngredients();
                }
            } catch (SQLException e) {
                showMessage("Gagal memproses bahan: " + e.getMessage(), false);
                return;
            }
        }

        // Validate quantity
        String qText = quantityField.getText().trim();
        if (qText.isEmpty()) { showMessage("Jumlah tidak boleh kosong.", false); return; }
        double quantity;
        try {
            quantity = Double.parseDouble(qText);
            if (quantity < 0) { showMessage("Jumlah tidak boleh negatif.", false); return; }
        } catch (NumberFormatException ex) {
            showMessage("Jumlah harus berupa angka (cth: 1.5).", false);
            return;
        }

        String unit     = unitField.getText().trim();
        String location = locationField.getText().trim();
        LocalDate expiry = expiryDatePicker.getValue();

        try {
            KitchenStock stock = new KitchenStock(
                user.getUserId(), ingredient.getIngredientId(),
                quantity, unit, location, expiry
            );
            stock.setIngredientName(ingredient.getName());

            boolean ok;
            if (editingStockId > 0) {
                stock.setStockId(editingStockId);
                ok = stockController.updateStock(stock);
                if (ok) showMessage("Stok berhasil diperbarui.", true);
            } else {
                ok = stockController.addStock(stock);
                if (ok) showMessage("Stok berhasil ditambahkan.", true);
            }
            if (ok) { handleClearForm(); loadStockData(); }
        } catch (SQLException e) {
            showMessage("Gagal menyimpan: " + e.getMessage(), false);
        }
    }

    private void confirmAndDelete(KitchenStock stock) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus stok?");
        confirm.setContentText("Hapus \"" + stock.getIngredientName() + "\"?");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;
            Akun user = AppNavigator.getCurrentUser();
            if (user == null) return;
            try {
                if (stockController.deleteStock(stock.getStockId(), user.getUserId())) {
                    showMessage("Stok berhasil dihapus.", true);
                    loadStockData();
                }
            } catch (SQLException e) {
                showMessage("Gagal menghapus: " + e.getMessage(), false);
            }
        });
    }

    @FXML
    private void handleClearForm() {
        editingStockId = -1;
        formTitleLabel.setText("Tambah Stok Baru");
        ingredientCombo.setValue(null);
        ingredientCombo.getEditor().clear();
        quantityField.clear();
        unitField.clear();
        locationField.clear();
        expiryDatePicker.setValue(null);
        messageLabel.setVisible(false);
    }

    private void populateForm(KitchenStock stock) {
        editingStockId = stock.getStockId();
        formTitleLabel.setText("Edit Stok");

        ingredientCombo.getItems().stream()
            .filter(i -> i.getIngredientId() == stock.getIngredientId())
            .findFirst()
            .ifPresentOrElse(
                ingredientCombo::setValue,
                () -> ingredientCombo.getEditor().setText(
                    stock.getIngredientName() != null ? stock.getIngredientName() : ""
                )
            );

        quantityField.setText(String.valueOf(stock.getQuantity()));
        unitField.setText(stock.getUnit() != null ? stock.getUnit() : "");
        locationField.setText(stock.getStorageLocation() != null ? stock.getStorageLocation() : "");
        expiryDatePicker.setValue(stock.getExpiryDate());

        // scroll to form
        formTitleLabel.getParent().getParent().requestFocus();
    }

    // ═══════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════

    /** Dipanggil oleh DashboardController setiap kali halaman Stok Dapur dibuka. */
    public void refreshData() { loadStockData(); }

    @FXML private void handleRefresh() { loadStockData(); }

    @FXML private void handleLogout() { AppNavigator.showLogin(); }

    @FXML private void handleBackToDashboard() {
        AppNavigator.showDashboard(AppNavigator.getCurrentUser());
    }

    // ═══════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════

    private void showMessage(String msg, boolean success) {
        messageLabel.getStyleClass().removeAll("message-success", "message-error");
        messageLabel.getStyleClass().add(success ? "message-success" : "message-error");
        messageLabel.setText(msg);
        messageLabel.setVisible(true);
    }
}
