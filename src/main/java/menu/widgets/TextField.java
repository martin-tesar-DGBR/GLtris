package menu.widgets;

import menu.component.Component;
import menu.component.TextInfo;
import org.joml.Math;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.system.libffi.FFICIF;
import render.texture.TextureAtlas;
import render.manager.ResourceManager;
import util.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class TextField extends Component implements GLFWCharCallbackI, GLFWKeyCallbackI, OnComponentClick, OnComponentHover {
	TextureAtlas texture = ResourceManager.getTextureNineSliceByName("images/widgets.png");

	GapBuffer textBuffer;

	float fontSize;
	float r, g, b;
	boolean isFocused = false;

	public TextField(double xPos, double yPos, double width, double height, boolean isActive,
					 String displayText, String initialText, float fontSize, float r, float g, float b) {
		super(xPos, yPos, width, height, displayText, isActive);
		this.fontSize = fontSize;
		this.r = r;
		this.g = g;
		this.b = b;

		int cap = (int) ((width - fontSize) / fontSize);
		textBuffer = new GapBuffer(initialText, cap, 20);

		KeyListener.registerKeyCallback(this);
		KeyListener.registerCharCallback(this);
	}

	public String getText() {
		return textBuffer.getText();
	}

	@Override
	public float[] generateVertices() {
		float[] uvs = texture.getElementUVs(0, 0, 1, 1);
		float p0x = (float) xPos;
		float p0y = (float) yPos;
		float p0u = uvs[0];
		float p0v = uvs[1];

		float p1x = (float) (xPos + width);
		float p1y = (float) (yPos + height);
		float p1u = uvs[2];
		float p1v = uvs[3];

		float[] vertices = new float[Constants.WIDGET_ATTRIBUTES_PER_VERTEX * Constants.WIDGET_ELEMENTS_PER_QUAD];

		Utils.addBlockVertices(vertices, 0,
			p0x, p0y, p0u, p0v,
			p1x, p1y, p1u, p1v);

		return vertices;
	}

	@Override
	public List<TextInfo> getTextInfo() {
		TextInfo title = new TextInfo(displayText, fontSize, (float) (xPos - displayText.length() * fontSize - 0.5 * (height - fontSize)), (float) (yPos + 0.5 * (height - fontSize)), r, g, b);
		TextInfo contents = new TextInfo(textBuffer.getText(), fontSize, (float) (xPos + 0.5 * (height - fontSize)), (float) (yPos + 0.5 * (height - fontSize)), r, g, b);
		List<TextInfo> ret = new ArrayList<>();
		ret.add(title);
		ret.add(contents);
		return ret;
	}

	@Override
	public void destroy() {
		KeyListener.unregisterKeyCallback(this);
		KeyListener.unregisterCharCallback(this);
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button, int action, int mods) {
		if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
			isFocused = isCursorHovered(mouseX, mouseY);

			int charIndex = (int) Math.round((mouseX - xPos) / fontSize);
			textBuffer.setPosition(charIndex);
		}
	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {

	}

	@Override
	public FFICIF getCallInterface() {
		return GLFWCharCallbackI.super.getCallInterface();
	}

	@Override
	public void callback(long ret, long args) {
		GLFWCharCallbackI.super.callback(ret, args);
	}

	@Override
	public void invoke(long window, int codepoint) {
		if (!isFocused) {
			return;
		}

		//should probably support more than basic ascii at some point
		char toAdd = (char) codepoint;
		textBuffer.insert(toAdd);
	}

	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		if (!isFocused) {
			return;
		}
		if (action != GLFW_RELEASE) {
			switch (key) {
				case GLFW_KEY_BACKSPACE -> {
					textBuffer.delete();
				}
			}
		}
	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {

	}
}