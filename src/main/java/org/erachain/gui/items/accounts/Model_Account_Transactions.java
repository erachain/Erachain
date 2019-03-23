package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.TransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.SortedListTableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class Model_Account_Transactions extends SortedListTableModelCls<Tuple2<String, String>, Transaction> implements Observer {
    public static final int COLUMN_AMOUNT = 1;
    public static final int COLUMN_TRANSACTION = 2;
    private static final int COLUMN_ADDRESS = 0;
    //	public static final int COLUMN_CONFIRMED_BALANCE = 1;
    //	public static final int COLUMN_WAINTING_BALANCE = 2;
    //public static final int COLUMN_GENERATING_BALANCE = 3;
    //	public static final int COLUMN_FEE_BALANCE = 3;
    List<Tuple2<Tuple3<String, Long, String>, BigDecimal>> cred;
    //private String[] columnNames = Lang.getInstance().translate(new String[]{"Account", "Amount", "Type"}); //, "Confirmed Balance", "Waiting", AssetCls.FEE_NAME});
    private Boolean[] column_AutuHeight = new Boolean[]{true, false, false, false};
    private List<PublicKeyAccount> publicKeyAccounts;
    private Account account;
    private long asset_Key = 1l;
    private AssetCls asset = GenesisBlock.makeAsset(asset_Key);
    private SortableList<Tuple2<String, String>, Transaction> transactions;
    private List<Transaction> transactions_Asset;
    //private  Account_Cls account;

    @SuppressWarnings("unchecked")
    public Model_Account_Transactions() {
        super(new String[]{"Account", "Amount", "Type"}, true);

        LOGGER = LoggerFactory.getLogger(Model_Account_Transactions.class.getName());

        this.transactions_Asset = new ArrayList<Transaction>();
        this.publicKeyAccounts = Controller.getInstance().getPublicKeyAccounts();
        cred = new ArrayList<Tuple2<Tuple3<String, Long, String>, BigDecimal>>();
        account = new Account("");

    }

    @Override
    public SortableList<Tuple2<String, String>, Transaction> getSortableList() {
        return this.transactions;
    }

    @Override
    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] getColumnAutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void setColumnAutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }


    public Account getAccount(int row) {
        return publicKeyAccounts.get(row);
    }

    public PublicKeyAccount getPublicKeyAccount(int row) {
        return publicKeyAccounts.get(row);
    }

    public void setParam(AssetCls asset, Account account) {
        if (account != null) this.account = account;
        if (asset != null) {
            this.asset = asset;
            asset_Key = asset.getKey();
        }

        List<Transaction> transactions = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddress(this.account.getAddress());


        this.transactions_Asset.clear();
        ;
        for (Transaction trans1 : transactions) {
            long a = trans1.getAssetKey();
            if ((a == asset_Key || a == -asset_Key)) {

                this.transactions_Asset.add(trans1);


            }


        }


        this.fireTableDataChanged();
    }

    @Override
    public int getRowCount() {

        return transactions_Asset.size();
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (transactions_Asset.isEmpty()) return null;

		/*	if(this.publicKeyAccounts == null || row > this.publicKeyAccounts.size() - 1 )
		{
			return null;
		}


		account = this.publicKeyAccounts.get(row);

		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance;
		Tuple3<BigDecimal, BigDecimal, BigDecimal> unconfBalance;
		String str;
		 */
        switch (column) {
            case COLUMN_ADDRESS:
                return transactions_Asset.get(row).getKey();
            case COLUMN_AMOUNT:
                return transactions_Asset.get(row).getAmount().toPlainString();
            case COLUMN_TRANSACTION:
                return Lang.getInstance().translate(transactions_Asset.get(row).viewTypeName());
			/*
		case COLUMN_CONFIRMED_BALANCE:
			if (this.asset == null) return "-";
			balance = account.getBalance(this.asset.getKey(DLSet.getInstance()));
			str = NumberAsString.getInstance().numberAsString(balance.a) + "/" + balance.b.toPlainString() + "/" + balance.c.toPlainString();
			return str;
		case COLUMN_WAINTING_BALANCE:
			if (this.asset == null) return "-";
			balance = account.getBalance(this.asset.getKey(DLSet.getInstance()));
			unconfBalance = account.getUnconfirmedBalance(this.asset.getKey(DLSet.getInstance()));
			str = NumberAsString.getInstance().numberAsString(unconfBalance.a.subtract(balance.a))
					+ "/" + unconfBalance.b.subtract(balance.b).toPlainString()
					+ "/" + unconfBalance.c.subtract(balance.c).toPlainString();
			return str;
		case COLUMN_FEE_BALANCE:
			if (this.asset == null) return "-";
			return NumberAsString.getInstance().numberAsString(account.getBalanceUSE(Transaction.FEE_KEY));
			 */

			/*

		case COLUMN_GENERATING_BALANCE:

			if(this.asset == null || this.asset.getKey() == AssetCls.FEE_KEY)
			{
				return  NumberAsString.getInstance().numberAsString(account.getGeneratingBalance());
			}
			else
			{
				return NumberAsString.getInstance().numberAsString(BigDecimal.ZERO);
			}
			 */

        }

        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;


        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_TRANSACTION_TYPE) {
            if (this.transactions == null) {
                this.transactions = (SortableList<Tuple2<String, String>, Transaction>) message.getValue();
                //this.transactions.registerObserver();
                this.transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);

                this.transactions_Asset.clear();
                ;
                for (Pair<Tuple2<String, String>, Transaction> trans : this.transactions) {
                    long a = trans.getB().getAssetKey();
                    Transaction trans1 = trans.getB();
                    Tuple2<Tuple2<String, String>, Transaction> ss = null;

                    if ((a == asset_Key || a == -asset_Key) && (account.getAddress() == trans1.viewCreator() || account.getAddress() == trans1.viewRecipient())) {
                        this.transactions_Asset.add(trans.getB());


                    }


                }


            }

            this.fireTableDataChanged();
        }


        if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE) {
            this.publicKeyAccounts = Controller.getInstance().getPublicKeyAccounts();
            cred.clear();
            for (PublicKeyAccount account : this.publicKeyAccounts) {
                cred.addAll(DCSet.getInstance().getCredit_AddressesMap().getList(account.getAddress(), -asset_Key));
                //cred.addAll(DLSet.getInstance().getCredit_AddressesMap().getList(Base58.decode(account.getAddress()), asset_Key));
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
        return transactions_Asset.get(k);
    }
}
