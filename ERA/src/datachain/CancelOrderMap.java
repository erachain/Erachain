package datachain;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import core.item.assets.Order;
import core.transaction.CancelOrderTransaction;
import database.DBMap;
import database.serializer.OrderSerializer;
import datachain.DCSet;
import utils.ObserverMessage;

public class CancelOrderMap extends DCMap<byte[], Order> 
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
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], Order> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("cancelOrderOrphanData")
				.keySerializer(BTreeKeySerializer.BASIC)
				.valueSerializer(new OrderSerializer())
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], Order> getMemoryMap() 
	{
		return new TreeMap<byte[], Order>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected Order getDefaultValue() 
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
	
	public Order get(CancelOrderTransaction transaction)
	{
		return this.get(transaction.getSignature());
	}
	
	public void set(CancelOrderTransaction transaction, Order value)
	{
		this.set(transaction.getSignature(), value);
	}
}
