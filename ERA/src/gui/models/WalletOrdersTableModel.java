package gui.models;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;

import javax.validation.constraints.Null;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import controller.Controller;
import core.item.assets.Order;
import datachain.SortableList;
import lang.Lang;
import utils.DateTimeFormat;
import utils.ObserverMessage;

@SuppressWarnings("serial")
public class WalletOrdersTableModel extends TableModelCls<Tuple2<String, BigInteger>, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> implements Observer
{
	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_HAVE = 1;
	public static final int COLUMN_WANT = 2;
	public static final int COLUMN_AMOUNT = 3;
	public static final int COLUMN_PRICE = 4;
	public static final int COLUMN_FULFILLED = 5;
	public static final int COLUMN_CREATOR = 6;
	public static final int COLUMN_CONFIRMED = 7;
	public static final int COLUMN_DONE = 8;

	private SortableList<Tuple2<String, BigInteger>, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders;

	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Have", "Want", "Amount", "Price", "Fulfilled", "Creator", "Confirmed", "DONE"});

	public WalletOrdersTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}

	@Override
	public SortableList<Tuple2<String, BigInteger>, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getSortableList() {
		return this.orders;
	}

	@Override
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
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
		//	 return this.orders.size();
		return (this.orders == null)? 0 : this.orders.size();

	}

	@Override
	public Object getValueAt(int row, int column)
	{
		if(this.orders == null || row > this.orders.size() - 1 )
		{
			return null;
		}

		Order order = Order.fromDBrec(this.orders.get(row).getB());


		switch(column)
		{
		case COLUMN_TIMESTAMP:

			return DateTimeFormat.timestamptoString(order.getTimestamp());

		case COLUMN_HAVE:

			return order.getHaveAsset().getShort();

		case COLUMN_WANT:

			return order.getWantAsset().getShort();

		case COLUMN_AMOUNT:

			return order.getAmountHave().toPlainString();

		case COLUMN_PRICE:

			return order.viewPrice();

		case COLUMN_FULFILLED:

			return order.getFulfilledHave().toPlainString();
			//return order.getFulfilledHave().toPlainString();

		case COLUMN_CREATOR:

			return order.getCreator().getPersonAsString();

		case COLUMN_CONFIRMED:

			return order.isConfirmed();

		case COLUMN_DONE:

			if (order.isExecutable())
				//if (DBSet.getInstance().getOrderMap().contains(order.getId()))
				return "";
			return "++";

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

	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;

		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_ORDER_TYPE || message.getType() == ObserverMessage.WALLET_LIST_ORDER_TYPE)
		{
			if(this.orders == null)
			{
				this.orders = (SortableList<Tuple2<String, BigInteger>, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
						Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>) message.getValue();
				this.orders.registerObserver();
				//this.assets.sort(PollMap.NAME_INDEX);
			}

			this.fireTableDataChanged();
		}

		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_ORDER_TYPE || message.getType() == ObserverMessage.REMOVE_ORDER_TYPE
				|| message.getType() == ObserverMessage.WALLET_ADD_ORDER_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_ORDER_TYPE)
		{
			this.fireTableDataChanged();
		}
	}

	public void removeObservers()
	{
		this.orders.removeObserver();
		Controller.getInstance().deleteWalletObserver(this);
	}

	@Override
	public Object getItem(int k) {
		// TODO Auto-generated method stub
		return this.orders.get(k).getB();
	}
}
