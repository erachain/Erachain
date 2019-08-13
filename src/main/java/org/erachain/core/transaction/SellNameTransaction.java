package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.naming.NameSale;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
* @deprecated
 */
public class SellNameTransaction extends Transaction {
    private static final byte TYPE_ID = (byte) SELL_NAME_TRANSACTION;
    private static final String NAME_ID = "OLD: Sale Name";
    private static final int BASE_LENGTH = TransactionAmount.BASE_LENGTH;

    private PublicKeyAccount creator;
    private NameSale nameSale;

    public SellNameTransaction(byte[] typeBytes, PublicKeyAccount creator, NameSale nameSale, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

        this.creator = creator;
        this.nameSale = nameSale;
    }

    public SellNameTransaction(byte[] typeBytes, PublicKeyAccount creator, NameSale nameSale, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, nameSale, feePow, timestamp, reference);
        this.signature = signature;
        //this.calcFee();
    }

    public SellNameTransaction(PublicKeyAccount creator, NameSale nameSale, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, nameSale, feePow, timestamp, reference, signature);
    }

    public SellNameTransaction(PublicKeyAccount creator, NameSale nameSale, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, nameSale, feePow, timestamp, reference);
    }

    //GETTERS/SETTERS

    //public static String getName() { return "OLD: Sell Name"; }

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
        byte[] registrantBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(registrantBytes);
        position += CREATOR_LENGTH;

        //READ NAMESALE
        NameSale nameSale = NameSale.Parse(Arrays.copyOfRange(data, position, data.length));
        position += nameSale.getDataLength();

        //READ FEE POWER
        byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
        byte feePow = feePowBytes[0];
        position += 1;

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);

        return new SellNameTransaction(typeBytes, creator, nameSale, feePow, timestamp, reference, signatureBytes);
    }

    public NameSale getNameSale() {
        return this.nameSale;
    }

    //PARSE CONVERT

    @Override
    public boolean hasPublicText() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        //ADD REGISTRANT/NAME/VALUE
        transaction.put("creator", this.creator.getAddress());
        transaction.put("name", this.nameSale.getKey());
        transaction.put("amount", this.nameSale.getAmount().toPlainString());

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
        //WRITE REFERENCE - in any case as Pack or not
        if (this.reference != null) {
            // NULL in imprints
            byte[] referenceBytes = Longs.toByteArray(this.reference);
            referenceBytes = Bytes.ensureCapacity(referenceBytes, REFERENCE_LENGTH, 0);
            data = Bytes.concat(data, referenceBytes);
        }

        //WRITE CREATOR
        data = Bytes.concat(data, this.creator.getPublicKey());

        //WRITE NAMESALE
        data = Bytes.concat(data, this.nameSale.toBytes());

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
        return BASE_LENGTH + this.nameSale.getDataLength();
    }

    //VALIDATE

    //@Override
    @Override
    public int isValid(int asDeal, long flags) {
        //CHECK NAME LENGTH
        int nameLength = this.nameSale.getKey().getBytes(StandardCharsets.UTF_8).length;
        if (nameLength > 400 || nameLength < 1) {
            return INVALID_NAME_LENGTH_MAX;
        }

        //CHECK IF NAME EXISTS
        if (!this.dcSet.getNameMap().contains(this.nameSale.getName(this.dcSet))) {
            return NAME_DOES_NOT_EXIST;
        }

        //CHECK CREATOR
        if (!Crypto.getInstance().isValidAddress(this.nameSale.getName(this.dcSet).getOwner().getAddressBytes())) {
            return INVALID_ADDRESS;
        }

        //CHECK IF CREATOR IS CREATOR
        if (!this.dcSet.getNameMap().get(this.nameSale.getKey()).getOwner().getAddress().equals(this.creator.getAddress())) {
            return INVALID_MAKER_ADDRESS;
        }

        //CHECK IF NOT FOR SALE ALREADY
        if (this.dcSet.getNameExchangeMap().contains(this.nameSale)) {
            return NAME_ALREADY_ON_SALE;
        }

        //CHECK IF AMOUNT POSITIVE
        if (this.nameSale.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return NEGATIVE_AMOUNT;
        }

        //CHECK IF AMOUNT POSSIBLE
        if (this.nameSale.getAmount().compareTo(BigDecimal.valueOf(10000000000l)) > 0) {
            return INVALID_AMOUNT;
        }

        return super.isValid(asDeal, flags);
    }

    //PROCESS/ORPHAN

    //@Override
    @Override
    public void process(Block block, int asDeal) {
        //UPDATE CREATOR
        super.process(block, asDeal);

        //UPDATE REFERENCE OF CREATOR
        //this.creator.setLastReference(this.signature, db);

        //INSERT INTO DATABASE
        this.dcSet.getNameExchangeMap().add(this.nameSale);
    }

    //@Override
    @Override
    public void orphan(Block block, int asDeal) {
        //UPDATE CREATOR
        super.orphan(block, asDeal);

        //UPDATE REFERENCE OF CREATOR
        //this.creator.setLastReference(this.reference, db);

        //DELETE FORM DATABASE
        this.dcSet.getNameExchangeMap().delete(this.nameSale);

    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        return new HashSet<Account>();
    }

    @Override
    public boolean isInvolved(Account account) {
        String address = account.getAddress();

        if (address.equals(this.creator.getAddress())) {
            return true;
        }

        return false;
    }

    //@Override
    @Override
    public BigDecimal getAmount(Account account) {
        String address = account.getAddress();

        if (address.equals(this.creator.getAddress())) {
            return BigDecimal.ZERO.subtract(this.fee);
        }

        return BigDecimal.ZERO;
    }

    //@Override
    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        return subAssetAmount(null, this.creator.getAddress(), FEE_KEY, this.fee);
    }

    @Override
    public long calcBaseFee() {
        return calcCommonFee();
    }
}
