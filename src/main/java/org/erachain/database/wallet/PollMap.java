package org.erachain.database.wallet;

import org.erachain.core.account.Account;
import org.erachain.core.voting.Poll;
import org.erachain.database.serializer.PollSerializer;
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

import java.util.*;

public class PollMap extends DCUMapImpl<Tuple2<String, String>, Poll> {
    public static final int NAME_INDEX = 1;
    public static final int CREATOR_INDEX = 2;
    static Logger LOGGER = LoggerFactory.getLogger(PollMap.class.getName());

    public PollMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.WALLET_RESET_POLL_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.WALLET_LIST_POLL_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.WALLET_ADD_POLL_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.WALLET_REMOVE_POLL_TYPE);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
        //NAME INDEX
        NavigableSet<Tuple2<String, Tuple2<String, String>>> nameIndex = database.createTreeSet("polls_index_name")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingNameIndex = database.createTreeSet("polls_index_name_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(NAME_INDEX, nameIndex, descendingNameIndex, new Fun.Function2<String, Tuple2<String, String>, Poll>() {
            @Override
            public String run(Tuple2<String, String> key, Poll value) {
                return value.getName();
            }
        });

        //CREATOR INDEX
        NavigableSet<Tuple2<String, Tuple2<String, String>>> creatorIndex = database.createTreeSet("polls_index_creator")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingCreatorIndex = database.createTreeSet("polls_index_creator_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(CREATOR_INDEX, creatorIndex, descendingCreatorIndex, new Fun.Function2<String, Tuple2<String, String>, Poll>() {
            @Override
            public String run(Tuple2<String, String> key, Poll poll) {
                return key.a;
            }
        });
    }

    @Override
    protected void getMap() {
        //OPEN MAP
        map = database.createTreeMap("polls")
                .keySerializer(BTreeKeySerializer.TUPLE2)
                .valueSerializer(new PollSerializer())
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<Tuple2<String, String>, Poll>(Fun.TUPLE2_COMPARATOR);
    }

    @Override
    protected Poll getDefaultValue() {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Poll> get(Account account) {
        List<Poll> polls = new ArrayList<Poll>();

        try {
            Map<Tuple2<String, String>, Poll> accountPolls = ((BTreeMap) this.map).subMap(
                    Fun.t2(account.getAddress(), null),
                    Fun.t2(account.getAddress(), Fun.HI()));

            //GET ITERATOR
            Iterator<Poll> iterator = accountPolls.values().iterator();

            while (iterator.hasNext()) {
                polls.add(iterator.next());
            }
        } catch (Exception e) {
            //ERROR
            LOGGER.error(e.getMessage(), e);
        }

        return polls;
    }

    public List<Pair<Account, Poll>> get(List<Account> accounts) {
        List<Pair<Account, Poll>> polls = new ArrayList<Pair<Account, Poll>>();

        try {
            //FOR EACH ACCOUNTS
            synchronized (accounts) {
                for (Account account : accounts) {
                    List<Poll> accountPolls = get(account);
                    for (Poll poll : accountPolls) {
                        polls.add(new Pair<Account, Poll>(account, poll));
                    }
                }
            }
        } catch (Exception e) {
            //ERROR
            LOGGER.error(e.getMessage(), e);
        }

        return polls;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Account account) {
        //GET ALL POLLS THAT BELONG TO THAT ADDRESS
        Map<Tuple2<String, String>, Poll> accountPolls = ((BTreeMap) this.map).subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI()));

        //DELETE NAMES
        for (Tuple2<String, String> key : accountPolls.keySet()) {
            this.delete(key);
        }
    }

    public void delete(Poll poll) {
        this.delete(poll.getCreator(), poll);
    }

    public void delete(Account account, Poll poll) {
        this.delete(new Tuple2<String, String>(account.getAddress(), poll.getName()));
    }

    public void deleteAll(List<Account> accounts) {
        for (Account account : accounts) {
            this.delete(account);
        }
    }

    public boolean add(Poll poll) {
        return this.set(new Tuple2<String, String>(poll.getCreator().getAddress(), poll.getName()), poll);
    }

    public void addAll(Map<Account, List<Poll>> polls) {
        //FOR EACH ACCOUNT
        for (Account account : polls.keySet()) {
            //FOR EACH TRANSACTION
            for (Poll poll : polls.get(account)) {
                this.add(poll);
            }
        }
    }
}
