package org.erachain.database;

import org.erachain.dbs.IMap;
import org.erachain.dbs.IteratorCloseable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Make pages by Index (Secondary Key)
 *
 * @param <T> primary Key
 * @param <K> secondary key
 * @param <U> Value for page rows
 */
public abstract class PagedIndexMap<T, K, U> {

    public boolean filterRows() {
        return false;
    }

    public void rowCalc() {
    }

    protected IMap mapImpl;
    protected long timestamp;

    protected U currentRow;

    public PagedIndexMap(IMap mapImpl) {
        this.mapImpl = mapImpl;
    }

    public abstract IteratorCloseable<T> getIterator(K fromSecondaryKey, boolean descending);

    public abstract K makeSecondaryKey(T key, U value);

    public U get(T key) {
        return (U) mapImpl.get(key);
    }

    public List<U> getPageList(K fromSecondaryKey, int offset, int limit, boolean fillFullPage) {

        timestamp = System.currentTimeMillis();

        List<U> rows = new ArrayList<>();
        T key = null;

        if (offset < 0 || limit < 0) {
            if (limit < 0)
                limit = -limit;

            // надо отмотать назад (вверх) - то есть нашли точку и в обратном направлении пропускаем
            // и по пути создаем список обратный что нашли по обратному итератору
            int offsetHere = -(offset + limit);
            try (IteratorCloseable<T> iterator = getIterator(fromSecondaryKey, false)) {
                int skipped = 0;
                int count = 0;
                while (iterator.hasNext() && (limit <= 0 || count < limit)) {
                    key = iterator.next();
                    currentRow = get(key);
                    if (currentRow == null || filterRows()) {
                        continue;
                    }

                    if (offsetHere > 0 && skipped++ < offsetHere) {
                        continue;
                    }

                    count++;

                    // обратный отсчет в списке
                    rowCalc();
                    rows.add(0, currentRow);
                }

                if (fillFullPage && key != null // && fromKey != 0
                        && limit > 0 && count < limit) {
                    // сюда пришло значит не полный список - дополним его

                    // возьмем новый вторичный ключ для старта с текущего значения
                    fromSecondaryKey = makeSecondaryKey(key, currentRow);
                    for (U pageRow : getPageList(fromSecondaryKey, 0, limit - count, false)) {
                        if (currentRow == null)
                            continue;

                        currentRow = pageRow;
                        boolean exist = false;
                        for (U rowHere : rows) {
                            if (pageRow.equals(rowHere)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            if (filterRows()) {
                                continue;
                            }
                            rowCalc();
                            rows.add(currentRow);
                        }
                    }
                }

            } catch (IOException e) {
                String mess = e.getLocalizedMessage();
            }

        } else {

            try (IteratorCloseable<T> iterator = getIterator(fromSecondaryKey, true)) {
                int skipped = 0;
                int count = 0;
                while (iterator.hasNext() && (limit <= 0 || count < limit)) {

                    if (false && System.currentTimeMillis() - timestamp > 5000) {
                        break;
                    }

                    key = iterator.next();
                    currentRow = get(key);
                    if (currentRow == null || filterRows()) {
                        continue;
                    }

                    if (offset > 0 && skipped++ < offset) {
                        continue;
                    }

                    count++;

                    rowCalc();
                    rows.add(currentRow);
                }

                if (fillFullPage && key != null // && fromKey != 0
                        && limit > 0 && count < limit) {
                    // сюда пришло значит не полный список - дополним его
                    int index = 0;
                    int limitLeft = limit - count;
                    // возьмем новый вторичный ключ для старта с текущего значения
                    fromSecondaryKey = makeSecondaryKey(key, currentRow);
                    for (U pageRow : getPageList(fromSecondaryKey, -(limitLeft + (count > 0 ? 1 : 0)), limitLeft, false)) {
                        currentRow = pageRow;
                        if (currentRow == null)
                            continue;

                        boolean exist = false;
                        for (U rowHere : rows) {
                            if (pageRow.equals(rowHere)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            if (filterRows()) {
                                continue;
                            }
                            rowCalc();
                            rows.add(index++, currentRow);
                        }
                    }
                }

            } catch (IOException e) {
                String mess = e.getLocalizedMessage();
            }
        }
        return rows;
    }

    public List<T> getPageKeysList(K fromSecondaryKey, int offset, int limit, boolean fillFullPage) {

        timestamp = System.currentTimeMillis();

        List<T> keys = new ArrayList<>();
        T key;

        if (offset < 0 || limit < 0) {
            if (limit < 0)
                limit = -limit;

            // надо отмотать назад (вверх) - то есть нашли точку и в обратном направлении пропускаем
            // и по пути создаем список обратный что нашли по обратному итератору
            int offsetHere = -(offset + limit);
            try (IteratorCloseable<T> iterator = getIterator(fromSecondaryKey, false)) {
                int skipped = 0;
                int count = 0;
                while (iterator.hasNext() && (limit <= 0 || count < limit)) {
                    key = iterator.next();

                    if (offsetHere > 0 && skipped++ < offsetHere) {
                        continue;
                    }

                    count++;

                    // обратный отсчет в списке
                    keys.add(0, key);
                }

                if (fillFullPage && fromSecondaryKey != null // && fromKey != 0
                        && limit > 0 && count < limit) {
                    // сюда пришло значит не полный список - дополним его
                    for (T pageKey : getPageKeysList(fromSecondaryKey, 0, limit - count, false)) {
                        boolean exist = false;
                        for (T keyHere : keys) {
                            if (pageKey.equals(keyHere)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            keys.add(pageKey);
                        }
                    }
                }

            } catch (IOException e) {
            }

        } else {

            try (IteratorCloseable<T> iterator = getIterator(fromSecondaryKey, true)) {
                int skipped = 0;
                int count = 0;
                while (iterator.hasNext() && (limit <= 0 || count < limit)) {

                    if (false && System.currentTimeMillis() - timestamp > 5000) {
                        break;
                    }

                    key = iterator.next();

                    if (offset > 0 && skipped++ < offset) {
                        continue;
                    }

                    count++;

                    keys.add(key);
                }

                if (fillFullPage && fromSecondaryKey != null // && fromKey != 0
                        && limit > 0 && count < limit) {
                    // сюда пришло значит не полный список - дополним его
                    int index = 0;
                    int limitLeft = limit - count;
                    for (T pageKey : getPageKeysList(fromSecondaryKey, -(limitLeft + (count > 0 ? 1 : 0)), limitLeft, false)) {
                        boolean exist = false;
                        for (T keyHere : keys) {
                            if (pageKey.equals(keyHere)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            keys.add(index++, pageKey);
                        }
                    }
                }

            } catch (IOException e) {
            }
        }
        return keys;
    }

}
