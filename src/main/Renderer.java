package main;

import lwjglutils.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2019-09-02
 */
public class Renderer extends AbstractRenderer {

	private int shaderProgram;
	private OGLBuffers buffers;

	private Camera camera;
	private Mat4 projection;
	private int locView, locProjection;
	private OGLTexture2D mosaicTexture;

	@Override
	public void init() {
		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		shaderProgram = ShaderUtils.loadProgram("/start");

		locView = glGetUniformLocation(shaderProgram, "view");
		locProjection = glGetUniformLocation(shaderProgram, "projection");

		buffers = GridFactory.createGrid(200, 200);

//		view = new Mat4ViewRH();
//		camera = new Camera(
//				new Vec3D(6, 6, 5),
//				5 / 4f * Math.PI, // -3 / 4f * Math.PI; 225 stupňů
//				-1 / 5f * Math.PI, // -0.6 rad, -36 stupňů
//				1.0,
//				true
//		);
		camera = new Camera()
				.withPosition(new Vec3D(3, 3, 2))
				.withAzimuth(5 / 4f * Math.PI)
				.withZenith(-1 / 5f * Math.PI);

		projection = new Mat4PerspRH(Math.PI / 3, 600 / 800f, 1.0, 20.0);
//		projection = new Mat4OrthoRH();

		textRenderer = new OGLTextRenderer(width, height);

		try {
			mosaicTexture = new OGLTexture2D("textures/mosaic.jpg");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void display() {
		glUseProgram(shaderProgram);
		glEnable(GL_DEPTH_TEST);
		glViewport(0, 0, width, height);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
		glUniformMatrix4fv(locProjection, false, projection.floatArray());

		mosaicTexture.bind(shaderProgram, "mosaic", 0);

		buffers.draw(GL_TRIANGLES, shaderProgram);

		textRenderer.addStr2D(width - 90, height - 3, "PGRF");
	}

	@Override
	public GLFWCursorPosCallback getCursorCallback() {
		return new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {

			}
		};
	}
}