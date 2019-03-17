package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletOrdersTableModel extends TableModelCls<Tuple2<String, Long>, Order> implements Observer {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_BLOCK = 1;
    public static final int COLUMN_AMOUNT = 2;
    public static final int COLUMN_HAVE = 3;
    public static final int COLUMN_PRICE = 4;
    public static final int COLUMN_WANT = 5;
    public static final int COLUMN_AMOUNT_WANT = 6;
    public static final int COLUMN_LEFT = 7;
    public static final int COLUMN_CREATOR = 8;
    public static final int COLUMN_STATUS = 9;
    int start = 0, step = 100;

    private SortableList<Tuple2<String, Long>, Order> orders;
    List<Pair<Tuple2<String, Long>, Order>> pp = new ArrayList<Pair<Tuple2<String, Long>, Order>>();

    public WalletOrdersTableModel() {
        super("WalletOrdersTableModel", 1000,
                new String[]{"Timestamp", "Block - transaction", "Amount", "Have", "Price", "Want", "Total", "Left", "Creator", "Status"});
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
        if (item == null)
            return null;

        Order order = item.getB();
        Long blockDBrefLong = item.getA().b;
        Tuple2<Integer, Integer> blockDBref = Transaction.parseDBRef(blockDBrefLong);

        switch (column) {
            case COLUMN_TIMESTAMP:

                return DateTimeFormat.timestamptoString(Controller.getInstance().getBlockChain().getTimestamp(blockDBref.a));

            case COLUMN_AMOUNT:

                return order.getAmountHave().toPlainString();

            case COLUMN_HAVE:

                AssetCls asset = DCSet.getInstance().getItemAssetMap().get(order.getHave());
                return asset == null ? "[" + order.getHave() + "]" : asset.getShort();

            case COLUMN_PRICE:

                return order.getPrice();

            case COLUMN_WANT:

                asset = DCSet.getInstance().getItemAssetMap().get(order.getWant());
                return asset == null ? "[" + order.getWant() + "]" : asset.getShort();

            case COLUMN_AMOUNT_WANT:

                return order.getAmountWant().toPlainString();


            case COLUMN_LEFT:

                return order.getFulfilledWant().toPlainString();

            case COLUMN_CREATOR:

                return order.getCreator().getPersonAsString();

            case COLUMN_STATUS:

                if (order.getAmountHave().compareTo(order.getFulfilledHave()) == 0) {
                    return Lang.getInstance().translate("Done");
                } else {

                    if (DCSet.getInstance().getCompletedOrderMap().contains(order.getId()))
                        return Lang.getInstance().translate("Canceled");

                    if (DCSet.getInstance().getOrderMap().contains(order.getId())) {
                        if (order.getFulfilledHave().signum() == 0)
                            return Lang.getInstance().translate("Active");
                        else
                            return Lang.getInstance().translate("Fulfilled");
                    }

                    return "orphaned"; //"unconfirmed";
                }
                // TODO: return "orphaned !";

            case COLUMN_BLOCK:

                return blockDBref == null ? "?-?" : blockDBref.a + "-" + blockDBref.b;

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
            this.pp.clear();
            this.fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.WALLET_LIST_ORDER_TYPE) {
            if (this.orders == null) {
                this.orders = (SortableList<Tuple2<String, Long>, Order>) message.getValue();
                this.orders.sort(0, true);
                this.orders.registerObserver();
            }
            getInterval(start, step);
            this.fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.WALLET_ADD_ORDER_TYPE) {
            //CHECK IF LIST UPDATED
            Pair<Tuple2<String, Long>, Order> item = (Pair<Tuple2<String, Long>, Order>) message.getValue();
            //this.pp.add(0, item);
            //this.fireTableRowsInserted(0, 0);
            getInterval(start, step);
            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.WALLET_REMOVE_ORDER_TYPE) {
            //CHECK IF LIST UPDATED
            //this.pp.remove(0);
            //this.fireTableRowsDeleted(0, 0);
            getInterval(start, step);
            this.fireTableDataChanged();
        }

    }

    public void addObserversThis() {
        Controller.getInstance().addWalletListener(this);

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            this.orders.registerObserver();
            Controller.getInstance().addWalletListener(this);
        }
    }

    public void removeObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            this.orders.removeObserver();
            Controller.getInstance().deleteWalletObserver(this);
        }
    }

    @Override
    public Object getItem(int k) {
        if (this.orders == null)
            return null;

        // TODO Auto-generated method stub
        return this.orders.get(k).getB();
    }

    public void getInterval(int start, int step) {

        if (this.orders == null || orders.isEmpty())
            pp = new ArrayList<>();

        // pp.c.clear();
        int end = start + step;
        //if (start > orders.size()) start = orders.size();
        if (end > orders.size()) end = orders.size();

        this.start = start;
        this.step = step;

        // new (!) LIST
        pp = new ArrayList<>(this.orders.subList(start, end));

    }

    public void setInterval(int start, int step) {
        getInterval(start, step);
    }

}
