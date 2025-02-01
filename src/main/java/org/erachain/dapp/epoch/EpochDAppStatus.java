package org.erachain.dapp.epoch;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import lombok.Getter;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.Transaction;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;

public abstract class EpochDAppStatus extends EpochDApp {

    @Getter
    protected String status;

    public EpochDAppStatus(int id, PublicKeyAccount maker) {
        super(id, maker);
    }

    public EpochDAppStatus(int id, PublicKeyAccount maker, String status) {
        super(id, maker);
        this.status = status;
    }

    public EpochDAppStatus(int id, PublicKeyAccount maker, String status, Transaction commandTx, Block block) {
        super(id, maker, commandTx, block);
        this.status = status;
    }

    @Override
    public int length(int forDeal) {
        int len = 4;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            len += 4;
            if (status != null)
                len += status.length();
        }
        return len;
    }

    /**
     * "error:" - need for stop orphan
     *
     * @param mess
     */
    protected void fail(String mess) {
        status = "fail: " + mess;
    }

    @Override
    public byte[] toBytes(int forDeal) {

        byte[] bytes = Ints.toByteArray(id);

        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] statusBytes;

            if (status != null) {
                statusBytes = status.getBytes(StandardCharsets.UTF_8);
            } else {
                statusBytes = new byte[0];
            }

            bytes = Bytes.concat(bytes, Ints.toByteArray(statusBytes.length));
            bytes = Bytes.concat(bytes, statusBytes);
        }

        return bytes;

    }

    public String getHTML(JSONObject langObj) {
        String out = super.getHTML(langObj) + "<br>";
        return status == null ? out : out
                + Lang.T("Status", langObj) + ": <b>" + status + "</b>";
    }

    /**
     * For use FAIL status
     */
    public void orphanBody() {
        status = null;
    }

    /**
     * Use FAIL status
     *
     */
    @Override
    public void orphan() {

        if (status.startsWith("fail")) {
            // not processed
            return;
        } else if (status.startsWith("wait")) {
            /// WAIT RANDOM FROM FUTURE
            dcSet.getTimeTXWaitMap().remove(commandTx.getDBRef());
            return;
        }

        orphanBody();
    }

}
