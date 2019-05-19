package org.erachain.database;

import org.erachain.utils.Pair;

import java.util.List;

public interface FilteredByStringArray<T> {

    //abstract Pair<String, Iterable> getKeysByFilterAsArray(String filter, int offset, int limit);

    abstract List<T> getKeysByFilterAsArray(String filter, int offset, int limit);

    }
