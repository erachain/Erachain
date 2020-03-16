package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.HashesSignsMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.util.List;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import org.slf4j.LoggerFactory;


public class RHashes extends Transaction {

    private static final byte TYPE_ID = (byte) HASHES_RECORD;
    private static final String NAME_ID = "Hashes Record";

    private static final int URL_SIZE_LENGTH = 1;
    public static final int MAX_URL_LENGTH = (int) Math.pow(256, URL_SIZE_LENGTH) - 1;
    private static final int HASH_LENGTH = 32;


    protected static final int LOAD_LENGTH = URL_SIZE_LENGTH + DATA_SIZE_LENGTH;
    protected static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH;

    protected byte[] url; // url
    protected byte[] data;
    protected byte[][] hashes;

    public RHashes(byte[] typeBytes, PublicKeyAccount creator, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference) {

        super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

        this.url = url;
        this.data = data;
        this.hashes = hashes;

    }

    public RHashes(byte[] typeBytes, PublicKeyAccount creator, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, feePow, url, data, hashes, timestamp, reference);
        this.signature = signature;
    }
    public RHashes(byte[] typeBytes, PublicKeyAccount creator, byte feePow, byte[] url, byte[] data, byte[][] hashes,
                   long timestamp, Long reference, byte[] signature, long feeLong) {
        this(typeBytes, creator, feePow, url, data, hashes, timestamp, reference);
        this.signature = signature;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    // asPack
    public RHashes(byte[] typeBytes, PublicKeyAccount creator, byte[] url, byte[] data, byte[][] hashes, Long reference, byte[] signature) {
        this(typeBytes, creator, (byte) 0, url, data, hashes, 0l, reference);
        this.signature = signature;
        // not need this.calcFee();
    }

    public RHashes(PublicKeyAccount creator, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, url, data, hashes, timestamp, reference, signature);
        // set props
        this.setTypeBytes();
    }

    public RHashes(PublicKeyAccount creator, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, url, data, hashes, timestamp, reference);
        // set props
        this.setTypeBytes();
    }

    public static int getHashesLength(byte[] typeBytes) {
        return Ints.fromBytes((byte) 0, (byte) 0, typeBytes[2], typeBytes[3]);
    }

    public static Stack<Tuple3<Long, Integer, Integer>> findRecord(DCSet db, byte[] hash) {
        return db.getHashesSignsMap().get(hash);
    }

    //public static String getName() { return "Statement"; }

    // find twins before insert a record
    public static List<String> findTwins(DCSet db, List<String> hashes58) {
        List<String> twins = new ArrayList<String>();
        for (String hash58 : hashes58) {
            if (db.getHashesSignsMap().contains(Base58.decode(hash58))) {
                twins.add(hash58);
            }
        }
        return twins;
    }

    public static List<String> findTwins(DCSet db, String[] hashes58) {
        List<String> twins = new ArrayList<String>();
        for (String hash58 : hashes58) {
            if (db.getHashesSignsMap().contains(Base58.decode(hash58))) {
                twins.add(hash58);
            }
        }
        return twins;
    }

    // releaserReference = null - not a pack
    // releaserReference = reference for releaser account - it is as pack
    public static Transaction Parse(byte[] data, int asDeal) throws Exception {
        //boolean asPack = releaserReference != null;

        //CHECK IF WE MATCH BLOCK LENGTH
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
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        long feeLong = 0;
        if (asDeal == FOR_DB_RECORD) {
            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

        //////// local parameters

        //READ NAME
        //byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
        //int nameLength = Ints.fromByteArray(nameLengthBytes);
        //position += NAME_SIZE_LENGTH;
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
		/*
		encryptedByte = Arrays.copyOfRange(data, position, position + ENCRYPTED_LENGTH);
		position += ENCRYPTED_LENGTH;

		isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
		position += IS_TEXT_LENGTH;
		*/

        int hashesLen = getHashesLength(typeBytes);
        byte[][] hashes = new byte[hashesLen][];
        for (int i = 0; i < hashesLen; i++) {
            hashes[i] = Arrays.copyOfRange(data, position, position + HASH_LENGTH);
            position += HASH_LENGTH;
        }

        if (asDeal > Transaction.FOR_MYPACK) {
            return new RHashes(typeBytes, creator, feePow, url, arbitraryData, hashes, timestamp, reference,
                    signatureBytes, feeLong);
        } else {
            return new RHashes(typeBytes, creator, url, arbitraryData, hashes, reference, signatureBytes);
        }

    }

    protected void setTypeBytes() {

        byte[] bytesLen = Ints.toByteArray(this.hashes.length);

        this.typeBytes[2] = bytesLen[2];
        this.typeBytes[3] = bytesLen[3];

    }

    public byte[] getURL() {
        return this.url;
    }

    public byte[] getData() {
        return this.data;
    }

    public String[] getHashesB58() {
        String[] strHashes = new String[RHashes.getHashesLength(this.typeBytes)];
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
        
        //if (Arrays.equals(this.encrypted,new byte[0]))
        //	return false;

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
            //transaction.put("url", new String(this.url, StandardCharsets.UTF_8));
            transaction.put("url", new String(this.url, StandardCharsets.UTF_8));
            //transaction.put("data", Base58.encode(this.data));
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
        byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
        data = Bytes.concat(data, dataSizeBytes);

        //WRITE DATA
        data = Bytes.concat(data, this.data);

		/*
		//WRITE ENCRYPTED
		data = Bytes.concat(data, this.encrypted);
		
		//WRITE ISTEXT
		data = Bytes.concat(data, this.isText);
		*/

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

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        int add_len = this.url == null? 0 : this.url.length
                + this.data.length
                + this.hashes.length * HASH_LENGTH;

        return base_len + add_len;
    }

    //@Override
    public int isValid(int asDeal, long flags) {

        //CHECK DATA SIZE
        if (url != null && url.length > MAX_URL_LENGTH) {
            return INVALID_URL_LENGTH;
        }

        if (data.length > 2 * Short.MAX_VALUE - 1) {
            return INVALID_DATA_LENGTH;
        }

        if (hashes.length > 2 * Short.MAX_VALUE - 1) {
            return INVALID_PARAMS_LENGTH;
        }

        int result = super.isValid(asDeal, flags);
        if (result != Transaction.VALIDATE_OK) return result;

        /** double singns is available
         HashesMap map = db.getHashesMap();
         for (byte[] hash: hashes) {
         if (map.contains(hash)) {
         return Transaction.ITEM_DUPLICATE_KEY;
         }
         }
         */


        return Transaction.VALIDATE_OK;

    }

    //PROCESS/ORPHAN

    public void process(Block block, int asDeal) {

        //UPDATE SENDER
        super.process(block, asDeal);

        int height = this.getBlockHeightByParentOrLast(dcSet);

        int transactionIndex = -1;
		/*
		int blockIndex = -1;
		if (block == null) {
			blockIndex = dcSet.getBlocksHeadMap().last().getHeight(dcSet);
		} else {
			blockIndex = block.getHeight(dcSet);
			if (blockIndex < 1 ) {
				// if block not is confirmed - get last block + 1
				blockIndex = dcSet.getBlocksHeadMap().last().getHeight(dcSet) + 1;
			}			
			transactionIndex = block.getTransactionSeq(signature);
		}
		*/

        long personKey;
        Tuple2<Integer, PersonCls> asPerson = this.creator.getPerson(dcSet, height);
        if (asPerson != null && asPerson.a >= 0) {
            personKey = asPerson.b.getKey(dcSet);
        } else {
            personKey = 0l;
        }

        HashesSignsMap map = dcSet.getHashesSignsMap();
        for (byte[] hash : hashes) {
            map.addItem(hash, new Tuple3<Long, Integer, Integer>(personKey, height, transactionIndex));
        }
    }

    public void orphan(Block block, int asDeal) {

        //UPDATE SENDER
        super.orphan(block, asDeal);

        HashesSignsMap map = dcSet.getHashesSignsMap();
        for (byte[] hash : hashes) {
            map.removeItem(hash);
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
        String address = account.getAddress();

        if (address.equals(this.creator.getAddress())) {
            return true;
        }

        return false;
    }

}
