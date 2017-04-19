package utils;

import java.util.Comparator;

import core.transaction.Transaction;

public class TransactionFeeComparator implements Comparator<Transaction> {
	
	@Override
	public int compare(Transaction one, Transaction two) 
	{
		return two.getFee().compareTo(one.getFee());
	}
}