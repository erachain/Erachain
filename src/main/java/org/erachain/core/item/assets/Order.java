package org.erachain.core.item.assets;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.CompletedOrderMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.OrderMap;
import org.erachain.datachain.TradeMap;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public class Order implements Comparable<Order> {

    private static final MathContext rounding = new java.math.MathContext(12, RoundingMode.HALF_DOWN);

    final private static BigDecimal PRECISION_UNIT = BigDecimal.ONE.scaleByPowerOfTen(-(BlockChain.TRADE_PRECISION));
    // нужно на 1 больше сделать
    final private static BigDecimal PRICE_CLOSESD = BigDecimal.ONE.scaleByPowerOfTen(-(BlockChain.TRADE_PRECISION - 1));

    /**
     * с какого номера блока включить новое округление
     */
    public static final int NEW_FLOR = BlockChain.DEVELOP_USE ? 317000 : BlockChain.VERS_4_12;
    /**
     * с какого блока считаем новое округление в обратную строну. Иначе цена 0,65 не срабатывает и обе заявки стоят в стакане
     */
    public static final int NEW_FLOR2 = BlockChain.DEVELOP_USE ? 317000 : 253000;

    public static final int ID_LENGTH = 8;
    private static final int CREATOR_LENGTH = 20; // as SHORT (old - 25)
    private static final int HAVE_LENGTH = 8;
    private static final int WANT_LENGTH = 8;
    private static final int SCALE_LENGTH = 1;
    private static final int AMOUNT_LENGTH = 8;
    private static final int STATUS_LENGTH = 1;
    public static final int FULFILLED_LENGTH = AMOUNT_LENGTH + 4;
    private static final int BASE_LENGTH = ID_LENGTH + CREATOR_LENGTH + HAVE_LENGTH + WANT_LENGTH
            + 4 * SCALE_LENGTH + 2 * AMOUNT_LENGTH + SCALE_LENGTH + FULFILLED_LENGTH
            + STATUS_LENGTH;

    public static final int UNCONFIRMED = 0;
    public static final int ACTIVE = 1;
    public static final int FULFILLED = 2;
    public static final int COMPLETED = 3;
    public static final int CANCELED = 4;
    public static final int ORPHANED = -1;

    //protected DCSet dcSet;

    /**
     * height[int] + SeqNo[int]
     */
    private Long id;
    private Account creator;
    private long haveAssetKey;
    private BigDecimal amountHave;
    private int haveAssetScale;
    private BigDecimal fulfilledHave;

    private long wantAssetKey;
    private BigDecimal amountWant;
    private int wantAssetScale;

    private BigDecimal price;

    DCSet dcSet;

    int status;

    public Order(DCSet dcSet, Long id, Account creator, long haveAssetKey, BigDecimal haveAmount, int haveAssetScale, long wantAssetKey, BigDecimal wantAmount, int wantAssetScale) {

        this.dcSet = dcSet;
        this.id = id;
        this.creator = creator;
        this.haveAssetKey = haveAssetKey;
        this.wantAssetKey = wantAssetKey;

        this.amountHave = haveAmount;
        this.amountWant = wantAmount;

        this.haveAssetScale = haveAssetScale;
        this.wantAssetScale = wantAssetScale;

        this.fulfilledHave = BigDecimal.ZERO;

        price = calcPrice();
    }

    public Order(Long id, Account creator, long haveAssetKey, BigDecimal haveAmount, int haveAssetScale, BigDecimal fulfilledHave, long wantAssetKey,
                 BigDecimal wantAmount, int wantAssetScale, int status) {

        this.id = id;
        this.creator = creator;
        this.haveAssetKey = haveAssetKey;
        this.wantAssetKey = wantAssetKey;

        this.amountHave = haveAmount;
        this.amountWant = wantAmount;

        this.haveAssetScale = haveAssetScale;
        this.wantAssetScale = wantAssetScale;

        this.fulfilledHave = fulfilledHave;

        this.status = status;

        price = calcPrice();

    }

    //GETTERS/SETTERS

    public static Order getOrder(DCSet db, Long key) {
        if (db.getOrderMap().contains(key)) {
            return db.getOrderMap().get(key);
        }

        if (db.getCompletedOrderMap().contains(key)) {
            return db.getCompletedOrderMap().get(key);
        }

        return null;

    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    public static int powerTen(BigDecimal value) {
        BigDecimal t = value;
        int i = 0;
        while (t.compareTo(BigDecimal.ONE) > 0) {
            t = t.movePointLeft(1);
            i++;
        }
        return i;
    }


    /**
     * проверяем по остаткам - сильно ли съехала цена для них.
     * если сильно то это уже ордер который не исполнится - его нужно отменять.
     * Перед употреблением нужно задать базу
     * <hr>
     * Перед использованием необходимо проверить order.isFulfilled - может он исполнился полностью
     * @return
     */
    public boolean isUnResolved() {

        BigDecimal priceForLeft = calcPrice(amountHave.subtract(fulfilledHave), amountWant.subtract(getFulfilledWant()), wantAssetScale);
        if (priceForLeft.signum() == 0)
            // уже не сошлось
            return true;

        BigDecimal diff = price.subtract(priceForLeft).divide(price,
                wantAssetScale + BlockChain.TRADE_PRECISION + 3, RoundingMode.HALF_DOWN).abs();
        // если разница цены выросла от начального сильно - то
        if (diff.compareTo(PRECISION_UNIT) > 0)
            return true;
        return false;
    }

    /**
     * есди цены в погрешности
     * @param price1
     * @param price2
     * @return
     */
    public static boolean isPricesClose(BigDecimal price1, BigDecimal price2) {

        BigDecimal diff = price1.subtract(price2).divide(price1.min(price2),
                BlockChain.TRADE_PRECISION + 3, RoundingMode.UP).abs();
        if (diff.compareTo(PRICE_CLOSESD) < 0)
            return true;
        return false;
    }

    public boolean willUnResolvedFor(BigDecimal fulfilledHave, BigDecimal fulfilledWant) {
        BigDecimal priceForLeft = calcPrice(amountHave.subtract(fulfilledHave),
                amountWant.subtract(fulfilledWant), wantAssetScale + BlockChain.TRADE_PRECISION);
        BigDecimal diff = price.subtract(priceForLeft).divide(price,
                wantAssetScale + BlockChain.TRADE_PRECISION + 3, RoundingMode.HALF_DOWN).abs();
        // если разница цены выросла от начального сильно - то
        if (diff.compareTo(PRECISION_UNIT) > 0)
            return true;
        return false;
    }

    // BigDecimal.precision() - is WRONG calculating!!! Sometime = 0 for 100 or 10
    public static int precision(BigDecimal value) {
        return powerTen(value) + value.scale();
    }

    public static BigDecimal calcPrice(BigDecimal amountHave, BigDecimal amountWant, int wantScale) {
        // .precision() - WRONG calculating!!!! scalePrice = amountHave.setScale(0, RoundingMode.UP).precision() + scalePrice>0?scalePrice : 0;
        int scalePrice = Order.powerTen(amountHave) + (wantScale > 0 ? wantScale : 0) + (BlockChain.TRADE_PRECISION + 3);
        BigDecimal result = amountWant.divide(amountHave, scalePrice, RoundingMode.HALF_DOWN).stripTrailingZeros();

        // IF SCALE = -1..1 - make error in mapDB - org.mapdb.DataOutput2.packInt(DataOutput, int)
        if (result.scale() < 0)
            return result.setScale(0);
        return result;
    }

    public BigDecimal calcPrice() {
        return calcPrice(amountHave, amountWant, wantAssetScale);
    }

    public BigDecimal calcPriceReverse() {
        return calcPrice(amountWant, amountHave, haveAssetScale);
    }

    public static Order reloadOrder(DCSet dcSet, Long orderID) {

        return dcSet.getCompletedOrderMap().contains(orderID) ?
                dcSet.getCompletedOrderMap().get(orderID) :
                dcSet.getOrderMap().get(orderID);

    }

    public void setDC(DCSet dcSet) {
        this.dcSet = dcSet;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getCreator() {
        return this.creator;
    }

    public long getHaveAssetKey() {
        return this.haveAssetKey;
    }

    public long getWantAssetKey() {
        return this.wantAssetKey;
    }

    /*
    public AssetCls getHaveAsset() {
        if (dcSet == null)
            dcSet = DCSet.getInstance();

        if (haveAsset == null)
            haveAsset = dcSet.getItemAssetMap().get(this.haveAssetKey);

        return haveAsset;
    }

    public AssetCls getWantAsset() {
        if (dcSet == null)
            dcSet = DCSet.getInstance();

        if (wantAsset == null)
            wantAsset = dcSet.getItemAssetMap().get(this.wantAssetKey);

        return wantAsset;
    }
*/

    ///////////////////////// AMOUNTS
    public BigDecimal getAmountHave() {
        return this.amountHave;
    }

    public BigDecimal getAmountWant() {
        return this.amountWant;
    }

    public BigDecimal getAmountHaveLeft() {
        return this.amountHave.subtract(this.fulfilledHave);
    }

    public BigDecimal getAmountWantLeft() {
        // надо округлять до точности актива, иначе из-за более точной цены может точность лишу дать в isUnResolved
        //return this.getAmountHaveLeft().multiply(this.price, rounding).setScale(this.wantAssetScale, RoundingMode.HALF_DOWN);
        return this.getAmountHaveLeft().multiply(this.price).setScale(this.wantAssetScale, RoundingMode.UP);
    }

    //////// FULFILLED
    public BigDecimal getFulfilledHave() {
        return this.fulfilledHave;
    }

    public void setFulfilledHave(BigDecimal fulfilled) {
        this.fulfilledHave = fulfilled;
    }

    public boolean isFulfilled() {
        return this.fulfilledHave.compareTo(this.amountHave) == 0;
    }

    public boolean isActive(DCSet dcSet) {
        return dcSet.getOrderMap().contains(id);
    }

    // TO DO
    public BigDecimal getFulfilledWant() {
        // надо округлять до точности актива, иначе из-за более точной цены может точность лишу дать в isUnResolved
        return this.fulfilledHave.multiply(this.price).setScale(this.wantAssetScale, RoundingMode.UP);
    }

    /**
     * Проверка - если уменьшим то остаток норм все еще?
     * @param fulfilledHave
     * @return
     */
    public BigDecimal willFulfilledWant(BigDecimal fulfilledHave) {
        return fulfilledHave.multiply(this.price); //.setScale(this.wantAssetScale, RoundingMode.HALF_DOWN);
    }

    public String state() {
        if (amountHave.compareTo(fulfilledHave) == 0) {
            return "Done";
        } else {

            if (DCSet.getInstance().getCompletedOrderMap().contains(id))
                return "Canceled";

            if (DCSet.getInstance().getOrderMap().contains(id)) {
                if (fulfilledHave.signum() == 0)
                    return "Active";
                else
                    return "Fulfilled";
            }

            return "Orphaned"; //"unconfirmed";
        }

    }
    ///////// PRICE
    public BigDecimal getPrice() {
        return this.price;
    }

    public String viewPrice() {
        return getPrice().toPlainString();
    }

    public List<Trade> getInitiatedTrades() {
        return this.getInitiatedTrades(DCSet.getInstance());
    }

    public List<Trade> getInitiatedTrades(DCSet db) {
        return db.getTradeMap().getInitiatedTrades(this);
    }

    //PARSE/CONVERT

	// forDB - use fulFill
	public static Order parse(byte[] data) throws Exception
	{
		//CHECK IF CORRECT LENGTH
		if(data.length != BASE_LENGTH)
		{
			throw new Exception("Data does not match order length");
		}

		int position = 0;

		//READ ID
		byte[] idBytes = Arrays.copyOfRange(data, position, position + ID_LENGTH);
        long id = Longs.fromByteArray(idBytes);
		position += ID_LENGTH;

		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		Account creator = Account.makeAccountFromShort(creatorBytes);
		position += CREATOR_LENGTH;

		//READ HAVE
		byte[] haveBytes = Arrays.copyOfRange(data, position, position + HAVE_LENGTH);
		long haveKey = Longs.fromByteArray(haveBytes);
		position += HAVE_LENGTH;

        //READ HAVE
		byte[] wantBytes = Arrays.copyOfRange(data, position, position + WANT_LENGTH);
		long wantKey = Longs.fromByteArray(wantBytes);
		position += WANT_LENGTH;

        //READ HAVE SCALE
        byte scaleHave = Arrays.copyOfRange(data, position, position + 1)[0];
        position ++;

        //READ AMOUNT HAVE
		byte[] amountHaveBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amountHave = new BigDecimal(new BigInteger(amountHaveBytes), scaleHave);
		position += AMOUNT_LENGTH;

        //READ WANT SCALE
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

        //READ FULFILLED HAVE SCALE
        byte scalefulfilledHave = Arrays.copyOfRange(data, position, position + 1)[0];
        position ++;

        //READ FULFILLED HAVE
		byte[] fulfilledHaveBytes = Arrays.copyOfRange(data, position, position + FULFILLED_LENGTH);
		BigDecimal fulfilledHave = new BigDecimal(new BigInteger(fulfilledHaveBytes), scalefulfilledHave);
		position += FULFILLED_LENGTH;

        //READ FULFILLED HAVE
        byte[] statusBytes = Arrays.copyOfRange(data, position, position + STATUS_LENGTH);
        int status = (int)statusBytes[0];
        position += STATUS_LENGTH;

        return new Order(id, creator, haveKey, amountHave, haveAssetScale, fulfilledHave, wantKey, amountWant, wantAssetScale, status);

	}

	public byte[] toBytes()
	{
		byte[] data = new byte[0];

		//WRITE ID
		byte[] idBytes = Longs.toByteArray(this.id);
		byte[] fill = new byte[ID_LENGTH - idBytes.length];
		idBytes = Bytes.concat(fill, idBytes);
		data = Bytes.concat(data, idBytes);

		//WRITE CREATOR
		try
		{
			data = Bytes.concat(data , this.creator.getShortAddressBytes());
		}
		catch(Exception e)
		{
			//DECODE EXCEPTION
		}

		//WRITE HAVE KEY
		byte[] haveBytes = Longs.toByteArray(this.haveAssetKey);
        // only for BIGInteger and BigDecimal it need:
		//haveBytes = Bytes.ensureCapacity(haveBytes, HAVE_LENGTH, 0);
		data = Bytes.concat(data, haveBytes);

		//WRITE WANT KEY
		byte[] wantBytes = Longs.toByteArray(this.wantAssetKey);
		// only for BIGInteger and BigDecimal it need:
        // wantBytes = Bytes.ensureCapacity(wantBytes, WANT_LENGTH, 0);
		data = Bytes.concat(data, wantBytes);

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

        // TRY CUT SCALE
        byte[] fulfilledHaveBytes = this.fulfilledHave.unscaledValue().toByteArray();
        while (fulfilledHaveBytes.length > FULFILLED_LENGTH) {
            this.fulfilledHave.setScale(this.fulfilledHave.scale() - 1, BigDecimal.ROUND_HALF_UP);
            fulfilledHaveBytes = this.fulfilledHave.unscaledValue().toByteArray();
        }

        //WRITE AMOUNT HAVE SCALE
        data = Bytes.concat(data, new byte[]{(byte)this.fulfilledHave.scale()});

		//WRITE FULFILLED HAVE
		///fulfilledHaveBytes = this.fulfilledHave.unscaledValue().toByteArray();
		fill = new byte[FULFILLED_LENGTH - fulfilledHaveBytes.length];
		fulfilledHaveBytes = Bytes.concat(fill, fulfilledHaveBytes);
		data = Bytes.concat(data, fulfilledHaveBytes);

        //WRITE STATUS
        data = Bytes.concat(data, new byte[]{(byte)this.status});

        return data;
	}

	public int getDataLength()
	{
		return BASE_LENGTH;
	}

    public boolean isConfirmed() {
        return isConfirmed(DCSet.getInstance());
    }

    public boolean isConfirmed(DCSet dc) {
        return dc.getOrderMap().contains(this.id)
                || dc.getCompletedOrderMap().contains(this.id);
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject order = new JSONObject();
        order.put("id", this.id);
        order.put("creator", this.creator.getAddress());
        order.put("haveAssetKey", this.haveAssetKey);
        order.put("wantAssetKey", this.wantAssetKey);
        order.put("amountHave", this.amountHave.toPlainString());
        order.put("amountWant", this.amountWant.toPlainString());
        order.put("fulfilledHave", this.fulfilledHave.toPlainString());
        order.put("price", this.price.toPlainString());
        order.put("status", this.status);

        return order;

    }

    //PROCESS/ORPHAN

    /**
     * пересчет если ордер невозможно уже исполнить
     */
    public void processOnUnresolved(Block block, Transaction transaction) {
        // REVERT not completed AMOUNT
        this.creator.changeBalance(this.dcSet, false,
                this.haveAssetKey, this.getAmountHaveLeft(), false);
        transaction.addCalculated(block, this.creator, this.haveAssetKey, this.getAmountHaveLeft(),
                "ended order @" + transaction.viewDBRef(this.id));

    }

    /**
     * Поидее тут ордер активный должен себе получить лучшие условия если округление пошло в строну,
     * так как он в мнее выгодных условиях по цене
     * @param block
     * @param transaction
     */
    public void process(Block block, Transaction transaction) {

        // GET HEIGHT from ID
        int height = (int) (this.id >> 32);

        CompletedOrderMap completedMap = this.dcSet.getCompletedOrderMap();
        OrderMap ordersMap = this.dcSet.getOrderMap();
        TradeMap tradesMap = this.dcSet.getTradeMap();

        boolean debug = false;

        if (//this.creator.equals("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5") &&
                //this.id.equals(Transaction.makeDBRef(12435, 1))
                //this.id.equals(770667456757788l) // 174358
                height == 174358 // 133236 //  - тут остаток неисполнимый и у ордера нехватка - поэтому иницалицирующий отменяется
                //|| height == 133232 // - здесь хвостики какието у сделки с 1 в последнем знаке
                //|| height == 253841 // сработал NEW_FLOR 2-й
                //|| height == 255773 // тут мизерные остатки - // 70220 - 120.0000234 - обратный сработал
        //|| (this.haveAssetKey == 12L && this.wantAssetKey == 95L)
                //|| (this.wantAssetKey == 95L && this.haveAssetKey == 12L)
                //Arrays.equals(Base58.decode("3PVq3fcMxEscaBLEYgmmJv9ABATPasYjxNMJBtzp4aKgDoqmLT9MASkhbpaP3RNPv8CECmUyH5sVQtEAux2W9quA"), transaction.getSignature())
                //Arrays.equals(Base58.decode("2GnkzTNDJtMgDHmKKxkZSQP95S7DesENCR2HRQFQHcspFCmPStz6yn4XEnpdW4BmSYW5dkML6xYZm1xv7JXfbfNz"), transaction.getSignature()
                //this.id.equals(new BigInteger(Base58.decode("4NxUYDifB8xuguu5gVkma4V1neseHXYXhFoougGDzq9m7VdZyn7hjWUYiN6M7vkj4R5uwnxauoxbrMaavRMThh7j")))
                //&& !db.isFork()
                ) {
            debug = true;
        }

        ////// NEED FOR making secondary keys in TradeMap
        /// not need now ordersMap.add(this);

        //REMOVE HAVE
        //this.creator.setBalance(this.have, this.creator.getBalance(db, this.have).subtract(this.amountHave), db);
        this.creator.changeBalance(this.dcSet, true, this.haveAssetKey, this.amountHave, true);

        BigDecimal thisPrice = this.price;

        //BigDecimal tempPrice;
        BigDecimal thisIncrement;
        //boolean isReversePrice = thisPrice.compareTo(BigDecimal.ONE) < 0;

        //GET ALL ORDERS(WANT, HAVE) LOWEST PRICE FIRST
        //TRY AND COMPLETE ORDERS
        List<Order> orders = ordersMap.getOrdersForTradeWithFork(this.wantAssetKey, this.haveAssetKey, false);

        /// ЭТО ПРОВЕРКА на правильную сортировку - все пашет
        if (false && !orders.isEmpty()) {
            BigDecimal price = orders.get(0).getPrice();
            Long timestamp = orders.get(0).getId();
            for (Order item: orders) {
                if (item.getHaveAssetKey() != this.wantAssetKey
                        || item.getWantAssetKey() != this.haveAssetKey) {
                    // RISE ERROR
                    timestamp = null;
                    ++timestamp;
                }
                // потому что сранивается потом обратная цена то тут должно быть возрастание
                // и если не так то ошибка
                int comp = price.compareTo(item.getPrice());
                if (comp > 0) {
                    // RISE ERROR
                    timestamp = null;
                    ++timestamp;
                } else if (comp == 0) {
                    // здесь так же должно быть возростание
                    // если не так то ошибка
                    if (timestamp.compareTo(item.getId()) > 0) {
                        // RISE ERROR
                        timestamp = null;
                        ++timestamp;
                    }
                }

                price = item.getPrice();
                timestamp = item.getId();
            }

        }

        BigDecimal thisAmountHaveLeft = this.getAmountHaveLeft();
        BigDecimal processedAmountFulfilledWant = BigDecimal.ZERO;

        int compare = 0;
        int compareLeft = 0;

        if (debug) {
            debug = true;
        }

        boolean completedOrder = false;
        int index = 0;

        while (!completedOrder && index < orders.size()) {
            //GET ORDER
            Order order;
            if (this.dcSet.inMemory()) {
                // так как это все в памяти расположено то нужно создать новый объект
                // иначе везде будет ссылка на один и тот же объект и
                // при переходе на MAIN базу возьмется уже обновленный ордер из памяти с уже пересчитанными остатками
                order = orders.get(index).copy();
            } else {
                order = orders.get(index);
            }

            index++;

            if (debug) {
                debug = true;
            }

            BigDecimal orderAmountHaveLeft;
            BigDecimal orderAmountWantLeft;
            // REVERSE
            BigDecimal orderReversePrice = order.calcPriceReverse();
            // PRICE
            ///BigDecimal orderPrice = Order.calcPrice(order.amountHave, order.amountWant, haveAssetScale);
            BigDecimal orderPrice = order.price;

            Trade trade;
            BigDecimal tradeAmountForHave;
            BigDecimal tradeAmountForWant; // GET
            BigDecimal tradeAmountAccurate;
            BigDecimal differenceTrade;
            //BigDecimal differenceTradeThis;

            /////////////// - разность точности цены из-за того что у одного ордера значение больше на порядки и этот порядок в точность уходит
            //CHECK IF BUYING PRICE IS HIGHER OR EQUAL THEN OUR SELLING PRICE
            compare = thisPrice.compareTo(orderReversePrice);
            if (compare > 0) {
                // Делаем просто проверку на обратную цену и все - без игр с округлением и проверки дополнительной
                BigDecimal thisReversePrice = calcPriceReverse();
                // и сравним так же по прямой цене со сниженной точностью у Заказа
                if (orderPrice.compareTo(thisReversePrice) == 0) {
                    compare = 0;
                } else {
                    break;
                }
            }

            orderAmountHaveLeft = order.getAmountHaveLeft();
            // SCALE for HAVE in ORDER
            // цену ему занижаем так как это держатель позиции
            if (order.fulfilledHave.signum() == 0) {
                orderAmountWantLeft = order.amountWant;
            } else {
                orderAmountWantLeft = orderAmountHaveLeft.multiply(orderPrice).setScale(haveAssetScale, RoundingMode.DOWN);
            }

            compareLeft = orderAmountWantLeft.compareTo(thisAmountHaveLeft);
            if (compareLeft <= 0) {

                // У позиции меньше чем нам надо - берем все данные с позиции
                tradeAmountForHave = orderAmountHaveLeft;
                tradeAmountForWant = orderAmountWantLeft;

                if (compareLeft == 0)
                    completedOrder = true;

            } else {

                tradeAmountForWant = thisAmountHaveLeft;

                if (debug) {
                    debug = true;
                }

                if (compare == 0) {
                    // цена совпала (возможно с округлением) то без пересчета берем что раньше посчитали
                    tradeAmountForHave = this.getAmountWantLeft();

                } else {

                    // RESOLVE amount with SCALE
                    // тут округляем наоборот вверх - больше даем тому кто активный
                    tradeAmountForHave = tradeAmountForWant.multiply(orderReversePrice).setScale(wantAssetScale, RoundingMode.UP);
                    if (tradeAmountForHave.compareTo(orderAmountHaveLeft) >= 0) {
                        // если вылазим после округления за предел то берем что есть
                        tradeAmountForHave = orderAmountHaveLeft;

                    } else {

                        if (debug) {
                            debug = true;
                        }

                        // если там сотаток слишком маленький то добавим его в сделку
                        // так как выше было округление и оно могло чуточку недотянуть
                        if (order.willUnResolvedFor(tradeAmountForHave, tradeAmountForWant)) {
                            BigDecimal reversePriceUnResolved =
                                    calcPrice(tradeAmountForWant, tradeAmountForHave, wantAssetScale);
                            if (Order.isPricesClose(orderReversePrice, reversePriceUnResolved)) {
                                tradeAmountForHave = orderAmountHaveLeft;
                            }

                            // проверим еще раз может вылезло за рамки
                            if (tradeAmountForHave.compareTo(orderAmountHaveLeft) > 0) {
                                // если вылазим после округления за предел то берем что есть
                                tradeAmountForHave = orderAmountHaveLeft;
                            }
                        }
                    }

                }

                //THIS is COMPLETED
                completedOrder = true;

            }

            if (tradeAmountForHave.compareTo(BigDecimal.ZERO) <= 0
                || tradeAmountForWant.compareTo(BigDecimal.ZERO) <= 0) {
                debug = true;
                Long error = null;
                error ++;
            }

                //CHECK IF AMOUNT AFTER ROUNDING IS NOT ZERO
            //AND WE CAN BUY ANYTHING
            if (tradeAmountForHave.compareTo(BigDecimal.ZERO) > 0) {
                //CREATE TRADE

                // CUT PRECISION in bytes
                tradeAmountForHave = tradeAmountForHave.stripTrailingZeros();
                byte[] amountBytes = tradeAmountForHave.unscaledValue().toByteArray();
                while (amountBytes.length > FULFILLED_LENGTH) {
                    tradeAmountForHave.setScale(tradeAmountForHave.scale() - 1, BigDecimal.ROUND_HALF_UP);
                    amountBytes = tradeAmountForHave.unscaledValue().toByteArray();
                }
                tradeAmountForWant = tradeAmountForWant.stripTrailingZeros();
                amountBytes = tradeAmountForWant.unscaledValue().toByteArray();
                while (amountBytes.length > FULFILLED_LENGTH) {
                    tradeAmountForWant.setScale(tradeAmountForWant.scale() - 1, BigDecimal.ROUND_HALF_UP);
                    amountBytes = tradeAmountForWant.unscaledValue().toByteArray();
                }

                if (debug) {
                    debug = true;
                }

                //////////////////////////// TRADE /////////////////
                if (tradeAmountForHave.scale() > wantAssetScale
                || tradeAmountForWant.scale() > haveAssetScale) {
                    Long error = null;
                    error ++;
                }
                if (tradeAmountForHave.signum() <= 0
                        || tradeAmountForWant.signum() < 0) {
                    Long error = null;
                    error ++;
                }
                trade = new Trade(this.getId(), order.getId(), this.haveAssetKey, this.wantAssetKey,
                        tradeAmountForHave, tradeAmountForWant,
                        haveAssetScale, wantAssetScale, index);

                //ADD TRADE TO DATABASE
                tradesMap.add(trade);

                //UPDATE FULFILLED HAVE
                order.setFulfilledHave(order.getFulfilledHave().add(tradeAmountForHave)); // this.amountHave));
                this.setFulfilledHave(this.getFulfilledHave().add(tradeAmountForWant)); //this.amountWant));

                if (order.isFulfilled()) {
                    //REMOVE FROM ORDERS
                    ordersMap.delete(order);

                    //ADD TO COMPLETED ORDERS
                    completedMap.add(order);
                } else {
                    //UPDATE ORDER
                    if (order.isUnResolved()) {
                        // if left not enough for 1 buy by price this order
                        order.dcSet = dcSet;
                        order.processOnUnresolved(block, transaction);

                        //REMOVE FROM ORDERS
                        ordersMap.delete(order);

                        //ADD TO COMPLETED ORDERS
                        completedMap.add(order);
                    } else {
                        ordersMap.add(order);
                    }
                }

                //TRANSFER FUNDS
                order.getCreator().changeBalance(this.dcSet, false, order.wantAssetKey, tradeAmountForWant, false);
                transaction.addCalculated(block, order.getCreator(), order.getWantAssetKey(), tradeAmountForWant,
                        "order @" + Transaction.viewDBRef(order.id));

                // Учтем что у стороны ордера обновилась форжинговая информация
                if (order.wantAssetKey == Transaction.RIGHTS_KEY && block != null) {
                    block.addForgingInfoUpdate(order.getCreator());
                }

                // update new values
                thisAmountHaveLeft = this.getAmountHaveLeft();
                processedAmountFulfilledWant = processedAmountFulfilledWant.add(tradeAmountForHave);

                if (debug) {
                    debug = true;
                }

                if (completedOrder)
                    break;

                // возможно схлопнулся?
                if (isFulfilled()) {
                    completedOrder = true;
                    break;
                }

                // if can't trade by more good price than self - by orderOrice - then  auto cancel!
                if (this.isUnResolved()) {

                    // cancel order if it not fulfiled isDivisible

                    // or HAVE not enough to one WANT  = price
                    ///CancelOrderTransaction.process_it(this.dcSet, this);
                    //and stop resolve
                    completedOrder = true;
                    // REVERT not completed AMOUNT
                    processOnUnresolved(block, transaction);
                    break;
                }

            }
        }

        if (debug) {
            debug = true;
        }

        if (!completedOrder) {
            ordersMap.add(this);
        } else {
            completedMap.add(this);
        }

        //TRANSFER FUNDS
        if (processedAmountFulfilledWant.signum() > 0) {
            this.creator.changeBalance(this.dcSet, false, this.wantAssetKey, processedAmountFulfilledWant, false);
            transaction.addCalculated(block, this.creator, this.wantAssetKey, processedAmountFulfilledWant,
                    "order @" + Transaction.viewDBRef(this.id));
        }


    }

    public void orphan(Block block) {

        CompletedOrderMap completedMap = this.dcSet.getCompletedOrderMap();
        OrderMap ordersMap = this.dcSet.getOrderMap();
        TradeMap tradesMap = this.dcSet.getTradeMap();

        //CHECK IF ORDER IS FULFILLED
        if (this.isFulfilled()) {
            //REMOVE FROM COMPLETED ORDERS
            completedMap.delete(this);
        }

        BigDecimal thisAmountFulfilledWant = BigDecimal.ZERO;

        //ORPHAN TRADES
        for (Trade trade : this.getInitiatedTrades(this.dcSet)) {
            Order target = trade.getTargetOrder(this.dcSet);

            //REVERSE FUNDS
            BigDecimal tradeAmountHave = trade.getAmountHave();
            BigDecimal tradeAmountWant = trade.getAmountWant();

            if (target.isFulfilled()) {
                //DELETE FROM COMPLETED ORDERS
                completedMap.delete(target);
            }

            //REVERSE FULFILLED
            target.setFulfilledHave(target.getFulfilledHave().subtract(tradeAmountHave));
            thisAmountFulfilledWant = thisAmountFulfilledWant.add(tradeAmountHave);

            target.getCreator().changeBalance(this.dcSet, true, target.wantAssetKey, tradeAmountWant, false);

            // Учтем что у стороны ордера обновилась форжинговая информация
            if (target.wantAssetKey == Transaction.RIGHTS_KEY && block != null) {
                block.addForgingInfoUpdate(target.getCreator());
            }

            //UPDATE ORDERS
            ordersMap.add(target);

            //REMOVE TRADE FROM DATABASE
            tradesMap.delete(trade);
        }

        //REMOVE ORDER FROM DATABASE
        ordersMap.delete(this);

        //REMOVE HAVE
        // GET HAVE LEFT - if it CANCELWED by INCREMENT close
        //   - если обработка остановлена по достижению порога Инкремента
        this.creator.changeBalance(this.dcSet, false, this.haveAssetKey, this.getAmountHaveLeft(), true);
        //REVERT WANT
        this.creator.changeBalance(this.dcSet, true, this.wantAssetKey, thisAmountFulfilledWant, false);
    }

    @Override
    public int hashCode() {
        return id.hashCode() - fulfilledHave.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Order) {
            Order order = (Order) object;
            // только если Ид сопало и Остаок тоже - так чтобы в базе данных при обновлении выскакивало событие
            if (id.equals(order.id) && fulfilledHave.equals(order.fulfilledHave)) {
                return true;
            }
        }

        return false;
    }

    //COMPARE
    @Override
    public int compareTo(Order order) {
        //COMPARE ONLY BY PRICE
        int result = this.getPrice().compareTo(order.getPrice());
        if (result != 0)
            return result;

        return this.id.compareTo(order.getId());
    }

    @Override
    public String toString() {
        return this.id.toString() + "-" + this.haveAssetKey + "/" + this.wantAssetKey;
    }

    //COPY
    public Order copy() {
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
