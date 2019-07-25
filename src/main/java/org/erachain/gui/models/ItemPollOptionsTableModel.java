package org.erachain.gui.models;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.NumberAsString;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.List;

@SuppressWarnings("serial")
public class ItemPollOptionsTableModel extends AbstractTableModel {
    private static final int COLUMN_KEY = 0;
    private static final int COLUMN_NAME = 1;
    public static final int COLUMN_PERSONAL_VOTES = 2;
    public static final int COLUMN_PERSONAL_PERCENTAGE = 3;
    public static final int COLUMN_VOTES = 4;
    public static final int COLUMN_PERCENTAGE = 5;
    private String[] columnNames = Lang.getInstance().translate(
            new String[]{"Number", "Name", "Persons", "% of Total", "Votes", "% of Total"});
    private PollCls poll;
    private AssetCls asset;

    public ItemPollOptionsTableModel(PollCls poll, AssetCls asset) {
        this.poll = poll;
        this.asset = asset;
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return this.columnNames[index];
    }

    @Override
    public int getRowCount() {
        return this.poll.getOptions().size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.poll.getOptions() == null || row > this.poll.getOptions().size() - 1) {
            return null;
        }

        switch (column) {
            case COLUMN_KEY:

                return row+1;

            case COLUMN_NAME:

                return this.poll.getOptions().get(row);

            case COLUMN_PERSONAL_VOTES:

                List<Long> personsVotes = this.poll.getPersonCountVotes(DCSet.getInstance());
                return personsVotes.get(row);

            case COLUMN_PERSONAL_PERCENTAGE:

                long totalPerson = this.poll.getPersonCountTotalVotes(DCSet.getInstance());
                Long personVotes = this.poll.getPersonCountVotes(DCSet.getInstance()).get(row);

                if (personVotes == 0 || totalPerson == 0) {
                    return "0 %";
                }

                return 100.0 * personVotes / totalPerson + " %";

            case COLUMN_VOTES:

                return NumberAsString.formatAsString(poll.getTotalVotes(DCSet.getInstance(), this.asset.getKey(DCSet.getInstance())));

            case COLUMN_PERCENTAGE:

                BigDecimal total = this.poll.getTotalVotes(DCSet.getInstance(), this.asset.getKey(DCSet.getInstance()));
                BigDecimal votes = this.poll.getTotalVotes(DCSet.getInstance(), this.asset.getKey(DCSet.getInstance()), row);

                if (votes.compareTo(BigDecimal.ZERO) == 0) {
                    return "0 %";
                }

                return votes.divide(total, BigDecimal.ROUND_UP).multiply(BigDecimal.valueOf(100)).toPlainString() + " %";

        }

        return null;
    }

    public void setAsset(AssetCls asset) {
        this.asset = asset;
        this.fireTableDataChanged();
    }
}
