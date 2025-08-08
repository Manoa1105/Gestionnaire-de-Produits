package util;

import javafx.animation.FadeTransition;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Utils {

    /** Formate un montant avec espace comme séparateur de milliers et SANS décimales. Exemple: 1 500 000 */
    public static String formaterMontant(double montant) {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.FRANCE);
        sym.setGroupingSeparator(' ');
        sym.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0", sym); // pas de décimales
        return df.format(montant);
    }

    public static void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Snackbar simple (2s) sur un StackPane parent. */
    public static void afficherNotification(javafx.scene.layout.StackPane root, String message) {
        if (root == null) return;
        Label msg = new Label(message);
        msg.getStyleClass().addAll("snackbar", "snackbar-label");
        root.getChildren().add(msg);
        FadeTransition ft = new FadeTransition(Duration.millis(1800), msg);
        ft.setFromValue(1); ft.setToValue(0); ft.setDelay(Duration.millis(1200));
        ft.setOnFinished(e -> root.getChildren().remove(msg));
        ft.play();
    }

    /** TextFormatter entier (optionnel si tu veux le remettre). */
    public static TextFormatter<String> textFormatterEntier() {
        return new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null);
    }
    /** TextFormatter décimal (optionnel si tu veux le remettre). */
    public static TextFormatter<String> textFormatterDecimal() {
        return new TextFormatter<>(c -> c.getControlNewText().matches("\\d*(\\.\\d*)?") ? c : null);
    }
}
