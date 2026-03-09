package com.mharruengsang.service;

import com.mharruengsang.service.dto.QRCodeResult;
import java.util.Base64;

public interface QRCodeService {
    
    /**
     * Generate PromptPay QR code for the given phone number and amount
     */
    QRCodeResult generatePromptPayQR(String phoneNumber, java.math.BigDecimal amount, String orderId);
    
    /**
     * Generate static PromptPay QR code (no specific amount)
     */
    QRCodeResult generateStaticPromptPayQR(String phoneNumber);
}
