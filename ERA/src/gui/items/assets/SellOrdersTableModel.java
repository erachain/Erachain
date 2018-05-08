package gui.items.assets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import datachain.SortableList;
import gui.models.TableModelCls;
import lang.Lang;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;

@SuppressWarnings("serial")
public class SellOrdersTableModel extends TableModelCls<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> implements Observer
{
	public static final int COLUMN_PRICE = 0;
	public static final int COLUMN_AMOUNT = 1;
	public static final int COLUMN_TOTAL = 2;

	public SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders;

	private String[] columnNames = Lang.getInstance().translate(new String[]{"Price", "Amount", "Total"});

	BigDecimal sumAmount;
	BigDecimal sumTotal;
	private AssetCls have;
	private AssetCls want;

	public SellOrdersTableModel(AssetCls have, AssetCls want)
	{
		this.have = have;
		this.want= want;
		this.orders = Controller.getInstance().getOrders(have, want, true);

		columnNames[COLUMN_PRICE] += " " + want.getShort();
		columnNames[COLUMN_AMOUNT] += " " + have.getShort();
		columnNames[COLUMN_TOTAL] += " " + want.getShort();

		totalCalc();

		Controller.getInstance().addObserver(this);
		//this.orders.registerObserver();

	}

	private void totalCalc()
	{
		sumAmount = BigDecimal.ZERO;
		sumTotal = BigDecimal.ZERO;
		for (Pair<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orderPair : this.orders)
		{

			Tuple3<Long, BigDecimal, BigDecimal> haveItem = orderPair.getB().b;
			BigDecimal amount = haveItem.b.subtract(haveItem.c);
			sumAmount = sumAmount.add(amount);
			sumTotal = sumTotal.add(amount);
		}
	}

	@Override
	public SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getSortableList()
	{
		return this.orders;
	}

	public Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> getOrder(int row)
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

		Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = null;
		boolean isMine = false;
		if(row < this.orders.size())
		{
			order = this.orders.get(row).getB();
			Controller cntr = Controller.getInstance();
			if(cntr.isAddressIsMine(order.a.b)) {
				isMine = true;
			}
		}

		switch(column)
		{
		case COLUMN_PRICE:

			if(row == this.orders.size())
				return "<html><b>"+Lang.getInstance().translate("Total") + ":</b></html>";
			
			return NumberAsString.getInstance().numberAsString12(Order.calcPrice(order.b.b, order.c.b));

		case COLUMN_AMOUNT:

			if(row == this.orders.size())
				return "<html><i>" + NumberAsString.getInstance().numberAsString(sumAmount) + "</i></html>";


			// It shows unacceptably small amount of red.
			BigDecimal amount = order.b.b.subtract(order.b.c);
			String amountStr = NumberAsString.getInstance().numberAsString(amount);
			if (order.a.d)
				return amountStr;
			else
				return "<html><font color=#808080>" + amountStr + "</font></html>";

		case COLUMN_TOTAL:

			if(row == this.orders.size())
				return "<html><i>" + NumberAsString.getInstance().numberAsString(sumTotal) + "</i></html>";

			amountStr = NumberAsString.getInstance().numberAsString(Order.calcAmountWantLeft(order)); // getAmountWantLeft());

			if (isMine)
				amountStr = "<html><b>" + amountStr + "</b></html>";

			return amountStr;

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
			this.orders = Controller.getInstance().getOrders(this.have, want, true);
			//List<Order> items = DCSet.getInstance().getOrderMap().getOrders(have.getKey(), want.getKey(), false);
			totalCalc();
			this.fireTableDataChanged();
		}
	}

	public void removeObservers()
	{
		this.orders.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}

	@Override
	public Object getItem(int k) {
		// TODO Auto-generated method stub
		return this.orders.get(k).getB();
	}
}
