package nasi_bergizi_pajak.app;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import nasi_bergizi_pajak.model.Akun;

public final class AppNavigator {
    private static Stage stage;
    private static Akun currentUser;

    private AppNavigator() {
    }

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
    }

    public static Akun getCurrentUser() {
        return currentUser;
    }

    public static void showLogin() {
        currentUser = null;
        loadScene("/view/LoginView.fxml", "Nasi Bergizi Pajak - Login");
    }

    public static void showRegister() {
        loadScene("/view/RegisterView.fxml", "Nasi Bergizi Pajak - Registrasi");
    }

    public static void showDashboard(Akun akun) {
        currentUser = akun;
        if (akun != null && akun.isAdmin()) {
            loadScene("/view/AdminDashboardView.fxml", "Nasi Bergizi Pajak - Dashboard Admin");
            return;
        }

        loadScene("/view/DashboardView.fxml", "Nasi Bergizi Pajak - Dashboard");
    }

    public static void showManageIngredientPrice() {
        loadScene("/view/ManageIngredientPriceView.fxml", "Nasi Bergizi Pajak - Manajemen Harga Bahan");
    }

    private static void loadScene(String fxmlPath, String title) {
        if (stage == null) {
            throw new IllegalStateException("Stage belum diset.");
        }

        try {
            URL resource = AppNavigator.class.getResource(fxmlPath);

            if (resource == null) {
                throw new IOException("Resource tidak ditemukan: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Scene scene = new Scene(root, 1180, 720);

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();

            showFatalError(
                "Gagal membuka halaman",
                e.getClass().getSimpleName() + "\n\n" + e.getMessage()
            );

            throw new IllegalStateException(e);
        }
    }

    public static void showFatalError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "Terjadi kesalahan tidak diketahui." : message);
        alert.showAndWait();
    }

    public static void showRecipeForm() {
    loadScene("/view/RecipeFormView.fxml",
              "Form Resep");
}
}
