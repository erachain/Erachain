package org.erachain.core.exdata.exActions;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple pay - for all same amount
 */

public class ExAirDrop extends ExAction<List<Fun.Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>>>> {

    public static final byte BASE_LENGTH = 4 + 8 + 8 + 2;

    public static final int MAX_COUNT = 1 << 16;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExAirDrop.class);

    /**
     * 0 - version; 1 - flags;
     */
    private final int flags; // byte[2]

    private long assetKey;
    private final BigDecimal amount;
    private final int balancePos;
    final boolean backward;
    private final byte[][] addresses;

    /////////////////

    public String errorValue;

    public ExAirDrop(int flags, long assetKey, BigDecimal amount, int balancePos, boolean backward, byte[][] addresses) {
        super(LIST_PAYOUTS_TYPE);
        this.flags = flags;
        this.assetKey = assetKey;
        this.amount = amount;
        this.balancePos = balancePos;
        this.backward = backward;
        this.addresses = addresses;

        totalPay = amount.multiply(BigDecimal.valueOf(addresses.length));

    }

    public Long getAssetKey() {
        return assetKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public AssetCls getAsset() {
        return asset;
    }

    public byte[][] getAddresses() {
        return addresses;
    }

    public int getAddressesCount() {
        return addresses.length;
    }

    public long getTotalFeeBytes() {
        return addresses.length * 25;
    }

    public BigDecimal getTotalPay() {
        return totalPay;
    }

    @Override
    public void updateItemsKeys(List listTags) {
        listTags.add(new Object[]{ItemCls.ASSET_TYPE, getAssetKey(), getAsset().getTags()});
    }

    public void setDC(DCSet dcSet) {
        if (this.dcSet == null || !this.dcSet.equals(dcSet)) {
            this.dcSet = dcSet;
            this.asset = this.dcSet.getItemAssetMap().get(this.assetKey);
        }
    }

    public void makePayList(DCSet dcSet, int height, AssetCls asset, Account creator, boolean andValidate) {

        results = new ArrayList<>();
        errorValue = null;
        resultCode = Transaction.VALIDATE_OK;

        BigDecimal accrual = amount;

        boolean creatorIsPerson = creator.isPerson(dcSet, height);

        if (andValidate && addresses.length > MAX_COUNT) {
            errorValue = "" + MAX_COUNT;
            resultCode = Transaction.INVALID_MAX_ITEMS_COUNT;
            return;
        }

        for (byte[] recipientShort : addresses) {

            Account recipient = new Account(recipientShort);

            // IF send from PERSON to ANONYMOUS
            if (andValidate && !TransactionAmount.isValidPersonProtect(dcSet, height, recipient,
                    creatorIsPerson, assetKey, balancePos,
                    asset)) {
                resultCode = Transaction.RECEIVER_NOT_PERSONALIZED;
                errorValue = null;
                results.add(new Fun.Tuple3(recipient, accrual, new Fun.Tuple2<>(resultCode, null)));
            } else {
                results.add(new Fun.Tuple3(recipient, accrual, null));
            }

        }

        totalPay = amount.multiply(new BigDecimal(addresses.length));

        return;

    }

    public byte[] toBytes() throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        outStream.write(Ints.toByteArray(flags));

        byte[] buff;
        outStream.write(Longs.toByteArray(this.assetKey));

        outStream.write(this.amount.scale());
        buff = this.amount.unscaledValue().toByteArray();
        outStream.write(buff.length);
        outStream.write(buff);

        outStream.write(Ints.toByteArray(addresses.length));
        for (int i = 0; i < addresses.length; i++) {
            outStream.write(addresses[i]);
        }

        return outStream.toByteArray();

    }

    public int length() {
        return BASE_LENGTH + addresses.length * Account.ADDRESS_SHORT_LENGTH;
    }

    public int getLengthDBData() {
        return (totalPay == null ? 1 : 2 + totalPay.unscaledValue().toByteArray().length);
    }

    public byte[] getDBdata() {

        byte[] buff;
        byte[] dbData;

        if (totalPay == null) {
            dbData = new byte[1];
            buff = null;
        } else {
            buff = this.totalPay.unscaledValue().toByteArray();
            dbData = new byte[2 + buff.length];
        }

        int pos = 0;

        dbData[pos++] = (byte) buff.length;

        dbData[pos++] = (byte) this.totalPay.scale();
        System.arraycopy(buff, 0, dbData, pos, buff.length);

        return dbData;

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ExAirDrop parse(byte[] data, int pos) throws Exception {

        int scale;
        int len;

        int flags = Ints.fromByteArray(Arrays.copyOfRange(data, pos, pos + Integer.BYTES));
        pos += Integer.BYTES;

        Long assetKey = null;
        int balancePos = 0;
        boolean backward = false;

        assetKey = Longs.fromByteArray(Arrays.copyOfRange(data, pos, pos + Long.BYTES));
        pos += Long.BYTES;

        balancePos = data[pos++];
        backward = data[pos++] > 0;

        scale = data[pos++];
        len = data[pos++];
        BigDecimal payValue = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, pos, pos + len)), scale);
        pos += len;

        len = Ints.fromByteArray(Arrays.copyOfRange(data, pos, pos + Integer.BYTES));
        pos += Integer.BYTES;

        byte[][] addresses = new byte[len][];
        for (int i = 0; i < len; i++) {
            System.arraycopy(data, pos, addresses[i], 0, Account.ADDRESS_SHORT_LENGTH);
            pos += Account.ADDRESS_SHORT_LENGTH;
        }

        return new ExAirDrop(flags, assetKey, payValue, balancePos, backward, addresses);
    }

    public int parseDBData(byte[] dbData, int position) {

        int len = dbData[position++];
        if (len == 0) {
            totalPay = null;
        } else {
            int scale = dbData[position++];
            totalPay = new BigDecimal(new BigInteger(Arrays.copyOfRange(dbData, position, position + len)), scale);
        }

        position += len;

        return position;

    }

    public static Fun.Tuple2<ExAction, String> make(Long assetKey, String amountStr,
                                                    int balancePos, boolean backward,
                                                    String[] addressesStr) {

        int steep = 0;
        BigDecimal amount;

        //Controller cntr = Controller.getInstance();
        //BlockChain chain = cntr.getBlockChain();

        try {
            amount = amountStr == null || amountStr.isEmpty() ? null : new BigDecimal(amountStr);
        } catch (Exception e) {
            String error;
            switch (steep) {
                case 0:
                    error = "Wrong amount";
                    break;
                default:
                    error = e.getMessage();
            }
            return new Fun.Tuple2<>(null, error);
        }

        if (assetKey == null || assetKey == 0L) {
            return new Fun.Tuple2<>(null, "Wrong assetKey (null or ZERO)");
        } else if (amount == null || amount.signum() == 0) {
            return new Fun.Tuple2<>(null, "Wrong payMethodValue (null or ZERO)");
        }

        Fun.Tuple2<Account, String> result;
        byte[][] addresses = new byte[addressesStr.length][];
        for (int i = 0; i < addressesStr.length; i++) {
            result = Account.tryMakeAccount(addressesStr[i]);
            if (result.a == null) {
                return new Fun.Tuple2<>(null, i + ":" + addressesStr[i] + " - " + result.b);
            }
            addresses[i] = result.a.getShortAddressBytes();
        }

        int flags = 0;
        return new Fun.Tuple2<>(new ExAirDrop(flags, assetKey, amount, balancePos, backward, addresses), null);

    }

    public static Fun.Tuple2<ExAction, String> parseJSON_local(JSONObject jsonObject) throws Exception {
        long assetKey = Long.valueOf(jsonObject.getOrDefault("assetKey", 0L).toString());
        int position = Integer.valueOf(jsonObject.getOrDefault("position", 1).toString());
        boolean backward = Boolean.valueOf((boolean) jsonObject.getOrDefault("backward", false));

        String value = (String) jsonObject.get("amount");

        JSONArray addressesJson = (JSONArray) jsonObject.get("addresses");
        String[] addressesStr = (String[]) addressesJson.toArray(new String[addressesJson.size()]);

        return make(assetKey, value, position, backward, addressesStr);
    }

    /**
     * Version 2 maker for BlockExplorer
     */
    public JSONObject makeJSONforHTML(JSONObject langObj) {
        JSONObject json = toJson();

        json.put("asset", asset.getName());

        json.put("Label_Counter", Lang.T("Counter", langObj));
        json.put("Label_Total_Amount", Lang.T("Total Amount", langObj));
        json.put("Label_Additional_Fee", Lang.T("Additional Fee", langObj));

        return json;

    }

    public JSONObject toJson() {

        JSONObject toJson = new JSONObject();

        toJson.put("flags", flags);
        toJson.put("assetKey", assetKey);
        toJson.put("amount", amount);
        toJson.put("balancePosition", balancePos);
        toJson.put("backward", backward);

        JSONArray array = new JSONArray();
        for (byte[] address : addresses) {
            array.add(Base58.encode(address));
        }
        toJson.put("addresses", array);

        return toJson;
    }

    @Override
    public String getInfoHTML() {
        String out = "<h3>" + Lang.T("Accruals") + "</h3>";
        out += Lang.T("Asset") + ": <b>" + asset.getName() + "<br>";
        out += Lang.T("Count # кол-во") + ": <b>" + addresses.length
                + "</b>, " + Lang.T("Additional Fee") + ": <b>" + BlockChain.feeBG(getTotalFeeBytes())
                + "</b>, " + Lang.T("Total") + ": <b>" + totalPay;
        return out;
    }

    @Override
    public int preProcess(int height, Account creator, boolean andPreValid) {
        makePayList(dcSet, height, asset, creator, andPreValid);
        return resultCode;
    }

    public int isValid(RSignNote rNote) {

        if (this.assetKey == 0L) {
            errorValue = "Airdrop: assetKey == null or ZERO";
            return Transaction.INVALID_ITEM_KEY;
        } else if (this.balancePos < TransactionAmount.ACTION_SEND || this.balancePos > TransactionAmount.ACTION_SPEND) {
            errorValue = "Airdrop: balancePos out off range";
            return Transaction.INVALID_BALANCE_POS;
        } else if (amount.signum() < 0) {
            errorValue = "Airdrop: payMethodValue < 0";
            return Transaction.INVALID_AMOUNT;
        }

        height = rNote.getBlockHeight();

        Account recipient = new Account(addresses[0]);
        PublicKeyAccount creator = rNote.getCreator();
        byte[] signature = rNote.getSignature();
        boolean creatorIsPerson = creator.isPerson(dcSet, height);

        // возьмем знаки (минус) для создания позиции баланса такой
        Fun.Tuple2<Integer, Integer> signs = Account.getSignsForBalancePos(balancePos);
        long key = signs.a * assetKey;

        // комиссию не проверяем так как она не правильно считается внутри?
        long actionFlags = Transaction.NOT_VALIDATE_FLAG_FEE;

        BigDecimal totalFeeBG = rNote.getFee();
        // проверим как будто всю сумму одному переводим - с учетом комиссии полной
        Fun.Tuple2<Integer, String> result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                key, asset, signs.b > 0 ? totalPay : totalPay.negate(), recipient,
                backward, totalFeeBG, null, creatorIsPerson, actionFlags);
        if (result.a != Transaction.VALIDATE_OK) {
            errorValue = "Airdrop: totalPay + totalFee = " + totalPay.toPlainString() + " / " + totalFeeBG.toPlainString();
            return result.a;
        }

        int index = 0;
        for (byte[] recipientShort : addresses) {

            recipient = new Account(recipientShort);

            result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                    key, asset, amount, recipient,
                    backward, BigDecimal.ZERO, null, creatorIsPerson, actionFlags);

            if (result.a != Transaction.VALIDATE_OK) {
                errorValue = "Airdrop: address[" + index + "] -> " + recipient.getAddress();
                return result.a;
            }

            ++index;
        }

        return Transaction.VALIDATE_OK;
    }

    public void processBody(Transaction rNote, boolean asOrphan, Block block) {
        PublicKeyAccount creator = rNote.getCreator();

        long absKey = assetKey;

        // возьмем знаки (минус) для создания позиции баланса такой
        Fun.Tuple2<Integer, Integer> signs = Account.getSignsForBalancePos(balancePos);
        Long actionPayKey = signs.a * assetKey;
        boolean isAmountNegate;
        BigDecimal actionPayAmount;
        boolean incomeReverse = balancePos == Account.BALANCE_POS_HOLD;
        boolean reversedBalancesInPosition = asset.isReverseBalancePos(balancePos);
        boolean backwardAction;

        Account recipient;
        for (byte[] address : addresses) {

            recipient = new Account(address);
            if (recipient == null)
                break;

            isAmountNegate = amount.signum() < 0;
            backwardAction = (reversedBalancesInPosition ^ backward) ^ isAmountNegate;

            if (!asOrphan && block != null) {
                rNote.addCalculated(block, recipient, absKey, amount,
                        asset.viewAssetTypeAction(backwardAction, balancePos, asset.getMaker().equals(creator)));
            }

            if (creator.equals(recipient))
                // пропустим себя в любом случае - хотя КАлькулейтед оставим для виду
                continue;

            // сбросим направление от фильтра
            actionPayAmount = amount.abs();
            // зададим направление от Действия нашего
            actionPayAmount = signs.b > 0 ? actionPayAmount : actionPayAmount.negate();

            TransactionAmount.processAction(dcSet, asOrphan, creator, recipient, balancePos, absKey,
                    asset, actionPayKey, actionPayAmount, backwardAction,
                    incomeReverse);

        }

    }

    public void process(Transaction rNote, Block block) {

        height = rNote.getBlockHeight();
        asset = dcSet.getItemAssetMap().get(assetKey);

        processBody(rNote, false, block);

    }

    public void orphan(Transaction rNote) {

        height = rNote.getBlockHeight();
        asset = dcSet.getItemAssetMap().get(assetKey);

        processBody(rNote, true, null);

    }

}
