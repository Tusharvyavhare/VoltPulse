package com.voltpulse.ui;

import java.awt.Color;
import java.awt.Font;
import java.time.LocalTime;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SlotBookingFrame extends JFrame {

    private String userEmail;
    private String stationName;
    private double baseRate;
    private int batteryPercentage;

    private JComboBox<String> comboBookingType;
    private JTextField tfKwh;
    private JLabel lblCalculatedBill;

    public SlotBookingFrame(String userEmail, String stationName, String rateStr, int batteryPercentage) {
        this.userEmail = userEmail;
        this.stationName = stationName;
        this.batteryPercentage = batteryPercentage;
        
        try {
            this.baseRate = Double.parseDouble(rateStr.replace("₹", "").trim());
        } catch (Exception e) {
            this.baseRate = 15.0;
        }

        setTitle("VoltPulse 1.0 - Slot Booking & Dynamic COA Billing");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        Color bgColor = new Color(15, 23, 42);
        Color cardColor = new Color(30, 41, 59);
        Color primaryColor = new Color(56, 189, 248);
        Color textColor = new Color(248, 250, 252);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(bgColor);

        JLabel lblTitle = new JLabel("⚡ Charging Slot Booking & Dynamic Billing");
        lblTitle.setBounds(30, 20, 500, 30);
        lblTitle.setForeground(primaryColor);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainPanel.add(lblTitle);

        JLabel lblStationInfo = new JLabel("Selected Station: " + stationName + " (Base: ₹" + baseRate + "/kWh) | Battery: " + batteryPercentage + "%");
        lblStationInfo.setBounds(30, 55, 740, 25);
        lblStationInfo.setForeground(textColor);
        lblStationInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mainPanel.add(lblStationInfo);

        JLabel lblType = new JLabel("Select Booking Queue Type:");
        lblType.setBounds(30, 110, 220, 25);
        lblType.setForeground(textColor);
        mainPanel.add(lblType);

        String[] bookingTypes = {"Normal Booking (Standard Rate)", "Priority Express Queue (+20% Surge)", "Off-Peak Night Slot (-10% Discount)"};
        comboBookingType = new JComboBox<>(bookingTypes);
        comboBookingType.setBounds(250, 110, 310, 30);
        comboBookingType.setBackground(cardColor);
        comboBookingType.setForeground(textColor);
        mainPanel.add(comboBookingType);

        JLabel lblKwh = new JLabel("Estimated Energy Required (kWh):");
        lblKwh.setBounds(30, 160, 220, 25);
        lblKwh.setForeground(textColor);
        mainPanel.add(lblKwh);

        tfKwh = new JTextField("25.0");
        tfKwh.setBounds(250, 160, 310, 30);
        tfKwh.setBackground(cardColor);
        tfKwh.setForeground(textColor);
        mainPanel.add(tfKwh);

        JPanel billPanel = new JPanel();
        billPanel.setBounds(30, 215, 730, 160);
        billPanel.setBackground(cardColor);
        billPanel.setLayout(null);
        billPanel.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));

        JLabel lblBillHeader = new JLabel("COA Hardware & Peak-Pricing Calculation Monitor");
        lblBillHeader.setBounds(20, 12, 400, 20);
        lblBillHeader.setForeground(primaryColor);
        lblBillHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        billPanel.add(lblBillHeader);

        lblCalculatedBill = new JLabel("<html>Click 'Calculate Bill' to evaluate current tariff rates & GST.</html>");
        lblCalculatedBill.setBounds(20, 38, 690, 110);
        lblCalculatedBill.setForeground(textColor);
        billPanel.add(lblCalculatedBill);

        mainPanel.add(billPanel);

        JButton btnCalculate = new JButton("Calculate Bill (COA Engine)");
        btnCalculate.setBounds(30, 395, 350, 42);
        btnCalculate.setBackground(cardColor);
        btnCalculate.setForeground(primaryColor);
        btnCalculate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCalculate.setBorder(BorderFactory.createLineBorder(primaryColor, 1));
        btnCalculate.setFocusPainted(false);
        mainPanel.add(btnCalculate);

        JButton btnConfirmBooking = new JButton("Confirm Booking & View Route Map 🗺️");
        btnConfirmBooking.setBounds(410, 395, 350, 42);
        btnConfirmBooking.setBackground(primaryColor);
        btnConfirmBooking.setForeground(bgColor);
        btnConfirmBooking.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirmBooking.setFocusPainted(false);
        mainPanel.add(btnConfirmBooking);

        btnCalculate.addActionListener(e -> computeBillDetails());

        btnConfirmBooking.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Slot Successfully Booked in Queue!\nOpening Dynamic GPS Route & Live Simulation...");
            dispose();
            new ChargingRouteSimulationFrame(userEmail, stationName, batteryPercentage).setVisible(true);
        });

        add(mainPanel);
    }

    private void computeBillDetails() {
        try {
            double kwh = Double.parseDouble(tfKwh.getText().trim());
            int typeIndex = comboBookingType.getSelectedIndex();
            
            double effectiveRate = baseRate;
            String queueName = "Normal Queue";
            
            if (typeIndex == 1) {
                effectiveRate = baseRate * 1.20; // +20%
                queueName = "Priority Express Queue";
            } else if (typeIndex == 2) {
                effectiveRate = baseRate * 0.90; // -10%
                queueName = "Off-Peak Night Slot";
            }

            // Time-of-day Peak check (5 PM to 9 PM)
            int currentHour = LocalTime.now().getHour();
            boolean isPeakHour = (currentHour >= 17 && currentHour <= 21);
            if (isPeakHour) {
                effectiveRate *= 1.10; // Extra 10% peak surge
            }

            double energyCost = kwh * effectiveRate;
            double tax = energyCost * 0.05;
            double totalBill = energyCost + tax;

            lblCalculatedBill.setText(String.format(
                "<html><b>Queue Assigned:</b> %s | <b>Grid Load:</b> %s<br/>" +
                "<b>Energy Consumed:</b> %.1f kWh @ ₹%.2f/kWh<br/>" +
                "<b>Base Cost:</b> ₹%.2f | <b>GST (5%%):</b> ₹%.2f<br/>" +
                "<b><font color='#38BDF8'>Total Estimated Bill:</font></b> ₹%.2f (Computed via COA Engine)</html>",
                queueName, (isPeakHour ? "<font color='#EF4444'>Peak Hours Surge (+10%)</font>" : "<font color='#22C55E'>Standard Grid</font>"),
                kwh, effectiveRate, energyCost, tax, totalBill
            ));

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric value for kWh!");
        }
    }
}