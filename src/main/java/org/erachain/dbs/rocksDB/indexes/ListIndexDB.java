package org.erachain.rocksDB.indexes;

import lombok.Getter;

import java.util.List;
import java.util.function.BiFunction;

@Getter
public class ListIndexDB<K, V, R> extends IndexDB{
    public ListIndexDB(String nameIndex, BiFunction<K, V, List<R>> biFunction, IndexByteable<R, K> indexByteable) {
        super(nameIndex, indexByteable);
        this.biFunction = biFunction;
    }

    private BiFunction<K, V, List<R>> biFunction;
}
