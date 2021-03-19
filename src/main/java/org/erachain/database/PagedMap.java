package org.erachain.database;

import org.erachain.dbs.IMap;
import org.erachain.dbs.IteratorCloseable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PagedMap<T, U> {

    public boolean filerRows() {
        return false;
    }

    Object[] args;
    IMap map;
    long timestamp;

    public PagedMap(IMap map, Object[] args) {
        this.map = map;
        this.args = args;
    }

    public List<U> getPageList(T fromKey, int offset, int limit, boolean fillFullPage) {

        timestamp = System.currentTimeMillis();

        List<U> rows = new ArrayList<>();
        U row;
        T key;

        if (offset < 0 || limit < 0) {
            if (limit < 0)
                limit = -limit;

            // надо отмотать назад (вверх) - то есть нашли точку и в обратном направлении пропускаем
            // и по пути создаем список обратный что нашли по обратному итератору
            int offsetHere = -(offset + limit);
            try (IteratorCloseable<T> iterator = map.getIterator(fromKey, false)) {
                int skipped = 0;
                int count = 0;
                while (iterator.hasNext() && (limit <= 0 || count < limit)) {
                    key = iterator.next();
                    row = (U) map.get(key);
                    if (filerRows()) {
                        continue;
                    }

                    if (offsetHere > 0 && skipped++ < offsetHere) {
                        continue;
                    }

                    count++;

                    // обратный отсчет в списке
                    rows.add(0, row);
                }

                if (fillFullPage && fromKey != null // && fromKey != 0
                        && limit > 0 && count < limit) {
                    // сюда пришло значит не полный список - дополним его
                    for (U pageRow : getPageList(fromKey, 0, limit - count, false)) {
                        boolean exist = false;
                        for (U rowHere : rows) {
                            if (pageRow.equals(rowHere)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            rows.add(pageRow);
                        }
                    }
                }

            } catch (IOException e) {
            }

        } else {

            try (IteratorCloseable<T> iterator = map.getIterator(fromKey, true)) {
                int skipped = 0;
                int count = 0;
                while (iterator.hasNext() && (limit <= 0 || count < limit)) {
                    key = iterator.next();
                    row = (U) map.get(key);
                    if (filerRows()) {
                        continue;
                    }

                    if (offset > 0 && skipped++ < offset) {
                        continue;
                    }

                    count++;

                    rows.add(row);
                }

                if (fillFullPage && fromKey != null // && fromKey != 0
                        && limit > 0 && count < limit) {
                    // сюда пришло значит не полный список - дополним его
                    int index = 0;
                    int limitLeft = limit - count;
                    for (U pageRow : getPageList(fromKey, -(limitLeft + (count > 0 ? 1 : 0)), limitLeft, false)) {
                        boolean exist = false;
                        for (U rowHere : rows) {
                            if (pageRow.equals(rowHere)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            rows.add(index++, pageRow);
                        }
                    }
                }

            } catch (IOException e) {
            }
        }
        return rows;
    }

}
