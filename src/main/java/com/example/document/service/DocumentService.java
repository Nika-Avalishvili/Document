package com.example.document.service;

import com.example.document.client.*;
import com.example.document.model.DocumentEntry;
import com.example.document.model.DocumentEntryDTO;
import com.example.document.model.DocumentEntryMapper;
import com.example.document.model.DocumentWithEmployeeDTOAndBenefitDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentEntryMapper documentEntryMapper;
    private final DocumentRepository documentRepository;
    private final EmployeeClient employeeClient;
    private final BenefitClient benefitClient;

    public List<DocumentWithEmployeeDTOAndBenefitDTO> uploadExcelDocument(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
        Sheet sheet = xssfWorkbook.getSheetAt(0);

        List<DocumentEntry> docEntryList = new ArrayList<>();
        Map<Long, EmployeeDTO> mapOfEmployees = new HashMap<>();
        Map<Long, BenefitDTO> mapOfBenefits = new HashMap<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            LocalDate uploadDate = row.getCell(0).getLocalDateTimeCellValue().toLocalDate();
            LocalDate effectiveDate = row.getCell(1).getLocalDateTimeCellValue().toLocalDate();
            Long employeeId = (long) row.getCell(2).getNumericCellValue();
            mapOfEmployees.put(employeeId, employeeClient.getEmployeeById(employeeId));

            Long benefitId = (long) row.getCell(3).getNumericCellValue();
            mapOfBenefits.put(benefitId, benefitClient.getBenefitDtoById(benefitId));

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
                .map(documentEntryDTO -> DocumentWithEmployeeDTOAndBenefitDTO.of(documentEntryDTO, mapOfEmployees.get(documentEntryDTO.getEmployeeId()), mapOfBenefits.get(documentEntryDTO.getBenefitId())))
                .collect(Collectors.toList());
    }

    public List<DocumentWithEmployeeDTOAndBenefitDTO> insertDocumentEntries(List<DocumentEntryDTO> documentEntryDTOS) {

        Map<Long, EmployeeDTO> mapOfEmployees = new HashMap<>();
        Map<Long, BenefitDTO> mapOfBenefits = new HashMap<>();

        //Employee validation
        for (int i = 0; i < documentEntryDTOS.size(); i++) {
            Long employeeId = documentEntryDTOS.get(i).getEmployeeId();
            Long benefitId = documentEntryDTOS.get(i).getBenefitId();
            mapOfEmployees.put(employeeId, employeeClient.getEmployeeById(employeeId));
            mapOfBenefits.put(documentEntryDTOS.get(i).getBenefitId(), benefitClient.getBenefitDtoById(benefitId));
        }

        List<DocumentEntry> savedDocumentEntries = documentRepository.saveAll(documentEntryMapper.dtoToEntity(documentEntryDTOS));
        List<DocumentEntryDTO> savedDocumentEntryDTOs = documentEntryMapper.entityToDto(savedDocumentEntries);

        return savedDocumentEntryDTOs.stream()
                .map(documentEntryDTO -> DocumentWithEmployeeDTOAndBenefitDTO.of(documentEntryDTO, mapOfEmployees.get(documentEntryDTO.getEmployeeId()), mapOfBenefits.get(documentEntryDTO.getBenefitId())))
                .collect(Collectors.toList());
    }

    public List<DocumentWithEmployeeDTOAndBenefitDTO> getAllDocuments() {
        List<DocumentEntryDTO> allDocumentEntryDTOs = documentEntryMapper.entityToDto(documentRepository.findAll());
        return allDocumentEntryDTOs.stream()
                .map(documentEntryDTO -> DocumentWithEmployeeDTOAndBenefitDTO.of(documentEntryDTO, employeeClient.getEmployeeById(documentEntryDTO.getEmployeeId()), benefitClient.getBenefitDtoById(documentEntryDTO.getBenefitId()))).collect(Collectors.toList());
    }

    public DocumentWithEmployeeDTOAndBenefitDTO viewDocumentEntry(Long id) {
        DocumentEntry documentEntry = documentRepository.findById(id).orElse(null);
        DocumentEntryDTO documentEntryDTO = documentEntryMapper.entityToDto(documentEntry);
        EmployeeDTO employeeById = employeeClient.getEmployeeById(documentEntryDTO.getEmployeeId());
        BenefitDTO benefitById = benefitClient.getBenefitDtoById(documentEntryDTO.getBenefitId());
        return DocumentWithEmployeeDTOAndBenefitDTO.of(documentEntryDTO, employeeById, benefitById);
    }

    public List<DocumentWithEmployeeDTOAndBenefitDTO> viewMultipleDocumentEntries(LocalDate startDate, LocalDate endDate) {
        List<DocumentEntry> documentEntry = documentRepository.findByEffectiveDateBetween(startDate, endDate);
        List<DocumentEntryDTO> documentEntryDTOS = documentEntryMapper.entityToDto(documentEntry);

        return documentEntryDTOS.stream()
                .map(documentEntryDTO -> DocumentWithEmployeeDTOAndBenefitDTO.of(documentEntryDTO, employeeClient.getEmployeeById(documentEntryDTO.getEmployeeId()), benefitClient.getBenefitDtoById(documentEntryDTO.getBenefitId())))
                .collect(Collectors.toList());
    }

    public DocumentWithEmployeeDTOAndBenefitDTO editDocumentEntry(DocumentEntryDTO documentEntryDTO) {
        DocumentEntry documentEntry = documentEntryMapper.dtoToEntity(documentEntryDTO);
        DocumentEntry savedDocumentEntry = documentRepository.save(documentEntry);
        DocumentEntryDTO savedDocumentEntryDTO = documentEntryMapper.entityToDto(savedDocumentEntry);
        EmployeeDTO employeeById = employeeClient.getEmployeeById(savedDocumentEntryDTO.getEmployeeId());
        BenefitDTO benefitById = benefitClient.getBenefitDtoById(savedDocumentEntryDTO.getBenefitId());
        return DocumentWithEmployeeDTOAndBenefitDTO.of(savedDocumentEntryDTO, employeeById, benefitById);
    }

    public void deleteDocumentEntry(Long id) {
        documentRepository.deleteById(id);
    }

    public void deleteMultipleDocumentEntry(List<Long> id) {
        documentRepository.deleteAllByIdInBatch(id);
    }

}
