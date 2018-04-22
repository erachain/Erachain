package datachain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import com.google.common.primitives.UnsignedBytes;

import core.transaction.CancelOrderTransaction;
import database.DBMap;
import utils.ObserverMessage;

public class CancelOrderMap extends DCMap<byte[], Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
Tuple3<Long, BigDecimal, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>>>
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	public CancelOrderMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);

		if (databaseSet.isWithObserver()) {
			this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_CANCEL_ORDER_TYPE);
			if (databaseSet.isDynamicGUI()) {
				this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_CANCEL_ORDER_TYPE);
				this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_CANCEL_ORDER_TYPE);
			}
			this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_CANCEL_ORDER_TYPE);
		}

	}

	public CancelOrderMap(CancelOrderMap parent)
	{
		super(parent, null);
	}

	@Override
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>>> getMap(DB database)
	{
		//OPEN MAP
		return database.createTreeMap("cancelOrderOrphanData")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				//.valueSerializer(new OrderSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>>> getMemoryMap()
	{
		return new TreeMap<byte[], Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>>>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>> getDefaultValue()
	{
		return null;
	}

	@Override
	protected Map<Integer, Integer> getObservableData()
	{
		return this.observableData;
	}

	public void delete(CancelOrderTransaction transaction) {
		this.delete(transaction.getSignature());
	}

	public Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>> get(CancelOrderTransaction transaction)
	{
		return this.get(transaction.getSignature());
	}

	public void set(CancelOrderTransaction transaction, Tuple3<Tuple4<BigInteger, String, Long, Boolean>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>> value)
	{
		this.set(transaction.getSignature(), value);
	}
}
