package service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import model.Produit;

import java.util.*;
import java.util.stream.Collectors;

public class ProduitService {

    private List<Produit> produits = new ArrayList<>();

    // ✅ Ajouter un produit
    public void ajouter(Produit produit) {
        produits.add(produit);
    }

    // ✅ Lister tous les produits
    public List<Produit> lister() {
        return new ArrayList<>(produits);
    }

    // ✅ Calculer valeur totale
    public double calculerValeurTotale(ObservableList<Produit> produits) {
        return produits.stream()
                .mapToDouble(p -> p.getPrix() * p.getQuantite())
                .sum();
    }

    // ✅ Extraire toutes les catégories uniques
    public List<String> getToutesCategories() {
        return produits.stream()
                .map(Produit::getCategorie)
                .distinct()
                .collect(Collectors.toList());
    }

    // ✅ Catégorie la plus fréquente
    public String getCategorieLaPlusFrequente() {
        return produits.stream()
                .collect(Collectors.groupingBy(Produit::getCategorie, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
    }

    // ✅ Fournisseur principal
    public String getFournisseurPrincipal() {
        return produits.stream()
                .collect(Collectors.groupingBy(Produit::getFournisseur, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
    }

    // ✅ Données pour BarChart (Quantité par fournisseur)
    public ObservableList<XYChart.Series<String, Number>> getDataBarChart() {
        Map<String, Integer> fournisseurQuantite = produits.stream()
                .collect(Collectors.groupingBy(Produit::getFournisseur,
                        Collectors.summingInt(Produit::getQuantite)));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : fournisseurQuantite.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        return FXCollections.observableArrayList(series);
    }

    // ✅ Données pour PieChart (Répartition par catégorie)
    public ObservableList<PieChart.Data> getDataPieChart() {
        Map<String, Integer> repartition = produits.stream()
                .collect(Collectors.groupingBy(Produit::getCategorie,
                        Collectors.summingInt(Produit::getQuantite)));

        return repartition.entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
}
