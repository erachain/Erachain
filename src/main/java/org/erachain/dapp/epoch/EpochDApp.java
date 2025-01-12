package org.erachain.dapp.epoch;

import com.google.common.primitives.Ints;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.DApp;
import org.erachain.lang.Lang;
import org.erachain.utils.Pair;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class EpochDApp extends DApp {

    protected PublicKeyAccount stock;
    // заполнить один раз для показа в ИНФО - address = {commands}
    final public List<Pair<PublicKeyAccount, String[]>> accountsInfo = new ArrayList<>();

    protected EpochDApp(int id, PublicKeyAccount stock) {
        super(id);
        assert (stock.isDAppOwned());
        this.stock = stock;
    }

    public EpochDApp(int id, PublicKeyAccount stock, Transaction commandTx, Block block) {
        super(id, commandTx, block);
        assert (stock.isDAppOwned());
        this.stock = stock;
    }

    public PublicKeyAccount getStock() {
        return this.stock;
    }

    /**
     * Эпохальный смарт-контракт
     *
     * @return
     */
    @Override
    public boolean isEpoch() {
        return true;
    }

    public String getHTML(JSONObject langObj) {
        return super.getHTML(langObj) + Lang.T("Address", langObj) + ": <b>" + stock.getAddress() + "</b>";
    }

    public String getCommandInfo(String command, String format) {
        return format;
    }

    public JSONObject getInfo(JSONObject langObj) {
        JSONObject out = super.getInfo(langObj);

        String key = String.format("DApp_%d_", getID());
        out.put("desc", Lang.T(key + "DESC", langObj));

        if (!accountsInfo.isEmpty()) {
            JSONObject accs = new JSONObject();
            accountsInfo.forEach(pair -> {
                JSONObject acc = new JSONObject();
                String address = pair.getA().getAddress();
                accs.put(address, acc);
                acc.put("desc", Lang.T(key + address, langObj)); //"Счет для JOB");
                if (pair.getB() != null && pair.getB().length > 0) {
                    JSONObject commands = new JSONObject();
                    Arrays.stream(pair.getB()).forEach(command -> commands.put(command, getCommandInfo(command, Lang.T(key + address + "_" + command, langObj))));
                    acc.put("commands", commands);
                }
            });

            out.put("accs", accs);
        }

        return out;
    }

    /**
     * make public key from Base with Nonce
     *
     * @param base
     * @param nonce
     * @return
     */
    public static PublicKeyAccount noncePubKey(byte[] base, byte nonce) {
        byte[] hash = new byte[base.length];
        System.arraycopy(base, 0, hash, 0, base.length);
        hash[base.length - 1] += nonce;
        return PublicKeyAccount.makeForDApp(hash);
    }

    @Override
    public int length(int forDeal) {
        return 4;
    }

    @Override
    public byte[] toBytes(int forDeal) {
        return Ints.toByteArray(id);
    }

}
