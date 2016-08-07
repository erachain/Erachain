package core.item.assets;
// 16/03
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import core.crypto.Crypto;
import database.DBSet;

public class Trade {
	
	private static final int ORDER_LENGTH = Crypto.SIGNATURE_LENGTH;
	private static final int AMOUNT_LENGTH = 12;
	private static final int TIMESTAMP_LENGTH = 8;
	private static final int BASE_LENGTH = ORDER_LENGTH + ORDER_LENGTH + 2 * AMOUNT_LENGTH + TIMESTAMP_LENGTH;
	
	private BigInteger initiator;
	private BigInteger target;
	private BigDecimal amountHave;
	private BigDecimal amountWant;
	private long timestamp;
	
	// make trading if two orders is seeked  
	public Trade(BigInteger initiator, BigInteger target, BigDecimal amountHave, BigDecimal amountWant, long timestamp)
	{
		this.initiator = initiator;
		this.target = target;
		this.amountHave = amountHave.setScale(8);
		this.amountWant = amountWant.setScale(8);
		this.timestamp = timestamp;
	}

	public BigInteger getInitiator() 
	{
		return this.initiator;
	}
	
	public Order getInitiatorOrder(DBSet db)
	{
		return this.getOrder(this.initiator, db);
	}

	public BigInteger getTarget() 
	{
		return this.target;
	}
	
	public Order getTargetOrder(DBSet db)
	{
		return this.getOrder(this.target, db);
	}
	
	public static Order getOrder(BigInteger key, DBSet db)
	{
		if(db.getOrderMap().contains(key))
		{
			return db.getOrderMap().get(key);
		}
		
		if(db.getCompletedOrderMap().contains(key))
		{
			Order order = db.getCompletedOrderMap().get(key);
			order.setExecutable(false);
			return order;
		}
		
		return null;
		
	}

	public BigDecimal getAmountHave() 
	{
		return this.amountHave;
	}
	public BigDecimal getAmountWant() 
	{
		return this.amountWant;
	}

	public BigDecimal getPriceCalc() 
	{
		return this.amountWant.divide(amountHave, 12, RoundingMode.HALF_DOWN);
	}
	public BigDecimal getPriceCalcBack() 
	{
		return this.amountHave.divide(amountWant, 12, RoundingMode.HALF_UP);
	}
	
	public long getTimestamp()
	{
		return this.timestamp;
	}
	
	//PARSE/CONVERT
	
	public static Trade parse(byte[] data) throws Exception
	{
		//CHECK IF CORRECT LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match trade length");
		}
		
		int position = 0;
		
		//READ INITIATOR
		byte[] initiatorBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
		BigInteger initiator = new BigInteger(initiatorBytes);
		position += ORDER_LENGTH;
		
		//READ TARGET
		byte[] targetBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
		BigInteger target = new BigInteger(targetBytes);
		position += ORDER_LENGTH;
		
		//READ AMOUNT HAVE
		byte[] amountHaveBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amountHave = new BigDecimal(new BigInteger(amountHaveBytes), 8);
		position += AMOUNT_LENGTH;		
		
		//READ AMOUNT WANT
		byte[] amountWantBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amountWant = new BigDecimal(new BigInteger(amountWantBytes), 8);
		position += AMOUNT_LENGTH;		
		
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
		position += TIMESTAMP_LENGTH;	
		
		return new Trade(initiator, target, amountHave, amountWant, timestamp);
	}	
	
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE INITIATOR
		byte[] initiatorBytes = this.initiator.toByteArray();
		byte[] fill = new byte[ORDER_LENGTH - initiatorBytes.length];
		initiatorBytes = Bytes.concat(fill, initiatorBytes);
		data = Bytes.concat(data, initiatorBytes);
		
		//WRITE TARGET
		byte[] targetBytes = this.target.toByteArray();
		fill = new byte[ORDER_LENGTH - targetBytes.length];
		targetBytes = Bytes.concat(fill, targetBytes);
		data = Bytes.concat(data, targetBytes);
		
		//WRITE AMOUNT HAVE
		byte[] amountHaveBytes = this.amountHave.unscaledValue().toByteArray();
		fill = new byte[AMOUNT_LENGTH - amountHaveBytes.length];
		amountHaveBytes = Bytes.concat(fill, amountHaveBytes);
		data = Bytes.concat(data, amountHaveBytes);
		
		//WRITE AMOUNT WANT
		byte[] amountWantBytes = this.amountWant.unscaledValue().toByteArray();
		fill = new byte[AMOUNT_LENGTH - amountWantBytes.length];
		amountWantBytes = Bytes.concat(fill, amountWantBytes);
		data = Bytes.concat(data, amountWantBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		data = Bytes.concat(data, timestampBytes);
		
		return data;
	}
	
	public int getDataLength() 
	{
		return BASE_LENGTH;
	}	
	
	//PROCESS/ORPHAN
	
	public void process(DBSet db)
	{
		Order initiator = this.getInitiatorOrder(db).copy();
		Order target = this.getTargetOrder(db).copy();
			
		//ADD TRADE TO DATABASE
		db.getTradeMap().add(this);
		
		//UPDATE FULFILLED HAVE
		initiator.setFulfilledHave(initiator.getFulfilledHave().add(this.amountWant));
		target.setFulfilledHave(target.getFulfilledHave().add(this.amountHave));

		//UPDATE FULFILLED WANT
		initiator.setFulfilledWant(initiator.getFulfilledWant().add(this.amountHave));
		target.setFulfilledWant(target.getFulfilledWant().add(this.amountWant));

		//CHECK IF FULFILLED
		if(initiator.isFulfilled())
		{
			//REMOVE FROM ORDERS
			db.getOrderMap().delete(initiator);
			
			//ADD TO COMPLETED ORDERS
			db.getCompletedOrderMap().add(initiator);
		}
		else
		{
			//UPDATE ORDER
			// in any case because .copy
			db.getOrderMap().add(initiator);
		}
		
		if(target.isFulfilled())
		{
			//REMOVE FROM ORDERS
			db.getOrderMap().delete(target);
			
			//ADD TO COMPLETED ORDERS
			db.getCompletedOrderMap().add(target);
		}
		else
		{
			//UPDATE ORDER
			db.getOrderMap().add(target);
		}
		
		//TRANSFER FUNDS
		initiator.getCreator().setConfirmedBalance(initiator.getWant(), initiator.getCreator().getConfirmedBalance(initiator.getWant(), db).add(this.amountHave), db);
		target.getCreator().setConfirmedBalance(target.getWant(), target.getCreator().getConfirmedBalance(target.getWant(), db).add(this.amountWant), db);
	}

	public void orphan(DBSet db) 
	{
		Order initiator = this.getInitiatorOrder(db).copy();
		Order target = this.getTargetOrder(db).copy();
		
		//REVERSE FUNDS
		initiator.getCreator().setConfirmedBalance(initiator.getWant(), initiator.getCreator().getConfirmedBalance(initiator.getWant(), db).subtract(this.amountHave), db);
		target.getCreator().setConfirmedBalance(target.getWant(), target.getCreator().getConfirmedBalance(target.getWant(), db).subtract(this.amountWant), db);	
		
		//CHECK IF ORDER IS FULFILLED
		if(initiator.isFulfilled())
		{
			//REMOVE FROM COMPLETED ORDERS
			db.getCompletedOrderMap().delete(initiator);
		}
		if(target.isFulfilled())
		{
			//DELETE TO COMPLETED ORDERS
			db.getCompletedOrderMap().delete(target);
		}
		
		//REVERSE FULFILLED
		initiator.setFulfilledHave(initiator.getFulfilledHave().subtract(this.amountWant));
		target.setFulfilledHave(target.getFulfilledHave().subtract(this.amountHave));
		
		//UPDATE ORDERS
		db.getOrderMap().add(initiator);
		db.getOrderMap().add(target);
		
		//REMOVE FROM DATABASE
		db.getTradeMap().delete(this);
	}
	
	@Override 
	public boolean equals(Object object)
	{
		if(object instanceof Trade)
		{
			Trade trade = (Trade) object;
			
			return (trade.getInitiator().equals(this.getInitiator()) && trade.getTarget().equals(this.getTarget()));
		}
		
		return false;
	}
}
