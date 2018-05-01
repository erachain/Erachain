package core.blockexplorer;
import java.io.UnsupportedEncodingException;
// 30/03 ++ asset - Trans_Amount
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;
import org.mapdb.Fun.Tuple6;

import at.AT;
import at.AT_Transaction;
import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import core.item.assets.Trade;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.item.templates.TemplateCls;
import core.naming.Name;
import core.payment.Payment;
import core.transaction.ArbitraryTransaction;
import core.transaction.BuyNameTransaction;
import core.transaction.CancelOrderTransaction;
import core.transaction.CancelSellNameTransaction;
import core.transaction.CreateOrderTransaction;
import core.transaction.CreatePollTransaction;
import core.transaction.DeployATTransaction;
import core.transaction.IssueAssetTransaction;
import core.transaction.Issue_ItemRecord;
import core.transaction.MultiPaymentTransaction;
import core.transaction.R_Send;
import core.transaction.R_SignNote;
import core.transaction.RegisterNameTransaction;
import core.transaction.SellNameTransaction;
import core.transaction.Transaction;
import core.transaction.TransactionAmount;
import core.transaction.UpdateNameTransaction;
import core.transaction.VoteOnPollTransaction;
import core.voting.Poll;
import core.voting.PollOption;
import datachain.DCSet;
import datachain.SortableList;
import datachain.TradeMap;
import gui.models.PeersTableModel;
import gui.models.PersonAccountsModel;
import lang.Lang;
import settings.Settings;
import utils.BlExpUnit;
import utils.DateTimeFormat;
import utils.GZIP;
import utils.NumberAsString;
import utils.Pair;
import utils.ReverseComparator;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BlockExplorer
{
	private JSONObject langObj;
	private static final Logger LOGGER = Logger.getLogger(BlockExplorer.class);
	private static BlockExplorer blockExplorer;
	private Locale local = new Locale("ru","RU"); // РЎвЂћР С•РЎР‚Р С�Р В°РЎвЂљ Р Т‘Р В°РЎвЂљРЎвЂ№
	private DateFormat df = DateFormat.getDateInstance(DateFormat.DATE_FIELD, local); // Р Т‘Р В»РЎРЏ РЎвЂћР С•РЎР‚Р С�Р В°РЎвЂљР В° Р Т‘Р В°РЎвЂљРЎвЂ№
	private static final long FEE_KEY = Transaction.FEE_KEY;
	public static final String  LANG_DEFAULT = "en";
	private  String lang_file;
	private DCSet dcSet;

	public static BlockExplorer getInstance()
	{
		if(blockExplorer == null)
		{
			blockExplorer = new BlockExplorer();
			blockExplorer.dcSet = DCSet.getInstance();
		}

		return blockExplorer;
	}

	public static String timestampToStr(long timestamp)
	{
		return DateTimeFormat.timestamptoString(timestamp);
	}


	@SuppressWarnings("static-access")
	public Map jsonQueryMain(UriInfo info) throws UnsupportedEncodingException
	{
		Stopwatch stopwatchAll = new Stopwatch();

		Map output = new LinkedHashMap();
		//lang
		if(!info.getQueryParameters().containsKey("lang")){
			lang_file = LANG_DEFAULT +".json";
		}else {

			lang_file = 	info.getQueryParameters().getFirst("lang") +".json";
		}

		LOGGER.error("try lang file: " + lang_file);

		langObj = Lang.getInstance().openLangFile(lang_file);

		List<Tuple2<String, String>> langs = Lang.getInstance().getLangListToWeb();

		Map lang_list =  new LinkedHashMap();
		int i = 0;
		for ( Tuple2<String, String> lang:langs){
			Map lang_par =  new LinkedHashMap();
			lang_par.put("ISO", lang.a);
			lang_par.put("name", lang.b);
			lang_list.put(i,lang_par );
			i++;

		}
		output.put("Lang", lang_list);

		output.put("id_home2",Lang.getInstance().translate_from_langObj("Blocks",langObj));
		output.put("id_menu_top_100", Lang.getInstance().translate_from_langObj("Top 100 Richest",langObj));
		output.put("id_menu_percons",  Lang.getInstance().translate_from_langObj("Persons",langObj));
		output.put("id_menu_pals_asset",  Lang.getInstance().translate_from_langObj("Polls",langObj));
		output.put("id_menu_assets", Lang.getInstance().translate_from_langObj("Assets",langObj));
		output.put("id_menu_aTs", Lang.getInstance().translate_from_langObj("ATs",langObj));

		if(info.getQueryParameters().containsKey("balance"))
		{
			output.put("lastBlock", jsonQueryLastBlock());
			for (String address : info.getQueryParameters().get("balance")) {
				output.put(address, jsonQueryBalance(address));
			}
			return output;
		}

		if(info.getQueryParameters().containsKey("q"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			if(info.getQueryParameters().containsKey("search")) {

				String type = info.getQueryParameters().get("search").get(0);
				if (type.equals("persons") || type.equals("person") ){
					// search persons
					output.put("search", type);
					output.putAll(jsonQuerySearchPersons(info.getQueryParameters().getFirst("q")));
					return output;
				}
				else if (type.equals("assets") || type.equals("asset") ){
					// search assets
					output.put("search", type);
					output.putAll(jsonQuerySearchAssets(info.getQueryParameters().getFirst("q")));
					return output;
				}
				else if (type.equals("status") || type.equals("statuses") ){
					// search assets
					output.put("search", type);
					output.putAll(jsonQuerySearchStatuses(info.getQueryParameters().getFirst("q")));
					return output;
				}

			}
			output.putAll(jsonQuerySearch(URLDecoder.decode(info.getQueryParameters().getFirst("q"), "UTF-8")));
			return output;
		}

		if(info.getQueryParameters().containsKey("names"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryNames());

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("top"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			if(info.getQueryParameters().containsKey("asset"))
			{
				output.putAll(jsonQueryTopRichest(
						Integer.valueOf((info.getQueryParameters().getFirst("top"))),
						Long.valueOf((info.getQueryParameters().getFirst("asset")))
						));
			}
			else
			{
				output.putAll(jsonQueryTopRichest(Integer.valueOf((info.getQueryParameters().getFirst("top"))), 1l ));
			}

			output.put("assets", jsonQueryAssetsLite());

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("assetsLite"))
		{
			output.put("assetsLite", jsonQueryAssetsLite());

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("assets"))
		{

			output.put("lastBlock", jsonQueryLastBlock());
			int start = 0;
			if(info.getQueryParameters().containsKey("start"))
			{
				if (info.getQueryParameters().getFirst("start").matches("[0-9]*"))
					start = Integer.valueOf((info.getQueryParameters().getFirst("start")));
			}

			output.putAll(jsonQueryAssets(start));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;


		}

		if(info.getQueryParameters().containsKey("aTs"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			output.put("aTs", jsonQueryATs());

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("polls"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryPools(info.getQueryParameters().getFirst("asset")));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("asset"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			if(info.getQueryParameters().get("asset").size() == 1)
			{
				try
				{
					output.put("asset", jsonQueryAsset(Long.valueOf((info.getQueryParameters().getFirst("asset")))));
				} catch (Exception e) {
					output.put("error", "Asset with given key is missing!");
					return output;
				}
			}

			if(info.getQueryParameters().get("asset").size() == 2)
			{
				long have = Integer.valueOf(info.getQueryParameters().get("asset").get(0));
				long want = Integer.valueOf(info.getQueryParameters().get("asset").get(1));

				output.putAll(jsonQueryTrades(have, want));
			}

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("blocks"))
		{
			int start = -1;

			if(info.getQueryParameters().containsKey("start"))
			{
				start = Integer.valueOf((info.getQueryParameters().getFirst("start")));
			}

			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryBlocks(start));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("peers"))
		{
			int start = 0;

			if(info.getQueryParameters().containsKey("start"))
			{
				start = Integer.valueOf((info.getQueryParameters().getFirst("start")));
			}

			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryPeers(start));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}



		if(info.getQueryParameters().containsKey("lastBlock"))
		{
			output = jsonQueryLastBlock();

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("addr"))
		{
			int start = -1;
			int txOnPage = 100;
			String filter = "standart";
			boolean allOnOnePage = false;
			String showOnly = "";
			String showWithout = "";

			int transPage = 1;
			if (info.getQueryParameters().containsKey("page"))
			{
				transPage = Integer.parseInt(info.getQueryParameters().getFirst("page"));
			}
			if(info.getQueryParameters().containsKey("start"))
			{
				start = Integer.valueOf((info.getQueryParameters().getFirst("start")));
			}

			if(info.getQueryParameters().containsKey("txOnPage"))
			{
				txOnPage = Integer.valueOf((info.getQueryParameters().getFirst("txOnPage")));
			}

			if(info.getQueryParameters().containsKey("filter"))
			{
				filter = info.getQueryParameters().getFirst("filter");
			}

			if(info.getQueryParameters().containsKey("allOnOnePage"))
			{
				allOnOnePage = true;
			}

			if(info.getQueryParameters().containsKey("showOnly"))
			{
				showOnly = info.getQueryParameters().getFirst("showOnly");
			}

			if(info.getQueryParameters().containsKey("showWithout"))
			{
				showWithout = info.getQueryParameters().getFirst("showWithout");
			}

			output.put("lastBlock", jsonQueryLastBlock());
			output.putAll(jsonQueryAddress(info.getQueryParameters().get("addr"), transPage, start, txOnPage, filter, allOnOnePage, showOnly, showWithout));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("name"))
		{
			int start = -1;
			int txOnPage = 100;
			String filter = "standart";
			boolean allOnOnePage = false;

			if(info.getQueryParameters().containsKey("start"))
			{
				start = Integer.valueOf((info.getQueryParameters().getFirst("start")));
			}

			if(info.getQueryParameters().containsKey("txOnPage"))
			{
				txOnPage = Integer.valueOf((info.getQueryParameters().getFirst("txOnPage")));
			}

			if(info.getQueryParameters().containsKey("filter"))
			{
				filter = info.getQueryParameters().getFirst("filter");
			}

			if(info.getQueryParameters().containsKey("allOnOnePage"))
			{
				allOnOnePage = true;
			}

			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryName(info.getQueryParameters().getFirst("name"), start, txOnPage, filter, allOnOnePage));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("block"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			int transPage = 1;
			if (info.getQueryParameters().containsKey("page"))
			{
				transPage = Integer.parseInt(info.getQueryParameters().getFirst("page"));
			}
			output.putAll(jsonQueryBlock(info.getQueryParameters().getFirst("block"), transPage));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("tx"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryTX(info.getQueryParameters().getFirst("tx")));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("trade"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryTrade(info.getQueryParameters().getFirst("trade")));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("atTx"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryATtx(info.getQueryParameters().getFirst("atTx")));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("poll"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryPool(info.getQueryParameters().getFirst("poll"),info.getQueryParameters().getFirst(" asset")));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("unconfirmed"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryUnconfirmedTXs());

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("blogposts"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryBlogPostsTx(info.getQueryParameters().getFirst("blogposts")));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}


		if(info.getQueryParameters().containsKey("persons"))
		{
			String start = null;

			if(info.getQueryParameters().containsKey("startPerson"))
			{
				start = info.getQueryParameters().getFirst("startPerson");
			}

			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryPersons(start));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("person"))
		{

			output.put("lastBlock", jsonQueryLastBlock());
			output.putAll(jsonQueryPerson(info.getQueryParameters().getFirst("person")));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}


		if(info.getQueryParameters().containsKey("templates"))
		{
			int start = -1;

			if(info.getQueryParameters().containsKey("start"))
			{
				start = Integer.valueOf((info.getQueryParameters().getFirst("start")));
			}

			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryTemplates(start));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("statuses"))
		{
			int start = -1;

			if(info.getQueryParameters().containsKey("start"))
			{

				try {
					start = Integer.valueOf((info.getQueryParameters().getFirst("start")));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					start = 0;
				}
			}

			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryStatuses(start));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}




		if(info.getQueryParameters().containsKey("template"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryTemplate(Long.valueOf(info.getQueryParameters().getFirst("template"))));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}

		if(info.getQueryParameters().containsKey("status"))
		{
			output.put("lastBlock", jsonQueryLastBlock());

			output.putAll(jsonQueryStatus(Long.valueOf(info.getQueryParameters().getFirst("status"))));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}



		if(info.getQueryParameters().containsKey("Seg_No"))
		{

			output.put("lastBlock", jsonQueryLastBlock());
			output.putAll(jsonQueryStatement(info.getQueryParameters().getFirst("block"),info.getQueryParameters().getFirst("Seg_No")));

			output.put("queryTimeMs", stopwatchAll.elapsedTime());
			return output;
		}





		output.put("queryTimeMs", stopwatchAll.elapsedTime());

		/*	} catch (Exception e1) {
			output = new LinkedHashMap();
			output.put("error", e1.getLocalizedMessage());
			output.put("help", jsonQueryHelp());
			return output;
		} */

		output.put("error", "Not enough parameters.");
		output.put("help", jsonQueryHelp());

		return output;
	}














	public Map jsonQueryHelp()
	{
		Map help = new LinkedHashMap();

		help.put("Unconfirmed Transactions", "blockexplorer.json?unconfirmed");
		help.put("Block", "blockexplorer.json?block={block}[&page={page}]");
		help.put("Blocks List", "blockexplorer.json?blocks[&start={height}]");
		help.put("Assets List", "blockexplorer.json?assets");
		help.put("Assets List Lite", "blockexplorer.json?assetsLite");
		help.put("Asset", "blockexplorer.json?asset={asset}");
		help.put("Asset Trade", "blockexplorer.json?asset={assetHave}&asset={assetWant}");
		help.put("Polls List", "blockexplorer.json?polls");
		help.put("Poll", "blockexplorer.json?poll={poll}&asset={asset}");
		help.put("AT TX", "blockexplorer.json?atTx={atTx}");
		help.put("Trade", "blockexplorer.json?trade={initiatorSignature}/{targetSignature}");
		help.put("Transaction", "blockexplorer.json?tx={txSignature}");
		help.put("Name", "blockexplorer.json?name={name}");
		help.put("Name (additional)", "blockexplorer.json?name={name}&start={offset}&allOnOnePage");
		help.put("Address", "blockexplorer.json?addr={address}");
		help.put("Address (additional)", "blockexplorer.json?addr={address}&start={offset}&allOnOnePage&withoutBlocks&showWithout={1,2,blocks}&showOnly={type}");
		help.put("Top Richest", "blockexplorer.json?top");
		help.put("Top Richest", "blockexplorer.json?top={limit}&asset={asset}");
		help.put("Address All Not Zero", "blockexplorer.json?top=allnotzero");
		help.put("Address All Addresses", "blockexplorer.json?top=all");
		help.put("Assets List", "blockexplorer.json?assets");
		help.put("Assets List", "blockexplorer.json?assets");
		help.put("AT List", "blockexplorer.json?aTs");
		help.put("Names List", "blockexplorer.json?names");
		help.put("BlogPosts of Address", "blockexplorer.json?blogposts={addr}");
		help.put("Search", "blockexplorer.json?q={text}");
		help.put("Balance", "blockexplorer.json?balance={address}[&balance=address2...]");


		return help;
	}

	public Map jsonQuerySearch(String query)
	{
		Map output=new LinkedHashMap();
		Map outputItem=new LinkedHashMap();
		Map foundList=new LinkedHashMap();

		output.put("query", query);

		int i = 0;

		byte[] signatureBytes = null;

		try
		{
			signatureBytes = Base58.decode(query);
		}
		catch (Exception e)
		{

		}

		if (Crypto.getInstance().isValidAddress(query))
		{
			if(query.startsWith("7"))
			{
				i++;
				outputItem=new LinkedHashMap();
				outputItem.put(1, "standardAccount");
				outputItem.put(2, Lang.getInstance().translate_from_langObj("Standard Account",langObj));
				foundList.put(i,outputItem);
			}

			if(query.startsWith("A"))
			{
				i++;
				outputItem=new LinkedHashMap();
				outputItem.put(1,"atAccount");
				outputItem.put(2,  Lang.getInstance().translate_from_langObj("At Account",langObj));
				foundList.put(i,outputItem);
			}

			output.put("foundCount", i);
			output.put("foundList", foundList);

			return output;
		}

		if (query.indexOf(',') != -1 )
		{
			String[] strings = query.split(",");

			boolean isAddresses = strings.length > 0;

			for (String string : strings)
			{
				if (!string.startsWith("7"))
				{
					isAddresses = false;
					break;
				}

				if (!Crypto.getInstance().isValidAddress(string))
				{
					isAddresses = false;
					break;
				}
			}

			if (isAddresses)
			{
				i++;
				outputItem=new LinkedHashMap();
				outputItem.put(1, "multiAccount");
				outputItem.put(2,  Lang.getInstance().translate_from_langObj("Multi Account",langObj));
				foundList.put(i, outputItem);

				output.put("foundCount", i);
				output.put("foundList", foundList);

				return output;

			}
		}

		if (signatureBytes != null && dcSet.getBlockSignsMap().contains(signatureBytes))
		{
			i++;
			outputItem=new LinkedHashMap();
			outputItem.put(1,"blockSignature");
			outputItem.put(2, Lang.getInstance().translate_from_langObj("Block Signature",langObj));
			foundList.put(i, outputItem);
		}
		else if(query.matches("\\d+") && Integer.valueOf(query) > 0 && Integer.valueOf(query) <= getHeight())
		{
			i++;
			outputItem=new LinkedHashMap();
			outputItem.put(1,"blockHeight");
			outputItem.put(2, Lang.getInstance().translate_from_langObj("Block",langObj));
			foundList.put(i,outputItem);
		}
		else if (query.equals("last"))
		{
			i++;
			outputItem=new LinkedHashMap();
			outputItem.put(1,"blockLast");
			outputItem.put(2, Lang.getInstance().translate_from_langObj("Block Last",langObj));
			foundList.put(i,outputItem);
		}
		else
		{
			if(!(signatureBytes == null) && (dcSet.getTransactionFinalMapSigns().contains(signatureBytes)))
			{
				i++;
				outputItem=new LinkedHashMap();
				outputItem.put(1,"transactionSignature");
				outputItem.put(2, Lang.getInstance().translate_from_langObj("Transaction Signature",langObj));
				foundList.put(i,outputItem);

			}
		}

		if (dcSet.getNameMap().contains(query))
		{
			i++;
			outputItem=new LinkedHashMap();
			outputItem.put(1,"name");
			outputItem.put(2, Lang.getInstance().translate_from_langObj("Name",langObj));
			foundList.put(i, outputItem);
		}

		if (query.matches("\\d+") && dcSet.getItemAssetMap().contains(Long.valueOf(query)))
		{
			i++;
			outputItem=new LinkedHashMap();
			outputItem.put(1,"asset");
			outputItem.put(2, Lang.getInstance().translate_from_langObj("Asset",langObj));
			foundList.put(i, outputItem);
		}

		if (query.matches("\\d+") && dcSet.getItemPersonMap().contains(Long.valueOf(query)))
		{
			i++;
			outputItem=new LinkedHashMap();
			outputItem.put(1,"person");
			outputItem.put(2, Lang.getInstance().translate_from_langObj("Person",langObj));
			foundList.put(i, outputItem);
		}


		if (dcSet.getPollMap().contains(query))
		{
			i++;
			outputItem=new LinkedHashMap();
			outputItem.put(1,"pool");
			outputItem.put(2, Lang.getInstance().translate_from_langObj("pool",langObj));

			foundList.put(i, outputItem);
		}

		if (query.indexOf('/') != -1 )
		{
			String[] signatures = query.split("/");

			try
			{
				if(dcSet.getTransactionFinalMapSigns().contains(Base58.decode(signatures[0])) ||
						dcSet.getTransactionFinalMapSigns().contains(Base58.decode(signatures[1])))
				{
					i++;
					outputItem=new LinkedHashMap();
					outputItem.put(1,"trade");
					outputItem.put(2, Lang.getInstance().translate_from_langObj("Trade",langObj));
					foundList.put(i, outputItem);
				}
			}
			catch (Exception e)
			{
				LOGGER.error(e.getMessage(),e);
			}
		}


		if (query.indexOf(':') != -1 )
		{

			int blockHeight = Integer.valueOf(query.split(":")[0]);
			int seq = Integer.valueOf(query.split(":")[1]);

			LinkedHashMap<Tuple2<Integer, Integer>, AT_Transaction> atTxs = dcSet.getATTransactionMap().getATTransactions(blockHeight);

			if(atTxs.size()>seq)
			{
				i++;
				outputItem=new LinkedHashMap();
				outputItem.put(1,"atTx");
				outputItem.put(2, Lang.getInstance().translate_from_langObj("atTx",langObj));
				foundList.put(i, outputItem);
			}
		}

		Pattern statementPattern = Pattern.compile("^(\\d*)-(\\d*)$");
		Matcher matcher = statementPattern.matcher(query);
		if (matcher.matches())
		{
			Integer blockNo = Integer.valueOf(matcher.group(1));
			Integer seqNo = Integer.valueOf(matcher.group(2));

			if (dcSet.getTransactionFinalMap().contains(new Tuple2(blockNo, seqNo)))
			{
				i++;
				outputItem=new LinkedHashMap();
				outputItem.put(1,"statement");
				outputItem.put(2, Lang.getInstance().translate_from_langObj("Statement",langObj));
				foundList.put(i,outputItem);
			}
		}

		output.put("foundCount", i);
		output.put("foundList", foundList);

		if (i <1 ) {
			output.put("title", Lang.getInstance().translate_from_langObj("Search no results",langObj));
		}
		else {
			output.put("title", Lang.getInstance().translate_from_langObj("Search results",langObj));
		};
		return output;
	}

	public Map jsonQueryBlogPostsTx(String addr) {

		Map output=new LinkedHashMap();
		try {

			AssetNames assetNames = new AssetNames();

			List<Transaction> transactions = new ArrayList<Transaction>();

			if (Crypto.getInstance().isValidAddress(addr)) {
				Account account = new Account(addr);

				String address = account.getAddress();
				// get reference to parent record for this account
				Long timestampRef = account.getLastTimestamp();
				// get signature for account + time
				byte[] signatureBytes = dcSet.getAddressTime_SignatureMap().get(address, timestampRef);

				Controller cntr = Controller.getInstance();
				do{
					//Transaction transaction = Controller.getInstance().getTransaction(signatureBytes);
					Transaction transaction = cntr.getTransaction(signatureBytes);
					if(transaction == null)
					{
						break;
					}
					if(transaction.getCreator() == null
							&& !transaction.getCreator().getAddress().equals(addr))
					{
						break;
					}

					if(transaction.getType() == Transaction.ARBITRARY_TRANSACTION
							&& ((ArbitraryTransaction)transaction).getService() == 777
							)
					{
						transactions.add(transaction);
					}
					// get reference to parent record for this account
					//timestampRef = transaction.getReference();
					timestampRef = account.getLastTimestamp();
					// get signature for account + time
					signatureBytes = dcSet.getAddressTime_SignatureMap().get(address, timestampRef);

				}while(true);

				int count = transactions.size();

				output.put("count", count);

				int i = 0;
				for (Transaction transaction : transactions) {
					output.put(count - i, jsonUnitPrint(transaction, assetNames));
					i++;
				}
			}

			output.put("assetNames", assetNames.getMap());

		} catch (Exception e1) {
			output=new LinkedHashMap();
			output.put("error", e1.getLocalizedMessage());
		}
		return output;
	}

	public Map jsonQueryAssetsLite()
	{
		Map output=new LinkedHashMap();

		Collection<ItemCls> items = Controller.getInstance().getAllItems(ItemCls.ASSET_TYPE);

		for (ItemCls item : items) {
			output.put(item.getKey(), item.getName());
		}

		return output;
	}

	public Map jsonQueryAssets()
	{
		Map output=new LinkedHashMap();

		Collection<ItemCls> items = Controller.getInstance().getAllItems(ItemCls.ASSET_TYPE);

		for (ItemCls item : items) {

			AssetCls asset = (AssetCls) item;

			Map assetJSON=new LinkedHashMap();

			assetJSON.put("key", asset.getKey());
			assetJSON.put("name", asset.getName());
			assetJSON.put("description", asset.getDescription());
			//assetJSON.put("description", asset.getDescription());
			assetJSON.put("owner", asset.getOwner().getAddress());
			assetJSON.put("quantity", NumberAsString.getInstance().numberAsString( asset.getTotalQuantity(dcSet)));
			assetJSON.put("scale", asset.getScale());
			//String a =  Lang.getInstance().translate_from_langObj("False",langObj);
			//if (asset.isDivisible()) a =  Lang.getInstance().translate_from_langObj("True",langObj);
			//assetJSON.put("isDivisible", a);
			assetJSON.put("assetType", Lang.getInstance().translate_from_langObj(asset.viewAssetType(), langObj));
			//a =  Lang.getInstance().translate_from_langObj("False",langObj);
			//if (asset.isMovable()) a =  Lang.getInstance().translate_from_langObj("True",langObj);
			//assetJSON.put("isMovable", a);

			assetJSON.put("img", Base64.encodeBase64String(asset.getImage()));
			assetJSON.put("icon", Base64.encodeBase64String(asset.getIcon()));
			List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = dcSet.getOrderMap().getOrders(asset.getKey());
			List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades = dcSet.getTradeMap().getTrades(asset.getKey());

			assetJSON.put("operations", orders.size() + trades.size());

			output.put(asset.getKey(), assetJSON);


		}



		return output;
	}

	public Map jsonQueryAssets(int start)
	{
		Map output=new LinkedHashMap();


		SortableList<Long, ItemCls> it = dcSet.getItemAssetMap().getList();


		int view_Row = 21;
		int end = start + view_Row;
		if (end > it.size()) end = it.size();

		output.put("start_row", start);
		int i ;
		Map assetsJSON=new LinkedHashMap();
		for (i=start ; i< end; i++){

			AssetCls asset = (AssetCls) it.get(i).getB();

			//	 }
			//	while (ItemCls item : items) {

			//		AssetCls asset = (AssetCls) item;

			Map assetJSON=new LinkedHashMap();

			assetJSON.put("key", asset.getKey());
			assetJSON.put("name", asset.getName());
			assetJSON.put("description", asset.getDescription());
			assetJSON.put("owner", asset.getOwner().getAddress());
			assetJSON.put("quantity", NumberAsString.getInstance().numberAsString( asset.getTotalQuantity(dcSet)));
			assetJSON.put("scale", asset.getScale());
			//String a =  Lang.getInstance().translate_from_langObj("False",langObj);
			//if (asset.isDivisible()) a =  Lang.getInstance().translate_from_langObj("True",langObj);
			//assetJSON.put("isDivisible", a);
			assetJSON.put("assetType", Lang.getInstance().translate_from_langObj(asset.viewAssetType(), langObj));
			//a =  Lang.getInstance().translate_from_langObj("False",langObj);
			//if (asset.isMovable()) a =  Lang.getInstance().translate_from_langObj("True",langObj);
			//assetJSON.put("isMovable", a);

			assetJSON.put("img", Base64.encodeBase64String(asset.getImage()));
			assetJSON.put("icon", Base64.encodeBase64String(asset.getIcon()));
			List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = dcSet.getOrderMap().getOrders(asset.getKey());
			List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades = dcSet.getTradeMap().getTrades(asset.getKey());

			assetJSON.put("operations", orders.size() + trades.size());

			assetsJSON.put(asset.getKey(), assetJSON);


		}
		output.put("assets", assetsJSON);
		output.put("maxHeight",it.size());
		output.put("row", i);
		output.put("view_Row", view_Row);
		output.put("label_Title",  Lang.getInstance().translate_from_langObj("Assets",langObj));
		output.put("label_table_key", Lang.getInstance().translate_from_langObj("Key",langObj));
		output.put("label_table_asset_name", Lang.getInstance().translate_from_langObj("Name",langObj));
		output.put("label_table_asset_creator", Lang.getInstance().translate_from_langObj("Creator",langObj));
		output.put("label_table_asset_movable", Lang.getInstance().translate_from_langObj("Movable",langObj));
		output.put("label_table_asset_description", Lang.getInstance().translate_from_langObj("Description",langObj));
		output.put("label_table_asset_divisible", Lang.getInstance().translate_from_langObj("Divisible",langObj));
		output.put("label_table_asset_amount", Lang.getInstance().translate_from_langObj("Amount",langObj));
		output.put("Label_Later", Lang.getInstance().translate_from_langObj(">>",langObj));
		output.put("Label_Previous", Lang.getInstance().translate_from_langObj("<<",langObj));








		return output;
	}

	public Map jsonQuerySearchAssets(String search)
	{
		Map output=new LinkedHashMap();



		List<ItemCls> listAssets = new ArrayList();
		if (search != ""){

			if (search.matches("\\d+") && dcSet.getItemAssetMap().contains(Long.valueOf(search))){
				listAssets .add(dcSet.getItemAssetMap().get(Long.valueOf(search)));
			}else{
				listAssets = dcSet.getItemAssetMap().get_By_Name(search, false);
			}
		}





		int view_Row = listAssets.size();
		int end = 0 + view_Row;
		if (end > listAssets.size()) end = listAssets.size();

		output.put("start_row", 0);
		int i ;
		Map assetsJSON=new LinkedHashMap();
		for ( ItemCls asset1:listAssets){

			AssetCls asset = (AssetCls) asset1;


			Map assetJSON=new LinkedHashMap();

			assetJSON.put("key", asset.getKey());
			assetJSON.put("name", asset.getName());
			assetJSON.put("description", asset.getDescription());
			assetJSON.put("owner", asset.getOwner().getAddress());
			assetJSON.put("quantity", NumberAsString.getInstance().numberAsString( asset.getTotalQuantity(dcSet)));
			assetJSON.put("scale", asset.getScale());
			//String a =  Lang.getInstance().translate_from_langObj("False",langObj);
			//if (asset.isDivisible()) a =  Lang.getInstance().translate_from_langObj("True",langObj);
			//assetJSON.put("isDivisible", a);
			assetJSON.put("assetType", Lang.getInstance().translate_from_langObj(asset.viewAssetType(),langObj));
			//a =  Lang.getInstance().translate_from_langObj("False",langObj);
			//if (asset.isMovable()) a =  Lang.getInstance().translate_from_langObj("True",langObj);
			//assetJSON.put("isMovable", a);

			assetJSON.put("img", Base64.encodeBase64String(asset.getImage()));
			assetJSON.put("icon", Base64.encodeBase64String(asset.getIcon()));
			List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = dcSet.getOrderMap().getOrders(asset.getKey());
			List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades = dcSet.getTradeMap().getTrades(asset.getKey());

			assetJSON.put("operations", orders.size() + trades.size());

			assetsJSON.put(asset.getKey(), assetJSON);


		}
		output.put("assets", assetsJSON);
		output.put("maxHeight",listAssets.size());
		output.put("row", listAssets.size());
		output.put("view_Row", view_Row);
		output.put("label_Title",  Lang.getInstance().translate_from_langObj("Assets",langObj));
		output.put("label_table_key", Lang.getInstance().translate_from_langObj("Key",langObj));
		output.put("label_table_asset_name", Lang.getInstance().translate_from_langObj("Name",langObj));
		output.put("label_table_asset_creator", Lang.getInstance().translate_from_langObj("Creator",langObj));
		output.put("label_table_asset_movable", Lang.getInstance().translate_from_langObj("Movable",langObj));
		output.put("label_table_asset_description", Lang.getInstance().translate_from_langObj("Description",langObj));
		output.put("label_table_asset_divisible", Lang.getInstance().translate_from_langObj("Divisible",langObj));
		output.put("label_table_asset_amount", Lang.getInstance().translate_from_langObj("Amount",langObj));
		output.put("Label_Later", Lang.getInstance().translate_from_langObj(">>",langObj));
		output.put("Label_Previous", Lang.getInstance().translate_from_langObj("<<",langObj));








		return output;
	}


	public Map jsonQueryATs()
	{
		Map output=new LinkedHashMap();

		Iterable<String> ids = dcSet.getATMap().getATsLimited(100);

		Iterator<String> iter = ids.iterator();
		while (iter.hasNext())
		{
			String atAddr = iter.next();

			AT at = dcSet.getATMap().getAT(atAddr);

			output.put(atAddr, at.toJSON());
		}

		return output;
	}

	public Map jsonQueryPools(String asset_1)
	{
		Map lastPools = new LinkedHashMap();;
		Map output=new LinkedHashMap();

		Long asset_g;
		if ( asset_1 == null) {
			asset_g =(long) 1;
		}else{
			asset_g = Long.valueOf(asset_1);
		}

		List<Poll> pools = new ArrayList< Poll > (dcSet.getPollMap().getValuesAll());

		if(pools.isEmpty())
		{
			output.put("error", "There is no Polls.");
			return output;
		}

		//SCAN
		int back = 815; // 3*24*60*60/318 = 815 // 3 days
		//back = 40815;
		Pair<Block, List<Transaction>> result = Controller.getInstance().scanTransactions(Controller.getInstance().getBlockByHeight(getHeight()-back), back, 100, Transaction.CREATE_POLL_TRANSACTION, -1, null);

		for(Transaction transaction: result.getB())
		{
			lastPools.put(((CreatePollTransaction)transaction).getPoll().getName(), true);
		}

		Comparator<Poll> comparator = new Comparator<Poll>() {
			@Override
			public int compare(Poll c1, Poll c2) {

				BigDecimal c1votes = c1.getTotalVotes(asset_g);
				BigDecimal c2votes = c2.getTotalVotes(asset_g);

				return c2votes.compareTo(c1votes);
			}
		};

		Collections.sort(pools, comparator);

		Map poolsJSON=new LinkedHashMap();

		for (Poll pool : pools) {
			Map poolJSON=new LinkedHashMap();

			poolJSON.put( "totalVotes",  pool.getTotalVotes(asset_g).toPlainString() );

			poolJSON.put( "new",  lastPools.containsKey(pool.getName()) );

			poolsJSON.put(pool.getName(), poolJSON);
		}

		output.put("pools", poolsJSON);

		Map assets1 = jsonQueryAssets();
		output.put("assets",assets1);

		return output;
	}

	public Map jsonQueryPool(String query, String asset_1)
	{

		Long asset_q = Long.valueOf(asset_1);

		Map output = new LinkedHashMap();

		Poll poll = Controller.getInstance().getPoll(query);

		Map pollJSON = new LinkedHashMap();

		pollJSON.put("creator", poll.getCreator().getAddress());
		pollJSON.put("name", poll.getName());
		pollJSON.put("description", poll.getDescription());
		pollJSON.put("totalVotes", poll.getTotalVotes(asset_q).toPlainString());


		List<Transaction> transactions = dcSet.getTransactionFinalMap().getTransactionsByTypeAndAddress(poll.getCreator().getAddress(), Transaction.CREATE_POLL_TRANSACTION, 0);
		for (Transaction transaction : transactions) {
			CreatePollTransaction createPollTransaction = ((CreatePollTransaction)transaction);
			if(createPollTransaction.getPoll().getName().equals(poll.getName()))
			{
				pollJSON.put("timestamp", createPollTransaction.getTimestamp());
				pollJSON.put("dateTime", BlockExplorer.timestampToStr(createPollTransaction.getTimestamp()));
				break;
			}
		}

		Map optionsJSON = new LinkedHashMap();
		for(PollOption option: poll.getOptions())
		{
			optionsJSON.put(option.getName(), option.getVotes(asset_q).toPlainString());
		}
		pollJSON.put("options", optionsJSON);

		Comparator<Pair<Account, PollOption>> comparator = new Comparator<Pair<Account, PollOption>>() {
			@Override
			public int compare(Pair<Account, PollOption> c1, Pair<Account, PollOption> c2) {

				BigDecimal c1votes = c1.getA().getBalanceUSE(asset_q);
				BigDecimal c2votes = c2.getA().getBalanceUSE(asset_q);

				return c2votes.compareTo(c1votes);
			}
		};


		Map votesJSON = new LinkedHashMap();

		List<Pair<Account, PollOption>> votes = poll.getVotes();

		Collections.sort(votes, comparator);

		for(Pair<Account, PollOption> vote: votes)
		{
			Map voteJSON = new LinkedHashMap();
			voteJSON.put("option", vote.getB().getName());
			voteJSON.put("votes", vote.getA().getBalanceUSE(asset_q).toPlainString());

			votesJSON.put(vote.getA().getAddress(), voteJSON);
		}
		pollJSON.put("votes", votesJSON);

		output.put("pool", pollJSON);

		return output;
	}

	public Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>>
	calcForAsset(List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders,
			List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades)
	{

		Map<Long, Integer> pairsOpenOrders = new TreeMap<Long, Integer>();
		Map<Long, BigDecimal> volumePriceOrders = new TreeMap<Long, BigDecimal>();
		Map<Long, BigDecimal> volumeAmountOrders = new TreeMap<Long, BigDecimal>();

		int count;
		BigDecimal volumePrice =  BigDecimal.ZERO;
		BigDecimal volumeAmount =  BigDecimal.ZERO;

		for (Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order : orders)
		{
			if(!pairsOpenOrders.containsKey(order.c.a))
			{
				count = 0;
			}
			else
			{
				count = pairsOpenOrders.get(order.c.a);
			}

			if(!volumeAmountOrders.containsKey(order.c.a))
			{
				volumeAmount =  BigDecimal.ZERO;
			}
			else
			{
				volumeAmount =  volumeAmountOrders.get(order.c.b);
			}

			if(!volumePriceOrders.containsKey(order.b.a))
			{
				volumePrice =  BigDecimal.ZERO;
			}
			else
			{
				volumePrice =  volumePriceOrders.get(order.c.b);
			}

			count ++;
			pairsOpenOrders.put(order.c.a, count);

			volumeAmount = volumeAmount.add(order.b.c);

			volumeAmountOrders.put(order.c.a, volumeAmount);

			volumePriceOrders.put(order.c.a, volumePrice);

			if(!pairsOpenOrders.containsKey(order.b.a))
			{
				count = 0;
			}
			else
			{
				count = pairsOpenOrders.get(order.b.a);
			}

			if(!volumePriceOrders.containsKey(order.b.a))
			{
				volumePrice =  BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
			}
			else
			{
				volumePrice =  volumePriceOrders.get(order.b.a);
			}

			if(!volumeAmountOrders.containsKey(order.b.a))
			{
				volumeAmount =  BigDecimal.ZERO;
			}
			else
			{
				volumeAmount =  volumeAmountOrders.get(order.b.a);
			}

			count ++;
			pairsOpenOrders.put(order.b.a, count);

			volumePrice = volumePrice.add(order.b.b);

			volumePriceOrders.put(order.b.a, volumePrice);

			volumeAmountOrders.put(order.b.a, volumeAmount);
		}

		Map<Long, Integer> pairsTrades = new TreeMap<Long, Integer>();
		Map<Long, BigDecimal> volumePriceTrades = new TreeMap<Long, BigDecimal>();
		Map<Long, BigDecimal> volumeAmountTrades = new TreeMap<Long, BigDecimal>();

		for (Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> trade : trades)
		{

			Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> initiator = Order.getOrder(dcSet, trade.a);
			if(!pairsTrades.containsKey(initiator.c.a) )
			{
				count = 0;
				volumePrice =  BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
				volumeAmount =  BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
			}
			else
			{
				count = pairsTrades.get(initiator.c.a);
				volumePrice =  volumePriceTrades.get(initiator.c.a);
				volumeAmount =  volumeAmountTrades.get(initiator.c.a);
			}

			count ++;
			pairsTrades.put(initiator.c.a, count);

			volumePrice = volumePrice.add(trade.d);
			volumeAmount = volumeAmount.add(trade.c);

			volumePriceTrades.put(initiator.c.a, volumePrice);
			volumeAmountTrades.put(initiator.c.a, volumeAmount);

			Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> target = Order.getOrder(dcSet, trade.b);
			if(!pairsTrades.containsKey(target.c.a))
			{
				count = 0;
				volumePrice =  BigDecimal.ZERO;
				volumeAmount =  BigDecimal.ZERO; //.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
			}
			else
			{
				count = pairsTrades.get(target.c.a);
				volumePrice =  volumePriceTrades.get(target.c.a);
				volumeAmount =  volumeAmountTrades.get(target.c.a);
			}

			count ++;
			pairsTrades.put(target.c.a, count);

			volumePrice = volumePrice.add(trade.c);
			volumeAmount = volumeAmount.add(trade.d);

			volumePriceTrades.put(target.c.a, volumePrice);
			volumeAmountTrades.put(target.c.a, volumeAmount);
		}

		Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> all =
				new TreeMap<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>>();

		for(Map.Entry<Long, Integer> pair : pairsOpenOrders.entrySet())
		{
			all.put(pair.getKey(),
					Fun.t6(
							pair.getValue(),
							0,
							volumePriceOrders.get(pair.getKey()), volumeAmountOrders.get(pair.getKey()),
							BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE)
							)
					);
		}

		for(Map.Entry<Long, Integer> pair : pairsTrades.entrySet())
		{

			if(all.containsKey(pair.getKey()))
			{
				all.put(
						pair.getKey(), Fun.t6(
								all.get(pair.getKey()).a,
								pair.getValue(),
								all.get(pair.getKey()).c,
								all.get(pair.getKey()).d,
								volumePriceTrades.get(pair.getKey()),
								volumeAmountTrades.get(pair.getKey())
								)
						);
			}
			else
			{
				all.put(
						pair.getKey(), Fun.t6(
								0,
								pair.getValue(),
								BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
								BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
								volumePriceTrades.get(pair.getKey()),
								volumeAmountTrades.get(pair.getKey())
								)
						);
			}
		}

		return all;
	}

	public Map jsonQueryAsset(long key)
	{
		Map output=new LinkedHashMap();

		List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = dcSet.getOrderMap().getOrders(key);

		List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades = dcSet.getTradeMap().getTrades(key);

		AssetCls asset = Controller.getInstance().getAsset(key);

		Map assetJSON=new LinkedHashMap();

		assetJSON.put("key", asset.getKey());
		assetJSON.put("name", asset.getName());
		assetJSON.put("description", asset.getDescription());
		assetJSON.put("owner", asset.getOwner().getAddress());
		assetJSON.put("quantity", asset.getQuantity());
		assetJSON.put("scale", asset.getScale());
		//String a =  Lang.getInstance().translate_from_langObj("False",langObj);
		//if (asset.isDivisible()) a =  Lang.getInstance().translate_from_langObj("True",langObj);
		//assetJSON.put("isDivisible", a);
		assetJSON.put("assetType", Lang.getInstance().translate_from_langObj(asset.viewAssetType(), langObj));
		//a =  Lang.getInstance().translate_from_langObj("False",langObj);
		//if (asset.isMovable()) a =  Lang.getInstance().translate_from_langObj("True",langObj);
		//assetJSON.put("isMovable", a);
		assetJSON.put("img", Base64.encodeBase64String(asset.getImage()));
		assetJSON.put("icon", Base64.encodeBase64String(asset.getIcon()));


		List<Transaction> transactions = dcSet.getTransactionFinalMap().getTransactionsByTypeAndAddress(asset.getOwner().getAddress(), Transaction.ISSUE_ASSET_TRANSACTION, 0);
		for (Transaction transaction : transactions) {
			IssueAssetTransaction issueAssetTransaction = ((IssueAssetTransaction)transaction);
			if(issueAssetTransaction.getItem().getName().equals(asset.getName()))
			{
				assetJSON.put("timestamp", issueAssetTransaction.getTimestamp());
				assetJSON.put("dateTime", BlockExplorer.timestampToStr(issueAssetTransaction.getTimestamp()));
				break;
			}
		}


		output.put("this", assetJSON);

		output.put("totalOpenOrdersCount", orders.size());
		output.put("totalTradesCount", trades.size());

		Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> all = calcForAsset(orders, trades);

		if(all.containsKey(key))
		{
			output.put("totalOrdersVolume", all.get(key).c.toPlainString());
		}
		else
		{
			output.put("totalOrdersVolume", BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE).toPlainString());
		}

		if(all.containsKey(key))
		{
			output.put("totalTradesVolume", all.get(key).f.toPlainString());
		}
		else
		{
			output.put("totalTradesVolume", BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE).toPlainString());
		}

		Map pairsJSON=new LinkedHashMap();

		pairsJSON=new LinkedHashMap();
		for(Map.Entry<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> pair : all.entrySet())
		{
			if(pair.getKey() == key)
			{
				continue;
			}
			AssetCls assetWant = Controller.getInstance().getAsset(pair.getKey());

			Map pairJSON = new LinkedHashMap();
			pairJSON.put("openOrdersCount", pair.getValue().a);
			pairJSON.put("tradesCount", pair.getValue().b);
			pairJSON.put("sum", pair.getValue().a + pair.getValue().b);
			pairJSON.put("ordersPriceVolume", pair.getValue().c.toPlainString());
			pairJSON.put("ordersAmountVolume", pair.getValue().d.toPlainString());
			pairJSON.put("tradesPriceVolume", pair.getValue().e.toPlainString());
			pairJSON.put("tradeAmountVolume", pair.getValue().f.toPlainString());
			pairJSON.put("asset", pair.getKey());
			pairJSON.put("assetName", assetWant.getName());
			pairJSON.put("description", assetWant.getDescription());
			pairsJSON.put(pair.getKey(), pairJSON);
		}

		output.put("pairs", pairsJSON);
		output.put("label_Asset", Lang.getInstance().translate_from_langObj("Asset", langObj));
		output.put("label_Key", Lang.getInstance().translate_from_langObj("Key", langObj));
		output.put("label_Creator", Lang.getInstance().translate_from_langObj("Creator", langObj));
		output.put("label_Description", Lang.getInstance().translate_from_langObj("Description", langObj));
		output.put("label_Scale", Lang.getInstance().translate_from_langObj("Accuracy", langObj));
		output.put("label_AssetType", Lang.getInstance().translate_from_langObj("TYPE", langObj));
		output.put("label_Quantity", Lang.getInstance().translate_from_langObj("Quantity", langObj));
		output.put("label_Holders", Lang.getInstance().translate_from_langObj("Holders", langObj));
		output.put("label_Available_pairs", Lang.getInstance().translate_from_langObj("Available pairs", langObj));
		output.put("label_Pair", Lang.getInstance().translate_from_langObj("Pair", langObj));
		output.put("label_Orders_Count", Lang.getInstance().translate_from_langObj("Orders Count", langObj));
		output.put("label_Open_Orders_Volume", Lang.getInstance().translate_from_langObj("Open Orders Volume", langObj));
		output.put("label_Trades_Count", Lang.getInstance().translate_from_langObj("Trades Count", langObj));
		output.put("label_Trades_Volume", Lang.getInstance().translate_from_langObj("Trades Volume", langObj));
		output.put("label_Total", Lang.getInstance().translate_from_langObj("Total", langObj));
		output.put("label_View", Lang.getInstance().translate_from_langObj("View", langObj));


		return output;
	}

	public Map jsonQueryTrades(long have, long want)
	{
		Map output=new LinkedHashMap();

		List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> ordersHave = dcSet.getOrderMap().getOrders(have, want, false);
		List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> ordersWant = dcSet.getOrderMap().getOrders(want, have, true);

		//Collections.reverse(ordersWant);

		List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades = dcSet.getTradeMap().getTrades(have, want);

		AssetCls assetHave = Controller.getInstance().getAsset(have);
		AssetCls assetWant = Controller.getInstance().getAsset(want);

		output.put("assetHaveOwner", assetHave.getOwner().getAddress());
		output.put("assetWantOwner", assetWant.getOwner().getAddress());

		output.put("assetHave", assetHave.getKey());
		output.put("assetHaveName", assetHave.getName());
		output.put("assetWant", assetWant.getKey());
		output.put("assetWantName", assetWant.getName());

		Map sellsJSON = new LinkedHashMap();
		Map buysJSON = new LinkedHashMap();


		BigDecimal sumAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		BigDecimal sumAmountGood = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

		BigDecimal sumSellingAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		BigDecimal sumSellingAmountGood = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

		for (Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order : ordersHave)
		{
			Map sellJSON = new LinkedHashMap();

			sellJSON.put("price", order.a.e.toPlainString());
			sellJSON.put("amount", order.b.c.toPlainString());
			sumAmount = sumAmount.add(order.b.c);

			sellJSON.put("sellingPrice", Order.calcPrice(order.c.b, order.b.b).toPlainString());

			BigDecimal sellingAmount = Order.getAmountWantLeft(order);

			sellJSON.put("sellingAmount", sellingAmount.toPlainString());

			boolean good = Order.isGoodIncrement(order, order);

			sellJSON.put("good", good);

			if(good)
			{
				sumAmountGood = sumAmountGood.add(order.b.b);

				sumSellingAmountGood = sumSellingAmountGood.add(sellingAmount);
			}

			sumSellingAmount = sumSellingAmount.add(sellingAmount);

			sellsJSON.put(Base58.encode(order.a.a), sellJSON);
		}

		output.put("sells", sellsJSON);

		output.put("sellsSumAmount", sumAmount.toPlainString());
		output.put("sellsSumAmountGood", sumAmountGood.toPlainString());
		output.put("sellsSumTotal", sumSellingAmount.toPlainString());
		output.put("sellsSumTotalGood", sumSellingAmountGood.toPlainString());

		sumAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		sumAmountGood = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

		BigDecimal sumBuyingAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		BigDecimal sumBuyingAmountGood = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

		for (Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order : ordersWant)
		{
			Map buyJSON = new LinkedHashMap();

			buyJSON.put("price", order.a.e.toPlainString());
			buyJSON.put("amount", order.b.c.toPlainString());

			sumAmount = sumAmount.add(order.b.c);

			buyJSON.put("buyingPrice", Order.calcPrice(order.c.b, order.b.b).toPlainString());

			BigDecimal buyingAmount = Order.calcAmountWantLeft(order);

			buyJSON.put("buyingAmount", buyingAmount.toPlainString());

			boolean good = Order.isGoodIncrement(order, order);

			buyJSON.put("good", good);

			if(good)
			{
				sumBuyingAmountGood = sumBuyingAmountGood.add(buyingAmount);
				sumAmountGood = sumAmountGood.add(order.b.c);
			}

			sumBuyingAmount = sumBuyingAmount.add(buyingAmount);

			buysJSON.put(Base58.encode(order.a.a), buyJSON);
		}
		output.put("buys", buysJSON);

		output.put("buysSumAmount", sumBuyingAmount.toPlainString());
		output.put("buysSumAmountGood", sumBuyingAmountGood.toPlainString());
		output.put("buysSumTotal", sumAmount.toPlainString());
		output.put("buysSumTotalGood", sumAmountGood.toPlainString());

		Map tradesJSON = new LinkedHashMap();

		output.put("tradesCount", trades.size());

		BigDecimal tradeWantAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		BigDecimal tradeHaveAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

		int i = 0;
		for (Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> trade : trades)
		{
			i++;
			Map tradeJSON = new LinkedHashMap();

			Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> orderInitiator = Order.getOrder(dcSet, trade.a);

			Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> orderTarget = Order.getOrder(dcSet, trade.b);

			tradeJSON.put("amountHave", trade.c.toPlainString());
			tradeJSON.put("amountWant", trade.d.toPlainString());

			tradeJSON.put("realPrice", Order.calcPrice(trade.c, trade.d));
			tradeJSON.put("realReversePrice", Order.calcPrice(trade.d, trade.c));

			tradeJSON.put("initiatorTxSignature", Base58.encode(orderInitiator.a.a));

			tradeJSON.put("initiatorCreator", orderInitiator.a.b);
			tradeJSON.put("initiatorAmount", orderInitiator.b.b.toPlainString());
			if(orderInitiator.b.a == have)
			{
				tradeJSON.put("type", Lang.getInstance().translate_from_langObj("Sell", langObj));
				tradeWantAmount = tradeWantAmount.add(trade.c);
				tradeHaveAmount = tradeHaveAmount.add(trade.d);

			}
			else
			{
				tradeJSON.put("type",Lang.getInstance().translate_from_langObj("Buy", langObj));

				tradeHaveAmount = tradeHaveAmount.add(trade.c);
				tradeWantAmount = tradeWantAmount.add(trade.d);
			}
			tradeJSON.put("targetTxSignature", Base58.encode(orderTarget.a.a));
			tradeJSON.put("targetCreator", orderTarget.a.b);
			tradeJSON.put("targetAmount", orderTarget.b.b.toPlainString());

			tradeJSON.put("timestamp", trade.e);
			tradeJSON.put("dateTime", BlockExplorer.timestampToStr(trade.e));

			tradesJSON.put(i, tradeJSON);
		}
		output.put("trades", tradesJSON);

		output.put("tradeWantAmount", tradeWantAmount.toPlainString());
		output.put("tradeHaveAmount", tradeHaveAmount.toPlainString());

		output.put("label_Trades", Lang.getInstance().translate_from_langObj("Trades", langObj));
		output.put("label_Price", Lang.getInstance().translate_from_langObj("Price", langObj));
		output.put("label_Amount", Lang.getInstance().translate_from_langObj("Amount", langObj));
		output.put("label_Sell_Orders", Lang.getInstance().translate_from_langObj("Sell Orders", langObj));
		output.put("label_Buy_Orders", Lang.getInstance().translate_from_langObj("Buy Orders", langObj));
		output.put("label_Total", Lang.getInstance().translate_from_langObj("Total", langObj));
		output.put("label_Trade_History", Lang.getInstance().translate_from_langObj("Trade History", langObj));
		output.put("label_Date", Lang.getInstance().translate_from_langObj("Date", langObj));
		output.put("label_Type", Lang.getInstance().translate_from_langObj("Type", langObj));
		output.put("label_Trade_Volume", Lang.getInstance().translate_from_langObj("Trade Volume", langObj));
		output.put("label_Go_To", Lang.getInstance().translate_from_langObj("Go To", langObj));

		return output;
	}

	public Map jsonQueryNames()
	{
		Map output=new LinkedHashMap();
		Map namesJSON=new LinkedHashMap();

		Collection<Name> names = dcSet.getNameMap().getValuesAll();

		for (Name name : names) {
			namesJSON.put(name.toString(), name.getOwner().getAddress());
		}

		output.put("names", namesJSON);
		output.put("count", names.size());

		return output;
	}

	public Map jsonQueryBlocks(int start)
	{
		Block block = null;
		if(start > 0)
		{
			block = Controller.getInstance().getBlockByHeight(start);
		}

		if (block == null)
		{
			block = getLastBlock();
			start = block.getHeight(dcSet);
		}

		Map output=new LinkedHashMap();

		output.put("maxHeight", block.getHeight(dcSet));

		//long startTime = System.currentTimeMillis();
		output.put("unconfirmedTxs", dcSet.getTransactionMap().size());
		//LOGGER.debug("unconfCount time: " +  (System.currentTimeMillis() - startTime)*0.001);
		//startTime = System.currentTimeMillis();
		//output.put("totaltransactions", dcSet.getTransactionRef_BlockRef_Map().size());
		output.put("totaltransactions", dcSet.getTxCounter());
		//LOGGER.debug("refsCount time: " +  (System.currentTimeMillis() - startTime)*0.001);
		//startTime = System.currentTimeMillis();
		//output.put("totaltransactions", dcSet.getTransactionFinalMap().size());
		//LOGGER.debug("finalCount time: " +  (System.currentTimeMillis() - startTime)*0.001);

		// TODO translate_web(

		output.put("Label_Unconfirmed_transactions",Lang.getInstance().translate_from_langObj("Unconfirmed transactions", langObj));
		output.put("Label_total_transactions",Lang.getInstance().translate_from_langObj("Total Transactions", langObj));
		output.put("Label_Height", Lang.getInstance().translate_from_langObj("Height", langObj));
		output.put("Label_Time", Lang.getInstance().translate_from_langObj("Time", langObj));
		output.put("Label_Generator", Lang.getInstance().translate_from_langObj("Creator", langObj));
		output.put("Label_Gen_balance", Lang.getInstance().translate_from_langObj("Gen.Balance", langObj));
		output.put("Label_TXs",  Lang.getInstance().translate_from_langObj("TXs", langObj));
		output.put("Label_Fee", Lang.getInstance().translate_from_langObj("Height", langObj));
		output.put("Label_AT_Amount", Lang.getInstance().translate_from_langObj("AT Amount", langObj));
		output.put("Label_Amount", Lang.getInstance().translate_from_langObj("Amount", langObj));
		output.put("Label_Later", Lang.getInstance().translate_from_langObj("Later", langObj));
		output.put("Label_Previous",  Lang.getInstance().translate_from_langObj("Previous", langObj));

		int counter = start;

		do{
			Map blockJSON=new LinkedHashMap();
			blockJSON.put("height", counter);
			blockJSON.put("signature", Base58.encode(block.getSignature()));
			blockJSON.put("generator", block.getCreator().getAddress());
			blockJSON.put("generatingBalance", block.getForgingValue());
			//blockJSON.put("winValue", block.calcWinValue(dcSet.getInstance()));
			blockJSON.put("winValueTargetted", block.calcWinValueTargeted(dcSet));
			blockJSON.put("transactionCount", block.getTransactionCount());
			blockJSON.put("timestamp", block.getTimestamp(dcSet));
			blockJSON.put("dateTime", BlockExplorer.timestampToStr(block.getTimestamp(dcSet)));
			blockJSON.put("totalFee", block.getTotalFee().toPlainString());


			BigDecimal totalAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
			for (Transaction transaction : block.getTransactions()) {
				for (Account account : transaction.getInvolvedAccounts()) {
					BigDecimal amount = transaction.getAmount(account);
					if(amount.compareTo(BigDecimal.ZERO) > 0)
					{
						totalAmount = totalAmount.add(amount);
					}
				}
			}

			blockJSON.put("totalAmount", totalAmount.toPlainString());

			LinkedHashMap< Tuple2<Integer, Integer> , AT_Transaction> aTtxs = dcSet.getATTransactionMap().getATTransactions(counter);

			BigDecimal totalATAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

			for(Map.Entry<Tuple2<Integer, Integer> , AT_Transaction> e : aTtxs.entrySet())
			{
				totalATAmount = totalATAmount.add(BigDecimal.valueOf( e.getValue().getAmount() , BlockChain.AMOUNT_DEDAULT_SCALE));
			}

			blockJSON.put("totalATAmount", totalATAmount.toPlainString());
			//blockJSON.put("aTfee", block.getATfee().toPlainString());

			output.put(counter, blockJSON);

			counter --;
			block = block.getParent(dcSet);
		}
		while(block != null && counter >= start - 20);


		return output;
	}


	private Map jsonQueryPerson(String first) {
		// TODO Auto-generated method stub
		Map output=new LinkedHashMap();
		PersonCls person = (PersonCls) dcSet.getItemPersonMap().get(new Long(first));
		byte[] b = person.getImage();
		String a = Base64.encodeBase64String(b);

		output.put("Label_key", Lang.getInstance().translate_from_langObj("Key",langObj));
		output.put("Label_name", Lang.getInstance().translate_from_langObj("Name",langObj));
		output.put("Label_creator", Lang.getInstance().translate_from_langObj("Creator",langObj));
		output.put("Label_born", Lang.getInstance().translate_from_langObj("Birthday",langObj));
		output.put("Label_gender", Lang.getInstance().translate_from_langObj("Gender",langObj));
		output.put("Label_description", Lang.getInstance().translate_from_langObj("Description",langObj));


		output.put("img", a);
		output.put("key", person.getKey());
		output.put("creator", person.getOwner().getPersonAsString());

		if (person.getOwner().getPerson() != null){
			output.put("creator_key", person.getOwner().getPerson().b.getKey());
			output.put("creator_name", person.getOwner().getPerson().b.getName());
		}else{
			output.put("creator_key", "");
			output.put("creator_name", "");
		}


		output.put("name", person.getName());
		////////output.put("birthday", df.format(new Date(person.getBirthday())).toString());
		output.put("birthday", person.getBirthdayStr());
		output.put("description", person.getDescription());

		String gender = Lang.getInstance().translate_from_langObj("Man",langObj);
		if (person.getGender() != 0) gender = Lang.getInstance().translate_from_langObj("Woman",langObj);
		output.put("gender", gender);

		// statuses
		output.put("Label_statuses", Lang.getInstance().translate_from_langObj("Statuses",langObj));
		output.put("Label_Status_table_status", Lang.getInstance().translate_from_langObj("Status",langObj));
		output.put("Label_Status_table_data", Lang.getInstance().translate_from_langObj("Date",langObj));


		Map statusesJSON=new LinkedHashMap();

		WEB_PersonStatusesModel statusModel = new WEB_PersonStatusesModel (person.getKey());
		int rowCount = statusModel.getRowCount();
		if (rowCount > 0 ){
			for (int i = 0; i<rowCount; i++){
				Map statusJSON=new LinkedHashMap();
				statusJSON.put("status_name", statusModel.getValueAt(i, statusModel.COLUMN_STATUS_NAME));
				statusJSON.put("status_data", statusModel.getValueAt(i, statusModel.COLUMN_MAKE_DATA));
				Object creat = statusModel.getValueAt(i, statusModel.COLUMN_MAKER);

				if (!creat.equals("")){
					PersonCls pers = statusModel.getCreatorPerson(i);
					statusJSON.put("status_creator", creat.toString());
					statusJSON.put("status_creator_key", pers.getKey());
					statusJSON.put("status_creator_name",pers.getName());

					//	PersonCls pp = (PersonCls) statusModel.getValueAt(i, statusModel.COLUMN_MAKER);
					//	statusJSON.put("status_creator_name", pp.getName());
					//	statusJSON.put("status_creator_key", pp.getKey());
				} else {
					statusJSON.put("status_creator", "");
					statusJSON.put("status_creator_key", "");
					statusJSON.put("status_creator_name","");
				}



				statusesJSON.put(i, statusJSON);
			}



			output.put("statuses", statusesJSON);
		}
		// accounts
		output.put("Label_accounts", Lang.getInstance().translate_from_langObj("Accounts",langObj));
		output.put("Label_accounts_table_adress", Lang.getInstance().translate_from_langObj("Address",langObj));
		output.put("Label_accounts_table_data", Lang.getInstance().translate_from_langObj("Date",langObj));
		output.put("Label_accounts_table_creator", Lang.getInstance().translate_from_langObj("Creator",langObj));

		Map accountsJSON=new LinkedHashMap();

		PersonAccountsModel personModel = new PersonAccountsModel(person.getKey());
		rowCount = personModel.getRowCount();


		List<Transaction> my_Issue_Persons = new ArrayList<Transaction>();
		if (rowCount >0){
			BigDecimal eraBalanceA = new BigDecimal(0);
			BigDecimal eraBalanceB = new BigDecimal(0);
			BigDecimal eraBalanceC = new BigDecimal(0);
			BigDecimal eraBalanceTotal = new BigDecimal(0);
			BigDecimal compuBalance = new BigDecimal(0);

			for (int i = 0; i<rowCount; i++){
				Map accountJSON=new LinkedHashMap();
				accountJSON.put("adress", personModel.getValueAt(i, 0));
				accountJSON.put("data", personModel.getValueAt(i, 1));
				PersonCls  cc= (PersonCls) personModel.getValueAt(i, 3);

				accountJSON.put("creator", personModel.getValueAt(i, 2));
				if (cc != null){
					accountJSON.put("creator_name", cc.getName());
					accountJSON.put("creator_key", cc.getKey());
				}else{
					accountJSON.put("creator_name", "");
					accountJSON.put("creator_key", "");


				}





				accountsJSON.put(i, accountJSON);

				String acc = personModel.getValueAt(i, 0).toString();

				my_Issue_Persons.addAll(dcSet.getTransactionFinalMap().getTransactionsByTypeAndAddress(acc,Transaction.ISSUE_PERSON_TRANSACTION, 0));

				WEB_Balance_from_Adress_TableModel balanceTableModel = new WEB_Balance_from_Adress_TableModel(new Account(acc));

				for (int idr = 0; idr < balanceTableModel.getRowCount(); idr++)
				{
					switch ((String)balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_ASSET_NAME))
					{
					case "ERA":
						eraBalanceA = eraBalanceA.add((BigDecimal)balanceTableModel.getBalanceAt(idr, balanceTableModel.COLUMN_A));
						eraBalanceB = eraBalanceB.add((BigDecimal)balanceTableModel.getBalanceAt(idr, balanceTableModel.COLUMN_B));
						eraBalanceC = eraBalanceC.add((BigDecimal)balanceTableModel.getBalanceAt(idr, balanceTableModel.COLUMN_C));
						eraBalanceTotal = eraBalanceA.add(eraBalanceB).add(eraBalanceC);
						break;
					case "COMPU":
						compuBalance = compuBalance.add((BigDecimal)balanceTableModel.getBalanceAt(idr, balanceTableModel.COLUMN_A));
						break;
					}
				}
			}
			output.put("era_balance_a", NumberAsString.getInstance().numberAsString(eraBalanceA));
			output.put("era_balance_b", NumberAsString.getInstance().numberAsString(eraBalanceB));
			output.put("era_balance_c", NumberAsString.getInstance().numberAsString(eraBalanceC));
			output.put("era_balance_total", NumberAsString.getInstance().numberAsString(eraBalanceTotal));
			output.put("compu_balance", NumberAsString.getInstance().numberAsString(compuBalance));
		}
		output.put("accounts", accountsJSON);

		// my persons

		output.put("Label_My_Persons", Lang.getInstance().translate_from_langObj("My Persons",langObj));
		output.put("Label_My_Person_key", Lang.getInstance().translate_from_langObj("Key",langObj));
		output.put("Label_My_Persons_Name", Lang.getInstance().translate_from_langObj("Name",langObj));

		Map my_Persons_JSON=new LinkedHashMap();




		int i = 0;
		for ( Transaction my_Issue_Person:my_Issue_Persons){
			Map my_Person_JSON=new LinkedHashMap();
			Issue_ItemRecord record = (Issue_ItemRecord) my_Issue_Person;
			ItemCls item = record.getItem();

			my_Person_JSON.put("key", item.getKey());
			my_Person_JSON.put("name",item.getName());

			my_Person_JSON.put("date", df.format(new Date(my_Issue_Person.getTimestamp())).toString());//new Date(my_Issue_Person.getTimestamp().toString()));
			///my_Person_JSON.put("date", utils.DateTimeFormat.timestamptoString(my_Issue_Person.getTimestamp()));
			my_Persons_JSON.put(i, my_Person_JSON);
			i++;
		}

		output.put("My_Persons", my_Persons_JSON);








		return output;
	}


	public Map jsonQuerySearchPersons(String search)
	{
		/*	Block block;
		if(start > 0)
		{
			block = Controller.getInstance().getBlockByHeight(start);
		}
		else
		{
			block = getLastBlock();
			start = block.getHeight(dcSet);
		}
		 */
		Map output=new LinkedHashMap();





		output.put("unconfirmedTxs", dcSet.getTransactionMap().size());

		// TODO translate_web(

		output.put("Label_key", Lang.getInstance().translate_from_langObj("Key",langObj));
		output.put("Label_name", Lang.getInstance().translate_from_langObj("Name",langObj));
		output.put("Label_creator", Lang.getInstance().translate_from_langObj("Creator",langObj));
		output.put("Label_Later", Lang.getInstance().translate_from_langObj(">>",langObj));
		output.put("Label_Previous", Lang.getInstance().translate_from_langObj("<<",langObj));



		/*
		output.put("Label_Unconfirmed_transactions", "Unconfirmed transactions");
		output.put("Label_Height", "Height");
		output.put("Label_Time", "Time");
		output.put("Label_Generator", "Creator");
		output.put("Label_Gen_balance", "Gen.Balance");
		output.put("Label_TXs", "TXs");
		output.put("Label_Fee", "Fee");
		output.put("Label_AT_Amount", "AT_Amount");
		output.put("Label_Amount", "Amount");
		output.put("Label_Later", "Later");
		output.put("Label_Previous", "Previous");

		int counter = start;
		 */



		//	if (i <0) i =i + maxRow - start_Web;
		//		k =  maxRow - i;
		List<ItemCls> listPerson = new ArrayList();
		if (search != ""){

			if (search.matches("\\d+") && dcSet.getItemPersonMap().contains(Long.valueOf(search))){
				listPerson.add(dcSet.getItemPersonMap().get(Long.valueOf(search)));
			}else{
				listPerson = dcSet.getItemPersonMap().get_By_Name(search, false);
			}
		}



		//	if (k> dcSet.getItemPersonMap().getSize()) k= dcSet.getItemPersonMap().getSize();

		int i=0;
		if (listPerson != null){
			for(ItemCls pers:listPerson){

				PersonCls person = (PersonCls) pers;


				Map blockJSON=new LinkedHashMap();
				blockJSON.put("key", person.getKey());
				blockJSON.put("name", person.getName());
				blockJSON.put("creator",person.getOwner().getAddress());
				String img = Base64.encodeBase64String(person.getImage());
				blockJSON.put("img",img);


				/*
			blockJSON.put("generatingBalance", block.getGeneratingBalance(dcSet));
			//blockJSON.put("winValue", block.calcWinValue(dcSet));
			blockJSON.put("winValueTargetted", block.calcWinValueTargeted(dcSet));
			blockJSON.put("transactionCount", block.getTransactionCount());
			blockJSON.put("timestamp", block.getTimestamp(dcSet));
			blockJSON.put("dateTime", BlockExplorer.timestampToStr(block.getTimestamp(dcSet)));
			blockJSON.put("totalFee", block.getTotalFee().toPlainString());

			BigDecimal totalAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
			for (Transaction transaction : block.getTransactions()) {
				for (Account account : transaction.getInvolvedAccounts()) {
					BigDecimal amount = transaction.getAmount(account);
					if(amount.compareTo(BigDecimal.ZERO) > 0)
					{
						totalAmount = totalAmount.add(amount);
					}
				}
			}

			blockJSON.put("totalAmount", totalAmount.toPlainString());

			LinkedHashMap< Tuple2<Integer, Integer> , AT_Transaction> aTtxs = dcSet.getATTransactionMap().getATTransactions(counter);

			BigDecimal totalATAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

			for(Map.Entry<Tuple2<Integer, Integer> , AT_Transaction> e : aTtxs.entrySet())
			{
				totalATAmount = totalATAmount.add(BigDecimal.valueOf( e.getValue().getAmount() , BlockChain.AMOUNT_DEDAULT_SCALE));
			}

			blockJSON.put("totalATAmount", totalATAmount.toPlainString());
			//blockJSON.put("aTfee", block.getATfee().toPlainString());

			output.put(counter, blockJSON);

			counter --;
			block = block.getParent(dcSet);
				 */
				output.put(i, blockJSON);
				i++;

			}


			output.put("start_row", listPerson.size()-1);
			output.put("maxHeight",dcSet.getItemPersonMap().getLastKey());
			output.put("row", -1);
			output.put("view_Row", listPerson.size()-1);
		}



		return output;
	}



	public Map jsonQueryPersons(String start_Web)
	{
		/*	Block block;
		if(start > 0)
		{
			block = Controller.getInstance().getBlockByHeight(start);
		}
		else
		{
			block = getLastBlock();
			start = block.getHeight(dcSet);
		}
		 */
		Map output=new LinkedHashMap();





		output.put("unconfirmedTxs", dcSet.getTransactionMap().size());

		// TODO translate_web(

		output.put("Label_key", Lang.getInstance().translate_from_langObj("Key",langObj));
		output.put("Label_name", Lang.getInstance().translate_from_langObj("Name",langObj));
		output.put("Label_creator", Lang.getInstance().translate_from_langObj("Creator",langObj));
		output.put("Label_Later", Lang.getInstance().translate_from_langObj(">>",langObj));
		output.put("Label_Previous", Lang.getInstance().translate_from_langObj("<<",langObj));



		/*
		output.put("Label_Unconfirmed_transactions", "Unconfirmed transactions");
		output.put("Label_Height", "Height");
		output.put("Label_Time", "Time");
		output.put("Label_Generator", "Creator");
		output.put("Label_Gen_balance", "Gen.Balance");
		output.put("Label_TXs", "TXs");
		output.put("Label_Fee", "Fee");
		output.put("Label_AT_Amount", "AT_Amount");
		output.put("Label_Amount", "Amount");
		output.put("Label_Later", "Later");
		output.put("Label_Previous", "Previous");

		int counter = start;
		 */

		long maxRow = dcSet.getItemPersonMap().getLastKey();
		long view_Row = 21;
		Long startRow;
		try {
			startRow = Long.valueOf(start_Web);
			if(startRow> maxRow) startRow = maxRow;

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block

			startRow = maxRow;
		}




		if (startRow <1) startRow = view_Row;

		long i = startRow;
		long k  = i - view_Row;
		if(startRow-view_Row < 0 ) k = 0;
		//	if (i <0) i =i + maxRow - start_Web;
		//		k =  maxRow - i;



		//	if (k> dcSet.getItemPersonMap().getSize()) k= dcSet.getItemPersonMap().getSize();
		output.put("start_row", i);
		do{

			PersonCls person = (PersonCls) dcSet.getItemPersonMap().get(i);



			Map blockJSON=new LinkedHashMap();
			blockJSON.put("key", person.getKey());
			blockJSON.put("name", person.getName());
			blockJSON.put("creator",person.getOwner().getAddress());
			String img = Base64.encodeBase64String(person.getImage());
			blockJSON.put("img",img);


			/*
			blockJSON.put("generatingBalance", block.getGeneratingBalance(dcSet));
			//blockJSON.put("winValue", block.calcWinValue(dcSet));
			blockJSON.put("winValueTargetted", block.calcWinValueTargeted(dcSet));
			blockJSON.put("transactionCount", block.getTransactionCount());
			blockJSON.put("timestamp", block.getTimestamp(dcSet));
			blockJSON.put("dateTime", BlockExplorer.timestampToStr(block.getTimestamp(dcSet)));
			blockJSON.put("totalFee", block.getTotalFee().toPlainString());

			BigDecimal totalAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
			for (Transaction transaction : block.getTransactions()) {
				for (Account account : transaction.getInvolvedAccounts()) {
					BigDecimal amount = transaction.getAmount(account);
					if(amount.compareTo(BigDecimal.ZERO) > 0)
					{
						totalAmount = totalAmount.add(amount);
					}
				}
			}

			blockJSON.put("totalAmount", totalAmount.toPlainString());

			LinkedHashMap< Tuple2<Integer, Integer> , AT_Transaction> aTtxs = dcSet.getATTransactionMap().getATTransactions(counter);

			BigDecimal totalATAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

			for(Map.Entry<Tuple2<Integer, Integer> , AT_Transaction> e : aTtxs.entrySet())
			{
				totalATAmount = totalATAmount.add(BigDecimal.valueOf( e.getValue().getAmount() , BlockChain.AMOUNT_DEDAULT_SCALE));
			}

			blockJSON.put("totalATAmount", totalATAmount.toPlainString());
			//blockJSON.put("aTfee", block.getATfee().toPlainString());

			output.put(counter, blockJSON);

			counter --;
			block = block.getParent(dcSet);
			 */
			output.put(i, blockJSON);
			i--;

		}
		while(i > k);

		output.put("maxHeight",dcSet.getItemPersonMap().getLastKey());
		output.put("row", i);
		output.put("view_Row", view_Row);




		return output;
	}




	public Map jsonQueryLastBlock()
	{
		Map output=new LinkedHashMap();

		Block lastBlock = getLastBlock();

		output.put("height", lastBlock.getHeight(dcSet));
		output.put("timestamp", lastBlock.getTimestamp(dcSet));
		output.put("dateTime", BlockExplorer.timestampToStr(lastBlock.getTimestamp(dcSet)));

		output.put("timezone", Settings.getInstance().getTimeZone());
		output.put("timeformat", Settings.getInstance().getTimeFormat());
		output.put("label_hour", Lang.getInstance().translate_from_langObj("hour", langObj));
		output.put("label_hours", Lang.getInstance().translate_from_langObj("hours", langObj));
		output.put("label_mins", Lang.getInstance().translate_from_langObj("mins", langObj));
		output.put("label_min", Lang.getInstance().translate_from_langObj("min", langObj));
		output.put("label_secs", Lang.getInstance().translate_from_langObj("secs", langObj));
		output.put("label_ago", Lang.getInstance().translate_from_langObj("ago", langObj));
		output.put("label_Last_processed_block", Lang.getInstance().translate_from_langObj("Last processed block", langObj));



		return output;
	}

	public Map jsonQueryTopRichest(int limit, long key)
	{
		Map output=new LinkedHashMap();
		Map balances=new LinkedHashMap();
		BigDecimal all = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		BigDecimal alloreders = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

		List<Tuple3<String, BigDecimal, BigDecimal>> top100s = new ArrayList<Tuple3<String, BigDecimal, BigDecimal>>();


		Collection<Tuple2<String, Long>> addrs = dcSet.getAssetBalanceMap().getKeys();
		for (Tuple2<String, Long> addr : addrs) {
			if(addr.b == key)
			{
				Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> ball = dcSet.getAssetBalanceMap().get(addr);
				//all = all.add(ball.a);
				Account account = new Account(addr.a);
				BigDecimal ballans = account.getBalanceUSE(key);

				top100s.add(Fun.t3(addr.a, ballans, ball.a.b));
			}
		}

		/*
		// LIST to LOG.txt
		JSONObject listJSON = new JSONObject();
		listJSON.put("item", top100s);
		LOGGER.info(listJSON);
		 */

		Collection<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = dcSet.getOrderMap().getValuesAll();

		for (Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order : orders) {
			if(order.b.a == key)
			{
				alloreders = alloreders.add(order.b.c);
			}
		}
		Collections.sort(top100s, new ReverseComparator(new BigDecimalComparator_C()));

		int couter = 0;
		for (Tuple3<String, BigDecimal, BigDecimal> top100 : top100s) {
			/*
			if(limit == -1) // allnotzero
			{
				if(top100.b.compareTo(BigDecimal.ZERO) <= 0)
				{
					break;
				}
			}
			 */
			couter ++;


			Account account = new Account(top100.a);

			Tuple2<Integer, PersonCls> person = account.getPerson();

			Map balance=new LinkedHashMap();
			balance.put("address", top100.a);
			balance.put("balance", top100.b.toPlainString());
			balance.put("in_OWN", top100.c.toPlainString());

			if (person != null){
				balance.put("person", person.b.getName());
				balance.put("person_key", person.b.getKey());
			} else{
				balance.put("person", "-");
				balance.put("person_key", "-");//(String)person.b.getKey());

			}


			balances.put(couter, balance);

			if(couter >= limit && limit != -2 && limit != -1) // -2 = all
			{
				break;
			}
		}
		AssetCls asset = Controller.getInstance().getAsset(key);
		output.put("Label_Table_Account", Lang.getInstance().translate_from_langObj("Account",langObj));
		output.put("Label_Table_Balance", Lang.getInstance().translate_from_langObj("Balance",langObj));
		output.put("Label_Table_in_OWN", Lang.getInstance().translate_from_langObj("in OWN",langObj));
		output.put("Label_Table_Prop", Lang.getInstance().translate_from_langObj("Prop.",langObj));
		output.put("Label_Table_person", Lang.getInstance().translate_from_langObj("Owner",langObj));

		output.put("Label_minus", Lang.getInstance().translate_from_langObj("minus",langObj));
		output.put("Label_in_order", Lang.getInstance().translate_from_langObj("in order",langObj));


		output.put("Label_Top", Lang.getInstance().translate_from_langObj("Top",langObj));
		output.put("Label_Top", Lang.getInstance().translate_from_langObj("Top",langObj));

		output.put("all", all.toPlainString());
		output.put("allinOrders", alloreders.toPlainString());
		output.put("allTotal",asset.getTotalQuantity(dcSet));//(all.add(alloreders)).toPlainString());
		output.put("assetKey", key);
		output.put("assetName",asset.getName());
		output.put("limit", limit);
		output.put("count", couter);

		output.put("top", balances);
		output.put("Label_Title",(Lang.getInstance().translate_from_langObj("Top %limit% %assetName% Richest",langObj).replace("%limit%",String.valueOf(limit))).replace("%assetName%", asset.getName()));
		output.put("Label_All_non", (Lang.getInstance().translate_from_langObj("All non-empty %assetName% accounts (%count%)",langObj).replace("%assetName%", asset.getName())).replace("%count%", String.valueOf(couter)));
		output.put("Label_All_accounts",(Lang.getInstance().translate_from_langObj("All %assetName% accounts (%count%)",langObj).replace("%assetName%", asset.getName())).replace("%count%", String.valueOf(couter)));
		output.put("Label_Total_coins_in_the_system", Lang.getInstance().translate_from_langObj("Total asset units in the system", langObj));

		return output;
	}


	public LinkedHashMap Transactions_JSON(List<Transaction> transactions){
		return Transactions_JSON(transactions, 0, 0);
	}


	public LinkedHashMap Transactions_JSON(List<Transaction> transactions, int fromIndex, int toIndex){

		LinkedHashMap output = new LinkedHashMap();
		int i1 = 0;
		LinkedHashMap transactionsJSON = new LinkedHashMap();
		List<Transaction> transactions2 = (toIndex == 0) ? transactions : transactions.subList(fromIndex, Math.min(toIndex, transactions.size()));
		for (Transaction trans:transactions2){
			LinkedHashMap transactionJSON = new LinkedHashMap();

			trans.setDC(dcSet, false);

			/*
					String itemName = "-";
					Long itemKey = 0L;
					if (trans instanceof TransactionAmount && trans.getAbsKey() >0)
					{
						TransactionAmount transAmo = (TransactionAmount)trans;
						//recipient = transAmo.getRecipient();
						ItemCls item = dcSet.getItemAssetMap().get(transAmo.getAbsKey());
						if (item==null){
							itemName = "-";
							itemKey = 0L;

						}
						itemName = item.toString();
						itemKey   = item.getKey();
					} else if ( trans instanceof GenesisTransferAssetTransaction)
					{
						GenesisTransferAssetTransaction transGen = (GenesisTransferAssetTransaction)trans;
						//recipient = transGen.getRecipient();
						ItemCls item = dcSet.getItemAssetMap().get(transGen.getAbsKey());
						itemName = item.toString();
						itemKey  = item.getKey();
					} else if ( trans instanceof Issue_ItemRecord)
					{
						Issue_ItemRecord transIssue = (Issue_ItemRecord)trans;
						ItemCls item = transIssue.getItem();
						itemName = item.getShort();
						itemKey  = item.getKey();
					} else if ( trans instanceof GenesisIssue_ItemRecord)
					{
						GenesisIssue_ItemRecord transIssue = (GenesisIssue_ItemRecord)trans;
						ItemCls item = transIssue.getItem();
						itemName = item.getShort();
						itemKey  = item.getKey();
					} else if (trans instanceof R_SertifyPubKeys )
					{
						R_SertifyPubKeys sertifyPK = (R_SertifyPubKeys)trans;
						//recipient = transAmo.getRecipient();
						ItemCls item = dcSet.getItemPersonMap().get(sertifyPK.getAbsKey());
						if (item == null){
							itemName = "-";
							itemKey  = (long) -1;

						}
						itemName = item.toString();
						itemKey  = item.getKey();
					} else {
						itemName = trans.viewItemName();
						itemKey  = (long) -1;
					}

					transactionJSON.put("amount",amount);
					transactionJSON.put("item_name", itemName);
					transactionJSON.put("item_key", itemKey);
			 */

			//
			transactionJSON.put("block",trans.getBlockHeight(dcSet));//.getSeqNo(dcSet));

			transactionJSON.put("seq",trans.getSeqNo(dcSet));
			//transactionJSON.put("reference",trans.getReference());
			transactionJSON.put("signature",Base58.encode(trans.getSignature()));
			transactionJSON.put("date",DateTimeFormat.timestamptoString(trans.getTimestamp()));
			transactionJSON.put("creator",trans.viewCreator());


			if (trans.getCreator() == null){
				transactionJSON.put("creator_key", "-");
				transactionJSON.put("creator_addr", "-");
			}
			else {
				transactionJSON.put("pub_key",Base58.encode(trans.getCreator().getPublicKey()));
				transactionJSON.put("creator_addr", trans.getCreator().getAddress());
				if (trans.getCreator().getPerson() == null){
					transactionJSON.put("creator_key", "+");

				}
				else {
					transactionJSON.put("creator_key",trans.getCreator().getPerson().b.getKey());
				}
			}

			transactionJSON.put("size",trans.viewSize(false));
			transactionJSON.put("fee",trans.getFee());
			transactionJSON.put("confirmations",trans.getConfirmations(dcSet));
			transactionJSON.put("type",Lang.getInstance().translate_from_langObj(trans.viewFullTypeName(),langObj));


			//String amount = "-";
			//if (trans.getAmount() != null) amount = trans.getAmount().toString();

			long absKey = trans.getAbsKey();
			String amount = trans.viewAmount();
			if (absKey > 0) {
				if (amount.length() > 0) {
					transactionJSON.put("amount_key", trans.viewAmount() + ":" + absKey);
				} else {
					transactionJSON.put("amount_key", "" + absKey);
				}
			} else {
				transactionJSON.put("amount_key", "");
			}

			if (trans.viewRecipient() == null){transactionJSON.put("recipient","-");}
			else {transactionJSON.put("recipient",trans.viewRecipient());}

			transactionsJSON.put(i1, transactionJSON);
			i1++;
		}


		output.put("transactions", transactionsJSON);
		output.put("label_block",Lang.getInstance().translate_from_langObj("Block",langObj));
		output.put("label_date",Lang.getInstance().translate_from_langObj("Date",langObj));
		output.put("label_type_transaction",Lang.getInstance().translate_from_langObj("Type",langObj));
		output.put("label_creator",Lang.getInstance().translate_from_langObj("Creator",langObj));
		output.put("label_asset",Lang.getInstance().translate_from_langObj("Asset",langObj));
		output.put("label_amount",Lang.getInstance().translate_from_langObj("Amount",langObj));
		output.put("label_confirmations",Lang.getInstance().translate_from_langObj("Confirmations",langObj));
		output.put("label_recipient",Lang.getInstance().translate_from_langObj("Recipient",langObj));
		output.put("label_size",Lang.getInstance().translate_from_langObj("Size",langObj));
		output.put("label_seq",Lang.getInstance().translate_from_langObj("Seq",langObj));
		output.put("label_signature",Lang.getInstance().translate_from_langObj("Signature",langObj));
		//output.put("label_reference",Lang.getInstance().translate_from_langObj("Reference",langObj));
		output.put("label_amount_key",Lang.getInstance().translate_from_langObj("Amount:Key", langObj));
		output.put("label_fee",Lang.getInstance().translate_from_langObj("Fee",langObj));
		output.put("label_transactions_table",Lang.getInstance().translate_from_langObj("Transactions",langObj));



		return output;

	}

	@SuppressWarnings("static-access")
	private LinkedHashMap Balance_JSON(Account account){

		// balance assets from
		LinkedHashMap output = new LinkedHashMap();
		WEB_Balance_from_Adress_TableModel balanceTableModel = new WEB_Balance_from_Adress_TableModel(account);
		int ad = balanceTableModel.getRowCount();
		int idr;
		Map bal_Assets = new LinkedHashMap();
		if (ad > 0) for (idr=0; idr<ad; idr++){
			Map bal = new LinkedHashMap();
			bal.put("asset_key", balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_ASSET_KEY));
			bal.put("asset_name", balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_ASSET_NAME));
			bal.put("balance_A", balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_A));
			bal.put("balance_B", balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_B));
			bal.put("balance_C", balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_C));
			if (!(balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_A).equals("0.00000000")
					&& balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_B).equals("0.00000000")
					&& balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_C).equals("0.00000000")))bal_Assets.put(idr, bal);
		}

		output.put("balances", bal_Assets);
		output.put("label_Balance_table",Lang.getInstance().translate_from_langObj("Balance",langObj));
		output.put("label_asset_key",Lang.getInstance().translate_from_langObj("Key",langObj));
		output.put("label_asset_name",Lang.getInstance().translate_from_langObj("Name",langObj));
		output.put("label_Balance_A",Lang.getInstance().translate_from_langObj("Balance",langObj)+" A");
		output.put("label_Balance_B",Lang.getInstance().translate_from_langObj("Balance",langObj)+" B");
		output.put("label_Balance_C",Lang.getInstance().translate_from_langObj("Balance",langObj)+" C");


		return output;



	}
	// dcSet
	public Map jsonUnitPrint(Object unit, AssetNames assetNames)
	{

		Map transactionDataJSON = new LinkedHashMap();
		Map transactionJSON = new LinkedHashMap();

		if (unit instanceof Trade)
		{
			Trade trade = (Trade)unit;

			Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> orderInitiator = trade.getInitiatorOrder(dcSet);

			/*
			if(dcSet.getOrderMap().contains(trade.getInitiator()))
			{
				orderInitiator =  dcSet.getOrderMap().get(trade.getInitiator());
			}
			else
			{
				orderInitiator =  dcSet.getCompletedOrderMap().get(trade.getInitiator());
			}
			 */

			Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> orderTarget = trade.getTargetOrder(dcSet);

			/*
			if(dcSet.getOrderMap().contains(trade.getTarget()))
			{
				orderTarget =  dcSet.getOrderMap().get(trade.getTarget());
			}
			else
			{
				orderTarget =  dcSet.getCompletedOrderMap().get(trade.getTarget());
			}
			 */

			transactionDataJSON.put("amountHave", trade.getAmountHave().toPlainString());
			transactionDataJSON.put("amountWant", trade.getAmountWant().toPlainString());

			transactionDataJSON.put("realPrice", trade.getAmountWant().divide(trade.getAmountHave(), 8, RoundingMode.FLOOR).toPlainString());

			transactionDataJSON.put("initiatorTxSignature", Base58.encode(orderInitiator.a.a));

			transactionDataJSON.put("initiatorCreator", orderInitiator.a.b);
			transactionDataJSON.put("initiatorAmount", orderInitiator.b.b.toPlainString());
			transactionDataJSON.put("initiatorHave", orderInitiator.b.a);
			transactionDataJSON.put("initiatorWant", orderInitiator.c.a);

			if(assetNames != null)
			{
				assetNames.setKey(orderInitiator.b.a);
				assetNames.setKey(orderInitiator.c.a);
			}

			transactionDataJSON.put("targetTxSignature", Base58.encode(orderTarget.a.a));
			transactionDataJSON.put("targetCreator", orderTarget.a.b);
			transactionDataJSON.put("targetAmount", orderTarget.b.b.toPlainString());

			Block parentBlock = Controller.getInstance().getTransaction(orderInitiator.a.a.toByteArray()).getBlock(dcSet);
			transactionDataJSON.put("height", parentBlock.getHeight(dcSet));
			transactionDataJSON.put("confirmations", getHeight() - parentBlock.getHeight(dcSet) + 1 );

			transactionDataJSON.put("timestamp", trade.getTimestamp());
			transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(trade.getTimestamp()));

			transactionJSON.put("type", "trade");
			transactionJSON.put("trade", transactionDataJSON);
			return transactionJSON;
		}

		if (unit instanceof Transaction)
		{
			Transaction transaction = (Transaction)unit;

			transactionDataJSON = transaction.toJson();
			//transactionDataJSON.put("Р ВµРЎв‚¬РЎРЉРЎС“РЎвЂ№Р ВµРЎвЂћ", GZIP.webDecompress(transactionDataJSON.get("value").toString()));

			if(transaction.getType() == Transaction.REGISTER_NAME_TRANSACTION)
			{
				if(transactionDataJSON.get("value").toString().startsWith("?gz!"))
				{
					transactionDataJSON.put("value", GZIP.webDecompress(transactionDataJSON.get("value").toString()));
					transactionDataJSON.put("compressed", true);
				}
				else
				{
					transactionDataJSON.put("compressed", false);
				}

			} else if(transaction.getType() == Transaction.UPDATE_NAME_TRANSACTION)
			{
				if(transactionDataJSON.get("newValue").toString().startsWith("?gz!"))
				{
					transactionDataJSON.put("newValue", GZIP.webDecompress(transactionDataJSON.get("newValue").toString()));
					transactionDataJSON.put("compressed", true);
				}
				else
				{
					transactionDataJSON.put("compressed", false);
				}
			} else if(transaction.getType() == Transaction.CANCEL_ORDER_TRANSACTION)
			{
				BigInteger key = ((CancelOrderTransaction)unit).getOrder();
				Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order;
				if(dcSet.getCompletedOrderMap().contains(key))
				{
					order =  dcSet.getCompletedOrderMap().get(key);
				}
				else
				{
					order =  dcSet.getOrderMap().get(key);
				}

				Map orderJSON = new LinkedHashMap();

				if (assetNames != null) {
					assetNames.setKey(order.b.a);
					assetNames.setKey(order.c.a);
				}

				orderJSON.put("have", order.b.a);
				orderJSON.put("want", order.c.a);

				orderJSON.put("amount", order.b.b.toPlainString());
				orderJSON.put("amountLeft", order.b.c.toPlainString());
				orderJSON.put("amountWant", order.c.b.toPlainString());
				orderJSON.put("price", order.a.e.toPlainString());

				transactionDataJSON.put("orderSource", orderJSON);

			} else if(transaction.getType() == Transaction.ISSUE_ASSET_TRANSACTION)
			{
				long assetkey = ((IssueAssetTransaction) transaction).getItem().getKey();

				transactionDataJSON.put("asset", assetkey);

				transactionDataJSON.put("assetName", ((IssueAssetTransaction) transaction).getItem().getName());

			} else if(transaction.getType() == Transaction.SEND_ASSET_TRANSACTION)
			{
				if (assetNames != null)
				{
					assetNames.setKey(((R_Send)unit).getKey());
				}

				//R_Send r_Send = (R_Send) transaction;
				//transactionDataJSON.put("assetName", Controller.getInstance().getAsset( r_Send.getKey()).toString());
				transactionDataJSON.put("assetName", assetNames.getMap().get(((R_Send)unit).getAbsKey()));

				if(((R_Send)unit).isEncrypted()){
					transactionDataJSON.put("data", "encrypted");
				}

			} else if(transaction.getType() == Transaction.HASHES_RECORD)
			{

			} else if(transaction.getType() == Transaction.MULTI_PAYMENT_TRANSACTION)
			{
				Map<Long, BigDecimal> totalAmountOfAssets = new TreeMap<Long, BigDecimal>();

				for (Payment payment : ((MultiPaymentTransaction)transaction).getPayments()) {
					BigDecimal amount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
					if(totalAmountOfAssets.containsKey(payment.getAsset())) {
						amount = totalAmountOfAssets.get(payment.getAsset());
					}
					amount = amount.add(payment.getAmount());

					if (assetNames != null) {
						assetNames.setKey(payment.getAsset());
					}

					totalAmountOfAssets.put( payment.getAsset(), amount );
				}

				Map amountOfAssetsJSON = new LinkedHashMap();

				for(Map.Entry<Long, BigDecimal> assetInfo : totalAmountOfAssets.entrySet())
				{
					amountOfAssetsJSON.put(assetInfo.getKey(), assetInfo.getValue().toPlainString());
				}

				transactionDataJSON.put("amounts", amountOfAssetsJSON);

			} else if(transaction.getType() == Transaction.ARBITRARY_TRANSACTION)
			{
				Map<Long, BigDecimal> totalAmountOfAssets = new TreeMap<Long, BigDecimal>();

				for (Payment payment : ((ArbitraryTransaction)transaction).getPayments()) {
					BigDecimal amount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
					if(totalAmountOfAssets.containsKey(payment.getAsset())) {
						amount = totalAmountOfAssets.get(payment.getAsset());
					}
					amount = amount.add(payment.getAmount());

					if (assetNames != null) {
						assetNames.setKey(payment.getAsset());
					}

					totalAmountOfAssets.put( payment.getAsset(), amount );
				}

				Map amountOfAssetsJSON = new LinkedHashMap();

				for(Map.Entry<Long, BigDecimal> assetInfo : totalAmountOfAssets.entrySet())
				{
					amountOfAssetsJSON.put(assetInfo.getKey(), assetInfo.getValue().toPlainString());
				}

				transactionDataJSON.put("amounts", amountOfAssetsJSON);

			} else if(transaction.getType() == Transaction.VOTE_ON_POLL_TRANSACTION)
			{
				transactionDataJSON.put("optionString",
						Controller.getInstance().getPoll(((VoteOnPollTransaction)transaction).getPoll()).getOptions().get(((VoteOnPollTransaction)transaction).getOption()).getName()
						);

			} else if(transaction.getType() == Transaction.CREATE_ORDER_TRANSACTION)
			{
				if (assetNames != null) {
					assetNames.setKey(((CreateOrderTransaction)transaction).getHaveKey());
					assetNames.setKey(((CreateOrderTransaction)transaction).getWantKey());
				}

			} else if(transaction.getType() == Transaction.DEPLOY_AT_TRANSACTION)
			{
				transactionDataJSON.put("atAddress", ((DeployATTransaction)transaction).getATaccount(dcSet).getAddress());
			}

			if(transaction.isConfirmed(dcSet))
			{
				Block parent = transaction.getBlock(dcSet);
				transactionDataJSON.put("block", Base58.encode(parent.getSignature()));
				transactionDataJSON.put("blockHeight", parent.getHeight(dcSet));
			}

			transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(transaction.getTimestamp()));

			transactionJSON.put("type", "transaction");
			transactionJSON.put("transaction", transactionDataJSON);
			return transactionJSON;
		}

		if (unit instanceof Block)
		{
			Block block = (Block)unit;

			transactionDataJSON = new LinkedHashMap();
			transactionDataJSON.put("timestamp", block.getTimestamp(dcSet));
			transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(block.getTimestamp(dcSet)));

			int height = block.getHeight(dcSet);
			transactionDataJSON.put("confirmations", getHeight() - height + 1 );
			transactionDataJSON.put("height", height);

			transactionDataJSON.put("generator", block.getCreator().getAddress());
			transactionDataJSON.put("signature", Base58.encode(block.getSignature()));

			/*
			transactionDataJSON.put("generatingBalance", block.getGeneratingBalance());
			transactionDataJSON.put("atFees", block.getATfee());
			transactionDataJSON.put("reference", Base58.encode(block.getReference()));
			transactionDataJSON.put("generatorSignature", Base58.encode(block.getGeneratorSignature()));
			transactionDataJSON.put("transactionsSignature", block.getTransactionsSignature());
			transactionDataJSON.put("version", block.getVersion());
			 */

			//transactionDataJSON.put("fee", balances[size - counter].getTransactionBalance().get(0l).toPlainString());
			transactionDataJSON.put("fee", block.getTotalFee().toPlainString());

			transactionJSON.put("type", "block");
			transactionJSON.put("block", transactionDataJSON);
			return transactionJSON;

		}

		if (unit instanceof AT_Transaction)
		{
			AT_Transaction aTtransaction = (AT_Transaction)unit;
			transactionDataJSON = aTtransaction.toJSON();

			Block block = Controller.getInstance().getBlockByHeight(aTtransaction.getBlockHeight());
			long timestamp = block.getTimestamp(dcSet);
			transactionDataJSON.put("timestamp", timestamp);
			transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(timestamp));

			transactionDataJSON.put("confirmations", getHeight() - ((AT_Transaction)unit).getBlockHeight() + 1 );

			if(((AT_Transaction)unit).getRecipient().equals("1111111111111111111111111"))
			{
				transactionDataJSON.put("generatorAddress", block.getCreator().getAddress());
			}

			transactionJSON.put("type", "atTransaction");
			transactionJSON.put("atTransaction", transactionDataJSON);
			return transactionJSON;
		}

		return transactionJSON;
	}

	public Map jsonQueryName(String query, int start, int txOnPage, String filter, boolean allOnOnePage)
	{
		TreeSet<BlExpUnit> all = new TreeSet<>();
		String name = query;

		int[] txsTypeCount = new int[256];

		Map output=new LinkedHashMap();

		int txsCount = 0;
		int height = 1;

		Block block = new GenesisBlock();
		do
		{
			int seq = 1;
			for(Transaction transaction: block.getTransactions())
			{
				if	(
						(transaction.getType() == Transaction.REGISTER_NAME_TRANSACTION && ((RegisterNameTransaction)transaction).getName().toString().equals(name))
						||(transaction.getType() == Transaction.UPDATE_NAME_TRANSACTION && ((UpdateNameTransaction)transaction).getName().toString().equals(name))
						||(transaction.getType() == Transaction.SELL_NAME_TRANSACTION && ((SellNameTransaction)transaction).getNameSale().toString().equals(name))
						||(transaction.getType() == Transaction.CANCEL_SELL_NAME_TRANSACTION && ((CancelSellNameTransaction)transaction).getName().equals(name))
						||(transaction.getType() == Transaction.BUY_NAME_TRANSACTION && ((BuyNameTransaction)transaction).getNameSale().toString().equals(name))
						)
				{
					all.add( new BlExpUnit( height, seq, transaction));
					txsTypeCount[transaction.getType()-1] ++;
				}
				seq ++;
			}
			block = block.getChild(dcSet);
			height ++;
		}
		while(block != null);

		int size = all.size();
		txsCount = size;

		if(start == -1 )
		{
			start = size;
		}

		output.put("type", "name");

		output.put("name", name);

		Map txCountJSON = new LinkedHashMap();

		if(txsCount > 0)
		{
			txCountJSON.put("txsCount", txsCount);
			Map txTypeCountJSON = new LinkedHashMap();
			int n = 1;
			for (int txCount : txsTypeCount) {
				if(txCount > 0)
				{
					txTypeCountJSON.put(n, txCount);
				}
				n ++;
			}
			txCountJSON.put("txsTypesCount", txTypeCountJSON);
		}

		txCountJSON.put("allCount", txsCount);

		output.put("countTx", txCountJSON);

		output.put("txOnPage", txOnPage);

		output.put("filter", filter);

		output.put("allOnOnePage", allOnOnePage);

		output.put("start", start);

		int end;

		if(start > txOnPage)
		{
			if(allOnOnePage)
			{
				end = 1;
			}
			else
			{
				end = start - txOnPage;
			}
		}
		else
		{
			end = 1;
		}

		output.put("end", end);

		int counter = 0;

		AssetNames assetNames = new AssetNames();

		for (BlExpUnit unit : all) {
			if(counter >= size - start)
			{
				output.put(size - counter, jsonUnitPrint(unit.getUnit(), assetNames));
			}

			if(counter > size - end)
			{
				break;
			}

			counter++;
		}

		return output;
	}

	public Map jsonQueryBalance(String address)
	{
		Map output = new LinkedHashMap();

		if(!Crypto.getInstance().isValidAddress(address))
		{
			output.put("error", "Address is not valid!");
			return output;
		}

		SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetsBalances = dcSet.getAssetBalanceMap().getBalancesSortableList(new Account(address));

		for (Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetsBalance : assetsBalances)
		{
			Map assetBalance = new LinkedHashMap();

			assetBalance.put("assetName", Controller.getInstance().getAsset(assetsBalance.getA().b).getName());
			assetBalance.put("amount", assetsBalance.getB().toString());

			output.put(assetsBalance.getA().b, assetBalance);
		}

		return output;
	}

	public Map<Long, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetBalance(String address)
	{
		Map<Long, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> output = new LinkedHashMap();

		SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetsBalances = dcSet.getAssetBalanceMap().getBalancesSortableList(new Account(address));

		for (Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetsBalance : assetsBalances)
		{
			output.put(assetsBalance.getA().b, assetsBalance.getB());
		}

		return output;
	}



	@SuppressWarnings({ "serial", "static-access" })
	public Map jsonQueryAddress(List<String> addresses, int transPage, int start, int txOnPage, String filter, boolean allOnOnePage, String showOnly, String showWithout)
	{

		List<Transaction> tt = dcSet.getTransactionFinalMap().getTransactionsByAddress(addresses.get(0));

		TreeSet<BlExpUnit> all = new TreeSet<>();

		addresses = new ArrayList<>(new LinkedHashSet<String>(addresses));

		LinkedHashMap error = new LinkedHashMap();

		LinkedHashMap output = new LinkedHashMap();
		LinkedHashMap transactionsJSON = new LinkedHashMap();
		output.put("account", addresses.get(0));

		Account acc = new Account(addresses.get(0));
		Long person_key = (long) -10;
		Tuple2<Integer, PersonCls> pp = acc.getPerson();

		if (pp != null){
			output.put("label_person_name", Lang.getInstance().translate_from_langObj("Name",langObj));
			output.put("person_Img",  Base64.encodeBase64String(pp.b.getImage()));
			output.put("Person_Name", pp.b.getName());
			person_key = pp.b.getKey();
		}
		output.put("person_key", person_key);
		output.put("label_account", Lang.getInstance().translate_from_langObj("Account",langObj));

		// balance assets from
		output.put("Balance", Balance_JSON(new Account(addresses.get(0))));

		// Transactions view
		output.put("Transactions", Transactions_JSON(tt, (transPage - 1) * 100, transPage * 100));
		output.put("pageCount", (int)Math.ceil((tt.size()) / 100d));
		output.put("pageNumber", transPage);


		output.put("type", "standardAccount");

		int a = 1;
		if (a ==1) return output;

		Map<String, Boolean> showOnlyMap = new LinkedHashMap<String, Boolean>();
		for (String string : showOnly.split(",")) {
			showOnlyMap.put(string, true);
		}

		Map<String, Boolean> showWithoutMap = new LinkedHashMap<String, Boolean>();
		for (String string : showWithout.split(",")) {
			showWithoutMap.put(string, true);
		}


		for (String address : addresses)
		{
			if(!Crypto.getInstance().isValidAddress(address))
			{
				error.put(address, "Address is not valid!");
			}

			if (addresses.size() > 1 && address.startsWith("A"))
			{
				error.put(address, "Multiple addresses with the AT is not supported!");
			}
		}

		if (!error.isEmpty()) {
			output.put("error", error);
			return output;
		}

		if (addresses.size() > 10)
		{
			output.put("error", "Too many addresses!");
		}

		Map<String, Integer> txsCountOfAddr = new LinkedHashMap<>();

		output.put( "addresses", addresses );

		if(addresses.get(0).startsWith("A"))
		{
			String address = addresses.get(0);

			AT at = dcSet.getATMap().getAT(address);
			Block block = Controller.getInstance().getBlockByHeight(at.getCreationBlockHeight());
			long aTtimestamp = block.getTimestamp(dcSet);
			BigDecimal aTbalanceCreation = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
			for (Transaction transaction : block.getTransactions()) {
				if (transaction.getType() == Transaction.DEPLOY_AT_TRANSACTION )
				{
					Account atAccount = ((DeployATTransaction)transaction).getATaccount(dcSet);

					if(atAccount.getAddress().equals(address))
					{
						all.add( new BlExpUnit(at.getCreationBlockHeight(), 0, transaction) );
						aTbalanceCreation = ((DeployATTransaction)transaction).getAmount();
					}
				}
			}

			Set<BlExpUnit> atTransactions = dcSet.getATTransactionMap().getBlExpATTransactionsBySender(address);

			all.addAll( atTransactions );

			output.put("type", "at");

			Map atJSON = new LinkedHashMap();
			atJSON = at.toJSON();
			atJSON.put("balanceCreation", aTbalanceCreation.toPlainString());
			atJSON.put("timestamp", aTtimestamp);
			atJSON.put("dateTime", BlockExplorer.timestampToStr(aTtimestamp));

			output.put("at", atJSON);
		}
		else
		{
			output.put("type", "standardAccount");
		}

		for (String address : addresses) {
			if (!address.startsWith("A")) {
				Collection<Integer> block_heights = dcSet.getBlockMap().getGeneratorBlocks(address);

				for (int height : block_heights)
				{
					Block block = dcSet.getBlockMap().get(height);
					all.add( new BlExpUnit( block.getHeight(dcSet), 0, block ) );
				}
			}

			Set<BlExpUnit> transactions = dcSet.getTransactionFinalMap().getBlExpTransactionsByAddress(address);
			txsCountOfAddr.put(address, transactions.size());
			all.addAll(transactions);
		}

		for (String address : addresses) {
			Map<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades = new TreeMap<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>();
			List<Transaction> orders = dcSet.getTransactionFinalMap().getTransactionsByTypeAndAddress(address, Transaction.CREATE_ORDER_TRANSACTION, 0);
			TradeMap tradeMap = dcSet.getTradeMap();
			for (Transaction transaction : orders)
			{
				SortableList<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> tradesBuf = tradeMap.getTradesByOrderID(new BigInteger(transaction.getSignature()));
				for (Pair<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> pair : tradesBuf) {
					trades.put(pair.getA(), pair.getB());
				}
			}

			for(Map.Entry<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trade : trades.entrySet())
			{
				Transaction txInitiator = Controller.getInstance().getTransaction(trade.getValue().a.toByteArray());

				Transaction txTarget = Controller.getInstance().getTransaction(trade.getValue().b.toByteArray());

				all.add( new BlExpUnit(txInitiator.getBlockHeightByParentOrLast(dcSet),
						txTarget.getBlockHeightByParentOrLast(dcSet), txInitiator.getSeqNo(dcSet), txTarget.getSeqNo(dcSet), trade.getValue() ) );
			}

			Set<BlExpUnit> atTransactions = dcSet.getATTransactionMap().getBlExpATTransactionsByRecipient(address);
			all.addAll( atTransactions );
		}

		int size = all.size();

		if(size == 0)
		{
			output.put("error", "No transactions found for this address.<br>It has probably not been used on the network yet.");
			return output;
		}

		int tradesCount = 0;
		int aTTxsCount = 0;
		int txsCount = 0;
		int totalBlocksGeneratedCount = 0;
		BigDecimal totalBlocksGeneratedFee = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		int[] txsTypeCount = new int[256];
		List<Map<String, Map<Long, BigDecimal>>> tXincomes = new ArrayList<>();
		List<Map<Long, BigDecimal>> totalBalances = new ArrayList<>();
		BigDecimal spentFee = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		Map<Long, BigDecimal> receivedCoins = new LinkedHashMap<>();
		Map<Long, BigDecimal> sentCoins = new LinkedHashMap<>();
		Map<String, BigDecimal> generatedFee = new LinkedHashMap<>();
		Map<String, Integer> blocksGeneratedCount = new LinkedHashMap<>();

		Map<Long, BigDecimal> zeroAmount = new LinkedHashMap<Long, BigDecimal>(){{put(FEE_KEY, BigDecimal.ZERO);}};

		int i = 1;
		for ( BlExpUnit unit : all ) {

			Map<String, Map<Long, BigDecimal>> tXincome = new LinkedHashMap<>();

			if (unit.getUnit() instanceof TransactionAmount) {

				TransactionAmount tx = (TransactionAmount)unit.getUnit();
				tXincome = tx.getAssetAmount();

				if (tx.getCreator() != null && addresses.contains(tx.getCreator().getAddress()))
				{
					spentFee = spentFee.add(tx.getFee());
				}

				txsCount ++;
				txsTypeCount[((Transaction)unit.getUnit()).getType()-1] ++;

			} else if (unit.getUnit() instanceof Block) {

				BigDecimal fee = ((Block)unit.getUnit()).getTotalFee();
				String generator = ((Block)unit.getUnit()).getCreator().getAddress();

				tXincome = Transaction.addAssetAmount(tXincome, generator, FEE_KEY, fee);

				generatedFee.put(
						generator,
						generatedFee.getOrDefault(generator, BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE)).add(fee)
						);

				totalBlocksGeneratedFee = totalBlocksGeneratedFee.add(fee);

				blocksGeneratedCount.put(
						generator,
						blocksGeneratedCount.getOrDefault(generator, 0) + 1
						);

				totalBlocksGeneratedCount ++;

			}
			else if (unit.getUnit() instanceof Trade)
			{
				Trade trade = (Trade)unit.getUnit();

				Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> orderInitiator;
				if (dcSet.getCompletedOrderMap().contains(trade.getInitiator()))
				{
					orderInitiator =  dcSet.getCompletedOrderMap().get(trade.getInitiator());
				}
				else
				{
					orderInitiator =  dcSet.getOrderMap().get(trade.getInitiator());
				}

				Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> orderTarget;
				if (dcSet.getCompletedOrderMap().contains(trade.getTarget()))
				{
					orderTarget =  dcSet.getCompletedOrderMap().get(trade.getTarget());
				}
				else
				{
					orderTarget =  dcSet.getOrderMap().get(trade.getTarget());
				}

				if(addresses.contains(orderInitiator.a.b))
				{
					tXincome = Transaction.addAssetAmount(tXincome, orderInitiator.a.b, orderInitiator.b.a, trade.getAmountHave());
				}

				if(addresses.contains(orderTarget.a.b)) {

					tXincome = Transaction.addAssetAmount(tXincome, orderTarget.a.b, orderInitiator.b.a, trade.getAmountWant());

				}

				tradesCount ++;

			}
			else if (unit.getUnit() instanceof AT_Transaction)
			{
				AT_Transaction atTransaction = (AT_Transaction)unit.getUnit();

				if (addresses.contains(atTransaction.getSender()))
				{
					tXincome = Transaction.subAssetAmount(tXincome, atTransaction.getSender(), FEE_KEY, BigDecimal.valueOf( atTransaction.getAmount() , BlockChain.AMOUNT_DEDAULT_SCALE));
				}

				if(addresses.contains(atTransaction.getRecipient()))
				{
					tXincome = Transaction.addAssetAmount(tXincome, atTransaction.getRecipient(), FEE_KEY, BigDecimal.valueOf( atTransaction.getAmount() , BlockChain.AMOUNT_DEDAULT_SCALE));
				}

				aTTxsCount++;
			}

			tXincomes.add(tXincome);

			Map<Long, BigDecimal> newTotalBalance;
			if (totalBalances.size() > 0)
			{
				newTotalBalance = new LinkedHashMap<>(totalBalances.get(totalBalances.size()-1));
			}
			else
			{
				newTotalBalance = new LinkedHashMap<>(zeroAmount);
			}

			for (String address : addresses)
			{
				for (Map.Entry<Long, BigDecimal> assetAmount : tXincome.getOrDefault(address, zeroAmount).entrySet())
				{
					if (assetAmount.getValue().compareTo(BigDecimal.ZERO) < 0)
					{
						sentCoins.put(
								assetAmount.getKey(),
								sentCoins.getOrDefault(assetAmount.getKey(), BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE))
								.subtract(assetAmount.getValue())
								);
					}

					if (assetAmount.getValue().compareTo(BigDecimal.ZERO) > 0)
					{
						receivedCoins.put(
								assetAmount.getKey(),
								receivedCoins.getOrDefault(assetAmount.getKey(), BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE))
								.add(assetAmount.getValue())
								);
					}

					newTotalBalance.put(assetAmount.getKey(),
							newTotalBalance.getOrDefault(assetAmount.getKey(), BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE))
							.add(assetAmount.getValue())
							);
				}

				if ((newTotalBalance.containsKey(FEE_KEY)) && newTotalBalance.get(FEE_KEY).compareTo(BigDecimal.ZERO) < 0)
				{
					LOGGER.info(i);
				}
			}

			totalBalances.add(newTotalBalance);

			i++;
		}

		Map blockExplorerBalance=new LinkedHashMap();
		Map total=new LinkedHashMap();

		Map<Long, String> receivedCoinsPrint = new LinkedHashMap();
		for (Map.Entry<Long, BigDecimal> e : receivedCoins.entrySet())
		{
			receivedCoinsPrint.put(e.getKey(), e.getValue().toPlainString());
		}
		blockExplorerBalance.put("received", receivedCoinsPrint);

		Map<Long, String> sentCoinsPrint = new LinkedHashMap();
		for (Map.Entry<Long, BigDecimal> e : sentCoins.entrySet())
		{
			if (e.getKey() == FEE_KEY)
			{
				sentCoinsPrint.put(e.getKey(), e.getValue().subtract(spentFee).toPlainString());
			}
			else
			{
				sentCoinsPrint.put(e.getKey(), e.getValue().toPlainString());
			}
		}
		blockExplorerBalance.put("sent", sentCoinsPrint);

		blockExplorerBalance.put("spentFee", spentFee.toPlainString());

		for (Map.Entry<Long, BigDecimal> assetAmounts : totalBalances.get(size - 1).entrySet())
		{
			total.put(assetAmounts.getKey(), assetAmounts.getValue().toPlainString());
		}
		blockExplorerBalance.put("total", total);

		output.put("balance", blockExplorerBalance);

		Map generatedBlocks = new LinkedHashMap();
		for (Map.Entry<String, Integer> e : blocksGeneratedCount.entrySet())
		{
			Map generatedInfo = new LinkedHashMap();
			generatedInfo.put("count", e.getValue());
			generatedInfo.put("fees", generatedFee.get(e.getKey()).toPlainString());
			generatedBlocks.put(e.getKey(), generatedInfo);
		}

		Map generatedInfo = new LinkedHashMap();
		generatedInfo.put("count", totalBlocksGeneratedCount);
		generatedInfo.put("fees", totalBlocksGeneratedFee.toPlainString());
		generatedBlocks.put("total", generatedInfo);

		output.put("generatedBlocks", generatedBlocks);

		output.put("txsCountOfAddr", txsCountOfAddr);

		Map nativeBalance = new LinkedHashMap();

		Map<Long, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetAmountTotal = new LinkedHashMap<>();

		for (String address : addresses) {

			Map<Long, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetAmountOfAddr = assetBalance(address);

			Map<Long, String> assetAmountOfAddrPrint = new LinkedHashMap<>();

			for (Map.Entry<Long, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetAmounts : assetAmountOfAddr.entrySet())
			{
				if (assetAmountTotal.containsKey(assetAmounts.getKey()))
				{
					Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = assetAmountTotal.get(assetAmounts.getKey());
					Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value = assetAmounts.getValue();
					balance = new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
							new Tuple2<BigDecimal, BigDecimal>(balance.a.a, balance.a.b.add(value.a.b)),
							new Tuple2<BigDecimal, BigDecimal>(balance.b.a, balance.b.b.add(value.b.b)),
							new Tuple2<BigDecimal, BigDecimal>(balance.c.a, balance.c.b.add(value.c.b)),
							new Tuple2<BigDecimal, BigDecimal>(balance.d.a, balance.d.b.add(value.d.b)),
							new Tuple2<BigDecimal, BigDecimal>(balance.e.a, balance.e.b.add(value.e.b))
							);

					assetAmountTotal.put(assetAmounts.getKey(), balance);
				}
				else
				{
					assetAmountTotal.put(assetAmounts.getKey(), assetAmounts.getValue());
				}

				assetAmountOfAddrPrint.put(assetAmounts.getKey(), assetAmounts.getValue().toString());
			}

			nativeBalance.put(address, assetAmountOfAddrPrint);
		}

		Map<Long, String> assetAmountTotalPrint = new LinkedHashMap<>();
		for (Map.Entry<Long, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetAmounts : assetAmountTotal.entrySet())
		{
			assetAmountTotalPrint.put(assetAmounts.getKey(), assetAmounts.getValue().toString());
		}

		nativeBalance.put("total", assetAmountTotalPrint);

		output.put("nativeBalance", nativeBalance);

		Map assetNames=new LinkedHashMap();

		for (Map.Entry<Long, BigDecimal> assetAmounts : totalBalances.get(size - 1).entrySet())
		{
			assetNames.put(assetAmounts.getKey(), Controller.getInstance().getAsset(assetAmounts.getKey()).getName());
		}

		output.put("assetNames", assetNames);

		Map txCountJSON = new LinkedHashMap();

		if (!showOnly.equals(""))
		{
			showWithoutMap.clear();
			int n = 1;
			for (int txCount : txsTypeCount) {
				if(txCount > 0)
				{
					if(!showOnlyMap.containsKey(String.valueOf(n)))
					{
						showWithoutMap.put(String.valueOf(n), true);
					}
				}

				n ++;
			}

			if(totalBlocksGeneratedCount > 0)
			{
				if(!showOnlyMap.containsKey("blocks"))
				{
					showWithoutMap.put("blocks", true);
				}
			}

			if(aTTxsCount > 0)
			{
				if(!showOnlyMap.containsKey("aTTxs"))
				{
					showWithoutMap.put("aTTxs", true);
				}
			}

			if(tradesCount > 0)
			{
				if(!showOnlyMap.containsKey("trades"))
				{
					showWithoutMap.put("trades", true);
				}
			}
		}

		if(txsCount > 0)
		{
			txCountJSON.put("txsCount", txsCount);
			Map txTypeCountJSON = new LinkedHashMap();
			int n = 1;
			for (int txCount : txsTypeCount) {
				if(txCount > 0)
				{
					txTypeCountJSON.put(n, txCount);
				}
				n ++;
			}
			txCountJSON.put("txsTypesCount", txTypeCountJSON);
		}
		if(totalBlocksGeneratedCount > 0)
		{
			txCountJSON.put("blocksCount", totalBlocksGeneratedCount);
		}
		if(aTTxsCount > 0)
		{
			txCountJSON.put("aTTxsCount", aTTxsCount);
		}
		if(tradesCount > 0)
		{
			txCountJSON.put("tradesCount", tradesCount);
		}

		txCountJSON.put("allCount",  tradesCount + aTTxsCount + totalBlocksGeneratedCount + txsCount);





		output.put("countTx", txCountJSON);

		output.put("txOnPage", txOnPage);

		output.put("filter", filter);

		output.put("allOnOnePage", allOnOnePage);

		output.put("showOnly", showOnly);

		output.put("showWithout", showWithout);

		int end = -1;

		int counter = size;

		Map<Integer, Map<String, Integer>> pagesStartEnd = new LinkedHashMap();
		Map<String, Integer> pageStartEnd = new LinkedHashMap();

		int onThisPage = 0;
		int pagesCounter = 0;

		int onThisPageCurent = 0;
		boolean firstPage = false;

		Iterator iterator;
		iterator = all.descendingIterator();

		while (iterator.hasNext()){

			BlExpUnit unit = (BlExpUnit) iterator.next();

			onThisPage ++;

			if(((unit.getUnit() instanceof Block) && (showWithoutMap.containsKey("blocks"))))
			{
				onThisPage --;
			}

			if(((unit.getUnit() instanceof Trade) && showWithoutMap.containsKey("trades")))
			{
				onThisPage --;
			}

			if(((unit.getUnit() instanceof AT_Transaction) && showWithoutMap.containsKey("aTTxs")))
			{
				onThisPage --;
			}

			if(((unit.getUnit() instanceof Transaction) && showWithoutMap.containsKey(String.valueOf(((Transaction)unit.getUnit()).getType()))))
			{
				onThisPage --;
			}

			if(!firstPage && onThisPage == 1)
			{
				pageStartEnd.put("start", counter);
				firstPage = true;

				if(start == -1)
				{
					start = counter;
				}
			}

			if(onThisPage >= txOnPage)
			{
				pageStartEnd.put("end", counter);

				onThisPage = 0;
				firstPage = false;
			}

			if(pageStartEnd.size() == 2)
			{
				pagesCounter ++;

				pagesStartEnd.put(pagesCounter, new LinkedHashMap(pageStartEnd));

				pageStartEnd.clear();
			}

			if(start != -1 && counter <= start && ((onThisPageCurent < txOnPage) || allOnOnePage))
			{
				if((unit.getUnit() instanceof Block) && (showWithoutMap.containsKey("blocks")))
				{
					counter--;
					continue;
				}

				if((unit.getUnit() instanceof Trade) && showWithoutMap.containsKey("trades"))
				{
					counter--;
					continue;
				}

				if((unit.getUnit() instanceof AT_Transaction) && showWithoutMap.containsKey("aTTxs"))
				{
					counter--;
					continue;
				}

				if((unit.getUnit() instanceof Transaction) && showWithoutMap.containsKey(String.valueOf(((Transaction)unit.getUnit()).getType())))
				{
					counter--;
					continue;
				}

				onThisPageCurent++;

				Map transactionJSON = new LinkedHashMap();

				transactionJSON.putAll(jsonUnitPrint(unit.getUnit(), null));

				Map tXbalanceChange = new LinkedHashMap();
				Map<Long, Boolean> assetIsChange = new LinkedHashMap(){{ put(FEE_KEY, true); }};

				for(Map.Entry<String, Map<Long, BigDecimal>> addrsMap : tXincomes.get(counter - 1).entrySet())
				{
					if (addresses.contains(addrsMap.getKey()))
					{
						Map<Long, String> tXaddrBalanceChange = new LinkedHashMap();

						for(Map.Entry<Long, BigDecimal> assetAmount : addrsMap.getValue().entrySet())
						{
							tXaddrBalanceChange.put(assetAmount.getKey(), assetAmount.getValue().toPlainString());
							assetIsChange.put(assetAmount.getKey(), true);
						}

						tXbalanceChange.put(addrsMap.getKey(), tXaddrBalanceChange);
					}
				}

				transactionJSON.put("tXbalanceChange", tXbalanceChange);

				Map<Long, String> tXbalance = new LinkedHashMap();

				for (Long assetKey : assetIsChange.keySet() )
				{
					tXbalance.put(assetKey, totalBalances.get( counter - 1 ).get(assetKey).toPlainString());
				}

				transactionJSON.put("tXbalance", tXbalance);

				output.put(counter, transactionJSON);

				end = counter;
			}

			counter--;
		}

		if(pageStartEnd.size() == 1)
		{
			pageStartEnd.put("end", 1);

			pagesCounter ++;

			pagesStartEnd.put(pagesCounter, new LinkedHashMap(pageStartEnd));
		}

		output.put("start", start);
		output.put("end", end);

		output.put("pages", pagesStartEnd);

		return output;
	}

	public Map jsonQueryATtx(String query)
	{
		Map output=new LinkedHashMap();

		int blockHeight = Integer.valueOf(query.split(":")[0]);
		int seq = Integer.valueOf(query.split(":")[1]);

		output.put("type", "atTransaction");

		output.put("atTransaction", query);

		LinkedHashMap<Tuple2<Integer, Integer>, AT_Transaction> atTxs = dcSet.getATTransactionMap().getATTransactions(blockHeight);

		AssetNames assetNames = new AssetNames();

		for(Entry<Tuple2<Integer, Integer>, AT_Transaction> e : atTxs.entrySet())
		{
			if(e.getValue().getSeq() == seq)
			{
				output.put(1, jsonUnitPrint(e.getValue(), assetNames));
			}
		}

		output.put("assetNames", assetNames.getMap());

		output.put("start", 1);
		output.put("end", 1);

		return output;
	}

	public Map jsonQueryTrade(String query)
	{
		Map output=new LinkedHashMap();
		AssetNames assetNames = new AssetNames();

		List<Object> all = new ArrayList<Object>();

		String[] signatures = query.split("/");

		Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> trade = dcSet.getTradeMap().get(Fun.t2(Base58.decodeBI(signatures[0]), Base58.decodeBI(signatures[1])));
		output.put("type", "trade");
		output.put("trade", query);

		all.add(trade);

		all.add(Controller.getInstance().getTransaction(Base58.decode(signatures[0])));
		all.add(Controller.getInstance().getTransaction(Base58.decode(signatures[1])));

		int size = all.size();

		output.put("start", size);
		output.put("end", 1);

		int counter = 0;
		for (Object unit : all) {
			output.put(size - counter, jsonUnitPrint(unit, assetNames));
			counter ++;
		}

		output.put("assetNames", assetNames.getMap());

		return output;
	}

	public Map jsonQueryPeers(int start)
	{


		Map output=new LinkedHashMap();
		PeersTableModel model_Peers =  new PeersTableModel();
		int rowCount = start+20;
		int column_Count = model_Peers.getColumnCount();

		for (int column=0; column < column_Count; column++ ){

			output.put("Label_"+ model_Peers.getColumnNameNO_Translate(column).replace(' ', '_'),  Lang.getInstance().translate_from_langObj(model_Peers.getColumnNameNO_Translate(column),langObj));
		}

		Map out_peers=new LinkedHashMap();
		//	if (rowCount> model_Peers.getRowCount()) rowCount = model_Peers.getRowCount();
		rowCount = model_Peers.getRowCount();
		for (int row = 0; row< rowCount; row++ ){
			Map out_peer=new LinkedHashMap();

			for (int column=0; column < column_Count; column++ ){
				out_peer.put(model_Peers.getColumnNameNO_Translate(column).replace(' ', '_'), model_Peers.getValueAt(row, column).toString());

			}
			out_peers.put(row, out_peer);
		}
		//		output.put("rowCount", rowCount);
		//		output.put("start", start);
		output.put("Label_No",  Lang.getInstance().translate_from_langObj("No.",langObj));
		output.put("Peers", out_peers);
		return output;
	}


	public Map jsonQueryStatements (int start){
		Map output=new LinkedHashMap();
		WEB_Statements_Table_Model_Search model_Statements =  new WEB_Statements_Table_Model_Search();
		int rowCount = start+20;
		int column_Count = model_Statements.getColumnCount();

		for (int column=0; column < column_Count; column++ ){

			output.put("Label_"+ model_Statements.getColumnNameNO_Translate(column).replace(' ', '_'),  Lang.getInstance().translate_from_langObj(model_Statements.getColumnNameNO_Translate(column),langObj));
		}

		Map out_Statements=new LinkedHashMap();
		//	if (rowCount> model_Peers.getRowCount()) rowCount = model_Peers.getRowCount();
		rowCount = model_Statements.getRowCount();
		for (int row = 0; row< rowCount; row++ ){
			Map out_statement=new LinkedHashMap();
			Transaction statement = model_Statements.get_Statement(row);
			out_statement.put("Block", statement.getBlockHeight(dcSet));
			out_statement.put("Seg_No", statement.getSeqNo(dcSet));
			out_statement.put("person_key", model_Statements.get_person_key(row));

			for (int column=0; column < column_Count; column++ ){
				String value = model_Statements.getValueAt(row, column).toString();
				if (value == null || value.isEmpty())
					value = "***";

				out_statement.put(model_Statements.getColumnNameNO_Translate(column).replace(' ', '_'), value);
			}
			out_Statements.put(row, out_statement);
		}
		//		output.put("rowCount", rowCount);
		//		output.put("start", start);
		output.put("Label_No",  Lang.getInstance().translate_from_langObj("No.",langObj));
		output.put("Label_block",  Lang.getInstance().translate_from_langObj("Block",langObj));
		output.put("Statements", out_Statements);
		return output;
	}


	public Map jsonQueryTemplates(int start)
	{
		Map output=new LinkedHashMap();

		SortableList<Long, ItemCls> it = dcSet.getItemTemplateMap().getList();


		int view_Row = 21;
		int end = start + view_Row;
		if (end > it.size()) end = it.size();

		output.put("start_row", start);
		int i;
		Map templatesJSON = new LinkedHashMap();
		for (i=start ; i< end; i++){

			TemplateCls template = (TemplateCls) it.get(i).getB();

			Map templateJSON=new LinkedHashMap();

			templateJSON.put("key", template.getKey());
			templateJSON.put("name", template.getName());
			templateJSON.put("description", template.getDescription());
			templateJSON.put("owner", template.getOwner().getAddress());

			templatesJSON.put(template.getKey(), templateJSON);
		}
		output.put("view_Row", view_Row);
		output.put("hasLess", start > view_Row);
		output.put("hasMore", end < it.size());
		output.put("templates", templatesJSON);
		output.put("label_table_key", Lang.getInstance().translate_from_langObj("Key",langObj));
		output.put("label_table_name", Lang.getInstance().translate_from_langObj("Name",langObj));
		output.put("label_table_creator", Lang.getInstance().translate_from_langObj("Creator",langObj));
		output.put("label_table_description", Lang.getInstance().translate_from_langObj("Description",langObj));
		output.put("Label_Later", Lang.getInstance().translate_from_langObj(">>",langObj));
		output.put("Label_Previous", Lang.getInstance().translate_from_langObj("<<",langObj));

		return output;
	}

	public Map jsonQueryStatuses(int start)
	{
		Map output=new LinkedHashMap();

		SortableList<Long, ItemCls> it = dcSet.getItemStatusMap().getList();


		int view_Row = 21;
		int end = start + view_Row;
		if (end > it.size()) end = it.size();

		output.put("start_row", start);
		int i;
		Map templatesJSON = new LinkedHashMap();
		for (i=start ; i< end; i++){

			StatusCls template = (StatusCls) it.get(i).getB();

			Map templateJSON=new LinkedHashMap();

			templateJSON.put("key", template.getKey());
			templateJSON.put("name", template.getName());
			templateJSON.put("description", template.getDescription());
			templateJSON.put("owner", template.getOwner().getAddress());

			templatesJSON.put(template.getKey(), templateJSON);
		}
		output.put("view_Row", view_Row);
		output.put("hasLess", start > view_Row);
		output.put("hasMore", end < it.size());
		output.put("templates", templatesJSON);
		output.put("label_table_key", Lang.getInstance().translate_from_langObj("Key",langObj));
		output.put("label_table_name", Lang.getInstance().translate_from_langObj("Name",langObj));
		output.put("label_table_creator", Lang.getInstance().translate_from_langObj("Creator",langObj));
		output.put("label_table_description", Lang.getInstance().translate_from_langObj("Description",langObj));
		output.put("Label_Later", Lang.getInstance().translate_from_langObj(">>",langObj));
		output.put("Label_Previous", Lang.getInstance().translate_from_langObj("<<",langObj));

		return output;
	}
	public Map jsonQuerySearchStatuses(String search)
	{

		List<ItemCls> listPerson = new ArrayList();
		if (search != ""){

			if (search.matches("\\d+") && dcSet.getItemStatusMap().contains(Long.valueOf(search))){
				listPerson.add(dcSet.getItemPersonMap().get(Long.valueOf(search)));
			}else{
				listPerson = dcSet.getItemStatusMap().get_By_Name(search, false);
			}
		}
		Map output=new LinkedHashMap();
		Map templatesJSON = new LinkedHashMap();
		if (listPerson != null){
			for(ItemCls pers:listPerson){

				StatusCls template = (StatusCls) pers;

				Map templateJSON=new LinkedHashMap();

				templateJSON.put("key", template.getKey());
				templateJSON.put("name", template.getName());
				templateJSON.put("description", template.getDescription());
				templateJSON.put("owner", template.getOwner().getAddress());

				templatesJSON.put(template.getKey(), templateJSON);
			}
			output.put("view_Row", listPerson.size()-1);
			output.put("hasLess", false);
			output.put("hasMore", true);
			output.put("templates", templatesJSON);
			output.put("label_table_key", Lang.getInstance().translate_from_langObj("Key",langObj));
			output.put("label_table_name", Lang.getInstance().translate_from_langObj("Name",langObj));
			output.put("label_table_creator", Lang.getInstance().translate_from_langObj("Creator",langObj));
			output.put("label_table_description", Lang.getInstance().translate_from_langObj("Description",langObj));
			output.put("Label_Later", Lang.getInstance().translate_from_langObj(">>",langObj));
			output.put("Label_Previous", Lang.getInstance().translate_from_langObj("<<",langObj));
		}
		return output;
	}

	public Map jsonQueryTemplate(Long key) {
		Map output=new LinkedHashMap();

		TemplateCls template = (TemplateCls)dcSet.getItemTemplateMap().get(key);

		Map templateJSON = new LinkedHashMap();
		templateJSON.put("key", template.getKey());
		templateJSON.put("name", template.getName());
		templateJSON.put("description", template.getDescription());
		templateJSON.put("owner", template.getOwner().getAddress());

		output.put("template", templateJSON);

		output.put("label_Template", Lang.getInstance().translate_from_langObj("Template", langObj));
		output.put("label_Key", Lang.getInstance().translate_from_langObj("Key", langObj));
		output.put("label_Creator", Lang.getInstance().translate_from_langObj("Creator", langObj));
		output.put("label_Description", Lang.getInstance().translate_from_langObj("Description", langObj));

		return output;
	}

	public Map jsonQueryStatus(Long key) {
		Map output=new LinkedHashMap();

		StatusCls template = (StatusCls)dcSet.getItemStatusMap().get(key);

		Map templateJSON = new LinkedHashMap();
		templateJSON.put("key", template.getKey());
		templateJSON.put("name", template.getName());
		templateJSON.put("description", template.getDescription());
		templateJSON.put("owner", template.getOwner().getAddress());

		output.put("status", templateJSON);

		output.put("label_Template", Lang.getInstance().translate_from_langObj("Status", langObj));
		output.put("label_Key", Lang.getInstance().translate_from_langObj("Key", langObj));
		output.put("label_Creator", Lang.getInstance().translate_from_langObj("Creator", langObj));
		output.put("label_Description", Lang.getInstance().translate_from_langObj("Description", langObj));

		return output;
	}


	private Map jsonQueryStatement(String block, String seg_No) {
		// TODO Auto-generated method stub
		Map output=new LinkedHashMap();

		R_SignNote trans = (R_SignNote) dcSet.getTransactionFinalMap().getTransaction(new Integer(block), new Integer(seg_No));

		//	output.put("Label_title", Lang.getInstance().translate_from_langObj("Title",langObj));
		output.put("Label_statement", Lang.getInstance().translate_from_langObj("Statement",langObj));
		output.put("Label_creator", Lang.getInstance().translate_from_langObj("Creator",langObj));
		output.put("Label_date", Lang.getInstance().translate_from_langObj("Date",langObj));
		output.put("Label_block", Lang.getInstance().translate_from_langObj("Block",langObj));
		output.put("Label_seg_No", Lang.getInstance().translate_from_langObj("Seg_no",langObj));
		output.put("Label_No",  Lang.getInstance().translate_from_langObj("No.",langObj));

		output.put("block",block);
		output.put("Seg_No",seg_No);

		TemplateCls statement = (TemplateCls)ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, trans.getKey());

		if (!trans.isEncrypted()) {



			if (trans.getVersion()==2){
				// version 2
				Tuple4<String, String, JSONObject, HashMap<String, Tuple2<Boolean, byte[]>>> map_Data;

				try {
					map_Data = trans.parse_Data_V2();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}

				String str_HTML = "<br>";
				if ( map_Data.b != null) str_HTML = "<b>"+Lang.getInstance().translate_from_langObj("Title", langObj) + ": </b>" +  map_Data.b +"<br>";

				JSONObject jSON = map_Data.c;
				// parse JSON
				if (jSON != null){



					// V2.0 Template
					if (jSON.containsKey("Template") ){
						Long key = new Long(jSON.get("Template")+"");
						TemplateCls template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE,key);
						if (template != null){
							String description = template.getDescription();

							// Template Params
							if (jSON.containsKey("Statement_Params") ){

								String str = jSON.get("Statement_Params").toString();
								JSONObject params = new JSONObject();;
								try {
									params = (JSONObject) JSONValue.parseWithException(str);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								Set<String> kS = params.keySet();
								for (String s:kS){
									description = description.replace("{{" + s + "}}", (CharSequence) params.get(s));
								}

							}
							str_HTML+= description + "<br>";
						}
					}
					// V2.1 Template
					if ( jSON.containsKey("TM")){
						Long key = new Long(jSON.get("TM")+"");
						TemplateCls template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE,key);
						if (template != null){
							String description = template.getDescription();

							// Template Params
							if (jSON.containsKey("PR")){
								String str = jSON.get("PR").toString();
								JSONObject params = new JSONObject();;
								try {
									params = (JSONObject) JSONValue.parseWithException(str);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								Set<String> kS = params.keySet();
								for (String s:kS){
									description = description.replace("{{" + s + "}}", (CharSequence) params.get(s));
								}




							}
							//str_HTML+= description + "<br>";
							str_HTML += description + "<br>";

						}

					}
					// Message v2.0
					if (jSON.containsKey("Message")) str_HTML += "<b>"+ Lang.getInstance().translate_from_langObj("Message", langObj)
							+ ": </b><br>"+ jSON.get("Message") +"<br>";
					// v 2.1
					if (jSON.containsKey("MS")) {
						String mess = (String)jSON.get("MS");
						str_HTML += "<b>"+ Lang.getInstance().translate_from_langObj("Message", langObj)
								+ ": </b><br>" + mess + "<br><br>";

					}
					// Hashes
					// v2.0
					if (jSON.containsKey("Hashes")){
						str_HTML += "<b>"+Lang.getInstance().translate_from_langObj("Hashes", langObj) + ": </b><br>";
						String hasHes = "";
						String str = jSON.get("Hashes").toString();
						JSONObject params = new JSONObject();
						try {
							params = (JSONObject) JSONValue.parseWithException(str);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Set<String> kS = params.keySet();

						int i = 1;
						for (String s:kS){
							hasHes += i + " " + s + " " + params.get(s) + "<br>";
						}

						str_HTML += hasHes + "<br>";
					}
					// v2.1
					if (jSON.containsKey("HS")){

						str_HTML += "<b>"+Lang.getInstance().translate_from_langObj("Hashes", langObj) + ": <b><br>";
						String hasHes = "";
						String str = jSON.get("HS").toString();
						JSONObject params = new JSONObject();
						try {
							params = (JSONObject) JSONValue.parseWithException(str);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Set<String> kS = params.keySet();

						int i = 1;
						for (String s:kS){
							hasHes += i + " " + s + " " + params.get(s) + "<br>";
						}

						str_HTML += hasHes + "<br>";

					}


				}
				// parse Ffiles
				// v2.0
				if (jSON.containsKey("&*&*%$$%_files_#$@%%%") ){
					str_HTML += "<b>"+Lang.getInstance().translate_from_langObj("Files", langObj) + ": </b><br>";
					String hasHes = "";
					String str = jSON.get("&*&*%$$%_files_#$@%%%").toString();
					JSONObject params = new JSONObject();
					try {
						params = (JSONObject) JSONValue.parseWithException(str);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Set<String> kS = params.keySet();

					int i = 1;
					JSONObject ss = new JSONObject();
					for (String s:kS){


						ss = (JSONObject) params.get(s);

						hasHes += i + " "  + ss.get("File_Name") + "<br>";
					}

					str_HTML += hasHes + "<br>";

				}
				//	v 2.1
				if (jSON.containsKey("F") ){

					str_HTML += "<b>"+Lang.getInstance().translate_from_langObj("Files", langObj) + ": </b><br>";
					String hasHes = "";
					String str = jSON.get("F").toString();
					JSONObject params = new JSONObject();
					try {
						params = (JSONObject) JSONValue.parseWithException(str);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Set<String> kS = params.keySet();

					int i = 1;
					JSONObject ss = new JSONObject();
					for (String s:kS){

						ss = (JSONObject) params.get(s);

						hasHes += i + " "  + ss.get("FN") + "<br>";
					}

					str_HTML += hasHes + "<br>";

				}



				output.put("statement", str_HTML);
				//output.put("statement", str_HTML);
			}


			else{

				// version 1
				try {

					Set<String> kS;
					String description ="";
					String str;
					JSONObject params = new JSONObject();
					JSONObject data = new JSONObject();
					TemplateCls template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, statement.getKey());
					if (template != null){
						description = template.getDescription();
						data = (JSONObject) JSONValue.parseWithException(new String(trans.getData(), Charset.forName("UTF-8")));
						str = data.get("Statement_Params").toString();
						params = (JSONObject) JSONValue.parseWithException(str);
						kS = params.keySet();
						for (String s:kS){
							description = description.replace("{{" + s + "}}", (CharSequence) params.get(s));
						}
					}
					String hasHes = "";
					str = data.get("Hashes").toString();
					params = (JSONObject) JSONValue.parseWithException(str);
					kS = params.keySet();

					int i = 1;
					for (String s:kS){
						hasHes += i + " " + s + " " + params.get(s) + "<br>";
					}



					String sT = "<br>"
							+ data.get("Title") + "<br><br>"
							+ description + "<br><br>"
							+ data.get("Message") + "<br><br>"
							+ hasHes;



					//output.put("statement", library.to_HTML(sT));
					output.put("statement", sT);


				} catch (ParseException e) {





					output.put("statement", new String( trans.getData(), Charset.forName("UTF-8") ));

				}
			}

		} else {

			TemplateCls template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, statement.getKey());
			output.put("statement",template.getName() + "<br>"
					+  Lang.getInstance().translate_from_langObj("Encrypted",langObj));
		}




		output.put("creator", trans.getCreator().getPersonAsString());


		if (trans.getCreator().getPerson() !=null) {
			output.put("creator_key", trans.getCreator().getPerson().b.getKey());
			output.put("creator_name", trans.getCreator().getPerson().b.getName());
		} else {
			output.put("creator_key", "");
			output.put("creator_name", "");
		}

		//	output.put("name", person.getName());
		output.put("date", df.format(new Date(trans.getTimestamp())).toString());
		//	output.put("description", person.getDescription());

		// vouchers
		output.put("Label_vouchs", Lang.getInstance().translate_from_langObj("Certified",langObj));
		output.put("Label_accounts_table_adress", Lang.getInstance().translate_from_langObj("Address",langObj));
		output.put("Label_accounts_table_data", Lang.getInstance().translate_from_langObj("Date",langObj));
		output.put("Label_accounts_table_creator", Lang.getInstance().translate_from_langObj("Creator",langObj));

		Map vouchesJSON=new LinkedHashMap();

		WEB_Statements_Vouch_Table_Model table_sing_model = new WEB_Statements_Vouch_Table_Model(trans);
		int rowCount = table_sing_model.getRowCount();

		if (rowCount >0) {
			for (int i = 0; i<rowCount; i++) {

				Transaction vouch_Tr = (Transaction)  table_sing_model.getValueAt(i, 3);
				Map vouchJSON=new LinkedHashMap();
				vouchJSON.put("date", vouch_Tr.viewTimestamp());
				vouchJSON.put("block", "" + vouch_Tr.getBlockHeight(dcSet));
				vouchJSON.put("Seg_No", "" + vouch_Tr.getSeqNo(dcSet));
				vouchJSON.put("creator",  vouch_Tr.getCreator().getAddress());

				Tuple2<Integer, PersonCls> personInfo = vouch_Tr.getCreator().getPerson();
				if (personInfo != null) {
					PersonCls person = personInfo.b;
					vouchJSON.put("creator_name", person.getName());
					vouchJSON.put("creator_key", "" + person.getKey());
				}

				vouchesJSON.put(i, vouchJSON);
			}
		}
		output.put("vouches", vouchesJSON);

		return output;
	}

	public Map jsonQueryTX(String query)
	{

		Map output=new LinkedHashMap();
		AssetNames assetNames = new AssetNames();

		TreeSet<BlExpUnit> all = new TreeSet<>();
		Map<Tuple2<BigInteger, BigInteger>, Trade> trades = new TreeMap<Tuple2<BigInteger, BigInteger>, Trade>();

		String[] signatures = query.split(",");

		byte[] signatureBytes = null;

		output.put("type", "transaction");

		for (int i = 0; i < signatures.length; i++) {
			signatureBytes = Base58.decode(signatures[i]);
			Transaction transaction = Controller.getInstance().getTransaction(signatureBytes);
			transaction.setDC(dcSet, false);
			List<Transaction>tt = new ArrayList<Transaction>();
			tt.add(transaction);
			//	output.put("transaction_Header",Transactions_JSON(tt));
			output.put("body", WEB_Transactions_HTML.getInstance().get_HTML( tt.get(0), langObj));
			//		output.put("Json", transaction.toJson().toString());

			//		output.put("",transaction);

			/*	all.add( new BlExpUnit( transaction.getBlock(dcSet).getHeight(dcSet), transaction.getSeqNo(dcSet), transaction));

			if(transaction instanceof CreateOrderTransaction)
			{
				Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order =  ((CreateOrderTransaction)transaction).getOrder();

				SortableList<Tuple2<BigInteger, BigInteger>, Trade> tradesBuf = Controller.getInstance().getTrades(order);
				for (Pair<Tuple2<BigInteger, BigInteger>, Trade> pair : tradesBuf) {
					trades.put(pair.getA(), pair.getB());
				}
			}
		}

		for(Map.Entry<Tuple2<BigInteger, BigInteger>, Trade> trade : trades.entrySet())
		{
			Transaction txInitiator = Controller.getInstance().getTransaction(trade.getValue().getInitiator().toByteArray());

			Transaction txTarget = Controller.getInstance().getTransaction(trade.getValue().getTarget().toByteArray());

			all.add( new BlExpUnit(txInitiator.getBlock(dcSet).getHeight(dcSet), txTarget.getBlock(dcSet).getHeight(dcSet), txInitiator.getSeqNo(dcSet), txTarget.getSeqNo(dcSet), trade.getValue() ) );
		}

		int size = all.size();

		output.put("start", size);
		output.put("end", 1);

		int counter = 0;
		for (BlExpUnit unit : all) {
			output.put(size - counter, jsonUnitPrint(unit.getUnit(), assetNames));
			counter ++;
			 */		}

		output.put("Label_Transaction", Lang.getInstance().translate_from_langObj("Transaction", langObj));

		return output;
	}

	public Map jsonQueryBlock(String query, int transPage)
	{

		Map output=new LinkedHashMap();
		List<Object> all = new ArrayList<Object>();
		int[] txsTypeCount = new int[256];
		int aTTxsCount = 0;
		Block block;
		AssetNames assetNames = new AssetNames();

		if(query.matches("\\d+"))
		{
			block = Controller.getInstance().getBlockByHeight(dcSet, Integer.parseInt(query));
		}
		else if (query.equals("last"))
		{
			block = getLastBlock();
		}
		else
		{
			block = Controller.getInstance().getBlock(Base58.decode(query));
		}

		for(Transaction transaction: block.getTransactions())
		{
			all.add(transaction);
			txsTypeCount[transaction.getType() - 1] ++;
		}

		// Transactions view
		output.put("Transactions", Transactions_JSON(block.getTransactions(), (transPage - 1) * 100, transPage * 100));
		output.put("pageCount", (int)Math.ceil((block.getTransactions().size()) / 100d));
		output.put("pageNumber", transPage);

		int txsCount = all.size();

		LinkedHashMap<Tuple2<Integer, Integer>, AT_Transaction> atTxs = dcSet.getATTransactionMap().getATTransactions(block.getHeight(dcSet));

		for(Entry<Tuple2<Integer, Integer>, AT_Transaction> e : atTxs.entrySet())
		{
			all.add(e.getValue());
			aTTxsCount ++;
		}

		output.put("type", "block");

		output.put("blockSignature", Base58.encode(block.getSignature()));
		output.put("blockHeight", block.getHeight(dcSet));

		if(block.getParent(dcSet) != null)
		{
			output.put("parentBlockSignature", Base58.encode(block.getParent(dcSet).getSignature()));
		}

		if(block.getChild(dcSet) != null)
		{
			output.put("childBlockSignature", Base58.encode(block.getChild(dcSet).getSignature()));
		}

		int size = all.size();

		Map txCountJSON = new LinkedHashMap();

		if(txsCount > 0)
		{
			txCountJSON.put("txsCount", txsCount);
			Map txTypeCountJSON = new LinkedHashMap();
			int n = 1;
			for (int txCount : txsTypeCount) {
				if(txCount > 0)
				{
					txTypeCountJSON.put(n, txCount);
				}
				n ++;
			}
			txCountJSON.put("txsTypesCount", txTypeCountJSON);
		}

		if(aTTxsCount > 0)
		{
			txCountJSON.put("aTTxsCount", aTTxsCount);
		}

		txCountJSON.put("allCount", txsCount);

		output.put("countTx", txCountJSON);

		BigDecimal totalAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		for (Transaction transaction : block.getTransactions()) {
			for (Account account : transaction.getInvolvedAccounts()) {
				BigDecimal amount = transaction.getAmount(account);
				if(amount.compareTo(BigDecimal.ZERO) > 0)
				{
					totalAmount = totalAmount.add(amount);
				}
			}
		}

		output.put("totalAmount", totalAmount.toPlainString());

		BigDecimal totalATAmount = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

		for(Map.Entry<Tuple2<Integer, Integer> , AT_Transaction> e : atTxs.entrySet())
		{
			totalATAmount = totalATAmount.add(BigDecimal.valueOf( e.getValue().getAmount() , BlockChain.AMOUNT_DEDAULT_SCALE));
		}

		output.put("totalATAmount", totalATAmount.toPlainString());
		//output.put("aTfee", block.getATfee().toPlainString());
		output.put("totalFee", block.getTotalFee().toPlainString());
		output.put("version", block.getVersion());


		output.put("start", size+1);
		output.put("end", 1);

		Map assetsJSON=new LinkedHashMap();

		int counter = 0;

		//	for(Object unit: all)
		//	{
		//		counter ++;

		//		output.put(counter, jsonUnitPrint(unit, assetNames));
		//	}




		{
			Map transactionJSON = new LinkedHashMap();
			Map transactionDataJSON = new LinkedHashMap();

			transactionDataJSON = new LinkedHashMap();
			transactionDataJSON.put("timestamp", block.getTimestamp(dcSet));
			transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(block.getTimestamp(dcSet)));

			int height = block.getHeight(dcSet);
			transactionDataJSON.put("confirmations", getHeight() - height + 1 );
			transactionDataJSON.put("height", height);

			transactionDataJSON.put("generator", block.getCreator().getAddress());
			transactionDataJSON.put("signature", Base58.encode(block.getSignature()));

			transactionDataJSON.put("generatingBalance", block.getForgingValue());
			//transactionDataJSON.put("atFees", block.getATfee().toPlainString());
			transactionDataJSON.put("reference", Base58.encode(block.getReference()));
			transactionDataJSON.put("generatorSignature", Base58.encode(block.getSignature()));
			//transactionDataJSON.put("transactionsSignature", Base58.encode(block.getTransactionsSignature()));
			transactionDataJSON.put("version", block.getVersion());

			transactionDataJSON.put("fee", block.getTotalFee().toPlainString());

			transactionJSON.put("type", "block");
			transactionJSON.put("block", transactionDataJSON);

			output.put(counter + 1, transactionJSON);
		}

		//	output.put("assetNames", assetNames.getMap());

		//	output.put("totalBalance", assetsJSON);

		output.put("label_block", Lang.getInstance().translate_from_langObj("Block", langObj));
		output.put("label_Block_version", Lang.getInstance().translate_from_langObj("Block version", langObj));
		output.put("label_Transactions_count", Lang.getInstance().translate_from_langObj("Transactions count", langObj));
		output.put("label_Total_Amount", Lang.getInstance().translate_from_langObj("Total Amount", langObj));
		output.put("label_Total_AT_Amount", Lang.getInstance().translate_from_langObj("Total AT Amount", langObj));
		output.put("label_Total_Fee", Lang.getInstance().translate_from_langObj("Total Fee", langObj));
		output.put("label_Parent_block", Lang.getInstance().translate_from_langObj("Parent block", langObj));
		output.put("label_Current_block", Lang.getInstance().translate_from_langObj("Current block", langObj));
		output.put("label_Child_block", Lang.getInstance().translate_from_langObj("Child block", langObj));
		output.put("label_Including", Lang.getInstance().translate_from_langObj("Including", langObj));
		output.put("label_Signature", Lang.getInstance().translate_from_langObj("Signature", langObj));


		return output;
	}


	public Map jsonQueryUnconfirmedTXs()
	{
		Map output=new LinkedHashMap();
		List<Transaction> all = new ArrayList<Transaction>();

		AssetNames assetNames = new AssetNames();

		all.addAll(Controller.getInstance().getUnconfirmedTransactions(0, 100, true));

		output.put("type", "unconfirmed");

		int size = all.size();

		output.put("start", size);

		if(size>0)
		{
			output.put("end", 1);
		}
		else
		{
			output.put("end", 0);
		}

		int counter = 0;
		for(Object unit: all)
		{
			counter ++;

			output.put(counter, jsonUnitPrint(unit, assetNames));
		}

		return output;
	}

	class AssetNames
	{
		private Map<Long, String> assetNames;

		public AssetNames()
		{
			assetNames = new TreeMap<Long, String>();
		}

		public void setKey(long key)
		{
			if(!assetNames.containsKey(key))
			{
				assetNames.put(key, Controller.getInstance().getAsset(key).getName());
			}
		}

		public Map<Long, String> getMap()
		{
			return assetNames;
		}
	}

	class Balance
	{
		private Map<Long, BigDecimal> totalBalance;
		private Map<Long, BigDecimal> transactionBalance;
		public Balance()
		{
			totalBalance = new TreeMap<Long, BigDecimal>();
			transactionBalance = new TreeMap<Long, BigDecimal>();
		}
		public void setTotalBalance(long key, BigDecimal amount)
		{
			totalBalance.put(key, amount);
		}

		public void addTotalBalance(long key, BigDecimal amount)
		{
			if(totalBalance.containsKey(key))
			{
				totalBalance.put(key, totalBalance.get(key).add(amount));
			}
			else
			{
				totalBalance.put(key, amount);
			}
		}

		public void setTransactionBalance(long key, BigDecimal amount)
		{
			transactionBalance.put(key, amount);
		}

		public void addTransactionBalance(long key, BigDecimal amount)
		{
			if(transactionBalance.containsKey(key))
			{
				transactionBalance.put(key, transactionBalance.get(key).add(amount));
			}
			else
			{
				transactionBalance.put(key, amount);
			}
		}

		public BigDecimal getTransactionBalance(long key)
		{
			if(transactionBalance.containsKey(key))
			{
				return transactionBalance.get(key);
			}
			else
			{
				return BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
			}
		}

		public BigDecimal getTotalBalance(long key)
		{
			if(totalBalance.containsKey(key))
			{
				return totalBalance.get(key);
			}
			else
			{
				return BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
			}
		}

		public Map<Long, BigDecimal> getTotalBalance()
		{
			return totalBalance;
		}

		public Map<Long, BigDecimal> getTransactionBalance()
		{
			return transactionBalance;
		}

		public void setFromTransactionToTotalBalance()
		{
			for(Map.Entry<Long, BigDecimal> e : transactionBalance.entrySet()){
				if(totalBalance.containsKey(e.getKey()))
				{
					totalBalance.put(e.getKey(), totalBalance.get(e.getKey()).add(e.getValue()));
				}
				else
				{
					totalBalance.put(e.getKey(), e.getValue());
				}
			}
		}

		public void copyTotalBalanceFrom(Map<Long, BigDecimal> fromTotalBalance)
		{
			for(Map.Entry<Long, BigDecimal> e : fromTotalBalance.entrySet())
			{
				totalBalance.put(e.getKey(), e.getValue());
			}
		}
	}

	public class BigDecimalComparator implements Comparator<Tuple2<String, BigDecimal>> {

		@Override
		public int compare(Tuple2<String, BigDecimal> a, Tuple2<String, BigDecimal> b)
		{
			try
			{
				return a.b.compareTo(b.b);
			}
			catch(Exception e)
			{
				return 0;
			}
		}

	}

	public class BigDecimalComparator_C implements Comparator<Tuple3<String, BigDecimal, BigDecimal>> {

		@Override
		public int compare(Tuple3<String, BigDecimal, BigDecimal> a, Tuple3<String, BigDecimal, BigDecimal> b)
		{
			try
			{
				return a.c.compareTo(b.c);
			}
			catch(Exception e)
			{
				return 0;
			}
		}

	}


	public int getHeight() {

		//GET LAST BLOCK
		//byte[] lastBlockSignature = dcSet.getBlockMap().getLastBlockSignature();
		//RETURN HEIGHT
		//return dcSet.getBlockSignsMap().getHeight(lastBlockSignature);
		return dcSet.getBlockMap().size();
	}
	public Tuple2<Integer, Long> getHWeightFull() {

		//RETURN HEIGHT
		return Controller.getInstance().getBlockChain().getHWeightFull(dcSet);
	}

	public Block getLastBlock()
	{
		return dcSet.getBlockMap().last();
	}

	public static class Stopwatch {

		private long start;

		/**
		 * Create a stopwatch object.
		 */
		public Stopwatch() {
			start = System.currentTimeMillis();
		}


		/**
		 * Return elapsed time (in seconds) since this object was created.
		 */
		public double elapsedTime() {
			long now = System.currentTimeMillis();
			return (now - start);
		}
		public double elapsedTime0() {
			long now = System.currentTimeMillis();
			long start0 = start;
			start = System.currentTimeMillis();
			return (now - start0);
		}

	}





}
