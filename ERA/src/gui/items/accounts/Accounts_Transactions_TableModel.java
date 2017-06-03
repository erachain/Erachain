package gui.items.accounts;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
////////
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.mapdb.Fun.Tuple2;

import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.naming.Name;
import core.transaction.GenesisTransferAssetTransaction;
import core.transaction.R_Send;
import core.transaction.Transaction;
import core.wallet.Wallet;
import database.DBSet;
import database.SortableList;
import database.wallet.NameMap;
import database.wallet.TransactionMap;
import gui.items.accounts.Account_Transactions_Table.MessageBuf;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class Accounts_Transactions_TableModel extends AbstractTableModel implements Observer
{
	public static final int COLUMN_TIMESTAMP = 0;

	public static final int COLUMN_AMOUNT = 1;
	public static final int COLUMN_ASSET = 2;
	public static final int COLUMN_RECIPIENT = 3;
	public static final int COLUMN_MESSAGE = 4;
	public static final int COLUMN_CONFIRM = 5;
	
	private List<Transaction> r_Trans;
	private boolean isEncrypted = true; 
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Date", "Amount", "Asset", "Recipient", "Title", "Confirmation"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false,false};

	private SortableList<Tuple2<String, String>, Transaction> ss;
	private Account sender;

	private AssetCls asset;

	private byte[] privateKey;

	private byte[] publicKey;
	
	public Accounts_Transactions_TableModel()
	{
		sender = new Account("");
		r_Trans = new ArrayList <Transaction>();
		Controller.getInstance().addWalletListener(this);
//		Controller.getInstance().addObserver(this);
	}
	
	public List<Transaction> getList() {
		return this.r_Trans;
	}
	
	public void repaint(){
		
		this.fireTableDataChanged();
	}
	
	public void set_Account(Account sender){
		
		this.sender = sender;
	}
	
	public void set_Asset(AssetCls asset){
		
		this.asset = asset;
		
	}
	public void set_Encryption( boolean encr){
		this.isEncrypted = encr;
		
		
		
	}
	// читаем колонки которые изменяем высоту	   
		public Boolean[] get_Column_AutoHeight(){
			
			return this.column_AutuHeight;
		}
	// устанавливаем колонки которым изменить высоту	
		public void set_get_Column_AutoHeight( Boolean[] arg0){
			this.column_AutuHeight = arg0;	
		}
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
    }
	
	public Transaction getItem(int row)
	{
		return this.r_Trans.get(row);
	}
	
	@Override
	public int getColumnCount() 
	{
		return this.columnNames.length;
	}
	
	@Override
	public String getColumnName(int index) 
	{
		return this.columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		 return this.r_Trans.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.r_Trans == null || row > this.r_Trans.size() - 1 || this.r_Trans.size() == 0 )
		{
			return null;
		}
		
		Transaction r_Tran = this.r_Trans.get(row);
		
		String str;
		switch(column)
		{
		case COLUMN_TIMESTAMP:
			return r_Tran.viewTimestamp();
		
			
		case COLUMN_AMOUNT:
			if (r_Tran.getType() == Transaction.GENESIS_SEND_ASSET_TRANSACTION)
				return r_Tran.getAmount();
			if (r_Tran.getCreator().getAddress().equals(this.sender.getAddress())) 
				return r_Tran.getAmount().multiply(new BigDecimal(-1));
			return r_Tran.getAmount();
		case COLUMN_ASSET:
			return Controller.getInstance().getAsset(r_Tran.getAbsKey()).toString();
		case COLUMN_RECIPIENT:
			return r_Tran.viewRecipient();
		case COLUMN_CONFIRM:
			return r_Tran.isConfirmed(DBSet.getInstance());
			
		case COLUMN_MESSAGE:
			
			if (r_Tran.getType() != Transaction.SEND_ASSET_TRANSACTION) return Lang.getInstance().translate("Genesis Transaction");
			
			 R_Send rs = ((R_Send) r_Tran);
			 if (rs == rs)return rs.getHead();
			 if (!rs.isEncrypted()) return rs.viewData();
			 if (this.isEncrypted) return rs.viewData();
			 if(!Controller.getInstance().isWalletUnlocked()) return rs.viewData();		
			
			 //IF SENDER ANOTHER
     	//	if(account == null)
 			if (!r_Tran.getCreator().getAddress().equals(this.sender.getAddress()))	
     			
     			
     		{
         		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(rs.getRecipient().getAddress());
     			privateKey = accountRecipient.getPrivateKey();		
     			
     			publicKey = rs.getCreator().getPublicKey();    				
     		}
     		//IF SENDER ME
     		else
     		{
         		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(this.sender.getAddress());
     			privateKey = accountRecipient.getPrivateKey();		
     			
     			publicKey = Controller.getInstance().getPublicKeyByAddress(rs.getRecipient().getAddress());    				
     		}

     		try {
        		byte[] ddd = AEScrypto.dataDecrypt(rs.getData(), privateKey, publicKey);
        		String sss = new String(ddd, "UTF-8");
					String str1 = (new String(AEScrypto.dataDecrypt(rs.getData(), privateKey, publicKey), "UTF-8"));
					return str1; //"{{" +  str.substring(0,R_Send.MAX_DATA_VIEW) + "...}}"); 
				} catch (UnsupportedEncodingException | InvalidCipherTextException e1) {
					return ("unknown password");
				}
			
		}
		
		return null;
	}

	@Override
	public void update(Observable o, Object arg) 
	{	
	//try
	//	{
			this.syncUpdate(o, arg);
	//	}
	//	catch(Exception e)
	//	{
			//GUI ERROR
	//	}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		if( message.getType() == ObserverMessage.WALLET_STATUS )
		{
			//int status = (int) message.getValue();
			
		//	if(status == Wallet.STATUS_LOCKED)
		//	{
				//cryptoCloseAll();
				get_R_Send();
		//	}
		}
		
	//	if( message.getType() == ObserverMessage.NETWORK_STATUS || (int)message.getValue() == Controller.STATUS_OK ) {
	//		this.fireTableDataChanged();
	//	}
		
		//CHECK IF NEW LIST
				if(message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE)
				{
					if(this.r_Trans.size() == 0)
					{
						ss = (SortableList<Tuple2<String, String>, Transaction>) message.getValue();
						ss.registerObserver();
						ss.sort(TransactionMap.TIMESTAMP_INDEX, true);
					//	this.r_Trans.sort(NameMap.NAME_INDEX);
						get_R_Send();
					}
					
					this.fireTableDataChanged();
				}
				
				//CHECK IF LIST UPDATED
				if(message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
				{
					get_R_Send();
					
				}	
		}
	public void get_R_Send(){
		
		
		
		if (this.sender.getAddress()== null || this.asset == null) return;
		this.r_Trans.clear();
		Iterator<Pair<Tuple2<String, String>, Transaction>> s_it = ss.iterator();
		while (s_it.hasNext()){
			Pair<Tuple2<String, String>, Transaction> tt = s_it.next();
			Transaction ttt = tt.getB();
			long absKey; 
			if (ttt.getType() == Transaction.SEND_ASSET_TRANSACTION){
				R_Send tttt = (R_Send) ttt;
				absKey = tttt.getAbsKey(); 
				if (absKey != 0){
					String a = tttt.getCreator().getAddress();
					String b = this.sender.getAddress();
					String c = tttt.getRecipient().getAddress();
					if (tttt.getCreator().getAddress().equals( this.sender.getAddress()) || tttt.getRecipient().getAddress().equals(this.sender.getAddress()))
						if (!this.r_Trans.contains(tttt) && absKey == this.asset.getKey())
							this.r_Trans.add(tttt);
				}
				
			} else if (ttt.getType() == Transaction.GENESIS_SEND_ASSET_TRANSACTION){
				GenesisTransferAssetTransaction ttt1 =  (GenesisTransferAssetTransaction) ttt;
				absKey = ttt1.getAbsKey(); 
				if (absKey != 0){
				//	String a = ttt1.getCreator().getAddress();
					String b = this.sender.getAddress();
					String c = ttt1.getRecipient().getAddress();
					if ( ttt1.getRecipient().getAddress().equals(this.sender.getAddress()))
						if (!this.r_Trans.contains(ttt1) && absKey == this.asset.getKey())
							this.r_Trans.add(ttt1);
				}
			
			
			}
		}
		
		this.fireTableDataChanged();
	}
	
	
	}

