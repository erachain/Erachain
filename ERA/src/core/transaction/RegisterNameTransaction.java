package core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import core.BlockChain;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Crypto;
import core.naming.Name;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

//import java.math.BigInteger;

/**
 * @deprecated
 */
public class RegisterNameTransaction extends Transaction {
    private static final byte TYPE_ID = (byte) REGISTER_NAME_TRANSACTION;
    private static final String NAME_ID = "OLD: Register Name";
    private static final int BASE_LENGTH = TransactionAmount.BASE_LENGTH;

    private PublicKeyAccount creator;
    private Name name;

    public RegisterNameTransaction(byte[] typeBytes, PublicKeyAccount creator, Name name, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

        this.creator = creator;
        this.name = name;
    }

    public RegisterNameTransaction(byte[] typeBytes, PublicKeyAccount creator, Name name, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, name, feePow, timestamp, reference);
        this.signature = signature;
        //this.calcFee();
    }

    public RegisterNameTransaction(PublicKeyAccount creator, Name name, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, name, feePow, timestamp, reference, signature);
    }

    public RegisterNameTransaction(PublicKeyAccount creator, Name name, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, name, feePow, timestamp, reference);
    }

    //GETTERS/SETTERS

    // public static String getName() { return "OLD: Gegister Name"; }

    public static Transaction Parse(byte[] data) throws Exception {
        //CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < BASE_LENGTH) {
            throw new Exception("Data does not match block length");
        }


        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ TIMESTAMP
        byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
        long timestamp = Longs.fromByteArray(timestampBytes);
        position += TIMESTAMP_LENGTH;

        //READ REFERENCE
        byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
        Long reference = Longs.fromByteArray(referenceBytes);
        position += REFERENCE_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        //READ NAME
        Name name = Name.Parse(Arrays.copyOfRange(data, position, data.length));
        position += name.getDataLength();

        //READ FEE POWER
        byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
        byte feePow = feePowBytes[0];
        position += 1;

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);

        return new RegisterNameTransaction(typeBytes, creator, name, feePow, timestamp, reference, signatureBytes);
    }

    public Name getName() {
        return this.name;
    }

    //PARSE CONVERT

    @Override
    public boolean hasPublicText() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        //ADD CREATOR/NAME/VALUE
        transaction.put("creator", this.creator.getAddress());
        //transaction.put("owner", this.name.getOwner().getAddress());
        transaction.put("name", this.name.getName());
        transaction.put("value", this.name.getValue());

        return transaction;
    }

    @Override
    public byte[] toBytes(boolean withSign, Long releaserReference) {
        byte[] data = new byte[0];

        //WRITE TYPE
        data = Bytes.concat(data, this.typeBytes);

        //WRITE TIMESTAMP
        byte[] timestampBytes = Longs.toByteArray(this.timestamp);
        timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
        data = Bytes.concat(data, timestampBytes);

        //WRITE REFERENCE - in any case as Pack or not
        if (this.reference != null) {
            byte[] referenceBytes = Longs.toByteArray(this.reference);
            referenceBytes = Bytes.ensureCapacity(referenceBytes, REFERENCE_LENGTH, 0);
            data = Bytes.concat(data, referenceBytes);
        }

        //WRITE CREATOR
        data = Bytes.concat(data, this.creator.getPublicKey());

        //WRITE NAME
        data = Bytes.concat(data, this.name.toBytes());

        //WRITE FEE POWER
        byte[] feePowBytes = new byte[1];
        feePowBytes[0] = this.feePow;
        data = Bytes.concat(data, feePowBytes);

        //SIGNATURE
        if (withSign) data = Bytes.concat(data, this.signature);

        return data;
    }

    @Override
    public int getDataLength(boolean asPack) {
        return BASE_LENGTH + this.name.getDataLength();
    }

    //VALIDATE

    //@Override
    @Override
    public int isValid(Long releaserReference, long flags) {
        //CHECK NAME LENGTH
        int nameLength = this.name.getName().getBytes(StandardCharsets.UTF_8).length;
        if (nameLength > 400 || nameLength < 1) {
            return INVALID_NAME_LENGTH;
        }

        //CHECK IF LOWERCASE
        if (!this.name.getName().equals(this.name.getName().toLowerCase())) {
            return NAME_NOT_LOWER_CASE;
        }

        //CHECK VALUE LENGTH
        int valueLength = this.name.getValue().getBytes(StandardCharsets.UTF_8).length;
        if (valueLength > BlockChain.MAX_REC_DATA_BYTES || valueLength < 1) {
            return INVALID_VALUE_LENGTH;
        }

        //CHECK OWNER
        if (!Crypto.getInstance().isValidAddress(this.name.getOwner().getAddress())) {
            return INVALID_ADDRESS;
        }

        //CHECK NAME NOT REGISTRED ALREADY
        if (this.dcSet.getNameMap().contains(this.name)) {
            return NAME_ALREADY_REGISTRED;
        }


        return super.isValid(releaserReference, flags);

    }

    //PROCESS/ORPHAN

    //@Override
    @Override
    public void process(Block block, boolean asPack) {
        //UPDATE OWNER
        super.process(block, asPack);

        //UPDATE REFERENCE OF OWNER
        //this.creator.setLastReference(this.timestamp, db);

        //INSERT INTO DATABASE
        this.dcSet.getNameMap().add(this.name);
    }


    //@Override
    @Override
    public void orphan(boolean asPack) {
        //UPDATE OWNER
        super.orphan(asPack);

        //UPDATE REFERENCE OF OWNER
        //this.creator.setLastReference(this.reference, db);

        //INSERT INTO DATABASE
        this.dcSet.getNameMap().delete(this.name);
    }


    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.creator);
        accounts.add(this.name.getOwner());
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        return new HashSet<Account>();
    }

    @Override
    public boolean isInvolved(Account account) {
        String address = account.getAddress();

        if (address.equals(this.creator.getAddress()) || address.equals(this.name.getOwner().getAddress())) {
            return true;
        }

        return false;
    }


    //@Override
    @Override
    public BigDecimal getAmount(Account account) {
        if (account.getAddress().equals(this.creator.getAddress())) {
            return BigDecimal.ZERO.subtract(this.fee);
        }

        return BigDecimal.ZERO;
    }

    //@Override
    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

        assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);

        return assetAmount;
    }

    @Override
    public int calcBaseFee() {
        return calcCommonFee();
    }
}
