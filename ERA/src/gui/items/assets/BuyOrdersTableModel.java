package gui.items.assets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import controller.Controller;
import core.BlockChain;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import datachain.SortableList;
import gui.models.TableModelCls;
import lang.Lang;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;

@SuppressWarnings("serial")
public class BuyOrdersTableModel extends TableModelCls<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> implements Observer
{
	public static final int COLUMN_BUYING_PRICE = 0;
	public static final int COLUMN_BUYING_AMOUNT = 1;
	public static final int COLUMN_PRICE = 2;
	public static final int COLUMN_AMOUNT = 3;

	public SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders;

	private String[] columnNames = Lang.getInstance().translate(new String[]{"Buying Price", "Buying Amount", "Price", "Amount"});

	BigDecimal sumAmount;
	BigDecimal sumTotal;
	private AssetCls have;
	private AssetCls want;

	public BuyOrdersTableModel(AssetCls have, AssetCls want)
	{
		this.have = have;
		this.want= want;

		this.orders = Controller.getInstance().getOrders(have, want, true);

		columnNames[COLUMN_BUYING_PRICE] += " " + have.getShort();
		columnNames[COLUMN_BUYING_AMOUNT] += " " + want.getShort();
		columnNames[COLUMN_PRICE] += " " + want.getShort();
		columnNames[COLUMN_AMOUNT] += " " + have.getShort();

		totalCalc();

		Controller.getInstance().addObserver(this);
		//this.orders.registerObserver();

	}

	private void totalCalc()
	{
		sumAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		sumTotal = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
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
		Pair<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> rec = this.orders.get(row);
		if (rec == null)
			return null;

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
			if (order != null) {
				Controller cntr = Controller.getInstance();
				if(cntr.isAddressIsMine(order.a.b)) {
					isMine = true;
				}
			}
		}

		switch(column)
		{
		case COLUMN_BUYING_PRICE:

			if(row == this.orders.size())
				return "<html>Total:</html>";


			return NumberAsString.getInstance().numberAsString12(Order.calcPrice(order.b.b, order.c.b));

		case COLUMN_BUYING_AMOUNT:

			if(row == this.orders.size())
				return "<html><i>" + NumberAsString.getInstance().numberAsString(sumAmount) + "</i></html>";

			if (isMine)
				return "<html><b>" + NumberAsString.getInstance().numberAsString(Order.calcAmountWantLeft(order)) + "</b></html>";

			return NumberAsString.getInstance().numberAsString(Order.calcAmountWantLeft(order));

		case COLUMN_PRICE:

			if(row == this.orders.size())
				return "";

			if (isMine)
				return "<html><b>" + NumberAsString.getInstance().numberAsString12(Order.calcPrice(order.b.b, order.c.b));
			return NumberAsString.getInstance().numberAsString12(Order.calcPrice(order.b.b, order.c.b));

		case COLUMN_AMOUNT:

			if(row == this.orders.size())
				return "<html><i>" + NumberAsString.getInstance().numberAsString(sumTotal) + "</i></html>";

			// It shows unacceptably small amount of red.
			//BigDecimal increment = order.calculateBuyIncrement();
			BigDecimal amount = order.b.c;
			String amountStr = NumberAsString.getInstance().numberAsString(amount);
			//amount = amount.subtract(amount.remainder(increment));

			//if (amount.compareTo(BigDecimal.ZERO) <= 0)
			if (order.a.d)
				return "<html><font color=#808080>" + amountStr + "</font></html>";
			else
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
			this.orders = Controller.getInstance().getOrders(have, want, true);
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
		return  this.orders.get(k).getB();
	}
}
