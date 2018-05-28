package gui.items.assets;

import controller.Controller;
import core.item.assets.AssetCls;
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
import utils.Pair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class TradesTableModel extends TableModelCls<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> implements Observer {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_ASSET_1 = 2;
    public static final int COLUMN_PRICE = 3;
    public static final int COLUMN_ASSET_2 = 4;
    BigDecimal sumAsset1;
    BigDecimal sumAsset2;
    private SortableList<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades;
    private AssetCls have;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Type", "Check 1", "Price", "Check 2"});

    public TradesTableModel(AssetCls have, AssetCls want) {
        Controller.getInstance().addObserver(this);

        this.have = have;
        this.trades = Controller.getInstance().getTrades(have, want);
        this.trades.registerObserver();

        this.columnNames[2] = have.getShort();

        this.columnNames[4] = want.getShort();

        this.columnNames[3] = "Price: " + this.columnNames[4];

        totalCalc();
    }

    private void totalCalc() {
        sumAsset1 = BigDecimal.ZERO;
        sumAsset2 = BigDecimal.ZERO;

        for (Pair<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> tradePair : this.trades) {

            Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> trade = tradePair.getB();
            String type = Order.getOrder(DCSet.getInstance(), trade.a).b.a == this.have.getKey() ? "Sell" : "Buy";

            if (type.equals("Buy")) {
                sumAsset1 = sumAsset1.add(trade.c);// getAmountHave());
                sumAsset2 = sumAsset2.add(trade.d); //getAmountWant());
            } else {
                sumAsset1 = sumAsset1.add(trade.d); //getAmountWant());
                sumAsset2 = sumAsset2.add(trade.c); //getAmountHave());
            }

        }
    }

    @Override
    public SortableList<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getSortableList() {
        return this.trades;
    }

    public Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> getTrade(int row) {
        Pair<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> rec = this.trades.get(row);
        if (rec == null)
            return null;

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
        return this.trades.size() + 1;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.trades == null || row > this.trades.size()) {
            return null;
        }

        Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> trade = null;
        int type = 0;
        Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> initatorOrder = null;
        Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> targetOrder = null;

        if (row < this.trades.size()) {
            trade = this.trades.get(row).getB();
            if (trade != null) {
                DCSet db = DCSet.getInstance();

                initatorOrder = Order.getOrder(db, trade.a);
                targetOrder = Order.getOrder(db, trade.b);

                type = initatorOrder.b.a
                        == this.have.getKey() ?
                        -1 : 1;

            }
        }

        switch (column) {
            case COLUMN_TIMESTAMP:

                if (row == this.trades.size())
                    return "<html>" + Lang.getInstance().translate("Total") + ":</html>";

                return DateTimeFormat.timestamptoString(trade.e);

            case COLUMN_TYPE:

                return type == 0 ? "" : type > 0 ? "Sell" : "Buy";

            case COLUMN_ASSET_1:

                if (row == this.trades.size())
                    return "<html><i>" + NumberAsString.formatAsString(sumAsset1) + "</i></html>";

                String result = "";
                if (type > 0)
                    result = NumberAsString.formatAsString(trade.c);
                else
                    result = NumberAsString.formatAsString(trade.d);

                if (Controller.getInstance().isAddressIsMine(initatorOrder.a.b)) {
                    result = "<html><b>" + result + "</b></html>";
                }

                return result;


            case COLUMN_PRICE:

                if (row == this.trades.size())
                    return null;

                if (type > 0)
                    return NumberAsString.formatAsString(Order.calcPrice(trade.c, trade.d));
                else
                    return NumberAsString.formatAsString(Order.calcPrice(trade.d, trade.c));

            case COLUMN_ASSET_2:

                if (row == this.trades.size())
                    return "<html><i>" + NumberAsString.formatAsString(sumAsset2) + "</i></html>";

                if (type > 0)
                    result = NumberAsString.formatAsString(trade.d);
                else
                    result = NumberAsString.formatAsString(trade.c);

                if (Controller.getInstance().isAddressIsMine(targetOrder.a.b)) {
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
            totalCalc();
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
        Pair<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> rec = this.trades.get(k);
        if (rec == null)
            return null;

        return this.trades.get(k).getB();
    }
}
