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

    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public OrphanNameStorageHelperMap(DCSet dcSet, DB database) {
        super(dcSet, database);
    }

    public OrphanNameStorageHelperMap(DCMap<String, List<byte[]>> parent) {
        super(parent, null);
    }


    @Override
    protected Map<String, List<byte[]>> getMap(DB database) {


        return database.createTreeMap("OrphanNameStorageHelperMap")
                .makeOrGet();

    }

    @Override
    protected Map<String, List<byte[]>> getMemoryMap() {
        return new HashMap<>();
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
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
