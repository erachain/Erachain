package org.erachain.database;

import java.util.List;

public interface FilteredByStringArray<T> {

    List<T> getKeysByFilterAsArray(String filter, int offset, int limit);

}