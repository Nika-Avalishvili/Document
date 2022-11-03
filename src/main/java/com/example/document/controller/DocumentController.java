package com.example.document.controller;

import com.example.document.model.DocumentEntryDTO;
import com.example.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/document")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public List<DocumentEntryDTO> uploadExcelDocument(@RequestParam("file") MultipartFile file) throws Exception {
        return documentService.uploadExcelDocument(file);
    }

    @PostMapping("/insert")
    public List<DocumentEntryDTO> InsertDocumentEntries(@RequestBody List<DocumentEntryDTO> documentEntryDTOS) {
        return documentService.insertDocumentEntries(documentEntryDTOS);
    }

    @GetMapping("/viewAll")
    public List<DocumentEntryDTO> viewAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/viewById/{id}")
    public DocumentEntryDTO viewDocumentEntry(@PathVariable Long id) {
        return documentService.viewDocumentEntry(id);
    }

    @PutMapping("/updateEntry")
    public DocumentEntryDTO editDocumentEntry(@RequestBody DocumentEntryDTO documentEntryDTO) {
        return documentService.editDocumentEntry(documentEntryDTO);
    }

    @DeleteMapping("/deleteEntry")
    public void deleteDocumentEntry(@RequestParam(value = "id") Long id) {
        documentService.deleteDocumentEntry(id);
    }
}
