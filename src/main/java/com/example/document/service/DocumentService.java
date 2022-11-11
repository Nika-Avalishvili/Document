package com.example.document.service;

import com.example.document.client.EmployeeClient;
import com.example.document.client.EmployeeDTO;
import com.example.document.model.DocumentEntry;
import com.example.document.model.DocumentEntryDTO;
import com.example.document.model.DocumentEntryMapper;
import com.example.document.model.DocumentWithEmployeeDTO;
import com.example.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentEntryMapper documentEntryMapper;
    private final DocumentRepository documentRepository;
    private final EmployeeClient employeeClient;

    public List<DocumentWithEmployeeDTO> uploadExcelDocument(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
        Sheet sheet = xssfWorkbook.getSheetAt(0);

        List<DocumentEntry> docEntryList = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            LocalDate uploadDate = row.getCell(0).getLocalDateTimeCellValue().toLocalDate();
            LocalDate effectiveDate = row.getCell(1).getLocalDateTimeCellValue().toLocalDate();
            Long employeeId = (long) row.getCell(2).getNumericCellValue();
            Long benefitId = (long) row.getCell(3).getNumericCellValue();
            BigDecimal amount = BigDecimal.valueOf(row.getCell(4).getNumericCellValue());

            DocumentEntry newDocumentEntry = DocumentEntry.builder()
                    .uploadDate(uploadDate)
                    .effectiveDate(effectiveDate)
                    .employeeId(employeeId)
                    .benefitId(benefitId)
                    .amount(amount)
                    .build();
            docEntryList.add(newDocumentEntry);
        }
        documentRepository.saveAll(docEntryList);
        List<DocumentEntryDTO> documentEntryDTOS = documentEntryMapper.entityToDto(docEntryList);

        return documentEntryDTOS.stream()
                .map(documentEntryDTO -> DocumentWithEmployeeDTO.of(documentEntryDTO, employeeClient.getEmployeeById(documentEntryDTO.getEmployeeId())))
                .collect(Collectors.toList());
    }

    public List<DocumentWithEmployeeDTO> insertDocumentEntries(List<DocumentEntryDTO> documentEntryDTOS) {
        List<DocumentEntry> savedDocumentEntries = documentRepository.saveAll(documentEntryMapper.dtoToEntity(documentEntryDTOS));
        List<DocumentEntryDTO> savedDocumentEntryDTOs = documentEntryMapper.entityToDto(savedDocumentEntries);

        return savedDocumentEntryDTOs.stream()
                .map(documentEntryDTO -> DocumentWithEmployeeDTO.of(documentEntryDTO, employeeClient.getEmployeeById(documentEntryDTO.getEmployeeId())))
                .collect(Collectors.toList());
    }

    public List<DocumentWithEmployeeDTO> getAllDocuments() {
        List<DocumentEntryDTO> allDocumentEntryDTOs = documentEntryMapper.entityToDto(documentRepository.findAll());

        return allDocumentEntryDTOs.stream()
                .map(documentEntryDTO -> DocumentWithEmployeeDTO.of(documentEntryDTO,employeeClient.getEmployeeById(documentEntryDTO.getEmployeeId()))).collect(Collectors.toList());
    }

    public DocumentWithEmployeeDTO viewDocumentEntry(Long id) {
        DocumentEntry documentEntry = documentRepository.findById(id).orElse(null);
        DocumentEntryDTO documentEntryDTO = documentEntryMapper.entityToDto(documentEntry);
        EmployeeDTO employeeById = employeeClient.getEmployeeById(documentEntryDTO.getEmployeeId());
        return DocumentWithEmployeeDTO.of(documentEntryDTO, employeeById);
    }

    public List<DocumentWithEmployeeDTO> viewMultipleDocumentEntries(LocalDate startDate, LocalDate endDate) {
        List<DocumentEntry> documentEntry = documentRepository.findByEffectiveDateBetween(startDate, endDate);
        List<DocumentEntryDTO> documentEntryDTOS = documentEntryMapper.entityToDto(documentEntry);

        return documentEntryDTOS.stream()
                .map(documentEntryDTO -> DocumentWithEmployeeDTO.of(documentEntryDTO,employeeClient.getEmployeeById(documentEntryDTO.getEmployeeId())))
                .collect(Collectors.toList());
    }

    public DocumentWithEmployeeDTO editDocumentEntry(DocumentEntryDTO documentEntryDTO) {
        DocumentEntry documentEntry = documentEntryMapper.dtoToEntity(documentEntryDTO);
        DocumentEntry savedDocumentEntry = documentRepository.save(documentEntry);
        DocumentEntryDTO savedDocumentEntryDTO = documentEntryMapper.entityToDto(savedDocumentEntry);
        EmployeeDTO employeeById = employeeClient.getEmployeeById(savedDocumentEntryDTO.getEmployeeId());
        return DocumentWithEmployeeDTO.of(savedDocumentEntryDTO, employeeById);
    }

    public void deleteDocumentEntry(Long id) {
        documentRepository.deleteById(id);
    }

    public void deleteMultipleDocumentEntry(List<Long> id) {
        documentRepository.deleteAllByIdInBatch(id);
    }

}
