package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.wallet.WTransactionMap;
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
public class ModelAccountTransactions extends TimerTableModelCls<Transaction> implements Observer {
    public static final int COLUMN_AMOUNT = 1;
    public static final int COLUMN_TRANSACTION = 2;
    private static final int COLUMN_ADDRESS = 0;
    List<Tuple2<Tuple3<String, Long, String>, BigDecimal>> cred;
    private Boolean[] column_AutuHeight = new Boolean[]{true, false, false, false};
    private List<PublicKeyAccount> publicKeyAccounts;
    private Account account;
    private long filterAssetKey = 1l;
    private AssetCls asset = GenesisBlock.makeAsset(filterAssetKey);
    private List<Transaction> assetTransactions;

    @SuppressWarnings("unchecked")
    public ModelAccountTransactions() {
        super(new String[]{"Account", "Amount", "Type"}, true);

        logger = LoggerFactory.getLogger(ModelAccountTransactions.class);

        this.assetTransactions = new ArrayList<Transaction>();
        this.publicKeyAccounts = Controller.getInstance().getWalletPublicKeyAccounts();
        cred = new ArrayList<Tuple2<Tuple3<String, Long, String>, BigDecimal>>();
        account = new Account("");

    }

    public Account getAccount(int row) {
        return publicKeyAccounts.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (assetTransactions.isEmpty()) return null;

        switch (column) {
            case COLUMN_ADDRESS:
                return assetTransactions.get(row).getKey();
            case COLUMN_AMOUNT:
                return assetTransactions.get(row).getAmount().toPlainString();
            case COLUMN_TRANSACTION:
                return Lang.getInstance().translate(assetTransactions.get(row).viewTypeName());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;


        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_TRANSACTION_TYPE) {
            if (this.list == null) {
                this.list = ((WTransactionMap) message.getValue()).get(account, 1000);

                this.assetTransactions.clear();

                for (Transaction trans : this.list) {
                    long assetKey = trans.getAssetKey();

                    if (assetKey == filterAssetKey
                            && (trans.isInvolved(account))) {
                        this.assetTransactions.add(trans);

                    }
                }
            }

            this.fireTableDataChanged();
        }


        if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE) {
            this.publicKeyAccounts = Controller.getInstance().getWalletPublicKeyAccounts();
            cred.clear();
            for (PublicKeyAccount account : this.publicKeyAccounts) {
                cred.addAll(DCSet.getInstance().getCredit_AddressesMap().getList(account.getAddress(), -filterAssetKey));
            }


            this.fireTableDataChanged();

            //	this.fireTableRowsUpdated(0, this.getRowCount()-1);  // WHEN UPDATE DATA - SELECTION DOES NOT DISAPPEAR
        }
		/*
			if(message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE || message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE)
			{
	// обновляем данные
				this.publicKeyAccounts = Controller.getInstance().getPublicKeyAccounts();
				this.fireTableDataChanged();
			}
		 */


    }

    public BigDecimal getTotalBalance() {
        BigDecimal totalBalance = BigDecimal.ZERO;

        for (Account account : this.publicKeyAccounts) {
            if (this.asset == null) {
                totalBalance = totalBalance.add(account.getBalanceUSE(Transaction.FEE_KEY));
            } else {
                totalBalance = totalBalance.add(account.getBalanceUSE(this.asset.getKey(DCSet.getInstance())));
            }
        }

        return totalBalance;
    }

    public void addObservers() {
        super.addObservers();
        //Controller.getInstance().addWalletObserver(this);

        // REGISTER ON ACCOUNTS
        Controller.getInstance().wallet.database.getAccountMap().addObserver(this);

        // REGISTER ON TRANSACTIONS
        Controller.getInstance().wallet.database.getTransactionMap().addObserver(this);

    }

    public void deleteObservers() {
        // REGISTER ON ACCOUNTS
        Controller.getInstance().wallet.database.getAccountMap().deleteObserver(this);

        // REGISTER ON TRANSACTIONS
        Controller.getInstance().wallet.database.getTransactionMap().deleteObserver(this);

        super.deleteObservers();
    }

    @Override
    public Transaction getItem(int k) {
        // TODO Auto-generated method stub
        return assetTransactions.get(k);
    }
}
