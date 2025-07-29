package util;

import javafx.animation.FadeTransition;
import javafx.scene.control.*;
import javafx.util.Duration;
import model.Produit;

import java.text.DecimalFormat;
import java.util.List;

public class Utils {

    // ✅ Valide tous les champs (avec fournisseur inclus)
    public static boolean validerChamps(TextField nom, ComboBox<String> cat, TextField prix, TextField qte, TextField fournisseur) {
        return !nom.getText().trim().isEmpty()
                && cat.getValue() != null
                && prix.getText().matches("\\d+(\\.\\d+)?")  // nombre décimal
                && qte.getText().matches("\\d+")            // entier
                && !fournisseur.getText().trim().isEmpty(); // fournisseur non vide
    }

    // ✅ Notification discrète avec fade-out
    public static void notifier(Label label, String message) {
        label.setOpacity(1.0);
        label.setText(message);
        FadeTransition fade = new FadeTransition(Duration.seconds(2), label);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.seconds(1));
        fade.play();
    }

    // ✅ Vider les champs d’un formulaire
    public static void viderChamps(TextField nom, ComboBox<String> cat, TextField prix, TextField qte, TextField fournisseur) {
        nom.clear();
        cat.getSelectionModel().clearSelection();
        prix.clear();
        qte.clear();
        fournisseur.clear();
    }

    // ✅ Calcule valeur totale du stock
    public static double calculerValeurTotale(List<Produit> produits) {
        return produits.stream()
                .mapToDouble(p -> p.getPrix() * p.getQuantite())
                .sum();
    }

    // ✅ Formate un montant avec séparateur
    public static String formaterMontant(double montant) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(montant).replace(",", " ").replace(".", ",");
    }

    // ✅ Affiche une alerte JavaFX classique
    public static void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
