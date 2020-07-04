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
import org.erachain.core.crypto.Base64;
import org.erachain.core.exdata.ExData;
import org.erachain.core.item.ItemCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMapSigns;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;

//import java.math.BigDecimal;
//import java.math.BigInteger;


public class RSignNote extends Transaction implements Itemable {

    protected static final byte HAS_TEMPLATE_MASK = (byte) (1 << 7);
    protected static final byte HAS_DATA_MASK = (byte) (1 << 7);

    private static final byte TYPE_ID = (byte) SIGN_NOTE_TRANSACTION;
    private static final String NAME_ID = "Note";
    /*
    PROPERTIES:
    [0] - type
    [1] - version
    [2] bits[0] - =1 - has Template (OLD)
    [2] bits [6,7] - signers: 0 - none; 1..3 = 1..3; 4 = LIST -> 1 byte for LIST.len + 3
    [3] - < 0 - has DATA
     */
    protected long key; // key for Template
    protected byte[] data;
    protected PublicKeyAccount[] signers; // for all it need encrypt
    protected byte[][] signatures; // - multi sign

    ExData extendedData;

    public RSignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, long timestamp, Long reference) {

        super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

        this.key = templateKey;
        this.data = data;
    }

    public RSignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, feePow, templateKey, data, timestamp, reference);
        this.signature = signature;
    }

    public RSignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data,
                     long timestamp, Long reference, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, feePow, templateKey, data, timestamp, reference);
        this.signature = signature;
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    // asPack
    public RSignNote(byte[] typeBytes, PublicKeyAccount creator, long templateKey, byte[] data, Long reference, byte[] signature) {
        this(typeBytes, creator, (byte) 0, templateKey, data, 0l, reference);
        this.signature = signature;
        // not need this.calcFee();
    }

    public RSignNote(PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, templateKey, data, timestamp, reference, signature);
        // set props
        this.setTypeBytes();
    }

    public RSignNote(PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, templateKey, data, timestamp, reference);
        // set props
        this.setTypeBytes();
    }

    public RSignNote(byte version, byte ptoperty1, byte ptoperty2, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, version, ptoperty1, ptoperty2}, creator, feePow, templateKey, data, timestamp, reference);
        // set props
        this.setTypeBytes();
    }

    public RSignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data,
                     PublicKeyAccount[] signers, byte[][] signatures, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, feePow, templateKey, data, timestamp, reference, signature);
        this.signers = signers;
        this.signatures = signatures;
        this.setTypeBytes();
    }

    public RSignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data,
                     PublicKeyAccount[] signers, byte[][] signatures, long timestamp,
                     Long reference, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, feePow, templateKey, data, timestamp, reference, signature);
        this.signers = signers;
        this.signatures = signatures;
        this.setTypeBytes();
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    // as Pack
    public RSignNote(byte[] typeBytes, PublicKeyAccount creator, long templateKey, byte[] data,
                     PublicKeyAccount[] signers, byte[][] signatures, Long reference, byte[] signature) {
        this(typeBytes, creator, templateKey, data, reference, signature);
        this.signers = signers;
        this.signatures = signatures;
        this.setTypeBytes();
    }

    //GETTERS/SETTERS

    @Override
    public void setDC(DCSet dcSet, boolean andSetup) {
        super.setDC(dcSet, false);

        // LOAD values from EXTERNAL DATA
        parseDataV2WithoutFiles();

        if (typeBytes[1] > 2) {
            // если новый порядок - ключ в Даных
            key = extendedData.getTemplateKey();
        }

        if (andSetup && !isWiped())
            setupFromStateDB();
    }

    @Override
    public ItemCls getItem() {
        return extendedData.getTemplate();
    }

    @Override
    public void makeItemsKeys() {
        if (creatorPersonDuration != null && key != 0) {
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a},
                    new Object[]{ItemCls.TEMPLATE_TYPE, key}
            };
        } else if (creatorPersonDuration != null) {
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.PERSON_TYPE, creatorPersonDuration.a}
            };
        } else if (key != 0) {
            itemsKeys = new Object[][]{
                    new Object[]{ItemCls.TEMPLATE_TYPE, key}
            };
        }
    }

    public static boolean hasTemplate(byte[] typeBytes) {
        if (typeBytes[2] < 0) return true;
        return false;
    }

    public static int getSignersLength(byte[] typeBytes) {
        // Переверенем - а зачем? - типа 7 бит - это длинна
        byte mask = ~HAS_TEMPLATE_MASK;
        return typeBytes[2] & mask;
    }

    //GETTERS/SETTERS
    public void setSidnerSignature(int index, byte[] signature) {
        if (signatures == null)
            signatures = new byte[signers.length][];

        signatures[index] = signature;

    }

    protected boolean hasTemplate() {
        return hasTemplate(this.typeBytes);
    }

    protected void setTypeBytes() {

        byte vers = 0;

        byte prop1 = 0;
        if (this.signers != null && this.signers.length > 0) {
            int len = this.signers.length;
            if (len < 4) {
                prop1 = (byte) len;
            } else {
                prop1 = (byte) 4;
            }
        }
        // set has TEMPLATE byte
        if (this.key > 0 && this.typeBytes[1] < 3) prop1 = (byte) (HAS_TEMPLATE_MASK | prop1);

        byte prop2 = 0;
        if (data != null && data.length > 0) {
            prop2 = (byte) (prop2 | HAS_DATA_MASK);
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

    public PublicKeyAccount[] getSigners() {
        return this.signers;
    }

    public String[] getSignersB58() {
        String[] pbKeys = new String[0];
        int i = 0;
        for (PublicKeyAccount key : this.signers) {
            pbKeys[i++] = Base58.encode(key.getPublicKey());
        }
        ;
        return pbKeys;
    }

    public byte[][] getSignersSignatures() {
        return this.signatures;
    }

    public String[] getSignersSignaturesB58() {
        String[] items = new String[0];
        int i = 0;
        for (byte[] item : this.signatures) {
            items[i++] = Base58.encode(item);
        }
        ;
        return items;
    }

    @Override
    public boolean hasPublicText() {
        return extendedData.hasPublicText();
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        //ADD CREATOR/SERVICE/DATA
        if (data != null && data.length > 0) {

            if (getVersion() == 0 && this.isText() && !this.isEncrypted()) {
                transaction.put("data", new String(this.data, StandardCharsets.UTF_8));
            } else {
                transaction.put("data", Base64.encode(this.data));
            }

            transaction.put("encrypted", this.isEncrypted());
            transaction.put("isText", this.isText());
        }

        if (this.key > 0)
            transaction.put("template", this.key);

        if (signers != null && signers.length > 0) {
            transaction.put("singers", this.getSignersB58());
            transaction.put("signatures", this.getSignersSignaturesB58());
        }
        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        if (typeBytes[1] < 3 && this.key > 0) {
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
        if (typeBytes[1] < 3 && hasTemplate(typeBytes)) {
            //READ KEY
            byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
            key = Longs.fromByteArray(keyBytes);
            position += KEY_LENGTH;
        }

        // DATA +++ - from org.erachain.core.transaction.RSend.Parse(byte[], Long)
        byte[] externalData = null;
        byte[] encryptedByte = null;
        byte[] isTextByte = null;
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
        }

        int signersLen = getSignersLength(typeBytes);
        PublicKeyAccount[] signers = null;
        byte[][] signatures = null;
        if (signersLen > 0) {
            if (signersLen == 4) {
                //READ ONE BITE for len
                byte[] signersLenBytes = Arrays.copyOfRange(data, position, position + 1);
                signersLen = Byte.toUnsignedInt(signersLenBytes[0]) + 4;
                position++;
            }
            signers = new PublicKeyAccount[signersLen];
            signatures = new byte[signersLen][];
            for (int i = 0; i < signersLen; i++) {
                signers[i] = new PublicKeyAccount(Arrays.copyOfRange(data, position, position + CREATOR_LENGTH));
                position += CREATOR_LENGTH;
                signatures[i] = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
                position += SIGNATURE_LENGTH;
            }
        }

        if (signersLen == 0) {
            if (forDeal > Transaction.FOR_MYPACK) {
                return new RSignNote(typeBytes, creator, feePow, key, externalData,
                        timestamp, reference, signatureBytes, seqNo, feeLong);
            } else {
                return new RSignNote(typeBytes, creator, key, externalData, reference, signatureBytes);
            }
        } else {
            if (forDeal > Transaction.FOR_MYPACK) {
                return new RSignNote(typeBytes, creator, feePow, key, externalData, signers,
                        signatures, timestamp, reference, signatureBytes, seqNo, feeLong);
            } else {
                return new RSignNote(typeBytes, creator, key, externalData, signers, signatures, reference, signatureBytes);
            }
        }
    }

    //PROCESS/ORPHAN

    @Override
    public void process(Block block, int forDeal) {

        super.process(block, forDeal);
        if (Controller.getInstance().onlyProtocolIndexing)
            return;

        try {
            parseData(); // need for take HASHES from FILES
            byte[][] hashes = extendedData.getAllHashesAsBytes();
            Long dbKey = makeDBRef(height, seqNo);
            if (hashes != null) {
                for (byte[] hash : hashes) {
                    dcSet.getTransactionFinalMapSigns().put(hash, dbKey);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            Long error = null;
            error++;
        }
    }

    @Override
    public void orphan(Block block, int forDeal) {

        super.orphan(block, forDeal);

        if (Controller.getInstance().onlyProtocolIndexing)
            return;

        try {
            parseData(); // need for take HASHES from FILES
            byte[][] hashes = extendedData.getAllHashesAsBytes();
            if (hashes != null) {
                for (byte[] hash : hashes) {
                    dcSet.getTransactionFinalMapSigns().delete(hash);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            Long error = null;
            error++;
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

        int add_len = 0;
        if (this.data != null && this.data.length > 0)
            if (getVersion() > 2) {
                add_len += DATA_SIZE_LENGTH + this.data.length;
            } else {
                add_len += IS_TEXT_LENGTH + ENCRYPTED_LENGTH + DATA_SIZE_LENGTH + this.data.length;
            }

        if (this.key > 0 && getVersion() < 3)
            add_len += KEY_LENGTH;

        return base_len + add_len;
    }

    //@Override
    @Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        //CHECK DATA SIZE
        if (data == null && key <= 0)
            return INVALID_DATA_LENGTH;

        if (data != null && data.length > BlockChain.MAX_REC_DATA_BYTES) {
            return INVALID_DATA_LENGTH;
        }

        int result = super.isValid(forDeal, flags);
        if (result != Transaction.VALIDATE_OK) return result;

        // ITEM EXIST? - for assets transfer not need - amount expect instead
        if (this.key > 0 && !this.dcSet.getItemTemplateMap().contains(this.key))
            return Transaction.ITEM_DOES_NOT_EXIST;

        if (extendedData == null) {
            parseDataV2WithoutFiles();
        }

        JSONObject hashes = extendedData.getHashes();

        if (hashes != null) {
            for (Object hashObject : hashes.keySet()) {
                if (Base58.isExtraSymbols(hashObject.toString())) {
                    return INVALID_DATA_FORMAT;
                }
            }
        }

        if (height > BlockChain.VERS_4_23_01) {
            // только уникальные - так как иначе каждый новый перезатрет поиск старого
            parseData(); // need for take HASHES from FILES
            byte[][] allHashes = extendedData.getAllHashesAsBytes();
            if (allHashes != null && allHashes.length > 0) {
                TransactionFinalMapSigns map = dcSet.getTransactionFinalMapSigns();
                for (byte[] hash : allHashes) {
                    if (map.contains(hash)) {
                        return HASH_ALREDY_EXIST;
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

        for (Account item : extendedData.getRecipients()) {
            if (account.equals(item))
                return true;
        }

        return false;
    }

    @Override
    public long calcBaseFee() {
        return calcCommonFee();
    }

    public void parseDataV2WithoutFiles() {
        if (extendedData == null) {
            //Version, Title, JSON, Files
            try {
                // здесь нельзя сохранять в parsedData
                extendedData = ExData.parse(getVersion(), this.data, false, false);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                Long error = null;
                error++;
            }
            extendedData.resolveValues(dcSet);
        }
    }

    public void parseData() {

        if (extendedData == null || !extendedData.isParsedWithFiles()) {
            // если уже парсили или парсили без файлов а надо с файлами

            if (true || getVersion() == 2) {

                // version 2
                try {
                    extendedData = ExData.parse(getVersion(), this.data, false, true);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    Long error = null;
                    error++;
                }

            } else {

                // сюда не должно прийти - OLS
                Long error = null;
                error++;

                // version 1
                String text = new String(getData(), StandardCharsets.UTF_8);

                try {
                    JSONObject dataJson = (JSONObject) JSONValue.parseWithException(text);
                    String title = dataJson.get("Title").toString();

                    extendedData = new ExData(1, title, dataJson, null);

                } catch (ParseException e) {
                    // version 0
                    String[] items = text.split("\n");
                    JSONObject dataJson = new JSONObject();
                    dataJson.put("Message", text.substring(items[0].length()));
                    extendedData = new ExData(0, items[0], dataJson, null);
                }
            }
            extendedData.resolveValues(dcSet);
        }
    }

    public boolean isFavorite() {
        return Controller.getInstance().wallet.database.getDocumentFavoritesSet().contains(this.dbRef);
    }

    public boolean decrypt(Account recipient) {
        return extendedData.decrypt(creator, recipient);
    }
}
