package org.erachain.datachain;
//04/01 +- 

//import java.lang.reflect.Array;

import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.database.serializer.ExLinkSerializer;
import org.erachain.dbs.DBTab;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeMap;
import org.mapdb.DB;

import java.util.Map;


/**
 * Ссылка на другую транзакцию<br><br>
 * Ключ: ссылка на эту транзакцию (с ссылкой).<br>
 * Значение: ExLink
 *
 * @return dcMap
 */

public class ExLinksMap extends DCUMap<Long, ExLink> {

    public ExLinksMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_VOUCH_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_VOUCH_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_VOUCH_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_VOUCH_TYPE);
        }
    }

    public ExLinksMap(ExLinksMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    //@SuppressWarnings("unchecked")
    private Map<Long, ExLink> openMap(DB database) {

        BTreeMap<Long, ExLink> map =
                database.createTreeMap("ex_links_records")
                        .valueSerializer(new ExLinkSerializer())
                        .makeOrGet();
        return map;

    }


    @Override
    public void openMap() {
        //OPEN MAP
        map = openMap(database);
    }

    @Override
    protected void getMemoryMap() {
        openMap();
    }

    //public void put(Long dbRef, ExLink exLink) {
    //}
}
