package gui.records;

import gui.CoreRowSorter;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletTransactionsTableModel;
import gui.transaction.TransactionDetailsFactory;
import lang.Lang;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
//import java.awt.ScrollPaneLayout;
//import java.awt.la
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.AWTEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import core.transaction.Transaction;
import database.wallet.TransactionMap;

@SuppressWarnings("serial")
public class RecordsPanel extends  JPanel // JPanel

{

	private WalletTransactionsTableModel transactionsModel;
	private JTable transactionsTable;

	public RecordsPanel()
	{
		//this.parent = parent;
		this.setLayout(new GridBagLayout());
		//this.setLayout(new ScrollPaneLayout());
		//ScrollPaneLayout
		
		//PADDING
		//this.setBorder(new EmptyBorder(10, 10, 10, 10));
		//this.setSize(500, 500);
		//this.setLocation(20, 20);
		//this.setMaximizable(true);
		//this.setTitle(Lang.getInstance().translate("Accounts"));
		//this.setClosable(true);
		//this.setResizable(true);
		//this.setBorder(true);
		
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.fill = GridBagConstraints.BOTH; 
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;
		tableGBC.weighty = 1;
		tableGBC.gridx = 1;	
		tableGBC.gridy= 1;	

				
		//TRANSACTIONS
		this.transactionsModel = new WalletTransactionsTableModel();
		this.transactionsTable = new JTable(this.transactionsModel);
		
		//TRANSACTIONS SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		indexes.put(WalletTransactionsTableModel.COLUMN_CONFIRMATIONS, TransactionMap.TIMESTAMP_INDEX);
		indexes.put(WalletTransactionsTableModel.COLUMN_TIMESTAMP, TransactionMap.TIMESTAMP_INDEX);
		indexes.put(WalletTransactionsTableModel.COLUMN_CREATOR, TransactionMap.ADDRESS_INDEX);
		indexes.put(WalletTransactionsTableModel.COLUMN_AMOUNT, TransactionMap.AMOUNT_INDEX);
		CoreRowSorter sorter = new CoreRowSorter(transactionsModel, indexes);
		transactionsTable.setRowSorter(sorter);
		
		//Custom renderer for the String column;
		//RenderingHints.
		this.transactionsTable.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
		//this.transactionsTable.setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
		this.transactionsTable.setDefaultRenderer(Boolean.class, new Renderer_Boolean()); // set renderer
		this.transactionsTable.setDefaultRenderer(Double.class, new Renderer_Right()); // set renderer
		this.transactionsTable.setDefaultRenderer(Integer.class, new Renderer_Right()); // set renderer
		
		TableColumn column_Size = this.transactionsTable.getColumnModel().getColumn(WalletTransactionsTableModel.COLUMN_SIZE);
		column_Size.setMinWidth(50);
		column_Size.setMaxWidth(1000);
		column_Size.setPreferredWidth(70);
		
		TableColumn column_Confirm = this.transactionsTable.getColumnModel().getColumn(WalletTransactionsTableModel.COLUMN_CONFIRMATIONS);//.COLUMN_SIZE);
		column_Confirm.setMinWidth(50);
		column_Confirm.setMaxWidth(1000);
		column_Confirm.setPreferredWidth(70);
		
		TableColumn column_Fee = this.transactionsTable.getColumnModel().getColumn(WalletTransactionsTableModel.COLUMN_FEE);//.COLUMN_SIZE);
		column_Fee.setMinWidth(80);
		column_Fee.setMaxWidth(1000);
		column_Fee.setPreferredWidth(80);
		
		TableColumn column_Date = this.transactionsTable.getColumnModel().getColumn(WalletTransactionsTableModel.COLUMN_TIMESTAMP);//.COLUMN_FEE);//.COLUMN_SIZE);
		column_Date.setMinWidth(120);
		column_Date.setMaxWidth(1000);
		column_Date.setPreferredWidth(120);
		
		//TRANSACTION DETAILS
		this.transactionsTable.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent e) 
			{
				if(e.getClickCount() == 2) 
				{
					//GET ROW
			        int row = transactionsTable.getSelectedRow();
			        row = transactionsTable.convertRowIndexToModel(row);
			        
			        //GET TRANSACTION
			        Transaction transaction = transactionsModel.getTransaction(row);
			         
			        //SHOW DETAIL SCREEN OF TRANSACTION
			        TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
			    }
			}
		});			
		
		this.add(new JScrollPane(this.transactionsTable), tableGBC);

		//this.add(this.transactionsTable);       
		
	}
	
}
