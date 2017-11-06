package datachain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import core.transaction.CancelSellNameTransaction;
import database.DBMap;
import datachain.DCSet;
import utils.ObserverMessage;

public class CancelSellNameMap extends DCMap<byte[], BigDecimal> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public CancelSellNameMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);

		if (databaseSet.isWithObserver()) {
			this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_NAME_SALE_TYPE);
			if (databaseSet.isDynamicGUI()) {
				this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_NAME_SALE_TYPE);
				this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_NAME_SALE_TYPE);
			}
			this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_NAME_SALE_TYPE);
		}
	}

	public CancelSellNameMap(CancelSellNameMap parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], BigDecimal> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("cancelNameOrphanData")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], BigDecimal> getMemoryMap() 
	{
		return new TreeMap<byte[], BigDecimal>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected BigDecimal getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	public void delete(CancelSellNameTransaction transaction) {
		this.delete(transaction.getSignature());
	}
	
	public BigDecimal get(CancelSellNameTransaction transaction)
	{
		return this.get(transaction.getSignature());
	}
	
	public void set(CancelSellNameTransaction transaction, BigDecimal value)
	{
		this.set(transaction.getSignature(), value);
	}
}
