package core.item.assets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.crypto.Base58;
import core.transaction.CancelOrderTransaction;
import core.transaction.Transaction;
import database.DBSet;
import database.SortableList;

public class Order implements Comparable<Order> {
	
	private static final int ID_LENGTH = 64;
	private static final int CREATOR_LENGTH = 25;
	private static final int HAVE_LENGTH = 8;
	private static final int WANT_LENGTH = 8;
	private static final int AMOUNT_LENGTH = 12;
	private static final int FULFILLED_LENGTH = AMOUNT_LENGTH;
	private static final int TIMESTAMP_LENGTH = 8;
	private static final int EXECUTABLE_LENGTH = 1;
	private static final int BASE_LENGTH = ID_LENGTH + CREATOR_LENGTH + HAVE_LENGTH + WANT_LENGTH
			+ 2*AMOUNT_LENGTH + 2*FULFILLED_LENGTH + TIMESTAMP_LENGTH + EXECUTABLE_LENGTH;
	
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
	private boolean isExecutable = true;
	
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
			BigDecimal amountWant, BigDecimal fulfilledHave, BigDecimal fulfilledWant,
			byte isExecutable, long timestamp)
	{
		this.id = id;
		this.creator = creator;
		this.have = have;
		this.want = want;
		this.amountHave = amountHave;//.setScale(8);
		this.amountWant = amountWant;//.setScale(8);
		this.fulfilledHave = fulfilledHave;//.setScale(8);
		this.fulfilledWant = fulfilledWant;//.setScale(8);
		this.isExecutable = isExecutable == 1? true: false;
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
	public boolean isExecutable() {
		return this.isExecutable;
	}
	public void setExecutable(boolean is) {
		this.isExecutable = is;
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
	
	// need for process
	public BigDecimal calcAmountWantLeft(DBSet db)
	{
		BigDecimal temp;
		if (this.amountHave.compareTo(this.amountWant) > 0){
			temp = this.fulfilledHave.multiply(this.getPriceCalc()).setScale(8);
		} else {
			temp = this.fulfilledHave.divide(this.getPriceCalcReverse(), 8, RoundingMode.HALF_DOWN);
		}
		
		if ( !this.isWantDivisible(db) )
			temp = temp.setScale(0,  RoundingMode.HALF_DOWN);

		return this.amountWant.subtract(temp);
	}

	public boolean isFulfilled()
	{
		return this.fulfilledHave.compareTo(this.amountHave) == 0;
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
	public String viewPrice() 
	{
		if(this.amountHave.compareTo(this.amountWant) > 0)
			return ":" + this.amountHave.divide(amountWant, 8, RoundingMode.HALF_UP).toPlainString();

		return "*" + this.amountWant.divide(amountHave, 8, RoundingMode.HALF_DOWN).toPlainString();
		
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
	
	//PARSE/CONVERT
	
	// forDB - use fulFill
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
				
		//READ AMOUNT WANT
		byte[] amountWantBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amountWant = new BigDecimal(new BigInteger(amountWantBytes), 8);
		position += AMOUNT_LENGTH;		

		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
		
		//READ FULFILLED HAVE
		byte[] fulfilledHaveBytes = Arrays.copyOfRange(data, position, position + FULFILLED_LENGTH);
		BigDecimal fulfilledHave = new BigDecimal(new BigInteger(fulfilledHaveBytes), 8);
		position += FULFILLED_LENGTH;

		//READ FULFILLED WANT
		byte[] fulfilledWantBytes = Arrays.copyOfRange(data, position, position + FULFILLED_LENGTH);
		BigDecimal fulfilledWant = new BigDecimal(new BigInteger(fulfilledWantBytes), 8);
		position += FULFILLED_LENGTH;		

		//READ IS EXECUTABLE
		byte[] isExecutableBytes = Arrays.copyOfRange(data, position, position + 1);
		byte isExecutable = isExecutableBytes[0];
		position += 1;

		return new Order(id, creator, have, want, amountHave, amountWant, fulfilledHave, fulfilledWant, isExecutable, timestamp);

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
				
		//WRITE AMOUNT WANT
		byte[] amountWantBytes = this.amountWant.unscaledValue().toByteArray();
		fill = new byte[AMOUNT_LENGTH - amountWantBytes.length];
		amountWantBytes = Bytes.concat(fill, amountWantBytes);
		data = Bytes.concat(data, amountWantBytes);

		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE FULFILLED HAVE
		byte[] fulfilledHaveBytes = this.fulfilledHave.unscaledValue().toByteArray();
		fill = new byte[FULFILLED_LENGTH - fulfilledHaveBytes.length];
		fulfilledHaveBytes = Bytes.concat(fill, fulfilledHaveBytes);
		data = Bytes.concat(data, fulfilledHaveBytes);

		//WRITE FULFILLED WANT
		byte[] fulfilledWantBytes = this.fulfilledWant.unscaledValue().toByteArray();
		fill = new byte[FULFILLED_LENGTH - fulfilledWantBytes.length];
		fulfilledWantBytes = Bytes.concat(fill, fulfilledWantBytes);
		data = Bytes.concat(data, fulfilledWantBytes);
		
		//WRITE IS EXECUTABLE
		byte[] isExecutableBytes = new byte[1];
		isExecutableBytes[0] = this.isExecutable? (byte)1: (byte)0;
		data = Bytes.concat(data, isExecutableBytes);

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
		//TRY AND COMPLETE ORDERS
		boolean completedOrder = false;
		int i = -1;
		BigDecimal thisPrice = this.getPriceCalc();
		BigDecimal thisReversePrice = this.getPriceCalcReverse();
		boolean isReversePrice = thisPrice.compareTo(BigDecimal.ONE) < 0;

		List<Order> orders = db.getOrderMap().getOrders(this.want, this.have);
		//Collections.sort(orders);		

		boolean isDivisibleHave = this.isHaveDivisible(db);
		boolean isDivisibleWant = this.isWantDivisible(db);
		BigDecimal thisAmountHaveLeft = this.getAmountHaveLeft();
		
		while( !completedOrder && ++i < orders.size())
		{
			
			//GET ORDER
			Order order = orders.get(i);
			
			BigDecimal orderAmountHaveLeft;
			BigDecimal orderAmountWantLeft;
			BigDecimal orderReversePrice = order.getPriceCalcReverse();
			BigDecimal orderPrice = order.getPriceCalc();

			Trade trade;
			BigDecimal tradeAmount;
			BigDecimal tradeAmountGet;

			///////////////
			//CHECK IF BUYING PRICE IS HIGHER OR EQUAL THEN OUR SELLING PRICE
			if( isReversePrice?
					thisReversePrice.compareTo(orderPrice) < 0:
					thisPrice.compareTo(orderReversePrice) > 0
					)
				//continue;
				break;
			if ( ! isDivisibleWant && thisAmountHaveLeft.compareTo(orderPrice) < 0)
				// if left not enough for 1 buy by price this order
				continue;
				//break;

			orderAmountHaveLeft = order.getAmountHaveLeft();
							
			// calculate WANT amount left for ORDER
			if (order.getFulfilledHave().compareTo(BigDecimal.ZERO) == 0)
				orderAmountWantLeft = order.getAmountWant();
			else
				// recalv Want
				orderAmountWantLeft = order.calcAmountWantLeft(db); // .getAmountWantLeft();
			
			int compare = orderAmountWantLeft.compareTo(thisAmountHaveLeft); 
			if ( compare >= 0) {
									
				tradeAmountGet = thisAmountHaveLeft;
				if (compare == 0)
					tradeAmount = orderAmountHaveLeft;
				else
				{
					tradeAmount = isReversePrice?
							thisAmountHaveLeft.divide(orderPrice, 8, RoundingMode.HALF_DOWN):
							thisAmountHaveLeft.multiply(orderReversePrice).setScale(8, RoundingMode.HALF_DOWN);
					if ( !isDivisibleWant && tradeAmount.stripTrailingZeros().scale() > 0) {
						// rounding only DOWN !
						tradeAmount = tradeAmount.setScale(0, RoundingMode.DOWN);
						
						if (tradeAmount.compareTo(BigDecimal.ZERO) == 0)
							// not completed yet by this order price
							continue;
						
						// recalc
						tradeAmountGet = isReversePrice?
								tradeAmount.multiply(orderPrice).setScale(8, RoundingMode.HALF_DOWN):
								tradeAmount.divide(orderReversePrice, 8, RoundingMode.HALF_DOWN);
						if ( !isDivisibleHave && tradeAmountGet.stripTrailingZeros().scale() > 0) {
							// wrong trade by non Divisible items
							continue;
						}
					}
				}

				//THIS is COMPLETED
				completedOrder = true;
				
			} else {
				
				if ( !isDivisibleWant && thisAmountHaveLeft.compareTo(orderPrice) < 0)
					// if left not enough for 1 buy by price this order
					break;

				tradeAmount = orderAmountHaveLeft;
				tradeAmountGet = orderAmountWantLeft;
			}				

			//CHECK IF AMOUNT AFTER ROUNDING IS NOT ZERO
			//AND WE CAN BUY ANYTHING
			if(tradeAmount.compareTo(BigDecimal.ZERO) > 0)
			{
				//CREATE TRADE
				trade = new Trade(this.getId(), order.getId(), tradeAmount, tradeAmountGet, transaction.getTimestamp());
				trade.process(db);
				
				// need to update from DB -.copy() not update automative 
				this.fulfilledHave = this.fulfilledHave.add(tradeAmountGet);
				this.fulfilledWant = this.fulfilledWant.add(tradeAmount);
				
				// update new values
				thisAmountHaveLeft = this.getAmountHaveLeft();
				
				// recalc new LEFTS
				// if amountWant id not Divisible
				if ( !completedOrder
						&&
						// if can't trade by more good price than self - by orderOrice - then  auto cancel!
						!isDivisibleWant && thisAmountHaveLeft.compareTo(orderPrice) < 0)
				{
					// cancel order if it not fulfiled isDivisible
					// or HAVE not enough to one WANT  = price
					CancelOrderTransaction.process_it(db, this.copy());
					//and stop resolve
					return;
				}
				
			}
		}
		if (!completedOrder) {
			db.getOrderMap().add(this.copy());
		} else {
			db.getCompletedOrderMap().add(this.copy());			
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
