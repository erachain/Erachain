package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("serial")
public class BalanceFromAddressTableModel extends TimerTableModelCls<Tuple2<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> implements Observer {
    public static final int COLUMN_C = 4;
    public static final int COLUMN_B = 3;
    public static final int COLUMN_A = 2;
    public static final int COLUMN_ASSET_NAME = 1;
    public static final int COLUMN_ASSET_KEY = 0;

    public static final int COLUMN_FOR_ICON = 1;

    Account account;
    private List<Tuple2<Account, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> tableBalance;
    private List<Tuple2<Account, Tuple2<Long, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> tableBalance1;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public BalanceFromAddressTableModel() {
        super(DCSet.getInstance().getAssetBalanceMap(), new String[]{"key Asset", "Asset", "Balance A", "Balance B", "Balance C"}, false);
        Controller.getInstance().addObserver(this);
        List<Account> accounts = Controller.getInstance().getWalletAccounts();
        tableBalance = new ArrayList<>();// Pair();
        tableBalance1 = new ArrayList<>();
        HashSet<Long> assetKeys = new HashSet();

        //ItemAssetBalanceMap map = DCSet.getInstance().getAssetBalanceMap();
        for (Account account1 : accounts) {
            account = account1;
            list = Controller.getInstance().getBalances(account);
            for (Tuple2<byte[], Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balance : this.list) {

                if (Controller.getInstance().getAsset(ItemAssetBalanceMap.getAssetKeyFromKey(balance.a)) == null) {
                    // SKIP LIA etc.
                    continue;
                }

                Long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(balance.a);
                tableBalance1.add(new Tuple2(account, new Pair(assetKey, balance.b)));
                assetKeys.add(assetKey);
            }
        }

        for (Long assetKey: assetKeys) {
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
            for (Tuple2<Account, Tuple2<Long, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> k : this.tableBalance1) {
                if (assetKey.equals(k.b.a)) {

                    sumAA = sumAA.add(k.b.b.a.a);
                    sumBA = sumBA.add(k.b.b.b.a);
                    sumCA = sumCA.add(k.b.b.c.a);
                    sumDA = sumDA.add(k.b.b.d.a);
                    sumEA = sumEA.add(k.b.b.e.a);

                    sumAB = sumAB.add(k.b.b.a.b);
                    if (BlockChain.ERA_COMPU_ALL_UP) {
                        sumAB = sumAB.add(k.a.addDEVAmount(assetKey));
                    }
                    sumBB = sumBB.add(k.b.b.b.b);
                    sumCB = sumCB.add(k.b.b.c.b);
                    sumDB = sumDB.add(k.b.b.d.b);
                    sumEB = sumEB.add(k.b.b.e.b);

                }

            }
            tableBalance.add(new Tuple2(account, new Tuple2(assetKey, new Tuple5(
                    new Tuple2(sumAA, sumAB), new Tuple2(sumBA, sumBB), new Tuple2(sumCA, sumCB),
                    new Tuple2(sumDA, sumDB), new Tuple2(sumEA, sumEB)))));

        }

    }

    public AssetCls getAsset(int row) {
        return Controller.getInstance().getAsset(tableBalance.get(row).b.a);
    }

    public String getAccount(int row) {
        return tableBalance.get(row).a.getAddress();
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (tableBalance == null || row > tableBalance.size() - 1) {
            return null;
        }

        AssetCls asset = Controller.getInstance().getAsset(tableBalance.get(row).b.a);

        switch (column) {
            case COLUMN_ASSET_KEY:
                return asset.getKey();
            case COLUMN_A:
                return NumberAsString.formatAsString(tableBalance.get(row).b.a.b);
            case COLUMN_B:
                return NumberAsString.formatAsString(tableBalance.get(row).b.b.b);
            case COLUMN_C:
                return NumberAsString.formatAsString(tableBalance.get(row).b.c.b);
            case COLUMN_ASSET_NAME:
                return asset;
        }
        return null;
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        // CHECK IF LIST UPDATED
        if ((message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK)
                || (false && Controller.getInstance().getStatus() == Controller.STATUS_OK
                        && (message.getType() == ObserverMessage.ADD_BALANCE_TYPE || message.getType() == ObserverMessage.REMOVE_BALANCE_TYPE))
        ) {
            this.fireTableDataChanged();
        }
    }

}
