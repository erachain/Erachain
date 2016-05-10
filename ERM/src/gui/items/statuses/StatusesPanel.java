package gui.items.statuses;


import utils.ObserverMessage;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import core.item.statuses.StatusCls;
import gui.CoreRowSorter;
import gui.items.ItemsPanel;
import gui.items.statuses.AllStatusesFrame;
import gui.items.statuses.StatusFrame;
import gui.items.statuses.IssueStatusPanel;
//import gui.items.statuses.MyOrdersFrame;
//import gui.items.statuses.PayDividendFrame;
import gui.models.WalletItemStatusesTableModel;
import lang.Lang;

/*
@SuppressWarnings("serial")
public class StatusesPanel extends ItemsPanel
{
	public StatusesPanel(WalletItemStatusesTableModel itemsModel)
	{		
		super(itemsModel, "All Statuses", "Issue Status");
	}
}
*/	
@SuppressWarnings("serial")
public class StatusesPanel extends JPanel
{
	public StatusesPanel()
	{
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.fill = GridBagConstraints.BOTH; 
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;
		tableGBC.weighty = 1;
		tableGBC.gridwidth = 10;
		tableGBC.gridx = 0;	
		tableGBC.gridy= 0;	
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(10, 0, 0, 10);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridx = 0;
		buttonGBC.gridy = 1;
		
		//TABLE
		final WalletItemStatusesTableModel statusesModel = new WalletItemStatusesTableModel();
		final JTable table = new JTable(statusesModel);
		
		//POLLS SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		CoreRowSorter sorter = new CoreRowSorter(statusesModel, indexes);
		table.setRowSorter(sorter);

		/*
		//CHECKBOX FOR DIVISIBLE
		TableColumn divisibleColumn = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_DIVISIBLE);
		divisibleColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		*/
		
		//CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_CONFIRMED);
		confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		
		//MENU
		JPopupMenu statusesMenu = new JPopupMenu();
		JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				StatusCls status = (StatusCls)statusesModel.getItem(row);
				new StatusFrame(status);
			}
		});
		statusesMenu.add(details);
		
		/*
		JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
		dividend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				StatusCls status = (StatusCls)statusesModel.getItem(row);	
				new PayDividendFrame(status);
			}
		});
		statusesMenu.add(dividend);
		*/
		
		table.setComponentPopupMenu(statusesMenu);
		
		//MOUSE ADAPTER
		table.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = table.rowAtPoint(p);
				table.setRowSelectionInterval(row, row);
		     }
		});
		
		table.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = table.rowAtPoint(p);
				table.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = table.convertRowIndexToModel(row);
					StatusCls status = (StatusCls)statusesModel.getItem(row);
					new StatusFrame(status);
				}
		     }
		});
		
		//ADD NAMING SERVICE TABLE
		this.add(new JScrollPane(table), tableGBC);
		
		//ADD REGISTER BUTTON
		JButton issueButton = new JButton(Lang.getInstance().translate("Issue Status"));
		issueButton.setPreferredSize(new Dimension(120, 25));
		issueButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onIssueClick();
		    }
		});	
		this.add(issueButton, buttonGBC);
		
		//ADD ALL BUTTON
		buttonGBC.gridx = 1;
		JButton allButton = new JButton(Lang.getInstance().translate("All Statuses"));
		allButton.setPreferredSize(new Dimension(120, 25));
		allButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onAllClick();
			}
		});	
		this.add(allButton, buttonGBC);
		
		//ADD MY ORDERS BUTTON
		buttonGBC.gridx = 2;
		JButton myOrdersButton = new JButton(Lang.getInstance().translate("My Orders"));
		myOrdersButton.setPreferredSize(new Dimension(120, 25));
		myOrdersButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onMyOrdersClick();
			}
		});	
		this.add(myOrdersButton, buttonGBC);
	}
	
	public void onIssueClick()
	{
		//new IssueStatusPanel();
	}
	
	public void onAllClick()
	{
		//new AllStatusesFrame();
	}
	
	public void onMyOrdersClick()
	{
		//new MyOrdersFrame();
	}
}
