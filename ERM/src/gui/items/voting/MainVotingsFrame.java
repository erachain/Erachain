package gui.items.voting;

import java.awt.Dimension;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.TableRowSorter;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
import lang.Lang;


public class MainVotingsFrame extends Main_Internal_Frame{
	private static final long serialVersionUID = 2717571093561259483L;



	Split_Panel search_Voting_SplitPanel;
	JTable votingsTable;
	RowSorter<?> search_Sorter;
	JTable table_My;
	TableRowSorter<?> sorter_My;
	
	Votings_My_SplitPanel my_Voting_SplitPanel;
	
// для прозрачности
     int alpha =255;
     int alpha_int;
	
	
	public MainVotingsFrame(){
	
		// not show buttons main Toolbar
		this.setTitle(Lang.getInstance().translate("Votings"));
		this.jToolBar.setVisible(false);
	
		//this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with Votings"));
	
		///////////////////////
		// ALL VOTING
		///////////////////////
		
		search_Voting_SplitPanel = new Votings_Search_SplitPanel();
		//////////////////////////////////////	
		// MY VOTING
		//////////////////////////////////////
		my_Voting_SplitPanel = new Votings_My_SplitPanel();
	
		
// issue Voting
		  JScrollPane Issue_Voting_Panel = new JScrollPane();
		  Issue_Voting_Panel.setName(Lang.getInstance().translate("Issue Voting"));
		  Issue_Voting_Panel.add(new Create_Voting_Panel());
		  Issue_Voting_Panel.setViewportView(new Create_Voting_Panel());
	
		this.jTabbedPane.add(my_Voting_SplitPanel);
		
		this.jTabbedPane.add(search_Voting_SplitPanel);
		this.jTabbedPane.add(Issue_Voting_Panel);
		
		this.pack();
		//	this.setSize(800,600);
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
	    search_Voting_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	 	my_Voting_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	}

// set favorine My
	void favorite_my(JTable table){
		int row = table.getSelectedRow();
		row = table.convertRowIndexToModel(row);
			table.repaint();

	}
	

	
	
}


