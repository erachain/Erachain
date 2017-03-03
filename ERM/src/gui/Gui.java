package gui;

// 16/03
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.TrayIcon.MessageType;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import gui.create.NoWalletFrame;
import gui.create.SettingLangFrame;
import gui.library.MTable;
import lang.Lang;
import settings.Settings;
import utils.SysTray;

public class Gui extends JFrame{

	//private static final long serialVersionUID = 1L;
	private static final long serialVersionUID = 2717571093561259483L;


	private static Gui maingui;
	private MainFrame mainframe;
	public static Gui getInstance() throws Exception
	{
		if(maingui == null)
		{
			maingui = new Gui();
		}
		
		return maingui;
	}
	
	private Gui() throws Exception
	{
		//USE SYSTEM STYLE
		   //     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				

				Toolkit.getDefaultToolkit().setDynamicLayout(true);
				System.setProperty("sun.awt.noerasebackground", "true");
				JFrame.setDefaultLookAndFeelDecorated(true);
				JDialog.setDefaultLookAndFeelDecorated(true);

				try {
				    UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
				    SwingUtilities.updateComponentTreeUI(this);
				} catch(Exception ex) {
				    ex.printStackTrace();
				}
				
        
				
			/*	
				int size_font = 24;
			      Font font = new Font("Courier", Font.PLAIN, size_font);
			      UIManager.put("Button.font", font);
			      UIManager.put("Table.font", font);
			      UIManager.put("Label.font", font);
			      UIManager.put("ComboBox.font", font);
			      UIManager.put("TextField.font", font);
			      UIManager.put("TableHeader.font", font);
			      UIManager.put("TabbedPane.font", font);
			      UIManager.put("RadioButton.font", font);
			      UIManager.put("ComboBox.font", font);
			      UIManager.put("CheckBox.font", font);
			 
			      UIManager.put("Menu.font", font);
			      UIManager.put("MenuItem.font", font);
			      UIManager.put("Frame.titleFont", font);
			      UIManager.put("InternalFrame.font",font);
			         
			      UIManager.put( "TextPane.font", font ); 
			   //   UIManager.put( "ScrollBar.minimumThumbSize", new Dimension(20,30) );
			      UIManager.put("ScrollBar.minimumThumbSize", new Dimension(25,25));
			      UIManager.put("Table.height", size_font*5);
			*/      
			     
		//	      UIManager.put("Label.foreground", Color.GREEN);
			      
        
        UIManager.put("RadioButton.focus", new Color(0, 0, 0, 0));
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
        UIManager.put("ComboBox.focus", new Color(0, 0, 0, 0));
        UIManager.put("TextArea.font", UIManager.get("TextField.font"));

        
        if(Settings.getInstance().Dump().containsKey("lang"))
        {
        	if(!Settings.getInstance().getLang().equals(Settings.DEFAULT_LANGUAGE))
        	{
	        	File langFile = new File( Settings.getInstance().getLangDir(), Settings.getInstance().getLang() );
				if ( !langFile.isFile() ) {
					new SettingLangFrame();	
				}
        	}
        } 
        else
        {
        	new SettingLangFrame();
        } 
        
        //CHECK IF WALLET EXISTS
        if(!Controller.getInstance().doesWalletExists())
        {
        	//OPEN WALLET CREATION SCREEN
        	new NoWalletFrame(this);
        } else if (Settings.getInstance().isGuiEnabled())
    	{
    		mainframe =	new MainFrame();
    		mainframe.setVisible(true);
    	}
        
	}
	
	public static boolean isGuiStarted()
	{
		return maingui != null;
	}
	
	public void onWalletCreated()
	{

		SysTray.getInstance().sendMessage(Lang.getInstance().translate("Wallet Initialized"),
				Lang.getInstance().translate("Your wallet is initialized"), MessageType.INFO);
		if (Settings.getInstance().isGuiEnabled())
			mainframe = new MainFrame();
	}
	
	public void bringtoFront()
	{
		if(mainframe != null)
		{
			mainframe.toFront();
		}
	}

	public void hideMainFrame()
	{
		if(mainframe != null)
		{
			mainframe.setVisible(false);
		}
	}
	
	public void onCancelCreateWallet() 
	{
		Controller.getInstance().stopAll();
		System.exit(0);
	}
	
	public static <T extends TableModel> MTable createSortableTable(T tableModel, int defaultSort)
	{
		//CREATE TABLE
		MTable table = new MTable(tableModel);
		
		//CREATE SORTER
		TableRowSorter<T> rowSorter = new TableRowSorter<T>(tableModel);
		//drowSorter.setSortsOnUpdates(true);
		
		//DEFAULT SORT DESCENDING
		rowSorter.toggleSortOrder(defaultSort);	
		rowSorter.toggleSortOrder(defaultSort);	
		
		//ADD TO TABLE
		table.setRowSorter(rowSorter);
		
		//RETURN
		return table;
	}

	public static <T extends TableModel> MTable createSortableTable(T tableModel, int defaultSort, RowFilter<T, Object> rowFilter)
	{
		//CREATE TABLE
		MTable table = new MTable(tableModel);
		
		//CREATE SORTER
		TableRowSorter<T> rowSorter = new TableRowSorter<T>(tableModel);
		//rowSorter.setSortsOnUpdates(true);
		rowSorter.setRowFilter(rowFilter);
		
		//DEFAULT SORT DESCENDING
		rowSorter.toggleSortOrder(defaultSort);	
		rowSorter.toggleSortOrder(defaultSort);	
		
		//ADD TO TABLE
		table.setRowSorter(rowSorter);
		
		//RETURN
		return table;
	}
	
}
