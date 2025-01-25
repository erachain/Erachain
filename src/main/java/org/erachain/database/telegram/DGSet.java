package org.erachain.database.telegram;
// 30/03 ++

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.database.DBASet;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

//import org.mapdb.Serializer;

@Slf4j
public class DGSet extends DBASet {

    private AllTelegramsMap telegramsMap;

    public DGSet(File dbFile, DB database, boolean withObserver, boolean dynamicGUI) {
        super(dbFile, database, withObserver, dynamicGUI);

        this.telegramsMap = new AllTelegramsMap(this, this.database);

    }

    public static DB makeDB(File dbFile) {

        dbFile.getParentFile().mkdirs();

        return DBMaker.newFileDB(dbFile)
                // .cacheSize(2048)
                // .cacheDisable()

                // это чистит сама память если соталось 25% от кучи - так что она безопасная
                ///.cacheHardRefEnable()
                //.cacheLRUEnable()
                //.cacheSoftRefEnable()
                ///.cacheWeakRefEnable()

                // количество точек в таблице которые хранятся в HashMap как в КЭШе
                .cacheSize(1 << 10)

                .checksumEnable()
                .mmapFileEnableIfSupported()

                // если при записи на диск блока процессор сильно нагружается - то уменьшить это
                .freeSpaceReclaimQ(7) // не нагружать процессор для поиска свободного места в базе данных

                //.compressionEnable()

                .transactionDisable() // DISABLE TRANSACTIONS
                .make();

    }

    public static DGSet reCreateDB(boolean withObserver, boolean dynamicGUI) {

        File dbFile = new File(Settings.getInstance().getTelegramDir(), "telegram.dat");

        DB database = null;
        try {
            database = makeDB(dbFile);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            try {
                Files.walkFileTree(dbFile.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
            } catch (NoSuchFileException e1) {
            } catch (Throwable e1) {
                logger.error(e.getMessage(), e1);
            }

            database = makeDB(dbFile);
        }

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

    @Override
    public void close() {

        if (this.database == null || this.database.isClosed())
            return;

        this.uses++;
        this.database.close();
        this.tables = null;
        this.uses--;

    }

}