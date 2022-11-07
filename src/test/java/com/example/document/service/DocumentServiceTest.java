package com.example.document.service;

import com.example.document.model.DocumentEntry;
import com.example.document.model.DocumentEntryDTO;
import com.example.document.model.DocumentEntryMapper;
import com.example.document.repository.DocumentRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.readAllBytes;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    void uploadExcelDocument() throws Exception {
        Path path = Paths.get("src/test/resources/test_files/input_file.xlsx");
        String name = "input_file.xlsx";
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        MockMultipartFile file = new MockMultipartFile(
                name,
                name,
                contentType,
                readAllBytes(path));

        List<DocumentEntryDTO> actualDocumentEntryDTOS = documentService.uploadExcelDocument(file);
        Assertions.assertEquals(4, actualDocumentEntryDTOS.size());
        Assertions.assertEquals(BigDecimal.valueOf(new XSSFWorkbook(file.getInputStream()).getSheetAt(0).getRow(1).getCell(4).getNumericCellValue()), actualDocumentEntryDTOS.get(0).getAmount());
        Assertions.assertEquals(BigDecimal.valueOf(new XSSFWorkbook(file.getInputStream()).getSheetAt(0).getRow(2).getCell(4).getNumericCellValue()), actualDocumentEntryDTOS.get(1).getAmount());
    }

    @Test
    void insertDocumentEntries() {
        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        DocumentEntryDTO documentEntryDTO1 = new DocumentEntryDTO(1L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, BigDecimal.valueOf(300));
        DocumentEntryDTO documentEntryDTO2 = new DocumentEntryDTO(2L, LocalDate.of(2022, 11, 26), LocalDate.of(2022, 12, 5), 1L, 1L, BigDecimal.valueOf(300));
        documentEntryDTOS.add(documentEntryDTO1);
        documentEntryDTOS.add(documentEntryDTO2);
        Mockito.when(documentRepository.saveAll(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        List<DocumentEntryDTO> actualDocumentEntryDTOS = documentService.insertDocumentEntries(documentEntryDTOS);

        Assertions.assertEquals(documentEntryDTOS.size(), actualDocumentEntryDTOS.size());
        Assertions.assertEquals(documentEntryDTOS.get(0), actualDocumentEntryDTOS.get(0));
        Assertions.assertEquals(documentEntryDTOS.get(1), actualDocumentEntryDTOS.get(1));
    }

    @Test
    void getAllDocuments() {
        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        DocumentEntryDTO documentEntryDTO1 = new DocumentEntryDTO(1L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, BigDecimal.valueOf(300));
        DocumentEntryDTO documentEntryDTO2 = new DocumentEntryDTO(2L, LocalDate.of(2022, 11, 26), LocalDate.of(2022, 12, 5), 1L, 1L, BigDecimal.valueOf(300));
        documentEntryDTOS.add(documentEntryDTO1);
        documentEntryDTOS.add(documentEntryDTO2);
        Mockito.when(documentRepository.findAll()).thenReturn(List.of(documentEntryMapper.dtoToEntity(documentEntryDTO1), documentEntryMapper.dtoToEntity(documentEntryDTO2)));

        Assertions.assertEquals(2, documentService.getAllDocuments().size());
    }

    @Test
    void viewDocumentEntry() {
        DocumentEntry documentEntry1 = new DocumentEntry(1L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, BigDecimal.valueOf(300));
        DocumentEntry documentEntry2 = new DocumentEntry(2L, LocalDate.of(2022, 11, 26), LocalDate.of(2022, 12, 5), 1L, 1L, BigDecimal.valueOf(300));

        Mockito.when(documentRepository.findById(anyLong())).thenAnswer(invocationOnMock -> Stream.of(documentEntry1, documentEntry2).filter(e -> e.getId().equals(invocationOnMock.getArgument(0))).findFirst());

        Assertions.assertEquals(BigDecimal.valueOf(300), documentService.viewDocumentEntry(1L).getAmount());
    }

    @Test
    void viewMultipleDocumentEntries() {
        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        for (long i = 0; i < 5; i++) {
            DocumentEntryDTO documentEntryDTO = DocumentEntryDTO.builder()
                    .uploadDate(LocalDate.of(2022, 10, 13))
                    .effectiveDate(LocalDate.of(2022 + (int) i, 9, 30))
                    .employeeId(1L)
                    .benefitId(2L)
                    .amount(BigDecimal.valueOf(300 * i))
                    .build();
            documentEntryDTOS.add(documentEntryDTO);
        }

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        Mockito.when(documentRepository.findByEffectiveDateBetween(any(), any())).thenAnswer(invocationOnMock -> documentEntryDTOS.stream().filter(e -> (e.getEffectiveDate().isAfter(startDate) && e.getEffectiveDate().isBefore(endDate))).collect(Collectors.toList()));

        List<DocumentEntry> actualDocumentEntries = documentRepository.findByEffectiveDateBetween(startDate, endDate);

        Assertions.assertEquals(3, actualDocumentEntries.size());
    }

    @Test
    void editDocumentEntry() {
        DocumentEntryDTO documentEntryDTO = new DocumentEntryDTO(1L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, BigDecimal.valueOf(300));
        Mockito.when(documentRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        DocumentEntryDTO actualDTO = documentService.editDocumentEntry(documentEntryDTO);

        Assertions.assertEquals(documentEntryDTO, actualDTO);
    }

    @Test
    void deleteDocumentEntry() {
        DocumentEntryDTO documentEntryDTO = new DocumentEntryDTO(1L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, BigDecimal.valueOf(300));
        documentService.deleteDocumentEntry(1L);
        Mockito.verify(documentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteMultipleDocumentEntry() {
        DocumentEntryDTO documentEntryDTO1 = new DocumentEntryDTO(1L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, BigDecimal.valueOf(400));
        DocumentEntryDTO documentEntryDTO2 = new DocumentEntryDTO(2L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, BigDecimal.valueOf(509));
        DocumentEntryDTO documentEntryDTO3 = new DocumentEntryDTO(3L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, BigDecimal.valueOf(893));
        DocumentEntryDTO documentEntryDTO4 = new DocumentEntryDTO(4L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, BigDecimal.valueOf(233));
        DocumentEntryDTO documentEntryDTO5 = new DocumentEntryDTO(5L, LocalDate.of(2022, 10, 18), LocalDate.of(2022, 11, 15), 1L, 1L, BigDecimal.valueOf(123));
        documentService.deleteMultipleDocumentEntry(List.of(1L, 3L, 5L));
        Mockito.verify(documentRepository, times(1)).deleteAllByIdInBatch(List.of(1L, 3L, 5L));
    }
}