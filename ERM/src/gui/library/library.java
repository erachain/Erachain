package gui.library;

import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;

import gui.MainFrame;

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

}
