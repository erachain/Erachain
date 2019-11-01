package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.database.wallet.BlocksHeadMap;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

@SuppressWarnings("serial")
public class WalletBlocksTableModel extends WalletTableModel<Block.BlockHead> {
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
    }

    @Override
    public Object getValueAt(int row, int column) {
        try {
            if (list == null || list.size() - 1 < row) {
                return null;
            }
            Block.BlockHead blockHead = list.get(row);
            if (blockHead == null) {
                return null;
            }

            switch (column) {
                case COLUMN_HEIGHT:
                    return blockHead.heightBlock;
                case COLUMN_TIMESTAMP:
                    BlockChain blockChain = Controller.getInstance().getBlockChain();
                    return DateTimeFormat.timestamptoString(blockChain.getTimestamp(blockHead.heightBlock));
                case COLUMN_GENERATOR:
                    return blockHead.creator.getPersonAsString();
                case COLUMN_GB:
                    return blockHead.forgingValue + " ";
                case COLUMN_dtWV:
                    return (float) (100000 * (blockHead.forgingValue - blockHead.target) / blockHead.target) / 1000.0 + ""; //.movePointLeft(3);
                case COLUMN_TRANSACTIONS:
                    return blockHead.transactionsCount;
                case COLUMN_FEE:
                    return BigDecimal.valueOf(blockHead.totalFee, BlockChain.FEE_SCALE);
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
        if (message.getType() == ObserverMessage.GUI_REPAINT && needUpdate
                || message.getType() == ObserverMessage.WALLET_LIST_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_RESET_BLOCK_TYPE) {
            needUpdate = false;
            list = new ArrayList<>();
            Iterator iterator = map.getIterator(BlocksHeadMap.TIMESTAMP_INDEX, true);
            int count = 50;
            while (iterator.hasNext() && --count > 0) {
                list.add((Block.BlockHead) map.get(iterator.next()));
            }
            fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_REMOVE_BLOCK_TYPE) {
            needUpdate = true;
        }
    }

}
