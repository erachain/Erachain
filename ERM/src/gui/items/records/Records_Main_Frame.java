package gui.items.records;

import java.awt.Dimension;
import javax.swing.JInternalFrame;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
import lang.Lang;


public class Records_Main_Frame extends Main_Internal_Frame{
	private static final long serialVersionUID = 2717571093561259483L;


	Split_Panel search_Records_SplitPanel;
	Split_Panel my_Records_SplitPanel;
	


	
	public Records_Main_Frame(){
	
		// not show buttons main Toolbar
		this.setTitle(Lang.getInstance().translate("Records"));
		this.jToolBar.setVisible(false);
	
		this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with Records"));
	
		///////////////////////
		// ALL PERSONS
		///////////////////////
		
		search_Records_SplitPanel = new Persons_Search_SplitPanel();
		
	 
		//////////////////////////////////////	
		// MY PERSONS
		//////////////////////////////////////
		my_Records_SplitPanel = new Records_My_SplitPanel();
	
		
///////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
		
		this.jTabbedPane.add(my_Records_SplitPanel);
		
		this.jTabbedPane.add(search_Records_SplitPanel);
			
		this.pack();
		
		this.setMaximizable(true);
		
		this.setClosable(true);
		this.setResizable(true);
		this.setLocation(20, 20);
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
	    this.setResizable(true);
	    this.setVisible(true);
	    Dimension size = MainFrame.desktopPane.getSize();
	    this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	    search_Records_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	 	my_Records_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	}


	



	
	
	
	
	
	
}


