package org.erachain.core.web;

import com.google.common.primitives.SignedBytes;
import org.erachain.datachain.DCUMap;
import org.erachain.datachain.DCSet;
import org.mapdb.DB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SharedPostsMap extends DCUMap<byte[], List<String>> {

    public SharedPostsMap(DCSet dcSet, DB database) {
        super(dcSet, database);
    }

    public SharedPostsMap(DCUMap<byte[], List<String>> parent) {
        super(parent, null);
    }

    @Override
    protected void getMap() {

        map = database.createTreeMap("SharedPostsMap")
                .comparator(SignedBytes.lexicographicalComparator())
                .makeOrGet();

    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<>();
    }

    public void add(byte[] postSignature, String name) {
        List<String> list = get(postSignature);
        if (list == null) {
            list = new ArrayList<String>();
        }

        if (!list.contains(name)) {
            list.add(name);
        }

        set(postSignature, list);
    }

    public void remove(byte[] postSignature, String name) {
        List<String> list = get(postSignature);
        if (list == null) {
            return;
        }

        list.remove(name);

        if (list.isEmpty()) {
            delete(postSignature);
            return;
        }

        set(postSignature, list);
    }

    @Override
    protected void createIndexes() {
    }

    @Override
    protected List<String> getDefaultValue() {
        return null;
    }
}
