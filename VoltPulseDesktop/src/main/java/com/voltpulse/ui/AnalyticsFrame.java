package com.voltpulse.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.voltpulse.graphics.OpenGLPanel;

public class AnalyticsFrame extends JFrame {

    public AnalyticsFrame(String userEmail) {
        setTitle("VoltPulse 1.0 - Energy Consumption & Expense Analytics");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

        JLabel lblTitle = new JLabel("📊 Monthly EV Energy & Expense Analytics");
        lblTitle.setBounds(30, 20, 500, 30);
        lblTitle.setForeground(primaryColor);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainPanel.add(lblTitle);

        OpenGLPanel chartPanel = new OpenGLPanel() {
            @Override
            protected void renderOpenGL(int width, int height) {
                org.lwjgl.opengl.GL11.glPushMatrix();
                org.lwjgl.opengl.GL11.glScalef(width / 740.0f, height / 320.0f, 1);

                fillRect(0, 0, 740, 320, cardColor);
                drawRect(20, 20, 725, 280, new Color(51, 65, 85), 1);
                drawText(30, 30, "Energy (kWh)", secondaryText, 1.25f);
                drawText(670, 278, "Months (2026)", secondaryText, 1.25f);

                String[] months = {"Jun", "Jul", "Aug", "Sep", "Oct"};
                int[] kwhValues = {45, 60, 85, 110, 95};
                int startX = 80;
                int barWidth = 70;
                int maxVal = 120;
                int maxHeight = 200;

                for (int i = 0; i < months.length; i++) {
                    int barHeight = (int) ((double) kwhValues[i] / maxVal * maxHeight);
                    int x = startX + (i * 120);
                    int y = 250 - barHeight;

                    fillRoundRect(x, y, barWidth, barHeight, 8, primaryColor);
                    drawText(x + 10, y - 20, kwhValues[i] + " kWh", textColor, 1.5f);
                    drawText(x + 22, 265, months[i], secondaryText, 1.5f);
                }

                org.lwjgl.opengl.GL11.glPopMatrix();
            }
        };
        chartPanel.setBounds(30, 70, 740, 320);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        mainPanel.add(chartPanel);

        // Summary Statistics Card
        JPanel statsCard = new JPanel();
        statsCard.setBounds(30, 410, 740, 75);
        statsCard.setBackground(cardColor);
        statsCard.setLayout(null);
        statsCard.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));

        JLabel lblStats = new JLabel("<html><b>Total Consumption (Last 5 Months):</b> 395 kWh &nbsp;|&nbsp; <b>Total Spent:</b> ₹6,220 &nbsp;|&nbsp; <b>Avg Efficiency:</b> 6.2 km/kWh</html>");
        lblStats.setBounds(20, 22, 700, 30);
        lblStats.setForeground(textColor);
        lblStats.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsCard.add(lblStats);

        mainPanel.add(statsCard);

        add(mainPanel);
    }
}