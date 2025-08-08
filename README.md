# Gestionnaire de Produits — JavaFX

Application de gestion de produits (BTP) développée avec **JavaFX**.

## Fonctionnalités

* **CRUD Produits** : Ajouter, Modifier, Supprimer
* Recherche + filtre par catégorie
* Deux vues : **Tableau** et **Grille**
* **Formulaire unique** pour Ajouter/Modifier avec design moderne et unités de quantité *(m³, tonnes, sacs, pièces)*
* Accueil : **alertes de stock bas** + **Top fournisseurs** (par quantités)
* Prix formatés (`1 500 000 Ar`)
* Thème CSS cohérent (palette verte) avec TableView, boutons, modals et cartes produits stylés

## Aperçu

<img width="1916" height="944" alt="image" src="https://github.com/user-attachments/assets/c95f4c55-172a-4155-9e13-7551845073b4" />

## Structure du projet

```
src/
  application/Main.java
  controller/MainController.java
  controller/ProduitFormController.java
  model/Produit.java
  service/ProduitService.java (si utilisé)

resources/
  view/MainView.fxml
  view/ProduitForm.fxml
  style/style.css
```

## Prérequis

* **Java 17+** (testé avec Java 24)
* **JavaFX 21+** (libs présentes dans `/opt/javafx/lib` ou configurées dans IntelliJ IDEA)

## Lancer dans IntelliJ IDEA (recommandé)

1. Ouvrir le projet.
2. Dans la configuration **Run/Debug**, ajouter les **VM options** :

   ```
   --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml
   ```
3. Lancer `application.Main`.

## Lancer en ligne de commande

```bash
javac --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml \
  -d out $(find src -name "*.java")

java --module-path /opt/javafx/lib:out \
  --add-modules javafx.controls,javafx.fxml \
  application.Main
```

> Adapter `/opt/javafx/lib` selon l'emplacement de JavaFX sur votre machine.

## Utilisation

* **Onglet Produits** : gère la liste, applique des filtres, bascule entre **Grille** et **Tableau**.
* **Boutons Ajouter / Modifier** : ouvrent un modal avec validation, unité de quantité et raccourcis *(Entrée/Échap)*.
* **Onglet Accueil** :

  * Alertes de stock bas *(seuil par défaut : 50 unités)*
  * Top fournisseurs par quantités.

## Personnalisation

* **Seuil d’alerte** : variable `SEUIL_STOCK_BAS` dans `MainController.java`
* **Palette & Styles** : fichier `resources/style/style.css`
* **Catégories pré-remplies** *(granulats & liants)* : dans `ProduitFormController`

## Licence

Projet libre d’utilisation dans un cadre académique.
