package com.voltpulse.ui;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignupFrame extends JFrame {

    private JTextField tfFirstName, tfLastName, tfEmail;
    private JPasswordField pfPassword, pfConfirmPassword;
    private JButton btnSignup, btnBack;
    private JLabel lblStatus;

    public SignupFrame() {
        setTitle("VoltPulse 1.0 - Sign Up");
        setSize(450, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        Color bgColor = new Color(15, 23, 42);      
        Color cardColor = new Color(30, 41, 59);    
        Color primaryColor = new Color(56, 189, 248); 
        Color textColor = new Color(248, 250, 252); 

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(bgColor);

        JLabel lblTitle = new JLabel("Create VoltPulse Account");
        lblTitle.setBounds(100, 20, 280, 30);
        lblTitle.setForeground(primaryColor);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitle);

        JLabel lblFirstName = new JLabel("First Name :");
        lblFirstName.setBounds(50, 65, 300, 20);
        lblFirstName.setForeground(textColor);
        panel.add(lblFirstName);

        tfFirstName = new JTextField();
        tfFirstName.setBounds(50, 88, 335, 30);
        tfFirstName.setBackground(cardColor);
        tfFirstName.setForeground(textColor);
        tfFirstName.setCaretColor(textColor);
        panel.add(tfFirstName);

        JLabel lblLastName = new JLabel("Last Name :");
        lblLastName.setBounds(50, 125, 300, 20);
        lblLastName.setForeground(textColor);
        panel.add(lblLastName);

        tfLastName = new JTextField();
        tfLastName.setBounds(50, 148, 335, 30);
        tfLastName.setBackground(cardColor);
        tfLastName.setForeground(textColor);
        tfLastName.setCaretColor(textColor);
        panel.add(tfLastName);

        JLabel lblEmail = new JLabel("Email ID:");
        lblEmail.setBounds(50, 185, 300, 20);
        lblEmail.setForeground(textColor);
        panel.add(lblEmail);

        tfEmail = new JTextField();
        tfEmail.setBounds(50, 208, 335, 30);
        tfEmail.setBackground(cardColor);
        tfEmail.setForeground(textColor);
        tfEmail.setCaretColor(textColor);
        panel.add(tfEmail);

        JLabel lblPassword = new JLabel("Password :");
        lblPassword.setBounds(50, 245, 350, 20);
        lblPassword.setForeground(textColor);
        panel.add(lblPassword);

        pfPassword = new JPasswordField();
        pfPassword.setBounds(50, 268, 335, 30);
        pfPassword.setBackground(cardColor);
        pfPassword.setForeground(textColor);
        pfPassword.setCaretColor(textColor);
        panel.add(pfPassword);

        JLabel lblConfirmPassword = new JLabel("Confirm Password:");
        lblConfirmPassword.setBounds(50, 305, 300, 20);
        lblConfirmPassword.setForeground(textColor);
        panel.add(lblConfirmPassword);

        pfConfirmPassword = new JPasswordField();
        pfConfirmPassword.setBounds(50, 328, 335, 30);
        pfConfirmPassword.setBackground(cardColor);
        pfConfirmPassword.setForeground(textColor);
        pfConfirmPassword.setCaretColor(textColor);
        panel.add(pfConfirmPassword);

        lblStatus = new JLabel("");
        lblStatus.setBounds(50, 362, 335, 25);
        lblStatus.setForeground(new Color(239, 68, 68));
        panel.add(lblStatus);

        btnSignup = new JButton("REGISTER");
        btnSignup.setBounds(50, 395, 335, 38);
        btnSignup.setBackground(primaryColor);
        btnSignup.setForeground(bgColor);
        btnSignup.setFont(new Font("Arial", Font.BOLD, 14));
        btnSignup.setFocusPainted(false);
        panel.add(btnSignup);

        btnBack = new JButton("Already have an account? Login");
        btnBack.setBounds(50, 442, 335, 28);
        btnBack.setBackground(cardColor);
        btnBack.setForeground(primaryColor);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        panel.add(btnBack);

        btnSignup.addActionListener(e -> {
            String firstName = tfFirstName.getText().trim();
            String lastName = tfLastName.getText().trim();
            String email = tfEmail.getText().trim();
            String password = new String(pfPassword.getPassword());
            String confirmPassword = new String(pfConfirmPassword.getPassword());

            String nameRegex = "^[a-zA-Z]+$";
            if (!Pattern.matches(nameRegex, firstName) || !Pattern.matches(nameRegex, lastName)) {
                lblStatus.setText("Error: First & Last name must contain alphabets only!");
                return;
            }

            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!Pattern.matches(emailRegex, email)) {
                lblStatus.setText("Error: Invalid Email format!");
                return;
            }

            String passRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";
            if (!Pattern.matches(passRegex, password)) {
                lblStatus.setText("Password must be 8+ chars with Upper, Lower, Num & Special!");
                return;
            }

            if (!password.equals(confirmPassword)) {
                lblStatus.setText("Error: Passwords do not match!");
                return;
            }

            lblStatus.setForeground(new Color(34, 197, 94));
            lblStatus.setText("Saving data to Firebase Database...");

            String sanitizedEmail = email.replace(".", "_");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + sanitizedEmail);

            Map<String, Object> userData = new HashMap<>();
            userData.put("firstName", firstName);
            userData.put("lastName", lastName);
            userData.put("email", email);
            userData.put("password", password);

            ref.setValueAsync(userData);

            JOptionPane.showMessageDialog(null, "Registration Successful! Please login.");
            dispose();
            new LoginFrame().setVisible(true);
        });

        btnBack.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        add(panel);
    }
}