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
    public static final int COLUMN_AMOUNT_WHO = 1;
    public static final int COLUMN_PRICE = 2;
    public static final int COLUMN_WHO_AMOUNT = 3;

    private Order order;
    private boolean isSell;

    /**
     * Ордера которые погрызли данный
     * @param order
     * @param isSell
     */
    public OrderTradesTableModel(Order order, boolean isSell) {
        super(new String[]{"Timestamp", isSell?"Amount":"Creator", "Price", !isSell?"Amount":"Creator"}, true);

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
        DCSet db = DCSet.getInstance();

        Order initiatorOrder = Order.getOrder(db, trade.getInitiator());
        Order targetOrder = Order.getOrder(db, trade.getTarget());

        switch (column) {
            case COLUMN_TIMESTAMP:

                return DateTimeFormat.timestamptoString(trade.getTimestamp());

            case COLUMN_AMOUNT_WHO:

                String result;

                if (isSell)
                    result = NumberAsString.formatAsString(trade.getAmountHave());
                else
                    result = initiatorOrder.getCreator().getPersonAsString();

                if (Controller.getInstance().isAddressIsMine(initiatorOrder.getCreator().getAddress())
                        || Controller.getInstance().isAddressIsMine(targetOrder.getCreator().getAddress())) {
                    result = "<html><b>" + result + "</b></html>";
                }

                return result;

            case COLUMN_PRICE:

                boolean isMine = Controller.getInstance().isAddressIsMine(initiatorOrder.getCreator().getAddress())
                        || Controller.getInstance().isAddressIsMine(targetOrder.getCreator().getAddress());

                if (isSell)
                    return "<html><span style='color:green'>▲</span>"
                            + (isMine? "<b>" + NumberAsString.formatAsString(trade.calcPrice()) + "</b>"
                                : NumberAsString.formatAsString(trade.calcPrice()))
                            + "</html>";
                else
                    return "<html><span style='color:red'>▼</span>"
                            + (isMine? "<b>" + NumberAsString.formatAsString(trade.calcPriceRevers()) + "</b>"
                                : NumberAsString.formatAsString(trade.calcPriceRevers()))
                            + "</html>";

            case COLUMN_WHO_AMOUNT:

                if (isSell)
                    result = initiatorOrder.getCreator().getPersonAsString();
                    //result = NumberAsString.formatAsString(trade.getAmountWant());
                else
                    result = NumberAsString.formatAsString(trade.getAmountHave());

                if (Controller.getInstance().isAddressIsMine(initiatorOrder.getCreator().getAddress())
                        || Controller.getInstance().isAddressIsMine(targetOrder.getCreator().getAddress())) {
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
