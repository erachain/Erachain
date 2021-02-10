package org.erachain.core.item.assets;
// 16/03

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TradeMapImpl;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

public class Pair {

    private static final int ASSET_KEY_LENGTH = Transaction.KEY_LENGTH;
    private static final int AMOUNT_LENGTH = Order.AMOUNT_LENGTH;
    private static final int SCALE_LENGTH = 1;
    private static final int BASE_LENGTH = 2 * (ASSET_KEY_LENGTH + 1) + 8 * (AMOUNT_LENGTH + SCALE_LENGTH)
            + Long.BYTES + Integer.BYTES;

    private Long assetKey1;
    private Long assetKey2;

    private AssetCls asset1;
    private AssetCls asset2;

    private int assetScale1;
    private int assetScale2;

    private BigDecimal lastPrice;
    private long lastTime;

    private BigDecimal askPrice; // lowest_ask - наилучшая цена продажи
    private BigDecimal bidPrice; // highest_bid - наилучшая цена покупки

    private BigDecimal base_volume; // base_volume - объем 24 в токене базовом
    private BigDecimal quote_volume; // quote_volume - объем 24 в ценовом токене

    private BigDecimal price_change_percent_24h; //

    private BigDecimal highest_price_24h; // base_volume - объем в токене базовом
    private BigDecimal lowest_price_24h; // quote_volume - объем в ценовом токене

    private int count24;


    // make trading if two orders is seeked
    public Pair(Long assetKey1, Long assetKey2, int AssetScale1, int assetScale2, BigDecimal lastPrice, long lastTime,
                BigDecimal bidPrice, BigDecimal askPrice,
                BigDecimal base_volume, BigDecimal quote_volume, BigDecimal price_change_percent_24h,
                BigDecimal highest_price_24h, BigDecimal lowest_price_24h,
                int count24) {
        this.assetKey1 = assetKey1;
        this.assetKey2 = assetKey2;
        this.assetScale1 = AssetScale1;
        this.assetScale2 = assetScale2;

        this.lastPrice = lastPrice;
        this.lastTime = lastTime;

        this.bidPrice = bidPrice;
        this.askPrice = askPrice;

        this.base_volume = base_volume;
        this.quote_volume = quote_volume;

        this.price_change_percent_24h = price_change_percent_24h;

        this.highest_price_24h = highest_price_24h;
        this.lowest_price_24h = lowest_price_24h;

        this.count24 = count24;

    }

    public String viewID() {
        return Transaction.viewDBRef(assetKey1) + "/" + Transaction.viewDBRef(assetKey2);
    }

    public Long getAssetKey1() {
        return this.assetKey1;
    }

    public Long getAssetKey2() {
        return this.assetKey2;
    }

    public static Pair get(DCSet db, Long assetKey1, Long assetKey2) {
        return db.getPairMap().get(TradeMapImpl.key);
    }

    public int getCount24() {
        return this.count24;
    }

    public BigDecimal getHighest_price_24h() {
        return this.highest_price_24h;
    }

    public BigDecimal getLowest_price_24h() {
        return this.lowest_price_24h;
    }

    public BigDecimal getBase_volume() {
        return base_volume;
    }

    public BigDecimal getQuote_volume() {
        return quote_volume;
    }

    public BigDecimal getBidPrice() {
        return this.bidPrice;
    }

    public BigDecimal getAskPrice() {
        return this.askPrice;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public long getLastTime() {
        return lastTime;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson(long keyForBuySell) {

        JSONObject pair = new JSONObject();
        pair.put("id", viewID());
        pair.put("assetKey1", assetKey1);
        pair.put("assetKey2", assetKey2);

        pair.put("last_price", lastTime);

        if (keyForBuySell == 0 || keyForBuySell == assetKey1) {

            if (keyForBuySell != 0) {
                // задана пара и направление можно давать
                pair.put("type", "sell");
            }

            pair.put("base_currency", asset1.getName());
            pair.put("quote_currency", asset2.getName());

            pair.put("last_price", lastPrice);

            pair.put("lowest_ask", askPrice);
            pair.put("highest_bid", bidPrice);

            pair.put("base_volume", base_volume);
            pair.put("quote_volume", quote_volume);
            pair.put("count24", count24);

            pair.put("price_change_percent_24h", price_change_percent_24h);
            pair.put("highest_price_24h", highest_price_24h);
            pair.put("lowest_price_24h", lowest_price_24h);

        } else {
            pair.put("type", "buy");

            pair.put("quote_currency", asset1.getName());
            pair.put("base_currency", asset2.getName());

            pair.put("last_price", BigDecimal.ONE.divide(lastPrice, assetScale2, RoundingMode.HALF_DOWN).stripTrailingZeros());

            pair.put("highest_bid", askPrice);
            pair.put("lowest_ask", bidPrice);

            pair.put("base_volume", quote_volume);
            pair.put("quote_volume", base_volume);
            pair.put("count24", count24);

            pair.put("price_change_percent_24h", price_change_percent_24h);
            pair.put("highest_price_24h", lowest_price_24h);
            pair.put("lowest_price_24h", highest_price_24h);

        }

        return pair;

    }


    //PARSE/CONVERT
    public static Pair parse(byte[] data) throws Exception {
        //CHECK IF CORRECT LENGTH
        if (data.length != BASE_LENGTH) {
            throw new Exception("Data does not match trade length");
        }

        int position = 0;
        int scale;

        //READ ASSET 1 KEY
        Long assetKey1 = Longs.fromByteArray(Arrays.copyOfRange(data, position, position + ASSET_KEY_LENGTH));
        position += ASSET_KEY_LENGTH;

        //READ ASSET 2 KEY
        Long assetKey2 = Longs.fromByteArray(Arrays.copyOfRange(data, position, position + ASSET_KEY_LENGTH));
        position += ASSET_KEY_LENGTH;

        //READ HAVE SCALE
        byte assetScale1 = Arrays.copyOfRange(data, position, position + 1)[0];
        position++;

        //READ WANT SCALE
        byte assetScale2 = Arrays.copyOfRange(data, position, position + 1)[0];
        position++;

        //READ LAST PRICE
        scale = data[position++];
        BigDecimal lastPrice = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH)), scale);
        position += AMOUNT_LENGTH;

        //READ LAST TIME
        Long lastTime = Longs.fromByteArray(Arrays.copyOfRange(data, position, position + ASSET_KEY_LENGTH));
        position += ASSET_KEY_LENGTH;

        //READ BID PRICE
        scale = data[position++];
        BigDecimal bidPrice = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH)), scale);
        position += AMOUNT_LENGTH;

        //READ ASK PRICE
        scale = data[position++];
        BigDecimal askPrice = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH)), scale);
        position += AMOUNT_LENGTH;

        //READ BASE VOLUME
        scale = data[position++];
        BigDecimal baseVolume = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH)), scale);
        position += AMOUNT_LENGTH;

        //READ QUOTE VOLUME
        scale = data[position++];
        BigDecimal quoteVolume = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH)), scale);
        position += AMOUNT_LENGTH;

        //READ PRICE _change_percent_24h
        scale = data[position++];
        BigDecimal price_change_percent_24h = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH)), scale);
        position += AMOUNT_LENGTH;

        //READ highest_price_24h
        scale = data[position++];
        BigDecimal highest_price_24h = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH)), scale);
        position += AMOUNT_LENGTH;

        //READ lowest_price_24h
        scale = data[position++];
        BigDecimal lowest_price_24h = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH)), scale);
        position += AMOUNT_LENGTH;

        //READ COUNT 24
        int count24 = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + Integer.BYTES));
        position += Integer.BYTES;

        return new Pair(assetKey1, assetKey2, assetScale1, assetScale2, lastPrice, lastTime, bidPrice, askPrice,
                baseVolume, quoteVolume, price_change_percent_24h,
                highest_price_24h, lowest_price_24h, count24);
    }

    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE INITIATOR
        data = Bytes.concat(data, Longs.toByteArray(this.assetKey1));

        //WRITE TARGET
        byte[] assetKey2Bytes = Longs.toByteArray(this.assetKey2);
        data = Bytes.concat(data, assetKey2Bytes);

        // ASSETS SCALE
        data = Bytes.concat(data, new byte[]{(byte) this.assetScale1});
        data = Bytes.concat(data, new byte[]{(byte) this.assetScale2});

        // last price
        data = Bytes.concat(data, new byte[]{(byte) this.lastPrice.scale()});
        data = Bytes.concat(data, Longs.toByteArray(lastPrice.unscaledValue().longValue()));

        // last time
        data = Bytes.concat(data, Longs.toByteArray(this.lastTime));

        // bid price
        data = Bytes.concat(data, new byte[]{(byte) this.bidPrice.scale()});
        data = Bytes.concat(data, Longs.toByteArray(bidPrice.unscaledValue().longValue()));

        // ask price
        data = Bytes.concat(data, new byte[]{(byte) this.askPrice.scale()});
        data = Bytes.concat(data, Longs.toByteArray(askPrice.unscaledValue().longValue()));

        // base volume
        data = Bytes.concat(data, new byte[]{(byte) this.base_volume.scale()});
        data = Bytes.concat(data, Longs.toByteArray(base_volume.unscaledValue().longValue()));

        // quote volume
        data = Bytes.concat(data, new byte[]{(byte) this.quote_volume.scale()});
        data = Bytes.concat(data, Longs.toByteArray(quote_volume.unscaledValue().longValue()));

        // PRICE _change_percent_24h
        data = Bytes.concat(data, new byte[]{(byte) this.price_change_percent_24h.scale()});
        data = Bytes.concat(data, Longs.toByteArray(price_change_percent_24h.unscaledValue().longValue()));

        // highest_price_24h
        data = Bytes.concat(data, new byte[]{(byte) this.highest_price_24h.scale()});
        data = Bytes.concat(data, Longs.toByteArray(highest_price_24h.unscaledValue().longValue()));

        // lowest_price_24h
        data = Bytes.concat(data, new byte[]{(byte) this.lowest_price_24h.scale()});
        data = Bytes.concat(data, Longs.toByteArray(lowest_price_24h.unscaledValue().longValue()));

        // count 24
        data = Bytes.concat(data, Ints.toByteArray(this.count24));

        return data;
    }

    public int getDataLength() {
        return BASE_LENGTH;
    }

    //PROCESS/ORPHAN

    public void process(DCSet db) {
        Trade pair = db.getPairMap().get(this);
    }

    public void orphan(DCSet db) {
    }

    @Override
    public int hashCode() {
        return assetKey1.hashCode() + assetKey2.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Pair) {
            Pair trade = (Pair) object;

            return (trade.getAssetKey1().equals(this.getAssetKey1()) && trade.getAssetKey2().equals(this.getAssetKey2()));
        }

        return false;
    }


    @Override
    public String toString() {
        return viewID();
    }

}
