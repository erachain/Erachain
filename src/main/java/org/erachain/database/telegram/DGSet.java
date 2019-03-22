package org.erachain.database.telegram;
// 30/03 ++

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.database.DBASet;
import org.erachain.settings.Settings;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;

//import org.mapdb.Serializer;

public class DGSet extends DBASet {

    private AllTelegramsMap telegramsMap;

    public DGSet(File dbFile, DB database, boolean withObserver, boolean dynamicGUI) {
        super(dbFile, database, withObserver, dynamicGUI);

        this.telegramsMap = new AllTelegramsMap(this, this.database);

    }

    public static DGSet reCreateDB(boolean withObserver, boolean dynamicGUI) {

        File dbFile = new File(Settings.getInstance().getTelegramDir(), "telegram.dat");
        dbFile.getParentFile().mkdirs();

        DB database = DBMaker.newFileDB(dbFile)
                // .cacheSize(2048)
                // .cacheDisable()

                // это чистит сама память если соталось 25% от кучи - так что она безопасная
                ///.cacheHardRefEnable()
                //.cacheLRUEnable()
                //.cacheSoftRefEnable()
                ///.cacheWeakRefEnable()

                // количество точек в таблице которые хранятся в HashMap как в КЭШе
                .cacheSize(1000)

                .checksumEnable()
                .mmapFileEnableIfSupported()

                // если при записи на диск блока процессор сильно нагружается - то уменьшить это
                .freeSpaceReclaimQ(7) // не нагружать процессор для поиска свободного места в базе данных

                //.compressionEnable()

                .transactionDisable() // DISABLE TRANSACTIONS
                .make();

        return new DGSet(dbFile, database, withObserver, dynamicGUI);

    }


    public AllTelegramsMap getTelegramsMap() {
        return this.telegramsMap;
    }

    public void delete(PublicKeyAccount account) {
        this.uses++;
        this.telegramsMap.deleteFromAccount(account);
        this.uses--;

    }

}