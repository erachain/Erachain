package org.erachain.dbs.rocksDB.indexes;

import lombok.Getter;
import lombok.Setter;
import org.rocksdb.ColumnFamilyHandle;

import java.util.function.BiFunction;
@Getter
public class ArrayIndexDB<K, V, R> extends IndexDB{
    public ArrayIndexDB(String nameIndex, BiFunction<K, V, R[]> biFunction, IndexByteable<R, K> indexByteable) {
        super(nameIndex, indexByteable);
        this.biFunction = biFunction;
    }

    private BiFunction<K, V, R[]> biFunction;
}
