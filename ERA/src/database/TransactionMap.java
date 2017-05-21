package database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import ntp.NTP;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;

import com.google.common.primitives.UnsignedBytes;

import core.transaction.Transaction;
import utils.ObserverMessage;
import utils.ReverseComparator;
import database.DBSet;
import database.serializer.TransactionSerializer;
import network.Peer;

// memory pool for unconfirmed transaction
// tx.signature -> <<broadcasted peers>, transaction>
// ++ seek by TIMESTAMP
// тут надо запминать каким пирам мы уже разослали транзакцию неподтвержденную
// так что бы при подключении делать автоматически broadcast
public class TransactionMap extends DBMap<byte[], Tuple2<List<byte[]>, Transaction>> implements Observer
{
	public static final int TIMESTAMP_INDEX = 1;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	private Map<byte[]> peersBroadcasted = new Map<byte[]>();
	
	public TransactionMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_TRANSACTION_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRANSACTION_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_TRANSACTION_TYPE);
	}

	public TransactionMap(TransactionMap parent) 
	{
		super(parent, null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database)
	{
		//TIMESTAMP INDEX
		Tuple2Comparator<Long, byte[]> comparator = new Fun.Tuple2Comparator<Long, byte[]>(Fun.COMPARATOR, UnsignedBytes.lexicographicalComparator());
		NavigableSet<Tuple2<Integer, byte[]>> heightIndex = database.createTreeSet("transactions_index_timestamp")
				.comparator(comparator)
				.makeOrGet();
				
		NavigableSet<Tuple2<Integer, byte[]>> descendingHeightIndex = database.createTreeSet("transactions_index_timestamp_descending")
				.comparator(new ReverseComparator(comparator))
				.makeOrGet();
				
		createIndex(TIMESTAMP_INDEX, heightIndex, descendingHeightIndex, new Fun.Function2<Long, byte[], Tuple2<List<byte[]>, Transaction>>() {
		   	@Override
		    public Long run(byte[] key, Tuple2<List<byte[]>, Transaction> value) {
		   		return value.b.getTimestamp();
		    }
		});
	}

	@Override
	protected Map<byte[], Tuple2<List<byte[]>, Transaction>> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("transactions")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.valueSerializer(new TransactionSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<byte[], Tuple2<List<byte[]>, Transaction>> getMemoryMap() 
	{
		return new TreeMap<byte[], Tuple2<List<byte[]>, Transaction>>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected Tuple2<List<byte[]>, Transaction> getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	@Override
	public void update(Observable o, Object arg) 
	{	
		ObserverMessage message = (ObserverMessage) arg;
		
		//ON NEW BLOCK
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE)
		{			
			//CLEAN UP
			for(Tuple2<List<byte[]>, Transaction> item: this.getValues())
			{
				//CHECK IF DEADLINE PASSED
				if(item.b.getDeadline() < NTP.getTime())
				{
					this.delete(item.b.getSignature());
					
					//NOTIFY
					this.setChanged();
					this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_TRANSACTION_TYPE, item));
				}
			}
		}
	}

	public void add(Transaction transaction) {
		
		Tuple2<List<byte[]>, Transaction> item = new Tuple2<List<byte[]>, Transaction>(new ArrayList<byte[]>(), transaction);
		this.set(transaction.getSignature(), item);
		
	}

	// ADD broadcasted PEER
	public void addBroadcastedPeer(Transaction transaction, byte[] newPeer) {
		
		Tuple2<List<byte[]>, Transaction> item = this.get(transaction.getSignature());
		for (byte[] peer: item.a) {
			if (Arrays.equals(peer, newPeer))
				return;
		}

		if (item.a.add(newPeer)) {
			Tuple2<List<byte[]>, Transaction>newItem = new Tuple2<List<byte[]>, Transaction>(item.a, item.b); 
			this.set(item.b.getSignature(), newItem);
		}
	}

	public List<Tuple2<List<byte[]>, Transaction>> getTransactions() {
		return new ArrayList<Tuple2<List<byte[]>, Transaction>>(this.getValues());
	}

	// HOW many PEERS broadcasted by this TRANSACTION
	public int getBroadcasts(Transaction transaction) {
		Tuple2<List<byte[]>, Transaction> item = this.get(transaction.getSignature());
		if (item.a == null)
			return 0;
		
		return item.a.size();
	}
	
	
	public void delete(Transaction transaction) {
		this.delete(transaction.getSignature());
	}

	public boolean contains(Transaction transaction) {
		return this.contains(transaction.getSignature());
	}
}
