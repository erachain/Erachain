package org.erachain.core.transaction;


import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.eclipse.jetty.util.StringUtil;
import org.erachain.at.AT;
import org.erachain.at.ATConstants;
import org.erachain.at.ATController;
import org.erachain.at.ATException;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @deprecated
 */
public class DeployATTransaction extends Transaction {

    private static final byte TYPE_ID = (byte) Transaction.DEPLOY_AT_TRANSACTION;
    private static final String NAME_ID = "Deploy AT";
    private static final int AMOUNT_LENGTH = TransactionAmount.AMOUNT_LENGTH;

    private static final int NAME_SIZE_LENGTH = 4;
    private static final int DESCRIPTION_SIZE_LENGTH = 4;
    private static final int TYPE_SIZE_LENGTH = 4;
    private static final int TAGS_SIZE_LENGTH = 4;
    private static final int CREATION_BYTES_SIZE_LENGTH = 4;
    //private static final int AMOUNT_LENGTH = 8;
    private static final int BASE_LENGTH = Transaction.BASE_LENGTH + NAME_SIZE_LENGTH + DESCRIPTION_SIZE_LENGTH + TYPE_SIZE_LENGTH + TAGS_SIZE_LENGTH + CREATION_BYTES_SIZE_LENGTH + AMOUNT_LENGTH;


    private String name;
    private String description;
    private String type;
    private String tags;
    private BigDecimal amount;
    private byte[] creationBytes;

    public DeployATTransaction(byte[] typeBytes, PublicKeyAccount creator, String name, String description, String type, String tags, byte[] creationBytes, BigDecimal quantity, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, null, (byte) 0, timestamp, reference);

        this.name = name;
        this.description = description;
        this.creationBytes = creationBytes;
        this.type = type;
        this.tags = tags;
        this.amount = quantity;
    }

    public DeployATTransaction(byte[] typeBytes, PublicKeyAccount creator, String name, String description, String type, String tags, byte[] creationBytes, BigDecimal quantity, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, name, description, type, tags, creationBytes, quantity, timestamp, reference);
        this.signature = signature;
        this.feePow = feePow;
        //this.calcFee();
    }

    public DeployATTransaction(byte[] typeBytes, PublicKeyAccount creator, String name, String description, String type, String tags, byte[] creationBytes, BigDecimal quantity, byte feePow, long timestamp, Long reference) {
        this(typeBytes, creator, name, description, type, tags, creationBytes, quantity, timestamp, reference);
        this.feePow = feePow;
    }

    public DeployATTransaction(PublicKeyAccount creator, String name, String description, String type, String tags, byte[] creationBytes, BigDecimal quantity, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, name, description, type, tags, creationBytes, quantity, timestamp, reference);
    }

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
        long reference = Longs.fromByteArray(referenceBytes);
        position += REFERENCE_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        //READ NAME
        byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
        int nameLength = Ints.fromByteArray(nameLengthBytes);
        position += NAME_SIZE_LENGTH;

        if (nameLength < 1 || nameLength > ATConstants.NAME_MAX_LENGTH) {
            throw new Exception("Invalid name length");
        }

        byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
        String name = new String(nameBytes, StandardCharsets.UTF_8);
        position += nameLength;

        //READ DESCRIPTION
        byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
        int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
        position += DESCRIPTION_SIZE_LENGTH;

        if (descriptionLength < 1 || descriptionLength > ATConstants.DESC_MAX_LENGTH) {
            throw new Exception("Invalid description length");
        }

        byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
        String description = new String(descriptionBytes, StandardCharsets.UTF_8);
        position += descriptionLength;

        //READ TYPE
        byte[] typeLengthBytes = Arrays.copyOfRange(data, position, position + TYPE_SIZE_LENGTH);
        int typeLength = Ints.fromByteArray(typeLengthBytes);
        position += TYPE_SIZE_LENGTH;

        if (typeLength < 1 || typeLength > ATConstants.TYPE_MAX_LENGTH) {
            throw new Exception("Invalid type length");
        }

        byte[] typeStrBytes = Arrays.copyOfRange(data, position, position + typeLength);
        String type = new String(typeStrBytes, StandardCharsets.UTF_8);
        position += typeLength;

        //READ TAGS
        byte[] tagsLengthBytes = Arrays.copyOfRange(data, position, position + TAGS_SIZE_LENGTH);
        int tagsLength = Ints.fromByteArray(tagsLengthBytes);
        position += TAGS_SIZE_LENGTH;

        if (tagsLength < 1 || tagsLength > ATConstants.TAGS_MAX_LENGTH) {
            throw new Exception("Invalid tags length");
        }

        byte[] tagsBytes = Arrays.copyOfRange(data, position, position + tagsLength);
        String tags = new String(tagsBytes, StandardCharsets.UTF_8);
        position += tagsLength;

        //READ CREATIONBYTES
        byte[] creationLengthBytes = Arrays.copyOfRange(data, position, position + CREATION_BYTES_SIZE_LENGTH);
        int creationLength = Ints.fromByteArray(creationLengthBytes);
        position += CREATION_BYTES_SIZE_LENGTH;

        if (creationLength < 1 || creationLength > ATConstants.CREATION_BYTES_MAX_LENGTH) //TODO SEE WHAT IS BEST
        {
            throw new Exception("Invalid creation bytes length");
        }

        byte[] creationBytes = Arrays.copyOfRange(data, position, position + creationLength);
        position += creationLength;

        //READ AMOUNT
        byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
        BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
        position += AMOUNT_LENGTH;

        //READ FEE POWER
        byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
        byte feePow = feePowBytes[0];
        position += 1;

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);

        return new DeployATTransaction(typeBytes, creator, name, description, type, tags, creationBytes, amount, feePow, timestamp, reference, signatureBytes);
    }

    //PARSE/CONVERT
    //public static String getName() { return "OLD: Deploy AT"; }
    @Override
    public boolean hasPublicText() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        JSONObject transaction = this.getJsonBase();

        transaction.put("creator", this.creator.getAddress());
        transaction.put("name", this.name);
        transaction.put("description", this.description);
        transaction.put("atType", this.type);
        transaction.put("tags", this.tags);
        transaction.put("creationBytes", Base58.encode(this.creationBytes)); //Converter.toHex(this.creationBytes));
        transaction.put("amount", this.amount.toPlainString());

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = new byte[0];

        //WRITE TYPE
        data = Bytes.concat(data, this.typeBytes);

        //WRITE TIMESTAMP
        byte[] timestampBytes = Longs.toByteArray(this.timestamp);
        timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
        data = Bytes.concat(data, timestampBytes);

        //WRITE REFERENCE
        byte[] referenceBytes = Longs.toByteArray(this.reference);
        referenceBytes = Bytes.ensureCapacity(referenceBytes, REFERENCE_LENGTH, 0);
        data = Bytes.concat(data, referenceBytes);

        //WRITE CREATOR
        data = Bytes.concat(data, this.creator.getPublicKey());

        //WRITE NAME SIZE
        byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
        int nameLength = nameBytes.length;
        byte[] nameLengthBytes = Ints.toByteArray(nameLength);
        data = Bytes.concat(data, nameLengthBytes);

        //WRITE NAME
        data = Bytes.concat(data, nameBytes);

        //WRITE DESCRIPTION SIZE
        byte[] descriptionBytes = this.description.getBytes(StandardCharsets.UTF_8);
        int descriptionLength = descriptionBytes.length;
        byte[] descriptionLengthBytes = Ints.toByteArray(descriptionLength);
        data = Bytes.concat(data, descriptionLengthBytes);

        //WRITE DESCRIPTION
        data = Bytes.concat(data, descriptionBytes);

        //WRITE TYPE SIZE
        byte[] typeATBytes = this.type.getBytes(StandardCharsets.UTF_8);
        int typeLength = typeATBytes.length;
        byte[] typeLengthBytes = Ints.toByteArray(typeLength);
        data = Bytes.concat(data, typeLengthBytes);

        //WRITE TYPE
        data = Bytes.concat(data, typeATBytes);

        //WRITE TAGS SIZE
        byte[] tagsBytes = this.tags.getBytes(StandardCharsets.UTF_8);
        int tagsLength = tagsBytes.length;
        byte[] tagsLengthBytes = Ints.toByteArray(tagsLength);
        data = Bytes.concat(data, tagsLengthBytes);

        //WRITE TAGS
        data = Bytes.concat(data, tagsBytes);

        //WRITE CREATIONBYTES SIZE
        int creationBytesLength = this.creationBytes.length;
        byte[] creationLengthBytes = Ints.toByteArray(creationBytesLength);
        data = Bytes.concat(data, creationLengthBytes);

        //WRITE DESCRIPTION
        data = Bytes.concat(data, this.creationBytes);

        //WRITE AMOUNT
        byte[] amountBytes = this.amount.unscaledValue().toByteArray();
        byte[] fillAmount = new byte[AMOUNT_LENGTH - amountBytes.length];
        amountBytes = Bytes.concat(fillAmount, amountBytes);
        data = Bytes.concat(data, amountBytes);

        //WRITE FEE POWER
        byte[] feePowBytes = new byte[1];
        feePowBytes[0] = this.feePow;
        data = Bytes.concat(data, feePowBytes);

        //SIGNATURE
        if (withSignature)
            data = Bytes.concat(data, this.signature);

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        return BASE_LENGTH +
                this.name.getBytes(StandardCharsets.UTF_8).length +
                this.description.getBytes(StandardCharsets.UTF_8).length +
                this.type.getBytes(StandardCharsets.UTF_8).length +
                this.tags.getBytes(StandardCharsets.UTF_8).length +
                this.creationBytes.length;
    }

    //VALIDATE

    @Override
    public int isValid(int forDeal, long flags) {
        return isValid(0);
    }

    //
    public int isValid(Integer forkHeight) {
		/*
		//CHECK IF RELEASED
		if(db.getBlocksHeadMap().getLastBlock().getHeight(db) + 1 < Transaction.getAT_BLOCK_HEIGHT_RELEASE())
		{
			return NOT_YET_RELEASED;
		}
		 */

        //CHECK NAME LENGTH
        int nameLength = this.name.getBytes(StandardCharsets.UTF_8).length;
        if (nameLength > ATConstants.NAME_MAX_LENGTH || nameLength < 1) {
            return INVALID_NAME_LENGTH_MAX;
        }

        //CHECK DESCRIPTION LENGTH
        int descriptionLength = this.description.getBytes(StandardCharsets.UTF_8).length;
        if (descriptionLength > ATConstants.DESC_MAX_LENGTH || descriptionLength < 1) {
            return INVALID_DESCRIPTION_LENGTH_MAX;
        }

        int typeLength = this.type.getBytes(StandardCharsets.UTF_8).length;
        if (typeLength > ATConstants.TYPE_MAX_LENGTH || typeLength < 1) {
            return INVALID_TYPE_LENGTH;
        }

        int tagsLength = this.tags.getBytes(StandardCharsets.UTF_8).length;
        if (tagsLength > ATConstants.TYPE_MAX_LENGTH || tagsLength < 1) {
            return INVALID_TAGS_LENGTH;
        }

        //CHECK IF AMOUNT IS POSITIVE
        if (this.amount.compareTo(BigDecimal.ZERO) <= 0) {
            return NEGATIVE_AMOUNT;
        }

        //CHECK IF CREATIONBYTES VALID
        try {
            int height = this.dcSet.getBlockMap().last().getHeight() + 1;
            byte[] balanceBytes = this.getFee().unscaledValue().toByteArray();
            byte[] fill = new byte[8 - balanceBytes.length];
            balanceBytes = Bytes.concat(fill, balanceBytes);

            long lFee = Longs.fromByteArray(balanceBytes);
            int returnCode = ATController.checkCreationBytes(this.creationBytes, this.type, lFee, height, forkHeight, this.dcSet);
            if (returnCode != 0) {
                return returnCode + AT_ERROR;
            }

            String atId = Crypto.getInstance().getATAddress(getBytesForAddress(this.dcSet));
            if (this.dcSet.getATMap().getAT(atId) != null) {
                return 12 + AT_ERROR;
            }

        } catch (ATException e) {
            //TODO CAN BE CHANGED TO HANDLE THE ERRORS BETTER
            return INVALID_CREATION_BYTES;
        }

        return super.isValid(Transaction.FOR_NETWORK, 0l);

    }

    //PROCESS/ORPHAN

    //@Override
    @Override
    public void process(Block block, int forDeal) {
        //UPDATE ISSUER
        super.process(block, forDeal);
        //this.creator.setBalance(Transaction.FEE_KEY, this.creator.getBalance(db, Transaction.FEE_KEY).subtract(this.amount), db);
        this.creator.changeBalance(this.dcSet, true, false, Transaction.FEE_KEY, this.amount,
                false, false, false, false);

        //CREATE AT ID = ADDRESS
        String atId = Crypto.getInstance().getATAddress(getBytesForAddress(this.dcSet));

        Account atAccount = new Account(atId);

        //atAccount.setBalance(Transaction.FEE_KEY, this.amount , db );
        atAccount.changeBalance(this.dcSet, false, false, Transaction.FEE_KEY, this.amount,
                false, false, false, false);

        //UPDATE REFERENCE OF RECIPIENT
        if (true || atAccount.getLastTimestamp(this.dcSet) == null) {
            atAccount.setLastTimestamp(new long[]{this.timestamp, dbRef}, this.dcSet);
        }

        //CREATE AT - public key or address? Is that the correct height?
        AT at = new AT(Base58.decode(atId), Base58.decode(this.creator.getAddress()), this.name, this.description, this.type, this.tags, this.creationBytes, this.dcSet.getBlockMap().last().getHeight() + 1);

        //INSERT INTO DATABASE
        this.dcSet.getATMap().add(at);
        this.dcSet.getATStateMap().addOrUpdate(at.getCreationBlockHeight(), at.getId(), at.getState());

    }

    public byte[] getBytesForAddress(DCSet db) {
        byte[] name = StringUtil.getUtf8Bytes(this.name);
        byte[] desc = StringUtil.getUtf8Bytes(this.description.replaceAll("\\s", ""));
        ByteBuffer bf = ByteBuffer.allocate(name.length + desc.length + this.creator.getPublicKey().length + this.creationBytes.length + 4);
        bf.order(ByteOrder.LITTLE_ENDIAN);

        bf.put(name);
        bf.put(desc);
        bf.put(this.creator.getPublicKey());
        bf.put(this.creationBytes);
        bf.putInt(db.getBlockMap().last().getHeight() + 1);
        return bf.array().clone();
    }

    public Account getATaccount(DCSet db) {
        byte[] name = StringUtil.getUtf8Bytes(this.name);
        byte[] desc = StringUtil.getUtf8Bytes(this.description.replaceAll("\\s", ""));
        ByteBuffer bf = ByteBuffer.allocate(name.length + desc.length + this.creator.getPublicKey().length + this.creationBytes.length + 4);
        bf.order(ByteOrder.LITTLE_ENDIAN);

        bf.put(name);
        bf.put(desc);
        bf.put(this.creator.getPublicKey());
        bf.put(this.creationBytes);

        bf.putInt(getBlockHeightByParentOrLast(db));

        String atId = Crypto.getInstance().getATAddress(bf.array().clone());

        return new Account(atId);
    }

    //@Override
    @Override
    public void orphan(Block block, int forDeal) {

        //UPDATE ISSUER
        super.orphan(block, forDeal);
        //this.creator.setBalance(Transaction.FEE_KEY, this.creator.getBalance(db, Transaction.FEE_KEY).add(this.amount), db);
        this.creator.changeBalance(this.dcSet, false, false, Transaction.FEE_KEY, this.amount,
                false, false, false, false);

        String atId = Crypto.getInstance().getATAddress(getBytesForAddress(this.dcSet));

        Account atAccount = new Account(atId);

        //UPDATE RECIPIENT
        //atAccount.setBalance(Transaction.FEE_KEY, atAccount.getBalance(db, Transaction.FEE_KEY).subtract(this.amount), db);
        atAccount.changeBalance(this.dcSet, true, false, Transaction.FEE_KEY, this.amount,
                false, false, false, false);

        //UPDATE REFERENCE OF SENDER
        this.creator.setLastTimestamp(new long[]{this.reference, dbRef}, this.dcSet);

    }

    //REST

    @Override
    public BigDecimal getAmount() {
        return this.amount;
    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<>();
        accounts.add(this.creator);
        accounts.addAll(this.getRecipientAccounts());
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<>();
        accounts.add(this.getATaccount(dcSet));
        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {

        if (account.equals(this.creator)) {
            return true;
        }

        if (account.equals(this.getATaccount(dcSet))) {
            return true;
        }

        return false;
    }

    //@Override
    @Override
    public BigDecimal getAmount(Account account) {
        if (account.getAddress().equals(this.creator.getAddress())) {
            return BigDecimal.ZERO.subtract(this.amount.add(this.fee));
        }

        return BigDecimal.ZERO;
    }

    //@Override
    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

        assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);

        assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.amount);
        assetAmount = addAssetAmount(assetAmount, this.getATaccount(dcSet).getAddress(), FEE_KEY, this.amount);

        return assetAmount;
    }

}
