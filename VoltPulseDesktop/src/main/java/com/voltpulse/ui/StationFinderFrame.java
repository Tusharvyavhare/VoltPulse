package com.voltpulse.ui;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class StationFinderFrame extends JFrame {

    static class Station {
        String name;
        String location;
        double x, y;
        int availableSlots;
        double ratePerKwh;

        public Station(String name, String location, double x, double y, int availableSlots, double ratePerKwh) {
            this.name = name;
            this.location = location;
            this.x = x;
            this.y = y;
            this.availableSlots = availableSlots;
            this.ratePerKwh = ratePerKwh;
        }
    }

    private static final List<Station> puneStations = new ArrayList<>();
    private static String selectedOriginLocation = "Shivajinagar";

    static {
        initStationData();
    }

    private JTable stationTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> comboLocation;
    private String userEmail;
    private int batteryPercentage;

    public StationFinderFrame(String userEmail, int batteryPercentage) {
        this.userEmail = userEmail;
        this.batteryPercentage = batteryPercentage;

        setTitle("VoltPulse 1.0 - Pune EV Station Finder");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        Color bgColor = new Color(15, 23, 42);      // #0F172A
        Color cardColor = new Color(30, 41, 59);    // #1E293B
        Color primaryColor = new Color(56, 189, 248); // #38BDF8
        Color textColor = new Color(248, 250, 252); // #F8FAFC

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(bgColor);

        // Header
        JLabel lblTitle = new JLabel("📍 Pune EV Charging Stations");
        lblTitle.setBounds(30, 20, 400, 30);
        lblTitle.setForeground(primaryColor);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        mainPanel.add(lblTitle);

        // Location Selector
        JLabel lblSelect = new JLabel("Select Your Current Location:");
        lblSelect.setBounds(30, 65, 200, 25);
        lblSelect.setForeground(textColor);
        mainPanel.add(lblSelect);

        String[] locations = {"Alandi", "Shivajinagar", "Baner", "Kothrud", "Viman Nagar", "Hinjewadi"};
        comboLocation = new JComboBox<>(locations);
        comboLocation.setSelectedItem(selectedOriginLocation);
        comboLocation.setBounds(230, 65, 180, 28);
        comboLocation.setBackground(cardColor);
        comboLocation.setForeground(textColor);
        mainPanel.add(comboLocation);

        JButton btnSearch = new JButton("Find Nearest 5 Stations");
        btnSearch.setBounds(425, 65, 200, 28);
        btnSearch.setBackground(primaryColor);
        btnSearch.setForeground(bgColor);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setFocusPainted(false);
        mainPanel.add(btnSearch);

        // Table Setup
        String[] columns = {"Station Name", "Area", "Available Slots", "Rate (₹/kWh)", "Distance (km)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        stationTable = new JTable(tableModel);
        stationTable.setBackground(cardColor);
        stationTable.setForeground(textColor);
        stationTable.setRowHeight(30);
        stationTable.getTableHeader().setBackground(new Color(51, 65, 85));
        stationTable.getTableHeader().setForeground(primaryColor);
        stationTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(stationTable);
        scrollPane.setBounds(30, 115, 775, 300);
        mainPanel.add(scrollPane);

        // Proceed to Booking Button
        JButton btnProceed = new JButton("Proceed to Slot Booking ⚡");
        btnProceed.setBounds(30, 435, 775, 45);
        btnProceed.setBackground(primaryColor);
        btnProceed.setForeground(bgColor);
        btnProceed.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProceed.setFocusPainted(false);
        mainPanel.add(btnProceed);

        // Search Action (DS Sorting)
        btnSearch.addActionListener(e -> findAndSortNearestStations());
        findAndSortNearestStations();

        // Proceed Button Action - Opens SlotBookingFrame with battery percentage
        btnProceed.addActionListener(e -> {
            int selectedRow = stationTable.getSelectedRow();
            if (selectedRow != -1) {
                String stationName = (String) tableModel.getValueAt(selectedRow, 0);
                String rate = tableModel.getValueAt(selectedRow, 3).toString();
                
                dispose();
                new SlotBookingFrame(userEmail, stationName, rate, batteryPercentage).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a station from the list first!");
            }
        });

        add(mainPanel);
    }

    private static void initStationData() {
        if (!puneStations.isEmpty()) return;
        puneStations.add(new Station("VoltPulse Hub Alandi", "Alandi", 18.67, 73.89, 4, 15.0));
        puneStations.add(new Station("Alandi Green Grid", "Alandi", 18.68, 73.90, 2, 14.5));
        puneStations.add(new Station("Shivajinagar Fast Charge", "Shivajinagar", 18.53, 73.85, 6, 18.0));
        puneStations.add(new Station("FC Road Power Station", "Shivajinagar", 18.52, 73.84, 3, 17.5));
        puneStations.add(new Station("Baner Express Plug", "Baner", 18.56, 73.78, 5, 16.0));
        puneStations.add(new Station("Baner Highway Charging", "Baner", 18.57, 73.77, 1, 16.5));
        puneStations.add(new Station("Kothrud Eco Charge", "Kothrud", 18.50, 73.81, 4, 15.5));
        puneStations.add(new Station("Paud Road Grid", "Kothrud", 18.51, 73.80, 2, 15.0));
        puneStations.add(new Station("Viman Nagar Airport Hub", "Viman Nagar", 18.56, 73.91, 8, 19.0));
        puneStations.add(new Station("Phoenix Market Charge", "Viman Nagar", 18.55, 73.92, 3, 18.5));
        puneStations.add(new Station("Hinjewadi IT Park Grid", "Hinjewadi", 18.59, 73.73, 10, 20.0));
        puneStations.add(new Station("Phase 1 Fast Power", "Hinjewadi", 18.60, 73.74, 5, 19.5));
        puneStations.add(new Station("Pimpri Central Hub", "Pimpri", 18.62, 73.80, 4, 15.0));
        puneStations.add(new Station("Chinchwad Power Dock", "Chinchwad", 18.63, 73.79, 3, 14.0));
        puneStations.add(new Station("Hadapsar Solar Grid", "Hadapsar", 18.50, 73.93, 2, 16.0));
        puneStations.add(new Station("Magarpatta City Plug", "Hadapsar", 18.51, 73.92, 6, 17.0));
        puneStations.add(new Station("Swargate Transit Station", "Swargate", 18.50, 73.85, 4, 15.0));
        puneStations.add(new Station("Deccan Gymkhana Hub", "Deccan", 18.51, 73.84, 2, 16.5));
        puneStations.add(new Station("Aundh Smart Dock", "Aundh", 18.55, 73.80, 5, 17.0));
        puneStations.add(new Station("Wakad Bypass Charge", "Wakad", 18.59, 73.76, 3, 16.0));
    }

    static List<Station> getAllStations() {
        return puneStations;
    }

    static Station findStationByName(String stationName) {
        if (stationName != null) {
            String cleanName = stationName.replace("(Direct)", "").trim();
            for (Station s : puneStations) {
                if (s.name.equalsIgnoreCase(cleanName)) {
                    return s;
                }
            }
        }
        return puneStations.get(0); // Default fallback: VoltPulse Hub Alandi
    }

    static double[] getOriginLatLon(String area) {
        if ("Alandi".equals(area)) return new double[]{18.67, 73.89};
        if ("Baner".equals(area)) return new double[]{18.56, 73.78};
        if ("Kothrud".equals(area)) return new double[]{18.50, 73.81};
        if ("Viman Nagar".equals(area)) return new double[]{18.56, 73.91};
        if ("Hinjewadi".equals(area)) return new double[]{18.59, 73.73};
        return new double[]{18.53, 73.85}; // Shivajinagar default
    }

    static String getSelectedOriginLocation() {
        return selectedOriginLocation;
    }

    private void findAndSortNearestStations() {
        String selectedLoc = (String) comboLocation.getSelectedItem();
        selectedOriginLocation = selectedLoc;

        double[] coords = getOriginLatLon(selectedLoc);
        final double targetX = coords[0];
        final double targetY = coords[1];

        puneStations.sort(Comparator.comparingDouble(s -> 
            Math.sqrt(Math.pow(s.x - targetX, 2) + Math.pow(s.y - targetY, 2))
        ));

        tableModel.setRowCount(0);
        for (int i = 0; i < Math.min(5, puneStations.size()); i++) {
            Station s = puneStations.get(i);
            double dist = Math.sqrt(Math.pow(s.x - targetX, 2) + Math.pow(s.y - targetY, 2)) * 100;
            tableModel.addRow(new Object[]{
                s.name, s.location, s.availableSlots + " Slots", "₹" + s.ratePerKwh, String.format("%.1f km", dist)
            });
        }
    }
}