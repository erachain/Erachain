package org.erachain.dbs.mapDB;

// 30/03

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.Order;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.OrderSerializer;
import org.erachain.datachain.OrderMap;
import org.erachain.datachain.OrderSuit;
import org.erachain.dbs.IteratorCloseable;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


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
                            Long key, Order order) {
                        return new Fun.Tuple4<>(order.getHaveAssetKey(), order.getWantAssetKey(),

                                // по остаткам цены НЕЛЬЗЯ! так как при изменении цены после покусывания стрый ключ не находится!
                                // и потом при поиске по итераторы находятся эти неудалившиеся ключи!
                                key > BlockChain.LEFT_PRICE_HEIGHT_SEQ ? order.calcLeftPrice() : order.getPrice(),
                                //// теперь можно - в Обработке ордера сделал решение этой проблемы value.getPrice(),

                                order.getId());
                    }
                });

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

    }

    // GET KEYs with FORKED rules
    @Override
    public HashMap<Long, Order> getUnsortedEntries(long have, long want, BigDecimal stopPrice, Map deleted_empty) {

        // GET FROM PARENT and exclude DELETED here
        HashMap<Long, Order> result = ((OrderMap) parent).getProtocolEntries(have, want, stopPrice, deleted);

        // берем все сейчас! так как тут просто перебор будет и нам надо взять + одну выше цены
        // Object limitOrHI = stopPrice == null ? Fun.HI() : stopPrice; // надо тут делать выбор иначе ошибка преобразования в subMap
        Collection<Long> keys = ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI()))
                .values();

        // UPDATE from this FORKED TABLE
        for (Long key : keys) {
            Order order = get(key);
            // здесь дубли сами собой схлопнутся но если брать итератор, то нажо использовать
            // в будущем new MergedOR_IteratorsNoDuplicates
            result.put(key, order);
            // сдесь хотя бы одну заявку с неподходящей вроде бы ценой нужно взять
            // причем берем по Остаткам Цену теперь
            if (stopPrice != null && order.calcLeftPrice().compareTo(stopPrice) > 0) {
                break;
            }
        }

        return result;
    }

    @Override
    public Order getHaveWanFirst(long have, long want) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getHaveWantIterator(long have, long want) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getHaveWantIterator(long have) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getWantHaveIterator(long want, long have) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getWantHaveIterator(long want) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getAddressHaveWantIterator(String address, long have, long want) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getAddressIterator(String address) {
        return null;
    }

    @Override
    public boolean set(Long key, Order value) {

        try {

            // сначала проверим - есть ли он тут включая форк
            boolean exist = this.contains(key);

            this.map.put(key, value);

            if (false) {
                // так как тут особый случай по замене записи - нужно ставить в ДЕЛЕТЕД значения
                // см issues/1276
                if (this.deleted != null) {
                    if (this.deleted.remove(key) != null)
                        ++this.shiftSize;
                }
            }

            return exist;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return false;
    }

    @Override
    public void put(Long key, Order value) {

        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.set, так как
        /// если в подклассе будет из SET вызов PUT то он придет сюда и при перевузове THIS.SET отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> set(key, value);
        ///

        try {

            this.map.put(key, value);

            if (false) {
                // так как тут особый случай по замене записи - нужно ставить в ДЕЛЕТЕД значения
                // см issues/1276
                if (this.deleted != null) {
                    if (this.deleted.remove(key) != null)
                        ++this.shiftSize;
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}
