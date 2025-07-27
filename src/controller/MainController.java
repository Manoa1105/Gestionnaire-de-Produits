package controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import model.Produit;
import service.ProduitService;
import util.Utils;

public class MainController {

    // 🎯 Lien avec la table et ses colonnes
    @FXML private TableView<Produit> tableProduits;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, String> colCategorie;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, Integer> colQuantite;

    // 🧾 Champs du formulaire
    @FXML private TextField txtNom;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private TextField txtPrix;
    @FXML private TextField txtQuantite;

    @FXML private ComboBox<String> comboFiltre;
    @FXML private Label lblValeurStock;
    @FXML private Label notificationLabel;

    @FXML private StackPane rootPane; // Utilisé plus tard pour animations/modales

    // 📦 Données
    private final ObservableList<Produit> produits = FXCollections.observableArrayList();
    private final ProduitService service = new ProduitService(); // En vue d'une extension

    // 🔁 Initialisation
    public void initialize() {
        // 🧠 Lier les colonnes avec les attributs de Produit
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        // 🧪 Données de démonstration
        produits.addAll(
                new Produit("Ordinateur", "Électronique", 2500.0, 5),
                new Produit("Shampoing", "Hygiène", 20.0, 10)
        );

        // 📋 Remplissage des catégories dans ComboBox
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Électronique", "Vêtements", "Alimentaire", "Maison", "Hygiène", "Loisirs"
        );
        comboCategorie.setItems(categories);
        comboFiltre.setItems(categories);

        // 🔍 Filtrage dynamique par catégorie
        FilteredList<Produit> filteredData = new FilteredList<>(produits, p -> true);
        comboFiltre.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(produit -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return produit.getCategorie().equalsIgnoreCase(newVal);
            });
        });

        // 🔃 Tri automatique
        SortedList<Produit> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableProduits.comparatorProperty());
        tableProduits.setItems(sortedData);

        // 💰 Calcul de la valeur totale du stock
        lblValeurStock.textProperty().bind(Bindings.createStringBinding(() ->
                "Valeur totale : " + Utils.calculerValeurTotale(produits) + " Ar", produits));
    }

    // ➕ Ajouter un produit
    @FXML
    public void ajouterProduit(ActionEvent event) {
        if (Utils.validerChamps(txtNom, comboCategorie, txtPrix, txtQuantite)) {
            try {
                Produit p = new Produit(
                        txtNom.getText(),
                        comboCategorie.getValue(),
                        Double.parseDouble(txtPrix.getText()),
                        Integer.parseInt(txtQuantite.getText())
                );
                produits.add(p);
                Utils.notifier(notificationLabel, "✅ Produit ajouté !");
                Utils.viderChamps(txtNom, comboCategorie, txtPrix, txtQuantite);
            } catch (NumberFormatException e) {
                Utils.notifier(notificationLabel, "⚠️ Prix ou quantité non valide.");
            }
        }
    }

    // ✏️ Modifier un produit
    @FXML
    public void modifierProduit(ActionEvent event) {
        Produit selection = tableProduits.getSelectionModel().getSelectedItem();
        if (selection != null && Utils.validerChamps(txtNom, comboCategorie, txtPrix, txtQuantite)) {
            try {
                selection.setNom(txtNom.getText());
                selection.setCategorie(comboCategorie.getValue());
                selection.setPrix(Double.parseDouble(txtPrix.getText()));
                selection.setQuantite(Integer.parseInt(txtQuantite.getText()));
                tableProduits.refresh();
                Utils.notifier(notificationLabel, "✏️ Produit modifié !");
                Utils.viderChamps(txtNom, comboCategorie, txtPrix, txtQuantite);
            } catch (NumberFormatException e) {
                Utils.notifier(notificationLabel, "⚠️ Prix ou quantité non valide.");
            }
        } else {
            Utils.notifier(notificationLabel, "⚠️ Sélectionnez un produit à modifier.");
        }
    }

    // 🗑️ Supprimer un produit
    @FXML
    public void supprimerProduit(ActionEvent event) {
        Produit selection = tableProduits.getSelectionModel().getSelectedItem();
        if (selection != null) {
            produits.remove(selection);
            Utils.notifier(notificationLabel, "🗑️ Produit supprimé.");
        } else {
            Utils.notifier(notificationLabel, "⚠️ Aucun produit sélectionné.");
        }
    }
    @FXML
    public void confirmerSuppression(ActionEvent event) {
        Produit selection = tableProduits.getSelectionModel().getSelectedItem();
        if (selection != null) {
            // Boîte de confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Voulez-vous vraiment supprimer ce produit ?");
            alert.setContentText("Produit : " + selection.getNom());

            ButtonType oui = new ButtonType("Oui");
            ButtonType non = new ButtonType("Non");
            alert.getButtonTypes().setAll(oui, non);

            alert.showAndWait().ifPresent(response -> {
                if (response == oui) {
                    produits.remove(selection);
                    Utils.notifier(notificationLabel, "🗑️ Produit supprimé.");
                }
            });
        } else {
            Utils.notifier(notificationLabel, "⚠️ Aucun produit sélectionné.");
        }
    }

}
