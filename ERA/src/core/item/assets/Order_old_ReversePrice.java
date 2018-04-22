package core.item.assets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;

import core.BlockChain;
import core.account.Account;
import core.crypto.Crypto;
import core.transaction.CancelOrderTransaction;
import core.transaction.Transaction;
import datachain.DCSet;

public class Order_old_ReversePrice implements Comparable<Order_old_ReversePrice>
{

	private static final MathContext rounding = new java.math.MathContext(12, RoundingMode.HALF_DOWN);

	private static final int ID_LENGTH = Crypto.SIGNATURE_LENGTH;
	private static final int CREATOR_LENGTH = 25;
	private static final int HAVE_LENGTH = 8;
	private static final int WANT_LENGTH = 8;
	private static final int AMOUNT_LENGTH = 12;
	private static final int FULFILLED_LENGTH = AMOUNT_LENGTH;
	private static final int TIMESTAMP_LENGTH = 8;
	private static final int EXECUTABLE_LENGTH = 1;
	private static final int BASE_LENGTH = ID_LENGTH + CREATOR_LENGTH + HAVE_LENGTH + WANT_LENGTH
			+ 2*AMOUNT_LENGTH + 2*FULFILLED_LENGTH + TIMESTAMP_LENGTH + EXECUTABLE_LENGTH;

	protected DCSet dcSet;
	private BigInteger id;
	private Account creator;
	private long have;
	private long want;
	private BigDecimal amountHave;
	private BigDecimal fulfilledHave;
	private BigDecimal amountWant;
	//private BigDecimal fulfilledWant;
	protected long timestamp;
	private boolean isExecutable = true;

	public Order_old_ReversePrice(BigInteger id, Account creator, long have, long want, BigDecimal amountHave, BigDecimal amountWant, long timestamp)
	{
		this.id = id;
		this.creator = creator;
		this.have = have;
		this.want = want;

		// SCALE to BASE
		int different_scale = amountHave.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
		if (different_scale != 0) {
			amountHave = amountHave.scaleByPowerOfTen(different_scale);
		}
		this.amountHave = amountHave;

		different_scale = amountWant.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
		if (different_scale != 0) {
			amountWant = amountWant.scaleByPowerOfTen(different_scale);
		}
		this.amountWant = amountWant;

		this.fulfilledHave = BigDecimal.ZERO;//.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		//this.fulfilledWant = BigDecimal.ZERO;//.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

		this.timestamp = timestamp;
	}

	public Order_old_ReversePrice(BigInteger id, Account creator, long have, long want, BigDecimal amountHave,
			BigDecimal amountWant, BigDecimal fulfilledHave,
			byte isExecutable, long timestamp)
	{
		this.id = id;
		this.creator = creator;
		this.have = have;
		this.want = want;

		// SCALE to BASE
		int different_scale = amountHave.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
		if (different_scale != 0) {
			amountHave = amountHave.scaleByPowerOfTen(different_scale);
		}
		this.amountHave = amountHave;

		different_scale = amountWant.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
		if (different_scale != 0) {
			amountWant = amountWant.scaleByPowerOfTen(different_scale);
		}
		this.amountWant = amountWant;

		this.fulfilledHave = fulfilledHave;
		//this.fulfilledWant = fulfilledWant;

		this.isExecutable = isExecutable == 1? true: false;
		this.timestamp = timestamp;
	}

	//GETTERS/SETTERS

	public void setDC(DCSet dcSet) {
		this.dcSet = dcSet;
	}

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
		return this.getHaveAsset(this.dcSet);
	}

	public AssetCls getHaveAsset(DCSet db)
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
		return this.getWantAsset(this.dcSet);
	}

	public AssetCls getWantAsset(DCSet db)
	{
		return (AssetCls)db.getItemAssetMap().get(this.want);
	}

	/*
	//////////// DIVISIBLE
	public boolean isHaveDivisible(DCSet db)
	{
		if (this.haveDivisible == 0)
			this.haveDivisible = this.getHaveAsset(db).isDivisible()? 1:-1;

		return this.haveDivisible == 1;
	}

	public boolean isHaveDivisibleGood(DCSet db, BigDecimal amount)
	{
		if(this.isHaveDivisible(db))
			return true;

		return amount.stripTrailingZeros().scale() == 0;
	}

	public boolean isWantDivisible(DCSet db)
	{
		if (this.wantDivisible == 0)
			this.wantDivisible = this.getWantAsset(db).isDivisible()? 1:-1;

		return this.wantDivisible == 1;
	}

	public boolean isWantDivisibleGood(DCSet db, BigDecimal amount)
	{
		if(this.isWantDivisible(db))
			return true;

		return amount.stripTrailingZeros().scale() == 0;
	}
	 */

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
		return this.getAmountHaveLeft().multiply(getPriceCalc(), rounding);
	}

	/*
	public BigDecimal getAmountHaveLeft(DBSet db, BigDecimal newPrice)
	{
		if (this.isHaveDivisible(db))
			return this.amountHave.subtract(this.fulfilledHave).multiply(newPrice, rounding).setScale(BlockChain.AMOUNT_DEDAULT_SCALE, RoundingMode.HALF_DOWN);
		return this.amountHave.subtract(this.fulfilledHave).multiply(newPrice, rounding).setScale(0, RoundingMode.HALF_DOWN);
	}

	 */

	public static BigDecimal getAmountWantLeft(Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> orderREC)
	{
		//		if (true) //this.isWantDivisible(db))
		return orderREC.b.c.multiply(getPriceCalc2(orderREC.b.b, orderREC.b.c)).setScale(orderREC.c.b.scale(), RoundingMode.HALF_DOWN);
		//return this.amountWant.subtract(this.amountWant).multiply(newPrice, rounding).setScale(0, RoundingMode.HALF_DOWN);
	}

	//////// FULFILLED
	public BigDecimal getFulfilledHave()
	{
		return this.fulfilledHave;
	}
	/*
	public BigDecimal getFulfilledWant()
	{
		return this.fulfilledWant;
	}
	 */

	public void setFulfilledHave(BigDecimal fulfilled)
	{
		this.fulfilledHave = fulfilled;
	}
	/*
	public void setFulfilledWant(BigDecimal fulfilled)
	{
		this.fulfilledWant = fulfilled;
	}
	 */

	// need for process
	public BigDecimal calcAmountWantLeft(DCSet db)
	{
		BigDecimal temp;
		if (this.amountHave.compareTo(this.amountWant) > 0) {
			temp = this.fulfilledHave.multiply(this.getPriceCalc(), rounding).setScale(this.amountWant.scale(), RoundingMode.HALF_DOWN);
		} else {
			temp = this.fulfilledHave.divide(this.getPriceCalcReverse(), this.amountWant.scale(), RoundingMode.HALF_DOWN);
		}

		/*
		if ( !this.isWantDivisible(db) )
			temp = temp.setScale(0,  RoundingMode.HALF_DOWN);
		 */

		return this.amountWant.subtract(temp);
	}

	public boolean isFulfilled()
	{
		return this.fulfilledHave.compareTo(this.amountHave) == 0;
	}

	///////// PRICE
	/*
	public int getScaleForPrice()
	{
		return this.amountWant.scale();
	}

	public int getScaleForPriceReverse()
	{
		return this.amountHave.scale();
	}
	 */

	///////// PRICE
	public BigDecimal getPriceCalc()
	{
		//return this.amountWant.divide(amountHave, getScaleForPrice(), RoundingMode.HALF_DOWN);
		// PRICE all > 1
		return this.amountWant.divide(this.amountHave,
				this.amountWant.scale() + BlockChain.AMOUNT_DEDAULT_SCALE
				+ this.amountHave.precision(), RoundingMode.HALF_DOWN);
	}

	public static BigDecimal getPriceCalc2(BigDecimal amountHave, BigDecimal amountWant)
	{
		//return this.amountWant.divide(amountHave, getScaleForPrice(), RoundingMode.HALF_DOWN);
		// PRICE all > 1
		return amountWant.divide(amountHave, 6, RoundingMode.HALF_DOWN);
	}

	public BigDecimal getPriceCalcReverse()
	{
		//return this.amountHave.divide(amountWant, getScaleForPriceReverse(), RoundingMode.HALF_UP);
		return this.amountHave.divide(this.amountWant, 6, RoundingMode.HALF_UP);
	}
	public static BigDecimal getPriceCalcReverse2(BigDecimal amountHave, BigDecimal amountWant)
	{
		//return this.amountHave.divide(amountWant, getScaleForPriceReverse(), RoundingMode.HALF_UP);
		return amountHave.divide(amountWant, 6, RoundingMode.HALF_UP);
	}

	public String viewPrice()
	{
		if(this.amountHave.compareTo(this.amountWant) > 0)
			return ":" + getPriceCalcReverse().toPlainString();

		return "*" + getPriceCalc().toPlainString();

	}

	public long getTimestamp()
	{
		return this.timestamp;
	}

	public List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getInitiatedTrades()
	{
		return this.getInitiatedTrades(DCSet.getInstance());
	}

	public List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getInitiatedTrades(DCSet db)
	{
		return db.getTradeMap().getInitiatedTrades(null);
	}

	public boolean isConfirmed()
	{
		return DCSet.getInstance().getOrderMap().contains(this.id)
				|| DCSet.getInstance().getCompletedOrderMap().contains(this.id);
	}

	public boolean isConfirmed(DCSet dc)
	{
		return dc.getOrderMap().contains(this.id)
				|| dc.getCompletedOrderMap().contains(this.id);
	}

	//PARSE/CONVERT

	/*
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
		BigDecimal amountHave = new BigDecimal(new BigInteger(amountHaveBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
		position += AMOUNT_LENGTH;

		//READ AMOUNT WANT
		byte[] amountWantBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amountWant = new BigDecimal(new BigInteger(amountWantBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
		position += AMOUNT_LENGTH;

		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
		position += TIMESTAMP_LENGTH;

		//READ FULFILLED HAVE
		byte[] fulfilledHaveBytes = Arrays.copyOfRange(data, position, position + FULFILLED_LENGTH);
		BigDecimal fulfilledHave = new BigDecimal(new BigInteger(fulfilledHaveBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
		position += FULFILLED_LENGTH;

		//READ FULFILLED WANT
		byte[] fulfilledWantBytes = Arrays.copyOfRange(data, position, position + FULFILLED_LENGTH);
		BigDecimal fulfilledWant = new BigDecimal(new BigInteger(fulfilledWantBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
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
	 */

	@SuppressWarnings("unchecked")
	public JSONObject toJson() {

		JSONObject order = new JSONObject();
		order.put("haveKey", this.have);
		order.put("wantKey", this.want);
		order.put("amountHave", this.amountHave.toPlainString());
		order.put("amountWant", this.amountWant.toPlainString());
		order.put("fulfilledHave", this.fulfilledHave.toPlainString());
		//order.put("fulfilledWant", this.fulfilledWant.toPlainString());

		return order;

	}

	public static Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> toDBrec(Order_old_ReversePrice order) {
		return new Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>(
						new Tuple4<BigInteger, String, Long, Boolean>(order.getId(), order.getCreator().getAddress(), order.getTimestamp(), order.isExecutable()),
						new Tuple3<Long, BigDecimal, BigDecimal>(order.getHave(), order.getAmountHave(), order.getFulfilledHave()),
						new Tuple2<Long, BigDecimal>(order.getWant(), order.getAmountWant()));

	}

	public static Order_old_ReversePrice fromDBrec(Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order) {
		return new Order_old_ReversePrice(order.a.a, new Account(order.a.b), order.b.a, order.c.a, order.b.b,
				order.c.b, order.b.c,
				(byte)(order.a.d?1:0), order.a.c);

	}

	//PROCESS/ORPHAN

	public void process(Transaction transaction)
	{

		DCSet db = this.dcSet;
		//REMOVE HAVE
		//this.creator.setBalance(this.have, this.creator.getBalance(db, this.have).subtract(this.amountHave), db);
		this.creator.changeBalance(this.dcSet, true, this.have, this.amountHave, false);

		//ADD ORDER TO DATABASE
		db.getOrderMap().add(Order_old_ReversePrice.toDBrec(this));

		//GET ALL ORDERS(WANT, HAVE) LOWEST PRICE FIRST
		//TRY AND COMPLETE ORDERS
		boolean completedOrder = false;
		int i = -1;
		BigDecimal thisPrice = this.getPriceCalc();
		BigDecimal thisReversePrice = this.getPriceCalcReverse();
		boolean isReversePrice = thisPrice.compareTo(BigDecimal.ONE) < 0;

		List<Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = db.getOrderMap().getOrders(this.want, this.have, true);
		//Collections.sort(orders);

		boolean isDivisibleHave = true; //this.isHaveDivisible(db);
		boolean isDivisibleWant = true; //this.isWantDivisible(db);
		BigDecimal thisAmountHaveLeft = this.getAmountHaveLeft();

		while( !completedOrder && ++i < orders.size())
		{
			//GET ORDER
			Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = orders.get(i);

			BigDecimal orderAmountHaveLeft;
			BigDecimal orderAmountWantLeft;
			BigDecimal orderReversePrice = Order_old_ReversePrice.getPriceCalcReverse2(order.b.b, order.c.b);
			BigDecimal orderPrice = Order_old_ReversePrice.getPriceCalc2(order.b.b, order.c.b);

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

			orderAmountHaveLeft = order.b.b.subtract(order.b.c); //.getAmountHaveLeft();

			// calculate WANT amount left for ORDER
			if (order.b.c.compareTo(BigDecimal.ZERO) == 0)
				orderAmountWantLeft = order.c.b; //getAmountWant();
			else
				// recalv Want
				orderAmountWantLeft = Order.fromDBrec(order).calcAmountWantLeft_old(db); // .getAmountWantLeft();

			int compare = orderAmountWantLeft.compareTo(thisAmountHaveLeft);
			if ( compare >= 0) {

				tradeAmountGet = thisAmountHaveLeft;
				if (compare == 0)
					tradeAmount = orderAmountHaveLeft;
				else
				{

					tradeAmount = isReversePrice?
							thisAmountHaveLeft.divide(orderPrice, 8, RoundingMode.HALF_DOWN):
								///thisAmountHaveLeft.multiply(orderReversePrice, rounding).setScale(thisAmountHaveLeft.scale(), RoundingMode.HALF_DOWN);
								tradeAmountGet.multiply(orderReversePrice, rounding).setScale(tradeAmountGet.scale(), RoundingMode.HALF_DOWN);

							//tradeAmount.
							if ( !isDivisibleWant && tradeAmount.stripTrailingZeros().scale() > 0) {
								// rounding only DOWN !
								tradeAmount = tradeAmount.setScale(0, RoundingMode.DOWN);

								if (tradeAmount.compareTo(BigDecimal.ZERO) == 0)
									// not completed yet by this order price
									continue;

								// recalc
								tradeAmountGet = isReversePrice?
										tradeAmount.multiply(orderPrice, rounding).setScale(tradeAmount.scale(), RoundingMode.HALF_DOWN):
											tradeAmount.divide(orderReversePrice, tradeAmount.scale(), RoundingMode.HALF_DOWN);
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
				trade = new Trade(this.getId(), order.a.a, tradeAmount, tradeAmountGet, transaction.getTimestamp());
				trade.process(db);

				// need to update from DB -.copy() not update automative
				this.fulfilledHave = this.fulfilledHave.add(tradeAmountGet);
				//this.fulfilledWant = this.fulfilledWant.add(tradeAmount);

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
					CancelOrderTransaction.process_it(db, toDBrec(this));
					//and stop resolve
					return;
				}

			}
		}
		if (!completedOrder) {
			db.getOrderMap().add(toDBrec(this));
		} else {
			db.getCompletedOrderMap().add(toDBrec(this));
		}
	}

	public void orphan() {

		DCSet db = this.dcSet;

		//ORPHAN TRADES
		for(Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> trade: this.getInitiatedTrades(db))
		{
			Trade.fromDBrec(trade).orphan(db);
		}

		//REMOVE ORDER FROM DATABASE
		db.getOrderMap().delete(this.getId());

		//REMOVE HAVE
		//this.creator.setBalance(this.have, this.creator.getBalance(db, this.have).add(this.amountHave), db);
		this.creator.changeBalance(db, false, this.have, this.amountHave, true);
	}

	/*
	// TODO delete this
	// SCALE - different for ASSETS
	public BigDecimal calculateBuyIncrement()
	{
		BigInteger multiplier = BigInteger.valueOf(100000000l);

		//CALCULATE THE MINIMUM INCREMENT AT WHICH I CAN BUY USING GCD
		BigInteger haveAmount = BigInteger.ONE.multiply(multiplier);
		BigInteger priceAmount = this.getPriceCalc().multiply(new BigDecimal(multiplier), rounding)
				.setScale(this.getScaleForPrice(), RoundingMode.HALF_DOWN).toBigInteger();
		BigInteger gcd = haveAmount.gcd(priceAmount);
		haveAmount = haveAmount.divide(gcd);
		priceAmount = priceAmount.divide(gcd);

		//CALCULATE GCD IN COMBINATION WITH DIVISIBILITY
		if(true) //this.getWantAsset(this.dcSet).isDivisible())
		{
			haveAmount = haveAmount.multiply(multiplier);
		}
		if(true) //this.getHaveAsset(this.dcSet).isDivisible())
		{
			priceAmount = priceAmount.multiply(multiplier);
		}
		gcd = haveAmount.gcd(priceAmount);

		//CALCULATE THE INCREMENT AT WHICH WE HAVE TO BUY
		BigDecimal increment = new BigDecimal(haveAmount.divide(gcd));
		if(true) // this.getWantAsset(this.dcSet).isDivisible())
		{
			increment = increment.divide(new BigDecimal(multiplier));
		}

		//RETURN
		return increment;
	}
	 */

	//COMPARE

	//@Override
	@Override
	public int compareTo(Order_old_ReversePrice order)
	{
		//COMPARE ONLY BY PRICE
		int result = this.getPriceCalc().compareTo(order.getPriceCalc());
		if (result != 0)
			return result;

		// TODO: REMOVE it in new CHAIN
		//if (this.timestamp < 1501816130973000l)
		//	return 0;

		long orderTimestamp = order.getTimestamp();
		if (this.timestamp < orderTimestamp)
			return -1;
		else if (this.timestamp > orderTimestamp)
			return 1;

		return 0;

	}

	//COPY

	public Order_old_ReversePrice copy()
	{
		return fromDBrec(toDBrec(this));
		/*
		try
		{
			return parse(this.toBytes());
		}
		catch (Exception e)
		{
			return null;
		}
		 */
	}
}
