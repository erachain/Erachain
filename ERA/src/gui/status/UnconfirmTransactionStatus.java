package gui.status;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import gui.items.records.Records_My_SplitPanel;
import gui.models.Debug_Transactions_Table_Model;
import gui.models.WalletTransactionsTableModel;
import gui2.Main_Panel;
import lang.Lang;
import utils.ObserverMessage;
import utils.Pair;

public class UnconfirmTransactionStatus extends JLabel implements Observer {

	
	
	
	private database.wallet.TransactionMap map;
	private int k;

	public UnconfirmTransactionStatus(){
	super("| "+Lang.getInstance().translate("Unconfirmed Records") + ": 0 |");
	
	 k=0;
	map = Controller.getInstance().wallet.database.getTransactionMap();
	map.addObserver(this);
	this.addMouseListener(new MouseListener(){

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			if (k == 0) return;
		Main_Panel.getInstance().ccase1( Lang.getInstance().translate("My Records"), Records_My_SplitPanel.getInstance());
		
		}
		
		
	});
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		ObserverMessage message = (ObserverMessage) arg1;
		k=0;
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE || message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{
		Collection<Transaction> col = map.getValues();
		
		for (Transaction s:col){
			if (!s.isConfirmed(DBSet.getInstance())) k++;
			
		}
		
		if (k > 0){
			this.setCursor(new Cursor(Cursor.HAND_CURSOR));
			setText("<HTML>| <A href = ' '>"+Lang.getInstance().translate("Unconfirmed Records") + ": " + k +"</a> |");
			return;
		}
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		setText("| "+Lang.getInstance().translate("Unconfirmed Records") + ": " + k +" |");
		
		}	
		
		
		
	}

	

}
