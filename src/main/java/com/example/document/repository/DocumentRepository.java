package com.example.document.repository;

import com.example.document.model.DocumentEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DocumentRepository extends JpaRepository<DocumentEntry, Long> {
    void deleteAllByIdInBatch(Iterable<Long> ids);

    List<DocumentEntry> findByEffectiveDateBetween(LocalDate to, LocalDate from);

}
