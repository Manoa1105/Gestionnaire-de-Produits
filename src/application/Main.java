package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Chargement de l'interface
        Parent root = FXMLLoader.load(getClass().getResource("/view/MainView.fxml"));

        // Création de la scène
        Scene scene = new Scene(root);

        // ✅ Ajout effectif du fichier CSS
        scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());

        // Configuration de la fenêtre
        primaryStage.setTitle("Gestionnaire de Produits");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}