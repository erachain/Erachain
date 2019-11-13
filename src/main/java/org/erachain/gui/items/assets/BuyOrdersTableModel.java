package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.CompletedOrderMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.OrderMap;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class BuyOrdersTableModel extends TimerTableModelCls<Order> implements Observer {
    //public static final int COLUMN_BUYING_PRICE = -1;
    public static final int COLUMN_AMOUNT_WANT = 0;
    public static final int COLUMN_PRICE = 1;
    public static final int COLUMN_AMOUNT_HAVE = 2;

    private AssetCls have;
    private AssetCls want;
    private long haveKey;
    private long wantKey;

    private CompletedOrderMap completedMap = DCSet.getInstance().getCompletedOrderMap();

    public BuyOrdersTableModel(AssetCls have, AssetCls want) {
        super(DCSet.getInstance().getOrderMap(), new String[]{"Creator", "Price", "Amount"}, true);

        this.have = have;
        this.want = want;

        this.haveKey = this.have.getKey();
        this.wantKey = this.want.getKey();

        repaint();

        addObservers();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size()) {
            return null;
        }

        Order order;
        boolean isMine = false;
        int size = this.list.size();
        if (row < size) {
            order = this.list.get(row);
            if (order == null) {
                //totalCalc();
                //this.fireTableRowsDeleted(row, row);
                return null;
            }

            Controller cntr = Controller.getInstance();
            if (cntr.isAddressIsMine(order.getCreator().getAddress())) {
                isMine = true;
            }

        } else  {
            needUpdate = true;
            return null;
        }

        String amountStr;
        switch (column) {

            case COLUMN_AMOUNT_WANT:

                String personStr;
                Fun.Tuple2<Integer, PersonCls> person = order.getCreator().getPerson();
                if (person == null || person.b == null) {
                    personStr = order.getCreator().getAddress();
                    if (isMine)
                        personStr = "<html><b>" + personStr + "</b></html>";
                } else {
                    personStr = "<b>[" + person.b.getKey() + "]</b> "
                            + (isMine? "<b>" + person.b.getName() + "</b>" : person.b.getName());

                    personStr = "<html>" + personStr + "</html>";
                }

                return personStr;

            case COLUMN_PRICE:

                if (row == this.list.size())
                    return "<html><b><span style='color:green'>" + Lang.getInstance().translate("Total") + "</span></b></html>";

                //BigDecimal price = Order.calcPrice(order.getAmountWant(), order.getAmountHave());
                // TODO: в новой версии нужно сделать везде +Scale = 0 - иначе несостыкоавка в процессинге ордера - там то 0
                BigDecimal price = order.calcPriceReverse();
                amountStr = NumberAsString.formatAsString(price.stripTrailingZeros());

                if (isMine)
                    amountStr = "<b>" + amountStr + "</b>";

                return "<html><span style='color:green'>▲</span>" + amountStr + "</html>";


            case COLUMN_AMOUNT_HAVE:

                amountStr = NumberAsString.formatAsString(order.getAmountWantLeft(), want.getScale());

                if (isMine)
                    amountStr = "<html><b>" + amountStr + "</b></html>";

                return amountStr;

        }

        return null;
    }

    public synchronized void repaint() {

        this.needUpdate = false;

        this.list = ((OrderMap)map).getOrders(haveKey, wantKey, 300);

        this.fireTableDataChanged();

    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        int type = message.getType();

        // CHECK IF LIST UPDATED
        if (type == ObserverMessage.ADD_ORDER_TYPE
                || type == ObserverMessage.REMOVE_ORDER_TYPE
        ) {
            Order order;
            Object object = message.getValue();
            if (object instanceof Order) {
                order = (Order) object;
            } else if (object instanceof Long) {
                order = completedMap.get((Long) object);
            } else {
                return;
            }
            if (order == null) {
                // это возможно если ордера нету
                return;
            }

            long haveKey = order.getHaveAssetKey();
            long wantKey = order.getWantAssetKey();
            if (!(haveKey == this.haveKey && wantKey == this.wantKey)
                    && !(haveKey == this.wantKey && wantKey == this.haveKey)) {
                return;
            }

            this.needUpdate = true;
            return;

        } else if (this.needUpdate == true) {
            if (type == ObserverMessage.CHAIN_ADD_BLOCK_TYPE
                    || type == ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE) {
                if (Controller.getInstance().isStatusOK()) {
                    this.repaint();
                    return;
                } else if (type == ObserverMessage.BLOCKCHAIN_SYNC_STATUS
                        || type == ObserverMessage.NETWORK_STATUS) {
                    if (Controller.getInstance().isStatusOK()) {
                        this.repaint();
                        return;
                    }
                }
            } else if (type == ObserverMessage.GUI_REPAINT) {
                this.repaint();
                return;
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
