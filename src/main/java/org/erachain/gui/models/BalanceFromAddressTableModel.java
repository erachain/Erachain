package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.dbs.DBTabImpl;
import org.erachain.utils.NumberAsString;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observer;

@SuppressWarnings("serial")
public class BalanceFromAddressTableModel extends TimerTableModelCls<Tuple2<Long, Tuple5<
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> implements Observer {
    public static final int COLUMN_ASSET_KEY = 0;
    public static final int COLUMN_ASSET_NAME = 1;
    public static final int COLUMN_B1 = 2;
    public static final int COLUMN_B2 = 3;
    public static final int COLUMN_B3 = 4;
    public static final int COLUMN_B4 = 5;

    public static final int COLUMN_FOR_ICON = 1;

    List<Account> accounts;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public BalanceFromAddressTableModel() {
        super((DBTabImpl) DCSet.getInstance().getAssetBalanceMap(),
                new String[]{"key Asset", "Asset", "OWN (1)", "DEBT (2)", "HOLD (3)", "SPEND (4)"}, false);
        Controller.getInstance().addObserver(this);
        accounts = Controller.getInstance().getWalletAccounts();

        // AFTER init ACCOUNTS
        getInterval();

    }

    public AssetCls getAsset(int row) {
        return Controller.getInstance().getAsset(list.get(row).a);
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (list == null || row > list.size() - 1) {
            return null;
        }

        switch (column) {
            case COLUMN_ASSET_KEY:
                AssetCls asset = Controller.getInstance().getAsset(list.get(row).a);
                return asset.getKey();
            case COLUMN_ASSET_NAME:
                return Controller.getInstance().getAsset(list.get(row).a);
            case COLUMN_B1:
                return NumberAsString.formatAsString(list.get(row).b.a.b);
            case COLUMN_B2:
                return NumberAsString.formatAsString(list.get(row).b.b.b);
            case COLUMN_B3:
                return NumberAsString.formatAsString(list.get(row).b.c.b);
            case COLUMN_B4:
                return NumberAsString.formatAsString(list.get(row).b.d.b);
        }
        return null;
    }

    public void getInterval() {

        list = new ArrayList<>();
        accounts = Controller.getInstance().getWalletAccounts();

        HashSet<Long> assetKeys = new HashSet();
        List<Tuple2<byte[], Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>
                accountBalances;
        List<Tuple2<Long, Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> tableBalances = new ArrayList<>();

        for (Account account : accounts) {
            accountBalances = Controller.getInstance().getBalances(account);
            for (Tuple2<byte[], Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                    balance : accountBalances) {

                if (Controller.getInstance().getAsset(ItemAssetBalanceMap.getAssetKeyFromKey(balance.a)) == null) {
                    // SKIP LIA etc.
                    continue;
                }

                Long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(balance.a);
                Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                        accBalance = balance.b;

                tableBalances.add(new Tuple2(assetKey, accBalance));
                assetKeys.add(assetKey);
            }
        }

        for (Long assetKey : assetKeys) {
            BigDecimal sumAA = new BigDecimal(0);
            BigDecimal sumBA = new BigDecimal(0);
            BigDecimal sumCA = new BigDecimal(0);
            BigDecimal sumDA = new BigDecimal(0);
            BigDecimal sumEA = new BigDecimal(0);
            BigDecimal sumAB = new BigDecimal(0);
            BigDecimal sumBB = new BigDecimal(0);
            BigDecimal sumCB = new BigDecimal(0);
            BigDecimal sumDB = new BigDecimal(0);
            BigDecimal sumEB = new BigDecimal(0);
            for (Tuple2<Long, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                    balance : tableBalances) {
                if (assetKey.equals(balance.a)) {

                    sumAA = sumAA.add(balance.b.a.a);
                    sumBA = sumBA.add(balance.b.b.a);
                    sumCA = sumCA.add(balance.b.c.a);
                    sumDA = sumDA.add(balance.b.d.a);
                    sumEA = sumEA.add(balance.b.e.a);

                    sumAB = sumAB.add(balance.b.a.b);
                    sumBB = sumBB.add(balance.b.b.b);
                    sumCB = sumCB.add(balance.b.c.b);
                    sumDB = sumDB.add(balance.b.d.b);
                    sumEB = sumEB.add(balance.b.e.b);

                }

            }
            list.add(new Tuple2(assetKey, new Tuple5(
                    new Tuple2(sumAA, sumAB), new Tuple2(sumBA, sumBB), new Tuple2(sumCA, sumCB),
                    new Tuple2(sumDA, sumDB), new Tuple2(sumEA, sumEB))));

        }

    }

}
