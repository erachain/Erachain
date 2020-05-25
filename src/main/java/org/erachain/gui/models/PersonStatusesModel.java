package org.erachain.gui.models;

import org.erachain.core.account.Account;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemStatusMap;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.text.SimpleDateFormat;
import java.util.*;

////////

@SuppressWarnings("serial")
public class PersonStatusesModel extends TimerTableModelCls<Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>>> {

    public static final int COLUMN_STATUS_NAME = 0;
    public static final int COLUMN_PERIOD = 1;
    public static final int COLUMN_MAKER = 2;
    public static final int COLUMN_CREATOR_NAME = 30;

    //TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> statuses;

    SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
    //TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> addresses; //= DLSet.getInstance().getPersonAddressMap().getItems(person.getKey());
    String from_date_str;
    String to_date_str;

    Long dteStart;
    Long dteEnd;
    boolean startIs = true;
    boolean endIs = true;

    ItemStatusMap statusesMap;
    long itemKey;
    private DCSet dcSet = DCSet.getInstance();

    public PersonStatusesModel(long personKey) {

        super(DCSet.getInstance().getPersonStatusMap(),
                new String[]{"Status", "Period", "Appointing"},
                new Boolean[]{true, false}, false);

        itemKey = personKey;
        statusesMap = dcSet.getItemStatusMap();

        addObservers();

    }

    public String getTransactionHeightSeqNo(int row) {

        Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>> value = list.get(row);
        return value.b.d + "-" + value.b.e;
    }

    public Account getCreator(int row) {

        Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>> value = list.get(row);

        int block = value.b.d;
        int recNo = value.b.e;
        Transaction transaction = Transaction.findByHeightSeqNo(dcSet, block, recNo);
        return transaction.getCreator();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (list == null || row > list.size() - 1) {
            return null;
        }

        Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>> value = list.get(row);
        int block;
        int recNo;
        Transaction record;

        switch (column) {


            case COLUMN_STATUS_NAME:

                return statusesMap.get(value.a).toString(dcSet, value.b.c);

            case COLUMN_PERIOD:

                dteStart = value.b.a;
                dteEnd = value.b.b;
                startIs = true;
                endIs = true;

                if (dteStart == null || dteStart == Long.MIN_VALUE) {
                    from_date_str = "-> ";
                    startIs = false;
                } else from_date_str = formatDate.format(new Date(dteStart));

                if (dteEnd == null || dteEnd == Long.MAX_VALUE) {
                    to_date_str = " ->";
                    endIs = false;
                } else to_date_str = formatDate.format(new Date(dteEnd));

                return !startIs && !endIs? "" :
                        from_date_str + (startIs && endIs? " - " : "") + to_date_str;

            case COLUMN_MAKER:

                block = value.b.d;
                recNo = value.b.e;
                record = Transaction.findByHeightSeqNo(dcSet, block, recNo);
                return record == null ? "" : record.getCreator().getPersonAsString();

            case COLUMN_CREATOR_NAME:

                block = value.b.d;
                recNo = value.b.e;
                record = Transaction.findByHeightSeqNo(dcSet, block, recNo);
                return record == null ? "" : record.getCreator().getPerson().b.viewName();


        }

        return null;
    }

    @Override
    public void getIntervalThis(long start, int limit) {
        TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> statuses = dcSet.getPersonStatusMap().get(itemKey);

        list = new ArrayList<Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>>>();

        for (long statusKey : statuses.keySet()) {
            Stack<Tuple5<Long, Long, byte[], Integer, Integer>> statusStack = statuses.get(statusKey);
            if (statusStack == null || statusStack.isEmpty()) {
                return;
            }

            StatusCls status = (StatusCls) statusesMap.get(statusKey);
            if (status.isUnique()) {
                // UNIQUE - only on TOP of STACK
                list.add(new Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>>(statusKey, statusStack.peek()));
            } else {
                for (Tuple5<Long, Long, byte[], Integer, Integer> statusItem : statusStack) {
                    list.add(new Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>>(statusKey, statusItem));
                }
            }

            Comparator<Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>>> comparator = new Comparator<Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>>>() {
                public int compare(Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>> c1, Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>> c2) {
                    if (c1.b.d > c2.b.d)
                        return 1;
                    else if (c1.b.d < c2.b.d)
                        return -1;

                    if (c1.b.e > c2.b.e)
                        return 1;
                    else if (c1.b.e < c2.b.e)
                        return -1;

                    return 0;
                }
            };

            Collections.sort(list, comparator);

        }

    }

}
