package org.erachain.database.wallet;

import org.erachain.core.account.Account;
import org.erachain.core.naming.Name;
import org.erachain.database.serializer.NameSerializer;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DCUMapImpl;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.erachain.utils.ReverseComparator;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NameMap extends DCUMapImpl<Tuple2<String, String>, Name> {
    public static final int NAME_INDEX = 1;
    public static final int OWNER_INDEX = 2;
    static Logger LOGGER = LoggerFactory.getLogger(NameMap.class.getName());

    public NameMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.WALLET_ADD_NAME_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.WALLET_LIST_NAME_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.WALLET_ADD_NAME_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.WALLET_REMOVE_NAME_TYPE);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
        //NAME INDEX
        NavigableSet<Tuple2<String, Tuple2<String, String>>> nameIndex = database.createTreeSet("names_index_name")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingNameIndex = database.createTreeSet("names_index_name_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(NAME_INDEX, nameIndex, descendingNameIndex, new Fun.Function2<String, Tuple2<String, String>, Name>() {
            @Override
            public String run(Tuple2<String, String> key, Name value) {
                return value.getName();
            }
        });

        //OWNER INDEX
        NavigableSet<Tuple2<String, Tuple2<String, String>>> ownerIndex = database.createTreeSet("names_index_owner")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingOwnerIndex = database.createTreeSet("names_index_owner_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(OWNER_INDEX, ownerIndex, descendingOwnerIndex, new Fun.Function2<String, Tuple2<String, String>, Name>() {
            @Override
            public String run(Tuple2<String, String> key, Name value) {
                return value.getOwner().getAddress();
            }
        });
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap("names")
                .keySerializer(BTreeKeySerializer.TUPLE2)
                .valueSerializer(new NameSerializer())
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<Tuple2<String, String>, Name>(Fun.TUPLE2_COMPARATOR);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Name> get(Account account) {
        List<Name> names = new ArrayList<Name>();

        try {
            Map<Tuple2<String, String>, Name> accountNames = ((BTreeMap) this.map).subMap(
                    Fun.t2(account.getAddress(), null),
                    Fun.t2(account.getAddress(), Fun.HI()));

            //GET ITERATOR
            Iterator<Name> iterator = accountNames.values().iterator();

            while (iterator.hasNext()) {
                names.add(iterator.next());
            }
        } catch (Exception e) {
            //ERROR
            LOGGER.error(e.getMessage(), e);
        }

        return names;
    }

    public List<Pair<Account, Name>> get(List<Account> accounts) {
        List<Pair<Account, Name>> names = new ArrayList<Pair<Account, Name>>();

        try {
            //FOR EACH ACCOUNTS
            synchronized (accounts) {
                for (Account account : accounts) {
                    List<Name> accountNames = get(account);
                    for (Name name : accountNames) {
                        names.add(new Pair<Account, Name>(account, name));
                    }
                }
            }
        } catch (Exception e) {
            //ERROR
            LOGGER.error(e.getMessage(), e);
        }

        return names;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Account account) {
        //GET ALL NAMES THAT BELONG TO THAT ADDRESS
        Map<Tuple2<String, String>, Name> accountNames = ((BTreeMap) this.map).subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI()));

        //DELETE NAMES
        for (Tuple2<String, String> key : accountNames.keySet()) {
            this.delete(key);
        }
    }

    public void delete(Name name) {
        this.delete(name.getOwner(), name);
    }

    public void delete(Account account, Name name) {
        this.delete(new Tuple2<String, String>(account.getAddress(), name.getName()));
    }

    public void deleteAll(List<Account> accounts) {
        for (Account account : accounts) {
            this.delete(account);
        }
    }

    public boolean add(Name name) {
        return this.set(new Tuple2<String, String>(name.getOwner().getAddress(), name.getName()), name);
    }

    public void addAll(Map<Account, List<Name>> names) {
        //FOR EACH ACCOUNT
        for (Account account : names.keySet()) {
            //FOR EACH TRANSACTION
            for (Name name : names.get(account)) {
                this.add(name);
            }
        }
    }
}
