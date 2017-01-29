package gui.items.accounts;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import utils.NumberAsString;
import utils.ObserverMessage;
import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import database.DBSet;
import lang.Lang;

@SuppressWarnings("serial")
public class CreditsTableModel extends AbstractTableModel implements Observer
{
	private static final int COLUMN_ADDRESS = 0;
//	public static final int COLUMN_BALANCE = 1;
//	public static final int COLUMN_CONFIRMED_BALANCE = 1;
//	public static final int COLUMN_WAINTING_BALANCE = 2;
	//public static final int COLUMN_GENERATING_BALANCE = 3;
//	public static final int COLUMN_FEE_BALANCE = 3;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Address"}); //, "Confirmed Balance", "Waiting", AssetCls.FEE_NAME});
	private Boolean[] column_AutuHeight = new Boolean[]{true,false,false,false};
	private List<PublicKeyAccount> publicKeyAccounts;
	private AssetCls asset;
	private Account account;
	long key;
	List<Tuple2<Tuple3<String, Long, String>, BigDecimal>> cred ;
	
	public CreditsTableModel()
	{
		this.publicKeyAccounts = Controller.getInstance().getPublicKeyAccounts();
		Controller.getInstance().addWalletListener(this);
		Controller.getInstance().addObserver(this);
		this.key = key;
		 cred = new ArrayList<Tuple2<Tuple3<String, Long, String>, BigDecimal>>();
		for (PublicKeyAccount account:this.publicKeyAccounts){
			
			
		 cred.addAll(DBSet.getInstance().getCredit_AddressesMap().getList(account.getAddress(), 1));
		 cred.addAll(DBSet.getInstance().getCredit_AddressesMap().getList(account.getAddress(), -1));	
			
			
		}
		
//		int a = 1;
		
	}
	
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?null:o.getClass();
	   }
// читаем колонки которые изменяем высоту	   
	public Boolean[] get_Column_AutoHeight(){
		
		return this.column_AutuHeight;
	}
// устанавливаем колонки которым изменить высоту	
	public void set_get_Column_AutoHeight( Boolean[] arg0){
		this.column_AutuHeight = arg0;	
	}
	
	
	public Account getAccount(int row)
	{
		return (Account)publicKeyAccounts.get(row);
	}
	public PublicKeyAccount getPublicKeyAccount(int row)
	{
		return publicKeyAccounts.get(row);
	}
	
	public void setAsset(AssetCls asset) 
	{
		cred.clear();
		for (PublicKeyAccount account:this.publicKeyAccounts){
			
			
			 cred.addAll(DBSet.getInstance().getCredit_AddressesMap().getList(account.getAddress(), asset.getKey()));
			 cred.addAll(DBSet.getInstance().getCredit_AddressesMap().getList(account.getAddress(), -asset.getKey()));	
				
				
			}
		
		this.fireTableDataChanged();
	}
	
	@Override
	public int getColumnCount() 
	{
		return columnNames.length;
	}
	
	@Override
	public String getColumnName(int index) 
	{
		return columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		
		return cred.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		
		if (cred.size() == 0) return null;
		
	/*	if(this.publicKeyAccounts == null || row > this.publicKeyAccounts.size() - 1 )
		{
			return null;
		}
		
		
		account = this.publicKeyAccounts.get(row);
		
		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance;
		Tuple3<BigDecimal, BigDecimal, BigDecimal> unconfBalance;
		String str;
	*/	
		switch(column)
		{
		case COLUMN_ADDRESS:			
			return cred.get(row).toString();
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
				return NumberAsString.getInstance().numberAsString(BigDecimal.ZERO.setScale(8));
			}
			*/
			
		}
		
		return null;
	}

	@Override
	public void update(Observable o, Object arg) 
	{
		try
		{
			this.syncUpdate(o, arg);
		}
		catch(Exception e)
		{
			//GUI ERROR
		}
	}
	
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		if( message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK ) {
			
			this.fireTableRowsUpdated(0, this.getRowCount()-1);
			
		} else if (Controller.getInstance().getStatus() == Controller.STATUS_OK) {
			
			if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE || message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE || message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
			{
				this.publicKeyAccounts = Controller.getInstance().getPublicKeyAccounts();
				
				
				this.fireTableRowsUpdated(0, this.getRowCount()-1);  // WHEN UPDATE DATA - SELECTION DOES NOT DISAPPEAR
			}
			
			if(message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE || message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE)
			{
	// обновляем данные
				this.publicKeyAccounts = Controller.getInstance().getPublicKeyAccounts();
				this.fireTableDataChanged();
			}
		}
	
		
	
	}

	public BigDecimal getTotalBalance() 
	{
		BigDecimal totalBalance = BigDecimal.ZERO.setScale(8);
		
		for(Account account: this.publicKeyAccounts)
		{
			if(this.asset == null)
			{
				totalBalance = totalBalance.add(account.getBalanceUSE(Transaction.FEE_KEY));
			}
			else
			{
				totalBalance = totalBalance.add(account.getBalanceUSE(this.asset.getKey(DBSet.getInstance())));
			}
		}
		
		return totalBalance;
	}
}
