package core.item.assets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.transaction.CancelOrderTransaction;
import core.transaction.Transaction;
import database.DBSet;
import database.SortableList;

public class OrderReverse extends Order {
	
	public OrderReverse(BigInteger id, Account creator, long have, long want, BigDecimal amountHave, BigDecimal amountWant, long timestamp)
	{
		super(id, creator, have, want, amountHave, amountWant, timestamp);
	}
	
	public OrderReverse(BigInteger id, Account creator, long have, long want, BigDecimal amountHave,
			BigDecimal amountWant, BigDecimal fulfilledHave, BigDecimal fulfilledWant,
			byte isExecutable, long timestamp)
	{
		super(id, creator, have, want, amountHave,
				amountWant, fulfilledHave, fulfilledWant,
				isExecutable, timestamp);
	}


	//@Override
	public int compareTo(OrderReverse order) 
	{	
		//COMPARE ONLY BY PRICE
		int result = this.getPriceCalc().compareTo(order.getPriceCalc());
		if (result != 0)
			return result;

		// TODO: REMOVE it in new CHAIN
		//if (this.timestamp < 1501816130973000l)
		//	return 0;
		
		long orderTimestamp = order.getTimestamp();
		if (this.timestamp < orderTimestamp)
			return 1;
		else if (this.timestamp > orderTimestamp)
			return -1;
		
		return 0;
		
		
	}
	
}
