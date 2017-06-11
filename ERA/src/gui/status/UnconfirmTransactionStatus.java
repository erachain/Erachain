package gui.status;

import java.util.Collection;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.transaction.Transaction;
import database.DBSet;
import database.SortableList;
import database.TransactionMap;
import gui.models.Debug_Transactions_Table_Model;
import gui.models.WalletTransactionsTableModel;
import lang.Lang;
import utils.ObserverMessage;
import utils.Pair;

public class UnconfirmTransactionStatus extends JLabel implements Observer {

	
	
	
	private database.wallet.TransactionMap map;

	public UnconfirmTransactionStatus(){
	super("| "+Lang.getInstance().translate("Unconfirmed Records") + ": 0 |");
	map = Controller.getInstance().wallet.database.getTransactionMap();
	map.addObserver(this);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		ObserverMessage message = (ObserverMessage) arg1;
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE || message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{
		Collection<Transaction> col = map.getValues();
		int k=0;
		for (Transaction s:col){
			if (!s.isConfirmed(DBSet.getInstance())) k++;
			
		}
		setText("| "+Lang.getInstance().translate("Unconfirmed Records") + ": " + k +" |");
		}	
		
		
		
	}

	

}
