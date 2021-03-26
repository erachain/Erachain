package org.erachain.core.item.assets;


import org.erachain.controller.PairsController;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.database.PairMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IssueItemMap;
import org.erachain.datachain.ItemMap;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;
import org.erachain.utils.NumberAsString;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


// 1019 - Movable = true; Divisible = NO; Quantity = 1
public abstract class AssetCls extends ItemCls {

    public static final int TYPE_KEY = ItemCls.ASSET_TYPE;

    protected static final int ASSET_TYPE_LENGTH = 1;
    //
    protected int assetType;


    // CORE KEY
    public static final long ERA_KEY = 1L;
    public static final String ERA_ABBREV = "ERA"; // ERA (main rights units)
    public static final String ERA_NAME = "ERA";
    public static final String ERA_DESCR = "Основная учётная единица, мера собственности и управления данной средой - \"правовая\", \"управляющая\"" + ": "
            + ERA_NAME + "(" + ERA_ABBREV + "). "
            + "Именно единицы Эра позволяют собирать блоки и получать комиссию с упакованных в них транзакций"
            + ". "
            + ("Более чем %MIN% ЭРА, находящихся в пользовании на счету позволяет собирать блоки (форжить) с этого счёта, а более чем %MINOR% позволяет удостоверять других участников среды"
            .replace("%MIN%", "" + BlockChain.MIN_GENERATING_BALANCE)
            .replace("%MINOR%", "" + BlockChain.MINOR_ERA_BALANCE))
            + ". "
            + "Число единиц %GENERAL% ЭРА дает права создавать новые статусы и другие сущности в среде"
            .replace("%GENERAL%", "" + BlockChain.GENERAL_ERA_BALANCE)
            + ".";

    // FEE KEY
    public static final long FEE_KEY = 2L;
    public static final String FEE_ABBREV = "CMP"; // COMPU (compute units)
    public static final String FEE_NAME = "COMPU";
    public static final String FEE_DESCR = "Основная учётная единица среды, используемая для оплаты комиссий за внесение записей в среду - \"рабочая\", \"оплатная\"" + ": "
            + FEE_NAME + "(" + FEE_ABBREV + "). ";

    // TRUST KEY
    public static final long TRUST_KEY = 3L;
    public static final String TRUST_ABBREV = "АЗЫ"; // COMPU (compute units)
    public static final String TRUST_NAME = "АЗЫ";
    public static final String TRUST_DESCR = "Честь, доблесть и доверие" + ": "
            + TRUST_NAME + "(" + TRUST_ABBREV + "). ";

    // REAL KEY
    public static final long REAL_KEY = 4L;
    public static final String REAL_ABBREV = "ВЕД"; // COMPU (compute units)
    public static final String REAL_NAME = "ВЕДЫ";
    public static final String REAL_DESCR = "Труд, знания, заслуги и польза" + ": "
            + REAL_NAME + "(" + REAL_ABBREV + "). ";

    // DEaL KEY
    public static final long LIA_KEY = 5L;
    public static final String LIA_ABBREV = "LIA"; //
    public static final String LIA_NAME = "LIA";
    public static final String LIA_DESCR = "Life ID Asset (" + LIA_NAME + ")";

    public static final long BTC_KEY = 12L;

    public static final int UNIQUE = 1;
    public static final int VENTURE = 2;
    public static final int NAME = 3;
    public static final int INITIAL_FAVORITES = 100;

    ///////////////////////////////////////////////////
    /**
     * GOODS
     * передача в собственность, взять на хранение
     * 0 : движимая вещь вовне - может быть доставлена и передана на хранение (товары)
     */
    public static final int AS_OUTSIDE_GOODS = 0; // movable

    /**
     * ASSETS
     * передача имущества не требует действий во вне - все исполняется тут же. Их можно дать в долг и заьрать самостоятельно
     * Требования не предъявляются.
     * 3 : цифровое имущество - не требует действий вовне и исполняется внутри платформы (токены, цифровые валюты, цифровые билеты, цифровые права и т.д.)
     */
    public static final int AS_INSIDE_ASSETS = 1;

    /**
     * IMMOVABLE
     * передача в сосбтвенность, дать в аренду (по графику времени), взять на охрану
     * 1 : недвижимая вещь вовне - может быть передана в аренду (недвижимость)
     */

    public static final int AS_OUTSIDE_IMMOVABLE = 2;

    /**
     * outside CURRENCY
     * +++ деньги вовне - можно истребовать вернуть и подтвердить получение денег
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_CURRENCY = 11;

    /**
     * outside SERVICE
     * +++ услуги во вне
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_SERVICE = 12; // UTILITY

    /**
     * outside SHARE
     * +++ акция предприятия вовне
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_SHARE = 13;

    /**
     * outside BILL - вексель
     * +++ вексель на оплату во вне
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_BILL = 14;

    /**
     * outside BILL - вексель
     * +++ вексель на оплату во вне
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_BILL_EX = 15;

    /**
     * my debt
     * +++ мой долг перед другим лицом - это обязательство
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_MY_DEBT = 26;

    /**
     * 🕐🕜🕑🕝🕒🕞🕓🕟🕔🕠🕕🕡🕖🕢🕗🕣🕘🕤🕙🕥🕚🕦🕛🕧
     * outside WORK TIME - рабочее время, которое можно купить и потребовать потратить и учесть как затрата
     */
    public static final int AS_OUTSIDE_WORK_TIME_MINUTES = 34;
    public static final int AS_OUTSIDE_WORK_TIME_HOURS = 35;

    /**
     * outside CLAIMS
     * +++ требования и обязательства вовне - можно истребовать право и подтвердить его исполнение (ссуда, займ, услуга, право, требование, деньги, билеты и т.д.)
     * <p>
     * учет обязательств прав и требований на услуги и действия во внешнем мире - в том числе займы, ссуды, кредиты, фьючерсы и т.д.
     * нельзя вернуть эмитенту - но можно потребовать исполнение прав и можно подтвердить исполнение (погасить требование)
     * это делается теми же трнзакциями что выдать и забрать долг у внутренних активов
     * И в момент погашения одновременно передается как имущество эмитенту
     */
    public static final int AS_OUTSIDE_OTHER_CLAIM = 49;

    ///////////////
    /**
     * inside CURRENCY
     * +++ деньги
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_CURRENCY = 51;

    /**
     * inside CLAIMS
     * +++ требования и обязательства
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_UTILITY = 52; // SERVICE

    /**
     * inside CLAIMS
     * +++ требования и обязательства
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_SHARE = 53;

    /**
     * inside BONUS
     * +++ бонусы - для анонимов так же платежи возможны
     * === ASSET - без обмена на бирже и можно анонимам переводить
     */
    public static final int AS_INSIDE_BONUS = 54;

    /**
     * inside RIGHTS
     * +++ права и доступы
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     * можно вернуть право себе создателю и справо дается не в долг а как на харанение - и потом любой может забрать с хранения
     * 2 баланса - имущечтыо и хранение - при передаче? короче каждый может кто имеет право выдавать или назначать право
     * потом забирать назначение с баланса Хранить - получается как с движимым товарос
     */
    public static final int AS_INSIDE_ACCESS = 55;

    /**
     * inside VOTE
     * +++ права и доступы
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_VOTE = 56;

    /**
     * bank guarantee - банковская гарантия
     * === полный аналог AS_INSIDE_ASSETS по действиям в протоколе - чисто для наименования другого - так как не требует действий 2-й стороны - скорее бухгалтерская единица?
     */

    public static final int AS_BANK_GUARANTEE = 60;
    /**
     * bank guarantee total - банковская гарантия общая сумма - так как не требует действий 2-й стороны - скорее бухгалтерская единица?
     * === полный аналог AS_INSIDE_ASSETS по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_BANK_GUARANTEE_TOTAL = 61;

    /**
     * NFT - Non Fungible Token. невзаимозаменяемый токен
     * === полный аналог AS_INSIDE_ASSETS по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_NON_FUNGIBLE = 65;
    public static final int AS_RELEASED_FUNGIBLE = 67;

    /**
     * INDEXES (FOREX etc.)
     * +++ требования и обязательства
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INDEX = 100;

    /**
     * inside CLAIMS
     * +++ требования и обязательства
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_OTHER_CLAIM = 119;

    /**
     * ACCOUNTING
     * учетные единицы - нельзя на бирже торговать - они ничего не стоят, можно делать любые действия от своего имени
     * 4 : учетные единицы - не имеет стоимости и не может быть продано (бухгалтерский учет)
     */
    public static final int AS_ACCOUNTING = 123;

    /**
     * self-managed
     * === Не может управляться ни кем кроме обладателя актива
     * === доступны 4-ре баланса и у каждого работает Возврат - backward
     */
    public static final int AS_SELF_MANAGED_ACCOUNTING = 124;

    /**
     * accounting loan
     * +++ мой займ другому лицу - учетный, бухгалтерский учет
     * === подобно AS_SELF_MANAGED_ACCOUNTING - но долговой баланс - отражает требование к оплате
     */
    public static final int AS_SELF_ACCOUNTING_LOAN = 125;

    /**
     * mutual aid fund
     * +++ фонд взаимопомощи - учетный, бухгалтерский учет
     * === подобно AS_SELF_MANAGED_ACCOUNTING - по-идее тут без требований к оплате
     */
    public static final int AS_SELF_ACCOUNTING_MUTUAL_AID_FUND = 126;

    /**
     * cash fund
     * +++ денежный фонд - для учета взносов ТСЖ например - учетный, бухгалтерский учет
     * === подобно AS_SELF_MANAGED_ACCOUNTING - c требованиями к оплате и с автоматическим снятием требования (DEBT) при погашении
     */
    public static final int AS_SELF_ACCOUNTING_CASH_FUND = 127;

    /**
     * self-managed - direct OWN balances
     * === Не может управляться ни кем кроме обладателя актива
     * === доступны 4-ре баланса и у каждого работает Возврат - backward
     */
    public static final int AS_SELF_MANAGED_DIRECT_SEND = 128;
    /**
     * self-managed - direct OWN balances
     * === Не может управляться ни кем кроме обладателя актива
     * === доступны 4-ре баланса и у каждого работает Возврат - backward
     */
    public static final int AS_SELF_MANAGED_SHARE = 129;

    protected AssetCls(byte[] typeBytes, long[] flags, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int assetType) {
        super(typeBytes, flags, owner, name, icon, image, description);
        this.assetType = assetType;

    }

    public AssetCls(int type, byte pars, long[] flags, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int assetType) {
        this(new byte[TYPE_LENGTH], flags, owner, name, icon, image, description, assetType);
        this.typeBytes[0] = (byte) type;
        this.typeBytes[1] = pars;
    }

    //GETTERS/SETTERS

    public static int[] assetTypes;

    public static int[] assetTypes() {

        if (assetTypes != null)
            return assetTypes;

        int[] array = new int[]{

                AS_OUTSIDE_GOODS,
                AS_OUTSIDE_IMMOVABLE,
                AS_OUTSIDE_CURRENCY,
                AS_OUTSIDE_SERVICE,
                AS_OUTSIDE_BILL,
                AS_OUTSIDE_WORK_TIME_HOURS,
                AS_OUTSIDE_WORK_TIME_MINUTES,
                AS_OUTSIDE_SHARE,

                AS_MY_DEBT,

                AS_OUTSIDE_OTHER_CLAIM,

                AS_INSIDE_ASSETS,
                AS_INSIDE_CURRENCY,
                AS_INSIDE_UTILITY,
                AS_INSIDE_SHARE,
                AS_INSIDE_BONUS,
                AS_INSIDE_ACCESS,
                AS_INSIDE_VOTE,
                AS_BANK_GUARANTEE,
                AS_BANK_GUARANTEE_TOTAL,
                AS_NON_FUNGIBLE,
                AS_INDEX,
                AS_INSIDE_OTHER_CLAIM,

                AS_ACCOUNTING,
                AS_SELF_MANAGED_ACCOUNTING,
                AS_SELF_ACCOUNTING_LOAN,
                AS_SELF_ACCOUNTING_MUTUAL_AID_FUND,
                AS_SELF_ACCOUNTING_CASH_FUND,
                AS_SELF_MANAGED_DIRECT_SEND,
                AS_SELF_MANAGED_SHARE
        };

        if (BlockChain.TEST_MODE) {
            // AS_SELF_ACCOUNTING_CASH_FUND,
        }

        Arrays.sort(array);

        return array;
    }

    @Override
    public int getItemType() {
        return TYPE_KEY;
    }

    @Override
    public long START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return START_KEY_OLD;
    }

    @Override
    public long MIN_START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return MIN_START_KEY_OLD;
    }

    @Override
    public String getItemTypeName() {
        return "asset";
    }

    // DB
    @Override
    public ItemMap getDBMap(DCSet db) {
        return db.getItemAssetMap();
    }

    @Override
    public IssueItemMap getDBIssueMap(DCSet db) {
        return db.getIssueAssetMap();
    }

    //public abstract long getQuantity();

    public abstract BigDecimal getReleased();
    public abstract BigDecimal getReleased(DCSet dc);

    public int getAssetType() {
        return this.assetType;
    }

    // https://unicode-table.com/ru/#23FC
    public static String charAssetType(long key, int assetType) {

        if (key < 100) {
            return "";
        }

        switch (assetType) {
            case AS_OUTSIDE_GOODS:
                return "▲";
            case AS_OUTSIDE_IMMOVABLE:
                return "▼";
            case AS_ACCOUNTING:
                if (key == 555L || key == 666L || key == 777L)
                    return "♥";

                return "±";
            case AS_INDEX:
                return "⤴";
            case AS_INSIDE_VOTE:
                return "✋";
            case AS_OUTSIDE_BILL:
                return "⬖"; // ⬒
            case AS_OUTSIDE_SERVICE:
                return "⬔";
            case AS_INSIDE_BONUS:
                return "⮌";
            case AS_INSIDE_ACCESS:
                return "⛨";
            case AS_INSIDE_SHARE:
                return "◒";
            case AS_SELF_MANAGED_ACCOUNTING:
            case AS_SELF_ACCOUNTING_LOAN:
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
            case AS_SELF_ACCOUNTING_CASH_FUND:
            case AS_SELF_MANAGED_DIRECT_SEND:
            case AS_SELF_MANAGED_SHARE:
                return "±";
            case AS_MY_DEBT:
                return "◆";
            case AS_OUTSIDE_WORK_TIME_HOURS:
                // 🕐🕜🕑🕝🕒🕞🕓🕟🕔🕠🕕🕡🕖🕢🕗🕣🕘🕤🕙🕥🕚🕦🕛🕧
                return "◕";
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return "◔";


        }

        if (assetType >= AS_OUTSIDE_CURRENCY
                && assetType <= AS_OUTSIDE_OTHER_CLAIM)
            return "◄";

        if (assetType == AS_INSIDE_ASSETS
                || assetType >= AS_INSIDE_CURRENCY
                && assetType <= AS_INSIDE_OTHER_CLAIM)
            return "►";

        // ● ⚫ ◆ █ ▇ ■ ◢ ◤ ◔ ◑ ◕ ⬛ ⬜ ⬤ ⛃
        return "⚫";

    }

    public String charAssetType() {
        return charAssetType(this.key, this.assetType);
    }

    @Override
    public String getName() {
		/*
		if (this.key == 1)
			return "ERA";
		 */

        return this.name;
    }

    @Override
    public int getMinNameLen() {
        return 1;
    }

    @Override
    public String viewName() {

        if (this.key < 100) {
            return this.name;
        } else if (key < getStartKey()) {
            return charAssetType() + this.name;
        }

        return charAssetType() + viewAssetTypeAbbrev() + ":" + this.name;

    }

    public PublicKeyAccount getMaker() {
        if (this.key > 10 && this.key < 100 && BlockChain.ASSET_OWNERS.containsKey(this.key)) {
            return BlockChain.ASSET_OWNERS.get(this.key);
        }

        return this.maker;
    }

    @Override
    public String getDescription() {
		/*
		if (this.key == 1)
			return "'Управляющая единица' (единица доли собственности) - подобна акции предприятия. Дает право собирать блоки тем чаще, чем больше Вы имеете их в обладании. Так же дает право удостоверять других персон и создавать новые статусы.";
		else if (this.key == 2)
			return "'Рабочая единица' (единица оплаты) - применяется для оплаты транзакций и как награда за сборку блоков.";
		else if (this.key == 3)
			return "'Доверяющая единица' (единица доверия) - применяется для оценки доверия и чести личности.";
		else if (this.key == 4)
			return "'Полезная единица' (единица пользы) - применяется для оценки пользы личности.";
		else if (this.key == 5)
			return "'Деловая единица' (единица деловитости) - применяется для оценки деловитости и активности личности.";
		 */

        return this.description;
    }

    @Override
    public String viewDescription() {
        switch ((int) this.key) {
            case 1:
                return "<b>ERA</b> is an <u>Accounting Unit</u> allowing a User, that has a sufficient amount of such units and with such sufficiency threshold preset in the ERACHAIN Software, to use the ERACHAIN Software for making his Request Entries to the Log, including provision of such service to other Users, receiving it for the COMPU Accounting Units and producing new COMPU Accounting Units as per the ERACHAIN Software rules and operating procedure available on the Erachain.org website. For more information see Erachain Licence Agreement on the <a href=\"http://erachain.org\">Erachain.org</a>.";
            case 2:
                return "<b>COMPU</b> is an <u>Accounting Unit</u> allowing a User that has a sufficient amount of such units, with such sufficiency threshold computed in the ERACHAIN Software, to use the ERACHAIN Software for entering that User’s Request Entries on the Log, both on his own and by having such service provided by other Users. The COMPU Accounting Unit operates on the Log as a unit used to pay for the provision of service of making an entry to the Log. For more information see Erachain Licence Agreementon the <a href=\"http://erachain.org\">Erachain.org</a>.";
        }

        return this.description;
    }

    @Override
    public String[] getTags() {
        return new String[]{":" + viewAssetTypeAbbrev().toLowerCase()};
    }

    @Override
    public byte[] getIcon() {
        switch ((int) key) {
            case 1:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/ERA.png"));
                } catch (Exception e) {
                }
                return icon;
            case 2:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/COMPU.png"));
                } catch (Exception e) {
                }
                return icon;
            case 3:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/LIA.png"));
                } catch (Exception e) {
                }
                return icon;
            case 12:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/BTC.gif"));
                } catch (Exception e) {
                }
                return icon;
            case 82:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/UAH.png"));
                } catch (Exception e) {
                }
                return icon;
            case 83:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/KZT.png"));
                } catch (Exception e) {
                }
                return icon;
            case 84:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/KGS.png"));
                } catch (Exception e) {
                }
                return icon;
            case 85:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/BYN.png"));
                } catch (Exception e) {
                }
                return icon;
            case 92:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/RUB.png"));
                } catch (Exception e) {
                }
                return icon;
            case 93:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/CNY.png"));
                } catch (Exception e) {
                }
                return icon;
            case 94:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/EUR.png"));
                } catch (Exception e) {
                }
                return icon;
            case 95:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/USD.png"));
                } catch (Exception e) {
                }
                return icon;
        }
        return icon;
    }

    @Override
    public byte[] getImage() {
        if (key < 1000 && image.length > 0)
            return new byte[0];

        return image;
    }

    public abstract long getQuantity();

    public abstract int getScale();

    @Override
    public HashMap getNovaItems() {
        return BlockChain.NOVA_ASSETS;
    }

    public boolean isMovable() {
        if (false && this.key < BlockChain.AMOUNT_SCALE_FROM) {
            return (this.typeBytes[1] & (byte) 1) > 0;
        }
        return this.assetType == AS_OUTSIDE_GOODS;
    }

    public boolean isImMovable() {
        if (false && this.key < BlockChain.AMOUNT_SCALE_FROM) {
            return (this.typeBytes[1] & (byte) 1) <= 0;
        }
        return this.assetType == AS_OUTSIDE_IMMOVABLE;
    }

    public boolean isInsideType() {
        return this.assetType == AS_INSIDE_ASSETS
                || this.assetType >= AS_INSIDE_CURRENCY
                && this.assetType <= AS_INSIDE_OTHER_CLAIM;
    }

    public boolean isInsideCurrency() {
        return this.assetType == AS_INSIDE_CURRENCY;
    }

    public boolean isInsideUtility() {
        return this.assetType == AS_INSIDE_UTILITY;
    }

    public boolean isInsideShare() {
        return this.assetType == AS_INSIDE_SHARE;
    }

    public boolean isInsideBonus() {
        return this.assetType == AS_INSIDE_BONUS;
    }

    public boolean isInsideAccess() {
        return this.assetType == AS_INSIDE_ACCESS;
    }

    public boolean isInsideVote() {
        return this.assetType == AS_INSIDE_VOTE;
    }

    public boolean isIndex() {
        return this.assetType == AS_INDEX;
    }

    public boolean isInsideOtherClaim() {
        return this.assetType == AS_INSIDE_OTHER_CLAIM;
    }

    /**
     * Их нельзя вернуть из долга самостоятельно
     *
     * @return
     */
    public boolean isNotReDebted() {
        return isOutsideType();
    }

    public boolean isOutsideType() {
        return isOutsideType(this.assetType);
    }

    public static boolean isOutsideType(int assetType) {
        return // ?? this.assetType == AS_OUTSIDE_GOODS ||
                assetType >= AS_OUTSIDE_CURRENCY
                        && assetType <= AS_OUTSIDE_OTHER_CLAIM;
    }

    public boolean isOutsideCurrency() {
        return this.assetType == AS_OUTSIDE_CURRENCY;
    }

    public boolean isOutsideService() {
        return this.assetType == AS_OUTSIDE_SERVICE;
    }

    public boolean isOutsideShare() {
        return this.assetType == AS_OUTSIDE_SHARE;
    }

    public boolean isOutsideBill() {
        return this.assetType == AS_OUTSIDE_BILL;
    }

    public boolean isOutsideBillEx() {
        return this.assetType == AS_OUTSIDE_BILL_EX;
    }

    public boolean isOutsideOtherClaim() {
        return this.assetType == AS_OUTSIDE_OTHER_CLAIM;
    }

    public static boolean isUnHoldable(long key, int assetType) {
        if (key < getStartKey(ItemCls.ASSET_TYPE, AssetCls.START_KEY_OLD, AssetCls.MIN_START_KEY_OLD)
                || assetType == AS_INSIDE_ASSETS
                || assetType > AS_OUTSIDE_OTHER_CLAIM
                && assetType <= AS_INSIDE_OTHER_CLAIM
        ) {
            return true;
        }
        return false;
    }

    public boolean isUnHoldable() {
        return isUnHoldable(key, assetType);
    }

    public static boolean isUnSpendable(long key, int assetType) {
        return key < 100
                || assetType == AssetCls.AS_INDEX
                || assetType == AssetCls.AS_INSIDE_ACCESS
                || assetType == AssetCls.AS_INSIDE_BONUS;
    }

    public boolean isUnSpendable() {
        return isUnSpendable(key, assetType);
    }

    public static boolean isUnTransferable(long key, int assetType, boolean senderIsAssetMaker) {
        return assetType == AssetCls.AS_NON_FUNGIBLE && !senderIsAssetMaker;
    }

    public boolean isUnTransferable(boolean senderIsAssetMaker) {
        return isUnTransferable(key, assetType, senderIsAssetMaker);
    }

    public static boolean isUnDebtable(long key, int assetType) {
        return assetType == AssetCls.AS_INDEX
                || assetType == AssetCls.AS_INSIDE_BONUS;
    }

    public boolean isUnDebtable() {
        return isUnDebtable(key, assetType);
    }

    public static boolean isTypeUnique(int assetType, long quantity) {
        if (quantity == 1L
                || assetType == AS_OUTSIDE_BILL
                || assetType == AS_OUTSIDE_BILL_EX
                || assetType == AS_BANK_GUARANTEE
                || assetType == AS_NON_FUNGIBLE
        ) {
            return true;
        }
        return false;
    }

    public abstract boolean isUnique();

    public abstract boolean isUnlimited(Account address, boolean notAccounting);

    /**
     * Управлять может только сам обладатель
     *
     * @return
     */
    public boolean isSelfManaged() {
        return assetType >= AS_SELF_MANAGED_ACCOUNTING;
    }

    /**
     * Активы у которых есть только 4-ре баланса и каждый из них имеет возможность забрать - backward
     *
     * @return
     */
    public boolean isDirectBalances() {
        return assetType >= AS_SELF_MANAGED_ACCOUNTING;
    }

    public static boolean isAccounting(int assetType) {
        return assetType >= AS_ACCOUNTING;
    }

    public boolean isAccounting() {
        return isAccounting(assetType);
    }

    public boolean isSendPersonProtected() {
        return (key <= AssetCls.ERA_KEY || key > getStartKey()) // GATE Assets
                && assetType != AssetCls.AS_NON_FUNGIBLE
                && !isAccounting()
                && assetType != AssetCls.AS_INSIDE_BONUS
                && assetType != AssetCls.AS_INSIDE_VOTE;
    }

    /**
     * Actions on OWN balance will update DEBT balance too
     *
     * @return
     */
    public boolean isChangeDebtBySendActions() {
        return this.assetType == AS_SELF_ACCOUNTING_CASH_FUND;
    }

    public static boolean isChangeDebtBySpendActions(int assetType) {
        return isOutsideType(assetType);
    }

    public boolean isChangeDebtBySpendActions() {
        return isChangeDebtBySpendActions(this.assetType);
    }

    /**
     * Если обратный Послать то в меню местами меняем
     *
     * @return
     */
    public static boolean isReverseSend(int assetType) {
        return assetType == AS_SELF_MANAGED_ACCOUNTING
                || assetType == AS_SELF_ACCOUNTING_MUTUAL_AID_FUND
                || assetType == AS_SELF_ACCOUNTING_CASH_FUND;
    }

    public boolean isReverseSend() {
        return isReverseSend(this.assetType);
    }


    /**
     * в обычном сотоянии тут отрицательные балансы или нет?
     *
     * @param balPos
     * @return
     */
    public static boolean isReverseBalancePos(int assetType, int balPos) {

        switch (balPos) {
            case Account.BALANCE_POS_OWN:
                return isReverseSend(assetType);
            case Account.BALANCE_POS_SPEND:
                return true;
        }
        return false;
    }

    /**
     * в обычном сотоянии тут отрицательные балансы или нет?
     *
     * @param balPos
     * @return
     */
    public boolean isReverseBalancePos(int balPos) {
        return isReverseBalancePos(this.assetType, balPos);
    }

    public BigDecimal defaultAmountAssetType() {
        switch (assetType) {
            case AS_BANK_GUARANTEE:
            case AS_NON_FUNGIBLE:
                return BigDecimal.ONE;
        }
        return isUnique() ? BigDecimal.ONE : null;
    }

    public PublicKeyAccount defaultRecipient(int actionType, boolean backward) {

        if (isOutsideType()) {
            if (actionType == Account.BALANCE_POS_SPEND
                    || actionType == Account.BALANCE_POS_DEBT) {
                return getMaker();
            }
        }

        return null;
    }

    public static String viewAssetTypeCls(int assetType) {
        switch (assetType) {
            case AS_OUTSIDE_GOODS:
                return "Movable";
            case AS_OUTSIDE_IMMOVABLE:
                return "Immovable";

            case AS_OUTSIDE_CURRENCY:
                return "Outside Currency";
            case AS_OUTSIDE_WORK_TIME_HOURS:
                return "Work Time [hours]";
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return "Work Time [minutes]";
            case AS_OUTSIDE_SERVICE:
                return "Outside Service";
            case AS_OUTSIDE_SHARE:
                return "Outside Share";
            case AS_OUTSIDE_BILL:
                return "AS_OUTSIDE_BILL_N";
            case AS_OUTSIDE_BILL_EX:
                return "Bill of exchange";
            case AS_MY_DEBT:
                return "AS_MY_DEBT_N";
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Outside Other Claim";

            case AS_INSIDE_ASSETS:
                return "Digital Asset";
            case AS_INSIDE_CURRENCY:
                return "Digital Currency";
            case AS_INSIDE_UTILITY:
                return "Digital Utility";
            case AS_INSIDE_SHARE:
                return "Digital Share";
            case AS_INSIDE_BONUS:
                return "Digital Bonus";
            case AS_INSIDE_ACCESS:
                return "Digital Access";
            case AS_INSIDE_VOTE:
                return "Digital Vote";
            case AS_BANK_GUARANTEE:
                return "Bank Guarantee";
            case AS_BANK_GUARANTEE_TOTAL:
                return "Accounting Bank Guarantee";
            case AS_NON_FUNGIBLE:
                return "Non Fungible Token";
            case AS_INDEX:
                return "Index";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other Digital Claim";

            case AS_ACCOUNTING:
                return "Accounting";
            case AS_SELF_MANAGED_ACCOUNTING:
                return "Self Managed";
            case AS_SELF_ACCOUNTING_LOAN:
                return "AS_SELF_ACCOUNTING_LOAN_N";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                return "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_N";
            case AS_SELF_ACCOUNTING_CASH_FUND:
                return "AS_SELF_ACCOUNTING_CASH_FUND_N";
            case AS_SELF_MANAGED_DIRECT_SEND:
                return "AS_SELF_MANAGED_DIRECT_SEND_N";
            case AS_SELF_MANAGED_SHARE:
                return "AS_SELF_MANAGED_SHARE_N";

        }
        return null;
    }

    public String viewAssetType() {
        return viewAssetTypeCls(this.assetType);
    }

    public static String viewAssetTypeFullCls(int assetType) {
        switch (assetType) {
            case AS_OUTSIDE_GOODS:
                return "Movable Goods";
            case AS_OUTSIDE_IMMOVABLE:
                return "Immovable Goods, Real Estate";
            case AS_OUTSIDE_CURRENCY:
                return "Outside Currency";
            case AS_OUTSIDE_WORK_TIME_HOURS:
                return "Work Time [hours]";
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return "Work Time [minutes]";
            case AS_OUTSIDE_SERVICE:
                return "Outside Service";
            case AS_OUTSIDE_SHARE:
                return "Outside Share Rights";
            case AS_OUTSIDE_BILL:
                return "AS_OUTSIDE_BILL_NF";
            case AS_OUTSIDE_BILL_EX:
                return "Bill of Exchange";
            case AS_MY_DEBT:
                return "AS_MY_DEBT_NF";
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Other Outside Right of Claim";

            case AS_INSIDE_ASSETS:
                return "Digital Asset";
            case AS_INSIDE_CURRENCY:
                return "Digital Currency";
            case AS_INSIDE_UTILITY:
                return "Digital Utility";
            case AS_INSIDE_SHARE:
                return "Digital Share";
            case AS_INSIDE_BONUS:
                return "Bonuses, Loyalty Points";
            case AS_INSIDE_ACCESS:
                return "Digital Access Rights";
            case AS_INSIDE_VOTE:
                return "Digital Vote";
            case AS_BANK_GUARANTEE:
                return "Bank Guarantee";
            case AS_BANK_GUARANTEE_TOTAL:
                return "Accounting Bank Guarantee";
            case AS_NON_FUNGIBLE:
                return "Non Fungible Token";
            case AS_INDEX:
                return "Digital Index";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other Digital Right of Claim";

            case AS_ACCOUNTING:
                return "Accounting";
            case AS_SELF_MANAGED_ACCOUNTING:
                return "Self Managed for Accounting";
            case AS_SELF_ACCOUNTING_LOAN:
                return "AS_SELF_ACCOUNTING_LOAN_NF";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                return "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_NF";
            case AS_SELF_ACCOUNTING_CASH_FUND:
                return "AS_SELF_ACCOUNTING_CASH_FUND_NF";
            case AS_SELF_MANAGED_DIRECT_SEND:
                return "AS_SELF_MANAGED_DIRECT_SEND_NF";
            case AS_SELF_MANAGED_SHARE:
                return "AS_SELF_MANAGED_SHARE_NF";

        }
        return null;
    }

    public static String viewAssetTypeFullClsAndChars(int assetType) {
        return charAssetType(Long.MAX_VALUE, assetType) + viewAssetTypeAbbrev(assetType) + ":" + viewAssetTypeFullCls(assetType);
    }

    public String viewAssetTypeFullClsAndChars() {
        return charAssetType(Long.MAX_VALUE, assetType) + viewAssetTypeAbbrev(assetType) + ":" + viewAssetTypeFullCls(assetType);
    }

    public static String viewAssetTypeAbbrev(int asset_type) {
        switch (asset_type) {
            case AS_OUTSIDE_GOODS:
                return "OGd";
            case AS_OUTSIDE_IMMOVABLE:
                return "UIm";
            case AS_OUTSIDE_CURRENCY:
                return "OCr";
            case AS_OUTSIDE_WORK_TIME_HOURS:
                return "WH";
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return "WM";
            case AS_OUTSIDE_SERVICE:
                return "OSv";
            case AS_OUTSIDE_SHARE:
                return "OSh";
            case AS_OUTSIDE_BILL:
                return "PNo"; // Promissory Note";
            case AS_OUTSIDE_BILL_EX:
                return "BEx"; //Bill of Exchange";
            case AS_MY_DEBT:
                return "Dbt"; // Debt to Loaner
            case AS_OUTSIDE_OTHER_CLAIM:
                return "OCl";

            case AS_INSIDE_ASSETS:
                return "Ast";
            case AS_INSIDE_CURRENCY:
                return "Cur";
            case AS_INSIDE_UTILITY:
                return "Utl";
            case AS_INSIDE_SHARE:
                return "Shr";
            case AS_INSIDE_BONUS:
                return "Bon";
            case AS_INSIDE_ACCESS:
                return "Rit";
            case AS_INSIDE_VOTE:
                return "Vte";
            case AS_BANK_GUARANTEE:
                return "BGu";
            case AS_BANK_GUARANTEE_TOTAL:
                return "BGuT";
            case AS_NON_FUNGIBLE:
                return "NFT";
            case AS_INDEX:
                return "Idx";
            case AS_INSIDE_OTHER_CLAIM:
                return "CLM";

            case AS_ACCOUNTING:
                return "Acc";
            case AS_SELF_MANAGED_ACCOUNTING:
                return "SAcc";
            case AS_SELF_ACCOUNTING_LOAN:
                return "AccL";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                return "AccAF";
            case AS_SELF_ACCOUNTING_CASH_FUND:
                return "AccCF";
            case AS_SELF_MANAGED_DIRECT_SEND:
                return "AccDS";
            case AS_SELF_MANAGED_SHARE:
                return "AccSh";
        }
        return "?";
    }

    public String viewAssetTypeAbbrev() {
        return viewAssetTypeAbbrev(assetType);
    }

    public String viewAssetTypeFull() {
        return viewAssetTypeFullCls(this.assetType);
    }

    public static String viewAssetTypeDescriptionCls(int assetType) {
        switch (assetType) {
            case AS_OUTSIDE_GOODS:
                return "Movable things and goods. These goods can be taken for storage by the storekeeper or for confirmation of delivery. In this case you can see the balances on the accounts of storekeepers and delivery agents";
            case AS_OUTSIDE_IMMOVABLE:
                return "Real estate and other goods and things not subject to delivery. Such things can be taken and given for rent and handed over to the guard";
            case AS_OUTSIDE_CURRENCY:
                return "AS_OUTSIDE_CURRENCY_D";
            case AS_OUTSIDE_WORK_TIME_HOURS:
                return "AS_OUTSIDE_WORK_TIME_HOURS_D";
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return "AS_OUTSIDE_WORK_TIME_MINUTES_D";
            case AS_OUTSIDE_SERVICE:
                return "An external service that needs to be provided outside. To notify your wish to provide services you must make demands and then confirm the fulfillment";
            case AS_OUTSIDE_SHARE:
                return "External shares which have to be transferred to an external depository. The depositary can be notified by presenting the claim and then confirm the shares transfer";
            case AS_OUTSIDE_BILL:
                return "AS_OUTSIDE_BILL_D";
            case AS_OUTSIDE_BILL_EX:
                return "A digital bill of exchange can be called for redemption by external money. You can take it into your hands";
            case AS_MY_DEBT:
                return "AS_MY_DEBT_D";
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Other external rights, requirements and obligations. Any obligation (as well as other external assets), which can be claimed by the record \"summon\" and discharged by the record \"confirmation of fulfillment\" of this obligation. You can take it into your hands";
            case AS_INSIDE_ASSETS:
                return "Internal (digital) asset. It does not require any external additional actions when transferring between accounts inside Erachain";
            case AS_INSIDE_CURRENCY:
                return "Digital money";
            case AS_INSIDE_UTILITY:
                return "Digital service or a cost is something that can be used inside Erachain nvironment, for example as a payment for external services";
            case AS_INSIDE_SHARE:
                return "Digital share. The share of ownership of an external or internal enterpris, the possession of which establishes the right to own the corresponding share of the enterprise without the need to take any external actions";
            case AS_INSIDE_BONUS:
                return "Digital loyalty points, bonuses, awards, discount points (bonus). It has no generally accepted value and can not be exchanged for other types of assets inside the Erachain environment. The exchange for other bonuses and rewards are allowed";
            case AS_INSIDE_ACCESS:
                return "Digital rights of access and control, membership, pass";
            case AS_INSIDE_VOTE:
                return "A digital voice for voting";
            case AS_BANK_GUARANTEE:
                return "A digital bank guarantee";
            case AS_BANK_GUARANTEE_TOTAL:
                return "A digital accounting bank guarantee";
            case AS_NON_FUNGIBLE:
                return "AS_NON_FUNGIBLE_D";
            case AS_INDEX:
                return "Index on foreign and domestic assets, for example currencies on FOREX";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other digital rights, requirements and obligations. These assets (as well as other digital assets) can be given in debt and seized by the lender";
            case AS_ACCOUNTING:
                return "AS_ACCOUNTING_D";
            case AS_SELF_MANAGED_ACCOUNTING:
                return "AS_SELF_MANAGED_ACCOUNTING_D";
            case AS_SELF_ACCOUNTING_LOAN:
                return "AS_SELF_ACCOUNTING_LOAN_D";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                return "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_D";
            case AS_SELF_ACCOUNTING_CASH_FUND:
                return "AS_SELF_ACCOUNTING_CASH_FUND_D";
            case AS_SELF_MANAGED_DIRECT_SEND:
                return "AS_SELF_MANAGED_DIRECT_SEND_D";
            case AS_SELF_MANAGED_SHARE:
                return "AS_SELF_MANAGED_SHARE_D";

        }
        return "";
    }

    public static String viewAssetTypeDescriptionDEX(int assetType, long key) {
        if (key < 100)
            return "AS_CURRENCY_100_DEX";

        switch (assetType) {
            case AS_OUTSIDE_CURRENCY:
            case AS_INSIDE_CURRENCY:
            case AS_NON_FUNGIBLE:
                return "AS_NON_FUNGIBLE_DEX";
        }
        return null;
    }

    public static String viewAssetTypeAction(long assetKey, int assetType, boolean backward, int actionType, boolean isCreatorMaker) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "Transfer to the ownership ";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "Confiscate from rent" : "Transfer to rent";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "Return from rent";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "The employment security/received from security" : null;
                    default:
                        return null;
                }
            case AS_OUTSIDE_CURRENCY:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null // для формирования списка действия надо выдать НУЛЬ
                                : isCreatorMaker ? "AS_OUTSIDE_CURRENCY_Issue" : "AS_OUTSIDE_CURRENCY_1";
                    case Account.BALANCE_POS_DEBT:
                        return isCreatorMaker ? null
                                : backward ? "AS_OUTSIDE_CURRENCY_2B" // Отозвать требование об исполнении денежного требования
                                : "AS_OUTSIDE_CURRENCY_2"; // Потребовать исполнения денежного требования
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null
                                : isCreatorMaker ? null
                                : "AS_OUTSIDE_CURRENCY_4"; // Подтвердить исполнение денежного требования
                    default:
                        return null;
                }
            case AS_OUTSIDE_WORK_TIME_HOURS:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "AS_OUTSIDE_WORK_TIME_HOURS_1"; // Передать в собственность рабочие часы
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_OUTSIDE_WORK_TIME_HOURS_2B" // Отозвать требование траты рабочих часов
                                : "AS_OUTSIDE_WORK_TIME_HOURS_2"; // Потребовать потратить рабочие часы
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_OUTSIDE_WORK_TIME_HOURS_4"; // Подтвердить затраты рабочих часов
                    default:
                        return null;
                }
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "AS_OUTSIDE_WORK_TIME_MINUTES_1"; // Передать в собственность рабочие минуты
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_OUTSIDE_WORK_TIME_MINUTES_2B" // Отозвать требование траты рабочих минут
                                : "AS_OUTSIDE_WORK_TIME_MINUTES_2"; // Потребовать потратить рабочие минуты
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_OUTSIDE_WORK_TIME_MINUTES_4"; // Подтвердить затраты рабочих минут
                    default:
                        return null;
                }
            case AS_OUTSIDE_SERVICE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "Transfer Service Requirement";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "To reduce the provision of services" // Отозвать требование в предоставлении услуг
                                : "To require the provision of services";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "Confirm the provision of services";
                    default:
                        return null;
                }
            case AS_OUTSIDE_SHARE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "To transfer shares in the property";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "To reduce the transfer of shares"
                                : "To require the transfer of shares";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "Return debt";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "Confirm receipt of shares";
                    default:
                        return null;
                }
            case AS_OUTSIDE_BILL:
            case AS_OUTSIDE_BILL_EX:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "AS_OUTSIDE_BILL_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_OUTSIDE_BILL_2B"
                                : "AS_OUTSIDE_BILL_2";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_OUTSIDE_BILL_4";
                    default:
                        return null;
                }
            case AS_MY_DEBT:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null
                                : isCreatorMaker ? "AS_MY_DEBT_Issue" : "AS_MY_DEBT_1";
                    case Account.BALANCE_POS_DEBT:
                        return isCreatorMaker ? null // эмитент долга не может делать требования
                                : backward ? "AS_MY_DEBT_2B"
                                : "AS_MY_DEBT_2";
                    case Account.BALANCE_POS_SPEND:
                        return isCreatorMaker ? null // эмитент долга не может делать погашения
                                : backward ? null : "AS_MY_DEBT_4";
                    default:
                        return null;
                }
            case AS_OUTSIDE_OTHER_CLAIM:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_OUTSIDE_OTHER_CLAIM_Issue"
                                : "AS_OUTSIDE_OTHER_CLAIM_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_OUTSIDE_OTHER_CLAIM_2B"
                                : "AS_OUTSIDE_OTHER_CLAIM_2";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_OUTSIDE_OTHER_CLAIM_4";
                    default:
                        return null;
                }
            case AS_INSIDE_CURRENCY:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_CURRENCY_Issue"
                                : "AS_INSIDE_CURRENCY_1";
                    default:
                        return null;
                }
            case AS_INSIDE_UTILITY:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_UTILITY_Issue"
                                : "AS_INSIDE_UTILITY_1";
                    default:
                        return null;
                }
            case AS_INSIDE_SHARE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_SHARE_Issue"
                                : "AS_INSIDE_SHARE_1";
                    default:
                        return null;
                }
            case AS_INSIDE_BONUS:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_BONUS_Issue"
                                : "AS_INSIDE_BONUS_1";
                    default:
                        return null;
                }
            case AS_INSIDE_ACCESS:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_ACCESS_Issue" : "AS_INSIDE_ACCESS_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_INSIDE_ACCESS_2B"
                                : "AS_INSIDE_ACCESS_2";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "AS_INSIDE_ACCESS_2R";
                    case Account.BALANCE_POS_SPEND:
                        return "AS_INSIDE_ACCESS_4";
                    default:
                        return null;
                }
            case AS_INSIDE_VOTE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_VOTE_Issue" : "AS_INSIDE_VOTE_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_INSIDE_VOTE_2B"
                                : "AS_INSIDE_VOTE_2";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "AS_INSIDE_VOTE_2R";
                    case Account.BALANCE_POS_SPEND:
                        return "AS_INSIDE_VOTE_4";
                    default:
                        return null;
                }
            case AS_BANK_GUARANTEE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_BANK_GUARANTEE_Issue" : "AS_BANK_GUARANTEE_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_BANK_GUARANTEE_2B" : "AS_BANK_GUARANTEE_2";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "AS_BANK_GUARANTEE_2R";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "AS_BANK_GUARANTEE_3" : null;
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_BANK_GUARANTEE_4";
                    default:
                        return null;
                }
            case AS_BANK_GUARANTEE_TOTAL:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_BANK_GUARANTEE_TOTAL_Issue" : "AS_BANK_GUARANTEE_TOTAL_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_BANK_GUARANTEE_TOTAL_2B" : "AS_BANK_GUARANTEE_TOTAL_2";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "AS_BANK_GUARANTEE_TOTAL_2R";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_BANK_GUARANTEE_TOTAL_4";
                    default:
                        return null;
                }
            case AS_NON_FUNGIBLE: {
                if (actionType == Account.BALANCE_POS_OWN) {
                    return backward ? null : isCreatorMaker ? "AS_NON_FUNGIBLE_Issue" : null;
                }
                return null;
            }
            case AS_INDEX:
                break;
            case AS_INSIDE_OTHER_CLAIM:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_OTHER_CLAIM_Issue" : "AS_INSIDE_OTHER_CLAIM_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_INSIDE_OTHER_CLAIM_2B"
                                : "AS_INSIDE_OTHER_CLAIM_2";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_INSIDE_OTHER_CLAIM_4";
                    default:
                        return null;
                }
            case AS_ACCOUNTING:
                break;
            case AS_SELF_MANAGED_ACCOUNTING:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_MANAGED_ACCOUNTING_1B" : "AS_SELF_MANAGED_ACCOUNTING_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_SELF_MANAGED_ACCOUNTING_2B" : "AS_SELF_MANAGED_ACCOUNTING_2";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "AS_SELF_MANAGED_ACCOUNTING_3B" : "AS_SELF_MANAGED_ACCOUNTING_3";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? "AS_SELF_MANAGED_ACCOUNTING_4B" : "AS_SELF_MANAGED_ACCOUNTING_4";
                    default:
                        return null;
                }
            case AS_SELF_ACCOUNTING_LOAN:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_ACCOUNTING_LOAN_1B" : "AS_SELF_ACCOUNTING_LOAN_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_SELF_ACCOUNTING_LOAN_2B" : "AS_SELF_ACCOUNTING_LOAN_2";
                    case Account.BALANCE_POS_HOLD:
                        // SPEND нельзя брать так как он Баланс Мой изменит у меня
                        return backward ? "AS_SELF_ACCOUNTING_LOAN_3B" : "AS_SELF_ACCOUNTING_LOAN_3";
                    default:
                        return null;
                }
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_1B" : "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_1";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_4B" : "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_4";
                    default:
                        return null;
                }
            case AS_SELF_ACCOUNTING_CASH_FUND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_ACCOUNTING_CASH_FUND_1B" : "AS_SELF_ACCOUNTING_CASH_FUND_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_SELF_ACCOUNTING_CASH_FUND_2B" : "AS_SELF_ACCOUNTING_CASH_FUND_2";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? "AS_SELF_ACCOUNTING_CASH_FUND_4B" : "AS_SELF_ACCOUNTING_CASH_FUND_4";
                    default:
                        return null;
                }
            case AS_SELF_MANAGED_DIRECT_SEND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_MANAGED_DIRECT_SEND_1B" : "AS_SELF_MANAGED_DIRECT_SEND_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_SELF_MANAGED_DIRECT_SEND_2B" : "AS_SELF_MANAGED_DIRECT_SEND_2";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "AS_SELF_MANAGED_DIRECT_SEND_3B" : "AS_SELF_MANAGED_DIRECT_SEND_3";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? "AS_SELF_MANAGED_DIRECT_SEND_4B" : "AS_SELF_MANAGED_DIRECT_SEND_4";
                    default:
                        return null;
                }
            case AS_SELF_MANAGED_SHARE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_MANAGED_SHARE_1B" : "AS_SELF_MANAGED_SHARE_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_SELF_MANAGED_SHARE_2B" : "AS_SELF_MANAGED_SHARE_2";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "AS_SELF_MANAGED_SHARE_3B" : "AS_SELF_MANAGED_SHARE_3";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? "AS_SELF_MANAGED_SHARE_4B" : "AS_SELF_MANAGED_SHARE_4";
                    default:
                        return null;
                }

        }

        switch (actionType) {
            case Account.BALANCE_POS_OWN:
                return backward ? null : "Transfer to the ownership";
            case Account.BALANCE_POS_DEBT:
                return backward ? "To confiscate a debt"
                        : "Transfer to debt";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return backward ? null : "Return debt";
            case Account.BALANCE_POS_HOLD:
                return isUnHoldable(assetKey, assetType) ? null
                        : backward ? "Confirm acceptance \"in hand\"" : null;
            case Account.BALANCE_POS_SPEND:
                return backward ? null : "Spend";
            case Account.BALANCE_POS_PLEDGE:
                return backward ? null //"Re-pledge"
                        : null; //"Pledge";
            case TransactionAmount.ACTION_RESERVED_6:
                // for CALCULATED TX
                return null; // backward ? "Reserved 6-" : "Reserved 6+";
        }

        return null;
    }

    public String viewAssetTypeAction(boolean backward, int actionType, boolean isCreatorMaker) {
        return viewAssetTypeAction(key, assetType, backward, actionType, isCreatorMaker);
    }

    public static String viewAssetTypeAdditionAction(long assetKey, int assetType, boolean backward, int actionType, boolean isCreatorMaker) {
        switch (assetType) {
            case AS_SELF_ACCOUNTING_CASH_FUND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return "AS_SELF_SEND_ADDITIONAL_ACT_DEBT";
                }
        }

        if (actionType == Account.BALANCE_POS_SPEND && isChangeDebtBySpendActions(assetType)) {
            return "AdditionAction_on_isChangeDebtBySpendActions";
        }

        return null;
    }

    /**
     * isMirrorDebtBySend - same
     *
     * @param backward
     * @param actionType
     * @param isCreatorMaker
     * @return
     */
    public String viewAssetTypeAdditionAction(boolean backward, int actionType, boolean isCreatorMaker) {
        return viewAssetTypeAdditionAction(key, assetType, backward, actionType, isCreatorMaker);
    }

    /**
     * Balance Position + Backward + Action Name
     *
     * @param assetKey
     * @param assetType
     * @param isCreatorMaker
     * @param useAddedActions
     * @return
     */
    public static List<Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>> viewAssetTypeActionsList(long assetKey,
                                                                                                  int assetType, Boolean isCreatorMaker, boolean useAddedActions) {

        List<Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>> list = new ArrayList<>();

        String actionStr;
        String addActionStr;
        Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String> item;
        for (int balPos : TransactionAmount.ACTIONS_LIST) {

            boolean backward = !AssetCls.isReverseSend(assetType) || balPos != Account.BALANCE_POS_OWN;

            actionStr = viewAssetTypeAction(assetKey, assetType, !backward, balPos,
                    isCreatorMaker != null ? isCreatorMaker : true);
            if (actionStr != null) {
                item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, !backward), actionStr);
                if (!list.contains(item)) {
                    list.add(item);
                    if (useAddedActions) {
                        addActionStr = viewAssetTypeAdditionAction(assetKey, assetType, !backward, balPos,
                                isCreatorMaker != null ? isCreatorMaker : true);
                        if (addActionStr != null) {
                            item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, !backward), addActionStr);
                            list.add(item);
                        }
                    }
                }
            }

            if (isCreatorMaker == null) {
                actionStr = viewAssetTypeAction(assetKey, assetType, !backward, balPos,
                        false);
                if (actionStr != null) {
                    item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, !backward), actionStr);
                    if (!list.contains(item)) {
                        list.add(item);
                        if (useAddedActions) {
                            addActionStr = viewAssetTypeAdditionAction(assetKey, assetType, !backward, balPos,
                                    false);
                            if (addActionStr != null) {
                                item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, !backward), addActionStr);
                                list.add(item);
                            }
                        }
                    }
                }
            }

            actionStr = viewAssetTypeAction(assetKey, assetType, backward, balPos,
                    isCreatorMaker != null ? isCreatorMaker : true);
            if (actionStr != null) {
                item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, backward), actionStr);
                if (!list.contains(item)) {
                    list.add(item);
                    if (useAddedActions) {
                        addActionStr = viewAssetTypeAdditionAction(assetKey, assetType, backward, balPos,
                                isCreatorMaker != null ? isCreatorMaker : true);
                        if (addActionStr != null) {
                            item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, backward), addActionStr);
                            list.add(item);
                        }
                    }
                }
            }

            if (isCreatorMaker == null) {
                actionStr = viewAssetTypeAction(assetKey, assetType, backward, balPos,
                        false);
                if (actionStr != null) {
                    item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, backward), actionStr);
                    if (!list.contains(item)) {
                        list.add(item);
                        if (useAddedActions) {
                            addActionStr = viewAssetTypeAdditionAction(assetKey, assetType, backward, balPos,
                                    false);
                            if (addActionStr != null) {
                                item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, backward), addActionStr);
                                list.add(item);
                            }
                        }
                    }
                }
            }
        }

        return list;
    }

    public List<Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>> viewAssetTypeActionsList(Boolean isCreatorMaker, boolean useAddedActions) {
        return viewAssetTypeActionsList(key, assetType, isCreatorMaker, useAddedActions);
    }

    public String viewAssetTypeActionTitle(boolean backward, int actionType, boolean isCreatorMaker) {
        return viewAssetTypeAction(backward, actionType, isCreatorMaker);
    }

    public static String viewAssetTypeCreator(int assetType, boolean backward, int actionType, boolean isCreatorMaker) {
        switch (assetType) {
            case AS_MY_DEBT:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return isCreatorMaker ? "Debtor" : "Lender";
                    case Account.BALANCE_POS_DEBT:
                    case Account.BALANCE_POS_SPEND:
                        return isCreatorMaker ? null // эмитент долга не может делать требования
                                : "Debtor";
                    default:
                        return null;
                }
            case AS_BANK_GUARANTEE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                    case Account.BALANCE_POS_DEBT:
                    case Account.BALANCE_POS_SPEND:
                        return "Guarantee";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Beneficiary";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "Beneficiary" : null;
                }
            case AS_BANK_GUARANTEE_TOTAL:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                    case Account.BALANCE_POS_DEBT:
                        return "Guarantee";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Beneficiary";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "Beneficiary" : null;
                    case Account.BALANCE_POS_SPEND:
                        return "Spender";
                }
            case AS_NON_FUNGIBLE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "Author" : null;
                }
            case AS_SELF_MANAGED_ACCOUNTING:
            case AS_SELF_MANAGED_DIRECT_SEND:
            case AS_SELF_MANAGED_SHARE:
                return "Accountant";
            case AS_SELF_ACCOUNTING_LOAN:
                return "Lender";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
            case AS_SELF_ACCOUNTING_CASH_FUND:
                return "Cashier";
            case AS_OUTSIDE_IMMOVABLE:
            case AS_OUTSIDE_CURRENCY:
            case AS_OUTSIDE_SERVICE:
            case AS_OUTSIDE_SHARE:
            case AS_OUTSIDE_BILL:
            case AS_OUTSIDE_BILL_EX:
            case AS_OUTSIDE_OTHER_CLAIM:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return isCreatorMaker ? "Issuer" : "Sender";
                    case Account.BALANCE_POS_DEBT:
                    case Account.BALANCE_POS_SPEND:
                        return isCreatorMaker ? null : "Issuer";
                    default:
                        return null;
                }
            case AS_INSIDE_ASSETS:
            case AS_INSIDE_CURRENCY:
            case AS_INSIDE_UTILITY:
            case AS_INSIDE_SHARE:
            case AS_INSIDE_BONUS:
            case AS_INSIDE_ACCESS:
            case AS_INSIDE_VOTE:
            case AS_INDEX:
            case AS_INSIDE_OTHER_CLAIM:
            case AS_ACCOUNTING:
        }

        switch (actionType) {
            case Account.BALANCE_POS_OWN:
                return backward ? null : "Sender";
            case Account.BALANCE_POS_DEBT:
                return "Creditor";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return "Debtor";
            case Account.BALANCE_POS_HOLD:
                return backward ? "Taker" : null;
            case Account.BALANCE_POS_SPEND:
                return backward ? null : "Spender";
        }

        return null;
    }

    public String viewAssetTypeCreator(boolean backward, int actionType, boolean isCreatorMaker) {
        return viewAssetTypeCreator(assetType, backward, actionType, isCreatorMaker);
    }

    public static String viewAssetTypeTarget(int assetType, boolean backward, int actionType, boolean isRecipientMaker) {
        switch (assetType) {
            case AS_MY_DEBT:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return isRecipientMaker ? null : "Lender"; // Тут может быть начальная эмиссия к Кредитору и переуступка - тоже кредитору по сути
                    case Account.BALANCE_POS_DEBT:
                    case Account.BALANCE_POS_SPEND:
                        return isRecipientMaker ?
                                "Debtor"
                                : null; // реципиент только эмитент долга;
                    default:
                        return null;
                }
            case AS_BANK_GUARANTEE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return "Recipient";
                    case Account.BALANCE_POS_DEBT:
                        return "Beneficiary";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Guarantee";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "Guarantee" : null;
                    case Account.BALANCE_POS_SPEND:
                        return "Spender";
                }
            case AS_BANK_GUARANTEE_TOTAL:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return "Recipient";
                    case Account.BALANCE_POS_DEBT:
                        return "Principal";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Guarantee";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "Guarantee" : null;
                    case Account.BALANCE_POS_SPEND:
                        return "Spender";
                }
            case AS_NON_FUNGIBLE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "Recipient";
                }
            case AS_SELF_MANAGED_ACCOUNTING:
            case AS_SELF_MANAGED_DIRECT_SEND:
            case AS_SELF_MANAGED_SHARE:
                return "Ledger";
            case AS_SELF_ACCOUNTING_LOAN:
                return "Debtor";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return "Benefactor";
                    case Account.BALANCE_POS_SPEND:
                        return "Recipient";
                }
            case AS_SELF_ACCOUNTING_CASH_FUND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                    case Account.BALANCE_POS_DEBT:
                        return "Participant";
                    case Account.BALANCE_POS_SPEND:
                        return "Recipient";
                }
            case AS_OUTSIDE_IMMOVABLE:
            case AS_OUTSIDE_CURRENCY:
            case AS_OUTSIDE_SERVICE:
            case AS_OUTSIDE_SHARE:
            case AS_OUTSIDE_BILL:
            case AS_OUTSIDE_BILL_EX:
            case AS_OUTSIDE_OTHER_CLAIM:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return isRecipientMaker ? "Issuer" : "Recipient";
                    case Account.BALANCE_POS_DEBT:
                    case Account.BALANCE_POS_SPEND:
                        return isRecipientMaker ? "Issuer" : null;
                    default:
                        return null;
                }
            case AS_INSIDE_ASSETS:
            case AS_INSIDE_CURRENCY:
            case AS_INSIDE_UTILITY:
            case AS_INSIDE_SHARE:
            case AS_INSIDE_BONUS:
            case AS_INSIDE_ACCESS:
            case AS_INSIDE_VOTE:
            case AS_INDEX:
            case AS_INSIDE_OTHER_CLAIM:
            case AS_ACCOUNTING:
        }

        switch (actionType) {
            case Account.BALANCE_POS_OWN:
                return "Recipient";
            case Account.BALANCE_POS_DEBT:
                return "Debtor";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return "Creditor";
            case Account.BALANCE_POS_HOLD:
                return "Supplier";
            case Account.BALANCE_POS_SPEND:
                return "Spender";
        }

        return null;
    }

    public String viewAssetTypeTarget(boolean backward, int actionType, boolean isRecipientMaker) {
        return viewAssetTypeTarget(assetType, backward, actionType, isRecipientMaker);

    }

    public String viewAssetTypeActionOK(boolean backward, int actionType, boolean isCreatorMaker) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
            case AS_OUTSIDE_CURRENCY:
            case AS_OUTSIDE_SERVICE:
            case AS_OUTSIDE_SHARE:
            case AS_OUTSIDE_BILL:
            case AS_OUTSIDE_BILL_EX:
            case AS_OUTSIDE_OTHER_CLAIM:
            case AS_INSIDE_ASSETS:
            case AS_INSIDE_CURRENCY:
            case AS_INSIDE_UTILITY:
            case AS_INSIDE_SHARE:
            case AS_INSIDE_BONUS:
            case AS_INSIDE_ACCESS:
            case AS_INSIDE_VOTE:
            case AS_BANK_GUARANTEE:
            case AS_BANK_GUARANTEE_TOTAL:
            case AS_NON_FUNGIBLE:
            case AS_INDEX:
            case AS_INSIDE_OTHER_CLAIM:
            case AS_ACCOUNTING:
        }

        return viewAssetTypeAction(backward, actionType, isCreatorMaker) + " # to";

    }

    public int getOperations(DCSet dcSet) {
        return dcSet.getOrderMap().getCountOrders(key);
    }

    //OTHER
    public static JSONObject AssetTypeJson(int assetType, JSONObject langObj) {

        JSONObject assetTypeJson = new JSONObject();
        assetTypeJson.put("id", assetType);
        assetTypeJson.put("name", Lang.T(AssetCls.viewAssetTypeCls(assetType), langObj));
        assetTypeJson.put("nameFull", Lang.T(AssetCls.viewAssetTypeFullCls(assetType), langObj));

        long startKey = ItemCls.getStartKey(
                AssetCls.ASSET_TYPE, AssetCls.START_KEY_OLD, AssetCls.MIN_START_KEY_OLD);
        List<Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>> actions = AssetCls.viewAssetTypeActionsList(startKey,
                assetType, null, true);
        StringJoiner joiner = new StringJoiner(", ");
        JSONArray actionsArray = new JSONArray();
        for (Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String> actionItem : actions) {
            int action = actionItem.a.a;
            boolean backward = actionItem.a.b;

            joiner.add(Lang.T(actionItem.b, langObj));
            JSONObject actionJson = new JSONObject();
            actionJson.put("position", action);
            actionJson.put("backward", backward);
            actionJson.put("name", Lang.T(actionItem.b, langObj));

            String name;
            //// CREATOR
            name = viewAssetTypeCreator(assetType, backward, action, false);
            if (name != null) actionJson.put("creator", Lang.T(name, langObj));

            name = viewAssetTypeCreator(assetType, backward, action, true);
            if (name != null) actionJson.put("creator_owner", Lang.T(name, langObj));

            //////// TARGET
            name = viewAssetTypeTarget(assetType, backward, action, false);
            if (name != null) actionJson.put("target", Lang.T(name, langObj));

            name = viewAssetTypeTarget(assetType, backward, action, true);
            if (name != null) actionJson.put("target_owner", Lang.T(name, langObj));

            actionsArray.add(actionJson);
        }

        assetTypeJson.put("actions", actionsArray);

        String description = Lang.T(AssetCls.viewAssetTypeDescriptionCls(assetType), langObj) + ".<br>";
        if (AssetCls.isReverseSend(assetType)) {
            description += Lang.T("Actions for OWN balance is reversed", langObj) + ".<br>";
        }
        description += "<b>" + Lang.T("Acceptable actions", langObj) + ":</b><br>" + joiner.toString();

        String dexDesc = AssetCls.viewAssetTypeDescriptionDEX(assetType, startKey);
        if (dexDesc != null) {
            description += "<br><b>" + Lang.T("DEX rules and taxes", langObj) + ":</b><br>" + Lang.T(dexDesc, langObj);
        }

        assetTypeJson.put("description", description);

        return assetTypeJson;
    }

    public static JSONObject assetTypesJson;

    public static JSONObject AssetTypesActionsJson() {

        if (assetTypesJson != null)
            return assetTypesJson;

        assetTypesJson = new JSONObject();
        for (String iso : Lang.getInstance().getLangListAvailable().keySet()) {
            JSONObject langObj = Lang.getInstance().getLangJson(iso);
            JSONObject langJson = new JSONObject();
            for (int type : assetTypes()) {
                langJson.put(type, AssetTypeJson(type, langObj));
            }
            assetTypesJson.put(iso, langJson);
        }
        return assetTypesJson;
    }

    public static JSONObject typeJson(int type) {

        String assetTypeName;

        assetTypeName = viewAssetTypeCls(type);
        if (assetTypeName == null)
            return null;

        JSONObject typeJson = new JSONObject();

        JSONObject langObj = Lang.getInstance().getLangJson("en");

        long startKey = getStartKey(ItemCls.ASSET_TYPE, AssetCls.START_KEY_OLD, AssetCls.MIN_START_KEY_OLD);
        typeJson.put("key", type);
        typeJson.put("char", charAssetType(startKey, type));
        typeJson.put("abbrev", viewAssetTypeAbbrev(type));
        typeJson.put("name", Lang.T(assetTypeName, langObj));
        typeJson.put("name_full", Lang.T(viewAssetTypeFullCls(type), langObj));
        typeJson.put("desc", Lang.T(viewAssetTypeDescriptionCls(type), langObj));
        String dexDesc = AssetCls.viewAssetTypeDescriptionDEX(type, startKey);
        if (dexDesc != null) {
            typeJson.put("desc_DEX", Lang.T(dexDesc, langObj));
        }

        return typeJson;
    }

    public static JSONArray typesJson() {

        JSONArray types = new JSONArray();

        for (int i = 0; i < 256; i++) {
            JSONObject json = typeJson(i);
            if (json == null)
                continue;

            types.add(json);
        }
        return types;
    }

    public String viewProperties(JSONObject langObj) {

        StringJoiner joiner = new StringJoiner(", ");

        if (isImMovable())
            joiner.add(Lang.T("ImMovable", langObj));
        if (isUnlimited(maker, false))
            joiner.add(Lang.T("Unlimited", langObj));
        if (isAccounting())
            joiner.add(Lang.T("Accounting", langObj));
        if (isUnique())
            joiner.add(Lang.T("Unique", langObj));
        if (isUnHoldable())
            joiner.add(Lang.T("Not holdable", langObj));
        if (isOutsideType())
            joiner.add(Lang.T("Outside Claim", langObj));
        if (isSelfManaged())
            joiner.add(Lang.T("Self Managed", langObj));
        if (isChangeDebtBySendActions())
            joiner.add(Lang.T("isChangeDebtBySendActions", langObj));
        if (isChangeDebtBySpendActions())
            joiner.add(Lang.T("isChangeDebtBySpendActions", langObj));
        if (isDirectBalances())
            joiner.add(Lang.T("isDirectBalances", langObj));
        if (isNotReDebted())
            joiner.add(Lang.T("isNotReDebted", langObj));
        if (isOutsideOtherClaim())
            joiner.add(Lang.T("isOutsideOtherClaim", langObj));
        if (isReverseSend())
            joiner.add(Lang.T("isReverseSend", langObj));

        return joiner.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject assetJSON = super.toJson();

        JSONObject landObj = Lang.getInstance().getLangJson("en");

        // ADD DATA
        assetJSON.put("assetTypeKey", this.assetType);
        assetJSON.put("assetTypeName", Lang.T(viewAssetType(), landObj));
        assetJSON.put("assetTypeDesc", Lang.T(viewAssetTypeDescriptionCls(assetType), landObj));

        String dexDesc = AssetCls.viewAssetTypeDescriptionDEX(assetType, START_KEY());
        if (dexDesc != null) {
            assetJSON.put("type_desc_DEX", Lang.T(dexDesc, landObj));
        }

        assetJSON.put("released", this.getReleased());
        assetJSON.put("type_key", this.assetType);
        assetJSON.put("type_char", charAssetType());
        assetJSON.put("type_abbrev", viewAssetTypeAbbrev());
        assetJSON.put("type_name", Lang.T(viewAssetType(), landObj));
        assetJSON.put("type_name_full", Lang.T(viewAssetTypeFull(), landObj));
        assetJSON.put("type_desc", Lang.T(viewAssetTypeDescriptionCls(assetType), landObj));

        assetJSON.put("scale", this.getScale());
        assetJSON.put("quantity", this.getQuantity());

        assetJSON.put("isImMovable", this.isImMovable());
        assetJSON.put("isUnlimited", this.isUnlimited(maker, false));
        assetJSON.put("isAccounting", this.isAccounting());
        assetJSON.put("isUnique", this.isUnique());
        assetJSON.put("isUnHoldable", this.isUnHoldable());
        assetJSON.put("isOutsideType", this.isOutsideType());
        assetJSON.put("isSelfManaged", this.isSelfManaged());
        assetJSON.put("isChangeDebtBySendActions", this.isChangeDebtBySendActions());
        assetJSON.put("isChangeDebtBySpendActions", this.isChangeDebtBySpendActions());
        assetJSON.put("isDirectBalances", this.isDirectBalances());
        assetJSON.put("isNotReDebted", this.isNotReDebted());
        assetJSON.put("isOutsideOtherClaim", this.isOutsideOtherClaim());
        assetJSON.put("isReverseSend", this.isReverseSend());

        JSONObject revPos = new JSONObject();
        for (int pos = Account.BALANCE_POS_OWN; pos <= Account.BALANCE_POS_6; pos++) {
            revPos.put("" + pos, isReverseBalancePos(pos));
        }
        assetJSON.put("reversedBalPos", revPos);


        return assetJSON;
    }

    public JSONObject jsonForExplorerPage(JSONObject langObj, Object[] args) {

        JSONObject assetJSON = super.jsonForExplorerPage(langObj, args);
        assetJSON.put("assetTypeNameFull", charAssetType() + viewAssetTypeAbbrev() + ":" + Lang.T(viewAssetTypeFull(), langObj));

        assetJSON.put("quantity", this.getQuantity());

        BigDecimal released = getReleased();
        assetJSON.put("released", released);

        if (args != null) {
            // параметры для показа Объемов торгов
            AssetCls quoteAsset = (AssetCls) args[0];
            TradePair tradePair = PairsController.reCalcAndUpdate(this, quoteAsset, (PairMap) args[1], 10);

            BigDecimal price = tradePair.getLastPrice();
            if (price.signum() == 0) {
                price = tradePair.getLower_askPrice();
                if (price.signum() == 0) {
                    price = tradePair.getHighest_bidPrice();
                }
            }
            BigDecimal marketCap = released.multiply(price);
            assetJSON.put("marketCap", marketCap);
            assetJSON.put("price", price);

            assetJSON.put("changePrice", tradePair.getFirstPrice().signum() > 0 ?
                    price.subtract(tradePair.getFirstPrice())
                            .movePointRight(2).divide(tradePair.getFirstPrice(), 3, RoundingMode.DOWN)
                    : 0.0);

        }

        return assetJSON;
    }

    public JSONObject jsonForExplorerInfo(DCSet dcSet, JSONObject langObj, boolean forPrint) {

        JSONObject itemJson = super.jsonForExplorerInfo(dcSet, langObj, forPrint);
        itemJson.put("Label_Asset", Lang.T("Asset", langObj));
        itemJson.put("Label_Scale", Lang.T("Accuracy", langObj));
        itemJson.put("Label_AssetType", Lang.T("Type # вид", langObj));
        itemJson.put("Label_AssetType_Desc", Lang.T("Type Description", langObj));
        itemJson.put("Label_Quantity", Lang.T("Quantity", langObj));
        itemJson.put("Label_Released", Lang.T("Released", langObj));

        itemJson.put("Label_ImMovable", Lang.T("ImMovable", langObj));
        itemJson.put("Label_Unlimited", Lang.T("Unlimited", langObj));
        itemJson.put("Label_Accounting", Lang.T("Accounting", langObj));
        itemJson.put("Label_Unique", Lang.T("Unique", langObj));
        itemJson.put("Label_UnHoldable", Lang.T("Un holdable", langObj));
        itemJson.put("Label_OutsideType", Lang.T("Outside Type", langObj));
        itemJson.put("Label_SelfManaged", Lang.T("Self Managed", langObj));
        itemJson.put("Label_ChangeDebtBySendActions", Lang.T("isChangeDebtBySendActions", langObj));
        itemJson.put("Label_ChangeDebtBySpendActions", Lang.T("isChangeDebtBySpendActions", langObj));
        itemJson.put("Label_DirectBalances", Lang.T("isDirectBalances", langObj));
        itemJson.put("Label_isNotReDebted", Lang.T("isNotReDebted", langObj));
        itemJson.put("Label_isOutsideOtherClaim", Lang.T("isOutsideOtherClaim", langObj));
        itemJson.put("Label_isReverseSend", Lang.T("isReverseSend", langObj));
        itemJson.put("Label_Properties", Lang.T("Properties", langObj));

        itemJson.put("assetTypeNameFull", charAssetType() + viewAssetTypeAbbrev() + ":" + Lang.T(viewAssetTypeFull(), langObj));
        itemJson.put("released", getReleased());

        if (!forPrint) {
            itemJson.put("Label_Holders", Lang.T("Holders", langObj));
            itemJson.put("Label_Available_pairs", Lang.T("Available pairs", langObj));
            itemJson.put("Label_Pair", Lang.T("Pair", langObj));
            itemJson.put("Label_Orders_Count", Lang.T("Orders Count", langObj));
            itemJson.put("Label_Open_Orders_Volume", Lang.T("Open Orders Volume", langObj));
            itemJson.put("Label_Trades_Count", Lang.T("Trades Count", langObj));
            itemJson.put("Label_Trades_Volume", Lang.T("Trades Volume", langObj));

            itemJson.put("orders", getOperations(DCSet.getInstance()));
        }

        itemJson.put("quantity", NumberAsString.formatAsString(getQuantity()));
        itemJson.put("released", NumberAsString.formatAsString(getReleased(dcSet)));

        itemJson.put("scale", getScale());

        itemJson.put("assetType", Lang.T(viewAssetType(), langObj));
        itemJson.put("assetTypeChar", charAssetType() + viewAssetTypeAbbrev());

        itemJson.put("assetTypeFull", Lang.T(viewAssetTypeFull(), langObj));
        StringJoiner joiner = new StringJoiner(", ");
        for (Fun.Tuple2<?, String> item : viewAssetTypeActionsList(null, true)) {
            joiner.add(Lang.T(item.b, langObj));
        }

        String desc = Lang.T(viewAssetTypeDescriptionCls(getAssetType()), langObj)
                + ".<br><b>" + Lang.T("Acceptable actions", langObj) + "</b>: " + joiner.toString();
        String dexDesc = AssetCls.viewAssetTypeDescriptionDEX(assetType, START_KEY());
        if (dexDesc != null) {
            desc += "<br><b>" + Lang.T("DEX rules and taxes", langObj) + ":</b><br>" + Lang.T(dexDesc, langObj);
        }

        itemJson.put("assetTypeDesc", desc);


        itemJson.put("properties", viewProperties(langObj));

        return itemJson;
    }

    public String makeHTMLHeadView() {

        String text = super.makeHTMLHeadView();
        text += Lang.T("Asset Class") + ":&nbsp;"
                + Lang.T(getItemSubType() + "") + "<br>"
                + Lang.T("Asset Type") + ":&nbsp;"
                + "<b>" + charAssetType() + viewAssetTypeAbbrev() + "</b>:" + Lang.T(viewAssetTypeFull() + "") + "<br>"
                + Lang.T("Quantity") + ":&nbsp;" + getQuantity() + ", "
                + Lang.T("Scale") + ":&nbsp;" + getScale() + "<br>"
                + Lang.T("Description") + ":<br>";
        if (getKey() > 0 && getKey() < START_KEY()) {
            text += Library.to_HTML(Lang.T(viewDescription())) + "<br>";
        } else {
            text += Library.to_HTML(viewDescription()) + "<br>";
        }

        return text;

    }

    public int getDataLength(boolean includeReference) {
        return super.getDataLength(includeReference) + ASSET_TYPE_LENGTH;
    }

    static BigDecimal taxCoefficient = new BigDecimal("0.1");
    static BigDecimal referralsCoefficient = new BigDecimal("0.02");

    public static void processTrade(DCSet dcSet, Block block, Account receiver,
                                    boolean isInitiator, AssetCls assetHave, AssetCls assetWant,
                                    boolean asOrphan, BigDecimal tradeAmountForWant, long timestamp, Long orderID) {
        //TRANSFER FUNDS
        BigDecimal tradeAmount = tradeAmountForWant.setScale(assetWant.getScale());
        BigDecimal assetMakerRoyalty;
        BigDecimal inviterRoyalty;
        BigDecimal forgerFee;
        int scale = assetWant.getScale();
        Long assetWantKey = assetWant.getKey();

        PublicKeyAccount haveAssetMaker = assetHave.getMaker();
        PublicKeyAccount inviter;
        if (assetHave.getAssetType() == AS_NON_FUNGIBLE
                && !receiver.equals(haveAssetMaker)) {
            // значит приход + это тот актив который мы можем поделить
            // и это не сам автор продает
            assetMakerRoyalty = tradeAmount.movePointLeft(1).setScale(scale, RoundingMode.DOWN);
            forgerFee = tradeAmount.movePointLeft(3).setScale(scale, RoundingMode.DOWN);

            Fun.Tuple4<Long, Integer, Integer, Integer> issuerPersonDuration = haveAssetMaker.getPersonDuration(dcSet);
            if (issuerPersonDuration != null) {
                inviter = PersonCls.getIssuer(dcSet, issuerPersonDuration.a);
                if (inviter == null)
                    inviterRoyalty = BigDecimal.ZERO;
                else
                    inviterRoyalty = assetMakerRoyalty.movePointLeft(1).setScale(scale, RoundingMode.DOWN);
            } else {
                inviter = null;
                inviterRoyalty = BigDecimal.ZERO;
            }

        } else if (assetWant.getKey() < 100 && !isInitiator) {
            // это системные активы - берем комиссию за них
            assetMakerRoyalty = BigDecimal.ZERO;
            forgerFee = tradeAmount.movePointLeft(3).setScale(scale, RoundingMode.DOWN);

            // за рефералку тут тоже
            Fun.Tuple4<Long, Integer, Integer, Integer> issuerPersonDuration = receiver.getPersonDuration(dcSet);
            if (issuerPersonDuration != null) {
                inviter = PersonCls.getIssuer(dcSet, issuerPersonDuration.a);
                if (inviter == null)
                    inviterRoyalty = BigDecimal.ZERO;
                else
                    inviterRoyalty = forgerFee;
            } else {
                inviter = null;
                inviterRoyalty = BigDecimal.ZERO;
            }

        } else {
            assetMakerRoyalty = BigDecimal.ZERO;
            inviterRoyalty = BigDecimal.ZERO;
            inviter = null;
            forgerFee = BigDecimal.ZERO;
        }

        if (assetMakerRoyalty.signum() > 0) {
            tradeAmount = tradeAmount.subtract(assetMakerRoyalty);

            haveAssetMaker.changeBalance(dcSet, asOrphan, false, assetWantKey,
                    assetMakerRoyalty, false, false, false);
            if (!asOrphan && block != null)
                block.addCalculated(haveAssetMaker, assetWantKey, assetMakerRoyalty,
                        "NFT Royalty by Order @" + Transaction.viewDBRef(orderID), orderID);
        }

        if (inviterRoyalty.signum() > 0) {
            tradeAmount = tradeAmount.subtract(inviterRoyalty);

            long inviterRoyaltyLong = inviterRoyalty.setScale(assetWant.getScale()).unscaledValue().longValue();
            Transaction.process_gifts(dcSet, BlockChain.FEE_INVITED_DEEP, inviterRoyaltyLong, inviter, asOrphan,
                    assetWant, block,
                    "NFT Royalty referral bonus " + "@" + Transaction.viewDBRef(orderID),
                    orderID, timestamp);
        }

        if (forgerFee.signum() > 0) {
            tradeAmount = tradeAmount.subtract(forgerFee);

            if (block != null) {
                block.addAssetFee(assetWant, forgerFee, null);
            }
        }

        receiver.changeBalance(dcSet, asOrphan, false, assetWantKey,
                tradeAmount, false, false, false);
        if (!asOrphan && block != null)
            block.addCalculated(receiver, assetWantKey, tradeAmount,
                    "Trade Order @" + Transaction.viewDBRef(orderID), orderID);

    }

}
