package render.batch;

import render.batch.Batch;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class TextBatch extends Batch {
	private static final int FLOATS_PER_VERTEX = 7;
	private static final int VERTEX_SIZE = FLOATS_PER_VERTEX * Float.BYTES;

	private int maxVertices;
	private int usedVertices;
	private int vaoID;
	private int vboID;

	public TextBatch(int maxVertices) {
		if (maxVertices <= 0) {
			throw new IllegalArgumentException("Cannot have less than or equal to 0 vertices in the batch.");
		}

		this.maxVertices = maxVertices;
		usedVertices = 0;

		vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);

		vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, VERTEX_SIZE, 0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, VERTEX_SIZE, 2 * Float.BYTES);
		glVertexAttribPointer(2, 2, GL_FLOAT, false, VERTEX_SIZE, 5 * Float.BYTES);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);

		glBufferData(GL_ARRAY_BUFFER, (long) maxVertices * VERTEX_SIZE, GL_STREAM_DRAW);


	}

	@Override
	public boolean isEmpty() {
		return usedVertices == 0;
	}

	@Override
	public boolean isEnoughRoom(int numVerticesToAdd) {
		return usedVertices + numVerticesToAdd <= maxVertices;
	}

	public void addVertices(float[] vertexData) {
		if (vertexData.length % FLOATS_PER_VERTEX != 0) {
			throw new IllegalArgumentException("Vertex data given does not conform to the given format.");
		}
		int numVertices = vertexData.length / FLOATS_PER_VERTEX;
		if (numVertices > maxVertices) {
			throw new IllegalArgumentException("Number of vertices passed in exceeds the maximum capacity of the batch.");
		}
		if(!isEnoughRoom(numVertices)) {
			flush();
		}

		glBindVertexArray(vaoID);
		glBindBuffer(GL_ARRAY_BUFFER, vboID);

		glBufferSubData(GL_ARRAY_BUFFER, (long) usedVertices * VERTEX_SIZE, vertexData);
		usedVertices += numVertices;
	}

	@Override
	public void flush(){
		if (usedVertices <= 0) {
			return;
		}

		glBindVertexArray(vaoID);
		glDrawArrays(GL_TRIANGLES, 0, usedVertices);

		usedVertices = 0;
	}

	@Override
	public void destroy() {
		glDeleteBuffers(vboID);
		glDeleteVertexArrays(vaoID);
	}
}
