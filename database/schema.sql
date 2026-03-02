-- ============================================
-- GESTION CABINET - Schéma de base de données
-- Application Psychiatrique
-- ============================================

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS gestion_cabinet_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gestion_cabinet_db;

-- ============================================
-- SUPPRESSION DES TABLES (ordre inverse des dépendances)
-- ============================================

DROP TABLE IF EXISTS psy_cabinet;
DROP TABLE IF EXISTS cabinet;
DROP TABLE IF EXISTS psychologue;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS administrateur;

-- ============================================
-- TABLE : psychologue
-- ============================================
CREATE TABLE psychologue (
    id_psy INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    specialite VARCHAR(100),
    email VARCHAR(100) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    mot_de_passe VARCHAR(255) NOT NULL,
    CONSTRAINT chk_psychologue_email CHECK (email LIKE '%@%')
) ENGINE=InnoDB;

-- ============================================
-- TABLE : patient
-- ============================================
CREATE TABLE patient (
    id_patient INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    mot_de_passe VARCHAR(255) NOT NULL,
    CONSTRAINT chk_patient_email CHECK (email LIKE '%@%')
) ENGINE=InnoDB;

-- ============================================
-- TABLE : administrateur
-- ============================================
CREATE TABLE administrateur (
    id_admin INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    CONSTRAINT chk_admin_email CHECK (email LIKE '%@%')
) ENGINE=InnoDB;

-- ============================================
-- TABLE : cabinet
-- ============================================
CREATE TABLE cabinet (
    id_cabinet INT AUTO_INCREMENT PRIMARY KEY,
    adresse VARCHAR(200),
    ville VARCHAR(100),
    horaires VARCHAR(200),
    description TEXT,
    valide BOOLEAN DEFAULT FALSE,
    archive BOOLEAN DEFAULT FALSE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================
-- TABLE : psy_cabinet (relation many-to-many)
-- ============================================
CREATE TABLE psy_cabinet (
    id_psy INT NOT NULL,
    id_cabinet INT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    PRIMARY KEY (id_psy, id_cabinet),
    CONSTRAINT fk_psy_cabinet_psychologue
        FOREIGN KEY (id_psy) REFERENCES psychologue(id_psy)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_psy_cabinet_cabinet
        FOREIGN KEY (id_cabinet) REFERENCES cabinet(id_cabinet)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_psy_cabinet_dates CHECK (date_fin IS NULL OR date_fin >= date_debut)
) ENGINE=InnoDB;

-- ============================================
-- INDEX pour optimiser les recherches
-- ============================================
CREATE INDEX idx_cabinet_ville ON cabinet(ville);
CREATE INDEX idx_cabinet_valide ON cabinet(valide);
CREATE INDEX idx_cabinet_archive ON cabinet(archive);
CREATE INDEX idx_psy_cabinet_psy ON psy_cabinet(id_psy);
CREATE INDEX idx_psy_cabinet_cabinet ON psy_cabinet(id_cabinet);

-- ============================================
-- DONNÉES DE TEST
-- Les données utilisateurs (psychologue, patient, administrateur) sont créées
-- automatiquement au premier lancement de l'application via DbInitializer,
-- avec des mots de passe hashés BCrypt.
--
-- Comptes de test après initialisation :
-- Psychologue : amira.bensalem@psy.tn / psy123
-- Patient : mohamed.ali@email.com / patient123
-- Administrateur : admin@gestioncabinet.tn / admin123
-- ============================================
