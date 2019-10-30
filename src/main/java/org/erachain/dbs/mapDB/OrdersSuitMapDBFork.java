package org.erachain.dbs.mapDB;

// 30/03

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.OrderComparatorForTrade;
import org.erachain.core.item.assets.OrderComparatorForTradeReverse;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.OrderSerializer;
import org.erachain.datachain.OrderMap;
import org.erachain.datachain.OrderSuit;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.*;


/**
 * Хранит блоки полностью - с транзакциями
 * <p>
 * ключ: номер блока (высота, height)<br>
 * занчение: Блок<br>
 * <p>
 * Есть вторичный индекс, для отчетов (blockexplorer) - generatorMap
 * TODO - убрать длинный индек и вставить INT
 *
 * @return
 */

@Slf4j
public class OrdersSuitMapDBFork extends DBMapSuitFork<Long, Order> implements OrderSuit {

    @SuppressWarnings("rawtypes")
    private BTreeMap haveWantKeyMap;

    public OrdersSuitMapDBFork(OrderMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, null);
    }

    @Override
    public void openMap() {
        // OPEN MAP
        map = database.createTreeMap("orders")
                .valueSerializer(new OrderSerializer())
                //.comparator(Fun.BYTE_ARRAY_COMPARATOR) // for byte[]
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //HAVE/WANT KEY
        this.haveWantKeyMap = database.createTreeMap("orders_key_have_want")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        ///////////////////// HERE PROTOCOL INDEX

        //BIND HAVE/WANT KEY
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.haveWantKeyMap,
                new Fun.Function2<Fun.Tuple4<Long, Long, BigDecimal, Long>, Long,
                        Order>() {
                    @Override
                    public Fun.Tuple4<Long, Long, BigDecimal, Long> run(
                            Long key, Order value) {
                        return new Fun.Tuple4<>(value.getHaveAssetKey(), value.getWantAssetKey(),
                                value.calcPrice(),
                                value.getId());
                    }
                });

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

    }

    // GET KEYs with FORKED rules
    @Override
    public HashSet<Long> getSubKeysWithParent(long have, long want) {

        HashSet<Long> keys = new HashSet<Long>(((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).values());

        //USE THE FORK KEYS
        //GET ALL KEYS FOR FORK in PARENT - getOrdersForTradeWithFork
        HashSet<Long> parentKeys = ((OrderMap) this.parent).getSubKeysWithParent(have, want);

        ////// Почемуто не получилось удалить дубли ключей при Мерже - по 2 раза один и тот же Ордер потом вылазил
        ////// вернее ключи Long одинаковые были в списке - зотя как объекты они разные но значения одинаковые (((
        ///// Iterable<Long> mergedIterable = Iterables.mergeSorted((Iterable) ImmutableList.of(senderKeys, recipientKeys), Fun.COMPARATOR);
        ///// return Lists.newLinkedList(mergedIterable).descendingIterator();

        // REMOVE those who DELETED here
        if (this.deleted != null) {
            //DELETE DELETED
            for (Object deleted : this.deleted.keySet()) {
                parentKeys.remove(deleted);
            }
        }

        keys.addAll(parentKeys);

        return keys;
    }

    // GET KEYs with FORKED rules
    @Override
    public Iterator<Long> getIteratorWithParent(long have, long want) {

        Iterator<Long> keys = ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).values().iterator();

        Iterator<Long> iterator = Iterators.mergeSorted(ImmutableList.of(
                ((OrderMap) this.parent).getIteratorWithParent(have, want), keys), Fun.COMPARATOR);

        return super.getIterator();
    }


    @Override
    public List<Order> getOrdersForTradeWithFork(long have, long want, boolean reverse) {
        //FILTER ALL KEYS
        Collection<Long> keys = this.getSubKeysWithParent(have, want);

        //GET ALL ORDERS FOR KEYS
        List<Order> orders = new ArrayList<Order>();

        for (Long key : keys) {
            Order order = this.get(key);
            if (order != null) {
                orders.add(order);
            } else {
                // возможно произошло удаление в момент запроса??
            }
        }

        if (reverse) {
            Collections.sort(orders, new OrderComparatorForTradeReverse());
        } else {
            Collections.sort(orders, new OrderComparatorForTrade());
        }

        //RETURN
        return orders;
    }

    @Override
    public Iterator<Long> getHaveWantIterator(long have, long want) {
        return null;
    }

    @Override
    public Iterator<Long> getHaveWantIterator(long have) {
        return null;
    }

    @Override
    public Iterator<Long> getWantHaveIterator(long want, long have) {
        return null;
    }

    @Override
    public Iterator<Long> getWantHaveIterator(long want) {
        return null;
    }

    @Override
    public Iterator<Long> getAddressHaveWantIterator(String address, long have, long want) {
        return null;
    }

}
