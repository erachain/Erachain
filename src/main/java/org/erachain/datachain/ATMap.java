package org.erachain.datachain;
// 16/03

import com.google.common.collect.Lists;
import org.erachain.at.AT;
import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.database.serializer.ATSerializer;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DCUMapImpl;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.util.*;

@SuppressWarnings("rawtypes")
public class ATMap extends DCUMapImpl<String, AT> {

    private NavigableSet typeATs;
    private NavigableSet creatorATs;
    private NavigableSet orderedATs;
    private NavigableSet creationHeightATs;
    private NavigableSet hashATs;
    private Map<String, byte[]> stateAT;
    private Map<String, String> hashCodes;
    private BTreeMap<String, Integer> atToNextHeight;

    //private ATMap parentATMap;

    public ATMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_AT_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_ATS);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_AT_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_AT_TYPE);
        }
    }

    public ATMap(ATMap parent, DCSet dcSet) {
        super(parent, dcSet);
        //this.parentATMap = parent;

    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = this.openMap(database);
    }

    @Override
    protected void getMemoryMap() {
        openMap();
    }

    @SuppressWarnings("unchecked")
    private Map<String, AT> openMap(DB database) {
        //OPEN MAP
        BTreeMap<String, AT> map = database.createTreeMap("ats")
                .valueSerializer(new ATSerializer())
                .makeOrGet();

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return map;


        this.typeATs = database.createTreeSet("type_ats")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        Bind.secondaryKey(map, this.typeATs, new Fun.Function2<String, String, AT>() {
            @Override
            public String run(String key, AT value) {
                return value.getType();
            }
        });

        this.creatorATs = database.createTreeSet("creator_ats")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        Bind.secondaryKey(map, this.creatorATs, new Fun.Function2<String, String, AT>() {
            @Override
            public String run(String key, AT value) {
                return Base58.encode(value.getCreator());
            }
        });


        this.atToNextHeight = database.createTreeMap("at_to_next_height").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryValue(map, this.atToNextHeight, new Fun.Function2<Integer, String, AT>() {
            @Override
            public Integer run(String key, AT value) {
                return value.getHeight() + value.getWaitForNumberOfBlocks();
            }
        });

        this.orderedATs = database.createTreeSet("ordered_ats")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        Bind.secondaryKey(this.atToNextHeight, this.orderedATs, new Fun.Function2<Integer, String, Integer>() {
            @Override
            public Integer run(String key, Integer value) {
                return value;
            }
        });

        this.creationHeightATs = database.createTreeSet("creation_height_ats")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        Bind.secondaryKey(map, this.creationHeightATs, new Fun.Function2<Integer, String, AT>() {
            @Override
            public Integer run(String key, AT value) {
                return value.getCreationBlockHeight();
            }
        });

        this.stateAT = database.createTreeMap("state_ats_index").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryValue(map, this.stateAT, new Fun.Function2<byte[], String, AT>() {
            @Override
            public byte[] run(String key, AT value) {
                return value.getState();
            }
        });

        this.hashCodes = database.createTreeMap("hash_code_ats").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryValue(map, this.hashCodes, new Fun.Function2<String, String, AT>() {
            @Override
            public String run(String key, AT value) {
                return Base58.encode(Crypto.getInstance().digest(value.getAp_Code()));
            }
        });

        this.hashATs = database.createTreeSet("hash_ats").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryKey(map, this.hashATs, new Fun.Function2<String, String, AT>() {
            @Override
            public String run(String key, AT value) {
                return Base58.encode(Crypto.getInstance().digest(value.getAp_Code()));
            }
        });

        //RETURN
        return map;
    }

    @SuppressWarnings("unchecked")
    public boolean validTypeHash(byte[] hash, String type, Integer forkHeight) {
        String hashString = Base58.encode(hash);
        if ((!this.hashCodes.containsValue(hashString) && !Fun.filter(this.typeATs, type).iterator().hasNext())) {
            return (this.parent != null) ? ((ATMap) this.parent).validTypeHash(hash, type, forkHeight) : true;
        }
        if (this.hashCodes.containsValue(hashString) && Fun.filter(this.typeATs, type).iterator().hasNext()) {
            Iterable<String> ats = getTypeATs(type);
            if (this.hashCodes.get(ats.iterator().next()).equals(hashString)) {
                return true;
            }
        }

        if (forkHeight > 1) {
            Iterable<String> ats = getTypeATs(type);
            Iterator<String> iter = ats.iterator();
            while (iter.hasNext()) {
                AT at = getAT(iter.next());
                if (at.getCreationBlockHeight() < forkHeight && forkHeight > 1) {
                    return false;
                }
            }

            iter = Fun.filter(this.hashATs, hashString).iterator();
            while (iter.hasNext()) {
                AT at = getAT(iter.next());
                if (at.getCreationBlockHeight() < forkHeight && forkHeight > 1) {
                    return false;
                }
            }

            return true;
        }
        return false;

    }

    @SuppressWarnings("unchecked")
    public Iterable<String> getTypeATs(String type) {
        return Fun.filter(this.typeATs, type);
    }

    public Collection<String> getTypeATsList(String type) {
        @SuppressWarnings("unchecked")
        Collection<String> keys = Lists.newArrayList(Fun.filter(this.typeATs, type));
        return keys;
    }

    public boolean add(AT at) {
        //TODO CHECK PROPER at.getCreator() ENCODING
        return add(at, at.getCreationBlockHeight());
    }

    public boolean add(AT at, Integer height) {
        return this.set(Base58.encode(at.getId()), at);
    }


    public void update(AT at, Integer height) {
        delete(at);
        add(at, height);
    }

    public void delete(AT at) {
        // USE parents
        this.remove(Base58.encode(at.getId()));

    }


    //GET AT BY ID
    public AT getAT(String id) {
        AT at = super.get(id);
        if (at != null) {
            byte[] state = stateAT.get(id);
            at.setState(state);
        }
        return at;
    }

    public AT getAT(byte[] id) {
        String atId = Base58.encode(id);
        AT at = getAT(atId);
        if (at == null && this.parent != null) {
            at = this.parent.get(atId);
        }
        return at;

    }

    //delete all ATs created after blockHeight
    @SuppressWarnings({"unchecked"})
    public void deleteAllAfterHeight(int blockHeight) {
        Iterable<String> ids = Fun.filter(this.creationHeightATs, blockHeight + 1, true, Integer.MAX_VALUE, true);
        Iterator<String> iter = ids.iterator();
        while (iter.hasNext()) {
            remove(iter.next());
        }
    }

    @SuppressWarnings({"unchecked"})
    public Iterable<String> getATsLimited(int limit) {
        Iterable<String> ids = Fun.filter(this.creationHeightATs, limit + 1, true, Integer.MAX_VALUE, true);
        return ids;
    }

    @SuppressWarnings({"unchecked"})
    public Iterable<String> getATsByCreator(String creator) {
        return Fun.filter(this.creatorATs, creator);
    }

    @SuppressWarnings({"unchecked"})
    public Iterator<String> getOrderedATs(Integer height) {
        return Fun.filter(this.orderedATs, null, true, height, true).iterator();
    }

    @SuppressWarnings({"unchecked"})
    public List<AT> getAcctATs(String type, boolean initiators) {
        Collection<String> keys = Lists.newArrayList(Fun.filter(this.typeATs, type));
        //RETURN
        return null; //new List<AT>(this, keys);
    }

}
