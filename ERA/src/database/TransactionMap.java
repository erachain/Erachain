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

import core.payment.Payment;
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
public class TransactionMap extends DBMap<byte[],  Transaction> implements Observer
{
	public static final int TIMESTAMP_INDEX = 1;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	// PEERS for transaction signature
	private Map<byte[], List<byte[]>> peersBroadcasted = new HashMap<byte[], List<byte[]>>();

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
				
		createIndex(TIMESTAMP_INDEX, heightIndex, descendingHeightIndex, new Fun.Function2<Long, byte[],  Transaction>() {
		   	@Override
		    public Long run(byte[] key,  Transaction value) {
		   		return value.getTimestamp();
		    }
		});
	}

	@Override
	protected Map<byte[],  Transaction> getMap(DB database) 
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
	protected Map<byte[],  Transaction> getMemoryMap() 
	{
		return new TreeMap<byte[],  Transaction>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected  Transaction getDefaultValue() 
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
			for( Transaction item: this.getValues())
			{
				//CHECK IF DEADLINE PASSED
				if(item.getDeadline() < NTP.getTime())
				{
					this.delete(item.getSignature());
					
					//NOTIFY
					this.setChanged();
					this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_TRANSACTION_TYPE, item));
				}
			}
		}
	}

	public void add(Transaction transaction) {
		
		this.set(transaction.getSignature(), transaction);
		
	}

	// ADD broadcasted PEER
	public void addBroadcastedPeer(Transaction transaction, byte[] peer) {
		
		byte[] signature = transaction.getSignature();
		
		List<byte[]> peers;
		if (!this.peersBroadcasted.containsKey(signature)) {
			peers = new ArrayList<byte[]>();
		} else {		
			peers = this.peersBroadcasted.get(signature);
			if (peers == null)
				peers = new ArrayList<byte[]>();
		}

		if (peers.add(peer))
			this.peersBroadcasted.put(signature, peers);
	}

	public List< Transaction> getTransactions() {
		return new ArrayList< Transaction>(this.getValues());
	}

	// HOW many PEERS broadcasted by this TRANSACTION
	public int getBroadcasts(Transaction transaction) {
		
		byte[] signature = transaction.getSignature();
		if (!this.peersBroadcasted.containsKey(signature)) {
			return 0;
		} else {		
			List<byte[]> peers = this.peersBroadcasted.get(signature);
			if (peers == null || peers.isEmpty())
				return 0;
			
			return peers.size();
		}
		
	}

	// HOW many PEERS broadcasted by this TRANSACTION
	public List<byte[]> getBroadcastedPeers(Transaction transaction) {
		
		byte[] signature = transaction.getSignature();
		if (!this.peersBroadcasted.containsKey(signature)) {
			return new ArrayList<byte[]>();
		} else {		
			return this.peersBroadcasted.get(signature);
		}
		
	}

	// is this TRANSACTION is broadcasted to this PEER 
	public boolean isBroadcastedToPeer(Transaction transaction, byte[] peer) {
		
		byte[] signature = transaction.getSignature();
		if (!this.peersBroadcasted.containsKey(signature)) {
			return false;
		} else {	
			return this.peersBroadcasted.get(signature).contains(peer);
		}
		
	}
	
	public void delete(Transaction transaction) {
		this.delete(transaction.getSignature());
	}

	public boolean contains(Transaction transaction) {
		return this.contains(transaction.getSignature());
	}
}
