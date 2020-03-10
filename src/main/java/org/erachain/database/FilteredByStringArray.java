package org.erachain.database;

import java.util.List;

public interface FilteredByStringArray<T> {

    List<T> getKeysByFilterAsArray(String filter, String fromWord, Long fromSeqNo, int offset, int limit, boolean descending);

}