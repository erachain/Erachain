package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.datachain.DCSet;
import org.erachain.smartcontracts.SmartContract;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.*;

public class RecordReleasePack extends Transaction {

    private static final byte TYPE_ID = (byte) Transaction.RELEASE_PACK;
    private static final String NAME_ID = "Release pack";
    private static final int PACK_SIZE_LENGTH = 4;

    private static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + PACK_SIZE_LENGTH;
    private static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + PACK_SIZE_LENGTH;
    private static final int BASE_LENGTH = Transaction.BASE_LENGTH + PACK_SIZE_LENGTH;
    private static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + PACK_SIZE_LENGTH;

    private List<Transaction> transactions;

    public RecordReleasePack(byte[] typeBytes, PublicKeyAccount creator, List<Transaction> transactions, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, null, null, feePow, timestamp, reference);
        this.transactions = transactions;
    }

    public RecordReleasePack(byte[] typeBytes, PublicKeyAccount creator, List<Transaction> transactions, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, transactions, feePow, timestamp, reference);
        this.signature = signature;
    }

    public RecordReleasePack(byte[] typeBytes, PublicKeyAccount creator, List<Transaction> transactions, byte feePow,
                             long timestamp, Long reference, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, transactions, feePow, timestamp, reference);
        this.signature = signature;
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    // as pack - calcFee not needed
    public RecordReleasePack(byte[] typeBytes, PublicKeyAccount creator, List<Transaction> transactions, Long reference, byte[] signature) {
        this(typeBytes, creator, transactions, (byte) 0, 0l, reference);
        this.signature = signature;
    }

    public RecordReleasePack(PublicKeyAccount creator, List<Transaction> transactions, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, transactions, feePow, timestamp, reference);
    }

    // as Pack
    public RecordReleasePack(PublicKeyAccount creator, List<Transaction> transactions, Long reference) {
        this(creator, transactions, (byte) 0, 0l, reference);
    }

    //GETTERS/SETTERS

    //PARSE/CONVERT

    public static Transaction Parse(byte[] data, int forDeal) throws Exception {

        int test_len;
        if (forDeal == Transaction.FOR_MYPACK) {
            test_len = BASE_LENGTH_AS_MYPACK;
        } else if (forDeal == Transaction.FOR_PACK) {
            test_len = BASE_LENGTH_AS_PACK;
        } else if (forDeal == Transaction.FOR_DB_RECORD) {
            test_len = BASE_LENGTH_AS_DBRECORD;
        } else {
            test_len = BASE_LENGTH;
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

        ExLink exLink;
        if ((typeBytes[2] & HAS_EXLINK_MASK) > 0) {
            exLink = ExLink.parse(data, position);
            position += exLink.length();
        } else {
            exLink = null;
        }

        SmartContract smartContract;
        if ((typeBytes[2] & HAS_SMART_CONTRACT_MASK) > 0) {
            smartContract = SmartContract.Parses(data, position, forDeal);
            position += smartContract.length(forDeal);
        } else {
            smartContract = null;
        }

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

        long feeLong = 0;
        long seqNo = 0;
        if (forDeal == FOR_DB_RECORD) {
            //READ SEQ_NO
            byte[] seqNoBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            seqNo = Longs.fromByteArray(seqNoBytes);
            position += TIMESTAMP_LENGTH;

            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

        /////
        //READ PACK SIZE
        byte[] transactionsLengthBytes = Arrays.copyOfRange(data, position, position + PACK_SIZE_LENGTH);
        int transactionsLength = Ints.fromByteArray(transactionsLengthBytes);
        position += PACK_SIZE_LENGTH;

        if (transactionsLength < 1 || transactionsLength > 400) {
            throw new Exception("Invalid pack length");
        }

        //READ TRANSACTIONS
        TransactionFactory tf_inct = TransactionFactory.getInstance();
        List<Transaction> transactions = new ArrayList<Transaction>();
        for (int i = 0; i < transactionsLength; i++) {
            Transaction transaction = tf_inct.parse(Arrays.copyOfRange(data, position, data.length), Transaction.FOR_PACK);
            transactions.add(transaction);

            position += transaction.getDataLength(Transaction.FOR_PACK, true);
        }

        if (forDeal > Transaction.FOR_MYPACK) {
            return new RecordReleasePack(typeBytes, creator, transactions, feePow, timestamp, reference,
                    signatureBytes, seqNo, feeLong);
        } else {
            return new RecordReleasePack(typeBytes, creator, transactions, reference, signatureBytes);
        }
    }

    public List<Transaction> getTransactions() {
        return this.transactions;
    }

    @Override
    public boolean hasPublicText() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject json = this.getJsonBase();

        //ADD CREATOR/PAYMENTS
        json.put("creator", this.creator.getAddress());

        JSONArray transactions = new JSONArray();
        for (Transaction transaction : this.transactions) {
            transactions.add(transaction.toJson());
        }
        json.put("transactions", transactions);

        return json;
    }

    //@Override
    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        //WRITE PAYMENTS SIZE
        int transactionsLength = this.transactions.size();
        byte[] transactionsLengthBytes = Ints.toByteArray(transactionsLength);
        data = Bytes.concat(data, transactionsLengthBytes);

        //WRITE PAYMENTS
        for (Transaction transaction : this.transactions) {
            data = Bytes.concat(data, transaction.toBytes(forDeal, false));
        }

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {

        int base_len;
        if (forDeal == FOR_MYPACK)
            base_len = BASE_LENGTH_AS_MYPACK;
        else if (forDeal == FOR_PACK)
            base_len = BASE_LENGTH_AS_PACK;
        else if (forDeal == FOR_DB_RECORD)
            base_len = BASE_LENGTH_AS_DBRECORD;
        else
            base_len = BASE_LENGTH;

        if (exLink != null)
            base_len += exLink.length();

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        int transactionsLength = 0;
        for (Transaction transaction : this.getTransactions()) {
            transactionsLength += transaction.getDataLength(forDeal, true);
        }

        return base_len + transactionsLength;
    }

    //VALIDATE

    //@Override
    @Override
    public int isValid(int forDeal, long flags) {

        //CHECK PAYMENTS SIZE
        if (this.transactions.size() < 1 || this.transactions.size() > 400) {
            return INVALID_PAYMENTS_LENGTH;
        }

        try (DCSet fork = this.dcSet.fork(this.toString())) {

            int counter = 0;
            int result = 0;
            //CHECK PAYMENTS

            Block block = this.dcSet.getBlockMap().getAndProcess(this.height);
            for (Transaction transaction : this.transactions) {

                result = transaction.isValid(forDeal, flags);
                if (result != Transaction.VALIDATE_OK)
                    // transaction counter x100
                    return result + counter * 100;
                //PROCESS PAYMENT IN FORK AS PACK
                transaction.process(block, forDeal);
                counter++;
            }
        }
        // IN FORK
        return super.isValid(forDeal, flags);

    }

    //PROCESS/ORPHAN

    //@Override
    @Override
    public void processBody(Block block, int forDeal) {
        //UPDATE CREATOR
        super.processBody(block, forDeal);

        //PROCESS PAYMENTS
        for (Transaction transaction : this.transactions) {
            transaction.processBody(block, forDeal); // as Pack in body
        }
    }

    //@Override
    @Override
    public void orphan(Block block, int forDeal) {
        //UPDATE CREATOR
        super.orphan(block, forDeal);

        //ORPHAN PAYMENTS
        for (Transaction transaction : this.transactions) {
            transaction.setDC(this.dcSet,
                    true); // нужно нарастить мясо на скелет чтобы правильно удалить ключи?
            transaction.orphan(block, forDeal); // as Pack in body
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

        for (Transaction transaction : this.transactions) {
            accounts.addAll(transaction.getInvolvedAccounts());
        }

        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {

        for (Account involved : this.getInvolvedAccounts()) {
            if (account.equals(involved)) {
                return true;
            }
        }

        return false;
    }

    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

        assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);

        for (Transaction transaction : this.transactions) {
            //assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), transaction.getAsset(), transaction.getAmount());
            //assetAmount = addAssetAmount(assetAmount, transaction.getRecipient().getAddress(), transaction.getAsset(), transaction.getAmount());
        }

        return assetAmount;
    }

}