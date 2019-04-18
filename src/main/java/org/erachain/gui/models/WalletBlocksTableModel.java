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

@SuppressWarnings("serial")
public class WalletBlocksTableModel extends SortedListTableModelCls<Tuple2<String, String>, Block.BlockHead> {
    private int count;
    public static final int COLUMN_HEIGHT = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_GENERATOR = 2;
    public static final int COLUMN_GB = 3;
    public static final int COLUMN_dtWV = 4;
    public static final int COLUMN_TRANSACTIONS = 5;
    public static final int COLUMN_FEE = 6;

    public WalletBlocksTableModel() {
        super(Controller.getInstance().wallet.database.getBlocksHeadMap(),
                new String[]{"Height", "Timestamp", "Creator account", "Gen.Balance", "dtWV", "Transactions", "Fee"},
                new Boolean[]{false, true, true, false, false, true, false}, true);

        // сначала ЛОГЕР задаем
        logger = LoggerFactory.getLogger(WalletBlocksTableModel.class.getName());

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            addObservers();
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        try {
            if (listSorted == null || listSorted.size() - 1 < row) {
                return null;
            }
            Pair<Tuple2<String, String>, Block.BlockHead> data = listSorted.get(row);
            if (data == null) {
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
                case COLUMN_GB:
                    return block.forgingValue + " ";
                case COLUMN_dtWV:
                    return (float) (100000 * (block.forgingValue - block.target) / block.target) / 1000.0 + ""; //.movePointLeft(3);
                case COLUMN_TRANSACTIONS:
                    return block.transactionsCount;
                case COLUMN_FEE:
                    return BigDecimal.valueOf(block.totalFee, BlockChain.FEE_SCALE);
            }
        } catch (Exception e) {
            logger.error(e.getMessage() + " row:" + row, e);
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;
        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_BLOCK_TYPE) {
            needUpdate = true;
        } else if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_REMOVE_BLOCK_TYPE) {
            needUpdate = true;
        } else if (message.getType() == ObserverMessage.WALLET_RESET_BLOCK_TYPE) {
            count = 0;
            needUpdate = false;
            listSorted = SortableList.makeSortableList(map, true, 50);
            listSorted.sort();
            fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.GUI_REPAINT && needUpdate) {
            needUpdate = false;
            listSorted = SortableList.makeSortableList(map, true, 50);
            listSorted.sort();
            fireTableDataChanged();
        }
    }

    public void addObservers() {

        super.addObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.addObserver(this);

    }

    public void deleteObservers() {
        super.deleteObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.deleteObserver(this);
    }

}
