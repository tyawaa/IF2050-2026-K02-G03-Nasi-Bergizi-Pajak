package nasi_bergizi_pajak;

import javafx.application.Application;
import javafx.stage.Stage;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.config.DatabaseInitializer;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        AppNavigator.setStage(stage);

        try {
            DatabaseInitializer.initializeIfNeeded();
            AppNavigator.showLogin();
        } catch (Exception e) {
            AppNavigator.showFatalError("Gagal menyiapkan aplikasi", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
