package com.example.document.repository;

import com.example.document.model.DocumentEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntry, Long> {
}
