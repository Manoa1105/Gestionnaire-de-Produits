package model;

import javafx.beans.property.*;

public class Produit {
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty categorie = new SimpleStringProperty();
    private final DoubleProperty prix = new SimpleDoubleProperty();
    private final IntegerProperty quantite = new SimpleIntegerProperty();
    private final StringProperty fournisseur = new SimpleStringProperty();

    public Produit(String nom, String categorie, double prix, int quantite, String fournisseur) {
        this.nom.set(nom);
        this.categorie.set(categorie);
        this.prix.set(prix);
        this.quantite.set(quantite);
        this.fournisseur.set(fournisseur);
    }

    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }

    public String getCategorie() { return categorie.get(); }
    public void setCategorie(String categorie) { this.categorie.set(categorie); }
    public StringProperty categorieProperty() { return categorie; }

    public double getPrix() { return prix.get(); }
    public void setPrix(double prix) { this.prix.set(prix); }
    public DoubleProperty prixProperty() { return prix; }

    public int getQuantite() { return quantite.get(); }
    public void setQuantite(int quantite) { this.quantite.set(quantite); }
    public IntegerProperty quantiteProperty() { return quantite; }

    public String getFournisseur() { return fournisseur.get(); }
    public void setFournisseur(String fournisseur) { this.fournisseur.set(fournisseur); }
    public StringProperty fournisseurProperty() { return fournisseur; }
}
