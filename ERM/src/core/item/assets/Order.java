package core.item.assets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.crypto.Base58;
import core.transaction.CancelOrderTransaction;
import core.transaction.Transaction;
import database.DBSet;

public class Order implements Comparable<Order> {
	
	private static final int ID_LENGTH = 64;
	private static final int CREATOR_LENGTH = 25;
	private static final int HAVE_LENGTH = 8;
	private static final int WANT_LENGTH = 8;
	private static final int AMOUNT_LENGTH = 12;
	private static final int FULFILLED_LENGTH = 12;
	private static final int TIMESTAMP_LENGTH = 8;
	private static final int BASE_LENGTH = ID_LENGTH + CREATOR_LENGTH + HAVE_LENGTH + WANT_LENGTH + 2*AMOUNT_LENGTH + 2*FULFILLED_LENGTH + TIMESTAMP_LENGTH;
	
	private BigInteger id;
	private Account creator;
	private long have;
	private long want;
	private BigDecimal amountHave;
	private BigDecimal fulfilledHave;
	private BigDecimal amountWant;
	private BigDecimal fulfilledWant;
	private long timestamp;
	private int haveDivisible;
	private int wantDivisible;
	
	public Order(BigInteger id, Account creator, long have, long want, BigDecimal amountHave, BigDecimal amountWant, long timestamp)
	{
		this.id = id;
		this.creator = creator;
		this.have = have;
		this.want = want;
		this.amountHave = amountHave.setScale(8);
		this.amountWant = amountWant.setScale(8);
		this.timestamp = timestamp;
		this.fulfilledHave = BigDecimal.ZERO.setScale(8);
		this.fulfilledWant = BigDecimal.ZERO.setScale(8);
	}
	
	public Order(BigInteger id, Account creator, long have, long want, BigDecimal amountHave,
			BigDecimal fulfilledHave, BigDecimal amountWant, BigDecimal fulfilledWant, long timestamp)
	{
		this.id = id;
		this.creator = creator;
		this.have = have;
		this.want = want;
		this.amountHave = amountHave;//.setScale(8);
		this.fulfilledHave = fulfilledHave;//.setScale(8);
		this.amountWant = amountWant;//.setScale(8);
		this.fulfilledWant = fulfilledWant;//.setScale(8);
		this.timestamp = timestamp;
	}
	
	//GETTERS/SETTERS
	
	public BigInteger getId()
	{
		return this.id;
	}
	public void setId(byte[] id)
	{
		this.id = new BigInteger(id);
	}

	public Account getCreator() 
	{
		return this.creator;
	}

	public long getHave() 
	{
		return this.have;
	}
	
	public AssetCls getHaveAsset() 
	{
		return this.getHaveAsset(DBSet.getInstance());
	}
	
	public AssetCls getHaveAsset(DBSet db)
	{
		return (AssetCls)db.getItemAssetMap().get(this.have);
	}

	public long getWant() 
	{
		return this.want;
	}
	
	public AssetCls getWantAsset() 
	{
		return this.getWantAsset(DBSet.getInstance());
	}
	
	public AssetCls getWantAsset(DBSet db)
	{
		return (AssetCls)db.getItemAssetMap().get(this.want);
	}
	
	//////////// DIVISIBLE
	public boolean isHaveDivisible(DBSet db)
	{
		if (this.haveDivisible == 0)
			this.haveDivisible = this.getHaveAsset(db).isDivisible()? 1:-1;
		
		return this.haveDivisible == 1;
	}
	
	public boolean isHaveDivisibleGood(DBSet db, BigDecimal amount)
	{
		if(this.isHaveDivisible(db))
			return true;
		
		return amount.stripTrailingZeros().scale() == 0;
	}

	public boolean isWantDivisible(DBSet db)
	{
		if (this.wantDivisible == 0)
			this.wantDivisible = this.getWantAsset(db).isDivisible()? 1:-1;
		
		return this.wantDivisible == 1;
	}

	public boolean isWantDivisibleGood(DBSet db, BigDecimal amount)
	{
		if(this.isWantDivisible(db))
			return true;
		
		return amount.stripTrailingZeros().scale() == 0;
	}

	///////////////////////// AMOUNTS 
	public BigDecimal getAmountHave()
	{
		return this.amountHave;
	}
	public BigDecimal getAmountWant() 
	{
		return this.amountWant;
	}

	public BigDecimal getAmountHaveLeft()
	{
		return this.amountHave.subtract(this.fulfilledHave);
	}
	public BigDecimal getAmountWantLeft()
	{
		return this.amountWant.subtract(this.fulfilledWant);
	}

	public BigDecimal getAmountHaveLeft(DBSet db, BigDecimal newPrice)
	{
		if (this.isHaveDivisible(db))
			return this.amountHave.subtract(this.fulfilledHave).multiply(newPrice).setScale(8, RoundingMode.HALF_DOWN);
		return this.amountHave.subtract(this.fulfilledHave).multiply(newPrice).setScale(0, RoundingMode.HALF_DOWN);
	}	
	public BigDecimal getAmountWantLeft(DBSet db, BigDecimal newPrice)
	{
		if (this.isWantDivisible(db))
			return this.amountWant.subtract(this.amountWant).multiply(newPrice).setScale(8, RoundingMode.HALF_DOWN);
		return this.amountWant.subtract(this.amountWant).multiply(newPrice).setScale(0, RoundingMode.HALF_DOWN);
	}	

	//////// FULFILLED
	public BigDecimal getFulfilledHave()
	{
		return this.fulfilledHave;
	}
	public BigDecimal getFulfilledWant()
	{
		return this.fulfilledWant;
	}

	public void setFulfilledHave(BigDecimal fulfilled)
	{
		this.fulfilledHave = fulfilled;
	}
	public void setFulfilledWant(BigDecimal fulfilled)
	{
		this.fulfilledWant = fulfilled;
	}

	public boolean isFulfilledHave()
	{
		return this.fulfilledHave.compareTo(this.amountHave) == 0;
	}
	public boolean isFulfilledWant()
	{
		return this.fulfilledWant.compareTo(this.amountWant) == 0;
	}
	public boolean isFulfilled()
	{
		return this.fulfilledHave.compareTo(this.amountHave) == 0
				|| this.fulfilledWant.compareTo(this.amountWant) == 0;
	}

	///////// PRICE
	public BigDecimal getPriceCalc() 
	{
			return this.amountWant.divide(amountHave, 8, RoundingMode.HALF_DOWN);
	}	
	public BigDecimal getPriceCalcReverse() 
	{
			return this.amountHave.divide(amountWant, 8, RoundingMode.HALF_UP);
	}	
		
	public long getTimestamp() 
	{
		return this.timestamp;
	}
			
	public List<Trade> getInitiatedTrades()
	{
		return this.getInitiatedTrades(DBSet.getInstance());
	}
	
	public List<Trade> getInitiatedTrades(DBSet db)
	{
		return db.getTradeMap().getInitiatedTrades(this);
	}
	
	public boolean isConfirmed()
	{
		return DBSet.getInstance().getOrderMap().contains(this.id)
				|| DBSet.getInstance().getCompletedOrderMap().contains(this.id);
	}
	
	// check divisible or try adjust price or amount
	public boolean checkWant(DBSet db) {
		AssetCls wantAsset = this.getWantAsset(db);

		//CHECK IF WANT IS NOT DIVISIBLE
		if(!wantAsset.isDivisible())
		{
			//CHECK IF TOTAL RETURN DOES NOT HAVE ANY DECIMALS
			BigDecimal amountHave = this.amountHave;
			BigDecimal amountWant = this.amountWant;
			BigDecimal mul = amountHave.multiply(amountWant);
			int scale = mul.stripTrailingZeros().scale();
			if(scale > 0)
			{
				if (this.getHaveAsset(db).isDivisible() && scale > 5) {
					// round for divisible HAVE - amountHave or priceWant
					mul = mul.setScale(0, RoundingMode.HALF_DOWN);
					if (amountHave.compareTo(amountWant) < 0) {
						this.amountWant = mul.divide(amountHave, 8, RoundingMode.HALF_UP); 
					} else {
						this.amountHave = mul.divide(amountWant, 8, RoundingMode.HALF_UP);
					}
				} else {
					return false;
				}
			}
		}
		return true;

	}

	//PARSE/CONVERT
	
	public static Order parse(byte[] data) throws Exception
	{
		//CHECK IF CORRECT LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match order length");
		}
		
		int position = 0;
		
		// TODO - ID not need as reference in ASSETS ?? no!

		//READ ID
		byte[] idBytes = Arrays.copyOfRange(data, position, position + ID_LENGTH);
		BigInteger id = new BigInteger(idBytes);
		position += ID_LENGTH;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		Account creator = new Account(Base58.encode(creatorBytes));
		position += CREATOR_LENGTH;
		
		//READ HAVE
		byte[] haveBytes = Arrays.copyOfRange(data, position, position + HAVE_LENGTH);
		long have = Longs.fromByteArray(haveBytes);	
		position += HAVE_LENGTH;
		
		//READ HAVE
		byte[] wantBytes = Arrays.copyOfRange(data, position, position + WANT_LENGTH);
		long want = Longs.fromByteArray(wantBytes);	
		position += WANT_LENGTH;
		
		//READ AMOUNT HAVE
		byte[] amountHaveBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amountHave = new BigDecimal(new BigInteger(amountHaveBytes), 8);
		position += AMOUNT_LENGTH;		
		
		//READ FULFILLED HAVE
		byte[] fulfilledHaveBytes = Arrays.copyOfRange(data, position, position + FULFILLED_LENGTH);
		BigDecimal fulfilledHave = new BigDecimal(new BigInteger(fulfilledHaveBytes), 8);
		position += FULFILLED_LENGTH;		
		
		//READ AMOUNT WANT
		byte[] amountWantBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amountWant = new BigDecimal(new BigInteger(amountWantBytes), 8);
		position += AMOUNT_LENGTH;		

		//READ FULFILLED WANT
		byte[] fulfilledWantBytes = Arrays.copyOfRange(data, position, position + FULFILLED_LENGTH);
		BigDecimal fulfilledWant = new BigDecimal(new BigInteger(fulfilledWantBytes), 8);
		position += FULFILLED_LENGTH;		

		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
		
		return new Order(id, creator, have, want, amountHave, fulfilledHave, amountWant, fulfilledWant, timestamp);
	}
	
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE ID
		byte[] idBytes = this.id.toByteArray();
		byte[] fill = new byte[ID_LENGTH - idBytes.length];
		idBytes = Bytes.concat(fill, idBytes);
		data = Bytes.concat(data, idBytes);
		
		//WRITE CREATOR
		try
		{
			data = Bytes.concat(data , Base58.decode(this.creator.getAddress()));
		}
		catch(Exception e)
		{
			//DECODE EXCEPTION
		}
		
		//WRITE HAVE
		byte[] haveBytes = Longs.toByteArray(this.have);
		haveBytes = Bytes.ensureCapacity(haveBytes, HAVE_LENGTH, 0);
		data = Bytes.concat(data, haveBytes);
		
		//WRITE WANT
		byte[] wantBytes = Longs.toByteArray(this.want);
		wantBytes = Bytes.ensureCapacity(wantBytes, WANT_LENGTH, 0);
		data = Bytes.concat(data, wantBytes);
		
		//WRITE AMOUNT HAVE
		byte[] amountHaveBytes = this.amountHave.unscaledValue().toByteArray();
		fill = new byte[AMOUNT_LENGTH - amountHaveBytes.length];
		amountHaveBytes = Bytes.concat(fill, amountHaveBytes);
		data = Bytes.concat(data, amountHaveBytes);
		
		//WRITE FULFILLED HAVE
		byte[] fulfilledHaveBytes = this.fulfilledHave.unscaledValue().toByteArray();
		fill = new byte[FULFILLED_LENGTH - fulfilledHaveBytes.length];
		fulfilledHaveBytes = Bytes.concat(fill, fulfilledHaveBytes);
		data = Bytes.concat(data, fulfilledHaveBytes);
		
		//WRITE AMOUNT WANT
		byte[] amountWantBytes = this.amountWant.unscaledValue().toByteArray();
		fill = new byte[AMOUNT_LENGTH - amountWantBytes.length];
		amountWantBytes = Bytes.concat(fill, amountWantBytes);
		data = Bytes.concat(data, amountWantBytes);
		
		//WRITE FULFILLED WANT
		byte[] fulfilledWantBytes = this.fulfilledWant.unscaledValue().toByteArray();
		fill = new byte[FULFILLED_LENGTH - fulfilledWantBytes.length];
		fulfilledWantBytes = Bytes.concat(fill, fulfilledWantBytes);
		data = Bytes.concat(data, fulfilledWantBytes);

		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		return data;
	}
	
	public int getDataLength() 
	{
		return BASE_LENGTH;
	}
	
	//PROCESS/ORPHAN

	public void process(DBSet db, Transaction transaction) 
	{
		//REMOVE HAVE
		this.creator.setConfirmedBalance(this.have, this.creator.getConfirmedBalance(this.have, db).subtract(this.amountHave), db);
		
		//ADD ORDER TO DATABASE
		db.getOrderMap().add(this.copy());
		
		//GET ALL ORDERS(WANT, HAVE) LOWEST PRICE FIRST
		List<Order> orders = db.getOrderMap().getOrders(this.want, this.have);
		
		//TRY AND COMPLETE ORDERS
		boolean completedOrder = false;
		int i = -1;
		BigDecimal thisPrice = this.getPriceCalc();
		BigDecimal thisWantPrice = this.getPriceCalcReverse();
		boolean reversePrice = thisPrice.compareTo(BigDecimal.ONE) < 0;
		boolean isDivisibleHave = this.isHaveDivisible(db);
		boolean isDivisibleWant = this.isWantDivisible(db);
		
		while( !completedOrder && ++i < orders.size())
		{
			
			// update new values
			BigDecimal thisAmountHaveLeft = this.getAmountHaveLeft();
			BigDecimal thisAmountWantLeft = this.getAmountWantLeft();

			//GET ORDER
			Order order = orders.get(i);
			BigDecimal tradeAmount;
			BigDecimal tradeAmountGet;
			BigDecimal orderAmountLeft;
			Trade trade;

			if (!reversePrice) {
				
				//CALCULATE BUYING PRICE
				BigDecimal orderWantPrice = order.getPriceCalcReverse();
		
				//CHECK IF BUYING PRICE IS HIGHER OR EQUAL THEN OUR SELLING PRICE
				if(orderWantPrice.compareTo(thisPrice) < 0)
					continue;

				// get price from ORDER

				orderAmountLeft = order.getAmountHaveLeft();
				
				// recalc with order price wanted amount left
				thisAmountWantLeft = thisAmountWantLeft.multiply(orderWantPrice).setScale(8, RoundingMode.HALF_UP);
				
				if (orderAmountLeft.compareTo(thisAmountWantLeft) >= 0) {
					
					if (!this.isHaveDivisibleGood(db, thisAmountWantLeft))
						// amount not good for non divisible HAVE value
						continue;
					
					//RESET COMPLETED
					completedOrder = true;
					tradeAmount = thisAmountWantLeft;
					tradeAmountGet = thisAmountHaveLeft;
				} else {
					
					tradeAmountGet = order.getAmountWantLeft();	
					if (!this.isWantDivisibleGood(db, tradeAmountGet))
						// amount not good for non divisible WANT value
						continue;
					
					tradeAmount = orderAmountLeft;
				}				

			} else {
				// reverse price for accuracy
				
				//CALCULATE BUYING PRICE
				BigDecimal orderPrice = order.getPriceCalc();
		
				//CHECK IF BUYING PRICE IS HIGHER OR EQUAL THEN OUR SELLING PRICE
				if(orderPrice.compareTo(thisWantPrice) > 0)
					continue;

				orderAmountLeft = order.getAmountHaveLeft();

				//// recalc with order price wanted amount left
				////thisWantAmountLeft = this.getWantAmountLeft(db);

				//CALCULATE THE MAXIMUM AMOUNT WE COULD BUY
				if (orderAmountLeft.compareTo(thisAmountWantLeft) >= 0) {
					
					tradeAmountGet = thisAmountWantLeft.multiply(orderPrice).setScale(8, RoundingMode.HALF_UP);
					if (!this.isHaveDivisibleGood(db, tradeAmountGet))
						// amount not good for non divisible WANT value
						continue;

					//RESET COMPLETED
					completedOrder = true;
					// recalc amount for order price
					tradeAmount = thisAmountWantLeft;
				} else {

					tradeAmountGet = order.getAmountWantLeft();			
					if (!this.isHaveDivisibleGood(db, tradeAmountGet))
						// amount not good for non divisible WANT value
						continue;

					tradeAmount = orderAmountLeft;
				}									
			}

			//CHECK IF AMOUNT AFTER ROUNDING IS NOT ZERO
			//AND WE CAN BUY ANYTHING
			if(tradeAmount.compareTo(BigDecimal.ZERO) > 0)
			{
				//CREATE TRADE
				trade = new Trade(this.getId(), order.getId(), tradeAmount, tradeAmountGet, transaction.getTimestamp());
				trade.process(db);
				
				this.fulfilledHave = this.fulfilledHave.add(tradeAmountGet);
				this.fulfilledWant = this.fulfilledWant.add(tradeAmount);
				if (completedOrder 
					&& this.amountHave.subtract(this.fulfilledHave).compareTo(BigDecimal.ZERO) >0) {
					// cancel order if it not fulfiled isDivisible
					CancelOrderTransaction.process_it(db, this);
				}
				
			}
			
			//INCREMENT I
			//i++;
		}	
	}
	
	public void orphan(DBSet db) {
		
		//ORPHAN TRADES
		for(Trade trade: this.getInitiatedTrades(db))
		{
			trade.orphan(db);
		}
		
		//REMOVE ORDER FROM DATABASE
		db.getOrderMap().delete(this);	
		
		//REMOVE HAVE
		this.creator.setConfirmedBalance(this.have, this.creator.getConfirmedBalance(this.have, db).add(this.amountHave), db);
	}
	
	// TODO delete this
	public BigDecimal calculateBuyIncrement(Order order, DBSet db)
	{
		BigInteger multiplier = BigInteger.valueOf(100000000l);
		
		//CALCULATE THE MINIMUM INCREMENT AT WHICH I CAN BUY USING GCD
		BigInteger haveAmount = BigInteger.ONE.multiply(multiplier);
		BigInteger priceAmount = order.getPriceCalc().multiply(new BigDecimal(multiplier))
				.setScale(8, RoundingMode.HALF_DOWN).toBigInteger();
		BigInteger gcd = haveAmount.gcd(priceAmount);
		haveAmount = haveAmount.divide(gcd);
		priceAmount = priceAmount.divide(gcd);
		
		//CALCULATE GCD IN COMBINATION WITH DIVISIBILITY
		if(this.getWantAsset(db).isDivisible())
		{
			haveAmount = haveAmount.multiply(multiplier);
		}
		if(this.getHaveAsset(db).isDivisible())
		{
			priceAmount = priceAmount.multiply(multiplier);
		}
		gcd = haveAmount.gcd(priceAmount);
		
		//CALCULATE THE INCREMENT AT WHICH WE HAVE TO BUY
		BigDecimal increment = new BigDecimal(haveAmount.divide(gcd));
		if(this.getWantAsset(db).isDivisible())
		{
			increment = increment.divide(new BigDecimal(multiplier));
		}
		
		//RETURN
		return increment;
	}

	//COMPARE
	
	@Override
	public int compareTo(Order order) 
	{	
		//COMPARE ONLY BY PRICE
		return this.getPriceCalc().compareTo(order.getPriceCalc());	
	}
	
	//COPY
	
	public Order copy() 
	{	
		try 
		{
			return parse(this.toBytes());
		} 
		catch (Exception e) 
		{
			return null;
		}	
	}
}
