package main;


import lwjglutils.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import transforms.*;

import java.io.IOException;
import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer extends AbstractRenderer {

    double ox, oy;
    boolean mouseButton1, mouseButton2, wireframe = false;
    boolean persp = true;

    OGLBuffers buffers;
    OGLTexture2D texture;
    OGLTexture.Viewer textureView;
    OGLRenderTarget renderTarget;

    private double oldMx, oldMy;
    private boolean mousePressed;

    //promenne pro reflektorovy zdroj svetla
    private int spotlight = 0;
    private int locSpotlight;

    //promenne pro okno
    int width, height;

    int locTime, locTimeLight, locMathModelView, locMathViewView, locMathProjView, shaderProgramLight, shaderProgramView,
            locMathModelLight, locMathViewLight, locMathProjLight, locLightPos, locViewPos,
            locMathMVPLight, locObjectType, locLightModelType, locLightModelTypeForLight, locObjectTypeForLight, locEyePosition, locColorType;
    String barva, osvetleni;

    float rotace1 = 0;
    float rotace2 = 0;
    float time = 0;
    Vec3D light = new Vec3D();
    Mat4 matMVPLight = new Mat4Identity();

    Camera camera = new Camera();
    Mat4 projection = new Mat4PerspRH(Math.PI / 4, 1, 0.01, 100.0);

    int lightValue = 0;
    int colorValue = 0;



    private int locSceneView, locSceneProjection, locSceneTemp;


    private OGLTexture2D texture1;
    private OGLTexture2D.Viewer viewer;

    // nastaveni resizovani okna
    private GLFWWindowSizeCallback wsCallback = new GLFWWindowSizeCallback() {
        @Override
        public void invoke(long window, int w, int h) {
            if (w > 0 && h > 0 &&
                    (w != width || h != height)) {
                width = w;
                height = h;
                projection = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 100.0);
                if (textRenderer != null)
                    textRenderer.resize(width, height);
            }
        }
    };

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();

        // nastaveni barvy clear color
        glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
        // zavolani vytvoreni gridu (trida GridUtil)
        buffers = GridFactory.createGrid(200, 200);

        // nacteni shader programu z resources casti projektu
        shaderProgramView = ShaderUtils.loadProgram("/start.vert",
                "/start.frag",
                null, null, null, null);
        shaderProgramLight = ShaderUtils.loadProgram("/light.vert",
                "/light.frag",
                null, null, null, null);

        glUseProgram(this.shaderProgramLight);

        // nacteni textury z resources casti projektu
        try {
            texture = new OGLTexture2D("textures/mosaic.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }


        locMathModelLight = glGetUniformLocation(shaderProgramLight, "model");
        locMathViewLight = glGetUniformLocation(shaderProgramLight, "view");
        locMathProjLight = glGetUniformLocation(shaderProgramLight, "proj");
        locObjectTypeForLight = glGetUniformLocation(shaderProgramLight, "objectType");
        locTimeLight = glGetUniformLocation(shaderProgramLight, "time");
        locLightModelTypeForLight = glGetUniformLocation(shaderProgramLight, "lightModelType");
        locMathModelView = glGetUniformLocation(shaderProgramView, "model");
        locMathViewView = glGetUniformLocation(shaderProgramView, "view");
        locMathProjView = glGetUniformLocation(shaderProgramView, "proj");
        locLightPos = glGetUniformLocation(shaderProgramView, "lightPos");
        locEyePosition = glGetUniformLocation(shaderProgramView, "eyePosition");
        locMathMVPLight = glGetUniformLocation(shaderProgramView, "matMVPLight");
        locLightModelType = glGetUniformLocation(shaderProgramView, "lightModelType");
        locColorType = glGetUniformLocation(shaderProgramView, "colorType");
        locObjectType = glGetUniformLocation(shaderProgramView, "objectType");
        locTime = glGetUniformLocation(shaderProgramView, "time");
        locViewPos = glGetUniformLocation(shaderProgramView, "viewPos");
        locSpotlight = glGetUniformLocation(shaderProgramView, "spotlight");

        textRenderer = new OGLTextRenderer(width, height);

        camera = camera.withPosition(new Vec3D(10, 10, 6))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125);

        textureView = new OGLTexture2D.Viewer();
        renderTarget = new OGLRenderTarget(1024, 1024);
        viewer = new OGLTexture2D.Viewer();



        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        textRenderer = new OGLTextRenderer(LwjglWindow.WIDTH, LwjglWindow.HEIGHT);


        try {
            texture1 = new OGLTexture2D("./textures/mosaic.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    // vykreslení scény
    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);
        glLineWidth(5);
        time += 0.03;
        // "podrzeni" sceny
        if (!mouseButton1)
            rotace1 += 0.03;
        if (!mouseButton2)
            rotace2 += 0.03;

        //----------------------------------------------------From Light
        renderTarget.bind();
        glClearColor(0.1f, 0.5f, 0.1f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUniform1i(locLightModelTypeForLight, lightValue);
        glUseProgram(shaderProgramLight);

        light = new Vec3D(0, 0, 10).mul(new Mat3RotY(rotace2/2)).mul(new Mat3RotX(rotace2/2));

        glUniformMatrix4fv(locMathViewLight, false,
                new Mat4ViewRH(light, light.mul(-1), new Vec3D(0, 1, 0)).floatArray());
        glUniformMatrix4fv(locMathProjLight, false,
                new Mat4OrthoRH(10, 10, 1, 20).floatArray());
        glUniform1f(locTimeLight, time);

        matMVPLight = new Mat4ViewRH(light, light.mul(-1), new Vec3D(0, 1, 0))
                .mul(new Mat4OrthoRH(10, 10, 1, 20));

        // rozdeleni objektu
        glUniform1i(locObjectTypeForLight, 0); // plocha
        glUniformMatrix4fv(locMathModelLight, false,
                new Mat4Scale(15).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramLight);

        glUniform1i(locObjectTypeForLight, 1); // V
        glUniformMatrix4fv(locMathModelLight, false,
                new Mat4RotX(rotace1).mul(new Mat4Transl(4, 0, 2)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramLight);

        glUniform1i(locObjectTypeForLight, 2); // kuzel
        glUniformMatrix4fv(locMathModelLight, false,
                new Mat4RotX(rotace1).mul(new Mat4Transl(-3, 0, 3)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramLight);

        glUniform1i(locObjectTypeForLight, 3); // sloni hlava
        glUniformMatrix4fv(locMathModelLight, false,
                new Mat4RotX(rotace1).mul(new Mat4Transl(22, -15, 12)).mul(new Mat4Scale(0.20)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramLight);

        glUniform1i(locObjectTypeForLight, 4); // polokoule
        glUniformMatrix4fv(locMathModelLight, false,
                new Mat4Transl(-12, 15, 8).mul(new Mat4Scale(0.15)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramLight);

        glUniform1i(locObjectTypeForLight, 5); // sombrero
        glUniformMatrix4fv(locMathModelLight, false,
                new Mat4RotX(rotace1).mul(new Mat4Transl(-7, 10, 10)).mul(new Mat4Scale(0.1)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramLight);

        glUniform1i(locObjectTypeForLight, 6); // donut
        glUniformMatrix4fv(locMathModelLight, false,
                new Mat4RotX(-rotace1).mul(new Mat4Transl(5, 0, 5)).mul(new Mat4Scale(0.15)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramLight);
        //persepktivni a ortogonalni
        if (persp) {
            projection = new Mat4PerspRH(Math.PI / 4, 0.5, 0.01, 100.0);
        } else {
            projection = new Mat4OrthoRH(40, 20, 0.01, 100.0);
        }

        // prepinani mezi dratovym modelem a vyplnenymi plochami
        if (wireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        //----------------------------------------------------From View
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, width, height);
        glClearColor(0.5f, 0.1f, 0.1f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(shaderProgramView);

        // poslani hodnot uniform promennych
        glUniform1i(locLightModelType, lightValue);
        glUniform1i(locColorType, colorValue);
        glUniform1f(locTime, time);
        glUniform3f(locLightPos, (float) light.getX(), (float) light.getY(), (float) light.getZ());
        glUniform3fv(locEyePosition, ToFloatArray.convert(camera.getEye()));
        glUniform3f(locViewPos, (float) camera.getPosition().getX(), (float) camera.getPosition().getY(), (float) camera.getPosition().getZ());
        glUniformMatrix4fv(locMathMVPLight, false,
                matMVPLight.floatArray());
        glUniformMatrix4fv(locMathViewView, false,
                camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locMathProjView, false,
                projection.floatArray());
        glUniform1i(locSpotlight, spotlight);
        // nabindovani textury
        texture.bind(shaderProgramView, "textureID", 0);
        renderTarget.getDepthTexture().bind(shaderProgramView, "textureDepth", 1);

        // rozdeleni objektu
        // Blinn-Phong objekty (prvni zobrazeni)
        glUniform1i(locObjectType, 0); // plocha
        glUniformMatrix4fv(locMathModelView, false,
                new Mat4Scale(10).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramView);

        glUniform1i(locObjectType, 1); // V
        glUniformMatrix4fv(locMathModelView, false,
                new Mat4RotX(rotace1).mul(new Mat4Transl(4, 0, 2)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramView);

        glUniform1i(locObjectType, 2); // koule - svetlo
        glUniformMatrix4fv(locMathModelView, false,
                new Mat4Transl(light).mul(new Mat4Scale(1)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramView);

        glUniform1i(locObjectType, 3); // kuzel
        glUniformMatrix4fv(locMathModelView, false,
                new Mat4RotX(rotace1).mul(new Mat4Transl(-3, 0, 3)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramView);

        glUniform1i(locObjectType, 4); // sloni hlava
        glUniformMatrix4fv(locMathModelView, false,
                new Mat4RotX(rotace1).mul(new Mat4Transl(22, -15, 12)).mul(new Mat4Scale(0.20)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramView);

        glUniform1i(locObjectType, 5); // polokoule
        glUniformMatrix4fv(locMathModelView, false,
                new Mat4Transl(-12, 15, 8).mul(new Mat4Scale(0.15)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramView);

        glUniform1i(locObjectType, 6); // sombrero
        glUniformMatrix4fv(locMathModelView, false,
                new Mat4RotX(rotace1).mul(new Mat4Transl(-7, 10, 10)).mul(new Mat4Scale(0.1)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramView);

        glUniform1i(locObjectType, 7); // donut
        glUniformMatrix4fv(locMathModelView, false,
                new Mat4RotX(-rotace1).mul(new Mat4Transl(5, 0, 5)).mul(new Mat4Scale(0.15)).floatArray());
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramView);


        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        textureView.view(renderTarget.getColorTexture(), -1, -1, 0.5);
        textureView.view(renderTarget.getDepthTexture(), -1, -0.5, 0.5);


        if (wireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }




    }
    // ovladani pomoci mysi (odlisne ovladani pro pohyb nez u tlacitek)
    private GLFWMouseButtonCallback mbCallback = new GLFWMouseButtonCallback() {
        private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                if (mousePressed) {
                    camera = camera.addAzimuth(Math.PI * (oldMx - x) / LwjglWindow.WIDTH);
                    camera = camera.addZenith(Math.PI * (oldMy - y) / LwjglWindow.HEIGHT);
                    oldMx = x;
                    oldMy = y;
                }
            }
        };


        @Override
        public void invoke(long window, int button, int action, int mods) {
            mouseButton1 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;
            mouseButton2 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) == GLFW_PRESS;

            if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
                mouseButton1 = true;
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                ox = xBuffer.get(0);
                oy = yBuffer.get(0);
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(window, xPos, yPos);
                oldMx = xPos[0];
                oldMy = yPos[0];
                mousePressed = action == GLFW_PRESS;

            }

            if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE) {
                mouseButton1 = false;
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                double x = xBuffer.get(0);
                double y = yBuffer.get(0);
                camera = camera.addAzimuth((double) Math.PI * (ox - x) / width)
                        .addZenith((double) Math.PI * (oy - y) / width);
                ox = x;
                oy = y;
            }

        }
    };

    private GLFWCursorPosCallback cpCallbacknew = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mouseButton1) {
                camera = camera.addAzimuth((double) Math.PI * (ox - x) / width)
                        .addZenith((double) Math.PI * (oy - y) / width);
                ox = x;
                oy = y;
            }
        }
    };
    // nastaveni ovladani pomoci tlacitek
    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_W:
                        camera = camera.forward(1);
                        break;
                    case GLFW_KEY_D:
                        camera = camera.right(1);
                        break;
                    case GLFW_KEY_S:
                        camera = camera.backward(1);
                        break;
                    case GLFW_KEY_A:
                        camera = camera.left(1);
                        break;
                    case GLFW_KEY_LEFT_CONTROL:

                        camera = camera.down(1);
                        break;
                    case GLFW_KEY_LEFT_SHIFT:

                        camera = camera.up(1);
                        break;
                    case GLFW_KEY_SPACE:
                        camera = camera.withFirstPerson(!camera.getFirstPerson());
                        break;
                    case GLFW_KEY_L: //nekonecne prepinani mezi blinn-phong, per vertex a per pixel
                        if (lightValue == 3) {
                            lightValue = 0;
                        } else {
                            lightValue++;
                        }
                        break;
                    case GLFW_KEY_P: // prepinani mezi pohledy perp a ortho
                        if (persp) {
                            persp = false;
                        } else {
                            persp = true;
                        }
                        break;
                    case GLFW_KEY_K: //prepinani mezi vyplnenymi plochami a dratenym modelem
                        if (wireframe) {
                            wireframe = false;
                        } else {
                            wireframe = true;
                        }
                        break;
                    case GLFW_KEY_R:
                        if (spotlight == 1) {
                            spotlight = 0;
                        } else {
                            spotlight = 1;
                        }
                        break;
                    case GLFW_KEY_C: // prepinani barev
                        if (colorValue == 4) {
                            colorValue = 0;
                        } else {
                            colorValue++;
                        }
                        break;
                }
            }
        }
    };

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    @Override
    public GLFWWindowSizeCallback getWsCallback() {
        return wsCallback;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mbCallback;
    }

    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cpCallbacknew;
    }


}








