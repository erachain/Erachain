package org.erachain.gui.models;

import com.sun.org.apache.xpath.internal.operations.Or;
import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.OrderMap;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.util.*;

@SuppressWarnings("serial")
public class WalletOrdersTableModel extends SortedListTableModelCls<Tuple2<String, Long>, Order> implements Observer {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_BLOCK = 1;
    public static final int COLUMN_AMOUNT = 2;
    public static final int COLUMN_HAVE = 3;
    public static final int COLUMN_PRICE = 4;
    public static final int COLUMN_WANT = 5;
    public static final int COLUMN_AMOUNT_WANT = 6;
    public static final int COLUMN_LEFT = 7;
    public static final int COLUMN_CREATOR = 8;
    public static final int COLUMN_STATUS = 9;

    //List<Pair<Tuple2<String, Long>, Order>> pp = new ArrayList<Pair<Tuple2<String, Long>, Order>>();

    public WalletOrdersTableModel() {
        super(Controller.getInstance().wallet.database.getOrderMap(),
                new String[]{"Timestamp", "Block - transaction", "Amount", "Have", "Price",
                "Want", "Total", "Left", "Creator", "Status"}, true);

        addObservers();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row >= this.listSorted.size()) {
            return null;
        }
        Pair<Tuple2<String, Long>, Order> item = this.listSorted.get(row);
        if (item == null)
            return null;

        Order order = item.getB();
        Long blockDBrefLong = item.getA().b;
        Tuple2<Integer, Integer> blockDBref = Transaction.parseDBRef(blockDBrefLong);

        switch (column) {
            case COLUMN_TIMESTAMP:

                return DateTimeFormat.timestamptoString(Controller.getInstance().getBlockChain().getTimestamp(blockDBref.a));

            case COLUMN_AMOUNT:

                return order.getAmountHave().toPlainString();

            case COLUMN_HAVE:

                AssetCls asset = DCSet.getInstance().getItemAssetMap().get(order.getHave());
                return asset == null ? "[" + order.getHave() + "]" : asset.getShort();

            case COLUMN_PRICE:

                return order.getPrice();

            case COLUMN_WANT:

                asset = DCSet.getInstance().getItemAssetMap().get(order.getWant());
                return asset == null ? "[" + order.getWant() + "]" : asset.getShort();

            case COLUMN_AMOUNT_WANT:

                return order.getAmountWant().toPlainString();


            case COLUMN_LEFT:

                return order.getFulfilledWant().toPlainString();

            case COLUMN_CREATOR:

                return order.getCreator().getPersonAsString();

            case COLUMN_STATUS:

                if (order.getAmountHave().compareTo(order.getFulfilledHave()) == 0) {
                    return Lang.getInstance().translate("Done");
                } else {

                    if (DCSet.getInstance().getCompletedOrderMap().contains(order.getId()))
                        return Lang.getInstance().translate("Canceled");

                    if (DCSet.getInstance().getOrderMap().contains(order.getId())) {
                        if (order.getFulfilledHave().signum() == 0)
                            return Lang.getInstance().translate("Active");
                        else
                            return Lang.getInstance().translate("Fulfilled");
                    }

                    return "orphaned"; //"unconfirmed";
                }
                // TODO: return "orphaned !";

            case COLUMN_BLOCK:

                return blockDBref == null ? "?-?" : blockDBref.a + "-" + blockDBref.b;

        }

        return null;
    }

    private int count;

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_RESET_ORDER_TYPE) {
            this.listSorted.clear();
            this.fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.WALLET_LIST_ORDER_TYPE) {
            needUpdate = false;
            getInterval();
            this.fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.WALLET_ADD_ORDER_TYPE) {
            needUpdate = true;
        } else if (message.getType() == ObserverMessage.WALLET_REMOVE_ORDER_TYPE) {
            needUpdate = true;

        } else if (message.getType() == ObserverMessage.GUI_REPAINT
                && Controller.getInstance().isDynamicGUI()
                && needUpdate) {

            if (count++ < 4)
                return;

            count = 0;
            needUpdate = false;

            getInterval();
            fireTableDataChanged();

        }

    }

    @Override
    public void getIntervalThis(long startBack, long endBack) {
        listSorted = new SortableList<Tuple2<String, Long>, Order>(
                map, ((OrderMap)map).getFromToKeys(startBack, endBack));

    }

    public void addObservers() {

        super.addObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.addObserver(this);

    }

    public void deleteObservers() {
        super.deleteObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.deleteObserver(this);
    }

}