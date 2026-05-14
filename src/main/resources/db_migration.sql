-- Migration: Add CONFIRMED and PAID to appointments.status enum
-- Run this once against your psychologie_app database

ALTER TABLE `appointments`
  MODIFY COLUMN `status`
    ENUM('SCHEDULED','CONFIRMED','PAID','CANCELLED','COMPLETED')
    NOT NULL DEFAULT 'SCHEDULED';
