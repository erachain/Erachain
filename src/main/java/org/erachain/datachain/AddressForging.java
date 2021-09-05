package org.erachain.datachain;

import org.erachain.core.BlockChain;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.util.Collection;
import java.util.HashMap;

/**
 * Хранит данные о сборке блока для данного счета - по номеру блока
 * если номер блока не задан - то это последнее значение.
 * При этом если номер блока не задана то хранится поледнее значение
 *  account.address + current block.Height ->
 *     previous making blockHeight + previous ForgingH balance + this ForgingH balance
 <hr>
 - not SAME with BLOCK HEADS - use point for not only forged blocks - with incoming ERA Volumes
 <br>
 Так же тут можно искать блоки собранны с данного счета - а вторичный индекс у блоков не нужен.
 <br>
 Если точка первая то предыдущее в ней значение Высоты = 0, то есть указывает что ниже нету но текущий баланс уже есть для Форжинга

 last forged block for ADDRESS -> by height = 0
 * @return
 */

// TODO укротить до 20 байт адрес
public class AddressForging extends DCUMap<Tuple2<String, Integer>, Tuple3<Integer, Integer, Integer>> {


    public AddressForging(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public AddressForging(AddressForging parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.getHashMap("address_forging");
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<Tuple2<String, Integer>, Tuple3<Integer, Integer, Integer>>();
    }

    /**
     * Возвращает ПУСТО если что, тут нельзя Последнее возвращать
     * @param address
     * @param height
     * @return
     */
    public Tuple3<Integer, Integer, Integer> get(String address, int height) {
        return this.get(new Tuple2<String, Integer>(address, height));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Collection<Tuple3<Integer, Integer, Integer>> getGeneratorBlocks(String address) {
        Collection<Tuple3<Integer, Integer, Integer>> headers = ((BTreeMap) (this.map))
                .subMap(Fun.t2(address, null), Fun.t2(address, Integer.MAX_VALUE)).values();

        return headers;
    }

    /**
     * заносит новую точку и обновляет Последнюю точку (height & ForgingValue)/
     * При этом если последняя точка уже с той же высотой - то обновляем только ForgingValue/
     * Внимание! нельзя в этот set() заносить при writeToParent иначе будет двойная обработка
     *
     * @param key          Addres + height
     * @param forgingValue
     * @return
     */
    // TODO надо перенести логику эту наверх в BlockChain поидее
    // иначе если бы set и put тут будут то они делают зацикливание
    public boolean setAndProcess(Tuple2<String, Integer> key, int forgingValue) {

        if (key.b == 0) {
            // это сохранение из writeToParent - там все значения сливаются из Форкнутой базы включая setLast с 0-м значением
            ///return super.set(key, forgingPoint);
        ///} else if (key.b.equals(forgingPoint.a)) {
            if (BlockChain.CHECK_BUGS > 0) {
                LOGGER.error("NOT VALID forging info " + key + " == " + key);
            }
            Long i = null;
            i++;
        }

        Tuple3<Integer, Integer, Integer> lastPoint = this.getLast(key.a);
        if (lastPoint == null) {
            // это последние значения. Причем в позиции Текущий - 0 - дальше то пока не известно
            // тут же и поиск по этой высоте должен дать Пусто
            this.setLast(key.a, new Tuple3(key.b, forgingValue, 0));
            /// там же пусто - поэтому ничего не делаем - super.set(key, previousPoint);
        } else {
            if (key.b > lastPoint.a) {
                // ONLY if not SAME HEIGHT !!! потому что в одном блоке может идти несколько
                // транзакций на один счет инициализирующих - нужно результат в конце поймать
                // и если одниковый блок и форжинговое значение - то обновлять только Последнее,
                // то есть сюда приходит только если НАОБОРОТ - это не Первое значение и Не с темже блоком в Последнее.
                super.put(key, new Tuple3(lastPoint.a, lastPoint.b, forgingValue));
            } else if (key.b < lastPoint.a) {
                // тут ошибка
                if (BlockChain.ERA_COMPU_ALL_UP) {
                    // так как непонятно когда генерация начнется в этом случае - игнорируем такую ситуацию
                    super.delete(new Tuple2(key.a, lastPoint.a));
                } else {
                    LOGGER.error("NOT VALID forging POINTS:" + lastPoint + " > " + key);
                    Long i = null;
                    i++;
                    //assert(forgingPoint.a >= lastPoint.a);
                }
            } else {
                // тут все нормально - такое бывает когда несколько раз в блоке пришли ERA
                // просто нужно обновить новое значение кующей величины
                ;
            }
            this.setLast(key.a, new Tuple3(key.b, forgingValue, 0));
        }

        return true;

    }

    public void putAndProcess(String address, Integer currentHeight,
                              Integer currentForgingVolume) {
        this.setAndProcess(new Tuple2<String, Integer>(address, currentHeight),
                currentForgingVolume);
    }

    /**
     * Удаляет текущую точку и обновляет ссылку на Последнюю точку - если из высоты совпали
     * Так как если нет соапвдения - то удалять нельзя так как уже удалили ранее ее
     * - по несколько раз при откате может быть удаление текущей точки
     * Нельзя сюда послать в writeToParent так как иначе будет двойная обработка.
     * @param key
     * @return
     */
    public Tuple3<Integer, Integer, Integer> removeAndProcess(Tuple2<String, Integer> key) {

        if (key.b < 1) {
            // not delete GENESIS forging data for all accounts
            return null;
        }

        // удалять можно только если последняя точка совпадает с удаляемой
        // иначе нельзя - так как может быть несколько удалений в один блок
        Tuple3<Integer, Integer, Integer> lastPoint = getLast(key.a);
        if (lastPoint == null) {
            // обычно такого не должно случаться!!!
            if (BlockChain.CHECK_BUGS > 2)
                LOGGER.error("ERROR LAST forging POINTS = null for KEY: " + key);
            return super.remove(key);
        } else {
            //LOGGER.debug("last POINT: " + lastPoint);
            if (lastPoint.a.equals(key.b)) {
                Tuple3<Integer, Integer, Integer> previous = super.remove(key);
                this.setLast(key.a, previous);
                //LOGGER.debug("delete and set prev POINT as last: " + (previous == null? "null" : previous) + " for " + key);
                return previous;
            } else if (lastPoint.a > key.b) {
                // там могут быть накладки если новый счет с отступом 100 виртуальный форжинг поставил при первой сборке блока
                // то игнорируем
                if (BlockChain.ERA_COMPU_ALL_UP) {
                    ;
                } else {
                    // тут ошибка
                    if (BlockChain.CHECK_BUGS > 2)
                        LOGGER.error("WRONG deleted and LAST forging POINTS:" + lastPoint + " > " + key);
                    //Tuple3<Integer, Integer, Integer> previous = super.remove(key);
                    //this.setLast(key.a, previous);
                    assert (lastPoint.a <= key.b);
                    Long iii = null;
                    iii++;
                }
            } else {
                // тут все нормально - такое бывает когда несколько раз в блоке пришли ERA
                // И при первом разе все уже удалилось - тут ничего не делаем
                return lastPoint;
            }
        }

        return null;
    }

    public void deleteAndProcess(String address, int height) {
        // Код почти не изменится если там (void)DELETE вставить так как при удалении всегда предыдущее значение выбирается
        this.removeAndProcess(new Tuple2<String, Integer>(address, height));
    }

    /**
     *
     * @param address
     * @return последний блок собранный и его кующее значение - a: current Height, b: current Forgingalue, c: None
     */
    public Tuple3<Integer, Integer, Integer> getLast(String address) {
        return this.get(new Tuple2<String, Integer>(address, 0));
    }

    private void setLast(String address, Tuple3<Integer, Integer, Integer> point) {
        if (point == null) {
            // вызываем супер-класс
            super.delete(new Tuple2<String, Integer>(address, 0));
        } else {
            // вызываем супер-класс
            super.put(new Tuple2<String, Integer>(address, 0), point);
        }
    }
}
