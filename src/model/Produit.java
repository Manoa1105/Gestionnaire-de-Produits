package model;

import javafx.beans.property.*;

public class Produit {
    private final StringProperty nom;
    private final StringProperty categorie;
    private final DoubleProperty prix;
    private final IntegerProperty quantite;

    public Produit(String nom, String categorie, double prix, int quantite) {
        this.nom = new SimpleStringProperty(nom);
        this.categorie = new SimpleStringProperty(categorie);
        this.prix = new SimpleDoubleProperty(prix);
        this.quantite = new SimpleIntegerProperty(quantite);
    }

    public String getNom() { return nom.get(); }
    public void setNom(String value) { nom.set(value); }
    public StringProperty nomProperty() { return nom; }

    public String getCategorie() { return categorie.get(); }
    public void setCategorie(String value) { categorie.set(value); }
    public StringProperty categorieProperty() { return categorie; }

    public double getPrix() { return prix.get(); }
    public void setPrix(double value) { prix.set(value); }
    public DoubleProperty prixProperty() { return prix; }

    public int getQuantite() { return quantite.get(); }
    public void setQuantite(int value) { quantite.set(value); }
    public IntegerProperty quantiteProperty() { return quantite; }
}
