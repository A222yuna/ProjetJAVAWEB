-- Migration : ajouter la colonne archive à la table cabinet si elle n'existe pas
-- Exécuter ce script si votre table cabinet a été créée sans cette colonne.
-- Utilisation : mysql -u root -p gestion_cabinet_db < migration_add_archive.sql

USE gestion_cabinet_db;

-- Ajouter la colonne archive (échoue silencieusement si elle existe déjà sur certaines versions MySQL)
ALTER TABLE cabinet ADD COLUMN archive BOOLEAN DEFAULT FALSE;

