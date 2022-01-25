package org.erachain.dapp.epoch;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.EpochDAPPjson;
import org.erachain.datachain.DCSet;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Refi extends EpochDAPPjson {


    static public final int ID = 1012;
    static public final String NAME = "Referal Asset";

    // APPBQyonEPbk2ZazbUuHZ2ffN1QJYaK1ow
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

    /**
     * admin account
     */
    final static public Account adminAddress = new Account("7NhZBb8Ce1H2S2MkPerrMnKLZNf9ryNYtP");

    /**
     * ASSET KEY
     */
    static final private Tuple2 INIT_KEY = new Tuple2(ID, "i");
    final static public String COMMAND_JOB = "job";

    private Long assetKey;

    public Refi(String data, String status) {
        super(ID, MAKER, data, status);
    }

    public String getName() {
        return NAME;
    }

    public static Refi make(RSend txSend, String dataStr) {
        return new Refi(dataStr, "");
    }

    public static Refi tryMakeJob(RSend txSend) {
        DCSet dcSet = txSend.getDCSet();
        if (!txSend.hasAmount()
                || txSend.balancePosition() != Account.BALANCE_POS_OWN || txSend.isBackward()
                || !dcSet.getSmartContractValues().contains(INIT_KEY)) {
            return null;
        }

        return dcSet.getSmartContractValues().get(INIT_KEY).equals(txSend.getAssetKey()) ? new Refi(COMMAND_JOB, "") : null;
    }

    private boolean isAdminCommand(Account txCreator) {
        return txCreator.equals(adminAddress);
    }

    /// PARSE / TOBYTES

    public static Refi Parse(byte[] bytes, int pos, int forDeal) {

        // skip ID
        pos += 4;

        String data;
        String status;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] dataSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            pos += 4;
            byte[] dataBytes = Arrays.copyOfRange(bytes, pos, pos + dataSize);
            pos += dataSize;
            data = new String(dataBytes, StandardCharsets.UTF_8);

            byte[] statusSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int statusLen = Ints.fromByteArray(statusSizeBytes);
            pos += 4;
            byte[] statusBytes = Arrays.copyOfRange(bytes, pos, pos + statusLen);
            pos += statusLen;
            status = new String(statusBytes, StandardCharsets.UTF_8);

        } else {
            data = "";
            status = "";
        }

        return new Refi(data, status);
    }

    ///////// COMMANDS
    private boolean job(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {

    }

    //////////////////// ADMIN PROCCESS

    /// INIT
    private boolean init(DCSet dcSet, Block block, Transaction commandTX, boolean asOrphan) {

        Account adminAccount = commandTX.getCreator();

        /**
         * issue asset
         */
        BigDecimal amount = new BigDecimal("10000");
        if (asOrphan) {

            // need to remove INIT_KEY - for reinit after orphans
            assetKey = (Long) dcSet.getSmartContractValues().remove(INIT_KEY);

            // BACKWARDS from ADMIN
            transfer(dcSet, block, commandTX, stock, adminAccount, amount, assetKey, true, null, null);

            // orphan GRAVITA ASSET
            dcSet.getItemAssetMap().decrementDelete(assetKey);

        } else {

            if (!isAdminCommand(adminAccount)) {
                fail("not admin");
                return false;
            }

            if (dcSet.getSmartContractValues().contains(INIT_KEY)) {
                fail("already initated");
                return false;
            }

            AssetVenture asset = new AssetVenture(null, stock, "NAME", null, null,
                    null, AssetCls.AS_INSIDE_ASSETS, 6, 0);
            asset.setReference(commandTX.getSignature(), commandTX.getDBRef());

            //INSERT INTO DATABASE
            assetKey = dcSet.getItemAssetMap().incrementPut(asset);
            dcSet.getSmartContractValues().put(INIT_KEY, assetKey);

            // TRANSFER GRAVITA to ADMIN
            transfer(dcSet, block, commandTX, stock, adminAccount, amount, assetKey, false, null, "init");

            status = "done";
        }

        return true;
    }

    @Override
    public boolean processByTime(DCSet dcSet, Block block, Transaction transaction) {
        fail("unknow command");
        return false;
    }

    @Override
    public boolean process(DCSet dcSet, Block block, Transaction commandTX) {

        /// COMMANDS
        if (COMMAND_JOB.equals(command))
            return job(dcSet, block, (RSend) commandTX, false);

            /// ADMIN COMMANDS
        else if ("init".equals(command))
            return init(dcSet, block, commandTX, false);

        fail("unknow command");
        return false;

    }

    @Override
    public void orphanByTime(DCSet dcSet, Block block, Transaction transaction) {
    }

    @Override
    public void orphan(DCSet dcSet, Transaction commandTX) {

        /// COMMANDS
        if (COMMAND_JOB.equals(command))
            job(dcSet, null, (RSend) commandTX, true);

            /// ADMIN COMMANDS
        else if ("init".equals(command))
            init(dcSet, null, commandTX, true);

    }

}
