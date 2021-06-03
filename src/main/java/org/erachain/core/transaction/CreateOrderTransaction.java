package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.TradePair;
import org.erachain.database.PairMapImpl;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;

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
public class CreateOrderTransaction extends Transaction implements Itemable {
    public static final byte[][] VALID_REC = new byte[][]{
            //Base58.decode("4...")
    };
    public static final byte TYPE_ID = (byte) Transaction.CREATE_ORDER_TRANSACTION;
    public static final String TYPE_NAME = "Create Order";

    public static final int AMOUNT_LENGTH = TransactionAmount.AMOUNT_LENGTH;
    private static final int HAVE_LENGTH = 8;
    private static final int WANT_LENGTH = 8;
    // private static final int PRICE_LENGTH = 12;

    private static final int LOAD_LENGTH = HAVE_LENGTH + WANT_LENGTH + 2 * AMOUNT_LENGTH;
    private static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + LOAD_LENGTH;
    private static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + LOAD_LENGTH;
    private static final int BASE_LENGTH = Transaction.BASE_LENGTH + LOAD_LENGTH;
    private static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH;

    private long haveKey;
    private long wantKey;
    private AssetCls haveAsset;
    private AssetCls wantAsset;
    //private Order order;
    private BigDecimal amountHave;
    private BigDecimal amountWant;

    public CreateOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, long haveKey, long wantKey,
                                  BigDecimal amountHave, BigDecimal amountWant, byte feePow, long timestamp, Long reference) {
        super(typeBytes, TYPE_NAME, creator, null, feePow, timestamp, reference);
        this.haveKey = haveKey;
        this.wantKey = wantKey;

        this.amountHave = amountHave;
        this.amountWant = amountWant;

    }

    public CreateOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, long haveKey, long wantKey,
                                  BigDecimal amountHave, BigDecimal amountWant, byte feePow, long timestamp, Long reference,
                                  byte[] signature) {
        this(typeBytes, creator, haveKey, wantKey, amountHave, amountWant, feePow, timestamp, reference);
        this.signature = signature;

    }

    public CreateOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, long haveKey, long wantKey,
                                  BigDecimal amountHave, BigDecimal amountWant, byte feePow, long timestamp, Long reference,
                                  byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, haveKey, wantKey, amountHave, amountWant, feePow, timestamp, reference);
        this.signature = signature;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);

    }

    public CreateOrderTransaction(PublicKeyAccount creator, long haveKey, long wantKey, BigDecimal amountHave,
                                  BigDecimal amountWant, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, haveKey, wantKey, amountHave, amountWant, feePow, timestamp, reference,
                signature);
    }

    public CreateOrderTransaction(PublicKeyAccount creator, long have, long want, BigDecimal amountHave,
                                  BigDecimal amountWant, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, have, want, amountHave, amountWant, feePow, timestamp,
                reference);
    }


    // GETTERS/SETTERS

    public void setDC(DCSet dcSet, boolean andUpdateFromState) {

        super.setDC(dcSet, false);

        this.haveAsset = dcSet.getItemAssetMap().get(this.haveKey);
        this.wantAsset = dcSet.getItemAssetMap().get(this.wantKey);

        if (false && andUpdateFromState && !isWiped())
            updateFromStateDB();

    }

    // public static String getName() { return "Create Order"; }

    @Override
    public String getTitle() {
        return //TYPE_NAME + " " +
                ItemCls.getItemTypeAndKey(ItemCls.ASSET_TYPE, haveKey)
                        + " " + ItemCls.getItemTypeAndKey(ItemCls.ASSET_TYPE, wantKey);
    }

    @Override
    public String viewAmount() {
        return this.amountHave.toPlainString();
    }

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {

        long long_fee = super.calcBaseFee(withFreeProtocol);
        if (long_fee == 0)
            return 0L;

        if (height > BlockChain.VERS_5_3 && (haveKey < 100 || wantKey < 100)) {
            return 0L;
        }

        if (!BlockChain.MAIN_MODE || height > BlockChain.VERS_5_01_01) {
            return 1000L * BlockChain.FEE_PER_BYTE;
        } else {
            return long_fee;
        }
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

        // READ HAVE
        byte[] haveBytes = Arrays.copyOfRange(data, position, position + HAVE_LENGTH);
        long have = Longs.fromByteArray(haveBytes);
        position += HAVE_LENGTH;

        // READ WANT
        byte[] wantBytes = Arrays.copyOfRange(data, position, position + WANT_LENGTH);
        long want = Longs.fromByteArray(wantBytes);
        position += WANT_LENGTH;

        // READ AMOUNT HAVE
        byte[] amountHaveBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
        BigDecimal amountHave = new BigDecimal(new BigInteger(amountHaveBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
        position += AMOUNT_LENGTH;
        // CHECK ACCURACY of AMOUNT
        int accuracy = typeBytes[2] & TransactionAmount.SCALE_MASK;
        if (accuracy > 0) {
            if (accuracy >= TransactionAmount.SCALE_MASK_HALF) {
                accuracy -= TransactionAmount.SCALE_MASK + 1;
            }

            // RESCALE AMOUNT
            amountHave = amountHave.scaleByPowerOfTen(-accuracy);
        }

        // READ AMOUNT WANT
        byte[] amountWantBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
        BigDecimal amountWant = new BigDecimal(new BigInteger(amountWantBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
        position += AMOUNT_LENGTH;
        // CHECK ACCURACY of AMOUNT
        accuracy = typeBytes[3] & TransactionAmount.SCALE_MASK;
        if (accuracy > 0) {
            if (accuracy >= TransactionAmount.SCALE_MASK_HALF) {
                accuracy -= TransactionAmount.SCALE_MASK + 1;
            }

            // RESCALE AMOUNT
            amountWant = amountWant.scaleByPowerOfTen(-accuracy);
        }

        return new CreateOrderTransaction(typeBytes, creator, have, want, amountHave, amountWant, feePow, timestamp,
                reference, signatureBytes, seqNo, feeLong);
    }

    public Long getOrderId() {
        //return this.signature;
        return Transaction.makeDBRef(this.height, this.seqNo);
    }

    @Override
    public BigDecimal getAmount() {
        return this.amountHave;
    }

    @Override
    public long getKey() {
        return this.haveKey;
    }

    @Override
    public long getAssetKey() {
        return this.haveKey;
    }

    @Override
    public ItemCls getItem() {
        return this.haveAsset;
    }

    public long getHaveKey() {
        return this.haveKey;
    }

    public AssetCls getHaveAsset() {
        return this.haveAsset;
    }

    public BigDecimal getAmountHave() {
        return this.amountHave;
    }

    public long getWantKey() {
        return this.wantKey;
    }

    public AssetCls getWantAsset() {
        return this.wantAsset;
    }

    public BigDecimal getAmountWant() {
        return this.amountWant;
    }

    public BigDecimal getPriceCalc() {
        // precision bad return Order.calcPrice(this.amountHave, this.amountWant);
        return makeOrder().calcPrice();
    }

    public BigDecimal getPriceCalcReverse() {
        //return Order.calcPrice(this.amountWant, this.amountHave);
        return makeOrder().calcPriceReverse();
    }

    @Override
    public boolean hasPublicText() {
        return false;
    }

    // PARSE CONVERT

    public Order makeOrder() {
        return new Order(dcSet, Transaction.makeDBRef(this.height, this.seqNo), this.creator,
                this.haveKey, this.amountHave, this.haveAsset.getScale(),
                this.wantKey, this.amountWant, this.wantAsset.getScale()
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        // GET BASE
        JSONObject transaction = this.getJsonBase();

        transaction.put("haveKey", this.haveKey);
        transaction.put("wantKey", this.wantKey);
        transaction.put("amountHave", this.amountHave.toPlainString());
        transaction.put("amountWant", this.amountWant.toPlainString());

        //if (this.order != null) {
        //	transaction.put("order", this.order.toJson());
        //}

        return transaction;
    }

    // @Override
    //@Override
    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = super.toBytes(forDeal, withSignature);

        // WRITE HAVE
        byte[] haveBytes = Longs.toByteArray(this.haveKey);
        haveBytes = Bytes.ensureCapacity(haveBytes, HAVE_LENGTH, 0);
        data = Bytes.concat(data, haveBytes);

        // WRITE WANT
        byte[] wantBytes = Longs.toByteArray(this.wantKey);
        wantBytes = Bytes.ensureCapacity(wantBytes, WANT_LENGTH, 0);
        data = Bytes.concat(data, wantBytes);

        // WRITE ACCURACY of AMOUNT HAVE
        int different_scale = this.amountHave.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
        BigDecimal amountBase;
        if (different_scale != 0) {
            // RESCALE AMOUNT
            amountBase = this.amountHave.scaleByPowerOfTen(different_scale);
            if (different_scale < 0)
                different_scale += TransactionAmount.SCALE_MASK + 1;

            data[2] = (byte) (data[2] | different_scale);
        } else {
            amountBase = this.amountHave;
        }
        // WRITE AMOUNT HAVE
        byte[] amountHaveBytes = amountBase.unscaledValue().toByteArray();
        byte[] fill_H = new byte[AMOUNT_LENGTH - amountHaveBytes.length];
        amountHaveBytes = Bytes.concat(fill_H, amountHaveBytes);
        data = Bytes.concat(data, amountHaveBytes);

        // WRITE ACCURACY of AMOUNT WANT
        different_scale = this.amountWant.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
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
    public int getDataLength(int forDeal, boolean withSignature)
    {
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

        if (this.haveAsset == null || this.wantAsset == null)
            return ITEM_ASSET_NOT_EXIST;

        if (this.wantAsset.isAccounting() ^ this.haveAsset.isAccounting()
                || haveAsset.isSelfManaged() || wantAsset.isSelfManaged()) {
            return INVALID_ACCOUNTING_PAIR;
        }
        if (!wantAsset.validPair(haveKey)
                || !haveAsset.validPair(wantKey)) {
            return INVALID_ECXHANGE_PAIR;
        }

        if (this.wantAsset.isInsideBonus() ^ this.haveAsset.isInsideBonus()) {
            if (this.height < BlockChain.VERS_4_12 || this.haveKey != AssetCls.FEE_KEY && this.wantKey != AssetCls.FEE_KEY)
                return INVALID_ECXHANGE_PAIR;
        }

        long haveKey = this.haveKey;
        long wantKey = this.wantKey;

        if (false && (haveKey == AssetCls.LIA_KEY || wantKey == AssetCls.LIA_KEY)) {
            return INVALID_ECXHANGE_PAIR;
        }

        for (byte[] valid_item : VALID_REC) {
            if (Arrays.equals(this.signature, valid_item)) {
                return VALIDATE_OK;
            }
        }

        // CHECK IF ASSETS NOT THE SAME
        if (haveKey == wantKey) {
            return HAVE_EQUALS_WANT;
        }

        if (haveKey == RIGHTS_KEY && BlockChain.FREEZE_FROM > 0
                && height > BlockChain.FREEZE_FROM
                && BlockChain.FOUNDATION_ADDRESSES.contains(this.creator.getAddress())) {
            // LOCK ERA sell
            return INVALID_CREATOR;
        }

        // CHECK IF AMOUNT POSITIVE
        BigDecimal amountHave = this.amountHave;
        BigDecimal amountWant = this.amountWant;
        if (amountHave.signum() <= 0 || amountWant.signum() <= 0) {
            return NEGATIVE_AMOUNT;
        }

        // CHECK IF HAVE EXISTS
        if (this.haveAsset == null) {
            // HAVE DOES NOT EXIST
            return ITEM_ASSET_NOT_EXIST;
        }
        // CHECK IF WANT EXISTS
        if (this.wantAsset == null) {
            // WANT DOES NOT EXIST
            return ITEM_ASSET_NOT_EXIST;
        }

        // CHECK IF SENDER HAS ENOUGH ASSET BALANCE
        if (height < BlockChain.ALL_BALANCES_OK_TO) {
            ; // NOT CHECK

        } else if (wantKey == FEE_KEY
                && (haveKey == RIGHTS_KEY || haveKey == BTC_KEY)
                // VALID if want to BY COMPU by ERA
                && amountHave.compareTo(BigDecimal.TEN) >= 0 // минимально меняем 1 ЭРА
                && this.creator.getForSale(this.dcSet, haveKey, height, true).compareTo(amountHave) >= 0 // ЭРА|BTC есть на счету
                && this.creator.getForSale(this.dcSet, FEE_KEY, height, true).signum() >= 0 // и COMPU не отрицательные
        ) { // на балансе компушки не минус
            flags = flags | NOT_VALIDATE_FLAG_FEE;
        } else if (haveKey == FEE_KEY) {
            if (!BlockChain.isFeeEnough(height, creator)
                    && this.creator.getForSale(this.dcSet, FEE_KEY, height, true).compareTo(amountHave.add(this.fee)) < 0) {
                return NO_BALANCE;
            }

        } else {

            switch ((int) haveKey) {
                case 111:
                case 222:
                case 333:
                case 444:
                case 555:
                case 666:
                case 777:
                case 888:
                case 999:
                    return NO_BALANCE;
            }


            ///// CHECK IF SENDER HAS ENOUGH FEE BALANCE
            ///if (this.creator.getBalance(this.dcSet, FEE_KEY).a.b.compareTo(this.fee) == -1) {
            ///    return NOT_ENOUGH_FEE;
            ///}

            // if asset is unlimited and me is creator of this asset
            boolean unLimited = haveAsset.isUnlimited(this.creator, false);

            if (!unLimited) {

                BigDecimal forSale = this.creator.getForSale(this.dcSet, haveKey, height, true);

                if (forSale.compareTo(amountHave) < 0) {
                    boolean wrong = true;
                    for (byte[] valid_item : BlockChain.VALID_BAL) {
                        if (Arrays.equals(this.signature, valid_item)) {
                            wrong = false;
                            break;
                        }
                    }

                    if (wrong)
                        return NO_BALANCE;
                }
            }

            if (height > BlockChain.FREEZE_FROM
                    && BlockChain.LOCKED__ADDRESSES.containsKey(this.creator.getAddress()))
                return INVALID_CREATOR;

            Tuple3<String, Integer, Integer> unlockItem = BlockChain.LOCKED__ADDRESSES_PERIOD.get(this.creator.getAddress());
            if (unlockItem != null && unlockItem.b > height && height < unlockItem.c)
                return INVALID_CREATOR;

            //
            Long maxWant = wantAsset.getQuantity();
            if (maxWant > 0 && new BigDecimal(maxWant).compareTo(amountWant) < 0)
                return INVALID_QUANTITY;

        }

        // for PARSE and toBYTES need only AMOUNT_LENGTH bytes
        // and SCALE
        byte[] amountBytes;
        if (true || BlockChain.AMOUNT_SCALE_FROM < haveKey) {
            amountBytes = amountHave.unscaledValue().toByteArray();
            if (amountBytes.length > AMOUNT_LENGTH) {
                return AMOUNT_LENGHT_SO_LONG;
            }
            // SCALE wrong
            int scale = this.amountHave.scale();
            if (scale < TransactionAmount.minSCALE
                    || scale > TransactionAmount.maxSCALE) {
                return AMOUNT_SCALE_WRONG;
            }
            scale = this.amountHave.stripTrailingZeros().scale();
            if (scale > haveAsset.getScale()) {
                return AMOUNT_SCALE_WRONG;
            }
        }
        if (true || BlockChain.AMOUNT_SCALE_FROM < wantKey) {
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
            if (scale > wantAsset.getScale()) {
                return AMOUNT_SCALE_WRONG;
            }
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
                    new Object[]{ItemCls.ASSET_TYPE, haveKey, haveAsset == null ? null : haveAsset.getTags()}, // транзакция ошибочная
                    new Object[]{ItemCls.ASSET_TYPE, wantKey, wantAsset == null ? null : wantAsset.getTags()},
            };
        } else {
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()},
                    new Object[]{ItemCls.ASSET_TYPE, haveKey, haveAsset == null ? null : haveAsset.getTags()},
                    new Object[]{ItemCls.ASSET_TYPE, wantKey, wantAsset == null ? null : wantAsset.getTags()},
            };
        }
    }

    // PROCESS/ORPHAN

    // @Override
    @Override
    public void process(Block block, int forDeal) {
        // UPDATE CREATOR
        super.process(block, forDeal);

        // PROCESS ORDER
        // изменяемые объекты нужно заново создавать
        //this.order.copy().process(this);
        //this.order.process(this);

        // изменяемые объекты нужно заново создавать
        //.copy() // тут надо что-то сделать новым - а то значения в памяти по ссылке меняются
        Order order = makeOrder(); //.copy();
        order.process(block, this, false);

        if (Controller.getInstance().dlSet != null
                // так как проверка в Форке - потом быстрый слив и эта таблица вообще не будет просчитана
                && !dcSet.isFork()) {
            // статистику по парам
            PairMapImpl pairMap = Controller.getInstance().dlSet.getPairMap();
            if (!pairMap.contains(new Fun.Tuple2(haveKey, wantKey))) {
                pairMap.put(new TradePair(haveAsset, wantAsset, order.getPrice(), timestamp,
                        order.getPrice(), order.getPrice(),
                        amountHave, amountWant, BigDecimal.ZERO,
                        order.getPrice(), order.getPrice(),
                        1, timestamp, 0, 0));
            }
        }

    }

    // @Override
    @Override
    public void orphan(Block block, int forDeal) {
        // UPDATE CREATOR
        super.orphan(block, forDeal);

        // ORPHAN ORDER
        // изменяемые объекты нужно заново создавать
        //this.order.copy().orphan();

        // изменяемые объекты нужно заново создавать
        Order order = makeOrder();
        order.orphan(block, false);

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
        if (account.getAddress().equals(this.creator.getAddress())) {
            return this.amountHave;
        }

        return BigDecimal.ZERO;
    }

    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

        assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);
        assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), this.haveKey,
                this.amountHave);

        return assetAmount;
    }

}
