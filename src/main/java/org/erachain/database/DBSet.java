package org.erachain.database;
// 30/03 ++

import org.erachain.settings.Settings;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;

public class DBSet extends DBASet {

    private PeerMap peerMap;

    private DBSet() {
        this.peerMap = new PeerMap(this, this.database);
    }


    public static DBSet getinstanse() {
        if (instance == null) {

            DATA_FILE = new File(Settings.getInstance().getLocalDir(), "data.dat");

            reCreateDatabase();
        }

        return instance;

    }

    public static void reCreateDatabase() {

        //OPEN DB
        //OPEN WALLET
        DATA_FILE.getParentFile().mkdirs();

        database = DBMaker.newFileDB(DATA_FILE)

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

        uses = 0;

        //CREATE INSTANCE
        instance = new DBSet();


    }

    public PeerMap getPeerMap() {
        return this.peerMap;
    }

}