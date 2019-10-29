package org.erachain.datachain;

import org.erachain.core.naming.Name;
import org.erachain.database.serializer.NameSerializer;
import org.mapdb.DB;

import java.util.HashMap;

//import org.erachain.database.DLSet;

public class NameMap extends DCUMap<String, Name> {

    public NameMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public NameMap(NameMap parent) {
        super(parent, null);
    }

    protected void createIndexes() {
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap("names")
                .valueSerializer(new NameSerializer())
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<String, Name>();
    }

    @Override
    protected Name getDefaultValue() {
        return null;
    }

    public boolean contains(Name name) {
        return this.contains(name.getName());
    }

    public void add(Name name) {
        this.put(name.getName(), name);
    }

    public void delete(Name name) {
        this.delete(name.getName());
    }
}
