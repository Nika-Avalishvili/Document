package com.example.document.service;

import com.example.document.model.DocumentEntryDTO;
import com.example.document.model.DocumentEntryMapper;
import com.example.document.repository.DocumentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    DocumentRepository documentRepository;
    DocumentEntryMapper documentEntryMapper;
    DocumentService documentService;


    @BeforeEach
    void setUp() {
        documentEntryMapper = new DocumentEntryMapper();
        documentService = new DocumentService(documentEntryMapper, documentRepository);
    }

    @Test
    void uploadDocument() {
        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        DocumentEntryDTO documentEntryDTO1 = new DocumentEntryDTO(1L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, 900);
        DocumentEntryDTO documentEntryDTO2 = new DocumentEntryDTO(2L, LocalDate.of(2022, 11, 26), LocalDate.of(2022, 12, 5), 1L, 1L, 900);
        documentEntryDTOS.add(documentEntryDTO1);
        documentEntryDTOS.add(documentEntryDTO2);
        Mockito.when(documentRepository.saveAll(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        List<DocumentEntryDTO> actualDocumentEntryDTOS = documentService.uploadDocument(documentEntryDTOS);

        Assertions.assertEquals(documentEntryDTOS.size(), actualDocumentEntryDTOS.size());
        Assertions.assertEquals(documentEntryDTOS.get(0), actualDocumentEntryDTOS.get(0));
        Assertions.assertEquals(documentEntryDTOS.get(1), actualDocumentEntryDTOS.get(1));
    }

    @Test
    void getAllDocuments() {
        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        DocumentEntryDTO documentEntryDTO1 = new DocumentEntryDTO(1L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, 900);
        DocumentEntryDTO documentEntryDTO2 = new DocumentEntryDTO(2L, LocalDate.of(2022, 11, 26), LocalDate.of(2022, 12, 5), 1L, 1L, 900);
        documentEntryDTOS.add(documentEntryDTO1);
        documentEntryDTOS.add(documentEntryDTO2);
        Mockito.when(documentRepository.findAll()).thenReturn(List.of(documentEntryMapper.dtoToEntity(documentEntryDTO1), documentEntryMapper.dtoToEntity(documentEntryDTO2)));

        Assertions.assertEquals(2, documentService.getAllDocuments().size());
    }

    @Test
    void viewDocumentEntry() {
        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        DocumentEntryDTO documentEntryDTO1 = new DocumentEntryDTO(1L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, 900);
        DocumentEntryDTO documentEntryDTO2 = new DocumentEntryDTO(2L, LocalDate.of(2022, 11, 26), LocalDate.of(2022, 12, 5), 1L, 1L, 900);
        documentEntryDTOS.add(documentEntryDTO1);
        documentEntryDTOS.add(documentEntryDTO2);
        Mockito.when(documentRepository.findAll()).thenReturn(List.of(documentEntryMapper.dtoToEntity(documentEntryDTO1), documentEntryMapper.dtoToEntity(documentEntryDTO2)));

        Assertions.assertEquals(2, documentService.getAllDocuments().size());
    }

    @Test
    void editDocumentEntry() {
        DocumentEntryDTO documentEntryDTO = new DocumentEntryDTO(1L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, 900);
        Mockito.when(documentRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        DocumentEntryDTO actualDTO = documentService.editDocumentEntry(documentEntryDTO);

        Assertions.assertEquals(documentEntryDTO, actualDTO);
    }

    @Test
    void deleteDocumentEntry() {
        DocumentEntryDTO documentEntryDTO = new DocumentEntryDTO(1L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, 900);
        documentService.deleteDocumentEntry(1L);
        Mockito.verify(documentRepository, times(1)).deleteById(1L);
    }
}