package gui.models;

import controller.Controller;
import core.account.Account;
import core.item.assets.Order;
import datachain.DCSet;
import datachain.SortableList;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.DateTimeFormat;
import utils.ObserverMessage;

import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletOrdersTableModel extends TableModelCls<Tuple2<String, byte[]>, Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
        Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> implements Observer {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_AMOUNT = 1;
    public static final int COLUMN_HAVE = 2;
    public static final int COLUMN_WANT = 3;
    public static final int COLUMN_PRICE = 4;
    public static final int COLUMN_FULFILLED = 5;
    public static final int COLUMN_CREATOR = 6;
    public static final int COLUMN_CONFIRMED = 7;
    public static final int COLUMN_DONE = 8;

    private SortableList<Tuple2<String, byte[]>, Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders;

    private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Amount", "Have", "Want", "Price", "Fulfilled", "Creator", "Confirmed", "DONE"});

    public WalletOrdersTableModel() {
        Controller.getInstance().addWalletListener(this);
    }

    @Override
    public SortableList<Tuple2<String, byte[]>, Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getSortableList() {
        return this.orders;
    }

    @Override
    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    public Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> getOrder(int row) {
        return this.orders.get(row).getB();
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
        return (this.orders == null) ? 0 : this.orders.size();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.orders == null || row > this.orders.size() - 1) {
            return null;
        }

        Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = Order.reloadOrder(DCSet.getInstance(), this.orders.get(row).getB().a.a);
        //order.setDC(DCSet.getInstance());


        switch (column) {
            case COLUMN_TIMESTAMP:

                return DateTimeFormat.timestamptoString(order.a.c);

            case COLUMN_HAVE:

                return DCSet.getInstance().getItemAssetMap().get(order.b.a).getShort();

            case COLUMN_WANT:

                return DCSet.getInstance().getItemAssetMap().get(order.c.a).getShort();

            case COLUMN_AMOUNT:

                return order.b.b.toPlainString();

            case COLUMN_PRICE:

                return Order.calcPrice(order.b.b, order.c.b);

            case COLUMN_FULFILLED:

                return order.b.c.toPlainString();
            //return order.getFulfilledHave().toPlainString();

            case COLUMN_CREATOR:

                return new Account(order.a.b).getPersonAsString();

            case COLUMN_CONFIRMED:

                return DCSet.getInstance().getOrderMap().contains(order.a.a)
                        || DCSet.getInstance().getCompletedOrderMap().contains(order.a.a);

            case COLUMN_DONE:

                if (order.b.b.compareTo(order.b.c) == 0)
                    return "++";
                return "";

        }

        return null;
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
        if (//message.getType() == ObserverMessage.LIST_ORDER_TYPE ||
                message.getType() == ObserverMessage.WALLET_LIST_ORDER_TYPE) {
            if (this.orders == null) {
                this.orders = (SortableList<Tuple2<String, byte[]>, Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                        Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>) message.getValue();
                this.orders.registerObserver();
                //this.assets.sort(PollMap.NAME_INDEX);
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED
        if (//message.getType() == ObserverMessage.ADD_ORDER_TYPE || message.getType() == ObserverMessage.REMOVE_ORDER_TYPE ||
                message.getType() == ObserverMessage.WALLET_ADD_ORDER_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_ORDER_TYPE) {
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
}
