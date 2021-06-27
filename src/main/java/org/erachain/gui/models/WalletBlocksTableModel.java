package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;

import java.io.IOException;
import java.util.ArrayList;
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
        super(Controller.getInstance().getWallet().dwSet.getBlocksHeadMap(),
                new String[]{"Height", "Timestamp", "Creator account", "Gen.Balance", "dtWV", "Transactions", "Fee"},
                new Boolean[]{false, true, true, false, false, true, false}, true, -1);
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
                    //return (float) (100000 * (blockHead.forgingValue - blockHead.target) / blockHead.target) / 1000.0 + ""; //.movePointLeft(3);
                    if (blockHead.heightBlock == 1) {
                        return "GENESIS";
                    }
                    if (blockHead.target == 0) {
                        return "--";
                    }
                    return String.format("%10.3f%%", (100f * (blockHead.winValue - blockHead.target) / blockHead.target));

                case COLUMN_TRANSACTIONS:
                    return blockHead.transactionsCount;
                case COLUMN_FEE:
                    return blockHead.viewFeeAsBigDecimal();
                //return BigDecimal.valueOf(blockHead.totalFee, BlockChain.FEE_SCALE);
            }
        } catch (Exception e) {
            logger.error(e.getMessage() + " row:" + row, e);
        }
        return null;
    }

    @Override
    public void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;
        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.GUI_REPAINT && needUpdate
                || message.getType() == ObserverMessage.WALLET_LIST_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_RESET_BLOCK_TYPE) {
            needUpdate = false;
            list = new ArrayList<>();
            try (IteratorCloseable iterator = map.getIndexIterator(0, true)) {
                int count = 50;
                while (iterator.hasNext() && --count > 0) {
                    list.add((Block.BlockHead) map.get(iterator.next()));
                }
            } catch (IOException e) {
            }
            fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_REMOVE_BLOCK_TYPE) {
            needUpdate = true;
        }
    }

}
