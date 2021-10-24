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

    private Account sender;
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
    public GenesisTransferAssetTransaction(Account recipient, long key, BigDecimal amount, Account sender) {
        this(recipient, key, amount);
        this.sender = sender;
        this.generateSignature();
    }

    //GETTERS/SETTERS

    public static Transaction Parse(byte[] data) throws Exception {

        //CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < BASE_LENGTH) {
            throw new Exception("Data does not match block length: " + data.length + " in " + NAME_ID);
        }

        // READ TYPE
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
        return this.sender;
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

        if (this.amount != null && this.amount.signum() != 0) {
            long assetKey = this.getAbsKey();
            AssetCls asset = this.dcSet.getItemAssetMap().get(assetKey);
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
    public BigDecimal getAmount(Account account) {
        if (recipient.equals(account)) {
            return amount;
        }

        return BigDecimal.ZERO;
    }

    @Override
    public String viewAmount() {
        return NumberAsString.formatAsString(this.amount);
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
        if (this.sender != null)
            sender.toJsonPersonInfo(transaction, "sender");

        recipient.toJsonPersonInfo(transaction, "recipient");
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
            data = Bytes.concat(data, this.sender.getAddressBytes());
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
    public void processBody(Block block, int forDeal) {

        long key = this.key;

        if (recipient.equals("76ACGgH8c63VrrgEw1wQA4Dno1JuPLTsWe")) {
            boolean test = true;
        }

        //UPDATE RECIPIENT OWN or RENT
        this.recipient.changeBalance(this.dcSet, false, false, key, this.amount,
                false, false, false);

        //UPDATE TIMESTAMP OF RECIPIENT
        this.recipient.setLastTimestamp(new long[]{this.timestamp, dbRef}, this.dcSet);

        if (this.getAbsKey() == Transaction.RIGHTS_KEY) {
            // PROCESS FORGING DATA
            //// SKIP Genesis Block
            int currentForgingBalance = this.recipient.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();
            this.recipient.setForgingData(this.dcSet, 1, currentForgingBalance);
        }

        if (key < 0) {
            // THIS is CREDIT
            this.sender.changeBalance(this.dcSet, true, false, key, this.amount,
                    false, false, false);
            this.dcSet.getCredit_AddressesMap().add(
                    new Tuple3<String, Long, String>(
                            this.sender.getAddress(), -key,
                            this.recipient.getAddress()),
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
    public void orphanBody(Block block, int forDeal) {
        // RISE ERROR
        DCSet err = null;
        err.hashCode();

        /* IT CANNOT BE orphanED !!!
         *
         */

    }

    //REST

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>(3, 1);
        if (this.sender != null)
            accounts.add(this.sender);

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

        if (account.equals(sender)
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
