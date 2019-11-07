package org.erachain.dbs.mapDB;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.block.GenesisBlock;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;
import org.mapdb.Fun;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.assertEquals;

@Slf4j
public class DBMapSuitForkTest {

    int[] TESTED_DBS = new int[]{
            //IDB.DBS_MAP_DB,
            IDB.DBS_ROCK_DB};

    DCSet dcSet;
    GenesisBlock gb;

    private void init(int dbs) {

        logger.info(" ********** open DBS: " + dbs);

        File tempDir = new File(Settings.getInstance().getDataTempDir());
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        dcSet = DCSet.createEmptyHardDatabaseSetWithFlush(null, dbs);
        gb = new GenesisBlock();

        try {
            gb.process(dcSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * не удаляет одинаковые ключи
     */
    @Test
    public void iteratorMerge() {
        Set<Long> list1 = new TreeSet<Long>() {{
            add(10L);
            add(112L);
            add(212L);
        }};

        List<Long> list2 = new ArrayList<Long>() {{
            add(112L);
            add(200L);
        }};

        Iterator<Long> iter1 = list1.iterator();
        Iterator<Long> iter2 = list2.iterator();

        // тут будет просто сложение - все элементы войдут, даже повторение
        Iterator<Long> iterator = Iterators.mergeSorted((Iterable) ImmutableList.of(iter1, iter2), Fun.COMPARATOR);

        int count = 0;
        while (iterator.hasNext()) {
            Long key = iterator.next();
            count++;
        }
        assertEquals(count, 5);

        /// оказывается итератор уже перебрали там и он в конце!
        iter1 = list1.iterator();
        count = 0;
        while (iter1.hasNext()) {
            Long key = iter1.next();
            count++;
        }
        assertEquals(count, 3);
        assertEquals(Iterators.size(iter1), 0);
        iter1 = list1.iterator();
        assertEquals(Iterators.size(iter1), 3);
        // тут он уже сброшен ы конец
        assertEquals(Iterators.size(iter1), 0);

        // заново возьмем
        iter1 = list1.iterator();
        iter2 = list2.iterator();
        // удалим повторы

        Iterable iterable = (Iterable) Iterables.mergeSorted((Iterable) ImmutableList.of(list1, list2), Fun.COMPARATOR);
        iter1 = iterable.iterator();
        count = 0;
        while (iter1.hasNext()) {
            Long key = iter1.next();
            count++;
        }
        /// так же все сложит без удления дубляжей
        assertEquals(count, 5);

        count = 0;
        while (iter2.hasNext()) {
            Long key = iter2.next();
            count++;
        }
        assertEquals(count, 3);

        iterator = Iterators.mergeSorted((Iterable) ImmutableList.of(iter1, iter2), Fun.COMPARATOR);

        count = 0;
        while (iterator.hasNext()) {
            Long key = iterator.next();
            count++;
        }

        assertEquals(count, 4);

    }

    /**
     * не удаляет одинаковые ключи
     */
    @Test
    public void iteratorMergeSet() {
        Set<Long> list1 = new HashSet<Long>() {{
            add(10L);
            add(112L);
            add(212L);
        }};

        Set<Long> list2 = new HashSet<Long>() {{
            add(112L);
            add(200L);
        }};

        Iterator<Long> iter1 = list1.iterator();
        Iterator<Long> iter2 = list2.iterator();

        // тут будет просто сложение - все элементы войдут, даже повторение
        Iterator<Long> iterator = Iterators.mergeSorted((Iterable) ImmutableList.of(iter1, iter2), Fun.COMPARATOR);

        int count = 0;
        while (iterator.hasNext()) {
            Long key = iterator.next();
            count++;
        }
        assertEquals(count, 5);

        /// оказывается итератор уже перебрали там и он в конце!
        iter1 = list1.iterator();
        count = 0;
        while (iter1.hasNext()) {
            Long key = iter1.next();
            count++;
        }
        assertEquals(count, 3);
        assertEquals(Iterators.size(iter1), 0);
        iter1 = list1.iterator();
        assertEquals(Iterators.size(iter1), 3);
        // тут он уже сброшен ы конец
        assertEquals(Iterators.size(iter1), 0);

        // заново возьмем
        iter1 = list1.iterator();
        iter2 = list2.iterator();
        // удалим повторы

        Iterable iterable = (Iterable) Iterables.mergeSorted((Iterable) ImmutableList.of(list1, list2), Fun.COMPARATOR);
        iter1 = iterable.iterator();
        assertEquals(Iterators.size(iter1), 5);
        assertEquals(Iterators.size(iter2), 2);

        iter1 = list1.iterator();
        iter2 = list2.iterator();
        Iterators.addAll(list1, iter2);
        assertEquals(list1.size(), 4);

    }

    @Test
    public void delete() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // TODO нужно сделать проверку на HashMap delete

            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void contains() {
    }

    // TODO нужно проверить на дублирование ключей при сливе с родителем - поидее нельзя чтобы такое происходило
    // см . как сделано в org.erachain.dbs.mapDB.OrdersSuitMapDBFork.getProtocolKeys
    // мам Iterable<Long> mergedIterable = Iterables.mergeSorted - не сработал как надо - в списке окалаось 2 одинаковых ключа
    @Test
    public void getIterator() {

        /// DBMapSuitFork.getIterator()
        // нужно проверить
        DCSet.getInstance().getOrderMap().getProtocolKeys(1, 2, null);
    }
}