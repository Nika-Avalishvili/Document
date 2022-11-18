package com.example.document.model;

import com.example.document.client.BenefitDTO;
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
public class DocumentWithEmployeeDTOAndBenefitDTO {
    private Long id;
    private LocalDate uploadDate;
    private LocalDate effectiveDate;
    private EmployeeDTO employeeDTO;
    private BenefitDTO benefitDTO;
    private BigDecimal amount;

    public static DocumentWithEmployeeDTOAndBenefitDTO of(DocumentEntryDTO documentEntryDTO, EmployeeDTO employeeDTO, BenefitDTO benefitDTO) {
        return DocumentWithEmployeeDTOAndBenefitDTO.builder()
                .id(documentEntryDTO.getId())
                .uploadDate(documentEntryDTO.getUploadDate())
                .effectiveDate(documentEntryDTO.getEffectiveDate())
                .employeeDTO(employeeDTO)
                .benefitDTO(benefitDTO)
                .amount(documentEntryDTO.getAmount())
                .build();
    }
}
