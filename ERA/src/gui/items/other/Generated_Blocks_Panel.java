package gui.items.other;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import database.wallet.BlockMap;
import gui.CoreRowSorter;
import gui.Split_Panel;
import gui.library.MTable;
import gui.models.TableModelCls;
import gui.models.WalletBlocksTableModel;
import gui.models.WalletTransactionsTableModel;
import lang.Lang;

public class Generated_Blocks_Panel extends Split_Panel {

	private WalletTransactionsTableModel transactionsModel;
	private MTable transactionsTable;
	private static final long serialVersionUID = -4045744114543168423L;
	WalletBlocksTableModel blocksModel;

	public  Generated_Blocks_Panel(){
		super("Generated_Blocks_Panel");
		setName(Lang.getInstance().translate("Generated Blocks"));
		searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
		
	// not show buttons
		jToolBar_RightPanel.setVisible(false);
		toolBar_LeftPanel.setVisible(false);
		
// not show My filter
		searth_My_JCheckBox_LeftPanel.setVisible(false);
		searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
		
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		
		CoreRowSorter sorter = new CoreRowSorter(transactionsModel, indexes);
		
		//TRANSACTIONS
		blocksModel = new WalletBlocksTableModel();
		jTable_jScrollPanel_LeftPanel = new MTable(blocksModel);
				
		//TRANSACTIONS SORTER
		indexes = new TreeMap<Integer, Integer>();
		indexes.put(WalletBlocksTableModel.COLUMN_HEIGHT, BlockMap.TIMESTAMP_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_TIMESTAMP, BlockMap.TIMESTAMP_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_GENERATOR, BlockMap.GENERATOR_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_BASETARGET, BlockMap.BALANCE_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_TRANSACTIONS, BlockMap.TRANSACTIONS_INDEX);
		indexes.put(WalletBlocksTableModel.COLUMN_FEE, BlockMap.FEE_INDEX);
		sorter = new CoreRowSorter(blocksModel, indexes);
		jTable_jScrollPanel_LeftPanel.setRowSorter(sorter);
		jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
	//	setRowHeightFormat(true);

       // this.addTab(Lang.getInstance().translate("Generated Blocks"), new JScrollPane(blocksTable));
		
		setVisible(true);
		
	}
	
	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
		this.blocksModel.removeTableModelListener(transactionsTable);
	}
}
