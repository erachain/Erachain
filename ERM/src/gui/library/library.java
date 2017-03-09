package gui.library;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.mapdb.Fun.Tuple2;

import de.muntjak.tinylookandfeel.Theme;
import de.muntjak.tinylookandfeel.ThemeDescription;

/*
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.skin.*;
import org.jvnet.substance.skin.SubstanceNebulaBrickWallLookAndFeel;
*/

import gui.MainFrame;
import settings.Settings;

public class library {

	// РїРѕРґРїСЂРѕРіСЂР°РјРјР° РІС‹РІРѕРґРёС‚ РІ РїР°РЅРµР»Рё РѕРєРЅРѕ РёР»Рё
	// РїРµСЂРµРґР°РµС‚ С„РѕРєСѓСЃ РµСЃР»Рё РѕРєРЅРѕ СѓР¶Рµ РѕС‚РєСЂС‹С‚Рѕ
	// item РѕС‚РєСЂС‹РІР°РµРјРѕРµ РѕРєРЅРѕ
	// РјР°СЃСЃРёРІ РІСЃРµС… РѕС‚РєСЂС‹С‚С‹С… РѕРєРѕРЅ РІ РїР°РЅРµР»Рё
	public static void selectOrAdd(JInternalFrame item, JInternalFrame[] openedFrames) {

		// РїСЂРѕРІРµСЂРєР° РµСЃР»Рё СѓР¶Рµ РѕС‚РєСЂС‹С‚Рѕ С‚Р°РєРѕРµ РѕРєРЅРѕ
		// С‚Рѕ РїРµСЂРµРґР°РµРј С‚РѕР»СЊРєРѕ С„РѕРєСѓСЃ РЅР° РЅРµРіРѕ
		String itemName = item.getName();
		if (itemName == null)
			itemName = item.getClass().getName();

		int k = -1;
		if (openedFrames != null) {
			for (int i = 0; i < openedFrames.length; i = i + 1) {
				String name = openedFrames[i].getName();
				if (name == null)
					name = openedFrames[i].getClass().getName();
				if (name == itemName) {
					k = i;
				}
			}
			;
		}

		if (k == -1) {
			MainFrame.desktopPane.add(item);
			try {
				item.setSelected(true);
			} catch (java.beans.PropertyVetoException e1) {
			}
		} else {
			try {
				openedFrames[k].setSelected(true);
			} catch (PropertyVetoException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	@SuppressWarnings("unchecked")
	public static void Set_GUI_Look_And_Feel(String text) {
		String name_font = "Courier";
		int size_font;
		
		
		// theme
		String name_Theme = Settings.getInstance().get_LookAndFell();
		
		if (name_Theme.equals("System")){
			
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
									
		if (name_Theme.equals("Metal")){
			
			try {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		
		
			if (name_Theme.equals("Other")){	
		
			

				try {
					
	
				    UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
					} catch(Exception ex) {
				    ex.printStackTrace();
				}
	
		}	
				/*	
				//USE SYSTEM STYLE
				        try {
				        	int a = 1;
				//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				//       	UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName()); // С‚РѕР¶Рµ С‡С‚Рѕ Рё РјРµС‚Р°Р»
			//	        	UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); // work
			//	        	UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");; // РЅРµ СѓРІРµР»РёС‡РёРІР°РµС‚ С€СЂРёС„С‚С‹
			//	        	UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");; // work
				    //    	UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); // works
				 //       	UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"); // works
				// UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName());
				//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");//
				//	com.sun.java.swing.plaf.gtk.GTKLookAndFeel
			//	com.sun.java.swing.plaf.motif.MotifLookAndFeel
			//	com.sun.java.swing.plaf.windows.WindowsLookAndFeel
				
				

						
		*/
								
	

		
				Toolkit.getDefaultToolkit().setDynamicLayout(true);
				System.setProperty("sun.awt.noerasebackground", "true");
				JFrame.setDefaultLookAndFeelDecorated(true);
				JDialog.setDefaultLookAndFeelDecorated(true);
		
				
				
				
		if ( text == ""){
			size_font =new Integer(Settings.getInstance().get_Font());
			name_font = Settings.getInstance().get_Font_Name();
		
		}
		else{
		size_font =new Integer(text);
		}
		
		
		
		
	      Font font = new Font(name_font, Font.TRUETYPE_FONT, size_font);
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
	      UIManager.put("FormattedTextField.font", font);
	      
	 
	      UIManager.put("Menu.font", font);
	      UIManager.put("MenuItem.font", font);
	      UIManager.put("Frame.titleFont", font);
	      UIManager.put("InternalFrame.font",font);
	      UIManager.put("InternalFrame.titleFont",font);
	     
	      UIManager.put("TextPane.font", font ); 
	   //   UIManager.put( "ScrollBar.minimumThumbSize", new Dimension(20,30) );
	    //  UIManager.put("ScrollBar.minimumThumbSize", new Dimension(25,25));
	    //  UIManager.put("Table.height", size_font*5);
	      UIManager.put("TextArea.font", font);
	      
	      UIManager.put("InternalFrame.paletteTitleFont", font);
	      UIManager.put("InternalFrame.normalTitleFont", font);
	      
	      UIManager.put("FileChooser.font",font);
	      
	      
	     
	  
	      UIManager.put("CheckBoxMenuItem.acceleratorFont",font);
	      UIManager.put("CheckBoxMenuItem.font",font);
	      UIManager.put("ColorChooser.font",font);
	  
	      UIManager.put("EditorPane.font",font);
	      UIManager.put("FormattedTextField.font",font);
	      UIManager.put("IconButton.font",font);
	      UIManager.put("InternalFrame.optionDialogTitleFont",font);
	   UIManager.put("InternalFrame.paletteTitleFont",font);
	   UIManager.put("InternalFrame.titleFont",font);
	UIManager.put("Label.font",font);
	 UIManager.put("List.font",font);
			 UIManager.put("Menu.acceleratorFont",font);
			 UIManager.put("Menu.font",font);
			 UIManager.put("MenuBar.font",font);
			 UIManager.put("MenuItem.acceleratorFont",font);
			UIManager.put("MenuItem.font",font);
			UIManager.put("OptionPane.buttonFont",font);
			UIManager.put("OptionPane.font",font);
			 UIManager.put("OptionPane.messageFont",font);
			UIManager.put("Panel.font",font);
			 UIManager.put("PasswordField.font",font);
		UIManager.put("PopupMenu.font",font);
		UIManager.put("ProgressBar.font",font);
		UIManager.put("RadioButton.font",font);
		UIManager.put("RadioButtonMenuItem.acceleratorFont",font);
		 UIManager.put("RadioButtonMenuItem.font",font);
		 UIManager.put("ScrollPane.fon",font);
		UIManager.put("Slider.font",font);
		UIManager.put("Spinner.font",font);
		UIManager.put("TabbedPane.font",font);
		UIManager.put("TabbedPane.smallFont",font);
		 UIManager.put("Table.font",font);
		 UIManager.put("TableHeader.font",font);
		 UIManager.put("TextArea.font",font);
		UIManager.put("TextField.font",font);
		UIManager.put("TextPane.font",font);
		 UIManager.put("TitledBorder.font",font);
		 UIManager.put("ToggleButton.font",font);
		UIManager.put("ToolBar.font",font);
		 UIManager.put("ToolTip.font",font);
		 UIManager.put("Tree.font",font);
	      
		 UIManager.put("TitledBorder.font",font);
				 UIManager.put("Panel.font",font);
		 
	      
	      
		// .setUIFont(new javax.swing.plaf.FontUIResource("Tahoma",Font.PLAIN,12));
		 
		 ArrayList<Tuple2<String,Object>> ss = new ArrayList<Tuple2<String, Object>>();
	      
		  
	      
	      
	      
	      
	 //       UIManager.put("RadioButton.focus", new Color(0, 0, 0, 0));
	 //       UIManager.put("Button.focus", new Color(0, 0, 0, 0));
	 //       UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
	//        UIManager.put("ComboBox.focus", new Color(0, 0, 0, 0));
	   //     UIManager.put("TextArea.font", UIManager.get("TextField.font"));

	       
	        int scrolH = (int)(size_font*1.2);
	        if (scrolH < 17) scrolH=17;
	        
	        UIManager.put("InternalFrame.titlePaneHeight",scrolH);
	        UIManager.put("InternalFrame.titleButtonHeight",scrolH);
	        UIManager.put("InternalFrame.titleButtonWidth",scrolH);
	     
	        
	        
	        Theme.scrollSize.setValue(scrolH);
	     	
	        Theme.internalPaletteTitleFont.setFont(font);
	        Theme.toolTipFont.setFont(font);
	 
	        java.util.Enumeration keys = UIManager.getDefaults().keys();
			 
		    while(keys.hasMoreElements())
		    {
		       
		    	
		    	String key = keys.nextElement().toString();
		    	if (key.contains("Title")){
		        Object value = UIManager.get(key);
		        ss.add(new Tuple2<String, Object>(key,value));
		//        if(value instanceof javax.swing.plaf.FontUIResource) UIManager.put(key, f);
		    	}
		    }
	      
	    font = font;
	       
	        
		
	}

	/*
	 * public static void setupSubstance() { try { final String fileName =
	 * System.getProperty("user.home") + System.getProperty("file.separator") +
	 * "insubstantial.txt"; final Properties properties = new Properties();
	 * LookAndFeel laf = new SubstanceGeminiLookAndFeel();
	 * UIManager.setLookAndFeel(laf);
	 * UIManager.put(SubstanceGeminiLookAndFeel.SHOW_EXTRA_WIDGETS,
	 * Boolean.TRUE); JFrame.setDefaultLookAndFeelDecorated(true);
	 * JDialog.setDefaultLookAndFeelDecorated(true);
	 * Runtime.getRuntime().addShutdownHook(new Thread() {
	 * 
	 * @Override public void run() { try { String skinClassName =
	 * SubstanceLookAndFeel.getCurrentSkin().getClass().getCanonicalName();
	 * properties.setProperty("skinClassName", skinClassName);
	 * properties.store(new FileOutputStream(fileName), fileName); } catch
	 * (Throwable t) { t.printStackTrace(); } } }); properties.load(new
	 * FileInputStream(fileName)); String skinClassName =
	 * properties.getProperty("skinClassName"); ((SubstanceLookAndFeel)
	 * laf).setSkin(skinClassName); } catch (Throwable t) { t.printStackTrace();
	 * } }
	 */

}
