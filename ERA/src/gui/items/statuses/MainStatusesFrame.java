package gui.items.statuses;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.List;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

import javax.swing.DefaultRowSorter;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.RunMenu;
import gui.Split_Panel;
import gui.items.assets.AssetFrame;
import gui.items.assets.TableModelItemAssets;
import gui.items.persons.MainPersonsFrame;
import gui.items.persons.PersonConfirmDialog;
import gui.items.persons.PersonSetStatusDialog;
import gui.library.MTable;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemStatusesTableModel;
import lang.Lang;

public class MainStatusesFrame extends Main_Internal_Frame{
	private static final long serialVersionUID = 1L;
	private  TableModelItemStatuses tableModelItemStatuses;
	MTable statusesTable ;
	private WalletItemStatusesTableModel statusesModel;
	private MTable table;
	

	
	public MainStatusesFrame(){

		this.setTitle(Lang.getInstance().translate("Statuses"));
		this.jButton2_jToolBar.setVisible(false);
		this.jButton3_jToolBar.setVisible(false);
		// buttun1
		this.jButton1_jToolBar.setText(Lang.getInstance().translate("Issue Status"));
		this.jButton1_jToolBar.setVisible(false);
		// status panel
		//this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with statuses"));
	 
		this.jButton1_jToolBar.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
		    	 new IssueStatusDialog();
			}
		});	
		this.jToolBar.setVisible(false);
		 
		// all statuses 
		Search_Statuses_Tab search_Status_SplitPanel= new Search_Statuses_Tab();
		
		 My_Statuses_Tab my_Status_SplitPanel = new My_Statuses_Tab(); 
	
	// issue status
			
		 JPanel issuePanel = new IssueStatusPanel();
		 issuePanel.setName(Lang.getInstance().translate("Create Status"));	
			
	
		this.jTabbedPane.add(my_Status_SplitPanel);
		
		this.jTabbedPane.add(search_Status_SplitPanel);
		
		this.jTabbedPane.add(issuePanel);
		
		this.pack();
	//	this.setSize(800,600);
		this.setMaximizable(true);
		
		this.setClosable(true);
		this.setResizable(true);
	//	this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		this.setLocation(20, 20);
	//	this.setIconImages(icons);
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
	    this.setResizable(true);
	//    splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
	    //my_status_panel.requestFocusInWindow();
	    this.setVisible(true);
	    Rectangle k = this.getNormalBounds();
	 //   this.setBounds(k);
	    Dimension size = MainFrame.getInstance().desktopPane.getSize();
	    this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	 // setDividerLocation(700)
	
	 	search_Status_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	 	my_Status_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));

	}
	
	
	
}