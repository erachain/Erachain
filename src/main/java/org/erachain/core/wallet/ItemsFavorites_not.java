package org.erachain.core.wallet;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.ItemCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.Gui;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class ItemsFavorites_not implements Observer {

    private List<Long> favorites;
    private int type;


    public ItemsFavorites_not(int type) {

        this.type = type;
        this.favorites = new ArrayList<Long>();

        Controller.getInstance().addWalletObserver(this);
        Controller.getInstance().addObserver(this);
        ///this.reload();
        //this.getAssets();

    }

    public List<Long> getKeys() {
        return this.favorites;
    }

    public List<ItemCls> getItems() {
        List<ItemCls> assets = new ArrayList<ItemCls>();
        for (Long key : this.favorites) {
            assets.add(Controller.getInstance().getItem(this.type, key));
        }
        return assets;
    }

    public void reload() {
        List<Long> favoritesUpadate = new ArrayList<Long>();
        SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balancesList;

        for (Account account : Controller.getInstance().getAccounts()) {

            balancesList = DCSet.getInstance().getAssetBalanceMap().getBalancesSortableList(account);
            for (Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balance : balancesList) {
                if (balance.getB().a.b.compareTo(BigDecimal.ZERO) != 0
                        || balance.getB().b.b.compareTo(BigDecimal.ZERO) != 0
                        || balance.getB().c.b.compareTo(BigDecimal.ZERO) != 0
                        || balance.getB().d.b.compareTo(BigDecimal.ZERO) != 0
                        || balance.getB().e.b.compareTo(BigDecimal.ZERO) != 0
                        ) {
                    if (!favoritesUpadate.contains(balance.getA().b)) {
                        favoritesUpadate.add(balance.getA().b);
                    }
                }
            }
        }
        this.favorites = favoritesUpadate;

        //Controller.getInstance().replaseAssetsFavorites();
        Controller.getInstance().replaseFavoriteItems(this.type);

    }

    @Override
    public void update(Observable o, Object arg) {

        if (!Gui.isGuiStarted()) {
            return;
        }

        ObserverMessage message = (ObserverMessage) arg;

        if ((message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK)
                || ((Controller.getInstance().getStatus() == Controller.STATUS_OK) &&
                (
                        message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE
                                ||
                                message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE
                                ||
                                message.getType() == ObserverMessage.ADD_BALANCE_TYPE
                                ||
                                message.getType() == ObserverMessage.REMOVE_BALANCE_TYPE
                ))) {
            this.reload();
        }
    }
}