package gui.items;

import gui.QoraRowSorter;
import gui.models.WalletItem_TableModel;
import lang.Lang;

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

import qora.item.ItemCls;

@SuppressWarnings("serial")
public class ItemsPanel extends JPanel
{
	
	private String mess_issue_item = "Issue Item";
	private String mess_all_items = "All Items";
	//private int observer_add;
	//private int observer_remove;
	//private int observer_list;

	public ItemsPanel(int observer_add, int observer_remove, int observer_list,
			String mess_all_items, String mess_issue_item)
	{
		
		this.mess_all_items = mess_all_items;
		this.mess_issue_item = mess_issue_item;
		
		//this.observer_add = observer_add;
		//this.observer_remove = observer_remove;
		//this.observer_list = observer_list;
		
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
		///final - no!
		WalletItem_TableModel itemsModel = new WalletItem_TableModel(observer_add, observer_remove, observer_list);
		///final  - no!
		JTable table = new JTable(itemsModel);
		
		//POLLS SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		QoraRowSorter sorter = new QoraRowSorter(itemsModel, indexes);
		table.setRowSorter(sorter);
				
		//CHECKBOX FOR DIVISIBLE
		// *** TableColumn divisibleColumn = table.getColumnModel().getColumn(WalletItemsTableModel.COLUMN_DIVISIBLE);
		// ***divisibleColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		
		//CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItem_TableModel.COLUMN_CONFIRMED);
		confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		
		//MENU
		JPopupMenu itemsMenu = new JPopupMenu();
		JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				ItemCls item = itemsModel.getItem(row);
				new ItemFrame(item);
			}
		});
		itemsMenu.add(details);
		JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
		dividend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				ItemCls item = itemsModel.getItem(row);	
				//new PayDividendFrame(item);
			}
		});
		itemsMenu.add(dividend);
		table.setComponentPopupMenu(itemsMenu);
		
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
					ItemCls item = itemsModel.getItem(row);
					new ItemFrame(item);
				}
		     }
		});
		//ADD TABLE
		this.add(new JScrollPane(table), tableGBC);
		
		//ADD REGISTER BUTTON
		JButton issueButton = new JButton(Lang.getInstance().translate(this.mess_issue_item));
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
		JButton allButton = new JButton(Lang.getInstance().translate(this.mess_all_items));
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
				//onMyOrdersClick();
			}
		});	
		this.add(myOrdersButton, buttonGBC);
	}
	
	public void onIssueClick()
	{
		new IssueItemFrame();
	}
	
	public void onAllClick()
	{
		new AllItemsFrame(this.mess_all_items);
	}
	
}
