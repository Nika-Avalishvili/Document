package com.example.document.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "documents")
public class DocumentEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate uploadDate;

    private LocalDate effectiveDate;
    private Long employeeId;
    private Long benefitId;
    private Integer amount;
}
