package core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.account.Account;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Base58;
import core.transaction.ArbitraryTransaction;
import core.transaction.Transaction;
import datachain.BlockSignsMap;
import datachain.BlocksHeadsMap;
import datachain.DCSet;
import datachain.TransactionMap;
import network.Peer;
import settings.Settings;
import utils.Pair;

public class BlockChain
{

	// 1825 - 13189664557 - 2718
	// 1824 - 7635471
	
	//public static final int START_LEVEL = 1;
	public static final boolean DEVELOP_USE = false;
	public static final boolean HARD_WORK = false;
	public static final boolean PERSON_SEND_PROTECT = true;
	//public static final int BLOCK_COUNT = 10000; // max count Block (if =<0 to the moon)

	public static final int TESTNET_PORT = DEVELOP_USE?9065:9045;
	public static final int MAINNET_PORT = DEVELOP_USE?9066:9046;

	public static final int DEFAULT_WEB_PORT = DEVELOP_USE?9067:9047;
	public static final int DEFAULT_RPC_PORT = DEVELOP_USE?9068:9048;

	//public static final String TIME_ZONE = "GMT+3";
	//
	public static final boolean ROBINHOOD_USE = false;
	public static final int NEED_PEERS_FOR_UPDATE = HARD_WORK?2:1;

	public static final int MAX_ORPHAN = 1000; // max orphan blocks in chain
	public static final int SYNCHRONIZE_PACKET = 300; // when synchronize - get blocks packet by transactions
	public static final int TARGET_COUNT_SHIFT = 10;
	public static final int TARGET_COUNT = 1<<TARGET_COUNT_SHIFT;
	public static final int BASE_TARGET = 100000;///1 << 15;
	public static final int REPEAT_WIN = DEVELOP_USE?4:40; // GENESIS START TOP ACCOUNTS

	// RIGHTs
	public static final int GENESIS_ERA_TOTAL = 10000000;
	public static final int GENERAL_ERA_BALANCE = GENESIS_ERA_TOTAL / 100;
	public static final int MAJOR_ERA_BALANCE = 33000;
	public static final int MINOR_ERA_BALANCE = 1000;
	public static final int MIN_GENERATING_BALANCE = 100;
	public static final BigDecimal MIN_GENERATING_BALANCE_BD = new BigDecimal(MIN_GENERATING_BALANCE);
	//public static final int GENERATING_RETARGET = 10;
	public static final int GENERATING_MIN_BLOCK_TIME = DEVELOP_USE?120:288; // 300 PER DAY
	public static final int GENERATING_MIN_BLOCK_TIME_MS = BlockChain.GENERATING_MIN_BLOCK_TIME * 1000;
	public static final int WIN_BLOCK_BROADCAST_WAIT_MS = 10000; //

	public static final int BLOCKS_PER_DAY = 24 * 60 * 60 / GENERATING_MIN_BLOCK_TIME; // 300 PER DAY
	//public static final int GENERATING_MAX_BLOCK_TIME = 1000;
	public static final int MAX_BLOCK_BYTES = 2<<21; //4 * 1048576;
	public static final int MAX_REC_DATA_BYTES = MAX_BLOCK_BYTES>>1;
	public static final int GENESIS_WIN_VALUE = DEVELOP_USE?3000:22000;
	public static final String[] GENESIS_ADMINS = new String[]{"78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5",
	"7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC"};

	public static final byte[][] WIPED_RECORDS = DEVELOP_USE?new byte[][]{}:
		new byte[][]{
		Base58.decode("2yTFTetbUrpZzTU3Y1kRSg3nfdetJDC2diwLJTGosnG7sScTkGaFudrTf6iyCkTfUDjP2rXP7pR1o5Y8M4DuwLe3"),
		Base58.decode("zDLLXWRmL8qhrU9DaxTTG4xrLHgb7xLx5fVrC2NXjRaw2vhzB1PArtgqNe2kxp655saohUcWcsSZ8Bo218ByUzH"),
		//Base58.decode("585CPBAusjDWpx9jyx2S2hsHByTd52wofYB3vVd9SvgZqd3igYHSqpS2gWu2THxNevv4LNkk4RRiJDULvHahPRGr"),
		//Base58.decode("4xDHswuk5GsmHAeu82qysfdq9GyTxZ798ZQQGquprirrNBr7ACUeLZxBv7c73ADpkEvfBbhocGMhouM9y13sP8dK"),
		//Base58.decode("2Y81A7YjBji7NDKxYWMeNapSqFWFr8D4PSxBc4dCxSrCCVia6HPy2ZsezYKgeqZugNibAMra6DYT7NKCk6cSVUWX"),
		//Base58.decode("4drnqT2e8uYdhqz2TqscPYLNa94LWHhMZk4UD2dgjT5fLGMuSRiKmHyyghfMUMKreDLMZ5nCK2EMzUGz3Ggbc6W9")
		//Base58.decode("3t9wdnPfDdKxjxjTr7yhVq21ygX5oBMiJLkDFLxp4ZDwH7CiBA8vVcPAk455ec17xub71jYXNtxaMNe7KyERK97N")
	};

	public static final byte[][] VALID_RECORDS = new byte[][]{
	};

	public static final byte[][] VALID_ADDRESSES = new byte[][] {
		Base58.decode("1A3P7u56G4NgYfsWMms1BuctZfnCeqrYk3")
	};

	public static final byte[][] VALID_BAL = DEVELOP_USE?new byte[][]{}:
		new byte[][]{
		Base58.decode("5sAJS3HeLQARZJia6Yzh7n18XfDp6msuaw8J5FPA8xZoinW4FtijNru1pcjqGjDqA3aP8HY2MQUxfdvk8GPC5kjh"),
		Base58.decode("3K3QXeohM3V8beSBVKSZauSiREGtDoEqNYWLYHxdCREV7bxqE4v2VfBqSh9492dNG7ZiEcwuhhk6Y5EEt16b6sVe"),
		Base58.decode("5JP71DmsBQAVTQFUHJ1LJXw4qAHHcoBCzXswN9Ez3H5KDzagtqjpWUU2UNofY2JaSC4qAzaC12ER11kbAFWPpukc"),
		Base58.decode("33okYP8EdKkitutgat1PiAnyqJGnnWQHBfV7NyYndk7ZRy6NGogEoQMiuzfwumBTBwZyxchxXj82JaQiQXpFhRcs"),
		Base58.decode("23bci9zcrPunGppKCm6hKvfRoAStWv4JV2xe16tBEVZSmkCrhw7bXAFzPvv2jqZJXcbA8cmr8oMUfdmS1HJGab7s"),
		Base58.decode("54xdM25ommdxTbAVvP7C9cFYPmwaAexkWHfkhgb8yhfCVvvRNrs166q8maYuXWpk4w9ft2HvctaFaafnKNfjyoKR"),
		Base58.decode("61Fzu3PhsQ74EoMKrwwxKHMQi3z9fYAU5UeUfxtGdXPRfKbWdgpBQWgAojEnmDHK2LWUKtsmyqWb4WpCEatthdgK"),
	};


	public static final int AMOUNT_SCALE_FROM = DEVELOP_USE ? 1034 : 1033;
	public static final int AMOUNT_DEDAULT_SCALE = 8;
	public static final int TRADE_PRECISION = 5;
	public static final int FREEZE_FROM = DEVELOP_USE ? 12980 : 123100;
	public static final String[] TRUE_ADDRESSES = new String[] {
			"7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"
			//"78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5",
			// "7S8qgSTdzDiBmyw7j3xgvXbVWdKSJVFyZv",
	};

	public static HashSet<String> TRUSTED_ANONYMOUS = new HashSet<String>();
	public static HashSet<String> ANONYMASERS = new HashSet<String>();
	public static HashSet<String> FOUNDATION_ADDRESSES = new HashSet<String>();
	public static HashMap<String, int[][]> FREEZED_BALANCES = new HashMap<String, int[][]>();
	public static HashMap<String, Pair<Integer, byte[]>> NOVA_ASSETS = new HashMap<String, Pair<Integer, byte[]>>();
	public static HashMap<String, String> LOCKED__ADDRESSES = new HashMap<String, String>();


	// CHAIN
	public static final int CONFIRMS_HARD = 3; // for reference by signature
	// MAX orphan CHAIN
	public static final int CONFIRMS_TRUE = MAX_ORPHAN; // for reference by ITEM_KEY

	//TESTNET
	//   1486444444444l
	//	 1487844444444   1509434273     1509434273
	public static final long DEFAULT_MAINNET_STAMP = DEVELOP_USE?1511164500000l:1487844793333l;

	//public static final int FEE_MIN_BYTES = 200;
	public static final int FEE_PER_BYTE = 64;
	public static final int FEE_SCALE = 8;
	public static final BigDecimal FEE_RATE = BigDecimal.valueOf(1, FEE_SCALE);
	public static final BigDecimal MIN_FEE_IN_BLOCK = BigDecimal.valueOf(FEE_PER_BYTE * 8 * 128, FEE_SCALE);
	public static final float FEE_POW_BASE = (float)1.5;
	public static final int FEE_POW_MAX = 6;
	public static final int ISSUE_MULT_FEE = 1<<10;
	public static final int ISSUE_ASSET_MULT_FEE = 1<<8;
	//
	public static final int FEE_INVITED_DEEP = 4; // levels for deep
	public static final int FEE_INVITED_SHIFT = 5; // 2^5 = 64 - total FEE -> fee for Forger and fee for Inviter
	public static final int FEE_INVITED_SHIFT_IN_LEVEL = 3;
	public static final int FEE_FOR_ANONIMOUSE = 33;

	// issue PERSON
	//public static final BigDecimal PERSON_MIN_ERA_BALANCE = BigDecimal.valueOf(10000000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

	// SERTIFY
	// need RIGHTS for non PERSON account
	public static final BigDecimal MAJOR_ERA_BALANCE_BD = BigDecimal.valueOf(MAJOR_ERA_BALANCE).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
	// need RIGHTS for PERSON account
	public static final BigDecimal MINOR_ERA_BALANCE_BD = BigDecimal.valueOf(MINOR_ERA_BALANCE).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
	// GIFTS for R_SertifyPubKeys
	public static final int GIFTED_COMPU_AMOUNT = FEE_PER_BYTE<<8;
	public static final BigDecimal GIFTED_COMPU_AMOUNT_BD = BigDecimal.valueOf(GIFTED_COMPU_AMOUNT, FEE_SCALE);
	public static final int GIFTED_COMPU_AMOUNT_FOR_PERSON = GIFTED_COMPU_AMOUNT<<3;
	public static final BigDecimal GIFTED_COMPU_AMOUNT_FOR_PERSON_BD = BigDecimal.valueOf(GIFTED_COMPU_AMOUNT_FOR_PERSON, FEE_SCALE);

	static Logger LOGGER = Logger.getLogger(BlockChain.class.getName());
	private GenesisBlock genesisBlock;
	private long genesisTimestamp;

	private Block waitWinBuffer;
	//private int checkPoint = DEVELOP_USE?1:32400;
	public static final Tuple2<Integer, byte[]> CHECKPOINT = new Tuple2<Integer, byte[]>(36654,
			Base58.decode("4MhxLvzH3svg5MoVi4sX8LZYVQosamoBubsEbeTo2fqu6Fcv14zJSVPtZDuu93Tc7RuS2nPJDYycWjpvdSYdmm1W"));

	//private int target = 0;
	//private byte[] lastBlockSignature;
	//private Tuple2<Integer, Long> HWeight;


	//private DBSet dcSet;

	// dcSet_in = db() - for test
	public BlockChain(DCSet dcSet_in) throws Exception
	{
		//CREATE GENESIS BLOCK
		genesisBlock = new GenesisBlock();
		genesisTimestamp = genesisBlock.getTimestamp(null);

		// GENERAL TRUST
		TRUSTED_ANONYMOUS.add("7BAXHMTuk1vh6AiZU65oc7kFVJGqNxLEpt");
		//TRUSTED_ANONYMOUS.add("79ZVGgCFrQPoVTsFm6qCNTZNkRbYNsTY4u");

		if (DEVELOP_USE) {
			;
		} else {
			// ANOMIMASER for incomes from PRSONALIZED
			ANONYMASERS.add("7BAXHMTuk1vh6AiZU65oc7kFVJGqNxLEpt");
			ANONYMASERS.add("79ZVGgCFrQPoVTsFm6qCNTZNkRbYNsTY4u");


			// TIKER = KEY + CREATOR
			NOVA_ASSETS.put("BTC",
					new Pair<Integer, byte[]>(21, new Account("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh").getShortAddressBytes()));
			//NOVA_ASSETS.put("@@USD",
			//		new Pair<Integer, byte[]>(95, new Account("7JS4ywtcqrcVpRyBxfqyToS2XBDeVrdqZL").getShortBytes()));
			//NOVA_ASSETS.put("¤¤RUB",
			//		new Pair<Integer, byte[]>(93, new Account("7JS4ywtcqrcVpRyBxfqyToS2XBDeVrdqZL").getShortBytes()));
			//NOVA_ASSETS.put("ERARUB",
			//		new Pair<Integer, byte[]>(91, new Account("7JS4ywtcqrcVpRyBxfqyToS2XBDeVrdqZL").getShortBytes()));
			//NOVA_ASSETS.put("ERAUSD",
			//		new Pair<Integer, byte[]>(85, new Account("7JS4ywtcqrcVpRyBxfqyToS2XBDeVrdqZL").getShortBytes()));

			// LOCKED ->
			LOCKED__ADDRESSES.put("7PvUGfFTYPjYi5tcoKHL4UWcf417C8B3oh", "79ZVGgCFrQPoVTsFm6qCNTZNkRbYNsTY4u");
			LOCKED__ADDRESSES.put("7Rt6gdkrFzayyqNec3nLhEGjuK9UsxycZ6", "79ZVGgCFrQPoVTsFm6qCNTZNkRbYNsTY4u");

			// TEST
			//FOUNDATION_ADDRESSES.add("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");

			// ERACHAIN FUNDATION
			FOUNDATION_ADDRESSES.add("74a73pykkNwmuwkZdh5Lt2xTbK7anG5B6i");
			FOUNDATION_ADDRESSES.add("7QTDHp15vcHN3F4zP2BTcDXJkeotzQZkG4");
			FOUNDATION_ADDRESSES.add("7FiXN8VTgjMsLrZUQY9ZBFNfek7SsDP6Uc");
			FOUNDATION_ADDRESSES.add("74QcLxHgPkuMSPsKTh7zGpJsd5aAxpWpFA");
			FOUNDATION_ADDRESSES.add("7BAXHMTuk1vh6AiZU65oc7kFVJGqNxLEpt");
			FOUNDATION_ADDRESSES.add("7P3HR8kdj4ojXPvpTnEtVnpEwenipvrcH1");
			FOUNDATION_ADDRESSES.add("75Mb8cGchcG4DF31wavhNrnoycWsoLQqP4");
			FOUNDATION_ADDRESSES.add("75LzKAoxx4TgAAkpMRStve26YEY625TCRE");

			FOUNDATION_ADDRESSES.add("73QYndpFQeFvyMvwBcMUwJRDTp7XaxkSmZ"); // STOLEN
			FOUNDATION_ADDRESSES.add("7FJUV5GLMuVdopUHSwTLsjmKF4wkPwFEcG"); // LOSED
			FOUNDATION_ADDRESSES.add("75LK84g7JHoLG2jRUmbJA6srLrFkaXEU5A"); // FREEZED


			// TEST
			//FREEZED_BALANCES.put("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7",
			//		new int[][]{{9000, 110000}, {3200, 90000}, {138000, 7000}, {547500, 5000}});

			// TEAM 2
			FREEZED_BALANCES.put("77QMFKSdY4ZsG8bFHynYdFNCmis9fNw5yP",
					new int[][]{{225655, 90000}, {333655, 60000}});
			FREEZED_BALANCES.put("7N7d8juuSSeEd92rkcEsfXhdi9WXE8zYXs",
					new int[][]{{225655, 80000}, {333655, 53000}});
			FREEZED_BALANCES.put("7LETj4cW4rLWBCN52CaXmzQDnhwkEcrv9G",
					new int[][]{{225655, 97000}, {333655, 65000}});

			// TEAM 3
			FREEZED_BALANCES.put("7GMENsugxjV8PToyUyHNUQF7yr9Gy6tJou",
					new int[][]{{225655, 197000}, {333655, 131000}});
			FREEZED_BALANCES.put("7DMJcs8kw7EXUSeEFfNwznRKRLHLrcXJFm",
					new int[][]{{225655, 150000}, {333655, 100000}});
			FREEZED_BALANCES.put("7QUeuMiWQjoQ3MZiriwhKfEG558RJWUUis",
					new int[][]{{225655, 150000}, {333655, 100000}});
			FREEZED_BALANCES.put("7MxscS3mS6VWim8B9K3wEzFAUWYbsMkVon",
					new int[][]{{225655, 140000}, {333655, 90000}});
			FREEZED_BALANCES.put("79NMuuW7thad2JodQ5mKxbMoyf1DjNT9Ap",
					new int[][]{{225655, 130000}, {333655, 90000}});
			FREEZED_BALANCES.put("7MhifBHaZsUcjgckwFN57bAE9fPJVDLDQq",
					new int[][]{{225655, 110000}, {333655, 80000}});
			FREEZED_BALANCES.put("7FRWJ4ww3VstdyAyKFwYfZnucJBK7Y4zmT",
					new int[][]{{225655, 100000}, {333655, 70000}});
			FREEZED_BALANCES.put("7FNAphtSYXtP5ycn88B2KEywuHXzM3XNLK",
					new int[][]{{225655, 90000}, {333655, 60000}});
			FREEZED_BALANCES.put("79ZVGgCFrQPoVTsFm6qCNTZNkRbYNsTY4u",
					new int[][]{{225655, 80000}, {333655, 60000}});

			// TEAM 1
			FREEZED_BALANCES.put("74rRXsxoKtVKJqN8z6t1zHfufBXsELF94y",
					new int[][]{{225655, 20000}, {333655, 10000}});
			FREEZED_BALANCES.put("7PChKkoASF1eLtCnAMx8ynU2sMYdSPwkGV",
					new int[][]{{225655, 60000}, {333655, 40000}});

			FREEZED_BALANCES.put("7Jhh3TPmfoLag8FxnJRBRYYfqnUduvFDbv",
					new int[][]{{225655, 150000}, {333655, 100000}});
			FREEZED_BALANCES.put("7Rt6gdkrFzayyqNec3nLhEGjuK9UsxycZ6",
					new int[][]{{115000, 656000}, {225655, 441000}});
		}

		DCSet dcSet = dcSet_in;
		if (dcSet == null) {
			dcSet = DCSet.getInstance();
		}

		if(Settings.getInstance().isTestnet()) {
			LOGGER.info( genesisBlock.getTestNetInfo() );
		}

		int height = dcSet.getBlockMap().size();
		if(height == 0 )
			// process genesis block
		{
			if(dcSet_in == null && dcSet.getBlockMap().getLastBlockSignature() != null)
			{
				LOGGER.info("reCreate Database...");

				try {
					dcSet.close();
					dcSet = Controller.getInstance().reCreateDC();
				} catch (Exception e) {
					LOGGER.error(e.getMessage(),e);
				}
			}

			//PROCESS
			genesisBlock.process(dcSet);

		} else {
			if (!dcSet.getBlockSignsMap().contains(genesisBlock.getSignature()) ) {

				throw new Exception("wrong DB for GENESIS BLOCK");
			}

		}

		//lastBlockSignature = dcSet.getBlockMap().getLastBlockSignature();
		//HWeight = dcSet.getBlockSignsMap().get(lastBlockSignature);

	}

	public GenesisBlock getGenesisBlock() {
		return this.genesisBlock;
	}
	public long getGenesisTimestamp() {
		return this.genesisTimestamp;
	}
	public long getTimestamp(int height) {
		return this.genesisTimestamp + (long)height * GENERATING_MIN_BLOCK_TIME_MS;
	}
	public long getTimestamp(DCSet dcSet) {
		return this.genesisTimestamp + (long)getHeight(dcSet) * GENERATING_MIN_BLOCK_TIME_MS;
	}

	// BUFFER of BLOCK for WIN solving
	public Block getWaitWinBuffer() {
		return this.waitWinBuffer;
	}
	public void clearWaitWinBuffer() {
		this.waitWinBuffer = null;
	}
	public Block popWaitWinBuffer() {
		Block block = this.waitWinBuffer;
		this.waitWinBuffer = null;
		return block;
	}

	public int compareNewWin(DCSet dcSet, Block block) {
		return this.waitWinBuffer == null?-1: this.waitWinBuffer.compareWin(block);
	}

	// SOLVE WON BLOCK
	// 0 - unchanged;
	// 1 - changed, need broadcasting;
	public synchronized boolean setWaitWinBuffer(DCSet dcSet, Block block) {

		LOGGER.info("try set new winBlock: " + block.toString(dcSet));

		if (this.waitWinBuffer == null
				|| block.compareWin(waitWinBuffer) > 0) {

			this.waitWinBuffer = block;

			LOGGER.info("new winBlock setted! Transactions: " + block.getTransactionCount());
			return true;
		}

		LOGGER.info("new winBlock ignored!");
		return false;
	}

	//
	public static int getHeight(DCSet dcSet) {

		//GET LAST BLOCK
		///byte[] lastBlockSignature = dcSet.getBlockMap().getLastBlockSignature();
		///return dcSet.getBlockSignsMap().getHeight(lastBlockSignature);
		return dcSet.getBlockMap().size();
	}

	/*
	//public synchronized Tuple2<Integer, Long> getHWeight(DCSet dcSet, boolean withWinBuffer) {
	public Tuple2<Integer, Long> getHWeight(DCSet dcSet, boolean withWinBuffer) {

		if (dcSet.isStoped())
			return null;

		//GET LAST BLOCK
		byte[] lastBlockSignature = dcSet.getBlockMap().getLastBlockSignature();
		// test String b58 = Base58.encode(lastBlockSignature);

		int height;
		long weight;
		if (withWinBuffer && this.waitWinBuffer != null) {
			// with WIN BUFFER BLOCK
			height = 1;
			weight = this.waitWinBuffer.calcWinValueTargeted(dcSet);
		} else {
			height = 0;
			weight = 0l;
		}

		if (lastBlockSignature == null) {
			height++;
		} else {
			height += dcSet.getBlockSignsMap().getHeight(lastBlockSignature);
			weight += dcSet.getBlockSignsMap().getFullWeight();
		}

		return  new Tuple2<Integer, Long>(height, weight);

	}
	 */

	public Tuple2<Integer, Long> getHWeightFull(DCSet dcSet) {
		return new Tuple2<Integer, Long>(dcSet.getBlocksHeadsMap().size(),
				dcSet.getBlocksHeadsMap().getFullWeight());
	}

	public long getFullWeight(DCSet dcSet) {

		return dcSet.getBlocksHeadsMap().getFullWeight();
	}

	public static int getCheckPoint(DCSet dcSet) {

		Integer item = dcSet.getBlockSignsMap().get(CHECKPOINT.b);
		if (item == null || item == -1)
			return 2;

		int heightCheckPoint = item;
		int dynamicCheckPoint = getHeight(dcSet) - BlockChain.MAX_ORPHAN;

		if (dynamicCheckPoint > heightCheckPoint)
			return dynamicCheckPoint;
		return heightCheckPoint;
	}

	/*
	public void setCheckPoint(int checkPoint) {

		if (checkPoint > 1)
			this.checkPoint = checkPoint;
	}
	 */

	public static int getNetworkPort() {
		if(Settings.getInstance().isTestnet()) {
			return BlockChain.TESTNET_PORT;
		} else {
			return BlockChain.MAINNET_PORT;
		}
	}

	public List<byte[]> getSignatures(DCSet dcSet, byte[] parentSignature) {

		//LOGGER.debug("getSignatures for ->" + Base58.encode(parent));

		List<byte[]> headers = new ArrayList<byte[]>();

		//CHECK IF BLOCK EXISTS
		Integer height = dcSet.getBlockSignsMap().get(parentSignature);
		if(height != null && height > 0)
		{

			int packet;
			if (Arrays.equals(parentSignature, this.genesisBlock.getSignature())
					|| Arrays.equals(parentSignature, CHECKPOINT.b)) {
				packet = 3;
			} else {
				packet = SYNCHRONIZE_PACKET;
			}
			//BlocksHeadsMap childsMap = dcSet.getBlockHeightsMap();
			//BlocksHeadsMap map = dcSet.getBlockHeightsMap();
			BlocksHeadsMap map = dcSet.getBlocksHeadsMap();
			int counter = 0;
			while(parentSignature != null && counter++ < packet)
			{
				headers.add(parentSignature);
				if (map.contains(++height))
					parentSignature = map.get(height).b;
				else
					parentSignature = null;
			}
			//LOGGER.debug("get size " + counter);
		} else if (Arrays.equals(parentSignature, this.CHECKPOINT.b)) {
			headers.add(parentSignature);
		} else {
			//LOGGER.debug("*** getSignatures NOT FOUND !");
		}

		return headers;
	}

	public Block getBlock(DCSet dcSet, byte[] header) {

		return dcSet.getBlockSignsMap().getBlock(header);
	}
	public Block getBlock(DCSet dcSet, int height) {

		return dcSet.getBlockMap().get(height);
	}

	public int isNewBlockValid(DCSet dcSet, Block block, Peer peer) {

		//CHECK IF NOT GENESIS
		if(block.getVersion() == 0 || block instanceof GenesisBlock)
		{
			LOGGER.debug("isNewBlockValid ERROR -> as GenesisBlock");
			return -100;
		}

		//CHECK IF SIGNATURE IS VALID
		if(!block.isSignatureValid())
		{
			LOGGER.debug("isNewBlockValid ERROR -> signature");
			return -200;
		}

		//int height = dcSet.getBlockHeightsMap().getSize();
		BlockSignsMap dbMap = dcSet.getBlockSignsMap();
		//byte[] lastSignature = dcSet.getBlockHeightsMap().last();
		byte[] lastSignature = dcSet.getBlockMap().getLastBlockSignature();

		byte[] newBlockReference = block.getReference();
		if(!Arrays.equals(lastSignature, newBlockReference)) {

			// при больших нагрузках увеличивает развал сети
			if (true)
				return -111;
			
			/*
			//CHECK IF WE KNOW THIS BLOCK
			if(dbMap.contains(block.getSignature()))
			{
				LOGGER.debug("isNewBlockValid IGNORE -> already in DB #" + block.getHeight(dcSet));
				return 3;
			}

			int height01 = dcSet.getBlocksHeadsMap().size() - 1;
			lastSignature = dcSet.getBlocksHeadsMap().get(height01).b;
			if(Arrays.equals(lastSignature, block.getReference())) {
				// CONCURENT for LAST BLOCK
				Block lastBlock = dcSet.getBlockMap().last();
				if (block.calcWinValue(dcSet) > lastBlock.calcWinValue(dcSet)) {
					LOGGER.debug("isNewBlockValid -> reference to PARENT last block >>> TRY WIN");
					return 4;
				} else {
					LOGGER.debug("isNewBlockValid -> reference to PARENT last block >>> weak...");
					peer.sendMessage(MessageFactory.getInstance().createWinBlockMessage(lastBlock));
					return -4;
				}
			}

			Block winBlock = this.getWaitWinBuffer();
			if (winBlock == null) {
				LOGGER.debug("isNewBlockValid ERROR -> reference NOT to last block AND win BLOCK is NULL");
				return -6;
			}

			if (Arrays.equals(winBlock.getSignature(), newBlockReference)) {
				// this new block for my winBlock + 1 Height
				if (this.getTimestamp(dcSet) + BlockChain.GENERATING_MIN_BLOCK_TIME_MS - (GENERATING_MIN_BLOCK_TIME_MS>>8) > NTP.getTime()) {
					// BLOCK from FUTURE
					LOGGER.debug("isNewBlockValid ERROR -> reference to WIN block in BUFFER and IT from FUTURE");
					return -5;
				} else {
					// LETS FLUSH winBlock and set newBlock as waitWIN
					LOGGER.debug("isNewBlockValid ERROR -> reference to WIN block in BUFFER try SET IT");
					return 5;
				}
			}

			LOGGER.debug("isNewBlockValid ERROR -> reference NOT to last block -9");
			return -7;
			*/
		}

		return 0;
	}

	public Pair<Block, List<Transaction>> scanTransactions(DCSet dcSet, Block block, int blockLimit, int transactionLimit, int type, int service, Account account)
	{
		//CREATE LIST
		List<Transaction> transactions = new ArrayList<Transaction>();
		int counter = 0;

		//IF NO BLOCK START FROM GENESIS
		if(block == null)
		{
			block = new GenesisBlock();
		}

		//START FROM BLOCK
		int scannedBlocks = 0;
		do
		{
			//FOR ALL TRANSACTIONS IN BLOCK
			for(Transaction transaction: block.getTransactions())
			{
				//CHECK IF ACCOUNT INVOLVED
				if(account != null && !transaction.isInvolved(account))
				{
					continue;
				}

				//CHECK IF TYPE OKE
				if(type != -1 && transaction.getType() != type)
				{
					continue;
				}

				//CHECK IF SERVICE OKE
				if(service != -1 && transaction.getType() == Transaction.ARBITRARY_TRANSACTION)
				{
					ArbitraryTransaction arbitraryTransaction = (ArbitraryTransaction) transaction;

					if(arbitraryTransaction.getService() != service)
					{
						continue;
					}
				}

				//ADD TO LIST
				transactions.add(transaction);
				counter++;
			}

			//SET BLOCK TO CHILD
			block = block.getChild(dcSet);
			scannedBlocks++;
		}
		//WHILE BLOCKS EXIST && NOT REACHED TRANSACTIONLIMIT && NOT REACHED BLOCK LIMIT
		while(block != null && (counter < transactionLimit || transactionLimit == -1) && (scannedBlocks < blockLimit || blockLimit == -1));

		//CHECK IF WE REACHED THE END
		if(block == null)
		{
			block = this.getLastBlock(dcSet);
		}
		else
		{
			block = block.getParent(dcSet);
		}

		//RETURN PARENT BLOCK AS WE GET CHILD RIGHT BEFORE END OF WHILE
		return new Pair<Block, List<Transaction>>(block, transactions);
	}

	public Block getLastBlock(DCSet dcSet)
	{
		return dcSet.getBlockMap().last();
	}
	public byte[] getLastBlockSignature(DCSet dcSet)
	{
		return dcSet.getBlockMap().getLastBlockSignature();
	}

	// get last blocks for target
	public List<Block> getLastBlocksForTarget_old(DCSet dcSet)
	{

		Block last = dcSet.getBlockMap().last();

		/*
		if (this.lastBlocksForTarget != null
				&& Arrays.equals(this.lastBlocksForTarget.get(0).getSignature(), last.getSignature())) {
			return this.lastBlocksForTarget;
		}
		 */

		List<Block> list =  new ArrayList<Block>();

		if (last == null || last.getVersion() == 0) {
			return list;
		}

		for (int i=0; i < TARGET_COUNT && last.getVersion() > 0; i++) {
			list.add(last);
			last = last.getParent(dcSet);
		}

		return list;
	}

	public static long calcTarget(int height, long targetPrevios, long winValue)
	{

		if (height < TARGET_COUNT) {
			return targetPrevios - (targetPrevios/height) + (winValue/height);
		}
		
		// CUT GROWTH
		long cut1 = targetPrevios + (targetPrevios>>1);
		if (height > TARGET_COUNT && winValue > cut1) {
			winValue = cut1;
		}
		
		//return targetPrevios - (targetPrevios>>TARGET_COUNT_SHIFT) + (winValue>>TARGET_COUNT_SHIFT);
		// better accuracy
		return (((targetPrevios<<TARGET_COUNT_SHIFT) - targetPrevios) + winValue) >> TARGET_COUNT_SHIFT;
	}

	/*
	// ignore BIG win_values
	public static long getTarget_old(DCSet dcSet, Block block)
	{

		if (block == null)
			return 1000l;

		long min_value = 0;
		long win_value = 0;
		Block parent = block.getParent(dcSet);
		int i = 0;
		long value = 0;

		while (parent != null && parent.getVersion() > 0 && i < BlockChain.TARGET_COUNT)
		{
			i++;
			value = parent.calcWinValue(dcSet);
			if (min_value==0
					|| min_value > value) {
				min_value = value;
			}

			if (min_value + (min_value<<1) < value)
				value = min_value + (min_value<<1);

			parent = parent.getParent(dcSet);
		}

		if (i == 0) {
			return block.calcWinValue(dcSet);
		}

		int height = block.getHeightByParent(dcSet);
		min_value = min_value<<1;

		parent = block.getParent(dcSet);
		i = 0;
		while (parent != null && parent.getVersion() > 0 && i < BlockChain.TARGET_COUNT)
		{
			i++;
			value = parent.calcWinValue(dcSet);
			if (height > TARGET_COUNT && min_value < value)
				value = min_value;
			win_value += value;

			parent = parent.getParent(dcSet);
		}

		return win_value / i;
	}
	 */

	// get Target by last blocks in chain
	public long getTarget(DCSet dcSet)
	{
		Block block = this.getLastBlock(dcSet);
		block.loadHeadMind(dcSet);
		return block.getTarget();
	}

	// GET MIN TARGET
	// TODO GENESIS_CHAIN
	// SEE core.block.Block.calcWinValue(DBSet, Account, int, int)
	public static int getTargetedMin(int height) {
		int base;
		if ( height < BlockChain.REPEAT_WIN)
			// FOR not repeated WINS - not need check BASE_TARGET
			/////base = BlockChain.BASE_TARGET>>1;
			base = BlockChain.BASE_TARGET - (BlockChain.BASE_TARGET>>2); // ONLY UP
		else if (DEVELOP_USE)
			base = 1; //BlockChain.BASE_TARGET >>5;
		else if ( height < 110000)
			base = (BlockChain.BASE_TARGET>>3); // + (BlockChain.BASE_TARGET>>4);
		else if ( height < 115000)
			base = (BlockChain.BASE_TARGET>>1) - (BlockChain.BASE_TARGET>>4);
		else
			base = (BlockChain.BASE_TARGET>>1) + (BlockChain.BASE_TARGET>>4);
	
		return base;
	
		}


	/*
	// IT IS RIGHTS ONLY WHEN BLOCK is MAKING
	// MABE used only in isValid and in Block Generator
	public static int calcGeneratingBalance(DCSet dcSet, Account creator, int height)
	{

		long incomed_amount = 0l;
		//long amount;

		int previousForgingHeight = getPreviousForgingHeightForCalcWin(dcSet, creator, height);
		int previousForgingHeight = creator.getForgingData(dcSet, height);
		if (previousForgingHeight == -1) {
			// IF BLOCK not inserted in MAP
			previousForgingHeight = creator.getLastForgingData(dcSet);
		}

		if (false && !BlockChain.DEVELOP_USE) {
			if (height > 87090 && height - previousForgingHeight < 10 ) {
				return -1;
			}
		}

		if (previousForgingHeight > height) {
			return height;
		}

		previousForgingHeight++;
		if (previousForgingHeight < height) {

			// for recipient only
			List<Transaction> txs = dcSet.getTransactionFinalMap().findTransactions(null, null, creator.getAddress(),
					previousForgingHeight, height,
					0, 0, false, 0, 0);

			//amount = 0l;
			for(Transaction transaction: txs)
			{
				if ( transaction.getAbsKey() != Transaction.RIGHTS_KEY )
					continue;

				transaction.setDC(dcSet, false);
				if (transaction instanceof TransactionAmount) {
					TransactionAmount recordAmount = (TransactionAmount) transaction;
					if (recordAmount.isBackward())
						continue;

					int amo_sign = recordAmount.getAmount().signum();
					if (amo_sign > 0) {
						// SEND or DEBT
						incomed_amount += recordAmount.getAmount().longValue();
					} else {
						continue;
					}
					//} else if (transaction instanceof CreateOrderTransaction) {
					//	amount = transaction.getAmount().longValue();
				} else {
					continue;
				}
				//incomed_amount += amount;
			}

			// for creator
			txs = dcSet.getTransactionFinalMap().findTransactions(null, creator.getAddress(), null,
					previousForgingHeight, height,
					0, 0, false, 0, 0);

			//amount = 0l;
			for(Transaction transaction: txs)
			{
				transaction.setDC(dcSet, false);

				if (false && transaction instanceof R_SertifyPubKeys) {
					//	amount = BlockChain.GIFTED_ERA_AMOUNT.intValue();
					//	incomed_amount += amount;

				} else if (transaction instanceof TransactionAmount) {

					if ( transaction.getAbsKey() != Transaction.RIGHTS_KEY )
						continue;

					TransactionAmount recordAmount = (TransactionAmount) transaction;
					// TODO: delete  on new CHAIN
					if (height > 45281 && recordAmount.isBackward()
							&& Account.actionType(recordAmount.getKey(), recordAmount.getAmount()) == 2) {
						// RE DEBT to me
						long amount = transaction.getAmount().abs().longValue();
						if (amount < 200) {
							continue;
						} else if (amount < 1000) {
							amount >>=2;
						} else {
							amount >>=1;
						}
						incomed_amount += amount;
					} else {
						continue;
					}
				} else {
					continue;
				}

				//incomed_amount += amount;
			}
			txs = null;
		}

		// OWN + RENT balance - in USE
		long used_amount = creator.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).longValue();
		if (used_amount < BlockChain.MIN_GENERATING_BALANCE)
			return 0;

		if (used_amount - incomed_amount < BlockChain.MIN_GENERATING_BALANCE ) {
			return BlockChain.MIN_GENERATING_BALANCE;
		} else {
			return (int)(used_amount - incomed_amount);
		}
	}
	OLD
	 */

	public static int calcWinValueTargeted(long win_value, long target)
	{

		if (target == 0) {
			// in forked chain in may be = 0
			return -1;
		}

		int result = (int)(BlockChain.BASE_TARGET * win_value / target);			
		return result;

	}

	// calc WIN_VALUE for ACCOUNT in HEIGHT
	public static long calcWinValue(DCSet dcSet, Account creator, int height, int forgingBalance) {

		if (forgingBalance < MIN_GENERATING_BALANCE)
			return 0l;
		
		Tuple2<Integer, Integer> previousForgingPoint = creator.getForgingData(dcSet, height);

		if (previousForgingPoint == null) {
			// IF BLOCK not inserted in MAP
			previousForgingPoint = creator.getLastForgingData(dcSet);
			if (previousForgingPoint == null)
				return 0l;
		}

		int previousForgingHeight = previousForgingPoint.a;

		// OWN + RENT balance - in USE
		if (forgingBalance > previousForgingPoint.b) {
			forgingBalance = previousForgingPoint.b;
		}

		if (!Controller.getInstance().isTestNet() && forgingBalance < BlockChain.MIN_GENERATING_BALANCE)
			return 0l;

		int difference = height - previousForgingHeight;
		if (Controller.getInstance().isTestNet()) {
			;
		} else if (BlockChain.DEVELOP_USE) {
			if (height < BlockChain.REPEAT_WIN + 2) {
				if (difference < height - 2)
					return difference - height + 2;
			} else if (height < 20) {
				if (difference < 4)
					return -999l;
			} else {
				//difference -= REPEAT_WIN;				
				if (difference < REPEAT_WIN)
					return difference - REPEAT_WIN;
			}
		} else {

			int repeatsMin;

			if (height < BlockChain.REPEAT_WIN) {
				repeatsMin = height - 2;
			} else {
				repeatsMin = BlockChain.GENESIS_ERA_TOTAL/forgingBalance;
				repeatsMin  = (repeatsMin>>2);

				if (height < 40000) {
					if (repeatsMin > 4)
						repeatsMin = 4;
				} else if (height < 100000) {
					if (repeatsMin > 6)
						repeatsMin = 6;
				} else if (height < 110000) {
					if (repeatsMin > 10) {
						repeatsMin = 10;
					}
				} else if (height < 120000) {
					if (repeatsMin > 40)
						repeatsMin = 40;
				} else if (repeatsMin < 10) {
					repeatsMin = 10;
				}
			}

			if (difference < repeatsMin) {
				return difference - repeatsMin;
			}
		}

		long win_value;

		if (difference > 1)
			win_value = (long)forgingBalance * (long)difference;
		else
			win_value = forgingBalance;
		
		if (DEVELOP_USE)
			return win_value>>2;

		if (false) {
		if (height < BlockChain.REPEAT_WIN)
			win_value >>= 4;
		else if (BlockChain.DEVELOP_USE)
			win_value >>= 4;
		else if (height < BlockChain.TARGET_COUNT)
			win_value = (win_value >>4) - (win_value >>6);
		else if (height < BlockChain.TARGET_COUNT<<2)
			win_value >>= 5;
		else if (height < BlockChain.TARGET_COUNT<<6)
			win_value = (win_value >>5) - (win_value >>7);
		else if (height < BlockChain.TARGET_COUNT<<10)
			win_value >>= 6;
		else
			win_value = (win_value >>7) - (win_value >>9);
		} else {
			if (height < BlockChain.REPEAT_WIN)
				win_value >>= 6;
			//else if (height < 110000)
			//	win_value = (win_value >>6) + (win_value >>9);
			else
				win_value >>=7;
		}


		return win_value;

	}

	public static int calcWinValueTargetedBase(DCSet dcSet, int height, long win_value, long target) {

		if (win_value < 1)
			return (int)win_value;

		int base = BlockChain.getTargetedMin(height);
		int targetedWinValue = calcWinValueTargeted(win_value, target);
		if (!Controller.getInstance().isTestNet() && base > targetedWinValue) {
			return -targetedWinValue;
		}

		return targetedWinValue;

	}
	// CLEAR UNCONFIRMED TRANSACTION from Invalid and DEAD
	public void clearUnconfirmedRecords(Controller ctrl, DCSet dcSetOriginal) {

		if (true)
			return;

		long startTime = System.currentTimeMillis();

		long timestamp = GENERATING_MIN_BLOCK_TIME_MS + this.getTimestamp(dcSetOriginal);

		TransactionMap unconfirmedMap = dcSetOriginal.getTransactionMap();
		Iterator<byte[]> iterator = unconfirmedMap.getIterator(0, false);

		//CREATE FORK OF GIVEN DATABASE
		///DCSet dcFork = dcSetOriginal.fork();
		Transaction transaction;
		byte[] key;

		while(iterator.hasNext())
		{

			if (ctrl.isOnStopping()) {
				return;
			}

			key = iterator.next();
			transaction = unconfirmedMap.get(key);

			//CHECK TRANSACTION DEADLINE
			if(transaction.getDeadline() < timestamp) {
				unconfirmedMap.delete(key);
				continue;
			}

			//CHECK IF VALID
			if(!transaction.isSignatureValid(DCSet.getInstance())) {
				// INVALID TRANSACTION
				unconfirmedMap.delete(key);
				continue;
			}

			/*
				transaction.setDB(dcFork, false);

				if (transaction.isValid(dcFork, null) != Transaction.VALIDATE_OK) {
					// INVALID TRANSACTION
					unconfirmedMap.delete(transaction);
					continue;
				}
			}
			 */
		}

		LOGGER.debug("timerUnconfirmed ----------------  work: "
				+ (System.currentTimeMillis() - startTime)
				+ " new SIZE: " + dcSetOriginal.getUncTxCounter());

	}
}
