package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.exdata.exActions.ExAction;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAuthor;
import org.erachain.core.exdata.exLink.ExLinkSource;
import org.erachain.core.item.ItemCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMapSigns;
import org.erachain.gui.library.ASMutableTreeNode;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.mapdb.Fun;

import javax.swing.tree.DefaultMutableTreeNode;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * PROPERTIES:
 * [0] - type
 * [1] - version
 * [2] bits[0] - =1 - has Template (OLD)
 * [3] - < 0 - has DATA
 */
public class RSignNote extends Transaction implements Itemable {

    public static final byte CURRENT_VERS = 3;

    protected static final byte HAS_TEMPLATE_MASK = (byte) (1 << 7);
    protected static final byte HAS_DATA_MASK = (byte) (1 << 7);
    protected static final byte HAS_DB_DATA_MASK = (byte) 1;

    public static final byte TYPE_ID = (byte) SIGN_NOTE_TRANSACTION;
    public static final String TYPE_NAME = "Note";
    protected long key; // key for Template
    protected byte[] data;
    /**
     * Тут запоминаем откуда данные получили - если из базы то и парсим соответствующе
     */
    protected byte[] dataForDB;
    ExData extendedData;

    public RSignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, byte[] dataForDB, long timestamp, long flags) {

        super(typeBytes, TYPE_NAME, creator, null, null, feePow, timestamp, flags);

        this.key = templateKey;
        this.data = data;
        this.dataForDB = dataForDB;
    }

    public RSignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, byte[] dataForDB, long timestamp, long flags, byte[] signature) {
        this(typeBytes, creator, feePow, templateKey, data, dataForDB, timestamp, flags);
        this.signature = signature;
    }

    public RSignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data,
                     byte[] dataForDB, long timestamp, long flags, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, feePow, templateKey, data, dataForDB, timestamp, flags);
        this.signature = signature;
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    // asPack
    public RSignNote(byte[] typeBytes, PublicKeyAccount creator, long templateKey, byte[] data, byte[] dataForDB, long flags, byte[] signature) {
        this(typeBytes, creator, (byte) 0, templateKey, data, dataForDB, 0L, flags);
        this.signature = signature;
        // not need this.calcFee();
    }

    public RSignNote(PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, byte[] dataForDB, long timestamp, long flags, byte[] signature) {
        this(new byte[]{TYPE_ID, CURRENT_VERS, 0, 0}, creator, feePow, templateKey, data, dataForDB, timestamp, flags, signature);
        // set props
        this.setTypeBytes();
    }

    public RSignNote(PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, byte[] dataForDB, long timestamp, long flags) {
        this(new byte[]{TYPE_ID, CURRENT_VERS, 0, 0}, creator, feePow, templateKey, data, dataForDB, timestamp, flags);
        // set props
        this.setTypeBytes();
    }

    public RSignNote(byte version, byte ptoperty1, byte ptoperty2, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, long timestamp, long flags) {
        this(new byte[]{TYPE_ID, version, ptoperty1, ptoperty2}, creator, feePow, templateKey, data, null, timestamp, flags);
        // set props
        this.setTypeBytes();
    }

    //GETTERS/SETTERS

    @Override
    public void setDC(DCSet dcSet, boolean andUpdateFromState) {
        super.setDC(dcSet, false);


        // LOAD values from EXTERNAL DATA
        parseDataV2WithoutFiles();

        if (typeBytes[1] > 2) {
            // если новый порядок - ключ в Данных
            key = extendedData.getTemplateKey();
        }

        if (andUpdateFromState && !isWiped())
            updateFromStateDB();
    }

    @Override
    public ItemCls getItem() {
        if (extendedData.hasExAction()) {
            return getExAction().getAsset();
        }
        return extendedData.getTemplate();
    }

    @Override
    public void makeItemsKeys() {
        if (isWiped()) {
            itemsKeys = new Object[][]{};
        }

        List<Object> listTags = new ArrayList<>();

        if (creatorPersonDuration != null) {
            // AS PERSON
            listTags.add(new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a, creatorPerson.getTags()});
            // AS AUTHOR
            listTags.add(new Object[]{ItemCls.AUTHOR_TYPE, creatorPersonDuration.a, creatorPerson.getTags()});
        }

        if (extendedData.hasExAction()) {
            extendedData.getExAction().updateItemsKeys(listTags);
        }

        if (typeBytes[1] > 1 && extendedData != null && extendedData.getTemplateKey() != 0L) {
            // если новый порядок - ключ в Данных
            listTags.add(new Object[]{ItemCls.TEMPLATE_TYPE, extendedData.getTemplateKey()});
        } else if (key != 0L) {
            listTags.add(new Object[]{ItemCls.TEMPLATE_TYPE, key});
        }

        if (extendedData.hasAuthors()) {
            for (ExLinkAuthor author : extendedData.getAuthors()) {
                listTags.add(new Object[]{ItemCls.AUTHOR_TYPE, author.getRef()});
            }
        }

        itemsKeys = listTags.toArray(new Object[][]{});

    }

    @Override
    public ExLink getExLink() {
        // нельзя использовать внутренюю от Трнзакции - так как она начнет по другому байт-код делать и парсить
        return extendedData.getExLink();
    }

    public ExAction getExAction() {
        return extendedData.getExAction();
    }

    public boolean hasExAction() {
        return extendedData.hasExAction();
    }

    @Override
    public String getExTags() {
        if (extendedData != null) {
            byte[] exTags = extendedData.getTags();
            if (exTags != null && exTags.length > 0) {
                return new String(exTags, StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    public static boolean hasTemplate(byte[] typeBytes) {
        if (typeBytes[2] < 0) return true;
        return false;
    }

    protected boolean hasTemplate() {
        return hasTemplate(this.typeBytes);
    }

    public boolean hasLinkRecipients() {
        return extendedData.hasRecipients();
    }

    public Account[] getRecipients() {
        return extendedData.getRecipients();
    }

    public boolean hasAuthors() {
        return extendedData.hasAuthors();
    }

    public boolean hasSources() {
        return extendedData.hasSources();
    }

    public ExLinkAuthor[] getAuthors() {
        return extendedData.getAuthors();
    }

    public ExLinkSource[] getSources() {
        return extendedData.getSources();
    }

    public static int getSignersLength(byte[] typeBytes) {
        // Переверенем - а зачем? - типа 7 бит - это длинна
        byte mask = ~HAS_TEMPLATE_MASK;
        return typeBytes[2] & mask;
    }

    protected void setTypeBytes() {

        byte vers = 0;

        byte prop1 = 0;

        // set has TEMPLATE byte for OLD Versions (0..2)
        if (this.key > 0 && this.typeBytes[1] < 3) prop1 = (byte) (HAS_TEMPLATE_MASK | prop1);

        byte prop2 = 0;
        if (data != null && data.length > 0) {
            prop2 = (byte) (prop2 | HAS_DATA_MASK);

            if (dataForDB != null && dataForDB.length > 0) {
                prop2 = (byte) (prop2 | HAS_DB_DATA_MASK);
            }
        }

        if (this.typeBytes == null) {
            this.typeBytes = new byte[]{TYPE_ID, vers, prop1, prop2};
        } else {
            this.typeBytes[2] = prop1; // property 1
            this.typeBytes[3] = prop2; // property 2
        }
    }

    @Override
    public long getKey() {
        if (this.key == 0 && typeBytes[1] > 1) {
            // если новый порядок - ключ в Данных
            if (extendedData == null) {
                parseDataV2WithoutFiles();
            }

            if (extendedData.hasExAction()) {
                return getExAction().getAssetKey();
            }

            return extendedData.getTemplateKey();
        }
        return this.key;
    }

    /**
     * Title не может быть Нуль
     *
     * @return
     */
    @Override
    public String getTitle() {

        if (extendedData == null) {
            if (getVersion() > 1) {

                // version +2
                try {
                    // парсим только заголовок
                    parseDataV2WithoutFiles();
                    return extendedData.getTitle();

                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    Long error = null;
                    error++;
                    return "";
                }

            } else {

                // version 1
                String text = new String(getData(), StandardCharsets.UTF_8);

                try {
                    JSONObject dataJson = (JSONObject) JSONValue.parseWithException(text);
                    if (dataJson.containsKey("Title")) {
                        return dataJson.get("Title").toString();
                    }
                    return "";

                } catch (ParseException e) {
                    // version 0
                    return text.split("\n")[0];
                }
            }
        } else {
            return extendedData.getTitle();
        }
    }


    public byte[] getData() {
        return this.data;
    }

    public String getMessage() {
        if (extendedData == null) {
            parseDataV2WithoutFiles();
        }
        return extendedData.getMessage();
    }

    @Override
    public BigDecimal getAmount() {
        if (extendedData == null) {
            parseDataV2WithoutFiles();
        }

        if (extendedData.hasExAction()) {
            return getExAction().getTotalPay();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getAmount(Account account) {
        if (extendedData == null) {
            parseDataV2WithoutFiles();
        }

        if (extendedData.hasExAction()) {
            return getExAction().getAmount(account);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String viewSubTypeName() {
        if (extendedData == null) {
            parseDataV2WithoutFiles();
        }

        if (extendedData.hasExAction()) {
            return Lang.T(getExAction().viewActionType()) + ":" + Lang.T(getExAction().viewType());
        }

        return "";
    }

    @Override
    public String viewSubTypeName(JSONObject langObj) {
        if (extendedData == null) {
            parseDataV2WithoutFiles();
        }

        if (extendedData.hasExAction()) {
            return Lang.T(getExAction().viewActionType(), langObj) + ":" + Lang.T(getExAction().viewType(), langObj);
        }

        return "";
    }


    public DefaultMutableTreeNode viewLinksTree() {

        DefaultMutableTreeNode root = super.viewLinksTree();

        if (extendedData.getTemplate() != null) {
            ASMutableTreeNode item = new ASMutableTreeNode(Lang.T("Template"));
            item.add(new DefaultMutableTreeNode(extendedData.getTemplate()));
            if (root == null) root = new DefaultMutableTreeNode(this);

            root.add(item);
        }

        if (extendedData.hasAuthors()) {
            ASMutableTreeNode item = new ASMutableTreeNode(Lang.T("Authors"));
            for (ExLinkAuthor author : extendedData.getAuthors()) {
                item.add(new DefaultMutableTreeNode(author));
            }
            if (root == null) root = new DefaultMutableTreeNode(this);
            root.add(item);
        }

        if (extendedData.hasSources()) {
            ASMutableTreeNode item = new ASMutableTreeNode(Lang.T("Sources"));
            for (ExLinkSource source : extendedData.getSources()) {
                item.add(new DefaultMutableTreeNode(source));
            }
            if (root == null) root = new DefaultMutableTreeNode(this);
            root.add(item);
        }

        return root;

    }

    public ExData getExData() {
        return this.extendedData;
    }

    public boolean isText() {
        if (data == null || data.length == 0) return false;
        return true; // || Arrays.equals(this.isText, new byte[1])) ? false : true;
    }

    public boolean isEncrypted() {
        return extendedData.isEncrypted();
    }

    @Override
    public boolean hasPublicText() {
        return extendedData.hasPublicText();
    }

    public boolean isCanSignOnlyRecipients() {
        return extendedData.isCanSignOnlyRecipients();
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        // если из RAW берется JSON - то надо просчитать все
        parseDataFull();

        //ADD CREATOR/SERVICE/DATA
        if (data != null && data.length > 0) {

            transaction.put("exData", extendedData.toJson());
            // SLOW for HUGE files - transaction.put("data64", Base64.getEncoder().encodeToString(this.data));

        }

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        if (forDeal == FOR_DB_RECORD
                || typeBytes[1] < 3 && this.key > 0) {
            //WRITE KEY
            byte[] keyBytes = Longs.toByteArray(this.key);
            keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
            data = Bytes.concat(data, keyBytes);
        }

        if (this.data != null) {

            //WRITE DATA SIZE
            byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
            data = Bytes.concat(data, dataSizeBytes);

            //WRITE DATA
            data = Bytes.concat(data, this.data);

            if (typeBytes[1] < 3) {
                //WRITE ENCRYPTED
                data = Bytes.concat(data, new byte[]{0}); //this.encrypted);

                //WRITE ISTEXT
                data = Bytes.concat(data, new byte[]{1}); //this.isText);
            }

            if (forDeal == Transaction.FOR_DB_RECORD && typeBytes[1] > 2) {
                // only for 3+ version

                if (this.extendedData != null) {
                    this.dataForDB = this.extendedData.makeDBData();
                }

                if (this.dataForDB == null || this.dataForDB.length == 0) {
                    data = Bytes.concat(data, new byte[DATA_SIZE_LENGTH]);
                } else {
                    //WRITE DB-DATA SIZE
                    byte[] dataDBSizeBytes = Ints.toByteArray(this.dataForDB.length);
                    data = Bytes.concat(data, dataDBSizeBytes);

                    //WRITE DB-DATA
                    data = Bytes.concat(data, this.dataForDB);
                }
            }

        }

        return data;
    }


    // releaserReference = null - not a pack
    // releaserReference = reference for releaser account - it is as pack
    public static Transaction Parse(byte[] data, int forDeal) throws Exception {
        //boolean asPack = forDeal != null;

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

        //READ FLAGS
        byte[] flagsBytes = Arrays.copyOfRange(data, position, position + FLAGS_LENGTH);
        long flagsTX = Longs.fromByteArray(flagsBytes);
        position += FLAGS_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        //
        // Transaction.exLink not USED here
        //

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

        //////// local parameters

        long key = 0L;
        if (forDeal == FOR_DB_RECORD
                || typeBytes[1] < 3 && hasTemplate(typeBytes)) {
            //READ KEY
            byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
            key = Longs.fromByteArray(keyBytes);
            position += KEY_LENGTH;
        }

        // DATA +++ - from org.erachain.core.transaction.RSend.Parse(byte[], Long)
        byte[] externalData = null;
        byte[] encryptedByte = null;
        byte[] isTextByte = null;
        byte[] dbData = null;

        if (typeBytes[3] < 0) {
            // IF here is DATA

            //READ DATA SIZE
            byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            position += DATA_SIZE_LENGTH;

            //READ DATA
            externalData = Arrays.copyOfRange(data, position, position + dataSize);
            position += dataSize;

            if (typeBytes[1] < 3) {
                encryptedByte = Arrays.copyOfRange(data, position, position + ENCRYPTED_LENGTH);
                position += ENCRYPTED_LENGTH;

                isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
                position += IS_TEXT_LENGTH;
            }

            if (forDeal == Transaction.FOR_DB_RECORD && typeBytes[1] > 2) {
                // only for 3+ version
                // ADD local DB-DATA
                //READ DB-DATA SIZE
                byte[] dbDataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
                int dbDataSize = Ints.fromByteArray(dbDataSizeBytes);
                position += DATA_SIZE_LENGTH;

                if (dbDataSize > 0) {
                    //READ DB-DATA
                    dbData = Arrays.copyOfRange(data, position, position + dbDataSize);
                    position += dbDataSize;
                }
            }

        }


        if (forDeal > Transaction.FOR_MYPACK) {
            return new RSignNote(typeBytes, creator, feePow, key, externalData,
                    dbData, timestamp, flagsTX, signatureBytes, seqNo, feeLong);
        } else {
            return new RSignNote(typeBytes, creator, key, externalData, dbData, flagsTX, signatureBytes);
        }
    }

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

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        if (dApp != null) {
            if (forDeal == FOR_DB_RECORD || !dApp.isEpoch()) {
                base_len += dApp.length(forDeal);
            }
        }

        int add_len = 0;
        if (this.data != null && this.data.length > 0)
            add_len += DATA_SIZE_LENGTH + this.data.length;

        if (getVersion() > 2) {
            if (forDeal == FOR_DB_RECORD) {
                add_len += DATA_SIZE_LENGTH + (extendedData == null ?
                        dataForDB == null || dataForDB.length == 0 ? 0 : dataForDB.length : extendedData.getLengthDBData());
            }
        } else {
            add_len += IS_TEXT_LENGTH + ENCRYPTED_LENGTH;
        }

        if (forDeal == FOR_DB_RECORD
                || this.key > 0 && getVersion() < 3)
            add_len += KEY_LENGTH;

        return base_len + add_len;
    }

    //PROCESS/ORPHAN

    @Override
    public void processBody(Block block, int forDeal) {

        super.processBody(block, forDeal);

        parseDataFull(); // need for take HASHES from FILES
        extendedData.process(this, block);

        byte[][] hashes = extendedData.getAllHashesAsBytes(true);
        if (hashes != null) {
            Long dbKey = makeDBRef(height, seqNo);
            for (byte[] hash : hashes) {
                dcSet.getTransactionFinalMapSigns().put(hash, dbKey);
            }
        }

    }

    @Override
    public void orphanBody(Block block, int forDeal) {

        parseDataFull(); // also need for take HASHES from FILES
        extendedData.orphan(this);

        byte[][] hashes = extendedData.getAllHashesAsBytes(true);
        if (hashes != null) {
            for (byte[] hash : hashes) {
                dcSet.getTransactionFinalMapSigns().delete(hash);
            }
        }

        super.orphanBody(block, forDeal);

    }

    //@Override
    @Override
    public int isValid(int forDeal, long checkFlags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        //CHECK DATA SIZE
        if (data == null && key <= 0)
            return INVALID_DATA_LENGTH;

        if (data != null && data.length > MAX_DATA_BYTES_LENGTH) {
            errorValue = "DATA: totalLen (" + data.length + ")  > MAX_DATA_BYTES_LENGTH (" + MAX_DATA_BYTES_LENGTH + ")";
            return INVALID_DATA_LENGTH;
        }

        // parse with files
        // need for test PUBLIC by files
        parseDataFull();

        int result;

        // тут еще нет преРасчета и непонятно какая плата будет
        // и общая комиссия будет неверной, поэтому не считаем её тут
        if ((result = super.isValid(forDeal, checkFlags | NOT_VALIDATE_FLAG_FEE))
                != Transaction.VALIDATE_OK) return result;

        // ITEM EXIST? - for assets transfer not need - amount expect instead
        if (this.key > 0 && !this.dcSet.getItemTemplateMap().contains(this.key))
            return Transaction.ITEM_DOES_NOT_EXIST;

        result = extendedData.isValid(this);
        if (result != Transaction.VALIDATE_OK) {
            // errorValue updated in extendedData
            errorValue = extendedData.errorValue;
            return result;
        }

        // тут отдельно посчитаем комиссию - после переРасчета начислений по Фильтру она будет корректной
        calcFee(true);
        if ((checkFlags & NOT_VALIDATE_FLAG_FEE) == 0L
                && height > BlockChain.ALL_BALANCES_OK_TO
                && !BlockChain.isFeeEnough(height, creator)
                && this.creator.getForFee(dcSet).compareTo(this.fee) < 0) {
            return NOT_ENOUGH_FEE;
        }

        if (height > BlockChain.VERS_5_01_01) {
            // только уникальные - так как иначе каждый новый перезатрет поиск старого
            byte[][] allHashes = extendedData.getAllHashesAsBytes(true);
            if (allHashes != null && allHashes.length > 0) {
                TransactionFinalMapSigns map = dcSet.getTransactionFinalMapSigns();
                for (byte[] hash : allHashes) {
                    if (map.contains(hash)) {
                        errorValue = Base58.encode(hash);
                        return HASH_ALREADY_EXIST;
                    }
                }
            }
        }

        return Transaction.VALIDATE_OK;
    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>(8, 1);
        accounts.add(this.creator);
        if (extendedData.hasRecipients()) {
            for (Account account : extendedData.getRecipients()) {
                accounts.add(account);
            }
        }
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<>(8, 1);
        if (extendedData.hasRecipients()) {
            for (Account account : extendedData.getRecipients()) {
                accounts.add(account);
            }
        }
        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {

        if (account.equals(this.creator)) {
            return true;
        }

        if (extendedData == null) {
            parseDataV2WithoutFiles();
        }

        if (extendedData.isInvolved(account))
            return true;

        return false;
    }

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {

        long long_fee = super.calcBaseFee(withFreeProtocol);
        if (false && // дело втом что выше не считается общая комиссия и она как первая транзакция = 0 становится
                long_fee == 0)
            return getRoyaltyFee();

        byte[][] allHashes = extendedData.getAllHashesAsBytes(true);

        if (allHashes != null) {
            long_fee += allHashes.length * 100 * BlockChain.FEE_PER_BYTE;
        }

        if (false && extendedData.hasRecipients()) {
            long_fee += extendedData.getRecipients().length * 25 * BlockChain.FEE_PER_BYTE;
        }

        if (getExLink() != null)
            long_fee += 100 * BlockChain.FEE_PER_BYTE;

        ExAction exAction = extendedData.getExAction();
        if (exAction != null) { /// тут еще нет ПРЕрасчета и так как в setDC ет прерасчетап
            long_fee += exAction.getTotalFeeBytes() * BlockChain.FEE_PER_BYTE;
        }

        if (extendedData.hasAuthors()) {
            long_fee += extendedData.getAuthors().length * 100 * BlockChain.FEE_PER_BYTE;
        }

        if (extendedData.hasSources()) {
            long_fee += extendedData.getSources().length * 100 * BlockChain.FEE_PER_BYTE;
        }

        return long_fee + getRoyaltyFee();
    }

    @Override
    public long getRoyaltyFee() {
        return extendedData.getRoyaltyFee();
    }

    public void parseDataV2WithoutFiles() {
        if (extendedData == null) {
            //Version, Title, JSON, Files
            try {
                // здесь нельзя сохранять в parsedData
                extendedData = ExData.parse(getVersion(), this.data, false, false);
                if (dataForDB != null)
                    extendedData.parseDBData(dataForDB);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                Long error = null;
                error++;
            }
        }

        // may by in new FORKED DB - try set if NEW
        if (dcSet != null)
            extendedData.setDC(dcSet);

    }

    public void parseDataFull() {

        if (extendedData == null || !extendedData.isParsedWithFiles()) {
            // если уже парсили или парсили без файлов а надо с файлами

            // version 2
            try {
                extendedData = ExData.parse(getVersion(), this.data, false, true);
                if (dataForDB != null)
                    extendedData.parseDBData(dataForDB);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                Long error = null;
                error++;
            }
        }

        // may by in new FORKED DB - try set if NEW
        if (dcSet != null)
            extendedData.setDC(dcSet);
    }

    public boolean isFavorite() {
        if (Controller.getInstance().doesWalletKeysExists()) {
            return Controller.getInstance().getWallet().dwSet.getDocumentFavoritesSet().contains(this.dbRef);
        }
        return false;
    }

    public Fun.Tuple3<Integer, String, RSignNote> decrypt(Account recipient) {

        Fun.Tuple3<Integer, String, ExData> decryptedExData = extendedData.decrypt(creator, recipient);
        if (decryptedExData.c == null) {
            return new Fun.Tuple3<>(decryptedExData.a, decryptedExData.b, null);
        }

        byte[] exData;
        try {
            exData = decryptedExData.c.toByte();
        } catch (Exception e) {
            return new Fun.Tuple3<>(decryptedExData.a, e.getMessage(), null);
        }

        RSignNote decryptedNote = new RSignNote(typeBytes, creator, feePow, key, exData,
                dataForDB, timestamp, extFlags, signature,
                seqNo, fee.longValue());
        return new Fun.Tuple3<>(decryptedExData.a, decryptedExData.b, decryptedNote);

    }

    public Fun.Tuple2<String, RSignNote> decryptByPassword(byte[] password) {

        Fun.Tuple2<String, ExData> decryptedExData = extendedData.decryptByPassword(password);
        if (decryptedExData.b == null) {
            return new Fun.Tuple2<>(decryptedExData.a, null);
        }

        byte[] exData;
        try {
            exData = decryptedExData.b.toByte();
        } catch (Exception e) {
            return new Fun.Tuple2<>(e.getMessage(), null);
        }

        RSignNote decryptedNote = new RSignNote(typeBytes, creator, feePow, key, exData,
                dataForDB, timestamp, extFlags, signature,
                seqNo, fee.longValue());
        return new Fun.Tuple2<>(null, decryptedNote);

    }

    public Fun.Tuple2<String, RSignNote> decryptByPassword(String password) {
        try {
            byte[] pass = Base58.decode(password);
            return decryptByPassword(pass);
        } catch (Exception e) {
            return new Fun.Tuple2<>(e.getMessage(), null);
        }
    }

    public Fun.Tuple3<Integer, String, byte[]> getPassword(Account recipient) {
        return extendedData.getPassword(creator, recipient);
    }
}
