package org.erachain.utils;

import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionsList extends AbstractList<Transaction> {
    private List<byte[]> transactionSignatures;
    private Map<byte[], Transaction> transactions;

    public TransactionsList(List<byte[]> transactionSignatures) {
        this.transactionSignatures = transactionSignatures;
        this.transactions = new HashMap<byte[], Transaction>();
    }


    @Override
    public Transaction get(int index) {
        if (!this.transactions.containsKey(this.transactionSignatures.get(index))) {
            this.transactions.put(this.transactionSignatures.get(index), DCSet.getInstance().getTransactionTab()
                    .get(this.transactionSignatures.get(index)));
        }

        return this.transactions.get(this.transactionSignatures.get(index));
    }

    @Override
    public int size() {
        return this.transactionSignatures.size();
    }

}
