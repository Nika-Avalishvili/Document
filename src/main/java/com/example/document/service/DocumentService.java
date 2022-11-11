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

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentEntryMapper documentEntryMapper;
    private final DocumentRepository documentRepository;
    private final EmployeeClient employeeClient;

    public List<DocumentEntryDTO> uploadExcelDocument(MultipartFile file) throws Exception {
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
        return documentEntryMapper.entityToDto(docEntryList);
    }

    public List<DocumentEntryDTO> insertDocumentEntries(List<DocumentEntryDTO> documentEntryDTOS) {
        List<DocumentEntry> documentEntries = documentEntryMapper.dtoToEntity(documentEntryDTOS);
        documentRepository.saveAll(documentEntries);
        return documentEntryMapper.entityToDto(documentEntries);
    }

    public List<DocumentEntryDTO> getAllDocuments() {
        List<DocumentEntry> allDocumentEntries = documentRepository.findAll();
        return documentEntryMapper.entityToDto(allDocumentEntries);
    }

    public DocumentWithEmployeeDTO viewDocumentEntry(Long id) {
        DocumentEntry documentEntry = documentRepository.findById(id).orElse(null);
        DocumentEntryDTO documentEntryDTO = documentEntryMapper.entityToDto(documentEntry);
        EmployeeDTO employeeById = employeeClient.getEmployeeById(documentEntryDTO.getEmployeeId());
        employeeById.setEmployeeId(documentEntry.getEmployeeId());
        return DocumentWithEmployeeDTO.of(documentEntryDTO, employeeById);
    }

    public List<DocumentEntryDTO> viewMultipleDocumentEntries(LocalDate startDate, LocalDate endDate) {
        List<DocumentEntry> documentEntry = documentRepository.findByEffectiveDateBetween(startDate, endDate);
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

    public void deleteMultipleDocumentEntry(List<Long> id) {
        documentRepository.deleteAllByIdInBatch(id);
    }

}
