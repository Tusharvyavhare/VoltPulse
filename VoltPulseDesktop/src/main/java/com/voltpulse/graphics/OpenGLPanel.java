package com.voltpulse.graphics;

import java.awt.Color;
import java.awt.Insets;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBEasyFont.stb_easy_font_print;

public abstract class OpenGLPanel extends JPanel {

    private final AWTGLCanvas canvas;
    //configures opengl context settings
    protected OpenGLPanel() {
        setLayout(null);
        setOpaque(true);

        GLData data = new GLData();
        data.doubleBuffer = true;
        data.depthSize = 24;// double buffering to prevent screen flicker and a 24 bit depth
        canvas = new AWTGLCanvas(data) {
            @Override
            public void initGL() {
                GL.createCapabilities();// blinds LWJGL java opengl functions to active native driver
            }

            @Override
            public void paintGL() {//by defalut configures 2d orthogrtaphics projection, enables alpha blending for smooth transperancy clears the screen 
                int width = Math.max(1, getFramebufferWidth());
                int height = Math.max(1, getFramebufferHeight());

                glViewport(0, 0, width, height);
                glMatrixMode(GL_PROJECTION);
                glLoadIdentity();
                glOrtho(0, width, height, 0, -1, 1);
                glMatrixMode(GL_MODELVIEW);
                glLoadIdentity();
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glClearColor(0, 0, 0, 0);
                glClear(GL_COLOR_BUFFER_BIT);

                renderOpenGL(width, height);
                swapBuffers();
            }
        };

        add(canvas);
    }

    protected abstract void renderOpenGL(int width, int height);// forces sublclasses to implement their custom drawing commands

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(this::requestRender);
    }

    public final void requestRender() {
        if (canvas.isValid()) {
            canvas.render();
        }
    }

    public final void sendCanvasToBack() {
        setComponentZOrder(canvas, getComponentCount() - 1);
    }

    @Override
    public void doLayout() {
        Insets insets = getInsets();
        canvas.setBounds(
                insets.left,
                insets.top,
                Math.max(0, getWidth() - insets.left - insets.right),
                Math.max(0, getHeight() - insets.top - insets.bottom));
    }

    protected final void setDrawColor(Color color) {//RGB or alpha  integrs into normalized OPengl floats
        glColor4f(
                color.getRed() / 255.0f,
                color.getGreen() / 255.0f,
                color.getBlue() / 255.0f,
                color.getAlpha() / 255.0f);
    }

    protected final void fillRect(float x, float y, float width, float height, Color color) {// draw solid filled rectangles for chart backgrounds and outlined rectangles for borders
        setDrawColor(color);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();
    }

    protected final void drawRect(float x, float y, float width, float height, Color color, float lineWidth) {
        setDrawColor(color);
        glLineWidth(lineWidth);
        glBegin(GL_LINE_LOOP);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();
    }
    // rnders solid or segmented dashed lines by stepping along the vector lengthin increments
   // line & polygon rasterization
    protected final void drawLine(
            float startX, float startY, float endX, float endY, Color color, float lineWidth) {
        setDrawColor(color);
        glLineWidth(lineWidth);
        glBegin(GL_LINES);
        glVertex2f(startX, startY);
        glVertex2f(endX, endY);
        glEnd();
    }

    protected final void drawDashedLine(
            float startX,
            float startY,
            float endX,
            float endY,
            Color color,
            float lineWidth,
            float dashLength,
            float gapLength) {
        float deltaX = endX - startX;
        float deltaY = endY - startY;
        float distance = (float) Math.hypot(deltaX, deltaY);
        if (distance == 0) {
            return;
        }

        float patternLength = dashLength + gapLength;
        setDrawColor(color);
        glLineWidth(lineWidth);
        glBegin(GL_LINES);
        for (float offset = 0; offset < distance; offset += patternLength) {
            float segmentEnd = Math.min(offset + dashLength, distance);
            glVertex2f(startX + deltaX * offset / distance, startY + deltaY * offset / distance);
            glVertex2f(
                    startX + deltaX * segmentEnd / distance,
                    startY + deltaY * segmentEnd / distance);
        }
        glEnd();
    }
    //filled 2d circle using a GL_TRIANGLE_FAN
    protected final void fillCircle(float centerX, float centerY, float radius, Color color) {
        setDrawColor(color);
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(centerX, centerY);
        for (int angle = 0; angle <= 360; angle += 10) {
            double radians = Math.toRadians(angle);
            glVertex2f(
                    centerX + (float) Math.cos(radians) * radius,
                    centerY + (float) Math.sin(radians) * radius);
        }
        glEnd();
    }
    // Bresenhams circle , midpoint circle drwaning 
    protected final void fillRoundRect(
            float x, float y, float width, float height, float radius, Color color) {
        float cornerRadius = Math.min(radius, Math.min(width, height) / 2.0f);
        float[] centerX = {x + cornerRadius, x + width - cornerRadius,
                x + width - cornerRadius, x + cornerRadius};
        float[] centerY = {y + cornerRadius, y + cornerRadius,
                y + height - cornerRadius, y + height - cornerRadius};
        int[] startAngle = {180, 270, 0, 90};

        setDrawColor(color);
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(x + width / 2.0f, y + height / 2.0f);
        for (int corner = 0; corner < 4; corner++) {
            for (int step = 0; step <= 8; step++) {
                double radians = Math.toRadians(startAngle[corner] + step * 90.0 / 8.0);
                glVertex2f(
                        centerX[corner] + (float) Math.cos(radians) * cornerRadius,
                        centerY[corner] + (float) Math.sin(radians) * cornerRadius);
            }
        }
        glVertex2f(x, y + cornerRadius);
        glEnd();
    }

    protected final void drawText(float x, float y, String text, Color color, float scale) {
        int bufferSize = Math.max(1, text.length() * 270);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer vertices = stack.malloc(bufferSize);
            int quadCount = stb_easy_font_print(0, 0, text, null, vertices);
            if (quadCount == 0) {
                return;
            }

            glPushMatrix();
            glTranslatef(x, y, 0);
            glScalef(scale, scale, 1);
            setDrawColor(color);
            glEnableClientState(GL_VERTEX_ARRAY);
            glVertexPointer(2, GL_FLOAT, 16, vertices);
            glDrawArrays(GL_QUADS, 0, quadCount * 4);
            glDisableClientState(GL_VERTEX_ARRAY);
            glPopMatrix();
        }
    }
    // switches the canvas from 2D mode into a full 3D perspective scne
    protected final void begin3D(int width, int height, float horizontalFov, float cameraDistance) {
        float aspect = (float) width / height;
        float nearPlane = 0.1f;
        float halfWidth = nearPlane * (float) Math.tan(Math.toRadians(horizontalFov / 2.0));
        float halfHeight = halfWidth / aspect;

        glViewport(0, 0, width, height);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glFrustum(-halfWidth, halfWidth, -halfHeight, halfHeight, nearPlane, 100);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glClearColor(13 / 255.0f, 19 / 255.0f, 33 / 255.0f, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glDisable(GL_BLEND);
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
        glEnable(GL_NORMALIZE);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer ambient = stack.floats(0.35f, 0.39f, 0.48f, 1);
            FloatBuffer diffuse = stack.floats(0.9f, 0.92f, 1, 1);
            FloatBuffer position = stack.floats(-3, 7, 8, 0);
            glLightfv(GL_LIGHT0, GL_AMBIENT, ambient);
            glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuse);
            glLightfv(GL_LIGHT0, GL_POSITION, position);
        }

        glTranslatef(0, 0, -cameraDistance);
    }
    //this constructs a 3D rectangular prism centered by emitting 6 gl_quads faces,
    protected final void drawBox(
            float centerX,
            float centerY,
            float centerZ,
            float width,
            float height,
            float depth,
            Color color) {
        float x = width / 2;
        float y = height / 2;
        float z = depth / 2;

        setDrawColor(color);
        glBegin(GL_QUADS);
        glNormal3f(0, 0, 1);
        glVertex3f(centerX - x, centerY - y, centerZ + z);
        glVertex3f(centerX + x, centerY - y, centerZ + z);
        glVertex3f(centerX + x, centerY + y, centerZ + z);
        glVertex3f(centerX - x, centerY + y, centerZ + z);

        glNormal3f(0, 0, -1);
        glVertex3f(centerX + x, centerY - y, centerZ - z);
        glVertex3f(centerX - x, centerY - y, centerZ - z);
        glVertex3f(centerX - x, centerY + y, centerZ - z);
        glVertex3f(centerX + x, centerY + y, centerZ - z);

        glNormal3f(1, 0, 0);
        glVertex3f(centerX + x, centerY - y, centerZ + z);
        glVertex3f(centerX + x, centerY - y, centerZ - z);
        glVertex3f(centerX + x, centerY + y, centerZ - z);
        glVertex3f(centerX + x, centerY + y, centerZ + z);

        glNormal3f(-1, 0, 0);
        glVertex3f(centerX - x, centerY - y, centerZ - z);
        glVertex3f(centerX - x, centerY - y, centerZ + z);
        glVertex3f(centerX - x, centerY + y, centerZ + z);
        glVertex3f(centerX - x, centerY + y, centerZ - z);

        glNormal3f(0, 1, 0);
        glVertex3f(centerX - x, centerY + y, centerZ + z);
        glVertex3f(centerX + x, centerY + y, centerZ + z);
        glVertex3f(centerX + x, centerY + y, centerZ - z);
        glVertex3f(centerX - x, centerY + y, centerZ - z);

        glNormal3f(0, -1, 0);
        glVertex3f(centerX - x, centerY - y, centerZ - z);
        glVertex3f(centerX + x, centerY - y, centerZ - z);
        glVertex3f(centerX + x, centerY - y, centerZ + z);
        glVertex3f(centerX - x, centerY - y, centerZ + z);
        glEnd();
    }

    protected final void drawSphere(
            float centerX,
            float centerY,
            float centerZ,
            float radiusX,
            float radiusY,
            float radiusZ,
            Color color) {
        setDrawColor(color);
        int slices = 16;
        int stacks = 10;
        for (int stackIndex = 0; stackIndex < stacks; stackIndex++) {
            double lowerLatitude = -Math.PI / 2 + Math.PI * stackIndex / stacks;
            double upperLatitude = -Math.PI / 2 + Math.PI * (stackIndex + 1) / stacks;
            glBegin(GL_QUAD_STRIP);
            for (int sliceIndex = 0; sliceIndex <= slices; sliceIndex++) {
                double longitude = 2 * Math.PI * sliceIndex / slices;
                addSphereVertex(centerX, centerY, centerZ, radiusX, radiusY, radiusZ,
                        lowerLatitude, longitude);
                addSphereVertex(centerX, centerY, centerZ, radiusX, radiusY, radiusZ,
                        upperLatitude, longitude);
            }
            glEnd();
        }
    }

    private void addSphereVertex(
            float centerX,
            float centerY,
            float centerZ,
            float radiusX,
            float radiusY,
            float radiusZ,
            double latitude,
            double longitude) {
        float normalX = (float) (Math.cos(latitude) * Math.cos(longitude));
        float normalY = (float) Math.sin(latitude);
        float normalZ = (float) (Math.cos(latitude) * Math.sin(longitude));
        glNormal3f(normalX, normalY, normalZ);
        glVertex3f(
                centerX + normalX * radiusX,
                centerY + normalY * radiusY,
                centerZ + normalZ * radiusZ);
    }

    protected final void drawGroundSegment(
            float startX,
            float startZ,
            float endX,
            float endZ,
            float y,
            float width,
            Color color) {
        float deltaX = endX - startX;
        float deltaZ = endZ - startZ;
        float distance = (float) Math.hypot(deltaX, deltaZ);
        if (distance == 0) {
            return;
        }

        float perpendicularX = -deltaZ / distance * width / 2;
        float perpendicularZ = deltaX / distance * width / 2;
        setDrawColor(color);
        glNormal3f(0, 1, 0);
        glBegin(GL_QUADS);
        glVertex3f(startX + perpendicularX, y, startZ + perpendicularZ);
        glVertex3f(startX - perpendicularX, y, startZ - perpendicularZ);
        glVertex3f(endX - perpendicularX, y, endZ - perpendicularZ);
        glVertex3f(endX + perpendicularX, y, endZ + perpendicularZ);
        glEnd();
    }

    protected final void drawWorldLine(
            float startX,
            float startY,
            float startZ,
            float endX,
            float endY,
            float endZ,
            Color color,
            float lineWidth) {
        glDisable(GL_LIGHTING);
        setDrawColor(color);
        glLineWidth(lineWidth);
        glBegin(GL_LINES);
        glVertex3f(startX, startY, startZ);
        glVertex3f(endX, endY, endZ);
        glEnd();
        glEnable(GL_LIGHTING);
    }
    // 3D vehicle model thst isolates transformtions
    protected final void drawElectricCar(
            float centerX, float groundY, float centerZ, float headingDegrees, Color bodyColor) {
        glPushMatrix();// moves and rotate the car
        glTranslatef(centerX, groundY, centerZ);//(transaltion)
        glRotatef(headingDegrees, 0, 1, 0);//(rotation)

        drawBox(0, 0.34f, 0, 0.88f, 0.38f, 1.72f, bodyColor);
        drawBox(0, 0.64f, -0.12f, 0.76f, 0.32f, 0.94f, new Color(14, 116, 144));
        drawBox(0, 0.65f, 0.38f, 0.66f, 0.20f, 0.30f, new Color(125, 211, 252));

        Color tireColor = new Color(12, 18, 30);
        drawSphere(-0.47f, 0.17f, -0.52f, 0.15f, 0.17f, 0.12f, tireColor);
        drawSphere(0.47f, 0.17f, -0.52f, 0.15f, 0.17f, 0.12f, tireColor);
        drawSphere(-0.47f, 0.17f, 0.52f, 0.15f, 0.17f, 0.12f, tireColor);
        drawSphere(0.47f, 0.17f, 0.52f, 0.15f, 0.17f, 0.12f, tireColor);

        drawBox(-0.26f, 0.37f, 0.87f, 0.18f, 0.10f, 0.035f, new Color(254, 240, 138));
        drawBox(0.26f, 0.37f, 0.87f, 0.18f, 0.10f, 0.035f, new Color(254, 240, 138));
        drawBox(0, 0.29f, -0.87f, 0.42f, 0.09f, 0.035f, new Color(239, 68, 68));

        glPopMatrix();
    }
}
