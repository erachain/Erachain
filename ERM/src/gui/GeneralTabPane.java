package gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import gui.items.assets.AssetsPanel;
import gui.items.imprints.ImprintsPanel;
import gui.items.notes.NotesPanel;
import gui.items.persons.PersonsPanel;
import gui.at.ATPanel;
import gui.at.ATTransactionsPanel;
import gui.at.AcctPanel;
import gui.models.WalletBlocksTableModel;
import gui.models.WalletTransactionsTableModel;
//import gui.models.WalletItemNotesTableModel;

import gui.naming.NamingServicePanel;
import gui.transaction.TransactionDetailsFactory;
import gui.voting.VotingPanel;
import lang.Lang;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import core.transaction.Transaction;
import database.wallet.BlockMap;
import database.wallet.TransactionMap;

public class GeneralTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2717571093561259483L;

	private WalletTransactionsTableModel transactionsModel;
	private JTable transactionsTable;
	
	public GeneralTabPane()
	{
		super();
		
		//ACCOUNTS
		this.addTab(Lang.getInstance().translate("Accounts"), new AccountsPanel());
        
		//STATEMENT
		this.addTab(Lang.getInstance().translate("Statement"), new RecordNotePanel());

		//SEND
		this.addTab(Lang.getInstance().translate("Send money"), new SendMoneyPanel());

		//MESSAGE
		this.addTab(Lang.getInstance().translate("Messages"), new SendMessagePanel());
			   
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
		this.addTab(Lang.getInstance().translate("Transactions"), new JScrollPane(this.transactionsTable));       
		
		//TRANSACTIONS
		WalletBlocksTableModel blocksModel = new WalletBlocksTableModel();
		JTable blocksTable = new JTable(blocksModel);
				
		//TRANSACTIONS SORTER
		indexes = new TreeMap<Integer, Integer>();
		indexes.put(WalletBlocksTableModel.COLUMN_HEIGHT, BlockMap.TIMESTAMP_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_TIMESTAMP, BlockMap.TIMESTAMP_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_GENERATOR, BlockMap.GENERATOR_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_BASETARGET, BlockMap.BALANCE_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_TRANSACTIONS, BlockMap.TRANSACTIONS_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_FEE, BlockMap.FEE_INDEX);
		sorter = new CoreRowSorter(blocksModel, indexes);
		blocksTable.setRowSorter(sorter);

        this.addTab(Lang.getInstance().translate("Generated Blocks"), new JScrollPane(blocksTable));
        
        //IMPRINTS
        this.addTab(Lang.getInstance().translate("Imprints"), new ImprintsPanel());

        //NOTES
        //this.addTab(Lang.getInstance().translate("Persons"), new PersonsPanel());

        //NOTES
        this.addTab(Lang.getInstance().translate("Notes"), new NotesPanel());
        		//new WalletItemNotesTableModel())
        		//);

        //ASSETS
        this.addTab(Lang.getInstance().translate("Assets"), new AssetsPanel());        

        //NAMING
        //this.addTab(Lang.getInstance().translate("Naming service"), new NamingServicePanel());      
        
        //VOTING
        this.addTab(Lang.getInstance().translate("Voting"), new VotingPanel());       
        
		//ATs
		//this.addTab(Lang.getInstance().translate("AT"), new ATPanel());

		//AT TXs
		//this.addTab(Lang.getInstance().translate("AT Transactions"), new ATTransactionsPanel());

		//AT Acct
		//this.addTab(Lang.getInstance().translate("ACCT"), new AcctPanel());
	}
	
}
