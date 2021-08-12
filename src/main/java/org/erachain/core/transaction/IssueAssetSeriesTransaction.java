package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetFactory;
import org.erachain.core.item.assets.AssetUniqueSeriesCopy;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemMap;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Здесь item - это обертка (FOIL) - в ней данные по бертки, а все остальное а Оригинале.
 * В Обертку грзим рамки как картинки
 */
public class IssueAssetSeriesTransaction extends IssueAssetTransaction {
    public static final byte TYPE_ID = (byte) Transaction.ISSUE_ASSET_SERIES_TRANSACTION;
    public static final String TYPE_NAME = "Issue Asset Series";

    public static final byte WITHOUT_ORIGINAL_MASK = 1;
    /**
     * typeBytes[3] = WITHOUT_ORIGINAL_MASK
     */

    private final byte[] origAssetRef;

    long origAssetKey;
    private AssetCls origAsset;

    /**
     * @param typeBytes
     * @param creator
     * @param linkTo
     * @param origAssetRef   signature of Creating or last Changing Order transaction
     * @param foilAsset
     * @param feePow
     * @param timestamp
     * @param reference
     */
    public IssueAssetSeriesTransaction(byte[] typeBytes, PublicKeyAccount creator,
                                       ExLink linkTo, byte[] origAssetRef,
                                       AssetVenture foilAsset, byte feePow, long timestamp, Long reference) {
        super(typeBytes, creator, linkTo, foilAsset, feePow, timestamp, reference);

        this.origAssetRef = origAssetRef;
        if (origAssetRef == null)
            typeBytes[3] |= WITHOUT_ORIGINAL_MASK;

    }

    public IssueAssetSeriesTransaction(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, byte[] origAssetRef,
                                       AssetVenture foilAsset, byte feePow, long timestamp, Long reference,
                                       byte[] signature) {
        this(typeBytes, creator, linkTo, origAssetRef, foilAsset, feePow, timestamp, reference);
        this.signature = signature;
        item.setReference(signature, dbRef);

    }

    public IssueAssetSeriesTransaction(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, byte[] origAssetRef,
                                       long origAssetKey,
                                       AssetVenture foilAsset, byte feePow, long timestamp, Long reference,
                                       byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, linkTo, origAssetRef, foilAsset, feePow, timestamp, reference);

        this.origAssetKey = origAssetKey;
        this.signature = signature;
        item.setReference(signature, dbRef);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);

        if (seqNo > 0)
            this.setHeightSeq(seqNo);

    }

    // as pack
    public IssueAssetSeriesTransaction(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, byte[] origAssetRef,
                                       AssetVenture foilAsset, byte[] signature) {
        super(typeBytes, creator, linkTo, foilAsset, (byte) 0, 0L, null, signature);
        this.origAssetRef = origAssetRef;
        if (origAssetRef == null)
            typeBytes[3] |= WITHOUT_ORIGINAL_MASK;

    }

    public IssueAssetSeriesTransaction(PublicKeyAccount creator, ExLink linkTo, byte[] origAssetRef,
                                       AssetVenture foilAsset, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, linkTo, origAssetRef, foilAsset, feePow, timestamp, reference,
                signature);
    }

    public IssueAssetSeriesTransaction(PublicKeyAccount creator, ExLink linkTo,
                                       byte[] origAssetRef, AssetVenture foilAsset, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, linkTo, origAssetRef, foilAsset, feePow, timestamp, reference);
    }


    // GETTERS/SETTERS

    public void setDC(DCSet dcSet, boolean andUpdateFromState) {

        super.setDC(dcSet, false);

        if (hasOriginal()) {
            // на выходе может быть NULL - он в long не преобразуется - поэтому сначала исследуем
            Long seqNo = dcSet.getTransactionFinalMapSigns().get(origAssetRef);

            if (seqNo == null) {
                return;
            }

            IssueAssetTransaction tx = (IssueAssetTransaction) dcSet.getTransactionFinalMap().get(seqNo);

            origAssetKey = tx.getKey();
            origAsset = dcSet.getItemAssetMap().get(origAssetKey);
        }

        if (andUpdateFromState && !isWiped())
            updateFromStateDB();

    }

    public boolean hasOriginal() {
        return (typeBytes[3] & WITHOUT_ORIGINAL_MASK) == 0;
    }

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {

        int len = getFeeLength();

        return (len + ((AssetVenture) item).getQuantity() * 5000L) * BlockChain.FEE_PER_BYTE;

    }

    public byte[] getOrigAssetRef() {
        return this.origAssetRef;
    }

    public long getOrigAssetKey() {
        return origAssetKey;
    }

    public AssetCls getOrigAsset() {
        return origAsset;
    }

    public String viewOrigAssetRef() {
        if (hasOriginal())
            return Base58.encode(this.origAssetRef);
        return "";
    }

    public int getTotal() {
        return (int) ((AssetVenture) item).getQuantity();
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
        JSONObject json = super.toJson();

        if (hasOriginal()) {
            json.put("originalRef", Base58.encode(origAssetRef));
            json.put("originalKey", origAssetKey);
        }

        return json;
    }

    public static Transaction Parse(byte[] data, int forDeal) throws Exception {
        //boolean asPack = releaserReference != null;

        //CHECK IF WE MATCH BLOCK LENGTH
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

        ExLink linkTo;
        if ((typeBytes[2] & HAS_EXLINK_MASK) > 0) {
            linkTo = ExLink.parse(data, position);
            position += linkTo.length();
        } else {
            linkTo = null;
        }

        byte feePow = 0;
        if (forDeal > Transaction.FOR_PACK) {
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
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

        // READ FOIL ASSET
        // asset parse without reference - if is = signature
        AssetVenture foilAsset = (AssetVenture) AssetFactory.getInstance().parse(forDeal,
                Arrays.copyOfRange(data, position, data.length), false);
        position += foilAsset.getDataLength(false);

        long origKey = 0;
        if (forDeal == FOR_DB_RECORD) {
            //READ FOIL KEY
            byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
            long key = Longs.fromByteArray(keyBytes);
            position += KEY_LENGTH;

            foilAsset.setKey(key);

            //READ ORIGINAL KEY
            if ((typeBytes[3] & WITHOUT_ORIGINAL_MASK) == 0) {
                keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
                origKey = Longs.fromByteArray(keyBytes);
                position += KEY_LENGTH;
            }

        }

        // READ ORIGINAL ASSET TX SIGNATURE
        byte[] assetRef;
        if ((typeBytes[3] & WITHOUT_ORIGINAL_MASK) == 0) {
            assetRef = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
            position += SIGNATURE_LENGTH;
        } else {
            assetRef = null;
        }

        if (forDeal > Transaction.FOR_MYPACK) {
            return new IssueAssetSeriesTransaction(typeBytes, creator, linkTo, assetRef, origKey, foilAsset, feePow, timestamp,
                    reference, signatureBytes, seqNo, feeLong);
        } else {
            return new IssueAssetSeriesTransaction(typeBytes, creator, linkTo, assetRef, foilAsset, signatureBytes);
        }
    }

    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = super.toBytes(forDeal, withSignature);

        if (forDeal == FOR_DB_RECORD) {
            if (hasOriginal()) {
                if (origAssetKey == 0) {
                    // для неподтвержденных когда еще номера нету
                    data = Bytes.concat(data, new byte[KEY_LENGTH]);
                } else {
                    byte[] keyBytes = Longs.toByteArray(origAssetKey);
                    data = Bytes.concat(data, keyBytes);
                }
            }

        }

        // WRITE ASSET REF
        if (hasOriginal()) {
            data = Bytes.concat(data, this.origAssetRef);
        }

        return data;
    }

    // VALIDATE

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        int len = super.getDataLength(forDeal, withSignature);

        if (hasOriginal()) {
            len += SIGNATURE_LENGTH;

            if (forDeal == Transaction.FOR_DB_RECORD) {
                len += KEY_LENGTH;
            }
        }

        return len;
    }

    @Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        if (hasOriginal()) {
            if (origAssetKey == 0L
                    || origAsset == null) {
                return ITEM_ASSET_NOT_EXIST;
            }

            if (origAsset.getAssetType() != AssetCls.AS_NON_FUNGIBLE) {
                errorValue = "original not NFT";
                return INVALID_ASSET_TYPE;
            }
            if (!origAsset.isUnique()) {
                errorValue = "original not unique";
                return INVALID_ASSET_TYPE;
            }

            if (item.getIconType() != ItemCls.MEDIA_TYPE_IMG) {
                return Transaction.INVALID_ICON_TYPE;
            }
            if (item.getImageType() != ItemCls.MEDIA_TYPE_IMG) {
                return Transaction.INVALID_IMAGE_TYPE;
            }

            // проверим - а он имеет баланс Оригинала для выпуска Серии?
            Fun.Tuple2<BigDecimal, BigDecimal> own = creator.getBalance(dcSet, origAssetKey, Account.BALANCE_POS_OWN);
            if (own.b.signum() <= 0) {
                return Transaction.CREATOR_NOT_OWNER;
            }

        }

        // CHECK IF AMOUNT POSITIVE
        int total = (int) ((AssetVenture) item).getQuantity();
        if (total < 5 || total > 100) {
            return INVALID_AMOUNT;
        }

        // выше уже не проверяем обертку
        return super.isValid(forDeal, flags | NOT_VALIDATE_ITEM);
    }

    // PROCESS/ORPHAN

    /**
     * Суть такова что мы делаем новый ордер с новым ID так как иначе сортировка Сделок будет нарушена так как
     * будет по Инициатору ключ, а его мы тогда берем старый ИД. А надо новый чтобы история действий не менялась
     */
    @Override
    protected void processItem() {

        long origAssetKey;
        if (hasOriginal()) {
            origAssetKey = origAsset.getKey();
        } else {
            origAssetKey = 0L;
        }

        AssetVenture foilAsset = (AssetVenture) item;
        AssetUniqueSeriesCopy uniqueSeriesCopy;
        ItemMap map = item.getDBMap(dcSet);
        int total = (int) foilAsset.getQuantity();
        for (int indexCopy = 1; indexCopy <= total; indexCopy++) {

            uniqueSeriesCopy = AssetUniqueSeriesCopy.makeCopy(this, foilAsset, origAssetKey, total, indexCopy);

            //INSERT INTO DATABASE
            key = map.incrementPut(uniqueSeriesCopy);
            if (indexCopy == 1) {
                // Set KEY for foilAsset - it need foe other copies
                foilAsset.setKey(key);
            }

            // SET BALANCES
            creator.changeBalance(dcSet, false, false, key,
                    BigDecimal.ONE, false, false, false);

            // make HOLD balance
            if (!foilAsset.isUnHoldable()) {
                creator.changeBalance(dcSet, false, true, key,
                        BigDecimal.ONE.negate(), false, false, false);
            }

        }

    }

    @Override
    protected void orphanItem() {

        long copyKey;
        AssetVenture foilAsset = (AssetVenture) item;
        ItemMap map = foilAsset.getDBMap(dcSet);
        int total = (int) foilAsset.getQuantity();
        for (int indexDel = 0; indexDel < total; indexDel++) {

            copyKey = key - indexDel;

            //DELETE FROM DATABASE
            map.decrementDelete(copyKey);

            // SET BALANCES
            creator.changeBalance(dcSet, true, false, copyKey,
                    BigDecimal.ONE, false, false, false);

            // make HOLD balance
            if (!foilAsset.isUnHoldable()) {
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
        return new BigDecimal(((AssetVenture) item).getQuantity());
    }

    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

        assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);

        return assetAmount;
    }

}
