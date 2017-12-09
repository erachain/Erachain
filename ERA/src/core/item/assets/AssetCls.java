package core.item.assets;


import core.BlockChain;
import core.account.PublicKeyAccount;
import core.item.ItemCls;
import database.wallet.DWSet;
import datachain.DCSet;
import datachain.Issue_ItemMap;
import datachain.Item_Map;
import lang.Lang;
import database.wallet.FavoriteItem;


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
			+ "."
			;

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
		
	protected AssetCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		super(typeBytes, owner, name, icon, image, description);
	}
	public AssetCls(int type, byte pars, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		this(new byte[TYPE_LENGTH], owner, name, icon, image, description);
		this.typeBytes[0] = (byte)type;
		this.typeBytes[1] = pars;
	}

	//GETTERS/SETTERS
	public String getName() {
		/*
		if (this.key == 1)
			return "ERA";
			*/
		
		return this.name;
	}
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

	public int getItemTypeInt() { return ItemCls.ASSET_TYPE; }
	public String getItemTypeStr() { return "asset"; }
	
	// DB
	public Item_Map getDBMap(DCSet db)
	{
		return db.getItemAssetMap();
	}
	public Issue_ItemMap getDBIssueMap(DCSet db)
	{
		return db.getIssueAssetMap();
	}

	public boolean isMovable() {
		return (this.typeBytes[1] & (byte)1) > 0;
	}
	public void setMovable(boolean movable) {
		this.typeBytes[1] = (byte)(this.typeBytes[1] & (movable?1:0));
	}
	
	public abstract Long getQuantity(DCSet dc);
	public abstract Long getTotalQuantity(DCSet dc);

	public boolean isDivisible() {
		return true;
	}
	public int getScale() {
		return 8;
	}

	/*
	public byte[] toBytes(boolean includeReference)
	{
		return super.toBytes(includeReference);
	}
	*/
	
}
