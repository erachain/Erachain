package org.erachain.database;

import java.util.List;

public interface FilteredByStringArray<U> {

    /**
     * Можно только список уже по значениям делать - так как в них ищется значения не из поискового запроса
     *
     * @param filter
     * @param fromSeqNo
     * @param offset
     * @param limit
     * @return
     */
    List<U> getByFilterAsArray(String filter, Long fromSeqNo, int offset, int limit);

}