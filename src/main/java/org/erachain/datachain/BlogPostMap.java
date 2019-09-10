package org.erachain.datachain;

import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;
import org.erachain.utils.ByteArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogPostMap extends DCMap<String, List<byte[]>> {

    public final static String MAINBLOG = "Erachain.org";

    public BlogPostMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public BlogPostMap(DCMap<String, List<byte[]>> parent) {
        super(parent, null);
    }

    @Override
    protected void getMap(DB database) {
        // / OPEN MAP
        BTreeMapMaker createTreeMap = database.createTreeMap("BlogPostMap");
        map = createTreeMap.makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<>();
    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected List<byte[]> getDefaultValue() {
        return null;
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

        set(blogname, list);

    }

    public void remove(String blogname, byte[] signature) {
        if (blogname == null) {
            blogname = MAINBLOG;
        }

        if (contains(blogname)) {
            List<byte[]> list = get(blogname);
            ByteArrayUtils.remove(list, signature);
            set(blogname, list);
        }

    }

}
