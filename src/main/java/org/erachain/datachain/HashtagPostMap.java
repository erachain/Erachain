package org.erachain.datachain;

import org.erachain.utils.ByteArrayUtils;
import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HashtagPostMap extends DCUMap<String, List<byte[]>> {

    public HashtagPostMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public HashtagPostMap(DCUMap<String, List<byte[]>> parent) {
        super(parent, null);
    }


    @Override
    public void openMap() {
        // / OPEN MAP
        BTreeMapMaker createTreeMap = database.createTreeMap("HashtagPostMap");
        map = createTreeMap.makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<>();
    }


    public void add(String hashtag, byte[] signature) {
        //no difference between lower and uppercase here
        hashtag = hashtag.toLowerCase();

        List<byte[]> list;
        list = get(hashtag);

        if (list == null) {
            list = new ArrayList<>();
        }

        if (!ByteArrayUtils.contains(list, signature)) {
            list.add(signature);
        }

        put(hashtag, list);

    }

    public void remove(String hashtag, byte[] signature) {
        //no difference between lower and uppercase here
        hashtag = hashtag.toLowerCase();

        if (contains(hashtag)) {
            List<byte[]> list = get(hashtag);
            ByteArrayUtils.remove(list, signature);
            put(hashtag, list);
        }

    }

}
