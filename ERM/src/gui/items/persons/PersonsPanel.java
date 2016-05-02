package gui.items.persons;


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

import core.item.persons.PersonCls;
import gui.CoreRowSorter;
import gui.items.ItemsPanel;
import gui.items.persons.AllPersonsPanel;
import gui.items.persons.PersonFrame;
import gui.items.persons.IssuePersonFrame;
import gui.models.Renderer_Right;
//import gui.items.persons.MyOrdersFrame;
//import gui.items.persons.PayDividendFrame;
import gui.models.WalletItemPersonsTableModel;
import lang.Lang;

/*
@SuppressWarnings("serial")
public class PersonsPanel extends ItemsPanel
{
	public PersonsPanel(WalletItemPersonsTableModel itemsModel)
	{		
		super(itemsModel, "All Persons", "Issue Person");
	}
}
*/	
@SuppressWarnings("serial")
public class PersonsPanel extends JPanel
{
	public PersonsPanel()
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
		this.add(new JLabel(Lang.getInstance().translate("My Persons")), tableGBC );
		
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
		final WalletItemPersonsTableModel personsModel = new WalletItemPersonsTableModel();
		final JTable table = new JTable(personsModel);
		
	//	table.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
	//	table.setDefaultRenderer(String.class, new Renderer_Right()); // set renderer
		
		//POLLS SORTER
	//	Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
	//	CoreRowSorter sorter = new CoreRowSorter(personsModel, indexes);
	//	table.setRowSorter(sorter);
		
		RowSorter sorter =   new TableRowSorter(personsModel);

		table.setRowSorter(sorter);
		table.getRowSorter();
		personsModel.fireTableDataChanged();

		/*
		//CHECKBOX FOR DIVISIBLE
		TableColumn divisibleColumn = table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_DIVISIBLE);
		divisibleColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		*/
		
		//CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_CONFIRMED);
		confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		
		//MENU
		JPopupMenu personsMenu = new JPopupMenu();
		JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				PersonCls person = (PersonCls)personsModel.getItem(row);
				new PersonFrame(person);
			}
		});
		personsMenu.add(details);
		
		/*
		JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
		dividend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				PersonCls person = (PersonCls)personsModel.getItem(row);	
				new PayDividendFrame(person);
			}
		});
		personsMenu.add(dividend);
		*/
		
		table.setComponentPopupMenu(personsMenu);
		
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
					PersonCls person = (PersonCls)personsModel.getItem(row);
					new PersonFrame(person);
				}
		     }
		});
		
		//ADD NAMING SERVICE TABLE
		this.add(new JScrollPane(table), tableGBC);
	/*	
		//ADD REGISTER BUTTON
		JButton issueButton = new JButton(Lang.getInstance().translate("Issue Person"));
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
		JButton allButton = new JButton(Lang.getInstance().translate("All Persons"));
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
		JButton myOrdersButton = new JButton(Lang.getInstance().translate("My Persons"));
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
		new IssuePersonFrame();
	}
	
	public void onAllClick()
	{
		new AllPersonsPanel();
	}
	
	public void onMyOrdersClick()
	{
		//new MyOrdersFrame();
	}
}
