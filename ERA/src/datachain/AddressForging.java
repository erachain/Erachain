package datachain;

import java.util.HashMap;
//import java.util.List;
import java.util.Map;
//import java.util.TreeMap;
//import java.util.TreeSet;

import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple4;
//import org.mapdb.Fun.Tuple3;

//
// account.address + current block.Height ->
//   -> previous making blockHeight + this ForgingH balance + this Target + this Win Value
// last forged block for ADDRESS -> by height = 0
public class AddressForging extends DCMap<Tuple2<String, Integer>, Tuple4<Integer, Integer, Long, Long>>
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

	protected Map<Tuple2<String, Integer>, Tuple4<Integer, Integer, Long, Long>> getMap(DB database)
	{
		//OPEN MAP
		return database.getTreeMap("address_forging");
	}

	@Override
	protected Map<Tuple2<String, Integer>, Tuple4<Integer, Integer, Long, Long>> getMemoryMap()
	{
		return new HashMap<Tuple2<String, Integer>, Tuple4<Integer, Integer, Long, Long>>();
	}

	@Override
	protected Tuple4<Integer, Integer, Long, Long> getDefaultValue()
	{
		return null; //new Tuple2<Integer, Integer>(-1, 0);
	}

	@Override
	protected Map<Integer, Integer> getObservableData()
	{
		return this.observableData;
	}

	public Tuple4<Integer, Integer, Long, Long> get(String address, int height)
	{
		Tuple4<Integer, Integer, Long, Long> point = this.get(new Tuple2<String, Integer>(address, height));
		if (point == null)
			this.get(new Tuple2<String, Integer>(address, 0));

		return point;

	}


	// height
	public void set(String address, Tuple4<Integer, Integer, Long, Long> currentPoint)
	{

		int height = currentPoint.a;
		Tuple4<Integer, Integer, Long, Long> previousPoint = this.getLast(address);
		if (previousPoint != null && height > previousPoint.a) {
			this.set(new Tuple2<String, Integer>(address, height), previousPoint);
		}

		this.setLast(address, currentPoint);

	}

	public void delete(String address, int height)
	{

		if (height < 3) {
			// not delete GENESIS forging data for all accounts
			return;
		}
		Tuple2<String, Integer> key = new Tuple2<String, Integer>(address, height);
		Tuple4<Integer, Integer, Long, Long> previous = this.get(key);
		this.delete(key);
		this.setLast(address, previous);

	}
	public Tuple4<Integer, Integer, Long, Long> getLast(String address)
	{
		return this.get(new Tuple2<String, Integer>(address, 0));
	}
	private void setLast(String address, Tuple4<Integer, Integer, Long, Long> point)
	{
		this.set(new Tuple2<String, Integer>(address, 0), point);
	}
}
