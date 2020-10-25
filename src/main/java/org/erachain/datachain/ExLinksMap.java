package org.erachain.datachain;
//04/01 +- 

//import java.lang.reflect.Array;

import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.database.serializer.ExLinkSerializer;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple3;

import java.util.NavigableMap;
import java.util.NavigableSet;


/**
 * Ключ: [Parent - ссылка из ExLink, Long:ref] + [ExLink.Type] + [Child - ссылка на эту транзакцию где ExLink, Long:SeqNo].<br>
 * Значение: ExLink
 *
 * @return dcMap
 */

public class ExLinksMap extends DCUMap<Tuple3<Long, Byte, Long>, ExLink> {

    public ExLinksMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_EXLINK_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_EXLINK_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_EXLINK_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_EXLINK_TYPE);
        }
    }

    public ExLinksMap(ExLinksMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap("ex_links")
                .valueSerializer(new ExLinkSerializer())
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        openMap();
    }

    public IteratorCloseable<Tuple3<Long, Byte, Long>> getTXLinksIterator(Long dbRef, Byte type, boolean descending) {
        NavigableSet<Tuple3<Long, Byte, Long>> keySet = (NavigableSet) (descending ?
                ((NavigableMap) map).descendingKeySet() : ((NavigableMap) map.keySet()));

        return IteratorCloseableImpl.make(keySet.subSet(
                new Tuple3<Long, Byte, Long>(dbRef, type, descending ? Long.MAX_VALUE : Long.MIN_VALUE),
                new Tuple3<Long, Byte, Long>(dbRef, type, descending ? Long.MIN_VALUE : Long.MAX_VALUE)
        ).iterator());

    }

    public void put(ExLink exLink, Long thisSeqNo) {
        super.put(new Tuple3<>(exLink.getRef(), exLink.getType(), thisSeqNo), exLink);
    }

    public void remove(Long exLinkRef, Byte type, Long thisSeqNo) {
        super.remove(new Tuple3<>(exLinkRef, type, thisSeqNo));
    }
}
