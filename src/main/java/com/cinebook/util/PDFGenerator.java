package com.cinebook.util;

import com.cinebook.model.Concession;
import com.cinebook.model.Reservation;
import com.cinebook.model.Seat;
import com.cinebook.model.SeatType;
import com.cinebook.model.Ticket;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for generating PDF documents.
 */
public class PDFGenerator {
    
    // Font definitions
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    
    /**
     * Generates a PDF ticket for a reservation.
     *
     * @param ticket The ticket to generate a PDF for
     * @return The PDF as a byte array
     */
    public byte[] generateTicketPDF(Ticket ticket) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Add cinema logo/header
            Paragraph header = new Paragraph("CineBook CDO", TITLE_FONT);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);
            
            Paragraph subheader = new Paragraph("Movie Ticket", SUBTITLE_FONT);
            subheader.setAlignment(Element.ALIGN_CENTER);
            document.add(subheader);
            
            document.add(Chunk.NEWLINE);
            
            // Add QR code
            try {
                byte[] qrCodeBytes = QRCodeGenerator.generateQRCodeBytes(ticket.getTicketCode(), 150, 150);
                Image qrCodeImage = Image.getInstance(qrCodeBytes);
                qrCodeImage.setAlignment(Element.ALIGN_CENTER);
                document.add(qrCodeImage);
            } catch (Exception e) {
                System.err.println("Error adding QR code to PDF: " + e.getMessage());
            }
            
            document.add(Chunk.NEWLINE);
            
            // Ticket details
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            
            addTableRow(table, "Ticket Code:", ticket.getTicketCode());
            addTableRow(table, "Movie:", ticket.getMovieTitle());
            addTableRow(table, "Cinema:", ticket.getCinemaName());
            addTableRow(table, "Date:", ticket.getFormattedScreeningDate());
            addTableRow(table, "Time:", ticket.getFormattedScreeningTime());
            addTableRow(table, "Seat:", ticket.getSeatNumber() + " (" + ticket.getSeatType() + ")");
            addTableRow(table, "Customer:", ticket.getCustomerName());
            
            document.add(table);
            
            document.add(Chunk.NEWLINE);
            
            // Footer
            Paragraph footer = new Paragraph("Please present this ticket at the entrance. Enjoy your movie!", SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
            return out.toByteArray();
            
        } catch (DocumentException e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generates a PDF receipt for a reservation.
     *
     * @param reservation The reservation to generate a receipt for
     * @param tickets The tickets associated with the reservation
     * @return The PDF as a byte array
     */
    public byte[] generateReceiptPDF(Reservation reservation, List<Ticket> tickets) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Add cinema logo/header
            Paragraph header = new Paragraph("CineBook CDO", TITLE_FONT);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);
            
            Paragraph subheader = new Paragraph("Booking Receipt", SUBTITLE_FONT);
            subheader.setAlignment(Element.ALIGN_CENTER);
            document.add(subheader);
            
            document.add(Chunk.NEWLINE);
            
            // Reservation details
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            
            addTableRow(infoTable, "Booking ID:", String.valueOf(reservation.getId()));
            addTableRow(infoTable, "Date:", reservation.getFormattedReservationTime());
            addTableRow(infoTable, "Customer:", reservation.getCustomerName());
            addTableRow(infoTable, "Movie:", reservation.getMovieTitle());
            addTableRow(infoTable, "Cinema:", reservation.getCinemaName());
            addTableRow(infoTable, "Screening:", reservation.getFormattedScreeningTime());
            
            // Format seats as comma-separated list
            StringBuilder seatsText = new StringBuilder();
            List<Seat> seats = reservation.getSelectedSeats();
            for (int i = 0; i < seats.size(); i++) {
                if (i > 0) {
                    seatsText.append(", ");
                }
                seatsText.append(seats.get(i).getSeatNumber());
            }
            addTableRow(infoTable, "Seats:", seatsText.toString());
            
            document.add(infoTable);
            
            document.add(Chunk.NEWLINE);
            
            // Ticket list
            Paragraph ticketsHeader = new Paragraph("Tickets", SUBTITLE_FONT);
            document.add(ticketsHeader);
            
            PdfPTable ticketsTable = new PdfPTable(3);
            ticketsTable.setWidthPercentage(100);
            
            // Table header
            PdfPCell cell1 = new PdfPCell(new Phrase("Ticket Code", BOLD_FONT));
            PdfPCell cell2 = new PdfPCell(new Phrase("Seat", BOLD_FONT));
            PdfPCell cell3 = new PdfPCell(new Phrase("Price", BOLD_FONT));
            
            ticketsTable.addCell(cell1);
            ticketsTable.addCell(cell2);
            ticketsTable.addCell(cell3);
            
            // Add tickets
            double subtotal = 0.0;
            for (Ticket ticket : tickets) {
                double price = 0.0;
                for (Seat seat : seats) {
                    if (seat.getSeatNumber().equals(ticket.getSeatNumber())) {
                        // Get price based on seat type
                        price = (seat.getSeatType() == SeatType.DELUXE) ? 150.0 : 100.0; // Placeholder price
                        break;
                    }
                }
                
                ticketsTable.addCell(new Phrase(ticket.getTicketCode(), NORMAL_FONT));
                ticketsTable.addCell(new Phrase(ticket.getSeatNumber() + " (" + ticket.getSeatType() + ")", NORMAL_FONT));
                ticketsTable.addCell(new Phrase(String.format("₱%.2f", price), NORMAL_FONT));
                
                subtotal += price;
            }
            
            document.add(ticketsTable);
            
            document.add(Chunk.NEWLINE);
            
            // Concessions
            List<Concession> concessions = reservation.getSelectedConcessions();
            if (concessions != null && !concessions.isEmpty()) {
                Paragraph concessionsHeader = new Paragraph("Concessions", SUBTITLE_FONT);
                document.add(concessionsHeader);
                
                PdfPTable concessionsTable = new PdfPTable(3);
                concessionsTable.setWidthPercentage(100);
                
                // Table header
                PdfPCell conCell1 = new PdfPCell(new Phrase("Item", BOLD_FONT));
                PdfPCell conCell2 = new PdfPCell(new Phrase("Quantity", BOLD_FONT));
                PdfPCell conCell3 = new PdfPCell(new Phrase("Price", BOLD_FONT));
                
                concessionsTable.addCell(conCell1);
                concessionsTable.addCell(conCell2);
                concessionsTable.addCell(conCell3);
                
                // Add concessions
                for (Concession concession : concessions) {
                    if (concession.getQuantity() > 0) {
                        concessionsTable.addCell(new Phrase(concession.getName(), NORMAL_FONT));
                        concessionsTable.addCell(new Phrase(String.valueOf(concession.getQuantity()), NORMAL_FONT));
                        concessionsTable.addCell(new Phrase(String.format("₱%.2f", concession.getTotalPrice()), NORMAL_FONT));
                        
                        subtotal += concession.getTotalPrice();
                    }
                }
                
                document.add(concessionsTable);
                
                document.add(Chunk.NEWLINE);
            }
            
            // Payment summary
            Paragraph paymentHeader = new Paragraph("Payment Summary", SUBTITLE_FONT);
            document.add(paymentHeader);
            
            PdfPTable paymentTable = new PdfPTable(2);
            paymentTable.setWidthPercentage(100);
            
            double tax = subtotal * 0.12; // 12% tax
            double total = subtotal + tax;
            
            addTableRow(paymentTable, "Subtotal:", String.format("₱%.2f", subtotal));
            addTableRow(paymentTable, "Tax (12%):", String.format("₱%.2f", tax));
            addTableRow(paymentTable, "Total:", String.format("₱%.2f", total));
            addTableRow(paymentTable, "Payment Method:", reservation.getPaymentMethod());
            addTableRow(paymentTable, "Payment Status:", reservation.isPaid() ? "Paid" : "Pending");
            
            document.add(paymentTable);
            
            document.add(Chunk.NEWLINE);
            
            // Footer
            Paragraph footer = new Paragraph("Thank you for booking with CineBook CDO!", SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
            return out.toByteArray();
            
        } catch (DocumentException e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Helper method to add a row to a PDF table.
     *
     * @param table The table to add a row to
     * @param label The label for the row
     * @param value The value for the row
     */
    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    /**
     * Saves a PDF to a file.
     *
     * @param pdfBytes The PDF as a byte array
     * @param filePath The path to save the PDF to
     * @return true if the PDF was successfully saved, false otherwise
     */
    public boolean savePDFToFile(byte[] pdfBytes, String filePath) {
        try {
            File file = new File(filePath);
            
            // Create directory if it doesn't exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(pdfBytes);
            fos.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving PDF to file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generates a PDF with all tickets for a reservation.
     *
     * @param reservation The reservation
     * @param tickets The tickets for the reservation
     * @return The generated PDF file
     */
    public File generateTicketsPDF(Reservation reservation, List<Ticket> tickets) {
        try {
            // Create temp directory if it doesn't exist
            File tempDir = new File("temp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            // Generate a unique filename
            String fileName = "tickets_" + reservation.getId() + "_" + System.currentTimeMillis() + ".pdf";
            String filePath = "temp/" + fileName;
            
            // Generate PDF bytes
            byte[] pdfBytes = generateReceiptPDF(reservation, tickets);
            
            // Save to file
            savePDFToFile(pdfBytes, filePath);
            
            return new File(filePath);
        } catch (Exception e) {
            System.err.println("Error generating tickets PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Opens a PDF file for printing.
     *
     * @param pdfFile The PDF file to print
     * @return true if the file was opened successfully, false otherwise
     */
    public boolean openPDFForPrinting(File pdfFile) {
        try {
            // In a real implementation, this would use the system's default PDF viewer
            // For now, just log the action
            System.out.println("Opening PDF for printing: " + pdfFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            System.err.println("Error opening PDF for printing: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}