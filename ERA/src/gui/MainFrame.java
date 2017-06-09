package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.simple.JSONObject;

import controller.Controller;
import gui.library.Menu_Deals;
import gui.library.Menu_Files;
import gui.library.My_JFileChooser;
import gui.status.StatusPanel;
import gui2.Main_Panel;
import lang.Lang;
import settings.Settings;
import utils.ObserverMessage;
import utils.SaveStrToFile;


@SuppressWarnings("serial")
public class MainFrame extends JFrame implements Observer{
	private JSONObject settingsJSONbuf;
	private JSONObject main_Frame_settingsJSON;
	// Variables declaration - do not modify
		private Menu_Files jMenu1;
		private Menu_Deals jMenu2;
		private javax.swing.JMenuBar jMenuBar1;
		private Main_Panel mainPanel;
		private StatusPanel statusPanel;
		private javax.swing.JTabbedPane jTabbedPane1;
		// End of variables declaration
		private MainFrame th;

private static MainFrame instance;
public static MainFrame getInstance()
{
	if(instance == null)
	{
		instance = new MainFrame();
	}
	
	return instance;
}



	private MainFrame()
	{
		
		//CREATE FRAME
		super(controller.Controller.APP_NAME +  " v." + Controller.getVersion());
		this.setVisible(false);
		if(Settings.getInstance().isTestnet()) {
			setTitle(controller.Controller.APP_NAME + " TestNet "
					 +  "v." + Controller.getVersion()
					 + " TS:" + Settings.getInstance().getGenesisStamp());
		}
		
	th = this;
	
       
		Controller.getInstance().addObserver(this);	
             
		settingsJSONbuf = new JSONObject();
		settingsJSONbuf = Settings.getInstance().Dump();
		initComponents();
		
		
		
        // WALLET STATS
        this.add(new StatusPanel(), BorderLayout.SOUTH);
        
        
		
         this.setVisible(true);
 	}

	 
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
public void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		jTabbedPane1 = new javax.swing.JTabbedPane();
		mainPanel = new Main_Panel();
		//statusPanel = new StatusPanel();
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new Menu_Files();
		jMenu2 = new Menu_Deals();

		
		// getContentPane().setLayout(new java.awt.GridBagLayout());
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		// getContentPane().add(jTabbedPane1, gridBagConstraints);

		add(jTabbedPane1, BorderLayout.NORTH);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.2;
		// getContentPane().add(jPanel1, gridBagConstraints);

		add(mainPanel, BorderLayout.CENTER);

	//	javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(statusPanel);
	//	statusPanel.setLayout(jPanel2Layout);
	//	jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	//			.addGap(0, 0, Short.MAX_VALUE));
	//	jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	//			.addGap(0, 0, Short.MAX_VALUE));

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.2;
		// getContentPane().add(jPanel2, gridBagConstraints);
		//this.add(new StatusPanel(), BorderLayout.SOUTH);

		jMenu1.setText(Lang.getInstance().translate("File"));
		jMenuBar1.add(jMenu1);

		jMenu2.setText(Lang.getInstance().translate("Deals"));
		jMenuBar1.add(jMenu2);

		setJMenuBar(jMenuBar1);

	
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				// TODO Auto-generated method stub
				// read settings
				int lDiv;
				int div;
				Split_Panel sP;
				HashMap outOpenTabbeds = new HashMap();
				JSONObject settingsJSON = new JSONObject();
				settingsJSONbuf = Settings.getInstance().read_setting_JSON();
				if (settingsJSONbuf.containsKey("Main_Frame_Setting"))  settingsJSON = (JSONObject) settingsJSONbuf.get("Main_Frame_Setting");
				if (th.getExtendedState() != MAXIMIZED_BOTH) {
					settingsJSON.put("Main_Frame_is_Max", "false");
				settingsJSON.put("Main_Frame_Height", getHeight() + ""); // высота
				settingsJSON.put("Main_Frame_Width", getWidth() + ""); // длина
				settingsJSON.put("Main_Frame_Loc_X", getX() + ""); // высота
				settingsJSON.put("Main_Frame_Loc_Y", getY() + ""); // высота
				}
				else{
					
					settingsJSON.put("Main_Frame_is_Max", "true");
				}

				settingsJSON.put("Main_Frame_Div_Orientation", mainPanel.jSplitPane1.getOrientation() + "");
				// horisontal - vertical orientation
				lDiv = mainPanel.jSplitPane1.getLastDividerLocation();
				div = mainPanel.jSplitPane1.getDividerLocation();

				settingsJSON.put("Main_Frame_Div_Last_Loc", lDiv + "");
				settingsJSON.put("Main_Frame_Div_Loc", div + "");
				settingsJSON.remove("OpenTabbeds");
				for (int i = 0; i < mainPanel.jTabbedPane1.getTabCount(); i++) {
					// write in setting opet tabbs
					Component comp = mainPanel.jTabbedPane1.getComponentAt(i);
					outOpenTabbeds.put(i, comp.getClass().getSimpleName());

					// write open tabbed settings Split panel
					if (comp instanceof Split_Panel) {
						HashMap outTabbedDiv = new HashMap();

						sP = ((Split_Panel) comp);
						outTabbedDiv.put("Div_Orientation", sP.jSplitPanel.getOrientation()+"");

						// write

						lDiv = sP.jSplitPanel.getLastDividerLocation();
						div = sP.jSplitPanel.getDividerLocation();

						outTabbedDiv.put("Div_Last_Loc", lDiv + "");
						outTabbedDiv.put("Div_Loc", div + "");

						settingsJSON.put(comp.getClass().getSimpleName(),
								outTabbedDiv);
					}
					
					settingsJSON.put("OpenTabbeds", outOpenTabbeds);
					

				}
				settingsJSON.put("Main_Frame_Selected_Tab", mainPanel.jTabbedPane1.getSelectedIndex()+ "");
				
				settingsJSONbuf.put("Main_Frame_Setting", settingsJSON);
				settingsJSONbuf.put("FileChooser_Path", new String(My_JFileChooser.get_Default_Path().getBytes(Charset.forName("UTF-8"))));
				settingsJSONbuf.put("FileChooser_Wight", My_JFileChooser.get_Default_Width());
				settingsJSONbuf.put("FileChooser_Height", My_JFileChooser.get_Default_Height());
				
				// save setting to setting file
				try {
					SaveStrToFile.saveJsonFine(Settings.getInstance().getSettingsPath(), settingsJSONbuf);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(new JFrame(), "Error writing to the file: "
							+ Settings.getInstance().getSettingsPath() + "\nProbably there is no access.", "Error!",
							JOptionPane.ERROR_MESSAGE);
				}

				new ClosingDialog();

			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub
			}

		});

		pack();
		// set perameters size $ split panel
		int devLastLoc = 250;
		int devLoc = 250;
		int x =0;
		int y =0;
		Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screens = kit.getScreenSize();
		int h = screens.height;
		int w = screens.width;
		int orientation=1;

		if (settingsJSONbuf.containsKey("Main_Frame_Setting")) {
			main_Frame_settingsJSON = new JSONObject();
			main_Frame_settingsJSON = (JSONObject) settingsJSONbuf.get("Main_Frame_Setting");
			
			if( main_Frame_settingsJSON.containsKey("Main_Frame_Loc_X")) x = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Loc_X")); // x
			if( main_Frame_settingsJSON.containsKey("Main_Frame_Loc_Y")) y = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Loc_Y")); // y
			
			
			if( main_Frame_settingsJSON.containsKey("Main_Frame_Height")) h = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Height")); // высота
			if( main_Frame_settingsJSON.containsKey("Main_Frame_Width")) w = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Width")); // длина
			
			if(main_Frame_settingsJSON.containsKey("Main_Frame_is_Max")){
				Boolean bb = new Boolean((String)main_Frame_settingsJSON.get("Main_Frame_is_Max"));
				if (bb)this.setExtendedState(MAXIMIZED_BOTH);
				
			}
			
			if( main_Frame_settingsJSON.containsKey("Main_Frame_Div_Orientation")) orientation = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Div_Orientation"));
			
			
			if( main_Frame_settingsJSON.containsKey("Main_Frame_Div_Loc")) devLoc = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Div_Loc"));
			
			if( main_Frame_settingsJSON.containsKey("Main_Frame_Div_Last_Loc")) devLastLoc = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Div_Last_Loc"));
			

			// load tabs
			if (main_Frame_settingsJSON.containsKey("OpenTabbeds")) {
				JSONObject openTabes = (JSONObject) main_Frame_settingsJSON.get("OpenTabbeds");
				Set ot = openTabes.keySet();
				for (int i = 0; i < ot.size(); i++) {
					String value = (String) openTabes.get(i + "");
					mainPanel.dylay(value);
				}
				
				if (main_Frame_settingsJSON.containsKey("Main_Frame_Selected_Tab"))
					try {
					mainPanel.jTabbedPane1.setSelectedIndex(
							new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Selected_Tab")));
					} catch (Exception e) {
					}
			}
		
		
		} 
		//else {
	//		setExtendedState(MAXIMIZED_BOTH);
			// mainPanel.jSplitPane1.setDividerLocation(250);
	//		mainPanel.jSplitPane1.setLastDividerLocation(300);

	//	}
		
		setLocation(x, y);
		setSize(w, h);
		mainPanel.jSplitPane1.setOrientation(orientation);
		mainPanel.jSplitPane1.setLastDividerLocation(devLastLoc);
		mainPanel.jSplitPane1.setDividerLocation(devLoc);
		mainPanel.jSplitPane1.set_button_title(); // set title diveders
													// buttons
		

	}// </editor-fold>

	
	

	@Override
	public void update(Observable arg0, Object arg1) {
		
		ObserverMessage message = (ObserverMessage) arg1;
		if(message.getType() == ObserverMessage.NETWORK_STATUS)
		{
			int status = (int) message.getValue();
			
			if(status == Controller.STATUS_NO_CONNECTIONS)
			{
				List<Image> icons = new ArrayList<Image>();
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16_No.png"));
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_No.png"));
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_No.png"));
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_No.png"));
				this.setIconImages(icons);
				
			}
			if(status == Controller.STATUS_SYNCHRONIZING)
			{
				List<Image> icons = new ArrayList<Image>();
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16_Se.png"));
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_Se.png"));
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_Se.png"));
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_Se.png"));
				this.setIconImages(icons);
			}
			if(status == Controller.STATUS_OK)
			{
				//ICON
				List<Image> icons = new ArrayList<Image>();
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
				icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
				this.setIconImages(icons);
			}
		}	
		
		
	}
}

