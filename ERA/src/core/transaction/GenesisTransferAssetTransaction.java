package core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import core.BlockChain;
import core.account.Account;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import datachain.DCSet;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import utils.NumberAsString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class GenesisTransferAssetTransaction extends Genesis_Record {

    private static final byte TYPE_ID = (byte) Transaction.GENESIS_SEND_ASSET_TRANSACTION;
    private static final String NAME_ID = "GENESIS Send Asset";
    private static final int RECIPIENT_LENGTH = TransactionAmount.RECIPIENT_LENGTH;
    private static final int OWNER_LENGTH = RECIPIENT_LENGTH;
    private static final int AMOUNT_LENGTH = TransactionAmount.AMOUNT_LENGTH;

    private static final int BASE_LENGTH = Genesis_Record.BASE_LENGTH + RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH;

    private Account owner;
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
    public GenesisTransferAssetTransaction(Account recipient, long key, BigDecimal amount, Account owner) {
        this(recipient, key, amount);
        this.owner = owner;
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
        Account recipient = new Account(Base58.encode(recipientBytes));
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
            byte[] ownerBytes = Arrays.copyOfRange(data, position, position + OWNER_LENGTH);
            Account owner = new Account(Base58.encode(ownerBytes));
            position += OWNER_LENGTH;
            return new GenesisTransferAssetTransaction(recipient, key, amount, owner);
        } else {
            return new GenesisTransferAssetTransaction(recipient, key, amount);
        }

    }

    public Account getOwner() {
        return this.owner;
    }

    public Account getRecipient() {
        return this.recipient;
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

    public void setBlock(Block block, DCSet dcSet, int asDeal, int blockHeight, int seqNo) {
        super.setBlock(block, dcSet, asDeal, blockHeight, seqNo);

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
    }

    public void setDC(DCSet dcSet, int asDeal, int blockHeight, int seqNo) {
        super.setDC(dcSet, asDeal, blockHeight, seqNo);

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
        if (this.owner != null)
            transaction.put("owner", this.owner.getAddress());

        transaction.put("recipient", this.recipient.getAddress());
        transaction.put("asset", this.key);
        transaction.put("amount", this.amount.toPlainString());

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {
        //byte[] data = new byte[0];

        //WRITE TYPE
        byte[] data = new byte[]{TYPE_ID};

        //WRITE RECIPIENT
        data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));

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
            data = Bytes.concat(data, Base58.decode(this.owner.getAddress()));
        }

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {

        return BASE_LENGTH + (this.key < 0 ? OWNER_LENGTH : 0);
    }


    //VALIDATE

    @Override
    public int isValid(int asDeal, long flags) {

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
    public void process(Block block, int asDeal) {

        long key = this.key;

        //UPDATE RECIPIENT OWN or RENT
        this.recipient.changeBalance(this.dcSet, false, key, this.amount, false);

        //UPDATE REFERENCE OF RECIPIENT
        this.recipient.setLastTimestamp(this.timestamp, this.dcSet);

        if (this.getAbsKey() == Transaction.RIGHTS_KEY) {
            // PROCESS FORGING DATA
            //// SKIP Genesis Block
            ///this.recipient.setForgingData(this.dcSet, 1, this.amount.intValue());
            int currentForgingBalance = this.recipient.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();
            this.recipient.setForgingData(this.dcSet, 1, currentForgingBalance);
        }

        if (key < 0) {
            // THIS is CREDIT
            //this.owner.setBalance(key, this.owner.getBalance(db, key).subtract(this.amount), db);
            this.owner.changeBalance(this.dcSet, true, key, this.amount, false);
            this.dcSet.getCredit_AddressesMap().add(
                    new Tuple3<String, Long, String>(
                            this.owner.getAddress(), -key,
                            this.recipient.getAddress()),
                    //new Tuple3<String, Long, String>(this.owner.getAddress(), -key, this.recipient.getAddress()),
                    this.amount);

        } else {
            // CREATOR update
            GenesisBlock.CREATOR.changeBalance(this.dcSet, true, key, this.amount, false);
        }
    }

    @Override
    public void orphan(int asDeal) {
        // RISE ERROR
        DCSet err = null;
        err.commit();

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
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.recipient);
        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {
        String address = account.getAddress();

        if (address.equals(recipient.getAddress())
                || owner != null && address.equals(owner.getAddress())) {
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

    public String viewActionType() {
        int amo_sign = this.amount.compareTo(BigDecimal.ZERO);

        if (this.key > 0) {
            if (amo_sign > 0) {
                return TransactionAmount.NAME_ACTION_TYPE_PROPERTY;
            } else {
                return TransactionAmount.NAME_ACTION_TYPE_HOLD;
            }
        } else {
            if (amo_sign > 0) {
                return TransactionAmount.NAME_CREDIT;
            } else {
                return TransactionAmount.NAME_SPEND;
            }
        }
    }


}
