package com.mharruengsang.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransferResult {
    private boolean success;
    private String bankReferenceNumber;
    private String bankAccountDetails;
    private String message;
}
