package org.erachain.gui.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Fun.Tuple2;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;

@SuppressWarnings("serial")
public class BlocksTableModel extends AbstractTableModel implements Observer {

    public static final int maxSize = 50;

    public static final int COLUMN_HEIGHT = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_GENERATOR = 2;
    public static final int COLUMN_BASETARGET = 3;
    public static final int COLUMN_TRANSACTIONS = 4;
    public static final int COLUMN_FEE = 5;
    static Logger LOGGER = LoggerFactory.getLogger(BlocksTableModel.class.getName());
    private List<Block.BlockHead> blocks;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Height", "Timestamp", "Generator",
            "GB pH WV dtWV", //"Generating Balance",
            "Transactions", "Fee"});
    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false, true, false};

    public BlocksTableModel(boolean select_Last_100) {
        //	Controller.getInstance().addObserver(this);
        DCSet.getInstance().getBlockMap().addObserver(this);

        resetRows();

    }

    @Override
    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

	/*
	@Override
	public SortableList<byte[], Block> getSortableList() {
		return this.blocks;
	}
	public SortableList<byte[], Block> getSortableList() {
		return this.blocks;
	}
	 */

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return columnNames[index];
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

        //if(row >100)return null;
        try {

            if (this.blocks == null || this.blocks.size() - 1 < row) {
                return null;
            }

            Block.BlockHead block = this.blocks.get(row);
            //Block block = data.getB();
            if (block == null) {
                //this.blocks.rescan();
                //data = this.blocks.get(row);
                //	return -1;
            } else {
                //block.calcHeadMind(dcSet);
            }

            switch (column) {
                case COLUMN_HEIGHT:

                    if (row == 0) {
                        return block.heightBlock
                                + " " + Controller.getInstance().getBlockChain().getFullWeight(DCSet.getInstance());

                    }


                    return block.heightBlock
                            + " " + block.target;

                case COLUMN_TIMESTAMP:

                    return DateTimeFormat.timestamptoString(block.getTimestamp());// + " " + block.getTimestamp(DBSet.getInstance())/ 1000;

                case COLUMN_GENERATOR:

                    return block.creator.getPersonAsString();


                case COLUMN_BASETARGET:

                    //int height = block.heightBlock;
                    Tuple2<Integer, Integer> forgingPoint = block.creator.getForgingData(DCSet.getInstance(), block.heightBlock);
                    if (block.target == 0)
                        return "GENESIS";

                    return forgingPoint.b + " "
                            + (block.heightBlock - forgingPoint.a) + " "
                            + block.winValue + " "
                            + new Float(100000 * (block.winValue - block.target)/block.target)/1000.0 + "%"; //.movePointLeft(3);

                case COLUMN_TRANSACTIONS:

                    return block.transactionsCount;

                case COLUMN_FEE:

                    return block.totalFee;

            }

            return null;

        } catch (Exception e) {
            LOGGER.error(e.getMessage() + "\n row:" + row, e);
            return null;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
            LOGGER.error(e.getMessage(), e);
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;
        int type = message.getType();

        if (type == ObserverMessage.CHAIN_LIST_BLOCK_TYPE) {
            //CHECK IF NEW LIST

            LOGGER.error("gui.models.BlocksTableModel.syncUpdate- CHAIN_LIST_BLOCK_TYPE");
            if (blocks == null) {
                this.resetRows();
                this.fireTableDataChanged();
            }

        } else if (type == ObserverMessage.CHAIN_ADD_BLOCK_TYPE) {
            //CHECK IF LIST UPDATED
            Block.BlockHead block = (Block.BlockHead) message.getValue();
            this.blocks.add(0, block);
            this.fireTableRowsInserted(0, 0);
            boolean needFire = false;
            while(this.blocks.size() > maxSize) {
                this.blocks.remove(maxSize);
                needFire = true;
            }

            if (needFire) this.fireTableRowsDeleted(maxSize, maxSize);

        } else if (type == ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE) {
            //CHECK IF LIST UPDATED
            try {
                this.blocks.remove(0);
            } catch (Exception e) {
                resetRows();
                this.fireTableDataChanged();
                return;
            }
            if (this.blocks.size() > 10) {
                this.fireTableRowsDeleted(0, 0);
            } else {
                resetRows();
                this.fireTableDataChanged();
            }

        } else if (type == ObserverMessage.CHAIN_RESET_BLOCK_TYPE) {
            //CHECK IF LIST UPDATED
            LOGGER.error("gui.models.BlocksTableModel.syncUpdate- CHAIN_RESET_BLOCK_TYPE");
            resetRows();
            this.fireTableDataChanged();
        }
    }

    public void resetRows() {
        this.blocks = new ArrayList<Block.BlockHead>();
        Controller cntr = Controller.getInstance();
        DCSet dcSet = DCSet.getInstance();
        Block.BlockHead head = cntr.getLastBlock().blockHead;
        int i = 0;
        while (i <= maxSize) {
            if (head == null)
                return;
            this.blocks.add(head);
            head = cntr.getBlockHead(head.heightBlock - ++i);
        }
    }

    public void removeObservers() {
        //this.blocks.removeObserver();
        DCSet.getInstance().getBlockMap().deleteObserver(this);
    }
}
