package com.cinebook.util;

import com.cinebook.model.Concession;
import com.cinebook.model.Payment;
import com.cinebook.model.Reservation;
import com.cinebook.model.Screening;
import com.cinebook.model.Seat;
import com.cinebook.model.SeatType;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Generates stylish, printable receipts for movie reservations and concessions.
 */
public class ReceiptGenerator implements Printable {
    private Reservation reservation;
    private Screening screening;
    private List<Seat> seats;
    private List<Concession> concessions;
    private Payment payment;
    
    // Receipt design constants
    private static final int MARGIN = 30;
    private static final int RECEIPT_WIDTH = 400;
    private static final int RECEIPT_HEIGHT = 600;
    private static final Color RECEIPT_BACKGROUND = new Color(250, 250, 250);
    private static final Color RECEIPT_BORDER = new Color(200, 200, 200);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    private static final Color ACCENT_COLOR = new Color(69, 123, 157);
    private static final Color HEADER_COLOR = new Color(29, 53, 87);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font SUBTITLE_FONT = new Font("SansSerif", Font.BOLD, 12);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 10);
    private static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 8);
    
    /**
     * Creates a new receipt generator.
     *
     * @param reservation The reservation
     * @param screening The screening
     * @param seats The seats
     * @param concessions The concessions
     * @param payment The payment
     */
    public ReceiptGenerator(Reservation reservation, Screening screening, List<Seat> seats, 
                            List<Concession> concessions, Payment payment) {
        this.reservation = reservation;
        this.screening = screening;
        this.seats = seats;
        this.concessions = concessions;
        this.payment = payment;
    }
    
    /**
     * Prints the receipt.
     */
    public void printReceipt() {
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
     * Displays a preview of the receipt.
     */
    public void showPreview() {
        JFrame previewFrame = new JFrame("Receipt Preview");
        
        JPanel receiptPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawReceipt((Graphics2D) g, getWidth(), getHeight());
            }
        };
        
        receiptPanel.setPreferredSize(new Dimension(RECEIPT_WIDTH + 2 * MARGIN, RECEIPT_HEIGHT + 2 * MARGIN));
        
        previewFrame.add(receiptPanel);
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
        
        drawReceipt(g2d, width, height);
        
        return PAGE_EXISTS;
    }
    
    /**
     * Draws the receipt.
     *
     * @param g2d The graphics context
     * @param width The available width
     * @param height The available height
     */
    private void drawReceipt(Graphics2D g2d, int width, int height) {
        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Scale the receipt to fit the available space
        double scaleX = (double) (width - 2 * MARGIN) / RECEIPT_WIDTH;
        double scaleY = (double) (height - 2 * MARGIN) / RECEIPT_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        
        g2d.translate(MARGIN, MARGIN);
        g2d.scale(scale, scale);
        
        // Draw receipt background
        g2d.setColor(RECEIPT_BACKGROUND);
        g2d.fillRect(0, 0, RECEIPT_WIDTH, RECEIPT_HEIGHT);
        
        // Draw border
        g2d.setColor(RECEIPT_BORDER);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(0, 0, RECEIPT_WIDTH, RECEIPT_HEIGHT);
        
        // Draw header background
        g2d.setColor(HEADER_COLOR);
        g2d.fillRect(0, 0, RECEIPT_WIDTH, 50);
        
        // Draw company name
        g2d.setColor(Color.WHITE);
        g2d.setFont(TITLE_FONT);
        centerString(g2d, "CINEBOOK CDO", RECEIPT_WIDTH / 2, 30);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        Date screeningDate = screening.getDateTime();
        Date currentDate = new Date();
        
        // Draw current date and receipt number
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(BODY_FONT);
        g2d.drawString("Date: " + dateFormat.format(currentDate) + " " + timeFormat.format(currentDate), 20, 70);
        g2d.drawString("Receipt #: " + payment.getPaymentId(), 20, 85);
        
        // Draw divider
        g2d.setColor(RECEIPT_BORDER);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(20, 95, RECEIPT_WIDTH - 20, 95);
        
        // Draw movie info
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(SUBTITLE_FONT);
        g2d.drawString("MOVIE DETAILS", 20, 115);
        
        g2d.setFont(BODY_FONT);
        g2d.drawString("Movie: " + screening.getMovieTitle(), 40, 130);
        g2d.drawString("Cinema: " + screening.getCinemaName(), 40, 145);
        g2d.drawString("Date: " + dateFormat.format(screeningDate), 40, 160);
        g2d.drawString("Time: " + timeFormat.format(screeningDate), 40, 175);
        
        // Draw seat info
        StringBuilder seatInfo = new StringBuilder("Seats: ");
        for (int i = 0; i < seats.size(); i++) {
            seatInfo.append(seats.get(i).getSeatCode());
            if (i < seats.size() - 1) {
                seatInfo.append(", ");
            }
        }
        g2d.drawString(seatInfo.toString(), 40, 190);
        
        // Calculate seat costs
        double standardSeatTotal = 0;
        double deluxeSeatTotal = 0;
        int standardSeatCount = 0;
        int deluxeSeatCount = 0;
        
        for (Seat seat : seats) {
            if (seat.getSeatType() == SeatType.STANDARD) {
                standardSeatCount++;
                standardSeatTotal += screening.getStandardSeatPrice();
            } else {
                deluxeSeatCount++;
                deluxeSeatTotal += screening.getDeluxeSeatPrice();
            }
        }
        
        // Draw divider
        g2d.setColor(RECEIPT_BORDER);
        g2d.drawLine(20, 205, RECEIPT_WIDTH - 20, 205);
        
        // Draw ticket details
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(SUBTITLE_FONT);
        g2d.drawString("TICKET DETAILS", 20, 225);
        
        g2d.setFont(BODY_FONT);
        
        int y = 245;
        if (standardSeatCount > 0) {
            g2d.drawString(String.format("Standard Seat x %d", standardSeatCount), 40, y);
            g2d.drawString(String.format("₱%.2f", screening.getStandardSeatPrice()), 260, y);
            g2d.drawString(String.format("₱%.2f", standardSeatTotal), RECEIPT_WIDTH - 40, y);
            y += 15;
        }
        
        if (deluxeSeatCount > 0) {
            g2d.drawString(String.format("Deluxe Seat x %d", deluxeSeatCount), 40, y);
            g2d.drawString(String.format("₱%.2f", screening.getDeluxeSeatPrice()), 260, y);
            g2d.drawString(String.format("₱%.2f", deluxeSeatTotal), RECEIPT_WIDTH - 40, y);
            y += 15;
        }
        
        // Draw concession details if any
        if (concessions != null && !concessions.isEmpty()) {
            // Draw divider
            g2d.setColor(RECEIPT_BORDER);
            g2d.drawLine(20, y + 5, RECEIPT_WIDTH - 20, y + 5);
            y += 25;
            
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(SUBTITLE_FONT);
            g2d.drawString("CONCESSIONS", 20, y);
            y += 20;
            
            g2d.setFont(BODY_FONT);
            double concessionsTotal = 0;
            
            for (Concession concession : concessions) {
                g2d.drawString(concession.getName() + " x " + concession.getQuantity(), 40, y);
                g2d.drawString(String.format("₱%.2f", concession.getPrice()), 260, y);
                double itemTotal = concession.getPrice() * concession.getQuantity();
                g2d.drawString(String.format("₱%.2f", itemTotal), RECEIPT_WIDTH - 40, y);
                concessionsTotal += itemTotal;
                y += 15;
            }
        }
        
        // Draw divider
        g2d.setColor(RECEIPT_BORDER);
        g2d.drawLine(20, y + 5, RECEIPT_WIDTH - 20, y + 5);
        y += 25;
        
        // Draw totals
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(SUBTITLE_FONT);
        g2d.drawString("TOTAL", 20, y);
        g2d.drawString(String.format("₱%.2f", payment.getAmount()), RECEIPT_WIDTH - 40, y);
        y += 20;
        
        // Draw payment details
        g2d.setFont(BODY_FONT);
        g2d.drawString("Payment Method: " + payment.getPaymentMethod(), 40, y);
        
        // Draw footer
        y = RECEIPT_HEIGHT - 40;
        g2d.setColor(ACCENT_COLOR);
        g2d.setFont(SMALL_FONT);
        centerString(g2d, "Thank you for choosing CineBook CDO!", RECEIPT_WIDTH / 2, y);
        y += 15;
        centerString(g2d, "For refunds and complaints, please contact our customer service.", RECEIPT_WIDTH / 2, y);
        y += 15;
        centerString(g2d, "Contact: 09123456789 | Email: support@cinebookcdo.com", RECEIPT_WIDTH / 2, y);
    }
    
    /**
     * Helper method to center a string.
     *
     * @param g2d The graphics context
     * @param text The text to center
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    private void centerString(Graphics2D g2d, String text, int x, int y) {
        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        g2d.drawString(text, x - textWidth / 2, y);
    }
}