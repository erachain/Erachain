package datachain;



import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

/*
 *  Block Height ->
 *  + signature
 *  + Block HEAD - creator, parentSignature, transactions Signature
 *  + Forging Data - previous height, this Forging Value, Win Value, Target Value
 *
 */
public class BlocksHeadsMap extends DCMap<Integer, Tuple3<byte[], Tuple3<byte[], byte[], byte[]>, Tuple4<Integer, Integer, Long, Long>>>
{

	protected Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	static Logger LOGGER = Logger.getLogger(AutoIntegerTuple3.class.getName());

	static final String NAME = "blocks_heads";

	public BlocksHeadsMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public BlocksHeadsMap(BlocksHeadsMap parent, DCSet dcSet)
	{
		super(parent, dcSet);
	}

	@Override
	protected Map<Integer, Tuple3<byte[], Tuple3<byte[], byte[], byte[]>, Tuple4<Integer, Integer, Long, Long>>> getMap(DB database)
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
	protected Map<Integer, Tuple3<byte[], Tuple3<byte[], byte[], byte[]>, Tuple4<Integer, Integer, Long, Long>>> getMemoryMap() {
		return new HashMap<Integer, Tuple3<byte[], Tuple3<byte[], byte[], byte[]>, Tuple4<Integer, Integer, Long, Long>>>();
	}

	@Override
	protected Tuple3<byte[], Tuple3<byte[], byte[], byte[]>, Tuple4<Integer, Integer, Long, Long>> getDefaultValue() {
		return null;
	}

	@Override
	protected Map<Integer, Integer> getObservableData() {
		return this.observableData;
	}

	public int add(Tuple3<byte[], Tuple3<byte[], byte[], byte[]>, Tuple4<Integer, Integer, Long, Long>> item) {

		int key = this.size() + 1;

		// INSERT WITH NEW KEY
		this.set(key, item);

		// RETURN KEY
		return key;
	}

	public Tuple3<byte[], Tuple3<byte[], byte[], byte[]>, Tuple4<Integer, Integer, Long, Long>> last() {
		return this.get(this.size());
	}

	public void remove() {
		super.delete(this.size());
	}


}