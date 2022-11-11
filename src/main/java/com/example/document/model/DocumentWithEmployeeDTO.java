package com.example.document.model;

import com.example.document.client.EmployeeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentWithEmployeeDTO {
    private Long id;
    private LocalDate uploadDate;
    private LocalDate effectiveDate;
    private EmployeeDTO employeeDTO;
    private Long benefitId;
    private BigDecimal amount;

    public static DocumentWithEmployeeDTO of(DocumentEntryDTO documentEntryDTO, EmployeeDTO employeeDTO) {
        return DocumentWithEmployeeDTO.builder()
                .id(documentEntryDTO.getId())
                .uploadDate(documentEntryDTO.getUploadDate())
                .effectiveDate(documentEntryDTO.getEffectiveDate())
                .employeeDTO(employeeDTO)
                .benefitId(documentEntryDTO.getBenefitId())
                .amount(documentEntryDTO.getAmount())
                .build();
    }
}
