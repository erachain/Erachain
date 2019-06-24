package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.SortedListTableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.NumberAsString;
import org.mapdb.Fun.Tuple2;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class OrderTradesTableModel extends SortedListTableModelCls<Tuple2<Long, Long>, Trade> implements Observer {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_AMOUNT = 2;
    public static final int COLUMN_PRICE = 3;
    public static final int COLUMN_AMOUNT_WANT = 4;

    private Order order;
    private boolean isSell;

    public OrderTradesTableModel(Order order, boolean isSell) {
        super(new String[]{"Timestamp", "Type", "Amount", "Price", "Total"}, true);

        this.order = order;
        this.isSell = isSell;
        this.listSorted = DCSet.getInstance().getTradeMap().getTrades(order.getId());

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row > this.listSorted.size() - 1) {
            return null;
        }

        Trade trade = this.listSorted.get(row).getB();
        if (trade == null) {
            return null;
        }


        int type = 0;
        Order initatorOrder = null;
        Order targetOrder = null;

        DCSet db = DCSet.getInstance();

        initatorOrder = Order.getOrder(db, trade.getInitiator());
        targetOrder = Order.getOrder(db, trade.getTarget());

        switch (column) {
            case COLUMN_TIMESTAMP:

                return DateTimeFormat.timestamptoString(trade.getTimestamp());

            case COLUMN_TYPE:

                return isSell ? Lang.getInstance().translate("Buy") : Lang.getInstance().translate("Sell");

            case COLUMN_AMOUNT:

                String result;

                if (isSell)
                    result = NumberAsString.formatAsString(trade.getAmountHave());
                else
                    result = NumberAsString.formatAsString(trade.getAmountWant());

                if (Controller.getInstance().isAddressIsMine(initatorOrder.getCreator().getAddress())) {
                    result = "<html><b>" + result + "</b></html>";
                }

                return result;

            case COLUMN_PRICE:

                if (isSell)
                    return NumberAsString.formatAsString(trade.calcPrice(order.getHaveAsset(), order.getWantAsset()));
                else
                    return NumberAsString.formatAsString(trade.calcPriceRevers(order.getHaveAsset(), order.getWantAsset()));

            case COLUMN_AMOUNT_WANT:

                if (isSell)
                    result = NumberAsString.formatAsString(trade.getAmountWant());
                else
                    result = NumberAsString.formatAsString(trade.getAmountHave());

                if (Controller.getInstance().isAddressIsMine(targetOrder.getCreator().getAddress())) {
                    result = "<html><b>" + result + "</b></html>";
                }

                return result;

        }

        return null;
    }

    @Override
    public synchronized void syncUpdate(Observable o, Object arg) {
    }

    @Override
    public void addObservers() {
    }

    @Override
    public void deleteObservers() {
    }

}
