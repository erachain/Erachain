package gui.items.records;

	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
	import java.awt.event.FocusEvent;
	import java.awt.event.FocusListener;
	import java.awt.event.MouseAdapter;
	import java.awt.event.MouseEvent;
	import java.awt.event.MouseListener;
	import java.awt.event.MouseMotionListener;
	import java.awt.event.WindowEvent;
	import java.awt.event.WindowFocusListener;
	import java.awt.image.ColorModel;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Timer;
	import java.awt.*;

	import javax.swing.DefaultRowSorter;
	import javax.swing.JButton;
	import javax.swing.JDialog;
	import javax.swing.JFrame;
	import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
	import javax.swing.JPanel;
	import javax.swing.JPopupMenu;
	import javax.swing.JScrollPane;
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

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.account.PublicKeyAccount;
import core.item.assets.AssetCls;
	import core.item.persons.PersonCls;
import core.item.unions.UnionCls;
import core.transaction.Transaction;
import database.DBSet;
import database.TransactionMap;
import gui.CoreRowSorter;
import gui.Main_Internal_Frame;
	import gui.Split_Panel;
	import gui.items.assets.IssueAssetPanel;
	import gui.items.assets.TableModelItemAssets;
import gui.items.records.Records_My_SplitPanel.search_listener;
import gui.items.statement.Statements_Vouch_Table_Model;
import gui.library.MTable;
import gui.library.Voush_Library_Panel;
import gui.models.BlocksTableModel;
import gui.models.Renderer_Boolean;
	import gui.models.Renderer_Left;
	import gui.models.Renderer_Right;
import gui.models.TransactionsTableModel;
import gui.models.WalletItemAssetsTableModel;
	import gui.models.WalletItemPersonsTableModel;
import gui.records.VouchRecordDialog;
import gui.transaction.TransactionDetailsFactory;
import lang.Lang;
import utils.MenuPopupUtil;


	public class Records_Search_SplitPanel extends Split_Panel{
	
		TransactionsTableModel transactionsTableModel;
		JScrollPane jScrollPane4;
		private JTextField sender_address;
		public JPanel info_Panel;
		public Voush_Library_Panel voush_Library_Panel;

		    public Records_Search_SplitPanel() {
		   super("Records_Search_SplitPanel");
		    	
		    	this.searchToolBar_LeftPanel.setVisible(true);
		    	jScrollPane4 = new  JScrollPane();
		    	
		    	this.setName(Lang.getInstance().translate("Search Records"));
		    	
		    	this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Insert Height Block or Block-SeqNo")+":");
		    	this.toolBar_LeftPanel.add(new JLabel(Lang.getInstance().translate("Insert Account")+":"));
		    	sender_address = new JTextField();
		    	sender_address.setToolTipText("");
		    	sender_address.setAlignmentX(1.0F);
		    	sender_address.setMinimumSize(new java.awt.Dimension(50, 20));
		    	sender_address.setName(""); // NOI18N
		    	sender_address.setPreferredSize(new java.awt.Dimension(200, 20));
		    	sender_address.setMaximumSize(new java.awt.Dimension(2000, 20));
		       	
		    	MenuPopupUtil.installContextMenu(sender_address);
		    	
		    	this.toolBar_LeftPanel.add(sender_address);
		    	sender_address.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						searchTextField_SearchToolBar_LeftPanel.setText("");
						transactionsTableModel.Find_Transactions_from_Address(sender_address.getText());	
						
					}
		    		
		    	});
		    	
		    	this.button1_ToolBar_LeftPanel.setVisible(false);
		    	this.button2_ToolBar_LeftPanel.setVisible(false);
		    	this.searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
		    	this.searth_My_JCheckBox_LeftPanel.setVisible(false);
		    	this.jButton1_jToolBar_RightPanel.setVisible(false);
		    	this.jButton2_jToolBar_RightPanel.setVisible(false);
		    	
		    	
		   // 	Records_Table_Model records_Model = new Records_Table_Model();
		   // 	this.jTable_jScrollPanel_LeftPanel = new JTable(records_Model);
		    	
		    	MenuPopupUtil.installContextMenu(this.searchTextField_SearchToolBar_LeftPanel);
		    	this.searchTextField_SearchToolBar_LeftPanel.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						sender_address.setText("");
						transactionsTableModel.setBlockNumber(searchTextField_SearchToolBar_LeftPanel.getText());
					
					}
		    		
		    	});
		    	
		    	//TRANSACTIONS TABLE MODEL
				this.transactionsTableModel = new TransactionsTableModel();
				this.jTable_jScrollPanel_LeftPanel = new MTable(this.transactionsTableModel);
				
				this.jTable_jScrollPanel_LeftPanel.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
				
				// MENU
				JPopupMenu mainMenu = new JPopupMenu();
				
				JMenuItem vouch_menu= new JMenuItem(Lang.getInstance().translate("Vouch"));
				vouch_menu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
						row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
						Transaction trans = transactionsTableModel.getTransaction(row);
						DBSet db = DBSet.getInstance();
						new VouchRecordDialog(trans.getBlockHeight(db), trans.getSeqNo(db));
						
					}
				});
				
				mainMenu.add(vouch_menu);
				
				this.jTable_jScrollPanel_LeftPanel.setComponentPopupMenu(mainMenu);
				
				this.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
				
				//TRANSACTIONS SORTER
				Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
				indexes.put(TransactionsTableModel.COLUMN_TIMESTAMP, TransactionMap.TIMESTAMP_INDEX);
				CoreRowSorter sorter = new CoreRowSorter(transactionsTableModel, indexes);
				this.jTable_jScrollPanel_LeftPanel.setRowSorter(sorter);
				
				//TRANSACTION DETAILS
				this.jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() 
				{
					public void mouseClicked(MouseEvent e) 
					{
						if(e.getClickCount() == 2) 
						{
							//GET ROW
					        int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
					        row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
					        
					        //GET TRANSACTION
					        Transaction transaction = transactionsTableModel.getTransaction(row);
					         
					        //SHOW DETAIL SCREEN OF TRANSACTION
					        TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
					    }
					}
				});
		    	
		    	this.jScrollPanel_LeftPanel.setViewportView(this.jTable_jScrollPanel_LeftPanel);		    	
		    	
		    }
		    
		    
		    
	 // listener select row	 
	 class search_listener implements ListSelectionListener  {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				String dateAlive;
				String date_birthday;
				String message; 
				// устанавливаем формат даты
				SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
				//создаем объект персоны
				UnionCls union;
				Transaction voting = null;
				if (jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0 ) {
					voting = (Transaction) transactionsTableModel.getTransaction(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
						
			//	Person_info_panel_001 info_panel = new Person_info_panel_001(voting, false);
				
			//	votingDetailsPanel = new VotingDetailPanel(voting, (AssetCls)allVotingsPanel.cbxAssets.getSelectedItem());
			//	votingDetailsPanel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
				//jScrollPane_jPanel_RightPanel.setHorizontalScrollBar(null);
			//	jScrollPane_jPanel_RightPanel.setViewportView(votingDetailsPanel);
				//jSplitPanel.setRightComponent(votingDetailsPanel);
				
				
								
		     //   TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
				  
				info_Panel = new JPanel();
		        info_Panel.setLayout(new GridBagLayout());
		      //  panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
				
				//TABLE GBC
				GridBagConstraints tableGBC = new GridBagConstraints();
				tableGBC.fill = GridBagConstraints.BOTH; 
				tableGBC.anchor = GridBagConstraints.FIRST_LINE_START;
				tableGBC.weightx = 1;
				tableGBC.weighty = 1;
				tableGBC.gridx = 0;	
				tableGBC.gridy= 0;	
			//	JPanel a = TransactionDetailsFactory.getInstance().createTransactionDetail(voting);
				info_Panel.add(TransactionDetailsFactory.getInstance().createTransactionDetail(voting),tableGBC);						
				  
		        Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> signs = DBSet.getInstance().getVouchRecordMap().get(voting.getBlockHeight(DBSet.getInstance()),voting.getSeqNo(DBSet.getInstance()));
		        GridBagConstraints gridBagConstraints = null;
		        if (signs != null) {
			  	  
				  	JLabel  jLabelTitlt_Table_Sign = new JLabel(Lang.getInstance().translate("Signatures")+":");
			        gridBagConstraints = new java.awt.GridBagConstraints();
			        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
			        gridBagConstraints.weightx = 0.1;
			        gridBagConstraints.insets = new java.awt.Insets(12, 11, 0, 11);
			        gridBagConstraints.gridx = 0;
			        gridBagConstraints.gridy = 1;
			        info_Panel.add(jLabelTitlt_Table_Sign, gridBagConstraints);
			  	  
			  	  
			  											
					
			        gridBagConstraints = new java.awt.GridBagConstraints();
			        gridBagConstraints.gridx = 0;
			        gridBagConstraints.gridy = 2;
			        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
			        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
			        gridBagConstraints.weightx = 1.0;
			        gridBagConstraints.weighty = 1.0;
			        voush_Library_Panel=new  Voush_Library_Panel(voting);
			        info_Panel. add( voush_Library_Panel, gridBagConstraints);
				
		        }
			     
		        jScrollPane_jPanel_RightPanel.setViewportView( info_Panel);

			}
		}
	}
	 @Override
		public void delay_on_close(){
			// delete observer left panel
		 transactionsTableModel.removeObservers();
			// get component from right panel
			Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
			// if Person_Info 002 delay on close
	//		  if (c1.getClass() == this.info_Panel.getClass()) voush_Library_Panel.delay_on_close();
			
		}
}