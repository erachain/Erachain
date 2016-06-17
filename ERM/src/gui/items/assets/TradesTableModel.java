package gui.items.assets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import gui.models.TableModelCls;
import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import core.item.assets.Trade;
import database.DBSet;
import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public class TradesTableModel extends TableModelCls<Tuple2<BigInteger, BigInteger>, Trade> implements Observer
{
	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_ASSET_1 = 2;
	public static final int COLUMN_PRICE = 3;
	public static final int COLUMN_ASSET_2 = 4;

	private SortableList<Tuple2<BigInteger, BigInteger>, Trade> trades;
	private AssetCls have;
	
	BigDecimal sumAsset1;
	BigDecimal sumAsset2;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Type", "Check 1", "Price", "Check 2"});
	
	private void totalCalc()
	{
		sumAsset1 = BigDecimal.ZERO.setScale(8);
		sumAsset2 = BigDecimal.ZERO.setScale(8);
		
		for (Pair<Tuple2<BigInteger, BigInteger>, Trade> tradePair : this.trades) 	
		{
			String type = tradePair.getB().getInitiatorOrder(DBSet.getInstance()).getHave() == this.have.getKey() ? "Sell" : "Buy";

			if(type.equals("Buy"))
			{
				sumAsset1 = sumAsset1.add(tradePair.getB().getAmountHave());
				sumAsset2 = sumAsset2.add(tradePair.getB().getAmountWant());
			}
			else
			{
				sumAsset1 = sumAsset1.add(tradePair.getB().getAmountWant());
				sumAsset2 = sumAsset2.add(tradePair.getB().getAmountHave());
			}
			
		}
	}
	
	public TradesTableModel(AssetCls have, AssetCls want)
	{
		Controller.getInstance().addObserver(this);
		
		this.have = have;
		this.trades = Controller.getInstance().getTrades(have, want);
		this.trades.registerObserver();
		
		this.columnNames[2] = have.getShort();
		
		this.columnNames[4] = want.getShort();
		
		this.columnNames[3] = "Price: " + this.columnNames[4];
		
		totalCalc();
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
		return this.trades.size() + 1;
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.trades == null || row > this.trades.size() )
		{
			return null;
		}
		
		Trade trade = null;
		int type = 0;
		Order initatorOrder = null;
		Order targetOrder = null;
		
		if(row < this.trades.size())
		{
			trade = this.trades.get(row).getB();
			if(trade != null) {
				DBSet db = DBSet.getInstance();
				
				initatorOrder = trade.getInitiatorOrder(db); 				
				targetOrder = trade.getTargetOrder(db); 

				type = initatorOrder.getHave()
						== this.have.getKey()?
								-1:1;

			}
		}
		
		switch(column)
		{
			case COLUMN_TIMESTAMP:
				
				if(row == this.trades.size())
					return "<html>"+Lang.getInstance().translate("Total") + ":</html>";
				
				return DateTimeFormat.timestamptoString(trade.getTimestamp());
				
			case COLUMN_TYPE:
				
				return type == 0? "": type > 0? "Sell" : "Buy";
	
			case COLUMN_ASSET_1:
				
				if(row == this.trades.size())
					return "<html><i>" + NumberAsString.getInstance().numberAsString(sumAsset1) + "</i></html>";

				String result = "";
				if(type > 0)
					result = NumberAsString.getInstance().numberAsString(trade.getAmountHave());
				else
					result = NumberAsString.getInstance().numberAsString(trade.getAmountWant());
				
				if (Controller.getInstance().isAddressIsMine(initatorOrder.getCreator().getAddress())) {
					result = "<html><b>" + result + "</b></html>";
				}
				
				return result;

				
			case COLUMN_PRICE:
				
				if(row == this.trades.size())
					return null;
				
				if(type > 0)
					return NumberAsString.getInstance().numberAsString12(trade.getPriceCalc());
				else
					return NumberAsString.getInstance().numberAsString12(trade.getPriceCalcBack());
			
			case COLUMN_ASSET_2:

				if(row == this.trades.size())
					return "<html><i>" + NumberAsString.getInstance().numberAsString(sumAsset2) + "</i></html>";

				if(type > 0)
					result = NumberAsString.getInstance().numberAsString(trade.getAmountWant());
				else
					result = NumberAsString.getInstance().numberAsString(trade.getAmountHave());

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
			totalCalc();
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.trades.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
