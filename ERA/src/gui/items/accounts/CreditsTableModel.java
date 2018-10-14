package gui.items.accounts;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import database.wallet.TransactionMap;
import datachain.DCSet;
import datachain.SortableList;
import gui.models.TableModelCls;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import utils.ObserverMessage;
import utils.Pair;

import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class CreditsTableModel extends TableModelCls<Tuple2<String, String>, Transaction> implements Observer {
    public static final int COLUMN_AMOUNT = 1;
    public static final int COLUMN_TRANSACTION = 2;
    private static final int COLUMN_ADDRESS = 0;
    //	public static final int COLUMN_CONFIRMED_BALANCE = 1;
    //	public static final int COLUMN_WAINTING_BALANCE = 2;
    //public static final int COLUMN_GENERATING_BALANCE = 3;
    //	public static final int COLUMN_FEE_BALANCE = 3;
    //	private AssetCls asset = core.block.GenesisBlock.makeAsset(asset_Key);
    //private Account account;
    List<Tuple2<Tuple3<String, Long, String>, BigDecimal>> cred;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Account", "Amount", "Type"}); //, "Confirmed Balance", "Waiting", AssetCls.FEE_NAME});
    private Boolean[] column_AutuHeight = new Boolean[]{true, false, false, false};
    private List<PublicKeyAccount> publicKeyAccounts;
    private long asset_Key = 1l;
    private SortableList<Tuple2<String, String>, Transaction> transactions;
    private List<Tuple2<Tuple2<String, String>, Transaction>> transactions_Asset;

    @SuppressWarnings("unchecked")
    public CreditsTableModel() {
        this.transactions_Asset = new ArrayList<Tuple2<Tuple2<String, String>, Transaction>>();
        this.publicKeyAccounts = Controller.getInstance().getPublicKeyAccounts();

        cred = new ArrayList<Tuple2<Tuple3<String, Long, String>, BigDecimal>>();
        for (PublicKeyAccount account : this.publicKeyAccounts) {
            //cred.addAll(DBSet.getInstance().getCredit_AddressesMap().getList(Base58.decode(account.getAddress()), asset_Key));
            cred.addAll(DCSet.getInstance().getCredit_AddressesMap().getList(account.getAddress(), -asset_Key));
        }

        Controller.getInstance().addWalletListener(this);
        //		Controller.getInstance().addObserver(this);
        //		int a = 1;

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
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
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
            List<Transaction> trans = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddress(account.getAddress());
            //cred.addAll(DBSet.getInstance().getCredit_AddressesMap().getList(Base58.decode(account.getAddress()), asset_Key));
            cred.addAll(DCSet.getInstance().getCredit_AddressesMap().getList(account.getAddress(), -asset_Key));
        }
		/*		for (Pair<Tuple2<String, String>, Transaction> trans:this.transactions){
			long a = trans.getB().getAssetKey();
			this.transactions_Asset.clear();
				if (a == asset_Key){

					this.transactions_Asset.add(trans);


				}


			}
		 */
        this.transactions_Asset.clear();
        ;
        for (Pair<Tuple2<String, String>, Transaction> trans : this.transactions) {
            long a = trans.getB().getAssetKey();
            Tuple2<Tuple2<String, String>, Transaction> ss = null;
            if (a == asset_Key || a == -asset_Key) {
                ss = new Tuple2(trans.getA(), trans.getB());
                this.transactions_Asset.add(ss);
            }
        }

        this.fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return columnNames[index];
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


		account = this.publicKeyAccounts.getBySignature(row);

		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance;
		Tuple3<BigDecimal, BigDecimal, BigDecimal> unconfBalance;
		String str;
		 */
        switch (column) {
            case COLUMN_ADDRESS:
                return transactions_Asset.get(row).b.getKey();
            case COLUMN_AMOUNT:
                return transactions_Asset.get(row).b.getAmount().toPlainString();
            case COLUMN_TRANSACTION:
                return Lang.getInstance().translate(transactions_Asset.get(row).b.viewTypeName());
			/*
		case COLUMN_CONFIRMED_BALANCE:
			if (this.asset == null) return "-";
			balance = account.getBalance(this.asset.getKey(DBSet.getInstance()));
			str = NumberAsString.getInstance().numberAsString(balance.a) + "/" + balance.b.toPlainString() + "/" + balance.c.toPlainString();
			return str;
		case COLUMN_WAINTING_BALANCE:
			if (this.asset == null) return "-";
			balance = account.getBalance(this.asset.getKey(DBSet.getInstance()));
			unconfBalance = account.getUnconfirmedBalance(this.asset.getKey(DBSet.getInstance()));
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
                this.transactions.registerObserver();
                this.transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);

                this.transactions_Asset.clear();
                ;
                for (Pair<Tuple2<String, String>, Transaction> trans : this.transactions) {
                    long a = trans.getB().getAssetKey();
                    Tuple2<Tuple2<String, String>, Transaction> ss = null;
                    if (a == asset_Key || a == -asset_Key) {

                        ss = new Tuple2(trans.getA(), trans.getB());


                        this.transactions_Asset.add(ss);


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
                //cred.addAll(DBSet.getInstance().getCredit_AddressesMap().getList(Base58.decode(account.getAddress()), asset_Key));
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
            totalBalance = totalBalance.add(account.getBalanceUSE(this.asset_Key));
        }

        return totalBalance;
    }

    @Override
    public Object getItem(int k) {
        // TODO Auto-generated method stub
        return transactions_Asset.get(k);
    }
}
