package com.mharruengsang.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeResult {
    private boolean success;
    private String qrCodeBase64;
    private String qrCodeData;
    private String message;
    private String errorCode;
}
