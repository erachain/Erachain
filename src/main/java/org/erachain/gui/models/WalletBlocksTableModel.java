package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.BlocksHeadMap;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletBlocksTableModel extends SortedListTableModelCls<Tuple2<String, String>, Block.BlockHead> implements Observer {

    public static final int COLUMN_HEIGHT = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_GENERATOR = 2;
    public static final int COLUMN_BASETARGET = 3;
    public static final int COLUMN_TRANSACTIONS = 4;
    public static final int COLUMN_FEE = 5;

    public WalletBlocksTableModel() {
        super(Controller.getInstance().wallet.database.getBlocksHeadMap(), "WalletBlocksTableModel", 1000,
                new String[]{"Height", "Timestamp", "Generator",
                        "GB dtWV", //"Generating Balance",
                        "Transactions", "Fee"}, new Boolean[]{false, true, true, false, true, false}, true);

        // сначала ЛОГЕР задаем
        LOGGER = LoggerFactory.getLogger(WalletBlocksTableModel.class.getName());

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            addObservers();
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        try {
            if (this.list == null || this.list.size() - 1 < row) {
                return null;
            }

            //
            Pair<Tuple2<String, String>, Block.BlockHead> data = this.list.get(row);

            if (data == null || data.getB() == null) {
                return null;
            }

            Block.BlockHead block = data.getB();
            if (block == null) {
                return null;
            }

            switch (column) {
                case COLUMN_HEIGHT:

                    return block.heightBlock;

                case COLUMN_TIMESTAMP:

                    BlockChain blockChain = Controller.getInstance().getBlockChain();
                    return DateTimeFormat.timestamptoString(blockChain.getTimestamp(block.heightBlock));

                case COLUMN_GENERATOR:

                    return block.creator.getPersonAsString();

                case COLUMN_BASETARGET:

                    return block.forgingValue + " "
                            + new Float(100000*(block.forgingValue - block.target)/block.target)/1000.0; //.movePointLeft(3);

                case COLUMN_TRANSACTIONS:

                    return block.transactionsCount;

                case COLUMN_FEE:

                    return BigDecimal.valueOf(block.totalFee, BlockChain.FEE_SCALE);

            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage() + " row:" + row, e);
        }

        return null;
    }

    private int count;

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_BLOCK_TYPE) {
            //this.list = map.getList();
            this.list = SortableList.makeSortableList(map, true, 50);
            this.list.sort(BlocksHeadMap.TIMESTAMP_INDEX, true);

            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_REMOVE_BLOCK_TYPE
                ) {
            this.list = SortableList.makeSortableList(map, true, 50);
            this.list.sort(BlocksHeadMap.TIMESTAMP_INDEX, true);

            this.fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.WALLET_RESET_BLOCK_TYPE
                ) {
            //CHECK IF LIST UPDATED
            //this.list = map.getList();
            this.list = SortableList.makeSortableList(map, true, 50);
            this.list.sort(BlocksHeadMap.TIMESTAMP_INDEX, true);

            this.fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.GUI_REPAINT
        ) {

            if (count++ < 1000)
                return;

            count = 0;

            //this.list = map.getList();
            this.list = SortableList.makeSortableList(map, true, 50);
            this.list.sort();
            this.fireTableDataChanged();
        }
    }

    public void addObservers() {
        map.addObserver(this);
        Controller.getInstance().guiTimer.addObserver(this);
    }

    public void deleteObservers() {
        Controller.getInstance().guiTimer.deleteObserver(this);
        map.deleteObserver(this);
    }

}
