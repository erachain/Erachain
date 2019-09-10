package org.erachain.dbs.rocksDB.indexes;

import lombok.Getter;
import lombok.Setter;
import org.rocksdb.ColumnFamilyHandle;

import java.util.function.BiFunction;
@Getter
public class IndexDB {

    public IndexDB(String nameIndex, IndexByteable indexByteable) {
        this.nameIndex = nameIndex;
        this.indexByteable = indexByteable;
    }

    private String nameIndex;

    private IndexByteable indexByteable;
    @Setter
    private ColumnFamilyHandle columnFamilyHandle;


}
