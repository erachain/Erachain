package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class CreditsTableModel extends TimerTableModelCls<Transaction> implements Observer {
    public static final int COLUMN_AMOUNT = 1;
    public static final int COLUMN_TRANSACTION = 2;
    private static final int COLUMN_ADDRESS = 0;
    List<Tuple2<Tuple3<String, Long, String>, BigDecimal>> cred;
    private Boolean[] column_AutuHeight = new Boolean[]{true, false, false, false};
    private List<PublicKeyAccount> publicKeyAccounts;
    private long asset_Key = 1L;
    private List<Transaction> transactions_Asset;

    @SuppressWarnings("unchecked")
    public CreditsTableModel() {
        super(DCSet.getInstance().getCredit_AddressesMap(),
                new String[]{"Account", "Amount", "Type"}, false);

        logger = LoggerFactory.getLogger(CreditsTableModel.class);

    }

    public Account getAccount(int row) {
        return publicKeyAccounts.get(row);
    }

    public PublicKeyAccount getPublicKeyAccount(int row) {
        return publicKeyAccounts.get(row);
    }

    public void setAsset(AssetCls asset) {
        asset_Key = asset.getKey();
        cred.clear();
        for (PublicKeyAccount account : this.publicKeyAccounts) {
            cred.addAll(DCSet.getInstance().getCredit_AddressesMap().getList(account.getAddress(), -asset_Key));
        }

        this.transactions_Asset.clear();

        for (Transaction trans : this.list) {
            long a = trans.getAssetKey();
            if (a == asset_Key || a == -asset_Key) {
                this.transactions_Asset.add(trans);
            }
        }

        this.fireTableDataChanged();
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (transactions_Asset.isEmpty()) return null;

        switch (column) {
            case COLUMN_ADDRESS:
                return transactions_Asset.get(row).getKey();
            case COLUMN_AMOUNT:
                return transactions_Asset.get(row).getAmount().toPlainString();
            case COLUMN_TRANSACTION:
                return Lang.T(transactions_Asset.get(row).viewFullTypeName());

        }

        return null;
    }


    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;


        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_TRANSACTION_TYPE) {
            this.fireTableDataChanged();
        }


        if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE) {
            this.publicKeyAccounts = Controller.getInstance().getWalletPublicKeyAccounts();
            cred.clear();
            for (PublicKeyAccount account : this.publicKeyAccounts) {
                cred.addAll(DCSet.getInstance().getCredit_AddressesMap().getList(account.getAddress(), -asset_Key));
            }


            this.fireTableDataChanged();

        }

    }

    public BigDecimal getTotalBalance() {
        BigDecimal totalBalance = BigDecimal.ZERO;

        for (Account account : this.publicKeyAccounts) {
            totalBalance = totalBalance.add(account.getBalanceUSE(this.asset_Key));
        }

        return totalBalance;
    }

    public void addObservers() {

        this.transactions_Asset = new ArrayList<Transaction>();
        this.publicKeyAccounts = Controller.getInstance().getWalletPublicKeyAccounts();

        cred = new ArrayList<Tuple2<Tuple3<String, Long, String>, BigDecimal>>();
        for (PublicKeyAccount account : this.publicKeyAccounts) {
            cred.addAll(DCSet.getInstance().getCredit_AddressesMap().getList(account.getAddress(), -asset_Key));
        }

        Controller.getInstance().addWalletObserver(this);

    }

}
