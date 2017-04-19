package gui.items.imprints;


import utils.ObserverMessage;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import core.item.imprints.ImprintCls;
import gui.CoreRowSorter;
import gui.items.ItemsPanel;
import gui.items.imprints.AllImprintsFrame;
import gui.items.imprints.ImprintFrame;
import gui.items.imprints.IssueImprintDialog;
//import gui.items.imprints.MyOrdersFrame;
//import gui.items.imprints.PayDividendFrame;
import gui.models.WalletItemImprintsTableModel;
import lang.Lang;

/*
@SuppressWarnings("serial")
public class ImprintsPanel extends ItemsPanel
{
	public ImprintsPanel(WalletItemImprintsTableModel itemsModel)
	{		
		super(itemsModel, "All Imprints", "Issue Imprint");
	}
}
*/	
@SuppressWarnings("serial")
public class ImprintsPanel extends JInternalFrame
{
	public ImprintsPanel()
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
		final WalletItemImprintsTableModel imprintsModel = new WalletItemImprintsTableModel();
		final JTable table = new JTable(imprintsModel);
		
		//POLLS SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		CoreRowSorter sorter = new CoreRowSorter(imprintsModel, indexes);
		table.setRowSorter(sorter);

		/*
		//CHECKBOX FOR DIVISIBLE
		TableColumn divisibleColumn = table.getColumnModel().getColumn(WalletItemImprintsTableModel.COLUMN_DIVISIBLE);
		divisibleColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		*/
		
		//CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItemImprintsTableModel.COLUMN_CONFIRMED);
		confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		
		//MENU
		JPopupMenu imprintsMenu = new JPopupMenu();
		JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				ImprintCls imprint = (ImprintCls)imprintsModel.getItem(row);
				new ImprintFrame(imprint);
			}
		});
		imprintsMenu.add(details);
		
		/*
		JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
		dividend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				ImprintCls imprint = (ImprintCls)imprintsModel.getItem(row);	
				new PayDividendFrame(imprint);
			}
		});
		imprintsMenu.add(dividend);
		*/
		
		table.setComponentPopupMenu(imprintsMenu);
		
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
					ImprintCls imprint = (ImprintCls)imprintsModel.getItem(row);
					new ImprintFrame(imprint);
				}
		     }
		});
		
		//ADD NAMING SERVICE TABLE
		this.add(new JScrollPane(table), tableGBC);
		
		//ADD REGISTER BUTTON
		JButton issueButton = new JButton(Lang.getInstance().translate("Issue Imprint"));
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
		JButton allButton = new JButton(Lang.getInstance().translate("All Imprints"));
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
		new IssueImprintDialog();
	}
	
	public void onAllClick()
	{
		new AllImprintsFrame();
	}
	
	public void onMyOrdersClick()
	{
		//new MyOrdersFrame();
	}
}
