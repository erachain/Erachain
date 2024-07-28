package org.erachain.database;
// 30/03 ++

import lombok.extern.slf4j.Slf4j;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public class DTHSet extends DBASet {

    /**
     * Trades History DBA
     * New version will auto-rebase DSet from empty db file
     */
    final static int CURRENT_VERSION = 0;

    TradeHistoryDayMap tradesHistoryDayMap;
    TradeHistoryMonthMap tradesHistoryMonthMap;

    public DTHSet(File dbFile, DB database, boolean withObserver, boolean dynamicGUI) {
        super(dbFile, database, withObserver, dynamicGUI);
        this.tradesHistoryDayMap = new TradeHistoryDayMap(this, this.database);
        this.tradesHistoryMonthMap = new TradeHistoryMonthMap(this, this.database);
    }

    static private DB makeDB(File dbFile) {

        boolean isNew = !dbFile.exists();
        if (isNew) {
            dbFile.getParentFile().mkdirs();
        }

        DB database = DBMaker.newFileDB(dbFile)
                // убрал .closeOnJvmShutdown() it closing not by my code and rise errors! closed before my closing

                .checksumEnable()
                .mmapFileEnableIfSupported() // ++ but -- error on asyncWriteEnable

                // тормозит сильно но возможно когда файл большеой не падает скорость сильно
                // вдобавок не сохраняет на диск даже Транзакционный файл и КРАХ теряет данные
                // НЕ ВКЛЮЧАТЬ!
                // .mmapFileEnablePartial()

                // Если этот отключить (закомментировать) то файлы на лету не обновляются на диске а только в момент Флуша
                // типа быстрее работают но по факту с Флушем нет и в описании предупрежджение - что
                // при крахе системы в момент флуша можно потерять данные - так как в Транзакционный фал изменения
                // катаются так же в момент флуша - а не при изменении данных
                // так что этот ключ тут полезный
                .commitFileSyncDisable() // ++

                //.snapshotEnable()
                //.asyncWriteEnable() - крах при коммитах и откатах тразакций - возможно надо asyncWriteFlushDelay больше задавать
                //.asyncWriteFlushDelay(2)

                // если при записи на диск блока процессор сильно нагружается - то уменьшить это
                .freeSpaceReclaimQ(10)// не нагружать процессор для поиска свободного места в базе данных

                .cacheLRUEnable()
                .compressionEnable()
                .make();

        if (isNew)
            DBASet.setVersion(database, CURRENT_VERSION);

        return database;

    }

    public static DTHSet reCreateDB() {

        //OPEN DB
        File dbFile = new File("data/trades", "history.dat");

        DB database = null;
        try {
            database = makeDB(dbFile);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            try {
                Files.walkFileTree(dbFile.getParentFile().toPath(),
                        new SimpleFileVisitorForRecursiveFolderDeletion());
            } catch (Throwable e1) {
                logger.error(e1.getMessage(), e1);
            }
            database = makeDB(dbFile);
        }

        if (DBASet.getVersion(database) < CURRENT_VERSION) {
            database.close();
            logger.warn("New Version: " + CURRENT_VERSION + ". Try remake DTHSet.");
            try {
                Files.walkFileTree(dbFile.getParentFile().toPath(),
                        new SimpleFileVisitorForRecursiveFolderDeletion());
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
            database = makeDB(dbFile);

        }

        return new DTHSet(dbFile, database, true, true);

    }

    public TradeHistoryDayMap getTradesHistoryDayMap() {
        return this.tradesHistoryDayMap;
    }

    public TradeHistoryMonthMap getTradesHistoryMonthMap() {
        return this.tradesHistoryMonthMap;
    }

    @Override
    public void close() {

        if (this.database == null || this.database.isClosed())
            return;

        this.uses++;
        this.database.close();
        // улучшает работу финалайзера
        this.tables = null;
        this.uses--;

    }

}