package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.payment.Payment;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.*;

/**
 * @deprecated
 */
public class MultiPaymentTransaction extends Transaction {

    private static final byte TYPE_ID = (byte) Transaction.MULTI_PAYMENT_TRANSACTION;
    private static final String NAME_ID = "Multi Send";
    private static final int PAYMENTS_SIZE_LENGTH = 4;
    private static final int BASE_LENGTH = TransactionAmount.BASE_LENGTH + PAYMENTS_SIZE_LENGTH;

    private List<Payment> payments;

    public MultiPaymentTransaction(byte[] typeBytes, PublicKeyAccount creator, List<Payment> payments, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, null, feePow, timestamp, reference);
        this.payments = payments;
    }

    public MultiPaymentTransaction(byte[] typeBytes, PublicKeyAccount creator, List<Payment> payments, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, payments, feePow, timestamp, reference);
        this.signature = signature;
        //this.calcFee();
    }

    // as pack
    public MultiPaymentTransaction(byte[] typeBytes, PublicKeyAccount creator, List<Payment> payments, Long reference, byte[] signature) {
        this(typeBytes, creator, payments, (byte) 0, 0l, reference);
        this.signature = signature;
    }

    public MultiPaymentTransaction(PublicKeyAccount creator, List<Payment> payments, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, payments, feePow, timestamp, reference);
    }

    public MultiPaymentTransaction(PublicKeyAccount creator, List<Payment> payments, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, payments, (byte) 0, 0l, reference);
    }

    //GETTERS/SETTERS

    //public static String getName() { return "Multi Send"; }

    public static Transaction Parse(byte[] data, int forDeal) throws Exception {

        int test_len = BASE_LENGTH;
        if (forDeal == Transaction.FOR_MYPACK) {
            test_len -= Transaction.TIMESTAMP_LENGTH + Transaction.FEE_POWER_LENGTH;
        } else if (forDeal == Transaction.FOR_PACK) {
            test_len -= Transaction.TIMESTAMP_LENGTH;
        } else if (forDeal == Transaction.FOR_DB_RECORD) {
            test_len += Transaction.FEE_POWER_LENGTH;
        }
        if (data.length < test_len) {
            throw new Exception("Data does not match RAW length " + data.length + " < " + test_len);
        }


        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        long timestamp = 0;
        if (forDeal > Transaction.FOR_MYPACK) {
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
        if (forDeal > Transaction.FOR_PACK) {
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        /////
        //READ PAYMENTS SIZE
        byte[] paymentsLengthBytes = Arrays.copyOfRange(data, position, position + PAYMENTS_SIZE_LENGTH);
        int paymentsLength = Ints.fromByteArray(paymentsLengthBytes);
        position += PAYMENTS_SIZE_LENGTH;

        if (paymentsLength < 1 || paymentsLength > 400) {
            throw new Exception("Invalid payments length");
        }

        //READ PAYMENTS
        List<Payment> payments = new ArrayList<Payment>();
        for (int i = 0; i < paymentsLength; i++) {
            Payment payment = Payment.parse(Arrays.copyOfRange(data, position, position + Payment.BASE_LENGTH));
            payments.add(payment);

            position += Payment.BASE_LENGTH;
        }

        if (forDeal > Transaction.FOR_MYPACK) {
            return new MultiPaymentTransaction(typeBytes, creator, payments, feePow, timestamp, reference, signatureBytes);
        } else {
            return new MultiPaymentTransaction(typeBytes, creator, payments, reference, signatureBytes);
        }

    }

    public List<Payment> getPayments() {
        return this.payments;
    }

    //PARSE/CONVERT

    @Override
    public boolean hasPublicText() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        //ADD CREATOR/PAYMENTS
        transaction.put("creator", this.creator.getAddress());

        JSONArray payments = new JSONArray();
        for (Payment payment : this.payments) {
            payments.add(payment.toJson());
        }
        transaction.put("payments", payments);

        return transaction;
    }

    //@Override
    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        //WRITE PAYMENTS SIZE
        int paymentsLength = this.payments.size();
        byte[] paymentsLengthBytes = Ints.toByteArray(paymentsLength);
        data = Bytes.concat(data, paymentsLengthBytes);

        //WRITE PAYMENTS
        for (Payment payment : this.payments) {
            data = Bytes.concat(data, payment.toBytes());
        }

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        int paymentsLength = 0;
        for (Payment payment : this.getPayments()) {
            paymentsLength += payment.getDataLength();
        }

        if (withSignature) {
            return BASE_LENGTH_AS_PACK + paymentsLength;
        } else {
            return BASE_LENGTH + paymentsLength;
        }
    }

    //VALIDATE

    //@Override
    @Override
    public int isValid(int forDeal, long flags) {

        //CHECK PAYMENTS SIZE
        if (this.payments.size() < 1 || this.payments.size() > 400) {
            return INVALID_PAYMENTS_LENGTH;
        }

        //REMOVE FEE
        // TODO REMOVE FORK!!!! - use calculate instead
        try (DCSet fork = this.dcSet.fork(this.toString())) {
            //this.creator.setBalance(FEE_KEY, this.creator.getBalance(fork, FEE_KEY).subtract(this.fee), fork);
            this.creator.changeBalance(fork, true, false, FEE_KEY, this.fee, false, false, false);

            //CHECK IF CREATOR HAS ENOUGH FEE BALANCE
            if (this.creator.getBalance(fork, FEE_KEY).a.b.compareTo(BigDecimal.ZERO) == -1) {
                return NO_BALANCE;
            }

            //CHECK PAYMENTS
            for (Payment payment : this.payments) {
                //CHECK IF RECIPIENT IS VALID ADDRESS
                if (!Crypto.getInstance().isValidAddress(payment.getRecipient().getAddressBytes())) {
                    return INVALID_ADDRESS;
                }

                //CHECK IF AMOUNT IS POSITIVE
                if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    return NEGATIVE_AMOUNT;
                }

                //CHECK IF CREATOR HAS ENOUGH ASSET BALANCE
                if (this.creator.getBalance(fork, payment.getAsset()).a.b.compareTo(payment.getAmount()) == -1) {
                    return NO_BALANCE;
                }

                // CHECK IF AMOUNT wrong SCALE
                AssetCls asset = (AssetCls) this.dcSet.getItemAssetMap().get(payment.getAsset());
                if (payment.getAmount().scale() != asset.getScale()) {
                    return AMOUNT_SCALE_WRONG;
                }

                //PROCESS PAYMENT IN FORK
                payment.process(this.creator, fork);
            }
        }

        return super.isValid(forDeal, flags);
    }

    //PROCESS/ORPHAN

    //@Override
    @Override
    public void process(Block block, int forDeal) {
        //UPDATE CREATOR
        super.process(block, forDeal);

        //PROCESS PAYMENTS
        for (Payment payment : this.payments) {
            payment.process(this.creator, this.dcSet);

            //UPDATE REFERENCE OF RECIPIENT
            if (false && payment.getRecipient().getLastTimestamp(this.dcSet) == null) {
                payment.getRecipient().setLastTimestamp(new long[]{this.timestamp, dbRef}, this.dcSet);
            }
        }
    }

    //@Override
    @Override
    public void orphan(Block block, int forDeal) {
        //UPDATE CREATOR
        super.orphan(block, forDeal);

        //ORPHAN PAYMENTS
        for (Payment payment : this.payments) {
            payment.orphan(this.creator, this.dcSet);

            //UPDATE REFERENCE OF RECIPIENT
            if (false && payment.getRecipient().getLastTimestamp(this.dcSet).equals(this.timestamp)) {
                payment.getRecipient().setLastTimestamp(new long[]{this.reference, dbRef}, this.dcSet);
            }
        }
    }

    //REST

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.creator);
        accounts.addAll(this.getRecipientAccounts());
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<>();

        for (Payment payment : this.payments) {
            accounts.add(payment.getRecipient());
        }

        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {

        if (account.equals(creator)) return true;

        for (Account recipient : this.getRecipientAccounts()) {
            if (recipient.equals(account))
                return true;
        }

        return false;
    }

    //@Override
    @Override
    public BigDecimal getAmount(Account account) {
        BigDecimal amount = BigDecimal.ZERO;
        String address = account.getAddress();

        //IF CREATOR
        if (address.equals(this.creator.getAddress())) {
            amount = amount.subtract(this.fee);
        }

        //CHECK PAYMENTS
        for (Payment payment : this.payments) {
            //IF FEE ASSET
            if (payment.getAsset() == FEE_KEY) {
                //IF CREATOR
                if (address.equals(this.creator.getAddress())) {
                    amount = amount.subtract(payment.getAmount());
                }

                //IF RECIPIENT
                if (address.equals(payment.getRecipient().getAddress())) {
                    amount = amount.add(payment.getAmount());
                }
            }
        }

        return amount;
    }

    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

        assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);

        for (Payment payment : this.payments) {
            assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), payment.getAsset(), payment.getAmount());
            assetAmount = addAssetAmount(assetAmount, payment.getRecipient().getAddress(), payment.getAsset(), payment.getAmount());
        }

        return assetAmount;
    }

}