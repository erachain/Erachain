package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.wallet.FavoriteAccountsMap;
import org.erachain.gui.ObserverWaiter;
import org.erachain.gui.items.accounts.SendableModel;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.Observable;

@SuppressWarnings("serial")
public class AccountsTableModel extends WalletTableModel<PublicKeyAccount> implements SendableModel, ObserverWaiter {
    public static final int COLUMN_ADDRESS = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_BALANCE_1 = 3;
    public static final int COLUMN_BALANCE_2 = 4;
    public static final int COLUMN_BALANCE_3 = 5;
    public static final int COLUMN_BALANCE_4 = 6;
    public static final int COLUMN_FEE_BALANCE = 7;
    public final int COLUMN_NO = 0;
    private AssetCls asset;
    private Long assetKey;

    public AccountsTableModel() {
        super(Controller.getInstance().getWallet().dwSet.getAccountMap(),
                new String[]{"No.", "Account", "Name",
                        "OWN (1)", "DEBT (2)", "HOLD (3)", "SPEND (4)",
                        AssetCls.FEE_NAME},
                new Boolean[]{true, false, false, false, false, false, false, false}, false, -1000);

        getInterval();
        fireTableDataChanged();
        needUpdate = false;

        // переиницализация после установки таблиц
        this.addObservers();

    }

    @Override
    protected void updateMap() {
        map = Controller.getInstance().getWallet().dwSet.getAccountMap();
    }

    public void setAsset(AssetCls asset) {
        this.asset = asset;
        assetKey = asset.getKey();

        fireTableDataChanged();
        needUpdate = false;

    }

    public PublicKeyAccount getCreator(int row) {
        if (list == null)
            return null;

        return this.list.get(row);

    }

    public Account getRecipent(int row) {
        return null;
    }


    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        PublicKeyAccount account = list.get(row);

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance;
        String str;

        switch (column) {
            case COLUMN_NO:
                return account.getAccountNo();

            case COLUMN_ADDRESS:
                return account.getPersonAsString();

            case COLUMN_NAME:
                FavoriteAccountsMap favoriteMap = Controller.getInstance().getWallet().dwSet.getFavoriteAccountsMap();
                if (favoriteMap.contains(account.getAddress())) {
                    Fun.Tuple3<String, String, String> itemAccount = favoriteMap.get(account.getAddress());
                    if (itemAccount.b != null)
                        return itemAccount.b;
                }
                return "";

            case COLUMN_BALANCE_1:
                if (this.asset == null) return "-";
                balance = account.getBalance(assetKey);
                str = NumberAsString.formatAsString(balance.a.b.setScale(asset.getScale()));
                return str;
            case COLUMN_BALANCE_2:
                if (this.asset == null) return "-";
                balance = account.getBalance(assetKey);
                str = NumberAsString.formatAsString(balance.b.b.setScale(asset.getScale()));
                return str;
            case COLUMN_BALANCE_3:
                if (this.asset == null) return "-";
                balance = account.getBalance(assetKey);
                str = NumberAsString.formatAsString(balance.c.b.setScale(asset.getScale()));
                return str;
            case COLUMN_BALANCE_4:
                if (this.asset == null) return "-";
                balance = account.getBalance(assetKey);
                str = NumberAsString.formatAsString(balance.d.b.setScale(asset.getScale()));
                return str;
            case COLUMN_FEE_BALANCE:
                return NumberAsString.formatAsString(account.getBalance(Transaction.FEE_KEY).a.b.stripTrailingZeros());
        }

        return null;
    }

    public Tuple4<BigDecimal, BigDecimal, BigDecimal, BigDecimal> getTotalBalance() {

        if (this.asset == null || list == null)
            return new Tuple4(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal totalBalance1 = BigDecimal.ZERO;
        BigDecimal totalBalance2 = BigDecimal.ZERO;
        BigDecimal totalBalance3 = BigDecimal.ZERO;
        BigDecimal totalBalance4 = BigDecimal.ZERO;

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance;
        for (Account account : this.list) {
            balance = account.getBalance(assetKey);

            totalBalance1 = totalBalance1.add(balance.a.b);
            totalBalance2 = totalBalance2.add(balance.b.b);
            totalBalance3 = totalBalance3.add(balance.c.b);
            totalBalance4 = totalBalance4.add(balance.d.b);
        }

        return new Tuple4(totalBalance1, totalBalance2, totalBalance3, totalBalance4);
    }

    public void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;
        super.syncUpdate(o, arg);

        // если блок собрали или транзакция наша - обновим баланс
        if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE) {
            needUpdate = true;
        }

    }

    @Override
    public void getInterval() {
        list = Controller.getInstance().getWallet().getPublicKeyAccounts();
    }

    public void addObservers() {

        super.addObservers();

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            Controller.getInstance().getWallet().dwSet.getBlocksHeadMap().addObserver(this);
            Controller.getInstance().getWallet().dwSet.getTransactionMap().addObserver(this);
        }

    }

    public void deleteObservers() {

        super.deleteObservers();

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            Controller.getInstance().getWallet().dwSet.getBlocksHeadMap().deleteObserver(this);
            Controller.getInstance().getWallet().dwSet.getTransactionMap().deleteObserver(this);
        }
    }

}
