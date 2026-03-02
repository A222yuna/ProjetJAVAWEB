-- Migration : ajouter la colonne date_creation à la table cabinet si elle n'existe pas
-- Exécuter ce script si votre table cabinet a été créée sans cette colonne.
-- Utilisation : mysql -u root -p gestion_cabinet_db < migration_add_date_creation.sql

USE gestion_cabinet_db;

-- Ajouter la colonne (échoue silencieusement si elle existe déjà sur certaines versions MySQL)
ALTER TABLE cabinet ADD COLUMN date_creation DATETIME DEFAULT CURRENT_TIMESTAMP;
