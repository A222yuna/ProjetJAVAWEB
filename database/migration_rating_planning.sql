-- Migration : tables rating, disponibilite, creneau
-- Exécuter après schema.sql (base gestion_cabinet_db)

USE gestion_cabinet_db;

-- ============================================
-- TABLE : rating (notation patient sur cabinet)
-- ============================================
CREATE TABLE IF NOT EXISTS rating (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    cabinet_id INT NOT NULL,
    note INT NOT NULL,
    CONSTRAINT chk_note_range CHECK (note BETWEEN 1 AND 5),
    CONSTRAINT uq_rating_patient_cabinet UNIQUE (patient_id, cabinet_id),
    CONSTRAINT fk_rating_patient FOREIGN KEY (patient_id) REFERENCES patient(id_patient) ON DELETE CASCADE,
    CONSTRAINT fk_rating_cabinet FOREIGN KEY (cabinet_id) REFERENCES cabinet(id_cabinet) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_rating_cabinet ON rating(cabinet_id);
CREATE INDEX idx_rating_patient ON rating(patient_id);

-- ============================================
-- TABLE : disponibilite (plages horaires par cabinet)
-- ============================================
CREATE TABLE IF NOT EXISTS disponibilite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cabinet_id INT NOT NULL,
    jour TINYINT NOT NULL COMMENT '1=Lundi..7=Dimanche',
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    duree_consultation INT NOT NULL DEFAULT 30 COMMENT 'Durée en minutes',
    CONSTRAINT chk_jour CHECK (jour BETWEEN 1 AND 7),
    CONSTRAINT chk_duree CHECK (duree_consultation > 0),
    CONSTRAINT fk_disp_cabinet FOREIGN KEY (cabinet_id) REFERENCES cabinet(id_cabinet) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_disp_cabinet ON disponibilite(cabinet_id);

-- ============================================
-- TABLE : creneau (créneaux générés, réservables)
-- ============================================
CREATE TABLE IF NOT EXISTS creneau (
    id INT AUTO_INCREMENT PRIMARY KEY,
    disponibilite_id INT NOT NULL,
    date_creneau DATE NOT NULL,
    heure TIME NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'LIBRE',
    patient_id INT NULL,
    CONSTRAINT chk_statut CHECK (statut IN ('LIBRE', 'RESERVE')),
    CONSTRAINT fk_creneau_disp FOREIGN KEY (disponibilite_id) REFERENCES disponibilite(id) ON DELETE CASCADE,
    CONSTRAINT fk_creneau_patient FOREIGN KEY (patient_id) REFERENCES patient(id_patient) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_creneau_disp ON creneau(disponibilite_id);
CREATE INDEX idx_creneau_date_statut ON creneau(date_creneau, statut);
CREATE INDEX idx_creneau_patient ON creneau(patient_id);
