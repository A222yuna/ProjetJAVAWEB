# 🏥 Gestion Cabinet - Application Psychiatrique

Application JavaFX complète de gestion des cabinets psychiatriques, basée sur une architecture MVC avec MySQL.

---

## 📁 Structure du projet (IntelliJ IDEA)

```
GestionCabinet/
├── pom.xml                                    # Configuration Maven
├── README.md
├── database/
│   └── schema.sql                             # Script SQL complet
└── src/
    └── main/
        ├── java/
        │   └── tn/
        │       └── psy/
        │           └── gestioncabinet/
        │               ├── model/             # Modèles (Entity)
        │               │   ├── Psychologue.java
        │               │   ├── Cabinet.java
        │               │   ├── PsyCabinet.java
        │               │   ├── Patient.java
        │               │   └── Administrateur.java
        │               ├── dao/               # Couche DAO (accès données)
        │               │   ├── PsychologueDAO.java
        │               │   ├── CabinetDAO.java
        │               │   ├── PsyCabinetDAO.java
        │               │   ├── PatientDAO.java
        │               │   └── AdministrateurDAO.java
        │               ├── service/           # Services métier
        │               │   ├── AuthService.java
        │               │   ├── InscriptionService.java
        │               │   ├── PsychologueService.java
        │               │   ├── CabinetService.java
        │               │   └── PsyCabinetService.java
        │               ├── controller/        # Contrôleurs JavaFX
        │               │   ├── LoginController.java
        │               │   ├── InscriptionController.java
        │               │   ├── PsychologueDashboardController.java
        │               │   ├── PatientDashboardController.java
        │               │   ├── AdminDashboardController.java
        │               │   ├── AddCabinetController.java
        │               │   ├── ModifyCabinetController.java
        │               │   └── CabinetDetailsController.java
        │               ├── gui/
        │               │   └── MainApp.java   # Point d'entrée JavaFX
        │               └── util/
        │                   ├── Connexion.java
        │                   ├── PasswordUtil.java     # Hash BCrypt
        │                   ├── DbInitializer.java    # Données de test
        │                   ├── SessionManager.java
        │                   └── SceneManager.java
        └── resources/
            ├── fxml/
            │   ├── login.fxml
            │   ├── inscription.fxml
            │   ├── psychologue_dashboard.fxml
            │   ├── patient_dashboard.fxml
            │   ├── admin_dashboard.fxml
            │   ├── add_cabinet.fxml
            │   ├── modify_cabinet.fxml
            │   └── cabinet_details.fxml
            └── css/
                └── style.css
```

---

## 🚀 ÉTAPE 1 : Créer la base de données

### 1.1 Ouvrir phpMyAdmin

- Démarrer XAMPP/WAMP (Apache + MySQL)
- Accéder à http://localhost/phpmyadmin

### 1.2 Exécuter le script SQL

1. Cliquer sur l'onglet **SQL**
2. Ouvrir le fichier `database/schema.sql` et copier tout son contenu
3. Coller dans la zone de texte de phpMyAdmin
4. Cliquer sur **Exécuter**

✅ La base `gestion_cabinet_db` est créée avec les tables :
- psychologue, patient, administrateur, cabinet, psy_cabinet

✅ Les données de test (utilisateurs avec mots de passe hashés BCrypt) sont créées automatiquement au **premier lancement** de l'application via `DbInitializer`.

---

## ⚙️ ÉTAPE 2 : Configurer la connexion MySQL

Ouvrir le fichier :

```
src/main/java/tn/psy/gestioncabinet/util/Connexion.java
```

Modifier les constantes selon votre configuration :

```java
private static final String URL = "jdbc:mysql://localhost:3306/gestion_cabinet_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String USER = "root";      // Votre utilisateur MySQL
private static final String PASSWORD = "";      // Votre mot de passe MySQL
```

- **USER** : généralement `root` en local
- **PASSWORD** : vide `""` si pas de mot de passe

---

## 📦 ÉTAPE 3 : Dépendances Maven

Le fichier `pom.xml` inclut déjà toutes les dépendances :

- **MySQL Connector** : `com.mysql:mysql-connector-j:8.0.33`
- **JavaFX** : `javafx-controls`, `javafx-fxml`, `javafx-graphics` (version 21.0.1)
- **BCrypt** : `org.mindrot:jbcrypt:0.4` (hash des mots de passe)

Aucune modification n'est nécessaire. IntelliJ téléchargera automatiquement les dépendances à l'ouverture du projet.

---

## 🖥️ ÉTAPE 4 : Ouvrir le projet dans IntelliJ

1. **File** → **Open**
2. Sélectionner le dossier `GestionCabinet` (contenant le `pom.xml`)
3. Choisir **Open as Project**
4. Accepter l'import Maven si demandé
5. Attendre la fin du téléchargement des dépendances (barre de progression en bas)

---

## ▶️ ÉTAPE 5 : Lancer l'application

### Option A : Depuis IntelliJ (recommandé)

1. Ouvrir `src/main/java/tn/psy/gestioncabinet/gui/MainApp.java`
2. Clic droit sur la classe → **Run 'MainApp.main()'**

### Option B : Via Maven (ligne de commande)

```bash
cd GestionCabinet
mvn javafx:run
```

### Option C : Créer une configuration de lancement

1. **Run** → **Edit Configurations**
2. **+** → **Application**
3. **Name** : `Gestion Cabinet`
4. **Main class** : `tn.psy.gestioncabinet.gui.MainApp`
5. **Module** : `GestionCabinet`
6. **OK**

---

## 🔐 Comptes de test

| Rôle        | Email                    | Mot de passe |
|-------------|--------------------------|--------------|
| Psychologue | amira.bensalem@psy.tn    | psy123       |
| Patient     | mohamed.ali@email.com    | patient123   |
| Administrateur | admin@gestioncabinet.tn | admin123     |

---

## 📖 Cas d'utilisation

### Inscription (Patient et Psychologue uniquement)

- **Créer un compte** : Login → "Créer un compte" → Remplir le formulaire (nom, email, mot de passe, confirmer mot de passe, rôle)
- **Validation** : Email unique, mots de passe identiques, format email valide, mot de passe minimum 6 caractères
- **Sécurité** : Mots de passe hashés avec BCrypt
- **Redirection** : Après inscription réussie → retour automatique vers la page de connexion

### Connexion (Login)

- **Champs** : Email, Mot de passe, Rôle (Psychologue, Patient, Administrateur)
- **Validation** : Vérification des champs vides, email et mot de passe dans la base
- **Alertes** : Messages d'erreur clairs en cas d'échec (champs vides, identifiants incorrects)

### Psychologue

- **Ajouter cabinet** : Dashboard → Ajouter → Remplir le formulaire
- **Modifier cabinet** : Sélectionner une ligne → Modifier
- **Supprimer cabinet** : Sélectionner une ligne → Supprimer

### Patient

- **Rechercher cabinet** : Saisir ville/adresse/description → Rechercher
- **Consulter détails** : Sélectionner une ligne → Voir détails

### Administrateur

- **Valider cabinet** : Sélectionner un cabinet "En attente" → Valider
- **Supprimer cabinet** : Sélectionner une ligne → Supprimer

---

## 📊 Schéma de la base de données

### Table psychologue

| Colonne      | Type         | Description              |
|--------------|--------------|--------------------------|
| id_psy       | INT          | Clé primaire, auto-incr. |
| nom          | VARCHAR(100) | Nom                      |
| specialite   | VARCHAR(100) | Spécialité               |
| email        | VARCHAR(100) | Email (unique)           |
| telephone    | VARCHAR(20)  | Téléphone                |
| mot_de_passe | VARCHAR(255) | Mot de passe             |

### Table cabinet

| Colonne      | Type         | Description              |
|--------------|--------------|--------------------------|
| id_cabinet   | INT          | Clé primaire, auto-incr. |
| adresse      | VARCHAR(200) | Adresse                  |
| ville        | VARCHAR(100) | Ville                    |
| horaires     | VARCHAR(200) | Horaires                 |
| description  | TEXT         | Description              |
| valide       | BOOLEAN      | Validé par admin         |
| date_creation| DATETIME     | Date de création         |

### Table psy_cabinet

| Colonne    | Type | Description                    |
|------------|------|--------------------------------|
| id_psy     | INT  | Clé étrangère → psychologue    |
| id_cabinet | INT  | Clé étrangère → cabinet        |
| date_debut | DATE | Date début association         |
| date_fin   | DATE | Date fin (NULL = toujours)     |

---

## ⚠️ Prérequis

- **Java 17** ou supérieur
- **Maven 3.6+**
- **MySQL 5.7+** (XAMPP, WAMP ou installation standalone)
- **phpMyAdmin** (inclus avec XAMPP/WAMP)
- **IntelliJ IDEA** (ou autre IDE avec support Maven et JavaFX)

---

## 🐛 Dépannage

| Erreur | Solution |
|--------|----------|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Maven → Reload Project |
| `Access denied for user 'root'` | Vérifier USER et PASSWORD dans Connexion.java |
| `Unknown database 'gestion_cabinet_db'` | Exécuter schema.sql dans phpMyAdmin |
| `Communications link failure` | Démarrer MySQL (XAMPP/WAMP) |
| JavaFX ne démarre pas | Vérifier Java 17+ et dépendances Maven |
| FXML non trouvé | Vérifier que `src/main/resources` est marqué comme Resources Root |

---

## 📝 Architecture MVC

| Couche    | Rôle |
|-----------|------|
| **Model** | Entités (Psychologue, Cabinet, PsyCabinet) |
| **DAO**   | Accès base de données (CRUD) |
| **Service** | Logique métier |
| **Controller** | Gestion des événements JavaFX |
| **GUI**   | FXML + CSS (interface) |
| **Util**  | Connexion, PasswordUtil (BCrypt), DbInitializer, Session, Navigation |

---

## 🔒 Sécurité - Hash des mots de passe

Les mots de passe sont hashés avec **BCrypt** avant stockage en base :

- **Inscription** : `PasswordUtil.hash(motDePasse)` → stockage du hash
- **Login** : `PasswordUtil.verify(motDePasseSaisi, hashStocke)` → vérification

---

Bonne utilisation ! 🎓
