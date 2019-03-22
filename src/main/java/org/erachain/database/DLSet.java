package org.erachain.database;
// 30/03 ++

import org.erachain.settings.Settings;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;

public class DLSet extends DBASet {

    private PeerMap peerMap;

    public DLSet(File dbFile, DB database, boolean withObserver, boolean dynamicGUI) {
        super(dbFile, database, withObserver, dynamicGUI);
        this.peerMap = new PeerMap(this, this.database);
    }

    public static DLSet reCreateDB() {

        //OPEN DB
        //OPEN WALLET
        File dbFile = new File(Settings.getInstance().getLocalDir(), "data.dat");
        dbFile.getParentFile().mkdirs();

        DB database = DBMaker.newFileDB(dbFile)

                //// иначе кеширует блок и если в нем удалить трнзакции или еще что то выдаст тут же такой блок с пустыми полями
                ///// добавил dcSet.clearCache(); --
                ///.cacheDisable()

                // это чистит сама память если соталось 25% от кучи - так что она безопасная
                //.cacheHardRefEnable()
                //.cacheLRUEnable()
                ///.cacheSoftRefEnable()
                //.cacheWeakRefEnable()

                // количество точек в таблице которые хранятся в HashMap как в КЭШе
                .cacheSize(1000)

                .checksumEnable()
                .mmapFileEnableIfSupported() // ++
                /// ICREATOR
                .commitFileSyncDisable() // ++

                // если при записи на диск блока процессор сильно нагружается - то уменьшить это
                .freeSpaceReclaimQ(7) // не нагружать процессор для поиска свободного места в базе данных

                //.compressionEnable()

                .transactionDisable()
                .make();

        return new DLSet(dbFile, database, true, true);

    }

    public PeerMap getPeerMap() {
        return this.peerMap;
    }

}