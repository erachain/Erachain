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
    public static final int COLUMN_ASSET_1 = 1;
    public static final int COLUMN_PRICE = 2;
    public static final int COLUMN_ASSET_2 = 3;

    private AssetCls have;
    private AssetCls want;
    private long haveKey;
    private long wantKey;

    public TradesTableModel(AssetCls have, AssetCls want) {

        super(DCSet.getInstance().getTradeMap(), new String[]{"Timestamp", "Amount", "Price", "Total"}, true);

        this.have = have;
        this.want = want;

        this.haveKey = this.have.getKey();
        this.wantKey = this.want.getKey();

        this.listSorted = Controller.getInstance().getTrades(have, want);
        //this.trades.registerObserver();

        //this.columnNames[2] = have.getShort();
        //this.columnNames[4] = want.getShort();
        //this.columnNames[3] = Lang.getInstance().translate("Price") + ": " + this.columnNames[4];

        ///totalCalc();
    }


    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row > this.listSorted.size()) {
            return null;
        }

        Trade trade = null;
        int type = 0;
        Order initatorOrder = null;
        Order targetOrder = null;

        if (row < this.listSorted.size()) {
            trade = this.listSorted.get(row).getB();
            if (trade != null) {
                DCSet db = DCSet.getInstance();

                initatorOrder = Order.getOrder(db, trade.getInitiator());
                targetOrder = Order.getOrder(db, trade.getTarget());

                type = initatorOrder.getHave() == this.have.getKey() ? -1 : 1;

            }
        }

        switch (column) {
            case COLUMN_TIMESTAMP:

                if (row == this.listSorted.size())
                    return "<html>" + Lang.getInstance().translate("Total") + ":</html>";

                return DateTimeFormat.timestamptoString(trade.getTimestamp());

            case COLUMN_ASSET_1:

                if (row == this.listSorted.size())
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

                if (row == this.listSorted.size())
                    return "";
                    ///return null;


            if (type > 0)
                    return "<html><span style='color:green'>▲</span>"
                        + NumberAsString.formatAsString(trade.calcPrice(have, want))
                        + "</html>";
                else
                    return "<html><span style='color:red'>▼</span>"
                        + NumberAsString.formatAsString(trade.calcPriceRevers(have, want))
                        + "</html>";

            case COLUMN_ASSET_2:

                if (row == this.listSorted.size())
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
    public void getIntervalThis(long start, long end) {

        this.listSorted = Controller.getInstance().getTrades(this.have, this.want);

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

            this.needUpdate = true;
            return;

        } else if (needUpdate) {
            if (type == ObserverMessage.CHAIN_ADD_BLOCK_TYPE
                    || type == ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE) {
                if (Controller.getInstance().isStatusOK()) {
                    this.getInterval();
                    this.needUpdate = true;
                    return;
                } else if (type == ObserverMessage.BLOCKCHAIN_SYNC_STATUS
                        || type == ObserverMessage.NETWORK_STATUS) {
                    if (Controller.getInstance().isStatusOK()) {
                        this.getInterval();
                        this.needUpdate = true;
                        return;
                    }
                }
            } else if (type == ObserverMessage.GUI_REPAINT) {
                this.getInterval();
                this.needUpdate = true;
                return;
            }
        }
    }
}
