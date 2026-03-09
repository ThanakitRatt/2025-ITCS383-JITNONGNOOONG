package com.mharruengsang.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.mharruengsang.service.QRCodeService;
import com.mharruengsang.service.dto.QRCodeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;

@Service
@Slf4j
public class PromptPayQRCodeService implements QRCodeService {
    
    private static final int QR_WIDTH = 400;
    private static final int QR_HEIGHT = 400;
    
    @Override
    public QRCodeResult generatePromptPayQR(String phoneNumber, BigDecimal amount, String orderId) {
        try {
            log.info("Generating PromptPay QR for phone: {}, amount: {}, order: {}", phoneNumber, amount, orderId);
            
            // PromptPay format: 00020126000000000000000000
            // This is a simplified format - actual PromptPay uses EMV QRCPS standard
            String promptPayData = buildPromptPayQRData(phoneNumber, amount);
            
            // Generate QR code
            String qrCodeBase64 = generateQRCode(promptPayData);
            
            return new QRCodeResult(
                    true,
                    qrCodeBase64,
                    promptPayData,
                    "PromptPay QR code generated successfully",
                    null
            );
        } catch (Exception e) {
            log.error("Failed to generate PromptPay QR code", e);
            return new QRCodeResult(
                    false,
                    null,
                    null,
                    "Failed to generate QR code",
                    e.getMessage()
            );
        }
    }
    
    @Override
    public QRCodeResult generateStaticPromptPayQR(String phoneNumber) {
        try {
            // Static QR (no specific amount)
            String promptPayData = buildStaticPromptPayQRData(phoneNumber);
            String qrCodeBase64 = generateQRCode(promptPayData);
            
            return new QRCodeResult(
                    true,
                    qrCodeBase64,
                    promptPayData,
                    "Static PromptPay QR code generated successfully",
                    null
            );
        } catch (Exception e) {
            log.error("Failed to generate static PromptPay QR code", e);
            return new QRCodeResult(
                    false,
                    null,
                    null,
                    "Failed to generate QR code",
                    e.getMessage()
            );
        }
    }
    
    /**
     * Build PromptPay QR data with dynamic amount
     * Format follows EMV QRCPS standard
     */
    private String buildPromptPayQRData(String phoneNumber, BigDecimal amount) {
        // Simplified PromptPay EMV QR format
        StringBuilder qrData = new StringBuilder();
        
        // EMV header
        qrData.append("00020126");
        qrData.append("0012");
        
        // Merchant category code (for PromptPay)
        qrData.append("5802TH");
        
        // Terminal ID (PromptPay identifier)
        qrData.append("0015com.promptpay");
        
        // Phone number or tax ID
        qrData.append("0213").append(String.format("%02d", phoneNumber.length())).append(phoneNumber);
        
        // Amount (optional, for dynamic QR)
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            String amountStr = amount.toPlainString();
            qrData.append("540").append(String.format("%02d", amountStr.length())).append(amountStr);
        }
        
        // Currency (THB)
        qrData.append("5303764");
        
        // Checksum (simplified - in production, calculate proper CRC)
        qrData.append("6304");
        
        return qrData.toString();
    }
    
    /**
     * Build static PromptPay QR (no amount)
     */
    private String buildStaticPromptPayQRData(String phoneNumber) {
        return buildPromptPayQRData(phoneNumber, null);
    }
    
    /**
     * Generate QR code image from data string
     */
    private String generateQRCode(String data) throws WriterException, IOException {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
        
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
