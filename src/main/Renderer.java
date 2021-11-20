package main;

import lwjglutils.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

/**
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2019-09-02
 */
public class Renderer extends AbstractRenderer {

    private double oldMx, oldMy;
    private boolean mousePressed, wireframe = false;
    boolean persp = true;

    private int shaderProgramViewer, shaderProgramLight;
    private OGLBuffers buffers;
    private OGLRenderTarget renderTarget;

    float rotace = 0;
    float time = 0;

    private Camera camera, cameraLight;
    private Mat4 projection;

    private int locView, locProjection, locSolid, locLightPosition, locEyePosition, locLightVP;
    private int locViewLight, locProjectionLight, locSolidLight;

    private OGLTexture2D mosaicTexture;
    private OGLTexture.Viewer viewer;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        shaderProgramViewer = ShaderUtils.loadProgram("/start");
        shaderProgramLight = ShaderUtils.loadProgram("/light");

        locView = glGetUniformLocation(shaderProgramViewer, "view");
        locProjection = glGetUniformLocation(shaderProgramViewer, "projection");
        locSolid = glGetUniformLocation(shaderProgramViewer, "solid");
        locLightPosition = glGetUniformLocation(shaderProgramViewer, "lightPosition");
        locEyePosition = glGetUniformLocation(shaderProgramViewer, "eyePosition");
        locLightVP = glGetUniformLocation(shaderProgramViewer, "lightVP");

        locViewLight = glGetUniformLocation(shaderProgramLight, "view");
        locProjectionLight = glGetUniformLocation(shaderProgramLight, "projection");
        locSolidLight = glGetUniformLocation(shaderProgramLight, "solid");

        buffers = GridFactory.createGrid(200, 200);
        renderTarget = new OGLRenderTarget(1024, 1024);
        viewer = new OGLTexture2D.Viewer();

//        view = new Mat4ViewRH();
//        camera = new Camera(
//                new Vec3D(6, 6, 5),
//                5 / 4f * Math.PI, // -3 / 4f * Math.PI; 225 stupňů
//                -1 / 5f * Math.PI, // -0.6 rad, -36 stupňů
//                1.0,
//                true
//        );
        camera = new Camera()
                .withPosition(new Vec3D(-3, 3, 3))
                .withAzimuth(-1 / 4f * Math.PI) // 7 / 4f * Math.PI
                .withZenith(-1.3 / 5f * Math.PI);

        projection = new Mat4PerspRH(Math.PI / 3, 600 / 800f, 1.0, 20.0);
//        projection = new Mat4OrthoRH();

        textRenderer = new OGLTextRenderer(width, height);

        cameraLight = new Camera()
                .withPosition(new Vec3D(6, 6, 6))
                .withAzimuth(5 / 4f * Math.PI)
                .withZenith(-1 / 5f * Math.PI);

        try {
            mosaicTexture = new OGLTexture2D("textures/mosaic.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST); // zapnout z-buffer (kvůli TextRendereru)
//        cameraLight = cameraLight.left(0.01);

        renderFromLight();
        renderFromViewer();

        viewer.view(renderTarget.getColorTexture(), -1.0, -1.0, 0.7);
        viewer.view(renderTarget.getDepthTexture(), -1.0, -0.3, 0.7);
//        viewer.view(mosaicTexture, -1.0, -1.0, 0.5);

        textRenderer.addStr2D(width - 90, height - 3, "Jindrich Svoboda");

        if (persp) {
            projection = new Mat4PerspRH(Math.PI / 4, 0.5, 0.01, 100.0);
        } else {
            projection = new Mat4OrthoRH(40, 20, 0.01, 100.0);
        }
        if (wireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    private void renderFromLight() {
        glUseProgram(shaderProgramLight);
        renderTarget.bind();
        glClearColor(0.5f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(locViewLight, false, cameraLight.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionLight, false, projection.floatArray());

        glUniform1i(locSolidLight, 1);
        buffers.draw(GL_TRIANGLES, shaderProgramLight);

        glUniform1i(locSolidLight, 2);
        buffers.draw(GL_TRIANGLES, shaderProgramLight);
    }

    private void renderFromViewer() {
        glUseProgram(shaderProgramViewer);

        // výchozí framebuffer - render do obrazovky
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // nutno opravit viewport, protože render target si nastavuje vlastní
        glViewport(0, 0, width, height);

        glClearColor(0f, 0.5f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection, false, projection.floatArray());
        glUniformMatrix4fv(locLightVP, false, cameraLight.getViewMatrix().mul(projection).floatArray());

        glUniform3fv(locLightPosition, ToFloatArray.convert(cameraLight.getPosition()));
        glUniform3fv(locEyePosition, ToFloatArray.convert(camera.getEye()));

        renderTarget.getDepthTexture().bind(shaderProgramViewer, "depthTexture", 1);
        mosaicTexture.bind(shaderProgramViewer, "mosaic", 0);

        glUniform1i(locSolid, 1);
        buffers.draw(GL_TRIANGLES, shaderProgramViewer);

        glUniform1i(locSolid, 2);
        buffers.draw(GL_TRIANGLES, shaderProgramViewer);
    }

    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cursorPosCallback;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mouseButtonCallback;
    }

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

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

    private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(window, xPos, yPos);
                oldMx = xPos[0];
                oldMy = yPos[0];
                mousePressed = (action == GLFW_PRESS);
            }
        }
    };

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
                    case GLFW_KEY_R:
                        camera = camera.mulRadius(0.9f);
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
                }
            }
        }
    };
}
