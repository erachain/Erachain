package org.erachain.core.web;

import org.erachain.datachain.DCSet;
import org.erachain.datachain.DCUMap;
import org.erachain.utils.ByteArrayUtils;
import org.mapdb.DB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Deprecated
public class OrphanNameStorageHelperMap extends DCUMap<String, List<byte[]>> {

    public OrphanNameStorageHelperMap(DCSet dcSet, DB database) {
        super(dcSet, database);
    }

    public OrphanNameStorageHelperMap(DCUMap<String, List<byte[]>> parent) {
        super(parent, null);
    }


    @Override
    public void openMap() {

        map = database.createTreeMap("OrphanNameStorageHelperMap")
                .makeOrGet();

    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<>();
    }

    public void add(String name, byte[] signatureOfTx) {
        List<byte[]> list = this.get(name);
        if (list == null) {
            list = new ArrayList<>();
        }

        if (!ByteArrayUtils.contains(list, signatureOfTx)) {
            list.add(signatureOfTx);
        }


        put(name, list);


    }

    public void remove(String name, byte[] signatureOfTx) {
        List<byte[]> list = this.get(name);
        if (list == null) {
            return;
        }

        ByteArrayUtils.remove(list, signatureOfTx);

        put(name, list);

    }

}
