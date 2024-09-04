/*

Alessandro della Frattina 753073 VA
Cristian Capiferri 752918 VA
Francesco Lops 753175 VA
Dariia Sniezhko 753057 VA

*/

package climatemonitoring;

import imgui.ImGui;
import climatemonitoring.core.Application;
import climatemonitoring.core.ConnectionLostException;
import climatemonitoring.core.DatabaseRequestException;
import climatemonitoring.core.ViewState;
import climatemonitoring.core.gui.Button;
import climatemonitoring.core.gui.Panel;
import climatemonitoring.core.headless.Console;
import climatemonitoring.core.utility.Command;

/**
 * The main view. Mainly to search areas (and centers in Headless mode)
 * 
 * @author adellafrattina
 * @version 1.0-SNAPSHOT
 */
class Master extends ViewState {

	@Override
	public void onHeadlessRender(String args) {

		Command c = new Command(args);

		try {

			// <what>
			switch (c.getCmd()) {

				case "area":
					c = new Command(c.getArgs());
					SearchArea.onHeadlessRender(c.getCmd(), c.getArgs());
					break;
				case "center":
					c = new Command(c.getArgs());
					SearchCenter.onHeadlessRender(c.getCmd(), c.getArgs());
					break;
				default:
					Console.write("Incorrect command syntax -->'" + c.getCmd() + "', expected [area, center]");
					break;
			}
		}

		catch (DatabaseRequestException e) {

			Console.write("Error message from database: " + e.getMessage());
		}

		catch (ConnectionLostException e) {

			Console.write("Connection lost");
			setCurrentState(ViewType.CONNECTION);
		}
	}

	@Override
	public void onGUIRender() {

		ImGui.sameLine();
		loginButton.setOriginX(loginButton.getWidth());
		loginButton.setPositionX(Application.getWidth() - 10.0f);
		if (loginButton.render())
			setCurrentState(ViewType.LOGIN);

		ImGui.separator();
		ImGui.newLine();
		panel.setWidth(Application.getWidth() / 2.0f);
		panel.setOriginX(panel.getWidth() / 2.0f);
		panel.setPositionX(Application.getWidth() / 2.0f);
		panel.begin(null);
		SearchArea.onGUIRender();
		panel.end();
	}

	private Panel panel = new Panel();
	private Button loginButton = new Button(" Login ");
}
