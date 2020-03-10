package org.erachain.datachain;

import org.erachain.utils.ByteArrayUtils;
import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlogPostMap extends DCUMap<String, List<byte[]>> {

    public final static String MAINBLOG = "Erachain.org";

    public BlogPostMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public BlogPostMap(DCUMap<String, List<byte[]>> parent) {
        super(parent, null);
    }

    @Override
    public void openMap() {
        // / OPEN MAP
        BTreeMapMaker createTreeMap = database.createTreeMap("BlogPostMap");
        map = createTreeMap.makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<>();
    }

    public void add(String blogname, byte[] signature) {
        List<byte[]> list;
        if (blogname == null) {
            blogname = MAINBLOG;
        }
        list = get(blogname);

        if (list == null) {
            list = new ArrayList<>();
        }

        if (!ByteArrayUtils.contains(list, signature)) {
            list.add(signature);
        }

        put(blogname, list);

    }

    public void remove(String blogname, byte[] signature) {
        if (blogname == null) {
            blogname = MAINBLOG;
        }

        if (contains(blogname)) {
            List<byte[]> list = get(blogname);
            ByteArrayUtils.remove(list, signature);
            put(blogname, list);
        }

    }

}
