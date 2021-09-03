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
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.HashesSignsMap;
import org.erachain.datachain.TransactionFinalMapSigns;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;


/**
 * typeBytes[2].[3] in OLD vers - as HASHES.len
 * vers 1 - HASHES.len in ext data
 */

public class RHashes extends Transaction {

    public static final byte TYPE_ID = (byte) HASHES_RECORD;
    public static final String TYPE_NAME = "Hashes";

    private static final int URL_SIZE_LENGTH = 1;
    public static final int MAX_URL_LENGTH = Transaction.MAX_TITLE_BYTES_LENGTH;
    private static final int HASH_LENGTH = 32;

    protected static final int LOAD_LENGTH = URL_SIZE_LENGTH + DATA_SIZE_LENGTH;
    protected static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH;

    protected byte[] url; // url
    protected byte[] data;
    protected byte[][] hashes;

    public RHashes(byte[] typeBytes, PublicKeyAccount creator, ExLink exLink, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference) {

        super(typeBytes, TYPE_NAME, creator, exLink, null, feePow, timestamp, reference);

        this.url = url;
        this.data = data;
        this.hashes = hashes;

    }

    public RHashes(byte[] typeBytes, PublicKeyAccount creator, ExLink exLink, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, exLink, feePow, url, data, hashes, timestamp, reference);
        this.signature = signature;
    }

    public RHashes(byte[] typeBytes, PublicKeyAccount creator, ExLink exLink, byte feePow, byte[] url, byte[] data, byte[][] hashes,
                   long timestamp, Long reference, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, exLink, feePow, url, data, hashes, timestamp, reference);
        this.signature = signature;
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    // asPack
    public RHashes(byte[] typeBytes, PublicKeyAccount creator, byte[] url, byte[] data, byte[][] hashes, Long reference, byte[] signature) {
        this(typeBytes, creator, null, (byte) 0, url, data, hashes, 0L, reference);
        this.signature = signature;
        // not need this.calcFee();
    }

    // OLD VERS
    public RHashes(PublicKeyAccount creator, ExLink exLink, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, exLink, feePow, url, data, hashes, timestamp, reference, signature);
        setTypeBytes();
    }

    // NEW VERS
    public RHashes(PublicKeyAccount creator, ExLink exLink, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, (byte) 1, 0, 0}, creator, exLink, feePow, url, data, hashes, timestamp, reference);
    }

    public static int getHashesLength(byte[] typeBytes) {
        return Ints.fromBytes((byte) 0, (byte) 0, typeBytes[2], typeBytes[3]);
    }

    protected void setTypeBytes() {

        byte[] bytesLen = Ints.toByteArray(this.hashes.length);

        this.typeBytes[2] = bytesLen[2];
        this.typeBytes[3] = bytesLen[3];

    }

    // releaserReference = null - not a pack
    // releaserReference = reference for releaser account - it is as pack
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

        ExLink exLink;
        if ((typeBytes[2] & HAS_EXLINK_MASK) > 0) {
            exLink = ExLink.parse(data, position);
            position += exLink.length();
        } else {
            exLink = null;
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

        //////// local parameters

        //READ NAME
        int urlLength = Byte.toUnsignedInt(data[position]);
        position++;

        if (urlLength > MAX_URL_LENGTH) {
            throw new Exception("Invalid URL length");
        }

        byte[] url = Arrays.copyOfRange(data, position, position + urlLength);
        position += urlLength;

        //READ DATA SIZE
        byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
        int dataSize = Ints.fromByteArray(dataSizeBytes);
        position += DATA_SIZE_LENGTH;

        //READ DATA
        byte[] arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
        position += dataSize;

        //READ HASHES SIZE
        int hashesLen;
        if (typeBytes[1] > 0) {
            byte[] hashesSizeBytes = Arrays.copyOfRange(data, position, position + 4);
            hashesLen = Ints.fromByteArray(hashesSizeBytes);
            position += 4;
        } else {
            hashesLen = getHashesLength(typeBytes);
        }

        byte[][] hashes = new byte[hashesLen][];
        for (int i = 0; i < hashesLen; i++) {
            hashes[i] = Arrays.copyOfRange(data, position, position + HASH_LENGTH);
            position += HASH_LENGTH;
        }

        if (forDeal > Transaction.FOR_MYPACK) {
            return new RHashes(typeBytes, creator, exLink, feePow, url, arbitraryData, hashes, timestamp, reference,
                    signatureBytes, seqNo, feeLong);
        } else {
            return new RHashes(typeBytes, creator, url, arbitraryData, hashes, reference, signatureBytes);
        }

    }

    public byte[] getURL() {
        return this.url;
    }

    public byte[] getData() {
        return this.data;
    }

    public String[] getHashesB58() {
        String[] strHashes = new String[hashes.length];
        int i = 0;
        for (byte[] hash : this.hashes) {
            strHashes[i++] = Base58.encode(hash);
        }
        ;
        return strHashes;
    }

    public byte[][] getHashes() {
        return this.hashes;
    }

    @Override
    public boolean hasPublicText() {
        if (url != null && url.length != 0)
            return true;

        if (true || data == null || data.length == 0)
            return false;

        return true;

    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        //ADD CREATOR/SERVICE/DATA
        if (data != null && data.length > 0) {
            transaction.put("data", new String(this.data, StandardCharsets.UTF_8));
            transaction.put("message", new String(this.data, StandardCharsets.UTF_8));
            //transaction.put("data", Base58.encode(this.data));
        }
        if (url != null && url.length > 0) {
            transaction.put("url", new String(this.url, StandardCharsets.UTF_8));
        }

        JSONArray hashesArray = new JSONArray();
        for (byte[] hash : this.hashes) {
            hashesArray.add(Base58.encode(hash));
        }
        transaction.put("hashes", hashesArray);

        return transaction;
    }

    //@Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        //WRITE URL SIZE
        if (this.url == null) {
            this.url = new byte[0];
        }

        data = Bytes.concat(data, new byte[]{(byte) this.url.length});

        //WRITE URL
        data = Bytes.concat(data, this.url);

        //WRITE DATA SIZE
        data = Bytes.concat(data, Ints.toByteArray(this.data.length));

        //WRITE DATA
        data = Bytes.concat(data, this.data);

        //WRITE HASHES SIZE - new VERS
        if (typeBytes[1] > 0)
            data = Bytes.concat(data, Ints.toByteArray(this.data.length));

        for (int i = 0; i < this.hashes.length; i++)
            data = Bytes.concat(data, this.hashes[i]);


        return data;
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

        if (exLink != null)
            base_len += exLink.length();

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        // new VERS + SIZE
        if (typeBytes[1] > 0)
            base_len += 4;

        int add_len = this.url == null ? 0 : this.url.length
                + this.data.length
                + this.hashes.length * HASH_LENGTH;

        return base_len + add_len;
    }

    //@Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        //CHECK DATA SIZE
        if (url != null && url.length > MAX_URL_LENGTH) {
            errorValue = "" + url.length;
            return INVALID_URL_LENGTH;
        }

        if (data.length > Transaction.MAX_DATA_BYTES_LENGTH) {
            errorValue = "" + data.length;
            return INVALID_DATA_LENGTH;
        }

        if (hashes.length > 2 * Short.MAX_VALUE - 1) {
            errorValue = "" + hashes.length;
            return INVALID_PARAMS_LENGTH;
        }

        int result = super.isValid(forDeal, flags);
        if (result != Transaction.VALIDATE_OK) return result;

        if (height > BlockChain.VERS_4_23_01) {
            // только уникальные - так как иначе каждый новый перезатрет поиск старого
            if (hashes != null && hashes.length > 0) {
                TransactionFinalMapSigns map = dcSet.getTransactionFinalMapSigns();
                for (byte[] hash : hashes) {
                    if (map.contains(hash)) {
                        errorValue = Base58.encode(hash);
                        return HASH_ALREADY_EXIST;
                    }
                }
            }
        }

        return Transaction.VALIDATE_OK;

    }

    //PROCESS/ORPHAN

    public void process(Block block, int forDeal) {

        //UPDATE SENDER
        super.process(block, forDeal);

        int height = this.getBlockHeightByParentOrLast(dcSet);

        int transactionIndex = -1;

        long personKey;
        Tuple2<Integer, PersonCls> asPerson = this.creator.getPerson(dcSet, height);
        if (asPerson != null && asPerson.a >= 0) {
            personKey = asPerson.b.getKey();
        } else {
            personKey = 0l;
        }

        HashesSignsMap map = dcSet.getHashesSignsMap();
        for (byte[] hash : hashes) {
            map.addItem(hash, new Tuple3<Long, Integer, Integer>(personKey, height, transactionIndex));
        }

        if (!Controller.getInstance().onlyProtocolIndexing) {
            TransactionFinalMapSigns mapSigns = dcSet.getTransactionFinalMapSigns();
            for (byte[] hash : hashes) {
                mapSigns.put(hash, dbRef);
            }
        }

    }

    public void orphan(Block block, int forDeal) {

        //UPDATE SENDER
        super.orphan(block, forDeal);

        HashesSignsMap map = dcSet.getHashesSignsMap();
        for (byte[] hash : hashes) {
            map.removeItem(hash);
        }

        if (!Controller.getInstance().onlyProtocolIndexing) {
            TransactionFinalMapSigns mapSigns = dcSet.getTransactionFinalMapSigns();
            for (byte[] hash : hashes) {
                mapSigns.delete(hash);
            }
        }

    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        return new HashSet<>();
    }

    @Override
    public boolean isInvolved(Account account) {

        if (account.equals(this.creator)) {
            return true;
        }

        return false;
    }

}
