package org.erachain.dapp;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.epoch.EpochDAPP;
import org.erachain.lang.Lang;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;

public abstract class EpochDAPPjson extends EpochDAPP {

    protected String command;
    protected String dataStr;
    protected JSONArray pars;
    protected String status;

    public EpochDAPPjson(int id, PublicKeyAccount maker, String command, String dataStr, String status) {
        super(id, maker);

        this.command = command;
        this.dataStr = dataStr;

        if (dataStr != null && !dataStr.isEmpty()) {

            try {
                //READ JSON
                pars = (JSONArray) JSONValue.parseWithException(dataStr);
                if (command == null || command.isEmpty()) {
                    this.command = (String) pars.get(0);
                }
                this.status = status;
            } catch (ParseException | NullPointerException | ClassCastException e) {
                //JSON EXCEPTION
                this.status = e.getMessage();
            }
        }

    }

    public EpochDAPPjson(int id, String dataStr, String status) {
        this(id, PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(id))), null, dataStr, status);

    }

    public String getHTML(JSONObject langObj) {
        String out = super.getHTML(langObj) + "<br>";
        return out + Lang.T("Command", langObj) + ": <b>" + (command == null ? "" : command) + "</b><br>"
                + Lang.T("Data", langObj) + ": <b>" + (dataStr == null ? "" : dataStr) + "</b><br>"
                + Lang.T("Status", langObj) + ": <b>" + (status == null ? "" : status) + "</b>";
    }

    /// PARSE / TOBYTES
    @Override
    public int length(int forDeal) {

        int len = 4;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            len += 8;
            if (dataStr != null)
                len += dataStr.length();
            if (status != null)
                len += status.length();
        }

        return len;
    }

    @Override
    public byte[] toBytes(int forDeal) {

        byte[] bytes = Ints.toByteArray(id);

        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] dataBytes;
            byte[] statusBytes;

            if (dataStr != null) {
                dataBytes = dataStr.getBytes(StandardCharsets.UTF_8);
            } else {
                dataBytes = new byte[0];
            }

            if (status != null) {
                statusBytes = status.getBytes(StandardCharsets.UTF_8);
            } else {
                statusBytes = new byte[0];
            }

            bytes = Bytes.concat(bytes, Ints.toByteArray(dataBytes.length));
            bytes = Bytes.concat(bytes, dataBytes);

            bytes = Bytes.concat(bytes, Ints.toByteArray(statusBytes.length));
            bytes = Bytes.concat(bytes, statusBytes);
        }

        return bytes;

    }

}
