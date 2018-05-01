package datachain;



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mapdb.Atomic;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

/*
 *  Block Height ->
 *  BLOCK HEAD:
 *  + FACE - version, creator, signature, transactionCount, transactionsHash
 *  + parentSignature
 *  + Forging Data - Forging Value, Win Value, Target Value
 *
 */
public class BlocksHeadsMap extends DCMap<Integer, Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>>>
{

	protected Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	static Logger LOGGER = Logger.getLogger(BlocksHeadsMap.class.getName());

	static final String NAME = "blocks_heads";

	// for saving in DB
	private Atomic.Long fullWeightVar;
	private Long fullWeight = 0L;
	private int startedInForkHeight = 0;


	public BlocksHeadsMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);

		this.fullWeightVar = database.getAtomicLong("fullWeight");
		this.fullWeight = this.fullWeightVar.get();
		if (this.fullWeight == null)
			this.fullWeight = 0L;

	}

	public BlocksHeadsMap(BlocksHeadsMap parent, DCSet dcSet)
	{
		super(parent, dcSet);

		this.fullWeight = parent.getFullWeight();

	}

	@Override
	protected Map<Integer, Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>>> getMap(DB database)
	{
		//OPEN MAP
		return database.createTreeMap(NAME)
				.keySerializer(BTreeKeySerializer.BASIC)
				.counterEnable()
				.makeOrGet();
	}

	/*
	 *  NEED .counterEnable in MAP(non-Javadoc)
	 * @see datachain.DCMap#size()
	@Override
	public int size() {
		return this.key;
	}
	 */

	@Override
	protected void createIndexes(DB database) {
	}

	@Override
	protected Map<Integer, Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>>> getMemoryMap() {
		return new HashMap<Integer, Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>>>();
	}

	@Override
	protected Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>> getDefaultValue() {
		return null;
	}

	@Override
	protected Map<Integer, Integer> getObservableData() {
		return this.observableData;
	}

	public Long getFullWeight() {
		return this.fullWeight;
	}

	public int getStartedInForkHeight() {
		return this.startedInForkHeight;
	}

	public void recalcWeightFull(DCSet dcSet) {

		long weightFull = 0l;
		Iterator<Integer> iterator = this.getIterator(0, true);
		while (iterator.hasNext()) {
			Integer key = iterator.next();
			Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>> item = this.get(key);
			weightFull += item.c.c;
		}

		fullWeight = weightFull;
		this.fullWeightVar.set(fullWeight);

	}

	public boolean set(Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>> item) {

		int key = this.size() + 1;
		int height = item.c.a;
		long weight = item.c.c;

		if (startedInForkHeight == 0 && this.parent != null) {
			startedInForkHeight = height;
		}

		fullWeight += weight;

		if(this.fullWeightVar != null)
		{
			this.fullWeightVar.set(fullWeight);
		}

		// INSERT WITH NEW KEY
		return super.set(key, item);

	}

	public int add(Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>> item) {

		int key = this.size() + 1;

		// INSERT WITH NEW KEY
		this.set(key, item);

		// RETURN KEY
		return key;
	}

	public Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>> last() {
		return this.get(this.size());
	}

	public void remove() {

		int key = this.size();
		if (this.contains(key)) {
			// sub old value from FULL
			Tuple3<Tuple5<Integer, byte[], byte[], Integer, byte[]>, byte[], Tuple3<Integer, Long, Long>> value_old = this.get(key);
			fullWeight -= value_old.c.c;

			if(this.fullWeightVar != null)
			{
				this.fullWeightVar.set(fullWeight);
			}
			super.delete(key);
		}
	}


}