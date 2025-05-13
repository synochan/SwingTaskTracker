package com.cinebook.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Utility class for generating QR codes.
 */
public class QRCodeGenerator {
    
    /**
     * Generates a QR code image from the given data.
     *
     * @param data The data to encode in the QR code
     * @param width The width of the QR code
     * @param height The height of the QR code
     * @return The generated Image object
     */
    public static Image generateQRCodeImage(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            return bufferedImage;
        } catch (WriterException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Saves a QR code image to a file.
     *
     * @param data The data to encode in the QR code
     * @param filePath The path where the QR code image will be saved
     * @param width The width of the QR code
     * @param height The height of the QR code
     * @return true if the QR code was successfully saved, false otherwise
     */
    public static boolean saveQRCodeImage(String data, String filePath, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            
            return true;
        } catch (WriterException | IOException e) {
            System.err.println("Error saving QR code: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generates a QR code as a byte array.
     *
     * @param data The data to encode in the QR code
     * @param width The width of the QR code
     * @param height The height of the QR code
     * @return The QR code as a byte array
     */
    public static byte[] generateQRCodeBytes(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR code bytes: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generates a temporary QR code file and returns its path.
     *
     * @param data The data to encode in the QR code
     * @param width The width of the QR code
     * @param height The height of the QR code
     * @return The path to the temporary QR code file
     */
    public static String generateTemporaryQRCodeFile(String data, int width, int height) {
        try {
            // Create temporary directory if it doesn't exist
            File tempDir = new File("temp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            // Generate a unique filename
            String fileName = "qrcode_" + System.currentTimeMillis() + ".png";
            String filePath = "temp/" + fileName;
            
            // Save QR code to file
            saveQRCodeImage(data, filePath, width, height);
            
            return filePath;
        } catch (Exception e) {
            System.err.println("Error generating temporary QR code file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}