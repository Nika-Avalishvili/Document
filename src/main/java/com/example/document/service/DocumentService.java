package com.example.document.service;

import com.example.document.model.DocumentEntry;
import com.example.document.model.DocumentEntryDTO;
import com.example.document.model.DocumentEntryMapper;
import com.example.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    public final DocumentEntryMapper documentEntryMapper;
    public final DocumentRepository documentRepository;

    public List<DocumentEntryDTO> uploadDocument(List<DocumentEntryDTO> documentEntryDTOS) {
        List<DocumentEntry> documentEntries = documentEntryMapper.dtoToEntity(documentEntryDTOS);
        documentRepository.saveAll(documentEntries);
        return documentEntryMapper.entityToDto(documentEntries);
    }

    public List<DocumentEntryDTO> getAllDocuments() {
        List<DocumentEntry> allDocumentEntries = documentRepository.findAll();
        return documentEntryMapper.entityToDto(allDocumentEntries);
    }

    public DocumentEntryDTO viewDocumentEntry(Long id) {
        DocumentEntry documentEntry = documentRepository.findById(id).orElse(null);
        return documentEntryMapper.entityToDto(documentEntry);
    }

    public DocumentEntryDTO editDocumentEntry(DocumentEntryDTO documentEntryDTO) {
        DocumentEntry documentEntry = documentEntryMapper.dtoToEntity(documentEntryDTO);
        documentRepository.save(documentEntry);
        return documentEntryMapper.entityToDto(documentEntry);
    }

    public void deleteDocumentEntry(Long id) {
        documentRepository.deleteById(id);
    }

}
