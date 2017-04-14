package gui.items.records;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import core.transaction.Transaction;
import database.TransactionMap;
import gui.CoreRowSorter;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
import gui.models.Debug_Transactions_Table_Model;
import gui.models.TransactionsTableModel;
import gui.transaction.TransactionDetailsFactory;
import lang.Lang;


public class Records_Main_Frame extends Main_Internal_Frame{
	private static final long serialVersionUID = 2717571093561259483L;


	Split_Panel search_Records_SplitPanel;
	Split_Panel my_Records_SplitPanel;


	private Debug_Transactions_Table_Model transactionsTableModel;


	private JTable transactionsTable;


	private Records_UnConfirmed_Panel unConfirmed_Records_SplitPanel;
	


	
	public Records_Main_Frame(){
	
		// not show buttons main Toolbar
		this.setTitle(Lang.getInstance().translate("Records"));
		this.jToolBar.setVisible(false);
	
		//this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with Records"));
	
		///////////////////////
		// search pecords
		///////////////////////
		
		search_Records_SplitPanel = new Records_Search_SplitPanel();
		
	 
		//////////////////////////////////////	
		// MY records
		//////////////////////////////////////
		my_Records_SplitPanel = new Records_My_SplitPanel();

////////////////////////////////////////////////////////////////////////
		// search from adress
		
//		Records_Search_From_Addres_SplitPanel search_from_adress = new Records_Search_From_Addres_SplitPanel();
	
		
///////////////////////////////////////////////////////////////////////
		
	// unconfirmed Records
		
		unConfirmed_Records_SplitPanel = new Records_UnConfirmed_Panel();
////////////////////////////////////////////////////////////////////////
		
		this.jTabbedPane.add(my_Records_SplitPanel);
		
		this.jTabbedPane.add(search_Records_SplitPanel);
		
//		this.jTabbedPane.add(search_from_adress);
		
		this.jTabbedPane.add(unConfirmed_Records_SplitPanel);
		
		
	
		
		
		
		
		
		
			
		this.pack();
		
		this.setMaximizable(true);
		
		this.setClosable(true);
		this.setResizable(true);
		this.setLocation(20, 20);
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
	    this.setResizable(true);
	    this.setVisible(true);
	    Dimension size = MainFrame.getInstance().desktopPane.getSize();
	    this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	    search_Records_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	 	my_Records_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	}


	



	
	
	
	
	
	
}


