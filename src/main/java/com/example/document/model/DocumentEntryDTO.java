package com.example.document.model;

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
public class DocumentEntryDTO {
    private Long id;

    private LocalDate uploadDate;
    private LocalDate effectiveDate;

    private Long employeeId;
    private Long benefitId;

    private BigDecimal amount;
}

