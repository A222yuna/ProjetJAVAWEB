-- Run this in phpMyAdmin on the psychologie_app database
USE psychologie_app;

ALTER TABLE users 
  ADD COLUMN IF NOT EXISTS statut_validation ENUM('en_attente','approuve','rejete') DEFAULT 'en_attente';

-- Set psychologues to 'en_attente' by default, others to 'approuve'
UPDATE users 
SET statut_validation = CASE 
    WHEN role = 'Psychologue' THEN 'en_attente'
    ELSE 'approuve'
END
WHERE statut_validation IS NULL;

-- IMPORTANT: Your current passwords are plain text (pass123).
-- The app uses BCrypt hashing. You have two options:
-- OPTION A (easiest): update your test passwords to a BCrypt hash of "pass123"
UPDATE users SET mot_de_passe = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkdEjuMYy6W';
-- That hash above = "admin123" ... use the app's Register page to create proper accounts instead.

-- OR just delete all test rows and use Register to create real accounts:
-- DELETE FROM users;

-- Check result:
SELECT id_user, nom, prenom, email, role, statut_validation FROM users;
