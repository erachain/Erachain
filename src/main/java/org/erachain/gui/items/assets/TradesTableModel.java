package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.SortedListTableModelCls;
import org.erachain.lang.Lang;
import org.erachain.ntp.NTP;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class TradesTableModel extends SortedListTableModelCls<Tuple2<Long, Long>, Trade> implements Observer {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_ASSET_1 = 2;
    public static final int COLUMN_PRICE = 3;
    public static final int COLUMN_ASSET_2 = 4;

    private boolean needRepaint = false;
    private long updateTime = 0l;

    BigDecimal sumAsset1;
    BigDecimal sumAsset2;

    private SortableList<Tuple2<Long, Long>, Trade> trades;

    private AssetCls have;
    private AssetCls want;
    private long haveKey;
    private long wantKey;

    public TradesTableModel(AssetCls have, AssetCls want) {

        super(new String[]{"Timestamp", "Type", "Check 1", "Price", "Check 2"}, true);

        this.have = have;
        this.want = want;

        this.haveKey = this.have.getKey();
        this.wantKey = this.want.getKey();

        this.trades = Controller.getInstance().getTrades(have, want);
        //this.trades.registerObserver();

        //this.columnNames[2] = have.getShort();
        //this.columnNames[4] = want.getShort();
        //this.columnNames[3] = Lang.getInstance().translate("Price") + ": " + this.columnNames[4];

        ///totalCalc();
    }

    private void totalCalc() {
        sumAsset1 = BigDecimal.ZERO;
        sumAsset2 = BigDecimal.ZERO;

        for (Pair<Tuple2<Long, Long>, Trade> tradePair : this.trades) {

            Trade trade = tradePair.getB();
            String type = Order.getOrder(DCSet.getInstance(), trade.getInitiator()).getHave() == this.have.getKey() ? "Sell" : "Buy";

            if (type.equals("Buy")) {
                sumAsset1 = sumAsset1.add(trade.getAmountHave());
                sumAsset2 = sumAsset2.add(trade.getAmountWant());
            } else {
                sumAsset1 = sumAsset1.add(trade.getAmountWant());
                sumAsset2 = sumAsset2.add(trade.getAmountHave());
            }

        }
    }

    @Override
    public SortableList<Tuple2<Long, Long>, Trade> getSortableList() {
        return this.trades;
    }

    public Trade getTrade(int row) {
        Pair<Tuple2<Long, Long>, Trade> rec = this.trades.get(row);
        if (rec == null)
            return null;

        return this.trades.get(row).getB();
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

        Trade trade = null;
        int type = 0;
        Order initatorOrder = null;
        Order targetOrder = null;

        if (row < this.trades.size()) {
            trade = this.trades.get(row).getB();
            if (trade != null) {
                DCSet db = DCSet.getInstance();

                initatorOrder = Order.getOrder(db, trade.getInitiator());
                targetOrder = Order.getOrder(db, trade.getTarget());

                type = initatorOrder.getHave() == this.have.getKey() ? -1 : 1;

            }
        }

        switch (column) {
            case COLUMN_TIMESTAMP:

                if (row == this.trades.size())
                    return "<html>" + Lang.getInstance().translate("Total") + ":</html>";

                return DateTimeFormat.timestamptoString(trade.getTimestamp());

            case COLUMN_TYPE:

                return type == 0 ? "" : type > 0 ? Lang.getInstance().translate("Sell") :
                        Lang.getInstance().translate("Buy");

            case COLUMN_ASSET_1:

                if (row == this.trades.size())
                    return "";
                //    return "<html><i>" + NumberAsString.formatAsString(sumAsset1) + "</i></html>";

                String result = "";
                if (type > 0)
                    result = NumberAsString.formatAsString(trade.getAmountHave());
                else
                    result = NumberAsString.formatAsString(trade.getAmountWant());

                if (Controller.getInstance().isAddressIsMine(initatorOrder.getCreator().getAddress())) {
                    result = "<html><b>" + result + "</b></html>";
                }

                return result;


            case COLUMN_PRICE:

                if (row == this.trades.size())
                    return "";
                    ///return null;

                if (type > 0)
                    return NumberAsString.formatAsString(trade.calcPrice());
                else
                    return NumberAsString.formatAsString(trade.calcPriceRevers());

            case COLUMN_ASSET_2:

                if (row == this.trades.size())
                    return "";
                    ///return "<html><i>" + NumberAsString.formatAsString(sumAsset2) + "</i></html>";

                if (type > 0)
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

    public synchronized void repaint() {
        this.needRepaint = false;
        this.updateTime = NTP.getTime();

        this.trades = Controller.getInstance().getTrades(this.have, this.want);

        /// so FARD to CALCULATE  --totalCalc();
        this.fireTableDataChanged();
        this.needRepaint = false;

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

        int type = message.getType();

        // CHECK IF LIST UPDATED
        if (type == ObserverMessage.ADD_TRADE_TYPE
                || type == ObserverMessage.REMOVE_TRADE_TYPE
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
    }

    public void deleteObservers() {
        //this.trades.removeObserver();
        Controller.getInstance().deleteObserver(this);
    }

    @Override
    public Trade getItem(int k) {
        // TODO Auto-generated method stub
        Pair<Tuple2<Long, Long>, Trade> rec = this.trades.get(k);
        if (rec == null)
            return null;

        return this.trades.get(k).getB();
    }
}
