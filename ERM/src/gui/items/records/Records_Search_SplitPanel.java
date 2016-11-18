package gui.items.records;

	import java.awt.Color;
	import java.awt.Component;
	import java.awt.Cursor;
	import java.awt.Dimension;
	import java.awt.GridLayout;
	import java.awt.Point;
	import java.awt.Rectangle;
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
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Timer;
	import java.awt.*;

	import javax.swing.DefaultRowSorter;
	import javax.swing.JButton;
	import javax.swing.JDialog;
	import javax.swing.JFrame;
	import javax.swing.JInternalFrame;
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

	import controller.Controller;
	import core.item.assets.AssetCls;
	import core.item.persons.PersonCls;
import core.transaction.Transaction;
import database.TransactionMap;
import gui.CoreRowSorter;
import gui.MainFrame;
	import gui.Main_Internal_Frame;
	import gui.RunMenu;
	import gui.Split_Panel;
	import gui.items.assets.IssueAssetPanel;
	import gui.items.assets.TableModelItemAssets;
import gui.models.BlocksTableModel;
import gui.models.Renderer_Boolean;
	import gui.models.Renderer_Left;
	import gui.models.Renderer_Right;
import gui.models.TransactionsTableModel;
import gui.models.WalletItemAssetsTableModel;
	import gui.models.WalletItemPersonsTableModel;
import gui.transaction.TransactionDetailsFactory;
import lang.Lang;


	public class Records_Search_SplitPanel extends Split_Panel{
	
		TransactionsTableModel transactionsTableModel;

		    public Records_Search_SplitPanel() {
		   
		    	
		    	this.setName(Lang.getInstance().translate("No ref Records"));
		    	
		    	this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Insert namber block")+":");
		    	
		    	this.button1_ToolBar_LeftPanel.setVisible(false);
		    	this.button2_ToolBar_LeftPanel.setVisible(false);
		    	this.searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
		    	this.searth_My_JCheckBox_LeftPanel.setVisible(false);
		    	this.jButton1_jToolBar_RightPanel.setVisible(false);
		    	this.jButton2_jToolBar_RightPanel.setVisible(false);
		    	
		    	
		   // 	Records_Table_Model records_Model = new Records_Table_Model();
		   // 	this.jTable_jScrollPanel_LeftPanel = new JTable(records_Model);
		    	
		    	
		    	
		    	//TRANSACTIONS TABLE MODEL
				this.transactionsTableModel = new TransactionsTableModel();
				this.jTable_jScrollPanel_LeftPanel = new JTable(this.transactionsTableModel);
				
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

		   
	}




