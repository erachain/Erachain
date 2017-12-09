package datachain;

//04/01 +- 
import java.lang.reflect.Array;
//import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import core.account.Account;
import core.crypto.Base58;
import core.transaction.ArbitraryTransaction;
import core.transaction.Transaction;
import database.serializer.TransactionSerializer;
import utils.BlExpUnit;
import utils.ObserverMessage;

//signature -> <BlockHeoght, Record No>
public class TransactionFinalMapSigns extends DCMap<byte[], Tuple2<Integer, Integer>>
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
		
	public TransactionFinalMapSigns(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
	//	this.observableData.put(DCMap.NOTIFY_ADD, ObserverMessage.ADD_TRANSACTION_TYPE);
	//	this.observableData.put(DCMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRANSACTION_TYPE);
	//	this.observableData.put(DCMap.NOTIFY_LIST, ObserverMessage.LIST_TRANSACTION_TYPE);
	}

	public TransactionFinalMapSigns(TransactionFinalMapSigns parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database)
	{
	}
	
	@SuppressWarnings("unchecked")
	private Map<byte[], Tuple2<Integer, Integer>> openMap(DB database)
	{
		
		BTreeMap<byte[], Tuple2<Integer, Integer>> map = database.createTreeMap("signature_final_tx")
				.comparator(Fun.BYTE_ARRAY_COMPARATOR)
				.makeOrGet();

		return map;
		
	}

	@Override
	protected Map<byte[], Tuple2<Integer, Integer>> getMap(DB database) 
	{
		//OPEN MAP
		return openMap(database);
	}

	@Override
	protected Map<byte[], Tuple2<Integer, Integer>> getMemoryMap() 
	{
		DB database = DBMaker.newMemoryDB().make();
		
		//OPEN MAP
		return this.getMap(database);
	}

	@Override
	protected Tuple2<Integer, Integer> getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	/*
	public Tuple2<Integer,Integer> getHeightSegBySignature(byte[] sign){
		
		return this.get(sign);
		
	}
	*/

}