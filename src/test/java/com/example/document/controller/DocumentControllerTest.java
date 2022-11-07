package com.example.document.controller;

import com.example.document.model.DocumentEntryDTO;
import com.example.document.repository.DocumentRepository;
import com.example.document.service.DocumentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.readAllBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllerTest {

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

        List<DocumentEntryDTO> docEntryList = documentService.uploadExcelDocument(file);

        String responseAsAString = mockMvc.perform(multipart("/document/upload")
                        .file("file", file.getBytes()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentEntryDTO> actualDocumentEntryDTOS = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

        assertThat(actualDocumentEntryDTOS.stream().findFirst())
                .usingRecursiveComparison()
                .ignoringFields("value.id")
                .isEqualTo(docEntryList.stream().findFirst());

        Assertions.assertEquals(4, docEntryList.size());
    }

    @Test
    void insertDocumentEntries() throws Exception {
        DocumentEntryDTO documentEntryDTO = DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022, 9, 30))
                .employeeId(1L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(300))
                .build();

        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        documentEntryDTOS.add(documentEntryDTO);

        documentService.insertDocumentEntries(documentEntryDTOS);

        String requestJson = objectMapper.writeValueAsString(documentEntryDTOS);

        String responseAsAString = mockMvc.perform(MockMvcRequestBuilders.post("/document/insert")
                        .contentType(APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentEntryDTO> actualDocumentEntryDTOS = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

        assertThat(actualDocumentEntryDTOS.stream().findFirst())
                .usingRecursiveComparison()
                .ignoringFields("value.id")
                .isEqualTo(documentEntryDTOS.stream().findFirst());
    }

    @Test
    void viewAllDocuments() throws Exception {
        DocumentEntryDTO documentEntryDTO1 = DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022, 9, 30))
                .employeeId(1L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(300))
                .build();
        DocumentEntryDTO documentEntryDTO2 = DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022, 9, 30))
                .employeeId(2L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(300))
                .build();

        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        documentEntryDTOS.add(documentEntryDTO1);
        documentEntryDTOS.add(documentEntryDTO2);
        documentService.insertDocumentEntries(documentEntryDTOS);

        String responseAsAString = mockMvc.perform(MockMvcRequestBuilders.get("/document/viewAll"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentEntryDTO> actualDocumentEntryDtoList = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

        Assertions.assertEquals(2, documentEntryDTOS.size());
        Assertions.assertEquals(2, actualDocumentEntryDtoList.size());

        DocumentEntryDTO expectedDocumentEntryDTO = documentEntryDTOS.stream().findFirst().orElseThrow();
        DocumentEntryDTO actualDocumentEntryDTO = actualDocumentEntryDtoList.stream().findFirst().orElseThrow();

        assertThat(expectedDocumentEntryDTO)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(actualDocumentEntryDTO);
    }

    @Test
    void viewDocumentEntry() throws Exception {
        DocumentEntryDTO documentEntryDTO = DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022, 9, 30))
                .employeeId(1L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(300))
                .build();
        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        documentEntryDTOS.add(documentEntryDTO);
        Long id = documentService.insertDocumentEntries(documentEntryDTOS).stream().findFirst().get().getId();

        String responseAsAString = mockMvc.perform(MockMvcRequestBuilders.get("/document/viewById/{id}", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        DocumentEntryDTO actualDocumentEntryDTO = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

        assertThat(actualDocumentEntryDTO)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(documentEntryDTO);
    }

    @Test
    void viewMultipleDocumentEntries() throws Exception {
        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        for (long i = 0; i < 5; i++) {
            DocumentEntryDTO documentEntryDTO = DocumentEntryDTO.builder()
                    .uploadDate(LocalDate.of(2022, 10, 13))
                    .effectiveDate(LocalDate.of(2022 + (int) i, 9, 30))
                    .employeeId(1L)
                    .benefitId(2L)
                    .amount(BigDecimal.valueOf(300))
                    .build();
            documentEntryDTOS.add(documentEntryDTO);
        }

        documentService.insertDocumentEntries(documentEntryDTOS);

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        String requestJson = objectMapper.writeValueAsString(documentEntryDTOS);

        String responseAsAString = mockMvc.perform(MockMvcRequestBuilders.get("/document/viewMultipleByDates/{startDate}/{endDate}", startDate, endDate)
                        .contentType(APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentEntryDTO> actualDocumentEntryDTO = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

        Assertions.assertEquals(3, actualDocumentEntryDTO.size());
        Assertions.assertEquals(actualDocumentEntryDTO.get(0).getEffectiveDate().getYear(), 2022);
        Assertions.assertEquals(actualDocumentEntryDTO.get(1).getEffectiveDate().getYear(), 2023);
        Assertions.assertEquals(actualDocumentEntryDTO.get(2).getEffectiveDate().getYear(), 2024);
    }


    @Test
    void editDocumentEntry() throws Exception {
        DocumentEntryDTO documentEntryDTO1 = DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022, 9, 30))
                .employeeId(1L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(300))
                .build();
        DocumentEntryDTO documentEntryDTO2 = DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022, 9, 30))
                .employeeId(2L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(300))
                .build();

        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        documentEntryDTOS.add(documentEntryDTO1);
        documentService.insertDocumentEntries(documentEntryDTOS);

        String requestJson = objectMapper.writeValueAsString(documentEntryDTO2);

        String responseAsAString = mockMvc.perform(MockMvcRequestBuilders.put("/document/updateEntry")
                        .contentType(APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        DocumentEntryDTO actualDocumentEntryDTO = objectMapper.readValue(responseAsAString, new TypeReference<>() {
        });

        assertThat(documentEntryDTO2)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(actualDocumentEntryDTO);
    }

    @Test
    void deleteEditDocumentEntry() throws Exception {
        DocumentEntryDTO documentEntryDTO1 = DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022, 9, 30))
                .employeeId(1L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(300))
                .build();
        DocumentEntryDTO documentEntryDTO2 = DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022, 9, 30))
                .employeeId(2L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(300))
                .build();

        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        documentEntryDTOS.add(documentEntryDTO1);
        documentEntryDTOS.add(documentEntryDTO2);

        Long firstId = documentService.insertDocumentEntries(documentEntryDTOS).stream().findFirst().get().getId();

        String firstResponseAsAString = mockMvc.perform(MockMvcRequestBuilders.delete("/document/deleteEntry?id={id}", firstId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String lastResponseAsAString = mockMvc.perform(MockMvcRequestBuilders.get("/document/viewAll"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentEntryDTO> actualDocumentEntryDTOList = objectMapper.readValue(lastResponseAsAString, new TypeReference<>() {
        });

        assertThat(actualDocumentEntryDTOList)
                .hasSize(1)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .doesNotContain(documentEntryDTO1);

        DocumentEntryDTO actualDocumentEntryDTO = actualDocumentEntryDTOList.stream().findFirst().orElseThrow();

        assertThat(actualDocumentEntryDTO).usingRecursiveComparison().ignoringFields("id").isEqualTo(documentEntryDTO2);
    }


    @Test
    void deleteMultipleDocumentEntries() throws Exception {
        DocumentEntryDTO documentEntryDTO1 = DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022, 9, 30))
                .employeeId(1L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(300))
                .build();
        DocumentEntryDTO documentEntryDTO2 = DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022, 9, 30))
                .employeeId(2L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(400))
                .build();
        DocumentEntryDTO documentEntryDTO3 = DocumentEntryDTO.builder()
                .uploadDate(LocalDate.of(2022, 10, 13))
                .effectiveDate(LocalDate.of(2022, 9, 30))
                .employeeId(2L)
                .benefitId(2L)
                .amount(BigDecimal.valueOf(500))
                .build();

        List<DocumentEntryDTO> documentEntryDTOS = new ArrayList<>();
        documentEntryDTOS.add(documentEntryDTO1);
        documentEntryDTOS.add(documentEntryDTO2);
        documentEntryDTOS.add(documentEntryDTO3);

        Long firstId = documentService.insertDocumentEntries(documentEntryDTOS).stream().findFirst().get().getId();
        Long secondId = firstId + 1L;

        String idFirst = "" + firstId;
        String idSecond = "" + secondId;

        String firstResponseAsAString = mockMvc.perform(MockMvcRequestBuilders.delete("/document/deleteMultipleEntries")
                        .param("ids", idFirst, idSecond))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String lastResponseAsAString = mockMvc.perform(MockMvcRequestBuilders.get("/document/viewAll"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<DocumentEntryDTO> actualDocumentEntryDTOList = objectMapper.readValue(lastResponseAsAString, new TypeReference<>() {
        });

        assertThat(actualDocumentEntryDTOList)
                .hasSize(1)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .doesNotContain(documentEntryDTO1);

        DocumentEntryDTO actualDocumentEntryDTO = actualDocumentEntryDTOList.stream().findFirst().orElseThrow();

        assertThat(actualDocumentEntryDTO).usingRecursiveComparison().ignoringFields("id").isEqualTo(documentEntryDTO3);
    }
}