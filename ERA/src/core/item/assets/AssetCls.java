package core.item.assets;


import org.json.simple.JSONObject;

import core.BlockChain;
import core.account.PublicKeyAccount;
import core.item.ItemCls;
import datachain.DCSet;
import datachain.Issue_ItemMap;
import datachain.Item_Map;


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

	/*
	// DEaL KEY
	public static final long DEAL_KEY = 5l;
	public static final String DEAL_ABBREV = "DIL"; // COMPU (compute units)
	public static final String DEAL_NAME = "DEAL";
	public static final String DEAL_DESCR = "It is an drops of the deal (" + DEAL_NAME + ")";
	 */
    public static final int UNIQUE = 1;
    public static final int VENTURE = 2;
    public static final int NAME = 3;
    public static final int INITIAL_FAVORITES = 4;
    protected static final int SCALE_LENGTH = 1;
    protected static final int ASSET_TYPE_LENGTH = 1;

    ///////////////////////////////////////////////////
    /*
     * GOODS
     *  передача в собственность, взять на хранение
     *  0 : движимая вещь вовне - может быть доставлена и передана на хранение (товары)
     */
    public static final int AS_OUTSIDE_GOODS = 0; // movable

    /*
     * ASSETS
     *  передача имущества не требует действий во вне - все исполняется тут же. Их можно дать в долг и заьрать самостоятельно
     *  Требования не предъявляются.
     *  3 : цифровое имущество - не требует действий вовне и исполняется внутри платформы (токены, цифровые валюты, цифровые билеты, цифровые права и т.д.)
     *  
     */
    public static final int AS_INSIDE_ASSETS = 1;

    /*
     * IMMOVABLE
     *  передача в сосбтвенность, дать в аренду (по графику времени), взять на охрану
     *  1 : недвижимая вещь вовне - может быть передана в аренду (недвижимость)
     */
    
    public static final int AS_OUTSIDE_IMMOVABLE = 2;

    /*
     * outside CURRENCY
     * +++ деньги вовне - можно истребовать вернуть и подтвердить получение денег
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_CURRENCY = 11;
       
    /*
     * outside SERVICE
     * +++ услуги во вне
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_SERVICE = 12; // UTILITY

    /*
     * outside SHARE
     * +++ акция предприятия вовне
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_SHARE = 13;

    /*
     * outside CLAIMS
     * +++ требования и обязательства вовне - можно истребовать право и подтвердить его исполнение (ссуда, займ, услуга, право, требование, деньги, билеты и т.д.)
     *
     * учет обязательств прав и требований на услуги и действия во внешнем мире - в том числе займы, ссуды, кредиты, фьючерсы и т.д.
     * нельзя вернуть эмитенту - но можно потребовать исполнение прав и можно подтвердить исполнение (погасить требование)
     * это делается теми же трнзакциями что выдать и забрать долг у внутренних активов
     * И в момент погашения одновременно передается как имущество эмитенту
     */
    public static final int AS_OUTSIDE_OTHER_CLAIM = 49;

    ///////////////
    /*
     * inside CURRENCY
     * +++ деньги 
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_CURRENCY = 51;

    /*
     * inside CLAIMS
     * +++ требования и обязательства
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_UTILITY = 52; // SERVICE
    
    /*
     * inside CLAIMS
     * +++ требования и обязательства
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_SHARE = 53;
    
    /*
     * inside BONUS
     * +++ бонусы - для анонимов так же платежи возможны
     * === ASSET - без обмена на бирже и можно анонимам переводить
     */
    public static final int AS_INSIDE_BONUS = 54;
    
    /*
     * inside RIGHTS
     * +++ права и доступы
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_RIGHTS = 55;

    /*
     * inside CLAIMS
     * +++ требования и обязательства
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_OTHER_CLAIM = 119;

    /*
     * ACCOUNTING
     * учетные единицы - нельзя на бирже торговать - они ничего не стоят, можно делать любые действия от своего имени
     * 4 : учетные единицы - не имеет стоимости и не может быть продано (бухгалтерский учет)
     */
    public static final int AS_ACCOUNTING = 123;

    // + or -
    protected int scale;
    //protected boolean divisible;
    // 0 - goods, movable
    // 1 - currency, immovable
    // 2 - claim or right or obligation
    protected int asset_type;

    protected AssetCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int asset_type, int scale) {
        super(typeBytes, owner, name, icon, image, description);
        this.asset_type = asset_type;
        this.scale = (byte) scale;

    }

    public AssetCls(int type, byte pars, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int asset_type, int scale) {
        this(new byte[TYPE_LENGTH], owner, name, icon, image, description, asset_type, scale);
        this.typeBytes[0] = (byte) type;
        this.typeBytes[1] = pars;
    }

    //GETTERS/SETTERS
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

        if (this.asset_type == AS_OUTSIDE_OTHER_CLAIM)
            return 8;
        if (this.asset_type == AS_OUTSIDE_CURRENCY)
            return 6;
        if (this.asset_type == AS_INSIDE_CURRENCY)
            return 4;
        if (this.asset_type == AS_ACCOUNTING)
            return 3;

        return BlockChain.DEVELOP_USE ? 8 : 12;
    }

    @Override
    public String viewName() {

        if (this.key < 5) {
            return "" + this.name; // ®
        }

        switch (this.asset_type) {
            case AS_OUTSIDE_GOODS:
                return "▲" + this.name;
            case AS_OUTSIDE_IMMOVABLE:
                return "▼" + this.name;
            case AS_ACCOUNTING:
                if (this.key == 555l || this.key == 666l || this.key == 777l)
                    return this.name;

                return "±" + this.name;
        }
        
        if (this.asset_type >= AS_OUTSIDE_CURRENCY
                && this.asset_type <= AS_OUTSIDE_OTHER_CLAIM)
                return "◄" + this.name;
                
        if (this.asset_type == AS_INSIDE_ASSETS
                || this.asset_type >= AS_INSIDE_CURRENCY
                && this.asset_type <= AS_INSIDE_OTHER_CLAIM)
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
    public int getItemTypeInt() {
        return ItemCls.ASSET_TYPE;
    }

    @Override
    public String getItemTypeStr() {
        return "asset";
    }

    // DB
    @Override
    public Item_Map getDBMap(DCSet db) {
        return db.getItemAssetMap();
    }

    @Override
    public Issue_ItemMap getDBIssueMap(DCSet db) {
        return db.getIssueAssetMap();
    }

    public abstract Long getQuantity();

    public abstract Long getTotalQuantity(DCSet dc);

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
            //return this.asset_type == 1? BlockChain.AMOUNT_DEDAULT_SCALE : 0;
            // IN ANY CASE
            return BlockChain.AMOUNT_DEDAULT_SCALE;
        }

        return this.scale;
    }

    public int getAssetType() {
        return this.asset_type;
    }

    public boolean isMovable() {
        if (false && this.key < BlockChain.AMOUNT_SCALE_FROM) {
            return (this.typeBytes[1] & (byte) 1) > 0;
        }
        return this.asset_type == AS_OUTSIDE_GOODS;
    }

    public boolean isImMovable() {
        if (false && this.key < BlockChain.AMOUNT_SCALE_FROM) {
            return (this.typeBytes[1] & (byte) 1) <= 0;
        }
        return this.asset_type == AS_OUTSIDE_IMMOVABLE;
    }

    public boolean isInsideType() {
        return this.asset_type == AS_INSIDE_ASSETS
                || this.asset_type >= AS_INSIDE_CURRENCY
                    && this.asset_type <= AS_INSIDE_OTHER_CLAIM;
    }

    public boolean isInsideAsset() {return this.asset_type == AS_INSIDE_ASSETS;}
    public boolean isInsideCurrency() {return this.asset_type == AS_INSIDE_CURRENCY;}
    public boolean isInsideUtility() {return this.asset_type == AS_INSIDE_UTILITY;}
    public boolean isInsideShare() {return this.asset_type == AS_INSIDE_SHARE;}
    public boolean isInsideBonus() {return this.asset_type == AS_INSIDE_BONUS;}
    public boolean isInsideRights() {return this.asset_type == AS_INSIDE_RIGHTS;}
    public boolean isInsideOtherClaim() {return this.asset_type == AS_INSIDE_OTHER_CLAIM;}

    public boolean isOutsideType() {
        return this.asset_type >= AS_OUTSIDE_CURRENCY
                && this.asset_type <= AS_OUTSIDE_OTHER_CLAIM;
    }

    public boolean isOutsideCurrency() {return this.asset_type == AS_OUTSIDE_CURRENCY;}
    public boolean isOutsideService() {return this.asset_type == AS_OUTSIDE_SERVICE;}
    public boolean isOutsideShare() {return this.asset_type == AS_OUTSIDE_SHARE;}
    public boolean isOutsideOtherClaim() {return this.asset_type == AS_OUTSIDE_OTHER_CLAIM;}

    public boolean isAccounting() {
        return this.asset_type == AS_ACCOUNTING;
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
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Outside Other Claim";

            case AS_INSIDE_ASSETS:
                return "Inside Asset";
            case AS_INSIDE_CURRENCY:
                return "Inside Currency";
            case AS_INSIDE_UTILITY:
                return "Inside Utility";
            case AS_INSIDE_SHARE:
                return "Inside Share";
            case AS_INSIDE_BONUS:
                return "Inside Bonus";
            case AS_INSIDE_RIGHTS:
                return "Inside Rights";
            case AS_INSIDE_OTHER_CLAIM:
                return "Inside Other Claim";

            case AS_ACCOUNTING:
                return "Accounting";
        }
        return "unknown";
    }
    public String viewAssetType() {
        return viewAssetTypeCls(this.asset_type);
    }

    public static String viewAssetTypeDescriptionCls(int asset_type) {
        switch (asset_type) {
            case AS_OUTSIDE_GOODS:
                return "Движимые вещи и товары. Эти товары могут быть взяты на хранение кладовщиком или за подтверждение доставки. При этом видны остатки на счетах кладовщиков и доставщиков";
            case AS_OUTSIDE_IMMOVABLE:
                return "Недвижимость и иные товары и вещи не подлежащие доставке. Такие вещи можно взять и дать в аренду";
                
            case AS_OUTSIDE_CURRENCY:
                return "Внешние деньги, которые необходимо перевести на внешний банковский счет или передать наличными. Величина на вашем счете показывает право требовать к эмитенту перевод такого количества денег на ваш банковский счет. Для удовлетворения требования необходимо выставить его на оплату, а после прихода денег подтвердить погашение этого требования";
            case AS_OUTSIDE_SERVICE:
                return "Внешняя услуга, которую необходимо оказать вовне. Для уведомления вашего желания на оказание услуги необходимо предъявить требование и потом подтвердить его исполнение для вас";
            case AS_OUTSIDE_SHARE:
                return "Внешние акции, которые необходимо передать во внешнем депозитарии. Депозитарий можно уведомить предъявлением требования и после подтвердить передачу акций";
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Другие внешние права, требования и обязательства. Любое обязательство (как и другие внешние активы), которое может быть истребовано внесением записи \"требования исполнения\" и погашено записью \"подтверждения исполнения\" этого обязательства";

            case AS_INSIDE_ASSETS:
                return "Внутренний (цифровой) актив. То что не требует каких либо внешних дополнительных действий при передаче его между счетами внутри Эрачейн";
            case AS_INSIDE_CURRENCY:
                return "Цифровые деньги";
            case AS_INSIDE_UTILITY:
                return "Цифровая услуга или стоимость - то что может быть использовано внутри Эрачейн, например как оплата за внешние услуги";
            case AS_INSIDE_SHARE:
                return "Цифровая акция. Доля собственности на внешнее или внутренне предприятие, обладание которой устанавливает право на владение соотвествующей долей на предприятии без надобности совершать какие-либо внешние действия";
            case AS_INSIDE_BONUS:
                return "Цифровая награда (бонус). То что не имеет общепринятой стомости и не может обмениваться на другие виды активов внутри Эрачейн. Хотя обмен на другие бонусы и награды разрешены";
            case AS_INSIDE_RIGHTS:
                return "Цифовые права и голоса. Например права доступа или голосования";
            case AS_INSIDE_OTHER_CLAIM:
                return "Другие цифровые права, требования и обязательства. Эти активы (как и другие цифровые) можно передать в долг и самостоятельно конфисковать долг у должника.";

            case AS_ACCOUNTING:
                return "Счетные (бухгалтерские) единицы. Не имеют общепринятой стоимости и могут обмениваться только между собой. Все действия с ними возможны в целях учета - передать, принять на хранение (на баланс), выдать в долг, забрать долг и т.д.";
        }
        return "";
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
        assetJSON.put("asset_type", this.asset_type);

        return assetJSON;
    }

}
