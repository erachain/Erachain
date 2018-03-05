package datachain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;
import java.util.WeakHashMap;

import ntp.NTP;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Atomic.Var;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;

import com.google.common.primitives.UnsignedBytes;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.transaction.Transaction;
import utils.ObserverMessage;
import utils.ReverseComparator;
import database.serializer.TransactionSerializer;
import database.DBMap;

// memory pool for unconfirmed transaction
// tx.signature -> <<broadcasted peers>, transaction>
// ++ seek by TIMESTAMP
// тут надо запминать каким пирам мы уже разослали транзакцию неподтвержденную
// так что бы при подключении делать автоматически broadcast
public class TransactionMap extends DCMap<byte[], Transaction> implements Observer {
	public static final int TIMESTAMP_INDEX = 1;
	public static final int MAX_MAP_SIZE = core.BlockChain.HARD_WORK ? 100000 : 5000;

	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	private NavigableSet<Tuple2<Integer, byte[]>> heightIndex;

	// PEERS for transaction signature
	private Map<byte[], List<byte[]>> peersBroadcasted = new HashMap<byte[], List<byte[]>>();

	public TransactionMap(DCSet databaseSet, DB database) {
		super(databaseSet, database);

		if (databaseSet.isWithObserver()) {
			if (databaseSet.isDynamicGUI()) {
				this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_UNC_TRANSACTION_TYPE);
				this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_UNC_TRANSACTION_TYPE);
				this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE);
				this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_UNC_TRANSACTION_TYPE);
			} else {
				this.observableData.put(DBMap.NOTIFY_COUNT, ObserverMessage.COUNT_UNC_TRANSACTION_TYPE);
			}
		}

	}

	public TransactionMap(TransactionMap parent, DCSet dcSet) {
		super(parent, dcSet);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database) {
		// TIMESTAMP INDEX
		Tuple2Comparator<Long, byte[]> comparator = new Fun.Tuple2Comparator<Long, byte[]>(Fun.COMPARATOR,
				UnsignedBytes.lexicographicalComparator());
		NavigableSet<Tuple2<Integer, byte[]>> heightIndex = database.createTreeSet("transactions_index_timestamp")
				.comparator(comparator).makeOrGet();

		NavigableSet<Tuple2<Integer, byte[]>> descendingHeightIndex = database
				.createTreeSet("transactions_index_timestamp_descending").comparator(new ReverseComparator(comparator))
				.makeOrGet();

		createIndex(TIMESTAMP_INDEX, heightIndex, descendingHeightIndex,
				new Fun.Function2<Long, byte[], Transaction>() {
					@Override
					public Long run(byte[] key, Transaction value) {
						return value.getTimestamp();
					}
				});
	}

	public Integer deleteObservableData(int index) {
		return this.observableData.remove(index);
	}

	public Integer setObservableData(int index, Integer data) {
		return this.observableData.put(index, data);
	}

	@Override
	protected Map<byte[], Transaction> getMap(DB database) {
		// OPEN MAP
		return database.createTreeMap("transactions").keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator()).valueSerializer(new TransactionSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<byte[], Transaction> getMemoryMap() {
		return new TreeMap<byte[], Transaction>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected Transaction getDefaultValue() {
		return null;
	}

	@Override
	protected Map<Integer, Integer> getObservableData() {
		return this.observableData;
	}

	public List<Transaction> getSubSet(long timestamp) {

		List<Transaction> values = new ArrayList<Transaction>();
		Iterator<byte[]> iterator = this.getIterator(0, false);
		Transaction transaction;
		int count = 0;
		int bytesTotal = 0;
		while (iterator.hasNext()) {
			byte[] key = iterator.next();
			transaction = this.map.get(key);
			if (transaction.getDeadline() < timestamp || transaction.getTimestamp() > timestamp)
				continue;

			bytesTotal += transaction.getDataLength(false);
			if (bytesTotal > core.BlockGenerator.MAX_BLOCK_SIZE_BYTE + (core.BlockGenerator.MAX_BLOCK_SIZE_BYTE >> 3)) {
				break;
			}

			if (count++ > 25222)
				break;

			values.add(transaction);

		}

		return values;
	}

	public void clear(long timestamp) {

		Iterator<byte[]> iterator = this.getIterator(0, false);
		Transaction transaction;
		while (iterator.hasNext()) {
			byte[] key = iterator.next();
			transaction = this.map.get(key);
			if (transaction.getDeadline() < timestamp) {
				this.delete(key);
			}

		}

	}

	@Override
	public void update(Observable o, Object arg) {
		
		if(true)
			return;
		
		ObserverMessage message = (ObserverMessage) arg;

		// ON NEW BLOCK
		if (message.getType() == ObserverMessage.CHAIN_ADD_BLOCK_TYPE) {

			long dTime = Controller.getInstance().getBlockChain().getTimestamp(DCSet.getInstance());

			Transaction item;
			long start = System.currentTimeMillis();

			int i = 0;

			Iterator<byte[]> iterator = this.getIterator(0, false);
			// CLEAN UP
			while (iterator.hasNext()) {

				byte[] key = iterator.next();
				item = this.get(key);

				// CHECK IF DEADLINE PASSED
				if (i > MAX_MAP_SIZE || item.getDeadline() < dTime) {
					iterator.remove();
					continue;
				}

				i++;

			}
			iterator = null;
			long tickets = System.currentTimeMillis() - start;
			LOGGER.debug("update CLEAR DEADLINE time " + tickets);

		}
	}

	public boolean set(byte[] signature, Transaction transaction) {

		if (this.size() > MAX_MAP_SIZE) {
			Iterator<byte[]> iterator = this.getIterator(0, false);
			Transaction item;
			long dTime = Controller.getInstance().getBlockChain().getTimestamp(DCSet.getInstance());

			do {
				byte[] key = iterator.next();
				item = this.get(key);
				this.delete(key);

			} while (item.getDeadline() < dTime && iterator.hasNext());
		}

		if (this.map.containsKey(signature)) {
			return true;
		}
		
		this.getDCSet().updateUncTxCounter(1);
		
		return super.set(signature, transaction);		

	}

	public boolean add(Transaction transaction) {

		return this.set(transaction.getSignature(), transaction);

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

	public List<Transaction> getTransactions(int from, int count, boolean descending) {

		ArrayList<Transaction> values = new ArrayList<Transaction>();
		TransactionMap map = getDCSet().getTransactionMap();
		Iterator<byte[]> iterator = map.getIterator(from, descending);

		for (int i = 0; i < count; i++) {
			if (!iterator.hasNext())
				break;

			values.add(map.get(iterator.next()));
		}
		iterator = null;
		map = null;
		return values;
	}

	public List<Transaction> getIncomedTransactions(String address) {

		ArrayList<Transaction> values = new ArrayList<Transaction>();
		Iterator<byte[]> iterator = this.getIterator(0, false);
		Account account = new Account(address);
		
		while(iterator.hasNext()) {
			Transaction transaction = map.get(iterator.next());
			HashSet<Account> recipients = transaction.getRecipientAccounts();
			if (recipients == null || recipients.isEmpty())
				continue;
			
			if (recipients.contains(account)) {
				values.add(transaction);
			}
		}
		return values;
	}

	public boolean needBroadcasting(Transaction transaction, byte[] peerBYtes) {

		byte[] signature = transaction.getSignature();
		List<byte[]> peers = this.peersBroadcasted.get(signature);
		if (peers == null || peers.isEmpty()
				|| (!peers.contains(peerBYtes) && peers.size() < 4)) {
			return true;
		}

		return false;

	}

	// HOW many PEERS broadcasted by this TRANSACTION
	public int getBroadcasts(Transaction transaction) {

		byte[] signature = transaction.getSignature();
		List<byte[]> peers = this.peersBroadcasted.get(signature);
		if (peers == null || peers.isEmpty())
			return 0;
		
		return peers.size();

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

	public void delete(byte[] signature) {

		// delete BROADCASTS
		this.peersBroadcasted.remove(signature);

		if (this.contains(signature))
			this.getDCSet().updateUncTxCounter(-1);

		super.delete(signature);
	}

	public void delete(Transaction transaction) {
		this.delete(transaction.getSignature());
	}

	public boolean contains(Transaction transaction) {
		return this.contains(transaction.getSignature());
	}
}
