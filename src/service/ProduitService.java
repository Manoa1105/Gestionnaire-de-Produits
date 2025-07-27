package service;

import javafx.collections.ObservableList;
import model.Produit;

public class ProduitService {
    public double calculerValeurTotale(ObservableList<Produit> produits) {
        return produits.stream().mapToDouble(p -> p.getPrix() * p.getQuantite()).sum();
    }
}