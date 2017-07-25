package gui.bank;

import java.awt.List;
import java.awt.TrayIcon.MessageType;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.validation.constraints.Null;

import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import settings.Settings;
import utils.DateTimeFormat;
import utils.ObserverMessage;
import utils.Pair;
import utils.PlaySound;
import utils.SysTray;
import controller.Controller;
import core.account.Account;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.GenesisIssue_ItemRecord;
import core.transaction.GenesisTransferAssetTransaction;
import core.transaction.Issue_ItemRecord;
import core.transaction.R_Send;
import core.transaction.R_SertifyPubKeys;
import core.transaction.Transaction;
import core.transaction.TransactionAmount;
import database.DBSet;
import database.SortableList;
import database.wallet.TransactionMap;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
// in list of records in wallet
public class Payment_Orders_TableModel extends TableModelCls<Tuple2<String, String>, Transaction> implements Observer {
	
	public static final int COLUMN_CONFIRMATIONS = 0;
	public static final int COLUMN_TIMESTAMP = 1;
	public static final int COLUMN_TYPE = 2;
	public static final int COLUMN_CREATOR = 3;
	public static final int COLUMN_ITEM = 4;
	public static final int COLUMN_AMOUNT = 5;
	public static final int COLUMN_RECIPIENT = 6;
	public static final int COLUMN_FEE = 7;
	public static final int COLUMN_SIZE = 8;
	
	private SortableList<Tuple2<String, String>, Transaction> transactions;
	private ArrayList <R_Send>trans;
	//ItemAssetMap dbItemAssetMap;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{
			"Confirmations", "Timestamp", "Type", "Creator", "Item", "Amount", "Recipient", "Fee", "Size"});
	private Boolean[] column_AutuHeight = new Boolean[]{true,true,true,true,true,true,true,false,false};

	static Logger LOGGER = Logger.getLogger(Payment_Orders_TableModel.class.getName());

	public Payment_Orders_TableModel()
	{
		
		addObservers();
		
		//dbItemAssetMap = DBSet.getInstance().getItemAssetMap();
		
	}
	
	@Override
	public SortableList<Tuple2<String, String>, Transaction> getSortableList() {
		return this.transactions;
	}
	
		
	
	public   Object getItem(int row)
	{
		return this.transactions.get(row).getB();
	}
	
	public Class<? extends Object> getColumnClass(int c)
	{     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
    }
	
	public Transaction getTransaction(int row)
	{
		Pair<Tuple2<String, String>, Transaction> data = this.transactions.get(row);
		if (data == null || data.getB() == null) {
			return null;
		}
		return data.getB();
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
		if(this.trans == null)
		{
			return 0;
		}
		
		return this.trans.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		//try 
		//{
			if(this.trans == null || this.trans.size() -1 < row)
			{
				return null;
			}
			
			
			
			Transaction transaction = trans.get(row);
			if (transaction == null)
				return null;
			//creator = transaction.getCreator();
			String itemName = "";
			if (transaction instanceof TransactionAmount && transaction.getAbsKey() >0)
			{
				TransactionAmount transAmo = (TransactionAmount)transaction;
				//recipient = transAmo.getRecipient();
				ItemCls item = DBSet.getInstance().getItemAssetMap().get(transAmo.getAbsKey());
				if (item==null)
					return null;
				
				itemName = item.toString();
			} else if ( transaction instanceof GenesisTransferAssetTransaction)
			{
				GenesisTransferAssetTransaction transGen = (GenesisTransferAssetTransaction)transaction;
				//recipient = transGen.getRecipient();				
				ItemCls item = DBSet.getInstance().getItemAssetMap().get(transGen.getAbsKey());
				if (item==null)
					return null;
				
				itemName = item.toString();
			} else if ( transaction instanceof Issue_ItemRecord)
			{
				Issue_ItemRecord transIssue = (Issue_ItemRecord)transaction;
				ItemCls item = transIssue.getItem();
				if (item==null)
					return null;

				itemName = item.getShort();
			} else if ( transaction instanceof GenesisIssue_ItemRecord)
			{
				GenesisIssue_ItemRecord transIssue = (GenesisIssue_ItemRecord)transaction;
				ItemCls item = transIssue.getItem();
				if (item==null)
					return null;

				itemName = item.getShort();
			} else if (transaction instanceof R_SertifyPubKeys )
			{
				R_SertifyPubKeys sertifyPK = (R_SertifyPubKeys)transaction;
				//recipient = transAmo.getRecipient();
				ItemCls item = DBSet.getInstance().getItemPersonMap().get(sertifyPK.getAbsKey());
				if (item == null)
					return null;
				
				itemName = item.toString();
			} else if (transaction.viewItemName() != null) {
				itemName = transaction.viewItemName();
			}
			
			switch(column)
			{
			case COLUMN_CONFIRMATIONS:
				
				return transaction.getConfirmations(DBSet.getInstance());
				
			case COLUMN_TIMESTAMP:
				
				
				return DateTimeFormat.timestamptoString(transaction.getTimestamp());//.viewTimestamp(); // + " " + transaction.getTimestamp() / 1000;
							
			case COLUMN_TYPE:
				
				return Lang.getInstance().translate(transaction.viewFullTypeName());
				
			case COLUMN_CREATOR:
				
				return transaction.viewCreator();
				
			case COLUMN_ITEM:
				return itemName;

			case COLUMN_AMOUNT:
								
				BigDecimal amo = transaction.getAmount();
				if (amo == null)
					return BigDecimal.ZERO;
				return amo;

			case COLUMN_RECIPIENT:
				
				return transaction.viewRecipient();

			case COLUMN_FEE:
				
				return transaction.getFee();

			case COLUMN_SIZE:
				return transaction.viewSize(false);
			}
			
			return null;
			
		//} catch (Exception e) {
			//GUI ERROR
		//	LOGGER.error(e.getMessage(),e);
		//	return null;
		//}
		
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
	
	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE)
		{
			if(this.transactions == null)
			{
				transactions = (SortableList<Tuple2<String, String>, Transaction>) message.getValue();
				transactions.registerObserver();
				transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);
				read_trans();
				this.fireTableDataChanged();
			}
			
		
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{
			read_trans();
			this.fireTableDataChanged();
		}	

		//
		if(Controller.getInstance().getStatus() == Controller.STATUS_OK && (message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE ||  message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE))
		{		
			if(DBSet.getInstance().getTransactionMap().contains(((Transaction) message.getValue()).getSignature()))
			{
				int type = ((Transaction) message.getValue()).getType(); 
				if( type == Transaction.SEND_ASSET_TRANSACTION)
				{
					R_Send r_Send = (R_Send) message.getValue();
					Account account = Controller.getInstance().getAccountByAddress(((R_Send) message.getValue()).getRecipient().getAddress());	
					if(account != null)
					{
						if(Settings.getInstance().isSoundReceiveMessageEnabled())
						{
							PlaySound.getInstance().playSound("receivemessage.wav", ((Transaction) message.getValue()).getSignature()) ;
						}
						
						SysTray.getInstance().sendMessage("Payment received", "From: " + r_Send.getCreator().getPersonAsString() + "\nTo: " + account.getPersonAsString()
						+ "\n" + "Asset Key" + ": " + r_Send.getAbsKey()
						+ ", " + "Amount" + ": " + r_Send.getAmount().toPlainString(), MessageType.INFO);
					}
					
					else if(Settings.getInstance().isSoundNewTransactionEnabled())
					{
						PlaySound.getInstance().playSound("newtransaction.wav", ((Transaction) message.getValue()).getSignature());
					}
				}
				else if(Settings.getInstance().isSoundNewTransactionEnabled())
				{
					PlaySound.getInstance().playSound("newtransaction.wav", ((Transaction) message.getValue()).getSignature());
				}
			}
		}	

		
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE || message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE
				|| message.getType() == ObserverMessage.LIST_BLOCK_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
	
	private void read_trans() {
		// TODO Auto-generated method stub
		Iterator<Pair<Tuple2<String, String>, Transaction>> it = transactions.iterator();
		trans = new ArrayList<R_Send>();
		while(it.hasNext()){
			 Pair<Tuple2<String, String>, Transaction> tr = it.next();
			Transaction ss = tr.getB();
				if (!( ss instanceof R_Send)) continue;
					String hh = ((R_Send) ss).getHead();
					if (!((R_Send) ss).getHead().equals("SPO")) continue;
					trans.add((R_Send) ss);
								
			
		}
	}

	public void addObservers() 
	{
		
		Controller.getInstance().addWalletListener(this);
	}
	
	
	public void removeObservers() 
	{
	
		Controller.getInstance().deleteObserver(this);
	}
}
