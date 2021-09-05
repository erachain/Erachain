package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.payment.Payment;
import org.erachain.datachain.DCSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @deprecated 
 */
public class ArbitraryTransactionV3 extends ArbitraryTransaction {
    protected static final int SERVICE_LENGTH = 4;
    private static final int PAYMENTS_SIZE_LENGTH = 4;
    protected static final int BASE_LENGTH = TYPE_LENGTH + FEE_POWER_LENGTH + TIMESTAMP_LENGTH
            + REFERENCE_LENGTH + CREATOR_LENGTH + SERVICE_LENGTH
            + DATA_SIZE_LENGTH + SIGNATURE_LENGTH
            + PAYMENTS_SIZE_LENGTH;

    public ArbitraryTransactionV3(byte[] typeBytes,
                                  PublicKeyAccount creator, List<Payment> payments, int service,
                                  byte[] data, byte feePow, long timestamp, Long reference) {
        super(typeBytes, creator, feePow, timestamp, reference);

        this.creator = creator;
        if (payments == null) {
            this.payments = new ArrayList<Payment>();
        } else {
            this.payments = payments;
        }
        this.service = service;
        this.data = data;
    }

    public ArbitraryTransactionV3(byte[] typeBytes,
                                  PublicKeyAccount creator, List<Payment> payments, int service, byte[] data,
                                  byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, payments, service, data, feePow, timestamp, reference);
        this.signature = signature;
        //this.calcFee();
    }

    public ArbitraryTransactionV3(
            PublicKeyAccount creator, List<Payment> payments, int service, byte[] data,
            byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{ArbitraryTransaction.TYPE_ID, 0, 0, 0}, creator, payments, service, data, feePow, timestamp, reference, signature);
    }

    public ArbitraryTransactionV3(
            PublicKeyAccount creator, List<Payment> payments, int service, byte[] data,
            byte feePow, long timestamp, Long reference) {
        this(new byte[]{ArbitraryTransaction.TYPE_ID, 0, 0, 0}, creator, payments, service, data, feePow, timestamp, reference);
    }

    public static Transaction Parse(byte[] data) throws Exception {
        // CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < BASE_LENGTH) {
            throw new Exception("Data does not match block length");
        }


        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        // READ TIMESTAMP
        byte[] timestampBytes = Arrays.copyOfRange(data, position, position
                + TIMESTAMP_LENGTH);
        long timestamp = Longs.fromByteArray(timestampBytes);
        position += TIMESTAMP_LENGTH;

        // READ REFERENCE
        byte[] referenceBytes = Arrays.copyOfRange(data, position, position
                + REFERENCE_LENGTH);
        long reference = Longs.fromByteArray(referenceBytes);
        position += REFERENCE_LENGTH;

        // READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position
                + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        // READ PAYMENTS SIZE
        byte[] paymentsLengthBytes = Arrays.copyOfRange(data, position,
                position + PAYMENTS_SIZE_LENGTH);
        int paymentsLength = Ints.fromByteArray(paymentsLengthBytes);
        position += PAYMENTS_SIZE_LENGTH;

        if (paymentsLength < 0 || paymentsLength > 400) {
            throw new Exception("Invalid payments length");
        }

        // READ PAYMENTS
        List<Payment> payments = new ArrayList<Payment>();
        for (int i = 0; i < paymentsLength; i++) {
            Payment payment = Payment.parse(Arrays.copyOfRange(data, position,
                    position + Payment.BASE_LENGTH));
            payments.add(payment);

            position += Payment.BASE_LENGTH;
        }

        // READ SERVICE
        byte[] serviceBytes = Arrays.copyOfRange(data, position, position
                + SERVICE_LENGTH);
        int service = Ints.fromByteArray(serviceBytes);
        position += SERVICE_LENGTH;

        // READ DATA SIZE
        byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position
                + DATA_SIZE_LENGTH);
        int dataSize = Ints.fromByteArray(dataSizeBytes);
        position += DATA_SIZE_LENGTH;

        // READ DATA
        byte[] arbitraryData = Arrays.copyOfRange(data, position, position
                + dataSize);
        position += dataSize;

        //READ FEE POWER
        byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
        byte feePow = feePowBytes[0];
        position += 1;

        // READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position
                + SIGNATURE_LENGTH);

        return new ArbitraryTransactionV3(typeBytes, creator, payments,
                service, arbitraryData, feePow, timestamp, reference, signatureBytes);
    }

    // PARSE CONVERT

    @Override
    public boolean hasPublicText() {
        return false;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = new byte[0];

        // WRITE TYPE
        data = Bytes.concat(data, this.typeBytes);

        // WRITE TIMESTAMP
        byte[] timestampBytes = Longs.toByteArray(this.timestamp);
        timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
        data = Bytes.concat(data, timestampBytes);

        // WRITE REFERENCE
        byte[] referenceBytes = Longs.toByteArray(this.reference);
        referenceBytes = Bytes.ensureCapacity(referenceBytes, REFERENCE_LENGTH, 0);
        data = Bytes.concat(data, referenceBytes);

        // WRITE CREATOR
        data = Bytes.concat(data, this.creator.getPublicKey());

        // WRITE PAYMENTS SIZE
        int paymentsLength = this.payments.size();
        byte[] paymentsLengthBytes = Ints.toByteArray(paymentsLength);
        data = Bytes.concat(data, paymentsLengthBytes);

        // WRITE PAYMENTS
        for (Payment payment : this.payments) {
            data = Bytes.concat(data, payment.toBytes());
        }

        // WRITE SERVICE
        byte[] serviceBytes = Ints.toByteArray(this.service);
        data = Bytes.concat(data, serviceBytes);

        // WRITE DATA SIZE
        byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
        data = Bytes.concat(data, dataSizeBytes);

        // WRITE DATA
        data = Bytes.concat(data, this.data);

        //WRITE FEE POWER
        byte[] feePowBytes = new byte[1];
        feePowBytes[0] = this.feePow;
        data = Bytes.concat(data, feePowBytes);

        // SIGNATURE
        if (withSignature)
            data = Bytes.concat(data, this.signature);

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        int paymentsLength = 0;
        for (Payment payment : this.getPayments()) {
            paymentsLength += payment.getDataLength();
        }

        return BASE_LENGTH + this.data.length + paymentsLength;
    }

    // VALIDATE

    //@Override
    @Override
    public int isValid(int forDeal, long flags) {

        int result = super.isValid(forDeal, flags);
        if (result != VALIDATE_OK) {
            return result;
        }

        // CHECK PAYMENTS SIZE
        if (this.payments.size() < 0 || this.payments.size() > 400) {
            return INVALID_PAYMENTS_LENGTH;
        }

        // CHECK DATA SIZE
        if (data.length > BlockChain.MAX_REC_DATA_BYTES || data.length < 1) {
            return INVALID_DATA_LENGTH;
        }

        //if (true)
        //    return INVALID_AMOUNT;
        
        // REMOVE FEE
        Transaction forkTransaction = this.copy();
        try (DCSet fork = this.dcSet.fork(this.toString())) {
            forkTransaction.setDC(fork, Transaction.FOR_NETWORK, this.height, this.seqNo, true);
            Block block = fork.getBlockMap().getAndProcess(this.height);
            forkTransaction.process(block, Transaction.FOR_NETWORK);
            // TODO process && orphan && isValid balances

            // CHECK PAYMENTS
            for (Payment payment : this.payments) {
                // CHECK IF RECIPIENT IS VALID ADDRESS
                if (!Crypto.getInstance().isValidAddress(
                        payment.getRecipient().getAddressBytes())) {
                    return INVALID_ADDRESS;
                }

                // CHECK IF AMOUNT IS POSITIVE
                if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    return NEGATIVE_AMOUNT;
                }

                // CHECK IF SENDER HAS ENOUGH ASSET BALANCE
                if (this.creator.getBalance(fork, payment.getAsset()).a.b
                        .compareTo(payment.getAmount()) == -1) {
                    return NO_BALANCE;
                }

                // CHECK IF AMOUNT wrong SCALE
                AssetCls asset = (AssetCls) this.dcSet.getItemAssetMap().get(payment.getAsset());
                if (payment.getAmount().scale() != asset.getScale()) {
                    return AMOUNT_SCALE_WRONG;
                }

            }
        }

        return VALIDATE_OK;
    }

}