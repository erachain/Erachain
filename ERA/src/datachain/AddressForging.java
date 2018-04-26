package datachain;

import java.util.HashMap;
//import java.util.List;
import java.util.Map;
//import java.util.TreeMap;
//import java.util.TreeSet;

import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
//import org.mapdb.Fun.Tuple3;

//
// account.address + current block.Height ->
//   -> last making blockHeight + ForgingH balance
// last forged block for ADDRESS -> by height = 0
public class AddressForging extends DCMap<Tuple2<String, Integer>, Tuple2<Integer, Long>>
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();


	public AddressForging(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public AddressForging(AddressForging parent)
	{
		super(parent, null);
	}

	@Override
	protected void createIndexes(DB database){}

	@Override

	protected Map<Tuple2<String, Integer>, Tuple2<Integer, Long>> getMap(DB database)
	{
		//OPEN MAP
		return database.getTreeMap("address_forging");
	}

	@Override
	protected Map<Tuple2<String, Integer>, Tuple2<Integer, Long>> getMemoryMap()
	{
		return new HashMap<Tuple2<String, Integer>, Tuple2<Integer, Long>>();
	}

	@Override
	protected Tuple2<Integer, Long> getDefaultValue()
	{
		return new Tuple2<Integer, Long>(-1, 0l);
	}

	@Override
	protected Map<Integer, Integer> getObservableData()
	{
		return this.observableData;
	}

	public Tuple2<Integer, Long> get(String address, int height)
	{
		return this.get(new Tuple2<String, Integer>(address, height));
	}

	private void set(String address, int height, int previosHeight, long forgingBalance)
	{

		if (height > previosHeight) {
			this.set(new Tuple2<String, Integer>(address, height),
					new Tuple2<Integer, Long>(previosHeight, forgingBalance));
		}

		this.setLast(address, new previousPoint height);

	}
	public void set(String address, int height, long forgingBalance)
	{

		Tuple2<Integer, Long> previous = this.getLast(address);
		int previosHeight = previous.a;
		this.set(address, height, previosHeight, forgingBalance);

	}

	public void delete(String address, int height)
	{

		if (height < 3) {
			// not delete GENESIS forging data for all accounts
			return;
		}
		Tuple2<String, Integer> key = new Tuple2<String, Integer>(address, height);
		Tuple2<Integer, Long> previous = this.get(key);
		int prevHeight = previous.a;
		this.delete(key);
		this.setLast(address, previous);

	}
	public Tuple2<Integer, Long> getLast(String address)
	{
		return this.get(new Tuple2<String, Integer>(address, 0));
	}
	private void setLast(String address, Tuple2<Integer, Long> previousPoint)
	{
		this.set(new Tuple2<String, Integer>(address, 0), previousPoint);
	}
}
