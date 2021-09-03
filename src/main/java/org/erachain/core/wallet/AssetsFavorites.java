package org.erachain.core.wallet;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.gui.Gui;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class AssetsFavorites implements Observer {

    private List<Long> favorites;

    public AssetsFavorites() {
        this.favorites = new ArrayList<Long>();

        Controller.getInstance().addWalletObserver(this);
        Controller.getInstance().addObserver(this);

    }

    public List<Long> getKeys() {
        return this.favorites;
    }

    public List<AssetCls> getAssets() {
        List<AssetCls> assets = new ArrayList<AssetCls>();
        for (Long key : this.favorites) {
            assets.add(Controller.getInstance().getAsset(key));
        }
        return assets;
    }

    public void reload() {
        List<Long> favoritesUpadate = new ArrayList<Long>();

        ItemAssetBalanceMap map = DCSet.getInstance().getAssetBalanceMap();
        for (Account account : Controller.getInstance().getWalletAccounts()) {
            List<Tuple2<byte[], Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> balancesList = map.getBalancesList(account);
            if (balancesList == null)
                return;

            for (Tuple2<byte[], Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balance : balancesList) {
                if (balance.b.a.b.compareTo(BigDecimal.ZERO) != 0
                        || balance.b.b.b.compareTo(BigDecimal.ZERO) != 0
                        || balance.b.c.b.compareTo(BigDecimal.ZERO) != 0
                        || balance.b.d.b.compareTo(BigDecimal.ZERO) != 0
                        || balance.b.e.b.compareTo(BigDecimal.ZERO) != 0) {
                    if (!favoritesUpadate.contains(ItemAssetBalanceMap.getAssetKeyFromKey(balance.a))) {
                        favoritesUpadate.add(ItemAssetBalanceMap.getAssetKeyFromKey(balance.a));
                    }
                }
            }
        }
        this.favorites = favoritesUpadate;

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
                        || message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE
                ))) {
            this.reload();
        }
    }
}