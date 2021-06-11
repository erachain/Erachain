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

/*

#### PROPERTY 1
typeBytes[2].3-7 = point accuracy for HAVE amount: -16..16 = BYTE - 16

#### PROPERTY 2
typeBytes[3].3-7 = point accuracy for WANT amount: -16..16 = BYTE - 16

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
    private CreateOrderTransaction createOrderTx;
    private Order order;

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

    public void setDC(DCSet dcSet, boolean andUpdateFromState) {

        super.setDC(dcSet, false);

        orderID = dcSet.getTransactionFinalMapSigns().get(orderRef);
        createOrderTx = (CreateOrderTransaction) dcSet.getTransactionFinalMap().get(orderID);

        order = dcSet.getOrderMap().get(orderID);
        if (order == null) {
            // for show in JSON and blockexplorer
            order = dcSet.getCompletedOrderMap().get(orderID);
        }
        order.setDC(dcSet);

        if (false && andUpdateFromState && !isWiped())
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
        return getKey();
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
        return new Order(order, this.amountWant);
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

        if (order == null) {
            return ORDER_DOES_NOT_EXIST;
        }

        if (!createOrderTx.getCreator().equals(creator)) {
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
        if (scale > createOrderTx.getWantAsset().getScale()) {
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
                    new Object[]{ItemCls.ASSET_TYPE, createOrderTx.getHaveKey(), createOrderTx.getHaveAsset() == null ?
                            null : createOrderTx.getHaveAsset().getTags()}, // транзакция ошибочная
                    new Object[]{ItemCls.ASSET_TYPE, createOrderTx.getWantKey(), createOrderTx.getWantAsset() == null ?
                            null : createOrderTx.getWantAsset().getTags()},
            };
        } else {
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()},
                    new Object[]{ItemCls.ASSET_TYPE, createOrderTx.getHaveKey(), createOrderTx.getHaveAsset() == null ?
                            null : createOrderTx.getHaveAsset().getTags()}, // транзакция ошибочная
                    new Object[]{ItemCls.ASSET_TYPE, createOrderTx.getWantKey(), createOrderTx.getWantAsset() == null ?
                            null : createOrderTx.getWantAsset().getTags()},
            };
        }
    }

    // PROCESS/ORPHAN

    // @Override
    @Override
    public void process(Block block, int forDeal) {
        // UPDATE CREATOR
        super.process(block, forDeal);

        // удалим сперва - чтобы почистить все ключ с ценой корректно
        dcSet.getOrderMap().delete(orderID);

        // запомним для отката что там было до изменения
        Trade trade = new Trade(Trade.TYPE_UPDATE, dbRef, orderID, order.getHaveAssetKey(), order.getWantAssetKey(),
                order.getAmountHave(), order.getAmountWant(),
                createOrderTx.getHaveAsset().getScale(), createOrderTx.getWantAsset().getScale(), 1);

        // нужно запомнить чтобы при откате обновить взад цену
        dcSet.getTradeMap().put(trade);

        // изменяемые объекты нужно заново создавать
        Order updatedOrder = makeUpdatedOrder();

        if (order.getAmountWant().compareTo(amountWant) > 0) {
            /// цена уменьшилась - проверим может он сработает
            updatedOrder.setDC(dcSet);
            updatedOrder.process(block, createOrderTx, true);
        } else {
            dcSet.getOrderMap().put(orderID, updatedOrder);
        }

    }

    // @Override
    @Override
    public void orphan(Block block, int forDeal) {
        // UPDATE CREATOR
        super.orphan(block, forDeal);

        // ORPHAN UPDATE ORDER

        // удалим чтобы очистить ключи вторичные по цене
        Order updatedOrder = dcSet.getOrderMap().remove(orderID);

        // трейд ищем по ордеру и своему дбРЕФ
        // чтобы восстановить старую цену
        Trade trade = dcSet.getTradeMap().remove(new Fun.Tuple2<>(dbRef, orderID));

        // изменяемые объекты нужно заново создавать
        // восстановим Хочу по инфо из Сделки
        Order orderBefore = new Order(updatedOrder, trade.getAmountWant());

        if (orderBefore.getAmountWant().compareTo(amountWant) > 0) {
            /// цена уменьшилась - откатим, ведь может он сработал
            updatedOrder.setDC(dcSet);
            updatedOrder.orphan(block, block == null ? timestamp : block.getTimestamp(), true);
        } else {
            dcSet.getOrderMap().put(orderID, orderBefore);
        }

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
