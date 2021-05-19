package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * StandardCharsets.UTF_8 JSON "TM" - template key "PR" - template params
 * "HS" - Hashes "MS" - message
 * <p>
 * PARAMS template:TemplateCls param_keys: [id:text] hashes_Set: [name:hash]
 * mess: message title: Title file_Set: [file Name, ZIP? , file byte[]]
 */

public class ExAirdrop {

    public static final byte BASE_LENGTH = 4 + 8 + 8 + 2;

    public static final int MAX_COUNT = Short.MAX_VALUE;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExAirdrop.class);

    /**
     * 0 - version; 1 - flags;
     */
    private int flags; // byte[2]

    private byte[][] addresses;
    private long assetKey;
    private BigDecimal amount;
    private int balancePos;
    boolean backward;

    /////////////////
    DCSet dcSet;
    private int height;
    AssetCls asset;
    BigDecimal totalPay;

    public String errorValue;

    public ExAirdrop(int flags, long assetKey, BigDecimal amount, byte[][] addresses) {
        this.flags = flags;
        this.assetKey = assetKey;
        this.amount = amount;
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


    public void setDC(DCSet dcSet) {
        if (this.dcSet == null || !this.dcSet.equals(dcSet)) {
            this.dcSet = dcSet;
            this.asset = this.dcSet.getItemAssetMap().get(this.assetKey);
        }
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

        return outStream.toByteArray();

    }

    public int length() {
        return BASE_LENGTH + addresses.length * Account.ADDRESS_SHORT_LENGTH;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ExAirdrop parse(byte[] data, int position) throws Exception {

        int scale;
        int len;

        int flags = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + Integer.BYTES));
        position += Integer.BYTES;

        Long assetKey = null;
        int balancePos = 0;
        boolean backward = false;
        BigDecimal amountMin = null;
        BigDecimal amountMax = null;
        int payMethod = 0;
        BigDecimal payMethodValue = null;

        assetKey = Longs.fromByteArray(Arrays.copyOfRange(data, position, position + Long.BYTES));
        position += Long.BYTES;

        balancePos = data[position++];
        backward = data[position++] > 0;
        payMethod = data[position++];

        scale = data[position++];
        len = data[position++];
        payMethodValue = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + len)), scale);
        position += len;

        return new ExAirdrop(flags, assetKey, balancePos, backward, payMethod, payMethodValue, amountMin, amountMax,
                );
    }

    public static Fun.Tuple2<ExAirdrop, String> make(Long assetKey, int balancePos, boolean backward,
                                                     String amountStr, String[] addressesStr) {

        int steep = 0;
        BigDecimal amount;

        Controller cntr = Controller.getInstance();
        BlockChain chain = cntr.getBlockChain();

        try {
            ++steep;
            amount = amountStr == null || amountStr.isEmpty() ? null : new BigDecimal(amountStr);
        } catch (Exception e) {
            String error;
            switch (steep) {
                case 0:
                    error = "Wrong amountMin";
                    break;
                case 1:
                    error = "Wrong amountMax";
                    break;
                case 2:
                    error = "Wrong payMethodValue";
                    break;
                case 3:
                    error = "Wrong filterBalanceMoreThen";
                    break;
                case 4:
                    error = "Wrong filterBalanceLessThen";
                    break;
                case 5:
                    error = "Wrong filterTimeStartStr";
                    break;
                case 6:
                    error = "Wrong filterTimeXEndStr";
                    break;
                default:
                    error = e.getMessage();
            }
            return new Fun.Tuple2<>(null, error);
        }

        if (assetKey == null || assetKey == 0L) {
            return new Fun.Tuple2<>(null, "Wrong assetKey (null or ZERO)");
        } else if (filterAssetKey == null || filterAssetKey == 0L) {
            return new Fun.Tuple2<>(null, "Wrong filterAssetKey (null or ZERO)");
        } else if (amount == null || amount.signum() == 0) {
            return new Fun.Tuple2<>(null, "Wrong payMethodValue (null or ZERO)");
        }

        int flags = 0;
        return new Fun.Tuple2<>(new ExAirdrop(flags, assetKey, balancePos, backward, payMethod, amount, amountMinBG, amountMaxBG,
                filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceMoreThenBG, filterBalanceLessThenBG,
                filterTXType, filterTimeStart, filterTimeEnd,
                filterByPerson, selfPay), null);

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

        return toJson;
    }

    public int checkValidList(DCSet dcSet, int height, AssetCls asset, Account creator) {

        Account recipient;
        //byte[] signature = rNote.getSignature();
        boolean creatorIsPerson = creator.isPerson(dcSet, height);

        // возьмем знаки (минус) для создания позиции баланса такой
        Fun.Tuple2<Integer, Integer> signs = Account.getSignsForBalancePos(balancePos);
        long key = signs.a * assetKey;

        // комиссию не проверяем так как она не правильно считается внутри?
        long actionFlags = Transaction.NOT_VALIDATE_FLAG_FEE;

        int result;
        byte[] signature = new byte[0];
        int index = 0;
        for (byte[] recipientShort : addresses) {

            recipient = new Account(recipientShort);

            result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                    key, asset, amount, recipient,
                    backward, BigDecimal.ZERO, null, creatorIsPerson, actionFlags);

            if (result != Transaction.VALIDATE_OK) {
                errorValue = "Airdrop: address[" + index + "] -> " + recipient.getAddress();
                return result;
            }

            ++index;
        }
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

        // просчитаем значения для точного округления Общей Суммы
        calcTotal();
        if (false) {
            // ошибка подсчета Общего значения - был взят в учет минус общий
            errorValue = "Accruals: PayTotal == 0 && payMethod == PAYMENT_METHOD_TOTAL";
            return Transaction.INVALID_AMOUNT;
        }

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
        int result;
        // проверим как будто всю сумму одному переводим - с учетом комиссии полной
        result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                key, asset, signs.b > 0 ? totalPay : totalPay.negate(), recipient,
                backward, totalFeeBG, null, creatorIsPerson, actionFlags);
        if (result != Transaction.VALIDATE_OK) {
            errorValue = "Airdrop: totalPay + totalFee = " + totalPay.toPlainString() + " / " + totalFeeBG.toPlainString();
            return result;
        }

        ////////// TODO NEED CHECK ALL
        boolean needCheckAllList = false;
        if (needCheckAllList) {

            result = checkValidList(dcSet, height, asset, creator);
            if (result != Transaction.VALIDATE_OK) {
                return result;
            }
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

            actionPayAmount = amount;

            isAmountNegate = actionPayAmount.signum() < 0;
            backwardAction = (reversedBalancesInPosition ^ backward) ^ isAmountNegate;

            if (!asOrphan && block != null) {
                rNote.addCalculated(block, recipient, absKey, actionPayAmount,
                        asset.viewAssetTypeAction(backwardAction, balancePos, asset.getMaker().equals(creator)));
            }

            if (creator.equals(recipient))
                // пропустим себя в любом случае - хотя КАлькулейтед оставим для виду
                continue;

            // сбросим направлени от фильтра
            actionPayAmount = actionPayAmount.abs();
            // зазадим направление от Действия нашего
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
