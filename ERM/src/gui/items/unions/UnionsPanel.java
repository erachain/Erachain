package gui.items.unions;


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
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import core.item.unions.UnionCls;
import gui.CoreRowSorter;
import gui.items.ItemsPanel;
import gui.items.unions.AllUnionsPanel;
import gui.items.unions.UnionFrame;
import gui.items.unions.IssueUnionFrame;
import gui.models.Renderer_Right;
//import gui.items.unions.MyOrdersFrame;
//import gui.items.unions.PayDividendFrame;
import gui.models.WalletItemUnionsTableModel;
import lang.Lang;

/*
@SuppressWarnings("serial")
public class UnionsPanel extends ItemsPanel
{
	public UnionsPanel(WalletItemUnionsTableModel itemsModel)
	{		
		super(itemsModel, "All Unions", "Issue Union");
	}
}
*/	
@SuppressWarnings("serial")
public class UnionsPanel extends JPanel
{
	public UnionsPanel()
	{
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.fill = GridBagConstraints.BOTH; 
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 0;
		tableGBC.weighty = 0;
		tableGBC.gridwidth = 10;
		tableGBC.gridx = 0;	
		tableGBC.gridy= 0;
		this.add(new JLabel(Lang.getInstance().translate("My Unions")), tableGBC );
		
		//TABLE GBC
		 tableGBC = new GridBagConstraints();
		tableGBC.fill = GridBagConstraints.BOTH; 
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;
		tableGBC.weighty = 1;
		tableGBC.gridwidth = 10;
		tableGBC.gridx = 0;	
		tableGBC.gridy= 1;	
		/*
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(10, 0, 0, 10);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridx = 0;
		buttonGBC.gridy = 1;
		*/
		//TABLE
		final WalletItemUnionsTableModel unionsModel = new WalletItemUnionsTableModel();
		final JTable table = new JTable(unionsModel);
		
		
		
	//	table.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
	//	table.setDefaultRenderer(String.class, new Renderer_Right()); // set renderer
		
		//POLLS SORTER
	//	Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
	//	CoreRowSorter sorter = new CoreRowSorter(unionsModel, indexes);
	//	table.setRowSorter(sorter);
		
		RowSorter sorter =   new TableRowSorter(unionsModel);

		table.setRowSorter(sorter);
		table.getRowSorter();
		unionsModel.fireTableDataChanged();

		/*
		//CHECKBOX FOR DIVISIBLE
		TableColumn divisibleColumn = table.getColumnModel().getColumn(WalletItemUnionsTableModel.COLUMN_DIVISIBLE);
		divisibleColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		*/
		
		//CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItemUnionsTableModel.COLUMN_CONFIRMED);
		confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		
		//MENU
		JPopupMenu unionsMenu = new JPopupMenu();
		JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				UnionCls union = (UnionCls)unionsModel.getItem(row);
				new UnionFrame(union);
			}
		});
		unionsMenu.add(details);
		
		/*
		JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
		dividend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				UnionCls union = (UnionCls)unionsModel.getItem(row);	
				new PayDividendFrame(union);
			}
		});
		unionsMenu.add(dividend);
		*/
		
		table.setComponentPopupMenu(unionsMenu);
		
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
					UnionCls union = (UnionCls)unionsModel.getItem(row);
					new UnionFrame(union);
				}
		     }
		});
		
		//ADD NAMING SERVICE TABLE
		this.add(new JScrollPane(table), tableGBC);
	/*	
		//ADD REGISTER BUTTON
		JButton issueButton = new JButton(Lang.getInstance().translate("Issue Union"));
		issueButton.setPreferredSize(new Dimension(120, 25));
		issueButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onIssueClick();
		    }
		});	
		this.add(issueButton, buttonGBC);
	*/
		
		/*	
		//ADD ALL BUTTON
		buttonGBC.gridx = 1;
		JButton allButton = new JButton(Lang.getInstance().translate("All Unions"));
		allButton.setPreferredSize(new Dimension(120, 25));
		allButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onAllClick();
			}
		});	
		this.add(allButton, buttonGBC);
	*/
		/*	
		//ADD MY ORDERS BUTTON
		buttonGBC.gridx = 2;
		JButton myOrdersButton = new JButton(Lang.getInstance().translate("My Unions"));
		myOrdersButton.setPreferredSize(new Dimension(120, 25));
		myOrdersButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onMyOrdersClick();
			}
		});	
		this.add(myOrdersButton, buttonGBC);
		*/
	}
	
	public void onIssueClick()
	{
		new IssueUnionFrame();
	}
	
	public void onAllClick()
	{
		new AllUnionsPanel();
	}
	
	public void onMyOrdersClick()
	{
		//new MyOrdersFrame();
	}
}
