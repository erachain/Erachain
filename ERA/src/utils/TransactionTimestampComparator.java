package utils;

import core.transaction.Transaction;

import java.util.Comparator;

public class TransactionTimestampComparator implements Comparator<Transaction> {

    @Override
    public int compare(Transaction one, Transaction two) {
        if (one.getTimestamp() < two.getTimestamp())
            return -1;
        else if (one.getTimestamp() > two.getTimestamp())
            return 1;

        byte[] s1 = one.getSignature();
        byte[] s2 = two.getSignature();
        for (int i = 0; i < s1.length; i++) {
            if (s1[i] < s2[i])
                return -1;
            else if (s1[i] > s2[i])
                return 1;
        }

        return 0;
    }
}