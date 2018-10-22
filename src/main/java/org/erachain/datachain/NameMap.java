package org.erachain.datachain;

import org.erachain.core.naming.Name;
import org.erachain.database.serializer.NameSerializer;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;

//import org.erachain.database.DBSet;

public class NameMap extends DCMap<String, Name> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public NameMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public NameMap(NameMap parent) {
        super(parent, null);
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<String, Name> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("names")
                .valueSerializer(new NameSerializer())
                .makeOrGet();
    }

    @Override
    protected Map<String, Name> getMemoryMap() {
        return new HashMap<String, Name>();
    }

    @Override
    protected Name getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public boolean contains(Name name) {
        return this.contains(name.getName());
    }

    public void add(Name name) {
        this.set(name.getName(), name);
    }

    public void delete(Name name) {
        this.delete(name.getName());
    }
}
