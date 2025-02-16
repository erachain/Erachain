package org.erachain.dapp.epoch;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.HasDataString;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.DApp;
import org.erachain.dapp.DAppTimed;
import org.erachain.lang.Lang;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;

/**
 * Общий класс в котором параметры задаются вОписании к транзакции в виде JSON
 */
public abstract class EpochDAppJson extends EpochDAppStatus {

    protected String command;
    // TODO нужно наследный класс делать для Версии - так как там другой парсинг и в байты для ДБ нужно версию тоже катать
    //  protected String version;
    protected String dataStr;
    protected JSONArray pars;
    protected JSONObject values;

    // Для одного объекта с Инфо
    public EpochDAppJson(int id, PublicKeyAccount maker) {
        super(id, maker);
    }

    public EpochDAppJson(int id, PublicKeyAccount maker, String dataStr, String status) {
        super(id, maker, status);
        this.dataStr = dataStr;
        // для отката отката нужен резолв и для API
        resolveJson();
    }

    public EpochDAppJson(int id, PublicKeyAccount maker, String dataStr, String status, Transaction commandTx, Block block) {
        super(id, maker, status, commandTx, block);
        this.dataStr = dataStr;
        resolveJson();
    }

    //public EpochDAppJson(int id, String dataStr, Transaction commandTx, Block block) {
    //    this(id, PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(id))), dataStr, "", commandTx, block);
    //}

    public abstract DApp of(String dataStr, Transaction commandTx, Block block);

    @Override
    public DApp of(Transaction commandTx, Block block) {
        if (commandTx instanceof HasDataString) {
            return of(((HasDataString) commandTx).getDataString(), commandTx, block);
        }
        return new ErrorDApp("Wrong Transaction type: need 'HasDataString' - Data not found");
    }

    private void resolveJson() {

        if (dataStr != null && !dataStr.isEmpty()) {

            if (dataStr.charAt(0) == '[') {
                try {
                    //READ JSON
                    pars = (JSONArray) JSONValue.parseWithException(dataStr);
                    if (pars.size() > 0)
                        this.command = (String) pars.get(0);
                    //if (pars.size() > 1)
                    //    this.version = (String) pars.get(1);
                } catch (ParseException | NullPointerException | ClassCastException e) {
                    //JSON EXCEPTION
                    fail("parse params: \"" + dataStr + "\"" + e.getMessage());
                    return;
                }
            } else if (dataStr.charAt(0) == '{') {
                try {
                    //READ JSON
                    values = (JSONObject) JSONValue.parseWithException(dataStr);
                    this.command = (String) values.get("command");
                    //this.version = (String) values.get("vers");
                } catch (ParseException | NullPointerException | ClassCastException e) {
                    //JSON EXCEPTION
                    fail("parse values: \"" + dataStr + "\"" + e.getMessage());
                    return;
                }

            } else
                command = dataStr;

            if (command != null)
                command = command.toLowerCase();

        } else
            command = "";

    }

    public String getHTML(JSONObject langObj) {
        String out = super.getHTML(langObj) + "<br>";
        return out + Lang.T("Command", langObj) + ": <b>" + (command == null ? "" : command) + "</b><br>"
                //+ Lang.T("Version", langObj) + ": <b>" + (version == null ? "" : version) + "</b><br>"
                + Lang.T("Data", langObj) + ": <b>" + (dataStr == null ? "" : dataStr) + "</b><br>";
    }

    /// PARSE / TOBYTES
    @Override
    public int length(int forDeal) {

        int len = super.length(forDeal);
        if (forDeal == Transaction.FOR_DB_RECORD) {
            len += 4;
            if (dataStr != null)
                len += dataStr.getBytes(StandardCharsets.UTF_8).length;
        }

        return len;
    }

    @Override
    public byte[] toBytes(int forDeal) {

        byte[] bytes = super.toBytes(forDeal);

        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] dataBytes;

            if (dataStr != null) {
                dataBytes = dataStr.getBytes(StandardCharsets.UTF_8);
            } else {
                dataBytes = new byte[0];
            }

            bytes = Bytes.concat(bytes, Ints.toByteArray(dataBytes.length));
            bytes = Bytes.concat(bytes, dataBytes);

        }

        return bytes;

    }

}
