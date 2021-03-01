package org.erachain.database;
// 30/03 ++

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.dbs.DBTab;
import org.mapdb.DB;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import org.mapdb.Serializer;

@Slf4j
abstract public class DBASet implements IDB {

    private static final String VERSION = "version";

    int DBS_USED;

    protected File DATA_FILE;
    public DB database;
    protected List<DBTab> tables = new ArrayList<>();

    protected int uses;

    private boolean withObserver;// observe
    private boolean dynamicGUI;// observe

    public DBASet() {
    }

    /**
     *
     * @param DATA_FILE
     * @param database общая база данных для данного набора - вообще надо ее в набор свтавить и все.
     *               У каждой таблицы внутри может своя база данных открытьваться.
     *               А команды базы данных типа close commit должны из таблицы передаваться в свою.
     *               Если в общей базе таблица, то не нужно обработка так как она делается в наборе наверху
     * @param withObserver
     * @param dynamicGUI
     */
    public DBASet(File DATA_FILE, DB database, boolean withObserver, boolean dynamicGUI) {
        this.DATA_FILE = DATA_FILE;
        this.withObserver = withObserver;
        this.dynamicGUI = dynamicGUI;
        this.database = database;
    }

    public DB getDatabase() {
        return this.database;
    }

    public int getVersion() {
        this.uses++;
        int u = getVersion(database);
        this.uses--;
        return u;
    }

    public static int getVersion(DB database) {
        return database.getAtomicInteger(VERSION).intValue();
    }

    public File getFile() {
        return DATA_FILE;
    }

    public void setVersion(int version) {
        this.uses++;
        this.database.getAtomicInteger(VERSION).set(version);
        this.uses--;
    }

    public static void setVersion(DB database, int version) {
        database.getAtomicInteger(VERSION).set(version);
    }

    public void addTable(DBTab table) {
        tables.add(table);
    }

    public boolean isWithObserver() {
        return this.withObserver;
    }

    public boolean isDynamicGUI() {
        return this.dynamicGUI;
    }

    public boolean exists() {
        return DATA_FILE.exists();
    }

    public void addUses() {
        this.uses++;
    }

    public void outUses() {
        this.uses--;
    }

    public boolean isBusy() {
        if (this.uses > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void clearCache() {
        logger.debug("CLEAR CACHE");
        this.database.getEngine().clearCache();
    }

    /**
     * !!! ВНИМАНИЕ !!! В общем в записях (транзакциях) нельзя делать индексы на основе других записей,
     * иначе в момент слива форкнутой базы если запись по ссылке была удалена ранее чем удаляется текущая,
     * то ключи будут битые или с Нуль и ошибку вызовут.
     */
    public void writeToParent() {
        this.addUses();

        try {

        } catch (java.lang.OutOfMemoryError e) {
            logger.error(e.getMessage(), e);
            this.outUses();
            Controller.getInstance().stopAll(1013);
        } finally {
            this.outUses();
        }

    }

    public void commit() {
        this.uses++;
        this.database.commit();
        this.uses--;

    }

    public void rollback() {
        this.uses++;
        this.database.rollback();
        this.uses--;

    }

    abstract public void close();

}