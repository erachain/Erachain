package org.erachain.database;

import java.util.List;

public interface Pageable<K, V> {

    List<V> getPage(K start, int offset, int pageSize);
}
