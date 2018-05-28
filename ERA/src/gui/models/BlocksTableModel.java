package gui.models;

import controller.Controller;
import core.block.Block;
import datachain.DCSet;
import lang.Lang;
import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;
import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class BlocksTableModel extends AbstractTableModel implements Observer {

    public static final int COLUMN_HEIGHT = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_GENERATOR = 2;
    public static final int COLUMN_BASETARGET = 3;
    public static final int COLUMN_TRANSACTIONS = 4;
    public static final int COLUMN_FEE = 5;
    static Logger LOGGER = Logger.getLogger(BlocksTableModel.class.getName());
    private List<Block> blocks;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Height", "Timestamp", "Generator",
            "GB pH WV tWV", //"Generating Balance",
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

            DCSet dcSet = DCSet.getInstance();
            Block block = this.blocks.get(row);
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

                    if (block == null) {
                        //this.blocks.rescan();
                        //data = this.blocks.get(row);
                        return "-1";
                    }
                    if (row == 0) {
                        return block.getHeight(dcSet)
                                + " " + Controller.getInstance().getBlockChain().getFullWeight(dcSet);

                    }


                    return block.getHeight(dcSet)
                            + " " + block.getTarget();

                case COLUMN_TIMESTAMP:
                    if (block == null) {
                        //this.blocks.rescan();
                        //data = this.blocks.get(row);
                        return "-1";
                    }

                    return DateTimeFormat.timestamptoString(block.getTimestamp(dcSet));// + " " + block.getTimestamp(DBSet.getInstance())/ 1000;

                case COLUMN_GENERATOR:

                    if (block == null) {
                        //this.blocks.rescan();
                        //data = this.blocks.get(row);
                        return "-1";
                    }
                    return block.getCreator().getPersonAsString();


                case COLUMN_BASETARGET:

                    if (block == null) {
                        //this.blocks.rescan();
                        //data = this.blocks.get(row);
                        return "-1";
                    }

                    int height = block.getHeight(dcSet);
                    Tuple2<Integer, Integer> forgingPoint = block.getCreator().getForgingData(dcSet, height);

                    return forgingPoint.b + " "
                            + (height - forgingPoint.a) + " "
                            + block.getWinValue() + " "
                            + new BigDecimal(block.calcWinValueTargeted()).movePointLeft(3);

                case COLUMN_TRANSACTIONS:
                    if (block == null) {
                        //this.blocks.rescan();
                        //data = this.blocks.get(row);
                        return -1;
                    }

                    return block.getTransactionCount();

                case COLUMN_FEE:
                    if (block == null) {
                        //this.blocks.rescan();
                        //data = this.blocks.get(row);
                        return "-1";
                    }

                    return NumberAsString.formatAsString(block.getTotalFee());

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
            Block block = (Block) message.getValue();
            block.loadHeadMind(DCSet.getInstance());
            this.blocks.add(0, block);
            this.fireTableRowsInserted(0, 0);
            if (this.blocks.size() > 100) {
                this.blocks.remove(100);
                this.fireTableRowsDeleted(100, 100);
            }

        } else if (type == ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE) {
            //CHECK IF LIST UPDATED
            this.blocks.remove(0);
            if (this.blocks.size() > 10) {
                this.fireTableRowsDeleted(0, 0);
            } else {
                resetRows();
            }

        } else if (type == ObserverMessage.CHAIN_RESET_BLOCK_TYPE) {
            //CHECK IF LIST UPDATED
            LOGGER.error("gui.models.BlocksTableModel.syncUpdate- CHAIN_RESET_BLOCK_TYPE");
            resetRows();
            this.fireTableDataChanged();
        }
    }

    public void resetRows() {
        this.blocks = new ArrayList<Block>();
        Controller cntr = Controller.getInstance();
        DCSet dcSet = DCSet.getInstance();
        Block block = cntr.getLastBlock();
        for (int i = 0; i < 100; i++) {
            block.loadHeadMind(dcSet);
            this.blocks.add(block);
            block = cntr.getBlock(block.getReference());
            if (block == null)
                return;
        }
    }

    public void removeObservers() {
        //this.blocks.removeObserver();
        DCSet.getInstance().getBlockMap().deleteObserver(this);
    }
}
