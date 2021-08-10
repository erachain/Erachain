package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.models.WalletTableModel;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class FavoriteAccountsTableModel extends WalletTableModel<Tuple2<String, Tuple3<String, String, String>>> {

    public static final int COLUMN_NO = 0;
    public static final int COLUMN_ADDRESS = 1;
    public static final int COLUMN_PUB_KEY = 2;
    public static final int COLUMN_NAME = 3;
    public static final int COLUMN_DESCRIPTION = 4;
    public static final int COLUMN_PERSON = 5;

    //private Tuple2<String, Tuple2<String, String>> account;

    public FavoriteAccountsTableModel() {
        super(Controller.getInstance().getWallet().dwSet.getFavoriteAccountsMap(),
                new String[]{"No.", "Account", "PubKey", "Name", "Description", "Person"},
                new Boolean[]{true, false, false, false, false}, false, -1);

    }

    @Override
    protected void updateMap() {
        map = Controller.getInstance().getWallet().dwSet.getFavoriteAccountsMap();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        Tuple2<String, Tuple3<String, String, String>> item = this.list.get(row);
        if (item == null) {
            return null;
        }

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                balance;
        Tuple3<BigDecimal, BigDecimal, BigDecimal> unconfBalance;
        String str;

        JSONObject json;
        switch (column) {
            case COLUMN_NO:
                return row + 1;
            case COLUMN_ADDRESS:
                return item.a;
            case COLUMN_PUB_KEY:
                return item.b.a;
            case COLUMN_NAME:
                return item.b.b;
            case COLUMN_PERSON:
                return new Account(item.a).viewPerson();
            case COLUMN_DESCRIPTION:
                json = (JSONObject) JSONValue.parse(item.b.c);
                json = json == null ? new JSONObject() : json;
                // set papams
                if (json.containsKey("description")) {
                    return json.get("description");
                }
                return "";
        }

        return null;
    }

    public void getInterval() {
        Object key;
        int count = 0;
        list = new ArrayList<>();
        if (startKey == null) {
            try (IteratorCloseable iterator = map.getIterator()) {
                while (iterator.hasNext() && count++ < step) {
                    key = iterator.next();
                    list.add(new Tuple2<String, Tuple3<String, String, String>>((String) key, (Tuple3) map.get(key)));
                }
            } catch (IOException e) {
            }
        } else {
            try (IteratorCloseable iterator = map.getIterator()) {
                while (iterator.hasNext() && count++ < step) {
                    key = iterator.next();
                    list.add(new Tuple2<String, Tuple3<String, String, String>>((String) key, (Tuple3) map.get(key)));
                }
            } catch (IOException e) {
            }
        }
    }
}
