package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
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
public class SellOrdersTableModel extends SortedListTableModelCls<Long, Order> implements Observer {
    public static final int COLUMN_AMOUNT_HAVE = 0;
    public static final int COLUMN_PRICE = 1;
    public static final int COLUMN_AMOUNT_WANT = 2;

    ///public SortableList<Long, Order> orders;
    BigDecimal sumAmountHave;
    BigDecimal sumAmountWant;
    private AssetCls have;
    private AssetCls want;
    private long haveKey;
    private long wantKey;

    public SellOrdersTableModel(AssetCls have, AssetCls want) {
        super(DCSet.getInstance().getOrderMap(), new String[]{"Amount", "Price", "Who"}, true);

        this.have = have;
        this.want = want;

        this.haveKey = this.have.getKey();
        this.wantKey = this.want.getKey();

        this.listSorted = Controller.getInstance().getOrders(have, want, false);

        repaint();
        totalCalc();

        addObservers();

    }

    private void totalCalc() {
        sumAmountHave = BigDecimal.ZERO;
        sumAmountWant = BigDecimal.ZERO;
        for (Pair<Long, Order> orderPair : this.listSorted) {

            Order order = orderPair.getB();
            sumAmountHave = sumAmountHave.add(order.getAmountHaveLeft());
            sumAmountWant = sumAmountWant.add(order.getAmountWantLeft());
        }
    }


    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row > this.listSorted.size()) {
            return null;
        }

        Order order = null;
        boolean isMine = false;
        int size = this.listSorted.size();
        if (row < size) {
            order = this.listSorted.get(row).getB();

            if (order == null) {
                //totalCalc();
                //this.fireTableRowsDeleted(row, row);
                return null;
            }

            Controller cntr = Controller.getInstance();
            if (cntr.isAddressIsMine(order.getCreator().getAddress())) {
                isMine = true;
            }

        } else {
            repaint();
            return null;
        }

        String amountStr;
        switch (column) {
            case COLUMN_AMOUNT_HAVE:

                if (row == this.listSorted.size())
                    return "<html><i>" + NumberAsString.formatAsString(sumAmountHave, have.getScale()) + "</i></html>";

                // It shows unacceptably small amount of red.
                BigDecimal amount = order.getAmountHaveLeft();
                amountStr = NumberAsString.formatAsString(amount, have.getScale());

                if (isMine)
                    amountStr = "<html><b>" + amountStr + "</b></html>";

                return amountStr;

            case COLUMN_PRICE:

                if (row == this.listSorted.size())
                    return "<html><b>" + Lang.getInstance().translate("Total") + "</b></html>";

                BigDecimal price = Order.calcPrice(order.getAmountHave(), order.getAmountWant(), 2);
                amountStr = NumberAsString.formatAsString(price.stripTrailingZeros());

                if (isMine)
                    amountStr = "<html><b>" + amountStr + "</b></html>";

                return amountStr;

            case COLUMN_AMOUNT_WANT:

                if (row == this.listSorted.size())
                    return "<html><i>" + NumberAsString.formatAsString(sumAmountWant, want.getScale()) + "</i></html>";

                amountStr = order.getCreator().getPersonAsString();

                if (isMine)
                    amountStr = "<html><b>" + amountStr + "</b></html>";

                return amountStr;

        }

        return null;
    }

    public synchronized void repaint() {

        this.needUpdate = false;

        this.listSorted = Controller.getInstance().getOrders(this.have, this.want, false);

        totalCalc();
        this.fireTableDataChanged();

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

            this.needUpdate = true;
            return;

        } else if (this.needUpdate == true) {
            if (Controller.getInstance().isStatusOK()) {
                if (type == ObserverMessage.CHAIN_ADD_BLOCK_TYPE || type == ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE
                        || type == ObserverMessage.GUI_REPAINT) {
                    this.repaint();
                    return;
                }
            } else {
                if (type == ObserverMessage.GUI_REPAINT) {
                    this.repaint();
                    return;
                }
            }
        }
    }

    public void addObservers() {
        super.addObservers();
        DCSet.getInstance().getBlockMap().addObserver(this);
    }

    public void deleteObservers() {
        super.deleteObservers();
        DCSet.getInstance().getBlockMap().deleteObserver(this);
    }

}
