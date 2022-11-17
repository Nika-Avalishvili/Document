package com.example.document.controller;

import com.example.document.client.EmployeeClient;
import com.example.document.client.EmployeeDTO;
import com.example.document.model.DocumentEntryDTO;
import com.example.document.model.DocumentWithEmployeeDTO;
import com.example.document.repository.DocumentRepository;
import com.example.document.service.DocumentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.file.Files.readAllBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllerTest {

    @MockBean
    private EmployeeClient employeeClient;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DocumentRepository documentRepository;


    @BeforeEach
    void cleanUp() {
        documentRepository.deleteAll();
    }

    public DocumentEntryDTO createDocumentEntryDTO(int i) {
        return DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022 + i, 9, 30))
                .employeeId(1L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(300.0))
                .build();
    }

    @Test
    void uploadExcelDocument() throws Exception {

        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        Path path = Paths.get("src/test/resources/test_files/input_file.xlsx");
        String name = "input_file.xlsx";
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        MockMultipartFile file = new MockMultipartFile(
                name,
                name,
                contentType,
                readAllBytes(path));

        String responseAsAString = mockMvc.perform(multipart("/document/upload")
                        .file("file", file.getBytes()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentWithEmployeeDTO> actualDocumentEntryDTOS = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

//      Manually Created Info from Excel File
        LocalDate docDate = LocalDate.of(2022, 11, 11);
        LocalDate effectiveDate = LocalDate.of(2022, 12, 11);
        List<DocumentWithEmployeeDTO> manuallyCreatedDocEntries =
                List.of(
                        new DocumentWithEmployeeDTO(1L, docDate, effectiveDate, employeeDTO, 1L, BigDecimal.valueOf(897.82)),
                        new DocumentWithEmployeeDTO(1L, docDate, effectiveDate, employeeDTO, 2L, BigDecimal.valueOf(782.91)),
                        new DocumentWithEmployeeDTO(1L, docDate, effectiveDate, employeeDTO, 1L, BigDecimal.valueOf(100.78)),
                        new DocumentWithEmployeeDTO(1L, docDate, effectiveDate, employeeDTO, 2L, BigDecimal.valueOf(400.0))
                );


        assertThat(actualDocumentEntryDTOS)
                .usingRecursiveComparison()
                .ignoringFields("value.id", "id")
                .isEqualTo(manuallyCreatedDocEntries);

        Assertions.assertEquals(4, actualDocumentEntryDTOS.size());
    }

    @Test
    void insertDocumentEntries() throws Exception {
        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        List<DocumentEntryDTO> documentEntryDTOS = List.of(createDocumentEntryDTO(1));
        documentService.insertDocumentEntries(documentEntryDTOS);

        String requestJson = objectMapper.writeValueAsString(documentEntryDTOS);

        String responseAsAString = mockMvc.perform(MockMvcRequestBuilders.post("/document/insert")
                        .contentType(APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentWithEmployeeDTO> actualDocumentEntryDTOS = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

        List<DocumentWithEmployeeDTO> expectedResult = List.of(DocumentWithEmployeeDTO.of(documentEntryDTOS.get(0), employeeDTO));

        assertThat(actualDocumentEntryDTOS)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedResult);
    }
    @Test
    void viewAllDocuments() throws Exception {

        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        List<DocumentEntryDTO> documentEntryDTOS = List.of(createDocumentEntryDTO(1), createDocumentEntryDTO(2));
        documentService.insertDocumentEntries(documentEntryDTOS);

        List<DocumentWithEmployeeDTO> expectedDocumentWithEmployeeDTO = IntStream.range(0, documentEntryDTOS.size()).mapToObj(i -> DocumentWithEmployeeDTO.of(documentEntryDTOS.get(i), employeeDTO)).collect(Collectors.toList());

        String responseAsAString = mockMvc.perform(MockMvcRequestBuilders.get("/document/viewAll"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentWithEmployeeDTO> actualDocumentWithEmployeeDTO = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

        Assertions.assertEquals(2, expectedDocumentWithEmployeeDTO.size());
        Assertions.assertEquals(2, actualDocumentWithEmployeeDTO.size());

        assertThat(expectedDocumentWithEmployeeDTO)
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .ignoringFields("id", "employeeId")
                .isEqualTo(actualDocumentWithEmployeeDTO);
    }

    @Test
    void viewDocumentEntry() throws Exception {
        DocumentEntryDTO documentEntryDTO = createDocumentEntryDTO(1);
        documentEntryDTO.setId(1L);
        EmployeeDTO employeeDTO = EmployeeDTO.builder()
                .id(1L)
                .firstName("Nika")
                .lastName("Avalishvili")
                .department("Business Development")
                .positions("Business Builder")
                .email("na@gmail.com")
                .isActive(true)
                .isPensionsPayer(true)
                .build();

        Long firstId = documentService.insertDocumentEntries(List.of(documentEntryDTO)).stream().findFirst().get().getId();

        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        String responseAsAString = mockMvc.perform(MockMvcRequestBuilders.get("/document/viewById/{id}", firstId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        DocumentWithEmployeeDTO expectedDocumentWithEmployeeDTO = DocumentWithEmployeeDTO.of(documentEntryDTO, employeeDTO);
        DocumentWithEmployeeDTO actualDocumentWithEmployeeDTO = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

        assertThat(actualDocumentWithEmployeeDTO)
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .ignoringFields("id")
                .isEqualTo(expectedDocumentWithEmployeeDTO);
    }


    @Test
    void viewMultipleDocumentEntries() throws Exception {
        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        List<DocumentEntryDTO> documentEntryDTOS = IntStream.range(0, 5).mapToObj(this::createDocumentEntryDTO).collect(Collectors.toList());

        documentService.insertDocumentEntries(documentEntryDTOS);

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        String requestJson = objectMapper.writeValueAsString(documentEntryDTOS);

        String responseAsAString = mockMvc.perform(MockMvcRequestBuilders.get("/document/viewMultipleByDates/{startDate}/{endDate}", startDate, endDate)
                        .contentType(APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentWithEmployeeDTO> actualDocumentWithEmployeeDTO = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

        Assertions.assertEquals(3, actualDocumentWithEmployeeDTO.size());
        Assertions.assertEquals(actualDocumentWithEmployeeDTO.get(0).getEffectiveDate().getYear(), 2022);
        Assertions.assertEquals(actualDocumentWithEmployeeDTO.get(1).getEffectiveDate().getYear(), 2023);
        Assertions.assertEquals(actualDocumentWithEmployeeDTO.get(2).getEffectiveDate().getYear(), 2024);
        Assertions.assertEquals(actualDocumentWithEmployeeDTO.get(2).getEmployeeDTO().getFirstName(), "Nika");
    }


    @Test
    void editDocumentEntry() throws Exception {
        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        List<DocumentEntryDTO> documentEntryDTOS = IntStream.range(0, 2).mapToObj(this::createDocumentEntryDTO).collect(Collectors.toList());
        documentService.insertDocumentEntries(documentEntryDTOS);

        String requestJson = objectMapper.writeValueAsString(documentEntryDTOS.get(1));

        String responseAsAString = mockMvc.perform(MockMvcRequestBuilders.put("/document/updateEntry")
                        .contentType(APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        DocumentWithEmployeeDTO actualDocumentWithEmployeeDTO = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

        DocumentWithEmployeeDTO expectedDocumentWithEmployeeDTO = DocumentWithEmployeeDTO.of(documentEntryDTOS.get(1), employeeDTO);

        assertThat(documentEntryDTOS.get(1))
                .usingRecursiveComparison()
                .ignoringFields("id", "employeeId")
                .isEqualTo(actualDocumentWithEmployeeDTO);
    }

    @Test
    void deleteDocumentEntry() throws Exception {
        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        List<DocumentEntryDTO> documentEntryDTOS = IntStream.range(0, 2).mapToObj(this::createDocumentEntryDTO).collect(Collectors.toList());
        Long firstId = documentService.insertDocumentEntries(documentEntryDTOS).get(0).getId();


        mockMvc.perform(MockMvcRequestBuilders.delete("/document/deleteEntry?id={id}", firstId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String lastResponseAsAString = mockMvc.perform(MockMvcRequestBuilders.get("/document/viewAll"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentWithEmployeeDTO> actualDocumentEntryDTOList = objectMapper.readValue(lastResponseAsAString, new TypeReference<>() {
        });

        List<DocumentWithEmployeeDTO> expectedDocumentWithEmployeeDTOS = List.of(DocumentWithEmployeeDTO.of(documentEntryDTOS.get(1), employeeDTO));

        assertThat(actualDocumentEntryDTOList)
                .hasSize(1)
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .ignoringFields("value.id", "id")
                .isEqualTo(expectedDocumentWithEmployeeDTOS);
    }

    @Test
    void deleteMultipleDocumentEntries() throws Exception {
        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "Nika", "Avalishvili", "Department1", "Position1", "email1", true, true);
        Mockito.when(employeeClient.getEmployeeById(anyLong())).thenReturn(employeeDTO);

        List<DocumentEntryDTO> documentEntryDTOS = IntStream.range(0, 5).mapToObj(this::createDocumentEntryDTO).collect(Collectors.toList());
        documentService.insertDocumentEntries(documentEntryDTOS);

        List<String> ids = documentService.getAllDocuments().stream()
                .map(documentEntryDTO -> String.valueOf(documentEntryDTO.getId()))
                .limit(3)
                .collect(Collectors.toList());

        mockMvc.perform(MockMvcRequestBuilders.delete("/document/deleteMultipleEntries")
                        .param("ids", ids.toArray(new String[0])))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String lastResponseAsAString = mockMvc.perform(MockMvcRequestBuilders.get("/document/viewAll"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentWithEmployeeDTO> actualDocumentWithEmployeeDTOs = objectMapper.readValue(lastResponseAsAString, new TypeReference<>() {
        });

        List<DocumentWithEmployeeDTO> expectedDocumentWithEmployeeDTOs = List.of(DocumentWithEmployeeDTO.of(documentEntryDTOS.get(3), employeeDTO), DocumentWithEmployeeDTO.of(documentEntryDTOS.get(4), employeeDTO));

        assertThat(actualDocumentWithEmployeeDTOs)
                .hasSize(2)
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .ignoringFields("value.id", "id")
                .isEqualTo(expectedDocumentWithEmployeeDTOs);


    }
}