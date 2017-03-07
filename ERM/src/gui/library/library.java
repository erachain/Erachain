package gui.library;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/*
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.skin.*;
import org.jvnet.substance.skin.SubstanceNebulaBrickWallLookAndFeel;
*/

import gui.MainFrame;
import settings.Settings;

public class library {
	
	
	// подпрограмма выводит в панели окно или передает фокус если окно уже открыто
	// item открываемое окно
	// массив всех открытых окон в панели
	public static void selectOrAdd(JInternalFrame item, JInternalFrame[] openedFrames ){
		    		
		//проверка если уже открыто такое окно то передаем только фокус на него
		String itemName = item.getName();
		if (itemName == null) itemName  = item.getClass().getName();
		
		int k= -1;
		if (openedFrames != null) 
		{
			for (int i=0 ; i < openedFrames.length; i=i+1) {
				String name = openedFrames[i].getName();
				if (name == null) name  = openedFrames[i].getClass().getName();
				if (name == itemName){
					k=i;
				}
			};
		}
			
		if (k==-1){
			MainFrame.desktopPane.add(item);
			try {
				 item.setSelected(true);
		        } catch (java.beans.PropertyVetoException e1) {}
		} else {
			try {
				openedFrames[k].setSelected(true);
			} catch (PropertyVetoException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}	
	
	}
	

	public static void Set_GUI_Look_And_Feel(String text) {
		String name_font = "Courier";
		int size_font;
		
		
		//USE SYSTEM STYLE
		   //     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	
		
	
		

				Toolkit.getDefaultToolkit().setDynamicLayout(true);
				System.setProperty("sun.awt.noerasebackground", "true");
				JFrame.setDefaultLookAndFeelDecorated(true);
				JDialog.setDefaultLookAndFeelDecorated(true);
				
/*
				try {
				    UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
				    SwingUtilities.updateComponentTreeUI(this);
				} catch(Exception ex) {
				    ex.printStackTrace();
				}
				*/
				try {
					UIManager.setLookAndFeel ( MLookAndFeel.class.getCanonicalName () );
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		
		
		
		
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
	      UIManager.put( "InternalFrame.titleFont",font);
	     
	      UIManager.put( "TextPane.font", font ); 
	   //   UIManager.put( "ScrollBar.minimumThumbSize", new Dimension(20,30) );
	      UIManager.put("ScrollBar.minimumThumbSize", new Dimension(25,25));
	      UIManager.put("Table.height", size_font*5);
	      UIManager.put("TextArea.font", font);
	      
	      
	        UIManager.put("RadioButton.focus", new Color(0, 0, 0, 0));
	        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
	        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
	        UIManager.put("ComboBox.focus", new Color(0, 0, 0, 0));
	   //     UIManager.put("TextArea.font", UIManager.get("TextField.font"));

	     	
		
	}

	/*
	 public static void setupSubstance() {
	        try {
	            final String fileName = System.getProperty("user.home") + System.getProperty("file.separator") + "insubstantial.txt";
	            final Properties properties = new Properties();
	           LookAndFeel laf = new SubstanceGeminiLookAndFeel();
	            UIManager.setLookAndFeel(laf);
	            UIManager.put(SubstanceGeminiLookAndFeel.SHOW_EXTRA_WIDGETS, Boolean.TRUE);
	            JFrame.setDefaultLookAndFeelDecorated(true);
	            JDialog.setDefaultLookAndFeelDecorated(true);
	            Runtime.getRuntime().addShutdownHook(new Thread() {
	                @Override public void run() {
	                    try {
	                        String skinClassName = SubstanceLookAndFeel.getCurrentSkin().getClass().getCanonicalName();
	                        properties.setProperty("skinClassName", skinClassName);
	                        properties.store(new FileOutputStream(fileName), fileName);
	                    } catch (Throwable t) {
	                        t.printStackTrace();
	                    }
	                }
	            });
	            properties.load(new FileInputStream(fileName));
	            String skinClassName = properties.getProperty("skinClassName");
	            ((SubstanceLookAndFeel) laf).setSkin(skinClassName);
	        } catch (Throwable t) {
	            t.printStackTrace();
	        }
	    }
	    */

}
