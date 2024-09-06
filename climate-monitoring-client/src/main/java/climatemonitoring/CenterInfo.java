/*

Alessandro della Frattina 753073 VA
Cristian Capiferri 752918 VA
Francesco Lops 753175 VA
Dariia Sniezhko 753057 VA

*/

package climatemonitoring;

import climatemonitoring.core.Application;
import climatemonitoring.core.Area;
import climatemonitoring.core.Center;
import climatemonitoring.core.ConnectionLostException;
import climatemonitoring.core.DatabaseRequestException;
import climatemonitoring.core.Result;
import climatemonitoring.core.ViewState;
import climatemonitoring.core.gui.Button;
import climatemonitoring.core.gui.Panel;
import climatemonitoring.core.headless.Console;
import climatemonitoring.core.utility.Command;
import imgui.ImGui;

/**
 * To view center info (also area inclusion if the operator is logged in GUI mode)
 * 
 * @author francescolops
 * @version 1.0-SNAPSHOT
 */
class CenterInfo extends ViewState {

	@Override
	public void onHeadlessRender(String args) {

		try {

			Command c = new Command(args);

			String by = c.getCmd();

			if (by.equals("id")) {

				String id = c.getArgs();
				Center center = Handler.getProxyServer().getCenter(id.trim());
				
				if (center != null) {

					printCenterInfo(center);
				} else {

					Console.write("Center not found");
				}
			} else if (by.equals("index")) {

				int index = Integer.parseInt(c.getArgs());

				Center[] centers = SearchCenter.getFoundCenters();
				Center center = centers[index];
				
				if (center != null) {

					printCenterInfo(center);
				} else {
					
					Console.write("Center not found");
				}
			}else{

				Console.write("Incorrect command syntax -->'" + c.getCmd() + "', expected [id, index]");
			}
		} catch (DatabaseRequestException e) {

			Console.write("Error message from database: " + e.getMessage());
		} catch (ConnectionLostException e) {
			Console.write("Connection lost");
			getView().setCurrentState(ViewType.CONNECTION);
		} catch (ArrayIndexOutOfBoundsException e) {
			
			Console.write("Invalid index provided. Please ensure the index is within the valid range");
		}
	}

	@Override
	public void onGUIRender() {

		try {

			if(centerarea == null){

				centerarea = Handler.getProxyServer().getArea(center.getCity());
			}
			
			panel.setSize(Application.getWidth() / 2.0f, Application.getHeight() / 2.0f);
			panel.setOrigin(panel.getWidth() / 2.0f, panel.getHeight() / 2.0f);
			panel.setPosition(Application.getWidth() / 2.0f, Application.getHeight() / 2.0f);
			panel.begin(center.getCenterID());

			ImGui.text("Location: " + centerarea.getName() + ", " + center.getStreet() + ", " + center.getHouseNumber() + (center.getDistrict() == null ? "": ", " + center.getDistrict()));
			ImGui.text("Monitored areas:");
			if(monitoredareasresult == null){

				monitoredareasresult = Handler.getProxyServerMT().getMonitoredAreas(center.getCenterID());
			}else{

				if(monitoredareasresult.ready()){

					monitoredareas = monitoredareasresult.get();
				}else{

					ImGui.text("Loading...");
				}
			}

			if(monitoredareas != null){

				for(int i = 0; i < monitoredareas.length; i++){

					ImGui.text("- " + monitoredareas[i].getName() + ", " + monitoredareas[i].getCountryCode());
				}
				SearchArea.onGUIRender();
			}
			panel.end();

			ImGui.newLine();
			cancel.setOriginX(0);
			cancel.setPositionX(panel.getPositionX() - panel.getWidth() / 2.0f);

			if(cancel.render()){

				resetStateData(new CenterInfo());
				returnToPreviousState();
			}

			ImGui.sameLine();
			add.setOriginX(add.getWidth());
			add.setPositionX(panel.getPositionX() + panel.getWidth() / 2.0f);

			if(add.render()){

				addingmode = true;
			}
		} catch (ConnectionLostException e) {

			setCurrentState(ViewType.CONNECTION);
		} catch (Exception e){

			e.printStackTrace();
			Application.close();
		}
	}

	private void printCenterInfo(Center center) throws ConnectionLostException, DatabaseRequestException {

		Console.write("Name > " + center.getCenterID());
		Console.write("Street > " + center.getStreet() + ", "+ center.getHouseNumber());
		Console.write("Postal code > " + center.getPostalCode());
		Area area = Handler.getProxyServer().getArea(center.getCity());
		Console.write("City > " + area.getAsciiName() + "(" + area.getGeonameID() + ")");
		Console.write("District > " + center.getDistrict());

		Console.write("\n--- Monitored Areas ---");

		Area[] monitoredAreas = Handler.getProxyServer().getMonitoredAreas(center.getCenterID());
		for (Area a : monitoredAreas)
			Console.write(a.getGeonameID() + " - " + a.getAsciiName() + ", " + a.getCountryCode());

		Console.write("\n");
	}

	private Panel panel = new Panel();
	Center center;
	private Result <Area[]> monitoredareasresult;
	private Area[] monitoredareas;
	private Area centerarea;
	private Button cancel = new Button("  Cancel  ");
	private Button add = new Button("Add new area");
	private Panel panelinfo = new Panel();
	private Panel paneladd = new Panel();
	private boolean addingmode = false;
}
