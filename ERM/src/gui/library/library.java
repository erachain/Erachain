package gui.library;

import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.UIManager;

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
	

	public static void Set_GUI_Font(String text) {
		
		int size_font;
		if ( text == ""){
			size_font =new Integer(Settings.getInstance().get_Font());
		}
		else{
		size_font =new Integer(text);
		}
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
	      UIManager.put("TextArea.font", font);
	     	
		
	}

}
