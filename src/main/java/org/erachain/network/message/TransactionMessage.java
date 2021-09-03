package org.erachain.network.message;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;

public class TransactionMessage extends Message {

    private Transaction transaction;

    public TransactionMessage(Transaction transaction) {
        super(TRANSACTION_TYPE);

        this.transaction = transaction;
    }

    @Override
    public boolean isHandled() {
        return true;
    }

    @Override
    public boolean quickSend() {
        return false;
    }

    // берем подпись с транзакции и трансформируем в Целое
    public static Long getHandledID(byte[] data) {

        int position = Transaction.TYPE_LENGTH
                + Transaction.TIMESTAMP_LENGTH
                + Transaction.REFERENCE_LENGTH
                + Transaction.CREATOR_LENGTH
                + 1 // FEE POWER
                ;

        return Longs.fromBytes(data[position + 1], data[position + 2], data[position + 3], data[position + 4],
                data[position + 5], data[position + 6], data[position + 7], data[position + 8]);

    }

    @Override
    public Long getHandledID() {
        return getHandledID(this.getLoadBytes());
    }

    public static TransactionMessage parse(byte[] data) throws Exception {
        //PARSE TRANSACTION
        Transaction transaction = TransactionFactory.getInstance().parse(data, Transaction.FOR_NETWORK);

        return new TransactionMessage(transaction);
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public boolean isRequest() {
        return false;
    }

    public byte[] toBytes() {
        byte[] data = new byte[0];

        //WRITE BLOCK
        byte[] transactionBytes = this.transaction.toBytes(Transaction.FOR_NETWORK, true);
        data = Bytes.concat(data, transactionBytes);

        //ADD CHECKSUM
        data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);

        return data;
    }

    @Override
    public int getDataLength() {
        return this.transaction.getDataLength(Transaction.FOR_NETWORK, true);
    }

}
