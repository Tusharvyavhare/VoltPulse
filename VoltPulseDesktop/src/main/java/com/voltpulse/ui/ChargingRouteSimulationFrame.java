package com.voltpulse.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.voltpulse.graphics.OpenGLPanel;
import com.voltpulse.ui.StationFinderFrame.Station;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

public class ChargingRouteSimulationFrame extends JFrame {

    // Pune Geographic Bounds matching all 20 stations in StationFinderFrame
    private static final double MIN_LAT = 18.49; // South: Kothrud / Swargate / Hadapsar
    private static final double MAX_LAT = 18.69; // North: Alandi
    private static final double MIN_LON = 73.72; // West: Hinjewadi
    private static final double MAX_LON = 73.94; // East: Hadapsar / Viman Nagar

    private final List<float[]> routeWaypoints = new ArrayList<>();
    private final Station targetStation;
    private final String originArea;
    private final double calculatedDistanceKm;

    private String userEmail;
    private String stationName;
    private int initialBattery;
    private int chargeProgress;
    private double powerIntake = 0.0;
    private Timer simulationTimer;

    private double carRouteProgress = 0.0;
    private int carParkX = -80;
    private boolean isDriving = false;
    private boolean isParked = false;
    private boolean ledBlinkState = false;
    private int animTick = 0;

    private final Color bgColor = new Color(11, 17, 32);
    private final Color cardColor = new Color(24, 34, 52);
    private final Color borderColor = new Color(45, 62, 88);
    private final Color primaryColor = new Color(56, 189, 248);
    private final Color accentGreen = new Color(34, 197, 94);
    private final Color textColor = new Color(248, 250, 252);
    private final Color secondaryText = new Color(148, 163, 184);

    private JLabel lblStatus;
    private JLabel lblEtaBadge;
    private JLabel lblTelemetryStatus;
    private JLabel lblPowerKw;
    private JLabel lblBatteryPct;
    private JProgressBar batteryBar;
    private JButton btnStartDrive;
    private JButton btnStopCharging;
    private JButton btnDiagnostics;

    public ChargingRouteSimulationFrame(String userEmail, String stationName, int initialBattery) {
        this.userEmail = userEmail;
        this.stationName = stationName;
        this.initialBattery = initialBattery;
        this.chargeProgress = initialBattery;

        // Match booked station against your 20 Pune stations in StationFinderFrame
        this.targetStation = StationFinderFrame.findStationByName(stationName);
        this.originArea = StationFinderFrame.getSelectedOriginLocation();
        buildPuneNavigationRoute();
        if (routeWaypoints.size() < 2) {
            routeWaypoints.clear();
            routeWaypoints.add(new float[]{-4.6f, 1.1f});
            routeWaypoints.add(new float[]{4.5f, -1.0f});
        }
        this.calculatedDistanceKm = computeRouteDistanceKm();

        setTitle("VoltPulse 1.0 - Enterprise EV Navigation & Simulation");
        setSize(920, 780);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(bgColor);

        // Header Card
        JPanel headerCard = new JPanel();
        headerCard.setLayout(null);
        headerCard.setBounds(20, 15, 865, 65);
        headerCard.setBackground(cardColor);
        headerCard.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        JLabel lblTitle = new JLabel("VOLTPULSE 1.0  |  PUNE SMART GRID");
        lblTitle.setBounds(20, 11, 440, 24);
        lblTitle.setForeground(primaryColor);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerCard.add(lblTitle);

        JLabel lblSubTitle = new JLabel("Route: " + originArea + "  -->  " + targetStation.name + " (" + targetStation.location + ")   |   Tariff: Rs." + targetStation.ratePerKwh + "/kWh");
        lblSubTitle.setBounds(20, 36, 560, 18);
        lblSubTitle.setForeground(secondaryText);
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        headerCard.add(lblSubTitle);

        btnDiagnostics = new JButton("Hardware Status");
        btnDiagnostics.setBounds(700, 16, 150, 32);
        btnDiagnostics.setBackground(new Color(15, 23, 42));
        btnDiagnostics.setForeground(textColor);
        btnDiagnostics.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDiagnostics.setFocusPainted(false);
        btnDiagnostics.setBorder(BorderFactory.createLineBorder(primaryColor, 1));
        headerCard.add(btnDiagnostics);
        mainPanel.add(headerCard);

        // Detailed 3D Pune Map Card
        OpenGLPanel mapCard = new OpenGLPanel() {
            @Override
            protected void renderOpenGL(int width, int height) {
                begin3D(width, height, 52, 14.8f);
                glRotatef(50, 1, 0, 0);
                glPushMatrix();

                // Subtle camera follow-pan as EV travels across Pune
                float camOffsetX = 0f;
                float camOffsetZ = 0.08f;
                if (isDriving && !isParked) {
                    float[] carPos = getInterpolatedCarPosition();
                    camOffsetX = -carPos[0] * 0.22f;
                    camOffsetZ = 0.08f - carPos[1] * 0.18f;
                }
                glTranslatef(camOffsetX, -0.12f, camOffsetZ);

                // 1. Base Pune Metropolitan Terrain & Perimeter Border
                drawBox(0, -0.26f, 0, 13.4f, 0.45f, 7.6f, new Color(15, 23, 38));
                drawBox(0, -0.02f, -3.75f, 13.4f, 0.06f, 0.08f, new Color(45, 65, 95));
                drawBox(0, -0.02f, 3.75f, 13.4f, 0.06f, 0.08f, new Color(45, 65, 95));

                Color gridColor = new Color(30, 44, 66);
                for (int x = -6; x <= 6; x++) {
                    drawWorldLine(x, -0.015f, -3.6f, x, -0.015f, 3.6f, gridColor, 1);
                }
                for (int z = -3; z <= 3; z++) {
                    drawWorldLine(-6.4f, -0.015f, z, 6.4f, -0.015f, z, gridColor, 1);
                }

                // 2. Pune Green Zones & Hills (Vetal Tekdi, Pune University, Pashan, Koregaon Park)
                drawBox(-0.85f, -0.005f, 0.75f, 1.35f, 0.035f, 0.95f, new Color(18, 54, 44));
                drawBox(-3.55f, -0.005f, -0.95f, 1.40f, 0.035f, 0.85f, new Color(18, 54, 44));
                drawBox(2.35f, -0.005f, 1.75f, 1.15f, 0.035f, 0.70f, new Color(18, 54, 44));

                // 3. Mula-Mutha River with Lighter Shoreline Highlight
                Color riverBank = new Color(22, 92, 132);
                Color riverCore = new Color(12, 68, 104);
                float[][] mulaMutha = {
                    {-5.2f, -1.6f}, {-3.0f, -0.9f}, {-1.2f, -0.3f}, {0.8f, 1.3f}, {2.8f, 1.4f}, {6.0f, 1.8f}
                };
                for (int i = 0; i < mulaMutha.length - 1; i++) {
                    drawGroundSegment(mulaMutha[i][0], mulaMutha[i][1], mulaMutha[i + 1][0], mulaMutha[i + 1][1],
                            0.004f, 0.28f, riverBank);
                    drawGroundSegment(mulaMutha[i][0], mulaMutha[i][1], mulaMutha[i + 1][0], mulaMutha[i + 1][1],
                            0.008f, 0.18f, riverCore);
                }

                // 4. Pune Arterial Road Network with Highway Center Lines
                Color roadColor = new Color(36, 48, 68);
                Color highwayColor = new Color(46, 61, 86);
                Color laneLineColor = new Color(94, 114, 145);

                float[][] nh48 = {
                    {-5.5f, -0.3f}, {-3.6f, -0.1f}, {-2.6f, 0.8f}, {-1.5f, 2.4f}
                };
                for (int i = 0; i < nh48.length - 1; i++) {
                    drawGroundSegment(nh48[i][0], nh48[i][1], nh48[i + 1][0], nh48[i + 1][1], 0.015f, 0.28f, highwayColor);
                    drawGroundSegment(nh48[i][0], nh48[i][1], nh48[i + 1][0], nh48[i + 1][1], 0.019f, 0.025f, laneLineColor);
                }

                float[][] pcmcHighway = {
                    {-2.2f, -1.3f}, {-1.5f, -0.9f}, {-1.5f, 1.1f}, {0.9f, 1.7f}
                };
                for (int i = 0; i < pcmcHighway.length - 1; i++) {
                    drawGroundSegment(pcmcHighway[i][0], pcmcHighway[i][1], pcmcHighway[i + 1][0], pcmcHighway[i + 1][1], 0.015f, 0.24f, highwayColor);
                    drawGroundSegment(pcmcHighway[i][0], pcmcHighway[i][1], pcmcHighway[i + 1][0], pcmcHighway[i + 1][1], 0.019f, 0.02f, laneLineColor);
                }

                float[][] alandiHighway = {
                    {0.9f, 1.7f}, {2.4f, 0.8f}, {3.1f, -2.5f}
                };
                for (int i = 0; i < alandiHighway.length - 1; i++) {
                    drawGroundSegment(alandiHighway[i][0], alandiHighway[i][1], alandiHighway[i + 1][0], alandiHighway[i + 1][1], 0.015f, 0.22f, roadColor);
                }

                float[][] metroCorridor = {
                    {-1.5f, 2.4f}, {0.4f, 2.1f}, {0.9f, 1.7f}, {4.1f, 0.8f}, {5.1f, 2.4f}
                };
                for (int i = 0; i < metroCorridor.length - 1; i++) {
                    drawGroundSegment(metroCorridor[i][0], metroCorridor[i][1], metroCorridor[i + 1][0], metroCorridor[i + 1][1], 0.015f, 0.25f, highwayColor);
                    drawGroundSegment(metroCorridor[i][0], metroCorridor[i][1], metroCorridor[i + 1][0], metroCorridor[i + 1][1], 0.019f, 0.02f, laneLineColor);
                }

                // 5. Multi-Tiered 3D Urban Skyline with Illuminated Roof Caps
                Color towerBody = new Color(44, 60, 84);
                Color towerGlass = new Color(62, 84, 116);
                Color roofBeacon = new Color(56, 189, 248);

                // Hinjewadi IT Park Towers (West)
                drawBox(-5.15f, 0.28f, -0.65f, 0.44f, 0.56f, 0.38f, towerBody);
                drawBox(-5.15f, 0.57f, -0.65f, 0.36f, 0.03f, 0.30f, roofBeacon);
                drawBox(-4.60f, 0.36f, 0.12f, 0.40f, 0.72f, 0.38f, towerGlass);
                drawBox(-4.60f, 0.73f, 0.12f, 0.32f, 0.03f, 0.30f, roofBeacon);

                // Pimpri-Chinchwad Industrial & Auto Hub (North-West)
                drawBox(-2.25f, 0.22f, -1.35f, 0.58f, 0.44f, 0.44f, towerBody);
                drawBox(-2.25f, 0.45f, -1.35f, 0.48f, 0.025f, 0.36f, towerGlass);

                // Shivajinagar & Deccan Metro District (Center)
                drawBox(0.45f, 0.26f, 1.95f, 0.50f, 0.52f, 0.44f, towerGlass);
                drawBox(1.18f, 0.24f, 2.25f, 0.42f, 0.48f, 0.38f, towerBody);
                drawBox(0.45f, 0.53f, 1.95f, 0.38f, 0.025f, 0.32f, roofBeacon);

                // Viman Nagar Airport & Magarpatta Cybercity (East)
                drawBox(4.35f, 0.28f, 0.45f, 0.58f, 0.56f, 0.42f, towerGlass);
                drawBox(4.35f, 0.57f, 0.45f, 0.46f, 0.03f, 0.32f, roofBeacon);
                drawBox(4.65f, 0.26f, 2.15f, 0.50f, 0.52f, 0.40f, towerBody);

                // Alandi Knowledge Hub (North-East)
                drawBox(3.45f, 0.22f, -2.45f, 0.48f, 0.44f, 0.42f, towerGlass);

                // 6. All 20 Pune EV Stations from StationFinderFrame
                List<Station> stations = StationFinderFrame.getAllStations();
                Color nodeColor = new Color(56, 189, 248);
                for (Station s : stations) {
                    if (s.name.equalsIgnoreCase(targetStation.name)) continue;
                    float sx = lonToWorldX(s.y);
                    float sz = latToWorldZ(s.x);
                    drawBox(sx, 0.08f, sz, 0.16f, 0.16f, 0.16f, new Color(25, 48, 75));
                    drawSphere(sx, 0.20f, sz, 0.08f, 0.08f, 0.08f, nodeColor);
                }

                // 7. Highlighted Active Navigation Corridor & Animated Flow Dashes
                for (int i = 0; i < routeWaypoints.size() - 1; i++) {
                    float[] a = routeWaypoints.get(i);
                    float[] b = routeWaypoints.get(i + 1);

                    // Outer route glow + asphalt base
                    drawGroundSegment(a[0], a[1], b[0], b[1], 0.028f, 0.40f, new Color(22, 48, 82));
                    drawGroundSegment(a[0], a[1], b[0], b[1], 0.034f, 0.30f, new Color(34, 66, 108));

                    float deltaX = b[0] - a[0];
                    float deltaZ = b[1] - a[1];
                    float segmentLength = (float) Math.hypot(deltaX, deltaZ);
                    if (segmentLength == 0) continue;

                    float phase = (animTick % 10) * 0.044f;
                    for (float offset = phase; offset < segmentLength; offset += 0.44f) {
                        float endOffset = Math.min(offset + 0.24f, segmentLength);
                        drawGroundSegment(
                                a[0] + deltaX * offset / segmentLength,
                                a[1] + deltaZ * offset / segmentLength,
                                a[0] + deltaX * endOffset / segmentLength,
                                a[1] + deltaZ * endOffset / segmentLength,
                                0.050f,
                                0.080f,
                                primaryColor);
                    }
                }

                // 8. Origin Beacon (Red) & Target Charging Hub Kiosk (Green)
                float[] startPt = routeWaypoints.get(0);
                float[] endPt = routeWaypoints.get(routeWaypoints.size() - 1);

                drawBox(startPt[0], 0.04f, startPt[1], 0.38f, 0.03f, 0.38f, new Color(127, 29, 29));
                drawSphere(startPt[0], 0.20f, startPt[1], 0.17f, 0.17f, 0.17f, new Color(239, 68, 68));

                // Target Station Pad, Supercharger Kiosk & Pulsing Green Beacon
                drawBox(endPt[0], 0.04f, endPt[1], 0.48f, 0.03f, 0.48f, new Color(20, 83, 45));
                drawBox(endPt[0] + 0.30f, 0.40f, endPt[1] - 0.24f, 0.32f, 0.76f, 0.28f, new Color(45, 68, 92));
                drawBox(endPt[0] + 0.30f, 0.52f, endPt[1] - 0.09f, 0.20f, 0.25f, 0.03f, primaryColor);
                float pulseScale = (animTick % 2 == 0) ? 0.21f : 0.18f;
                drawSphere(endPt[0], 0.22f, endPt[1], pulseScale, pulseScale, pulseScale, accentGreen);

                // 9. Animated EV Car along the extended Pune route
                if (isDriving || isParked) {
                    float[] carPose = getInterpolatedCarPosition();
                    glPushMatrix();
                    glTranslatef(carPose[0], 0.045f, carPose[1]);
                    glScalef(0.68f, 0.68f, 0.68f);
                    drawElectricCar(0, 0, 0, carPose[2], new Color(250, 204, 21));
                    glPopMatrix();
                }

                glPopMatrix();
            }
        };
        mapCard.setLayout(null);
        mapCard.setOpaque(false);
        mapCard.setBounds(20, 90, 865, 235);
        mapCard.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        JPanel statusPill = new JPanel();
        statusPill.setBounds(15, 12, 455, 30);
        statusPill.setBackground(new Color(15, 23, 42));
        statusPill.setBorder(BorderFactory.createLineBorder(primaryColor, 1));
        statusPill.setLayout(null);

        JLabel lblNavIcon = new JLabel("LIVE GIS");
        lblNavIcon.setBounds(10, 5, 52, 20);
        lblNavIcon.setForeground(accentGreen);
        lblNavIcon.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusPill.add(lblNavIcon);

        lblStatus = new JLabel("Ready: " + originArea + " -> " + stationName);
        lblStatus.setBounds(66, 5, 380, 20);
        lblStatus.setForeground(textColor);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusPill.add(lblStatus);
        mapCard.add(statusPill);

        JPanel distBadge = new JPanel();
        distBadge.setBounds(655, 12, 195, 30);
        distBadge.setBackground(new Color(15, 23, 42));
        distBadge.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        distBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 5));
        int estMins = Math.max(4, (int) Math.round(calculatedDistanceKm * 2.2));
        lblEtaBadge = new JLabel(String.format("%.1f km  |  ~%d min", calculatedDistanceKm, estMins));
        lblEtaBadge.setForeground(primaryColor);
        lblEtaBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        distBadge.add(lblEtaBadge);
        mapCard.add(distBadge);

        JPanel legendBadge = new JPanel();
        legendBadge.setBounds(15, 198, 545, 25);
        legendBadge.setBackground(new Color(15, 23, 42));
        legendBadge.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        legendBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 3));
        JLabel lblLegend = new JLabel("[Red] Start   [Green] Destination   [Cyan] 20 Pune EV Hubs (Hinjewadi, Shivajinagar, Baner, Alandi)");
        lblLegend.setForeground(secondaryText);
        lblLegend.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        legendBadge.add(lblLegend);
        mapCard.add(legendBadge);

        mapCard.sendCanvasToBack();
        mainPanel.add(mapCard);

        // Camera Card (Upgraded Bay #01 Docking Feed)
        OpenGLPanel cameraCard = new OpenGLPanel() {
            @Override
            protected void renderOpenGL(int width, int height) {
                begin3D(width, height, 70, 10.5f);
                glRotatef(16, 1, 0, 0);
                glPushMatrix();

                // Bay Floor, Safety Striping & Overhead Gantry Pillar
                drawBox(0, -0.25f, 0, 12, 0.45f, 5.6f, new Color(24, 34, 52));
                drawGroundSegment(-5.7f, -1.5f, 5.7f, -1.5f, 0.005f, 0.06f, new Color(250, 204, 21));
                drawGroundSegment(-5.7f, 1.5f, 5.7f, 1.5f, 0.005f, 0.06f, new Color(250, 204, 21));
                drawGroundSegment(-2.8f, -1.5f, -2.8f, 1.5f, 0.01f, 0.05f, new Color(71, 85, 105));

                // Supercharger Dispenser Cabinet + Top Status Bar + Screen
                drawBox(3.35f, 0.85f, 0, 0.64f, 1.75f, 0.74f, new Color(51, 68, 92));
                drawBox(3.35f, 1.05f, 0.38f, 0.42f, 0.50f, 0.035f, new Color(11, 17, 32));
                drawBox(3.35f, 1.68f, 0, 0.66f, 0.08f, 0.76f, primaryColor);

                Color ledColor = isParked && chargeProgress < 100
                        ? (ledBlinkState ? accentGreen : new Color(22, 101, 52))
                        : chargeProgress >= 100
                                ? accentGreen
                                : new Color(100, 116, 139);
                drawSphere(3.35f, 0.40f, 0.40f, 0.095f, 0.095f, 0.05f, ledColor);

                float carX = -2.0f + (carParkX + 80) / 125.0f * 2.6f;
                drawElectricCar(carX, 0.02f, 0, 90, primaryColor);

                if (isParked) {
                    drawWorldLine(carX + 0.78f, 0.82f, 0, 2.95f, 0.65f, 0,
                            new Color(250, 204, 21), 3);
                }

                glPopMatrix();
            }
        };
        cameraCard.setLayout(null);
        cameraCard.setOpaque(false);
        cameraCard.setBounds(20, 335, 865, 125);
        cameraCard.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        JPanel camBadge = new JPanel();
        camBadge.setBounds(12, 8, 460, 24);
        camBadge.setBackground(new Color(15, 23, 42));
        camBadge.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        camBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 3));
        JLabel lblCamTitle = new JLabel("REC  |  " + targetStation.name + " (" + targetStation.location + ") - Bay #01");
        lblCamTitle.setForeground(primaryColor);
        lblCamTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        camBadge.add(lblCamTitle);
        cameraCard.add(camBadge);
        cameraCard.sendCanvasToBack();
        mainPanel.add(cameraCard);

        // Control Panel
        JPanel controlCard = new JPanel();
        controlCard.setLayout(null);
        controlCard.setBounds(20, 470, 865, 115);
        controlCard.setBackground(cardColor);
        controlCard.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        JLabel lblCtrlTitle = new JLabel("MISSION CONTROL PANEL");
        lblCtrlTitle.setBounds(15, 10, 250, 20);
        lblCtrlTitle.setForeground(primaryColor);
        lblCtrlTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        controlCard.add(lblCtrlTitle);

        btnStartDrive = new JButton("Start Drive Route");
        btnStartDrive.setBounds(15, 36, 410, 46);
        btnStartDrive.setBackground(primaryColor);
        btnStartDrive.setForeground(bgColor);
        btnStartDrive.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnStartDrive.setFocusPainted(false);
        controlCard.add(btnStartDrive);

        btnStopCharging = new JButton("Stop Charging & Bill");
        btnStopCharging.setBounds(440, 36, 410, 46);
        btnStopCharging.setBackground(new Color(225, 29, 72));
        btnStopCharging.setForeground(textColor);
        btnStopCharging.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnStopCharging.setFocusPainted(false);
        btnStopCharging.setEnabled(false);
        controlCard.add(btnStopCharging);

        mainPanel.add(controlCard);

        // Telemetry Card with Live Battery Progress Bar
        JPanel telemetryCard = new JPanel();
        telemetryCard.setLayout(null);
        telemetryCard.setBounds(20, 595, 865, 115);
        telemetryCard.setBackground(cardColor);
        telemetryCard.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        JLabel lblTelemTitle = new JLabel("REAL-TIME ENERGY TELEMETRY");
        lblTelemTitle.setBounds(15, 10, 250, 20);
        lblTelemTitle.setForeground(primaryColor);
        lblTelemTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        telemetryCard.add(lblTelemTitle);

        lblTelemetryStatus = new JLabel("IDLE / READY");
        lblTelemetryStatus.setBounds(745, 10, 105, 22);
        lblTelemetryStatus.setForeground(secondaryText);
        lblTelemetryStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        telemetryCard.add(lblTelemetryStatus);

        lblBatteryPct = new JLabel(chargeProgress + "%");
        lblBatteryPct.setBounds(15, 32, 95, 35);
        lblBatteryPct.setForeground(textColor);
        lblBatteryPct.setFont(new Font("Segoe UI", Font.BOLD, 28));
        telemetryCard.add(lblBatteryPct);

        batteryBar = new JProgressBar(0, 100);
        batteryBar.setValue(chargeProgress);
        batteryBar.setBounds(120, 42, 580, 18);
        batteryBar.setBackground(new Color(15, 23, 42));
        batteryBar.setForeground(accentGreen);
        batteryBar.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        telemetryCard.add(batteryBar);

        JLabel lblCap = new JLabel("Lithium-Ion High-Density Battery Pack  |  CCS Type-2 DC Fast Port");
        lblCap.setBounds(15, 70, 420, 18);
        lblCap.setForeground(secondaryText);
        lblCap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        telemetryCard.add(lblCap);

        lblPowerKw = new JLabel(powerIntake + " kW");
        lblPowerKw.setBounds(755, 42, 95, 25);
        lblPowerKw.setForeground(accentGreen);
        lblPowerKw.setFont(new Font("Segoe UI", Font.BOLD, 18));
        telemetryCard.add(lblPowerKw);

        JLabel lblKwLabel = new JLabel("Power Intake Rate");
        lblKwLabel.setBounds(745, 70, 105, 18);
        lblKwLabel.setForeground(secondaryText);
        lblKwLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        telemetryCard.add(lblKwLabel);

        mainPanel.add(telemetryCard);

        btnStartDrive.addActionListener(e -> {
            btnStartDrive.setEnabled(false);
            btnStopCharging.setEnabled(true);
            isDriving = true;
            lblTelemetryStatus.setText("NAVIGATING");
            lblTelemetryStatus.setForeground(primaryColor);
            startSimulationEngine(mapCard, cameraCard);
        });

        btnStopCharging.addActionListener(e -> {
            if (simulationTimer != null) simulationTimer.stop();
            finalizeAndShowBill("User Manually Stopped Charging Session.");
        });

        btnDiagnostics.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "=== HARDWARE DIAGNOSTICS LOG ===\n\n" +
                "Hub Area: " + targetStation.location + ", Pune (" + targetStation.availableSlots + " Slots)\n" +
                "Dispenser Voltage: 415V AC (Stable)\n" +
                "Thermal Sensor: 34°C (Normal)\n" +
                "CCS Type 2 Latch: ENGAGED\n" +
                "COA Tariff Signal: ACTIVE (Rs." + targetStation.ratePerKwh + "/kWh)\n" +
                "Grid Frequency: 50.02 Hz",
                "Hardware Status Monitor", JOptionPane.INFORMATION_MESSAGE);
        });

        add(mainPanel);
    }

    private float lonToWorldX(double lon) {
        double norm = (lon - MIN_LON) / (MAX_LON - MIN_LON);
        return (float) (-5.6 + norm * 11.2);
    }

    private float latToWorldZ(double lat) {
        double norm = (lat - MIN_LAT) / (MAX_LAT - MIN_LAT);
        return (float) (2.8 - norm * 5.6);
    }

    private void buildPuneNavigationRoute() {
        routeWaypoints.clear();
        double[] originLatLon = StationFinderFrame.getOriginLatLon(originArea);

        if (Math.hypot(originLatLon[0] - targetStation.x, originLatLon[1] - targetStation.y) < 0.008) {
            originLatLon = new double[]{18.53, 73.85};
            if (Math.hypot(originLatLon[0] - targetStation.x, originLatLon[1] - targetStation.y) < 0.008) {
                originLatLon = new double[]{18.56, 73.78};
            }
        }

        float startX = lonToWorldX(originLatLon[1]);
        float startZ = latToWorldZ(originLatLon[0]);
        float endX = lonToWorldX(targetStation.y);
        float endZ = latToWorldZ(targetStation.x);

        float[] wakadNode = {lonToWorldX(73.76), latToWorldZ(18.59)};
        float[] banerNode = {lonToWorldX(73.78), latToWorldZ(18.56)};
        float[] aundhNode = {lonToWorldX(73.80), latToWorldZ(18.55)};
        float[] shivajinagarNode = {lonToWorldX(73.85), latToWorldZ(18.53)};
        float[] yerwadaNode = {lonToWorldX(73.88), latToWorldZ(18.55)};

        routeWaypoints.add(new float[]{startX, startZ});

        if (startX < -2.2f || endX < -2.2f) {
            if (startX < -3.5f || endX < -3.5f) {
                routeWaypoints.add(wakadNode);
            }
            routeWaypoints.add(banerNode);
            routeWaypoints.add(aundhNode);
        }

        if (Math.abs(endX - startX) > 1.8f || Math.abs(endZ - startZ) > 1.4f) {
            routeWaypoints.add(shivajinagarNode);
        }

        if (startX > 2.0f || endX > 2.0f || startZ < -1.5f || endZ < -1.5f) {
            routeWaypoints.add(yerwadaNode);
        }

        routeWaypoints.add(new float[]{endX, endZ});
    }

    private double computeRouteDistanceKm() {
        double[] originLatLon = StationFinderFrame.getOriginLatLon(originArea);
        double directKm = Math.hypot(targetStation.x - originLatLon[0], targetStation.y - originLatLon[1]) * 100.0;
        if (directKm < 1.5) directKm = 3.2;
        return directKm;
    }

    private float[] getInterpolatedCarPosition() {
        int segCount = routeWaypoints.size() - 1;
        if (segCount <= 0) {
            return new float[]{0f, 0f, 0f};
        }

        double[] segmentLengths = new double[segCount];
        double totalDistance = 0;
        for (int i = 0; i < segCount; i++) {
            float[] a = routeWaypoints.get(i);
            float[] b = routeWaypoints.get(i + 1);
            segmentLengths[i] = Math.hypot(b[0] - a[0], b[1] - a[1]);
            totalDistance += segmentLengths[i];
        }

        double remainingDistance = carRouteProgress * totalDistance;
        int segmentIndex = segCount - 1;
        for (int i = 0; i < segCount; i++) {
            if (remainingDistance <= segmentLengths[i] || i == segCount - 1) {
                segmentIndex = i;
                break;
            }
            remainingDistance -= segmentLengths[i];
        }

        float segmentProgress = segmentLengths[segmentIndex] == 0 ? 1.0f
                : (float) Math.min(1.0, remainingDistance / segmentLengths[segmentIndex]);
        float[] sPt = routeWaypoints.get(segmentIndex);
        float[] ePt = routeWaypoints.get(segmentIndex + 1);

        float deltaX = ePt[0] - sPt[0];
        float deltaZ = ePt[1] - sPt[1];
        float carX = sPt[0] + deltaX * segmentProgress;
        float carZ = sPt[1] + deltaZ * segmentProgress;
        float heading = (float) Math.toDegrees(Math.atan2(deltaX, deltaZ));

        return new float[]{carX, carZ, heading};
    }

    private void startSimulationEngine(OpenGLPanel mapCard, OpenGLPanel cameraCard) {
        simulationTimer = new Timer(150, e -> {
            animTick++;
            if (carRouteProgress < 1.0) {
                carRouteProgress = Math.min(1.0, carRouteProgress + 0.012);
                int pct = (int) (carRouteProgress * 100);
                double remKm = Math.max(0.0, calculatedDistanceKm * (1.0 - carRouteProgress));
                int remMins = Math.max(1, (int) Math.round(remKm * 2.2));
                lblStatus.setText("En Route to " + targetStation.name + " (" + pct + "%)");
                lblEtaBadge.setText(String.format("%.1f km  |  ~%d min", remKm, remMins));
                mapCard.requestRender();
            } else if (!isParked) {
                lblEtaBadge.setText("0.0 km  |  ARRIVED");
                lblEtaBadge.setForeground(accentGreen);
                if (carParkX < 45) {
                    carParkX += 5;
                    lblStatus.setText("Arrived at " + targetStation.name + ". Parking in Bay #01...");
                    cameraCard.requestRender();
                } else {
                    isParked = true;
                    lblStatus.setText("Docked! Establishing hardware communication...");
                    powerIntake = 45.2;
                    lblPowerKw.setText(powerIntake + " kW");
                    lblTelemetryStatus.setText("CHARGING");
                    lblTelemetryStatus.setForeground(accentGreen);
                }
            } else if (chargeProgress < 100) {
                chargeProgress = Math.min(100, chargeProgress + 1);
                ledBlinkState = !ledBlinkState;
                lblBatteryPct.setText(chargeProgress + "%");
                batteryBar.setValue(chargeProgress);
                lblStatus.setText("Fast Charging Active... (" + chargeProgress + "%)");
                mapCard.requestRender();
                cameraCard.requestRender();
            } else {
                simulationTimer.stop();
                powerIntake = 0.0;
                lblPowerKw.setText("0.0 kW");
                lblTelemetryStatus.setText("COMPLETE");
                lblTelemetryStatus.setForeground(primaryColor);
                lblStatus.setText("Charging Completed 100%! Session finalized.");
                cameraCard.requestRender();

                finalizeAndShowBill("Charging Completed 100% Successfully.");
            }
        });
        simulationTimer.start();
    }

    private void finalizeAndShowBill(String statusMessage) {
        int chargedPercentage = Math.max(0, chargeProgress - initialBattery);
        double kwhConsumed = (chargedPercentage / 100.0) * 25.0;
        double energyCost = kwhConsumed * targetStation.ratePerKwh;
        double gst = energyCost * 0.05;
        double totalBill = energyCost + gst;

        double distAdded = kwhConsumed * 6.5;
        double co2Added = distAdded * 0.12;
        double fuelAdded = distAdded * 7.0;

        String sanitizedEmail = userEmail.replace(".", "_");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/" + sanitizedEmail);

        userRef.child("batteryPercentage").setValueAsync(chargeProgress);

        userRef.child("ecoStats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double currentCo2 = 124.5;
                double currentFuel = 4250.0;
                double currentDist = 820.0;
                if (snapshot.exists()) {
                    if (snapshot.child("co2").getValue(Double.class) != null)
                        currentCo2 = snapshot.child("co2").getValue(Double.class);
                    if (snapshot.child("fuel").getValue(Double.class) != null)
                        currentFuel = snapshot.child("fuel").getValue(Double.class);
                    if (snapshot.child("distance").getValue(Double.class) != null)
                        currentDist = snapshot.child("distance").getValue(Double.class);
                }
                Map<String, Object> ecoMap = new HashMap<>();
                ecoMap.put("co2", currentCo2 + co2Added);
                ecoMap.put("fuel", currentFuel + fuelAdded);
                ecoMap.put("distance", currentDist + distAdded);
                userRef.child("ecoStats").setValueAsync(ecoMap);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        JOptionPane.showMessageDialog(this,
            "<html><body style='width: 340px; font-family: Segoe UI;'>" +
            "<h2 style='color: #38BDF8;'>VoltPulse 1.0 - Invoice Summary</h2>" +
            "<hr/>" +
            "<b>Status:</b> " + statusMessage + "<br/>" +
            "<b>Station:</b> " + targetStation.name + " (" + targetStation.location + ")<br/>" +
            "<b>Battery Charged:</b> " + initialBattery + "% -> " + chargeProgress + "% (" + chargedPercentage + "%)<br/>" +
            "<b>Energy Delivered:</b> " + String.format("%.2f", kwhConsumed) + " kWh<br/>" +
            "<b>Base Rate:</b> Rs." + String.format("%.2f", targetStation.ratePerKwh) + "/kWh<br/>" +
            "<b>Energy Cost:</b> Rs." + String.format("%.2f", energyCost) + "<br/>" +
            "<b>GST (5%):</b> Rs." + String.format("%.2f", gst) + "<br/>" +
            "<b><font color='#38BDF8'>Total Paid (QR Gateway):</font></b> Rs." + String.format("%.2f", totalBill) + "<br/>" +
            "<hr/>" +
            "<i>Eco-Impact & Battery Updated Successfully! Click OK to return to Dashboard.</i>" +
            "</body></html>",
            "Enterprise Invoice & Bill", JOptionPane.INFORMATION_MESSAGE);

        dispose();
        new DashboardFrame(userEmail).setVisible(true);
    }
}