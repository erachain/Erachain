package org.erachain.dbs.rocksDB.indexes;

import lombok.Getter;

import java.util.function.BiFunction;

@Getter
public class SimpleIndexDB<K, V, R> extends IndexDB {

    public SimpleIndexDB(String nameIndex, BiFunction<K, V, R> biFunction, IndexByteable<R, K> indexByteable) {
        super(nameIndex, indexByteable);
        this.biFunction = biFunction;
    }

    public BiFunction<K, V, R> biFunction;
}