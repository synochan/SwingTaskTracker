package com.cinebook.util;

import com.cinebook.model.Reservation;
import com.cinebook.model.Screening;
import com.cinebook.model.Seat;
import com.cinebook.model.Ticket;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Generates stylish, printable tickets for movie reservations.
 */
public class TicketGenerator implements Printable {
    private Reservation reservation;
    private Screening screening;
    private List<Seat> seats;
    private String qrCodeData;
    private Image qrCodeImage;
    private double totalPrice;
    
    // Ticket design constants
    private static final int MARGIN = 30;
    private static final int TICKET_WIDTH = 500;
    private static final int TICKET_HEIGHT = 250;
    private static final Color TICKET_BACKGROUND = new Color(42, 42, 64);
    private static final Color TICKET_BORDER = new Color(230, 57, 70);
    private static final Color TEXT_COLOR = new Color(241, 250, 238);
    private static final Color ACCENT_COLOR = new Color(69, 123, 157);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 18);
    private static final Font SUBTITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 11);
    
    /**
     * Creates a new ticket generator.
     *
     * @param reservation The reservation
     * @param screening The screening
     * @param seats The seats
     * @param qrCodeData The QR code data
     * @param totalPrice The total price
     */
    public TicketGenerator(Reservation reservation, Screening screening, List<Seat> seats, 
                           String qrCodeData, double totalPrice) {
        this.reservation = reservation;
        this.screening = screening;
        this.seats = seats;
        this.qrCodeData = qrCodeData;
        this.totalPrice = totalPrice;
        
        // Generate QR code image
        try {
            this.qrCodeImage = QRCodeGenerator.generateQRCodeImage(qrCodeData, 100, 100);
        } catch (Exception e) {
            System.err.println("Error generating QR code: " + e.getMessage());
        }
    }
    
    /**
     * Prints the ticket.
     */
    public void printTicket() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                System.err.println("Printing error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Displays a preview of the ticket.
     */
    public void showPreview() {
        JFrame previewFrame = new JFrame("Ticket Preview");
        
        JPanel ticketPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawTicket((Graphics2D) g, getWidth(), getHeight());
            }
        };
        
        ticketPanel.setPreferredSize(new Dimension(TICKET_WIDTH + 2 * MARGIN, TICKET_HEIGHT + 2 * MARGIN));
        
        previewFrame.add(ticketPanel);
        previewFrame.pack();
        previewFrame.setLocationRelativeTo(null);
        previewFrame.setVisible(true);
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        // Use the width of the paper minus margins
        int width = (int) pageFormat.getImageableWidth();
        int height = (int) pageFormat.getImageableHeight();
        
        drawTicket(g2d, width, height);
        
        return PAGE_EXISTS;
    }
    
    /**
     * Draws the ticket.
     *
     * @param g2d The graphics context
     * @param width The available width
     * @param height The available height
     */
    private void drawTicket(Graphics2D g2d, int width, int height) {
        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Scale the ticket to fit the available space
        double scaleX = (double) (width - 2 * MARGIN) / TICKET_WIDTH;
        double scaleY = (double) (height - 2 * MARGIN) / TICKET_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        
        g2d.translate(MARGIN, MARGIN);
        g2d.scale(scale, scale);
        
        // Draw ticket background
        g2d.setColor(TICKET_BACKGROUND);
        g2d.fillRoundRect(0, 0, TICKET_WIDTH, TICKET_HEIGHT, 20, 20);
        
        // Draw border
        g2d.setColor(TICKET_BORDER);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(0, 0, TICKET_WIDTH, TICKET_HEIGHT, 20, 20);
        
        // Draw perforated line
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        g2d.drawLine(TICKET_WIDTH - 140, 0, TICKET_WIDTH - 140, TICKET_HEIGHT);
        
        // Draw header
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(TITLE_FONT);
        g2d.drawString("CINEBOOK CDO", 20, 35);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        LocalDateTime screeningTime = screening.getScreeningTime();
        Date screeningDate = java.sql.Timestamp.valueOf(screeningTime);
        
        // Draw movie info
        g2d.setFont(SUBTITLE_FONT);
        g2d.drawString(screening.getMovieTitle(), 20, 70);
        
        g2d.setFont(BODY_FONT);
        g2d.drawString("Cinema: " + screening.getCinemaName(), 20, 95);
        g2d.drawString("Date: " + dateFormat.format(screeningDate), 20, 115);
        g2d.drawString("Time: " + timeFormat.format(screeningDate), 20, 135);
        
        // Draw seat info
        StringBuilder seatInfo = new StringBuilder("Seats: ");
        for (int i = 0; i < seats.size(); i++) {
            seatInfo.append(seats.get(i).getSeatNumber());
            if (i < seats.size() - 1) {
                seatInfo.append(", ");
            }
        }
        g2d.drawString(seatInfo.toString(), 20, 155);
        
        // Draw price and ticket number
        g2d.drawString("Total Price: ₱" + String.format("%.2f", totalPrice), 20, 175);
        g2d.drawString("Ticket #: " + reservation.getId(), 20, 195);
        g2d.drawString("Issued: " + dateFormat.format(new Date()) + " " + timeFormat.format(new Date()), 20, 215);
        
        // Draw QR code
        if (qrCodeImage != null) {
            g2d.drawImage(qrCodeImage, TICKET_WIDTH - 120, TICKET_HEIGHT/2 - 50, 100, 100, null);
            g2d.setFont(new Font("SansSerif", Font.ITALIC, 9));
            g2d.drawString("Scan for entry", TICKET_WIDTH - 120, TICKET_HEIGHT/2 + 60);
        }
        
        // Draw footer
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 10));
        g2d.setColor(ACCENT_COLOR);
        g2d.drawString("Thank you for choosing CineBook CDO! Enjoy your movie!", 20, TICKET_HEIGHT - 20);
    }
}