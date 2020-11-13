package org.erachain.core.payment;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class Payment {

    private static final int RECIPIENT_LENGTH = 25;
    private static final int ASSET_LENGTH = 8;
    private static final int AMOUNT_LENGTH = 12;
    public static final int BASE_LENGTH = RECIPIENT_LENGTH + ASSET_LENGTH + AMOUNT_LENGTH;

    private Account recipient;
    private long asset;
    private BigDecimal amount;

    public Payment(Account recipient, long asset, BigDecimal amount) {
        this.recipient = recipient;
        this.asset = asset;
        this.amount = amount;
    }

    //GETTERS/SETTERS

    public static Payment parse(byte[] data) throws Exception {
        int position = 0;

        //READ RECIPIENT
        byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
        Account recipient = new Account(Base58.encode(recipientBytes));
        position += RECIPIENT_LENGTH;

        //READ ASSET
        byte[] assetBytes = Arrays.copyOfRange(data, position, position + ASSET_LENGTH);
        long asset = Longs.fromByteArray(assetBytes);
        position += ASSET_LENGTH;

        //READ AMOUNT
        byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
        BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
        position += AMOUNT_LENGTH;

        return new Payment(recipient, asset, amount);
    }

    public Account getRecipient() {
        return this.recipient;
    }

    public long getAsset() {
        return this.asset;
    }

    //PARSE

    public BigDecimal getAmount() {
        return this.amount;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {
        //GET BASE
        JSONObject payment = new JSONObject();

        //ADD RECIPIENT/ASSET/AMOUNT
        payment.put("recipient", this.recipient.getAddress());
        payment.put("asset", this.asset);
        payment.put("amount", this.amount.toPlainString());

        return payment;
    }

    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE RECIPIENT
        data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));

        //WRITE ASSET
        byte[] assetBytes = Longs.toByteArray(this.asset);
        assetBytes = Bytes.ensureCapacity(assetBytes, ASSET_LENGTH, 0);
        data = Bytes.concat(data, assetBytes);

        //WRITE AMOUNT
        byte[] amountBytes = this.amount.unscaledValue().toByteArray();
        byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
        amountBytes = Bytes.concat(fill, amountBytes);
        data = Bytes.concat(data, amountBytes);

        return data;
    }

    public int getDataLength() {
        return BASE_LENGTH;
    }

    //PROCESS/ORPHAN

    public void process(PublicKeyAccount sender, DCSet db) {
        //UPDATE SENDER
        //sender.setBalance(this.asset, sender.getBalance(db, this.asset).subtract(this.amount), db);
        sender.changeBalance(db, true, false, this.asset, this.amount,
                false, false, true);

        //UPDATE RECIPIENT
        //this.recipient.setBalance(this.asset, this.recipient.getBalance(db, this.asset).add(this.amount), db);
        this.recipient.changeBalance(db, false, false, this.asset, this.amount,
                false, false, false);
    }

    public void orphan(PublicKeyAccount sender, DCSet db) {
        //UPDATE SENDER
        //sender.setBalance(this.asset, sender.getBalance(db, this.asset).add(this.amount), db);
        sender.changeBalance(db, false, false, this.asset, this.amount,
                false, false, true);

        //UPDATE RECIPIENT
        //this.recipient.setBalance(this.asset, this.recipient.getBalance(db, this.asset).subtract(this.amount), db);
        this.recipient.changeBalance(db, true, false, this.asset, this.amount,
                false, false, false);
    }
}
