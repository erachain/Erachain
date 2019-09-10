package org.erachain.core.web;

import org.erachain.datachain.DCMap;
import org.erachain.datachain.DCSet;
import org.mapdb.DB;
import org.erachain.utils.ByteArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrphanNameStorageHelperMap extends DCMap<String, List<byte[]>> {

    public OrphanNameStorageHelperMap(DCSet dcSet, DB database) {
        super(dcSet, database);
    }

    public OrphanNameStorageHelperMap(DCMap<String, List<byte[]>> parent) {
        super(parent, null);
    }


    @Override
    protected void getMap(DB database) {

        map = database.createTreeMap("OrphanNameStorageHelperMap")
                .makeOrGet();

    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<>();
    }

    @Override
    protected void createIndexes(DB database) {
    }

    public void add(String name, byte[] signatureOfTx) {
        List<byte[]> list = this.get(name);
        if (list == null) {
            list = new ArrayList<>();
        }

        if (!ByteArrayUtils.contains(list, signatureOfTx)) {
            list.add(signatureOfTx);
        }


        set(name, list);


    }

    public void remove(String name, byte[] signatureOfTx) {
        List<byte[]> list = this.get(name);
        if (list == null) {
            return;
        }

        ByteArrayUtils.remove(list, signatureOfTx);

        set(name, list);

    }

    @Override
    protected List<byte[]> getDefaultValue() {
        return null;
    }

}
