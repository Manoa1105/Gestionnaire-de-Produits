package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import model.Produit;

import java.io.IOException;
import java.util.Optional;

public class MainController {

    @FXML private TableView<Produit> tableProduits;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, String> colCategorie;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, Integer> colQuantite;
    @FXML private TableColumn<Produit, String> colFournisseur;

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltre;
    @FXML private Label labelValeurStock;

    @FXML private StackPane rootPane; // pour snackbar si utilisé

    private ObservableList<Produit> produits = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialisation des colonnes
        colNom.setCellValueFactory(data -> data.getValue().nomProperty());
        colCategorie.setCellValueFactory(data -> data.getValue().categorieProperty());
        colPrix.setCellValueFactory(data -> data.getValue().prixProperty().asObject());
        colQuantite.setCellValueFactory(data -> data.getValue().quantiteProperty().asObject());
        colFournisseur.setCellValueFactory(data -> data.getValue().fournisseurProperty());

        tableProduits.setItems(produits);
        mettreAJourValeurStock(); // ✅ Mise à jour initiale

        // Initialiser filtre catégorie
        comboFiltre.setItems(FXCollections.observableArrayList(
                "Liant", "Acier", "Granulat", "Ciment", "Bois", "Divers"
        ));

        // 🔍 Filtrage live
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            filtrerProduits();
        });
        comboFiltre.setOnAction(e -> filtrerProduits());
    }

    private void filtrerProduits() {
        String recherche = txtRecherche.getText().toLowerCase().trim();
        String filtreCategorie = comboFiltre.getValue();

        tableProduits.setItems(produits.filtered(p -> {
            boolean correspond = p.getNom().toLowerCase().contains(recherche) ||
                    p.getFournisseur().toLowerCase().contains(recherche);
            boolean categorieMatch = (filtreCategorie == null || filtreCategorie.isEmpty()) ||
                    filtreCategorie.equalsIgnoreCase(p.getCategorie());
            return correspond && categorieMatch;
        }));
    }

    @FXML
    private void reinitialiserFiltres() {
        txtRecherche.clear();
        comboFiltre.getSelectionModel().clearSelection();
        tableProduits.setItems(produits);
    }

    @FXML
    private void ajouterProduit() {
        ouvrirFormulaire(null);
    }

    @FXML
    private void modifierProduit() {
        Produit selection = tableProduits.getSelectionModel().getSelectedItem();
        if (selection != null) {
            ouvrirFormulaire(selection);
        } else {
            showAlert("Veuillez sélectionner un produit à modifier.");
        }
    }

    @FXML
    private void supprimerProduit() {
        Produit produit = tableProduits.getSelectionModel().getSelectedItem();
        if (produit == null) {
            showAlert("Aucun produit sélectionné.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce produit ?");
        confirm.setContentText(produit.getNom());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            produits.remove(produit);
            tableProduits.refresh();
            mettreAJourValeurStock(); // ✅ Mise à jour après suppression
            showSnackbar("Produit supprimé.");
        }
    }

    private void ouvrirFormulaire(Produit produitExistant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ProduitForm.fxml"));
            Parent root = loader.load();

            ProduitFormController controller = loader.getController();
            controller.initialiser(new ProduitFormController.FormCallback() {
                @Override
                public void onProduitAjoute(Produit produit) {
                    produits.add(produit);
                    tableProduits.refresh();
                    mettreAJourValeurStock(); // ✅ après ajout
                    showSnackbar("Produit ajouté avec succès !");
                }

                @Override
                public void onProduitModifie() {
                    tableProduits.refresh();
                    mettreAJourValeurStock(); // ✅ après modification
                    showSnackbar("Produit modifié avec succès !");
                }
            }, produitExistant);

            Stage modal = new Stage();
            modal.setScene(new Scene(root));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(produitExistant != null ? "Modifier Produit" : "Ajouter Produit");
            modal.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire.");
        }
    }

    private void mettreAJourValeurStock() {
        double total = 0;
        for (Produit p : produits) {
            total += p.getPrix() * p.getQuantite();
        }
        labelValeurStock.setText("Valeur du stock : " + String.format("%.0f", total) + " Ar");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSnackbar(String message) {
        if (rootPane == null) return;
        Label msg = new Label(message);
        msg.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 5;");
        rootPane.getChildren().add(msg);
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> rootPane.getChildren().remove(msg));
        }).start();
    }
}
