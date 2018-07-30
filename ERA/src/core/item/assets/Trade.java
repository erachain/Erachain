package core.item.assets;
// 16/03

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import core.crypto.Crypto;
import datachain.DCSet;

public class Trade {

    private static final int ORDER_LENGTH = Crypto.SIGNATURE_LENGTH;
    private static final int AMOUNT_LENGTH = 12;
    private static final int TIMESTAMP_LENGTH = 8;
    private static final int BASE_LENGTH = ORDER_LENGTH + ORDER_LENGTH + 2 * AMOUNT_LENGTH + TIMESTAMP_LENGTH;

    private Long initiator;
    private Long target;
    private BigDecimal amountHave;
    private BigDecimal amountWant;
    private long timestamp;

    // make trading if two orders is seeked
    public Trade(Long initiator, Long target, BigDecimal amountHave, BigDecimal amountWant, long timestamp) {
        this.initiator = initiator;
        this.target = target;
        this.amountHave = amountHave;
        this.amountWant = amountWant;
        this.timestamp = timestamp;
    }

    //PARSE/CONVERT
    public static Tuple5<Long, Long, BigDecimal, BigDecimal, Long> toDBrec(Trade trade) {
        return new Tuple5<Long, Long, BigDecimal, BigDecimal, Long>(
                trade.initiator, trade.target, trade.amountHave, trade.amountWant, trade.timestamp);

    }

    public static Trade fromDBrec(Tuple5<Long, Long, BigDecimal, BigDecimal, Long> trade) {
        return new Trade(trade.a, trade.b, trade.c, trade.d, trade.e);
    }

    public static List<Trade> getTradeByTimestmp(DCSet dcSet, long have, long want, long timestamp) {
        List<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> list = dcSet.getTradeMap().getTradesByTimestamp(have, want, timestamp);

        List<Trade> trades = new ArrayList<Trade>();
        for (Tuple5<Long, Long, BigDecimal, BigDecimal, Long> item : list) {
            trades.add(Trade.fromDBrec(item));
        }

        return trades;
    }

    public Long getInitiator() {
        return this.initiator;
    }

    public Order getInitiatorOrder(DCSet db) {
        return Order.getOrder(db, this.initiator);
    }

    public Long getTarget() {
        return this.target;
    }

    public Order getTargetOrder(DCSet db) {
        return Order.getOrder(db, this.target);
    }

    public BigDecimal getAmountHave() {
        return this.amountHave;
    }

    public BigDecimal getAmountWant() {
        return this.amountWant;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

	/*
	public static Trade parse(Long data) throws Exception
	{
		//CHECK IF CORRECT LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match trade length");
		}

		int position = 0;

		//READ INITIATOR
		Long initiatorBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
		Long initiator = new Long(initiatorBytes);
		position += ORDER_LENGTH;

		//READ TARGET
		Long targetBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
		Long target = new Long(targetBytes);
		position += ORDER_LENGTH;

		//READ AMOUNT HAVE
		Long amountHaveBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amountHave = new BigDecimal(new Long(amountHaveBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
		position += AMOUNT_LENGTH;

		//READ AMOUNT WANT
		Long amountWantBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amountWant = new BigDecimal(new Long(amountWantBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
		position += AMOUNT_LENGTH;

		//READ TIMESTAMP
		Long timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
		position += TIMESTAMP_LENGTH;

		return new Trade(initiator, target, amountHave, amountWant, timestamp);
	}

	public Long toBytes()
	{
		Long data = new byte[0];

		//WRITE INITIATOR
		Long initiatorBytes = this.initiator.toByteArray();
		Long fill = new byte[ORDER_LENGTH - initiatorBytes.length];
		initiatorBytes = Bytes.concat(fill, initiatorBytes);
		data = Bytes.concat(data, initiatorBytes);

		//WRITE TARGET
		Long targetBytes = this.target.toByteArray();
		fill = new byte[ORDER_LENGTH - targetBytes.length];
		targetBytes = Bytes.concat(fill, targetBytes);
		data = Bytes.concat(data, targetBytes);

		//WRITE AMOUNT HAVE
		Long amountHaveBytes = this.amountHave.unscaledValue().toByteArray();
		fill = new byte[AMOUNT_LENGTH - amountHaveBytes.length];
		amountHaveBytes = Bytes.concat(fill, amountHaveBytes);
		data = Bytes.concat(data, amountHaveBytes);

		//WRITE AMOUNT WANT
		Long amountWantBytes = this.amountWant.unscaledValue().toByteArray();
		fill = new byte[AMOUNT_LENGTH - amountWantBytes.length];
		amountWantBytes = Bytes.concat(fill, amountWantBytes);
		data = Bytes.concat(data, amountWantBytes);

		//WRITE TIMESTAMP
		Long timestampBytes = Longs.toByteArray(this.timestamp);
		data = Bytes.concat(data, timestampBytes);

		return data;
	}

	public int getDataLength()
	{
		return BASE_LENGTH;
	}
	 */

    //PROCESS/ORPHAN

    public void process(DCSet db) {
        Order initiator = this.getInitiatorOrder(db);
        Order target = this.getTargetOrder(db);

        //ADD TRADE TO DATABASE
        db.getTradeMap().add(toDBrec(this));
        if (!db.getTradeMap().contains(new Tuple2<Long, Long>(this.initiator, this.target))) {
            int error = 0;
        }

        //UPDATE FULFILLED HAVE
        initiator.setFulfilledHave(initiator.getFulfilledHave().add(this.amountWant));
        target.setFulfilledHave(target.getFulfilledHave().add(this.amountHave));

        //CHECK IF FULFILLED
        if (initiator.isFulfilled()) {
            //REMOVE FROM ORDERS
            db.getOrderMap().delete(initiator);

            //ADD TO COMPLETED ORDERS
            //initiator.setFulfilledWant(initiator.getAmountWant());
            db.getCompletedOrderMap().add(initiator);
        } else {
            //UPDATE ORDER
            db.getOrderMap().add(initiator);
        }

        if (target.isFulfilled()) {
            //REMOVE FROM ORDERS
            db.getOrderMap().delete(target);

            //ADD TO COMPLETED ORDERS
            //target.setFulfilledWant(target.getAmountWant());
            db.getCompletedOrderMap().add(target);
        } else {
            //UPDATE ORDER
            //target.setFulfilledWant(target.getFulfilledWant().add(amountWant));
            db.getOrderMap().add(target);
        }

        //TRANSFER FUNDS
        //initiator.getCreator().setBalance(initiator.getWant(), initiator.getCreator().getBalance(db, initiator.getWant()).add(this.amountHave), db);
        initiator.getCreator().changeBalance(db, false, initiator.getWant(), this.amountHave, false);
        //target.getCreator().setBalance(target.getWant(), target.getCreator().getBalance(db, target.getWant()).add(this.amountWant), db);
        target.getCreator().changeBalance(db, false, target.getWant(), this.amountWant, false);
    }

    public void orphan(DCSet db) {
        Order initiator = this.getInitiatorOrder(db);
        Order target = this.getTargetOrder(db);

        //REVERSE FUNDS
        //initiator.getCreator().setBalance(initiator.getWant(), initiator.getCreator().getBalance(db, initiator.getWant()).subtract(this.amountHave), db);
        initiator.getCreator().changeBalance(db, true, initiator.getWant(), this.amountHave, false);
        //target.getCreator().setBalance(target.getWant(), target.getCreator().getBalance(db, target.getWant()).subtract(this.amountWant), db);
        target.getCreator().changeBalance(db, true, target.getWant(), this.amountWant, false);

        //CHECK IF ORDER IS FULFILLED
        if (initiator.isFulfilled()) {
            //REMOVE FROM COMPLETED ORDERS
            db.getCompletedOrderMap().delete(initiator);
        }
        if (target.isFulfilled()) {
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
    public boolean equals(Object object) {
        if (object instanceof Trade) {
            Trade trade = (Trade) object;

            return (trade.getInitiator().equals(this.getInitiator()) && trade.getTarget().equals(this.getTarget()));
        }

        return false;
    }
}
