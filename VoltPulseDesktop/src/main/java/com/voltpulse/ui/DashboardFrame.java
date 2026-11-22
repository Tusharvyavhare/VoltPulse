package com.voltpulse.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardFrame extends JFrame {

    private String userEmail;
    private String fullName = "Loading...";
    private int batteryPercentage = 45;
    private String vehicleModel = "Tata Nexon EV";
    private String vehicleNumber = "MH-12-AB-1234";

    private JLabel lblUserName;
    private JButton btnBattery;
    private JButton btnVehicle;

    public DashboardFrame(String userEmail) {
        this.userEmail = userEmail;
        fetchUserDataFromFirebase();

        setTitle("VoltPulse 1.0 - Smart EV Charging Ecosystem");
        setSize(980, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        Color bgColor = new Color(15, 23, 42);      
        Color cardColor = new Color(30, 41, 59);    
        Color primaryColor = new Color(56, 189, 248); 
        Color textColor = new Color(248, 250, 252); 
        Color secondaryText = new Color(148, 163, 184);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(bgColor);

        // Header
        lblUserName = new JLabel("Welcome, Loading...");
        lblUserName.setBounds(40, 18, 450, 30);
        lblUserName.setForeground(textColor);
        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        mainPanel.add(lblUserName);

       JButton btnProfile = new JButton("👤 My Profile");
        btnProfile.setBounds(505, 15, 130, 35);
        btnProfile.setBackground(cardColor);
        btnProfile.setForeground(primaryColor);
        btnProfile.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnProfile.setFocusPainted(false);
        btnProfile.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248, 100), 1));
        mainPanel.add(btnProfile);

        JButton btnExportCsv = new JButton("📊 Export CSV");
        btnExportCsv.setBounds(648, 15, 135, 35);
        btnExportCsv.setBackground(new Color(16, 185, 129)); 
        btnExportCsv.setForeground(Color.WHITE);
        btnExportCsv.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportCsv.setFocusPainted(false);
        mainPanel.add(btnExportCsv);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBounds(796, 15, 114, 35);
        btnLogout.setBackground(new Color(225, 29, 72));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setFocusPainted(false);
        mainPanel.add(btnLogout);

        // Tiles Grid
        btnBattery = new JButton();
        btnBattery.setBounds(40, 65, 425, 140);
        btnBattery.setBackground(cardColor);
        btnBattery.setForeground(textColor);
        btnBattery.setFocusPainted(false);
        btnBattery.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        mainPanel.add(btnBattery);

        btnVehicle = new JButton();
        btnVehicle.setBounds(485, 65, 425, 140);
        btnVehicle.setBackground(cardColor);
        btnVehicle.setForeground(textColor);
        btnVehicle.setFocusPainted(false);
        btnVehicle.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        mainPanel.add(btnVehicle);

        JButton btnStation = new JButton("<html><center><font size='4'>📍</font><br/><b>Station Locator</b><br/><font color='#94A3B8' size='2'>Find Nearest Pune Hubs</font></center></html>");
        btnStation.setBounds(40, 215, 425, 140);
        btnStation.setBackground(cardColor);
        btnStation.setForeground(textColor);
        btnStation.setFocusPainted(false);
        btnStation.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        mainPanel.add(btnStation);

        com.voltpulse.coa.CoaIntegrationManager.runCompleteCoaPipeline();
        
        JButton btnBooking = new JButton("<html><center><font size='4'>⚡</font><br/><b>Slot Booking & Billing</b><br/><font color='#94A3B8' size='2'>COA Tariff Calculation</font></center></html>");
        btnBooking.setBounds(485, 215, 425, 140);
        btnBooking.setBackground(cardColor);
        btnBooking.setForeground(textColor);
        btnBooking.setFocusPainted(false);
        btnBooking.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        mainPanel.add(btnBooking);

        // Action Buttons Row
        JButton btnIntercity = new JButton("🛣️ Intercity Route Planner (Pune - Mumbai / Nashik)");
        btnIntercity.setBounds(40, 368, 425, 38);
        btnIntercity.setBackground(cardColor);
        btnIntercity.setForeground(primaryColor);
        btnIntercity.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnIntercity.setBorder(BorderFactory.createLineBorder(primaryColor, 1));
        btnIntercity.setFocusPainted(false);
        mainPanel.add(btnIntercity);

        JButton btnAnalytics = new JButton("📈 View Energy & Expense Analytics");
        btnAnalytics.setBounds(485, 368, 425, 38);
        btnAnalytics.setBackground(cardColor);
        btnAnalytics.setForeground(primaryColor);
        btnAnalytics.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAnalytics.setBorder(BorderFactory.createLineBorder(primaryColor, 1));
        btnAnalytics.setFocusPainted(false);
        mainPanel.add(btnAnalytics);

        updateButtonTexts();

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBounds(0, 470, 980, 40);
        footerPanel.setBackground(new Color(10, 15, 30));
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JLabel lblFooter = new JLabel("VoltPulse 1.0 Enterprise Edition  |  Smart EV Infrastructure");
        lblFooter.setForeground(secondaryText);
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerPanel.add(lblFooter);
        mainPanel.add(footerPanel);

        // Actions
        btnProfile.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "=== USER PROFILE ===\n\n" +
                    "Name: " + fullName + "\n" +
                    "Email: " + userEmail + "\n" +
                    "Vehicle: " + vehicleModel + " (" + vehicleNumber + ")\n" +
                    "Current Battery: " + batteryPercentage + "%",
                    "User Profile", JOptionPane.INFORMATION_MESSAGE);
        });

        btnExportCsv.addActionListener(e -> exportTransactionHistoryCsv());
        btnLogout.addActionListener(e -> {
            LoginFrame.SESSION_PREFS.remove(LoginFrame.KEY_EMAIL);
            dispose();
            new LoginFrame().setVisible(true);
        });

        btnBattery.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, 
                    "Current Battery Percentage: " + batteryPercentage + "%\nEnter new battery level (0-100):", 
                    String.valueOf(batteryPercentage));
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int val = Integer.parseInt(input.trim());
                    if (val >= 0 && val <= 100) {
                        batteryPercentage = val;
                        updateButtonTexts();
                        saveToFirebase();
                        JOptionPane.showMessageDialog(this, "Battery level updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Please enter a value between 0 and 100.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number format!");
                }
            }
        });

        btnVehicle.addActionListener(e -> {
            String[] options = {"Tata Nexon EV (MH-12-AB-1234)", "MG ZS EV (MH-14-EV-9999)", "Ather 450X (MH-12-AT-4321)", "+ Add Custom EV"};
            String choice = (String) JOptionPane.showInputDialog(this, "Select Active Garage EV:", "Multi-Vehicle Garage",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            
            if (choice != null) {
                if (choice.equals("+ Add Custom EV")) {
                    String model = JOptionPane.showInputDialog(this, "Enter Vehicle Model:", vehicleModel);
                    String number = JOptionPane.showInputDialog(this, "Enter Vehicle Number:", vehicleNumber);
                    if (model != null && number != null) {
                        vehicleModel = model;
                        vehicleNumber = number;
                    }
                } else {
                    vehicleModel = choice.split("\\(")[0].trim();
                    vehicleNumber = choice.split("\\(")[1].replace(")", "").trim();
                }
                updateButtonTexts();
                saveToFirebase();
                JOptionPane.showMessageDialog(this, "Active Garage Vehicle updated to " + vehicleModel + "!");
            }
        });

        btnStation.addActionListener(e -> {
            new StationFinderFrame(userEmail, batteryPercentage).setVisible(true);
        });

        btnBooking.addActionListener(e -> {
            dispose();
            new SlotBookingFrame(userEmail, "VoltPulse Hub Alandi (Direct)", "15.0", batteryPercentage).setVisible(true);
        });

        btnIntercity.addActionListener(e -> openIntercityPlanner());

        btnAnalytics.addActionListener(e -> {
            new AnalyticsFrame(userEmail).setVisible(true);
        });

        add(mainPanel);
    }

    private void openIntercityPlanner() {
        String[] routes = {"Pune ➔ Mumbai Express (150 km)", "Pune ➔ Nashik Highway (210 km)", "Pune ➔ Kolhapur Corridor (230 km)"};
        String selected = (String) JOptionPane.showInputDialog(this, "Select Highway Corridor:", "Intercity Trip Planner",
                JOptionPane.QUESTION_MESSAGE, null, routes, routes[0]);

        if (selected != null) {
            String advice = (batteryPercentage >= 80) ? "✓ Battery Sufficient! 1 stop recommended at Lonavala Power Hub." : "⚠️ Charge Low! 2 intermediate stops required.";
            JOptionPane.showMessageDialog(this, 
                "=== INTERCITY ROUTE PLANNER ===\n\n" +
                "Selected Route: " + selected + "\n" +
                "Current Range: ~" + (batteryPercentage * 2) + " km (" + batteryPercentage + "% Battery)\n\n" +
                "Route Advisory:\n" + advice,
                "Intercity Navigation", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportTransactionHistoryCsv() {
        try {
            File csvFile = new File("VoltPulse_Transaction_History.csv");
            PrintWriter pw = new PrintWriter(csvFile);
            pw.println("Transaction_ID,Station_Name,User_Email,Energy_kWh,Amount_INR,Date");
            pw.println("TXN1001,VoltPulse Hub Alandi," + userEmail + ",25.0,393.75,2026-10-24");
            pw.close();
            JOptionPane.showMessageDialog(this, "Transaction History exported successfully!\nFile Saved: " + csvFile.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage());
        }
    }

    private void updateButtonTexts() {
        if (btnBattery != null) {
            btnBattery.setText("<html><center><font size='4'>🔋</font><br/><b>Battery Monitor</b><br/><font color='#38BDF8' size='2'>Current Level: " + batteryPercentage + "%</font></center></html>");
        }
        if (btnVehicle != null) {
            btnVehicle.setText("<html><center><font size='4'>🚗</font><br/><b>Garage Management</b><br/><font color='#38BDF8' size='2'>" + vehicleModel + " (" + vehicleNumber + ")</font></center></html>");
        }
    }

    private void saveToFirebase() {
        String sanitizedEmail = userEmail.replace(".", "_");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + sanitizedEmail);
        Map<String, Object> map = new HashMap<>();
        map.put("batteryPercentage", batteryPercentage);
        Map<String, Object> vMap = new HashMap<>();
        vMap.put("model", vehicleModel);
        vMap.put("number", vehicleNumber);
        map.put("vehicle", vMap);
        ref.updateChildrenAsync(map);
    }

    private void fetchUserDataFromFirebase() {
        String sanitizedEmail = userEmail.replace(".", "_");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + sanitizedEmail);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fName = snapshot.child("firstName").getValue(String.class);
                    String lName = snapshot.child("lastName").getValue(String.class);
                    if (fName != null && lName != null) {
                        fullName = fName + " " + lName;
                        if (lblUserName != null) {
                            lblUserName.setText("Welcome, " + fullName);
                        }
                    }
                    if (snapshot.hasChild("batteryPercentage")) {
                        Integer bat = snapshot.child("batteryPercentage").getValue(Integer.class);
                        if (bat != null) batteryPercentage = bat;
                    }
                    if (snapshot.hasChild("vehicle")) {
                        String mod = snapshot.child("vehicle/model").getValue(String.class);
                        String num = snapshot.child("vehicle/number").getValue(String.class);
                        if (mod != null) vehicleModel = mod;
                        if (num != null) vehicleNumber = num;
                    }
                    updateButtonTexts();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}