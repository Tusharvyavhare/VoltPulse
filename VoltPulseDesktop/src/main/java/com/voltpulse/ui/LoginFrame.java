package com.voltpulse.ui;

import java.awt.Color;
import java.awt.Font;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.voltpulse.models.FirebaseConnection;

public class LoginFrame extends JFrame {

    public static final Preferences SESSION_PREFS = Preferences.userRoot().node("com/voltpulse/session");
    public static final String KEY_EMAIL = "logged_in_email";

    private JTextField tfEmail;
    private JPasswordField pfPassword;
    private JCheckBox chkRememberMe;
    private JButton btnLogin, btnSignup;
    private JLabel lblStatus;

    public LoginFrame() {
        FirebaseConnection.initializeFirebase();

        setTitle("VoltPulse 1.0 - Login");
        setSize(450, 445);
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

        JLabel lblTitle = new JLabel("VoltPulse 1.0 Login");
        lblTitle.setBounds(110, 25, 250, 30);
        lblTitle.setForeground(primaryColor);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(lblTitle);

        JLabel lblEmail = new JLabel("Registered Email (User ID):");
        lblEmail.setBounds(50, 75, 300, 20);
        lblEmail.setForeground(textColor);
        panel.add(lblEmail);

        tfEmail = new JTextField();
        tfEmail.setBounds(50, 100, 335, 35);
        tfEmail.setBackground(cardColor);
        tfEmail.setForeground(textColor);
        tfEmail.setCaretColor(textColor);
        panel.add(tfEmail);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(50, 145, 300, 20);
        lblPassword.setForeground(textColor);
        panel.add(lblPassword);

        pfPassword = new JPasswordField();
        pfPassword.setBounds(50, 170, 335, 35);
        pfPassword.setBackground(cardColor);
        pfPassword.setForeground(textColor);
        pfPassword.setCaretColor(textColor);
        panel.add(pfPassword);

        chkRememberMe = new JCheckBox("Keep me signed in", true);
        chkRememberMe.setBounds(50, 212, 200, 22);
        chkRememberMe.setBackground(bgColor);
        chkRememberMe.setForeground(textColor);
        chkRememberMe.setFocusPainted(false);
        panel.add(chkRememberMe);

        lblStatus = new JLabel("");
        lblStatus.setBounds(50, 238, 335, 35);
        lblStatus.setForeground(new Color(239, 68, 68));
        panel.add(lblStatus);

        btnLogin = new JButton("LOGIN");
        btnLogin.setBounds(50, 285, 335, 40);
        btnLogin.setBackground(primaryColor);
        btnLogin.setForeground(bgColor);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        panel.add(btnLogin);

        btnSignup = new JButton("New User? Sign Up");
        btnSignup.setBounds(50, 335, 335, 30);
        btnSignup.setBackground(cardColor);
        btnSignup.setForeground(primaryColor);
        btnSignup.setBorderPainted(false);
        btnSignup.setFocusPainted(false);
        panel.add(btnSignup);

        btnLogin.addActionListener(e -> {
            String email = tfEmail.getText().trim();
            String password = new String(pfPassword.getPassword());

            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!Pattern.matches(emailRegex, email)) {
                lblStatus.setText("Error: Invalid Email format!");
                return;
            }

            String passRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";
            if (!Pattern.matches(passRegex, password)) {
                lblStatus.setText("<html>Password Incorrect!</html>");
                return;
            }

            lblStatus.setForeground(new Color(34, 197, 94));
            lblStatus.setText("Checking credentials in Firebase...");

            String sanitizedEmail = email.replace(".", "_");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + sanitizedEmail);

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String storedPassword = snapshot.child("password").getValue(String.class);
                        if (storedPassword != null && storedPassword.equals(password)) {
                            if (chkRememberMe.isSelected()) {
                                SESSION_PREFS.put(KEY_EMAIL, email);
                            }
                            JOptionPane.showMessageDialog(null, "Login Successful! Welcome to VoltPulse 1.0");
                            dispose();
                            new DashboardFrame(email).setVisible(true);
                        } else {
                            lblStatus.setForeground(new Color(239, 68, 68));
                            lblStatus.setText("Error: Incorrect Password!");
                        }
                    } else {
                        lblStatus.setForeground(new Color(239, 68, 68));
                        lblStatus.setText("Error: User not registered! Please Sign Up.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    lblStatus.setForeground(new Color(239, 68, 68));
                    lblStatus.setText("Database Error: " + error.getMessage());
                }
            });
        });

        btnSignup.addActionListener(e -> {
            dispose();
            new SignupFrame().setVisible(true);
        });

        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String savedEmail = SESSION_PREFS.get(KEY_EMAIL, null);
            if (savedEmail != null && !savedEmail.trim().isEmpty()) {
                FirebaseConnection.initializeFirebase();
                new DashboardFrame(savedEmail).setVisible(true);
            } else {
                new LoginFrame().setVisible(true);
            }
        });
    }
}