package datachain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;

import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import core.BlockChain;
import core.naming.NameSale;
import database.DBMap;
import utils.ObserverMessage;
import utils.ReverseComparator;

public class NameExchangeMap extends DCMap<String, BigDecimal>
{
	public static final int AMOUNT_INDEX = 1;

	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	public NameExchangeMap(DCSet databaseSet, DB database)
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

	public NameExchangeMap(NameExchangeMap parent)
	{
		super(parent, null);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database)
	{
		//AMOUNT INDEX
		NavigableSet<Tuple2<BigDecimal, String>> namesIndex = database.createTreeSet("namesales_index_amount")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();

		NavigableSet<Tuple2<BigDecimal, String>> descendingNamesIndex = database.createTreeSet("namesales_index_amount_descending")
				.comparator(new ReverseComparator(Fun.COMPARATOR))
				.makeOrGet();

		createIndex(AMOUNT_INDEX, namesIndex, descendingNamesIndex, new Fun.Function2<BigDecimal, String, BigDecimal>() {
			@Override
			public BigDecimal run(String key, BigDecimal value) {
				return value;
			}
		});
	}

	@Override
	protected Map<String, BigDecimal> getMap(DB database)
	{
		//OPEN MAP
		return database.createTreeMap("namesales")
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<String, BigDecimal> getMemoryMap()
	{
		return new HashMap<String, BigDecimal>();
	}

	@Override
	protected BigDecimal getDefaultValue()
	{
		return BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
	}

	@Override
	protected Map<Integer, Integer> getObservableData()
	{
		return this.observableData;
	}

	public List<NameSale> getNameSales()
	{
		List<NameSale> nameSales = new ArrayList<NameSale>();

		for(Entry<String, BigDecimal> entry: this.map.entrySet())
		{
			nameSales.add(new NameSale(entry.getKey(), entry.getValue()));
		}

		if(this.parent != null) {

			//GET ALL KEYS FOR FORK
			List<NameSale> forkItems = this.parent.getDCSet().getNameExchangeMap().getNameSales();

			nameSales.addAll(forkItems);

			if (this.deleted != null) {
				//DELETE DELETED
				for(String deleted: this.deleted)
				{
					nameSales.remove(deleted);
				}

			}
		}

		return nameSales;
	}

	public NameSale getNameSale(String nameName)
	{
		return new NameSale(nameName, this.get(nameName));
	}

	public void add(NameSale nameSale)
	{
		this.set(nameSale.getKey(), nameSale.getAmount());
	}

	public boolean contains(NameSale nameSale)
	{
		return this.contains(nameSale.getKey());
	}

	public void delete(NameSale nameSale)
	{
		this.delete(nameSale.getKey());
	}
}
