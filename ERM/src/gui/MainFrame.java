package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import controller.Controller;
import core.wallet.Wallet;
import gui.items.accounts.Main_Accounts_Frame;
import gui.items.assets.MainAssetsFrame;
import gui.items.documents.Main_Hash_Document_Frame;
import gui.items.imprints.MainImprintsFrame;
import gui.items.mails.Mails_Main_Frame;
import gui.items.notes.MainNotesFrame;
import gui.items.other.Other_Internal_Frame;
import gui.items.persons.MainPersonsFrame;
import gui.items.records.Records_Main_Frame;
import gui.items.statement.MainStatementsFrame;
import gui.items.statuses.MainStatusesFrame;
import gui.items.unions.MainUnionsFrame;
import gui.items.voting.MainVotingsFrame;
import gui.library.Menu_Popup_Deals_button;
import gui.library.Menu_Popup_File_button;
import gui.records.RecordsFrame;
import gui.status.StatusPanel;
import lang.Lang;
import settings.Settings;
import utils.ObserverMessage;
import utils.SaveStrToFile;
import java.io.File;
import org.apache.commons.io.FileUtils;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements Observer{
	
private static final Color FFFF = null;
public static  JDesktopPane desktopPane;
private JFrame parent;


	@SuppressWarnings("null")
	public MainFrame()
	{
		
		//CREATE FRAME
		super(controller.Controller.APP_NAME +  " v." + Controller.getVersion());
		this.setVisible(false);
		if(Settings.getInstance().isTestnet()) {
			setTitle(controller.Controller.APP_NAME + " TestNet "
					 +  "v." + Controller.getVersion()
					 + " TS:" + Settings.getInstance().getGenesisStamp());
		}
		
		
	
       
		parent = MainFrame.this;
		Controller.getInstance().addObserver(this);	
		
		// tool bar
	//	JToolBar tb1 = new JToolBar(" РџР°РЅРµР»СЊ 1");

			JToolBar	 Toolbar_Main = new JToolBar(Lang.getInstance().translate("Menu"));

			//	tb1.setRollover(true);

			//	tb1.add(new JButton(new ImageIcon("Add24.gif"))); tb1.add(new JButton(new ImageIcon("AlignTop24.gif")));

			//	tb1.add(new JButton(new ImageIcon("About24.gif")));
	
			
			Toolbar_Main.add(new Menu_Popup_File_button());
			Toolbar_Main.add(new Menu_Popup_Deals_button());
			
		JButton button1_MainToolBar = new JButton();
		button1_MainToolBar.setText(Lang.getInstance().translate("Accounts"));
			//     button1_MainToolBar.setActionCommand("button1_Main_Panel");
		button1_MainToolBar.setFocusable(false);
		button1_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button1_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		button1_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
			//	gui.Menu.selectOrAdd( new AccountsFrame(parent), MainFrame.desktopPane.getAllFrames()); // old panel
				gui.library.library.selectOrAdd( new Main_Accounts_Frame(), MainFrame.desktopPane.getAllFrames());
		    }
		});
		Toolbar_Main.add(button1_MainToolBar);
		
			
		
			
		JButton button2_MainToolBar = new JButton();
		button2_MainToolBar.setText(Lang.getInstance().translate("Persons"));
		    //    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button2_MainToolBar.setFocusable(false);
		button2_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button2_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
       	button2_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
       		public void actionPerformed(java.awt.event.ActionEvent evt) {
       			gui.library.library.selectOrAdd( new MainPersonsFrame(), MainFrame.desktopPane.getAllFrames());
       		}
       	});
       	Toolbar_Main.add(button2_MainToolBar);
			
		
       	
      	JButton button_Mails_MainToolBar = new JButton();
		button_Mails_MainToolBar.setText(Lang.getInstance().translate("Mails"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button_Mails_MainToolBar.setFocusable(false);
		button_Mails_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button_Mails_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button_Mails_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.library.library.selectOrAdd( new Mails_Main_Frame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button_Mails_MainToolBar);
       	
       	
       	
       	
    	JButton button11_MainToolBar = new JButton();
		button11_MainToolBar.setText(Lang.getInstance().translate("Documents"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button11_MainToolBar.setFocusable(false);
		button11_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button11_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button11_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.library.library.selectOrAdd( new Main_Hash_Document_Frame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button11_MainToolBar);
       	
       	
       	
       	
       	JButton button_Statements_MainToolBar = new JButton();
       	button_Statements_MainToolBar.setText(Lang.getInstance().translate("Statements"));
		    //    button2_MainToolBar.setActionCommand("button1_Main_Panel");
       	button_Statements_MainToolBar.setFocusable(false);
       	button_Statements_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
       	button_Statements_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
       	button_Statements_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
       		public void actionPerformed(java.awt.event.ActionEvent evt) {
       			gui.library.library.selectOrAdd( new MainStatementsFrame(), MainFrame.desktopPane.getAllFrames());
       		}
       	});
       	
       	
       	Toolbar_Main.add(button_Statements_MainToolBar);
       	
       	
       	
       	
       	
       	
       	
       	
       	
       	
       	
       	
       	
       	JButton button3_MainToolBar = new JButton();
		button3_MainToolBar.setText(Lang.getInstance().translate("Assets"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button3_MainToolBar.setFocusable(false);
		button3_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button3_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button3_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.library.library.selectOrAdd( new MainAssetsFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button3_MainToolBar);	

		JButton button4_MainToolBar = new JButton();
		button4_MainToolBar.setText(Lang.getInstance().translate("Imprints"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button4_MainToolBar.setFocusable(false);
		button4_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button4_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button4_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.library.library.selectOrAdd( new MainImprintsFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button4_MainToolBar);
		        	
		JButton button5_MainToolBar = new JButton();
		button5_MainToolBar.setText(Lang.getInstance().translate("Unions"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button5_MainToolBar.setFocusable(false);
		button5_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button5_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button5_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.library.library.selectOrAdd( new MainUnionsFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button5_MainToolBar);
		
		
		JButton button41_MainToolBar = new JButton();
		button41_MainToolBar.setText(Lang.getInstance().translate("Templates"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button41_MainToolBar.setFocusable(false);
		button41_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button41_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button41_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.library.library.selectOrAdd( new MainNotesFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button41_MainToolBar);
		
		
		
		
		        	
		JButton button6_MainToolBar = new JButton();
		button6_MainToolBar.setText(Lang.getInstance().translate("Statuses"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button6_MainToolBar.setFocusable(false);
		button6_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button6_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button6_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.library.library.selectOrAdd( new MainStatusesFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button6_MainToolBar);
	/*	        	
		JButton button7_MainToolBar = new JButton();
		button7_MainToolBar.setText(Lang.getInstance().translate("Records"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button7_MainToolBar.setFocusable(false);
		button7_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button7_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button7_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new RecordsFrame(parent), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button7_MainToolBar);
	*/	
		
		JButton button9_MainToolBar = new JButton();
		button9_MainToolBar.setText(Lang.getInstance().translate("Votings"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button9_MainToolBar.setFocusable(false);
		button9_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button9_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button9_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.library.library.selectOrAdd( new MainVotingsFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button9_MainToolBar);
		
		
		
	
		
		
		
		
		
		JButton button10_MainToolBar = new JButton();
		button10_MainToolBar.setText(Lang.getInstance().translate("Records"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button10_MainToolBar.setFocusable(false);
		button10_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button10_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button10_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.library.library.selectOrAdd( new Records_Main_Frame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button10_MainToolBar);
		
		
		
		
		JButton button8_MainToolBar = new JButton();
		button8_MainToolBar.setText(Lang.getInstance().translate("DashBoard"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button8_MainToolBar.setFocusable(false);
		button8_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button8_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button8_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.library.library.selectOrAdd( new Other_Internal_Frame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button8_MainToolBar);
		
		
		
		
		
		
		
				//add(tb1, BorderLayout.NORTH); 
				add(Toolbar_Main, BorderLayout.NORTH);

		

		//MENU
     //   Menu menu = new Menu(this);

        //ADD MENU TO FRAME
     //   this.setJMenuBar(menu);
        
     
        // 
        addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@SuppressWarnings({ "unchecked", "unused" })
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
			
			
			String json = null;
			ArrayList<Frame_Class> frame_Classes = new ArrayList<Frame_Class>();
			
			JInternalFrame[] s = MainFrame.desktopPane.getAllFrames();
			
			
			
			json = "{\"Main Frame\":" + "{"
			+ "\"location_X\":\""+ e.getWindow().getLocation().x +"\","
			+ "\"location_Y\":\""+ e.getWindow().getLocation().y +"\","
			+ "\"width\":\""+ e.getWindow().getWidth() +"\","
			+ "\"height\":\""+ e.getWindow().getHeight() +"\""
			+ "}";
			
			
			int s1 = e.getWindow().getX();
			int s2 = e.getWindow().getWidth();
			
			
			for (int i=MainFrame.desktopPane.getAllFrames().length-1; i >= 0; i--) {
			
				
				frame_Classes.add(new Frame_Class(s[i].getClass().getCanonicalName(),s[i].getLocation().x,s[i].getLocation().y,s[i].getSize().width,s[i].getSize().height));
						
			}
			
			if (!frame_Classes.isEmpty()){
			
				Gson gson = new Gson();
				json = json +", \"Open Frames\": " + gson.toJson(frame_Classes) ; 
			}
			
			json = json + "}";
			try {
				SaveStrToFile.saveJsonFine_not_Convert(Settings.getInstance().getGuiSettingPath(), json);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
		
		}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
				JInternalFrame object = null;
				
				
				String stringFromInternet = "";
				
							
				try {
					stringFromInternet= FileUtils.readFileToString(new File(Settings.getInstance().getGuiSettingPath()));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				JsonParser parser = new JsonParser(); 
				
				if (!stringFromInternet.isEmpty()){
					
					JsonObject mainObject = parser.parse(stringFromInternet).getAsJsonObject();
					
					
			//		JsonObject mainFrameJSON = mainObject.getAsJsonObject("Main Frame");
					
			//		parent.setLocation(Integer.valueOf(mainFrameJSON.get("location_X").getAsString()), Integer.valueOf(mainFrameJSON.get("location_Y").getAsString()));
			//		parent.setSize(Integer.valueOf(mainFrameJSON.get("width").getAsString()), Integer.valueOf(mainFrameJSON.get("height").getAsString()));
					
				
					JsonArray pItem = mainObject.getAsJsonArray("Open Frames");
					if (pItem!=null && !pItem.isJsonNull()){
						for (JsonElement user : pItem) {
		
						    JsonObject userObject = user.getAsJsonObject(); 
						  
						    String str = userObject.get("name").getAsString();
						  
					    	try {
								object  = (JInternalFrame) Class.forName(str).newInstance();
								object.setLocation(Integer.valueOf( userObject.get("location_X").toString()), Integer.valueOf(userObject.get("location_Y").toString()));
								object.setSize(Integer.valueOf( userObject.get("size_X").toString()), Integer.valueOf(userObject.get("size_Y").toString()));
								gui.library.library.selectOrAdd( object, MainFrame.desktopPane.getAllFrames());
							} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
			} 
        	});
        
        
        
        // СЃРѕР·РґР°РµРј СЂР°Р±РѕС‡РёР№ СЃС‚РѕР» Swing
        desktopPane = new JDesktopPane();
        //desktopPane.setBackground(new Color(255, 255, 255, 255));//Color.LIGHT_GRAY);
        desktopPane.setBackground(MainFrame.getFrames()[0].getBackground());

        /*
        JInternalFrame item = new AccountsFrame(this);
        item.setVisible(true);
		MainFrame.desktopPane.add(item);
		try {
			 item.setSelected(true);
	        } catch (java.beans.PropertyVetoException e1) {}
	        */


      //  desktopPane.setSize(500, 300);
        // РґРѕР±Р°РІР»СЏРµРј РµРіРѕ РІ С†РµРЅС‚СЂ РѕРєРЅР°
        add(desktopPane);
        
        // WALLET STATS
        this.add(new StatusPanel(), BorderLayout.SOUTH);
        
        //CLOSE NICELY
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
            	new ClosingDialog();
            }
        });
        
   
      //set location and size
            
        
        String stringFromInternet = "";
		try {
			stringFromInternet= FileUtils.readFileToString(new File(Settings.getInstance().getGuiSettingPath()));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		JsonParser parser = new JsonParser(); 
		
		if (!stringFromInternet.isEmpty() && stringFromInternet != "" ){
		JsonObject mainObject = parser.parse(stringFromInternet).getAsJsonObject();
		
		
		JsonObject mainFrameJSON = mainObject.getAsJsonObject("Main Frame");
		
		setLocation(Integer.valueOf(mainFrameJSON.get("location_X").getAsString()), Integer.valueOf(mainFrameJSON.get("location_Y").getAsString()));
		setSize(Integer.valueOf(mainFrameJSON.get("width").getAsString()), Integer.valueOf(mainFrameJSON.get("height").getAsString()));	
    	
		}
		else{
			 Toolkit kit = Toolkit.getDefaultToolkit();

		        Dimension screens = kit.getScreenSize();

		        int w,h;

		        w = screens.width;

		        setSize((int) (w/1.3),(int) (w/1.3/1.618));

		        setLocation(w/12, w/12);
		       	
			
		}
        
        
        
        //SHOW FRAME
      //  this.pack();
     //   this.setLocationRelativeTo(null);
        this.setVisible(true);
      //  desktopPane.add(new AllPersonsFrame(this));
    //    desktopPane.add(new MainImprintsFrame());
        
	}


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
		
		
		
		// TODO Auto-generated method stub
		
	}
}

  class Frame_Class  {
    
    String name;
    int location_X;
    int location_Y;
    int size_X;
    int size_Y;
    

    public  Frame_Class (String name, int location_X, int location_Y, int size_X, int size_Y) {
       
        this.name = name;
        this.location_X=location_X;
        this.location_Y=location_Y;
        this.size_X=size_X;
        this.size_Y=size_Y;
    }
}
