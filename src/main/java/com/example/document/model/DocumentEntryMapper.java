package com.example.document.model;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DocumentEntryMapper {

    public DocumentEntryDTO entityToDto(DocumentEntry document) {
        return DocumentEntryDTO.builder()
                .id(document.getId())
                .uploadDate(document.getUploadDate())
                .effectiveDate(document.getEffectiveDate())
                .employeeId(document.getEmployeeId())
                .benefitId(document.getBenefitId())
                .amount(document.getAmount())
                .build();
    }

    public List<DocumentEntryDTO> entityToDto(List<DocumentEntry> documents) {
        return documents.stream().map(this::entityToDto).collect(Collectors.toList());
    }

    public DocumentEntry dtoToEntity(DocumentEntryDTO documentEntryDTO) {
        return DocumentEntry.builder()
                .id(documentEntryDTO.getId())
                .uploadDate(documentEntryDTO.getUploadDate())
                .effectiveDate(documentEntryDTO.getEffectiveDate())
                .employeeId(documentEntryDTO.getEmployeeId())
                .benefitId(documentEntryDTO.getBenefitId())
                .amount(documentEntryDTO.getAmount())
                .build();
    }

    public List<DocumentEntry> dtoToEntity(List<DocumentEntryDTO> documentsDTOs) {
        return documentsDTOs.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }
}
