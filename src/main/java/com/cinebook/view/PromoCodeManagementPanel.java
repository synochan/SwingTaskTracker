package com.cinebook.view;

import com.cinebook.controller.PromoCodeController;
import com.cinebook.model.PromoCode;
import com.cinebook.model.PromoCode.DiscountType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel for managing promo codes.
 */
public class PromoCodeManagementPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JFrame mainFrame;
    private PromoCodeController promoCodeController;
    
    private JTable promoCodesTable;
    private DefaultTableModel tableModel;
    
    private JTextField codeField;
    private JTextField descriptionField;
    private JComboBox<String> discountTypeComboBox;
    private JTextField discountAmountField;
    private JTextField validFromField;
    private JTextField validUntilField;
    private JTextField maxUsesField;
    private JTextField minPurchaseAmountField;
    private JCheckBox isActiveCheckBox;
    
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    
    private int selectedPromoCodeId = -1;
    
    /**
     * Constructor for PromoCodeManagementPanel.
     *
     * @param mainFrame The main application frame
     */
    public PromoCodeManagementPanel(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.promoCodeController = new PromoCodeController();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        initializeUI();
        loadPromoCodes();
    }
    
    /**
     * Initialize the UI components.
     */
    private void initializeUI() {
        // Table Panel (Center)
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Promo Codes"));
        
        // Table model with non-editable cells
        tableModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableModel.addColumn("ID");
        tableModel.addColumn("Code");
        tableModel.addColumn("Description");
        tableModel.addColumn("Discount");
        tableModel.addColumn("Valid From");
        tableModel.addColumn("Valid Until");
        tableModel.addColumn("Uses");
        tableModel.addColumn("Min Amount");
        tableModel.addColumn("Active");
        
        promoCodesTable = new JTable(tableModel);
        promoCodesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add selection listener
        promoCodesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = promoCodesTable.getSelectedRow();
                if (selectedRow != -1) {
                    selectedPromoCodeId = (int) tableModel.getValueAt(selectedRow, 0);
                    loadPromoCodeDetails(selectedPromoCodeId);
                    updateButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                } else {
                    clearFields();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(promoCodesTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Form Panel (East)
        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Promo Code Details"));
        
        // Fields Panel
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Code
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Code:"), gbc);
        
        gbc.gridx = 1;
        codeField = new JTextField(15);
        fieldsPanel.add(codeField, gbc);
        
        // Description
        gbc.gridx = 0;
        gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        descriptionField = new JTextField(15);
        fieldsPanel.add(descriptionField, gbc);
        
        // Discount Type
        gbc.gridx = 0;
        gbc.gridy = 2;
        fieldsPanel.add(new JLabel("Discount Type:"), gbc);
        
        gbc.gridx = 1;
        discountTypeComboBox = new JComboBox<>(new String[]{"PERCENTAGE", "FIXED"});
        fieldsPanel.add(discountTypeComboBox, gbc);
        
        // Discount Amount
        gbc.gridx = 0;
        gbc.gridy = 3;
        fieldsPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1;
        discountAmountField = new JTextField(15);
        fieldsPanel.add(discountAmountField, gbc);
        
        // Valid From
        gbc.gridx = 0;
        gbc.gridy = 4;
        fieldsPanel.add(new JLabel("Valid From (yyyy-MM-dd):"), gbc);
        
        gbc.gridx = 1;
        validFromField = new JTextField(15);
        fieldsPanel.add(validFromField, gbc);
        
        // Valid Until
        gbc.gridx = 0;
        gbc.gridy = 5;
        fieldsPanel.add(new JLabel("Valid Until (yyyy-MM-dd):"), gbc);
        
        gbc.gridx = 1;
        validUntilField = new JTextField(15);
        fieldsPanel.add(validUntilField, gbc);
        
        // Max Uses
        gbc.gridx = 0;
        gbc.gridy = 6;
        fieldsPanel.add(new JLabel("Max Uses (blank = unlimited):"), gbc);
        
        gbc.gridx = 1;
        maxUsesField = new JTextField(15);
        fieldsPanel.add(maxUsesField, gbc);
        
        // Min Purchase Amount
        gbc.gridx = 0;
        gbc.gridy = 7;
        fieldsPanel.add(new JLabel("Min Purchase Amount:"), gbc);
        
        gbc.gridx = 1;
        minPurchaseAmountField = new JTextField(15);
        fieldsPanel.add(minPurchaseAmountField, gbc);
        
        // Is Active
        gbc.gridx = 0;
        gbc.gridy = 8;
        fieldsPanel.add(new JLabel("Active:"), gbc);
        
        gbc.gridx = 1;
        isActiveCheckBox = new JCheckBox();
        isActiveCheckBox.setSelected(true);
        fieldsPanel.add(isActiveCheckBox, gbc);
        
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPromoCode();
            }
        });
        buttonsPanel.add(addButton);
        
        updateButton = new JButton("Update");
        updateButton.setEnabled(false);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePromoCode();
            }
        });
        buttonsPanel.add(updateButton);
        
        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePromoCode();
            }
        });
        buttonsPanel.add(deleteButton);
        
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        buttonsPanel.add(clearButton);
        
        formPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Set default values
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        validFromField.setText(today.format(formatter));
        validUntilField.setText(today.plusMonths(1).format(formatter));
        minPurchaseAmountField.setText("0.0");
        
        add(formPanel, BorderLayout.EAST);
    }
    
    /**
     * Load all promo codes from the database and populate the table.
     */
    private void loadPromoCodes() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Get all promo codes
        List<PromoCode> promoCodes = promoCodeController.getAllPromoCodes();
        
        // Add to table
        for (PromoCode promoCode : promoCodes) {
            Object[] rowData = new Object[9];
            rowData[0] = promoCode.getId();
            rowData[1] = promoCode.getCode();
            rowData[2] = promoCode.getDescription();
            rowData[3] = promoCode.getFormattedDiscount();
            rowData[4] = promoCode.getFormattedValidFrom();
            rowData[5] = promoCode.getFormattedValidUntil();
            
            String usesText = (promoCode.getMaxUses() != null) 
                ? promoCode.getCurrentUses() + "/" + promoCode.getMaxUses()
                : promoCode.getCurrentUses() + "/∞";
            rowData[6] = usesText;
            
            rowData[7] = String.format("₱%.2f", promoCode.getMinPurchaseAmount());
            rowData[8] = promoCode.isActive() ? "Yes" : "No";
            
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * Load the details of a promo code into the form fields.
     *
     * @param id The ID of the promo code to load
     */
    private void loadPromoCodeDetails(int id) {
        PromoCode promoCode = promoCodeController.getPromoCodeById(id);
        
        if (promoCode != null) {
            codeField.setText(promoCode.getCode());
            descriptionField.setText(promoCode.getDescription());
            discountTypeComboBox.setSelectedItem(promoCode.getDiscountType().name());
            discountAmountField.setText(String.valueOf(promoCode.getDiscountAmount()));
            validFromField.setText(promoCode.getFormattedValidFrom());
            validUntilField.setText(promoCode.getFormattedValidUntil());
            
            if (promoCode.getMaxUses() != null) {
                maxUsesField.setText(String.valueOf(promoCode.getMaxUses()));
            } else {
                maxUsesField.setText("");
            }
            
            minPurchaseAmountField.setText(String.valueOf(promoCode.getMinPurchaseAmount()));
            isActiveCheckBox.setSelected(promoCode.isActive());
        }
    }
    
    /**
     * Clear the form fields and reset the selection.
     */
    private void clearFields() {
        selectedPromoCodeId = -1;
        codeField.setText("");
        descriptionField.setText("");
        discountTypeComboBox.setSelectedIndex(0);
        discountAmountField.setText("");
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        validFromField.setText(today.format(formatter));
        validUntilField.setText(today.plusMonths(1).format(formatter));
        
        maxUsesField.setText("");
        minPurchaseAmountField.setText("0.0");
        isActiveCheckBox.setSelected(true);
        
        promoCodesTable.clearSelection();
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    /**
     * Validate the form fields.
     *
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateFields() {
        if (codeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "Please enter a code.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (descriptionField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "Please enter a description.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            double discountAmount = Double.parseDouble(discountAmountField.getText().trim());
            if (discountAmount <= 0) {
                JOptionPane.showMessageDialog(mainFrame, "Discount amount must be greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (discountTypeComboBox.getSelectedItem().equals("PERCENTAGE") && discountAmount > 100) {
                JOptionPane.showMessageDialog(mainFrame, "Percentage discount cannot exceed 100%.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainFrame, "Please enter a valid discount amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            LocalDate.parse(validFromField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(mainFrame, "Please enter a valid 'Valid From' date (yyyy-MM-dd).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            LocalDate.parse(validUntilField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(mainFrame, "Please enter a valid 'Valid Until' date (yyyy-MM-dd).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            LocalDate fromDate = LocalDate.parse(validFromField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate untilDate = LocalDate.parse(validUntilField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            if (fromDate.isAfter(untilDate)) {
                JOptionPane.showMessageDialog(mainFrame, "'Valid From' date cannot be after 'Valid Until' date.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (DateTimeParseException e) {
            // Already handled above
            return false;
        }
        
        if (!maxUsesField.getText().trim().isEmpty()) {
            try {
                int maxUses = Integer.parseInt(maxUsesField.getText().trim());
                if (maxUses <= 0) {
                    JOptionPane.showMessageDialog(mainFrame, "Max uses must be greater than 0 or left blank for unlimited.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter a valid number for max uses or leave it blank for unlimited.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        try {
            double minAmount = Double.parseDouble(minPurchaseAmountField.getText().trim());
            if (minAmount < 0) {
                JOptionPane.showMessageDialog(mainFrame, "Minimum purchase amount cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainFrame, "Please enter a valid minimum purchase amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Add a new promo code.
     */
    private void addPromoCode() {
        if (!validateFields()) {
            return;
        }
        
        String code = codeField.getText().trim();
        String description = descriptionField.getText().trim();
        String discountType = (String) discountTypeComboBox.getSelectedItem();
        double discountAmount = Double.parseDouble(discountAmountField.getText().trim());
        String validFrom = validFromField.getText().trim();
        String validUntil = validUntilField.getText().trim();
        String maxUses = maxUsesField.getText().trim();
        double minPurchaseAmount = Double.parseDouble(minPurchaseAmountField.getText().trim());
        
        int id = promoCodeController.addPromoCode(code, description, discountType, discountAmount, 
                                           validFrom, validUntil, maxUses, minPurchaseAmount);
        
        if (id != -1) {
            JOptionPane.showMessageDialog(mainFrame, "Promo code added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPromoCodes();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Failed to add promo code. Please check your inputs and try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Update an existing promo code.
     */
    private void updatePromoCode() {
        if (selectedPromoCodeId == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Please select a promo code to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!validateFields()) {
            return;
        }
        
        String code = codeField.getText().trim();
        String description = descriptionField.getText().trim();
        String discountType = (String) discountTypeComboBox.getSelectedItem();
        double discountAmount = Double.parseDouble(discountAmountField.getText().trim());
        String validFrom = validFromField.getText().trim();
        String validUntil = validUntilField.getText().trim();
        String maxUses = maxUsesField.getText().trim();
        double minPurchaseAmount = Double.parseDouble(minPurchaseAmountField.getText().trim());
        boolean isActive = isActiveCheckBox.isSelected();
        
        // Get current promo code to preserve current usage count
        PromoCode currentPromoCode = promoCodeController.getPromoCodeById(selectedPromoCodeId);
        int currentUses = 0;
        if (currentPromoCode != null) {
            currentUses = currentPromoCode.getCurrentUses();
        }
        
        boolean success = promoCodeController.updatePromoCode(selectedPromoCodeId, code, description, 
                                                     discountType, discountAmount, validFrom, validUntil, 
                                                     maxUses, currentUses, minPurchaseAmount, isActive);
        
        if (success) {
            JOptionPane.showMessageDialog(mainFrame, "Promo code updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPromoCodes();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Failed to update promo code. Please check your inputs and try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Delete a promo code.
     */
    private void deletePromoCode() {
        if (selectedPromoCodeId == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Please select a promo code to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(mainFrame, 
                                                 "Are you sure you want to delete this promo code?", 
                                                 "Confirm Delete", 
                                                 JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = promoCodeController.deletePromoCode(selectedPromoCodeId);
            
            if (success) {
                JOptionPane.showMessageDialog(mainFrame, "Promo code deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPromoCodes();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Failed to delete promo code.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}