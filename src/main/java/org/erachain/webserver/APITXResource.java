package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.api.TransactionsResource;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;
import org.erachain.utils.StrJSonFine;
import org.erachain.utils.TransactionTimestampComparator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

@Path("api/tx")
@Produces(MediaType.APPLICATION_JSON)
public class APITXResource {

    @Context
    HttpServletRequest request;

    @GET
    public Response Default() {

        Map<String, String> help = new LinkedHashMap<String, String>();

        help.put("api/tx/{signature}",
                Lang.T("Get transaction by signature."));
        help.put("api/tx/bynumber/{height-sequence}",
                "GET transaction by Height and Sequence (SeqNo)");
        help.put("api/tx/signature/{height-sequence}",
                "GET transaction Signature by Height and Sequence (SeqNo)");
        help.put("api/tx/raw/{height-sequence or signature}",
                "GET transaction RAW (Base64) by Height and Sequence (SeqNo) or signature (Base58)");
        help.put("api/tx/signs/{height-sequence}",
                "GET Signs of transaction by Height and Sequence");
        help.put("api/tx/vouches/{height-sequence}",
                "GET Vouches of transaction by Height and Sequence");
        help.put("api/tx/incomingfromblock/{address}/{blockStart}?type={type}",
                Lang.T("Get Incoming transactions for Address from {blockStart}. Filter by type. Limit checked blocks = 2000 or 100 found transactions. If blocks not end at height - NEXT parameter was set."));

        // DEPRECATED
        //help.put("api/tx/byaddress?address={address}&asset={asset}&txType={txType}&unconfirmed=true",
        //        Lang.T("Get all transactions (and Unconfirmed) for Address & Asset Key by transaction type. Here txType is option parameter"));

        // DEPRECATED
        //help.put("api/tx/lastbyaddress/{address}?timestamp={Timestamp}&limit={Limit}&unconfirmed=true",
        //        "Get last transactions (and Unconfirmed) from Unix Timestamp milisec(1512777600000)");

        help.put("api/tx/unconfirmed?address={address}&type={type}&from={from}&count={count}&descending=true",
                Lang.T("Get all incoming unconfirmed transaction by address, type transaction, timestamp limited by count"));

        help.put("api/tx/unconfirmedincomes/{address}?type={type}&from={from}&count={count}&descending=true",
                Lang.T("Get all unconfirmed transactions for Address from Start at Count filtered by Type"));

        help.put("api/tx/byblock/{height}", Lang.T("Get all transactions from Block"));

        help.put("api/tx/list?from=[seqNo]&offset={0}&limit={100}&desc&noforge",
                Lang.T("Get list of transactions. Set [seqNo] as 1234-1. Use 'noforge' for skip forging transactions"));

        help.put("api/tx/listbyaddress/{address}?from=[seqNo]&offset={0}&limit={100}&desc&noforge",
                Lang.T("Get list of transactions for address. Set [seqNo] as 1234-1. Use [noforge] for skip forging transactions"));

        help.put("api/tx/listbyaddressandtype/{address}/{type}?creator={false|true}&from=[seqNo]&offset={0}&limit={100}&desc&noforge",
                Lang.T("Get list of transactions for address and type. If [creator]=true - only as creator, if [creator]=false - only recipient, Set [seqNo] as 1234-1. Use [noforge] for skip forging transactions"));


        help.put("api/tx/find?address={address}&creator={creator}&recipient={recipient}&from=[seqNo]&startblock{s_minHeight}&endblock={s_maxHeight}&type={type Transaction}&service={service}&desc={false}&offset={offset}&limit={limit}&unconfirmed=false&count=false",
                Lang.T("Find transactions. Set [seqNo] as 1234-1"));

        help.put("api/tx/search?q={query}&from=[seqNo]&useforge={false}&offset={offset}&limit={limit}&fullpage={false}",
                Lang.T("Search transactions by Query via title and tags. Query=SeqNo|Signature|FilterWords. Result[0-1] - START & END Seq-No for use in paging (see as make it in blockexplorer. Signature as Base58. Set Set FilterWords as preffix words separated by space. Set [seqNo] as 1234-1. For use forge set &useforge=true. For fill full page - use fullpage=true"));

        help.put("api/tx/rawbyblock/{height}?forDeal={DEAL}", "Get raw transaction(encoding Base58). forDeal = 1..5 (FOR_MYPACK, FOR_PACK, FOR_NETWORK, FOR_DB_RECORD). By default forDeal is 3(for network)");

        help.put("api/tx/raw64byblock/{height}?forDeal={DEAL}", "Get raw transaction(encoding Base44 - more fast). forDeal = 1..5 (FOR_MYPACK, FOR_PACK, FOR_NETWORK, FOR_DB_RECORD). By default forDeal is 3(for network)");

        help.put("api/tx/links/{height-sequence}",
                "GET Links of transaction by Height and Sequence (SeqNo)");

        help.put("GET api/tx/types", "Return array of transaction types.");

        help.put("GET api/tx/parse/{raw in BaseXX}?check&lang=en&base58", "Parse RAW (transaction byte-code) and check. Param [lang] for localize error message. For validate use [check]. If RAW in Base58 use [base58]. Base58 is slow - use it for test only.");
        help.put("POST api/tx/parse?check&lang=en", "See 'GET parse'. Body: [RAW in Base64 only]");

        help.put("GET api/tx/broadcast/{raw in BaseXX}?lang=en&base58", "Broadcast RAW in Base58 or Base64. Base58 is slow - use it for test only. Use [lang] for localize error message. If RAW in Base58 use [base58]");
        help.put("POST api/tx/broadcast?lang=en", "See 'GET broadcast'. Body: [RAW in Base64 only]");
        help.put("POST api/tx/broadcastjson JSON", "See 'GET broadcast'. Body is JSON: {\"raw\":\"Base64\", \"lang\":\"en|ru\"}");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(help)).build();

    }

    @GET
    @Path("{signature}")
    public Response getBySign(@PathParam("signature") String signature) {

        Map out = new JSONObject();

        int step = 1;

        try {
            byte[] key = Base58.decode(signature);

            ++step;
            Transaction transaction = Controller.getInstance().getTransaction(key, DCSet.getInstance());
            out = transaction.toJson();

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "signature error, use Base58 value");
            else if (step == 2)
                out.put("message", "Transaction not found");
            else
                out.put("message", e.getMessage());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }

    @GET
    @Path("bynumber/{number}")
    public Response getByNumber(@PathParam("number") String numberStr) {

        Map out = new JSONObject();
        int step = 1;

        try {

            String[] strA = numberStr.split("\\-");
            int height = Integer.parseInt(strA[0]);
            int seq = Integer.parseInt(strA[1]);

            ++step;
            Transaction transaction = DCSet.getInstance().getTransactionFinalMap().get(height, seq);
            out = transaction.toJson();

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "height-sequence error, use integer-integer value");
            else if (step == 2)
                out.put("message", "Transaction not found");
            else
                out.put("message", e.getMessage());
        }


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }


    // getsignature
    @GET
    @Path("signature/{number}")
    public Response getSignByNumber(@PathParam("number") String numberStr) {

        int step = 1;

        try {

            String[] strA = numberStr.split("\\-");
            int height = Integer.parseInt(strA[0]);
            int seq = Integer.parseInt(strA[1]);

            ++step;
            Transaction transaction = DCSet.getInstance().getTransactionFinalMap().get(height, seq);

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(transaction.viewSignature())
                    .build();

        } catch (Exception e) {

            JSONObject out = new JSONObject();
            out.put("error", step);
            if (step == 1)
                out.put("message", "height-sequence error, use integer-integer value");
            else if (step == 2)
                out.put("message", "Transaction not found");
            else
                out.put("message", e.getMessage());

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(out.toJSONString())
                    .build();
        }
    }

    @GET
    @Path("raw/{number}")
    public Response raw(@PathParam("number") String numberStr) {

        Transaction tx = DCSet.getInstance().getTransactionFinalMap().getRecord(numberStr);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(Base64.getEncoder().encodeToString(tx.toBytes(Transaction.FOR_NETWORK, true)))
                .build();
    }

    @GET
    @Path("signs/{number}")
    public Response getSigns(@PathParam("number") String numberStr) {

        Map out = new JSONObject();
        int step = 1;

        Long dbRef = Transaction.parseDBRef(numberStr);
        if (dbRef == null) {
            out.put("error", step);
            out.put("message", "height-sequence error, use integer-integer value");
        } else {
            Fun.Tuple2<BigDecimal, List<Long>> signsItem = DCSet.getInstance().getVouchRecordMap().get(dbRef);
            JSONArray values = new JSONArray();
            if (signsItem != null) {
                out.put("sum", signsItem.a.toPlainString());
                for (Long dbRefSignatory : signsItem.b) {
                    values.add(Transaction.viewDBRef(dbRefSignatory));
                }
                out.put("signs", values);
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }

    @GET
    @Path("vouches/{number}")
    public Response getVouches(@PathParam("number") String numberStr) {

        Map out = new JSONObject();
        int step = 1;

        Long dbRef = Transaction.parseDBRef(numberStr);
        if (dbRef == null) {
            out.put("error", step);
            out.put("message", "height-sequence error, use integer-integer value");
        } else {
            TransactionFinalMapImpl txMap = DCSet.getInstance().getTransactionFinalMap();
            Fun.Tuple2<BigDecimal, List<Long>> vouchesItem = DCSet.getInstance().getVouchRecordMap().get(dbRef);
            JSONArray values = new JSONArray();
            if (vouchesItem != null) {
                out.put("sum", vouchesItem.a.toPlainString());
                for (Long dbRefVoucher : vouchesItem.b) {
                    Transaction transaction = txMap.get(dbRefVoucher);
                    if (transaction == null)
                        continue;

                    JSONObject json = new JSONObject();
                    json.put("tx", transaction.viewHeightSeq());
                    json.put("creator", transaction.getCreator().toJson());
                    values.add(json);
                }
                out.put("vouches", values);
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }

    /// TODO: затратно блоки грузит - надо по Итераторв для адреса и Типа!
    /**
     * по блокам проходится и берет записи в них пока не просмотрит 2000 блоков и не насобирвет 100 записей. Если при этом не достигнут конец цепочи,
     * то выдаст в ответе параметр next со значением блока с которого нужно начать новый поиск.
     * Ограничение поиска сделано чтобы не грузить сервер запросами
     */
    @GET
    @Path("incomingfromblock/{address}/{from}")
    public Response incomingFromBlock(@PathParam("address") String address, @PathParam("from") Long from,
                                      @QueryParam("type") int type) {

        int height = from.intValue();
        Block block;
        try {
            block = Controller.getInstance().getBlockByHeight(height);
            if (block == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
            }
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }

        JSONObject out = new JSONObject();
        JSONArray array = new JSONArray();
        DCSet dcSet = DCSet.getInstance();

        int counter = 0;
        int counterBlock = 0;

        do {
            for (Transaction transaction : block.getTransactions()) {
                if (type != 0 && type != transaction.getType())
                    continue;

                transaction.setDC(dcSet, true);
                HashSet<Account> recipients = transaction.getRecipientAccounts();
                for (Account recipient : recipients) {
                    if (recipient.equals(address)) {
                        array.add(transaction.toJson());
                        counter++;
                        break;
                    }
                }
            }

            // one BLOCK checked
            if (counter > 100 || counterBlock++ > 200)
                break;

            block = Controller.getInstance().getBlockByHeight(++height);

        } while (block != null);

        out.put("txs", array);

        // IF not ENDs of CHAIN
        if (block != null) {
            out.put("next", height + 1);
        } else {
            out.put("height", height - 1);
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toString()).build();

    }

    @Deprecated
    @SuppressWarnings("unchecked")
    @GET
    @Path("byaddress/{address}")
    public Response getByAddress(@PathParam("address") String address, @QueryParam("asset") Long asset,
                                 @QueryParam("txType") Integer txType, @QueryParam("unconfirmed") boolean unconfirmed) {

        Account account;
        if (address == null) {
            account = null;
        } else {
            Fun.Tuple2<Account, String> resultAcc = Account.tryMakeAccount(address);
            if (resultAcc.a == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
            } else {
                account = resultAcc.a;
            }
        }

        List<Transaction> result;

        result = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressLimit(account.getShortAddressBytes(), txType,
                null, null, 0, 1000, true, false);
        if (unconfirmed) {
            if (txType == null || txType == 0)
                result.addAll(DCSet.getInstance().getTransactionTab().getTransactionsByAddressFast100(address));
            else
                for (Transaction transaction : DCSet.getInstance().getTransactionTab().getTransactionsByAddressFast100(address)) {
                    if (transaction.getType() == txType)
                        result.add(transaction);
                }
        }

        JSONArray array = new JSONArray();
        for (Transaction transaction : result) {

            if (asset != null) {
                if (asset.equals(transaction.getAbsKey()))
                    array.add(transaction.toJson());

            } else
                array.add(transaction.toJson());


        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toString()).build();
    }

    @Deprecated
    // "api/tx/getlastbyaddress?address={address}&timestamp={Timestamp}&limit={Limit}"
    @GET
    @Path("lastbyaddress/{address}")
    public Response getLastByAddress(@PathParam("address") String address, @QueryParam("timestamp") Long timestamp,
                                     @QueryParam("limit") Integer limit, @QueryParam("unconfirmed") boolean unconfirmed) {
        JSONObject out = new JSONObject();
        if (timestamp == null)
            timestamp = new Date().getTime();
        if (limit == null)
            limit = 20;
        List<Transaction> transs = new ArrayList<Transaction>();

        List<Transaction> trans = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressLimit(Account.makeShortBytes(address),
                null, null, null, 0, 1000, true, true);
        if (unconfirmed)
            trans.addAll(DCSet.getInstance().getTransactionTab().getTransactionsByAddressFast100(address));

        Collections.sort(trans, new TransactionTimestampComparator().reversed());
        for (Transaction tr : trans) {
            Long t = tr.getTimestamp();
            if (tr.getTimestamp() < timestamp)
                transs.add(tr);
        }
        Collections.sort(transs, new TransactionTimestampComparator().reversed());
        if (limit > transs.size())
            limit = transs.size();
        List<Transaction> transss = transs.subList(0, limit);
        int i = 0;
        for (Transaction tr : transss) {
            out.put(i, tr.toJson());
            i++;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(out.toString()).build();
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    @GET
    @Path("byaddressfrom/{address}")
    public Response getByAddressLimit(@PathParam("address") String address, @QueryParam("asset") Long asset,
                                      @QueryParam("start") long start, @QueryParam("end") long end, @QueryParam("type") String type1,
                                      @QueryParam("sort") String sort) {
        List<Transaction> result;

        if (address == null || address.equals("")) {
            JSONObject ff = new JSONObject();
            ff.put("Error", "Invalid Address");
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(ff.toString()).build();
        }
        // SearchTransactionsTableModel a = new SearchTransactionsTableModel();
        // a.findByAddress(address);
        // result =a.getTransactions();
        Integer type;
        try {
            type = Integer.valueOf(type1);
            result = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressAndType(Account.makeShortBytes(address), type, 1000, 0);

        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            result = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressLimit(Account.makeShortBytes(address), null,
                    null, null, 0, 1000, true, false);
            // e.printStackTrace();
        }

        if (result == null) {
            JSONObject ff = new JSONObject();
            ff.put("message", "null");
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(ff.toString()).build();
        }

        // 7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC

        // read transactions from treeMap
        TreeMap<BigDecimal, Transaction> rec = new TreeMap<BigDecimal, Transaction>();
        for (Transaction transaction : result) {
            if (asset != null) {
                if (asset.equals(transaction.getAbsKey())) {
                    rec.put(Library.getBlockSegToBigInteger(transaction), transaction);
                }

            }
        }
        // read tree map from 1...n
        TreeMap<Long, JSONObject> k_Map = new TreeMap<Long, JSONObject>();
        // if descending = 1 sort descending
        NavigableMap<BigDecimal, Transaction> rec1;
        if (sort == null || !sort.equals("des")) {
            rec1 = rec;
        } else {
            rec1 = rec.descendingMap();
        }
        long i = 0;
        for (Entry<BigDecimal, Transaction> transaction : rec1.entrySet()) {
            k_Map.put(i++, transaction.getValue().toJson());
        }

        // json.put("transactions", array);
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(new JSONObject(k_Map.subMap(start, end)).toString()).build();

    }

    /**
     * Get all incoming unconfirmed transaction by address, type transaction, timestamp limited by count
     *
     * @param address    address
     * @param count      limit
     * @param type       type incoming transaction
     * @param descending order
     * @param timestamp  current timestamp
     * @return JSON list of transaction
     *
     * <h2>Example request</h2>
     * http://127.0.0.1:9067/api/tx/unconfirmed?address=7R5m1NKAL3c2p3B7jMQXMsdqNaqCktS4h9?from=23&count=13&descending=true&timestamp=1535966134229&type=36
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("/unconfirmed")

    public Response getNetworkTransactions(@QueryParam("address") String address,
                                           @QueryParam("count") int count,
                                           @QueryParam("type") int type, @QueryParam("descending") boolean descending,
                                           @QueryParam("timestamp") long timestamp) {
        JSONArray array = new JSONArray();
        DCSet dcSet = DCSet.getInstance();

        Account account;
        if (address == null) {
            account = null;
        } else {
            Fun.Tuple2<Account, String> result = Account.tryMakeAccount(address);
            if (result.a == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
            } else {
                account = result.a;
            }
        }
        List<Transaction> transactions = dcSet.getTransactionTab().getTransactions(account, type,
                timestamp, count, descending);

        for (Transaction transaction : transactions) {
            array.add(transaction.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toString()).build();
    }

    /**
     * Get all incoming unconfirmed transaction by timestamp and type transaction
     *
     * @param address    address
     * @param count      limit
     * @param type       type incoming transaction
     * @param descending order
     * @param timestamp  current timestamp
     * @return JSON list of transaction
     *
     * <h2>Example request</h2>
     * http://127.0.0.1:9067/api/tx/unconfirmedincomes/
     * 7R5m1NKAL3c2p3B7jMQXMsdqNaqCktS4h9?from=23&count=13&descending=true&timestamp=1535966134229&type=36
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("/unconfirmedincomes/{address}")

    public Response getNetworkIncomesTransactions(@PathParam("address") String address,
                                                  @QueryParam("count") int count,
                                                  @QueryParam("type") int type, @QueryParam("descending") boolean descending,
                                                  @QueryParam("timestamp") long timestamp) {
        JSONArray array = new JSONArray();
        DCSet dcSet = DCSet.getInstance();

        List<Transaction> transactions = dcSet.getTransactionTab().getIncomedTransactions(address, type,
                timestamp, count, descending);

        for (Transaction transaction : transactions) {
            array.add(transaction.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toString()).build();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("byblock/{block}")
    public Response getByBlock(@PathParam("block") int blockNo) {
        JSONObject ff = new JSONObject();

        Block block = DCSet.getInstance().getBlockMap().getAndProcess(blockNo);
        if (block == null) {
            ff.put("error", "block not found");
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(ff.toString()).build();
        }

        List<Transaction> result = block.getTransactions();
        if (result == null) {

            ff.put("error", "null");
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(ff.toString()).build();
        }

        JSONArray array = new JSONArray();
        for (Transaction trans : result) {
            array.add(trans.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toString()).build();

    }

    @GET
    @Path("list")
    public Response getList(@Context UriInfo info,
                            @QueryParam("from") String fromSeqNoStr,
                            @QueryParam("offset") int offset, @QueryParam("limit") int limit
    ) {

        return getList(info, null, null, null, fromSeqNoStr, offset, limit);
    }

    @GET
    @Path("listbyaddress/{address}")
    public Response getList(@Context UriInfo info,
                            @PathParam("address") String address,
                            @QueryParam("from") String fromSeqNoStr,
                            @QueryParam("offset") int offset, @QueryParam("limit") int limit
    ) {
        return getList(info, address, null, null, fromSeqNoStr, offset, limit);
    }

    @GET
    @Path("listbyaddressandtype/{address}/{type}")
    public Response getList(@Context UriInfo info,
                            @PathParam("address") String address,
                            @PathParam("type") Integer type,
                            @QueryParam("creator") Boolean isCreator,
                            @QueryParam("from") String fromSeqNoStr,
                            @QueryParam("offset") int offset, @QueryParam("limit") int limit
    ) {

        Long fromID = Transaction.parseDBRef(fromSeqNoStr);

        boolean desc = API.checkBoolean(info, "desc");
        boolean noForge = API.checkBoolean(info, "noforge");

        int limitMax = ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request)) ? 10000 : 100;
        if (limit > limitMax || limit <= 0)
            limit = limitMax;
        if (offset > limitMax)
            offset = limitMax;

        if (address == null && type != null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        if (type == null && isCreator != null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_TRANSACTION_TYPE);
        }

        Account account;
        if (address == null) {
            account = null;
        } else {
            Fun.Tuple2<Account, String> result = Account.tryMakeAccount(address);
            if (result.a == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
            } else {
                account = result.a;
            }
        }

        JSONArray array = new JSONArray();
        TransactionFinalMapImpl map = DCSet.getInstance().getTransactionFinalMap();

        for (Transaction tx : map.getTransactionsByAddressLimit(account == null ? null : account.getShortAddressBytes(),
                type, isCreator, fromID, offset, limit, noForge, desc)) {
            array.add(tx.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("find")
    public Response getTransactionsFind(@Context UriInfo info,
                                        @QueryParam("address") String address, @QueryParam("sender") String sender, @QueryParam("creator") String creator,
                                        @QueryParam("recipient") String recipient,
                                        @QueryParam("from") String fromSeqNo,
                                        @QueryParam("startblock") int minHeight,
                                        @QueryParam("endblock") int maxHeight, @QueryParam("type") int type,
                                        @QueryParam("desc") boolean desc,
                                        @QueryParam("offset") int offset,
                                        @QueryParam("limit") int limit,
                                        @QueryParam("unconfirmed") boolean unconfirmed,
                                        @DefaultValue("false") @QueryParam("count") boolean count
    ) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 50 || limit == 0)
                limit = 50;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TransactionsResource.getTransactionsFind(info, address, sender, creator, recipient, fromSeqNo,
                        minHeight, maxHeight, type, offset, limit)).build();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("search")
    public Response getTransactionsSearch(
            @QueryParam("q") String query,
            @QueryParam("from") String fromSeqNoStr,
            @QueryParam("useforge") boolean useForge,
            @QueryParam("fullpage") boolean fullPage,
            @QueryParam("offset") int offset, @QueryParam("limit") int limit
    ) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 50 || limit == 0)
                limit = 50;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TransactionsResource.getTransactionsSearch(query, fromSeqNoStr, useForge, fullPage, offset, limit)).build();
    }

    @GET
    @Path("/rawbyblock/{height}")
    @SuppressWarnings("unchecked")
    public Response getRawByBlock(@PathParam("height") int height,
                                  @DefaultValue("3") @QueryParam("forDeal") int forDeal) {
        Block block;

        if (forDeal > 6 || forDeal < 0) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_TRANSACTION_TYPE);
        }

        JSONArray txs = new JSONArray();
        block = Controller.getInstance().getBlockByHeight(height);
        if (block == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }

        for (Transaction transaction : block.getTransactions()) {
            String rawTransaction = Base58.encode(transaction.toBytes(forDeal, true));
            txs.add(rawTransaction);
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(txs.toString())
                .build();
    }

    @GET
    @Path("/raw64byblock/{height}")
    @SuppressWarnings("unchecked")
    public Response getRaw64ByBlock(@PathParam("height") int height,
                                    @DefaultValue("3") @QueryParam("forDeal") int forDeal) {
        Block block;

        if (forDeal > 6 || forDeal < 0) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_TRANSACTION_TYPE);
        }

        JSONArray txs = new JSONArray();
        block = Controller.getInstance().getBlockByHeight(height);
        if (block == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }

        for (Transaction transaction : block.getTransactions()) {
            String rawTransaction = Base64.getEncoder().encodeToString(transaction.toBytes(forDeal, true));
            txs.add(rawTransaction);
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(txs.toString())
                .build();
    }

    @GET
    @Path("links/{number}")
    public Response getLinks(@PathParam("number") String numberStr) {

        Map out = new JSONObject();
        int step = 1;

        Long dbRef = Transaction.parseDBRef(numberStr);
        if (dbRef == null) {
            out.put("error", step);
            out.put("message", "height-sequence error, use integer-integer value");
        } else {

            DCSet dcSet = DCSet.getInstance();

            try (IteratorCloseable<Fun.Tuple3<Long, Byte, Long>> iterator = dcSet.getExLinksMap()
                    .getTXLinksIterator(dbRef, ExData.LINK_APPENDIX_TYPE, false)) {
                JSONArray links = new JSONArray();
                while (iterator.hasNext()) {
                    links.add(Transaction.viewDBRef(iterator.next().c));
                }
                out.put("appendix", links);

            } catch (IOException e) {
                out.put("error", ++step);
                out.put("message", "LINK_APPENDIX_TYPE error: " + e.getMessage());
            }

            step++;
            try (IteratorCloseable<Fun.Tuple3<Long, Byte, Long>> iterator = dcSet.getExLinksMap()
                    .getTXLinksIterator(dbRef, ExData.LINK_AUTHOR_TYPE, false)) {
                JSONArray links = new JSONArray();
                while (iterator.hasNext()) {
                    links.add(Transaction.viewDBRef(iterator.next().c));
                }
                out.put("author", links);

            } catch (IOException e) {
                out.put("error", ++step);
                out.put("message", "LINK_AUTHOR_TYPE error: " + e.getMessage());
            }

            step++;
            try (IteratorCloseable<Fun.Tuple3<Long, Byte, Long>> iterator = dcSet.getExLinksMap()
                    .getTXLinksIterator(dbRef, ExData.LINK_SOURCE_TYPE, false)) {
                JSONArray links = new JSONArray();
                while (iterator.hasNext()) {
                    links.add(Transaction.viewDBRef(iterator.next().c));
                }
                out.put("source", links);

            } catch (IOException e) {
                out.put("error", ++step);
                out.put("message", "LINK_SOURCE_TYPE error: " + e.getMessage());
            }

            step++;
            try (IteratorCloseable<Fun.Tuple3<Long, Byte, Long>> iterator = dcSet.getExLinksMap()
                    .getTXLinksIterator(dbRef, ExData.LINK_REPLY_COMMENT_TYPE, false)) {
                JSONArray links = new JSONArray();
                while (iterator.hasNext()) {
                    links.add(Transaction.viewDBRef(iterator.next().c));
                }
                out.put("comment", links);

            } catch (IOException e) {
                out.put("error", ++step);
                out.put("message", "LINK_COMMENT_TYPE error: " + e.getMessage());
            }

        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toString())
                .build();
    }

    static String getTypesCACHE = null;

    @GET
    @Path("types")
    public String getTypes() {
        if (getTypesCACHE != null)
            return getTypesCACHE;

        JSONArray jsonArray = new JSONArray();
        for (int type : Transaction.getTransactionTypes(true)) {
            if (type <= 0)
                continue;

            JSONObject json = new JSONObject();
            json.put("id", type);
            json.put("name", Transaction.viewTypeName(type));

            jsonArray.add(json);
        }

        return (getTypesCACHE = jsonArray.toJSONString());
    }

    public Response parse(String raw, boolean base64, boolean check, String lang) {

        JSONObject out = new JSONObject();

        JSONObject langObj = null;
        if (lang != null) {
            out.put("lang", lang);
            langObj = Lang.getInstance().getLangJson(lang);
        }

        Fun.Tuple3<Transaction, Integer, String> result = Controller.getInstance().parseAndCheck(raw, base64, check);
        if (result.a == null) {
            Transaction.updateMapByErrorSimple2(out, result.b,
                    langObj == null || result.c == null ? result.c : Lang.T(result.c, langObj), lang);

        } else {
            try {
                out = result.a.toJson();
            } catch (Exception e) {
                result.a.updateMapByError2(out, Transaction.JSON_ERROR, e.toString());
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("parse/{raw}")
    public Response parse(@Context UriInfo info,
                          @PathParam("raw") String raw,
                          @QueryParam("lang") String lang) {

        boolean base58 = API.checkBoolean(info, "base58");
        boolean check = API.checkBoolean(info, "check");

        return parse(raw, !base58, check, lang);
    }

    @POST
    @Path("parse")
    public Response parsePost(@Context UriInfo info,
                              @QueryParam("lang") String lang,
                              String raw) {

        boolean check = API.checkBoolean(info, "check");
        return parse(raw, true, check, lang);
    }

    /////////////////// BROADCAST

    public static JSONObject broadcastFromRawByte(byte[] transactionBytes, String lang) {
        Fun.Tuple3<Transaction, Integer, String> result = Controller.getInstance().lightCreateTransactionFromRaw(transactionBytes, false);
        if (result.a == null) {
            JSONObject out = new JSONObject();
            Transaction.updateMapByErrorSimple2(out, result.b, result.c, lang);
            return out;

        }

        JSONObject out = result.a.toJson();
        if (result.b == Transaction.VALIDATE_OK) {
            out.put("status", "ok");
            return out;
        }

        result.a.updateMapByError2(out, result.b, lang);

        return out;

    }

    public static JSONObject broadcastFromRawString(String rawDataStr, boolean base64, String lang) {
        JSONObject out = new JSONObject();
        byte[] transactionBytes;
        try {
            if (base64) {
                transactionBytes = Base64.getDecoder().decode(rawDataStr);
            } else {
                transactionBytes = Base58.decode(rawDataStr);
            }
        } catch (Exception e) {
            Transaction.updateMapByErrorSimple2(out, Transaction.JSON_ERROR, e.getMessage(), lang);
            return out;
        }

        if (transactionBytes == null) {
            Transaction.updateMapByErrorSimple2(out, Transaction.JSON_ERROR, "", lang);
            return out;
        }

        return broadcastFromRawByte(transactionBytes, lang);
    }

    @GET
    @Path("broadcast/{raw}")
    public Response broadcast(@Context UriInfo info,
                              @PathParam("raw") String raw,
                              @QueryParam("lang") String lang) {

        boolean base58 = API.checkBoolean(info, "base58");
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastFromRawString(raw, !base58, lang).toJSONString())
                .build();
    }

    @POST
    @Path("broadcast")
    public Response broadcastPost(@Context UriInfo info,
                                  @QueryParam("lang") String lang,
                                  String raw) {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastFromRawString(raw, true, lang).toJSONString())
                .build();

    }

    @POST
    @Path("broadcastjson")
    public Response broadcastFromRawJsonPost(@Context UriInfo info,
                                             @Context HttpServletRequest request,
                                             MultivaluedMap<String, String> form) {

        String raw = form.getFirst("raw");
        String lang = form.getFirst("lang");

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastFromRawString(raw, true, lang).toJSONString())
                .build();

    }

}