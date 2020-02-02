package org.erachain.datachain;

import org.junit.Test;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import static org.junit.Assert.assertEquals;

public class AddressForgingTest {

    DCSet db;
    String address = "7CzxxwH7u9aQtx5iNHskLQjyJvybyKg8rF";
    AddressForging forgingMap;
    Tuple2<String, Integer> key;
    Tuple3<Integer, Integer, Integer> lastPoint;
    Tuple3<Integer, Integer, Integer> currentPoint;

    private void init() {

        db = DCSet.createEmptyDatabaseSet(1);
        forgingMap = db.getAddressForging();

    }

    @Test
    public void get() {
    }

    @Test
    public void set1() {

        init();

        int height = 123;
        int weight = 34789;

        forgingMap.putAndProcess(address, height, weight);
        currentPoint = new Tuple3<>(height, weight, 0);
        lastPoint = forgingMap.getLast(address);

        Tuple2<String, Integer> key = new Tuple2<>(address, height);
        Tuple3<Integer, Integer, Integer> point = forgingMap.get(key);

        assertEquals(point, null);
        assertEquals(currentPoint, lastPoint);

        height += 100;
        weight -= 1000;
        Tuple3<Integer, Integer, Integer> currentPoint2 = new Tuple3<>(height, weight, 0);
        forgingMap.putAndProcess(address, height, weight);

        Tuple3<Integer, Integer, Integer> lastPoint2 = forgingMap.getLast(address);
        assertEquals(currentPoint2, lastPoint2);

        Tuple2<String, Integer> key2 = new Tuple2<>(address, height);
        point = forgingMap.get(key2);
        assertEquals(point, lastPoint);

        // SET SAME height
        weight += 333;
        forgingMap.putAndProcess(address, height, weight);

        Tuple3<Integer, Integer, Integer> lastPoint3 = forgingMap.getLast(address);
        assertEquals(lastPoint3, new Tuple3<>(height, weight, 0));

        point = forgingMap.get(key2);
        assertEquals(point, lastPoint);

    }

    @Test
    public void set_wrong() {

        init();

        int height = 123;
        int weight = 34789;

        forgingMap.putAndProcess(address, height, weight);
        currentPoint = new Tuple3<>(height, weight, 0);
        lastPoint = forgingMap.getLast(address);

        Tuple2<String, Integer> key = new Tuple2<>(address, height);
        Tuple3<Integer, Integer, Integer> point = forgingMap.get(key);

        assertEquals(point, null);
        assertEquals(currentPoint, lastPoint);

        // WRONG
        height -= 100;
        weight -= 1000;
        Tuple3<Integer, Integer, Integer> currentPoint2 = new Tuple3<>(height, weight, 0);
        try {
            forgingMap.putAndProcess(address, height, weight);
            assert(false);
        } catch (java.lang.AssertionError e) {

        }

    }

    @Test
    public void delete() {

        init();

        int height = 123;
        int weight = 34789;

        forgingMap.putAndProcess(address, height, weight);
        currentPoint = new Tuple3<>(height, weight, 0);
        lastPoint = forgingMap.getLast(address);

        Tuple2<String, Integer> key = new Tuple2<>(address, height);
        Tuple3<Integer, Integer, Integer> point = forgingMap.get(key);

        assertEquals(point, null);
        assertEquals(currentPoint, lastPoint);

        height += 100;
        weight += 1000;
        Tuple3<Integer, Integer, Integer> currentPoint2 = new Tuple3<>(height, weight, 0);
        forgingMap.putAndProcess(address, height, weight);

        Tuple3<Integer, Integer, Integer> lastPoint2 = forgingMap.getLast(address);
        assertEquals(currentPoint2, lastPoint2);

        Tuple2<String, Integer> key2 = new Tuple2<>(address, height);
        point = forgingMap.get(key2);
        assertEquals(point, lastPoint);

        // SET SAME height
        weight += 5000;
        forgingMap.putAndProcess(address, height, weight);

        Tuple3<Integer, Integer, Integer> lastPoint3 = forgingMap.getLast(address);
        assertEquals(lastPoint3, new Tuple3<>(height, weight, 0));

        point = forgingMap.get(key2);
        assertEquals(point, lastPoint);

        // TRY DELETE
        Tuple2<String, Integer> key3 = new Tuple2<>(address, lastPoint3.a);
        point = forgingMap.removeAndProcess(key3);

        Tuple3<Integer, Integer, Integer> lastPoint4 = forgingMap.getLast(address);
        Tuple3<Integer, Integer, Integer> prevPoint4 = forgingMap.get(address, lastPoint4.b);

        assertEquals(lastPoint4, lastPoint);
        assertEquals(prevPoint4, null);

        // TRY DELETE TWICE
        Tuple2<String, Integer> key4 = new Tuple2<>(address, lastPoint3.a);
        point = forgingMap.removeAndProcess(key4);

        Tuple3<Integer, Integer, Integer> lastPoint5 = forgingMap.getLast(address);
        Tuple3<Integer, Integer, Integer> prevPoint5 = forgingMap.get(address, lastPoint5.b);

        assertEquals(lastPoint5, lastPoint);
        assertEquals(prevPoint5, null);

        // TRY DELETE FIRST
        Tuple2<String, Integer> key5 = new Tuple2<>(address, lastPoint5.a);
        point = forgingMap.removeAndProcess(key5);
        assertEquals(point, null);

        Tuple3<Integer, Integer, Integer> lastPoint6 = forgingMap.getLast(address);
        assertEquals(lastPoint6, null);

    }

    @Test
    public void delete_wrong() {

        init();

        int height = 123;
        int weight = 34789;

        forgingMap.putAndProcess(address, height, weight);
        currentPoint = new Tuple3<>(height, weight, 0);
        lastPoint = forgingMap.getLast(address);

        Tuple2<String, Integer> key = new Tuple2<>(address, height);
        Tuple3<Integer, Integer, Integer> point = forgingMap.get(key);

        assertEquals(point, null);
        assertEquals(currentPoint, lastPoint);

        height += 100;
        weight -= 1000;
        Tuple3<Integer, Integer, Integer> currentPoint2 = new Tuple3<>(height, weight, 0);
        forgingMap.putAndProcess(address, height, weight);

        Tuple3<Integer, Integer, Integer> lastPoint2 = forgingMap.getLast(address);
        assertEquals(currentPoint2, lastPoint2);

        //Tuple2<String, Integer> key2 = new Tuple2<>(address, height);

        // TRY DELETE
        Tuple2<String, Integer> key3 = new Tuple2<>(address, height - 100);
        /// здес должна быть ошибка поидее
        point = forgingMap.removeAndProcess(key3);

    }

    @Test
    public void testDelete() {
    }
}