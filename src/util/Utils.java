package util;

import javafx.animation.FadeTransition;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import model.Produit;

import java.util.List;

public class Utils {

    // ✅ Vérifie que tous les champs sont bien remplis et valides
    public static boolean validerChamps(TextField nom, ComboBox<String> cat, TextField prix, TextField qte) {
        return !nom.getText().isEmpty()
                && cat.getValue() != null
                && prix.getText().matches("\\d+(\\.\\d+)?")  // nombre à virgule ou entier
                && qte.getText().matches("\\d+");           // entier uniquement
    }

    // ✅ Affiche une notification avec effet d’apparition / disparition
    public static void notifier(Label label, String message) {
        label.setText(message);
        FadeTransition fade = new FadeTransition(Duration.seconds(2), label);
        fade.setFromValue(1.0);
        fade.setToValue(0);
        fade.setDelay(Duration.seconds(1));
        fade.play();
    }

    // ✅ Réinitialise tous les champs de saisie
    public static void viderChamps(TextField nom, ComboBox<String> cat, TextField prix, TextField qte) {
        nom.clear();
        cat.getSelectionModel().clearSelection();
        prix.clear();
        qte.clear();
    }

    // ✅ Calcule la valeur totale du stock
    public static double calculerValeurTotale(List<Produit> produits) {
        return produits.stream()
                .mapToDouble(p -> p.getPrix() * p.getQuantite())
                .sum();
    }
}
