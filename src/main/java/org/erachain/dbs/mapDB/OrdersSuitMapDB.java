package org.erachain.dbs.mapDB;

// 30/03

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.OrderSerializer;
import org.erachain.datachain.IndexIterator;
import org.erachain.datachain.OrderSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;

;

//import com.sun.media.jfxmedia.logging.Logger;

/**
 * Хранит блоки полностью - с транзакциями
 * <p>
 * ключ: номер блока (высота, height)<br>
 * занчение: Блок<br>
 * <p>
 * Есть вторичный индекс, для отчетов (blockexplorer) - generatorMap
 * TODO - убрать длинный индекс и вставить INT
 *
 * @return
 */

@Slf4j
public class OrdersSuitMapDB extends DBMapSuit<Long, Order> implements OrderSuit {

    @SuppressWarnings("rawtypes")
    private BTreeMap haveWantKeyMap;
    @SuppressWarnings("rawtypes")
    // TODO: cut index to WANT only
    private BTreeMap wantHaveKeyMap;
    private BTreeMap addressHaveWantKeyMap;
    private NavigableSet assetKeySet;

    public OrdersSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
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
                            Long key, Order order) {
                        return new Fun.Tuple4<>(order.getHaveAssetKey(), order.getWantAssetKey(),

                                // по остаткам цены НЕЛЬЗЯ! так как при изменении цены после покусывания стрый ключ не находится!
                                // и потом при поиске по итераторы находятся эти неудалившиеся ключи!
                                key > BlockChain.LEFT_PRICE_HEIGHT_SEQ ? order.calcLeftPrice() : order.getPrice(),
                                //// теперь можно - в Обработке ордера сделал решение этой проблемы value.getPrice(),
                                // но нужно помнить что сначала полностью удаляем ордер а потом добавляем его вместо простого Обновить
                                // тоже самое и при сливе из ФОРКнутой базы при просчете валидности и догонянии цепочки - надо учесть
                                // что сначала из ДЕЛЕТ удалить а потом добавить новое значение

                                order.getId());
                    }
                });

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        ///////////////////// HERE NOT PROTOCOL INDEXES

        // ADDRESS HAVE/WANT KEY
        this.addressHaveWantKeyMap = database.createTreeMap("orders_key_address_have_want")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.addressHaveWantKeyMap,
                new Fun.Function2<Fun.Tuple5<String, Long, Long, BigDecimal, Long>, Long, Order>() {
                    @Override
                    public Fun.Tuple5<String, Long, Long, BigDecimal, Long> run(
                            Long key, Order value) {
                        return new Fun.Tuple5<String, Long, Long, BigDecimal, Long>
                                (value.getCreator().getAddress(), value.getHaveAssetKey(), value.getWantAssetKey(), value.getPrice(),
                                        key);
                    }
                });

        // WANT/HAVE KEY
        this.wantHaveKeyMap = database.createTreeMap("orders_key_want_have")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.wantHaveKeyMap,
                new Fun.Function2<Fun.Tuple4<Long, Long, BigDecimal, Long>, Long,
                        Order>() {
                    @Override
                    public Fun.Tuple4<Long, Long, BigDecimal, Long> run(
                            Long key, Order value) {
                        return new Fun.Tuple4<>(value.getWantAssetKey(), value.getHaveAssetKey(),
                                value.getPrice(),
                                value.getId());
                    }
                });

        // WANT/HAVE KEY
        this.assetKeySet = database.createTreeSet("orders_key_asset")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.assetKeySet,
                new Fun.Function2<Long[], Long,
                        Order>() {
                    @Override
                    public Long[] run(
                            Long key, Order value) {
                        return new Long[]{value.getWantAssetKey(), value.getHaveAssetKey()};
                    }
                });

    }

    //@Override
    protected void getMemoryMap() {
        openMap();
    }

    @Override
    public Order getHaveWanFirst(long have, long want) {

        Map.Entry<Fun.Tuple4, Long> first = ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).firstEntry();

        if (first == null)
            return null;

        return get(first.getValue());

    }

    @Override
    public IteratorCloseable<Long> getIteratorByAssetKey(long assetKey, boolean descending) {

        if (this.assetKeySet == null)
            return null;

        if (descending)
            return IteratorCloseableImpl.make(new IndexIterator(
                    this.assetKeySet.descendingSet().subSet(
                            Fun.t2(assetKey, Long.MAX_VALUE),
                            Fun.t2(assetKey, 0L)).iterator()));

        return IteratorCloseableImpl.make(new IndexIterator(
                this.assetKeySet.subSet(
                        Fun.t2(assetKey, 0L),
                        Fun.t2(assetKey, Long.MAX_VALUE)).iterator()));

    }

    @Override
    public IteratorCloseable<Long> getHaveWantIterator(long have, long want) {

        return new IteratorCloseableImpl(((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).values().iterator());

    }

    @Override
    public IteratorCloseable<Long> getHaveWantIterator(long have) {
        return new IteratorCloseableImpl(((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, null, null, null),
                Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).values().iterator());
    }

    @Override
    public IteratorCloseable<Long> getWantHaveIterator(long want, long have) {

        return new IteratorCloseableImpl(((BTreeMap<Fun.Tuple4, Long>) this.wantHaveKeyMap).subMap(
                Fun.t4(want, have, null, null),
                Fun.t4(want, have, Fun.HI(), Fun.HI())).values().iterator());

    }

    @Override
    public IteratorCloseable<Long> getWantHaveIterator(long want) {
        return new IteratorCloseableImpl(((BTreeMap<Fun.Tuple4, Long>) this.wantHaveKeyMap).subMap(
                Fun.t4(want, null, null, null),
                Fun.t4(want, Fun.HI(), Fun.HI(), Fun.HI())).values().iterator());
    }

    @Override
    public IteratorCloseable<Long> getAddressHaveWantIterator(String address, long have, long want) {

        return new IteratorCloseableImpl(((BTreeMap<Fun.Tuple5, Long>) this.addressHaveWantKeyMap).subMap(
                Fun.t5(address, have, want, null, null),
                Fun.t5(address, have, want, Fun.HI(), Fun.HI())).values().iterator());
    }

    @Override
    public IteratorCloseable<Long> getAddressIterator(String address) {

        return new IteratorCloseableImpl(((BTreeMap<Fun.Tuple5, Long>) this.addressHaveWantKeyMap).subMap(
                Fun.t5(address, null, null, null, null),
                Fun.t5(address, Fun.HI(), Fun.HI(), Fun.HI(), Fun.HI())).values().iterator());
    }

    @Override // Tuple4[12, 95, 6236.96848586, 3644468729217028] in haveWantKeyMap
    public HashMap<Long, Order> getUnsortedEntries(long have, long want, BigDecimal stopPrice, Map deleted) {

        // берем все сейчас! так как тут просто перебор будет и нам надо взять + одну выше цены
        // Object limitOrHI = stopPrice == null ? Fun.HI() : stopPrice; // надо тут делать выбор иначе ошибка преобразования в subMap
        Collection<Long> keys = ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI()))
                .values();

        HashMap<Long, Order> result = new HashMap<>();
        for (Long key : keys) {
            if (deleted != null && deleted.containsKey(key)) {
                // SKIP deleted in FORK
                continue;
            }

            Order order = get(key);
            if (BlockChain.CHECK_BUGS > -1 && order == null) {
                logger.error("ORDER [" + Transaction.viewDBRef(key) + "] = NULL");
            }
            result.put(key, order);
            // здесь хотя бы одну заявку с неподходящей вроде бы ценой нужно взять
            // причем берем по Остаткам Цену теперь
            if (stopPrice != null && order.calcLeftPrice().compareTo(stopPrice) > 0) {
                break;
            }
        }

        return result;
    }

    @Override
    public void delete(Long key) {
        if (BlockChain.CHECK_BUGS > 33 && Transaction.viewDBRef(key).equals("176395-2")) {
            boolean debug = true;
        }
        super.delete(key);
    }

    @Override
    public Order remove(Long key) {
        if (BlockChain.CHECK_BUGS > 33 && Transaction.viewDBRef(key).equals("176395-2")) {
            boolean debug = true;
        }
        return super.remove(key);
    }

}
