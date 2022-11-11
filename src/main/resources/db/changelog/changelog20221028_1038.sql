-- liquibase formatted sql

-- changeset nika.avalishvili:1
CREATE TABLE documents (id SERIAL PRIMARY KEY,
                            upload_date DATE,
                            effective_date DATE,
                            employee_id INT,
                            benefit_id INT,
                            amount INT)

