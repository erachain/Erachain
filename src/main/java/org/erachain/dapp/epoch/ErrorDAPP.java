package org.erachain.dapp.epoch;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Для отображения ошибок распознания DAPP
 */
public class ErrorDAPP extends EpochDAPP {

    static public final int ID = 6;
    static public final String NAME = "Errors DAPP";
    static public final String SHORT = "Errors DAPP";
    static public final String DESC = "Used for error messages";

    private String message;

    public ErrorDAPP(String message) {
        super(ID);
        this.message = message;
    }

    public String getName() {
        return NAME;
    }

    public String getMessage() {
        return message;
    }

    public boolean isDisabled() {
        return false;
    }

    public String getHTML(JSONObject langObj) {
        return Lang.T("DAPP error", langObj) + ": <b>" + message + "</b>";
    }

    /// PARSE / TOBYTES

    @Override
    public int length(int forDeal) {
        int len = 4;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            len += 4;
            if (message != null)
                len += message.length();
        }
        return len;
    }

    @Override
    public byte[] toBytes(int forDeal) {
        byte[] bytes = Ints.toByteArray(id);

        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] dataBytes;

            if (message != null) {
                dataBytes = message.getBytes(StandardCharsets.UTF_8);
            } else {
                dataBytes = new byte[0];
            }

            bytes = Bytes.concat(bytes, Ints.toByteArray(dataBytes.length));
            bytes = Bytes.concat(bytes, dataBytes);

        }

        return bytes;

    }

    public static ErrorDAPP Parse(byte[] bytes, int pos, int forDeal) {

        // skip ID
        pos += 4;

        String message;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] dataSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            pos += 4;
            byte[] dataBytes = Arrays.copyOfRange(bytes, pos, pos + dataSize);
            message = new String(dataBytes, StandardCharsets.UTF_8);

        } else {
            message = "";
        }

        return new ErrorDAPP(message);
    }

    @Override
    public boolean process(DCSet dcSet, Block block, Transaction transaction) {
        return false;
    }

    @Override
    public void orphan(DCSet dcSet, Transaction commandTX) {
    }

}
