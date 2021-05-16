package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TradeMap;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class TradesTableModel extends TimerTableModelCls<Trade> implements Observer {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_ASSET_1 = 1;
    public static final int COLUMN_PRICE = 2;
    public static final int COLUMN_ASSET_2 = 3;

    private AssetCls have;
    private AssetCls want;
    private long haveKey;
    private long wantKey;

    public TradesTableModel(AssetCls have, AssetCls want) {

        super(DCSet.getInstance().getTradeMap(), new String[]{"Timestamp", "Amount", "Price", "Total"}, false);

        this.have = have;
        this.want = want;

        columnNames[1] = columnNames[1] + " " + have.getShortName();
        columnNames[3] = columnNames[3] + " " + want.getShortName();

        this.haveKey = this.have.getKey();
        this.wantKey = this.want.getKey();

        getInterval();
        fireTableDataChanged();
        addObservers();

        ///this.listSorted = Controller.getInstance().getTrades(have, want);
        //this.trades.registerObserver();

        //this.columnNames[2] = have.getShort();
        //this.columnNames[4] = want.getShort();
        //this.columnNames[3] = Lang.T("Price") + ": " + this.columnNames[4];

        ///totalCalc();

    }


    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size()) {
            return null;
        }

        Trade trade = null;
        int type = 0;
        Order initatorOrder = null;
        Order targetOrder = null;

        if (row < this.list.size()) {
            trade = this.list.get(row);
            if (trade != null) {
                DCSet db = DCSet.getInstance();

                initatorOrder = Order.getOrder(db, trade.getInitiator());
                targetOrder = Order.getOrder(db, trade.getTarget());

                if (initatorOrder != null)
                    type = initatorOrder.getHaveAssetKey() == this.have.getKey() ? -1 : 1;

            }
        }

        switch (column) {
            case COLUMN_TIMESTAMP:

                if (row == this.list.size())
                    return "<html>" + Lang.T("Total") + ":</html>";

                return DateTimeFormat.timestamptoString(trade.getTimestamp());

            case COLUMN_ASSET_1:

                if (row == this.list.size())
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

                if (row == this.list.size())
                    return "";
                    ///return null;

                if (type > 0)
                    return "<html><span style='color:green'>▲</span>"
                        + NumberAsString.formatAsString(trade.calcPrice())
                        + "</html>";
                else
                    return "<html><span style='color:red'>▼</span>"
                        + NumberAsString.formatAsString(trade.calcPriceRevers())
                        + "</html>";

            case COLUMN_ASSET_2:

                if (row == this.list.size())
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

    @Override
    public void getInterval() {

        this.list = ((TradeMap) map).getTrades(haveKey, wantKey, startKey, step, false);

    }


    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        int type = message.getType();

        // CHECK IF LIST UPDATED
        if (type == ObserverMessage.ADD_TRADE_TYPE
                || type == ObserverMessage.REMOVE_TRADE_TYPE
        ) {

            Object object = message.getValue();
            if (object instanceof Trade) {
                Trade trade = (Trade) message.getValue();
                long haveKey = trade.getHaveKey();
                long wantKey = trade.getWantKey();
                if (!(haveKey == this.haveKey && wantKey == this.wantKey)
                        && !(haveKey == this.wantKey && wantKey == this.haveKey)) {
                    return;
                }

                this.needUpdate = true;
                return;

            } else if (object instanceof Fun.Tuple2) {
                // Сработал ордер или отменили значит он удалился но еще не добавлся в Исполненые
                // Поэтому просто ищем тутт по ID
                Fun.Tuple2<Long, Long> key = (Fun.Tuple2<Long, Long>) object;
                for (Trade trade : list) {
                    if (trade.getInitiator() == key.a
                            && trade.getTarget() == key.b) {
                        this.needUpdate = true;
                        return;
                    }
                }
                // not found
                return;
            } else {
                return;
            }

        } else if (type == ObserverMessage.LIST_TRADE_TYPE) {
            this.needUpdate = true;
            return;

        } else if (needUpdate) {
            if (type == ObserverMessage.CHAIN_ADD_BLOCK_TYPE
                    || type == ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE) {
                if (Controller.getInstance().isStatusOK()) {
                    this.needUpdate = false;
                    this.getInterval();
                    fireTableDataChanged();
                    return;
                } else if (type == ObserverMessage.BLOCKCHAIN_SYNC_STATUS
                        || type == ObserverMessage.NETWORK_STATUS) {
                    if (Controller.getInstance().isStatusOK()) {
                        this.needUpdate = false;
                        this.getInterval();
                        fireTableDataChanged();
                        return;
                    }
                }
            } else if (type == ObserverMessage.GUI_REPAINT) {
                this.needUpdate = false;
                this.getInterval();
                fireTableDataChanged();
                return;
            }
        }
    }
}
