package gui.models;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.block.Block;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import core.transaction.CreateOrderTransaction;
import core.transaction.Transaction;
import datachain.DCSet;
import datachain.SortableList;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.DateTimeFormat;
import utils.ObserverMessage;
import utils.Pair;

import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletOrdersTableModel extends TableModelCls<Tuple2<String, Long>, Order> implements Observer {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_AMOUNT = 2;
    public static final int COLUMN_HAVE = 3;
    public static final int COLUMN_WANT = 4;
    public static final int COLUMN_PRICE = 5;
    public static final int COLUMN_FULFILLED = 6;
    public static final int COLUMN_CREATOR = 7;
    public static final int COLUMN_STATUS = 8;
    public static final int COLUMN_BLOCK = 1;
    int start =0,step=100;

    private SortableList<Tuple2<String, Long>, Order> orders;
    List<Pair<Tuple2<String, Long>, Order>> pp = new ArrayList<Pair<Tuple2<String, Long>, Order>>();
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", " ", "Amount", "Have", "Want", "Price", "Fulfilled", "Creator", "Status"});

    public WalletOrdersTableModel() {
        columnNames[COLUMN_BLOCK]= Lang.getInstance().translate("Block") + "-" + Lang.getInstance().translate("Transaction");
        Controller.getInstance().addWalletListener(this);
    }

    @Override
    public SortableList<Tuple2<String, Long>, Order> getSortableList() {
        return this.orders;
    }

    @Override
    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    public Order getOrder(int row) {
        if (this.pp == null || row >= this.pp.size())
            return null;

        Pair<Tuple2<String, Long>, Order> item = this.pp.get(row);
        if (item == null)
            return null;

        return item.getB();
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
        //	 return this.orders.size();
        return (this.pp == null) ? 0 : this.pp.size();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.pp == null || row >= this.pp.size()) {
            return null;
        }
        Pair<Tuple2<String, Long>, Order> item = this.pp.get(row);
        Order order = item.getB();
        Long blockDBrefLong = item.getA().b;
        Tuple2<Integer, Integer> blockDBref = Transaction.parseDBRef(blockDBrefLong);

        switch (column) {
            case COLUMN_TIMESTAMP:

                return DateTimeFormat.timestamptoString(Controller.getInstance().getBlockChain().getTimestamp(blockDBref.a));

            case COLUMN_HAVE:

                AssetCls asset = DCSet.getInstance().getItemAssetMap().get(order.getHave());
                return asset == null? -1 : asset.getShort();

            case COLUMN_WANT:

                asset = DCSet.getInstance().getItemAssetMap().get(order.getWant());
                return asset == null? -1 : asset.getShort();

            case COLUMN_AMOUNT:

                return order.getAmountHave().toPlainString();

            case COLUMN_PRICE:

                return order.getPrice();

            case COLUMN_FULFILLED:

                return order.getFulfilledHave().toPlainString();

            case COLUMN_CREATOR:

                return order.getCreator().getPersonAsString();

            case COLUMN_STATUS:

                if (order.getAmountHave().compareTo(order.getFulfilledHave()) == 0) {
                    return "DONE";
                } else {

                    if (DCSet.getInstance().getCompletedOrderMap().contains(order.getId()))
                        return "Canceled";

                    if (DCSet.getInstance().getOrderMap().contains(order.getId()))
                        return "ACTIVE";

                    return "unconfirmed";

                }
            case COLUMN_BLOCK:

                return blockDBref.a + "-" + blockDBref.b ;

        }

        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
            String msg = e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_RESET_ORDER_TYPE) {
            this.orders = (SortableList<Tuple2<String, Long>, Order>) message.getValue();
            this.orders.sort(0, true);
            getInterval(start, step);
            this.fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.WALLET_LIST_ORDER_TYPE) {
            if (this.orders == null) {
                this.orders = (SortableList<Tuple2<String, Long>, Order>) message.getValue();
                this.orders.sort(0, true);
                this.orders.registerObserver();
            }
            getInterval(start,step);
            this.fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.WALLET_ADD_ORDER_TYPE) {
            //CHECK IF LIST UPDATED
            Pair<Tuple2<String, Long>, Order> item = (Pair<Tuple2<String, Long>, Order>) message.getValue();
            //this.pp.add(0, item);
            //this.fireTableRowsInserted(0, 0);
            getInterval(start,step);
            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.WALLET_REMOVE_ORDER_TYPE) {
            //CHECK IF LIST UPDATED
            //this.pp.remove(0);
            //this.fireTableRowsDeleted(0, 0);
            getInterval(start,step);
            this.fireTableDataChanged();
        }

    }

    public void removeObservers() {
        this.orders.removeObserver();
        Controller.getInstance().deleteWalletObserver(this);
    }

    @Override
    public Object getItem(int k) {
        // TODO Auto-generated method stub
        return this.orders.get(k).getB();
    }

    public void getInterval(int start,int step){
       // pp.c.clear();
        int end = start + step;
        //if (start > orders.size()) start = orders.size();
        if (end > orders.size()) end = orders.size();

        this.start = start;
        this.step = step;

        // new (!) LIST
        pp = new ArrayList<>(this.orders.subList(start, end));

    }

    public void setInterval(int start, int step){
        getInterval(start,step);
    }

}
