package org.erachain.dapp.epoch;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransferredBalances;
import org.erachain.dapp.DApp;
import org.erachain.dapp.DAppFactory;
import org.erachain.utils.Pair;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Игра в рулетку. Смарт-контракт ожидает случайное число из будущего - по подписи блока через 3 от блока в который попала ваша транзакция со ставкой на игру.
 * Когда приходит нужный блок, то из его подписи берутся 3 последние цифры. Если все 3 цифры одинаковы и равны нулю, то это ЗЕРО, а если равны 7 - то БИНГО, иначе это МАЖОР.
 * Если цифры не равны, то суммируем их отбрасывая старший разряд,
 * так что всегда получается одна цифра. В итоге выпадет четное или нечетное число или 0.
 * Ставка = количеству денег в платеже на смарт-контракт. Адрес куда переводить ставки - APPC5iANrt6tdDfGHCLV5zmCnjvViC5Bgj
 * На ставку принимается только протокольные активы: ERA и COMPU. Размер ставки от 0.05 для КОМПУ и от 1 для ЭРА. Заголовок транзакции должен содержать всего одну цифру 0 или 1 или 2.
 * Правила игры:
 * - ваш выбор 0. Если выпало ЗЕРО - вы получаете Супер Приз - равный 777 вашим ставкам. Иначе проигрыш.
 * - ваш выбор 1 или 2 и если выпало БИНГО - вы получаете Приз БИНГО - равный 77 вашим ставкам. А если выпали МАЖОР - х12
 * - цифры 0 и 9 не играют в чёт-несёт
 * - ваш выбор 1 и если выпало нечётное число - ваш выигрыш двойная ставка
 * - ваш выбор 2 и если выпало чётное число - ваш выигрыш двойная ставка
 * Результат розыгрыша виден в самой транзакции - в блокэксплорере (сканере блоков).
 * Найти все выигрыши можно если в сканере (блокэксплорере) в разделе Транзакции сделать поиск по размеру выигрыша, например: x77
 */
public class OddEvenDApp extends EpochDAppJson {

    int WAIT_RAND = 3;

    static public final int ID = 777;
    static public final String NAME = "Odd-Even";
    static public final int DISABLED_BEFORE = 5910000;

    // DApp ACCOUNT: APPC5iANrt6tdDfGHCLV5zmCnjvViC5Bgj
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

    static public final BigDecimal MIN_BET_ERA = new BigDecimal("1");
    static public final BigDecimal MIN_BET_COMPU = new BigDecimal("0.05");
    static public final BigDecimal WIN_MULTI = new BigDecimal("2");
    static public final BigDecimal ZERO_MULTI = new BigDecimal("777");
    static public final BigDecimal BINGO_MULTI = new BigDecimal("77");
    static public final BigDecimal MAJOR_MULTI = new BigDecimal("12");

    private OddEvenDApp() {
        super(ID, MAKER);
    }

    public OddEvenDApp(String dataStr, String status) {
        super(ID, MAKER, dataStr, status);
    }

    public OddEvenDApp(String dataStr, Transaction commandTx, Block block) {
        super(ID, MAKER, dataStr, "", commandTx, block);
    }

    @Override
    public DApp of(String dataStr, Transaction commandTx, Block block) {
        if (commandTx instanceof TransferredBalances)
            return new OddEvenDApp(dataStr, commandTx, block);

        return new ErrorDApp("Wrong Transaction type: need 'TransferredBalances' - transfers of asset not found");
    }

    public static void setDAppFactory() {
        OddEvenDApp instance = new OddEvenDApp();
        instance.accountsInfo.add(new Pair<>(MAKER, new String[]{"0", "1", "2"}));
        for (Pair<PublicKeyAccount, ?> pair : instance.accountsInfo) {
            DAppFactory.STOCKS.put(pair.getA(), instance);
        }
        DAppFactory.DAPP_BY_ID.put(ID, instance);
    }

    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public boolean isDisabled(int height) {
        return BlockChain.MAIN_MODE && DISABLED_BEFORE > height;
    }

    @Override
    public boolean isValid() {

        RSend rSend = (RSend) commandTx;
        if (false) {
            return false;
        } else if (rSend.isEncrypted()) {
            fail("Wrong command. Text is encrypted!!!");
            return false;
        } else if (rSend.hasPacket()) {
            fail("Wrong amount. Packet not accepted");
            return false;
        } else if (!rSend.hasAmount()) {
            fail("Empty amount.");
            return false;
        } else if (rSend.getAssetKey() != AssetCls.FEE_KEY && rSend.getAssetKey() != AssetCls.ERA_KEY) {
            fail("Wrong asset. Use only " + AssetCls.FEE_NAME + " or " + AssetCls.ERA_NAME);
            return false;
        } else if (rSend.getAssetKey() == AssetCls.FEE_KEY && rSend.getAmount().compareTo(MIN_BET_COMPU) < 0) {
            fail("Wrong amount. Need >= " + MIN_BET_COMPU.toPlainString());
            return false;
        } else if (rSend.getAssetKey() == AssetCls.ERA_KEY && rSend.getAmount().compareTo(MIN_BET_ERA) < 0) {
            fail("Wrong amount. Need >= " + MIN_BET_ERA.toPlainString());
            return false;
        } else if (rSend.isBackward()) {
            fail("Wrong direction - backward");
            return false;
        } else if (rSend.balancePosition() != Account.BALANCE_POS_OWN) {
            fail("Wrong balance position. Need OWN[1]");
            return false;
        } else if (command == null || !command.equals("0") && !command.equals("1") && !command.equals("2")) {
            fail("Wrong choice. Need set only one digit: 0, 1 or 2 in transaction Text (not encrypted!!!)");
            return false;
        }

        return true;
    }

    /// PARSE / TOBYTES

    public static OddEvenDApp Parse(byte[] bytes, int pos, int forDeal) {

        // skip ID
        pos += 4;

        String data;
        String status;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] statusSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int statusLen = Ints.fromByteArray(statusSizeBytes);
            pos += 4;
            byte[] statusBytes = Arrays.copyOfRange(bytes, pos, pos + statusLen);
            pos += statusLen;
            status = new String(statusBytes, StandardCharsets.UTF_8);

            byte[] dataSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            pos += 4;
            byte[] dataBytes = Arrays.copyOfRange(bytes, pos, pos + dataSize);
            pos += dataSize;
            data = new String(dataBytes, StandardCharsets.UTF_8);

        } else {
            data = "";
            status = "";
        }

        return new OddEvenDApp(data, status);
    }

    ///////// COMMANDS

    protected static int sumLastDigits(String code, int len) {
        int i = code.length();
        char dig;
        int sum = 0;
        int serial = 0;
        while (len > 0 && i > 0) {
            dig = code.charAt(--i);
            if (dig < '0' || dig > '9')
                continue;

            if (serial == 0)
                serial = dig;
            else if (serial > 0 && serial != dig)
                serial = -1;

            sum += dig - '0';
            if (sum > 9)
                // нам нужна только одна цифра
                sum -= 10;

            len--;
        }

        if (serial == '0')
            return -10; // ZERO
        if (len > 0 || serial == '7')
            return -7;
        if (serial > 0)
            return -5;

        return sum;
    }

    /**
     * @param asOrphan
     */
    private boolean catchWin(boolean asOrphan) {
        // рождение выигрыша

        PublicKeyAccount creator = commandTx.getCreator();

        int choice = Integer.parseInt(command);
        BigDecimal bet = commandTx.getAmount();
        BigDecimal win = null;
        String tag = null;

        // найдем выигрышное значение
        int sum = sumLastDigits(Base58.encode(block.getSignature()), 3);
        if (choice == 0 || sum == -10 || sum == 0 || sum == 9) {
            if (choice == 0 && sum == -10) {
                // ZERO
                win = bet.multiply(ZERO_MULTI);
                tag = "x777";
            }
        } else if (sum == -7) {
            win = bet.multiply(BINGO_MULTI);
            tag = "x77"; // БИНГО
        } else if (sum == -5) {
            win = bet.multiply(MAJOR_MULTI);
            tag = "x12"; // МАЖОР
        } else if (sum % 2 == 0 ^ choice == 1) {
            win = bet.multiply(WIN_MULTI);
            tag = "x2";
        }

        if (win != null) {
            // TRANSFER ASSET
            transfer(dcSet, block, commandTx, stock, creator, win, commandTx.getAssetKey(), asOrphan, null, "WIN " + tag + " by DApp: APPC5iANrt6tdDfGHCLV5zmCnjvViC5Bgj");
        }


        if (asOrphan)
            status = "wait";
        else {
            if (win == null) {
                status = String.format("You loose... Block [%d] sum is: %s", block.heightBlock, (sum == -10 ? "ZERO" : sum)) + " See winners by tags: x777, x77, x12, x2";
            } else {
                status = (sum == -10 ? "You win SUPER PRISE!!!! x777 = " : sum == -7 ? "You win BINGO PRIZE!! x77 = " : sum == -5 ? "You win MAJOR PRIZE!! x12 = " : "You win! x2 = ") + win.toPlainString();
            }
        }

        return true;

    }

    @Override
    public boolean process() {

        if (!isValid())
            return true;

        if (commandTx instanceof RSend) {
            if (
                // это не проверка вне блока - в ней блока нет
                    block != null) {
                // рождение комет
                dcSet.getTimeTXWaitMap().put(commandTx.getDBRef(), block.heightBlock + WAIT_RAND);
                status = "wait";
                return false;
            }
        }

        fail("Wrong Transaction type: need 'RSend'");
        return false;

    }

    @Override
    public boolean processByTime() {
        return catchWin(false);
    }

    @Override
    public void orphanBody() {
        dcSet.getTimeTXWaitMap().remove(commandTx.getDBRef());
    }

    @Override
    public void orphanByTime() {
        catchWin(true);
    }

}
