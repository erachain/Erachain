package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.utils.NumberAsString;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class GenesisTransferAssetTransaction extends GenesisRecord {

    private static final byte TYPE_ID = (byte) Transaction.GENESIS_SEND_ASSET_TRANSACTION;
    private static final String NAME_ID = "GENESIS Send Asset";
    private static final int RECIPIENT_LENGTH = TransactionAmount.RECIPIENT_LENGTH;
    private static final int OWNER_LENGTH = RECIPIENT_LENGTH;
    private static final int AMOUNT_LENGTH = TransactionAmount.AMOUNT_LENGTH;

    private static final int BASE_LENGTH = GenesisRecord.BASE_LENGTH + RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH;

    private Account creator;
    private Account recipient;
    private BigDecimal amount;
    private long key;

    public GenesisTransferAssetTransaction(Account recipient, long key, BigDecimal amount) {
        super(TYPE_ID, NAME_ID);
        this.recipient = recipient;
        int different_scale = amount.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
        if (different_scale != 0) {
            amount = amount.scaleByPowerOfTen(different_scale);
        }
        this.amount = amount;
        this.key = key;
        if (key >= 0)
            this.generateSignature();
    }

    // RENT
    public GenesisTransferAssetTransaction(Account recipient, long key, BigDecimal amount, Account creator) {
        this(recipient, key, amount);
        this.creator = creator;
        this.generateSignature();
    }

    //GETTERS/SETTERS
    //public static String getName() { return NAME; }

    public static Transaction Parse(byte[] data) throws Exception {

        //CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < BASE_LENGTH) {
            throw new Exception("Data does not match block length: " + data.length + " in " + NAME_ID);
        }

        // READ TYPE
        //byte[] typeBytes = Arrays.copyOfRange(data, 0, SIMPLE_TYPE_LENGTH);
        int position = SIMPLE_TYPE_LENGTH;

        //READ RECIPIENT
        byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
        Account recipient = new Account(recipientBytes);
        position += RECIPIENT_LENGTH;

        //READ KEY
        byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
        long key = Longs.fromByteArray(keyBytes);
        position += KEY_LENGTH;

        //READ AMOUNT
        byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
        BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
        position += AMOUNT_LENGTH;

        if (key < 0) {
            //READ OWNER
            byte[] makerBytes = Arrays.copyOfRange(data, position, position + OWNER_LENGTH);
            Account maker = new Account(makerBytes);
            position += OWNER_LENGTH;
            return new GenesisTransferAssetTransaction(recipient, key, amount, maker);
        } else {
            return new GenesisTransferAssetTransaction(recipient, key, amount);
        }

    }

    public Account getSender() {
        return this.creator;
    }

    public Account getRecipient() {
        return this.recipient;
    }

    public String viewActionType() {
        return TransactionAmount.viewTypeName(this.amount, false);
    }

    @Override
    public String viewSubTypeName() {
        return TransactionAmount.viewSubTypeName(this.key, this.amount, false, false);
    }

    @Override
    public BigDecimal getAmount() {
        return this.amount;
    }

    @Override
    public long getKey() {
        return this.key;
    }

    @Override
    public long getAssetKey() {
        return this.key;
    }

    public void setDC(DCSet dcSet, int forDeal, int blockHeight, int seqNo, boolean andUpdateFromState) {
        super.setDC(dcSet, forDeal, blockHeight, seqNo, false);

        if (this.amount != null) {
            long assetKey = this.getAbsKey();
            AssetCls asset = (AssetCls) this.dcSet.getItemAssetMap().get(assetKey);
            if (asset == null || assetKey > BlockChain.AMOUNT_SCALE_FROM) {
                int different_scale = BlockChain.AMOUNT_DEDAULT_SCALE - asset.getScale();
                if (different_scale != 0) {
                    // RESCALE AMOUNT
                    this.amount = this.amount.scaleByPowerOfTen(different_scale);
                }
            }
        }

        if (false && andUpdateFromState && !isWiped())
            updateFromStateDB();
    }

    @Override
    public BigDecimal getAmount(String address) {
        BigDecimal amount = BigDecimal.ZERO;

        if (address.equals(this.recipient.getAddress())) {
            //IF RECIPIENT
            amount = amount.add(this.amount);
        }

        return amount;
    }

    @Override
    public BigDecimal getAmount(Account account) {
        String address = account.getAddress();
        return getAmount(address);
    }

    @Override
    public String viewAmount() {
        return NumberAsString.formatAsString(this.amount);
    }

    @Override
    public String viewAmount(Account account) {
        String address = account.getAddress();
        return NumberAsString.formatAsString(getAmount(address));
    }

    @Override
    public String viewAmount(String address) {
        return NumberAsString.formatAsString(getAmount(address));
    }

    @Override
    public String viewRecipient() {
        return recipient.getPersonAsString();
    }

    //PARSE/CONVERT

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = super.toJson();

        //ADD CREATOR/RECIPIENT/AMOUNT/ASSET
        if (this.creator != null)
            transaction.put("creator", this.creator.getAddress());

        transaction.put("recipient", this.recipient.getAddress());
        transaction.put("asset", this.key);
        transaction.put("amount", this.amount.toPlainString());

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        //WRITE TYPE
        byte[] data = super.toBytes(forDeal, withSignature);

        //WRITE RECIPIENT
        data = Bytes.concat(data, this.recipient.getAddressBytes());

        //WRITE KEY
        byte[] keyBytes = Longs.toByteArray(this.key);
        keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
        data = Bytes.concat(data, keyBytes);

        //WRITE AMOUNT
        byte[] amountBytes = this.amount.unscaledValue().toByteArray();
        byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
        amountBytes = Bytes.concat(fill, amountBytes);
        data = Bytes.concat(data, amountBytes);

        if (key < 0) {
            //WRITE OWNER
            data = Bytes.concat(data, this.creator.getAddressBytes());
        }

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {

        return BASE_LENGTH + (this.key < 0 ? OWNER_LENGTH : 0);
    }


    //VALIDATE

    @Override
    public int isValid(int forDeal, long flags) {

        //CHECK IF RECIPIENT IS VALID ADDRESS
        if (!"1A3P7u56G4NgYfsWMms1BuctZfnCeqrYk3".equals(this.recipient.getAddress())) {
            Tuple2<Account, String> result = Account.tryMakeAccount(this.recipient.getAddress());
            if (result.a == null) {
                return INVALID_ADDRESS;
            }
        }

        // CHECK IF AMOUNT wrong SCALE
        if (this.getAmount().scale() != GenesisBlock.makeAsset((int) this.getAbsKey()).getScale()) {
            return AMOUNT_SCALE_WRONG;
        }

        //CHECK IF AMOUNT IS POSITIVE
        if (this.amount.compareTo(BigDecimal.ZERO) <= 0) {
            return NEGATIVE_AMOUNT;
        }

        return VALIDATE_OK;
    }

    //PROCESS/ORPHAN

    @Override
    public void process(Block block, int forDeal) {

        long key = this.key;

        if (recipient.equals("76ACGgH8c63VrrgEw1wQA4Dno1JuPLTsWe")) {
            boolean test = true;
        }

        //UPDATE RECIPIENT OWN or RENT
        this.recipient.changeBalance(this.dcSet, false, false, key, this.amount,
                false, false, false);

        //UPDATE REFERENCE OF RECIPIENT
        this.recipient.setLastTimestamp(new long[]{this.timestamp, dbRef}, this.dcSet);

        if (this.getAbsKey() == Transaction.RIGHTS_KEY) {
            // PROCESS FORGING DATA
            //// SKIP Genesis Block
            ///this.recipient.setForgingData(this.dcSet, 1, this.amount.intValue());
            int currentForgingBalance = this.recipient.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();
            this.recipient.setForgingData(this.dcSet, 1, currentForgingBalance);
        }

        if (key < 0) {
            // THIS is CREDIT
            //this.maker.setBalance(key, this.owner.getBalance(db, key).subtract(this.amount), db);
            this.creator.changeBalance(this.dcSet, true, false, key, this.amount,
                    false, false, false);
            this.dcSet.getCredit_AddressesMap().add(
                    new Tuple3<String, Long, String>(
                            this.creator.getAddress(), -key,
                            this.recipient.getAddress()),
                    //new Tuple3<String, Long, String>(this.owner.getAddress(), -key, this.recipient.getAddress()),
                    this.amount);

        } else {
            // CREATOR update
            if (key == FEE_KEY) {
                BlockChain.FEE_ASSET_EMITTER.changeBalance(this.dcSet, true, false, key, this.amount,
                        false, false, false);

                if (BlockChain.CLONE_MODE) {
                    BigDecimal sideRoyalty = amount.multiply(new BigDecimal("0.05")); // 5%
                    BlockChain.CLONE_ROYALTY_ERACHAIN_ACCOUNT.changeBalance(dcSet, false, false, Transaction.FEE_KEY,
                            sideRoyalty, false, false, false);
                    BlockChain.FEE_ASSET_EMITTER.changeBalance(this.dcSet, true, false, Transaction.FEE_KEY,
                            sideRoyalty, false, false, false);
                }

            } else {
                GenesisBlock.CREATOR.changeBalance(this.dcSet, true, false, key, this.amount,
                        false, false, false);
            }

        }
    }

    @Override
    public void orphan(Block block, int forDeal) {
        // RISE ERROR
        DCSet err = null;
        err.hashCode();

		/* IT CANNOT BE orphanED !!!
		 *
		long key = this.key;
		//UPDATE RECIPIENT
		//this.recipient.setBalance(key, this.recipient.getBalance(db, key).subtract(this.amount), db);
		this.recipient.changeBalance(db, true, key, this.amount);

		//UPDATE REFERENCE OF RECIPIENT
		this.recipient.removeReference(db);

		if (this.getAbsKey() == Transaction.RIGHTS_KEY) {
			// ORPHAN FORGING DATA
			////this.recipient.setLastForgingData(db, -1);
			//this.recipient.delForgingData(db, 2);
		}

		if (key < 0) {
			//this.owner.setBalance(key, this.owner.getBalance(db, key).add(this.amount), db);
			this.owner.changeBalance(db, false, key, this.amount);
			db.getCredit_AddressesMap().sub(
					new Tuple3<String, Long, String>(this.owner.getAddress(), -key, this.recipient.getAddress()),
					this.amount);
		} else {
			// CREATOR update
			GenesisBlock.CREATOR.changeBalance(db, false, key, this.amount);
		}
		 */

    }

    //REST

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>(3, 1);
        if (this.creator != null)
            accounts.add(this.creator);

        accounts.addAll(this.getRecipientAccounts());
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.recipient);
        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {

        if (account.equals(creator)
                || account.equals(recipient)) {
            return true;
        }

        return false;
    }

    //@Override
    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

        assetAmount = addAssetAmount(assetAmount, this.recipient.getAddress(), this.key, this.amount);

        return assetAmount;
    }

}
