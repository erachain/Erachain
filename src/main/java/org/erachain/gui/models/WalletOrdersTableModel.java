package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.mapdb.Fun.Tuple2;

import java.util.Observer;

@SuppressWarnings("serial")
public class WalletOrdersTableModel extends WalletTableModel<Order> implements Observer {

    public static final int COLUMN_SEQNO = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_AMOUNT = 2;
    public static final int COLUMN_HAVE = 3;
    public static final int COLUMN_PRICE = 4;
    public static final int COLUMN_WANT = 5;
    public static final int COLUMN_AMOUNT_WANT = 6;
    public static final int COLUMN_LEFT = 7;
    public static final int COLUMN_CREATOR = 8;
    public static final int COLUMN_STATUS = 9;

    public WalletOrdersTableModel() {
        super(Controller.getInstance().wallet.database.getOrderMap(),
                new String[]{"№", "Timestamp", "Amount", "Have", "Price", "Want", "Total", "Left", "Creator", "Status"},
                new Boolean[]{false, true, true, true, true, true, true, true, true, true}, true, 10000);

        step = 200;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        Order order = this.list.get(row);

        switch (column) {

            case COLUMN_SEQNO:
                return Transaction.viewDBRef(order.getId());

            case COLUMN_TIMESTAMP:
                Tuple2<Integer, Integer> blockDBref = Transaction.parseDBRef(order.getId());
                return DateTimeFormat.timestamptoString(Controller.getInstance().getBlockChain().getTimestamp(blockDBref.a));

            case COLUMN_AMOUNT:
                return order.getAmountHave().toPlainString();

            case COLUMN_HAVE:
                AssetCls asset = DCSet.getInstance().getItemAssetMap().get(order.getHaveAssetKey());
                return asset == null ? "--" : asset.toString();

            case COLUMN_PRICE:
                return order.getPrice();

            case COLUMN_WANT:
                asset = DCSet.getInstance().getItemAssetMap().get(order.getWantAssetKey());
                return asset == null ? "--" : asset.toString();

            case COLUMN_AMOUNT_WANT:
                return order.getAmountWant().toPlainString();

            case COLUMN_LEFT:
                return order.getAmountWantLeft().toPlainString();

            case COLUMN_CREATOR:
                return order.getCreator().getPersonAsString();

            case COLUMN_STATUS:
                return Lang.T(order.viewStatus());

        }

        return null;
    }

}