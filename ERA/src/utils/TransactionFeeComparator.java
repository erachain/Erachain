package utils;

import java.util.Comparator;
import java.util.List;

import org.mapdb.Fun.Tuple2;

import core.transaction.Transaction;

public class TransactionFeeComparator implements Comparator<Tuple2<List<byte[]>, Transaction>> {
	
	@Override
	public int compare(Tuple2<List<byte[]>, Transaction> one, Tuple2<List<byte[]>, Transaction> two) 
	{
		return two.b.getFee().compareTo(one.b.getFee());
	}
}