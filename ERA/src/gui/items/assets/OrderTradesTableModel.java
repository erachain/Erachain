package gui.items.assets;

import controller.Controller;
import core.item.assets.Order;
import datachain.DCSet;
import datachain.SortableList;
import gui.models.TableModelCls;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class OrderTradesTableModel extends TableModelCls<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> implements Observer {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_AMOUNT = 2;
    public static final int COLUMN_PRICE = 3;
    public static final int COLUMN_AMOUNT_WANT = 4;

    private SortableList<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> trades;
    private Order order;

    private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Type", "Amount", "Price", "Total"});

    public OrderTradesTableModel(Order order) {
        this.order = order;
        this.trades = DCSet.getInstance().getTradeMap().getTrades(order.getId());
        this.trades.registerObserver();
    }

    @Override
    public SortableList<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> getSortableList() {
        return this.trades;
    }

    public Tuple5<Long, Long, BigDecimal, BigDecimal, Long> getTrade(int row) {
        return this.trades.get(row).getB();
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
        return this.trades.size();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.trades == null || row > this.trades.size() - 1) {
            return null;
        }

        Tuple5<Long, Long, BigDecimal, BigDecimal, Long> trade = this.trades.get(row).getB();
        int type = 0;
        Order initatorOrder = null;
        Order targetOrder = null;

        if (trade != null) {
            DCSet db = DCSet.getInstance();

            initatorOrder = Order.getOrder(db, trade.a);
            targetOrder = Order.getOrder(db, trade.b);
        }

        switch (column) {
            case COLUMN_TIMESTAMP:

                return DateTimeFormat.timestamptoString(trade.e);

            case COLUMN_TYPE:

                return initatorOrder.getHave() == this.order.getHave() ? Lang.getInstance().translate("Buy") : Lang.getInstance().translate("Sell");

            case COLUMN_AMOUNT:

                
                String result = NumberAsString.formatAsString(trade.c); //getAmountHave());

                if (Controller.getInstance().isAddressIsMine(initatorOrder.getCreator().getAddress())) {
                    result = "<html><b>" + result + "</b></html>";
                }

                return result;

            case COLUMN_PRICE:

                return NumberAsString.formatAsString(Order.calcPrice(trade.c, trade.d));

            case COLUMN_AMOUNT_WANT:

                result = NumberAsString.formatAsString(trade.d); //getAmountWant());

                if (Controller.getInstance().isAddressIsMine(targetOrder.getCreator().getAddress())) {
                    result = "<html><b>" + result + "</b></html>";
                }

                return result;

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

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_TRADE_TYPE || message.getType() == ObserverMessage.REMOVE_TRADE_TYPE) {
            this.fireTableDataChanged();
        }
    }

    public void removeObservers() {
        this.trades.removeObserver();
        Controller.getInstance().deleteObserver(this);
    }

    @Override
    public Object getItem(int k) {
        // TODO Auto-generated method stub
        return this.trades.get(k).getB();
    }
}
