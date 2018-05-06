package database.wallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import core.account.Account;
import core.block.Block;
import core.item.ItemCls;
import core.naming.Name;

import org.mapdb.BTreeMap;
import org.mapdb.Bind;

import utils.Converter;
import utils.ObserverMessage;
import utils.Pair;
import utils.ReverseComparator;
import database.DBMap;
import database.serializer.NameSerializer;
import datachain.DCMap;

// <Account, Tuple2<Title,JSON_String>>
public class AccountsPropertisMap extends DCMap<String, Tuple2<String, String>> {
	public static final int NAME_INDEX = 1;
	public static final int OWNER_INDEX = 2;

	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	BTreeMap<String, String> titleIndex;
	static Logger LOGGER = Logger.getLogger(AccountsPropertisMap.class.getName());

	public AccountsPropertisMap(DWSet dWSet, DB database) {
		super(dWSet, database);

		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_ACCOUNT_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_ACCOUNT_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ALL_ACCOUNT_TYPE);
	}

	public AccountsPropertisMap(AccountsPropertisMap parent) {
		super(parent, null);
	}

	@Override
	protected Map<String, Tuple2<String, String>> getMap(DB database) {
		// OPEN MAP
		return database.createTreeMap("accounts_propertis_map")
				//.keySerializer(BTreeKeySerializer.STRING)
				//.valueSerializer(new NameSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Tuple2<String, String>> getMemoryMap() {
		return new TreeMap<String, Tuple2<String, String>>(Fun.COMPARATOR);
	}

	@Override
	protected Tuple2<String, String> getDefaultValue() {
		return null;
	}

	@Override
	protected Map<Integer, Integer> getObservableData() {
		return this.observableData;
	}

	// get list items in name substring str
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Tuple2<String, String>> get_By_Name(String str, boolean caseCharacter) {
		Map<String, Tuple2<String, String>> txs = new TreeMap<String, Tuple2<String, String>>();
		// if (str == null || str.length() < 3)
		// return null;

		Iterator<Pair<String, Tuple2<String, String>>> it = this.getList().iterator();
		while (it.hasNext()) {
			Pair<String, Tuple2<String, String>> a = it.next();
			String s1 = a.getB().a;
			if (!caseCharacter) {
				s1 = s1.toLowerCase();
				str = str.toLowerCase();
			}
			if (s1.contains(str))
				txs.put(a.getA(), a.getB());
		}

		return txs;
	}

	@Override
	protected void createIndexes(DB database) {
		// TODO Auto-generated method stub

	}
}
