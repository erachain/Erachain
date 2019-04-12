package org.erachain.gui.items.other;

import org.erachain.database.wallet.BlocksHeadMap;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.WalletBlocksTableModel;
import org.erachain.lang.Lang;

import java.util.Map;
import java.util.TreeMap;

public class GeneratedBlocksPanel extends SplitPanel {

    private static final long serialVersionUID = -4045744114543168423L;
    WalletBlocksTableModel blocksModel;
    //private WalletTransactionsTableModel transactionsModel;
    private MTable transactionsTable;

    public GeneratedBlocksPanel() {
        super("GeneratedBlocksPanel");
        setName(Lang.getInstance().translate("Generated Blocks"));
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");

        // not show buttons
        jToolBarRightPanel.setVisible(false);
        toolBarLeftPanel.setVisible(false);

// not show My filter
        searchMyJCheckBoxLeftPanel.setVisible(false);
        searchFavoriteJCheckBoxLeftPanel.setVisible(false);

        Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();

        //CoreRowSorter sorter = new CoreRowSorter(transactionsModel, indexes);

        //TRANSACTIONS
        blocksModel = new WalletBlocksTableModel();
        jTableJScrollPanelLeftPanel = new MTable(blocksModel);

        //TRANSACTIONS SORTER
        indexes = new TreeMap<Integer, Integer>();
        indexes.put(WalletBlocksTableModel.COLUMN_HEIGHT, BlocksHeadMap.TIMESTAMP_INDEX);
        indexes.put(WalletBlocksTableModel.COLUMN_TIMESTAMP, BlocksHeadMap.TIMESTAMP_INDEX);
        indexes.put(WalletBlocksTableModel.COLUMN_GENERATOR, BlocksHeadMap.GENERATOR_INDEX);
        indexes.put(WalletBlocksTableModel.COLUMN_GB, BlocksHeadMap.BALANCE_INDEX);
        indexes.put(WalletBlocksTableModel.COLUMN_TRANSACTIONS, BlocksHeadMap.TRANSACTIONS_INDEX);
        indexes.put(WalletBlocksTableModel.COLUMN_FEE, BlocksHeadMap.FEE_INDEX);
        //sorter = new CoreRowSorter(blocksModel, indexes);
        //jTableJScrollPanelLeftPanel.setRowSorter(sorter);
        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);
        //	setRowHeightFormat(true);

        // this.addTab(Lang.getInstance().translate("Generated Blocks"), new JScrollPane(blocksTable));

        setVisible(true);

    }

    public void close() {
        //REMOVE OBSERVERS/HANLDERS
        this.blocksModel.removeTableModelListener(transactionsTable);
    }
}
