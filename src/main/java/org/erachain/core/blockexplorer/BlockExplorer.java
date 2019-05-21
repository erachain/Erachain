package org.erachain.core.blockexplorer;

import org.apache.commons.net.util.Base64;
import org.erachain.at.ATTransaction;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.Template;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.payment.Payment;
import org.erachain.core.transaction.*;
import org.erachain.core.voting.Poll;
import org.erachain.core.voting.PollOption;
import org.erachain.database.SortableList;
import org.erachain.database.FilteredByStringArray;
import org.erachain.datachain.DCMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.gui.models.PeersTableModel;
import org.erachain.gui.models.PersonAccountsModel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.mapdb.Fun;
import org.mapdb.Fun.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.*;
import java.util.Map.Entry;

// 30/03 ++ asset - Trans_Amount

@SuppressWarnings({"unchecked", "rawtypes"})
public class BlockExplorer {

    public static final int pageSize = 25;
    private static final String LANG_DEFAULT = "en";
    private static final Logger logger = LoggerFactory.getLogger(BlockExplorer.class);
    private volatile static BlockExplorer blockExplorer;
    private JSONObject langObj;
    private Locale local = new Locale("ru", "RU"); // Date format
    private DateFormat df = DateFormat.getDateInstance(DateFormat.DATE_FIELD, local); // for
    private String langFile;
    private DCSet dcSet;
    private Map output;

    public static BlockExplorer getInstance() {
        if (blockExplorer == null) {
            blockExplorer = new BlockExplorer();
            blockExplorer.dcSet = DCSet.getInstance();
        }
        return blockExplorer;
    }

    public Map getOutput() {
        return output;
    }

    public void makePage(Class type, int start, int pageSize,
                         Map output, JSONObject langObj) {

        DCMap map = dcSet.getMap(type);
        ExplorerJsonLine element;
        int size = map.size();

        if (start < 1 || start > size && size > 0) {
            start = size;
        }
        output.put("start", start);

        int key = start;
        JSONArray array = new JSONArray();

        while (key > start - pageSize && key > 0) {
            element = (ExplorerJsonLine) map.get(key--);
            if (element != null) {
                array.add(element.jsonForExolorerPage(langObj));
            }
        }

        output.put("pageItems", array);
        output.put("pageSize", pageSize);
        output.put("listSize", map.size());
        output.put("lastNumber", key);

    }

    public void makePage(Class type, long start, int pageSize,
                         Map output, JSONObject langObj) {

        DCMap map = dcSet.getMap(type);
        ExplorerJsonLine element;
        long size = map.size();

        if (start < 1 || start > size && size > 0) {
            start = size;
        }
        output.put("start", start);

        long key = start;
        JSONArray array = new JSONArray();

        while (key > start - pageSize && key > 0) {
            element = (ExplorerJsonLine) map.get(key--);
            if (element != null) {
                array.add(element.jsonForExolorerPage(langObj));
            }

        }

        output.put("pageItems", array);
        output.put("pageSize", pageSize);
        output.put("listSize", map.size());
        output.put("lastNumber", key);

    }

    public void makePage(Class type, List keys, int start, int pageSize,
                         Map output, JSONObject langObj) {

        int size = keys.size();

        if (start < 1 || start > size && size > 0) {
            start = size;
        }
        output.put("start", start);

        int index = start;
        JSONArray array = new JSONArray();

        if (size > 0) {
            DCMap map = dcSet.getMap(type);
            ExplorerJsonLine element;

            while (index > start - pageSize && index > 0) {
                element = (ExplorerJsonLine) map.get(keys.get(--index));
                if (element != null) {
                    array.add(element.jsonForExolorerPage(langObj));
                }
            }
        }

        output.put("pageItems", array);
        output.put("pageSize", pageSize);
        output.put("listSize", keys.size());

    }

    public Map jsonQueryPages(Class type, int start, int pageSize) {
        Map result = new LinkedHashMap();
        AdderHeadInfo.addHeadInfoCap(type, result, dcSet, langObj);
        makePage(type, start, pageSize, result, langObj);
        return result;
    }

    public Map jsonQueryPages(Class type, long start, int pageSize) {
        Map result = new LinkedHashMap();
        AdderHeadInfo.addHeadInfoCap(type, result, dcSet, langObj);
        makePage(type, start, pageSize, result, langObj);
        return result;
    }

    public Map jsonQuerySearchPages(Class type, String search, int start, int pageSize) throws WrongSearchException, Exception {
        //Результирующий сортированный в порядке добавления словарь(map)
        Map result = new LinkedHashMap();
        List<Object> keys = new ArrayList();
        //Добавить шапку в JSON. Для интернационализации названий - происходит перевод соответствующих элементов.
        //В зависимости от выбранного языка(ru,en)
        AdderHeadInfo.addHeadInfoCap(type, result, dcSet, langObj);

        DCMap map = dcSet.getMap(type);

        try {
            //Если в строке ввели число
            if (search.matches("\\d+")) {
                if (map.contains(Long.valueOf(search))) {
                    //Элемент найден - добавляем его
                    keys.add(Integer.valueOf(search));
                    //Не отображать для одного элемента навигацию и пагинацию
                    result.put("notDisplayPages", "true");
                }
            } else {
                //Поиск элементов по имени
                keys = ((FilteredByStringArray)map).getKeysByFilterAsArray(search, 0, 100);
            }
        } catch (Exception e) {
            logger.error("Wrong search while process assets... " + e.getMessage());
            throw new WrongSearchException();
        }
        if (keys == null || keys.isEmpty()) {
            logger.info("Wrong search while process assets... ");
            throw new WrongSearchException();
        }

        makePage(type, keys, start, pageSize, result, langObj);

        return result;
    }

    public static String timestampToStr ( long timestamp){
        return DateTimeFormat.timestamptoString(timestamp);
    }

    @SuppressWarnings("static-access")
    public Map jsonQueryMain(UriInfo info) throws WrongSearchException, Exception {
        Stopwatch stopwatchAll = new Stopwatch();
        long start = 0;
        start = checkAndGetIntParam(info, start, "start");

        output = new LinkedHashMap();
        output.put("search", "block");

        //lang
        if (!info.getQueryParameters().containsKey("lang")) {
            langFile = LANG_DEFAULT + ".json";
        } else {
            langFile = info.getQueryParameters().getFirst("lang") + ".json";
        }

        logger.info("try lang file: " + langFile);

        langObj = Lang.openLangFile(langFile);

        List<Tuple2<String, String>> langs = Lang.getInstance().getLangListToWeb();

        Map lang_list = new LinkedHashMap();
        for (int i = 0; i < langs.size(); i++) {
            Map lang_par = new LinkedHashMap();
            lang_par.put("ISO", langs.get(i).a);
            lang_par.put("name", langs.get(i).b);
            lang_list.put(i, lang_par);
        }
        output.put("Lang", lang_list);
        //Основное меню. заголовки и их перевод на выбранный язык
        output.put("id_home2", Lang.getInstance().translateFromLangObj("Blocks", langObj));
        output.put("id_menu_top_100", Lang.getInstance().translateFromLangObj("Top 100 Richest", langObj));
        output.put("id_menu_percons", Lang.getInstance().translateFromLangObj("Persons", langObj));
        output.put("id_menu_pals_asset", Lang.getInstance().translateFromLangObj("Polls", langObj));
        output.put("id_menu_assets", Lang.getInstance().translateFromLangObj("Assets", langObj));
        output.put("id_menu_aTs", Lang.getInstance().translateFromLangObj("ATs", langObj));
        output.put("id_menu_transactions", Lang.getInstance().translateFromLangObj("Transactions", langObj));

        //информация о последнем блоке
        output.put("lastBlock", jsonQueryLastBlock());

        //todo Gleb непонятно зачем - выпиливаю
//        if (info.getQueryParameters().containsKey("balance")) {
//            for (String address : info.getQueryParameters().get("balance")) {
//                output.put(address, jsonQueryBalance(address));
//            }
//
//        }

        if (info.getQueryParameters().containsKey("q")) {
            if (info.getQueryParameters().containsKey("search")) {
                String type = info.getQueryParameters().getFirst("search");
                String search = info.getQueryParameters().getFirst("q");
                switch (type) {
                    case "txs":
                    case "transactions":
                    case "transaction":
                        //search transactions
                        output.put("search", "transaction");
                        output.putAll(jsonQuerySearchPages(Transaction.class, search, (int)start, pageSize));

                        break;
                    case "persons":
                    case "person":
                        //search persons
                        output.put("search", "person");
                        output.putAll(jsonQuerySearchPages(PersonCls.class, search, (int)start, pageSize));

                        break;
                    case "assets":
                    case "asset":
                        //search assets
                        output.put("search", "asset");
                        output.putAll(jsonQuerySearchPages(AssetCls.class, search, (int)start, pageSize));
                        break;
                    case "statuses":
                    case "status":
                        //search statuses
                        output.put("search", "status");
                        output.putAll(jsonQuerySearchPages(StatusCls.class, search, (int)start, pageSize));
                        break;
                    case "templates":
                    case "template":
                        //search templates
                        output.put("search", "template");
                        output.putAll(jsonQuerySearchPages(TemplateCls.class, search, (int)start, pageSize));
                        break;
                    case "blocks":
                    case "block":
                        //search block
                        output.put("search", "block");
                        output.putAll(jsonQuerySearchPages(Block.class, search, (int)start, pageSize));
                        break;
                }

            }
            // top 100
        } else if (info.getQueryParameters().containsKey("top")) {
            output.putAll(jsonQueryTopRichest(info));
            // asset lite
        } else if (info.getQueryParameters().containsKey("assetsLite")) {
            output.put("assetsLite", jsonQueryAssetsLite());
            // assets list
        } else if (info.getQueryParameters().containsKey("assets")) {
            output.put("search", "asset");
            output.putAll(jsonQueryPages(AssetCls.class, start, pageSize));
            // asset
        } else if (info.getQueryParameters().containsKey("asset")) {
            // person asset balance
            if (info.getQueryParameters().containsKey("person")) {
                output.put("search", "person");
                output.putAll(jsonQueryPersonBalance(new Long(info.getQueryParameters().getFirst("person")),
                        new Long(info.getQueryParameters().getFirst("asset")),
                        new Integer(info.getQueryParameters().getFirst("position"))
                ));
            } else {
                output.put("search", "asset");
                if (info.getQueryParameters().get("asset").size() == 1) {
                    try {
                        output.put("asset", jsonQueryAsset(Long.valueOf((info.getQueryParameters().getFirst("asset")))));
                    } catch (Exception e) {
                        output.put("error", e.getMessage());
                        logger.error(e.getMessage(), e);
                        return output;
                    }
                }

                if (info.getQueryParameters().get("asset").size() == 2) {
                    long have = Integer.valueOf(info.getQueryParameters().get("asset").get(0));
                    long want = Integer.valueOf(info.getQueryParameters().get("asset").get(1));

                    output.putAll(jsonQueryTrades(have, want));
                }
            }
        } else if (info.getQueryParameters().containsKey("blocks")) {
            output.putAll(jsonQueryPages(Block.class, (int)start, pageSize));
        // polls list
        } else if (info.getQueryParameters().containsKey("polls")) {
            output.putAll(jsonQueryPools(info));
        }
        //peers
        else if (info.getQueryParameters().containsKey("peers")) {
            output.putAll(jsonQueryPeers(info));
        }
        //todo Gleb непонятно зачем. Информация о последнем блоке добавляется в другом месте. URL с таким параметром не нашел.
//        else if (info.getQueryParameters().containsKey("lastBlock")) {
//            output = jsonQueryLastBlock();
//        }
        // address
        else if (info.getQueryParameters().containsKey("addr")) {
            output.putAll(jsonQueryAddress(info.getQueryParameters().getFirst("addr"), (int)start));
            // block
        } else if (info.getQueryParameters().containsKey("block")) {
            output.put("search", "block");
            output.putAll(jsonQueryBlock(info.getQueryParameters().getFirst("block"), (int)start));
        }
        // transaction
        else if (info.getQueryParameters().containsKey("transactions")) {
            output.put("search", "transaction");
            output.putAll(jsonQueryTransactions(null, (int)start));
        }
        // transaction
        else if (info.getQueryParameters().containsKey("tx")) {
            output.put("search", "transaction");
            output.putAll(jsonQueryTX(info.getQueryParameters().getFirst("tx")));
        }
        // trade
        else if (info.getQueryParameters().containsKey("trade")) {
            output.putAll(jsonQueryTrade(info.getQueryParameters().getFirst("trade")));
        }
        //poll
        else if (info.getQueryParameters().containsKey("poll")) {
            output.putAll(jsonQueryPool(info.getQueryParameters().getFirst("poll"),
                    info.getQueryParameters().getFirst(" asset")));
        }
        // unconfirmed transactions
        else if (info.getQueryParameters().containsKey("unconfirmed")) {
            output.putAll(jsonQueryUnconfirmedTXs());
        }
        // blog tx
        else if (info.getQueryParameters().containsKey("blogposts")) {
            output.putAll(jsonQueryBlogPostsTx(info.getQueryParameters().getFirst("blogposts")));
        }
        // persons list
        else if (info.getQueryParameters().containsKey("persons")) {
            output.put("search", "person");
            output.putAll(jsonQueryPages(PersonCls.class, start, pageSize));
        }
        // person
        else if (info.getQueryParameters().containsKey("person")) {
            output.put("search", "person");
            // person asset balance
            if (info.getQueryParameters().containsKey("asset")) {
//                output.put("search", "person");
                output.putAll(jsonQueryPersonBalance(new Long(info.getQueryParameters().getFirst("person")),
                        new Long(info.getQueryParameters().getFirst("asset")),
                        new Integer(info.getQueryParameters().getFirst("position"))
                ));
            } else {
                output.putAll(jsonQueryPerson(info.getQueryParameters().getFirst("person")));
            }
        }
        // templates list
        else if (info.getQueryParameters().containsKey("templates")) {
            output.put("search", "template");
            output.putAll(jsonQueryPages(TemplateCls.class, start, pageSize));
        }
        // template
        else if (info.getQueryParameters().containsKey("template")) {
            output.put("search", "template");
            output.putAll(jsonQueryTemplate(Long.valueOf(info.getQueryParameters().getFirst("template"))));
        }
        // statuses list
        else if (info.getQueryParameters().containsKey("statuses")) {
            output.put("search", "status");
            output.putAll(jsonQueryPages(StatusCls.class, start, pageSize));
        }
        // status
        else if (info.getQueryParameters().containsKey("status")) {
            output.put("search", "status");
            output.putAll(jsonQueryStatus(Long.valueOf(info.getQueryParameters().getFirst("status"))));
        }
        // tx from seq-No
        else if (info.getQueryParameters().containsKey("seqNo")) {
            if (info.getQueryParameters().containsKey("statement")) {
                output.putAll(jsonQueryStatement(info.getQueryParameters().getFirst("statement"),
                        info.getQueryParameters().getFirst("seqNo")));
            } else {

                Transaction transaction = dcSet.getTransactionFinalMap().get(
                        new Integer(info.getQueryParameters().getFirst("block")),
                        new Integer(info.getQueryParameters().getFirst("seqNo")));
                output.put("body", WebTransactionsHTML.getInstance().get_HTML(transaction, langObj));
            }
        }
        // not key
        else {
            output.put("error", "Not enough parameters.");
            output.put("help", jsonQueryHelp());
        }
        // time guery
        output.put("queryTimeMs", stopwatchAll.elapsedTime());
        return output;
    }

    private long checkAndGetIntParam(UriInfo info, long param, String name) {
        if (info.getQueryParameters().containsKey(name)
                && !info.getQueryParameters().getFirst(name).equals("")
                && !info.getQueryParameters().getFirst(name).equals("undefined")) {
            param = Long.valueOf((info.getQueryParameters().getFirst(name)));
        }
        return param;
    }

    public Map jsonQueryHelp() {
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
        help.put("Address (additional)",
                "blockexplorer.json?addr={address}&start={offset}&allOnOnePage&withoutBlocks&showWithout={1,2,blocks}&showOnly={type}");
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


    public Map jsonQueryBlogPostsTx(String addr) {

        Map output = new LinkedHashMap();
        try {

            //AssetNames assetNames = new AssetNames();

            List<Transaction> transactions = new ArrayList<Transaction>();

            if (Crypto.getInstance().isValidAddress(addr)) {
                Account account = new Account(addr);

                String address = account.getAddress();
                // get reference to parent record for this account
                Long timestampRef = account.getLastTimestamp();
                // get signature for account + time
                byte[] signatureBytes = dcSet.getAddressTime_SignatureMap().get(address, timestampRef);

                Controller cntr = Controller.getInstance();
                do {
                    // Transaction transaction =
                    // Controller.getInstance().get(signatureBytes);
                    Transaction transaction = cntr.getTransaction(signatureBytes);
                    if (transaction == null) {
                        break;
                    }
                    if (transaction.getCreator() == null && !transaction.getCreator().getAddress().equals(addr)) {
                        break;
                    }

                    if (transaction.getType() == Transaction.ARBITRARY_TRANSACTION
                            && ((ArbitraryTransaction) transaction).getService() == 777) {
                        transactions.add(transaction);
                    }
                    // get reference to parent record for this account
                    // timestampRef = transaction.getReference();
                    timestampRef = account.getLastTimestamp();
                    // get signature for account + time
                    signatureBytes = dcSet.getAddressTime_SignatureMap().get(address, timestampRef);

                } while (true);

                int count = transactions.size();

                output.put("count", count);

                int i = 0;
                for (Transaction transaction : transactions) {
                    output.put(count - i, jsonUnitPrint(transaction)); //, assetNames));
                    i++;
                }
            }

            //output.put("assetNames", assetNames.getMap());

        } catch (Exception e1) {
            output = new LinkedHashMap();
            output.put("error", e1.getLocalizedMessage());
        }
        return output;
    }

    public Map jsonQueryAssetsLite() {
        Map output = new LinkedHashMap();

        Collection<ItemCls> items = Controller.getInstance().getAllItems(ItemCls.ASSET_TYPE);

        for (ItemCls item : items) {
            output.put(item.getKey(), item.viewName());
        }

        return output;
    }


    public Map jsonQueryPools(UriInfo info) {
        Map lastPools = new LinkedHashMap();
        Map output = new LinkedHashMap();
        String key = info.getQueryParameters().getFirst("asset");
        Long asset_g;
        if (key == null) {
            asset_g = (long) 1;
        } else {
            asset_g = Long.valueOf(key);
        }

        List<Poll> pools = new ArrayList<Poll>(dcSet.getPollMap().getValues());

        if (pools.isEmpty()) {
            output.put("error", "There is no Polls.");
            return output;
        }

        // SCAN
        int back = 815; // 3*24*60*60/318 = 815 // 3 days
        // back = 40815;
        Pair<Block, List<Transaction>> result = Controller.getInstance().scanTransactions(
                Controller.getInstance().getBlockByHeight(getHeight() - back), back, 100,
                Transaction.CREATE_POLL_TRANSACTION, -1, null);

        for (Transaction transaction : result.getB()) {
            lastPools.put(((CreatePollTransaction) transaction).getPoll().getName(), true);
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

        Map poolsJSON = new LinkedHashMap();

        for (Poll pool : pools) {
            Map poolJSON = new LinkedHashMap();

            poolJSON.put("totalVotes", pool.getTotalVotes(asset_g).toPlainString());

            poolJSON.put("new", lastPools.containsKey(pool.getName()));

            poolsJSON.put(JSONObject.escape(pool.getName()), poolJSON);
        }

        output.put("pools", poolsJSON);

        Map assets1 = jsonQueryAssets();
        output.put("assets", assets1);

        return output;
    }

    public Map jsonQueryPool(String query, String asset_1) {

        Long asset_q = Long.valueOf(asset_1);

        Map output = new LinkedHashMap();

        Poll poll = Controller.getInstance().getPoll(query);

        Map pollJSON = new LinkedHashMap();

        pollJSON.put("creator", poll.getCreator().getAddress());
        pollJSON.put("name", JSONObject.escape(poll.getName()));
        pollJSON.put("description", poll.getDescription());
        pollJSON.put("totalVotes", poll.getTotalVotes(asset_q).toPlainString());

        if (true) {
            //Tuple2<Integer, Integer> blocNoSeqNo = dcSet.getTransactionFinalMapSigns().get(poll.getReference());
            //Transaction transactions = dcSet.getTransactionFinalMap().get(blocNoSeqNo);
            pollJSON.put("timestamp", 0l);//transactions.getTimestamp());
            pollJSON.put("dateTime", BlockExplorer.timestampToStr(0l)); //transactions.getTimestamp()));
        } else {
            // OLD
            List<Transaction> transactions = dcSet.getTransactionFinalMap().getTransactionsByTypeAndAddress(
                    poll.getCreator().getAddress(), Transaction.CREATE_POLL_TRANSACTION, 0);
            for (Transaction transaction : transactions) {
                CreatePollTransaction createPollTransaction = ((CreatePollTransaction) transaction);
                if (createPollTransaction.getPoll().getName().equals(poll.getName())) {
                    pollJSON.put("timestamp", createPollTransaction.getTimestamp());
                    pollJSON.put("dateTime", BlockExplorer.timestampToStr(createPollTransaction.getTimestamp()));
                    break;
                }
            }
        }

        Map optionsJSON = new LinkedHashMap();
        for (PollOption option : poll.getOptions()) {
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

        for (Pair<Account, PollOption> vote : votes) {
            Map voteJSON = new LinkedHashMap();
            voteJSON.put("option", vote.getB().getName());
            voteJSON.put("votes", vote.getA().getBalanceUSE(asset_q).toPlainString());

            votesJSON.put(vote.getA().getAddress(), voteJSON);
        }
        pollJSON.put("votes", votesJSON);

        output.put("pool", pollJSON);

        return output;
    }

    // TODO: что-то тут напутано
    public Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> calcForAsset(
            List<Order> orders,
            List<Trade> trades) {

        Map<Long, Integer> pairsOpenOrders = new TreeMap<Long, Integer>();
        Map<Long, BigDecimal> volumePriceOrders = new TreeMap<Long, BigDecimal>();
        Map<Long, BigDecimal> volumeAmountOrders = new TreeMap<Long, BigDecimal>();

        int count;
        BigDecimal volumePrice = BigDecimal.ZERO;
        BigDecimal volumeAmount = BigDecimal.ZERO;

        for (Order order : orders) {
            if (!pairsOpenOrders.containsKey(order.getWant())) {
                count = 0;
            } else {
                count = pairsOpenOrders.get(order.getWant());
            }

            if (!volumeAmountOrders.containsKey(order.getWant())) {
                volumeAmount = BigDecimal.ZERO;
            } else {
                volumeAmount = volumeAmountOrders.get(order.getWant());
            }

            if (!volumePriceOrders.containsKey(order.getWant())) {
                volumePrice = BigDecimal.ZERO;
            } else {
                volumePrice = volumePriceOrders.get(order.getWant());
            }

            count++;
            pairsOpenOrders.put(order.getWant(), count);

            volumeAmount = volumeAmount.add(order.getAmountHaveLeft());

            volumeAmountOrders.put(order.getWant(), volumeAmount);

            volumePriceOrders.put(order.getWant(), volumePrice);

            if (!pairsOpenOrders.containsKey(order.getHave())) {
                count = 0;
            } else {
                count = pairsOpenOrders.get(order.getHave());
            }

            if (!volumePriceOrders.containsKey(order.getHave())) {
                volumePrice = BigDecimal.ZERO;
            } else {
                volumePrice = volumePriceOrders.get(order.getHave());
            }

            if (!volumeAmountOrders.containsKey(order.getHave())) {
                volumeAmount = BigDecimal.ZERO;
            } else {
                volumeAmount = volumeAmountOrders.get(order.getHave());
            }

            count++;
            pairsOpenOrders.put(order.getHave(), count);

            volumePrice = volumePrice.add(order.getAmountHaveLeft());

            volumePriceOrders.put(order.getHave(), volumePrice);

            volumeAmountOrders.put(order.getHave(), volumeAmount);
        }

        Map<Long, Integer> pairsTrades = new TreeMap<Long, Integer>();
        Map<Long, BigDecimal> volumePriceTrades = new TreeMap<Long, BigDecimal>();
        Map<Long, BigDecimal> volumeAmountTrades = new TreeMap<Long, BigDecimal>();

        for (Trade trade : trades) {

            Order initiator = Order.getOrder(dcSet, trade.getInitiator());
            if (!pairsTrades.containsKey(initiator.getWant())) { //.c.a)) {
                count = 0;
                volumePrice = BigDecimal.ZERO;
                volumeAmount = BigDecimal.ZERO;
            } else {
                count = pairsTrades.get(initiator.getWant());
                volumePrice = volumePriceTrades.get(initiator.getWant());
                volumeAmount = volumeAmountTrades.get(initiator.getWant());
            }

            count++;
            pairsTrades.put(initiator.getWant(), count);

            volumePrice = volumePrice.add(trade.getAmountHave());
            volumeAmount = volumeAmount.add(trade.getAmountWant());

            volumePriceTrades.put(initiator.getWant(), volumePrice);
            volumeAmountTrades.put(initiator.getWant(), volumeAmount);

            Order target = Order.getOrder(dcSet, trade.getTarget());
            if (!pairsTrades.containsKey(target.getWant())) {
                count = 0;
                volumePrice = BigDecimal.ZERO;
                volumeAmount = BigDecimal.ZERO; // ;
            } else {
                count = pairsTrades.get(target.getWant());
                volumePrice = volumePriceTrades.get(target.getWant());
                volumeAmount = volumeAmountTrades.get(target.getWant());
            }

            count++;
            pairsTrades.put(target.getWant(), count);

            volumePrice = volumePrice.add(trade.getAmountHave());
            volumeAmount = volumeAmount.add(trade.getAmountWant());

            volumePriceTrades.put(target.getWant(), volumePrice);
            volumeAmountTrades.put(target.getWant(), volumeAmount);
        }

        Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> all = new TreeMap<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>>();

        for (Map.Entry<Long, Integer> pair : pairsOpenOrders.entrySet()) {
            all.put(pair.getKey(), Fun.t6(pair.getValue(), 0, volumePriceOrders.get(pair.getKey()),
                    volumeAmountOrders.get(pair.getKey()), BigDecimal.ZERO, BigDecimal.ZERO));
        }

        for (Map.Entry<Long, Integer> pair : pairsTrades.entrySet()) {

            if (all.containsKey(pair.getKey())) {
                all.put(pair.getKey(),
                        Fun.t6(all.get(pair.getKey()).a, pair.getValue(), all.get(pair.getKey()).c,
                                all.get(pair.getKey()).d, volumePriceTrades.get(pair.getKey()),
                                volumeAmountTrades.get(pair.getKey())));
            } else {
                all.put(pair.getKey(), Fun.t6(0, pair.getValue(), BigDecimal.ZERO, BigDecimal.ZERO,
                        volumePriceTrades.get(pair.getKey()), volumeAmountTrades.get(pair.getKey())));
            }
        }

        return all;
    }

    public Map jsonQueryAsset(long key) {
        Map output = new LinkedHashMap();

        List<Order> orders = dcSet.getOrderMap().getOrders(key);

        List<Trade> trades = dcSet.getTradeMap().getTrades(key);

        AssetCls asset = Controller.getInstance().getAsset(key);

        Map assetJSON = new LinkedHashMap();

        assetJSON.put("key", asset.getKey());
        assetJSON.put("name", asset.getName());
        assetJSON.put("description", Lang.getInstance().translateFromLangObj(asset.viewDescription(), langObj));
        assetJSON.put("owner", asset.getOwner().getAddress());
        assetJSON.put("quantity", asset.getQuantity());
        assetJSON.put("scale", asset.getScale());
        // String a =
        // Lang.getInstance().translateFromLangObj("False",langObj);
        // if (asset.isDivisible()) a =
        // Lang.getInstance().translateFromLangObj("True",langObj);
        // assetJSON.put("isDivisible", a);
        assetJSON.put("assetType", Lang.getInstance().translateFromLangObj(asset.viewAssetType(), langObj));
        // a = Lang.getInstance().translateFromLangObj("False",langObj);
        // if (asset.isMovable()) a =
        // Lang.getInstance().translateFromLangObj("True",langObj);
        // assetJSON.put("isMovable", a);
        assetJSON.put("img", Base64.encodeBase64String(asset.getImage()));
        assetJSON.put("icon", Base64.encodeBase64String(asset.getIcon()));

        if (true) {
            if (true) { //asset.getKey() > AssetCls.START_KEY ) {
                Long blocNoSeqNo = dcSet.getTransactionFinalMapSigns().get(asset.getReference());
                Transaction transactions = dcSet.getTransactionFinalMap().get(blocNoSeqNo);
                assetJSON.put("timestamp", transactions.getTimestamp());
                assetJSON.put("dateTime", BlockExplorer.timestampToStr(transactions.getTimestamp()));
            }
        } else {
            // OLD
            List<Transaction> transactions = dcSet.getTransactionFinalMap()
                    .getTransactionsByTypeAndAddress(asset.getOwner().getAddress(), Transaction.ISSUE_ASSET_TRANSACTION, 0);
            for (Transaction transaction : transactions) {
                IssueAssetTransaction issueAssetTransaction = ((IssueAssetTransaction) transaction);
                if (issueAssetTransaction.getItem().viewName().equals(asset.getName())) {
                    assetJSON.put("timestamp", issueAssetTransaction.getTimestamp());
                    assetJSON.put("dateTime", BlockExplorer.timestampToStr(issueAssetTransaction.getTimestamp()));
                    break;
                }
            }
        }


        output.put("this", assetJSON);

        output.put("totalOpenOrdersCount", orders.size());
        output.put("totalTradesCount", trades.size());

        Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> all = calcForAsset(orders,
                trades);

        if (all.containsKey(key)) {
            output.put("totalOrdersVolume", all.get(key).c.toPlainString());
        } else {
            output.put("totalOrdersVolume", BigDecimal.ZERO.toPlainString());
        }

        if (all.containsKey(key)) {
            output.put("totalTradesVolume", all.get(key).f.toPlainString());
        } else {
            output.put("totalTradesVolume", BigDecimal.ZERO.toPlainString());
        }

        Map pairsJSON = new LinkedHashMap();

        pairsJSON = new LinkedHashMap();
        for (Map.Entry<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> pair : all
                .entrySet()) {
            if (pair.getKey() == key) {
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
            pairJSON.put("description", Lang.getInstance().translateFromLangObj(assetWant.viewDescription(), langObj));
            pairsJSON.put(pair.getKey(), pairJSON);
        }

        output.put("pairs", pairsJSON);
        output.put("label_Asset", Lang.getInstance().translateFromLangObj("Asset", langObj));
        output.put("label_Key", Lang.getInstance().translateFromLangObj("Key", langObj));
        output.put("label_Creator", Lang.getInstance().translateFromLangObj("Creator", langObj));
        output.put("label_Description", Lang.getInstance().translateFromLangObj("Description", langObj));
        output.put("label_Scale", Lang.getInstance().translateFromLangObj("Accuracy", langObj));
        output.put("label_AssetType", Lang.getInstance().translateFromLangObj("TYPE", langObj));
        output.put("label_Quantity", Lang.getInstance().translateFromLangObj("Quantity", langObj));
        output.put("label_Holders", Lang.getInstance().translateFromLangObj("Holders", langObj));
        output.put("label_Available_pairs", Lang.getInstance().translateFromLangObj("Available pairs", langObj));
        output.put("label_Pair", Lang.getInstance().translateFromLangObj("Pair", langObj));
        output.put("label_Orders_Count", Lang.getInstance().translateFromLangObj("Orders Count", langObj));
        output.put("label_Open_Orders_Volume",
                Lang.getInstance().translateFromLangObj("Open Orders Volume", langObj));
        output.put("label_Trades_Count", Lang.getInstance().translateFromLangObj("Trades Count", langObj));
        output.put("label_Trades_Volume", Lang.getInstance().translateFromLangObj("Trades Volume", langObj));
        output.put("label_Total", Lang.getInstance().translateFromLangObj("Total", langObj));
        output.put("label_View", Lang.getInstance().translateFromLangObj("View", langObj));

        return output;
    }

    public Map jsonQueryTrades(long have, long want) {
        Map output = new LinkedHashMap();

        List<Order> ordersHave = dcSet.getOrderMap().getOrdersForTradeWithFork(have, want, false);
        List<Order> ordersWant = dcSet.getOrderMap().getOrdersForTradeWithFork(want, have, true);

        // Collections.reverse(ordersWant);

        List<Trade> trades = dcSet.getTradeMap().getTrades(have, want, 0, 100);

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

        BigDecimal sumAmount = BigDecimal.ZERO;
        BigDecimal sumAmountGood = BigDecimal.ZERO;

        BigDecimal sumSellingAmount = BigDecimal.ZERO;
        BigDecimal sumSellingAmountGood = BigDecimal.ZERO;

        TransactionFinalMap finalMap = DCSet.getInstance().getTransactionFinalMap();
        Transaction createOrder;

        BigDecimal vol;
        // show SELLs in BACK order
        for (int i = ordersHave.size() - 1; i >= 0; i--) {

            Order order = ordersHave.get(i);
            Map sellJSON = new LinkedHashMap();

            sellJSON.put("price", order.getPrice().toPlainString());
            vol = order.getAmountHaveLeft(); //.b.b.subtract(order.b.c);
            sellJSON.put("amount", vol.toPlainString()); // getAmountHaveLeft
            sumAmount = sumAmount.add(vol);

            sellJSON.put("sellingPrice", Order.calcPrice(order.getAmountWant(), order.getAmountHave()).toPlainString());

            //BigDecimal sellingAmount = Order.calcAmountWantLeft(order);
            BigDecimal sellingAmount = order.getAmountWantLeft();

            sellJSON.put("sellingAmount", sellingAmount.toPlainString());

            sumAmountGood = sumAmountGood.add(vol);

            sumSellingAmountGood = sumSellingAmountGood.add(sellingAmount);

            sumSellingAmount = sumSellingAmount.add(sellingAmount);


            createOrder = finalMap.get(order.getId());
            sellsJSON.put(Base58.encode(createOrder.getSignature()), sellJSON);
        }

        output.put("sells", sellsJSON);

        output.put("sellsSumAmount", sumAmount.toPlainString());
        output.put("sellsSumAmountGood", sumAmountGood.toPlainString());
        output.put("sellsSumTotal", sumSellingAmount.toPlainString());
        output.put("sellsSumTotalGood", sumSellingAmountGood.toPlainString());

        sumAmount = BigDecimal.ZERO;
        sumAmountGood = BigDecimal.ZERO;

        BigDecimal sumBuyingAmount = BigDecimal.ZERO;
        BigDecimal sumBuyingAmountGood = BigDecimal.ZERO;

        for (int i = ordersWant.size() - 1; i >= 0; i--) {

            Order order = ordersWant.get(i);

            Map buyJSON = new LinkedHashMap();

            buyJSON.put("price", order.getPrice().toPlainString());
            vol = order.getAmountHaveLeft(); //.b.b.subtract(order.b.c);
            buyJSON.put("amount", vol.toPlainString()); // getAmountHaveLeft

            sumAmount = sumAmount.add(vol);

            buyJSON.put("buyingPrice", Order.calcPrice(order.getAmountWant(), order.getAmountHave()).toPlainString());

            //BigDecimal buyingAmount = Order.calcAmountWantLeft(order);
            BigDecimal buyingAmount = order.getAmountWantLeft();

            buyJSON.put("buyingAmount", buyingAmount.toPlainString());

            sumBuyingAmountGood = sumBuyingAmountGood.add(buyingAmount);

            sumAmountGood = sumAmountGood.add(vol);

            sumBuyingAmount = sumBuyingAmount.add(buyingAmount);

            createOrder = finalMap.get(order.getId());
            buysJSON.put(Base58.encode(createOrder.getSignature()), buyJSON);
        }
        output.put("buys", buysJSON);

        output.put("buysSumAmount", sumBuyingAmount.toPlainString());
        output.put("buysSumAmountGood", sumBuyingAmountGood.toPlainString());
        output.put("buysSumTotal", sumAmount.toPlainString());
        output.put("buysSumTotalGood", sumAmountGood.toPlainString());

        Map tradesJSON = new LinkedHashMap();

        output.put("tradesCount", trades.size());

        BigDecimal tradeWantAmount = BigDecimal.ZERO;
        BigDecimal tradeHaveAmount = BigDecimal.ZERO;

        int i = 0;
        for (Trade trade : trades) {

            i++;

            Map tradeJSON = new LinkedHashMap();

            Order orderInitiator = Order.getOrder(dcSet, trade.getInitiator());

            Order orderTarget = Order.getOrder(dcSet, trade.getTarget());

            tradeJSON.put("realPrice", trade.calcPrice());
            tradeJSON.put("realReversePrice", trade.calcPriceRevers());

            createOrder = finalMap.get(orderInitiator.getId());
            tradeJSON.put("initiatorTxSignature", Base58.encode(createOrder.getSignature()));

            tradeJSON.put("initiatorCreator", orderInitiator.getCreator().getAddress()); // viewCreator
            tradeJSON.put("initiatorAmount", orderInitiator.getAmountHave().toPlainString());
            if (orderInitiator.getHave() == have) {
                tradeJSON.put("type", "sell");
                tradeWantAmount = tradeWantAmount.add(trade.getAmountHave());
                tradeHaveAmount = tradeHaveAmount.add(trade.getAmountWant());

                tradeJSON.put("amountHave", trade.getAmountWant().toPlainString());
                tradeJSON.put("amountWant", trade.getAmountHave().toPlainString());
            } else {
                tradeJSON.put("type", "buy");
                tradeHaveAmount = tradeHaveAmount.add(trade.getAmountHave());
                tradeWantAmount = tradeWantAmount.add(trade.getAmountWant());

                tradeJSON.put("amountHave", trade.getAmountHave().toPlainString());
                tradeJSON.put("amountWant", trade.getAmountWant().toPlainString());
            }

            createOrder = finalMap.get(orderTarget.getId());
            tradeJSON.put("targetTxSignature", Base58.encode(createOrder.getSignature()));
            tradeJSON.put("targetCreator", orderTarget.getCreator().getAddress()); // viewCreator
            tradeJSON.put("targetAmount", orderTarget.getAmountHave().toPlainString());

            tradeJSON.put("timestamp", trade.getTimestamp());
            tradeJSON.put("dateTime", BlockExplorer.timestampToStr(trade.getTimestamp()));

            tradesJSON.put(i, tradeJSON);

            if (i > 100)
                break;
        }
        output.put("trades", tradesJSON);

        output.put("tradeWantAmount", tradeWantAmount.toPlainString());
        output.put("tradeHaveAmount", tradeHaveAmount.toPlainString());

        output.put("label_Trades", Lang.getInstance().translateFromLangObj("Trades", langObj));
        output.put("label_Price", Lang.getInstance().translateFromLangObj("Price", langObj));
        output.put("label_Amount", Lang.getInstance().translateFromLangObj("Amount", langObj));
        output.put("label_Orders", Lang.getInstance().translateFromLangObj("Orders", langObj));
        output.put("label_Sell_Orders", Lang.getInstance().translateFromLangObj("Sell Orders", langObj));
        output.put("label_Buy_Orders", Lang.getInstance().translateFromLangObj("Buy Orders", langObj));
        output.put("label_Total", Lang.getInstance().translateFromLangObj("Total", langObj));
        output.put("label_Total_For_Sell", Lang.getInstance().translateFromLangObj("Total for Sell", langObj));
        output.put("label_Total_For_Buy", Lang.getInstance().translateFromLangObj("Total for Buy", langObj));
        output.put("label_Trade_History", Lang.getInstance().translateFromLangObj("Trade History", langObj));
        output.put("label_Date", Lang.getInstance().translateFromLangObj("Date", langObj));
        output.put("label_Type", Lang.getInstance().translateFromLangObj("Type", langObj));
        output.put("label_Trade_Volume", Lang.getInstance().translateFromLangObj("Trade Volume", langObj));
        output.put("label_Go_To", Lang.getInstance().translateFromLangObj("Go To", langObj));

        return output;
    }

    private Map jsonQueryPersonBalance(Long personKey, Long assetKey, int position) {

        Map output = new HashMap();
        if (position < 1 || position > 5) {
            output.put("error", "wrong position");
            return output;
        }

        PersonCls person = (PersonCls) dcSet.getItemPersonMap().get(new Long(personKey));
        if (person == null) {
            output.put("error", "person not found");
            return output;
        }

        AssetCls asset = (AssetCls) dcSet.getItemAssetMap().get(new Long(assetKey));
        if (asset == null) {
            output.put("error", "person not found");
            return output;
        }

        byte[] b = person.getImage();
        String a = Base64.encodeBase64String(b);

        output.put("Label_key", Lang.getInstance().translateFromLangObj("Key", langObj));
        output.put("Label_name", Lang.getInstance().translateFromLangObj("Name", langObj));

        output.put("person_img", a);
        output.put("person_key", person.getKey());
        output.put("person_name", person.getName());

        output.put("asset_key", asset.getKey());
        output.put("asset_name", asset.getName());

        output.put("Label_denied", Lang.getInstance().translateFromLangObj("DENIED", langObj));
        output.put("Label_sum", Lang.getInstance().translateFromLangObj("SUM", langObj));
        BigDecimal sum = PersonCls.getBalance(personKey, assetKey, position);
        output.put("sum", sum);

        return output;
    }

    //todo Gleb for future убрать duplicateCodeAssets. Проблема в ключе.  заменит на assetsJSON
    public Map jsonQueryAssets() {
        Map output = new LinkedHashMap();
        Collection<ItemCls> items = Controller.getInstance().getAllItems(ItemCls.ASSET_TYPE);
        for (ItemCls item : items) {
            duplicateCodeAssets(output, (AssetCls) item);
        }
        return output;
    }

    private void duplicateCodeAssets(Map assetsJSON, AssetCls asset) {
        Map assetJSON = new LinkedHashMap();

        assetJSON.put("key", asset.getKey());
        assetJSON.put("name", asset.getName());
        assetJSON.put("description", Lang.getInstance().translateFromLangObj(asset.viewDescription(), langObj));
        assetJSON.put("owner", asset.getOwner().getAddress());
        assetJSON.put("quantity", NumberAsString.formatAsString(asset.getTotalQuantity(dcSet)));
        assetJSON.put("scale", asset.getScale());
        assetJSON.put("assetType", Lang.getInstance().translateFromLangObj(asset.viewAssetType(), langObj));
        assetJSON.put("img", Base64.encodeBase64String(asset.getImage()));
        assetJSON.put("icon", Base64.encodeBase64String(asset.getIcon()));
        List<Order> orders = dcSet
                .getOrderMap().getOrders(asset.getKey());
        List<Trade> trades = dcSet.getTradeMap()
                .getTrades(asset.getKey());
        assetJSON.put("operations", orders.size() + trades.size());
        assetsJSON.put(asset.getKey(), assetJSON);
    }

    private Map jsonQueryPerson(String first) {
        Map output = new LinkedHashMap();
        PersonCls person = (PersonCls) dcSet.getItemPersonMap().get(new Long(first));
        if (person == null) {
            return null;
        }

        byte[] b = person.getImage();
        String a = Base64.encodeBase64String(b);

        output.put("Label_key", Lang.getInstance().translateFromLangObj("Key", langObj));
        output.put("Label_name", Lang.getInstance().translateFromLangObj("Name", langObj));
        output.put("Label_creator", Lang.getInstance().translateFromLangObj("Creator", langObj));
        output.put("Label_born", Lang.getInstance().translateFromLangObj("Birthday", langObj));
        output.put("Label_gender", Lang.getInstance().translateFromLangObj("Gender", langObj));
        output.put("Label_description", Lang.getInstance().translateFromLangObj("Description", langObj));

        output.put("img", a);
        output.put("key", person.getKey());
        output.put("creator", person.getOwner().getPersonAsString());

        if (person.getOwner().getPerson() != null) {
            output.put("creator_key", person.getOwner().getPerson().b.getKey());
            output.put("creator_name", person.getOwner().getPerson().b.getName());
        } else {
            output.put("creator_key", "");
            output.put("creator_name", "");
        }

        output.put("name", person.getName());
        output.put("birthday", person.getBirthdayStr());
        if (!person.isAlive(0L)) {
            output.put("deathday", person.getDeathdayStr());
            output.put("Label_dead", Lang.getInstance().translateFromLangObj("Deathday", langObj));

        }
        output.put("description", person.getDescription());

        String gender = Lang.getInstance().translateFromLangObj("Man", langObj);
        if (person.getGender() == 0) {
            gender = Lang.getInstance().translateFromLangObj("Man", langObj);
        } else if (person.getGender() == 0) {
            gender = Lang.getInstance().translateFromLangObj("Woman", langObj);
        } else {
            gender = Lang.getInstance().translateFromLangObj("-", langObj);
        }
        output.put("gender", gender);

        // statuses
        output.put("Label_statuses", Lang.getInstance().translateFromLangObj("Statuses", langObj));
        output.put("Label_Status_table_status", Lang.getInstance().translateFromLangObj("Status", langObj));
        output.put("Label_Status_table_period", Lang.getInstance().translateFromLangObj("Period", langObj));

        Map statusesJSON = new LinkedHashMap();

        WebPersonStatusesModel statusModel = new WebPersonStatusesModel(person.getKey());
        int rowCount = statusModel.getRowCount();
        if (rowCount > 0) {
            for (int i = 0; i < rowCount; i++) {
                Map statusJSON = new LinkedHashMap();
                statusJSON.put("status_name", statusModel.getValueAt(i, WebPersonStatusesModel.COLUMN_STATUS_NAME));
                statusJSON.put("status_period", statusModel.getValueAt(i, WebPersonStatusesModel.COLUMN_PERIOD));
                Account creator = (Account) statusModel.getValueAt(i, WebPersonStatusesModel.COLUMN_MAKER_ACCOUNT);

                if (creator != null) {
                    statusJSON.put("status_creator_address", creator.getAddress());
                    statusJSON.put("status_creator", creator.getPersonAsString());

                } else {
                    statusJSON.put("status_creator_address", GenesisBlock.CREATOR.getAddress());
                    statusJSON.put("status_creator", "GENESIS");
                }

                statusesJSON.put(i, statusJSON);
            }

            output.put("statuses", statusesJSON);
        }
        // accounts
        output.put("Label_accounts", Lang.getInstance().translateFromLangObj("Accounts", langObj));
        output.put("Label_accounts_table_adress", Lang.getInstance().translateFromLangObj("Address", langObj));
        output.put("Label_accounts_table_to_date", Lang.getInstance().translateFromLangObj("To Date", langObj));
        output.put("Label_accounts_table_creator", Lang.getInstance().translateFromLangObj("Creator", langObj));

        Map accountsJSON = new LinkedHashMap();
        List<Transaction> myIssuePersons = new ArrayList<Transaction>();

        if (false) {
            PersonAccountsModel personModel = new PersonAccountsModel(person.getKey());
            rowCount = personModel.getRowCount();
            if (rowCount > 0) {
                TransactionFinalMap transactionsMap = DCSet.getInstance().getTransactionFinalMap();
                BigDecimal eraBalanceA = new BigDecimal(0);
                BigDecimal eraBalanceB = new BigDecimal(0);
                BigDecimal eraBalanceC = new BigDecimal(0);
                BigDecimal eraBalanceTotal = new BigDecimal(0);
                BigDecimal compuBalance = new BigDecimal(0);
                BigDecimal liaBalanceA = new BigDecimal(0);
                BigDecimal liaBalanceB = new BigDecimal(0);

                output.put("label_registered", Lang.getInstance().translateFromLangObj("Registered", langObj));
                output.put("label_certified", Lang.getInstance().translateFromLangObj("Certified", langObj));


                for (int i = 0; i < rowCount; i++) {
                    Map accountJSON = new LinkedHashMap();
                    accountJSON.put("address", personModel.getValueAt(i, PersonAccountsModel.COLUMN_ADDRESS));
                    accountJSON.put("to_date", personModel.getValueAt(i, PersonAccountsModel.COLUMN_TO_DATE));
                    accountJSON.put("creator", personModel.getValueAt(i, PersonAccountsModel.COLUMN_CREATOR));
                    accountJSON.put("creator_address", personModel.getValueAt(i, PersonAccountsModel.COLUMN_CREATOR_ADDRESS));

                    accountsJSON.put(i, accountJSON);

                    String acc = personModel.getValueAt(i, 0).toString();

                    myIssuePersons.addAll(transactionsMap.getTransactionsByTypeAndAddress(acc,
                            Transaction.ISSUE_PERSON_TRANSACTION, 200));

                    Account account = new Account(acc);
                    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance
                            = account.getBalance(AssetCls.ERA_KEY);

                    eraBalanceA = eraBalanceA.add(balance.a.b);
                    eraBalanceB = eraBalanceB.add(balance.b.b);
                    eraBalanceC = eraBalanceC.add(balance.c.b);
                    eraBalanceTotal = eraBalanceA.add(eraBalanceB).add(eraBalanceC);

                    balance = account.getBalance(AssetCls.FEE_KEY);
                    compuBalance = compuBalance.add(balance.a.b);

                    balance = account.getBalance(AssetCls.LIA_KEY);
                    liaBalanceA = liaBalanceA.add(balance.a.b);
                    liaBalanceB = liaBalanceB.add(balance.b.b);
                }
                output.put("era_balance_a", NumberAsString.formatAsString(eraBalanceA));
                output.put("era_balance_b", NumberAsString.formatAsString(eraBalanceB));
                output.put("era_balance_c", NumberAsString.formatAsString(eraBalanceC));
                output.put("era_balance_total", NumberAsString.formatAsString(eraBalanceTotal));
                output.put("compu_balance", NumberAsString.formatAsString(compuBalance));
                output.put("lia_balance_a", NumberAsString.formatAsString(liaBalanceA));
                output.put("lia_balance_b", NumberAsString.formatAsString(liaBalanceB));
            }

        } else {

            // НОВЫЙ ЛАД - без Обсерверов и Модели
            TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses = DCSet.getInstance().getPersonAddressMap().getItems(person.getKey());

            if (!addresses.isEmpty()) {
                TransactionFinalMap transactionsMap = DCSet.getInstance().getTransactionFinalMap();
                BigDecimal eraBalanceA = new BigDecimal(0);
                BigDecimal eraBalanceB = new BigDecimal(0);
                BigDecimal eraBalanceC = new BigDecimal(0);
                BigDecimal eraBalanceTotal = new BigDecimal(0);
                BigDecimal compuBalance = new BigDecimal(0);
                BigDecimal liaBalanceA = new BigDecimal(0);
                BigDecimal liaBalanceB = new BigDecimal(0);

                output.put("label_registered", Lang.getInstance().translateFromLangObj("Registered", langObj));
                output.put("label_certified", Lang.getInstance().translateFromLangObj("Certified", langObj));

                int i = 0;
                for (String address : addresses.keySet()) {

                    Stack<Tuple3<Integer, Integer, Integer>> stack = addresses.get(address);
                    if (stack == null || stack.isEmpty()) {
                        continue;
                    }

                    Tuple3<Integer, Integer, Integer> item = stack.peek();
                    Transaction transactionIssue = transactionsMap.get(item.b, item.c);

                    Map accountJSON = new LinkedHashMap();
                    accountJSON.put("address", address);
                    accountJSON.put("to_date", DateTimeFormat.timestamptoString(item.a * 86400000l));
                    accountJSON.put("creator", transactionIssue.getCreator().getPersonAsString());
                    accountJSON.put("creator_address", transactionIssue.getCreator().getAddress());

                    accountsJSON.put(i++, accountJSON);

                    myIssuePersons.addAll(transactionsMap.getTransactionsByTypeAndAddress(address,
                            Transaction.ISSUE_PERSON_TRANSACTION, 200));

                    Account account = new Account(address);
                    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance
                            = account.getBalance(AssetCls.ERA_KEY);

                    eraBalanceA = eraBalanceA.add(balance.a.b);
                    eraBalanceB = eraBalanceB.add(balance.b.b);
                    eraBalanceC = eraBalanceC.add(balance.c.b);
                    eraBalanceTotal = eraBalanceA.add(eraBalanceB).add(eraBalanceC);

                    balance = account.getBalance(AssetCls.FEE_KEY);
                    compuBalance = compuBalance.add(balance.a.b);

                    balance = account.getBalance(AssetCls.LIA_KEY);
                    liaBalanceA = liaBalanceA.add(balance.a.b);
                    liaBalanceB = liaBalanceB.add(balance.b.b);
                }
                output.put("era_balance_a", NumberAsString.formatAsString(eraBalanceA));
                output.put("era_balance_b", NumberAsString.formatAsString(eraBalanceB));
                output.put("era_balance_c", NumberAsString.formatAsString(eraBalanceC));
                output.put("era_balance_total", NumberAsString.formatAsString(eraBalanceTotal));
                output.put("compu_balance", NumberAsString.formatAsString(compuBalance));
                output.put("lia_balance_a", NumberAsString.formatAsString(liaBalanceA));
                output.put("lia_balance_b", NumberAsString.formatAsString(liaBalanceB));
            }

        }

        output.put("accounts", accountsJSON);

        // my persons

        output.put("Label_My_Persons", Lang.getInstance().translateFromLangObj("My Persons", langObj));
        output.put("Label_accounts_table_date", Lang.getInstance().translateFromLangObj("Creation Date", langObj));
        output.put("Label_My_Person_key", Lang.getInstance().translateFromLangObj("Key", langObj));
        output.put("Label_My_Persons_Name", Lang.getInstance().translateFromLangObj("Name", langObj));

        Map myPersonsJSON = new LinkedHashMap();

        if (myIssuePersons != null) {
            int i = 0;
            for (Transaction myIssuePerson : myIssuePersons) {
                Map myPersonJSON = new LinkedHashMap();
                IssueItemRecord record = (IssueItemRecord) myIssuePerson;
                ItemCls item = record.getItem();

                myPersonJSON.put("key", item.getKey());
                myPersonJSON.put("name", item.getName());

                myPersonJSON.put("date", df.format(new Date(myIssuePerson.getTimestamp())));
                myPersonsJSON.put(i, myPersonJSON);
                i++;
            }
        }

        output.put("My_Persons", myPersonsJSON);

        return output;
    }


    private Map jsonQueryLastBlock() {
        Map output = new LinkedHashMap();

        Block lastBlock = getLastBlock();

        output.put("height", lastBlock.getHeight());
        output.put("timestamp", lastBlock.getTimestamp());
        output.put("dateTime", BlockExplorer.timestampToStr(lastBlock.getTimestamp()));

        output.put("timezone", Settings.getInstance().getTimeZone());
        output.put("timeformat", Settings.getInstance().getTimeFormat());
        output.put("label_hour", Lang.getInstance().translateFromLangObj("hour", langObj));
        output.put("label_hours", Lang.getInstance().translateFromLangObj("hours", langObj));
        output.put("label_mins", Lang.getInstance().translateFromLangObj("mins", langObj));
        output.put("label_min", Lang.getInstance().translateFromLangObj("min", langObj));
        output.put("label_secs", Lang.getInstance().translateFromLangObj("secs", langObj));
        output.put("label_ago", Lang.getInstance().translateFromLangObj("ago", langObj));
        output.put("label_Last_processed_block",
                Lang.getInstance().translateFromLangObj("Last processed block", langObj));

        return output;
    }

    public Map jsonQueryTopRichest(UriInfo info) {
        Map output = new LinkedHashMap();
        Map balances = new LinkedHashMap();
        BigDecimal all = BigDecimal.ZERO;
        BigDecimal alloreders = BigDecimal.ZERO;
        int limit = Integer.valueOf((info.getQueryParameters().getFirst("top")));
        long key = 1l;
        if (info.getQueryParameters().containsKey("asset"))
            key = Long.valueOf(info.getQueryParameters().getFirst("asset"));
        List<Tuple3<String, BigDecimal, BigDecimal>> top100s = new ArrayList<Tuple3<String, BigDecimal, BigDecimal>>();

        Collection<Tuple2<String, Long>> addrs = dcSet.getAssetBalanceMap().getKeys();
        //BigDecimal total = BigDecimal.ZERO;
        //BigDecimal totalNeg = BigDecimal.ZERO;
        for (Tuple2<String, Long> addr : addrs) {
            if (addr.b == key) {
                Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> ball = dcSet
                        .getAssetBalanceMap().get(addr);
                // all = all.add(ball.a);
                Account account = new Account(addr.a);
                BigDecimal ballans = account.getBalanceUSE(key);
                //if (ball.a.b.signum() > 0) {
                //total = total.add(ball.a.b);
                //} else {
                //    totalNeg = totalNeg.add(ball.a.b);
                //}

                top100s.add(Fun.t3(addr.a, ballans, ball.a.b));
            }
        }

        //totalNeg = total.add(totalNeg);

        Collection<Order> orders = dcSet.getOrderMap().getValues();

        for (Order order : orders) {
            if (order.getHave() == key) {
                alloreders = alloreders.add(order.getFulfilledHave());
            }
        }
        Collections.sort(top100s, new ReverseComparator(new BigDecimalComparator_C()));

        int couter = 0;
        for (Tuple3<String, BigDecimal, BigDecimal> top100 : top100s) {
            /*
             * if(limit == -1) // allnotzero {
             * if(top100.b.compareTo(BigDecimal.ZERO) <= 0) { break; } }
             */
            couter++;

            Account account = new Account(top100.a);

            Tuple2<Integer, PersonCls> person = account.getPerson();

            Map balance = new LinkedHashMap();
            balance.put("address", top100.a);
            balance.put("balance", top100.b.toPlainString());
            balance.put("in_OWN", top100.c.toPlainString());

            if (person != null) {
                balance.put("person", person.b.getName());
                balance.put("person_key", person.b.getKey());
            } else {
                balance.put("person", "-");
                balance.put("person_key", "-");// (String)person.b.getKey());

            }

            balances.put(couter, balance);

            if (couter >= limit && limit != -2 && limit != -1) // -2 = all
            {
                break;
            }
        }
        AssetCls asset = Controller.getInstance().getAsset(key);
        output.put("Label_Table_Account", Lang.getInstance().translateFromLangObj("Account", langObj));
        output.put("Label_Table_Balance", Lang.getInstance().translateFromLangObj("Balance", langObj));
        output.put("Label_Table_in_OWN", Lang.getInstance().translateFromLangObj("in OWN", langObj));
        output.put("Label_Table_Prop", Lang.getInstance().translateFromLangObj("Prop.", langObj));
        output.put("Label_Table_person", Lang.getInstance().translateFromLangObj("Owner", langObj));

        output.put("Label_minus", Lang.getInstance().translateFromLangObj("minus", langObj));
        output.put("Label_in_order", Lang.getInstance().translateFromLangObj("in order", langObj));

        output.put("Label_Top", Lang.getInstance().translateFromLangObj("Top", langObj));

        output.put("all", all.toPlainString());
        output.put("allinOrders", alloreders.toPlainString());
        output.put("allTotal", asset.getTotalQuantity(dcSet));// (all.add(alloreders)).toPlainString());
        output.put("assetKey", key);
        output.put("assetName", asset.getName());
        output.put("limit", limit);
        output.put("count", couter);

        output.put("top", balances);
        output.put("Label_Title", (Lang.getInstance().translateFromLangObj("Top %limit% %assetName% Richest", langObj)
                .replace("%limit%", String.valueOf(limit))).replace("%assetName%", asset.getName()));
        output.put("Label_All_non",
                (Lang.getInstance().translateFromLangObj("All non-empty %assetName% accounts (%count%)", langObj)
                        .replace("%assetName%", asset.getName())).replace("%count%", String.valueOf(couter)));
        output.put("Label_All_accounts",
                (Lang.getInstance().translateFromLangObj("All %assetName% accounts (%count%)", langObj)
                        .replace("%assetName%", asset.getName())).replace("%count%", String.valueOf(couter)));
        output.put("Label_Total_coins_in_the_system",
                Lang.getInstance().translateFromLangObj("Total asset units in the system", langObj));

        output.put("assets", jsonQueryAssetsLite());
        return output;
    }


    @SuppressWarnings("static-access")
    private LinkedHashMap balanceJSON(Account account) {

        // balance assets from
        LinkedHashMap output = new LinkedHashMap();
        WebBalanceFromAddressTableModel balanceTableModel = new WebBalanceFromAddressTableModel(account);
        int ad = balanceTableModel.getRowCount();
        int idr;
        TreeMap bal_Assets = new TreeMap();
        if (ad > 0)
            for (idr = 0; idr < ad; idr++) {
                Map bal = new LinkedHashMap();
                bal.put("asset_key", balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_ASSET_KEY));
                bal.put("asset_name", balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_ASSET_NAME));
                bal.put("balance_A", balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_A));
                bal.put("balance_B", balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_B));
                bal.put("balance_C", balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_C));
                if (!(balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_A).equals("0.00000000")
                        && balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_B).equals("0.00000000")
                        && balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_C).equals("0.00000000")))
                    bal_Assets.put(balanceTableModel.getValueAt(idr, balanceTableModel.COLUMN_ASSET_KEY).toString(), bal);
            }

        output.put("balances", bal_Assets);
        output.put("label_Balance_table", Lang.getInstance().translateFromLangObj("Balance", langObj));
        output.put("label_asset_key", Lang.getInstance().translateFromLangObj("Key", langObj));
        output.put("label_asset_name", Lang.getInstance().translateFromLangObj("Name", langObj));
        output.put("label_Balance_A", Lang.getInstance().translateFromLangObj("Balance", langObj) + " A");
        output.put("label_Balance_B", Lang.getInstance().translateFromLangObj("Balance", langObj) + " B");
        output.put("label_Balance_C", Lang.getInstance().translateFromLangObj("Balance", langObj) + " C");

        return output;

    }

    // dcSet
    public Map jsonUnitPrint(Object unit) { //, AssetNames assetNames) {

        Map transactionDataJSON = new LinkedHashMap();
        Map transactionJSON = new LinkedHashMap();

        if (unit instanceof Trade) {
            Trade trade = (Trade) unit;

            Order orderInitiator = trade.getInitiatorOrder(dcSet);

            /*
             * if(dcSet.getOrderMap().contains(trade.getInitiator())) {
             * orderInitiator = dcSet.getOrderMap().get(trade.getInitiator()); }
             * else { orderInitiator =
             * dcSet.getCompletedOrderMap().get(trade.getInitiator()); }
             */

            Order orderTarget = trade.getTargetOrder(dcSet);

            /*
             * if(dcSet.getOrderMap().contains(trade.getTarget())) { orderTarget
             * = dcSet.getOrderMap().get(trade.getTarget()); } else {
             * orderTarget =
             * dcSet.getCompletedOrderMap().get(trade.getTarget()); }
             */

            transactionDataJSON.put("amount", trade.getAmountHave().toPlainString());
            transactionDataJSON.put("asset", trade.getHaveKey());

            transactionDataJSON.put("amountHave", trade.getAmountHave().toPlainString());
            transactionDataJSON.put("amountWant", trade.getAmountWant().toPlainString());

            transactionDataJSON.put("realPrice",
                    trade.getAmountWant().divide(trade.getAmountHave(), 8, RoundingMode.FLOOR).toPlainString());

            Transaction createOrder = this.dcSet.getTransactionFinalMap().get(orderInitiator.getId());
            transactionDataJSON.put("initiatorTxSignature", Base58.encode(createOrder.getSignature()));

            transactionDataJSON.put("initiatorCreator", orderInitiator.getCreator());
            transactionDataJSON.put("initiatorAmount", orderInitiator.getAmountHave().toPlainString());
            transactionDataJSON.put("initiatorHave", orderInitiator.getHave());
            transactionDataJSON.put("initiatorWant", orderInitiator.getWant());

            /*
            if (assetNames != null) {
                assetNames.setKey(orderInitiator.getHave());
                assetNames.setKey(orderInitiator.getWant());
            }
            */

            Transaction createOrderTarget = this.dcSet.getTransactionFinalMap().get(orderTarget.getId());
            transactionDataJSON.put("targetTxSignature", Base58.encode(createOrderTarget.getSignature()));
            transactionDataJSON.put("targetCreator", orderTarget.getCreator());
            transactionDataJSON.put("targetAmount", orderTarget.getAmountHave().toPlainString());

            transactionDataJSON.put("height", createOrderTarget.getBlockHeight());
            transactionDataJSON.put("confirmations", createOrderTarget.getConfirmations(DCSet.getInstance()));

            transactionDataJSON.put("timestamp", trade.getInitiator());
            transactionDataJSON.put("dateTime", "--"); //BlockExplorer.timestampToStr(trade.getTimestamp()));

            transactionJSON.put("type", "trade");
            transactionJSON.put("trade", transactionDataJSON);
            return transactionJSON;
        }

        if (unit instanceof Transaction) {
            Transaction transaction = (Transaction) unit;

            transactionDataJSON = transaction.toJson();
            // transactionDataJSON.put("Р ВµРЎв‚¬РЎРЉРЎС“РЎвЂ№Р ВµРЎвЂћ",
            // GZIP.webDecompress(transactionDataJSON.get("value").toString()));

            if (transaction.getType() == Transaction.REGISTER_NAME_TRANSACTION) {
                if (transactionDataJSON.get("value").toString().startsWith("?gz!")) {
                    transactionDataJSON.put("value", GZIP.webDecompress(transactionDataJSON.get("value").toString()));
                    transactionDataJSON.put("compressed", true);
                } else {
                    transactionDataJSON.put("compressed", false);
                }

            } else if (transaction.getType() == Transaction.UPDATE_NAME_TRANSACTION) {
                if (transactionDataJSON.get("newValue").toString().startsWith("?gz!")) {
                    transactionDataJSON.put("newValue",
                            GZIP.webDecompress(transactionDataJSON.get("newValue").toString()));
                    transactionDataJSON.put("compressed", true);
                } else {
                    transactionDataJSON.put("compressed", false);
                }
            } else if (transaction.getType() == Transaction.CANCEL_ORDER_TRANSACTION) {
                Order order;
                CancelOrderTransaction cancelOrder = (CancelOrderTransaction) unit;
                Long orderID = cancelOrder.getOrderID();
                if (orderID == null) {
                    byte[] orderSignature = cancelOrder.getorderSignature();
                    CreateOrderTransaction createOrder;
                    if (dcSet.getTransactionFinalMapSigns().contains(orderSignature)) {
                        createOrder = (CreateOrderTransaction) dcSet.getTransactionFinalMap().get(orderSignature);
                    } else {
                        createOrder = (CreateOrderTransaction) dcSet.getTransactionMap().get(orderSignature);
                    }
                    if (createOrder != null) {
                        Map orderJSON = new LinkedHashMap();

                    /*
                    if (assetNames != null) {
                        assetNames.setKey(order.getHave());
                        assetNames.setKey(order.getWant());
                    }
                    */

                        orderJSON.put("have", createOrder.getHaveKey());
                        orderJSON.put("want", createOrder.getWantKey());

                        orderJSON.put("amount", createOrder.getAmountHave().toPlainString());
                        orderJSON.put("amountLeft", "??");
                        orderJSON.put("amountWant", createOrder.getAmountWant().toPlainString());
                        orderJSON.put("price", Order.calcPrice(createOrder.getAmountHave(),
                                createOrder.getAmountWant()).toPlainString());

                        transactionDataJSON.put("orderSource", orderJSON);
                    }
                } else {
                    if (dcSet.getCompletedOrderMap().contains(orderID)) {
                        order = dcSet.getCompletedOrderMap().get(orderID);
                    } else {
                        order = dcSet.getOrderMap().get(orderID);
                    }

                    Map orderJSON = new LinkedHashMap();

                    /*
                    if (assetNames != null) {
                        assetNames.setKey(order.getHave());
                        assetNames.setKey(order.getWant());
                    }
                    */

                    orderJSON.put("have", order.getHave());
                    orderJSON.put("want", order.getWant());

                    orderJSON.put("amount", order.getAmountHave().toPlainString());
                    orderJSON.put("amountLeft", order.getAmountHaveLeft().toPlainString());
                    orderJSON.put("amountWant", order.getAmountWant().toPlainString());
                    orderJSON.put("price", order.getPrice().toPlainString());

                    transactionDataJSON.put("orderSource", orderJSON);

                }

            } else if (transaction.getType() == Transaction.ISSUE_ASSET_TRANSACTION) {
                /*
                if (transaction.getSeqNo() > 0 && assetNames != null) {
                    // IS CONFIRMED
                    long assetkey = ((IssueAssetTransaction) transaction).getItem().getKey();
                    transactionDataJSON.put("asset", assetkey);
                    transactionDataJSON.put("assetName", ((IssueAssetTransaction) transaction).getItem().getName());
                }
                */
            } else if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
                /*
                if (transaction.getSeqNo() > 0 && assetNames != null) {
                    long assetkey = ((RSend) unit).getAbsKey();
                    transactionDataJSON.put("asset", assetkey);
                    transactionDataJSON.put("assetName", assetNames.getMap().get(assetkey));
                }

                if (((RSend) unit).isEncrypted()) {
                    transactionDataJSON.put("data", "encrypted");
                }
                */

            } else if (transaction.getType() == Transaction.HASHES_RECORD) {

            } else if (transaction.getType() == Transaction.MULTI_PAYMENT_TRANSACTION) {
                Map<Long, BigDecimal> totalAmountOfAssets = new TreeMap<Long, BigDecimal>();

                for (Payment payment : ((MultiPaymentTransaction) transaction).getPayments()) {
                    BigDecimal amount = BigDecimal.ZERO;
                    if (totalAmountOfAssets.containsKey(payment.getAsset())) {
                        amount = totalAmountOfAssets.get(payment.getAsset());
                    }
                    amount = amount.add(payment.getAmount());

                    /*
                    if (assetNames != null) {
                        assetNames.setKey(payment.getAsset());
                    }
                    */

                    totalAmountOfAssets.put(payment.getAsset(), amount);
                }

                Map amountOfAssetsJSON = new LinkedHashMap();

                for (Map.Entry<Long, BigDecimal> assetInfo : totalAmountOfAssets.entrySet()) {
                    amountOfAssetsJSON.put(assetInfo.getKey(), assetInfo.getValue().toPlainString());
                }

                transactionDataJSON.put("amounts", amountOfAssetsJSON);

            } else if (transaction.getType() == Transaction.ARBITRARY_TRANSACTION) {
                Map<Long, BigDecimal> totalAmountOfAssets = new TreeMap<Long, BigDecimal>();

                for (Payment payment : ((ArbitraryTransaction) transaction).getPayments()) {
                    BigDecimal amount = BigDecimal.ZERO;
                    if (totalAmountOfAssets.containsKey(payment.getAsset())) {
                        amount = totalAmountOfAssets.get(payment.getAsset());
                    }
                    amount = amount.add(payment.getAmount());

                    /*
                    if (assetNames != null) {
                        assetNames.setKey(payment.getAsset());
                    }
                    */

                    totalAmountOfAssets.put(payment.getAsset(), amount);
                }

                Map amountOfAssetsJSON = new LinkedHashMap();

                for (Map.Entry<Long, BigDecimal> assetInfo : totalAmountOfAssets.entrySet()) {
                    amountOfAssetsJSON.put(assetInfo.getKey(), assetInfo.getValue().toPlainString());
                }

                transactionDataJSON.put("amounts", amountOfAssetsJSON);

            } else if (transaction.getType() == Transaction.VOTE_ON_POLL_TRANSACTION) {
                Poll poll = Controller.getInstance().getPoll(((VoteOnPollTransaction) transaction).getPoll());
                if (poll != null) {
                    transactionDataJSON.put("optionString",
                            Controller.getInstance().getPoll(((VoteOnPollTransaction) transaction).getPoll()).getOptions()
                                    .get(((VoteOnPollTransaction) transaction).getOption()).getName());
                }

            } else if (transaction.getType() == Transaction.CREATE_ORDER_TRANSACTION) {
                /*
                if (assetNames != null) {
                    assetNames.setKey(((CreateOrderTransaction) transaction).getHaveKey());
                    assetNames.setKey(((CreateOrderTransaction) transaction).getWantKey());
                }
                */

            } else if (transaction.getType() == Transaction.DEPLOY_AT_TRANSACTION) {
                transactionDataJSON.put("atAddress",
                        ((DeployATTransaction) transaction).getATaccount(dcSet).getAddress());
            }

            if (transaction.isConfirmed(dcSet)) {
                transactionDataJSON.put("blockHeight", transaction.getBlockHeight());
            }

            transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(transaction.getTimestamp()));

            transactionJSON.put("type", "transaction");
            transactionJSON.put("transaction", transactionDataJSON);
            return transactionJSON;
        }

        if (unit instanceof Block) {
            Block block = (Block) unit;

            transactionDataJSON = new LinkedHashMap();
            transactionDataJSON.put("timestamp", block.getTimestamp());
            transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(block.getTimestamp()));

            int height = block.getHeight();
            transactionDataJSON.put("confirmations", getHeight() - height + 1);
            transactionDataJSON.put("height", height);

            transactionDataJSON.put("generator", block.getCreator().getAddress());
            transactionDataJSON.put("signature", Base58.encode(block.getSignature()));

            /*
             * transactionDataJSON.put("generatingBalance",
             * block.getGeneratingBalance()); transactionDataJSON.put("atFees",
             * block.getATfee()); transactionDataJSON.put("reference",
             * Base58.encode(block.getReference()));
             * transactionDataJSON.put("generatorSignature",
             * Base58.encode(block.getGeneratorSignature()));
             * transactionDataJSON.put("transactionsSignature",
             * block.getTransactionsSignature());
             * transactionDataJSON.put("version", block.getVersion());
             */

            // transactionDataJSON.put("fee", balances[size -
            // counter].getTransactionBalance().get(0l).toPlainString());
            transactionDataJSON.put("fee", block.viewFeeAsBigDecimal());

            transactionJSON.put("type", "block");
            transactionJSON.put("block", transactionDataJSON);
            return transactionJSON;

        }

        if (unit instanceof ATTransaction) {
            ATTransaction aTtransaction = (ATTransaction) unit;
            transactionDataJSON = aTtransaction.toJSON();

            Block block = Controller.getInstance().getBlockByHeight(aTtransaction.getBlockHeight());
            long timestamp = block.getTimestamp();
            transactionDataJSON.put("timestamp", timestamp);
            transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(timestamp));

            transactionDataJSON.put("confirmations", getHeight() - ((ATTransaction) unit).getBlockHeight() + 1);

            if (((ATTransaction) unit).getRecipient().equals("1111111111111111111111111")) {
                transactionDataJSON.put("generatorAddress", block.getCreator().getAddress());
            }

            transactionJSON.put("type", "atTransaction");
            transactionJSON.put("atTransaction", transactionDataJSON);
            return transactionJSON;
        }

        return transactionJSON;
    }

    private void txCountJSONPut(int[] txsTypeCount, int txsCount, Map txCountJSON) {
        if (txsCount > 0) {
            txCountJSON.put("txsCount", txsCount);
            Map txTypeCountJSON = new LinkedHashMap();
            int n = 1;
            for (int txCount : txsTypeCount) {
                if (txCount > 0) {
                    txTypeCountJSON.put(n, txCount);
                }
                n++;
            }
            txCountJSON.put("txsTypesCount", txTypeCountJSON);
        }
    }

    public Map jsonQueryBalance(String address) {
        Map output = new LinkedHashMap();

        if (!Crypto.getInstance().isValidAddress(address)) {
            output.put("error", "Address is not valid!");
            return output;
        }

        SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetsBalances
                = dcSet.getAssetBalanceMap().getBalancesSortableList(new Account(address));

        for (Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetsBalance : assetsBalances) {
            Map assetBalance = new LinkedHashMap();

            assetBalance.put("assetName", Controller.getInstance().getAsset(assetsBalance.getA().b).getName());
            assetBalance.put("amount", assetsBalance.getB().toString());

            output.put(assetsBalance.getA().b, assetBalance);
        }

        return output;
    }

    public Map<Long, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetBalance(
            String address) {
        Map<Long, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> output = new LinkedHashMap();

        SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetsBalances = dcSet
                .getAssetBalanceMap().getBalancesSortableList(new Account(address));

        for (Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetsBalance : assetsBalances) {
            output.put(assetsBalance.getA().b, assetsBalance.getB());
        }

        return output;
    }

    @SuppressWarnings({"serial", "static-access"})
    public Map jsonQueryTransactions(String typeStr, int start) {

        int size = 200;
        List<Transaction> transactions;
        if (typeStr != null) {
            int type = Integer.parseInt(typeStr);
            transactions = dcSet.getTransactionFinalMap().getTransactionsByTitleAndType(null, type, size, true);
        } else {
            // берем все с перебором с последней
            TransactionFinalMap map = dcSet.getTransactionFinalMap();
            Iterator<Long> iterator = map.getIterator(0, true);
            int counter = size;
            transactions = new ArrayList<>();
            while (iterator.hasNext() && counter > 0 ) {
                Transaction transaction = map.get(iterator.next());
                if (transaction.getType() == Transaction.CALCULATED_TRANSACTION
                        && ((RCalculated)transaction).getMessage().equals("forging"))
                    continue;

                transactions.add(transaction);
                counter--;
            }
        }

        LinkedHashMap output = new LinkedHashMap();

        // Transactions view
        transactionsJSON(output, null, transactions, start, pageSize);

        output.put("search", "transactions");
        output.put("type", "transactions");

        return output;
    }

    @SuppressWarnings({"serial", "static-access"})
    public Map jsonQueryAddress(String address, int start) {

        List<Transaction> transactions = dcSet.getTransactionFinalMap().getTransactionsByAddressLimit(address, 100);
        LinkedHashMap output = new LinkedHashMap();
        output.put("account", address);

        Account acc = new Account(address);
        long personKey = -10;
        Tuple2<Integer, PersonCls> person = acc.getPerson();

        if (person != null) {
            output.put("label_person_name", Lang.getInstance().translateFromLangObj("Name", langObj));
            output.put("person_Img", Base64.encodeBase64String(person.b.getImage()));
            output.put("Person_Name", person.b.getName());
            personKey = person.b.getKey();

            Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balabce_LIA = acc.getBalance(AssetCls.LIA_KEY);
            output.put("registered", balabce_LIA.a.b.toPlainString());
            output.put("certified", balabce_LIA.b.b.toPlainString());
            output.put("label_registered", Lang.getInstance().translateFromLangObj("Registered", langObj));
            output.put("label_certified", Lang.getInstance().translateFromLangObj("Certified", langObj));

        }
        output.put("person_key", personKey);
        output.put("label_account", Lang.getInstance().translateFromLangObj("Account", langObj));

        // balance assets from
        output.put("Balance", balanceJSON(new Account(address)));

        // Transactions view
        transactionsJSON(output, acc, transactions, start, pageSize);

        output.put("type", "standardAccount");

        return output;
    }

    public Map jsonQueryTrade(String query) {
        Map output = new LinkedHashMap();

        //AssetNames assetNames = new AssetNames();

        List<Object> all = new ArrayList<Object>();

        String[] signatures = query.split("/");

        Transaction initiator = dcSet.getTransactionFinalMap().get(Base58.decode(signatures[0]));
        Transaction target = dcSet.getTransactionFinalMap().get(Base58.decode(signatures[1]));
        Trade trade = dcSet.getTradeMap()
                .get(Fun.t2(Transaction.makeDBRef(initiator.getHeightSeqNo()),
                        Transaction.makeDBRef(target.getHeightSeqNo())));
        output.put("type", "trade");
        output.put("trade", query);

        all.add(trade.toJson(0));

        all.add(Controller.getInstance().getTransaction(Base58.decode(signatures[0])));
        all.add(Controller.getInstance().getTransaction(Base58.decode(signatures[1])));

        int size = all.size();

        output.put("start", size);
        output.put("end", 1);

        int counter = 0;
        for (Object unit : all) {
            output.put(size - counter, jsonUnitPrint(unit)); //, assetNames));
            counter++;
        }

        //output.put("assetNames", assetNames.getMap());//

        return output;
    }

    // http://127.0.0.1:9067/index/blockexplorer.json?peers&lang=en&view=1&sort_reliable=1&sort_ping=1&start=4&row_view=3
    // view=1 0- view only work Peers; 1 - view all Peers
    // sort_reliable=1 0 - as sort ; 1 - des sort
    // sort_ping=1 0 - as sort ; 1 - des sort
    // start=0 start org.erachain.records 0....
    // row_view=3 view org.erachain.records 1.....

    public Map jsonQueryPeers(UriInfo info) {

        int start = 0;
        int end = 100;
        int view = 0;
        int sortPing = 0;
        PeersTableModel model_Peers = new PeersTableModel();
        // start org.erachain.records
        try {
            start = Integer.valueOf((info.getQueryParameters().getFirst("start")));
        } catch (NumberFormatException e3) {
            // TODO Auto-generated catch block

        }
        // end org.erachain.records
        try {
            end = Integer.valueOf((info.getQueryParameters().getFirst("row_view")));
        } catch (NumberFormatException e3) {
            // TODO Auto-generated catch block

        }
        // view all| only Active
        try {
            model_Peers.setView(Integer.valueOf((info.getQueryParameters().getFirst("view"))));
        } catch (NumberFormatException e2) {
            // TODO Auto-generated catch block
            // all peers
            model_Peers.setView(1);
        }

        // sort reliable
        try {
            model_Peers.setSortReliable(Integer.valueOf(info.getQueryParameters().getFirst("sort_reliable")));
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
        // sort PING
        try {
            model_Peers.setSortPing(Integer.valueOf(info.getQueryParameters().getFirst("sort_ping")));
        } catch (NumberFormatException e1) {
            // TODO Auto-generated catch block
            // e1.printStackTrace();
        }
        // repaint model
        model_Peers.fireTableDataChanged();

        Map output = new LinkedHashMap();

        int column_Count = model_Peers.getColumnCount();

        for (int column = 0; column < column_Count; column++) {

            output.put("Label_" + model_Peers.getColumnNameOrigin(column).replace(' ', '_'),
                    Lang.getInstance().translateFromLangObj(model_Peers.getColumnNameOrigin(column), langObj));
        }

        Map out_peers = new LinkedHashMap();
        // if (rowCount> model_Peers.getRowCount()) rowCount =
        // model_Peers.getRowCount();
        int rowCount = start + end;
        int rowCount1 = model_Peers.getRowCount();
        if (rowCount >= rowCount1) {
            rowCount = rowCount1;
            output.put("end_page", "end");
        }
        for (int row = start; row < rowCount; row++) {
            Map out_peer = new LinkedHashMap();

            for (int column = 0; column < column_Count; column++) {
                out_peer.put(model_Peers.getColumnNameOrigin(column).replace(' ', '_'),
                        model_Peers.getValueAt(row, column).toString());

            }
            out_peers.put(row, out_peer);
        }

        // calc many pages
        output.put("pages", M_Integer.roundUp((float) rowCount1 / end));
        output.put("Label_No", Lang.getInstance().translateFromLangObj("No.", langObj));
        output.put("Peers", out_peers);
        return output;
    }

    public Map jsonQueryStatements(int start) {
        Map output = new LinkedHashMap();
        WebStatementsTableModelSearch model_Statements = new WebStatementsTableModelSearch();
        int rowCount = start + 20;
        int column_Count = model_Statements.getColumnCount();

        for (int column = 0; column < column_Count; column++) {

            output.put("Label_" + model_Statements.getColumnNameNO_Translate(column).replace(' ', '_'), Lang
                    .getInstance().translateFromLangObj(model_Statements.getColumnNameNO_Translate(column), langObj));
        }

        Map out_Statements = new LinkedHashMap();
        // if (rowCount> model_Peers.getRowCount()) rowCount =
        // model_Peers.getRowCount();
        rowCount = model_Statements.getRowCount();
        for (int row = 0; row < rowCount; row++) {
            Map out_statement = new LinkedHashMap();
            Transaction statement = model_Statements.get_Statement(row);
            out_statement.put("Block", statement.getBlockHeight());
            out_statement.put("seqNo", statement.getSeqNo());
            out_statement.put("person_key", model_Statements.get_person_key(row));

            for (int column = 0; column < column_Count; column++) {
                String value = model_Statements.getValueAt(row, column).toString();
                if (value == null || value.isEmpty())
                    value = "***";

                out_statement.put(model_Statements.getColumnNameNO_Translate(column).replace(' ', '_'), value);
            }
            out_Statements.put(row, out_statement);
        }
        // output.put("rowCount", rowCount);
        // output.put("start", start);
        output.put("Label_No", Lang.getInstance().translateFromLangObj("No.", langObj));
        output.put("Label_block", Lang.getInstance().translateFromLangObj("Block", langObj));
        output.put("Statements", out_Statements);
        return output;
    }


    public Map jsonQueryTemplate(Long key) {
        Map output = new LinkedHashMap();

        TemplateCls template = (TemplateCls) dcSet.getItemTemplateMap().get(key);

        Map templateJSON = new LinkedHashMap();
        templateJSON.put("key", template.getKey());
        templateJSON.put("name", template.getName());
        templateJSON.put("description", template.getDescription());
        templateJSON.put("owner", template.getOwner().getAddress());

        output.put("template", templateJSON);

        output.put("label_Template", Lang.getInstance().translateFromLangObj("Template", langObj));
        output.put("label_Key", Lang.getInstance().translateFromLangObj("Key", langObj));
        output.put("label_Creator", Lang.getInstance().translateFromLangObj("Creator", langObj));
        output.put("label_Description", Lang.getInstance().translateFromLangObj("Description", langObj));

        return output;
    }

    public Map jsonQueryStatus(Long key) {
        Map output = new LinkedHashMap();

        StatusCls template = (StatusCls) dcSet.getItemStatusMap().get(key);

        Map templateJSON = new LinkedHashMap();
        templateJSON.put("key", template.getKey());
        templateJSON.put("name", template.getName());
        templateJSON.put("description", template.getDescription());
        templateJSON.put("owner", template.getOwner().getAddress());

        output.put("status", templateJSON);

        output.put("label_Template", Lang.getInstance().translateFromLangObj("Status", langObj));
        output.put("label_Key", Lang.getInstance().translateFromLangObj("Key", langObj));
        output.put("label_Creator", Lang.getInstance().translateFromLangObj("Creator", langObj));
        output.put("label_Description", Lang.getInstance().translateFromLangObj("Description", langObj));

        return output;
    }

    private Map jsonQueryStatement(String block, String seqNo) {
        // TODO Auto-generated method stub
        Map output = new LinkedHashMap();

        RSignNote trans = (RSignNote) dcSet.getTransactionFinalMap().get(new Integer(block),
                new Integer(seqNo));
        output.put("Label_type", Lang.getInstance().translateFromLangObj("Type", langObj));
        output.put("Label_statement", Lang.getInstance().translateFromLangObj("Statement", langObj));
        output.put("Label_creator", Lang.getInstance().translateFromLangObj("Creator", langObj));
        output.put("Label_date", Lang.getInstance().translateFromLangObj("Date", langObj));
        output.put("Label_block", Lang.getInstance().translateFromLangObj("Block", langObj));
        output.put("Label_seqNo", Lang.getInstance().translateFromLangObj("seqNo", langObj));
        output.put("Label_No", Lang.getInstance().translateFromLangObj("No.", langObj));
        output.put("Label_pubKey", Lang.getInstance().translateFromLangObj("Public Key", langObj));
        output.put("Label_signature", Lang.getInstance().translateFromLangObj("Signature", langObj));

        output.put("block", block);
        output.put("seqNo", seqNo);

        output.put("pubKey", Base58.encode(trans.getCreator().getPublicKey()));
        output.put("sign", Base58.encode(trans.getSignature()));

        //TemplateCls statement = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, trans.getKey());

        if (!trans.isEncrypted()) {

            if (trans.getVersion() == 2) {
                // version 2
                Tuple4<String, String, JSONObject, HashMap<String, Tuple2<Boolean, byte[]>>> map_Data;

                try {
                    map_Data = trans.parse_Data_V2();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }

                if (map_Data.b != null) {
                    output.put("Label_title", Lang.getInstance().translateFromLangObj("Title", langObj));
                    output.put("title", map_Data.b);
                }

                JSONObject jSON = map_Data.c;
                // parse JSON
                if (jSON != null) {

                    if (jSON.containsKey("TM")) {
                        // V2.1 Template
                        Long key = new Long(jSON.get("TM") + "");
                        TemplateCls template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, key);
                        if (template != null) {
                            String description = template.getDescription();

                            // Template Params
                            if (jSON.containsKey("PR")) {
                                String str = jSON.get("PR").toString();
                                JSONObject params = new JSONObject();
                                ;
                                try {
                                    params = (JSONObject) JSONValue.parseWithException(str);
                                } catch (ParseException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                Set<String> kS = params.keySet();
                                for (String s : kS) {
                                    description = description.replace("{{" + s + "}}", (CharSequence) params.get(s));
                                }

                            }

                            output.put("body", description);

                        }

                    } else if (jSON.containsKey("Template")) {

                        // V2.0 Template
                        Long key = new Long(jSON.get("Template") + "");
                        TemplateCls template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, key);
                        if (template != null) {
                            String description = template.getDescription();

                            // Template Params
                            if (jSON.containsKey("Statement_Params")) {

                                String str = jSON.get("Statement_Params").toString();
                                JSONObject params = new JSONObject();
                                ;
                                try {
                                    params = (JSONObject) JSONValue.parseWithException(str);
                                } catch (ParseException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                Set<String> kS = params.keySet();
                                for (String s : kS) {
                                    description = description.replace("{{" + s + "}}", (CharSequence) params.get(s));
                                }

                            }

                            output.put("body", description);

                        }

                    }

                    if (jSON.containsKey("MS")) {
                        // v 2.1
                        output.put("message", jSON.get("MS"));

                    } else if (jSON.containsKey("Message")) {
                        // Message v2.0
                        output.put("message", jSON.get("Message"));

                    }

                    // Hashes
                    if (jSON.containsKey("HS")) {
                        // v2.1
                        output.put("Label_hashes", Lang.getInstance().translateFromLangObj("Hashes", langObj));
                        String hashes = "";
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
                        for (String s : kS) {
                            hashes += i + " " + s + " " + params.get(s) + "<br>";
                        }

                        output.put("hashes", hashes);

                    } else if (jSON.containsKey("Hashes")) {
                        // v2.0
                        output.put("Label_hashes", Lang.getInstance().translateFromLangObj("Hashes", langObj));
                        String hashes = "";
                        String str = jSON.get("Hashes").toString();
                        JSONObject params = new JSONObject();
                        try {
                            params = (JSONObject) JSONValue.parseWithException(str);
                        } catch (ParseException e) {
                            logger.error(e.getMessage(), e);
                        }
                        Set<String> kS = params.keySet();

                        int i = 1;
                        for (String s : kS) {
                            hashes += i + " " + s + " " + params.get(s) + "<br>";
                        }

                        output.put("hashes", hashes);
                    }

                }

                // parse files
                if (jSON.containsKey("F")) {
                    // v 2.1
                    output.put("Label_files", Lang.getInstance().translateFromLangObj("Files", langObj));
                    String files = "";
                    String str = jSON.get("F").toString();
                    JSONObject params = new JSONObject();
                    try {
                        params = (JSONObject) JSONValue.parseWithException(str);
                    } catch (ParseException e) {
                        logger.error(e.getMessage(), e);
                    }
                    Set<String> kS = params.keySet();

                    int i = 1;
                    JSONObject ss = new JSONObject();
                    for (String s : kS) {

                        ss = (JSONObject) params.get(s);

                        files += i + " " + ss.get("FN");
//                        files += "<a href ='../apidocuments/getFile?download=false&block="
//                                + block + "&seqNo=" + seqNo + "&name=" + ss.get("FN") + "'> "
//                                + Lang.getInstance().translateFromLangObj("View", langObj) + " </a>";
                        files += "<a href ='../apidocuments/getFile?download=true&block="
                                + block + "&seqNo=" + seqNo + "&name=" + ss.get("FN") + "'> "
                                + Lang.getInstance().translateFromLangObj("Download", langObj) + "</a><br>";
                    }

                    output.put("files", files);

                } else if (jSON.containsKey("&*&*%$$%_files_#$@%%%")) {
                    // v2.0
                    output.put("Label_files", Lang.getInstance().translateFromLangObj("Files", langObj));
                    String files = "";
                    String str = jSON.get("&*&*%$$%_files_#$@%%%").toString();
                    JSONObject params = new JSONObject();
                    try {
                        params = (JSONObject) JSONValue.parseWithException(str);
                    } catch (ParseException e) {
                        logger.error(e.getMessage(), e);
                    }
                    Set<String> kS = params.keySet();

                    int i = 1;
                    JSONObject ss = new JSONObject();
                    for (String s : kS) {

                        ss = (JSONObject) params.get(s);

                        files += i + " " + ss.get("File_Name");
//                        files += "<a href = '../apidocuments/getFile?download=false&block="
//                                + block + "&seqNo=" + seqNo + "&name=" + ss.get("File_Name")
//                                + "'> " + Lang.getInstance().translateFromLangObj("View", langObj) + " </a><br>";
                        files += "<a href = '../apidocuments/getFile?download=true&block="
                                + block + "&seqNo=" + seqNo + "&name=" + ss.get("File_Name")
                                + "'> " + Lang.getInstance().translateFromLangObj("Download", langObj) + " </a><br>";
                    }

                    output.put("files", files);

                }

            } else {

                // version 1
                try {

                    Set<String> kS;
                    String description = "";
                    String str;
                    JSONObject params = new JSONObject();
                    JSONObject data = new JSONObject();
                    TemplateCls template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, trans.getKey());
                    if (template != null) {
                        description = template.getDescription();
                        data = (JSONObject) JSONValue
                                .parseWithException(new String(trans.getData(), Charset.forName("UTF-8")));

                        output.put("Label_title", Lang.getInstance().translateFromLangObj("Title", langObj));
                        output.put("title", data.get("Title"));

                        output.put("message", data.get("Message"));

                        str = data.get("Statement_Params").toString();
                        params = (JSONObject) JSONValue.parseWithException(str);
                        kS = params.keySet();
                        for (String s : kS) {
                            description = description.replace("{{" + s + "}}", (CharSequence) params.get(s));
                        }

                        output.put("body", description);

                    }

                    output.put("Label_hashes", Lang.getInstance().translateFromLangObj("Hashes", langObj));

                    String hashes = "";
                    str = data.get("Hashes").toString();
                    params = (JSONObject) JSONValue.parseWithException(str);
                    kS = params.keySet();

                    int i = 1;
                    for (String s : kS) {
                        hashes += i + " " + s + " " + params.get(s) + "<br>";
                    }
                    output.put("hashes", hashes);

                } catch (ParseException e) {

                    output.put("statement", new String(trans.getData(), Charset.forName("UTF-8")));

                }
            }

        } else {

            TemplateCls template = (TemplateCls) ItemCls.getItem(dcSet, ItemCls.TEMPLATE_TYPE, trans.getKey());
            output.put("statement",
                    template.getName() + "<br>" + Lang.getInstance().translateFromLangObj("Encrypted", langObj));
        }

        output.put("creator", trans.getCreator().getPersonAsString());

        Tuple2<Integer, PersonCls> personItem = trans.getCreator().getPerson();
        if (personItem != null) {
            output.put("creator_key", personItem.b.getKey());
            output.put("creator_name", personItem.b.getName());
        } else {
            output.put("creator_key", "");
            output.put("creator_name", "");
        }

        output.put("date", df.format(new Date(trans.getTimestamp())).toString());

        output.put("vouches_table", WebTransactionsHTML.getInstance().getVouchesNew(personItem, trans, langObj));


        return output;
    }

    public Map jsonQueryTX(String query) {

        Map output = new LinkedHashMap();

        //AssetNames assetNames = new AssetNames();

        TreeSet<BlExpUnit> all = new TreeSet<>();
        Map<Tuple2<byte[], byte[]>, Trade> trades = new TreeMap<Tuple2<byte[], byte[]>, Trade>();

        String[] signatures = query.split(",");

        byte[] signatureBytes = null; // new clear

        for (int i = 0; i < signatures.length; i++) {

            Transaction transaction = null; // new
            try {
                // as Base58
                signatureBytes = Base58.decode(signatures[i]);
                transaction = Controller.getInstance().getTransaction(signatureBytes);
            } catch (Exception e) {
                // as 12345-12
                transaction = dcSet.getTransactionFinalMap().getRecord(signatures[i]);
            }

            if (transaction == null)
                continue;

            if (transaction.getType() == Transaction.SIGN_NOTE_TRANSACTION) {//.ISSUE_STATEMENT_TRANSACTION){
                int block = transaction.getBlockHeight();
                int seqNo = transaction.getSeqNo();
                output.putAll(jsonQueryStatement(block + "", seqNo + ""));
                output.put("type", "statement");

            } else {
                output.put("type", "transaction");
                output.put("body", WebTransactionsHTML.getInstance().get_HTML(transaction, langObj));
                output.put("Label_Transaction", Lang.getInstance().translateFromLangObj("Transaction", langObj));
                output.put("heightSeqNo", transaction.viewHeightSeq());
            }
        }


        return output;
    }

    public Map jsonQueryBlock(String query, int start) throws WrongSearchException {

        LinkedHashMap output = new LinkedHashMap();
        List<Object> all = new ArrayList<Object>();
        int[] txsTypeCount = new int[256];
        int aTTxsCount = 0;
        Block block = null;

        //AssetNames assetNames = new AssetNames();

        if (query.matches("\\d+")) {
            int parseInt;
            try {
                parseInt = Integer.parseInt(query);
            } catch (NumberFormatException e) {
                logger.info("Wrong search while process blocks... ");
                throw new WrongSearchException();
            }
            block = Controller.getInstance().getBlockByHeight(dcSet, parseInt);
            if (block == null) {
                block = Controller.getInstance().getBlockByHeight(dcSet, 1);
            }
        } else if (query.equals("last")) {
            block = getLastBlock();
        } else {
            try {
                block = Controller.getInstance().getBlock(Base58.decode(query));
            } catch (Exception e) {
                logger.info("Wrong search while process blocks... ");
                throw new WrongSearchException();
            }
            if (block == null) {
                logger.info("Wrong search while process blocks... ");
                throw new WrongSearchException();
            }
        }

        int seqNo = 0;
        for (Transaction transaction : block.getTransactions()) {
            transaction.setDC(dcSet, block.heightBlock, block.heightBlock, ++seqNo);
            all.add(transaction);
            txsTypeCount[transaction.getType() - 1]++;
        }

        // Transactions view
        transactionsJSON(output, null, block.getTransactions(), start, pageSize);

        int txsCount = all.size();

        LinkedHashMap<Tuple2<Integer, Integer>, ATTransaction> atTxs = dcSet.getATTransactionMap()
                .getATTransactions(block.getHeight());

        for (Entry<Tuple2<Integer, Integer>, ATTransaction> e : atTxs.entrySet()) {
            all.add(e.getValue());
            aTTxsCount++;
        }
        output.put("type", "block");

        output.put("blockSignature", Base58.encode(block.getSignature()));
        output.put("blockHeight", block.getHeight());

        if (block.getHeight() > 1) {
            if (block.getParent(dcSet) != null) {
                output.put("parentBlockSignature", Base58.encode(block.getParent(dcSet).getSignature()));
            }
        } else {
            output.put("parentBlockSignature", "");
        }

        if (block.getChild(dcSet) != null) {
            output.put("childBlockSignature", Base58.encode(block.getChild(dcSet).getSignature()));
        }
        int size = all.size();

        Map txCountJSON = new LinkedHashMap();

        txCountJSONPut(txsTypeCount, txsCount, txCountJSON);

        if (aTTxsCount > 0) {
            txCountJSON.put("aTTxsCount", aTTxsCount);
        }

        txCountJSON.put("allCount", txsCount);

        output.put("countTx", txCountJSON);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Transaction transaction : block.getTransactions()) {
            for (Account account : transaction.getInvolvedAccounts()) {
                BigDecimal amount = transaction.getAmount(account);
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    totalAmount = totalAmount.add(amount);
                }
            }
        }

        output.put("totalAmount", totalAmount.toPlainString());

        BigDecimal totalATAmount = BigDecimal.ZERO;

        for (Map.Entry<Tuple2<Integer, Integer>, ATTransaction> e : atTxs.entrySet()) {
            totalATAmount = totalATAmount.add(BigDecimal.valueOf(e.getValue().getAmount()));
        }

        output.put("totalATAmount", totalATAmount.toPlainString());
        output.put("totalFee", block.viewFeeAsBigDecimal());
        output.put("version", block.getVersion());

        output.put("generatingBalance", block.getForgingValue());
        output.put("winValue", block.getWinValue());
        output.put("target", block.getTarget());
        output.put("winValueTargeted", block.calcWinValueTargeted());

        output.put("start", size + 1);
        output.put("end", 1);


        int counter = 0;

        {
            Map transactionJSON = new LinkedHashMap();
            Map transactionDataJSON = new LinkedHashMap();

            transactionDataJSON.put("timestamp", block.getTimestamp());
            transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(block.getTimestamp()));

            int height = block.getHeight();
            transactionDataJSON.put("confirmations", getHeight() - height + 1);
            transactionDataJSON.put("height", height);

            transactionDataJSON.put("generator", block.getCreator().getAddress());
            transactionDataJSON.put("signature", Base58.encode(block.getSignature()));
            transactionDataJSON.put("reference", Base58.encode(block.getReference()));
            transactionDataJSON.put("generatorSignature", Base58.encode(block.getSignature()));
            transactionDataJSON.put("version", block.getVersion());

            transactionDataJSON.put("fee", block.viewFeeAsBigDecimal());

            transactionJSON.put("type", "block");
            transactionJSON.put("block", transactionDataJSON);

            output.put(counter + 1, transactionJSON);
        }
        output.put("label_block", Lang.getInstance().translateFromLangObj("Block", langObj));
        output.put("label_Block_version", Lang.getInstance().translateFromLangObj("Block version", langObj));
        output.put("label_Transactions_count",
                Lang.getInstance().translateFromLangObj("Transactions count", langObj));
        output.put("label_Total_Amount", Lang.getInstance().translateFromLangObj("Total Amount", langObj));
        output.put("label_Total_AT_Amount", Lang.getInstance().translateFromLangObj("Total AT Amount", langObj));
        output.put("label_Total_Fee", Lang.getInstance().translateFromLangObj("Total Fee", langObj));

        output.put("label_Win_Value", Lang.getInstance().translateFromLangObj("Win Value", langObj));
        output.put("label_Generating_Balance",
                Lang.getInstance().translateFromLangObj("Generating Balance", langObj));
        output.put("label_Target", Lang.getInstance().translateFromLangObj("Target", langObj));
        output.put("label_Targeted_Win_Value",
                Lang.getInstance().translateFromLangObj("Targeted Win Value", langObj));

        output.put("label_Parent_block", Lang.getInstance().translateFromLangObj("Parent block", langObj));
        output.put("label_Current_block", Lang.getInstance().translateFromLangObj("Current block", langObj));
        output.put("label_Child_block", Lang.getInstance().translateFromLangObj("Child block", langObj));
        output.put("label_Including", Lang.getInstance().translateFromLangObj("Including", langObj));
        output.put("label_Signature", Lang.getInstance().translateFromLangObj("Signature", langObj));


        return output;
    }

    public Map jsonQueryUnconfirmedTXs() {
        Map output = new LinkedHashMap();

        //AssetNames assetNames = new AssetNames();

        List<Transaction> all = new ArrayList<>(
                Controller.getInstance().getUnconfirmedTransactions(0, 100, true));

        output.put("type", "unconfirmed");

        int size = all.size();

        output.put("start", size);

        if (size > 0) {
            output.put("end", 1);
        } else {
            output.put("end", 0);
        }

        int counter = 0;
        for (Object unit : all) {
            counter++;

            output.put(counter, jsonUnitPrint(unit)); //, assetNames));
        }

        return output;
    }

    public int getHeight() {
        return dcSet.getBlockMap().size();
    }

    public Tuple2<Integer, Long> getHWeightFull() {
        return Controller.getInstance().getBlockChain().getHWeightFull(dcSet);
    }

    public Block getLastBlock() {
        return dcSet.getBlockMap().last();
    }

    //Секундомер с остановом(stopwatch). При создании "секундомер пошел"
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
    }


    public class BigDecimalComparator_C implements Comparator<Tuple3<String, BigDecimal, BigDecimal>> {

        @Override
        public int compare(Tuple3<String, BigDecimal, BigDecimal> a, Tuple3<String, BigDecimal, BigDecimal> b) {
            try {
                return a.c.compareTo(b.c);
            } catch (Exception e) {
                return 0;
            }
        }

    }


//  todo Gleb -----------------------------------------------------------------------------------------------------------

    public void transactionsJSON(LinkedHashMap output, Account account, List<Transaction> transactions, int fromIndex, int pageSize) {
        LinkedHashMap outputTXs = new LinkedHashMap();
        int i = 0;
        boolean outcome;
        int type;
        int height = Controller.getInstance().getMyHeight();

        LinkedHashMap transactionsJSON = new LinkedHashMap();
        int listSize = transactions.size();
        List<Transaction> transactionList = (pageSize == 0) ? transactions
                : transactions.subList(fromIndex, Math.min(fromIndex + pageSize, listSize));
        for (Transaction transaction : transactionList) {

            transaction.setDC(dcSet);

            outcome = true;

            LinkedHashMap out = new LinkedHashMap();

            out.put("block", transaction.getBlockHeight());// .getSeqNo(dcSet));

            out.put("seqNo", transaction.getSeqNo());

            if (transaction.getType() == Transaction.CALCULATED_TRANSACTION) {
                RCalculated txCalculated = (RCalculated) transaction;
                outcome = txCalculated.getAmount().signum() < 0;

                //out.put("reference", "--");
                out.put("signature", transaction.getBlockHeight() + "-" + transaction.getSeqNo());


                String message = txCalculated.getMessage();
                if (message.equals("forging")) {
                    out.put("date", DateTimeFormat.timestamptoString(dcSet.getBlockMap().get(transaction.getBlockHeight()).getTimestamp()));
                } else {
                    out.put("date", message);
                }

                String typeName = transaction.viewFullTypeName();
                if (typeName.equals("_protocol_")) {
                    out.put("type", message);
                } else {
                    out.put("type", typeName);
                }

                out.put("confirmations", transaction.getConfirmations(height));

                out.put("creator", txCalculated.getRecipient().getPersonAsString());
                out.put("creator_addr", txCalculated.getRecipient().getAddress());

                out.put("size", "--");
                out.put("fee", "--");

            } else {
                out.put("signature", Base58.encode(transaction.getSignature()));
                out.put("date", DateTimeFormat.timestamptoString(transaction.getTimestamp()));
                String typeName = transaction.viewFullTypeName();
                out.put("type", typeName);
                if (transaction.getCreator() == null) {
                    out.put("creator", GenesisBlock.CREATOR.getAddress());
                    out.put("creator_addr", "GENESIS");
                    if (transaction.getType() == Transaction.GENESIS_SEND_ASSET_TRANSACTION) {
                        outcome = false;
                    }

                } else {

                    out.put("publickey", Base58.encode(transaction.getCreator().getPublicKey()));

                    Account atSideAccount;
                    atSideAccount = transaction.getCreator();
                    if (account != null) {
                        atSideAccount = transaction.getCreator();
                        type = transaction.getType();
                        if (type == Transaction.SEND_ASSET_TRANSACTION) {
                            RSend rSend = (RSend) transaction;
                            if (rSend.getCreator().equals(account)) {
                                outcome = false;
                                atSideAccount = rSend.getRecipient();
                            }
                            // возврат и взять на харенение обратный
                            outcome = outcome ^ !rSend.isBackward() ^ (rSend.getActionType() == TransactionAmount.ACTION_HOLD);
                        }
                    }

                    out.put("creator", atSideAccount.getPersonAsString());
                    out.put("creator_addr", atSideAccount.getAddress());

                }

                out.put("size", transaction.viewSize(Transaction.FOR_NETWORK));
                out.put("fee", transaction.getFee());
                out.put("confirmations", transaction.getConfirmations(height));

            }


            long absKey = transaction.getAbsKey();
            String amount = transaction.viewAmount();
            if (absKey > 0) {
                if (amount.length() > 0) {
                    out.put("amount_key",
                            (outcome ? "-" : "+") + transaction.viewAmount() + ":" + absKey);
                } else {
                    out.put("amount_key", "" + absKey);
                }
            } else {
                out.put("amount_key", "");
            }

            if (transaction.viewRecipient() == null) {
                out.put("recipient", "-");
            } else {
                out.put("recipient", transaction.viewRecipient());
            }

            transactionsJSON.put(i, out);
            i++;
        }

        outputTXs.put("transactions", transactionsJSON);
        outputTXs.put("label_block", Lang.getInstance().translateFromLangObj("Block", langObj));
        outputTXs.put("label_date", Lang.getInstance().translateFromLangObj("Date", langObj));
        outputTXs.put("label_type_transaction", Lang.getInstance().translateFromLangObj("Type", langObj));
        outputTXs.put("label_creator", Lang.getInstance().translateFromLangObj("Creator", langObj));
        outputTXs.put("label_atside", Lang.getInstance().translateFromLangObj("Side", langObj));
        outputTXs.put("label_asset", Lang.getInstance().translateFromLangObj("Asset", langObj));
        outputTXs.put("label_amount", Lang.getInstance().translateFromLangObj("Amount", langObj));
        outputTXs.put("label_confirmations", Lang.getInstance().translateFromLangObj("Confirmations", langObj));
        outputTXs.put("label_recipient", Lang.getInstance().translateFromLangObj("Recipient", langObj));
        outputTXs.put("label_size", Lang.getInstance().translateFromLangObj("Size", langObj));
        outputTXs.put("label_seqNo", Lang.getInstance().translateFromLangObj("SeqNo", langObj));
        outputTXs.put("label_signature", Lang.getInstance().translateFromLangObj("Signature", langObj));
        outputTXs.put("label_amount_key", Lang.getInstance().translateFromLangObj("Amount:Key", langObj));
        outputTXs.put("label_fee", Lang.getInstance().translateFromLangObj("Fee", langObj));
        outputTXs.put("label_transactions_table", Lang.getInstance().translateFromLangObj("Transactions", langObj));

        output.put("Transactions", outputTXs);

        output.put("pageSize", pageSize);
        output.put("start", fromIndex);
        output.put("listSize", listSize);

        return;

    }

}
