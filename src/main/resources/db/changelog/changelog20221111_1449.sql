-- liquibase formatted sql

-- changeset nika.avalishvili:1
ALTER TABLE documents
ALTER COLUMN amount TYPE NUMERIC(19,2);
