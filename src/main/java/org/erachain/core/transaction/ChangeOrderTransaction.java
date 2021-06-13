package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.OrderProcess;
import org.erachain.core.item.assets.Trade;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Закрывает родительский Заказ и создает новый.
 * При этом создает Сделку  с типом Измена Заказа, в которую вставляет новое Хочу
 */
public class ChangeOrderTransaction extends Transaction {
    public static final byte TYPE_ID = (byte) Transaction.CHANGE_ORDER_TRANSACTION;
    public static final String TYPE_NAME = "Change Order";

    private static final int AMOUNT_LENGTH = CreateOrderTransaction.AMOUNT_LENGTH;

    private static final int LOAD_LENGTH = AMOUNT_LENGTH + SIGNATURE_LENGTH;
    private static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + LOAD_LENGTH;
    private static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + LOAD_LENGTH;
    private static final int BASE_LENGTH = Transaction.BASE_LENGTH + LOAD_LENGTH;
    private static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH;

    byte[] orderRef;
    private BigDecimal amountWant;

    long orderID;
    //private CreateOrderTransaction createOrderTx;
    private Order order;

    /**
     * @param typeBytes
     * @param creator
     * @param orderRef   signature of Creating or last Changing Order transaction
     * @param amountWant
     * @param feePow
     * @param timestamp
     * @param reference
     */
    public ChangeOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] orderRef,
                                  BigDecimal amountWant, byte feePow, long timestamp, Long reference) {
        super(typeBytes, TYPE_NAME, creator, null, feePow, timestamp, reference);

        this.orderRef = orderRef;
        this.amountWant = amountWant;

    }

    public ChangeOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] orderRef,
                                  BigDecimal amountWant, byte feePow, long timestamp, Long reference,
                                  byte[] signature) {
        this(typeBytes, creator, orderRef, amountWant, feePow, timestamp, reference);
        this.signature = signature;

    }

    public ChangeOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] orderRef,
                                  BigDecimal amountWant, byte feePow, long timestamp, Long reference,
                                  byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, orderRef, amountWant, feePow, timestamp, reference);
        this.signature = signature;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);

    }

    public ChangeOrderTransaction(PublicKeyAccount creator, byte[] orderRef,
                                  BigDecimal amountWant, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, orderRef, amountWant, feePow, timestamp, reference,
                signature);
    }

    public ChangeOrderTransaction(PublicKeyAccount creator, byte[] orderRef,
                                  BigDecimal amountWant, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, orderRef, amountWant, feePow, timestamp,
                reference);
    }

    // GETTERS/SETTERS

    @Override
    public void updateFromStateDB() {
        if (order == null) {
            // возможно для блокэксплорера нужно - если ордер уже сыграл
            order = dcSet.getCompletedOrderMap().get(orderID);
        }
    }

    public void setDC(DCSet dcSet, boolean andUpdateFromState) {

        super.setDC(dcSet, false);

        orderID = dcSet.getTransactionFinalMapSigns().get(orderRef);
        //createOrderTx = (CreateOrderTransaction) dcSet.getTransactionFinalMap().get(orderID);

        // при откате может быть НУЛЬ
        order = dcSet.getOrderMap().get(orderID);

        if (andUpdateFromState && !isWiped())
            updateFromStateDB();

    }

    public String getTitle() {
        return //TYPE_NAME + " " +
                ItemCls.getItemTypeAndKey(ItemCls.ASSET_TYPE, order.getHaveAssetKey())
                        + " " + ItemCls.getItemTypeAndKey(ItemCls.ASSET_TYPE, order.getWantAssetKey());
    }

    @Override
    public String viewAmount() {
        return this.amountWant.toPlainString();
    }

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {

        long long_fee = super.calcBaseFee(withFreeProtocol);
        if (long_fee == 0)
            return 0L;

        return 250L * BlockChain.FEE_PER_BYTE;

    }

    public Long getOrderId() {
        return orderID;
    }

    @Override
    public BigDecimal getAmount() {
        return this.amountWant;
    }

    @Override
    public long getKey() {
        return order.getHaveAssetKey();
    }

    @Override
    public long getAssetKey() {
        return order.getHaveAssetKey();
    }

    public byte[] getOrderRef() {
        return this.orderRef;
    }

    public BigDecimal getAmountWant() {
        return this.amountWant;
    }

    public BigDecimal getPriceCalc() {
        // precision bad return Order.calcPrice(this.amountHave, this.amountWant);
        return makeUpdatedOrder().calcPrice();
    }

    public BigDecimal getPriceCalcReverse() {
        //return Order.calcPrice(this.amountWant, this.amountHave);
        return makeUpdatedOrder().calcPriceReverse();
    }

    @Override
    public boolean hasPublicText() {
        return false;
    }

    // PARSE CONVERT

    public Order makeUpdatedOrder() {
        return new Order(order, dbRef, this.amountWant);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        // GET BASE
        JSONObject json = this.getJsonBase();

        json.put("order", Base58.encode(orderRef));
        json.put("amountWant", this.amountWant.toPlainString());


        return json;
    }

    public static Transaction Parse(byte[] data, int forDeal) throws Exception {
        //boolean asPack = releaserReference != null;

        // CHECK IF WE MATCH BLOCK LENGTH
        int test_len;
        if (forDeal == Transaction.FOR_MYPACK) {
            test_len = BASE_LENGTH_AS_MYPACK;
        } else if (forDeal == Transaction.FOR_PACK) {
            test_len = BASE_LENGTH_AS_PACK;
        } else if (forDeal == Transaction.FOR_DB_RECORD) {
            test_len = BASE_LENGTH_AS_DBRECORD;
        } else {
            test_len = BASE_LENGTH;
        }

        if (data.length < test_len) {
            throw new Exception("Data does not match block length " + data.length);
        }

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        long timestamp = 0;
        if (forDeal > Transaction.FOR_MYPACK) {
            //READ TIMESTAMP
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            timestamp = Longs.fromByteArray(timestampBytes);
            position += TIMESTAMP_LENGTH;
        }

        //READ REFERENCE
        byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
        Long reference = Longs.fromByteArray(referenceBytes);
        position += REFERENCE_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        ExLink exLink;
        if ((typeBytes[2] & HAS_EXLINK_MASK) > 0) {
            exLink = ExLink.parse(data, position);
            position += exLink.length();
        } else {
            exLink = null;
        }

        byte feePow = 0;
        if (forDeal > Transaction.FOR_PACK) {
            // READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        // READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        long feeLong = 0;
        long seqNo = 0;
        if (forDeal == FOR_DB_RECORD) {
            //READ SEQ_NO
            byte[] seqNoBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            seqNo = Longs.fromByteArray(seqNoBytes);
            position += TIMESTAMP_LENGTH;

            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

        // READ ORDER TX SIGNATURE
        byte[] orderRef = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        // READ AMOUNT WANT
        byte[] amountWantBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
        BigDecimal amountWant = new BigDecimal(new BigInteger(amountWantBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
        position += AMOUNT_LENGTH;
        // CHECK ACCURACY of AMOUNT
        int accuracy = typeBytes[3] & TransactionAmount.SCALE_MASK;
        if (accuracy > 0) {
            if (accuracy >= TransactionAmount.SCALE_MASK_HALF) {
                accuracy -= TransactionAmount.SCALE_MASK + 1;
            }

            // RESCALE AMOUNT
            amountWant = amountWant.scaleByPowerOfTen(-accuracy);
        }

        return new ChangeOrderTransaction(typeBytes, creator, orderRef, amountWant, feePow, timestamp,
                reference, signatureBytes, seqNo, feeLong);
    }

    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = super.toBytes(forDeal, withSignature);

        // WRITE ORDER REF
        data = Bytes.concat(data, this.orderRef);

        BigDecimal amountBase;
        // WRITE ACCURACY of AMOUNT WANT
        int different_scale = this.amountWant.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
        if (different_scale != 0) {
            // RESCALE AMOUNT
            amountBase = this.amountWant.scaleByPowerOfTen(different_scale);
            if (different_scale < 0)
                different_scale += TransactionAmount.SCALE_MASK + 1;

            data[3] = (byte) (data[3] | different_scale);
        } else {
            amountBase = this.amountWant;
        }
        // WRITE AMOUNT WANT
        byte[] amountWantBytes = amountBase.unscaledValue().toByteArray();
        byte[] fill_W = new byte[AMOUNT_LENGTH - amountWantBytes.length];
        amountWantBytes = Bytes.concat(fill_W, amountWantBytes);
        data = Bytes.concat(data, amountWantBytes);

        return data;
    }

    // VALIDATE

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        int base_len;
        if (forDeal == FOR_MYPACK)
            base_len = BASE_LENGTH_AS_MYPACK;
        else if (forDeal == FOR_PACK)
            base_len = BASE_LENGTH_AS_PACK;
        else if (forDeal == FOR_DB_RECORD)
            base_len = BASE_LENGTH_AS_DBRECORD;
        else
            base_len = BASE_LENGTH;

        if (exLink != null)
            base_len += exLink.length();

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        return base_len;
    }

    @Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        if (orderID == 0L) {
            return ORDER_DOES_NOT_EXIST;
        }

        if (!order.getCreator().equals(creator)) {
            return INVALID_CREATOR;
        }

        // CHECK IF AMOUNT POSITIVE
        if (amountWant.signum() <= 0) {
            return NEGATIVE_AMOUNT;
        }
        if (amountWant.compareTo(order.getAmountWant()) == 0) {
            return INVALID_AMOUNT;
        }

        // for PARSE and toBYTES need only AMOUNT_LENGTH bytes
        // and SCALE
        byte[] amountBytes;
        amountBytes = amountWant.unscaledValue().toByteArray();
        if (amountBytes.length > AMOUNT_LENGTH) {
            return AMOUNT_LENGHT_SO_LONG;
        }
        int scale = this.amountWant.scale();
        if (scale < TransactionAmount.minSCALE
                || scale > TransactionAmount.maxSCALE) {
            return AMOUNT_SCALE_WRONG;
        }
        scale = this.amountWant.stripTrailingZeros().scale();
        if (scale > order.getWantAssetScale()) {
            return AMOUNT_SCALE_WRONG;
        }

        return super.isValid(forDeal, flags);
    }

    @Override
    public void makeItemsKeys() {
        if (isWiped()) {
            itemsKeys = new Object[][]{};
        }

        if (creatorPersonDuration == null) {
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.ASSET_TYPE, order.getHaveAssetKey()},
                    new Object[]{ItemCls.ASSET_TYPE, order.getWantAssetKey()},
            };
        } else {
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()},
                    new Object[]{ItemCls.ASSET_TYPE, order.getHaveAssetKey()},
                    new Object[]{ItemCls.ASSET_TYPE, order.getWantAssetKey()},
            };
        }
    }

    // PROCESS/ORPHAN

    /**
     * Суть такова что мы делаем новый ордер с новым ID так как иначе сортировка Сделок будет нарушена так как
     * будет по Инициатору ключ, а его мы тогда берем старый ИД. А надо новый чтобы история действий не менялась
     *
     * @param block
     * @param forDeal
     */
    @Override
    public void process(Block block, int forDeal) {
        super.process(block, forDeal);

        // PROCESS UPDATE ORDER

        // удалим
        dcSet.getOrderMap().delete(orderID);
        // делаем его как отмененный - чтобы новый ордер создать
        dcSet.getCompletedOrderMap().put(order);

        // запомним для отчета что цена изменилась
        Trade trade = new Trade(Trade.TYPE_CHANGE,
                dbRef, // номер инициатора по нашему номеру
                orderID, // номер оригинала?
                order.getHaveAssetKey(), order.getWantAssetKey(),
                order.getAmountHave(), amountWant,
                order.getHaveAssetScale(), order.getWantAssetScale(), 1);

        // нужно запомнить чтобы при откате обновить назад цену
        dcSet.getTradeMap().put(trade);

        // изменяемые объекты нужно заново создавать
        Order updatedOrder = makeUpdatedOrder();

        if (order.getAmountWant().compareTo(amountWant) > 0) {
            /// цена уменьшилась - проверим может он сработает
            updatedOrder.setDC(dcSet);
            OrderProcess.process(updatedOrder, block, this);
        } else {
            dcSet.getOrderMap().put(updatedOrder);
        }

    }

    @Override
    public void orphan(Block block, int forDeal) {
        super.orphan(block, forDeal);

        // ORPHAN UPDATE ORDER

        // сделку ищем по ордеру и своему дбРЕФ
        // чтобы восстановить старую цену
        dcSet.getTradeMap().delete(new Fun.Tuple2<>(dbRef, orderID));

        // удалим из исполненных
        Order orderOrig = dcSet.getCompletedOrderMap().remove(orderID);

        if (orderOrig.getAmountWant().compareTo(amountWant) > 0) {
            /// цена уменьшилась - откатим, ведь может он сработал
            Order updatedOrder = OrderProcess.orphan(dcSet, dbRef, block, block == null ? timestamp : block.getTimestamp());
        } else {
            dcSet.getOrderMap().delete(dbRef);
        }

        // добавим в ожидающие
        dcSet.getOrderMap().put(orderOrig);


    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<>(2, 1);
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        return new HashSet<>(1, 1);
    }

    @Override
    public boolean isInvolved(Account account) {

        if (account.equals(this.creator)) {
            return true;
        }

        return false;
    }

    @Override
    public BigDecimal getAmount(Account account) {
        return BigDecimal.ZERO;
    }

    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

        assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);

        return assetAmount;
    }

}
