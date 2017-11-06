package gui.items.assets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Observable;
import java.util.Observer;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import datachain.DCSet;
import datachain.SortableList;
import gui.models.TableModelCls;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;
import lang.Lang;

@SuppressWarnings("serial")
public class SellOrdersTableModel extends TableModelCls<BigInteger, Order> implements Observer
{
	public static final int COLUMN_PRICE = 0;
	public static final int COLUMN_AMOUNT = 1;
	public static final int COLUMN_TOTAL = 2;

	public SortableList<BigInteger, Order> orders;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Price", "Amount", "Buying Amount"});
	
	BigDecimal sumAmount;
	BigDecimal sumTotal;
	 
	public SellOrdersTableModel(AssetCls have, AssetCls want)
	{
		Controller.getInstance().addObserver(this);
		this.orders = Controller.getInstance().getOrders(have, want, true);
		
		this.orders.registerObserver();
		
		columnNames[COLUMN_PRICE] += " " + want.getShort();
		columnNames[COLUMN_AMOUNT] += " " + have.getShort();
		columnNames[COLUMN_TOTAL] += " " + want.getShort();
		
		totalCalc();
	}
	
	private void totalCalc()
	{
		sumAmount = BigDecimal.ZERO.setScale(8);
		sumTotal = BigDecimal.ZERO.setScale(8);
		for (Pair<BigInteger, Order> orderPair : this.orders) 	
		{
			sumAmount = sumAmount.add(orderPair.getB().getAmountHaveLeft());
			sumTotal = sumTotal.add(orderPair.getB().getAmountWantLeft());
		}
	}
	
	@Override
	public SortableList<BigInteger, Order> getSortableList() 
	{
		return this.orders;
	}
	
	public Order getOrder(int row)
	{
		return this.orders.get(row).getB();
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
		return this.orders.size() + 1;
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.orders == null || row > this.orders.size() )
		{
			return null;
		}
		
		Order order = null;
		boolean isMine = false;
		if(row < this.orders.size())
		{
			order = this.orders.get(row).getB();
			Controller cntr = Controller.getInstance();
			if(cntr.isAddressIsMine(order.getCreator().getAddress())) {
				isMine = true;
			}
		}
		
		switch(column)
		{
			case COLUMN_PRICE:
				
				if(row == this.orders.size())
					return "<html>"+Lang.getInstance().translate("Total") + ":</html>";
				
				return NumberAsString.getInstance().numberAsString12(order.getPriceCalc());
			
			case COLUMN_AMOUNT:
				
				if(row == this.orders.size())
					return "<html><i>" + NumberAsString.getInstance().numberAsString(sumAmount) + "</i></html>";
				
				
				// It shows unacceptably small amount of red.
				BigDecimal increment = order.calculateBuyIncrement(order, DCSet.getInstance());
				BigDecimal amount = order.getAmountHaveLeft();
				String amountStr = NumberAsString.getInstance().numberAsString(order.getAmountHaveLeft());
				amount = amount.subtract(amount.remainder(increment));
				
				if (amount.compareTo(BigDecimal.ZERO) <= 0)
					amountStr = "<font color=#808080>" + amountStr + "</font>";

				if (isMine)
					amountStr = "<b>" + amountStr + "</b>";
				
				return "<html>" + amountStr + "</html>";
			
			case COLUMN_TOTAL:
	
				if(row == this.orders.size())
					return "<html><i>" + NumberAsString.getInstance().numberAsString(sumTotal) + "</i></html>";
	
				amountStr = NumberAsString.getInstance().numberAsString(order.getAmountWantLeft());

				if (isMine)
					amountStr = "<b>" + amountStr + "</b>";

				return "<html>" + amountStr + "</html>";
					
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
		if(message.getType() == ObserverMessage.ADD_ORDER_TYPE || message.getType() == ObserverMessage.REMOVE_ORDER_TYPE
				|| message.getType() == ObserverMessage.WALLET_ADD_ORDER_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_ORDER_TYPE)
		{
			totalCalc();
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.orders.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
