package org.erachain.datachain;

import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.ReferenceSuitMapDB;
import org.erachain.dbs.mapDB.ReferenceSuitMapDBFork;
import org.erachain.dbs.nativeMemMap.nativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.ReferenceSuitRocksDB;
import org.mapdb.DB;
import org.mapdb.Fun;

import static org.erachain.database.IDB.*;


/**
 * seek reference to tx_Parent by address+timestamp
 * account.address -> LAST[TX.timestamp + TX.dbRef]
 * account.address + TX.timestamp -> PARENT[TX.timestamp + TX.dbRef]
 */
public class ReferenceMapImpl extends DBTabImpl<byte[], long[]>
        implements ReferenceMap {

    public ReferenceMapImpl(int dbsUsed, DCSet databaseSet, DB database) {
        super(dbsUsed, databaseSet, database);
    }

    public ReferenceMapImpl(int dbsUsed, ReferenceMap parent, DCSet dcSet) {
        super(dbsUsed, parent, dcSet);
    }

    @Override
    protected void getMap() {
        //OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    //map = new ReferenceSuitMapDB(databaseSet, database);
                    //break;
                case DBS_MAP_DB_IN_MEM:
                    map = new ReferenceSuitRocksDB(databaseSet, database);
                    break;
                default:
                    map = new ReferenceSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_MAP_DB:
                    map = new ReferenceSuitMapDBFork((ReferenceMap) parent, databaseSet);
                    //break;
                case DBS_ROCK_DB:
                    //map = new ReferenceSuitRocksDBFork((ReferenceMap) parent, databaseSet);
                    //break;
                default:
                    map = new nativeMapTreeMapFork(parent, databaseSet, Fun.BYTE_ARRAY_COMPARATOR, null);
            }
        }
    }

    /*
    @Override
    protected void getMemoryMap() {
        if (database == null) {
            map = new TreeMap<>(UnsignedBytes.lexicographicalComparator());
        } else {
            getMap();
        }
    }
     */

}