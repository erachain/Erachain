package org.erachain.core.item.assets;
// 16/03

import com.google.common.hash.HashCode;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DLSet;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class TradePair {

    private static final int ASSET_KEY_LENGTH = Transaction.KEY_LENGTH;
    private static final int AMOUNT_LENGTH = Order.AMOUNT_LENGTH;
    private static final int SCALE_LENGTH = 1;
    private static final int BASE_LENGTH = 2 * (ASSET_KEY_LENGTH + 1)
            + 8 * (AMOUNT_LENGTH + SCALE_LENGTH)
            + 2 * Long.BYTES + 3 * Integer.BYTES;

    private Long assetKey1;
    private Long assetKey2;

    private AssetCls asset1;
    private AssetCls asset2;

    private int assetScale1;
    private int assetScale2;

    private BigDecimal lastPrice;
    private long lastTime;

    private BigDecimal lower_askPrice; // lowest_ask - наилучшая цена продажи
    private BigDecimal highest_bidPrice; // highest_bid - наилучшая цена покупки

    private BigDecimal base_volume; // base_volume - объем 24 в токене базовом
    private BigDecimal quote_volume; // quote_volume - объем 24 в ценовом токене

    private BigDecimal price_change_percent_24h; //

    private BigDecimal lowest_price_24h; // quote_volume - объем в ценовом токене
    private BigDecimal highest_price_24h; // base_volume - объем в токене базовом

    private int count24;

    /**
     * Count orders in Cup
     */
    private int countOrdersBid;
    private int countOrdersAsk;

    // last updated on
    public long updateTime;

    // make trading if two orders is seeked
    public TradePair(Long assetKey1, Long assetKey2, int AssetScale1, int assetScale2, BigDecimal lastPrice, long lastTime,
                     BigDecimal highest_bidPrice, BigDecimal lower_askPrice,
                     BigDecimal base_volume, BigDecimal quote_volume, BigDecimal price_change_percent_24h,
                     BigDecimal lowest_price_24h, BigDecimal highest_price_24h,
                     int count24, long updateTime, int countOrdersBid, int countOrdersAsk) {
        this.assetKey1 = assetKey1;
        this.assetKey2 = assetKey2;
        this.assetScale1 = AssetScale1;
        this.assetScale2 = assetScale2;

        this.lastPrice = lastPrice == null ? BigDecimal.ZERO : lastPrice;
        this.lastTime = lastTime;

        this.highest_bidPrice = highest_bidPrice == null ? BigDecimal.ZERO : highest_bidPrice;
        this.lower_askPrice = lower_askPrice == null ? BigDecimal.ZERO : lower_askPrice;

        this.base_volume = base_volume == null ? BigDecimal.ZERO : base_volume;
        this.quote_volume = quote_volume == null ? BigDecimal.ZERO : quote_volume;

        this.price_change_percent_24h = price_change_percent_24h == null ? BigDecimal.ZERO : price_change_percent_24h;

        this.lowest_price_24h = lowest_price_24h == null ? BigDecimal.ZERO : lowest_price_24h;
        this.highest_price_24h = highest_price_24h == null ? BigDecimal.ZERO : highest_price_24h;

        this.count24 = count24;

        this.updateTime = updateTime;

        this.countOrdersBid = countOrdersBid;
        this.countOrdersAsk = countOrdersAsk;


    }

    public TradePair(AssetCls asset1, AssetCls asset2, BigDecimal lastPrice, long lastTime,
                     BigDecimal highest_bidPrice, BigDecimal lower_askPrice,
                     BigDecimal base_volume, BigDecimal quote_volume, BigDecimal price_change_percent_24h,
                     BigDecimal lowest_price_24h, BigDecimal highest_price_24h,
                     int count24, long updateTime, int countOrdersBid, int countOrdersAsk) {
        this(asset1.getKey(), asset2.getKey(), asset1.getScale(), asset2.getScale(), lastPrice, lastTime,
                highest_bidPrice, lower_askPrice, base_volume, quote_volume, price_change_percent_24h,
                lowest_price_24h, highest_price_24h, count24, updateTime, countOrdersBid, countOrdersAsk);
        this.asset1 = asset1;
        this.asset2 = asset2;
    }

    public void setDC(DCSet dcSet) {
        asset1 = dcSet.getItemAssetMap().get(assetKey1);
        asset2 = dcSet.getItemAssetMap().get(assetKey2);
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
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

    public AssetCls getAsset1() {
        return this.asset1;
    }

    public AssetCls getAsset2() {
        return this.asset2;
    }

    public static TradePair get(DLSet db, Long assetKey1, Long assetKey2) {
        return db.getPairMap().get(assetKey1, assetKey2);
    }

    public int getCount24() {
        return this.count24;
    }

    public int getCountOrdersBid() {
        return this.countOrdersBid;
    }

    public int getCountOrdersAsk() {
        return this.countOrdersAsk;
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

    public BigDecimal getHighest_bidPrice() {
        return this.highest_bidPrice;
    }

    public BigDecimal getLower_askPrice() {
        return this.lower_askPrice;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public BigDecimal getPriceChange() {
        return price_change_percent_24h;
    }

    public long getLastTime() {
        return lastTime;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject pair = new JSONObject();
        pair.put("id", viewID());
        pair.put("base_id", assetKey1);
        if (asset1 != null) {
            pair.put("base_name", asset1.viewName());
        }
        pair.put("quote_id", assetKey2);
        if (asset2 != null) {
            pair.put("quote_name", asset2.viewName());
        }

        pair.put("last_price", lastPrice);
        pair.put("last_time", lastTime);
        pair.put("price_change_percent_24h", price_change_percent_24h);

        pair.put("lowest_ask", lower_askPrice);
        pair.put("highest_bid", highest_bidPrice);

        pair.put("base_volume", base_volume);
        pair.put("quote_volume", quote_volume);
        pair.put("count_24h", count24);

        pair.put("countOrdersBid", countOrdersBid);
        pair.put("countOrdersAsk", countOrdersAsk);

        pair.put("lowest_price_24h", lowest_price_24h);
        pair.put("highest_price_24h", highest_price_24h);

        pair.put("frozen", 0);

        pair.put("update_time", updateTime);

        return pair;

    }


    //PARSE/CONVERT
    public static TradePair parse(byte[] data) throws Exception {
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

        //READ lowest_price_24h
        scale = data[position++];
        BigDecimal lowest_price_24h = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH)), scale);
        position += AMOUNT_LENGTH;

        //READ highest_price_24h
        scale = data[position++];
        BigDecimal highest_price_24h = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH)), scale);
        position += AMOUNT_LENGTH;

        //READ COUNT 24
        int count24 = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + Integer.BYTES));
        position += Integer.BYTES;

        //READ COUNT 24
        int countOrdersBid = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + Integer.BYTES));
        position += Integer.BYTES;

        //READ COUNT 24
        int countOrdersAsk = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + Integer.BYTES));
        position += Integer.BYTES;

        //READ UPDATE TIME
        Long updateTime = Longs.fromByteArray(Arrays.copyOfRange(data, position, position + ASSET_KEY_LENGTH));
        position += ASSET_KEY_LENGTH;

        return new TradePair(assetKey1, assetKey2, assetScale1, assetScale2, lastPrice, lastTime, bidPrice, askPrice,
                baseVolume, quoteVolume, price_change_percent_24h,
                lowest_price_24h, highest_price_24h, count24, updateTime, countOrdersBid, countOrdersAsk);
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
        data = Bytes.concat(data, new byte[]{(byte) this.highest_bidPrice.scale()});
        data = Bytes.concat(data, Longs.toByteArray(highest_bidPrice.unscaledValue().longValue()));

        // ask price
        data = Bytes.concat(data, new byte[]{(byte) this.lower_askPrice.scale()});
        data = Bytes.concat(data, Longs.toByteArray(lower_askPrice.unscaledValue().longValue()));

        // base volume
        data = Bytes.concat(data, new byte[]{(byte) this.base_volume.scale()});
        data = Bytes.concat(data, Longs.toByteArray(base_volume.unscaledValue().longValue()));

        // quote volume
        data = Bytes.concat(data, new byte[]{(byte) this.quote_volume.scale()});
        data = Bytes.concat(data, Longs.toByteArray(quote_volume.unscaledValue().longValue()));

        // PRICE _change_percent_24h
        data = Bytes.concat(data, new byte[]{(byte) this.price_change_percent_24h.scale()});
        data = Bytes.concat(data, Longs.toByteArray(price_change_percent_24h.unscaledValue().longValue()));

        // lowest_price_24h
        data = Bytes.concat(data, new byte[]{(byte) this.lowest_price_24h.scale()});
        data = Bytes.concat(data, Longs.toByteArray(lowest_price_24h.unscaledValue().longValue()));

        // highest_price_24h
        data = Bytes.concat(data, new byte[]{(byte) this.highest_price_24h.scale()});
        data = Bytes.concat(data, Longs.toByteArray(highest_price_24h.unscaledValue().longValue()));

        // count 24
        data = Bytes.concat(data, Ints.toByteArray(this.count24));

        // countOrdersBid
        data = Bytes.concat(data, Ints.toByteArray(this.countOrdersBid));

        // countOrdersAsk
        data = Bytes.concat(data, Ints.toByteArray(this.countOrdersAsk));

        // last time
        data = Bytes.concat(data, Longs.toByteArray(this.updateTime));

        return data;
    }

    public int getDataLength() {
        return BASE_LENGTH;
    }

    @Override
    public int hashCode() {
        return assetKey1.hashCode() + assetKey2.hashCode() + HashCode.fromLong(lastTime).asInt();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof TradePair) {
            TradePair tradePair = (TradePair) object;

            return (tradePair.assetKey1.equals(this.assetKey1) && tradePair.assetKey2.equals(this.assetKey2)
                    && tradePair.lastTime == lastTime);
        }

        return false;
    }

    public static TradePair reverse(TradePair tradePair) {

        BigDecimal lastPriceRev = Order.calcPrice(BigDecimal.ONE, tradePair.lastPrice, tradePair.assetScale1);
        BigDecimal highest_bidPriceRev = Order.calcPrice(BigDecimal.ONE, tradePair.lower_askPrice, tradePair.assetScale1);
        BigDecimal lower_askPriceRev = Order.calcPrice(BigDecimal.ONE, tradePair.highest_bidPrice, tradePair.assetScale1);

        BigDecimal highest_price_24hRev = Order.calcPrice(BigDecimal.ONE, tradePair.lowest_price_24h, tradePair.assetScale1);
        BigDecimal lowest_price_24hRev = Order.calcPrice(BigDecimal.ONE, tradePair.highest_price_24h, tradePair.assetScale1);

        if (tradePair.asset1 != null && tradePair.asset2 != null) {
            return new TradePair(
                    tradePair.asset2, tradePair.asset1, // reversed
                    lastPriceRev, // 1/
                    tradePair.lastTime, // same
                    highest_bidPriceRev, lower_askPriceRev, // 1/
                    tradePair.quote_volume, tradePair.base_volume, // reversed
                    tradePair.price_change_percent_24h, // same
                    lowest_price_24hRev, highest_price_24hRev, // 1/
                    tradePair.count24, tradePair.updateTime, // same
                    tradePair.countOrdersAsk, tradePair.countOrdersBid // reversed
            );
        } else {
            return new TradePair(
                    tradePair.assetKey2, tradePair.assetKey1, tradePair.assetScale2, tradePair.assetScale1, // reversed
                    lastPriceRev, // 1/
                    tradePair.lastTime, // same
                    highest_bidPriceRev, lower_askPriceRev, // 1/
                    tradePair.quote_volume, tradePair.base_volume, // reversed
                    tradePair.price_change_percent_24h, // same
                    lowest_price_24hRev, highest_price_24hRev, // 1/
                    tradePair.count24, tradePair.updateTime, // same
                    tradePair.countOrdersAsk, tradePair.countOrdersBid // reversed
            );
        }
    }

    @Override
    public String toString() {
        return viewID();
    }

}
