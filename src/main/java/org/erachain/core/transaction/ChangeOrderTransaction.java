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
import org.erachain.dapp.DAPP;
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
    private static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH
            + SEQ_NO_LENGTH;

    /**
     * typeBytes[3] used as TransactionAmount.SCALE_MASK = 31
     */
    private static final byte HAVE_AMOUNT_MASK = (byte) 1 << 5;

    byte[] orderRef;
    private final BigDecimal newAmount;

    long orderID;
    //private CreateOrderTransaction createOrderTx;
    private Order order;

    /**
     * @param typeBytes
     * @param creator
     * @param orderRef  signature of Creating or last Changing Order transaction
     * @param newAmount
     * @param feePow
     * @param timestamp
     * @param reference
     */
    public ChangeOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] orderRef,
                                  BigDecimal newAmount, byte feePow, long timestamp, Long reference) {
        super(typeBytes, TYPE_NAME, creator, null, null, feePow, timestamp, reference);

        this.orderRef = orderRef;
        this.newAmount = newAmount;

    }

    public ChangeOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] orderRef,
                                  BigDecimal newAmount, byte feePow, long timestamp, Long reference,
                                  byte[] signature) {
        this(typeBytes, creator, orderRef, newAmount, feePow, timestamp, reference);
        this.signature = signature;

    }

    public ChangeOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, byte[] orderRef,
                                  BigDecimal newAmount, byte feePow, long timestamp, Long reference,
                                  byte[] signature, long orderID, long seqNo, long feeLong) {
        this(typeBytes, creator, orderRef, newAmount, feePow, timestamp, reference);
        this.signature = signature;
        this.orderID = orderID;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);

    }

    public ChangeOrderTransaction(PublicKeyAccount creator, byte[] orderRef,
                                  BigDecimal newAmount, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, orderRef, newAmount, feePow, timestamp, reference,
                signature);
    }

    public ChangeOrderTransaction(PublicKeyAccount creator, byte[] orderRef,
                                  BigDecimal newAmount, boolean useHave, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, useHave ? HAVE_AMOUNT_MASK : 0}, creator, orderRef, newAmount, feePow, timestamp,
                reference);
    }

    // GETTERS/SETTERS

    public boolean isHaveUpdated() {
        return (typeBytes[3] & HAVE_AMOUNT_MASK) != 0;
    }

    public void setDC(DCSet dcSet, boolean andUpdateFromState) {

        super.setDC(dcSet, false);

        if (orderID == 0) {

            // на выходе может быть NULL - он в long не преобразуется - поэтому сначала исследуем
            Long res = dcSet.getTransactionFinalMapSigns().get(orderRef);

            if (res == null) {
                LOGGER.error("ORDER transaction not found: " + Base58.encode(this.orderRef));
                errorValue = Base58.encode(this.orderRef);
                if (BlockChain.CHECK_BUGS > 3) {
                    Long error = null;
                    error++;
                }
                return;
            }

            orderID = res;
        }

        order = dcSet.getOrderMap().get(orderID);
        // подтянем в любом случае даже из Completed, а ниже проверку вставим на Активен?
        if (order == null) {
            // возможно для блокэксплорера нужно - если ордер уже сыграл
            // и для кошелька тоже надо
            order = dcSet.getCompletedOrderMap().get(orderID);
        }

        if (andUpdateFromState && !isWiped())
            updateFromStateDB();

    }

    public String getTitle() {
        if (order == null) {
            // если подпись ошибочная и ордера нет вообще
            return "";
        }
        return //TYPE_NAME + " " +
                ItemCls.getItemTypeAndKey(ItemCls.ASSET_TYPE, order.getHaveAssetKey())
                        + " " + ItemCls.getItemTypeAndKey(ItemCls.ASSET_TYPE, order.getWantAssetKey());
    }

    @Override
    public String viewAmount() {
        return this.newAmount.toPlainString();
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
        return this.newAmount;
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

    public BigDecimal getNewAmount() {
        return this.newAmount;
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
        if (isHaveUpdated())
            return new Order(order, dbRef, this.newAmount, order.getAmountWant());

        return new Order(order, dbRef, order.getAmountHave(), this.newAmount);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        // GET BASE
        JSONObject json = this.getJsonBase();

        json.put("order", Base58.encode(orderRef));
        json.put("amount", this.newAmount.toPlainString());
        json.put("useHave", isHaveUpdated());


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
            throw new Exception("Data does not match RAW length " + data.length + " < " + test_len);
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
        byte[] flagsBytes = Arrays.copyOfRange(data, position, position + FLAGS_LENGTH);
        long flagsTX = Longs.fromByteArray(flagsBytes);
        position += FLAGS_LENGTH;

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

        DAPP dapp;
        if ((typeBytes[2] & HAS_SMART_CONTRACT_MASK) > 0) {
            dapp = DAPP.Parses(data, position, forDeal);
            position += dapp.length(forDeal);
        } else {
            dapp = null;
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

        long orderID = 0;
        if (forDeal == FOR_DB_RECORD) {
            //READ ORDER ID
            byte[] orderIDBytes = Arrays.copyOfRange(data, position, position + SEQ_NO_LENGTH);
            orderID = Longs.fromByteArray(orderIDBytes);
            position += SEQ_NO_LENGTH;
        }

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
                flagsTX, signatureBytes, orderID, seqNo, feeLong);
    }

    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = super.toBytes(forDeal, withSignature);

        // WRITE ORDER REF
        data = Bytes.concat(data, this.orderRef);

        if (forDeal == FOR_DB_RECORD) {
            // WRITE ORDER ID
            byte[] orderIDBytes = Longs.toByteArray(this.orderID);
            data = Bytes.concat(data, orderIDBytes);
        }

        BigDecimal amountBase;
        // WRITE ACCURACY of AMOUNT WANT
        int different_scale = this.newAmount.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
        if (different_scale != 0) {
            // RESCALE AMOUNT
            amountBase = this.newAmount.scaleByPowerOfTen(different_scale);
            if (different_scale < 0)
                different_scale += TransactionAmount.SCALE_MASK + 1;

            data[3] = (byte) (data[3] | different_scale);
        } else {
            amountBase = this.newAmount;
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

        if (dapp != null) {
            if (forDeal == FOR_DB_RECORD || !dapp.isEpoch()) {
                base_len += dapp.length(forDeal);
            }
        }

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        return base_len;
    }

    @Override
    public int isValid(int forDeal, long checkFlags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        if (orderID == 0L
                // в SetDC он в любом случае подтянется - даже из Completed, поэтому тут проверку
                || order == null) {
            return ORDER_DOES_NOT_EXIST;
        }

        // в SetDC он в любом случае подтянется - даже из Completed, поэтому тут проверку
        if (!order.isActive()) {
            return ORDER_ALREADY_COMPLETED;
        }

        if (!order.getCreator().equals(creator)) {
            return INVALID_CREATOR;
        }

        // CHECK IF AMOUNT POSITIVE
        if (newAmount.signum() <= 0) {
            return NEGATIVE_AMOUNT;
        }

        if (isHaveUpdated()) {
            if (newAmount.compareTo(order.getAmountHave()) == 0) {
                errorValue = "New amount is same";
                return INVALID_AMOUNT;
            } else if (newAmount.compareTo(order.getFulfilledHave()) <= 0) {
                errorValue = "newAmount <= fulfilledHave";
                return INVALID_AMOUNT;
            }
        } else {
            if (newAmount.compareTo(order.getAmountWant()) == 0) {
                errorValue = "New amount is same";
                return INVALID_AMOUNT;
            }
        }

        // for PARSE and toBYTES need only AMOUNT_LENGTH bytes
        // and SCALE
        byte[] amountBytes;
        amountBytes = newAmount.unscaledValue().toByteArray();
        if (amountBytes.length > AMOUNT_LENGTH) {
            return AMOUNT_LENGHT_SO_LONG;
        }
        int scale = this.newAmount.scale();
        if (scale < TransactionAmount.minSCALE
                || scale > TransactionAmount.maxSCALE) {
            return AMOUNT_SCALE_WRONG;
        }
        scale = this.newAmount.stripTrailingZeros().scale();
        if (scale > order.getWantAssetScale()) {
            return AMOUNT_SCALE_WRONG;
        }

        return super.isValid(forDeal, checkFlags);
    }

    @Override
    public void makeItemsKeys() {
        if (isWiped()
                || order == null // это может быть с инвалидной ссылкой на ордер
        ) {
            itemsKeys = new Object[][]{};
            return;
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
    public void processBody(Block block, int forDeal) {
        super.processBody(block, forDeal);

        // PROCESS UPDATE ORDER

        // удалим
        dcSet.getOrderMap().delete(orderID);
        // делаем его как отмененный - чтобы новый ордер создать
        dcSet.getCompletedOrderMap().put(order);

        // запомним для отчета что цена изменилась
        Trade trade;
        if (isHaveUpdated()) {
            trade = new Trade(Trade.TYPE_CHANGE,
                    dbRef, // номер инициатора по нашему номеру
                    orderID, // номер оригинала?
                    order.getHaveAssetKey(), order.getWantAssetKey(),
                    newAmount, order.getAmountWant(),
                    order.getHaveAssetScale(), order.getWantAssetScale(), 0);

            // change PLEDGE
            BigDecimal diffAmount = order.getAmountHave().subtract(newAmount);
            creator.changeBalance(dcSet, diffAmount.signum() > 0, true, order.getHaveAssetKey(),
                    diffAmount.abs(), false, false, true);

        } else {
            trade = new Trade(Trade.TYPE_CHANGE,
                    dbRef, // номер инициатора по нашему номеру
                    orderID, // номер оригинала?
                    order.getHaveAssetKey(), order.getWantAssetKey(),
                    order.getAmountHave(), newAmount,
                    order.getHaveAssetScale(), order.getWantAssetScale(), 0);
        }

        // нужно запомнить чтобы при откате обновить назад цену
        dcSet.getTradeMap().put(trade);

        // изменяемые объекты нужно заново создавать
        Order updatedOrder = makeUpdatedOrder();

        if (isHaveUpdated() && order.getAmountHave().compareTo(newAmount) < 0
                || !isHaveUpdated() && order.getAmountWant().compareTo(newAmount) > 0) {
            /// цена уменьшилась - проверим может он сработает
            updatedOrder.setDC(dcSet);
            OrderProcess.process(updatedOrder, block, this);
        } else {
            dcSet.getOrderMap().put(updatedOrder);
        }

    }

    @Override
    public void orphanBody(Block block, int forDeal) {
        super.orphanBody(block, forDeal);

        // ORPHAN UPDATE ORDER

        // сделку ищем по ордеру и своему дбРЕФ
        // чтобы восстановить старую цену
        dcSet.getTradeMap().delete(new Fun.Tuple2<>(dbRef, orderID));

        // удалим из исполненных
        Order orderOrig = dcSet.getCompletedOrderMap().remove(orderID);

        if (isHaveUpdated()) {
            // change PLEDGE
            BigDecimal diffAmount = orderOrig.getAmountHave().subtract(newAmount);
            creator.changeBalance(dcSet, diffAmount.signum() < 0, true, orderOrig.getHaveAssetKey(),
                    diffAmount.abs(), false, false, true);
        }

        if (isHaveUpdated() && orderOrig.getAmountHave().compareTo(newAmount) < 0
                || !isHaveUpdated() && orderOrig.getAmountWant().compareTo(newAmount) > 0) {
            /// цена уменьшилась - откатим, ведь может он сработал
            OrderProcess.orphan(dcSet, dbRef, block, block == null ? timestamp : block.getTimestamp());
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
