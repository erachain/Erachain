package gui.settings;
// 16 03
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import lang.Lang;


public class SettingsTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2198816415884720961L;
	
	public SettingsKnownPeersPanel settingsKnownPeersPanel;
	public SettingsBasicPanel settingsBasicPanel;
	public SettingsAllowedPanel settingsAllowedPanel;
	public UI_Setting_Panel uI_Settings_Panel;
	
	public SettingsTabPane()
	{
		super();
		
		//ADD TABS
			
		settingsBasicPanel = new SettingsBasicPanel();
        JScrollPane scrollPane1 = new JScrollPane(settingsBasicPanel);
        this.addTab(Lang.getInstance().translate("Basic"), scrollPane1);

		settingsKnownPeersPanel = new SettingsKnownPeersPanel();
        JScrollPane scrollPane2 = new JScrollPane(settingsKnownPeersPanel);
        this.addTab(Lang.getInstance().translate("Known Peers"), scrollPane2);
        
        settingsAllowedPanel = new SettingsAllowedPanel();
        JScrollPane scrollPane3 = new JScrollPane(settingsAllowedPanel);
        this.addTab(Lang.getInstance().translate("Access permission"), scrollPane3);
        
        uI_Settings_Panel = new UI_Setting_Panel();
        JScrollPane scrollPane4 = new JScrollPane(uI_Settings_Panel);
        this.addTab(Lang.getInstance().translate("UI Settings"), scrollPane4);
        

	}
	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
		this.settingsKnownPeersPanel.close();
		this.settingsAllowedPanel.close();
	}
}