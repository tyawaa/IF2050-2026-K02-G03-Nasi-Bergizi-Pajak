package nasi_bergizi_pajak.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.dao.IngredientDAO;
import nasi_bergizi_pajak.model.Ingredient;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ManageIngredientPriceViewController {

    @FXML private TextField searchField;

    @FXML private TableView<Ingredient> ingredientTable;
    @FXML private TableColumn<Ingredient, String> colNo;
    @FXML private TableColumn<Ingredient, String> colName;
    @FXML private TableColumn<Ingredient, String> colUnit;
    @FXML private TableColumn<Ingredient, String> colPrice;
    @FXML private TableColumn<Ingredient, String> colDate;

    @FXML private Label labelNamaBahan;
    @FXML private Label labelHargaSaatIni;
    @FXML private TextField fieldHargaBaru;
    @FXML private DatePicker datePickerBerlaku;
    @FXML private Button btnSimpan;
    @FXML private Label labelStatus;

    private final IngredientDAO ingredientDAO = new IngredientDAO();
    private final ObservableList<Ingredient> ingredientList = FXCollections.observableArrayList();
    private List<Ingredient> allIngredients;
    private Ingredient selectedIngredient;

    @FXML
    private void initialize() {
        setupTableColumns();
        ingredientTable.setItems(ingredientList);

        ingredientTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> onIngredientSelected(newVal));

        datePickerBerlaku.setValue(LocalDate.now());

        loadIngredients();
    }

    private void setupTableColumns() {
        colNo.setCellValueFactory(cellData -> {
            int idx = ingredientList.indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(idx));
        });

        colName.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getName()));

        colUnit.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getUnit()));

        colPrice.setCellValueFactory(cd -> {
            double p = cd.getValue().getPricePerUnit();
            return new SimpleStringProperty(p > 0 ? formatRupiah(p) : "—");
        });

        colDate.setCellValueFactory(cd ->
                new SimpleStringProperty("—"));
    }

    private void loadIngredients() {
        try {
            allIngredients = ingredientDAO.listAllIngredients();
            ingredientList.setAll(allIngredients);
        } catch (IllegalStateException e) {
            showError("Gagal memuat data bahan: " + e.getMessage());
        }
    }

    private void onIngredientSelected(Ingredient ingredient) {
        selectedIngredient = ingredient;
        clearStatus();

        if (ingredient == null) {
            labelNamaBahan.setText("— Pilih bahan dari tabel —");
            labelHargaSaatIni.setText("—");
            fieldHargaBaru.clear();
            btnSimpan.setDisable(true);
            return;
        }

        labelNamaBahan.setText(ingredient.getName() + "  (" + ingredient.getUnit() + ")");
        labelHargaSaatIni.setText(
                ingredient.getPricePerUnit() > 0
                        ? formatRupiah(ingredient.getPricePerUnit())
                        : "Belum ada harga");
        fieldHargaBaru.clear();
        btnSimpan.setDisable(false);
    }

    @FXML
    private void handleSearch() {
        String term = searchField.getText().trim().toLowerCase();
        if (term.isEmpty()) {
            ingredientList.setAll(allIngredients);
        } else {
            List<Ingredient> filtered = allIngredients.stream()
                    .filter(i -> i.getName().toLowerCase().contains(term))
                    .collect(Collectors.toList());
            ingredientList.setAll(filtered);
        }
    }

    @FXML
    private void handleSimpan() {
        if (selectedIngredient == null) return;

        String hargaStr = fieldHargaBaru.getText().trim();
        if (hargaStr.isEmpty()) {
            showError("Harga baru tidak boleh kosong.");
            return;
        }

        double hargaBaru;
        try {
            hargaBaru = Double.parseDouble(hargaStr.replace(".", "").replace(",", "."));
            if (hargaBaru < 0) {
                showError("Harga tidak boleh negatif.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Format harga tidak valid. Masukkan angka saja (contoh: 15000).");
            return;
        }

        LocalDate tanggal = datePickerBerlaku.getValue();
        if (tanggal == null) {
            showError("Pilih tanggal berlaku.");
            return;
        }

        try {
            ingredientDAO.addIngredientPrice(
                    selectedIngredient.getIngredientId(), hargaBaru, tanggal);

            showSuccess("Harga " + selectedIngredient.getName()
                    + " berhasil diperbarui menjadi " + formatRupiah(hargaBaru) + ".");

            fieldHargaBaru.clear();
            loadIngredients();

            final int selectedId = selectedIngredient.getIngredientId();
            ingredientTable.getItems().stream()
                    .filter(i -> i.getIngredientId() == selectedId)
                    .findFirst()
                    .ifPresent(i -> {
                        ingredientTable.getSelectionModel().select(i);
                        ingredientTable.scrollTo(i);
                    });

        } catch (IllegalStateException e) {
            showError("Gagal menyimpan harga: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        AppNavigator.showDashboard(AppNavigator.getCurrentUser());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String formatRupiah(double amount) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.of("id", "ID"));
        return "Rp " + fmt.format((long) amount);
    }

    private void showError(String msg) {
        labelStatus.setText(msg);
        labelStatus.getStyleClass().removeAll("message-success");
        if (!labelStatus.getStyleClass().contains("message-error")) {
            labelStatus.getStyleClass().add("message-error");
        }
    }

    private void showSuccess(String msg) {
        labelStatus.setText(msg);
        labelStatus.getStyleClass().removeAll("message-error");
        if (!labelStatus.getStyleClass().contains("message-success")) {
            labelStatus.getStyleClass().add("message-success");
        }
    }

    private void clearStatus() {
        labelStatus.setText("");
        labelStatus.getStyleClass().removeAll("message-error", "message-success");
    }
}
