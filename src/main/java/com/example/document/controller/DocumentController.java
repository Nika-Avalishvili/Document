package com.example.document.controller;

import com.example.document.model.DocumentEntryDTO;
import com.example.document.model.DocumentWithEmployeeDTOAndBenefitDTO;
import com.example.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/document")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public List<DocumentWithEmployeeDTOAndBenefitDTO> uploadExcelDocument(@RequestParam MultipartFile file) throws Exception {
        return documentService.uploadExcelDocument(file);
    }

    @PostMapping("/insert")
    public List<DocumentWithEmployeeDTOAndBenefitDTO> InsertDocumentEntries(@RequestBody List<DocumentEntryDTO> documentEntryDTOS) {
        return documentService.insertDocumentEntries(documentEntryDTOS);
    }

    @GetMapping("/viewAll")
    public List<DocumentWithEmployeeDTOAndBenefitDTO> viewAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/viewById/{id}")
    public DocumentWithEmployeeDTOAndBenefitDTO viewDocumentEntry(@PathVariable Long id) {
        return documentService.viewDocumentEntry(id);
    }

    @GetMapping("/viewMultipleByDates/{startDate}/{endDate}")
    public List<DocumentWithEmployeeDTOAndBenefitDTO> viewMultipleDocumentEntries(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return documentService.viewMultipleDocumentEntries(startDate, endDate);
    }

    @PutMapping("/updateEntry")
    public DocumentWithEmployeeDTOAndBenefitDTO editDocumentEntry(@RequestBody DocumentEntryDTO documentEntryDTO) {
        return documentService.editDocumentEntry(documentEntryDTO);
    }

    @DeleteMapping("/deleteEntry")
    public void deleteDocumentEntry(@RequestParam Long id) {
        documentService.deleteDocumentEntry(id);
    }

    @DeleteMapping("/deleteMultipleEntries")
    public void deleteMultipleDocumentEntries(@RequestParam List<Long> ids) {
        documentService.deleteMultipleDocumentEntry(ids);
    }
}
