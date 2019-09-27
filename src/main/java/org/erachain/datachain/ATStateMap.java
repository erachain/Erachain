package org.erachain.datachain;

import org.erachain.at.ATConstants;
import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.util.*;

//Integer -> blockHeight (f.e 0 -> 1000 -> 2000 if we keep state every 1000s blocks), byte[] -> atId , byte[] stateBytes
public class ATStateMap extends DCUMap<Tuple2<Integer, String>, byte[]> {

    @SuppressWarnings("rawtypes")
    private NavigableSet allATStates;

    public ATStateMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public ATStateMap(ATStateMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    protected void createIndexes() {
    }

    @Override
    protected void getMap() {
        map = this.openMap(database);
    }

    @Override
    protected void getMemoryMap() {
        getMap();
    }


    @SuppressWarnings("unchecked")
    private Map<Tuple2<Integer, String>, byte[]> openMap(DB database) {
        //OPEN MAP
        BTreeMap<Tuple2<Integer, String>, byte[]> map = database.createTreeMap("at_state")
                .makeOrGet();

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return map;


        allATStates = database.createTreeSet("at_id_to_height").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryKey(map, allATStates, new Fun.Function2<Tuple2<String, Integer>, Tuple2<Integer, String>, byte[]>() {
            @Override
            public Tuple2<String, Integer> run(Tuple2<Integer, String> key, byte[] val) {
                return new Tuple2<String, Integer>(key.b, key.a);
            }
        });

        //RETURN
        return map;
    }


    //add State if it does not exist or update the state
    public void addOrUpdate(Integer blockHeight, byte[] atId, byte[] stateBytes) {
        //Height to store state
        int height = (int) ((Math.round(blockHeight) / ATConstants.STATE_STORE_DISTANCE) + 1) * ATConstants.STATE_STORE_DISTANCE;

        this.set(new Tuple2<Integer, String>(height, Base58.encode(atId)), stateBytes);
    }

    //get the list of states stored at block height. use for roll back
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, byte[]> getStates(Integer blockHeight) {
        BTreeMap map = (BTreeMap) this.map;

        //Height to store state
        int height = (int) ((Math.round(blockHeight) / ATConstants.STATE_STORE_DISTANCE) + 1) * ATConstants.STATE_STORE_DISTANCE;

        //FILTER ALL ATS
        Collection<Tuple2> keys = ((BTreeMap<Tuple2, String>) map).subMap(
                Fun.t2(height, null),
                Fun.t2(Fun.HI(), Fun.HI())).keySet();
        // TODO: ERROR - NEED PARENT DB seek if FORKED

        Map<String, byte[]> states = new TreeMap<String, byte[]>();

        for (Tuple2 key : keys) {
            //states.put( (String)key.b , (byte[]) map.get(key));
            Iterator<Tuple2<Integer, String>> iter = Fun.filter(allATStates, new Tuple2<String, Integer>((String) key.b, 0), true, new Tuple2<String, Integer>((String) key.b, height - 1), true).iterator();
            if (iter.hasNext()) {
                states.put((String) key.b, this.get(iter.next()));
            }
        }

        if (this.parent != null) {

            states.putAll(((DCSet)this.parent.getDBSet()).getATStateMap().getStates(blockHeight));

            if (this.deleted != null) {
                //DELETE DELETED
                for (Object deleted : this.deleted.keySet()) {
                    states.remove(deleted);
                }
            }

        }

        return states;
    }

    //delete the List of States after blockHeight. When roll back occurs to 1000 blockHeight and you have also states stored at 2000 blockHeight f.e.
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void deleteStatesAfter(Integer blockHeight) {
        BTreeMap map = (BTreeMap) this.map;

        //FILTER ALL ATS
        Collection<Tuple2> keys = ((BTreeMap<Tuple2, String>) map).subMap(
                Fun.t2(blockHeight + 1, null),
                Fun.t2(Fun.HI(), Fun.HI())).keySet();

        //DELETE
        for (Tuple2 key : keys) {
            this.remove(key);
        }

        // in .deleted
        if (false && this.parent != null) {
            ((DCSet)this.parent.getDBSet()).getATStateMap().deleteStatesAfter(blockHeight);
        }
    }

    @Override
    protected byte[] getDefaultValue() {
        return null;
    }

}
