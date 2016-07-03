package gui.models;

import java.awt.TrayIcon.MessageType;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import settings.Settings;
import utils.ObserverMessage;
import utils.Pair;
import utils.PlaySound;
import utils.SysTray;
import controller.Controller;
import core.account.Account;
import core.item.ItemCls;
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
import lang.Lang;

@SuppressWarnings("serial")
// in list of records in wallet
public class WalletTransactionsTableModel extends TableModelCls<Tuple2<String, String>, Transaction> implements Observer {
	
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
	//ItemAssetMap dbItemAssetMap;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{
			"Confirmations", "Timestamp", "Type", "Creator", "Item", "Amount", "Recipient", "Fee", "Size"});

	static Logger LOGGER = Logger.getLogger(WalletTransactionsTableModel.class.getName());

	public WalletTransactionsTableModel()
	{
		Controller.getInstance().addWalletListener(this);
		//dbItemAssetMap = DBSet.getInstance().getItemAssetMap();
		
	}
	
	@Override
	public SortableList<Tuple2<String, String>, Transaction> getSortableList() {
		return this.transactions;
	}
	
	public Class<? extends Object> getColumnClass(int c)
	{     // set column type
		Object o = getValueAt(0, c);
		return o == null? null: o.getClass();
    }
	
	public Transaction getTransaction(int row)
	{
		return transactions.get(row).getB();
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
		if(this.transactions == null)
		{
			return 0;
		}
		
		return this.transactions.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		try 
		{
			if(this.transactions == null || this.transactions.size() -1 < row)
			{
				return null;
			}
			
			Pair<Tuple2<String, String>, Transaction> data = this.transactions.get(row);
			String creator_address = data.getA().a;
			//Account creator = new Account(data.getA().a);
			//Account recipient = null; // = new Account(data.getA().b);
			
			Transaction transaction = data.getB();
			//creator = transaction.getCreator();
			String itemName = "";
			if (transaction instanceof TransactionAmount && transaction.getKey() >=0)
			{
				TransactionAmount transAmo = (TransactionAmount)transaction;
				//recipient = transAmo.getRecipient();
				ItemCls item = DBSet.getInstance().getItemAssetMap().get(transAmo.getKey());
				itemName = item.toString();
			} else if ( transaction instanceof GenesisTransferAssetTransaction)
			{
				GenesisTransferAssetTransaction transGen = (GenesisTransferAssetTransaction)transaction;
				//recipient = transGen.getRecipient();				
				ItemCls item = DBSet.getInstance().getItemAssetMap().get(transGen.getKey());
				itemName = item.toString();
				creator_address = transGen.getRecipient().getAddress();
			} else if ( transaction instanceof Issue_ItemRecord)
			{
				Issue_ItemRecord transIssue = (Issue_ItemRecord)transaction;
				ItemCls item = transIssue.getItem();
				itemName = item.getShort();
			} else if ( transaction instanceof GenesisIssue_ItemRecord)
			{
				GenesisIssue_ItemRecord transIssue = (GenesisIssue_ItemRecord)transaction;
				ItemCls item = transIssue.getItem();
				itemName = item.getShort();
			} else if (transaction instanceof R_SertifyPubKeys )
			{
				R_SertifyPubKeys sertifyPK = (R_SertifyPubKeys)transaction;
				//recipient = transAmo.getRecipient();
				ItemCls item = DBSet.getInstance().getItemPersonMap().get(sertifyPK.getKey());
				itemName = item.toString();
			} else {
				itemName = transaction.viewItemName();
			}
			
			switch(column)
			{
			case COLUMN_CONFIRMATIONS:
				
				return transaction.getConfirmations(DBSet.getInstance());
				
			case COLUMN_TIMESTAMP:
				
				return transaction.viewTimestamp();
				
			case COLUMN_TYPE:
				
				return Lang.getInstance().translate(transaction.viewTypeName());
				
			case COLUMN_CREATOR:
				
				return transaction.viewCreator();
				
			case COLUMN_ITEM:
				return itemName;

			case COLUMN_AMOUNT:
								
				/*
				if (creator_address==null) return "";
				
				return NumberAsString.getInstance().numberAsString(
						transaction.getAmount(creator_address));
						*/
				return transaction.viewAmount(creator_address);

			case COLUMN_RECIPIENT:
				
				return transaction.viewRecipient();

			case COLUMN_FEE:
				
				return transaction.viewFee();			

			case COLUMN_SIZE:
				return transaction.viewSize(false);
			}
			
			return null;
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
			return null;
		}
		
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
				this.transactions = (SortableList<Tuple2<String, String>, Transaction>) message.getValue();
				this.transactions.registerObserver();
				this.transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{
			this.fireTableDataChanged();
		}	
		
		if(Controller.getInstance().getStatus() == Controller.STATUS_OK && message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE)
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
						
						SysTray.getInstance().sendMessage("Payment received", "From: " + r_Send.getCreator().asPerson() + "\nTo: " + account.asPerson()
						+ "\n" + "Asset Key" + ": " + r_Send.getKey()
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
}
