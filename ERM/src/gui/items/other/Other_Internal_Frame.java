package gui.items.other;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import core.transaction.Transaction;
import database.BlockMap;
import database.TransactionMap;
import gui.CoreRowSorter;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
import gui.items.persons.Persons_Search_SplitPanel;
import gui.models.BlocksTableModel;
import gui.models.PeersTableModel;
import gui.models.TransactionsTableModel;
import gui.transaction.TransactionDetailsFactory;
import lang.Lang;

public class Other_Internal_Frame extends Main_Internal_Frame {
	
	private BlocksTableModel blocksTableModel;
	private PeersTableModel peersTableModel;
	private TransactionsTableModel transactionsTableModel;

	
	public  Other_Internal_Frame(){
		
		
		
	    Dimension size = MainFrame.desktopPane.getSize();
	   
		
		this.setTitle(Lang.getInstance().translate("Other"));
		
		other_Panel other_panel = new other_Panel();
		
		jTabbedPane.setMinimumSize(new java.awt.Dimension(5, 40));
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(other_panel, gridBagConstraints);
        
        jTabbedPane.setVisible(false);
        
        this.jToolBar.setVisible(false);
		
 
        
       
        
        
     
		
		this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with Other"));
		
 // отключаем все что ниже  		
		
	/*	// My block
		Generated_Blocks_Panel split_generated_Block = new Generated_Blocks_Panel();
		this.jTabbedPane.add(split_generated_Block);
		
		this.setTitle(Lang.getInstance().translate("Other"));
		

// ALL Block
		
		Split_Panel split_ALL_Block = new Split_Panel();
		split_ALL_Block.jToolBar_RightPanel.setVisible(false);
		split_ALL_Block.toolBar_LeftPanel.setVisible(false);
		split_ALL_Block.searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
		split_ALL_Block.searth_My_JCheckBox_LeftPanel.setVisible(false);
//	    split_ALL_Block.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
		
		
		//BLOCKS TABLE MODEL
				this.blocksTableModel = new BlocksTableModel();
				JTable blocksTable = new JTable(this.blocksTableModel);
				
				//BLOCKS SORTER
				TreeMap<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
				indexes.put(BlocksTableModel.COLUMN_HEIGHT, BlockMap.HEIGHT_INDEX);
				CoreRowSorter sorter = new CoreRowSorter(blocksTableModel, indexes);
				blocksTable.setRowSorter(sorter);
	/*	
	/*	//ADD BLOCK TABLE
				split_ALL_Block.jTable_jScrollPanel_LeftPanel =	blocksTable;
				split_ALL_Block.jScrollPanel_LeftPanel.setViewportView(split_ALL_Block.jTable_jScrollPanel_LeftPanel);
				split_ALL_Block.setRowHeightFormat(true);
				
		this.jTabbedPane.addTab(Lang.getInstance().translate("All Blocks"), split_ALL_Block);
		*/
/*		this.jTabbedPane.addTab(Lang.getInstance().translate("All Blocks"), new JScrollPane(blocksTable));
		
// Peers
		
				Split_Panel split_Peers = new Split_Panel();
				split_Peers.jToolBar_RightPanel.setVisible(false);
				split_Peers.toolBar_LeftPanel.setVisible(false);
				split_Peers.searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
				split_Peers.searth_My_JCheckBox_LeftPanel.setVisible(false);
// peers model						
		
				
		
				
				this.peersTableModel = new PeersTableModel();
//					this.addTab(Lang.getInstance().translate("Peers"), new JScrollPane(new JTable(this.peersTableModel)));	
	//ADD BLOCK TABLE
					split_Peers.jTable_jScrollPanel_LeftPanel =	new JTable(this.peersTableModel);
					split_Peers.jScrollPanel_LeftPanel.setViewportView(split_Peers.jTable_jScrollPanel_LeftPanel);
					split_Peers.setRowHeightFormat(true);	
		
		this.jTabbedPane.addTab(Lang.getInstance().translate("Peers"), split_Peers);
		
// Transaction
		
		Split_Panel split_Transaction = new Split_Panel();
		split_Transaction.jToolBar_RightPanel.setVisible(false);
		split_Transaction.toolBar_LeftPanel.setVisible(false);
		split_Transaction.searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
		split_Transaction.searth_My_JCheckBox_LeftPanel.setVisible(false);		
		
//TRANSACTIONS TABLE MODEL
				transactionsTableModel = new TransactionsTableModel();
				split_Transaction.jTable_jScrollPanel_LeftPanel = new JTable(this.transactionsTableModel);
				
				//TRANSACTIONS SORTER
				 indexes = new TreeMap<Integer, Integer>();
				indexes.put(TransactionsTableModel.COLUMN_TIMESTAMP, TransactionMap.TIMESTAMP_INDEX);
				sorter = new CoreRowSorter(transactionsTableModel, indexes);
				split_Transaction.jTable_jScrollPanel_LeftPanel.setRowSorter(sorter);
				
				//TRANSACTION DETAILS
				split_Transaction.jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() 
				{
					public void mouseClicked(MouseEvent e) 
					{
						if(e.getClickCount() == 2) 
						{
							//GET ROW
					        int row = split_Transaction.jTable_jScrollPanel_LeftPanel.getSelectedRow();
					        row = split_Transaction.jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
					        
					        //GET TRANSACTION
					        Transaction transaction = transactionsTableModel.getTransaction(row);
					         
					        //SHOW DETAIL SCREEN OF TRANSACTION
					        TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
					    }
					}
				});
				
				//ADD TRANSACTIONS TABLE
				//split_Transaction.jTable_jScrollPanel_LeftPanel =	split_Transaction.jTable_jScrollPanel_LeftPanel;
				split_Transaction.jScrollPanel_LeftPanel.setViewportView(split_Transaction.jTable_jScrollPanel_LeftPanel);
				split_Transaction.setRowHeightFormat(true);	
				
			//	this.addTab(Lang.getInstance().translate("Transactions"), new JScrollPane(this.transactionsTable)); 
				this.jTabbedPane.addTab(Lang.getInstance().translate("Transactions"), split_Transaction);
		
		
	
				
				// other
				
				this.jTabbedPane.addTab(Lang.getInstance().translate("Other"), new other_Panel());
				
		
				  split_generated_Block.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
		    split_Peers.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	    split_Transaction.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
		*
		*/			
				
		
		
		this.pack();
		//	this.setSize(800,600);
		this.setMaximizable(true);
		
		this.setClosable(true);
		this.setResizable(true);
		this.setLocation(20, 20);
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
	  
	    
	    this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	  
	
	
	    
	    this.setVisible(true);
	    
	    
	   
	
	    
	    
	    
		
	}
	
	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
		this.peersTableModel.removeObservers();
		
		this.transactionsTableModel.removeObservers();
		
		this.blocksTableModel.removeObservers();
		
		
	}

}
