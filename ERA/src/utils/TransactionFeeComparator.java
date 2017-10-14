package utils;

import java.util.Comparator;
import java.util.List;

import org.mapdb.Fun.Tuple2;

import core.transaction.Transaction;

public class TransactionFeeComparator implements Comparator<Transaction> {
	
	@Override
	public int compare(Transaction one, Transaction two) 
	{

		// TODO need timestamp USE too see utils.TransactionTimestampComparator !!
		return two.getFee().compareTo(one.getFee());
	}
}