package org.erachain.core.item.assets;
// 16/03

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.transaction.Transaction;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;

import org.erachain.datachain.DCSet;

public class Trade {

    private static final int ORDER_LENGTH = Order.ID_LENGTH;
    private static final int ASSET_KEY_LENGTH = Transaction.KEY_LENGTH;
    private static final int AMOUNT_LENGTH = Order.FULFILLED_LENGTH;
    private static final int SEQUENCE_LENGTH = 4;
    private static final int SCALE_LENGTH = 1;
    private static final int BASE_LENGTH = 2 * ORDER_LENGTH + 2 * ASSET_KEY_LENGTH
            + 2 * SCALE_LENGTH + 2 * AMOUNT_LENGTH + SEQUENCE_LENGTH;

    private Long initiator;
    private Long target;
    private Long haveKey;
    private Long wantKey;
    private BigDecimal amountHave;
    private BigDecimal amountWant;
    private int sequence;

    // make trading if two orders is seeked
    public Trade(Long initiator, Long target, Long haveKey, Long wantKey, BigDecimal amountHave, BigDecimal amountWant, int sequence) {
        this.initiator = initiator;
        this.target = target;
        this.haveKey = haveKey;
        this.wantKey = wantKey;
        this.amountHave = amountHave;
        this.amountWant = amountWant;
        this.sequence = sequence;
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

    public Long getHaveKey() {
        return this.haveKey;
    }
    public Long getWantKey() {
        return this.wantKey;
    }

    public BigDecimal getAmountHave() {
        return this.amountHave;
    }

    public BigDecimal getAmountWant() {
        return this.amountWant;
    }

    public BigDecimal calcPrice() {
        return Order.calcPrice(this.amountHave, this.amountWant);
    }
    public BigDecimal calcPrice(AssetCls assetHave, AssetCls assetWant) {
        return Order.calcPrice(amountHave.setScale(assetHave.getScale(), RoundingMode.HALF_DOWN),
                amountWant.setScale(assetWant.getScale(), RoundingMode.HALF_DOWN));

    }
    public BigDecimal calcPriceRevers() {
        return Order.calcPrice(this.amountWant, this.amountHave);
    }
    public BigDecimal calcPriceRevers(AssetCls assetHave, AssetCls assetWant) {
        return Order.calcPrice(amountWant.setScale(assetWant.getScale(), RoundingMode.HALF_DOWN),
                amountHave.setScale(assetHave.getScale(), RoundingMode.HALF_DOWN));
    }

    public int getSequence() {
        return this.sequence;
    }

    public Long getTimestamp() {
        Tuple2<Integer, Integer> key = Transaction.parseDBRef(this.initiator);
        BlockChain blockChain = Controller.getInstance().getBlockChain();
        Long timestamp = blockChain.getTimestamp(key.a) + key.b * 0; // for add SEQUENCE in asset.Trade
        return timestamp; // + this.sequence;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson(long keyForBuySell) {

        JSONObject trade = new JSONObject();
        trade.put("initiator", Transaction.viewDBRef(initiator));
        trade.put("target", Transaction.viewDBRef(target));
        trade.put("sequence", sequence);
        if (keyForBuySell == haveKey) {
            trade.put("type", "sell");

            trade.put("haveKey", haveKey);
            trade.put("wantKey", wantKey);

            trade.put("amountHave", amountWant);
            trade.put("amountWant", amountHave);

            trade.put("price", calcPriceRevers());
            trade.put("reversePrice", calcPrice());
        } else {
            trade.put("type", "buy");

            trade.put("haveKey", wantKey);
            trade.put("wantKey", haveKey);

            trade.put("amountHave", amountHave);
            trade.put("amountWant", amountWant);

            trade.put("price", calcPrice());
            trade.put("reversePrice", calcPriceRevers());

        }

        return trade;

    }


    //PARSE/CONVERT
    public static Trade parse(byte[] data) throws Exception
	{
		//CHECK IF CORRECT LENGTH
		if(data.length != BASE_LENGTH)
		{
			throw new Exception("Data does not match trade length");
		}

		int position = 0;

		//READ INITIATOR
		byte[] initiatorBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
		Long initiator = Longs.fromByteArray(initiatorBytes);
		position += ORDER_LENGTH;

		//READ TARGET
		byte[] targetBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
		Long target = Longs.fromByteArray(targetBytes);
		position += ORDER_LENGTH;

        //READ HAVE
        byte[] haveBytes = Arrays.copyOfRange(data, position, position + ASSET_KEY_LENGTH);
        Long haveKey = Longs.fromByteArray(haveBytes);
        position += ASSET_KEY_LENGTH;

        //READ WANT
        byte[] wantBytes = Arrays.copyOfRange(data, position, position + ASSET_KEY_LENGTH);
        Long wantKey = Longs.fromByteArray(wantBytes);
        position += ASSET_KEY_LENGTH;

        //READ HAVE SCALE
        byte scaleHave = Arrays.copyOfRange(data, position, position + 1)[0];
        position ++;

        //READ AMOUNT HAVE
        byte[] amountHaveBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
        BigDecimal amountHave = new BigDecimal(new BigInteger(amountHaveBytes), scaleHave);
        position += AMOUNT_LENGTH;

        //READ HAVE SCALE
        byte scaleWant = Arrays.copyOfRange(data, position, position + 1)[0];
        position ++;

        //READ AMOUNT WANT
        byte[] amountWantBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
        BigDecimal amountWant = new BigDecimal(new BigInteger(amountWantBytes), scaleWant);
        position += AMOUNT_LENGTH;

        //READ SEQUENCE
        byte[] sequenceBytes = Arrays.copyOfRange(data, position, position + SEQUENCE_LENGTH);
        int sequence = Ints.fromByteArray(sequenceBytes);

        return new Trade(initiator, target, haveKey, wantKey, amountHave, amountWant, sequence);
	}

	public byte[] toBytes()
	{
        byte[] data = new byte[0];

		//WRITE INITIATOR
        byte[] initiatorBytes = Longs.toByteArray(this.initiator);
		data = Bytes.concat(data, initiatorBytes);

		//WRITE TARGET
        byte[] targetBytes = Longs.toByteArray(this.target);
		data = Bytes.concat(data, targetBytes);

        //WRITE HAVE KEY
        byte[] haveKeyBytes = Longs.toByteArray(this.haveKey);
        data = Bytes.concat(data, haveKeyBytes);

        //WRITE HAVE KEY
        byte[] wantKeyBytes = Longs.toByteArray(this.wantKey);
        data = Bytes.concat(data, wantKeyBytes);

        byte[] fill;
                                
        //WRITE AMOUNT HAVE SCALE
        data = Bytes.concat(data, new byte[]{(byte)this.amountHave.scale()});

		//WRITE AMOUNT HAVE
        byte[] amountHaveBytes = this.amountHave.unscaledValue().toByteArray();
		fill = new byte[AMOUNT_LENGTH - amountHaveBytes.length];
		amountHaveBytes = Bytes.concat(fill, amountHaveBytes);
		data = Bytes.concat(data, amountHaveBytes);

        //WRITE AMOUNT WANT SCALE
        data = Bytes.concat(data, new byte[]{(byte)this.amountWant.scale()});

		//WRITE AMOUNT WANT
        byte[] amountWantBytes = this.amountWant.unscaledValue().toByteArray();
		fill = new byte[AMOUNT_LENGTH - amountWantBytes.length];
		amountWantBytes = Bytes.concat(fill, amountWantBytes);
		data = Bytes.concat(data, amountWantBytes);

        //WRITE SEQUENCE
        byte[] sequenceBytes = Ints.toByteArray(this.sequence);
        data = Bytes.concat(data, sequenceBytes);

        return data;
	}

	public int getDataLength()
	{
		return BASE_LENGTH;
	}

    //PROCESS/ORPHAN

    public void process_old(DCSet db) {
        Order initiator = this.getInitiatorOrder(db);
        Order target = this.getTargetOrder(db);

        //ADD TRADE TO DATABASE
        db.getTradeMap().add(this);
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

    public void orphan_old(DCSet db) {
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
