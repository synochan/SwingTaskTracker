package com.cinebook.view;

import com.cinebook.controller.AdminController;
import com.cinebook.model.Payment;
import com.cinebook.model.Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Panel for displaying reports and analytics in the admin dashboard.
 */
public class ReportPanel extends JPanel {
    private MainFrame mainFrame;
    private AdminController adminController;
    
    // UI Components
    private JComboBox<String> reportTypeComboBox;
    private JPanel reportContentPanel;
    private JPanel filterPanel;
    
    // Date components
    private JTextField startDateField;
    private JTextField endDateField;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;
    
    // Tables
    private JTable revenueTable;
    private DefaultTableModel revenueTableModel;
    private JTable reservationsTable;
    private DefaultTableModel reservationsTableModel;
    
    // Data formatter
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Constructor for ReportPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param adminController The AdminController instance
     */
    public ReportPanel(MainFrame mainFrame, AdminController adminController) {
        this.mainFrame = mainFrame;
        this.adminController = adminController;
        
        // Setup panel properties
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create components
        createReportContentPanel();
        createControlPanel();
        
        // Initialize date fields
        updateFilterPanel();
        
        // Initial report
        generateReport();
    }
    
    /**
     * Creates the control panel with report type selection.
     */
    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // Report type selection
        JPanel reportTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportTypePanel.add(new JLabel("Report Type:"));
        
        String[] reportTypes = {
            "Daily Revenue", "Weekly Revenue", "Monthly Revenue",
            "Revenue by Movie", "Revenue by Cinema", "Ticket Sales",
            "Recent Reservations"
        };
        
        reportTypeComboBox = new JComboBox<>(reportTypes);
        reportTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFilterPanel();
                generateReport();
            }
        });
        
        reportTypePanel.add(reportTypeComboBox);
        
        // Filter panel (dynamic based on report type)
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        controlPanel.add(reportTypePanel, BorderLayout.NORTH);
        controlPanel.add(filterPanel, BorderLayout.CENTER);
        
        add(controlPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates the report content panel.
     */
    private void createReportContentPanel() {
        reportContentPanel = new JPanel(new BorderLayout());
        reportContentPanel.setBorder(new TitledBorder("Report Results"));
        
        // Initialize tables
        initializeTables();
        
        add(reportContentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Initializes the tables for displaying report data.
     */
    private void initializeTables() {
        // Revenue table
        String[] revenueColumns = {"Category", "Amount (₱)"};
        revenueTableModel = new DefaultTableModel(revenueColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        revenueTable = new JTable(revenueTableModel);
        revenueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        revenueTable.getTableHeader().setReorderingAllowed(false);
        
        // Reservations table
        String[] reservationsColumns = {"ID", "User", "Movie", "Cinema", "Date", "Time", "Seats", "Total Amount", "Status"};
        reservationsTableModel = new DefaultTableModel(reservationsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reservationsTable = new JTable(reservationsTableModel);
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationsTable.getTableHeader().setReorderingAllowed(false);
    }
    
    /**
     * Updates the filter panel based on the selected report type.
     */
    private void updateFilterPanel() {
        filterPanel.removeAll();
        
        String selectedReportType = (String) reportTypeComboBox.getSelectedItem();
        
        if (selectedReportType.equals("Daily Revenue")) {
            // Date selector
            filterPanel.add(new JLabel("Date:"));
            startDateField = new JTextField(10);
            startDateField.setText(LocalDate.now().format(dateFormatter));
            filterPanel.add(startDateField);
            
            JButton generateButton = new JButton("Generate Report");
            generateButton.addActionListener(e -> showDailyRevenueReport());
            filterPanel.add(generateButton);
        } else if (selectedReportType.equals("Weekly Revenue") || 
                   selectedReportType.equals("Revenue by Movie") || 
                   selectedReportType.equals("Revenue by Cinema") ||
                   selectedReportType.equals("Ticket Sales") ||
                   selectedReportType.equals("Recent Reservations")) {
            // Date range selector
            filterPanel.add(new JLabel("Start Date:"));
            startDateField = new JTextField(10);
            startDateField.setText(LocalDate.now().minusDays(7).format(dateFormatter));
            filterPanel.add(startDateField);
            
            filterPanel.add(new JLabel("End Date:"));
            endDateField = new JTextField(10);
            endDateField.setText(LocalDate.now().format(dateFormatter));
            filterPanel.add(endDateField);
            
            JButton generateButton = new JButton("Generate Report");
            generateButton.addActionListener(e -> generateReport());
            filterPanel.add(generateButton);
        } else if (selectedReportType.equals("Monthly Revenue")) {
            // Year and month selector
            filterPanel.add(new JLabel("Year:"));
            yearComboBox = new JComboBox<>();
            int currentYear = LocalDate.now().getYear();
            for (int year = currentYear - 5; year <= currentYear; year++) {
                yearComboBox.addItem(year);
            }
            yearComboBox.setSelectedItem(currentYear);
            filterPanel.add(yearComboBox);
            
            filterPanel.add(new JLabel("Month:"));
            monthComboBox = new JComboBox<>(new String[]{
                "January", "February", "March", "April", "May", "June", 
                "July", "August", "September", "October", "November", "December"
            });
            monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
            filterPanel.add(monthComboBox);
            
            JButton generateButton = new JButton("Generate Report");
            generateButton.addActionListener(e -> showMonthlyRevenueReport());
            filterPanel.add(generateButton);
        }
        
        filterPanel.revalidate();
        filterPanel.repaint();
    }
    
    /**
     * Generates a report based on the selected type.
     */
    private void generateReport() {
        String selectedReportType = (String) reportTypeComboBox.getSelectedItem();
        
        switch (selectedReportType) {
            case "Daily Revenue":
                showDailyRevenueReport();
                break;
            case "Weekly Revenue":
                showWeeklyRevenueReport();
                break;
            case "Monthly Revenue":
                showMonthlyRevenueReport();
                break;
            case "Revenue by Movie":
                showRevenueByMovieReport();
                break;
            case "Revenue by Cinema":
                showRevenueByCinemaReport();
                break;
            case "Ticket Sales":
                showTicketSalesReport();
                break;
            case "Recent Reservations":
                showRecentReservationsReport();
                break;
        }
    }
    
    /**
     * Shows the daily revenue report.
     */
    private void showDailyRevenueReport() {
        // Get data
        String date = startDateField.getText();
        double totalSales = adminController.getDailySalesReport(date);
        
        // Clear and update table
        revenueTableModel.setRowCount(0);
        revenueTableModel.addRow(new Object[]{"Total Sales", String.format("₱%.2f", totalSales)});
        
        // Set content
        JScrollPane scrollPane = new JScrollPane(revenueTable);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        JLabel summaryLabel = new JLabel("Daily Revenue Report for " + date);
        summaryLabel.setFont(new Font("Serif", Font.BOLD, 16));
        summaryPanel.add(summaryLabel, BorderLayout.NORTH);
        
        // Update content panel
        reportContentPanel.removeAll();
        reportContentPanel.add(summaryPanel, BorderLayout.NORTH);
        reportContentPanel.add(scrollPane, BorderLayout.CENTER);
        reportContentPanel.revalidate();
        reportContentPanel.repaint();
    }
    
    /**
     * Shows the weekly revenue report.
     */
    private void showWeeklyRevenueReport() {
        // Get data
        String startDate = startDateField.getText();
        String endDate = endDateField.getText();
        double totalSales = adminController.getWeeklySalesReport(startDate, endDate);
        
        // Clear and update table
        revenueTableModel.setRowCount(0);
        revenueTableModel.addRow(new Object[]{"Total Sales", String.format("₱%.2f", totalSales)});
        
        // Set content
        JScrollPane scrollPane = new JScrollPane(revenueTable);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        JLabel summaryLabel = new JLabel("Revenue Report from " + startDate + " to " + endDate);
        summaryLabel.setFont(new Font("Serif", Font.BOLD, 16));
        summaryPanel.add(summaryLabel, BorderLayout.NORTH);
        
        // Update content panel
        reportContentPanel.removeAll();
        reportContentPanel.add(summaryPanel, BorderLayout.NORTH);
        reportContentPanel.add(scrollPane, BorderLayout.CENTER);
        reportContentPanel.revalidate();
        reportContentPanel.repaint();
    }
    
    /**
     * Shows the monthly revenue report.
     */
    private void showMonthlyRevenueReport() {
        // Get data
        int year = (Integer) yearComboBox.getSelectedItem();
        int month = monthComboBox.getSelectedIndex() + 1;
        String monthName = (String) monthComboBox.getSelectedItem();
        
        double totalSales = adminController.getMonthlySalesReport(year, month);
        
        // Clear and update table
        revenueTableModel.setRowCount(0);
        revenueTableModel.addRow(new Object[]{"Total Sales", String.format("₱%.2f", totalSales)});
        
        // Set content
        JScrollPane scrollPane = new JScrollPane(revenueTable);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        JLabel summaryLabel = new JLabel("Monthly Revenue Report for " + monthName + " " + year);
        summaryLabel.setFont(new Font("Serif", Font.BOLD, 16));
        summaryPanel.add(summaryLabel, BorderLayout.NORTH);
        
        // Update content panel
        reportContentPanel.removeAll();
        reportContentPanel.add(summaryPanel, BorderLayout.NORTH);
        reportContentPanel.add(scrollPane, BorderLayout.CENTER);
        reportContentPanel.revalidate();
        reportContentPanel.repaint();
    }
    
    /**
     * Shows the revenue by movie report.
     */
    private void showRevenueByMovieReport() {
        // Get data
        String startDate = startDateField.getText();
        String endDate = endDateField.getText();
        Map<String, Double> salesByMovie = adminController.getSalesByMovie(startDate, endDate);
        
        // Clear and update table
        revenueTableModel.setRowCount(0);
        
        double totalSales = 0.0;
        for (Map.Entry<String, Double> entry : salesByMovie.entrySet()) {
            revenueTableModel.addRow(new Object[]{entry.getKey(), String.format("₱%.2f", entry.getValue())});
            totalSales += entry.getValue();
        }
        
        revenueTableModel.addRow(new Object[]{"", ""});
        revenueTableModel.addRow(new Object[]{"Total", String.format("₱%.2f", totalSales)});
        
        // Set content
        JScrollPane scrollPane = new JScrollPane(revenueTable);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        JLabel summaryLabel = new JLabel("Revenue by Movie from " + startDate + " to " + endDate);
        summaryLabel.setFont(new Font("Serif", Font.BOLD, 16));
        summaryPanel.add(summaryLabel, BorderLayout.NORTH);
        
        // Update content panel
        reportContentPanel.removeAll();
        reportContentPanel.add(summaryPanel, BorderLayout.NORTH);
        reportContentPanel.add(scrollPane, BorderLayout.CENTER);
        reportContentPanel.revalidate();
        reportContentPanel.repaint();
    }
    
    /**
     * Shows the revenue by cinema report.
     */
    private void showRevenueByCinemaReport() {
        // Get data
        String startDate = startDateField.getText();
        String endDate = endDateField.getText();
        Map<String, Double> salesByCinema = adminController.getSalesByCinema(startDate, endDate);
        
        // Clear and update table
        revenueTableModel.setRowCount(0);
        
        double totalSales = 0.0;
        for (Map.Entry<String, Double> entry : salesByCinema.entrySet()) {
            revenueTableModel.addRow(new Object[]{entry.getKey(), String.format("₱%.2f", entry.getValue())});
            totalSales += entry.getValue();
        }
        
        revenueTableModel.addRow(new Object[]{"", ""});
        revenueTableModel.addRow(new Object[]{"Total", String.format("₱%.2f", totalSales)});
        
        // Set content
        JScrollPane scrollPane = new JScrollPane(revenueTable);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        JLabel summaryLabel = new JLabel("Revenue by Cinema from " + startDate + " to " + endDate);
        summaryLabel.setFont(new Font("Serif", Font.BOLD, 16));
        summaryPanel.add(summaryLabel, BorderLayout.NORTH);
        
        // Update content panel
        reportContentPanel.removeAll();
        reportContentPanel.add(summaryPanel, BorderLayout.NORTH);
        reportContentPanel.add(scrollPane, BorderLayout.CENTER);
        reportContentPanel.revalidate();
        reportContentPanel.repaint();
    }
    
    /**
     * Shows the ticket sales report.
     */
    private void showTicketSalesReport() {
        // Get data
        String startDate = startDateField.getText();
        String endDate = endDateField.getText();
        int ticketsSold = adminController.getTicketsSold(startDate, endDate);
        
        // Clear and update table
        revenueTableModel.setRowCount(0);
        revenueTableModel.addRow(new Object[]{"Total Tickets Sold", ticketsSold});
        
        // Set content
        JScrollPane scrollPane = new JScrollPane(revenueTable);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        JLabel summaryLabel = new JLabel("Ticket Sales Report from " + startDate + " to " + endDate);
        summaryLabel.setFont(new Font("Serif", Font.BOLD, 16));
        summaryPanel.add(summaryLabel, BorderLayout.NORTH);
        
        // Update content panel
        reportContentPanel.removeAll();
        reportContentPanel.add(summaryPanel, BorderLayout.NORTH);
        reportContentPanel.add(scrollPane, BorderLayout.CENTER);
        reportContentPanel.revalidate();
        reportContentPanel.repaint();
    }
    
    /**
     * Shows the recent reservations report.
     */
    private void showRecentReservationsReport() {
        // Get data
        String startDate = startDateField.getText();
        String endDate = endDateField.getText();
        List<Reservation> reservations = adminController.getReservationsByDateRange(startDate, endDate);
        
        // Clear and update table
        reservationsTableModel.setRowCount(0);
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Reservation reservation : reservations) {
            String date = reservation.getReservationTime().toLocalDate().format(dateFormatter);
            String time = reservation.getReservationTime().toLocalTime().format(timeFormatter);
            
            Object[] row = {
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getMovieTitle(),
                reservation.getCinemaName(),
                date,
                time,
                reservation.getSelectedSeats().size(),
                String.format("₱%.2f", reservation.getTotalAmount()),
                reservation.isPaid() ? "Paid" : "Pending"
            };
            
            reservationsTableModel.addRow(row);
        }
        
        // Set content
        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        JLabel summaryLabel = new JLabel("Reservations from " + startDate + " to " + endDate);
        summaryLabel.setFont(new Font("Serif", Font.BOLD, 16));
        summaryPanel.add(summaryLabel, BorderLayout.NORTH);
        
        // Update content panel
        reportContentPanel.removeAll();
        reportContentPanel.add(summaryPanel, BorderLayout.NORTH);
        reportContentPanel.add(scrollPane, BorderLayout.CENTER);
        reportContentPanel.revalidate();
        reportContentPanel.repaint();
    }
    
    /**
     * Refreshes the data displayed in the panel.
     */
    public void refreshData() {
        generateReport();
    }
}