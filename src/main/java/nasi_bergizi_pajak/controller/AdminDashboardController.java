package nasi_bergizi_pajak.controller;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import nasi_bergizi_pajak.app.AppNavigator;

public class AdminDashboardController {
    @FXML private StackPane contentPane;
    @FXML private Label headerTitle;
    @FXML private Label userLabel;
    @FXML private Button btnResep;
    @FXML private Button btnBahanHarga;

    private final ObservableList<AdminRecipe> recipes = FXCollections.observableArrayList();
    private final ObservableList<AdminIngredient> ingredients = FXCollections.observableArrayList();
    private final ObservableList<AdminNutrition> nutritions = FXCollections.observableArrayList();
    private final ObservableList<AdminPriceHistory> priceHistory = FXCollections.observableArrayList();
    private final Map<Integer, AdminIngredient> ingredientById = new HashMap<>();
    private final Map<Integer, AdminNutrition> nutritionByIngredientId = new HashMap<>();
    private final NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private AdminRecipe selectedRecipe;
    private AdminIngredient selectedIngredient;
    private VBox recipeDetailCard;
    private VBox ingredientDetailCard;
    private VBox nutritionInfoCard;
    private VBox priceHistoryCard;
    private TableView<AdminRecipe> recipeTable;
    private TableView<AdminIngredient> ingredientTable;
    private TableView<AdminNutrition> nutritionTable;
    private TableView<AdminIngredient> priceTable;

    @FXML
    private void initialize() {
        seedAdminData();
        userLabel.setText("Budi");
        showRecipePage();
    }

    @FXML
    private void handleResep() {
        showRecipePage();
    }

    @FXML
    private void handleBahanHarga() {
        showIngredientPage("BAHAN");
    }

    @FXML
    private void handleLogout() {
        AppNavigator.showLogin();
    }

    private void showRecipePage() {
        headerTitle.setText("Admin: Kelola Resep");
        setActiveMenu(btnResep);
        selectedRecipe = null;

        VBox tableCard = card("admin-main-card");
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(0,
                title("Daftar Resep"),
                subtitle(recipes.size() + " resep terdaftar"));

        TextField search = new TextField();
        search.setPromptText("Cari resep...");
        search.getStyleClass().add("admin-search");

        Button addButton = primaryButton("Tambah Resep");
        addButton.setOnAction(event -> openAddRecipeDialog());

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        titleRow.getChildren().addAll(titleBox, titleSpacer, search, addButton);

        recipeTable = createRecipeTable();
        FilteredList<AdminRecipe> filteredRecipes = new FilteredList<>(recipes, recipe -> true);
        search.textProperty().addListener((obs, oldValue, newValue) -> {
            String keyword = newValue == null ? "" : newValue.toLowerCase().trim();
            filteredRecipes.setPredicate(recipe -> keyword.isBlank()
                    || recipe.name().toLowerCase().contains(keyword)
                    || recipe.description().toLowerCase().contains(keyword));
        });
        recipeTable.setItems(filteredRecipes);

        tableCard.getChildren().addAll(titleRow, recipeTable);

        recipeDetailCard = card("admin-side-card");
        renderRecipeDetail();

        HBox page = new HBox(18, tableCard, recipeDetailCard);
        page.setPadding(new Insets(18));
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        contentPane.getChildren().setAll(page);
    }

    private TableView<AdminRecipe> createRecipeTable() {
        TableView<AdminRecipe> table = new TableView<>();
        table.getStyleClass().add("admin-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(54);
        table.setPrefHeight(430);

        TableColumn<AdminRecipe, String> nameCol = textColumn("Nama Resep", recipe -> recipe.name(), 0.22);
        TableColumn<AdminRecipe, String> descCol = textColumn("Deskripsi", recipe -> recipe.description(), 0.43);
        TableColumn<AdminRecipe, String> servingCol = textColumn("Porsi", recipe -> String.valueOf(recipe.servingSize()), 0.07);
        TableColumn<AdminRecipe, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(statusBadge(status));
            }
        });
        statusCol.setMaxWidth(1f * Integer.MAX_VALUE * 0.12);

        TableColumn<AdminRecipe, String> actionCol = new TableColumn<>("Aksi");
        actionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));
        actionCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                AdminRecipe recipe = getTableRow().getItem();
                Button toggle = outlineButton(recipe.isActive() ? "Nonaktifkan" : "Aktifkan");
                toggle.setOnAction(event -> {
                    recipe.setStatus(recipe.isActive() ? "Nonaktif" : "Aktif");
                    table.refresh();
                    renderRecipeDetail();
                });
                Button edit = iconButton("Edit");
                edit.setOnAction(event -> openEditRecipeDialog(recipe));
                HBox actions = new HBox(8, toggle, edit);
                actions.setAlignment(Pos.CENTER_RIGHT);
                setGraphic(actions);
            }
        });
        actionCol.setMaxWidth(1f * Integer.MAX_VALUE * 0.16);

        table.getColumns().addAll(nameCol, descCol, servingCol, statusCol, actionCol);
        table.setRowFactory(tv -> {
            TableRow<AdminRecipe> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    selectedRecipe = row.getItem();
                    renderRecipeDetail();
                }
            });
            return row;
        });
        return table;
    }

    private void renderRecipeDetail() {
        recipeDetailCard.getChildren().clear();
        recipeDetailCard.getChildren().add(title("Detail Resep"));

        if (selectedRecipe == null) {
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            VBox empty = new VBox(10, new Label("Chef"), new Label("Pilih resep untuk melihat detail"));
            empty.setAlignment(Pos.CENTER);
            empty.getStyleClass().add("admin-empty-state");
            recipeDetailCard.getChildren().addAll(spacer, empty, new Region());
            return;
        }

        Label name = title(selectedRecipe.name());
        Label desc = muted(selectedRecipe.description());
        desc.setWrapText(true);

        HBox stats = new HBox(12,
                statBox("Porsi", String.valueOf(selectedRecipe.servingSize()), false),
                statBox("Status", selectedRecipe.status(), true));

        VBox ingredientList = new VBox(8);
        ingredientList.getChildren().add(sectionLabel("Bahan-bahan"));
        for (AdminRecipeIngredient item : selectedRecipe.ingredients()) {
            AdminIngredient ingredient = ingredientById.get(item.ingredientId());
            if (ingredient != null) {
                ingredientList.getChildren().add(muted(ingredient.name() + " - " + formatQuantity(item.quantity())
                        + " " + item.unit()));
            }
        }

        recipeDetailCard.getChildren().addAll(
                verticalGap(16),
                name,
                desc,
                verticalGap(10),
                stats,
                ingredientList,
                sectionLabel("Estimasi Biaya"),
                new Label(formatCurrency(estimateRecipeCost(selectedRecipe))));
    }

    private void showIngredientPage(String activeTab) {
        headerTitle.setText("Admin: Kelola Bahan & Harga");
        setActiveMenu(btnBahanHarga);
        if (selectedIngredient == null && !ingredients.isEmpty()) {
            selectedIngredient = ingredients.get(0);
        }

        VBox wrapper = new VBox(18);
        wrapper.setPadding(new Insets(16, 18, 18, 18));

        HBox tabs = new HBox(0);
        tabs.getStyleClass().add("admin-tabs");
        Button bahanTab = tabButton("Bahan", "BAHAN".equals(activeTab));
        Button nutrisiTab = tabButton("Nutrisi", "NUTRISI".equals(activeTab));
        Button hargaTab = tabButton("Harga", "HARGA".equals(activeTab));
        bahanTab.setOnAction(event -> showIngredientPage("BAHAN"));
        nutrisiTab.setOnAction(event -> showIngredientPage("NUTRISI"));
        hargaTab.setOnAction(event -> showIngredientPage("HARGA"));
        tabs.getChildren().addAll(bahanTab, nutrisiTab, hargaTab);

        Node body;
        if ("NUTRISI".equals(activeTab)) {
            body = buildNutritionTab();
        } else if ("HARGA".equals(activeTab)) {
            body = buildPriceTab();
        } else {
            body = buildIngredientTab();
        }

        wrapper.getChildren().addAll(tabs, body);
        contentPane.getChildren().setAll(wrapper);
    }

    private Node buildIngredientTab() {
        VBox tableCard = card("admin-main-card");
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox copy = new VBox(0, title("Daftar Bahan"), subtitle(ingredients.size() + " bahan terdaftar"));
        TextField search = new TextField();
        search.setPromptText("Cari bahan...");
        search.getStyleClass().add("admin-search");
        Button add = primaryButton("Tambah Bahan");
        add.setOnAction(event -> openAddIngredientDialog());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(copy, spacer, search, add);

        ingredientTable = createIngredientTable();
        FilteredList<AdminIngredient> filtered = new FilteredList<>(ingredients, ingredient -> true);
        search.textProperty().addListener((obs, oldValue, newValue) -> {
            String keyword = newValue == null ? "" : newValue.toLowerCase().trim();
            filtered.setPredicate(ingredient -> keyword.isBlank()
                    || ingredient.name().toLowerCase().contains(keyword));
        });
        ingredientTable.setItems(filtered);
        tableCard.getChildren().addAll(header, ingredientTable);

        ingredientDetailCard = card("admin-side-card");
        renderIngredientDetail();

        HBox body = new HBox(18, tableCard, ingredientDetailCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        return body;
    }

    private TableView<AdminIngredient> createIngredientTable() {
        TableView<AdminIngredient> table = new TableView<>();
        table.getStyleClass().add("admin-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(50);
        table.setPrefHeight(620);

        table.getColumns().addAll(
                textColumn("Nama Bahan", AdminIngredient::name, 0.32),
                textColumn("Unit", AdminIngredient::unit, 0.2),
                textColumn("Harga Saat Ini", item -> formatCurrency(item.currentPrice()), 0.28),
                ingredientActionColumn());

        table.setRowFactory(tv -> {
            TableRow<AdminIngredient> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    selectedIngredient = row.getItem();
                    renderIngredientDetail();
                }
            });
            return row;
        });
        return table;
    }

    private TableColumn<AdminIngredient, String> ingredientActionColumn() {
        TableColumn<AdminIngredient, String> actionCol = new TableColumn<>("Aksi");
        actionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        actionCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                AdminIngredient ingredient = getTableRow().getItem();
                Button edit = iconButton("Edit");
                edit.setOnAction(event -> showInfo("Edit Bahan", "Handler edit untuk " + ingredient.name() + " sudah siap."));
                HBox box = new HBox(edit);
                box.setAlignment(Pos.CENTER_RIGHT);
                setGraphic(box);
            }
        });
        actionCol.setMaxWidth(1f * Integer.MAX_VALUE * 0.2);
        return actionCol;
    }

    private void renderIngredientDetail() {
        ingredientDetailCard.getChildren().clear();
        ingredientDetailCard.getChildren().add(title("Detail Bahan"));

        if (selectedIngredient == null) {
            ingredientDetailCard.getChildren().add(emptyState("Pilih bahan untuk melihat detail"));
            return;
        }

        AdminNutrition nutrition = nutritionByIngredientId.get(selectedIngredient.id());
        ingredientDetailCard.getChildren().addAll(
                verticalGap(16),
                title(selectedIngredient.name()),
                muted("Unit: " + selectedIngredient.unit()),
                priceBox("Harga Saat Ini", formatCurrency(selectedIngredient.currentPrice()), "per " + selectedIngredient.unit()),
                sectionLabel("Informasi Gizi"));

        if (nutrition == null) {
            ingredientDetailCard.getChildren().add(muted("Belum ada data nutrisi."));
            return;
        }

        ingredientDetailCard.getChildren().addAll(
                keyValue("Kalori", formatNumber(nutrition.calories()) + " kcal"),
                keyValue("Protein", nutrition.protein() + "g"),
                keyValue("Karbohidrat", nutrition.carbohydrate() + "g"),
                keyValue("Lemak", nutrition.fat() + "g"),
                keyValue("Serat", nutrition.fibre() + "g"));
    }

    private Node buildNutritionTab() {
        VBox tableCard = card("admin-main-card");
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox copy = new VBox(0,
                title("Data Nutrisi Bahan"),
                subtitle("Kelola informasi gizi untuk setiap bahan"));
        Button edit = secondaryButton("Edit Nutrisi");
        edit.setOnAction(event -> showInfo("Edit Nutrisi", "Handler edit nutrisi sudah siap."));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(copy, spacer, edit);

        nutritionTable = createNutritionTable();
        nutritionTable.setItems(nutritions);
        tableCard.getChildren().addAll(header, nutritionTable);

        nutritionInfoCard = card("admin-nutrition-side-card");
        renderNutritionInfo();

        HBox body = new HBox(18, tableCard, nutritionInfoCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        return body;
    }

    private TableView<AdminNutrition> createNutritionTable() {
        TableView<AdminNutrition> table = new TableView<>();
        table.getStyleClass().add("admin-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(44);
        table.setPrefHeight(300);
        table.getColumns().addAll(
                textColumn("Bahan", data -> ingredientName(data.ingredientId()), 0.2),
                textColumn("Kalori", data -> formatNumber(data.calories()), 0.13),
                textColumn("Protein", data -> data.protein() + "g", 0.14),
                textColumn("Karbo", data -> data.carbohydrate() + "g", 0.14),
                textColumn("Lemak", data -> data.fat() + "g", 0.14),
                textColumn("Serat", data -> data.fibre() + "g", 0.14),
                textColumn("Per", AdminNutrition::per, 0.11));
        table.setRowFactory(tv -> {
            TableRow<AdminNutrition> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    selectedIngredient = ingredientById.get(row.getItem().ingredientId());
                    renderNutritionInfo();
                }
            });
            return row;
        });
        return table;
    }

    private void renderNutritionInfo() {
        nutritionInfoCard.getChildren().clear();
        nutritionInfoCard.getChildren().add(title("Info Nutrisi"));

        if (selectedIngredient == null) {
            nutritionInfoCard.getChildren().add(emptyState("Pilih bahan untuk melihat nutrisi"));
            return;
        }

        AdminNutrition nutrition = nutritionByIngredientId.get(selectedIngredient.id());
        nutritionInfoCard.getChildren().addAll(verticalGap(18), sectionLabel(selectedIngredient.name()));
        if (nutrition == null) {
            nutritionInfoCard.getChildren().add(muted("Belum ada data nutrisi."));
            return;
        }

        nutritionInfoCard.getChildren().addAll(
                nutritionMetric("Kalori", formatNumber(nutrition.calories()) + " kcal", true),
                new HBox(10,
                        nutritionMetric("Protein", nutrition.protein() + "g", false),
                        nutritionMetric("Karbo", nutrition.carbohydrate() + "g", false)),
                new HBox(10,
                        nutritionMetric("Lemak", nutrition.fat() + "g", false),
                        nutritionMetric("Serat", nutrition.fibre() + "g", false)));
    }

    private Node buildPriceTab() {
        VBox tableCard = card("admin-main-card");
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox copy = new VBox(0,
                title("Kelola Harga Bahan"),
                subtitle("Update dan lihat riwayat harga bahan"));
        Button update = primaryButton("Update Harga");
        update.setOnAction(event -> openUpdatePriceDialog(selectedIngredient == null ? ingredients.get(0) : selectedIngredient));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(copy, spacer, update);

        priceTable = createPriceTable();
        priceTable.setItems(ingredients);
        tableCard.getChildren().addAll(header, priceTable);

        priceHistoryCard = card("admin-side-card");
        renderPriceHistory();

        HBox body = new HBox(18, tableCard, priceHistoryCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        return body;
    }

    private TableView<AdminIngredient> createPriceTable() {
        TableView<AdminIngredient> table = new TableView<>();
        table.getStyleClass().add("admin-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(48);
        table.setPrefHeight(620);
        table.getColumns().addAll(
                textColumn("Bahan", AdminIngredient::name, 0.23),
                textColumn("Unit", AdminIngredient::unit, 0.15),
                textColumn("Harga Saat Ini", item -> formatCurrency(item.currentPrice()), 0.22),
                textColumn("Terakhir Update", this::latestDateFor, 0.22),
                updateActionColumn());
        table.setRowFactory(tv -> {
            TableRow<AdminIngredient> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    selectedIngredient = row.getItem();
                    renderPriceHistory();
                }
            });
            return row;
        });
        return table;
    }

    private TableColumn<AdminIngredient, String> updateActionColumn() {
        TableColumn<AdminIngredient, String> actionCol = new TableColumn<>("Aksi");
        actionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        actionCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                AdminIngredient ingredient = getTableRow().getItem();
                Button update = outlineButton("Update");
                update.setOnAction(event -> openUpdatePriceDialog(ingredient));
                HBox box = new HBox(update);
                box.setAlignment(Pos.CENTER_RIGHT);
                setGraphic(box);
            }
        });
        actionCol.setMaxWidth(1f * Integer.MAX_VALUE * 0.18);
        return actionCol;
    }

    private void renderPriceHistory() {
        priceHistoryCard.getChildren().clear();
        priceHistoryCard.getChildren().add(title("Riwayat Harga"));
        if (selectedIngredient == null) {
            priceHistoryCard.getChildren().add(emptyState("Pilih bahan untuk melihat riwayat harga"));
            return;
        }

        priceHistoryCard.getChildren().addAll(verticalGap(18), sectionLabel(selectedIngredient.name()));
        for (AdminPriceHistory history : historiesFor(selectedIngredient.id())) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            VBox copy = new VBox(2,
                    new Label(formatCurrency(history.price())),
                    muted(history.dateLabel()));
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(copy, spacer);
            if (history.active()) {
                row.getChildren().add(statusBadge("Aktif"));
            }
            row.getStyleClass().add(history.active() ? "admin-price-history-active" : "admin-price-history-row");
            priceHistoryCard.getChildren().add(row);
        }
    }

    private void openAddRecipeDialog() {
        Dialog<AdminRecipe> dialog = new Dialog<>();
        dialog.setTitle("Tambah Resep Baru");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(contentPane.getScene().getWindow());

        ButtonType addType = new ButtonType("Tambah Resep", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, addType);
        dialog.getDialogPane().getStyleClass().add("admin-dialog-pane");

        TextField name = new TextField();
        name.setPromptText("Nama resep");
        TextField serving = new TextField("3");
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("Aktif", "Nonaktif"));
        status.setValue("Aktif");
        TextArea description = new TextArea();
        description.setPromptText("Deskripsi singkat resep");
        description.setPrefRowCount(3);

        ObservableList<AdminRecipeIngredient> pendingIngredients = FXCollections.observableArrayList();
        VBox ingredientList = new VBox(8);
        Runnable renderPendingIngredients = () -> {
            ingredientList.getChildren().clear();
            if (pendingIngredients.isEmpty()) {
                ingredientList.getChildren().add(emptyState("Belum ada bahan ditambahkan"));
                return;
            }
            for (AdminRecipeIngredient item : pendingIngredients) {
                ingredientList.getChildren().add(muted(ingredientName(item.ingredientId()) + " - "
                        + formatQuantity(item.quantity()) + " " + item.unit()));
            }
        };
        renderPendingIngredients.run();

        Button addIngredient = outlineButton("Tambah Bahan");
        addIngredient.setOnAction(event -> {
            Optional<AdminRecipeIngredient> item = openAddRecipeIngredientDialog();
            item.ifPresent(value -> {
                pendingIngredients.add(value);
                renderPendingIngredients.run();
            });
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.add(label("Nama Resep"), 0, 0);
        form.add(name, 0, 1);
        form.add(label("Porsi"), 1, 0);
        form.add(serving, 1, 1);
        form.add(label("Status"), 2, 0);
        form.add(status, 2, 1);
        form.add(label("Deskripsi"), 0, 2, 3, 1);
        form.add(description, 0, 3, 3, 1);

        HBox ingredientHeader = new HBox(sectionLabel("Bahan-bahan"), new Region(), addIngredient);
        HBox.setHgrow(ingredientHeader.getChildren().get(1), Priority.ALWAYS);
        ingredientHeader.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(14,
                title("Tambah Resep Baru"),
                subtitle("Buat resep baru dengan bahan-bahan"),
                form,
                ingredientHeader,
                ingredientList);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Node addNode = dialog.getDialogPane().lookupButton(addType);
        addNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (name.getText().trim().isBlank()) {
                showInfo("Data belum lengkap", "Nama resep wajib diisi.");
                event.consume();
                return;
            }
            try {
                if (Integer.parseInt(serving.getText().trim()) <= 0) {
                    showInfo("Data belum valid", "Porsi harus lebih dari 0.");
                    event.consume();
                }
            } catch (NumberFormatException e) {
                showInfo("Data belum valid", "Porsi harus berupa angka.");
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != addType) {
                return null;
            }
            int nextId = recipes.stream().mapToInt(AdminRecipe::id).max().orElse(0) + 1;
            return new AdminRecipe(
                    nextId,
                    name.getText().trim(),
                    description.getText().trim(),
                    Integer.parseInt(serving.getText().trim()),
                    status.getValue(),
                    new ArrayList<>(pendingIngredients));
        });

        dialog.showAndWait().ifPresent(recipe -> {
            recipes.add(recipe);
            selectedRecipe = recipe;
            recipeTable.refresh();
            renderRecipeDetail();
        });
    }

    private void openEditRecipeDialog(AdminRecipe recipe) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Resep");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(contentPane.getScene().getWindow());

        ButtonType saveType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, saveType);
        dialog.getDialogPane().getStyleClass().add("admin-dialog-pane");

        TextField name = new TextField(recipe.name());
        TextField serving = new TextField(String.valueOf(recipe.servingSize()));
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("Aktif", "Nonaktif"));
        status.setValue(recipe.status());
        TextArea description = new TextArea(recipe.description());
        description.setPrefRowCount(3);

        ObservableList<AdminRecipeIngredient> pendingIngredients =
                FXCollections.observableArrayList(recipe.ingredients());
        VBox ingredientList = new VBox(8);
        Runnable[] renderPendingIngredients = new Runnable[1];
        renderPendingIngredients[0] = () -> {
            ingredientList.getChildren().clear();
            if (pendingIngredients.isEmpty()) {
                ingredientList.getChildren().add(emptyState("Belum ada bahan ditambahkan"));
                return;
            }
            for (AdminRecipeIngredient item : pendingIngredients) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                Label copy = muted(ingredientName(item.ingredientId()) + " - "
                        + formatQuantity(item.quantity()) + " " + item.unit());
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Button remove = outlineButton("Hapus");
                remove.setOnAction(event -> {
                    pendingIngredients.remove(item);
                    renderPendingIngredients[0].run();
                });
                row.getChildren().addAll(copy, spacer, remove);
                ingredientList.getChildren().add(row);
            }
        };
        renderPendingIngredients[0].run();

        Button addIngredient = outlineButton("Tambah Bahan");
        addIngredient.setOnAction(event -> {
            Optional<AdminRecipeIngredient> item = openAddRecipeIngredientDialog();
            item.ifPresent(value -> {
                pendingIngredients.add(value);
                renderPendingIngredients[0].run();
            });
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.add(label("Nama Resep"), 0, 0);
        form.add(name, 0, 1);
        form.add(label("Porsi"), 1, 0);
        form.add(serving, 1, 1);
        form.add(label("Status"), 2, 0);
        form.add(status, 2, 1);
        form.add(label("Deskripsi"), 0, 2, 3, 1);
        form.add(description, 0, 3, 3, 1);

        HBox ingredientHeader = new HBox(sectionLabel("Bahan-bahan"), new Region(), addIngredient);
        HBox.setHgrow(ingredientHeader.getChildren().get(1), Priority.ALWAYS);
        ingredientHeader.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(14,
                title("Edit Resep"),
                subtitle("Ubah data resep dan daftar bahan"),
                form,
                ingredientHeader,
                ingredientList);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Node saveNode = dialog.getDialogPane().lookupButton(saveType);
        saveNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (name.getText().trim().isBlank()) {
                showInfo("Data belum lengkap", "Nama resep wajib diisi.");
                event.consume();
                return;
            }
            try {
                if (Integer.parseInt(serving.getText().trim()) <= 0) {
                    showInfo("Data belum valid", "Porsi harus lebih dari 0.");
                    event.consume();
                }
            } catch (NumberFormatException e) {
                showInfo("Data belum valid", "Porsi harus berupa angka.");
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != saveType) {
                return false;
            }
            recipe.setName(name.getText().trim());
            recipe.setDescription(description.getText().trim());
            recipe.setServingSize(Integer.parseInt(serving.getText().trim()));
            recipe.setStatus(status.getValue());
            recipe.setIngredients(new ArrayList<>(pendingIngredients));
            return true;
        });

        dialog.showAndWait().ifPresent(saved -> {
            if (saved) {
                selectedRecipe = recipe;
                recipeTable.refresh();
                renderRecipeDetail();
            }
        });
    }

    private Optional<AdminRecipeIngredient> openAddRecipeIngredientDialog() {
        Dialog<AdminRecipeIngredient> dialog = new Dialog<>();
        dialog.setTitle("Tambah Bahan");
        dialog.initOwner(contentPane.getScene().getWindow());
        ButtonType addType = new ButtonType("Tambah", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, addType);

        ComboBox<AdminIngredient> ingredientSelect = new ComboBox<>(ingredients);
        ingredientSelect.setPromptText("Pilih bahan");
        TextField quantity = new TextField("100");
        TextField unit = new TextField("gram");
        ingredientSelect.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                unit.setText(defaultRecipeUnit(newValue.unit()));
            }
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.add(label("Nama Bahan"), 0, 0, 2, 1);
        form.add(ingredientSelect, 0, 1, 2, 1);
        form.add(label("Jumlah"), 0, 2);
        form.add(label("Unit"), 1, 2);
        form.add(quantity, 0, 3);
        form.add(unit, 1, 3);

        VBox content = new VBox(14, title("Tambah Bahan"), form);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Node addNode = dialog.getDialogPane().lookupButton(addType);
        addNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (ingredientSelect.getValue() == null) {
                showInfo("Data belum lengkap", "Pilih bahan dari master bahan.");
                event.consume();
                return;
            }
            try {
                if (Double.parseDouble(quantity.getText().trim()) <= 0) {
                    showInfo("Data belum valid", "Jumlah harus lebih dari 0.");
                    event.consume();
                }
            } catch (NumberFormatException e) {
                showInfo("Data belum valid", "Jumlah harus berupa angka.");
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != addType) {
                return null;
            }
            AdminIngredient ingredient = ingredientSelect.getValue();
            return new AdminRecipeIngredient(0, ingredient.id(),
                    Double.parseDouble(quantity.getText().trim()),
                    unit.getText().trim());
        });
        return dialog.showAndWait();
    }

    private void openAddIngredientDialog() {
        Dialog<AdminIngredient> dialog = new Dialog<>();
        dialog.setTitle("Tambah Bahan");
        dialog.initOwner(contentPane.getScene().getWindow());
        ButtonType addType = new ButtonType("Tambah Bahan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, addType);

        TextField name = new TextField();
        TextField unit = new TextField();
        TextField price = new TextField();
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.add(label("Nama"), 0, 0);
        form.add(name, 1, 0);
        form.add(label("Unit"), 0, 1);
        form.add(unit, 1, 1);
        form.add(label("Harga Saat Ini"), 0, 2);
        form.add(price, 1, 2);
        dialog.getDialogPane().setContent(form);

        Node addNode = dialog.getDialogPane().lookupButton(addType);
        addNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                if (name.getText().trim().isBlank() || unit.getText().trim().isBlank()
                        || Double.parseDouble(price.getText().trim()) < 0) {
                    showInfo("Data belum valid", "Isi nama, unit, dan harga dengan benar.");
                    event.consume();
                }
            } catch (NumberFormatException e) {
                showInfo("Data belum valid", "Harga harus berupa angka.");
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != addType) {
                return null;
            }
            int nextId = ingredients.stream().mapToInt(AdminIngredient::id).max().orElse(0) + 1;
            return new AdminIngredient(nextId, name.getText().trim(), unit.getText().trim(),
                    Double.parseDouble(price.getText().trim()));
        });

        dialog.showAndWait().ifPresent(ingredient -> {
            ingredients.add(ingredient);
            ingredientById.put(ingredient.id(), ingredient);
            priceHistory.add(new AdminPriceHistory(ingredient.id(), ingredient.currentPrice(), "-", true));
            ingredientTable.refresh();
        });
    }

    private void openUpdatePriceDialog(AdminIngredient ingredient) {
        if (ingredient == null) {
            return;
        }

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Update Harga");
        dialog.initOwner(contentPane.getScene().getWindow());
        ButtonType updateType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, updateType);

        TextField price = new TextField(String.valueOf((int) ingredient.currentPrice()));
        VBox content = new VBox(10,
                title("Update Harga " + ingredient.name()),
                muted("Harga saat ini: " + formatCurrency(ingredient.currentPrice())),
                label("Harga baru"),
                price);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Node updateNode = dialog.getDialogPane().lookupButton(updateType);
        updateNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                if (Double.parseDouble(price.getText().trim()) < 0) {
                    showInfo("Data belum valid", "Harga tidak boleh negatif.");
                    event.consume();
                }
            } catch (NumberFormatException e) {
                showInfo("Data belum valid", "Harga harus berupa angka.");
                event.consume();
            }
        });

        dialog.setResultConverter(button -> button == updateType
                ? Double.parseDouble(price.getText().trim())
                : null);

        dialog.showAndWait().ifPresent(newPrice -> {
            ingredient.setCurrentPrice(newPrice);
            for (AdminPriceHistory history : priceHistory) {
                if (history.ingredientId() == ingredient.id()) {
                    history.setActive(false);
                }
            }
            priceHistory.add(0, new AdminPriceHistory(ingredient.id(), newPrice, "11 Mei 2026", true));
            if (priceTable != null) {
                priceTable.refresh();
            }
            renderPriceHistory();
        });
    }

    private void seedAdminData() {
        ingredients.setAll(
                new AdminIngredient(1, "Beras", "kg", 15000),
                new AdminIngredient(2, "Telur", "butir", 2500),
                new AdminIngredient(3, "Ayam", "kg", 35000),
                new AdminIngredient(4, "Tempe", "papan", 5000),
                new AdminIngredient(5, "Bayam", "ikat", 3000),
                new AdminIngredient(6, "Wortel", "kg", 12000),
                new AdminIngredient(7, "Tahu", "papan", 4000),
                new AdminIngredient(8, "Minyak Goreng", "liter", 18000),
                new AdminIngredient(9, "Bawang Merah", "kg", 35000),
                new AdminIngredient(10, "Bawang Putih", "kg", 40000),
                new AdminIngredient(11, "Cabai Merah", "kg", 45000),
                new AdminIngredient(12, "Garam", "bungkus", 3000),
                new AdminIngredient(13, "Gula Pasir", "kg", 16000),
                new AdminIngredient(14, "Kecap Manis", "botol", 12000));

        ingredientById.clear();
        ingredients.forEach(ingredient -> ingredientById.put(ingredient.id(), ingredient));

        nutritions.setAll(
                new AdminNutrition(1, 130, 2.7, 28, 0.3, 0.4, "100g"),
                new AdminNutrition(2, 155, 13, 1.1, 11, 0, "butir"),
                new AdminNutrition(3, 239, 27, 0, 14, 0, "100g"),
                new AdminNutrition(4, 193, 19, 9.4, 11, 0, "100g"),
                new AdminNutrition(5, 23, 2.9, 3.6, 0.4, 2.2, "100g"));
        nutritionByIngredientId.clear();
        nutritions.forEach(nutrition -> nutritionByIngredientId.put(nutrition.ingredientId(), nutrition));

        priceHistory.setAll(
                new AdminPriceHistory(1, 15000, "1 April 2024", true),
                new AdminPriceHistory(1, 14500, "1 Maret 2024", false),
                new AdminPriceHistory(1, 14000, "1 Februari 2024", false),
                new AdminPriceHistory(2, 2500, "1 April 2024", true));

        recipes.setAll(
                new AdminRecipe(1, "Nasi Ayam Sayur", "Nasi dengan ayam goreng dan tumis sayuran segar", 3, "Aktif",
                        recipeItems(item(1, 100, "gram"), item(3, 75, "gram"), item(5, 50, "gram"),
                                item(6, 50, "gram"), item(10, 5, "gram"), item(12, 2, "gram"),
                                item(8, 10, "ml"))),
                new AdminRecipe(2, "Tumis Tempe Bayam", "Tumis tempe dengan bayam segar dan bumbu sederhana", 3, "Aktif",
                        recipeItems(item(4, 100, "gram"), item(5, 75, "gram"), item(9, 10, "gram"),
                                item(10, 5, "gram"), item(11, 5, "gram"), item(12, 2, "gram"),
                                item(8, 10, "ml"))),
                new AdminRecipe(3, "Sup Ayam Wortel", "Sup hangat dengan ayam dan wortel yang bergizi", 4, "Aktif",
                        recipeItems(item(3, 75, "gram"), item(6, 100, "gram"),
                                item(10, 5, "gram"), item(12, 2, "gram"))),
                new AdminRecipe(4, "Tahu Telur", "Tahu goreng dengan telur dadar dan sambal", 3, "Aktif",
                        recipeItems(item(7, 1, "papan"), item(2, 1, "butir"), item(11, 5, "gram"),
                                item(12, 2, "gram"), item(8, 10, "ml"))),
                new AdminRecipe(5, "Nasi Goreng Spesial", "Nasi goreng dengan telur, ayam, dan sayuran", 3, "Aktif",
                        recipeItems(item(1, 150, "gram"), item(2, 1, "butir"), item(3, 50, "gram"),
                                item(9, 10, "gram"), item(10, 5, "gram"), item(14, 10, "ml"),
                                item(8, 10, "ml"), item(12, 2, "gram"))),
                new AdminRecipe(6, "Sayur Asem", "Sayur asem khas Jawa dengan berbagai sayuran", 4, "Aktif",
                        recipeItems(item(6, 50, "gram"), item(5, 50, "gram"), item(9, 10, "gram"),
                                item(10, 5, "gram"), item(11, 5, "gram"), item(12, 2, "gram"),
                                item(13, 5, "gram"))));
    }

    private AdminRecipeIngredient item(int ingredientId, double quantity, String unit) {
        return new AdminRecipeIngredient(0, ingredientId, quantity, unit);
    }

    private List<AdminRecipeIngredient> recipeItems(AdminRecipeIngredient... items) {
        return new ArrayList<>(List.of(items));
    }

    private void setActiveMenu(Button activeButton) {
        btnResep.getStyleClass().remove("admin-nav-button-active");
        btnBahanHarga.getStyleClass().remove("admin-nav-button-active");
        activeButton.getStyleClass().add("admin-nav-button-active");
    }

    private VBox card(String styleClass) {
        VBox card = new VBox(16);
        card.getStyleClass().add(styleClass);
        card.setPadding(new Insets(22));
        return card;
    }

    private Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("admin-card-title");
        return label;
    }

    private Label subtitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("admin-card-subtitle");
        return label;
    }

    private Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("admin-muted");
        label.setWrapText(true);
        return label;
    }

    private Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("admin-label");
        return label;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("admin-section-label");
        return label;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("admin-primary-button");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("admin-secondary-button");
        return button;
    }

    private Button outlineButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("admin-outline-button");
        return button;
    }

    private Button iconButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("admin-icon-button");
        return button;
    }

    private Button tabButton(String text, boolean active) {
        Button button = new Button(text);
        button.getStyleClass().add(active ? "admin-tab-active" : "admin-tab");
        return button;
    }

    private Label statusBadge(String status) {
        Label label = new Label(status);
        label.getStyleClass().add("admin-status-badge");
        return label;
    }

    private VBox statBox(String label, String value, boolean status) {
        VBox box = new VBox(4, muted(label), status ? statusBadge(value) : title(value));
        box.getStyleClass().add("admin-stat-box");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private VBox priceBox(String label, String value, String helper) {
        VBox box = new VBox(2, muted(label), title(value), muted(helper));
        box.getStyleClass().add("admin-price-box");
        return box;
    }

    private HBox keyValue(String key, String value) {
        Label left = muted(key);
        Label right = new Label(value);
        right.getStyleClass().add("admin-key-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(left, spacer, right);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox nutritionMetric(String label, String value, boolean fullWidth) {
        VBox box = new VBox(4, muted(label), title(value));
        box.getStyleClass().add("admin-nutrition-metric");
        if (!fullWidth) {
            HBox.setHgrow(box, Priority.ALWAYS);
        }
        return box;
    }

    private VBox emptyState(String text) {
        VBox box = new VBox(8, new Label("Box"), muted(text));
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("admin-empty-box");
        return box;
    }

    private Region verticalGap(double height) {
        Region region = new Region();
        region.setMinHeight(height);
        region.setPrefHeight(height);
        return region;
    }

    private <T> TableColumn<T, String> textColumn(String title, java.util.function.Function<T, String> mapper, double widthRatio) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        column.setMaxWidth(1f * Integer.MAX_VALUE * widthRatio);
        return column;
    }

    private String ingredientName(int ingredientId) {
        AdminIngredient ingredient = ingredientById.get(ingredientId);
        return ingredient == null ? "-" : ingredient.name();
    }

    private String latestDateFor(AdminIngredient ingredient) {
        return historiesFor(ingredient.id()).stream()
                .filter(AdminPriceHistory::active)
                .findFirst()
                .map(AdminPriceHistory::dateLabel)
                .orElse("-");
    }

    private List<AdminPriceHistory> historiesFor(int ingredientId) {
        return priceHistory.stream()
                .filter(history -> history.ingredientId() == ingredientId)
                .toList();
    }

    private double estimateRecipeCost(AdminRecipe recipe) {
        double total = 0;
        for (AdminRecipeIngredient item : recipe.ingredients()) {
            AdminIngredient ingredient = ingredientById.get(item.ingredientId());
            if (ingredient != null) {
                total += estimateIngredientCost(ingredient, item);
            }
        }
        return total;
    }

    private double estimateIngredientCost(AdminIngredient ingredient, AdminRecipeIngredient item) {
        String ingredientUnit = ingredient.unit().toLowerCase();
        String itemUnit = item.unit().toLowerCase();
        if ("kg".equals(ingredientUnit) && "gram".equals(itemUnit)) {
            return ingredient.currentPrice() * item.quantity() / 1000;
        }
        if ("liter".equals(ingredientUnit) && "ml".equals(itemUnit)) {
            return ingredient.currentPrice() * item.quantity() / 1000;
        }
        return ingredient.currentPrice() * item.quantity();
    }

    private String defaultRecipeUnit(String ingredientUnit) {
        return switch (ingredientUnit.toLowerCase()) {
            case "kg" -> "gram";
            case "liter", "botol" -> "ml";
            default -> ingredientUnit;
        };
    }

    private String formatCurrency(double amount) {
        return rupiah.format(amount).replace(",00", "");
    }

    private String formatNumber(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private String formatQuantity(double value) {
        return formatNumber(value);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static final class AdminIngredient {
        private final int id;
        private final String name;
        private final String unit;
        private double currentPrice;

        private AdminIngredient(int id, String name, String unit, double currentPrice) {
            this.id = id;
            this.name = name;
            this.unit = unit;
            this.currentPrice = currentPrice;
        }

        private int id() {
            return id;
        }

        private String name() {
            return name;
        }

        private String unit() {
            return unit;
        }

        private double currentPrice() {
            return currentPrice;
        }

        private void setCurrentPrice(double currentPrice) {
            this.currentPrice = currentPrice;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private record AdminNutrition(
            int ingredientId,
            double calories,
            double protein,
            double carbohydrate,
            double fat,
            double fibre,
            String per) {
    }

    private static final class AdminPriceHistory {
        private final int ingredientId;
        private final double price;
        private final String dateLabel;
        private boolean active;

        private AdminPriceHistory(int ingredientId, double price, String dateLabel, boolean active) {
            this.ingredientId = ingredientId;
            this.price = price;
            this.dateLabel = dateLabel;
            this.active = active;
        }

        private int ingredientId() {
            return ingredientId;
        }

        private double price() {
            return price;
        }

        private String dateLabel() {
            return dateLabel;
        }

        private boolean active() {
            return active;
        }

        private void setActive(boolean active) {
            this.active = active;
        }
    }

    private static final class AdminRecipe {
        private final int id;
        private String name;
        private String description;
        private int servingSize;
        private String status;
        private List<AdminRecipeIngredient> ingredients;

        private AdminRecipe(int id, String name, String description, int servingSize, String status,
                            List<AdminRecipeIngredient> ingredients) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.servingSize = servingSize;
            this.status = status;
            this.ingredients = ingredients;
        }

        private int id() {
            return id;
        }

        private String name() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        private String description() {
            return description;
        }

        private void setDescription(String description) {
            this.description = description;
        }

        private int servingSize() {
            return servingSize;
        }

        private void setServingSize(int servingSize) {
            this.servingSize = servingSize;
        }

        private String status() {
            return status;
        }

        private void setStatus(String status) {
            this.status = status;
        }

        private boolean isActive() {
            return "Aktif".equalsIgnoreCase(status);
        }

        private List<AdminRecipeIngredient> ingredients() {
            return ingredients;
        }

        private void setIngredients(List<AdminRecipeIngredient> ingredients) {
            this.ingredients = ingredients;
        }
    }

    private record AdminRecipeIngredient(int recipeId, int ingredientId, double quantity, String unit) {
    }
}
