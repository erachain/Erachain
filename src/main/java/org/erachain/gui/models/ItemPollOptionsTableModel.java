package org.erachain.gui.models;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.NumberAsString;
import org.mapdb.Fun;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
            new String[]{"Number", "Name", "Vote #P", "Vote % #P", "Share #Vote", "Share % #Vote"});
    private PollCls poll;
    private AssetCls asset;
    private Fun.Tuple4<Integer, long[], BigDecimal, BigDecimal[]> votesWithPersons;

    public ItemPollOptionsTableModel(PollCls poll, AssetCls asset) {
        this.poll = poll;
        this.asset = asset;
        votesWithPersons = poll.votesWithPersons(DCSet.getInstance(), asset.getKey(), 0);
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

                //List<Long> personsVotes = this.poll.getPersonCountVotes(DCSet.getInstance());
                //return personsVotes.get(row);

                return votesWithPersons.b[row];

            case COLUMN_PERSONAL_PERCENTAGE:

                Long personVotes = votesWithPersons.b[row];
                long totalPerson = votesWithPersons.a;

                //long totalPerson = this.poll.getPersonCountTotalVotes(DCSet.getInstance());
                //Long personVotes = this.poll.getPersonCountVotes(DCSet.getInstance()).get(row);

                if (personVotes == 0 || totalPerson == 0) {
                    return "0 %";
                }

                return new BigDecimal(100000 * personVotes / totalPerson).movePointLeft(3).toPlainString() + " %";

            case COLUMN_VOTES:

                //return NumberAsString.formatAsString(poll.getTotalVotes(DCSet.getInstance(), this.asset.getKey(DCSet.getInstance())));
                return votesWithPersons.d[row].setScale(asset.getScale()).toPlainString();

            case COLUMN_PERCENTAGE:

                //BigDecimal total = this.poll.getTotalVotes(DCSet.getInstance(), this.asset.getKey(DCSet.getInstance()));
                //BigDecimal votes = this.poll.getTotalVotes(DCSet.getInstance(), this.asset.getKey(DCSet.getInstance()), row);

                BigDecimal votes = votesWithPersons.d[row];
                BigDecimal total = votesWithPersons.c;

                if (votes.compareTo(BigDecimal.ZERO) == 0
                        || total.compareTo(BigDecimal.ZERO) == 0) {
                    return "0 %";
                }

                return BigDecimal.valueOf(100).multiply(votes).divide(total, 3, BigDecimal.ROUND_UP).toPlainString() + " %";

        }

        return null;
    }

    public void setAsset(AssetCls asset) {
        this.asset = asset;
        votesWithPersons = poll.votesWithPersons(DCSet.getInstance(), asset.getKey(), 0);
        this.fireTableDataChanged();
    }
}
