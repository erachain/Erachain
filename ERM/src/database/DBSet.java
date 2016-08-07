package database;
// upd 09/03
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import controller.Controller;
import core.item.ItemCls;
//import core.item.ItemCls;
import core.web.NameStorageMap;
import core.web.OrphanNameStorageHelperMap;
import core.web.OrphanNameStorageMap;
import core.web.SharedPostsMap;
import settings.Settings;
import utils.ObserverMessage;

public class DBSet implements Observer, IDB {

	private static final int ACTIONS_BEFORE_COMMIT = 10000;
	
	private static DBSet instance;
	
	private AddressForging addressForging;
	private ItemAssetBalanceMap assetBalanceMap;
	private AddressStatement_Refs addressStatement_Refs;
	private ItemAssetBalanceMap assetBalanceAccountingMap;
	private KKAssetStatusMap kKAssetStatusMap;
	private KKPersonStatusMap kKPersonStatusMap;
	private KKUnionStatusMap kKUnionStatusMap;
	private KKAssetUnionMap kKAssetUnionMap;
	private KKPersonUnionMap kKPersonUnionMap;
	private KKStatusUnionMap kKStatusUnionMap;
	private AddressPersonMap addressPersonMap;
	private PersonAddressMap personAddressMap;
	private KK_KPersonStatusUnionMap kK_KPersonStatusUnionMap;
	private VouchRecordMap vouchRecordMap;

	private AddressTime_SignatureMap addressTime_SignatureMap;
	private BlockMap blockMap;
	private ChildMap childMap;
	private HeightMap heightMap;
	private ReferenceMap referenceMap;
	private PeerMap peerMap;
	private TransactionMap transactionMap;
	private NameMap nameMap;
	private NameStorageMap nameStorageMap;
	private OrphanNameStorageMap orphanNameStorageMap;
	private OrphanNameStorageHelperMap orphanNameStorageHelperMap;
	private SharedPostsMap sharedPostsMap;
	private PostCommentMap postCommentMap;
	private CommentPostMap commentPostMap;
	private LocalDataMap localDataMap;
	private BlogPostMap blogPostMap;
	private HashtagPostMap hashtagPostMap;
	private TransactionRef_BlockRef_Map transactionRef_BlockRef_Map;
	private NameExchangeMap nameExchangeMap;
	private UpdateNameMap updateNameMap;
	private CancelSellNameMap cancelSellNameMap;
	private PollMap pollMap;
	private VoteOnPollMap voteOnPollMap;
	private ItemAssetMap itemAssetMap;
	private IssueAssetMap issueAssetMap;
	private OrderMap orderMap;
	private CompletedOrderMap completedOrderMap;
	private TradeMap tradeMap;
	private ItemStatusMap itemStatusMap;
	private IssueStatusMap issueStatusMap;
	private ItemImprintMap imprintMap;
	private IssueImprintMap issueImprintMap;
	private ItemNoteMap itemNoteMap;
	private IssueNoteMap issueNoteMap;
	private IssueStatementMap issueStatementMap;
	private ItemPersonMap itemPersonMap;
	private IssuePersonMap issuePersonMap;
	private ItemUnionMap itemUnionMap;
	private IssueUnionMap issueUnionMap;
	private ATMap atMap;
	private ATStateMap atStateMap;
	private ATTransactionMap atTransactionMap;
	private TransactionFinalMap transactionFinalMap;
	
	private DB database;
	private int actions;
	
	public static DBSet getInstance()
	{
		if(instance == null)
		{
			reCreateDatabase();
		}
		
		return instance;
	}

	public static void reCreateDatabase() {
		//OPEN DB
		File dbFile = new File(Settings.getInstance().getDataDir(), "data.dat");
		dbFile.getParentFile().mkdirs();
		
		//CREATE DATABASE	
		DB database = DBMaker.newFileDB(dbFile)
				.closeOnJvmShutdown()
				.cacheSize(2048)
				.checksumEnable()
				.mmapFileEnableIfSupported()
				.make();
		
		//CREATE INSTANCE
		instance = new DBSet(database);
		
		
	}	
	
	public static DBSet createEmptyDatabaseSet()
	{
		DB database = DBMaker.newMemoryDB()
				.make();
		
		return new DBSet(database);
	}
	
	public DBSet(DB database)
	{
		try {
			this.database = database;
			this.actions = 0;
			
			this.addressForging = new AddressForging(this, database);
			this.assetBalanceMap = new ItemAssetBalanceMap(this, database);
			this.addressStatement_Refs = new AddressStatement_Refs(this, database);
			this.assetBalanceAccountingMap = new ItemAssetBalanceMap(this, database);
			this.kKAssetStatusMap = new KKAssetStatusMap(this, database);
			this.kKPersonStatusMap = new KKPersonStatusMap(this, database);
			this.kKUnionStatusMap = new KKUnionStatusMap(this, database);
			this.kKAssetUnionMap = new KKAssetUnionMap(this, database);
			this.kKPersonUnionMap = new KKPersonUnionMap(this, database);
			this.kKStatusUnionMap = new KKStatusUnionMap(this, database);
			this.addressPersonMap = new AddressPersonMap(this, database);
			this.personAddressMap = new PersonAddressMap(this, database);
			this.kK_KPersonStatusUnionMap = new KK_KPersonStatusUnionMap(this, database);
			this.transactionFinalMap = new TransactionFinalMap(this, database);
			this.vouchRecordMap = new VouchRecordMap(this, database);
			
			this.addressTime_SignatureMap = new AddressTime_SignatureMap(this, database);
			this.blockMap = new BlockMap(this, database);
			this.childMap = new ChildMap(this, database);
			this.heightMap = new HeightMap(this, database);
			this.referenceMap = new ReferenceMap(this, database);
			this.peerMap = new PeerMap(this, database);
			this.transactionMap = new TransactionMap(this, database);
			this.nameMap = new NameMap(this, database);
			this.nameStorageMap = new NameStorageMap(this, database);
			this.orphanNameStorageMap = new OrphanNameStorageMap(this, database);
			this.orphanNameStorageHelperMap = new OrphanNameStorageHelperMap(this, database);
			this.sharedPostsMap = new SharedPostsMap(this, database);
			this.postCommentMap = new PostCommentMap(this, database);
			this.commentPostMap = new CommentPostMap(this, database);
			this.localDataMap = new LocalDataMap(this, database);
			this.blogPostMap = new BlogPostMap(this, database);
			this.hashtagPostMap = new HashtagPostMap(this, database);
			this.transactionRef_BlockRef_Map = new TransactionRef_BlockRef_Map(this, database);
			this.nameExchangeMap = new NameExchangeMap(this, database);
			this.updateNameMap = new UpdateNameMap(this, database);
			this.cancelSellNameMap = new CancelSellNameMap(this, database);
			this.pollMap = new PollMap(this, database);
			this.voteOnPollMap = new VoteOnPollMap(this, database);
			this.itemAssetMap = new ItemAssetMap(this, database);
			this.issueAssetMap = new IssueAssetMap(this, database);
			this.orderMap = new OrderMap(this, database);
			this.completedOrderMap = new CompletedOrderMap(this, database);
			this.tradeMap = new TradeMap(this, database);
			this.imprintMap = new ItemImprintMap(this, database);
			this.issueImprintMap = new IssueImprintMap(this, database);
			this.itemNoteMap = new ItemNoteMap(this, database);
			this.issueNoteMap = new IssueNoteMap(this, database);
			this.issueStatementMap = new IssueStatementMap(this, database);
			this.itemPersonMap = new ItemPersonMap(this, database);
			this.issuePersonMap = new IssuePersonMap(this, database);
			this.itemStatusMap = new ItemStatusMap(this, database);
			this.issueStatusMap = new IssueStatusMap(this, database);
			this.itemUnionMap = new ItemUnionMap(this, database);
			this.issueUnionMap = new IssueUnionMap(this, database);
			this.atMap = new ATMap(this,database);
			this.atStateMap = new ATStateMap(this,database);
			this.atTransactionMap = new ATTransactionMap(this,database);
			
		} catch (Throwable e) {
			this.close();
			throw e;
		}
	}
	
	protected DBSet(DBSet parent)
	{
		this.addressForging = new AddressForging(parent.addressForging);
		this.assetBalanceMap = new ItemAssetBalanceMap(parent.assetBalanceMap);
		this.addressStatement_Refs = new AddressStatement_Refs(parent.addressStatement_Refs);
		this.assetBalanceAccountingMap = new ItemAssetBalanceMap(parent.assetBalanceAccountingMap);
		this.kKAssetStatusMap = new KKAssetStatusMap(parent.kKAssetStatusMap);
		this.kKPersonStatusMap = new KKPersonStatusMap(parent.kKPersonStatusMap);
		this.kKUnionStatusMap = new KKUnionStatusMap(parent.kKUnionStatusMap);
		this.kKAssetUnionMap = new KKAssetUnionMap(parent.kKAssetUnionMap);
		this.kKPersonUnionMap = new KKPersonUnionMap(parent.kKPersonUnionMap);
		this.kKStatusUnionMap = new KKStatusUnionMap(parent.kKStatusUnionMap);
		this.addressPersonMap = new AddressPersonMap(parent.addressPersonMap);
		this.personAddressMap = new PersonAddressMap(parent.personAddressMap);
		this.kK_KPersonStatusUnionMap = new KK_KPersonStatusUnionMap(parent.kK_KPersonStatusUnionMap);
		this.transactionFinalMap = new TransactionFinalMap(parent.transactionFinalMap);
		this.vouchRecordMap = new VouchRecordMap(parent.vouchRecordMap);

		this.addressTime_SignatureMap = new AddressTime_SignatureMap(parent.addressTime_SignatureMap);
		this.blockMap = new BlockMap(parent.blockMap);
		this.childMap = new ChildMap(this.blockMap, parent.childMap);
		this.heightMap = new HeightMap(parent.heightMap);
		this.referenceMap = new ReferenceMap(parent.referenceMap);
		this.peerMap = new PeerMap(parent.peerMap);
		this.transactionMap = new TransactionMap(parent.transactionMap);		
		this.nameMap = new NameMap(parent.nameMap);
		this.nameStorageMap = new NameStorageMap(parent.nameStorageMap);
		this.orphanNameStorageMap = new OrphanNameStorageMap(parent.orphanNameStorageMap);
		this.sharedPostsMap = new SharedPostsMap(parent.sharedPostsMap);
		this.postCommentMap = new PostCommentMap(parent.postCommentMap);
		this.commentPostMap = new CommentPostMap(parent.commentPostMap);
		this.orphanNameStorageHelperMap = new OrphanNameStorageHelperMap(parent.orphanNameStorageHelperMap);
		this.localDataMap = new LocalDataMap(parent.localDataMap);
		this.blogPostMap = new BlogPostMap(parent.blogPostMap);
		this.hashtagPostMap = new HashtagPostMap(parent.hashtagPostMap);
		this.transactionRef_BlockRef_Map = new TransactionRef_BlockRef_Map(this.blockMap, parent.transactionRef_BlockRef_Map);
		this.nameExchangeMap = new NameExchangeMap(parent.nameExchangeMap);
		this.updateNameMap = new UpdateNameMap(parent.updateNameMap);
		this.cancelSellNameMap = new CancelSellNameMap(parent.cancelSellNameMap);
		this.pollMap = new PollMap(parent.pollMap);
		this.voteOnPollMap = new VoteOnPollMap(parent.voteOnPollMap);
		this.itemAssetMap = new ItemAssetMap(parent.itemAssetMap);
		this.issueAssetMap = new IssueAssetMap(parent.getIssueAssetMap());
		this.orderMap = new OrderMap(parent.orderMap);
		this.completedOrderMap = new CompletedOrderMap(parent.completedOrderMap);
		this.tradeMap = new TradeMap(parent.tradeMap);
		this.imprintMap = new ItemImprintMap(parent.imprintMap);
		this.issueImprintMap = new IssueImprintMap(parent.issueImprintMap);
		this.itemNoteMap = new ItemNoteMap(parent.itemNoteMap);
		this.issueNoteMap = new IssueNoteMap(parent.getIssueNoteMap());
		this.issueStatementMap = new IssueStatementMap(parent.issueStatementMap);		
		this.itemPersonMap = new ItemPersonMap(parent.getItemPersonMap());
		this.issuePersonMap = new IssuePersonMap(parent.getIssuePersonMap());
		this.itemStatusMap = new ItemStatusMap(parent.itemStatusMap);
		this.issueStatusMap = new IssueStatusMap(parent.issueStatusMap);
		this.itemUnionMap = new ItemUnionMap(parent.itemUnionMap);
		this.issueUnionMap = new IssueUnionMap(parent.issueUnionMap);
		this.atMap = new ATMap(parent.atMap);
		this.atStateMap = new ATStateMap(parent.atStateMap);
		this.atTransactionMap = new ATTransactionMap(parent.atTransactionMap);
	}
	
	public void reset() {
		
		this.addressForging.reset();
		this.assetBalanceMap.reset();
		this.addressStatement_Refs.reset();
		this.assetBalanceAccountingMap.reset();
		this.kKAssetStatusMap.reset();
		this.kKPersonStatusMap.reset();
		this.kKUnionStatusMap.reset();
		this.kKAssetUnionMap.reset();
		this.kKPersonUnionMap.reset();
		this.kKStatusUnionMap.reset();
		this.addressPersonMap.reset();
		this.personAddressMap.reset();;
		this.kK_KPersonStatusUnionMap.reset();
		this.vouchRecordMap.reset();

		this.heightMap.reset();
		this.referenceMap.reset();
		this.peerMap.reset();
		this.transactionFinalMap.reset();
		this.transactionMap.reset();
		this.nameMap.reset();
		this.nameStorageMap.reset();
		this.orphanNameStorageMap.reset();
		this.orphanNameStorageHelperMap.reset();
		this.sharedPostsMap.reset();
		this.commentPostMap.reset();
		this.postCommentMap.reset();
		this.localDataMap.reset();
		this.blogPostMap.reset();
		this.hashtagPostMap.reset();
		this.transactionRef_BlockRef_Map.reset();
		this.nameExchangeMap.reset();
		this.updateNameMap.reset();
		this.cancelSellNameMap.reset();
		this.pollMap.reset();
		this.voteOnPollMap.reset();
		this.tradeMap.reset();
		this.orderMap.reset();
		this.completedOrderMap.reset();
		this.issueAssetMap.reset();
		this.itemAssetMap.reset();
		this.issueImprintMap.reset();
		this.imprintMap.reset();
		this.issueNoteMap.reset();
		this.issueStatementMap.reset();
		this.itemNoteMap.reset();
		this.issuePersonMap.reset();
		this.itemPersonMap.reset();
		this.issueStatusMap.reset();
		this.itemStatusMap.reset();
		this.issueUnionMap.reset();
		this.itemUnionMap.reset();
		this.atMap.reset();
		this.atStateMap.reset();
		this.atTransactionMap.reset();
	}
	
	public AddressForging getAddressForging() 
	{
		return this.addressForging;
	}
	public ItemAssetBalanceMap getAssetBalanceMap() 
	{
		return this.assetBalanceMap;
	}
	public AddressStatement_Refs getAddressStatement_Refs() 
	{
		return this.addressStatement_Refs;
	}
	
	public ItemAssetBalanceMap getAssetBalanceAccountingMap() 
	{
		return this.assetBalanceAccountingMap;
	}
	public PersonAddressMap getPersonAddressMap() 
	{
		return this.personAddressMap;
	}

	public AddressPersonMap getAddressPersonMap() 
	{
		return this.addressPersonMap;
	}

	public KKAssetStatusMap getAssetStatusMap()
	{
		return this.kKAssetStatusMap;
	}
	public KKPersonStatusMap getPersonStatusMap()
	{
		return this.kKPersonStatusMap;
	}
	public KKUnionStatusMap getUnionStatusMap()
	{
		return this.kKUnionStatusMap;
	}
	public KKAssetUnionMap getAssetUnionMap()
	{
		return this.kKAssetUnionMap;
	}
	public KKPersonUnionMap getPersonUnionMap()
	{
		return this.kKPersonUnionMap;
	}
	public KKStatusUnionMap getStatusUnionMap()
	{
		return this.kKStatusUnionMap;
	}
	/*
	public ItemsStatusesMap getItemsStatusesMap(ItemCls item)
	{
		if (item.getItemTypeInt() == ItemCls.PERSON_TYPE)
			return this.personStatusMap;
		else if (item.getItemTypeInt() == ItemCls.ASSET_TYPE)
			return this.assetStatusMap;
		else if (item.getItemTypeInt() == ItemCls.UNION_TYPE)
			return this.unionStatusMap;
	}
	*/

	public KK_KPersonStatusUnionMap getPersonStatusUnionMap()
	{
		return this.kK_KPersonStatusUnionMap;
	}

	public VouchRecordMap getVouchRecordMap() 
	{
		return this.vouchRecordMap;
	}

	public BlockMap getBlockMap() 
	{
		return this.blockMap;
	}

	public ChildMap getChildMap() 
	{
		return this.childMap;
	}

	public HeightMap getHeightMap() 
	{
		return this.heightMap;
	}

	public ReferenceMap getReferenceMap() 
	{
		return this.referenceMap;
	}
	public AddressTime_SignatureMap getAddressTime_SignatureMap() 
	{
		return this.addressTime_SignatureMap;
	}
	
	public PeerMap getPeerMap() 
	{
		return this.peerMap;
	}
	
	public TransactionMap getTransactionMap() 
	{
		return this.transactionMap;
	}
	
	public TransactionFinalMap getTransactionFinalMap()
	{
		return this.transactionFinalMap;
	}
	
	public NameMap getNameMap()
	{
		return this.nameMap;
	}
	
	public NameStorageMap getNameStorageMap()
	{
		return this.nameStorageMap;
	}
	public OrphanNameStorageMap getOrphanNameStorageMap()
	{
		return this.orphanNameStorageMap;
	}
	public SharedPostsMap getSharedPostsMap()
	{
		return this.sharedPostsMap;
	}
	public PostCommentMap getPostCommentMap()
	{
		return this.postCommentMap;
	}
	public CommentPostMap getCommentPostMap()
	{
		return this.commentPostMap;
	}
	
	public OrphanNameStorageHelperMap getOrphanNameStorageHelperMap()
	{
		return this.orphanNameStorageHelperMap;
	}
	
	public LocalDataMap getLocalDataMap()
	{
		return this.localDataMap;
	}
	
	public BlogPostMap getBlogPostMap()
	{
		return this.blogPostMap;
	}
	public HashtagPostMap getHashtagPostMap()
	{
		return this.hashtagPostMap;
	}
	
	public TransactionRef_BlockRef_Map getTransactionRef_BlockRef_Map()
	{
		return this.transactionRef_BlockRef_Map;
	}
	
	public NameExchangeMap getNameExchangeMap()
	{
		return this.nameExchangeMap;
	}
	
	public UpdateNameMap getUpdateNameMap()
	{
		return this.updateNameMap;
	}
	
	public CancelSellNameMap getCancelSellNameMap()
	{
		return this.cancelSellNameMap;
	}
	
	public PollMap getPollMap()
	{
		return this.pollMap;
	}
	
	public VoteOnPollMap getVoteOnPollDatabase()
	{
		return this.voteOnPollMap;
	}
	
	public ItemAssetMap getItemAssetMap()
	{
		return this.itemAssetMap;
	}
	
	public IssueAssetMap getIssueAssetMap()
	{
		return this.issueAssetMap;
	}
	
	public OrderMap getOrderMap()
	{
		return this.orderMap;
	}
	
	public CompletedOrderMap getCompletedOrderMap()
	{
		return this.completedOrderMap;
	}
	
	public TradeMap getTradeMap()
	{
		return this.tradeMap;
	}
	public ItemImprintMap getItemImprintMap()
	{
		return this.imprintMap;
	}	
	public IssueImprintMap getIssueImprintMap()
	{
		return this.issueImprintMap;
	}
	public ItemNoteMap getItemNoteMap()
	{
		return this.itemNoteMap;
	}	
	public IssueNoteMap getIssueNoteMap()
	{
		return this.issueNoteMap;
	}
	public IssueStatementMap getIssueStatementMap()
	{
		return this.issueStatementMap;
	}
	
	public ItemPersonMap getItemPersonMap()
	{
		return this.itemPersonMap;
	}
	public IssuePersonMap getIssuePersonMap()
	{
		return this.issuePersonMap;
	}
	public ItemStatusMap getItemStatusMap()
	{
		return this.itemStatusMap;
	}	
	public IssueStatusMap getIssueStatusMap()
	{
		return this.issueStatusMap;
	}
	public ItemUnionMap getItemUnionMap()
	{
		return this.itemUnionMap;
	}	
	public IssueUnionMap getIssueUnionMap()
	{
		return this.issueUnionMap;
	}

	public Item_Map getItem_Map(int type) {
		
		switch(type)
			{
			case ItemCls.ASSET_TYPE: {
				return this.getItemAssetMap();
			}
			case ItemCls.IMPRINT_TYPE: {
				return this.getItemImprintMap();
			}
			case ItemCls.NOTE_TYPE: {
				return this.getItemNoteMap();
			}
			case ItemCls.PERSON_TYPE: {
				return this.getItemPersonMap();
			}
			case ItemCls.STATUS_TYPE: {
				return this.getItemStatusMap();	
			}
			case ItemCls.UNION_TYPE: {
				return this.getItemUnionMap();
			}
		}
		return null;
	}

	public ATMap getATMap()
	{
		return this.atMap;
	}
	
	public ATStateMap getATStateMap()
	{
		return this.atStateMap;
	}
	
	public ATTransactionMap getATTransactionMap()
	{
		return this.atTransactionMap;
	}
	
	public DBSet fork()
	{
		return new DBSet(this);
	}
	
	public void close()
	{
		if(this.database != null)
		{
			if(!this.database.isClosed())
			{
				this.database.commit();
				this.database.close();
			}
		}
	}
	
	public boolean isStoped()
	{
		return this.database.isClosed();
	}
	
	public void commit()
	{
		this.actions++;
	}
	
	@Override
	public void update(Observable o, Object arg) 
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		//CHECK IF NEW BLOCK
		if(message.getType() == ObserverMessage.LIST_BLOCK_TYPE)
		{			
			
			//CHECK IF WE NEED TO COMMIT
			if(this.actions >= ACTIONS_BEFORE_COMMIT)
			{
				this.database.commit();
				this.actions = 0;
				
				//NOTIFY CONTROLLER SO HE CAN NOTIFY WALLET
				Controller.getInstance().onDatabaseCommit();
			}
		}
	}

}
