package org.citycare.citizenservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenDocumentResponse {

    private Long documentId;
    private String verificationStatus;
    private LocalDate uploadedDate;
}