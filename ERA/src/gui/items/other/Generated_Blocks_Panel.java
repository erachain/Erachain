package gui.items.other;

import database.wallet.BlocksHeadMap;
import gui.CoreRowSorter;
import gui.Split_Panel;
import gui.library.MTable;
import gui.models.WalletBlocksTableModel;
import gui.models.WalletTransactionsTableModel;
import lang.Lang;

import java.util.Map;
import java.util.TreeMap;

public class Generated_Blocks_Panel extends Split_Panel {

    private static final long serialVersionUID = -4045744114543168423L;
    WalletBlocksTableModel blocksModel;
    private WalletTransactionsTableModel transactionsModel;
    private MTable transactionsTable;

    public Generated_Blocks_Panel() {
        super("Generated_Blocks_Panel");
        setName(Lang.getInstance().translate("Generated Blocks"));
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");

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
        indexes.put(WalletBlocksTableModel.COLUMN_HEIGHT, BlocksHeadMap.TIMESTAMP_INDEX);
        indexes.put(WalletBlocksTableModel.COLUMN_TIMESTAMP, BlocksHeadMap.TIMESTAMP_INDEX);
        indexes.put(WalletBlocksTableModel.COLUMN_GENERATOR, BlocksHeadMap.GENERATOR_INDEX);
        indexes.put(WalletBlocksTableModel.COLUMN_BASETARGET, BlocksHeadMap.BALANCE_INDEX);
        indexes.put(WalletBlocksTableModel.COLUMN_TRANSACTIONS, BlocksHeadMap.TRANSACTIONS_INDEX);
        indexes.put(WalletBlocksTableModel.COLUMN_FEE, BlocksHeadMap.FEE_INDEX);
        sorter = new CoreRowSorter(blocksModel, indexes);
        jTable_jScrollPanel_LeftPanel.setRowSorter(sorter);
        jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
        //	setRowHeightFormat(true);

        // this.addTab(Lang.getInstance().translate("Generated Blocks"), new JScrollPane(blocksTable));

        setVisible(true);

    }

    public void close() {
        //REMOVE OBSERVERS/HANLDERS
        this.blocksModel.removeTableModelListener(transactionsTable);
    }
}
