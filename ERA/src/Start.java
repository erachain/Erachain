import gui.AboutFrame;
import gui.Gui;
import gui.create.License_JFrame;
import gui.library.Issue_Confirm_Dialog;

// 30/03
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import lang.Lang;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import api.ApiClient;
import controller.Controller;
import core.BlockChain;
import core.item.assets.AssetCls;
import core.item.notes.NoteCls;
import database.DBSet;
import settings.Settings;
import utils.SysTray;

public class Start {
	
	
	static Logger LOGGER = Logger.getLogger(Start.class.getName());
	private static AboutFrame about_frame;
	private static String info;

	public static void main(String args[]) throws IOException
	{	
		
		about_frame = AboutFrame.getInstance();
		about_frame.setUserClose(false);
		////
		File log4j = new File("log4j.properties");
		if(log4j.exists())
		{
			PropertyConfigurator.configure(log4j.getAbsolutePath());
		}else
		{
			try( InputStream resourceAsStream = ClassLoader.class.getResourceAsStream("/log4j/log4j.default");)
			{
				PropertyConfigurator.configure(resourceAsStream);
				LOGGER.error("log4j.properties not found, search path is " + log4j.getAbsolutePath() + " using default!");
			}
		}
		
		boolean cli = false;
		boolean nogui = false;
		
		for(String arg: args)
		{
			if(arg.equals("-cli"))
			{
				cli = true;
			} 
			else 
			{
				if(arg.equals("-nogui"))
					nogui = true;
					
				if(arg.startsWith("-peers=") && arg.length() > 7) 
				{
					Settings.getInstance().setDefaultPeers(arg.substring(7).split(","));
				}
				
				if(arg.equals("-testnet")) 
				{
					Settings.getInstance().setGenesisStamp(System.currentTimeMillis());
				} 
				else if(arg.startsWith("-testnet=") && arg.length() > 9) 
				{
					try
					{
						long testnetstamp = Long.parseLong(arg.substring(9));
						
						if (testnetstamp == 0)
						{
							testnetstamp = System.currentTimeMillis();
						}
							
						Settings.getInstance().setGenesisStamp(testnetstamp);
					} catch(Exception e) {
						Settings.getInstance().setGenesisStamp(BlockChain.DEFAULT_MAINNET_STAMP);
					}
				}
			}
		}
		
		if(!cli)
		{			
			try
			{
				
				//ONE MUST BE ENABLED
				if(!Settings.getInstance().isGuiEnabled() && !Settings.getInstance().isRpcEnabled())
				{
					throw new Exception(Lang.getInstance().translate("Both gui and rpc cannot be disabled!"));
				}
				
				info = Lang.getInstance().translate("Starting %app% / version: %version% / build date: %builddate% / ...")
						.replace("%app%", Lang.getInstance().translate(controller.Controller.APP_NAME))
						.replace("%version%", Controller.getVersion())
						.replace("%builddate%", Controller.getBuildDateString());
				LOGGER.info(info
						);
				about_frame.set_console_Text(info);
				
				//STARTING NETWORK/BLOCKCHAIN/RPC
				Controller.getInstance().start();
				
				if (nogui) {
					LOGGER.info("-nogui used");
				} else {
					
					try
					{
						Thread.sleep(100);

					//START GUI
						
						if(Gui.getInstance() != null && Settings.getInstance().isSysTrayEnabled())
						{					
							SysTray.getInstance().createTrayIcon();
							about_frame.setVisible(false);
							about_frame.getInstance().dispose();
							//Controller.getInstance().setWalletLicense(0); // TEST
							if (Controller.getInstance().doesWalletExists() &&
									Controller.LICENSE_KEY > Controller.getInstance().getWalletLicense()) {
								// TODO: тут нужно чтобы лицензия вызывалась для подтверждения и если НЕТ то закрывать прогу сразу
						        //ItemCls.NOTE_TYPE
						        NoteCls note = (NoteCls)DBSet.getInstance().getItemNoteMap().get(Controller.LICENSE_KEY);
						        if (note == null) {
						        	// USE default LICENSE
							        note = (NoteCls)DBSet.getInstance().getItemNoteMap().get(2l);
						        }
								new License_JFrame(note);
								Controller.getInstance().setWalletLicense(note.getKey());
							}
						}
					} catch(Exception e1) {
						about_frame.setVisible(false);
						about_frame.dispose();
						LOGGER.error(Lang.getInstance().translate("GUI ERROR - at Start") ,e1);
					}
				}
				
				//Controller.getInstance().isTestnet();

				
			} catch(Exception e) {
				
				LOGGER.error(e.getMessage(),e);
			// show error dialog	
				if (Settings.getInstance().isGuiEnabled()){
				 Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(null, true, Lang.getInstance().translate("STARTUP ERROR") + ": " + e.getMessage() , 600, 400, Lang.getInstance().translate(" "));
				 dd.jButton1.setVisible(false);
				 dd.jButton2.setText(Lang.getInstance().translate("Cancel"));
				 dd.setLocationRelativeTo(null);
				 dd.setVisible(true);
				}
				
				//USE SYSTEM STYLE
		        try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e2) {
					LOGGER.error(e2.getMessage(),e2);
				}
				
				//ERROR STARTING
				LOGGER.error(Lang.getInstance().translate("STARTUP ERROR") + ": " + e.getMessage());
				
				if(Gui.isGuiStarted())
				{
					JOptionPane.showMessageDialog(null, e.getMessage(), Lang.getInstance().translate("Startup Error"), JOptionPane.ERROR_MESSAGE);
				}
				
				
				about_frame.setVisible(false);
				about_frame.dispose();
				 //FORCE SHUTDOWN
				System.exit(0);
			}
		}
		else
		{
			Scanner scanner = new Scanner(System.in);
			ApiClient client = new ApiClient();
			
			while(true)
			{
				
				System.out.print("[COMMAND] ");
				String command = scanner.nextLine();
				
				if(command.equals("quit"))
				{

					about_frame.setVisible(false);
					about_frame.dispose();
					scanner.close();
					System.exit(0);
				}
				
				String result = client.executeCommand(command);
				LOGGER.info("[RESULT] " + result);
			}
		}		
	}
}
