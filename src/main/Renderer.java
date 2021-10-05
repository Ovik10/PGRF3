package main;

import lwjglutils.OGLBuffers;
import lwjglutils.ShaderUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2019-09-02
 */
public class Renderer extends AbstractRenderer {

	private int shaderProgram;
	private OGLBuffers buffers;

	@Override
	public void init() {
		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		shaderProgram = ShaderUtils.loadProgram("/start");
		buffers = GridFactory.createGrid(6, 6);
	}
	
	@Override
	public void display() {
		glUseProgram(shaderProgram);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		buffers.draw(GL_TRIANGLES, shaderProgram);
	}

}