-- liquibase formatted sql

-- changeset nika.avalishvili:1
CREATE TABLE document (doc_id SERIAL PRIMARY KEY,
                            document_data json)