package org.erachain.datachain;

import org.erachain.database.DBMap;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.math.BigInteger;
import java.util.*;

/**
 * Храним выбор голосующего по Сущности Голования
 * POLL KEY + OPTION KEY + ACCOUNT SHORT = result Transaction reference (BlockNo + SeqNo)
 * byte[] - un CORAMPABLE
 *
 * Ключ: Номер Голосвания + Номер выбора + Счет Короткий
 * Значение: СТЭК ссылок на трнзакцию голосвания
 *
 * TODO: передлать ссылку на запись на Лонг
 * TODO: передлать короткий Счет на байты
 */
public class VoteOnItemPollMap extends DCMap<Tuple3<Long, Integer, BigInteger>, Stack<Tuple2<Integer, Integer>>> {

    public VoteOnItemPollMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_VOTEPOLL_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_VOTEPOLL_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_VOTEPOLL_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_VOTEPOLL_TYPE);
        }

    }

    public VoteOnItemPollMap(VoteOnItemPollMap parent) {
        super(parent, null);
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Tuple3<Long, Integer, BigInteger>, Stack<Tuple2<Integer, Integer>>> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("vote_item_poll")
                .keySerializer(BTreeKeySerializer.TUPLE3)
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<Tuple3<Long, Integer, BigInteger>, Stack<Tuple2<Integer, Integer>>> getMemoryMap() {
        return new TreeMap<Tuple3<Long, Integer, BigInteger>, Stack<Tuple2<Integer, Integer>>>();
    }

    @Override
    protected Stack<Tuple2<Integer, Integer>> getDefaultValue() {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public NavigableSet<Tuple3> getVotes(Long pollKey) {
        BTreeMap map = (BTreeMap) this.map;
        //FILTER ALL KEYS
        NavigableSet<Tuple3> keys = ((BTreeMap<Tuple3, Tuple2>) map).subMap(
                Fun.t3(pollKey, null, null),
                Fun.t3(pollKey, Integer.MAX_VALUE, Fun.HI()))
                .keySet();


        //RETURN
        return keys;
    }

    public NavigableSet<Tuple3<Long, Integer, BigInteger>> getVotes_1(Long pollKey) {
        @SuppressWarnings("rawtypes")
        BTreeMap map = (BTreeMap) this.map;
        Set ss = new TreeSet();
        @SuppressWarnings("unchecked")
        NavigableSet<Tuple3<Long, Integer, BigInteger>> ks = map.keySet();
        Iterator<Tuple3<Long, Integer, BigInteger>> it = ks.iterator();
        while (it.hasNext()) {
            Tuple3<Long, Integer, BigInteger> a = it.next();
            if (a.a != pollKey) continue;
            ss.add(a);
        }
        return (NavigableSet<Tuple3<Long, Integer, BigInteger>>) ss;

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public NavigableSet<Tuple3<Long, Integer, BigInteger>> getVotes(Long pollKey, Integer option) {
        BTreeMap map = (BTreeMap) this.map;

        //FILTER ALL KEYS
        NavigableSet<Tuple3<Long, Integer, BigInteger>> keys = ((BTreeMap<Tuple3<Long, Integer, BigInteger>, Tuple2>) map).subMap(
                Fun.t3(pollKey, option, null),
                Fun.t3(pollKey, option, Fun.HI())).keySet();

        //RETURN
        return keys;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean hasVotes(Long pollKey) {
        BTreeMap map = (BTreeMap) this.map;

        //FILTER ALL KEYS
        Tuple3<Long, Integer, BigInteger> key = ((BTreeMap<Tuple3<Long, Integer, BigInteger>, Tuple2>) map).subMap(
                Fun.t3(pollKey, null, null),
                Fun.t3(pollKey, Fun.HI(), Fun.HI())).firstKey();

        //RETURN
        return key != null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean hasVotes(Long pollKey, Integer option) {
        BTreeMap map = (BTreeMap) this.map;

        //FILTER ALL KEYS
        Tuple3<Long, Integer, BigInteger> key = ((BTreeMap<Tuple3<Long, Integer, BigInteger>, Tuple2>) map).subMap(
                Fun.t3(pollKey, option, null),
                Fun.t3(pollKey, option, Fun.HI())).firstKey();

        //RETURN
        return key != null;
    }

    public Stack<Tuple2<Integer, Integer>> get(long pollKey, int optionKey, BigInteger accountShort) {
        return this.get(new Tuple3<Long, Integer, BigInteger>(pollKey, optionKey, accountShort));
    }

    @SuppressWarnings("unchecked")
    public void addItem(long pollKey, int optionKey, BigInteger accountShort, Tuple2<Integer, Integer> value) {
        Tuple3<Long, Integer, BigInteger> key = new Tuple3<Long, Integer, BigInteger>(pollKey, optionKey, accountShort);
        Stack<Tuple2<Integer, Integer>> stack = this.get(key);
        if (stack == null)
            stack = new Stack<Tuple2<Integer, Integer>>();

        Stack<Tuple2<Integer, Integer>> new_stack;

        if (this.parent == null)
            new_stack = stack;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            new_stack = (Stack<Tuple2<Integer, Integer>>) stack.clone();
        }

        new_stack.push(value);
        this.set(key, stack);
    }

    public Tuple2<Integer, Integer> getItem(long pollKey, int optionKey, BigInteger accountShort) {
        Tuple3<Long, Integer, BigInteger> key = new Tuple3<Long, Integer, BigInteger>(pollKey, optionKey, accountShort);
        Stack<Tuple2<Integer, Integer>> stack = this.get(key);
        return stack == null || stack.isEmpty() ? null : stack.peek();
    }

    @SuppressWarnings("unchecked")
    public Tuple2<Integer, Integer> removeItem(long pollKey, int optionKey, BigInteger accountShort) {
        Tuple3<Long, Integer, BigInteger> key = new Tuple3<Long, Integer, BigInteger>(pollKey, optionKey, accountShort);
        Stack<Tuple2<Integer, Integer>> stack = this.get(key);
        if (stack == null || stack.isEmpty())
            return null;

        Stack<Tuple2<Integer, Integer>> new_stack;
        if (this.parent == null)
            new_stack = stack;
        else {
            // !!!! NEEED .clone() !!!
            // need for updates only in fork - not in parent DB
            new_stack = (Stack<Tuple2<Integer, Integer>>) stack.clone();
        }

        Tuple2<Integer, Integer> itemRemoved = new_stack.pop();

        this.set(key, new_stack);

        return itemRemoved;

    }
}
