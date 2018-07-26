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
public class BuyOrdersTableModel extends
        TableModelCls<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>
        implements Observer {
    //public static final int COLUMN_BUYING_PRICE = -1;
    public static final int COLUMN_AMOUNT_WANT = 0;
    public static final int COLUMN_PRICE = 1;
    public static final int COLUMN_AMOUNT_HAVE = 2;

    public SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders;
    BigDecimal sumAmountWant;
    BigDecimal sumAmountHave;
    // private String[] columnNames = Lang.getInstance().translate(new
    // String[]{"Buying Price", "Buying Amount", "Price", "Amount"});
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Want", "Price", "Have"});
    private AssetCls have;
    private AssetCls want;

    public BuyOrdersTableModel(AssetCls have, AssetCls want) {
        this.have = have;
        this.want = want;

        this.orders = Controller.getInstance().getOrders(have, want);

        // columnNames[COLUMN_BUYING_PRICE] += " " + have.getShort();
        columnNames[COLUMN_PRICE] += " " + have.getShort();
        columnNames[COLUMN_AMOUNT_WANT] += " " + want.getShort();
        columnNames[COLUMN_AMOUNT_HAVE] += " " + have.getShort();

        totalCalc();

        Controller.getInstance().addObserver(this);
        // this.orders.registerObserver();

    }

    private void totalCalc() {
        sumAmountWant = BigDecimal.ZERO;
        sumAmountHave = BigDecimal.ZERO;
        for (Pair<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orderPair : this.orders) {

            Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = orderPair.getB();
            if (order == null)
                return;
            Tuple3<Long, BigDecimal, BigDecimal> haveItem = order.b;
            sumAmountHave = sumAmountHave.add(haveItem.b.subtract(haveItem.c));

            sumAmountWant = sumAmountWant.add(Order.calcAmountWantLeft(orderPair.getB()));
        }
    }

    @Override
    public SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getSortableList() {
        return this.orders;
    }

    public Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> getOrder(
            int row) {
        Pair<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> rec = this.orders
                .get(row);
        if (rec == null)
            return null;

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

        Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = null;
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
            this.orders = Controller.getInstance().getOrders(have, want);
            totalCalc();
            this.fireTableDataChanged();
            return null;
        }

        switch (column) {

            /*
             * case COLUMN_BUYING_PRICE:
             *
             * if(row == this.orders.size()) return "<html>Total:</html>";
             *
             *
             * return
             * NumberAsString.getInstance().numberAsString12(Order.calcPrice(order.b
             * .b, order.c.b));
             */

            case COLUMN_AMOUNT_WANT:

                if (row == this.orders.size())
                    return "<html><i>" + NumberAsString.formatAsString(sumAmountWant, want.getScale()) + "</i></html>";

                // It shows unacceptably small amount of red.
                BigDecimal amount = Order.calcAmountWantLeft(order);

                String amountStr = NumberAsString.formatAsString(amount, want.getScale());
                if (order.a.d)
                    return amountStr;
                else
                    return "<html><font color=#808080>" + amountStr + "</font></html>";

            case COLUMN_PRICE:

                if (row == this.orders.size())
                    return "<html><b>" + Lang.getInstance().translate("Total") + "</b></html>";

                BigDecimal price = Order.calcPrice(order.c.b, order.b.b);
                return NumberAsString.formatAsString(price.stripTrailingZeros());

            case COLUMN_AMOUNT_HAVE:

                if (row == this.orders.size())
                    return "<html><i>" + NumberAsString.formatAsString(sumAmountHave, have.getScale()) + "</i></html>";

                amountStr = NumberAsString.formatAsString(order.b.b.subtract(order.b.c), have.getScale());

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
                || message.getType() == ObserverMessage.WALLET_ADD_ORDER_TYPE
                || message.getType() == ObserverMessage.WALLET_REMOVE_ORDER_TYPE) {
            this.orders = Controller.getInstance().getOrders(have, want);
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
