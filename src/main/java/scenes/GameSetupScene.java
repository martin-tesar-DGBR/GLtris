package scenes;

import menu.component.*;
import menu.widgets.Button;
import menu.widgets.Switch;
import org.joml.Matrix4f;
import render.*;
import render.manager.ResourceManager;
import util.Constants;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public class GameSetupScene extends Scene{
	private static final double FRAME_SETTINGS_WIDTH = 1280;
	private static final double FRAME_SETTINGS_HEIGHT = 720;
	private static final double FRAME_SETTINGS_X_POS = (Constants.VIEWPORT_W - FRAME_SETTINGS_WIDTH) * 0.5;
	private static final double FRAME_SETTINGS_Y_POS = (Constants.VIEWPORT_H - FRAME_SETTINGS_HEIGHT) * 0.5;

	private Matrix4f projection;
	private Scene nextScene;

	private Shader widgetShader = ResourceManager.getShaderByName("shaders/widget_vertex.glsl", "shaders/widget_fragment.glsl");
	private TextureNineSlice widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
	private WidgetBatch batch = new WidgetBatch(80);

	private TextRenderer textRenderer = TextRenderer.getInstance();

	private TopFrame topFrame;
	private Frame settingsFrame;

	GameSetupScene(long windowID) {
		super(windowID);
		topFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);
		settingsFrame = new Frame(FRAME_SETTINGS_X_POS, FRAME_SETTINGS_Y_POS,
				FRAME_SETTINGS_WIDTH, FRAME_SETTINGS_HEIGHT,
				true, true, 0.0);

		topFrame.addComponent(
			new Button(200, 100, true,
			600, 100, 25, "Start Game",
				widgetTexture, 0, 2,
				(double mouseX, double mouseY, int button, int action, int mods) -> {
					nextScene = new GameScene(windowID);
					shouldChangeScene = true;
				}));
		topFrame.addComponent(new Switch(100, 100, 30, 10,
				true, true, widgetTexture, 0, 0, "",
				(boolean isOn) -> {
					System.out.println(isOn);
				}));
	}

	@Override
	public void updateProjection(long windowID) {
		float projectionWidth = Constants.VIEWPORT_W;
		float projectionHeight = Constants.VIEWPORT_H;
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetWindowSize(windowID, windowWidth, windowHeight);
		float windowAspect = (float) windowWidth[0] / windowHeight[0];
		if (windowAspect >= 16.0f / 9.0f) {
			projectionWidth = projectionHeight * windowAspect;
		}
		else {
			projectionHeight = projectionWidth / windowAspect;
		}
		projection = new Matrix4f().identity().ortho(
			0.0f, projectionWidth,
			0.0f, projectionHeight,
			0.0f, 100.0f);
	}

	@Override
	public void init() {
		updateProjection(windowID);
	}

	@Override
	public void update(double dt) {

	}

	@Override
	public void draw() {
		widgetShader.bind();
		widgetTexture.bind(widgetShader, "uTexture");

		float[] buffer = new float[16];

		widgetShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		batch.addVertices(topFrame.generateVertices());
		batch.flush();

		textRenderer.bind();

		for (Component component : topFrame.getComponents()) {
			if (component instanceof Button) {
				Button button = (Button) component;
				textRenderer.addText(button, 1.0f, 1.0f, 1.0f);
			}
		}

		textRenderer.draw();
	}

	@Override
	public boolean shouldChangeScene() {
		return shouldChangeScene;
	}

	@Override
	public Scene nextScene() {
		return nextScene;
	}

	@Override
	public void destroy() {
		topFrame.destroy();
	}
}
