package org.erachain.dbs.mapDB;

import org.erachain.datachain.DCSet;
import org.junit.Test;

public class DBMapSuitForkTest {

    @Test
    public void delete() {
    }

    @Test
    public void contains() {
    }

    // TODO нужно проверить на дублирование ключей при сливе с родителем - поидее нельзя чтобы такое происходило
    // см . как сделано в org.erachain.dbs.mapDB.OrdersSuitMapDBFork.getSubKeysWithParent
    // мам Iterable<Long> mergedIterable = Iterables.mergeSorted - не сработал как надо - в списке окалаось 2 одинаковых ключа
    @Test
    public void getIterator() {

        /// DBMapSuitFork.getIterator()
        // нужно проверить
        DCSet.getInstance().getOrderMap().getIteratorWithParent(1, 2);
    }
}