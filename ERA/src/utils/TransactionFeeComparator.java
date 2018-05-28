package utils;

import core.transaction.Transaction;

import java.util.Comparator;

public class TransactionFeeComparator implements Comparator<Transaction> {

    @Override
    public int compare(Transaction one, Transaction two) {
        return two.getFee().compareTo(one.getFee());
    }
}