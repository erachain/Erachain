package org.erachain.datachain;
//04/01 +- 

//import java.lang.reflect.Array;

import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.database.serializer.ExLinkSerializer;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.util.Map;
import java.util.NavigableSet;


/**
 * Ключ: ссылка на эту транзакцию (с ExLink) + ссылка из ExLink.<br>
 * Значение: ExLink
 *
 * @return dcMap
 */

public class ExLinksMap extends DCUMap<Tuple2<Long, Long>, ExLink> {

    @SuppressWarnings("rawtypes")
    private NavigableSet parentLinks;

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

    //@SuppressWarnings("unchecked")
    private Map<Tuple2<Long, Long>, ExLink> openMap(DB database) {

        BTreeMap<Tuple2<Long, Long>, ExLink> map =
                database.createTreeMap("ex_links")
                        .valueSerializer(new ExLinkSerializer())
                        .makeOrGet();

        this.parentLinks = database.createTreeSet("parent_ex_links")
                .makeOrGet();

        // в БИНЕ внутри уникальные ключи создаются добавлением основного ключа
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.parentLinks,
                new Fun.Function2<Tuple2<Long, Byte>, Tuple2<Long, Long>, ExLink>() {
                    @Override
                    public Tuple2<Long, Byte> run(Tuple2<Long, Long> key, ExLink exLink) {
                        return new Tuple2<Long, Byte>(key.b, exLink.getType());
                    }
                });

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

    public IteratorCloseable<Tuple2<Long, Long>> getTXLinksIterator(Long dbRef, Byte type, boolean descending) {
        return IteratorCloseableImpl.make(Fun.filter(descending ? this.parentLinks.descendingSet() : this.parentLinks,
                new Tuple2<Long, Byte>(dbRef, type)).iterator());

    }

    public void put(Long parentRef, ExLink exLink) {
        super.put(new Tuple2<>(exLink.getRef(), parentRef), exLink);
    }

    public void remove(Long parentRef, Long exLinkRef) {
        super.remove(new Tuple2<>(exLinkRef, parentRef));
    }
}
