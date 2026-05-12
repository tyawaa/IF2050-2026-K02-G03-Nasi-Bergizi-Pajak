package nasi_bergizi_pajak.controller;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.text.NumberFormat;
import java.util.Locale;
import java.sql.SQLException;

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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.collections.ObservableList;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.dao.FamilyMemberDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.model.FamilyMember;
import nasi_bergizi_pajak.model.RekomendasiMenu;

public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label emailLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Label dashboardFamilyIconCountLabel;
    @FXML private Label dashboardFamilyCountLabel;
    @FXML private Button dashboardNavButton;
    @FXML private Button familyProfileNavButton;
    @FXML private VBox dashboardPage;
    @FXML private VBox familyProfilePage;
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

    private final FamilyMemberDAO familyMemberDAO = new FamilyMemberDAO();
    private final ObservableList<FamilyMember> familyMembers = FXCollections.observableArrayList();
    private final ObservableList<RekomendasiMenu> recommendations = FXCollections.observableArrayList();
    private final RekomendasiController rekomendasiController = new RekomendasiController();
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private Akun currentUser;

    @FXML
    private void initialize() {
        currentUser = AppNavigator.getCurrentUser();
        initializeUserInfo();
        setupFamilyTable();
        refreshFamilyMembers();
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

        recommendationPage.setVisible(page == recommendationPage);
        recommendationPage.setManaged(page == recommendationPage);
    }

    private void setActiveNav(Button activeButton) {
        setNavClass(dashboardNavButton, activeButton == dashboardNavButton);
        setNavClass(familyProfileNavButton, activeButton == familyProfileNavButton);
        setNavClass(recommendationNavButton, activeButton == recommendationNavButton);
    }

    private void setNavClass(Button button, boolean active) {
        button.getStyleClass().removeAll("userdash-nav-button", "userdash-nav-active");
        button.getStyleClass().add(active ? "userdash-nav-active" : "userdash-nav-button");
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
