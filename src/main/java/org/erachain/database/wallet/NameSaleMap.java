package org.erachain.database.wallet;

import org.erachain.core.account.Account;
import org.erachain.core.naming.NameSale;
import org.erachain.dbs.DBMap;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

public class NameSaleMap extends DCUMapImpl<Tuple2<String, String>, BigDecimal> {
    public static final int NAME_INDEX = 1;
    public static final int SELLER_INDEX = 2;
    public static final int AMOUNT_INDEX = 3;
    static Logger LOGGER = LoggerFactory.getLogger(NameSaleMap.class.getName());

    public NameSaleMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.WALLET_RESET_NAME_SALE_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.WALLET_LIST_NAME_SALE_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.WALLET_ADD_NAME_SALE_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.WALLET_REMOVE_NAME_SALE_TYPE);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
        //NAME INDEX
        NavigableSet<Tuple2<String, Tuple2<String, String>>> nameIndex = database.createTreeSet("namesales_index_name")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingNameIndex = database.createTreeSet("namesales_index_name_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(NAME_INDEX, nameIndex, descendingNameIndex, new Fun.Function2<String, Tuple2<String, String>, BigDecimal>() {
            @Override
            public String run(Tuple2<String, String> key, BigDecimal value) {
                return key.b;
            }
        });

        //SELLER INDEX
        NavigableSet<Tuple2<String, Tuple2<String, String>>> ownerIndex = database.createTreeSet("namesales_index_seller")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingOwnerIndex = database.createTreeSet("namesales_index_seller_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(SELLER_INDEX, ownerIndex, descendingOwnerIndex, new Fun.Function2<String, Tuple2<String, String>, BigDecimal>() {
            @Override
            public String run(Tuple2<String, String> key, BigDecimal value) {
                return key.a;
            }
        });

        //AMOUNT INDEX
        NavigableSet<Tuple2<BigDecimal, Tuple2<String, String>>> amountIndex = database.createTreeSet("namesales_index_amount")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<BigDecimal, Tuple2<String, String>>> descendingAmountIndex = database.createTreeSet("namesales_index_amount_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(SELLER_INDEX, amountIndex, descendingAmountIndex, new Fun.Function2<BigDecimal, Tuple2<String, String>, BigDecimal>() {
            @Override
            public BigDecimal run(Tuple2<String, String> key, BigDecimal value) {
                return value;
            }
        });
    }

    @Override
    protected void getMap() {
        //OPEN MAP
        map = database.createTreeMap("namesales")
                .keySerializer(BTreeKeySerializer.TUPLE2)
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<Tuple2<String, String>, BigDecimal>(Fun.TUPLE2_COMPARATOR);
    }

    @Override
    protected BigDecimal getDefaultValue() {
        return BigDecimal.ZERO;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<NameSale> get(Account account) {
        List<NameSale> nameSales = new ArrayList<NameSale>();

        try {
            Map<Tuple2<String, String>, BigDecimal> accountNames = ((BTreeMap) this.map).subMap(
                    Fun.t2(account.getAddress(), null),
                    Fun.t2(account.getAddress(), Fun.HI()));

            for (Entry<Tuple2<String, String>, BigDecimal> entry : accountNames.entrySet()) {
                NameSale nameSale = new NameSale(entry.getKey().b, entry.getValue());
                nameSales.add(nameSale);
            }
        } catch (Exception e) {
            //ERROR
            LOGGER.error(e.getMessage(), e);
        }

        return nameSales;
    }

    public List<Pair<Account, NameSale>> get(List<Account> accounts) {
        List<Pair<Account, NameSale>> nameSales = new ArrayList<Pair<Account, NameSale>>();

        try {
            //FOR EACH ACCOUNTS
            synchronized (accounts) {
                for (Account account : accounts) {
                    List<NameSale> accountNameSales = get(account);
                    for (NameSale nameSale : accountNameSales) {
                        nameSales.add(new Pair<Account, NameSale>(account, nameSale));
                    }
                }
            }
        } catch (Exception e) {
            //ERROR
            LOGGER.error(e.getMessage(), e);
        }

        return nameSales;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Account account) {
        //GET ALL NAMES THAT BELONG TO THAT ADDRESS
        Map<Tuple2<String, String>, BigDecimal> accountNameSales = ((BTreeMap) this.map).subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI()));

        //DELETE NAMES
        for (Tuple2<String, String> key : accountNameSales.keySet()) {
            this.remove(key);
        }
    }

    public void delete(NameSale nameSale) {
        this.delete(nameSale.getName().getOwner(), nameSale);
    }

    public void delete(Account account, NameSale nameSale) {
        this.remove(new Tuple2<String, String>(account.getAddress(), nameSale.getKey()));
    }

    public void deleteAll(List<Account> accounts) {
        for (Account account : accounts) {
            this.delete(account);
        }
    }

    public boolean add(NameSale nameSale) {
        return this.set(new Tuple2<String, String>(nameSale.getName().getOwner().getAddress(), nameSale.getKey()), nameSale.getAmount());
    }

    public void addAll(Map<Account, List<NameSale>> nameSales) {
        //FOR EACH ACCOUNT
        for (Account account : nameSales.keySet()) {
            //FOR EACH TRANSACTION
            for (NameSale nameSale : nameSales.get(account)) {
                this.add(nameSale);
            }
        }
    }
}
