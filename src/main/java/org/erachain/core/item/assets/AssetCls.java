package org.erachain.core.item.assets;


import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IssueItemMap;
import org.erachain.datachain.ItemMap;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;


// 1019 - Movable = true; Divisible = NO; Quantity = 1
public abstract class AssetCls extends ItemCls {

    public static final int TYPE_KEY = ItemCls.ASSET_TYPE;

    public static final long MIN_START_KEY = 1000L;

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
    protected static final int SCALE_LENGTH = 1;
    protected static final int ASSET_TYPE_LENGTH = 1;

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
     * === полный аналог AS_INSIDE_ASSETS по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_BANK_GUARANTEE = 60;
    /**
     * bank guarantee total - банковская гарантия общая сумма
     * === полный аналог AS_INSIDE_ASSETS по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_BANK_GUARANTEE_TOTAL = 61;


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
    public static final int AS_SELF_MANAGED = 124;

    /**
     * accounting loan
     * +++ мой займ другому лицу - учетный, бухгалтерский учет
     * === подобно AS_SELF_MANAGED
     */
    public static final int AS_ACCOUNTING_LOAN = 125;

    // + or -
    protected int scale;
    //
    protected int assetType;

    protected AssetCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int assetType, int scale) {
        super(typeBytes, owner, name, icon, image, description);
        this.assetType = assetType;
        this.scale = (byte) scale;

    }

    public AssetCls(int type, byte pars, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int assetType, int scale) {
        this(new byte[TYPE_LENGTH], owner, name, icon, image, description, assetType, scale);
        this.typeBytes[0] = (byte) type;
        this.typeBytes[1] = pars;
    }

    //GETTERS/SETTERS

    @Override
    public int getItemType() {
        return TYPE_KEY;
    }

    @Override
    public long getStartKey() {

        if (!BlockChain.CLONE_MODE)
            return MIN_START_KEY;

        long startKey = BlockChain.startKeys[TYPE_KEY];

        if (startKey == 0) {
            return START_KEY;
        } else if (startKey < MIN_START_KEY) {
            return (BlockChain.startKeys[TYPE_KEY] = MIN_START_KEY);
        }
        return startKey;
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

    public abstract long getQuantity();

    public abstract BigDecimal getReleased();
    public abstract BigDecimal getReleased(DCSet dc);


    public int getScale() {
        // TODO убрать это если будет новая цепочка с регулируемой точностью
        if (BlockChain.MAIN_MODE && this.key > 0 && this.key < 5 ||
                this.key > 1000 &&
                        this.key < BlockChain.AMOUNT_SCALE_FROM
        ) {
            //return this.assetType == 1? BlockChain.AMOUNT_DEDAULT_SCALE : 0;
            // IN ANY CASE
            return BlockChain.AMOUNT_DEDAULT_SCALE;
        }

        return this.scale;
    }

    public int getAssetType() {
        return this.assetType;
    }

    // https://unicode-table.com/ru/#23FC
    public String charAssetType() {

        if (this.key < 100) {
            return "";
        }

        switch (this.assetType) {
            case AS_OUTSIDE_GOODS:
                return "▲";
            case AS_OUTSIDE_IMMOVABLE:
                return "▼";
            case AS_ACCOUNTING:
                if (this.key == 555l || this.key == 666l || this.key == 777l)
                    return this.name;

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
            case AS_SELF_MANAGED:
            case AS_ACCOUNTING_LOAN:
                return "±";
            case AS_MY_DEBT:
                return "◆";
            case AS_OUTSIDE_WORK_TIME_HOURS:
                // 🕐🕜🕑🕝🕒🕞🕓🕟🕔🕠🕕🕡🕖🕢🕗🕣🕘🕤🕙🕥🕚🕦🕛🕧
                return "◕";
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return "◔";


        }

        if (this.assetType >= AS_OUTSIDE_CURRENCY
                && this.assetType <= AS_OUTSIDE_OTHER_CLAIM)
            return "◄";

        if (this.assetType == AS_INSIDE_ASSETS
                || this.assetType >= AS_INSIDE_CURRENCY
                && this.assetType <= AS_INSIDE_OTHER_CLAIM)
            return "►";

        // ● ⚫ ◆ █ ▇ ■ ◢ ◤ ◔ ◑ ◕ ⬛ ⬜ ⬤ ⛃
        return "⚫";

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
        }

        return charAssetType() + this.name;

    }

    public PublicKeyAccount getOwner() {
        if (this.key > 10 && this.key < 100 && BlockChain.ASSET_OWNERS.containsKey(this.key)) {
            return BlockChain.ASSET_OWNERS.get(this.key);
        }

        return this.owner;
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

    public boolean isOutsideType() {
        return // ?? this.assetType == AS_OUTSIDE_GOODS ||
                this.assetType >= AS_OUTSIDE_CURRENCY
                        && this.assetType <= AS_OUTSIDE_OTHER_CLAIM;
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

    /**
     * Управлять может только сам обладатель
     *
     * @return
     */
    public boolean isSelfManaged() {
        return assetType == AS_SELF_MANAGED || assetType == AS_ACCOUNTING_LOAN;
    }

    /**
     * Активы у которых есть только 4-ре баланса и каждый из них имеет возможность забрать - backward
     *
     * @return
     */
    public boolean isDirectBalances() {
        return assetType == AS_SELF_MANAGED || assetType == AS_ACCOUNTING_LOAN;
    }

    public boolean isAccounting() {
        return this.assetType == AS_ACCOUNTING
                || assetType == AS_SELF_MANAGED
                || assetType == AS_ACCOUNTING_LOAN;
    }

    /**
     * Без ограничений - только если это счетная единица или сам владелец без огрничений
     *
     * @param address
     * @param notAccounting
     * @return
     */
    public boolean isUnlimited(Account address, boolean notAccounting) {
        return !notAccounting && isAccounting() || getQuantity() == 0L && owner.equals(address);
    }

    public BigDecimal defaultAmountAssetType() {
        switch (assetType) {
            case AS_BANK_GUARANTEE:
                return BigDecimal.ONE;
        }
        return null;
    }

    public PublicKeyAccount defaultRecipient(int actionType, boolean backward) {

        if (isOutsideType()) {
            if (actionType == TransactionAmount.ACTION_SPEND
                    || actionType == TransactionAmount.ACTION_DEBT) {
                return getOwner();
            }
        }

        return null;
    }

    public static String viewAssetTypeCls(int asset_type) {
        switch (asset_type) {
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
                return "Promissory Note";
            case AS_OUTSIDE_BILL_EX:
                return "Bill of exchange";
            case AS_MY_DEBT:
                return "My Debt";
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
                return "Bank Guarantee Total";
            case AS_INDEX:
                return "Index";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other Digital Claim";

            case AS_ACCOUNTING:
                return "Accounting";
            case AS_SELF_MANAGED:
                return "Self Managed";
            case AS_ACCOUNTING_LOAN:
                return "Accounting Loan";
        }
        return "unknown";
    }

    public String viewAssetType() {
        return viewAssetTypeCls(this.assetType);
    }

    public static String viewAssetTypeFullCls(int asset_type) {
        switch (asset_type) {
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
                return "Promissory Note";
            case AS_OUTSIDE_BILL_EX:
                return "Bill of Exchange";
            case AS_MY_DEBT:
                return "My Debt to Loaner";
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
                return "Bank Guarantee Total";
            case AS_INDEX:
                return "Digital Index";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other Digital Right of Claim";

            case AS_ACCOUNTING:
                return "Accounting";
            case AS_SELF_MANAGED:
                return "Self Managed for Accounting";
            case AS_ACCOUNTING_LOAN:
                return "Accounting Loan for Debtor";
        }
        return "unknown";
    }

    public String viewAssetTypeFull() {
        return viewAssetTypeFullCls(this.assetType);
    }

    public static String viewAssetTypeDescriptionCls(int asset_type) {
        Lang lang = Lang.getInstance();
        switch (asset_type) {
            case AS_OUTSIDE_GOODS:
                return lang.translate("Movable things and goods. These goods can be taken for storage by the storekeeper or for confirmation of delivery. In this case you can see the balances on the accounts of storekeepers and delivery agents");
            case AS_OUTSIDE_IMMOVABLE:
                return lang.translate("Real estate and other goods and things not subject to delivery. Such things can be taken and given for rent and handed over to the guard");
            case AS_OUTSIDE_CURRENCY:
                return lang.translate("External money that must be transferred to an external bank account or transferred in cash. The amount on your account shows the right to demand the issuer to transfer such amount of money to your bank account. In order to satisfy the demand it is necessary to set it up for the payment, and after the money has arrived into your account confirm the repayment of this demand. You can also save them for storage, for example, the total amount collected for the ICO to be distributed to the hands of different holders - they must confirm receipt of these mid-transaction \"confirm acceptance in hand\"");
            case AS_OUTSIDE_WORK_TIME_HOURS:
                return lang.translate("Рабочее время в часах. Учет ведется как ваш долг перед кем-то потратить на него свое рабочее время. Рабочие часы можно передать тому кому вы должны свою работу, можно потребовать исполнить работу и можно подтвердить что работа была сделана, выразив эти действия в часах рабочего времени");
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return lang.translate("Рабочее время в минутах. Учет ведется как ваш долг перед кем-то потратить на него свое рабочее время. Рабочие минуты можно передать тому кому вы должны свою работу, можно потребовать исполнить работу и можно подтвердить что работа была сделана, выразив эти действия в минутах рабочего времени");
            case AS_OUTSIDE_SERVICE:
                return lang.translate("An external service that needs to be provided outside. To notify your wish to provide services you must make demands and then confirm the fulfillment");
            case AS_OUTSIDE_SHARE:
                return lang.translate("External shares which have to be transferred to an external depository. The depositary can be notified by presenting the claim and then confirm the shares transfer");
            case AS_OUTSIDE_BILL:
                return lang.translate("A digital promissory note can be called for redemption by external money. You can take it into your hands");
            case AS_OUTSIDE_BILL_EX:
                return lang.translate("A digital bill of exchange can be called for redemption by external money. You can take it into your hands");
            case AS_SELF_MANAGED:
                return lang.translate("AS_SELF_MANAGED-D");
            case AS_ACCOUNTING_LOAN:
                return lang.translate("AS_ACCOUNTING_LOAN-D");
            case AS_MY_DEBT:
                return lang.translate("AS_MY_DEBT-D");
            case AS_OUTSIDE_OTHER_CLAIM:
                return lang.translate("Other external rights, requirements and obligations. Any obligation (as well as other external assets), which can be claimed by the record \"summon\" and discharged by the record \"confirmation of fulfillment\" of this obligation. You can take it into your hands");
            case AS_INSIDE_ASSETS:
                return lang.translate("Internal (digital) asset. It does not require any external additional actions when transferring between accounts inside Erachain");
            case AS_INSIDE_CURRENCY:
                return lang.translate("Digital money");
            case AS_INSIDE_UTILITY:
                return lang.translate("Digital service or a cost is something that can be used inside Erachain nvironment, for example as a payment for external services");
            case AS_INSIDE_SHARE:
                return lang.translate("Digital share. The share of ownership of an external or internal enterpris, the possession of which establishes the right to own the corresponding share of the enterprise without the need to take any external actions");
            case AS_INSIDE_BONUS:
                return lang.translate("Digital loyalty points, bonuses, awards, discount points (bonus). It has no generally accepted value and can not be exchanged for other types of assets inside the Erachain environment. The exchange for other bonuses and rewards are allowed");
            case AS_INSIDE_ACCESS:
                return lang.translate("Digital rights of access and control, membership, pass");
            case AS_INSIDE_VOTE:
                return lang.translate("A digital voice for voting");
            case AS_BANK_GUARANTEE:
                return lang.translate("A digital bank guarantee.");
            case AS_BANK_GUARANTEE_TOTAL:
                return lang.translate("A digital bank guarantee total accounting.");
            case AS_INDEX:
                return lang.translate("Index on foreign and domestic assets, for example currencies on FOREX");
            case AS_INSIDE_OTHER_CLAIM:
                return lang.translate("Other digital rights, requirements and obligations. These assets (as well as other digital assets) can be given in debt and seized by the lender.");
            case AS_ACCOUNTING:
                return lang.translate("Accounting units #DESC");
        }
        return "";
    }

    public String viewAssetTypeAction(boolean backward, int actionType, boolean isCreatorOwner) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Transfer to the ownership ";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Confiscate from rent" : "Transfer to rent";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Return from rent";
                    case TransactionAmount.ACTION_HOLD:
                        return "The employment security/received from security";
                }
                break;
            case AS_OUTSIDE_CURRENCY:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Transfer to the ownership of the monetary claim"; // Передать в собственность денежное требование
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Withdraw a request to fulfill a monetary claim" // Отозвать требование об исполнении денежного требования
                                : "Demand execution of a monetary claim"; // Потребовать исполнения денежного требования
                    case TransactionAmount.ACTION_SPEND:
                        return "Confirm the execution of the monetary claim"; // Подтвердить исполнение денежного требования
                    default:
                        return null;
                }
            case AS_OUTSIDE_WORK_TIME_HOURS:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Transfer to the ownership of person-hour"; // Передать в собственность рабочие часы
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Decline the demand for person-hour" // Отозвать требование траты рабочих часов
                                : "Demand to spend person-hour"; // Потребовать потратить рабочие часы
                    case TransactionAmount.ACTION_SPEND:
                        return "Confirm the spend of person-hour"; // Подтвердить затраты рабочих часов
                    default:
                        return null;
                }
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Transfer to the ownership of person-minutes"; // Передать в собственность рабочие минуты
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Decline the demand for person-minutes" // Отозвать требование траты рабочих минут
                                : "Demand to spend person-minutes"; // Потребовать потратить рабочие минуты
                    case TransactionAmount.ACTION_SPEND:
                        return "Confirm the spend of person-minutes"; // Подтвердить затраты рабочих минут
                    default:
                        return null;
                }
            case AS_OUTSIDE_SERVICE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Transfer Service Requirement";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Отозвать требование в предоставлении услуг"
                                : "To require the provision of services";
                    case TransactionAmount.ACTION_SPEND:
                        return "Confirm the provision of services";
                    default:
                        return null;
                }
            case AS_OUTSIDE_SHARE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "To transfer shares in the property";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "To reduce the transfer of shares"
                                : "To require the transfer of shares";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Return debt";
                    case TransactionAmount.ACTION_SPEND:
                        return "Confirm receipt of shares";
                    default:
                        return null;
                }
            case AS_OUTSIDE_BILL:
            case AS_OUTSIDE_BILL_EX:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Передать в собственность вексель";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Отозвать требование погашения векселя"
                                : "Потребовать погашения векселя";
                    case TransactionAmount.ACTION_SPEND:
                        return "Подтвердить погашение векселя";
                    default:
                        return null;
                }
            case AS_SELF_MANAGED:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return backward ? "Списать (сторно)" : "Начислить";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Отозвать требование исполнения"
                                : "Потребовать исполнения";
                    case TransactionAmount.ACTION_HOLD:
                        return !backward ? "Списать хранение (сторно)"
                                : "Учесть хранение";
                    case TransactionAmount.ACTION_SPEND:
                        return backward ? "Отменить исполнение (сторно)" : "Подтвердить исполнение";
                    default:
                        return null;
                }
            case AS_ACCOUNTING_LOAN:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return backward ? "Списать долг (сторно)" : "Начислить долг";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Отозвать требование погашения долга"
                                : "Потребовать исполнения долга";
                    case TransactionAmount.ACTION_SPEND:
                        return backward ? "Отменить погашение долга (сторно)" : "Подтвердить погашение долга";
                    default:
                        return null;
                }
            case AS_MY_DEBT:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return isCreatorOwner ? "Подтвердить свой долг" : "Переуступить займ";
                    case TransactionAmount.ACTION_DEBT:
                        return isCreatorOwner ? null // эмитент долга не может делать требования
                                : backward ? "Отозвать требование погашения займа"
                                : "Потребовать исполнения займа";
                    case TransactionAmount.ACTION_SPEND:
                        return isCreatorOwner ? null // эмитент долга не может делать погашения
                                : backward ? "Отменить погашения займа (сторно)" : "Подтвердить погашения займа";
                    default:
                        return null;
                }
            case AS_OUTSIDE_OTHER_CLAIM:
            case AS_INSIDE_OTHER_CLAIM:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Передать в собственность требование";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Отозвать требование исполнения права"
                                : "Потребовать исполнения своего права";
                    case TransactionAmount.ACTION_SPEND:
                        return "Подтвердить исполнение своего права";
                    default:
                        return null;
                }
            case AS_INSIDE_CURRENCY:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Певести в собственность деньги";
                    case TransactionAmount.ACTION_HOLD:
                        return "Учесть прием денег на баланс";
                    case TransactionAmount.ACTION_SPEND:
                    case TransactionAmount.ACTION_PLEDGE:
                        return null;
                }
                break;
            case AS_INSIDE_UTILITY:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Передать в собственность услугу";
                    case TransactionAmount.ACTION_HOLD:
                        return "Учесть получение услуги";
                    case TransactionAmount.ACTION_SPEND:
                    case TransactionAmount.ACTION_PLEDGE:
                        return null;
                }
                break;
            case AS_INSIDE_SHARE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Передать в собственность акции";
                    case TransactionAmount.ACTION_HOLD:
                        return "Take the reception into balance";
                    case TransactionAmount.ACTION_SPEND:
                    case TransactionAmount.ACTION_PLEDGE:
                        return null;
                }
                break;
            case AS_INSIDE_BONUS:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Transfer bonuses";
                    case TransactionAmount.ACTION_HOLD:
                        return "Take the reception into balance";
                    case TransactionAmount.ACTION_SPEND:
                    case TransactionAmount.ACTION_PLEDGE:
                        return null;
                }
                break;
            case AS_INSIDE_ACCESS:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Grant rights";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "To confiscate a delegated rights"
                                : "Delegate rights";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Return delegate rights";
                    case TransactionAmount.ACTION_HOLD:
                        return "Take the reception into balance";
                    case TransactionAmount.ACTION_SPEND:
                    case TransactionAmount.ACTION_PLEDGE:
                        return null;
                }
                break;
            case AS_INSIDE_VOTE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Grant voice";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "To confiscate a delegated vote"
                                : "Delegate voice";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Return delegate vote";
                    case TransactionAmount.ACTION_HOLD:
                        return "Take the reception into balance";
                    case TransactionAmount.ACTION_SPEND:
                    case TransactionAmount.ACTION_PLEDGE:
                        return null;
                }
                break;
            case AS_BANK_GUARANTEE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Передать банковскую гарантию";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Отозвать банковскую гарантию" : "Выдать банковскую гарантию";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Вернуть банковскую гарантию";
                    case TransactionAmount.ACTION_HOLD:
                        return backward ? "Акцептовать банковскую гарантию" : null;
                    case TransactionAmount.ACTION_SPEND:
                        return null;
                }
                break;
            case AS_BANK_GUARANTEE_TOTAL:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Передать учетную банковскую гарантию";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Отозвать учетную банковскую гарантию" : "Выдать учетную банковскую гарантию";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Вернуть учетную банковскую гарантию";
                    case TransactionAmount.ACTION_HOLD:
                        return backward ? "Hold" : null;
                }
                break;
            case AS_INDEX:
            case AS_ACCOUNTING:
                break;
        }

        switch (actionType) {
            case TransactionAmount.ACTION_SEND:
                return "Transfer to the ownership";
            case TransactionAmount.ACTION_DEBT:
                return backward ? "To confiscate a debt"
                        : "Transfer to debt";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return "Return debt";
            case TransactionAmount.ACTION_HOLD:
                return "Confirm acceptance \"in hand\"";
            case TransactionAmount.ACTION_SPEND:
                return backward ? "Produce"
                        : "Spend";
            case TransactionAmount.ACTION_PLEDGE:
                return backward ? "Re-pledge"
                        : "Pledge";
        }

        return null;
    }

    public String viewAssetTypeActionTitle(boolean backward, int actionType, boolean isCreatorOwner) {
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
                break;
            case AS_BANK_GUARANTEE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Передача банковской гарантии - %asset%";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Отзыв банковской гарантии - %asset%" : "Выдача банковской гарантии - %asset%";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Возврат банковской гарантии - %asset%";
                    case TransactionAmount.ACTION_HOLD:
                        return backward ? "Акцептование банковской гарантии - %asset%" : null;
                    case TransactionAmount.ACTION_SPEND:
                        return "Погашение банковской гарантии - %asset%";
                }
            case AS_BANK_GUARANTEE_TOTAL:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Передача учетной банковской гарантии - %asset%";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Отзыв учетной банковской гарантии - %asset%" : "Выдача учетной банковской гарантии - %asset%";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Возврат учетной банковской гарантии - %asset%";
                    case TransactionAmount.ACTION_HOLD:
                        return backward ? "Акцептование учетной банковской гарантии - %asset%" : null;
                    case TransactionAmount.ACTION_SPEND:
                        return "Погашение учетной банковской гарантии - %asset%";
                }
            case AS_INDEX:
            case AS_INSIDE_OTHER_CLAIM:
            case AS_ACCOUNTING:
        }

        return viewAssetTypeAction(backward, actionType, isCreatorOwner) + " - %asset%";
    }

    public String viewAssetTypeCreator(boolean backward, int actionType, boolean isCreatorOwner) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
            case AS_OUTSIDE_CURRENCY:
            case AS_OUTSIDE_SERVICE:
            case AS_OUTSIDE_SHARE:
            case AS_OUTSIDE_BILL:
            case AS_OUTSIDE_BILL_EX:
            case AS_SELF_MANAGED:
                return "Me";
            case AS_ACCOUNTING_LOAN:
                return "Lender";
            case AS_MY_DEBT:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return isCreatorOwner ? "Debtor" : "Lender";
                    case TransactionAmount.ACTION_DEBT:
                    case TransactionAmount.ACTION_SPEND:
                        return isCreatorOwner ? null // эмитент долга не может делать требования
                                : "Debtor";
                    default:
                        return null;
                }
            case AS_OUTSIDE_OTHER_CLAIM:
            case AS_INSIDE_ASSETS:
            case AS_INSIDE_CURRENCY:
            case AS_INSIDE_UTILITY:
            case AS_INSIDE_SHARE:
            case AS_INSIDE_BONUS:
            case AS_INSIDE_ACCESS:
            case AS_INSIDE_VOTE:
                break;
            case AS_BANK_GUARANTEE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                    case TransactionAmount.ACTION_DEBT:
                    case TransactionAmount.ACTION_SPEND:
                        return "Guarantee";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Beneficiary";
                    case TransactionAmount.ACTION_HOLD:
                        return backward ? "Beneficiary" : null;
                }
            case AS_BANK_GUARANTEE_TOTAL:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                    case TransactionAmount.ACTION_DEBT:
                        return "Guarantee";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Beneficiary";
                    case TransactionAmount.ACTION_HOLD:
                        return backward ? "Beneficiary" : null;
                    case TransactionAmount.ACTION_SPEND:
                        return "Spender";
                }
            case AS_INDEX:
            case AS_INSIDE_OTHER_CLAIM:
            case AS_ACCOUNTING:
        }

        switch (actionType) {
            case TransactionAmount.ACTION_SEND:
                return "Giver";
            case TransactionAmount.ACTION_DEBT:
                return "Creditor";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return "Debtor";
            case TransactionAmount.ACTION_HOLD:
                return "Taker";
            case TransactionAmount.ACTION_SPEND:
                return "Spender";
        }

        return "unknown";
    }

    public String viewAssetTypeTarget(boolean backward, int actionType, boolean isRecipientOwner) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
            case AS_OUTSIDE_CURRENCY:
            case AS_OUTSIDE_SERVICE:
            case AS_OUTSIDE_SHARE:
            case AS_OUTSIDE_BILL:
            case AS_OUTSIDE_BILL_EX:
            case AS_SELF_MANAGED:
                return "They";
            case AS_ACCOUNTING_LOAN:
                return "Debtor";
            case AS_MY_DEBT:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return isRecipientOwner ? null : "Lender"; // Тут может быть начальная эмиссия к Кредитору и переуступка - тоже кредитору по сути
                    case TransactionAmount.ACTION_DEBT:
                    case TransactionAmount.ACTION_SPEND:
                        return isRecipientOwner ?
                                "Debtor"
                                : null; // реципиент только эмитент долга;
                    default:
                        return null;
                }
            case AS_OUTSIDE_OTHER_CLAIM:
            case AS_INSIDE_ASSETS:
            case AS_INSIDE_CURRENCY:
            case AS_INSIDE_UTILITY:
            case AS_INSIDE_SHARE:
            case AS_INSIDE_BONUS:
            case AS_INSIDE_ACCESS:
            case AS_INSIDE_VOTE:
                break;
            case AS_BANK_GUARANTEE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Recipient";
                    case TransactionAmount.ACTION_DEBT:
                        return "Beneficiary";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Guarantee";
                    case TransactionAmount.ACTION_HOLD:
                        return backward ? "Guarantee" : null;
                    case TransactionAmount.ACTION_SPEND:
                        return "Spender";
                }
            case AS_BANK_GUARANTEE_TOTAL:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Recipient";
                    case TransactionAmount.ACTION_DEBT:
                        return "Principal";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Guarantee";
                    case TransactionAmount.ACTION_HOLD:
                        return backward ? "Guarantee" : null;
                    case TransactionAmount.ACTION_SPEND:
                        return "Spender";
                }
            case AS_INDEX:
            case AS_INSIDE_OTHER_CLAIM:
            case AS_ACCOUNTING:
        }

        switch (actionType) {
            case TransactionAmount.ACTION_SEND:
                return "Recipient";
            case TransactionAmount.ACTION_DEBT:
                return "Debtor";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return "Creditor";
            case TransactionAmount.ACTION_HOLD:
                return "Supplier";
            case TransactionAmount.ACTION_SPEND:
                return "Spender";
        }

        return "unknown";
    }

    public String viewAssetTypeActionOK(boolean backward, int actionType, boolean isCreatorOwner) {
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
            case AS_INDEX:
            case AS_INSIDE_OTHER_CLAIM:
            case AS_ACCOUNTING:
        }

        return viewAssetTypeAction(backward, actionType, isCreatorOwner) + " # to";

    }

    public long getOperations(DCSet dcSet) {
        long total = dcSet.getOrderMap().getCountOrders(key);
        return total;
    }

    //OTHER
    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject assetJSON = super.toJson();

        // ADD DATA
        assetJSON.put("scale", this.getScale());
        assetJSON.put("assetTypeKey", this.assetType);
        assetJSON.put("assetTypeName", viewAssetType());
        assetJSON.put("assetTypeNameFull", viewAssetTypeFull());
        assetJSON.put("assetTypeDesc", viewAssetTypeDescriptionCls(assetType));

        return assetJSON;
    }

    public JSONObject jsonForExplorerPage(JSONObject langObj) {
        //DCSet dcSet = DCSet.getInstance();

        JSONObject json =super.jsonForExplorerPage(langObj);
        json.put("assetTypeKey", this.assetType);
        json.put("assetTypeNameFull", viewAssetTypeFull());
        json.put("quantity", getQuantity());
        json.put("released", getReleased());
        json.put("scale", scale);
        json.put("orders", getOperations(DCSet.getInstance()));

        return json;
    }

}
