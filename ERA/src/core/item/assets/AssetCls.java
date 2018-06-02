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

    public static final int AS_GOODS = 0;
    public static final int AS_CURRENCY = 1;
    public static final int AS_CLAIM = 2;
    public static final int AS_ACCOUNTING = 3;

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

        if (this.asset_type == AS_CLAIM)
            return 6;
        if (this.asset_type == AS_ACCOUNTING)
            return 6;

        return BlockChain.DEVELOP_USE ? 10 : 12;
    }

    @Override
    public String viewName() {

        if (this.key < 5) {
            return "" + this.name; // ®
        }

        switch (this.asset_type) {
            case AS_CURRENCY:
                return "▼" + this.name;
            case AS_CLAIM:
                return "◄" + this.name; // ® ■ ± █
            case AS_ACCOUNTING:
                if (this.key == 555l || this.key == 666l || this.key == 777l)
                    return this.name;

                return "±" + this.name;
            default:
                return "▲" + this.name;
        }
    }


    public PublicKeyAccount getOwner() {
	if (this.key == 7 || this.key == 8) {
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
        if (this.key == 0) {
            Long error = null;
            error++;
        } else if (this.key < 5 ||
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
        return this.asset_type == AS_GOODS;
    }

    public boolean isImMovable() {
        if (false && this.key < BlockChain.AMOUNT_SCALE_FROM) {
            return (this.typeBytes[1] & (byte) 1) <= 0;
        }
        return this.asset_type == AS_CURRENCY;
    }

    public boolean isClaim() {
        return this.asset_type == AS_CLAIM;
    }

    public boolean isAccounting() {
        return this.asset_type == AS_ACCOUNTING;
    }

    public String viewAssetType() {
        switch (this.asset_type) {
            case AS_GOODS:
                return "Movable";
            case AS_CURRENCY:
                return "Immovable";
            case AS_CLAIM:
                return "Claim";
            case AS_ACCOUNTING:
                return "Accounting";
        }
        return "unknown";
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
