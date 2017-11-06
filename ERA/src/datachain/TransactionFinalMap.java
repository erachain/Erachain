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
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.SignedBytes;

import core.account.Account;
import core.block.Block;
import core.crypto.Base58;
import core.transaction.ArbitraryTransaction;
import core.transaction.Transaction;
import database.DBMap;
import database.serializer.TransactionSerializer;
import utils.BlExpUnit;
import utils.ObserverMessage;

// block.id + tx.ID in this block -> transaction
// ++ sender_txs
// ++ recipient_txs
// ++ address_type_txs
public class TransactionFinalMap extends DCMap<Tuple2<Integer, Integer>, Transaction>
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	@SuppressWarnings("rawtypes")
	private NavigableSet senderKey;
	@SuppressWarnings("rawtypes")
	private NavigableSet recipientKey;
	@SuppressWarnings("rawtypes")
	private NavigableSet typeKey;

	@SuppressWarnings("rawtypes")
	private NavigableSet block_Key;
	private NavigableSet <Tuple2<String,Tuple2<Integer, Integer>>>signature_key;
	private BTreeMap <byte[], Tuple2<Integer, Integer>> signature_key2;
	
	public TransactionFinalMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		if (databaseSet.isWithObserver()) {
			this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_TRANSACTION_TYPE);
			if (databaseSet.isDynamicGUI()) {
				this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_TRANSACTION_TYPE);
				this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRANSACTION_TYPE);
			}
			this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_TRANSACTION_TYPE);
		}
	}

	public TransactionFinalMap(TransactionFinalMap parent, DCSet dcSet) 
	{
		super(parent, dcSet);
	}
	
	protected void createIndexes(DB database)
	{
	}
	
	@SuppressWarnings("unchecked")
	private Map<Tuple2<Integer, Integer>, Transaction> openMap(DB database)
	{
		
		BTreeMap<Tuple2<Integer, Integer>, Transaction> map = database.createTreeMap("height_seq_transactions")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new TransactionSerializer())
				.makeOrGet();
		
		this.senderKey = database.createTreeSet("sender_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKey(map, this.senderKey, new Fun.Function2<String, Tuple2<Integer,Integer>, Transaction>(){
			@Override
			public String run(Tuple2<Integer, Integer> key, Transaction val) {
				Account account = val.getCreator();
				return account == null? "genesis": account.getAddress();
			}
		});
		
		
		this.block_Key = database.createTreeSet("Block_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKey(map, this.block_Key, new Fun.Function2<Integer, Tuple2<Integer,Integer>, Transaction>(){
			@Override
			public Integer run(Tuple2<Integer, Integer> key, Transaction val) {
				return  val.getBlockHeightByParentOrLast(getDCSet());
			}
		});
		
		
		
		
		this.recipientKey = database.createTreeSet("recipient_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKeys(map, this.recipientKey, new Fun.Function2<String[], Tuple2<Integer,Integer>, Transaction>(){
			@Override
			public String[] run(Tuple2<Integer, Integer> key, Transaction val) {
				List<String> recps = new ArrayList<String>();
				for ( Account acc : val.getRecipientAccounts())
				{
					recps.add(acc.getAddress());
				}
				String[] ret = new String[ recps.size() ];
				ret = recps.toArray( ret );
				return ret;
			}
		});
		
		this.typeKey = database.createTreeSet("address_type_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKeys(map, this.typeKey, new Fun.Function2<Tuple2<String, Integer>[], Tuple2<Integer,Integer>, Transaction>(){
			@Override
			public Tuple2<String, Integer>[] run(Tuple2<Integer, Integer> key, Transaction val) {
				List<Tuple2<String, Integer>> recps = new ArrayList<Tuple2<String, Integer>>();
				Integer type = val.getType();
				for ( Account acc : val.getInvolvedAccounts())
				{
						recps.add(new Tuple2<String, Integer>(acc.getAddress(),type));
					
				}
				//Tuple2<Integer, String>[] ret = (Tuple2<Integer, String>[]) new Object[ recps.size() ];
				Tuple2<String, Integer>[] ret = (Tuple2<String, Integer>[]) Array.newInstance(Fun.Tuple2.class,recps.size());
				ret = recps.toArray( ret );
				return ret;
			}
		});

		this.signature_key = database.createTreeSet("signature_key1")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKey(map, this.signature_key, new Fun.Function2<String, Tuple2<Integer,Integer>, Transaction>(){
			@Override
			public String run(Tuple2<Integer, Integer> key, Transaction val) {
				return  val.viewSignature();
			}
		});
		
		this.signature_key2 = database.createTreeMap("signature_key2")
				.comparator(SignedBytes.lexicographicalComparator())
				//.comparator(BTreeKeySerializer.BASIC)
				.makeOrGet();
		
		Bind.secondaryKey(map, this.signature_key2, new Fun.Function2<byte[], Tuple2<Integer, Integer>, Transaction>(){
			@Override
			public byte[] run(Tuple2<Integer, Integer> key, Transaction val) {
				return  val.getSignature();
			}
		});
		
		
		
		return map;
		
	}

	@Override
	protected Map<Tuple2<Integer, Integer>, Transaction> getMap(DB database) 
	{
		//OPEN MAP
		return openMap(database);
	}

	@Override
	protected Map<Tuple2<Integer, Integer>, Transaction> getMemoryMap() 
	{
		DB database = DBMaker.newMemoryDB().make();
		
		//OPEN MAP
		return this.getMap(database);	}

	@Override
	protected Transaction getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Integer height)
	{	
		BTreeMap map = (BTreeMap) this.map;
		//GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
		Collection<Tuple2> keys = ((BTreeMap<Tuple2, Transaction>) map).subMap(
				Fun.t2(height, null),
				Fun.t2(height, Fun.HI())).keySet();
		
		//DELETE TRANSACTIONS
		for(Tuple2<Integer, Integer> key: keys)
		{
			if (this.contains(key))
				this.delete(key);
		}
	}
	
	public void delete(Integer height, Integer seq)
	{
		this.delete(new Tuple2<Integer, Integer>(height, seq));
	}
	
	
	public boolean add(Integer height, Integer seq, Transaction transaction)
	{
		return this.set(new Tuple2<Integer, Integer>(height, seq), transaction);
	}
	
	public Transaction getTransaction(Integer height, Integer seq)
	{
		Transaction tx = this.get(new Tuple2<Integer,Integer>(height, seq));
		/*
		if ( this.parent != null )
		{
			if ( tx == null )
			{
				return this.parent.get(new Tuple2<Integer,Integer>(height, seq));
			}
		}
		*/
		return tx;
	}
	
	public List<Transaction> getTransactionsByRecipient(String address)
	{
		return getTransactionsByRecipient(address, 0);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Transaction> getTransactionsByRecipient(String address, int limit)
	{
		Iterable keys = Fun.filter(this.recipientKey, address);
		Iterator iter = keys.iterator();
		List<Transaction> txs = new ArrayList<>();
		int counter=0;
		while ( iter.hasNext() && (limit ==0 || counter<limit) )
		{
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		return txs;
	}
	
	public List<Transaction> getTransactionsByBlock(Integer block)
	{
		return getTransactionsByBlock(block, 0);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Transaction> getTransactionsByBlock(Integer block, int limit)
	{
		Iterable keys = Fun.filter(this.block_Key, block);
		Iterator iter = keys.iterator();
		List<Transaction> txs = new ArrayList<>();
		int counter=0;
		while ( iter.hasNext() && (limit ==0 || counter<limit) )
		{
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		return txs;
	}
	
	
	
	public List<Transaction> getTransactionsBySender(String address)
	{
		return getTransactionsBySender(address, 0);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Transaction> getTransactionsBySender(String address, int limit)
	{
		Iterable keys = Fun.filter(this.senderKey, address);
		Iterator iter = keys.iterator();

		List<Transaction> txs = new ArrayList<>();
		int counter=0;
		while ( iter.hasNext() && (limit ==0 || counter<limit) )
		{
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		
		return txs;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Transaction> getTransactionsByTypeAndAddress(String address, Integer type, int limit)
	{
		Iterable keys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(address, type));
		Iterator iter = keys.iterator();

		List<Transaction> txs = new ArrayList<>();
		int counter=0;
		while ( iter.hasNext() && (limit ==0 || counter<limit) )
		{
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		
		return txs;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<BlExpUnit> getBlExpTransactionsByAddress(String address)
	{
		Iterable senderKeys = Fun.filter(this.senderKey, address);
		Iterable recipientKeys = Fun.filter(this.recipientKey, address);

		Set<Tuple2<Integer, Integer>> treeKeys = new TreeSet<>();
		
		treeKeys.addAll(Sets.newTreeSet(senderKeys));
		treeKeys.addAll(Sets.newTreeSet(recipientKeys));

		Iterator iter = treeKeys.iterator();

		Set<BlExpUnit> txs = new TreeSet<>();
		while ( iter.hasNext() )
		{
			Tuple2<Integer, Integer> request = (Tuple2<Integer, Integer>) iter.next();
			txs.add(new BlExpUnit(request.a, request.b, this.map.get(request)));
		}
		
		return txs;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Transaction> getTransactionsByAddress(String address)
	{
		Iterable senderKeys = Fun.filter(this.senderKey, address);
		Iterable recipientKeys = Fun.filter(this.recipientKey, address);

		Set<Tuple2<Integer, Integer>> treeKeys = new TreeSet<>();
		
		treeKeys.addAll(Sets.newTreeSet(senderKeys));
		treeKeys.addAll(Sets.newTreeSet(recipientKeys));

		Iterator iter = treeKeys.iterator();

		List<Transaction> txs = new ArrayList<>();
		while ( iter.hasNext() )
		{
			txs.add(this.map.get(iter.next()));
		}
		
		return txs;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int getTransactionsByAddressCount(String address)
	{
		Iterable senderKeys = Fun.filter(this.senderKey, address);
		Iterable recipientKeys = Fun.filter(this.recipientKey, address);
		
		Set<Tuple2<Integer, Integer>> treeKeys = new TreeSet<>();
		
		treeKeys.addAll(Sets.newTreeSet(senderKeys));
		treeKeys.addAll(Sets.newTreeSet(recipientKeys));
		
		return treeKeys.size(); 	
	}	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Tuple2<Integer, Integer> getTransactionsAfterTimestamp(int startHeight, int numOfTx,
			String address) {
		Iterable keys = Fun.filter(this.recipientKey, address);
		Iterator iter = keys.iterator();
		int prevKey = startHeight;
		while ( iter.hasNext() )
		{
			Tuple2<Integer, Integer> key = (Tuple2<Integer, Integer>) iter.next();
			if ( key.a >= startHeight )
			{
					if (key.a != prevKey)
					{
						numOfTx = 0;
					}
					prevKey = key.a;
					if ( key.b > numOfTx)
						return key;
			}
		}
		
		return null;
	}

	public DCMap<Tuple2<Integer, Integer>, Transaction> getParent() {
		return this.parent;
	}
	
	
	@SuppressWarnings("rawtypes")
	public List<Transaction> findTransactions(String address, String sender, String recipient, 
			final int minHeight, final int maxHeight,
			int type, int service,
			boolean desc, int offset, int limit)
	{
		Iterable keys = findTransactionsKeys(address, sender, recipient, minHeight, maxHeight, 
				type, service, desc, offset, limit);
		
		Iterator iter = keys.iterator();

		List<Transaction> txs = new ArrayList<>();
		
		while ( iter.hasNext() )
		{
			txs.add(this.map.get(iter.next()));
		}
		
		return txs;
	}
	
	@SuppressWarnings("rawtypes")
	public int findTransactionsCount(String address, String sender, String recipient, 
			final int minHeight, final int maxHeight,
			int type, int service,
			boolean desc, int offset, int limit)
	{
		Iterable keys = findTransactionsKeys(address, sender, recipient, minHeight, maxHeight, 
				type, service, desc, offset, limit);
		return Iterables.size(keys);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Iterable findTransactionsKeys(String address, String sender, String recipient, 
			final int minHeight, final int maxHeight,
			int type, final int service,
			boolean desc, int offset, int limit)
	{
		Iterable senderKeys = null;
		Iterable recipientKeys = null;
		Set<Tuple2<Integer, Integer>> treeKeys = new TreeSet<>();
		
		if (address != null) {
			sender = address;
			recipient = address;
		}
		
		if (sender == null && recipient == null) {
			return treeKeys;
		}

		if (sender != null)
		{
			if(type > 0) {
				senderKeys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(sender, type));
			} else {
				senderKeys = Fun.filter(this.senderKey, sender);
			}
		}
		
		if (recipient != null)
		{
			if(type > 0) {
				recipientKeys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(recipient, type));
			} else {
				recipientKeys = Fun.filter(this.recipientKey, recipient);
			}
		}
		
		if (address != null) {
			treeKeys.addAll(Sets.newTreeSet(senderKeys));
			treeKeys.addAll(Sets.newTreeSet(recipientKeys));
		} else if (sender != null && recipient != null)	{
			treeKeys.addAll(Sets.newTreeSet(senderKeys));
			treeKeys.retainAll(Sets.newTreeSet(recipientKeys));
		} else if (sender != null) {
			treeKeys.addAll(Sets.newTreeSet(senderKeys));
		} else if (recipient != null) {
			treeKeys.addAll(Sets.newTreeSet(recipientKeys));
		}
		
		if( minHeight != 0 || maxHeight != 0 )
		{
			treeKeys = Sets.filter(treeKeys, new Predicate<Tuple2<Integer, Integer>>() {
		        @Override public boolean apply(Tuple2<Integer, Integer> key) {
		            return (minHeight == 0 || key.a >= minHeight) && (maxHeight == 0 || key.a <= maxHeight);
		        }               
		    });
		}
		
		if(type == Transaction.ARBITRARY_TRANSACTION && service > -1) {
			treeKeys = Sets.filter(treeKeys, new Predicate<Tuple2<Integer, Integer>>() {
		        @Override public boolean apply(Tuple2<Integer, Integer> key) {
		        	ArbitraryTransaction tx = (ArbitraryTransaction) map.get(key);
		        	return tx.getService() == service;
		        }               
		    });	
		}
		
		Iterable keys;
		if (desc) {
			keys = ((TreeSet)treeKeys).descendingSet();
		} else {
			keys = treeKeys;
		}
		
		limit = ( limit == 0 ) ? Iterables.size(keys) : limit;
		
		return Iterables.limit(Iterables.skip(keys, offset), limit);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public byte[] getSignature(int hight, int seg){
		
		 Iterator<Tuple2<String, Tuple2<Integer, Integer>>> it = signature_key.iterator();
		while (it.hasNext()){
			 Tuple2<String, Tuple2<Integer, Integer>> a = it.next();
			 if(a.b.equals(new Tuple2(hight,seg))) return Base58.decode(a.a);
					
		}
		return null;
	}
	public Tuple2<Integer,Integer> getHeightSegBySignature(byte[] sign){
		
		int dd = signature_key2.size();
		
		if (signature_key2.containsKey(sign)) {
			return signature_key2.get(sign);
		} else {
			return null;
		}
					
	}
	public Transaction getTransaction(byte[] signature) {
		
		return this.get(this.getHeightSegBySignature(signature));
		
	}

}
