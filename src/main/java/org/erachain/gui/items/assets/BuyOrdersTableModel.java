package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.database.SortableList;
import org.erachain.gui.models.SortedListTableModelCls;
import org.erachain.lang.Lang;
import org.erachain.ntp.NTP;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class BuyOrdersTableModel extends SortedListTableModelCls<Long, Order> implements Observer {
    //public static final int COLUMN_BUYING_PRICE = -1;
    public static final int COLUMN_AMOUNT_WANT = 0;
    public static final int COLUMN_PRICE = 1;
    public static final int COLUMN_AMOUNT_HAVE = 2;

    private boolean needRepaint = false;
    private long updateTime = 0l;

    public SortableList<Long, Order> orders;
    BigDecimal sumAmountWant;
    BigDecimal sumAmountHave;
    // private String[] columnNames = Lang.getInstance().translate(new
    // String[]{"Buying Price", "Buying Amount", "Price", "Amount"});
    private AssetCls have;
    private AssetCls want;
    private long haveKey;
    private long wantKey;

    public BuyOrdersTableModel(AssetCls have, AssetCls want) {
        super(new String[]{"Who", "Price", "Have"}, true);

        this.have = have;
        this.want = want;

        this.haveKey = this.have.getKey();
        this.wantKey = this.want.getKey();

        this.orders = Controller.getInstance().getOrders(have, want, false);

        // columnNames[COLUMN_BUYING_PRICE] += " " + have.getShort();
        ///columnNames[COLUMN_PRICE] += " " + have.getShort();
        ///columnNames[COLUMN_AMOUNT_WANT] += " " + want.getShort();
        ///columnNames[COLUMN_AMOUNT_HAVE] += " " + have.getShort();

        totalCalc();

        addObservers();

    }

    private void totalCalc() {
        sumAmountWant = BigDecimal.ZERO;
        sumAmountHave = BigDecimal.ZERO;
        for (Pair<Long, Order> orderPair : this.orders) {

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
        Pair<Long, Order> rec = this.orders.get(row);
        if (rec == null)
            return null;

        return this.orders.get(row).getB();
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

        Order order = null;
        boolean isMine = false;
        int size = this.orders.size();
        if (row < size) {
            order = this.orders.get(row).getB();
            if (order == null) {
                //totalCalc();
                //this.fireTableRowsDeleted(row, row);
                return null;
            }

            Controller cntr = Controller.getInstance();
            if (cntr.isAddressIsMine(order.getCreator().getAddress())) {
                isMine = true;
            }

        } else if (size > row) {
            this.orders = Controller.getInstance().getOrders(have, want, false);
            totalCalc();
            this.fireTableDataChanged();
            return null;
        }

        switch (column) {

            case COLUMN_AMOUNT_WANT:

                if (row == this.orders.size())
                    return "<html><i>" + NumberAsString.formatAsString(sumAmountWant, want.getScale()) + "</i></html>";

                String amountStr;
                    amountStr = order.getCreator().getPersonAsString();

            case COLUMN_PRICE:

                if (row == this.orders.size())
                    return "<html><b>" + Lang.getInstance().translate("Total") + "</b></html>";

                //BigDecimal price = Order.calcPrice(order.getAmountWant(), order.getAmountHave());
                BigDecimal price = Order.calcPrice(order.getAmountWant(), order.getAmountHave(), 2);
                amountStr = NumberAsString.formatAsString(price.stripTrailingZeros());

                if (isMine)
                    amountStr = "<html><b>" + amountStr + "</b></html>";

                return amountStr;


            case COLUMN_AMOUNT_HAVE:

                if (row == this.orders.size())
                    return "<html><i>" + NumberAsString.formatAsString(sumAmountHave, have.getScale()) + "</i></html>";

                amountStr = NumberAsString.formatAsString(order.getAmountHaveLeft(), have.getScale());

                if (isMine)
                    amountStr = "<html><b>" + amountStr + "</b></html>";

                return amountStr;

        }

        return null;
    }

    public synchronized void repaint() {
        this.needRepaint = false;
        this.updateTime = NTP.getTime();

        this.orders = Controller.getInstance().getOrders(this.have, this.want, false);

        totalCalc();
        this.fireTableDataChanged();

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

        int type = message.getType();

        // CHECK IF LIST UPDATED
        if (type == ObserverMessage.ADD_ORDER_TYPE
                || type == ObserverMessage.REMOVE_ORDER_TYPE
        ) {

            Order order = (Order) message.getValue();
            long haveKey = order.getHave();
            long wantKey = order.getWant();
            if (!(haveKey == this.haveKey && wantKey == this.wantKey)
                    && !(haveKey == this.wantKey && wantKey == this.haveKey)) {
                return;
            }

            this.needRepaint = true;
            return;

        } else if (this.needRepaint == true) {
            if (type == ObserverMessage.CHAIN_ADD_BLOCK_TYPE
                    || type == ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE) {
                if (Controller.getInstance().isStatusOK()) {
                    this.repaint();
                    return;
                } else {
                    if (NTP.getTime() - updateTime > 100000) {
                        this.repaint();
                        return;

                    }
                }
            } else if (type == ObserverMessage.BLOCKCHAIN_SYNC_STATUS
                    || type == ObserverMessage.NETWORK_STATUS) {
                if (Controller.getInstance().isStatusOK()) {
                    this.repaint();
                    return;
                }
            }
        }
    }

    public void addObservers() {
        Controller.getInstance().addObserver(this);
        //this.orders.registerObserver();

    }

    public void deleteObservers() {
        //this.orders.removeObserver();
        Controller.getInstance().deleteObserver(this);
    }

    @Override
    public Order getItem(int k) {
        // TODO Auto-generated method stub
        return this.orders.get(k).getB();
    }
}
