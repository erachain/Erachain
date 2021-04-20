package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Crypto;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.HashSet;

//import java.math.BigInteger;

public class GenesisRecord extends Transaction {

    protected static final int BASE_LENGTH = SIMPLE_TYPE_LENGTH;

    public GenesisRecord(byte type, String NAME_ID) {
        super(type, NAME_ID);
    }

    //GETTERS/SETTERS

	/*
	@Override
	public byte[] getReference()
	{
		return this.signature;
	}
	*/

    public boolean hasPublicText() {
        return false;
    }

    public void generateSignature() {

        //return generateSignature1(this.recipient, this.amount, this.timestamp);
        byte[] data = this.toBytes(FOR_NETWORK, false);

        //DIGEST
        byte[] digest = Crypto.getInstance().digest(data);
        digest = Bytes.concat(digest, digest);

        this.signature = digest;

    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        generateSignature();
        JSONObject transaction = this.getJsonBase();

        return transaction;
    }

    //PARSE CONVERT
    //public abstract Transaction Parse(byte[] data);

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        //WRITE TYPE in typeBytes[0]
        byte[] data = new byte[]{this.typeBytes[0]};

        // SIGNATURE not need - its calculated on fly

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {

        return BASE_LENGTH;

    }

    //VALIDATE

    public boolean isSignatureValid() {
        return Arrays.equals(this.signature, this.getSignature());
    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        return this.getRecipientAccounts();
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<>();
        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {
        return false;
    }

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {
        return 0L;
    }
}
