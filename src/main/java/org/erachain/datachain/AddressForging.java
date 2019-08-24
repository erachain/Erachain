package org.erachain.datachain;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//import java.util.List;
//import java.util.TreeMap;
//import java.util.TreeSet;
//import org.mapdb.Fun.Tuple3;

/*
 */

//
// last forged block for ADDRESS -> by height = 0
/**
 * Хранит данные о сборке блока для данного счета - по номеру блока
 * если номер блока не задан - то это последнее значение.
 * При этом если номер блока не задана то хранится поледнее значение
 *  account.address + current block.Height ->
 *     previous making blockHeight + this ForgingH balance
<hr>
 - not SAME with BLOCK HEADS - use point for not only forged blocks - with incoming ERA Volumes
 <br>
 Так же тут можно искать блоки собранны с данного счета - а вторичный индекс у блоков не нужен

 * @return
 */

// TODO укротить до 20 байт адрес
public class AddressForging extends DCMap<Tuple2<String, Integer>, Tuple2<Integer, Integer>> {


    public AddressForging(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public AddressForging(AddressForging parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override

    protected Map<Tuple2<String, Integer>, Tuple2<Integer, Integer>> getMap(DB database) {
        //OPEN MAP
        return database.getTreeMap("address_forging");
    }

    @Override
    protected Map<Tuple2<String, Integer>, Tuple2<Integer, Integer>> getMemoryMap() {
        return new HashMap<Tuple2<String, Integer>, Tuple2<Integer, Integer>>();
    }

    @Override
    protected Tuple2<Integer, Integer> getDefaultValue() {
        return null; //new Tuple2<Integer, Integer>(-1, 0);
    }

    public Tuple2<Integer, Integer> get(String address, int height) {
        Tuple2<Integer, Integer> point = this.get(new Tuple2<String, Integer>(address, height));
        if (point == null) {
            return this.getLast(address);
        }

        return point;

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Collection<Tuple2<Integer, Integer>> getGeneratorBlocks(String address) {
        Collection<Tuple2<Integer, Integer>> headers = ((BTreeMap) (this.map))
                .subMap(Fun.t2(address, null), Fun.t2(address, Fun.HI())).values();

        return headers;
    }

    // height
    public boolean set(Tuple2<String, Integer> key, Tuple2<Integer, Integer> currentForgingValue) {

        Tuple2<Integer, Integer> previousPoint = this.getLast(key.a);
        if (previousPoint != null && currentForgingValue.b > previousPoint.a) {
            // ONLY if not SAME HEIGHT !!! потому что в одном блоке может идти несколько
            // транзакций на один счет инициализирующих - нужно результат в конце поймать
            // и если одниковый блок и форжинговое значение - то обновлять только Последнее,
            // то есть сюда приходит только если НАОБОРОТ - это не Первое значение и Не с темже блоком в Последнее
            super.set(key, previousPoint);
        }

        this.setLast(key.a, currentForgingValue);

        return true;

    }

    // height
    public void set(String address, Integer currentHeight, Integer currentForgingVolume) {

        super.set(new Tuple2<String, Integer>(address, currentHeight),
                new Tuple2<Integer, Integer>(currentHeight, currentForgingVolume));

    }

    public Tuple2<Integer, Integer> delete(Tuple2<String, Integer> key) {

        if (key.b < 3) {
            // not delete GENESIS forging data for all accounts
            return null;
        }

        Tuple2<Integer, Integer> previous = super.delete(key);
        if (previous != null) {
            if (previous.a < key.b) {
                // только если там значение более ранне - его можно установить как последнее
                // иначе нельзя - так как может быть несколько удалений в один блок
                this.setLast(key.a, previous);
            } else {
                this.setLast(key.a, null);
            }
        }

        return previous;
    }

    public void delete(String address, int height) {
        this.delete(new Tuple2<String, Integer>(address, height));
    }

    public Tuple2<Integer, Integer> getLast(String address) {
        return this.get(new Tuple2<String, Integer>(address, 0));
    }

    private void setLast(String address, Tuple2<Integer, Integer> point) {
        if (point == null) {
            this.delete(new Tuple2<String, Integer>(address, 0));
        } else {
            this.set(new Tuple2<String, Integer>(address, 0), point);
        }
    }
}
