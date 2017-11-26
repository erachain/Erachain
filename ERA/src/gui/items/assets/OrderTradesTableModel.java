package gui.items.assets;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;
import controller.Controller;
import core.item.assets.Order;
import core.item.assets.Trade;
import datachain.DCSet;
import datachain.SortableList;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class OrderTradesTableModel extends TableModelCls<Tuple2<BigInteger, BigInteger>, Trade> implements Observer
{
	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_AMOUNT = 2;
	public static final int COLUMN_PRICE = 3;
	public static final int COLUMN_AMOUNT_WANT = 4;

	private SortableList<Tuple2<BigInteger, BigInteger>, Trade> trades;
	private Order order;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Type", "Amount", "Price", "Total"});
	
	public OrderTradesTableModel(Order order)
	{
		this.order = order;
		this.trades = Controller.getInstance().getTrades(order);
		this.trades.registerObserver();
	}
	
	@Override
	public SortableList<Tuple2<BigInteger, BigInteger>, Trade> getSortableList() 
	{
		return this.trades;
	}
	
	public Trade getTrade(int row)
	{
		return this.trades.get(row).getB();
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
		return this.trades.size();
		
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.trades == null || row > this.trades.size() - 1 )
		{
			return null;
		}
		
		Trade trade = this.trades.get(row).getB();
		int type = 0;
		Order initatorOrder = null;
		Order targetOrder = null;

		if(trade != null) {
			DCSet db = DCSet.getInstance();
			
			initatorOrder = trade.getInitiatorOrder(db); 				
			targetOrder = trade.getTargetOrder(db);
		}

		switch(column)
		{
		case COLUMN_TIMESTAMP:
			
			return DateTimeFormat.timestamptoString(trade.getTimestamp());
			
		case COLUMN_TYPE:
			
			return trade.getInitiatorOrder(DCSet.getInstance()).getHave() == this.order.getHave() ? Lang.getInstance().translate("Buy") : Lang.getInstance().translate("Sell");
				
		case COLUMN_AMOUNT:
			
			String result = NumberAsString.getInstance().numberAsString(trade.getAmountHave());
			
			if (Controller.getInstance().isAddressIsMine(initatorOrder.getCreator().getAddress())) {
				result = "<html><b>" + result + "</b></html>";
			}
			
			return result;
			
		case COLUMN_PRICE:
			
			return NumberAsString.getInstance().numberAsString12(trade.getPriceCalc());

		case COLUMN_AMOUNT_WANT:
			
			result = NumberAsString.getInstance().numberAsString(trade.getAmountWant());
			
			if (Controller.getInstance().isAddressIsMine(targetOrder.getCreator().getAddress())) {
				result = "<html><b>" + result + "</b></html>";
			}
			
			return result;

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
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_TRADE_TYPE || message.getType() == ObserverMessage.REMOVE_TRADE_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.trades.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}

	@Override
	public Object getItem(int k) {
		// TODO Auto-generated method stub
		return this.trades.get(k).getB();
	}
}
