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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletBlocksTableModel extends TableModelCls<Tuple2<String, String>, Block.BlockHead> implements Observer {

    public static final int COLUMN_HEIGHT = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_GENERATOR = 2;
    public static final int COLUMN_BASETARGET = 3;
    public static final int COLUMN_TRANSACTIONS = 4;
    public static final int COLUMN_FEE = 5;
    private SortableList<Tuple2<String, String>, Block.BlockHead> blocks;

    public WalletBlocksTableModel() {
        super(Controller.getInstance().wallet.database.getBlocksHeadMap(), "WalletBlocksTableModel", 1000,
                new String[]{"Height", "Timestamp", "Generator",
                        "GB dtWV", //"Generating Balance",
                        "Transactions", "Fee"}, new Boolean[]{false, true, true, false, true, false});
        if (!Controller.getInstance().doesWalletDatabaseExists()) {
            this.blocks = null;
        } else {
            addObservers();
        }
        LOGGER = LoggerFactory.getLogger(WalletBlocksTableModel.class.getName());
    }

    @Override
    public SortableList<Tuple2<String, String>, Block.BlockHead> getSortableList() {
        return this.blocks;
    }

    @Override
    public int getRowCount() {
        if (blocks == null) {
            return 0;
        }

        return blocks.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        try {
            if (this.blocks == null || this.blocks.size() - 1 < row) {
                return null;
            }

            //
            Pair<Tuple2<String, String>, Block.BlockHead> data = this.blocks.get(row);

            if (data == null || data.getB() == null) {
                return null;
            }

            Block.BlockHead block = data.getB();
            if (block == null) {
                //this.fireTableDataChanged();
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

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_BLOCK_TYPE) {
            //this.blocks.registerObserver();
            //Controller.getInstance().wallet.database.getBlocksHeadMap().addObserver(this.blocks);
            //this.blocks = (SortableList<Tuple2<String, String>, Block.BlockHead>) message.getValue();
            this.blocks = Controller.getInstance().wallet.database.getBlocksHeadMap().getList();
            //this.blocks.sort(BlocksHeadMap.TIMESTAMP_INDEX, true);
            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_REMOVE_BLOCK_TYPE
                ) {
            // тут вставить обработку на запрос обновления - не обновлять сразу - а раз в 30 секунд только
            // как это сделано в Мои Ордера
            //CHECK IF LIST UPDATED
            //this.blocks = (SortableList<Tuple2<String, String>, Block.BlockHead>) message.getValue();
            this.fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.WALLET_RESET_BLOCK_TYPE
                ) {
            //CHECK IF LIST UPDATED
            this.blocks = Controller.getInstance().wallet.database.getBlocksHeadMap().getList();
            //this.blocks.registerObserver();
            //this.blocks.sort(BlocksHeadMap.TIMESTAMP_INDEX, true);
            this.fireTableDataChanged();
        }
    }

    protected void addObserversThis() {
        Controller.getInstance().wallet.database.getBlocksHeadMap().addObserver(this);
        this.blocks = Controller.getInstance().wallet.database.getBlocksHeadMap().getList();
        Controller.getInstance().wallet.database.getBlocksHeadMap().addObserver(this.blocks);
        this.blocks.sort(BlocksHeadMap.TIMESTAMP_INDEX, true);
    }

    protected void removeObserversThis() {
        Controller.getInstance().wallet.database.getBlocksHeadMap().deleteObserver(this);
        Controller.getInstance().wallet.database.getBlocksHeadMap().deleteObserver(this.blocks);
    }

    @Override
    public Block.BlockHead getItem(int k) {
        return this.blocks.get(k).getB();
    }
}
