package org.erachain.gui.items.other;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class OtherSeasrchBlocksTableModel extends AbstractTableModel {

    public static final int COLUMN_HEIGHT = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_GENERATOR = 2;
    public static final int COLUMN_BASETARGET = 3;
    public static final int COLUMN_TRANSACTIONS = 4;
    public static final int COLUMN_FEE = 5;
    static Logger LOGGER = LoggerFactory.getLogger(OtherSeasrchBlocksTableModel.class);
    private List<Block> blocks;
    private String[] columnNames = Lang.T(new String[]{"Height", "Timestamp", "Generator", "GB pH WV dtWV", // "Generating
                    // Balance",
                    "Transactions", "Fee"});
    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false, true, false};

    public OtherSeasrchBlocksTableModel() {
        //
        //	searchBlock(1,1);
    }

    public Block getBlock(Integer row) {
        return blocks.get(row);

    }

    @Override
    public Class<? extends Object> getColumnClass(int c) { // set column type
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
     * @Override public SortableList<byte[], Block> getSortableList() { return
     * this.blocks; } public SortableList<byte[], Block> getSortableList() {
     * return this.blocks; }
     */

    public void clear() {
        blocks = null;
        blocks = new ArrayList();
        this.fireTableDataChanged();

    }

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

        // if(row >100)return null;
        try {

            if (this.blocks == null || this.blocks.size() - 1 < row) {
                return null;
            }

            DCSet dcSet = DCSet.getInstance();
            Block block = this.blocks.get(row);
            // Block block = data.getB();
            if (block == null) {
                // this.blocks.rescan();
                // data = this.blocks.get(row);
                // return -1;
            } else {
                //block.calcHeadMind(dcSet);
            }


            switch (column) {
                case COLUMN_HEIGHT:

                    if (block == null) {
                        // this.blocks.rescan();
                        // data = this.blocks.get(row);
                        return "-1";
                    }
                    if (row == 0) {
                        return block.getHeight() + " " + Controller.getInstance().getBlockChain().getFullWeight(dcSet);

                    }

                    return block.getHeight() + " " + block.getTarget();

                case COLUMN_TIMESTAMP:
                    if (block == null) {
                        // this.blocks.rescan();
                        // data = this.blocks.get(row);
                        return "-1";
                    }

                    return DateTimeFormat.timestamptoString(block.getTimestamp());// +
                // "
                // "
                // +
                // block.getTimestamp(DLSet.getInstance())/
                // 1000;

                case COLUMN_GENERATOR:

                    if (block == null) {
                        // this.blocks.rescan();
                        // data = this.blocks.get(row);
                        return "-1";
                    }
                    return block.getCreator().getPersonAsString();

                case COLUMN_BASETARGET:

                    if (block == null) {
                        // this.blocks.rescan();
                        // data = this.blocks.get(row);
                        return "-1";
                    }

                    int height = block.getHeight();
                    Tuple3<Integer, Integer, Integer> forgingPoint = block.getCreator().getForgingData(dcSet, height);

                    return forgingPoint.b + " "
                            + (height - forgingPoint.a) + " "
                            + block.getWinValue() + " "
                            + new BigDecimal(block.calcWinValueTargeted() - 100000);

                case COLUMN_TRANSACTIONS:
                    if (block == null) {
                        // this.blocks.rescan();
                        // data = this.blocks.get(row);
                        return -1;
                    }

                    return block.getTransactionCount();

                case COLUMN_FEE:
                    if (block == null) {
                        // this.blocks.rescan();
                        // data = this.blocks.get(row);
                        return "-1";
                    }

                    return block.viewTotalFeeAsBigDecimal();

            }

            return null;

        } catch (Exception e) {
            LOGGER.error(e.getMessage() + "\n row:" + row, e);
            return null;
        }
    }


    public void searchBlock(Integer start, Integer end) {
        clear();
        if (start <= end) {
            DCSet dcSet = DCSet.getInstance();
            for (Integer i = start; i <= end; i++) {
                Block block = DCSet.getInstance().getBlockMap().getAndProcess(i);
                if (block != null) {
                    blocks.add(block);
                }
            }
        }

    }

}
