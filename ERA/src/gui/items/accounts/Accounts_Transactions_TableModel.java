package gui.items.accounts;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
public class Accounts_Transactions_TableModel extends AbstractTableModel implements Observer {
	public static final int COLUMN_TIMESTAMP = 0;

	public static final int COLUMN_BLOCK = 1;
	public static final int COLUMN_TRANSACTION = 2;
	public static final int COLUMN_AMOUNT = 3;
	public static final int COLUMN_ASSET = 4;
	public static final int COLUMN_TYPE = 5;
	public static final int COLUMN_SENDER = 6;
	public static final int COLUMN_RECIPIENT = 7;
	public static final int COLUMN_MESSAGE = 8;
	public static final int COLUMN_CONFIRM = 9;

	private List<Transaction> r_Trans;
	private HashMap<String, Trans> trans_Hash_Map;
	private Object[] trans_List;
	private boolean isEncrypted = true;

	private String[] columnNames = Lang.getInstance().translate(new String[] { "Date", "Block", "RecNo", "Amount",
			"Asset", "Type", "Sender", "Recipient", "Title", "Confirmation" });
	private Boolean[] column_AutuHeight = new Boolean[] { false, true, true, false, false };

	private SortableList<Tuple2<String, String>, Transaction> ss;
	private Account sender;

	private AssetCls asset;

	private byte[] privateKey;

	private byte[] publicKey;

	public Accounts_Transactions_TableModel() {
		sender = new Account("");
		// trans_List = new ArrayList<Trans>();

		r_Trans = new ArrayList<Transaction>();
		Controller.getInstance().addWalletListener(this);
		// Controller.getInstance().addObserver(this);
	}

	public List<Transaction> getList() {
		return this.r_Trans;
	}

	public void repaint() {

		this.fireTableDataChanged();
	}

	public void set_Account(Account sender) {

		this.sender = sender;
	}

	public void set_Asset(AssetCls asset) {

		this.asset = asset;

	}

	public void set_Encryption(boolean encr) {
		this.isEncrypted = encr;

	}

	// читаем колонки которые изменяем высоту
	public Boolean[] get_Column_AutoHeight() {

		return this.column_AutuHeight;
	}

	// устанавливаем колонки которым изменить высоту
	public void set_get_Column_AutoHeight(Boolean[] arg0) {
		this.column_AutuHeight = arg0;
	}

	public Class<? extends Object> getColumnClass(int c) { // set column type
		Object o = getValueAt(0, c);
		return o == null ? Null.class : o.getClass();
	}

	public Transaction getItem(int row) {
		return this.r_Trans.get(row);
	}

	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public String getColumnName(int index) {
		return this.columnNames[index];
	}

	@Override
	public int getRowCount() {
		// return this.r_Trans.size();
		if (trans_List == null || trans_List.length ==0)
			return 0;
		return trans_List.length;
	}
	
	@SuppressWarnings("null")
	private BigDecimal get_summ(){
		BigDecimal res = new BigDecimal("0");
		for(int i =0; i<trans_List.length; i++){
	
		
		res=res.add(((Trans)trans_List[i]).ammount);
		}
		return res;
		
	}

	@Override
	public Object getValueAt(int row, int column) {
		// if(this.r_Trans == null || row > this.r_Trans.size() - 1 ||
		// this.r_Trans.size() == 0 )
		// {
		// return null;
		// }
		// Transaction r_Tran = this.r_Trans.get(row);
		
		
		
		if (this.trans_List == null || this.trans_List.length == 0) {
			return null;
		}
		// summa
/*		if (row == this.trans_List.length){
			
			switch (column){
			case COLUMN_TIMESTAMP:
				return Lang.getInstance().translate("Total")+":";
			case COLUMN_AMOUNT:
				return get_summ();
			
			}
			
			
			
			return null;
		}
		*/
		// fill table
		
		Trans r_Tran = (Trans) trans_List[row];

		String str;
		switch (column) {
		case COLUMN_TIMESTAMP:
			if (r_Tran.transaction.getTimestamp() == 0)
				return "---";
			return r_Tran.transaction.viewTimestamp();
		case COLUMN_BLOCK:

			if (r_Tran.transaction.isConfirmed(DBSet.getInstance())) return r_Tran.transaction.getBlock(DBSet.getInstance()).getHeight(DBSet.getInstance());
			return -1;
		case COLUMN_TRANSACTION:

			if (r_Tran.transaction.isConfirmed(DBSet.getInstance())) return r_Tran.transaction.getSeqNo(DBSet.getInstance());
			return -1;
		case COLUMN_AMOUNT:
		//	if (r_Tran.transaction.getType() == Transaction.GENESIS_SEND_ASSET_TRANSACTION)
				return r_Tran.ammount;
		case COLUMN_ASSET:
			return Controller.getInstance().getAsset(r_Tran.transaction.getAbsKey()).toString();
			
		case COLUMN_TYPE:
			
			return r_Tran.transaction.viewTypeName();
		case COLUMN_RECIPIENT:
			return r_Tran.transaction.viewRecipient();
		case COLUMN_SENDER:
			if (r_Tran.owner == null)
				return "GENESIS";
			return r_Tran.transaction.viewCreator();

		case COLUMN_CONFIRM:
			return r_Tran.transaction.isConfirmed(DBSet.getInstance());

		case COLUMN_MESSAGE:

			if (r_Tran.transaction.getType() != Transaction.SEND_ASSET_TRANSACTION)
				return Lang.getInstance().translate("Genesis Transaction");

			R_Send rs = ((R_Send) r_Tran.transaction);
			if (rs == rs)
				return rs.getHead();
			if (!rs.isEncrypted())
				return rs.viewData();
			if (this.isEncrypted)
				return rs.viewData();
			if (!Controller.getInstance().isWalletUnlocked())
				return rs.viewData();

			// IF SENDER ANOTHER
			// if(account == null)
			if (!r_Tran.transaction.getCreator().getAddress().equals(this.sender.getAddress()))

			{
				PrivateKeyAccount accountRecipient = Controller.getInstance()
						.getPrivateKeyAccountByAddress(rs.getRecipient().getAddress());
				privateKey = accountRecipient.getPrivateKey();

				publicKey = rs.getCreator().getPublicKey();
			}
			// IF SENDER ME
			else {
				PrivateKeyAccount accountRecipient = Controller.getInstance()
						.getPrivateKeyAccountByAddress(this.sender.getAddress());
				privateKey = accountRecipient.getPrivateKey();

				publicKey = Controller.getInstance().getPublicKeyByAddress(rs.getRecipient().getAddress());
			}

			try {
				byte[] ddd = AEScrypto.dataDecrypt(rs.getData(), privateKey, publicKey);
				String sss = new String(ddd, "UTF-8");
				String str1 = (new String(AEScrypto.dataDecrypt(rs.getData(), privateKey, publicKey), "UTF-8"));
				return str1; // "{{" + str.substring(0,R_Send.MAX_DATA_VIEW) +
								// "...}}");
			} catch (UnsupportedEncodingException | InvalidCipherTextException e1) {
				return ("unknown password");
			}

		}

		return null;
	}

	@Override
	public void update(Observable o, Object arg) {
		// try
		// {
		this.syncUpdate(o, arg);
		// }
		// catch(Exception e)
		// {
		// GUI ERROR
		// }
	}

	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg) {
		ObserverMessage message = (ObserverMessage) arg;

		if (message.getType() == ObserverMessage.WALLET_STATUS) {
			// int status = (int) message.getValue();

			// if(status == Wallet.STATUS_LOCKED)
			// {
			// cryptoCloseAll();
			get_R_Send();
			// }
		}

		// if( message.getType() == ObserverMessage.NETWORK_STATUS ||
		// (int)message.getValue() == Controller.STATUS_OK ) {
		// this.fireTableDataChanged();
		// }

		// CHECK IF NEW LIST
		if (message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE) {
			if (this.r_Trans.size() == 0) {
				ss = (SortableList<Tuple2<String, String>, Transaction>) message.getValue();
				ss.registerObserver();
				ss.sort(TransactionMap.TIMESTAMP_INDEX, true);
				// this.r_Trans.sort(NameMap.NAME_INDEX);
				get_R_Send();
			}

			this.fireTableDataChanged();
		}

		// CHECK IF LIST UPDATED
		if (message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE) {
			//get_R_Send();
			trans_Parse((Transaction) message.getValue());

		}
		// CHECK IF LIST UPDATED
				if ( message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE) {
					//get_R_Send();
					Object sss = message.getValue();
				
		trans_Hash_Map.remove((Transaction) message.getValue());

				}
		
		
		
	}

	public void get_R_Send() {

		if (this.sender.getAddress() == null || this.asset == null)
			return;
		this.r_Trans.clear();
		trans_Hash_Map = new HashMap<String, Trans>();
		trans_List = null;
		Iterator<Pair<Tuple2<String, String>, Transaction>> s_it = ss.iterator();
		while (s_it.hasNext()) {
			Pair<Tuple2<String, String>, Transaction> tt = s_it.next();
			Transaction ttt = tt.getB();
			trans_Parse(ttt);
		}

		trans_List = trans_Hash_Map.values().toArray();

		this.fireTableDataChanged();
	}
	
	private void trans_Parse(Transaction ttt){
	
	
	
	
	if (ttt.getAbsKey() != this.asset.getKey()) return;
	
	Trans trr = new Trans();
	if (ttt.getType() == Transaction.SEND_ASSET_TRANSACTION) {
		R_Send tttt = (R_Send) ttt;
		if (!this.sender.getAddress().equals(tttt.getCreator().getAddress()) 
				&& !this.sender.getAddress().equals(tttt.getRecipient().getAddress()))	return ;
			trr.owner = tttt.getCreator();
			trr.recipient = tttt.getRecipient();
			trr.transaction = tttt;
			trr.ammount = tttt.getAmount();
//if send for *-1
			if (tttt.getCreator().getAddress().equals(this.sender.getAddress()))
				trr.ammount = tttt.getAmount().multiply(new BigDecimal("-1"));

		
			trans_Hash_Map.put(ttt.viewSignature(), trr);		
		
		
	} 
	else if (ttt.getType() == Transaction.GENESIS_SEND_ASSET_TRANSACTION) {
		GenesisTransferAssetTransaction ttt1 = (GenesisTransferAssetTransaction) ttt;
		
			// String a = ttt1.getCreator().getAddress();
		
		String cr = "";
		if (ttt1.getCreator() != null) cr = ttt1.getCreator().getAddress();
		// if is owner
		String own = "";
		if (ttt1.getOwner() != null) own = ttt1.getOwner().getAddress();
		
		if ( this.sender.getAddress().equals(cr) ||
				this.sender.getAddress().equals(own) ||
				this.sender.getAddress().equals(ttt1.getRecipient().getAddress()))	{
			
			trr.transaction = ttt1;
			trr.ammount = ttt1.getAmount();
			// if send
			
			
			
			if (!ttt1.getRecipient().getAddress().equals(this.sender.getAddress()))	
					trr.ammount = ttt1.getAmount().multiply(new BigDecimal("-1"));
			// if is creator
			if (ttt1.getCreator() != null) trr.owner = ttt1.getCreator();
			// if is owner
			if (ttt1.getOwner() != null) trr.owner = ttt1.getOwner();
			trr.recipient = ttt1.getRecipient();
				
								

		
			trans_Hash_Map.put(ttt.viewSignature(), trr);	
		}
	}
				
}
	

	class Trans {
		public BigDecimal ammount;
		public Account owner;
		public Account recipient;
		public Transaction transaction;

	}

}
