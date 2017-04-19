package gui.items.mails;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;

import controller.Controller;
import core.account.Account;
import core.transaction.R_Send;
import core.transaction.Transaction;
import utils.DateTimeFormat;
import utils.ObserverMessage;
import database.DBSet;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelMails extends AbstractTableModel implements Observer {
	public static final int COLUMN_CONFIRMATION = 0;
	public static final int COLUMN_DATA = 1;
	public static final int COLUMN_SENDER = 3;
	public static final int COLUMN_RECIEVER = 4;
	public static final int COLUMN_HEAD = 2;
//	public static final int COLUMN_CONFIRM = 5;

	private ArrayList<R_Send> transactions;

	private String[] columnNames = Lang.getInstance()
			.translate(new String[] { "Confirmation", "Date", "Title", "Sender", "Reciever"});//, "Confirm" });
	private Boolean[] column_AutuHeight = new Boolean[] { false, true, true, false };
	boolean incoming;

	public TableModelMails(boolean incoming) {

		this.incoming = incoming;
		transactions = new ArrayList<R_Send>();
		DBSet.getInstance().getTransactionMap().addObserver(this);

	}

	public Class<? extends Object> getColumnClass(int c) { // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
	}

	// читаем колонки которые изменяем высоту
	public Boolean[] get_Column_AutoHeight() {

		return this.column_AutuHeight;
	}

	// устанавливаем колонки которым изменить высоту
	public void set_get_Column_AutoHeight(Boolean[] arg0) {
		this.column_AutuHeight = arg0;
	}

	public Transaction getTransaction(int row) {
		return this.transactions.get(row);
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
		return this.transactions.size();

	}

	@Override
	public Object getValueAt(int row, int column) {
		if (this.transactions == null || row > this.transactions.size() - 1) {
			return null;
		}

		R_Send tran = this.transactions.get(row);

		switch (column) {
		case COLUMN_CONFIRMATION:

			return tran.getConfirmations(DBSet.getInstance());

		case COLUMN_DATA:
			

			return DateTimeFormat.timestamptoString(tran.getTimestamp());

	//	case COLUMN_CONFIRM:

	//		return tran.isConfirmed(DBSet.getInstance());

		case COLUMN_SENDER:

			return tran.getCreator().viewPerson();

		case COLUMN_RECIEVER:

			return tran.getRecipient().viewPerson();

		case COLUMN_HEAD:

			return tran.getHead();

		}

		return null;
	}

	@Override
	public void update(Observable o, Object arg) {
		try {
			this.syncUpdate(o, arg);
		} catch (Exception e) {
			// GUI ERROR
		}
	}

	public synchronized void syncUpdate(Observable o, Object arg) {
		ObserverMessage message = (ObserverMessage) arg;

		// CHECK IF LIST UPDATED
		if (message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE
				|| message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE
				|| message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE) {
			filter(message);
			this.fireTableDataChanged();
		}

	}

	public void removeObservers() {
		DBSet.getInstance().getTransactionMap().deleteObserver(this);
	}

	public void filter(ObserverMessage message) {

		ArrayList<Transaction> all_transactions = new ArrayList<Transaction>();

		

		for (Account account : Controller.getInstance().getAccounts()) {
			all_transactions.addAll(DBSet.getInstance().getTransactionFinalMap()
					.getTransactionsByTypeAndAddress(account.getAddress(), Transaction.SEND_ASSET_TRANSACTION, 0));
		}

		for (Transaction transaction : Controller.getInstance().getUnconfirmedTransactions()) {
			if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
				all_transactions.add(transaction);
			}
		}
		
		
		
		
		for (Transaction messagetx : all_transactions) {
			boolean is = false;
			if (this.transactions.size() != 0){
			for (R_Send message1 : this.transactions) {
				if (Arrays.equals(messagetx.getSignature(), message1.getSignature())) {
					is = true;
					break;
				}
			}
			}
			if (!is) {

				if (messagetx.getAssetKey() == 0) {
					for (Account account1 : Controller.getInstance().getAccounts()) {
						R_Send a = (R_Send) messagetx;
						if (a.getRecipient().getAddress().equals(account1.getAddress()) && incoming) {
							this.transactions.add(a);
						}

						if (a.getCreator().getAddress().equals(account1.getAddress()) && !incoming) {
							this.transactions.add(a);
						}

					}
				}
			}
		}
		
		
		this.transactions.sort(new Comparator<Transaction>(){

			
			
			public int compare(Transaction o1, Transaction o2) {
				// TODO Auto-generated method stub
				
				return  (int) (o2.getTimestamp()- o1.getTimestamp());
			}});
		

	}
}
