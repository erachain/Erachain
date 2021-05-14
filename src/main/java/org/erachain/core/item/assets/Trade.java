package org.erachain.core.item.assets;
// 16/03

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class Trade {

    private static final int ORDER_LENGTH = Order.ID_LENGTH;
    private static final int ASSET_KEY_LENGTH = Transaction.KEY_LENGTH;
    private static final int AMOUNT_LENGTH = Order.FULFILLED_LENGTH;
    private static final int SEQUENCE_LENGTH = 4;
    private static final int SCALE_LENGTH = 1;
    private static final int BASE_LENGTH = 1 + 2 * ORDER_LENGTH + 2 * ASSET_KEY_LENGTH
            + 4 * SCALE_LENGTH + 2 * AMOUNT_LENGTH + SEQUENCE_LENGTH;

    private byte type;
    private long initiator;
    private long target;
    private long haveKey;
    private long wantKey;
    private BigDecimal amountHave;
    private BigDecimal amountWant;
    private int haveAssetScale;
    private int wantAssetScale;

    /**
     * Used only for inintiator Order - for make sorted secondary INDEX
     */
    private int sequence;

    public static final byte TYPE_TRADE = 0;
    public static final byte TYPE_CANCEL = 1;

    // make trading if two orders is seeked
    public Trade(long initiator, long target, long haveKey, long wantKey, BigDecimal amountHave, BigDecimal amountWant, int haveAssetScale, int wantAssetScale, int sequence) {
        this.initiator = initiator;
        this.target = target;
        this.haveKey = haveKey;
        this.wantKey = wantKey;
        this.amountHave = amountHave;
        this.amountWant = amountWant;
        this.haveAssetScale = haveAssetScale;
        this.wantAssetScale = wantAssetScale;

        this.sequence = sequence;
    }

    public Trade(byte type, long initiator, long target, long haveKey, long wantKey, BigDecimal amountHave, BigDecimal amountWant, int haveAssetScale, int wantAssetScale, int sequence) {
        this.type = type;
        this.initiator = initiator;
        this.target = target;
        this.haveKey = haveKey;
        this.wantKey = wantKey;
        this.amountHave = amountHave;
        this.amountWant = amountWant;
        this.haveAssetScale = haveAssetScale;
        this.wantAssetScale = wantAssetScale;

        this.sequence = sequence;
    }

    public String viewType() {
        switch (type) {
            case TYPE_TRADE:
                return "trade";
            case TYPE_CANCEL:
                return "cancel";
        }
        return "unknown";
    }

    public boolean isCancel() {
        return type == TYPE_CANCEL;
    }

    public String viewID() {
        return Transaction.viewDBRef(initiator) + "/" + Transaction.viewDBRef(target);
    }

    public long getInitiator() {
        return this.initiator;
    }

    public Order getInitiatorOrder(DCSet db) {
        if (type == TYPE_TRADE)
            return Order.getOrder(db, this.initiator);

        return null;
    }

    public long getTarget() {
        return this.target;
    }

    public static Trade get(DCSet db, Order initiator, Order target) {

        return db.getTradeMap().get(new Tuple2<>(initiator.getId(), target.getId()));
    }

    public Order getTargetOrder(DCSet db) {
        return Order.getOrder(db, this.target);
    }

    public long getHaveKey() {
        return this.haveKey;
    }

    public long getWantKey() {
        return this.wantKey;
    }

    public BigDecimal getAmountHave() {
        return this.amountHave;
    }

    public BigDecimal getAmountWant() {
        return this.amountWant;
    }

    public BigDecimal calcPrice() {
        return Order.calcPrice(this.amountHave, this.amountWant, wantAssetScale);
    }
    public BigDecimal calcPriceRevers() {
        return Order.calcPrice(this.amountWant, this.amountHave, haveAssetScale);
    }

    public int getSequence() {
        return this.sequence;
    }

    public long getTimestamp() {
        Tuple2<Integer, Integer> key = Transaction.parseDBRef(this.initiator);
        return Controller.getInstance().getBlockChain().getTimestamp(key.a);
    }

    public static long[] parseID(String ordersID) {
        try {
            String[] strA = ordersID.split("/");
            long orderIDinitiator = Transaction.parseDBRef(strA[0]);
            long orderIDtarget = Transaction.parseDBRef(strA[1]);
            return new long[]{orderIDinitiator, orderIDtarget};
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson(long keyForBuySell, boolean withCreators) {

        JSONObject trade = new JSONObject();
        trade.put("type", type);
        trade.put("typeName", viewType());
        trade.put("id", viewID());
        trade.put("initiator", Transaction.viewDBRef(initiator));
        trade.put("target", Transaction.viewDBRef(target));

        int height = Transaction.parseHeightDBRef(initiator);
        trade.put("height", height);
        trade.put("timestamp", Controller.getInstance().blockChain.getTimestamp(height));

        trade.put("sequence", sequence);

        if (keyForBuySell == 0 || keyForBuySell == haveKey) {

            if (keyForBuySell != 0) {
                // задана пара и направление можно давать
                trade.put("type", "sell");
            }

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

        if (withCreators) {
            if (isCancel()) {
                Transaction cancelTX = DCSet.getInstance().getTransactionFinalMap().get(initiator);
                trade.put("initiatorCreator", cancelTX.getCreator().getAddress());
            } else {
                Order order = getInitiatorOrder(DCSet.getInstance());
                trade.put("initiatorCreator", order.getCreator().getAddress());
            }

            Order orderTarget = getTargetOrder(DCSet.getInstance());
            trade.put("targetCreator", orderTarget.getCreator().getAddress());

        }

        return trade;

    }


    //PARSE/CONVERT
    public static Trade parse(byte[] data) throws Exception {
        //CHECK IF CORRECT LENGTH
        if (data.length != BASE_LENGTH) {
            throw new Exception("Data does not match trade length");
        }

        int position = 0;

        byte type = data[position++];

        //READ INITIATOR
        byte[] initiatorBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
        long initiator = Longs.fromByteArray(initiatorBytes);
        position += ORDER_LENGTH;

        //READ TARGET
        byte[] targetBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
        long target = Longs.fromByteArray(targetBytes);
        position += ORDER_LENGTH;

        //READ HAVE
        byte[] haveBytes = Arrays.copyOfRange(data, position, position + ASSET_KEY_LENGTH);
        long haveKey = Longs.fromByteArray(haveBytes);
        position += ASSET_KEY_LENGTH;

        //READ WANT
        byte[] wantBytes = Arrays.copyOfRange(data, position, position + ASSET_KEY_LENGTH);
        long wantKey = Longs.fromByteArray(wantBytes);
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

        byte haveAssetScale = Arrays.copyOfRange(data, position, position + 1)[0];
        position ++;
        byte wantAssetScale = Arrays.copyOfRange(data, position, position + 1)[0];
        position ++;

        //READ SEQUENCE
        byte[] sequenceBytes = Arrays.copyOfRange(data, position, position + SEQUENCE_LENGTH);
        int sequence = Ints.fromByteArray(sequenceBytes);

        return new Trade(type, initiator, target, haveKey, wantKey, amountHave, amountWant, haveAssetScale, wantAssetScale, sequence);
	}

	public byte[] toBytes()
	{
        byte[] data = new byte[]{type};

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

        // ASSETS SCALE
        data = Bytes.concat(data, new byte[]{(byte)this.haveAssetScale});
        data = Bytes.concat(data, new byte[]{(byte)this.wantAssetScale});

        //WRITE SEQUENCE
        byte[] sequenceBytes = Ints.toByteArray(this.sequence);
        data = Bytes.concat(data, sequenceBytes);

        return data;
	}

	public int getDataLength()
	{
		return BASE_LENGTH;
	}

    @Override
    public boolean equals(Object object) {
        if (object instanceof Trade) {
            Trade trade = (Trade) object;

            return (trade.getInitiator() == initiator && trade.getTarget() == target);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (int) (initiator + target);
    }


    @Override
    public String toString() {
        return viewID() + " : " + this.haveKey + "/" + this.wantKey;
    }

}
