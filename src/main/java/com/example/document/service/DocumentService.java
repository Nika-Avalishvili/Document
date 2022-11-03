package com.example.document.service;

import com.example.document.model.DocumentEntry;
import com.example.document.model.DocumentEntryDTO;
import com.example.document.model.DocumentEntryMapper;
import com.example.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    public final DocumentEntryMapper documentEntryMapper;
    public final DocumentRepository documentRepository;

    public List<DocumentEntryDTO> uploadExcelDocument(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
        Sheet sheet = xssfWorkbook.getSheetAt(0);

        List <DocumentEntry> docEntryList = new ArrayList<>();

        for (int i = 1; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            LocalDate uploadDate = row.getCell(0).getLocalDateTimeCellValue().toLocalDate();
            LocalDate effectiveDate = row.getCell(1).getLocalDateTimeCellValue().toLocalDate();
            Long employeeId = (long) row.getCell(2).getNumericCellValue();
            Long benefitId = (long) row.getCell(3).getNumericCellValue();
            Integer amount = (int) row.getCell(4).getNumericCellValue();

            DocumentEntry newDocumentEntry = DocumentEntry.builder()
                    .uploadDate(uploadDate)
                    .effectiveDate(effectiveDate)
                    .employeeId(employeeId)
                    .benefitId(benefitId)
                    .amount(amount)
                    .build();
            docEntryList.add(newDocumentEntry);
            documentRepository.save(newDocumentEntry);
        }
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
