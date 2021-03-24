package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.api.TransactionsResource;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.DCSet;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;
import org.erachain.utils.StrJSonFine;
import org.erachain.utils.TransactionTimestampComparator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

@Deprecated
@Path("apirecords")
@Produces(MediaType.APPLICATION_JSON)
public class APITransactionsResource {

    @Context
    HttpServletRequest request;

    @GET
    public Response Default() {

        Map<String, String> help = new LinkedHashMap<String, String>();

        help.put("apirecords/get/{signature}",
                Lang.T("Get Record by sigmature."));
        help.put("apirecords/getbynumber/{height-sequence}",
                "GET Record by Height and Sequence");
        help.put("apirecords/getsignature/{height-sequence}",
                "GET Record Signature by Height and Sequence");
        help.put("apirecords/getvouches/{height-sequence}",
                "GET Vouches of Record by Height and Sequence");
        help.put("apirecords/incomingfromblock/{address}/{blockStart}?type={type}",
                Lang.T("Get Incoming Records for Address from {blockStart}. Filter by type. Limit checked blocks = 2000 or 100 found records. If blocks not end at height - NEXT parameter was set."));
        help.put("apirecords/getbyaddress?address={address}&asset={asset}&recordType={recordType}&unconfirmed=true",
                Lang.T("Get all Records (and Unconfirmed) for Address & Asset Key by record type. recordType is option parameter"));
        help.put("apirecords/getlastbyaddress?address={address}&timestamp={Timestamp}&limit={Limit}&unconfirmed=true",
                "Get last Records (and Unconfirmed) from Unix Timestamp milisec(1512777600000)");
        help.put("apirecords/getbyaddressfromtransactionlimit?address={address}&asset={asset}&start={start record}&end={end record}&type={type Transaction}&sort={des/asc}",
                Lang.T("Get all Records for Address & Asset Key from Start to End"));

        help.put("apirecords/unconfirmed?address={address}&type={type}&from={from}&count={count}&descending=true",
                Lang.T("Get all incoming unconfirmed transaction by address, type transaction, timestamp limited by count"));

        help.put("apirecords/unconfirmedincomes/{address}?type={type}&from={from}&count={count}&descending=true",
                Lang.T("Get all unconfirmed Records for Address from Start at Count filtered by Type"));

        help.put("apirecords/getbyblock?block={block}", Lang.T("Get all Records from Block"));

        help.put("apirecords/find?address={address}&creator={creator}&recipient={recipient}&from=[seqNo]&startblock{s_minHeight}&endblock={s_maxHeight}&type={type Transaction}&service={service}&desc={false}&offset={offset}&limit={limit}&unconfirmed=false&count=false",
                Lang.T("Find Records. Set [seqNo] as 1234-1"));

        help.put("apirecords/search?q={query}&from=[seqNo]&useforge={false}&offset={offset}&limit={limit}&fullpage={false}",
                Lang.T("Search Records by Query. Query=SeqNo|Signature|FilterWords. Result[0-1] - START & END Seq-No for use in paging (see as make it in blockexplorer. Signature as Base58. Set Set FilterWords as preffix words separated by space. Set [seqNo] as 1234-1. For use forge set &useforge=true. For fill full page - use fullpage=true"));

        help.put("apirecords/rawTransactionsByBlock/{height}?param", "Get raw transaction(encoding Base58). By default param is 3(for network)");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(help)).build();

    }


    @GET
    @Path("get/{signature}")
    public Response getBySign(@PathParam("signature") String signature) {

        JSONObject out = new JSONObject();

        int step = 1;

        try {
            byte[] key = Base58.decode(signature);

            ++step;
            Transaction record = Controller.getInstance().getTransaction(key, DCSet.getInstance());
            out = record.toJson();

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "signature error, use Base58 value");
            else if (step == 2)
                out.put("message", "record not found");
            else
                out.put("message", e.getMessage());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("getbynumber/{number}")
    public Response getByNumber(@PathParam("number") String numberStr) {

        JSONObject out = new JSONObject();
        int step = 1;

        try {

            String[] strA = numberStr.split("\\-");
            int height = Integer.parseInt(strA[0]);
            int seq = Integer.parseInt(strA[1]);

            ++step;
            Transaction record = DCSet.getInstance().getTransactionFinalMap().get(height, seq);
            out = record.toJson();

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "height-sequence error, use integer-integer value");
            else if (step == 2)
                out.put("message", "record not found");
            else
                out.put("message", e.getMessage());
        }


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }


    // getsignature
    @GET
    @Path("getsignature/{number}")
    public Response getSignByNumber(@PathParam("number") String numberStr) {

        int step = 1;

        try {

            String[] strA = numberStr.split("\\-");
            int height = Integer.parseInt(strA[0]);
            int seq = Integer.parseInt(strA[1]);

            ++step;
            Transaction record = DCSet.getInstance().getTransactionFinalMap().get(height, seq);

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(record.viewSignature())
                    .build();

        } catch (Exception e) {

            JSONObject out = new JSONObject();
            out.put("error", step);
            if (step == 1)
                out.put("message", "height-sequence error, use integer-integer value");
            else if (step == 2)
                out.put("message", "record not found");
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
    @Path("getvouches/{number}")
    public Response getVouches(@PathParam("number") String numberStr) {

        JSONObject out = new JSONObject();
        int step = 1;

        Long dbRef = Transaction.parseDBRef(numberStr);
        if (dbRef == null) {
            out.put("error", step);
            out.put("message", "height-sequence error, use integer-integer value");
        } else {
            Fun.Tuple2<BigDecimal, List<Long>> vouchesItem = DCSet.getInstance().getVouchRecordMap().get(dbRef);
            JSONArray values = new JSONArray();
            if (vouchesItem != null) {
                out.put("sum", vouchesItem.a.toPlainString());
                for (Long dbRefVoucher : vouchesItem.b) {
                    values.add(Transaction.viewDBRef(dbRefVoucher));
                }
                out.put("vouches", values);
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

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
                .entity(out.toJSONString()).build();

    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("getbyaddress")
    public Response getByAddress(@QueryParam("address") String address, @QueryParam("asset") Long asset,
                                 @QueryParam("recordType") String recordType, @QueryParam("unconfirmed") boolean unconfirmed) {

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

        result = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressLimit(account.getShortAddressBytes(), 1000, true, false);
        if (unconfirmed)
            result.addAll(DCSet.getInstance().getTransactionTab().getTransactionsByAddressFast100(address));

        JSONArray array = new JSONArray();
        for (Transaction transaction : result) {
            if (recordType != null) {
                if (transaction.viewTypeName().toUpperCase().equals(recordType.toUpperCase())) {
                    if (asset != null) {
                        if (asset.equals(transaction.getAbsKey()))
                            array.add(transaction.toJson());
                    } else
                        array.add(transaction.toJson());
                }
            } else {

                if (asset != null) {
                    if (asset.equals(transaction.getAbsKey()))
                        array.add(transaction.toJson());

                } else
                    array.add(transaction.toJson());

            }

        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();
    }

    // "apirecords/getlastbyaddress?address={address}&timestamp={Timestamp}&limit={Limit}"
    @GET
    @Path("getlastbyaddress")
    public Response getLastByAddress(@QueryParam("address") String address, @QueryParam("timestamp") Long timestamp,
                                     @QueryParam("limit") Integer limit, @QueryParam("unconfirmed") boolean unconfirmed) {
        JSONObject out = new JSONObject();
        if (timestamp == null)
            timestamp = new Date().getTime();
        if (limit == null)
            limit = 20;
        List<Transaction> transs = new ArrayList<Transaction>();

        List<Transaction> trans = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressLimit(Account.makeShortBytes(address), 1000, true, false);
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
        // out =
        // Controller.getInstance().getBlockChain().getGenesisBlock().toJson();

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString()).build();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("getbyaddressfromtransactionlimit")
    public Response getByAddressLimit(@QueryParam("address") String address, @QueryParam("asset") Long asset,
                                      @QueryParam("start") long start, @QueryParam("end") long end, @QueryParam("type") String type1,
                                      @QueryParam("sort") String sort) {
        List<Transaction> result;

        if (address == null || address.equals("")) {
            JSONObject ff = new JSONObject();
            ff.put("Error", "Invalid Address");
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(ff.toJSONString()).build();
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
            result = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressLimit(Account.makeShortBytes(address), 1000, true, false);
            // e.printStackTrace();
        }

        if (result == null) {
            JSONObject ff = new JSONObject();
            ff.put("message", "null");
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(ff.toJSONString()).build();
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
                .entity(new JSONObject(k_Map.subMap(start, end)).toJSONString()).build();

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
     * http://127.0.0.1:9067/apirecords/unconfirmed?address=7R5m1NKAL3c2p3B7jMQXMsdqNaqCktS4h9?from=23&count=13&descending=true&timestamp=1535966134229&type=36
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
        List<Transaction> transaction = dcSet.getTransactionTab().getTransactions(account, type,
                timestamp, count, descending);

        for (Transaction record : transaction) {
            array.add(record.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();
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
     * http://127.0.0.1:9067/apirecords/unconfirmedincomes/
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

        List<Transaction> transaction = dcSet.getTransactionTab().getIncomedTransactions(address, type,
                timestamp, count, descending);

        for (Transaction record : transaction) {
            array.add(record.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("getbyblock")
    public Response getByBlock(@QueryParam("block") int blockNo) {
        JSONObject ff = new JSONObject();

        Block block = DCSet.getInstance().getBlockMap().getAndProcess(blockNo);
        if (block == null) {
            ff.put("error", "block not found");
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(ff.toJSONString()).build();
        }

        List<Transaction> result = block.getTransactions();
        if (result == null) {

            ff.put("error", "null");
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(ff.toJSONString()).build();
        }

        JSONArray array = new JSONArray();
        for (Transaction trans : result) {
            array.add(trans.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();

    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("find")
    public Response getTransactionsFind(@QueryParam("address") String address, @QueryParam("sender") String sender, @QueryParam("creator") String creator,
                                        @QueryParam("recipient") String recipient,
                                        @QueryParam("from") String fromSeqNo,
                                        @QueryParam("startblock") int minHeight,
                                        @QueryParam("endblock") int maxHeight, @QueryParam("type") int type,
                                        //@QueryParam("timestamp") long timestamp,
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
                .entity(TransactionsResource.getTransactionsFind(address, sender, creator, recipient, fromSeqNo, minHeight, maxHeight, type,
                        desc, offset, limit, unconfirmed, count)).build();
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
    @Path("/rawTransactionsByBlock/{height}")
    @SuppressWarnings("unchecked")
    public Response getRawTransactionByBlock(@PathParam("height") int height,
                                             @DefaultValue("3") @QueryParam("param") int paramTransaction) {
        Block block;

        if (paramTransaction > 6 || paramTransaction < 0) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_TRANSACTION_TYPE);
        }

        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObjectTransactions = new JSONObject();
        try {
            block = Controller.getInstance().getBlockByHeight(height);
            int i = 1;
            for (Transaction transaction : block.getTransactions()) {
                String rawTransaction = Base58.encode(transaction.toBytes(paramTransaction, true));
                jsonObjectTransactions.put(i, rawTransaction);
                i++;
            }
            jsonObject.put("transactions", jsonObjectTransactions);


            if (block == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
            }
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(jsonObject.toJSONString())
                .build();
    }
}