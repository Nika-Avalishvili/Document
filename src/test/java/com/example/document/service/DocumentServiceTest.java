package com.example.document.service;

import com.example.document.client.*;
import com.example.document.model.DocumentEntryDTO;
import com.example.document.model.DocumentEntryMapper;
import com.example.document.model.DocumentWithEmployeeDTOAndBenefitDTO;
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

    @Mock
    BenefitClient benefitClient;
    DocumentEntryMapper documentEntryMapper;
    DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentEntryMapper = new DocumentEntryMapper();
        documentService = new DocumentService(documentEntryMapper, documentRepository, employeeClient, benefitClient);
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

        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        BenefitDTO benefitDTO = new BenefitDTO(1L, "Salary", new BenefitTypeDTO(1L, "Accrual"), new CalculationMethodDTO(1L, "Net"));
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);
        Mockito.when(benefitClient.getBenefitDtoById(anyLong())).thenReturn(benefitDTO);

        List<DocumentWithEmployeeDTOAndBenefitDTO> actualDocumentWithEmployeeDTOAndBenefitDTOS = documentService.uploadExcelDocument(file);
        Assertions.assertEquals(4, actualDocumentWithEmployeeDTOAndBenefitDTOS.size());

        //      Manually Created Info from Excel File
        LocalDate docDate = LocalDate.of(2022, 11, 11);
        LocalDate effectiveDate = LocalDate.of(2022, 12, 11);
        List<DocumentWithEmployeeDTOAndBenefitDTO> manuallyCreatedDocEntries =
                List.of(
                        new DocumentWithEmployeeDTOAndBenefitDTO(1L, docDate, effectiveDate, employeeDTO, benefitDTO, BigDecimal.valueOf(897.82)),
                        new DocumentWithEmployeeDTOAndBenefitDTO(1L, docDate, effectiveDate, employeeDTO, benefitDTO, BigDecimal.valueOf(782.91)),
                        new DocumentWithEmployeeDTOAndBenefitDTO(1L, docDate, effectiveDate, employeeDTO, benefitDTO, BigDecimal.valueOf(100.78)),
                        new DocumentWithEmployeeDTOAndBenefitDTO(1L, docDate, effectiveDate, employeeDTO, benefitDTO, BigDecimal.valueOf(400.0))
                );

        assertThat(actualDocumentWithEmployeeDTOAndBenefitDTOS)
                .usingRecursiveComparison()
                .ignoringFields("value.id", "id")
                .isEqualTo(manuallyCreatedDocEntries);
    }

    @Test
    void insertDocumentEntries() {
        List<DocumentEntryDTO> documentEntryDTOS = List.of(createDocumentEntryDTO(1), createDocumentEntryDTO(2));
        Mockito.when(documentRepository.saveAll(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        BenefitDTO benefitDTO = new BenefitDTO(1L, "Salary", new BenefitTypeDTO(1L, "Accrual"), new CalculationMethodDTO(1L, "Net"));
        Mockito.when(benefitClient.getBenefitDtoById(anyLong())).thenReturn(benefitDTO);

        List<DocumentWithEmployeeDTOAndBenefitDTO> actualDocumentWithEmployeeDTOAndBenefitDTO = documentService.insertDocumentEntries(documentEntryDTOS);
        List<DocumentWithEmployeeDTOAndBenefitDTO> expectedDocumentWithEmployeeDTOAndBenefitDTO = List.of(DocumentWithEmployeeDTOAndBenefitDTO.of(documentEntryDTOS.get(0),employeeDTO, benefitDTO),
                DocumentWithEmployeeDTOAndBenefitDTO.of(documentEntryDTOS.get(1), employeeDTO, benefitDTO));

        Assertions.assertEquals(expectedDocumentWithEmployeeDTOAndBenefitDTO.size(), actualDocumentWithEmployeeDTOAndBenefitDTO.size());

        assertThat(expectedDocumentWithEmployeeDTOAndBenefitDTO.get(0)).usingRecursiveComparison()
                .ignoringFields("employeeId")
                .isEqualTo(actualDocumentWithEmployeeDTOAndBenefitDTO.get(0));

        assertThat(expectedDocumentWithEmployeeDTOAndBenefitDTO.get(1)).usingRecursiveComparison()
                .ignoringFields("employeeId")
                .isEqualTo(actualDocumentWithEmployeeDTOAndBenefitDTO.get(1));

        assertThat(employeeDTO.getFirstName())
                .isEqualTo(actualDocumentWithEmployeeDTOAndBenefitDTO.get(1).getEmployeeDTO().getFirstName());
    }

    @Test
    void getAllDocuments() {
        List<DocumentEntryDTO> documentEntryDTOS = List.of(createDocumentEntryDTO(1), createDocumentEntryDTO(2));
        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);

        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);
        Mockito.when(documentRepository.findAll()).thenReturn(documentEntryMapper.dtoToEntity(documentEntryDTOS));

        List<DocumentWithEmployeeDTOAndBenefitDTO> allDocuments = documentService.getAllDocuments();

        Assertions.assertEquals(2, allDocuments.size());
        assertThat(allDocuments.get(0).getEmployeeDTO().getLastName()).isEqualTo(employeeDTO.getLastName());
    }

    @Test
    void viewDocumentEntry() {
        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        DocumentEntryDTO documentEntryDTO = createDocumentEntryDTO(1);
        documentEntryDTO.setId(1L);

        BenefitDTO benefitDTO = new BenefitDTO(1L, "Salary", new BenefitTypeDTO(1L, "Accrual"), new CalculationMethodDTO(1L, "Net"));


        Mockito.when(documentRepository.findById(anyLong())).thenReturn(Optional.of(documentEntryMapper.dtoToEntity(documentEntryDTO)));
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);
        Mockito.when(benefitClient.getBenefitDtoById(anyLong())).thenReturn(benefitDTO);

        DocumentWithEmployeeDTOAndBenefitDTO expectedDocumentWithEmployeeDTOAndBenefitDTO = DocumentWithEmployeeDTOAndBenefitDTO.of(documentEntryDTO, employeeDTO, benefitDTO);
        DocumentWithEmployeeDTOAndBenefitDTO actualDocumentWithEmployeeDTOAndBenefitDTO = documentService.viewDocumentEntry(1L);

        assertThat(actualDocumentWithEmployeeDTOAndBenefitDTO)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedDocumentWithEmployeeDTOAndBenefitDTO);
    }

    @Test
    void viewMultipleDocumentEntries() {
        List<DocumentEntryDTO> documentEntryDTOS = IntStream.range(0, 5).mapToObj(this::createDocumentEntryDTO).collect(Collectors.toList());

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        Mockito.when(documentRepository.findByEffectiveDateBetween(any(), any())).thenAnswer(invocationOnMock -> documentEntryMapper.dtoToEntity(documentEntryDTOS).stream().filter(e -> (e.getEffectiveDate().isAfter(startDate) && e.getEffectiveDate().isBefore(endDate))).collect(Collectors.toList()));

        List<DocumentWithEmployeeDTOAndBenefitDTO> actualDocumentWithEmployeeDTOAndBenefitDTOS = documentService.viewMultipleDocumentEntries(startDate, endDate);


        Assertions.assertEquals(3, actualDocumentWithEmployeeDTOAndBenefitDTOS.size());
        assertThat(actualDocumentWithEmployeeDTOAndBenefitDTOS.get(0).getEmployeeDTO().getDepartment())
                .isEqualTo(employeeDTO.getDepartment());
    }

    @Test
    void editDocumentEntry() {
        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        BenefitDTO benefitDTO = new BenefitDTO(1L, "Salary", new BenefitTypeDTO(1L, "Accrual"), new CalculationMethodDTO(1L, "Net"));
        Mockito.when(benefitClient.getBenefitDtoById(anyLong())).thenReturn(benefitDTO);

        Mockito.when(documentRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        DocumentWithEmployeeDTOAndBenefitDTO actualDocumentWithEmployeeDTOAndBenefitDTOs = documentService.editDocumentEntry(createDocumentEntryDTO(1));

        DocumentWithEmployeeDTOAndBenefitDTO expectedDocumentWithEmployeeDTOAndBenefitDTOs = DocumentWithEmployeeDTOAndBenefitDTO.of(createDocumentEntryDTO(1), employeeDTO, benefitDTO);

        assertThat(actualDocumentWithEmployeeDTOAndBenefitDTOs)
                .isEqualTo(expectedDocumentWithEmployeeDTOAndBenefitDTOs);
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