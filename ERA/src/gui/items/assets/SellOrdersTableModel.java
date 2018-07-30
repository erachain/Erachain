package gui.items.assets;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import datachain.SortableList;
import gui.models.TableModelCls;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class SellOrdersTableModel extends
        TableModelCls<Long, Order>
        implements Observer {
    public static final int COLUMN_AMOUNT_HAVE = 0;
    public static final int COLUMN_PRICE = 1;
    public static final int COLUMN_AMOUNT_WANT = 2;

    public SortableList<Long, Order> orders;
    BigDecimal sumAmountHave;
    BigDecimal sumAmountWant;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Have", "Price", "Want"});
    private AssetCls have;
    private AssetCls want;

    public SellOrdersTableModel(AssetCls have, AssetCls want) {
        this.have = have;
        this.want = want;
        this.orders = Controller.getInstance().getOrders(have, want, true);

        columnNames[COLUMN_PRICE] += " " + want.getShort();
        columnNames[COLUMN_AMOUNT_HAVE] += " " + have.getShort();
        columnNames[COLUMN_AMOUNT_WANT] += " " + want.getShort();

        totalCalc();

        Controller.getInstance().addObserver(this);
        //this.orders.registerObserver();

    }

    private void totalCalc() {
        sumAmountHave = BigDecimal.ZERO;
        sumAmountWant = BigDecimal.ZERO;
        for (Pair<Long, Order> orderPair : this.orders) {

            //Tuple3<Long, BigDecimal, BigDecimal> haveItem = orderPair.getB().b;
            //sumAmountHave = sumAmountHave.add(haveItem.b.subtract(haveItem.c));
            Order order = orderPair.getB();
            sumAmountHave = sumAmountHave.add(order.getAmountHaveLeft());

            sumAmountWant = sumAmountWant.add(order.getAmountWantLeft());
        }
    }

    @Override
    public SortableList<Long, Order> getSortableList() {
        return this.orders;
    }

    public Order getOrder(int row) {
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
        return this.orders.size() + 1;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.orders == null || row > this.orders.size()) {
            return null;
        }

        Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>,
                Tuple2<Long, BigDecimal>> order = null;
        boolean isMine = false;
        int size = this.orders.size();
        if (row < size) {
            order = this.orders.get(row).getB();

            if (order == null) {
                totalCalc();
                this.fireTableRowsDeleted(row, row);
                return null;
            }

            Controller cntr = Controller.getInstance();
            if (cntr.isAddressIsMine(order.a.b)) {
                isMine = true;
            }

        } else if (size > row) {
            this.orders = Controller.getInstance().getOrders(have, want, true);
            totalCalc();
            this.fireTableDataChanged();
            return null;
        }

        switch (column) {
            case COLUMN_AMOUNT_HAVE:

                if (row == this.orders.size())
                    return "<html><i>" + NumberAsString.formatAsString(sumAmountHave, have.getScale()) + "</i></html>";

                // It shows unacceptably small amount of red.
                BigDecimal amount = order.b.b.subtract(order.b.c);
                String amountStr = NumberAsString.formatAsString(amount, have.getScale());
                if (order.a.d)
                    return amountStr;
                else
                    return "<html><font color=#808080>" + amountStr + "</font></html>";

            case COLUMN_PRICE:

                if (row == this.orders.size())
                    return "<html><b>" + Lang.getInstance().translate("Total") + "</b></html>";

                BigDecimal price = Order.calcPrice(order.b.b, order.c.b);
                return NumberAsString.formatAsString(price.stripTrailingZeros());

            case COLUMN_AMOUNT_WANT:

                if (row == this.orders.size())
                    return "<html><i>" + NumberAsString.formatAsString(sumAmountWant, want.getScale()) + "</i></html>";

                amountStr = NumberAsString.formatAsString(Order.calcAmountWantLeft(order), want.getScale()); // getAmountWantLeft());

                if (isMine)
                    amountStr = "<html><b>" + amountStr + "</b></html>";

                return amountStr;

        }

        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            // GUI ERROR
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        // CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_ORDER_TYPE
                || message.getType() == ObserverMessage.REMOVE_ORDER_TYPE
            //|| message.getType() == ObserverMessage.WALLET_ADD_ORDER_TYPE
            //|| message.getType() == ObserverMessage.WALLET_REMOVE_ORDER_TYPE
                ) {
            this.orders = Controller.getInstance().getOrders(this.have, want, true);
            // List<Order> items =
            // DCSet.getInstance().getOrderMap().getOrders(have.getKey(),
            // want.getKey(), false);
            totalCalc();
            this.fireTableDataChanged();
        }
    }

    public void removeObservers() {
        this.orders.removeObserver();
        Controller.getInstance().deleteObserver(this);
    }

    @Override
    public Object getItem(int k) {
        // TODO Auto-generated method stub
        return this.orders.get(k).getB();
    }
}
