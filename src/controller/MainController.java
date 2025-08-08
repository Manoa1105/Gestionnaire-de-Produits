package controller;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.Produit;
import util.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Accueil : KPI + Alertes + Top fournisseurs
 * Produits : Tableau/Grille, filtres par catégories RÉELLES ("Granulats", "Liants")
 */
public class MainController {

    @FXML private StackPane rootPane;
    @FXML private TabPane tabPane;

    // KPI Accueil
    @FXML private Label labelNbProduits, labelValeurTotale, labelTopCategorie, labelTopFournisseur;

    // Accueil – listes
    @FXML private TableView<Produit> tableAlertes;
    @FXML private TableColumn<Produit, String>  colAlerteNom;
    @FXML private TableColumn<Produit, Integer> colAlerteQuantite;
    @FXML private ListView<String> listTopFournisseurs;

    // Produits
    @FXML private TableView<Produit> tableProduits;
    @FXML private TableColumn<Produit, String>  colNom, colCategorie, colFournisseur;
    @FXML private TableColumn<Produit, Double>  colPrix;
    @FXML private TableColumn<Produit, Integer> colQuantite;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltre; // <- filtre par catégorie réelle
    @FXML private Label labelValeurStock;

    // Vue Grille
    @FXML private Button btnBasculeVue;
    @FXML private ScrollPane scrollGrille;
    @FXML private FlowPane grilleProduits;

    private final ObservableList<Produit> produits = FXCollections.observableArrayList();
    private FilteredList<Produit> filtered;
    private SortedList<Produit> sorted;

    private static final int SEUIL_STOCK_BAS = 50;

    // Référentiels
    private static final List<String> CATEGORIES = Arrays.asList("Granulats", "Liants");
    private static final List<String> PRODUITS_GRANULATS = Arrays.asList(
            "Sable fin", "Gravillon", "Gros sable", "Brique", "Moellon", "4/7"
    );
    private static final List<String> PRODUITS_LIANTS = Arrays.asList("Fer", "Ciment");

    @FXML
    public void initialize() {
        // Colonnes
        colNom.setCellValueFactory(d -> d.getValue().nomProperty());
        colCategorie.setCellValueFactory(d -> d.getValue().categorieProperty());
        colPrix.setCellValueFactory(d -> d.getValue().prixProperty().asObject());
        colQuantite.setCellValueFactory(d -> d.getValue().quantiteProperty().asObject());
        colFournisseur.setCellValueFactory(d -> d.getValue().fournisseurProperty());

        // Prix formaté
        colPrix.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : Utils.formaterMontant(value) + " Ar");
            }
        });

        // Données de démo si vide
        if (produits.isEmpty()) {
            produits.addAll(
                    new Produit("Sable fin (m³)", "Granulats", 9000, 300, "Local A"),
                    new Produit("Gros sable (m³)", "Granulats", 12000, 200, "Local A"),
                    new Produit("Ciment 32.5R (sacs)", "Liants", 45000, 120, "Habibo"),
                    new Produit("Fer torsadé Ø10 (pièces)", "Liants", 32000, 80, "JB Madagascar")
            );
        }

        // Filtre par CATÉGORIE réelle
        comboFiltre.setItems(FXCollections.observableArrayList(CATEGORIES));

        // Filtres + tri
        filtered = new FilteredList<>(produits, p -> true);
        txtRecherche.textProperty().addListener((o, a, b) -> appliquerFiltres());
        comboFiltre.setOnAction(e -> appliquerFiltres());

        sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tableProduits.comparatorProperty());
        tableProduits.setItems(sorted);

        // Init tableaux accueil
        mettreAJourValeurStock();
        mettreAJourAccueil();

        if (scrollGrille != null) scrollGrille.setVisible(false);
        if (btnBasculeVue != null) btnBasculeVue.setText("Basculer en vue Grille");
    }

    // ---------------- Navigation
    @FXML private void afficherAccueil() { tabPane.getSelectionModel().select(0); mettreAJourAccueil(); }
    @FXML private void afficherProduits() { tabPane.getSelectionModel().select(1); }

    // ---------------- Accueil
    private void mettreAJourAccueil() {
        labelNbProduits.setText("Nombre total de produits : " + produits.size());
        double total = produits.stream().mapToDouble(p -> p.getPrix() * p.getQuantite()).sum();
        labelValeurTotale.setText("Valeur totale du stock : " + Utils.formaterMontant(total) + " Ar");

        String topCat = produits.stream()
                .collect(Collectors.groupingBy(Produit::getCategorie, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("-");
        labelTopCategorie.setText("Catégorie la plus fréquente : " + topCat);

        String topFourn = produits.stream()
                .collect(Collectors.groupingBy(Produit::getFournisseur, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("-");
        labelTopFournisseur.setText("Fournisseur principal : " + topFourn);

        // Alertes
        ObservableList<Produit> bas = FXCollectors.toObservableList(
                produits.stream()
                        .filter(p -> p.getQuantite() <= SEUIL_STOCK_BAS)
                        .sorted(Comparator.comparingInt(Produit::getQuantite))
        );

        if (tableAlertes != null) {
            if (colAlerteNom != null) colAlerteNom.setCellValueFactory(d -> d.getValue().nomProperty());
            if (colAlerteQuantite != null) colAlerteQuantite.setCellValueFactory(d -> d.getValue().quantiteProperty().asObject());
            tableAlertes.setItems(bas);
        }

        // Top fournisseurs (quantités)
        ObservableList<String> top = FXCollectors.toObservableList(
                produits.stream()
                        .collect(Collectors.groupingBy(Produit::getFournisseur, Collectors.summingInt(Produit::getQuantite)))
                        .entrySet().stream()
                        .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                        .limit(5)
                        .map(e -> e.getKey() + " — " + e.getValue() + " unités")
        );
        if (listTopFournisseurs != null) listTopFournisseurs.setItems(top);
    }

    // ---------------- Filtres Produits
    private void appliquerFiltres() {
        String recherche = txtRecherche.getText() == null ? "" : txtRecherche.getText().toLowerCase().trim();
        String catFiltre = comboFiltre.getValue(); // "Granulats" | "Liants" | null

        filtered.setPredicate(p -> {
            boolean txtOk = p.getNom().toLowerCase().contains(recherche)
                    || p.getFournisseur().toLowerCase().contains(recherche);

            // Compat rétro : si l’enregistrement porte une "catégorie ancienne" (nom de produit),
            // on le fait correspondre au filtre choisi.
            String catProduit = p.getCategorie();
            boolean catOk;
            if (catFiltre == null || catFiltre.isEmpty()) {
                catOk = true;
            } else if (CATEGORIES.contains(catProduit)) {
                catOk = catFiltre.equalsIgnoreCase(catProduit);
            } else {
                // 'catProduit' n’est pas une vraie catégorie → on mappe
                if ("Granulats".equals(catFiltre)) {
                    catOk = PRODUITS_GRANULATS.stream().anyMatch(x -> x.equalsIgnoreCase(catProduit));
                } else { // "Liants"
                    catOk = PRODUITS_LIANTS.stream().anyMatch(x -> x.equalsIgnoreCase(catProduit));
                }
            }
            return txtOk && catOk;
        });

        if (isGrilleVisible()) afficherGrilleProduits();
        mettreAJourValeurStock();
        mettreAJourAccueil();
    }

    @FXML private void reinitialiserFiltres() {
        txtRecherche.clear();
        comboFiltre.getSelectionModel().clearSelection();
        filtered.setPredicate(p -> true);
        if (isGrilleVisible()) afficherGrilleProduits();
        mettreAJourValeurStock();
        mettreAJourAccueil();
    }

    // ---------------- CRUD
    @FXML private void ajouterProduit() { ouvrirFormulaire(null); }
    @FXML private void modifierProduit() {
        Produit sel = tableProduits.getSelectionModel().getSelectedItem();
        if (sel == null) { Utils.afficherAlerte("Modification", "Veuillez sélectionner un produit."); return; }
        ouvrirFormulaire(sel);
    }
    @FXML private void supprimerProduit() {
        Produit p = tableProduits.getSelectionModel().getSelectedItem();
        if (p == null) { Utils.afficherAlerte("Suppression", "Aucun produit sélectionné."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer : " + p.getNom() + " ?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            produits.remove(p);
            tableProduits.refresh();
            appliquerFiltres();
            Utils.afficherNotification(rootPane, "Produit supprimé.");
        }
    }

    private void ouvrirFormulaire(Produit produitExistant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ProduitForm.fxml"));
            Parent root = loader.load();

            ProduitFormController controller = loader.getController();
            controller.initialiser(new ProduitFormController.FormCallback() {
                @Override public void onProduitAjoute(Produit produit) {
                    produits.add(produit);
                    tableProduits.refresh();
                    appliquerFiltres();
                    Utils.afficherNotification(rootPane, "Produit ajouté.");
                }
                @Override public void onProduitModifie() {
                    tableProduits.refresh();
                    appliquerFiltres();
                    Utils.afficherNotification(rootPane, "Produit modifié.");
                }
            }, produitExistant);

            Stage modal = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
            modal.initStyle(StageStyle.UNDECORATED);
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setScene(scene);
            modal.setResizable(false);
            modal.setTitle(produitExistant != null ? "Modifier un produit" : "Ajouter un produit");
            modal.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Utils.afficherAlerte("Erreur", "Impossible d’ouvrir le formulaire.\n" + e.getMessage());
        }
    }

    // ---------------- Valeur stock
    private void mettreAJourValeurStock() {
        double total = sorted == null ? 0
                : sorted.stream().mapToDouble(p -> p.getPrix() * p.getQuantite()).sum();
        labelValeurStock.setText("Valeur du stock : " + Utils.formaterMontant(total) + " Ar");
    }

    // ---------------- Vue Grille
    @FXML private void basculerVue() {
        if (scrollGrille == null || tableProduits == null || btnBasculeVue == null) return;
        boolean afficherGrille = !scrollGrille.isVisible();
        scrollGrille.setVisible(afficherGrille);
        tableProduits.setVisible(!afficherGrille);
        btnBasculeVue.setText(afficherGrille ? "Basculer en vue Tableau" : "Basculer en vue Grille");
        if (afficherGrille) afficherGrilleProduits();
    }
    private boolean isGrilleVisible() { return scrollGrille != null && scrollGrille.isVisible(); }

    private void afficherGrilleProduits() {
        if (grilleProduits == null) return;
        grilleProduits.getChildren().clear();

        SequentialTransition seq = new SequentialTransition();
        int i = 0;
        for (Produit p : sorted) {
            VBox card = creerCarteProduit(p);
            grilleProduits.getChildren().add(card);

            FadeTransition fade = new FadeTransition(Duration.millis(160), card);
            fade.setFromValue(0); fade.setToValue(1); fade.setDelay(Duration.millis(60L * i++));
            seq.getChildren().add(fade);
        }
        seq.play();
    }

    private VBox creerCarteProduit(Produit p) {
        Label nom = new Label(p.getNom());
        nom.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label prix = new Label("Prix : " + Utils.formaterMontant(p.getPrix()) + " Ar");
        Label qte  = new Label("Quantité : " + p.getQuantite());
        Label cat  = new Label("Catégorie : " + p.getCategorie());
        Label frn  = new Label("Fournisseur : " + p.getFournisseur());
        VBox v = new VBox(6, nom, prix, qte, cat, frn);
        VBox card = new VBox(10, v);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 14; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 12, 0, 0, 2);");
        card.setPrefWidth(240);
        card.setOpacity(0);
        return card;
    }
}

/* Utilitaire : Stream/Collection -> ObservableList */
class FXCollectors {
    static <T> ObservableList<T> toObservableList(java.util.stream.Stream<T> stream) {
        return stream.collect(FXCollections::observableArrayList,
                ObservableList::add,
                ObservableList::addAll);
    }
    static <T> ObservableList<T> toObservableList(java.util.Collection<T> coll) {
        return FXCollections.observableArrayList(coll);
    }
}
