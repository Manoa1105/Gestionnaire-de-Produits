package controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Produit;

public class ProduitFormController {

    @FXML private StackPane formRoot; // Pour l'animation fade-in
    @FXML private TextField txtNom;
    @FXML private ComboBox<String> categorieComboBox;
    @FXML private TextField txtPrix;
    @FXML private TextField txtQuantite;
    @FXML private TextField txtFournisseur;
    @FXML private Button btnEnregistrer;

    private FormCallback callback;
    private Produit produitExistant;

    // ✅ Interface pour transmettre les actions à MainController
    public interface FormCallback {
        void onProduitAjoute(Produit produit);
        void onProduitModifie();
    }

    // ✅ Initialisation du formulaire (mode ajout ou modification)
    public void initialiser(FormCallback callback, Produit produit) {
        this.callback = callback;
        this.produitExistant = produit;

        // Liste des catégories proposées
        categorieComboBox.getItems().addAll(
                "Liant", "Acier", "Granulat", "Ciment", "Fer", "Brique", "Sable fin", "Sable gros", "4/7", "Moellon"
        );

        if (produit != null) {
            txtNom.setText(produit.getNom());
            categorieComboBox.setValue(produit.getCategorie());
            txtPrix.setText(String.valueOf(produit.getPrix()));
            txtQuantite.setText(String.valueOf(produit.getQuantite()));
            txtFournisseur.setText(produit.getFournisseur());
            btnEnregistrer.setText("Modifier");
        } else {
            btnEnregistrer.setText("Ajouter");
        }
    }

    // ✅ Animation Fade à l’ouverture
    @FXML
    private void initialize() {
        FadeTransition fade = new FadeTransition(Duration.millis(400), formRoot);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    // ✅ Enregistrement ou modification
    @FXML
    private void enregistrer() {
        String nom = txtNom.getText();
        String categorie = categorieComboBox.getValue();
        String fournisseur = txtFournisseur.getText();
        double prix;
        int quantite;

        try {
            prix = Double.parseDouble(txtPrix.getText());
            quantite = Integer.parseInt(txtQuantite.getText());
        } catch (NumberFormatException e) {
            showAlert("Prix ou quantité invalide.");
            return;
        }

        if (nom.isEmpty() || categorie == null || fournisseur.isEmpty()) {
            showAlert("Tous les champs sont requis.");
            return;
        }

        if (produitExistant != null) {
            produitExistant.setNom(nom);
            produitExistant.setCategorie(categorie);
            produitExistant.setPrix(prix);
            produitExistant.setQuantite(quantite);
            produitExistant.setFournisseur(fournisseur);
            callback.onProduitModifie();
        } else {
            Produit nouveau = new Produit(nom, categorie, prix, quantite, fournisseur);
            callback.onProduitAjoute(nouveau);
        }

        // ✅ Ferme la fenêtre
        ((Stage) btnEnregistrer.getScene().getWindow()).close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}