-- ============================================
-- Migration : colonnes valide et archive sur la table cabinet
-- Exécuter ce script si la colonne 'archive' ou 'valide' est absente.
-- Idempotent : peut être exécuté plusieurs fois sans erreur.
--
-- Utilisation :
--   mysql -u root -p gestion_cabinet_db < database/migration_cabinet_columns.sql
-- Ou dans phpMyAdmin : sélectionner la base gestion_cabinet_db, onglet SQL, coller et exécuter.
-- ============================================

USE gestion_cabinet_db;

-- Ajouter 'valide' uniquement si elle n'existe pas
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cabinet' AND COLUMN_NAME = 'valide');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE cabinet ADD COLUMN valide BOOLEAN DEFAULT FALSE', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ajouter 'archive' uniquement si elle n'existe pas
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cabinet' AND COLUMN_NAME = 'archive');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE cabinet ADD COLUMN archive BOOLEAN DEFAULT FALSE', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
