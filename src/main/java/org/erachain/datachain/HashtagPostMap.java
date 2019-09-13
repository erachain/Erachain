package org.erachain.datachain;

import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;
import org.erachain.utils.ByteArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HashtagPostMap extends DCMap<String, List<byte[]>> {

    public HashtagPostMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public HashtagPostMap(DCMap<String, List<byte[]>> parent) {
        super(parent, null);
    }


    @Override
    protected void getMap() {
        // / OPEN MAP
        BTreeMapMaker createTreeMap = database.createTreeMap("HashtagPostMap");
        map = createTreeMap.makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<>();
    }

    @Override
    protected List<byte[]> getDefaultValue() {
        return null;
    }

    @Override
    protected void createIndexes() {
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

        set(hashtag, list);

    }

    public void remove(String hashtag, byte[] signature) {
        //no difference between lower and uppercase here
        hashtag = hashtag.toLowerCase();

        if (contains(hashtag)) {
            List<byte[]> list = get(hashtag);
            ByteArrayUtils.remove(list, signature);
            set(hashtag, list);
        }

    }

}
