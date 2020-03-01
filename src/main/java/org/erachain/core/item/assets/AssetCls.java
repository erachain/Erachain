package org.erachain.core.item.assets;


import org.erachain.core.BlockChain;
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


// 1019 - Movable = true; Divisible = NO; Quantity = 1
public abstract class AssetCls extends ItemCls {

    // CORE KEY
    public static final long ERA_KEY = 1l;
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
    public static final long FEE_KEY = 2l;
    public static final String FEE_ABBREV = "CMP"; // COMPU (compute units)
    public static final String FEE_NAME = "COMPU";
    public static final String FEE_DESCR = "Основная учётная единица среды, используемая для оплаты комиссий за внесение записей в среду - \"рабочая\", \"оплатная\"" + ": "
            + FEE_NAME + "(" + FEE_ABBREV + "). ";

    // TRUST KEY
    public static final long TRUST_KEY = 3l;
    public static final String TRUST_ABBREV = "АЗЫ"; // COMPU (compute units)
    public static final String TRUST_NAME = "АЗЫ";
    public static final String TRUST_DESCR = "Честь, доблесть и доверие" + ": "
            + TRUST_NAME + "(" + TRUST_ABBREV + "). ";

    // REAL KEY
    public static final long REAL_KEY = 4l;
    public static final String REAL_ABBREV = "ВЕД"; // COMPU (compute units)
    public static final String REAL_NAME = "ВЕДЫ";
    public static final String REAL_DESCR = "Труд, знания, заслуги и польза" + ": "
            + REAL_NAME + "(" + REAL_ABBREV + "). ";

    // DEaL KEY
    public static final long LIA_KEY = 5l;
    public static final String LIA_ABBREV = "LIA"; //
    public static final String LIA_NAME = "LIA";
    public static final String LIA_DESCR = "Life ID Asset (" + LIA_NAME + ")";

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
        return ItemCls.ASSET_TYPE;
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

    public abstract Long getQuantity();

    public abstract BigDecimal getReleased();
    public abstract BigDecimal getReleased(DCSet dc);

	/*
	public boolean isDivisible() {
		if (this.key < BlockChain.AMOUNT_SCALE_FROM)
			return divisible;

		return this.scale > 0;
	}
	 */

    public int getScale() {
        if (this.key > 0 && this.key < 5 ||
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

        if (this.key < 5) {
            return "" + this.name; // ®
        }

        switch (this.assetType) {
            case AS_OUTSIDE_GOODS:
                return "▲" + this.name;
            case AS_OUTSIDE_IMMOVABLE:
                return "▼" + this.name;
            case AS_ACCOUNTING:
                if (this.key == 555l || this.key == 666l || this.key == 777l)
                    return this.name;

                return "±" + this.name;
        }

        if (this.assetType >= AS_OUTSIDE_CURRENCY
                && this.assetType <= AS_OUTSIDE_OTHER_CLAIM)
            return "◄" + this.name;

        if (this.assetType == AS_INSIDE_ASSETS
                || this.assetType >= AS_INSIDE_CURRENCY
                && this.assetType <= AS_INSIDE_OTHER_CLAIM)
            return "►" + this.name;

        return "?" + this.name;

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
        switch ((int) (long) key) {
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
            case 14:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/ETH.png"));
                } catch (Exception e) {
                }
                return icon;
            case 92:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/RUB.png"));
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
        if (key < 1000 && image.length > 0 )
            return new byte[0];

        return image;
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
        return this.assetType >= AS_OUTSIDE_CURRENCY
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

    public boolean isAccounting() {
        return this.assetType == AS_ACCOUNTING;
    }

    public BigDecimal defaultAmountAssetType() {
        switch (assetType) {
            case AS_BANK_GUARANTEE:
                return BigDecimal.ONE;
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
            case AS_OUTSIDE_SERVICE:
                return "Outside Service";
            case AS_OUTSIDE_SHARE:
                return "Outside Share";
            case AS_OUTSIDE_BILL:
                return "Promissory Note";
            case AS_OUTSIDE_BILL_EX:
                return "Bill of exchange";
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
            case AS_OUTSIDE_SERVICE:
                return "Outside Service";
            case AS_OUTSIDE_SHARE:
                return "Outside Share Rights";
            case AS_OUTSIDE_BILL:
                return "Promissory Note";
            case AS_OUTSIDE_BILL_EX:
                return "Bill of Exchange";
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Оther Outside Right of Claim";

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
            case AS_OUTSIDE_SERVICE:
                return lang.translate("An external service that needs to be provided outside. To notify your wish to provide services you must make demands and then confirm the fulfillment");
            case AS_OUTSIDE_SHARE:
                return lang.translate("External shares which have to be transferred to an external depository. The depositary can be notified by presenting the claim and then confirm the shares transfer");
            case AS_OUTSIDE_BILL:
                return lang.translate("A digital promissory note can be called for redemption by external money. You can take it into your hands");
            case AS_OUTSIDE_BILL_EX:
                return lang.translate("A digital bill of exchange can be called for redemption by external money. You can take it into your hands");
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
                return lang.translate("Accounting (bookkeeping) units. They do not have a generally accepted value and can only be exchanged among themselves. Actions with them are possible only for accounting purposes: transfer, take for storage (on balance), lend, take back the debt, etc.");
        }
        return "";
    }

    public String viewAssetTypeAction(boolean backward, int actionType) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Send";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Confiscate Debt" : "Debt";
                    case TransactionAmount.ACTION_HOLD:
                        return "Hold";
                    case TransactionAmount.ACTION_SPEND:
                        return "Spend";
                }
            case AS_OUTSIDE_CURRENCY:
                return "Outside Currency";
            case AS_OUTSIDE_SERVICE:
                return "Outside Service";
            case AS_OUTSIDE_SHARE:
                return "Outside Share Rights";
            case AS_OUTSIDE_BILL:
                return "Promissory Note";
            case AS_OUTSIDE_BILL_EX:
                return "Bill of Exchange";
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Оther Outside Right of Claim";

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
            case AS_BANK_GUARANTEE_TOTAL:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Передать учетную банковскую гарантию";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Выдать учетную банковскую гарантию" : "Забрать учетную банковскую гарантию";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Вернуть учетную банковскую гарантию";
                    case TransactionAmount.ACTION_HOLD:
                        return backward ? "Hold" : null;
                }
            case AS_INDEX:
                return "Digital Index";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other Digital Right of Claim";

            case AS_ACCOUNTING:
                return "Accounting";
        }

        switch (actionType) {
            case TransactionAmount.ACTION_SEND:
                return "Send";
            case TransactionAmount.ACTION_DEBT:
                return "Debt";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return "Repay Debt";
            case TransactionAmount.ACTION_HOLD:
                return "Hold";
            case TransactionAmount.ACTION_SPEND:
                return "Spend";
        }

        return null;
    }

    public String viewAssetTypeActionTitle(boolean backward, int actionType) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Send";
                    case TransactionAmount.ACTION_DEBT:
                        return "Debt";
                    case TransactionAmount.ACTION_HOLD:
                        return "Hold";
                    case TransactionAmount.ACTION_SPEND:
                        return "Spend";
                }
            case AS_OUTSIDE_CURRENCY:
                return "Outside Currency";
            case AS_OUTSIDE_SERVICE:
                return "Outside Service";
            case AS_OUTSIDE_SHARE:
                return "Outside Share Rights";
            case AS_OUTSIDE_BILL:
                return "Promissory Note";
            case AS_OUTSIDE_BILL_EX:
                return "Bill of Exchange";
            case AS_OUTSIDE_OTHER_CLAIM:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Send";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "If You want to give the borrowed asset %asset%, fill in this form"
                                : "Если Вы хотите подтвердить погашение требования %asset%, заполните эту форму";
                    case TransactionAmount.ACTION_HOLD:
                        return "Take on hold %asset%";
                    case TransactionAmount.ACTION_SPEND:
                        return "Spend";
                }

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
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Передача банковской гарантии - %asset%";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Отозыв банковской гарантии - %asset%" : "Выдача банковской гарантии - %asset%";
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
                        return backward ? "Отозыв учетной банковской гарантии - %asset%" : "Выдача учетной банковской гарантии - %asset%";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Возврат учетной банковской гарантии - %asset%";
                    case TransactionAmount.ACTION_HOLD:
                        return backward ? "Акцептование учетной банковской гарантии - %asset%" : null;
                    case TransactionAmount.ACTION_SPEND:
                        return "Погашение учетной банковской гарантии";
                }
            case AS_INDEX:
                return "Digital Index";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other Digital Right of Claim";

            case AS_ACCOUNTING:
                return "Accounting";
        }

        switch (actionType) {
            case TransactionAmount.ACTION_SEND:
                return "If You want to send asset %asset%, fill in this form";
            case TransactionAmount.ACTION_DEBT:
                return backward ? "If You want to give the borrowed asset %asset%, fill in this form"
                        : "If You want to confiscate in debt issued asset %asset%, fill in this form";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return "If You want to give the repay debt asset %asset%, fill in this form";
            case TransactionAmount.ACTION_HOLD:
                return "If You want to take on hold issued asset %asset%, fill in this form";
            case TransactionAmount.ACTION_SPEND:
                return "Spend";
        }

        return "unknown";
    }

    public String viewAssetTypeCreator(boolean backward, int actionType) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Send";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Confiscate Debt" : "Debt";
                    case TransactionAmount.ACTION_HOLD:
                        return "Hold";
                    case TransactionAmount.ACTION_SPEND:
                        return "Spend";
                }
            case AS_OUTSIDE_CURRENCY:
                return "Outside Currency";
            case AS_OUTSIDE_SERVICE:
                return "Outside Service";
            case AS_OUTSIDE_SHARE:
                return "Outside Share Rights";
            case AS_OUTSIDE_BILL:
                return "Promissory Note";
            case AS_OUTSIDE_BILL_EX:
                return "Bill of Exchange";
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Оther Outside Right of Claim";

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
                return "Digital Index";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other Digital Right of Claim";

            case AS_ACCOUNTING:
                return "Accounting";
        }

        switch (actionType) {
            case TransactionAmount.ACTION_SEND:
                return "Recipient";
            case TransactionAmount.ACTION_DEBT:
                return "Debitor";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return "Lender Account";
            case TransactionAmount.ACTION_HOLD:
                return "Giver";
            case TransactionAmount.ACTION_SPEND:
                return "Spender";
        }

        return "unknown";
    }

    public String viewAssetTypeTarget(boolean backward, int actionType) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Send";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Confiscate Debt" : "Debt";
                    case TransactionAmount.ACTION_HOLD:
                        return "Hold";
                    case TransactionAmount.ACTION_SPEND:
                        return "Spend";
                }
            case AS_OUTSIDE_CURRENCY:
                return "Outside Currency";
            case AS_OUTSIDE_SERVICE:
                return "Outside Service";
            case AS_OUTSIDE_SHARE:
                return "Outside Share Rights";
            case AS_OUTSIDE_BILL:
                return "Promissory Note";
            case AS_OUTSIDE_BILL_EX:
                return "Bill of Exchange";
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Оther Outside Right of Claim";

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
                return "Digital Index";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other Digital Right of Claim";

            case AS_ACCOUNTING:
                return "Accounting";
        }

        switch (actionType) {
            case TransactionAmount.ACTION_SEND:
                return "Recipient";
            case TransactionAmount.ACTION_DEBT:
                return "Vendor";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return "Lender";
            case TransactionAmount.ACTION_HOLD:
                return "Giver";
            case TransactionAmount.ACTION_SPEND:
                return "Spender";
        }

        return "unknown";
    }

    public String viewAssetTypeActionOK(boolean backward, int actionType) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Send";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Confiscate Debt" : "Debt";
                    case TransactionAmount.ACTION_HOLD:
                        return "Hold";
                    case TransactionAmount.ACTION_SPEND:
                        return "Spend";
                }
            case AS_OUTSIDE_CURRENCY:
                return "Outside Currency";
            case AS_OUTSIDE_SERVICE:
                return "Outside Service";
            case AS_OUTSIDE_SHARE:
                return "Outside Share Rights";
            case AS_OUTSIDE_BILL:
                return "Promissory Note";
            case AS_OUTSIDE_BILL_EX:
                return "Bill of Exchange";
            case AS_OUTSIDE_OTHER_CLAIM:
                switch (actionType) {
                    case TransactionAmount.ACTION_SEND:
                        return "Send";
                    case TransactionAmount.ACTION_DEBT:
                        return backward ? "Подтвердить погашение требования" : "Debt";
                    case TransactionAmount.ACTION_HOLD:
                        return "Hold";
                    case TransactionAmount.ACTION_SPEND:
                        return "Spend";
                }

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
            case AS_BANK_GUARANTEE_TOTAL:
                return viewAssetTypeAction(backward, actionType);
            case AS_INDEX:
                return "Digital Index";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other Digital Right of Claim";

            case AS_ACCOUNTING:
                return "Accounting";
        }

        switch (actionType) {
            case TransactionAmount.ACTION_SEND:
                return "Send Asset";
            case TransactionAmount.ACTION_DEBT:
                return backward ? "Confiscate Debt" : "Debt";
            case TransactionAmount.ACTION_HOLD:
                return "Hold Asset";
            case TransactionAmount.ACTION_SPEND:
                return "Spend Asset";
        }

        return "unknown";
    }

    public long getOperations(DCSet dcSet) {
        long total = dcSet.getOrderMap().getCountOrders(key);
        return total;
    }
    
	/*
	public void setMovable(boolean movable) {
		this.typeBytes[1] = (byte)(this.typeBytes[1] & (movable?1:0));
	}
	 */

	/*
	@Override
	public byte[] toBytes(boolean includeReference, boolean forOwnerSign)
	{

		byte[] data = super.toBytes(includeReference, forOwnerSign);

		//WRITE SCALE
		data = Bytes.concat(data, new byte[]{(byte)this.getScale()});

		//WRITE ASSET TYPE
		data = Bytes.concat(data, new byte[]{(byte)this.getAssetType()});

		return data;
	}

	@Override
	public int getDataLength(boolean includeReference)
	{
		return super.getDataLength(includeReference)
				+ SCALE_LENGTH + ASSET_TYPE_LENGTH;
	}
	 */

    //OTHER
    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject assetJSON = super.toJson();

        // ADD DATA
        assetJSON.put("scale", this.getScale());
        assetJSON.put("assetTypeKey", this.assetType);
        assetJSON.put("assetTypeName", viewAssetType());

        return assetJSON;
    }

    public JSONObject jsonForExplorerPage(JSONObject langObj) {
        //DCSet dcSet = DCSet.getInstance();

        JSONObject json =super.jsonForExplorerPage(langObj);
        json.put("assetTypeKey", this.assetType);
        json.put("assetTypeName", viewAssetType());
        json.put("assetTypeNameFull", viewAssetTypeFull());
        json.put("quantity", getQuantity());
        json.put("released", getReleased());
        json.put("scale", scale);
        json.put("orders", getOperations(DCSet.getInstance()));

        return json;
    }

}
