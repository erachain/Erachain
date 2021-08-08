package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetFactory;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.item.assets.AssetUniqueSeriesCopy;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemMap;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class IssueAssetSeriesTransaction extends IssueAssetTransaction {
    public static final byte TYPE_ID = (byte) Transaction.ISSUE_ASSET_SERIES_TRANSACTION;
    public static final String TYPE_NAME = "Issue Asset Series";

    private final byte[] origAssetRef;
    private final int total;

    /**
     * Нужно чтобы запомнить с какого номер началась Серия - для быстрого удаления всех
     */
    long lastCopyKey;
    long origAssetKey;
    private AssetCls origAsset;

    /**
     * @param typeBytes
     * @param creator
     * @param linkTo
     * @param origAssetRef   signature of Creating or last Changing Order transaction
     * @param prototypeAsset
     * @param total
     * @param feePow
     * @param timestamp
     * @param reference
     */
    public IssueAssetSeriesTransaction(byte[] typeBytes, PublicKeyAccount creator,
                                       ExLink linkTo, byte[] origAssetRef,
                                       AssetCls prototypeAsset, int total, byte feePow, long timestamp, Long reference) {
        super(typeBytes, creator, linkTo, prototypeAsset, feePow, timestamp, reference);

        this.origAssetRef = origAssetRef;
        this.total = total;

    }

    public IssueAssetSeriesTransaction(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, byte[] origAssetRef,
                                       AssetCls prototypeAsset, int total, byte feePow, long timestamp, Long reference,
                                       byte[] signature) {
        this(typeBytes, creator, linkTo, origAssetRef, prototypeAsset, total, feePow, timestamp, reference);
        this.signature = signature;

    }

    public IssueAssetSeriesTransaction(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, byte[] origAssetRef,
                                       AssetCls prototypeAsset, int total, byte feePow, long timestamp, Long reference,
                                       byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, linkTo, origAssetRef, prototypeAsset, total, feePow, timestamp, reference);
        this.signature = signature;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);

    }

    public IssueAssetSeriesTransaction(PublicKeyAccount creator, ExLink linkTo, byte[] origAssetRef,
                                       AssetCls prototypeAsset, int total, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, linkTo, origAssetRef, prototypeAsset, total, feePow, timestamp, reference,
                signature);
    }

    public IssueAssetSeriesTransaction(PublicKeyAccount creator, ExLink linkTo, byte[] origAssetRef,
                                       AssetCls prototypeAsset, int total, boolean useHave, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, linkTo, origAssetRef, prototypeAsset, total, feePow, timestamp,
                reference);
    }

    // GETTERS/SETTERS

    public void setDC(DCSet dcSet, boolean andUpdateFromState) {

        super.setDC(dcSet, false);

        // на выходе может быть NULL - он в long не преобразуется - поэтому сначала исследуем
        Long res = dcSet.getTransactionFinalMapSigns().get(origAssetRef);

        if (res == null) {
            return;
        }

        origAssetKey = res;
        origAsset = dcSet.getItemAssetMap().get(origAssetKey);

        if (andUpdateFromState && !isWiped())
            updateFromStateDB();

    }

    public String getTitle() {
        return origAsset.getName();
    }

    public int getTotal() {
        return total;
    }

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {

        int len = getFeeLength();

        return (len + (long) total * 5000L) * BlockChain.FEE_PER_BYTE;

    }

    @Override
    public long getKey() {
        return origAssetKey;
    }

    @Override
    public long getOrigAssetKey() {
        return origAssetKey;
    }


    @Override
    public AssetCls getAsset() {
        return origAsset;
    }

    public AssetCls getPrototypeAsset() {
        return prototypeAsset;
    }

    public byte[] getOrigAssetRef() {
        return this.origAssetRef;
    }

    public String viewOrigAssetRef() {
        return Base58.encode(this.origAssetRef);
    }

    @Override
    public boolean hasPublicText() {
        return true;
    }

    // PARSE CONVERT

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        // GET BASE
        JSONObject json = this.getJsonBase();

        json.put("asset", Base58.encode(origAssetRef));
        json.put("assetKey", origAssetKey);
        json.put("total", total);

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

        // READ ASSET TX SIGNATURE
        byte[] assetRef = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        // READ PROTOTYPE ASSET
        AssetCls prototypeAsset = AssetFactory.getInstance().parse(forDeal, Arrays.copyOfRange(data, position, data.length), false);
        position += prototypeAsset.getDataLength(false);

        // READ TOTAL
        byte[] totalBytes = Arrays.copyOfRange(data, position, position + Short.BYTES);
        int total = Shorts.fromByteArray(totalBytes);
        position += Short.BYTES;

        IssueAssetSeriesTransaction transaction = new IssueAssetSeriesTransaction(typeBytes, creator, linkTo, assetRef, prototypeAsset, total, feePow, timestamp,
                reference, signatureBytes, seqNo, feeLong);

        long lastCopyKey = 0;
        if (forDeal == FOR_DB_RECORD) {
            //READ SEQ_NO
            byte[] lastCopyKeyBytes = Arrays.copyOfRange(data, position, position + Long.BYTES);
            lastCopyKey = Longs.fromByteArray(lastCopyKeyBytes);
            position += Long.BYTES;

            transaction.lastCopyKey = lastCopyKey;
        }

        return transaction;
    }

    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = super.toBytes(forDeal, withSignature);

        // WRITE ASSET REF
        data = Bytes.concat(data, this.origAssetRef);

        // without reference
        data = Bytes.concat(data, this.item.toBytes(forDeal, false, false));

        if (forDeal == FOR_DB_RECORD) {
            if (key == null) {
                // для неподтвержденных когда еще номера нету
                data = Bytes.concat(data, new byte[KEY_LENGTH]);
            } else {
                byte[] keyBytes = Longs.toByteArray(key);
                keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
                data = Bytes.concat(data, keyBytes);
            }

        }

        // WRITE TOTAL
        data = Bytes.concat(data, Shorts.toByteArray((short) total));

        if (forDeal == FOR_DB_RECORD) {
            // WRITE LAST COPY KEY
            data = Bytes.concat(data, Longs.toByteArray(lastCopyKey));
        }

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

        if (origAssetKey == 0L
                || origAsset == null) {
            return ITEM_ASSET_NOT_EXIST;
        }

        if (!origAsset.isUnique()) {
            errorValue = "not unique";
            return INVALID_ASSET_TYPE;
        }

        // CHECK IF AMOUNT POSITIVE
        if (total <= 0 || total > 10000) {
            return INVALID_AMOUNT;
        }

        return super.isValid(forDeal, flags);
    }

    @Override
    public void makeItemsKeys() {
        if (isWiped()
                || origAsset == null // это может быть с инвалидной ссылкой на ордер
        ) {
            itemsKeys = new Object[][]{};
            return;
        }

        if (origAssetKey == 0)
            return;

        if (creatorPersonDuration != null) {
            // запомним что тут две сущности
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()},
                    new Object[]{origAsset.getItemType(), origAssetKey, origAsset.getTags()}
            };
        } else {
            itemsKeys = new Object[][]{
                    new Object[]{origAsset.getItemType(), origAssetKey, origAsset.getTags()}
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

        AssetUnique uniqueAsset = (AssetUnique) origAsset;
        AssetUniqueSeriesCopy uniqueSeriesCopy;
        ItemMap map = uniqueAsset.getDBMap(dcSet);
        for (int indexCopy = 1; indexCopy <= total; indexCopy++) {

            uniqueSeriesCopy = new AssetUniqueSeriesCopy(uniqueAsset, total, indexCopy);

            //INSERT INTO DATABASE
            lastCopyKey = map.incrementPut(uniqueSeriesCopy);

            // SET BALANCES
            creator.changeBalance(dcSet, false, false, lastCopyKey,
                    BigDecimal.ONE, false, false, false);

            // make HOLD balance
            if (!uniqueSeriesCopy.isUnHoldable()) {
                creator.changeBalance(dcSet, false, true, lastCopyKey,
                        BigDecimal.ONE.negate(), false, false, false);
            }

        }

    }

    @Override
    public void orphan(Block block, int forDeal) {
        super.orphan(block, forDeal);

        long copyKey;
        ItemMap map = origAsset.getDBMap(dcSet);
        for (int indexDel = 0; indexDel < total; indexDel++) {

            copyKey = lastCopyKey - indexDel;

            //DELETE FROM DATABASE
            map.decrementDelete(copyKey);

            // SET BALANCES
            creator.changeBalance(dcSet, true, false, copyKey,
                    BigDecimal.ONE, false, false, false);

            // make HOLD balance
            if (!origAsset.isUnHoldable()) {
                creator.changeBalance(dcSet, true, true, copyKey,
                        BigDecimal.ONE.negate(), false, false, false);
            }

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
