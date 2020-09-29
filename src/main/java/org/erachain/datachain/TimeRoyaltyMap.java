package org.erachain.datachain;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.util.HashMap;
import java.util.Iterator;

//
// last forged block for ADDRESS -> by height = 0

/**
 * Хранит данные о наградах за время
 * если номер Транзакции не задан - то это последнее значение.
 * Person.key ->
 * current point: seqNo + Base Value + Royalty - последняя точка
 * Person.key + seqNo ->
 * previous point: seqNo + Base Value + Royalty - предыдущая точка
 * предыдущая транзакция где начислялась награда + начисленная награда сейчас + начисленная ранее
 * <hr>
 *
 * @return
 */

// TODO укротить до 20 байт адрес
public class TimeRoyaltyMap extends DCUMap<Tuple2<Long, Long>, Tuple3<Long, Long, Long>> {


    public TimeRoyaltyMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public TimeRoyaltyMap(TimeRoyaltyMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        ////return database.createHashMap("address_Royalty").makeOrGet();
        map = database.getHashMap("time_royalty");
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<Tuple2<Long, Long>, Tuple3<Long, Long, Long>>();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Iterator<Tuple2<Long, Long>> getLine(Long personKey) {
        return ((BTreeMap) (this.map))
                .subMap(Fun.t2(personKey, null), Fun.t2(personKey, Long.MAX_VALUE)).keySet().iterator();

    }

    /**
     * заносит новую точку и обновляет Последнюю точку
     * При этом если последняя точка уже с той же высотой - то обновляем только RoyaltyValue/
     * (<b>!!!</b>)  Внимание! нельзя в этот set() заносить при writeToParent иначе будет двойная обработка
     *
     */
    // TODO надо перенести логику эту наверх в BlockChain поидее
    // иначе если бы set и put тут будут то они делают зацикливание
    public boolean push(Long personKey, Long seqNo, Long royaltyBase, Long royaltyValue) {

        Tuple3<Long, Long, Long> lastPoint = this.peek(personKey);

        this.setLast(personKey, new Tuple3(seqNo, royaltyBase, royaltyValue));

        if (lastPoint == null) {
            return false;
        } else {
            super.put(new Tuple2<Long, Long>(personKey, seqNo), lastPoint);
            return true;
        }

    }

    /**
     * Удаляет текущую точку и обновляет ссылку на Последнюю точку
     *
     * (<b>!!!</b>) Нельзя сюда послать в writeToParent так как иначе будет двойная обработка.
     *
     */
    public Tuple3<Long, Long, Long> pop(Long personKey) {

        Tuple3<Long, Long, Long> lastPoint = peek(personKey);
        if (lastPoint == null) {
            return null;
        } else {
            Tuple3<Long, Long, Long> prevPoint = super.remove(new Tuple2<Long, Long>(personKey, lastPoint.a));
            if (prevPoint != null) {
                this.setLast(personKey, prevPoint);
            }
        }
        return lastPoint;
    }

    public Tuple3<Long, Long, Long> peek(Long person) {
        return this.get(new Tuple2<Long, Long>(person, 0L));
    }

    private void setLast(Long person, Tuple3<Long, Long, Long> point) {
        if (point == null) {
            // вызываем супер-класс
            super.delete(new Tuple2<Long, Long>(person, 0L));
        } else {
            // вызываем супер-класс
            super.put(new Tuple2<Long, Long>(person, 0L), point);
        }
    }
}
