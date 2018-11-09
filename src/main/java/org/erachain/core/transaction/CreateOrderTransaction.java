package org.erachain.core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.datachain.DCSet;

/*

#### PROPERTY 1
typeBytes[2].3-7 = point accuracy for HAVE amount: -16..16 = BYTE - 16

#### PROPERTY 2
typeBytes[3].3-7 = point accuracy for WANT amount: -16..16 = BYTE - 16

 */
public class CreateOrderTransaction extends Transaction {
    public static final byte[][] VALID_REC = new byte[][]{
        //Base58.decode("3C41sWQNguCxhe66QhKSUr7NTFYQqQ8At6E2LfKDBNxpDtWZDjRBTwVRZN9ZuxQrzXL9R4V4EF1EP7B1HucctkqJ"),
        //Base58.decode("3BTEfHJ6cQJtrvA2A1QkKwuznN7LckVUU9YDBjaZiBPapQrN6zHtc6JhgBy1tU8k6z6i7iW9Q4H7ZpordUYdfu2t"),
        //Base58.decode("4EbKCt4QDfMvCHRPM36y5TyDayZUQzURBhS8wJ4Em4ejpbfd2bUn9oDyEWgXKy5Mwkc7MovGcvU5svAVfQyJW8y6"),
        //Base58.decode("5nv56Enkt24y2Lcxe1Zbjpeuk7Fd5vSNo4gY7oTbAh42RwPtrZ1jpaTZX8CdWAvqQzpbUNFD7AHAuvRxeMirrjnV"),
        
        //Base58.decode("2PLy4qTVeYnwAiESvaeaSUTWuGcERQr14bpGj3qo83c4vTP8RRMjnmRXnd6USsbvbLwWUNtjErcdvs5KtZMpyREC"),
        //Base58.decode("5XMmLXACUPu74absaKQwVSnzf91ppvYcMK8mBqQ18dALQxvVrB46atw2bfv4xXXq7ZXrM1iELKyW5jMiLgf8uHKf"),
        //Base58.decode("4fWbpHBsEzyG9paXH5oJswn3YMhvxw6fRssk6qZmB7jxQ72sRXJunEQhi9bnTwg2cUjwGCZy54u4ZseLRM7xh2x6")
    };
    private static final byte TYPE_ID = (byte) Transaction.CREATE_ORDER_TRANSACTION;
    private static final String NAME_ID = "Create Order";
    private static final int AMOUNT_LENGTH = TransactionAmount.AMOUNT_LENGTH;
    private static final int HAVE_LENGTH = 8;
    private static final int WANT_LENGTH = 8;
    // private static final int PRICE_LENGTH = 12;

    private static final int LOAD_LENGTH = HAVE_LENGTH + WANT_LENGTH + 2 * AMOUNT_LENGTH;
    private static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + LOAD_LENGTH;
    private static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + LOAD_LENGTH;
    private static final int BASE_LENGTH = Transaction.BASE_LENGTH + LOAD_LENGTH;
    private static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH;

    private final BigDecimal FEE_MIN_1 = new BigDecimal("-0.0001");
    private long haveKey;
    private long wantKey;
    private AssetCls haveAsset;
    private AssetCls wantAsset;
    //private Order order;
    private BigDecimal amountHave;
    private BigDecimal amountWant;

    public CreateOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, long haveKey, long wantKey,
                                  BigDecimal amountHave, BigDecimal amountWant, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
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
        //this.order = new Order(new BigInteger(signature), creator, haveKey, wantKey, amountHave, amountWant, timestamp);

    }
    public CreateOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, long haveKey, long wantKey,
                                  BigDecimal amountHave, BigDecimal amountWant, byte feePow, long timestamp, Long reference,
                                  byte[] signature, long feeLong) {
        this(typeBytes, creator, haveKey, wantKey, amountHave, amountWant, feePow, timestamp, reference);
        this.signature = signature;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.AMOUNT_DEDAULT_SCALE);

    }

    public CreateOrderTransaction(PublicKeyAccount creator, long haveKey, long wantKey, BigDecimal amountHave,
                                  BigDecimal amountWant, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, haveKey, wantKey, amountHave, amountWant, feePow, timestamp, reference,
                signature);
    }

    // GETTERS/SETTERS
    // public static String getName() { return "Create Order"; }

    @Override
    public String viewAmount() {
        return this.amountHave.toPlainString();
    }

    /*
     * public void makeOrder() { if (this.order == null) this.order = new
     * Order(new BigInteger(this.signature), this.creator, this.have, this.want,
     * this.amount, this.amountWant, this.timestamp); }
     */

    public CreateOrderTransaction(PublicKeyAccount creator, long have, long want, BigDecimal amountHave,
                                  BigDecimal amountWant, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, have, want, amountHave, amountWant, feePow, timestamp,
                reference);
    }

    static public BigDecimal unScaleAmountToDefault(BigDecimal amount, AssetCls asset) {
        return amount;
    }

    public static Transaction Parse(byte[] data, int asDeal) throws Exception {
        //boolean asPack = releaserReference != null;

        // CHECK IF WE MATCH BLOCK LENGTH
        int test_len;
        if (asDeal == Transaction.FOR_MYPACK) {
            test_len = BASE_LENGTH_AS_MYPACK;
        } else if (asDeal == Transaction.FOR_PACK) {
            test_len = BASE_LENGTH_AS_PACK;
        } else if (asDeal == Transaction.FOR_DB_RECORD) {
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
        if (asDeal > Transaction.FOR_MYPACK) {
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

        byte feePow = 0;
        if (asDeal > Transaction.FOR_PACK) {
            // READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        // READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        long feeLong = 0;
        if (asDeal == FOR_DB_RECORD) {
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
                reference, signatureBytes, feeLong);
    }

    /*
    public void setDC(DCSet dcSet, int asDeal) {

        super.setDC(dcSet, asDeal);

        this.haveAsset = (AssetCls) this.dcSet.getItemAssetMap().get(this.haveKey);
        this.wantAsset = (AssetCls) this.dcSet.getItemAssetMap().get(this.wantKey);

    }
    */

    public void setBlock(Block block, DCSet dcSet, int asDeal, int seqNo) {
        super.setBlock(block, dcSet, asDeal, seqNo);

        this.haveAsset = this.dcSet.getItemAssetMap().get(this.haveKey);
        this.wantAsset = this.dcSet.getItemAssetMap().get(this.wantKey);
    }

    public void setDC(DCSet dcSet, int asDeal, int blockHeight, int seqNo) {
        super.setDC(dcSet, asDeal, blockHeight, seqNo);

        this.haveAsset = this.dcSet.getItemAssetMap().get(this.haveKey);
        this.wantAsset = this.dcSet.getItemAssetMap().get(this.wantKey);
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
        return Order.calcPrice(this.amountHave, this.amountWant);
    }

    public BigDecimal getPriceCalcReverse() {
        return Order.calcPrice(this.amountWant, this.amountHave);
    }

    @Override
    public boolean hasPublicText() {
        return false;
    }

    // PARSE CONVERT

    public Order makeOrder() {
        // set SCALE by ASSETs
        BigDecimal amountHave = this.amountHave.setScale(this.haveAsset.getScale());
        BigDecimal amountWant = this.amountWant.setScale(this.wantAsset.getScale());

        return new Order(Transaction.makeDBRef(this.height, this.seqNo), this.creator, this.haveKey, this.wantKey,
                amountHave, amountWant // new SCALE
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

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        return base_len;
    }

    @Override
    public int isValid(int asDeal, long flags) {

        if (this.haveAsset == null || this.wantAsset == null)
            return ITEM_ASSET_NOT_EXIST;

        if (this.wantAsset.isAccounting() ^ this.haveAsset.isAccounting() && !BlockChain.DEVELOP_USE) {

            return INVALID_ACCOUNTING_PAIR;
        }
        
        if (this.wantAsset.isInsideBonus() ^ this.haveAsset.isInsideBonus()) {
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

        int height = this.getBlockHeightByParentOrLast(this.dcSet);

        // CHECK IF ASSETS NOT THE SAME
        if (haveKey == RIGHTS_KEY && !BlockChain.DEVELOP_USE
            // && wantKey != FEE_KEY
                ) {
            // haveKey ERA
            if (height > BlockChain.FREEZE_FROM
                    && BlockChain.FOUNDATION_ADDRESSES.contains(this.creator.getAddress())) {
                // LOCK ERA sell
                return INVALID_CREATOR;
            }
        }

        if (haveKey == wantKey) {
            return HAVE_EQUALS_WANT;
        }

        // CHECK IF AMOUNT POSITIVE
        BigDecimal amountHave = this.amountHave;
        BigDecimal amountWant = this.amountWant;
        if (amountHave.compareTo(BigDecimal.ZERO) <= 0 || amountWant.compareTo(BigDecimal.ZERO) <= 0) {
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
        if (height < BlockChain.ALL_BALANCES_OK_TO ) {
            ; // NOT CHECK
        } else if (FEE_KEY == haveKey) {
            if (this.creator.getBalance(this.dcSet, FEE_KEY).a.b.compareTo(amountHave.add(this.fee)) == -1) {
                return NO_BALANCE;
            }
            // VALID if want to BY COMPU by ERA
        } else if (wantKey == FEE_KEY && haveKey == RIGHTS_KEY
                && amountHave.compareTo(BigDecimal.ONE) >= 0
                && this.creator.getBalance(this.dcSet, FEE_KEY).a.b.compareTo(this.FEE_MIN_1) > 0) {
            flags = flags | NOT_VALIDATE_FLAG_FEE;
        } else {

            // CHECK IF SENDER HAS ENOUGH FEE BALANCE
            if (this.creator.getBalance(this.dcSet, FEE_KEY).a.b.compareTo(this.fee) == -1) {
                return NOT_ENOUGH_FEE;
            }

            // if asset is unlimited and me is creator of this asset
            boolean unLimited = haveAsset.getQuantity().equals(0l)
                    && haveAsset.getOwner().getAddress().equals(this.creator.getAddress());

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

            if (height > BlockChain.FREEZE_FROM && BlockChain.LOCKED__ADDRESSES.get(this.creator.getAddress()) != null)
                return INVALID_CREATOR;

            Tuple3<String, Integer, Integer> unlockItem = BlockChain.LOCKED__ADDRESSES_PERIOD.get(this.creator.getAddress());
            if (unlockItem != null && unlockItem.b > height && height < unlockItem.c)
                return INVALID_CREATOR;

        }

        //
        Long maxWant = wantAsset.getQuantity();
        if (maxWant > 0 && new BigDecimal(maxWant).compareTo(amountWant) < 0)
            return INVALID_QUANTITY;

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

        return super.isValid(asDeal, flags);
    }

    // PROCESS/ORPHAN

    // @Override
    @Override
    public void process(Block block, int asDeal) {
        // UPDATE CREATOR
        super.process(block, asDeal);

        // PROCESS ORDER
        // изменяемые объекты нужно заново создавать
        //this.order.copy().process(this);
        //this.order.process(this);

        // изменяемые объекты нужно заново создавать
        //.copy() // тут надо что-то сделать новым - а то значения впамяти по ссылке меняются
        Order order = makeOrder(); //.copy();
        order.setDC(dcSet);
        order.process(this);

    }

    // @Override
    @Override
    public void orphan(int asDeal) {
        // UPDATE CREATOR
        super.orphan(asDeal);

        // ORPHAN ORDER
        // изменяемые объекты нужно заново создавать
        //this.order.copy().orphan();

        // изменяемые объекты нужно заново создавать
        Order order = makeOrder();
        order.setDC(dcSet);
        order.orphan();

    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<>();
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        return new HashSet<>();
    }

    @Override
    public boolean isInvolved(Account account) {
        String address = account.getAddress();

        if (address.equals(this.creator.getAddress())) {
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

    @Override
    public int getJobLevel() {
        return 300;
    }

    @Override
    public long calcBaseFee() {
        if (this.height < BlockChain.VERS_4_11 && BlockChain.VERS_4_11_USE_OLD_FEE)
            return 5 * calcCommonFee();

        return calcCommonFee();

    }
}
