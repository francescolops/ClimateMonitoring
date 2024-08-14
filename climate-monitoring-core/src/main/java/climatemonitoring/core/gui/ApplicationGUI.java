/*

Alessandro della Frattina 753073 VA
Cristian Capiferri 752918 VA
Francesco Lops 753175 VA
Dariia Sniezhko 753057 VA

*/

package climatemonitoring.core.gui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL32;

import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import climatemonitoring.core.Application;
import climatemonitoring.core.ApplicationSpecification;
import climatemonitoring.core.Layer;

/**
 * ApplicationGUI is the application version that provides a Graphical User Interface (GUI).
 * It creates a window through the use of the {@link Window} class.
 * It also supports Dear ImGui commands, the GUI library used for this project
 * 
 * @author adellafrattina
 * @version 1.0-SNAPSHOT
 * @see Application
 * @see climatemonitoring.core.headless.ApplicationHeadless
 */
public class ApplicationGUI extends Application {

	/**
	 * This constructor needs to be called once only. It is responsible of window creation and Dear ImGui initialization
	 * @param spec The application specification
	 * @see ApplicationSpecification
	 * @see Window
	 */
	public ApplicationGUI(ApplicationSpecification spec) {

		super(spec, Application.GUI);

		WindowSpecification windowSpec = new WindowSpecification();

		windowSpec.width = spec.width;
		windowSpec.height = spec.height;
		windowSpec.title = spec.title;
		windowSpec.isFullscreen = spec.isFullscreen;
		windowSpec.vSync = spec.vSync;

		m_window = new Window(windowSpec);

		initImGui();
		m_imGuiGlfw.init(m_window.getHandle(), true);
		final boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
		m_imGuiGl3.init(isMac ? "#version 150" : "#version 130");
	}

	/**
	 * It runs the main loop where all the layers are updated and the graphics is rendered or the cmd inputs are executed
	 */
	@Override
	public void run() {

		while (isRunning() && !m_window.isClosed()) {

			GL32.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
			GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT);

			// Execute the onUpdate method
			for (Layer layer : m_layers)
				layer.onUpdate();

			startFrame();

			// Execute the onGUIRender method
			for (Layer layer : m_layers)
				layer.onGUIRender();

			endFrame();

			m_window.swap();
		}
	}

	/**
	 * Shuts down the application when {@link #close()} method gets called
	 */
	@Override
	public void shutdown() {

		for (Layer layer : m_layers)
			layer.onDetach();

		m_imGuiGl3.dispose();
		m_imGuiGlfw.dispose();
		disposeImGui();
		m_window.shutdown();
	}

	@Override
	protected int getWindowWidth() {

		return m_window.getWidth();
	}

	@Override
	protected int getWindowHeight() {

		return m_window.getHeight();
	}

	/**
	 * Utility function. ImGui initialization code goes here
	 */
	private void initImGui() {

		ImGui.createContext();

		// Disable imgui.ini file
		ImGui.getIO().setIniFilename(null);
		ImGui.getIO().setLogFilename(null);
	}

	/**
	 * Utility function. ImGui termination code goes here
	 */
	private void disposeImGui() {

		ImGui.destroyContext();
	}

	/**
	 * Start a new ImGui frame
	 */
	private void startFrame() {

		m_imGuiGlfw.newFrame();
		ImGui.newFrame();
	}

	/**
	 * Render the ImGui frame
	 */
	private void endFrame() {

		ImGui.render();
		m_imGuiGl3.renderDrawData(ImGui.getDrawData());

		if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
			final long backupWindowPtr = GLFW.glfwGetCurrentContext();
			ImGui.updatePlatformWindows();
			ImGui.renderPlatformWindowsDefault();
			GLFW.glfwMakeContextCurrent(backupWindowPtr);
		}
	}

	private Window m_window;
	private final ImGuiImplGlfw m_imGuiGlfw = new ImGuiImplGlfw();
	private final ImGuiImplGl3 m_imGuiGl3 = new ImGuiImplGl3();
}
