package org.erachain.core.blockexplorer;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemStatusMap;
import org.erachain.lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;
import org.erachain.utils.ObserverMessage;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

////////

@SuppressWarnings("serial")
public class WebPersonStatusesModel extends AbstractTableModel implements Observer {
    public static final int COLUMN_MAKE_DATE = 0;
    public static final int COLUMN_STATUS_NAME = 1;
    public static final int COLUMN_PERIOD = 2;
    public static final int COLUMN_MAKER = 3;
    public static final int COLUMN_RECORD_NO = 4;
    public static final int COLUMN_MAKER_ACCOUNT = 31;

    TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> statuses;
    List<Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>>> statusesRows;

    SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
    //TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> addresses; //= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
    String from_date_str;
    String to_date_str;

    Long dteStart;
    Long dteEnd;
    boolean startIs = true;
    boolean endIs = true;

    ItemStatusMap statusesMap;
    long itemKey;
    private DCSet dcSet = DCSet.getInstance();
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Maked", "Status", "Period", "Creator", "RecNo"}); //, "Data"});
    private Boolean[] column_AutuHeight = new Boolean[]{true, false};

    public WebPersonStatusesModel(long person_Key) {

        itemKey = person_Key;
        Controller.getInstance().addWalletListener(this);
        statuses = dcSet.getPersonStatusMap().get(itemKey);
        statusesMap = dcSet.getItemStatusMap();
        setRows();
    }


    public TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> getSortableList() {
        return statuses;
    }


// set class

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

    public Stack<Tuple5<Long, Long, byte[], Integer, Integer>> getStatus(int row) {
        return this.statuses.get(row);
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
        return statusesRows.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
		/*
		if( statuses == null || row >  statuses.size() - 1 )
		{
			return null;
		}
		
		Long status_key_value = 0l;
		int i = 0;
		for ( Long status_key: statuses.keySet()) {
			if (i++ == row)
			{
				status_key_value = status_key;
				break;
			}
		}
		 Stack<Tuple5<Long, Long, byte[], Integer, Integer>> entry = statuses.get(status_key_value);
		if (entry == null || entry.isEmpty() ) return 0;
		
		 Tuple5<Long, Long, byte[], Integer, Integer> value = entry.peek();
		 
		 */
        if (statusesRows == null || row > statusesRows.size() - 1) {
            return null;
        }

        Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>> value = statusesRows.get(row);
        int block;
        int recNo;
        Transaction record;

        switch (column) {

            case COLUMN_MAKE_DATE:

                block = value.b.d;
                recNo = value.b.e;
                record = Transaction.findByHeightSeqNo(dcSet, block, recNo);
                return record == null ? null : record.viewTimestamp();

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
                return record == null ? "" : ((Account) record.getCreator()).getPersonAsString();

            case COLUMN_MAKER_ACCOUNT:

                block = value.b.d;
                recNo = value.b.e;
                record = Transaction.findByHeightSeqNo(dcSet, block, recNo);
                return record == null ? null : record.getCreator();


            case COLUMN_RECORD_NO:

                block = value.b.d;
                recNo = value.b.e;
                return block > 0 ? block + "-" + recNo : "???";

        }

        return null;
    }

    public PersonCls getCreatorPerson(int row) {
        Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>> value = statusesRows.get(row);
        int block = value.b.d;
        int recNo = value.b.e;
        Transaction record = Transaction.findByHeightSeqNo(dcSet, block, recNo);
        return record == null ? null : record.getCreator().getPerson().b;


    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_STATUS_TYPE) {
            if (this.statuses == null) {
                this.statuses = (TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>) message.getValue();
                setRows();
            }

            this.fireTableDataChanged();
        }


        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_STATUS_TYPE
                || message.getType() == ObserverMessage.REMOVE_STATUS_TYPE
                || message.getType() == ObserverMessage.ADD_PERSON_STATUS_TYPE
                || message.getType() == ObserverMessage.REMOVE_PERSON_STATUS_TYPE
                || message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE) {
            //this.statuses = (TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>) message.getValue();
            statuses = dcSet.getPersonStatusMap().get(itemKey);
            setRows();
            this.fireTableDataChanged();
        }
    }

    public void setRows() {
        statusesRows = new ArrayList<Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>>>();

        for (long statusKey : statuses.keySet()) {
            Stack<Tuple5<Long, Long, byte[], Integer, Integer>> statusStack = statuses.get(statusKey);
            if (statusStack == null || statusStack.isEmpty()) {
                return;
            }

            StatusCls status = (StatusCls) statusesMap.get(statusKey);
            if (status.isUnique()) {
                // UNIQUE - only on TOP of STACK
                statusesRows.add(new Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>>(statusKey, statusStack.peek()));
            } else {
                for (Tuple5<Long, Long, byte[], Integer, Integer> statusItem : statusStack) {
                    statusesRows.add(new Tuple2<Long, Tuple5<Long, Long, byte[], Integer, Integer>>(statusKey, statusItem));
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

            Collections.sort(statusesRows, comparator);

        }
    }

}
