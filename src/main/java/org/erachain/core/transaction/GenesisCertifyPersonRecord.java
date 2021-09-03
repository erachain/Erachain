package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;

import java.util.Arrays;
import java.util.HashSet;

public class GenesisCertifyPersonRecord extends GenesisRecord {

    private static final byte TYPE_ID = (byte) Transaction.GENESIS_CERTIFY_PERSON_TRANSACTION;
    private static final String NAME_ID = "GENESIS Certify Person";
    private static final int RECIPIENT_LENGTH = TransactionAmount.RECIPIENT_LENGTH;


    private static final int BASE_LENGTH = GenesisRecord.BASE_LENGTH + RECIPIENT_LENGTH + KEY_LENGTH;

    private Account recipient;
    private long key;

    public GenesisCertifyPersonRecord(Account recipient, long key) {
        super(TYPE_ID, NAME_ID);
        this.recipient = recipient;
        this.key = key;
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

        return new GenesisCertifyPersonRecord(recipient, key);
    }

    public Account getRecipient() {
        return this.recipient;
    }

    @Override
    public long getKey() {
        return this.key;
    }

    //PARSE/CONVERT

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = super.toJson();

        //ADD CREATOR/RECIPIENT/AMOUNT/ASSET
        transaction.put("recipient", this.recipient.getAddress());
        transaction.put("person", this.key);

        return transaction;
    }

    //@Override
    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = super.toBytes(forDeal, withSignature);

        //WRITE RECIPIENT
        data = Bytes.concat(data, this.recipient.getAddressBytes());

        //WRITE KEY
        byte[] keyBytes = Longs.toByteArray(this.key);
        keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
        data = Bytes.concat(data, keyBytes);

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {

        return BASE_LENGTH;
    }


    //VALIDATE

    @Override
    public int isValid(int forDeal, long flags) {

        //CHECK IF RECIPIENT IS VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(this.recipient.getAddressBytes())) {
            return INVALID_ADDRESS;
        }

        if (!this.dcSet.getItemPersonMap().contains(this.key)) {
            return Transaction.ITEM_PERSON_NOT_EXIST;
        }

        return VALIDATE_OK;
    }

    //PROCESS/ORPHAN

    @Override
    public void process(Block block, int forDeal) {

        int transactionIndex = -1;
        int blockIndex = -1;
        if (block == null) {
            blockIndex = this.dcSet.getBlockMap().last().getHeight();
        } else {
            blockIndex = block.getHeight();
            if (blockIndex < 1) {
                // if block not is confirmed - get last block + 1
                blockIndex = this.dcSet.getBlockMap().last().getHeight() + 1;
            }
            transactionIndex = block.getTransactionSeq(signature);
        }

        //UPDATE RECIPIENT
        Tuple5<Long, Long, byte[], Integer, Integer> itemP =
                new Tuple5<Long, Long, byte[], Integer, Integer>
                        (timestamp, Long.MAX_VALUE, null, blockIndex, transactionIndex);

        // SET ALIVE PERSON for DURATION permanent
        ///db.getPersonStatusMap().addItem(this.key, StatusCls.ALIVE_KEY, itemP);

        // SET PERSON ADDRESS - end date as timestamp
        Tuple4<Long, Integer, Integer, Integer> itemA = new Tuple4<Long, Integer, Integer, Integer>(this.key, Integer.MAX_VALUE, blockIndex, transactionIndex);
        Tuple3<Integer, Integer, Integer> itemA1 = new Tuple3<Integer, Integer, Integer>(0, blockIndex, transactionIndex);
        this.dcSet.getAddressPersonMap().addItem(this.recipient.getShortAddressBytes(), itemA);
        this.dcSet.getPersonAddressMap().addItem(this.key, this.recipient.getAddress(), itemA1);

        //UPDATE REFERENCE OF RECIPIENT
        this.recipient.setLastTimestamp(new long[]{this.timestamp, dbRef}, this.dcSet);
    }

    @Override
    public void orphan(Block block, int forDeal) {

        // UNDO ALIVE PERSON for DURATION
        //db.getPersonStatusMap().removeItem(this.key, StatusCls.ALIVE_KEY);

        //UPDATE RECIPIENT
        this.dcSet.getAddressPersonMap().removeItem(this.recipient.getShortAddressBytes());
        this.dcSet.getPersonAddressMap().removeItem(this.key, this.recipient.getAddress());

        //UPDATE REFERENCE OF RECIPIENT
        this.recipient.removeLastTimestamp(this.dcSet);
    }

    //REST

    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<>();
        accounts.add(this.recipient);
        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {

        if (account.equals(recipient)) {
            return true;
        }

        return false;
    }

}
