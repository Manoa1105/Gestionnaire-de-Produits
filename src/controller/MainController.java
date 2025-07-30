package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import model.Produit;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {

    @FXML private TabPane tabPane;

    @FXML private Label labelNbProduits;
    @FXML private Label labelValeurTotale;
    @FXML private Label labelTopCategorie;
    @FXML private Label labelTopFournisseur;

    @FXML private PieChart pieChartCategories;
    @FXML private BarChart<String, Number> barChartFournisseurs;

    @FXML private TableView<Produit> tableProduits;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, String> colCategorie;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, Integer> colQuantite;
    @FXML private TableColumn<Produit, String> colFournisseur;

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltre;
    @FXML private Label labelValeurStock;
    @FXML private StackPane rootPane;

    @FXML private Button btnBasculeVue;
    @FXML private ScrollPane scrollGrille;
    @FXML private FlowPane grilleProduits;

    private ObservableList<Produit> produits = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(data -> data.getValue().nomProperty());
        colCategorie.setCellValueFactory(data -> data.getValue().categorieProperty());
        colPrix.setCellValueFactory(data -> data.getValue().prixProperty().asObject());
        colQuantite.setCellValueFactory(data -> data.getValue().quantiteProperty().asObject());
        colFournisseur.setCellValueFactory(data -> data.getValue().fournisseurProperty());

        tableProduits.setItems(produits);
        mettreAJourValeurStock();
        mettreAJourDashboard();

        comboFiltre.setItems(FXCollections.observableArrayList(
                "Liant", "Acier", "Granulat", "Ciment", "Bois", "Divers"
        ));

        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrerProduits());
        comboFiltre.setOnAction(e -> filtrerProduits());
    }

    @FXML private void afficherAccueil() {
        tabPane.getSelectionModel().select(0);
        mettreAJourDashboard();
    }

    @FXML private void afficherProduits() {
        tabPane.getSelectionModel().select(1);
    }

    private void filtrerProduits() {
        String recherche = txtRecherche.getText().toLowerCase().trim();
        String filtreCategorie = comboFiltre.getValue();

        tableProduits.setItems(produits.filtered(p -> {
            boolean correspond = p.getNom().toLowerCase().contains(recherche)
                    || p.getFournisseur().toLowerCase().contains(recherche);
            boolean categorieMatch = (filtreCategorie == null || filtreCategorie.isEmpty())
                    || filtreCategorie.equalsIgnoreCase(p.getCategorie());
            return correspond && categorieMatch;
        }));
    }

    @FXML private void reinitialiserFiltres() {
        txtRecherche.clear();
        comboFiltre.getSelectionModel().clearSelection();
        tableProduits.setItems(produits);
        if (scrollGrille.isVisible()) afficherGrilleProduits();
    }

    @FXML private void ajouterProduit() {
        ouvrirFormulaire(null);
    }

    @FXML private void modifierProduit() {
        Produit selection = tableProduits.getSelectionModel().getSelectedItem();
        if (selection != null) {
            ouvrirFormulaire(selection);
        } else {
            showAlert("Veuillez sélectionner un produit à modifier.");
        }
    }

    @FXML private void supprimerProduit() {
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
            mettreAJourValeurStock();
            mettreAJourDashboard();
            if (scrollGrille.isVisible()) afficherGrilleProduits();
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
                    mettreAJourValeurStock();
                    mettreAJourDashboard();
                    if (scrollGrille.isVisible()) afficherGrilleProduits();
                    showSnackbar("Produit ajouté avec succès !");
                }

                @Override
                public void onProduitModifie() {
                    tableProduits.refresh();
                    mettreAJourValeurStock();
                    mettreAJourDashboard();
                    if (scrollGrille.isVisible()) afficherGrilleProduits();
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
        double total = produits.stream().mapToDouble(p -> p.getPrix() * p.getQuantite()).sum();
        labelValeurStock.setText("Valeur du stock : " + String.format("%.0f", total) + " Ar");
    }

    private void mettreAJourDashboard() {
        labelNbProduits.setText("Nombre total de produits : " + produits.size());

        double total = produits.stream().mapToDouble(p -> p.getPrix() * p.getQuantite()).sum();
        labelValeurTotale.setText("Valeur totale du stock : " + String.format("%.0f", total) + " Ar");

        String topCat = produits.stream()
                .collect(Collectors.groupingBy(Produit::getCategorie, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("-");
        labelTopCategorie.setText("Catégorie la plus fréquente : " + topCat);

        String topFourn = produits.stream()
                .collect(Collectors.groupingBy(Produit::getFournisseur, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("-");
        labelTopFournisseur.setText("Fournisseur principal : " + topFourn);

        mettreAJourPieChart();
        mettreAJourBarChart();
    }

    private void mettreAJourPieChart() {
        if (pieChartCategories == null) return;
        pieChartCategories.getData().clear();
        Map<String, Long> repartition = produits.stream()
                .collect(Collectors.groupingBy(Produit::getCategorie, Collectors.counting()));
        repartition.forEach((categorie, count) -> {
            pieChartCategories.getData().add(new PieChart.Data(categorie, count));
        });
    }

    private void mettreAJourBarChart() {
        if (barChartFournisseurs == null) return;
        barChartFournisseurs.getData().clear();
        Map<String, Integer> parFournisseur = produits.stream()
                .collect(Collectors.groupingBy(Produit::getFournisseur,
                        Collectors.summingInt(Produit::getQuantite)));

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        parFournisseur.forEach((fournisseur, quantite) -> {
            serie.getData().add(new XYChart.Data<>(fournisseur, quantite));
        });

        barChartFournisseurs.getData().add(serie);
    }

    @FXML
    private void basculerVue() {
        boolean enGrille = scrollGrille.isVisible();
        scrollGrille.setVisible(!enGrille);
        tableProduits.setVisible(enGrille);
        btnBasculeVue.setText(enGrille ? "🔁 Vue Grille" : "🔁 Vue Tableau");

        if (!enGrille) {
            afficherGrilleProduits();
        }
    }

    private void afficherGrilleProduits() {
        grilleProduits.getChildren().clear();
        for (Produit p : produits) {
            VBox carte = new VBox(8);
            carte.getStyleClass().add("carte-produit");
            carte.setPrefWidth(220);

            Label nom = new Label("🧱 " + p.getNom());
            nom.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label prix = new Label("💰 " + p.getPrix() + " Ar");
            Label quantite = new Label("📦 " + p.getQuantite() + " unités");
            Label categorie = new Label("🏷️ " + p.getCategorie());
            Label fournisseur = new Label("🚚 " + p.getFournisseur());

            carte.getChildren().addAll(nom, prix, quantite, categorie, fournisseur);
            grilleProduits.getChildren().add(carte);
        }
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
