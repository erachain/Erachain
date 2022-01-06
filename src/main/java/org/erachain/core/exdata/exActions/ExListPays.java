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
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mass pay - address(short), amount, memo
 * Result: recipient + Validate_Result {code, mess}
 */

public class ExListPays extends ExAction<List<Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>>>> {

    public static final byte BASE_LENGTH = 4 + 8 + 1 + 4;

    public static final int MAX_COUNT = 1 << 16;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExListPays.class);

    /**
     * flags:
     * 0 - version; 1 - flags;
     */

    private final Tuple3<byte[], BigDecimal, String>[] addresses;

    /////////////////

    public String errorValue;

    public ExListPays(int flags, long assetKey, int balancePos, boolean backward, Tuple3<byte[], BigDecimal, String>[] addresses) {
        super(LIST_PAYOUTS_TYPE, flags, balancePos, backward);
        this.assetKey = assetKey;
        this.addresses = addresses;

    }

    public Tuple3<byte[], BigDecimal, String>[] getAddresses() {
        return addresses;
    }

    public int getAddressesCount() {
        return addresses.length;
    }

    @Override
    public String viewType() {
        return "List";
    }

    public long getTotalFeeBytes() {
        return addresses.length * 25;
    }

    @Override
    public BigDecimal getTotalPay() {
        if (totalPay == null) {

            totalPay = BigDecimal.ZERO;
            for (Tuple3<byte[], BigDecimal, String> item : addresses) {
                totalPay = totalPay.add(item.b);
            }
        }

        return totalPay;
    }

    @Override
    public BigDecimal getAmount(Account account) {
        for (Tuple3<byte[], BigDecimal, String> item : addresses) {
            if (account.equals(item.a))
                return item.b;
        }
        return BigDecimal.ZERO;
    }

    @Override
    public int getCount() {
        return addresses.length;
    }

    @Override
    public String viewResults(Transaction transactionParent) {
        String results = "";
        int i = 0;
        for (Tuple3<byte[], BigDecimal, String> item : addresses) {
            results += ++i + " " + crypto.getAddressFromShort(item.a) + " " + item.b.toPlainString() + " " + item.c + "\n";
        }

        return results;
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

        boolean creatorIsPerson = creator.isPerson(dcSet, height);

        if (andValidate && addresses.length > MAX_COUNT) {
            errorValue = "" + MAX_COUNT;
            resultCode = Transaction.INVALID_MAX_ITEMS_COUNT;
            return;
        }

        totalPay = BigDecimal.ZERO;
        for (Tuple3<byte[], BigDecimal, String> item : addresses) {

            Account recipient = new Account(item.a);

            // IF send from PERSON to ANONYMOUS
            if (andValidate) {
                if (!TransactionAmount.isValidPersonProtect(dcSet, height, recipient,
                        creatorIsPerson, assetKey, balancePos,
                        asset)) {
                    resultCode = Transaction.RECEIVER_NOT_PERSONALIZED;
                    errorValue = null;
                    results.add(new Fun.Tuple3(recipient, item.b, new Fun.Tuple2<>(resultCode, null)));
                }
                if (creator.equals(recipient)) {
                    resultCode = Transaction.INVALID_RECEIVERS_LIST;
                    errorValue = "equal creator";
                    results.add(new Fun.Tuple3(recipient, item.b, new Fun.Tuple2<>(resultCode, errorValue)));
                }
            } else {
                results.add(new Fun.Tuple3(recipient, item.b, null));
            }

            totalPay = totalPay.add(item.b);

        }

    }

    public byte[] toBytes() throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        outStream.write(Ints.toByteArray(flags));

        outStream.write(Longs.toByteArray(this.assetKey));

        if (backward) {
            outStream.write(-balancePos);
        } else {
            outStream.write(balancePos);
        }

        outStream.write(Ints.toByteArray(addresses.length));
        byte[] buff;
        for (Tuple3<byte[], BigDecimal, String> item : addresses) {
            outStream.write(item.a);

            outStream.write(item.b.scale());
            buff = item.b.unscaledValue().toByteArray();
            outStream.write(buff.length);
            outStream.write(buff);

            buff = item.c.getBytes(StandardCharsets.UTF_8);
            // HEAD SIZE
            outStream.write((byte) buff.length);
            // HEAD
            outStream.write(buff);

        }

        return outStream.toByteArray();

    }

    public int length() {
        int len = BASE_LENGTH + addresses.length * (Account.ADDRESS_SHORT_LENGTH + 3);
        for (Tuple3<byte[], BigDecimal, String> item : addresses) {
            len += item.b.unscaledValue().toByteArray().length;
            len += item.c.getBytes(StandardCharsets.UTF_8).length;
        }
        return len;
    }

    public int getLengthDBData() {
        return (totalPay == null ? 1 : 2 + totalPay.unscaledValue().toByteArray().length);
    }

    public byte[] getDBdata() {

        byte[] buff;
        byte[] dbData;

        if (totalPay == null) {
            dbData = new byte[1];
            return dbData;
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
    public static ExListPays parse(byte[] data, int pos) throws Exception {

        int flags = Ints.fromByteArray(Arrays.copyOfRange(data, pos, pos + Integer.BYTES));
        pos += Integer.BYTES;

        Long assetKey;
        int balancePos = 0;
        boolean backward = false;

        assetKey = Longs.fromByteArray(Arrays.copyOfRange(data, pos, pos + Long.BYTES));
        pos += Long.BYTES;

        balancePos = data[pos++];
        if (balancePos < 0) {
            backward = true;
            balancePos = -balancePos;
        }

        Tuple3<byte[], BigDecimal, String>[] addresses = new Tuple3[Ints.fromByteArray(
                Arrays.copyOfRange(data, pos, pos + Integer.BYTES))];
        pos += Integer.BYTES;

        int scale;
        int len;
        for (int i = 0; i < addresses.length; i++) {
            byte[] addressShort = new byte[Account.ADDRESS_SHORT_LENGTH];
            System.arraycopy(data, pos, addressShort, 0, Account.ADDRESS_SHORT_LENGTH);
            pos += Account.ADDRESS_SHORT_LENGTH;

            scale = data[pos++];
            len = data[pos++];
            BigDecimal payValue = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, pos, pos + len)), scale);
            pos += len;

            // MEMO STRING LEN
            int memoLen = Byte.toUnsignedInt(data[pos++]);
            // MEMO STRING
            byte[] memoBytes = Arrays.copyOfRange(data, pos, pos + memoLen);
            String memoStr = new String(memoBytes, StandardCharsets.UTF_8);
            pos += memoLen;

            addresses[i] = new Tuple3<>(addressShort, payValue, memoStr);
        }

        return new ExListPays(flags, assetKey, balancePos, backward, addresses);
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

    public static Fun.Tuple2<ExAction, String> make(Long assetKey,
                                                    int balancePos, boolean backward,
                                                    JSONArray addressesArray) {

        int steep = 0;
        BigDecimal amount;

        if (assetKey == null || assetKey <= 0L) {
            return new Fun.Tuple2<>(null, Lang.T("Wrong asset key (null or ZERO)"));
        }

        Fun.Tuple2<Account, String> result;
        Tuple3<byte[], BigDecimal, String>[] addresses = new Tuple3[addressesArray.size()];
        for (int i = 0; i < addresses.length; i++) {

            JSONArray item = (JSONArray) addressesArray.get(i);

            // CHECH ADDRESS
            result = Account.tryMakeAccount((String) item.get(0));
            if (result.a == null) {
                return new Fun.Tuple2<>(null, (i + 1) + ":" + item.toJSONString() + " - " + result.b);
            }

            // CHECK AMOUNT
            try {
                amount = (BigDecimal) item.get(1);
            } catch (ClassCastException e) {
                return new Fun.Tuple2<>(null, (i + 1) + ":" + item + " - " + Lang.T("Wrong amount # Ошибка в кол-ве"));
            }

            if (amount == null || amount.signum() == 0)
                return new Fun.Tuple2<>(null, (i + 1) + ":" + item + " - " + Lang.T("Wrong amount # Ошибка в кол-ве"));

            // CHECK MEMO
            String memoStr = (String) item.get(2);
            if (memoStr == null)
                memoStr = "";

            // MAKE
            addresses[i] = new Tuple3<>(result.a.getShortAddressBytes(), amount, memoStr);
        }


        int flags = 0;
        return new Fun.Tuple2<>(new ExListPays(flags, assetKey, balancePos, backward, addresses), null);

    }

    public static Fun.Tuple2<ExAction, String> parseJSON_local(JSONObject jsonObject) throws Exception {
        long assetKey = Long.valueOf(jsonObject.getOrDefault("assetKey", 0L).toString());
        int position = Integer.valueOf(jsonObject.getOrDefault("position", 1).toString());
        boolean backward = Boolean.valueOf((boolean) jsonObject.getOrDefault("backward", false));

        JSONArray addressesJson = (JSONArray) jsonObject.get("list");

        return make(assetKey, position, backward, addressesJson);
    }

    public JSONObject toJson() {

        JSONObject toJson = super.toJson();

        JSONArray array = new JSONArray();
        for (Tuple3<byte[], BigDecimal, String> item : addresses) {
            JSONArray itemArray = new JSONArray();
            itemArray.add(Base58.encode(crypto.getAddressFromShortBytes(item.a)));
            itemArray.add(item.b.toPlainString());
            itemArray.add(item.c);

            array.add(itemArray);
        }
        toJson.put("list", array);

        toJson.put("totalFeeBytes", getTotalFeeBytes());
        toJson.put("totalFee", BlockChain.feeBG(getTotalFeeBytes()).toPlainString());

        return toJson;
    }

    @Override
    public String getInfoHTML(boolean onlyTotal, JSONObject langObj) {

        String out = super.getInfoHTML(onlyTotal, langObj);

        if (onlyTotal)
            return out;

        out += "<b>" + Lang.T("Recipients", langObj) + ":</b><br>";
        for (Tuple3<byte[], BigDecimal, String> item : addresses) {
            out += item.b.toPlainString() + " " + crypto.getAddressFromShort(item.a)
                    + (item.c == null || item.c.isEmpty() ? "" : " - " + item.c) + "<br>";
        }

        return out;
    }

    @Override
    public int preProcess(int height, Account creator, boolean andPreValid) {
        makePayList(dcSet, height, asset, creator, andPreValid);
        return resultCode;
    }

    public int isValid(RSignNote rNote) {

        if (this.assetKey == 0L) {
            errorValue = "ListPays: assetKey == null or ZERO";
            return Transaction.INVALID_ITEM_KEY;
        } else if (this.balancePos < TransactionAmount.ACTION_SEND || this.balancePos > TransactionAmount.ACTION_SPEND) {
            errorValue = "ListPays: balancePos out off range";
            return Transaction.INVALID_BALANCE_POS;
        }

        height = rNote.getBlockHeight();

        PublicKeyAccount creator = rNote.getCreator();
        makePayList(dcSet, height, asset, creator, true);
        if (resultCode != Transaction.VALIDATE_OK) {
            // ERROR on make LIST
            return resultCode;
        }

        Account recipient = new Account(addresses[0].a);
        byte[] signature = rNote.getSignature();
        boolean creatorIsPerson = creator.isPerson(dcSet, height);

        // возьмем знаки (минус) для создания позиции баланса такой
        Fun.Tuple2<Integer, Integer> signs = Account.getSignsForBalancePos(balancePos);
        long key = signs.a * assetKey;

        // комиссию не проверяем так как она не правильно считается внутри?
        long actionFlags = Transaction.NOT_VALIDATE_FLAG_FEE;

        BigDecimal totalFeeBG = rNote.getFee();
        Fun.Tuple2<Integer, String> result;
        // проверим как будто всю сумму одному переводим - с учетом комиссии полной
        if (backward) {
            // 1. balancePos == Account.BALANCE_POS_DEBT &&
            // тут надо делать проверку на общую сумму по списку долгов у получателей, подсчитав ее заранее - что накладно
            // иначе она не пройдет - так как у одного адресата нет того долга
            // 2. balancePos == Account.BALANCE_POS_HOLD
            // тут вообще нельзя проверку общую делать
        } else {
            result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                    key, asset, signs.b > 0 ? totalPay : totalPay.negate(), recipient,
                    backward, totalFeeBG, null, creatorIsPerson, actionFlags, rNote.getTimestamp());
            if (result.a != Transaction.VALIDATE_OK) {
                errorValue = "ListPays: totalPay + totalFee = " + totalPay.toPlainString() + " / " + totalFeeBG.toPlainString();
                return result.a;
            }
        }

        int index = 0;
        for (Tuple3<byte[], BigDecimal, String> item : addresses) {

            if (item.a.length != Account.ADDRESS_SHORT_LENGTH) {
                errorValue = "ListPays: item[" + index + "] ->  address len != " + Account.ADDRESS_SHORT_LENGTH;
                return Transaction.INVALID_RECEIVER;
            }

            recipient = new Account(item.a);
            if (recipient == null) {
                errorValue = "ListPays: item[" + index + "]";
                return Transaction.INVALID_RECEIVER;
            }

            if (item.b.signum() == 0) {
                errorValue = "ListPays: item[" + index + "] ->  amount == 0";
                return Transaction.INVALID_AMOUNT;
            }

            if (item.c.getBytes(StandardCharsets.UTF_8).length > 256) {
                errorValue = "ListPays: item[" + index + "] -> Memo string > 256";
                return Transaction.INVALID_TITLE_LENGTH_MAX;
            }

            result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                    key, asset, item.b, recipient,
                    backward, BigDecimal.ZERO, null, creatorIsPerson, actionFlags, rNote.getTimestamp());

            if (result.a != Transaction.VALIDATE_OK) {
                errorValue = "ListPays: item[" + index + "] -> " + recipient.getAddress();
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
        boolean incomeReverse = balancePos == Account.BALANCE_POS_HOLD;
        boolean reversedBalancesInPosition = asset.isReverseBalancePos(balancePos);

        boolean isAmountNegate;
        boolean backwardAction = false;

        Account recipient;
        for (Tuple3<byte[], BigDecimal, String> item : addresses) {

            recipient = new Account(item.a);
            if (recipient == null)
                break;

            // сбросим направление от фильтра
            BigDecimal actionPayAmount = item.b.abs();
            // зададим направление от Действия нашего
            actionPayAmount = signs.b > 0 ? actionPayAmount : actionPayAmount.negate();

            isAmountNegate = item.b.signum() < 0;
            backwardAction = (reversedBalancesInPosition ^ backward) ^ isAmountNegate;

            if (!asOrphan && block != null) {
                rNote.addCalculated(block, recipient, absKey, item.b,
                        asset.viewAssetTypeAction(backwardAction, balancePos, asset.getMaker().equals(creator)));
            }

            if (creator.equals(recipient))
                // пропустим себя в любом случае
                continue;

            TransactionAmount.processAction(dcSet, asOrphan, creator, recipient, balancePos, absKey,
                    asset, actionPayKey, actionPayAmount, backwardAction,
                    incomeReverse);

        }

        if (!asOrphan && block != null) {
            rNote.addCalculated(block, creator, absKey, totalPay.negate(),
                    asset.viewAssetTypeAction(backwardAction, // by last list action on ITEM
                            balancePos, asset.getMaker().equals(creator)));
        }

    }

    public void process(Transaction rNote, Block block) {

        height = rNote.getBlockHeight();
        asset = dcSet.getItemAssetMap().get(assetKey);

        if (results == null) {
            preProcess(rNote);
        }

        processBody(rNote, false, block);

    }

    public void orphan(Transaction rNote) {

        height = rNote.getBlockHeight();
        asset = dcSet.getItemAssetMap().get(assetKey);

        processBody(rNote, true, null);

    }

    public boolean isInvolved(Account account) {
        for (Tuple3<byte[], BigDecimal, String> item : addresses) {
            if (account.equals(item.a))
                return true;
        }
        return false;
    }

}
