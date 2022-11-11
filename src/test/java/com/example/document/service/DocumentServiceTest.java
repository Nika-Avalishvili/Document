package com.example.document.service;

import com.example.document.client.EmployeeClient;
import com.example.document.client.EmployeeDTO;
import com.example.document.model.DocumentEntry;
import com.example.document.model.DocumentEntryDTO;
import com.example.document.model.DocumentEntryMapper;
import com.example.document.model.DocumentWithEmployeeDTO;
import com.example.document.repository.DocumentRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.file.Files.readAllBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    DocumentRepository documentRepository;
    @Mock
    EmployeeClient employeeClient;
    DocumentEntryMapper documentEntryMapper;
    DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentEntryMapper = new DocumentEntryMapper();
        documentService = new DocumentService(documentEntryMapper, documentRepository, employeeClient);
    }

    public DocumentEntryDTO createDocumentEntryDTO(int i) {
        return DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022 + i, 9, 30))
                .employeeId(1L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(300))
                .build();
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

        //      Manually Created Info from Excel File
        LocalDate docDate = LocalDate.of(2022, 11, 11);
        LocalDate effectiveDate = LocalDate.of(2022, 12, 11);
        List<DocumentEntryDTO> manuallyCreatedDocEntries =
                List.of(
                        new DocumentEntryDTO(1L, docDate, effectiveDate, 3L, 1L, BigDecimal.valueOf(897.82)),
                        new DocumentEntryDTO(1L, docDate, effectiveDate, 12L, 2L, BigDecimal.valueOf(782.91)),
                        new DocumentEntryDTO(1L, docDate, effectiveDate, 4L, 1L, BigDecimal.valueOf(100.78)),
                        new DocumentEntryDTO(1L, docDate, effectiveDate, 5L, 2L, BigDecimal.valueOf(400.0))
                );

        assertThat(actualDocumentEntryDTOS)
                .usingRecursiveComparison()
                .ignoringFields("value.id", "id")
                .isEqualTo(manuallyCreatedDocEntries);
    }

    @Test
    void insertDocumentEntries() {
        List<DocumentEntryDTO> documentEntryDTOS = List.of(createDocumentEntryDTO(1), createDocumentEntryDTO(2));
        Mockito.when(documentRepository.saveAll(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        List<DocumentEntryDTO> actualDocumentEntryDTOS = documentService.insertDocumentEntries(documentEntryDTOS);

        Assertions.assertEquals(documentEntryDTOS.size(), actualDocumentEntryDTOS.size());

        assertThat(documentEntryDTOS.get(0)).usingRecursiveComparison()
                .ignoringFields("employeeId")
                .isEqualTo(actualDocumentEntryDTOS.get(0));

        assertThat(documentEntryDTOS.get(1)).usingRecursiveComparison()
                .ignoringFields("employeeId")
                .isEqualTo(actualDocumentEntryDTOS.get(1));
    }

    @Test
    void getAllDocuments() {
        List<DocumentEntryDTO> documentEntryDTOS = List.of(createDocumentEntryDTO(1), createDocumentEntryDTO(2));
        Mockito.when(documentRepository.findAll()).thenReturn(documentEntryMapper.dtoToEntity(documentEntryDTOS));

        Assertions.assertEquals(2, documentService.getAllDocuments().size());
    }

    @Test
    void viewDocumentEntry() {
        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "as", "sa", "asds", "asdsad", "as", true, true);
        DocumentEntryDTO documentEntryDTO = createDocumentEntryDTO(1);
        documentEntryDTO.setId(1L);

        Mockito.when(documentRepository.findById(anyLong())).thenReturn(Optional.of(documentEntryMapper.dtoToEntity(documentEntryDTO)));
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        DocumentWithEmployeeDTO expectedDocumentWithEmployeeDTO = DocumentWithEmployeeDTO.of(documentEntryDTO, employeeDTO);
        DocumentWithEmployeeDTO actualDocumentWithEmployeeDTO = documentService.viewDocumentEntry(1L);

        assertThat(actualDocumentWithEmployeeDTO)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedDocumentWithEmployeeDTO);
    }

    @Test
    void viewMultipleDocumentEntries() {
        List<DocumentEntryDTO> documentEntryDTOS = IntStream.range(0, 5).mapToObj(this::createDocumentEntryDTO).collect(Collectors.toList());

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        Mockito.when(documentRepository.findByEffectiveDateBetween(any(), any())).thenAnswer(invocationOnMock -> documentEntryDTOS.stream().filter(e -> (e.getEffectiveDate().isAfter(startDate) && e.getEffectiveDate().isBefore(endDate))).collect(Collectors.toList()));

        List<DocumentEntry> actualDocumentEntries = documentRepository.findByEffectiveDateBetween(startDate, endDate);

        Assertions.assertEquals(3, actualDocumentEntries.size());
    }

    @Test
    void editDocumentEntry() {
        Mockito.when(documentRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        DocumentEntryDTO actualDTO = documentService.editDocumentEntry(createDocumentEntryDTO(1));

        assertThat(createDocumentEntryDTO(1)).usingRecursiveComparison()
                .ignoringFields("employeeId")
                .isEqualTo(actualDTO);
    }

    @Test
    void deleteDocumentEntry() {
        documentService.deleteDocumentEntry(1L);
        Mockito.verify(documentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteMultipleDocumentEntry() {
        documentService.deleteMultipleDocumentEntry(List.of(1L, 3L, 5L));
        Mockito.verify(documentRepository, times(1)).deleteAllByIdInBatch(List.of(1L, 3L, 5L));
    }
}