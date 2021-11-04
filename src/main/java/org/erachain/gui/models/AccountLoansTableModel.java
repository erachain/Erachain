package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.wallet.FavoriteAccountsMap;
import org.erachain.datachain.CreditAddressesMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.ObserverWaiter;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Observable;

@SuppressWarnings("serial")
public class AccountLoansTableModel extends WalletTableModel<Tuple3<PublicKeyAccount, Account, BigDecimal>> implements ObserverWaiter {
    public final int COLUMN_NO = 0;
    public static final int COLUMN_ADDRESS = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_SIDE_ADDRESS = 3;
    public static final int COLUMN_SIDE_NAME = 4;
    public static final int COLUMN_BALANCE_2 = 5;
    private AssetCls asset;

    public AccountLoansTableModel(AssetCls asset) {
        super(Controller.getInstance().getWallet().dwSet.getAccountMap(),
                new String[]{"No.", "Account", "Name", "Side Account", "Name", "DEBT or LOAN"},
                new Boolean[]{true, false, false, false, false}, false, -1000);

        this.asset = asset;
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

        getInterval();
        fireTableDataChanged();
        needUpdate = false;

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        Tuple3<PublicKeyAccount, Account, BigDecimal> item = list.get(row);

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance;
        String str;

        switch (column) {
            case COLUMN_NO:
                return item.a.getAccountNo();

            case COLUMN_ADDRESS:
                return item.a.getPersonAsString();

            case COLUMN_NAME:
                FavoriteAccountsMap favoriteMap = Controller.getInstance().getWallet().dwSet.getFavoriteAccountsMap();
                if (favoriteMap.contains(item.a.getAddress())) {
                    Fun.Tuple3<String, String, String> itemAccount = favoriteMap.get(item.a.getAddress());
                    if (itemAccount.b != null)
                        return itemAccount.b;
                }
                return "";

            case COLUMN_BALANCE_2:
                if (this.asset == null) return "-";
                return item.c.toPlainString();
            case COLUMN_SIDE_ADDRESS:
                return item.b.getPersonAsString();

            case COLUMN_SIDE_NAME:
                favoriteMap = Controller.getInstance().getWallet().dwSet.getFavoriteAccountsMap();
                if (favoriteMap.contains(item.b.getAddress())) {
                    Fun.Tuple3<String, String, String> itemAccount = favoriteMap.get(item.b.getAddress());
                    if (itemAccount.b != null)
                        return itemAccount.b;

                }
        }

        return null;
    }

    public BigDecimal getTotalBalance() {

        if (this.asset == null || list == null)
            return BigDecimal.ZERO;

        BigDecimal totalBalance2 = BigDecimal.ZERO;

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance;
        for (Tuple3<PublicKeyAccount, Account, BigDecimal> item : this.list) {
            totalBalance2 = totalBalance2.add(item.c);
        }

        return totalBalance2;
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
        Tuple3<String, Long, String> nextKey;
        list = new ArrayList<>();
        CreditAddressesMap creditsMap = dcSet.getCreditAddressesMap();
        for (PublicKeyAccount myAccount : Controller.getInstance().getWallet().getPublicKeyAccounts()) {
            try (IteratorCloseable<Tuple3<String, Long, String>> debitors = creditsMap.getDebitorsIterator(myAccount.getAddress(), asset.getKey())) {
                while (debitors.hasNext()) {
                    nextKey = debitors.next();
                    BigDecimal item = creditsMap.get(nextKey);
                    list.add(new Tuple3<>(myAccount, new Account(nextKey.c), item.negate()));
                }
            } catch (IOException e) {
            }
            try (IteratorCloseable<Tuple3<String, Long, String>> creditors = creditsMap.getCreditorsIterator(myAccount.getAddress(), asset.getKey())) {
                while (creditors.hasNext()) {
                    nextKey = creditors.next();
                    BigDecimal item = creditsMap.get(nextKey);
                    list.add(new Tuple3<>(myAccount, new Account(nextKey.c), item));
                }
            } catch (IOException e) {
            }

        }
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
