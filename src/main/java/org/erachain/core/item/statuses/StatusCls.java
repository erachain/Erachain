package org.erachain.core.item.statuses;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IssueItemMap;
import org.erachain.datachain.ItemMap;
import org.erachain.datachain.ItemStatusMap;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class StatusCls extends ItemCls {

    public static final int TYPE_KEY = ItemCls.STATUS_TYPE;

    public static final long MIN_START_KEY_OLD = 0L;

    public static final Long RIGHTS_KEY = 1L;
    public static final Long MEMBER_KEY = 2L;
    public static final int STATUS = 1;
    public static final int TITLE = 2;
    public static final int POSITION = 3;

    public static final int INITIAL_FAVORITES = 10;

    public StatusCls(byte[] typeBytes, long flags, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        super(typeBytes, flags, owner, name, icon, image, description);
    }

    public StatusCls(int type, long flags, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, boolean unique) {
        this(new byte[TYPE_LENGTH], flags, owner, name, icon, image, description);
        typeBytes[0] = (byte) type;
        typeBytes[1] = unique ? (byte) 1 : (byte) 0;

    }

    //GETTERS/SETTERS

    @Override
    public int getItemType() {
        return TYPE_KEY;
    }

    @Override
    public long START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return START_KEY_OLD;
    }

    @Override
    public long MIN_START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return MIN_START_KEY_OLD;
    }

    public String getItemTypeName() {
        return "status";
    }

    public boolean isUnique() {
        return typeBytes[1] == (byte) 1;
    }

    // DB
    public ItemMap getDBMap(DCSet db) {
        return db.getItemStatusMap();
    }

    public IssueItemMap getDBIssueMap(DCSet db) {
        return db.getIssueStatusMap();
    }

    static SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");

    public static String viewPeriod(Long dteStart, Long dteEnd) {
        String from_date_str;
        String to_date_str;

        boolean startIs = true;
        boolean endIs = true;

        if (dteStart == null || dteStart == Long.MIN_VALUE) {
            from_date_str = "-> ";
            startIs = false;
        } else from_date_str = formatDate.format(new Date(dteStart));

        if (dteEnd == null || dteEnd == Long.MAX_VALUE) {
            to_date_str = " ->";
            endIs = false;
        } else to_date_str = formatDate.format(new Date(dteEnd));

        return !startIs && !endIs ? "" :
                from_date_str + (startIs && endIs ? " - " : "") + to_date_str;

    }

    public static ArrayList<Fun.Tuple3<Long, StatusCls, Fun.Tuple5<Long, Long, byte[], Integer, Integer>>>
    getSortedItems(TreeMap<Long, Stack<Fun.Tuple5<Long, Long, byte[], Integer, Integer>>> statuses) {

        ArrayList<Fun.Tuple3<Long, StatusCls, Fun.Tuple5<Long, Long, byte[], Integer, Integer>>> statusesItems
                = new ArrayList<Fun.Tuple3<Long, StatusCls, Fun.Tuple5<Long, Long, byte[], Integer, Integer>>>();

        if (statuses == null || statuses.isEmpty())
            return statusesItems;

        ItemStatusMap statusesMap = DCSet.getInstance().getItemStatusMap();

        for (long statusKey : statuses.keySet()) {
            Stack<Fun.Tuple5<Long, Long, byte[], Integer, Integer>> statusStack = statuses.get(statusKey);
            if (statusStack == null || statusStack.isEmpty()) {
                continue;
            }

            StatusCls status = (StatusCls) statusesMap.get(statusKey);
            if (status.isUnique()) {
                // UNIQUE - only on TOP of STACK
                statusesItems.add(new Fun.Tuple3<Long, StatusCls, Fun.Tuple5<Long, Long, byte[], Integer, Integer>>(statusKey, status, statusStack.peek()));
            } else {
                for (Fun.Tuple5<Long, Long, byte[], Integer, Integer> statusItem : statusStack) {
                    statusesItems.add(new Fun.Tuple3<Long, StatusCls, Fun.Tuple5<Long, Long, byte[], Integer, Integer>>(statusKey, status, statusItem));
                }
            }

            Comparator<Fun.Tuple3<Long, StatusCls, Fun.Tuple5<Long, Long, byte[], Integer, Integer>>> comparator = new Comparator<Fun.Tuple3<Long, StatusCls, Fun.Tuple5<Long, Long, byte[], Integer, Integer>>>() {
                public int compare(Fun.Tuple3<Long, StatusCls, Fun.Tuple5<Long, Long, byte[], Integer, Integer>> c1, Fun.Tuple3<Long, StatusCls, Fun.Tuple5<Long, Long, byte[], Integer, Integer>> c2) {
                    if (c1.c.d > c2.c.d)
                        return 1;
                    else if (c1.c.d < c2.c.d)
                        return -1;

                    if (c1.c.e > c2.c.e)
                        return 1;
                    else if (c1.c.e < c2.c.e)
                        return -1;

                    return 0;
                }
            };

            Collections.sort(statusesItems, comparator);

        }

        return statusesItems;

    }

    public JSONObject jsonForExplorerInfo(DCSet dcSet, JSONObject langObj, boolean forPrint) {

        JSONObject itemJson = super.jsonForExplorerInfo(dcSet, langObj, forPrint);
        itemJson.put("Label_Status", Lang.T("Status", langObj));

        itemJson.put("unique", isUnique());

        itemJson.put("Label_unique_state", Lang.T("Unique State", langObj));
        itemJson.put("Label_multi_states", Lang.T("Multi States", langObj));

        if (!forPrint) {
        }

        return itemJson;
    }

}
