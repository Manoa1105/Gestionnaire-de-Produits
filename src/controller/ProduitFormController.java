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
    @FXML private TextField txtNom;                    // Nom du produit (ex. Sable fin, Ciment…)
    @FXML private ComboBox<String> categorieComboBox;  // "Granulats" | "Liants"
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

    // Référentiels
    private static final List<String> CATEGORIES = Arrays.asList("Granulats", "Liants");
    private static final List<String> PRODUITS_GRANULATS = Arrays.asList(
            "Sable fin", "Gravillon", "Gros sable", "Brique", "Moellon", "4/7"
    );
    private static final List<String> PRODUITS_LIANTS = Arrays.asList("Fer", "Ciment");

    @FXML
    private void initialize() {
        // Apparition douce
        FadeTransition fade = new FadeTransition(Duration.millis(220), formRoot);
        fade.setFromValue(0); fade.setToValue(1); fade.play();

        // Catégories valides
        categorieComboBox.getItems().setAll(CATEGORIES);

        // Unités proposées
        uniteComboBox.getItems().setAll("m³", "tonnes", "sacs", "pièces");

        // Filtre de saisie
        txtPrix.setTextFormatter(Utils.textFormatterDecimal());
        txtQuantite.setTextFormatter(Utils.textFormatterEntier());

        // Raccourcis
        formRoot.sceneProperty().addListener((obs, o, sc) -> {
            if (sc != null) sc.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) annuler();
                if (e.getCode() == KeyCode.ENTER) enregistrer();
            });
        });
    }

    /**
     * Initialisation :
     * - Ajout : titre/bouton adaptés
     * - Modification : remplit les champs + remapping éventuel catégorie héritée
     */
    public void initialiser(FormCallback callback, Produit produit) {
        this.callback = callback;
        this.produitExistant = produit;

        if (produit != null) {
            modalTitle.setText("Modifier un produit");
            btnEnregistrer.setText("Modifier");

            // Remplissage des champs
            txtNom.setText(produit.getNom());
            txtPrix.setText(String.valueOf(produit.getPrix()));
            txtQuantite.setText(String.valueOf(produit.getQuantite()));
            txtFournisseur.setText(produit.getFournisseur());

            // Cas de compatibilité : si l'ancienne "catégorie" est en fait un nom de produit
            String cat = produit.getCategorie();
            if (CATEGORIES.contains(cat)) {
                categorieComboBox.setValue(cat);
            } else {
                // Remap : nom de produit => catégorie
                if (isGranulat(cat)) categorieComboBox.setValue("Granulats");
                else if (isLiant(cat)) categorieComboBox.setValue("Liants");
                else categorieComboBox.getSelectionModel().clearSelection();
            }
        } else {
            modalTitle.setText("Ajouter un produit");
            btnEnregistrer.setText("Ajouter");
            categorieComboBox.setValue("Granulats"); // valeur par défaut
        }
    }

    private boolean isGranulat(String value) {
        return PRODUITS_GRANULATS.stream().anyMatch(p -> p.equalsIgnoreCase(value));
    }
    private boolean isLiant(String value) {
        return PRODUITS_LIANTS.stream().anyMatch(p -> p.equalsIgnoreCase(value));
    }

    @FXML
    private void enregistrer() {
        String nom = txtNom.getText().trim();                 // ex. "Sable fin"
        String categorie = categorieComboBox.getValue();      // "Granulats" | "Liants"
        String unite = uniteComboBox.getValue();              // ex. "m³"
        String fournisseur = txtFournisseur.getText().trim();

        if (nom.isEmpty() || categorie == null || unite == null || fournisseur.isEmpty()
                || txtPrix.getText().isBlank() || txtQuantite.getText().isBlank()) {
            Utils.afficherAlerte("Champs requis",
                    "Veuillez renseigner : Nom, Catégorie, Prix, Quantité, Unité et Fournisseur.");
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

        // Ajoute l’unité au nom affiché (compatibilité avec le modèle existant)
        String nomComplet = nom + " (" + unite + ")";

        if (produitExistant != null) {
            produitExistant.setNom(nomComplet);
            produitExistant.setCategorie(categorie);
            produitExistant.setPrix(prix);
            produitExistant.setQuantite(quantite);
            produitExistant.setFournisseur(fournisseur);
            if (callback != null) callback.onProduitModifie();
        } else {
            if (callback != null) {
                callback.onProduitAjoute(new Produit(nomComplet, categorie, prix, quantite, fournisseur));
            }
        }
        annuler();
    }

    @FXML
    private void annuler() {
        Stage stage = (Stage) formRoot.getScene().getWindow();
        stage.close();
    }
}
