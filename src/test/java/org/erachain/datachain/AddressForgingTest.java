package org.erachain.datachain;

import org.junit.Test;
import org.mapdb.Fun.Tuple2;

import static org.junit.Assert.assertEquals;

public class AddressForgingTest {

    DCSet db;
    String address = "7CzxxwH7u9aQtx5iNHskLQjyJvybyKg8rF";
    AddressForging forgingMap;
    Tuple2<String, Integer> key;
    Tuple2<Integer, Integer> lastPoint;
    Tuple2<Integer, Integer> currentPoint;

    private void init() {

        db = DCSet.createEmptyDatabaseSet();
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

        forgingMap.set(address, height, weight);
        currentPoint = new Tuple2<>(height, weight);
        lastPoint = forgingMap.getLast(address);

        Tuple2<String, Integer> key = new Tuple2<>(address, height);
        Tuple2<Integer, Integer> point = forgingMap.get(key);

        assertEquals(point, null);
        assertEquals(currentPoint, lastPoint);

        height += 100;
        weight -= 1000;
        Tuple2<Integer, Integer> currentPoint2 = new Tuple2<>(height, weight);
        forgingMap.set(address, height, weight);

        Tuple2<Integer, Integer> lastPoint2 = forgingMap.getLast(address);
        assertEquals(currentPoint2, lastPoint2);

        Tuple2<String, Integer> key2 = new Tuple2<>(address, height);
        point = forgingMap.get(key2);
        assertEquals(point, lastPoint);

        // SET SAME height
        weight += 333;
        forgingMap.set(address, height, weight);

        Tuple2<Integer, Integer> lastPoint3 = forgingMap.getLast(address);
        assertEquals(lastPoint3, new Tuple2<>(height, weight));

        point = forgingMap.get(key2);
        assertEquals(point, lastPoint);

    }

    @Test
    public void set_wrong() {

        init();

        int height = 123;
        int weight = 34789;

        forgingMap.set(address, height, weight);
        currentPoint = new Tuple2<>(height, weight);
        lastPoint = forgingMap.getLast(address);

        Tuple2<String, Integer> key = new Tuple2<>(address, height);
        Tuple2<Integer, Integer> point = forgingMap.get(key);

        assertEquals(point, null);
        assertEquals(currentPoint, lastPoint);

        // WRONG
        height -= 100;
        weight -= 1000;
        Tuple2<Integer, Integer> currentPoint2 = new Tuple2<>(height, weight);
        try {
            forgingMap.set(address, height, weight);
            assert(false);
        } catch (java.lang.AssertionError e) {

        }

    }

    @Test
    public void delete() {

        init();

        int height = 123;
        int weight = 34789;

        forgingMap.set(address, height, weight);
        currentPoint = new Tuple2<>(height, weight);
        lastPoint = forgingMap.getLast(address);

        Tuple2<String, Integer> key = new Tuple2<>(address, height);
        Tuple2<Integer, Integer> point = forgingMap.get(key);

        assertEquals(point, null);
        assertEquals(currentPoint, lastPoint);

        height += 100;
        weight -= 1000;
        Tuple2<Integer, Integer> currentPoint2 = new Tuple2<>(height, weight);
        forgingMap.set(address, height, weight);

        Tuple2<Integer, Integer> lastPoint2 = forgingMap.getLast(address);
        assertEquals(currentPoint2, lastPoint2);

        Tuple2<String, Integer> key2 = new Tuple2<>(address, height);
        point = forgingMap.get(key2);
        assertEquals(point, lastPoint);

        // SET SAME height
        weight += 5000;
        forgingMap.set(address, height, weight);

        Tuple2<Integer, Integer> lastPoint3 = forgingMap.getLast(address);
        assertEquals(lastPoint3, new Tuple2<>(height, weight));

        point = forgingMap.get(key2);
        assertEquals(point, lastPoint);

        // TRY DELETE
        Tuple2<String, Integer> key3 = new Tuple2<>(address, lastPoint3.a);
        point = forgingMap.delete(key3);

        Tuple2<Integer, Integer> lastPoint4 = forgingMap.getLast(address);
        Tuple2<Integer, Integer> prevPoint4 = forgingMap.get(address, lastPoint4.b);

        assertEquals(lastPoint4, lastPoint);
        assertEquals(prevPoint4, null);

        // TRY DELETE TWICE
        Tuple2<String, Integer> key4 = new Tuple2<>(address, lastPoint3.a);
        point = forgingMap.delete(key4);

        Tuple2<Integer, Integer> lastPoint5 = forgingMap.getLast(address);
        Tuple2<Integer, Integer> prevPoint5 = forgingMap.get(address, lastPoint5.b);

        assertEquals(lastPoint5, lastPoint);
        assertEquals(prevPoint5, null);

    }

    @Test
    public void delete_wrong() {

        init();

        int height = 123;
        int weight = 34789;

        forgingMap.set(address, height, weight);
        currentPoint = new Tuple2<>(height, weight);
        lastPoint = forgingMap.getLast(address);

        Tuple2<String, Integer> key = new Tuple2<>(address, height);
        Tuple2<Integer, Integer> point = forgingMap.get(key);

        assertEquals(point, null);
        assertEquals(currentPoint, lastPoint);

        height += 100;
        weight -= 1000;
        Tuple2<Integer, Integer> currentPoint2 = new Tuple2<>(height, weight);
        forgingMap.set(address, height, weight);

        Tuple2<Integer, Integer> lastPoint2 = forgingMap.getLast(address);
        assertEquals(currentPoint2, lastPoint2);

        Tuple2<String, Integer> key2 = new Tuple2<>(address, height);

        // TRY DELETE
        Tuple2<String, Integer> key3 = new Tuple2<>(address, height - 100);
        point = forgingMap.delete(key3);

    }

    @Test
    public void testDelete() {
    }
}