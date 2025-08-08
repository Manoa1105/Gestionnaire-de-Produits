package controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Produit;
import util.Utils;

import java.util.Arrays;
import java.util.List;

public class ProduitFormController {

    @FXML private StackPane formRoot;
    @FXML private Label modalTitle;
    @FXML private TextField txtNom;
    @FXML private ComboBox<String> categorieComboBox;
    @FXML private TextField txtPrix;
    @FXML private TextField txtQuantite;
    @FXML private ComboBox<String> uniteComboBox;
    @FXML private TextField txtFournisseur;
    @FXML private Button btnEnregistrer;

    private FormCallback callback;
    private Produit produitExistant;

    public interface FormCallback {
        void onProduitAjoute(Produit produit);
        void onProduitModifie();
    }

    @FXML
    private void initialize() {
        FadeTransition fade = new FadeTransition(Duration.millis(220), formRoot);
        fade.setFromValue(0); fade.setToValue(1); fade.play();

        categorieComboBox.getItems().setAll(
                "Sable fin","Gravillon","Gros sable","Brique","Moellon","4/7","Fer","Ciment"
        );
        uniteComboBox.getItems().setAll("m³","tonnes","sacs","pièces");

        txtPrix.setTextFormatter(Utils.textFormatterDecimal());
        txtQuantite.setTextFormatter(Utils.textFormatterEntier());

        formRoot.sceneProperty().addListener((obs, o, sc) -> {
            if (sc != null) sc.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) annuler();
                if (e.getCode() == KeyCode.ENTER) enregistrer();
            });
        });
    }

    public void initialiser(FormCallback callback, Produit produit) {
        this.callback = callback;
        this.produitExistant = produit;

        if (produit != null) {
            modalTitle.setText("Modifier un produit");
            btnEnregistrer.setText("Modifier");

            txtNom.setText(produit.getNom());
            categorieComboBox.setValue(produit.getCategorie());
            txtPrix.setText(String.valueOf(produit.getPrix()));
            txtQuantite.setText(String.valueOf(produit.getQuantite()));
            txtFournisseur.setText(produit.getFournisseur());
        } else {
            modalTitle.setText("Ajouter un produit");
            btnEnregistrer.setText("Ajouter");
        }
    }

    @FXML
    private void enregistrer() {
        String nom = txtNom.getText().trim();
        String categorie = categorieComboBox.getValue();
        String unite = uniteComboBox.getValue();
        String fournisseur = txtFournisseur.getText().trim();

        if (nom.isEmpty() || categorie == null || unite == null || fournisseur.isEmpty()
                || txtPrix.getText().isBlank() || txtQuantite.getText().isBlank()) {
            Utils.afficherAlerte("Champs requis",
                    "Veuillez compléter tous les champs (y compris l’unité).");
            return;
        }

        double prix;
        int quantite;
        try {
            prix = Double.parseDouble(txtPrix.getText());
            quantite = Integer.parseInt(txtQuantite.getText());
        } catch (NumberFormatException e) {
            Utils.afficherAlerte("Saisie invalide", "Prix ou quantité invalide.");
            return;
        }

        // on stocke l’unité dans le nom pour rester compatible avec votre modèle actuel
        String nomComplet = nom + " (" + unite + ")";

        if (produitExistant != null) {
            produitExistant.setNom(nomComplet);
            produitExistant.setCategorie(categorie);
            produitExistant.setPrix(prix);
            produitExistant.setQuantite(quantite);
            produitExistant.setFournisseur(fournisseur);
            if (callback != null) callback.onProduitModifie();
        } else {
            if (callback != null) callback.onProduitAjoute(
                    new Produit(nomComplet, categorie, prix, quantite, fournisseur)
            );
        }
        annuler();
    }

    @FXML
    private void annuler() {
        Stage stage = (Stage) formRoot.getScene().getWindow();
        stage.close();
    }
}
