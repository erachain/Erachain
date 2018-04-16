package webserver;

import core.transaction.Transaction;
import datachain.DCSet;
import gui.library.library;
import gui.models.TransactionsTableModel;
import lang.Lang;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.StrJSonFine;
import utils.TransactionTimestampComparator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

@Path("apirecords")
@Produces(MediaType.APPLICATION_JSON)
public class API_TransactionsResource {

//	static Logger LOGGER = Logger.getLogger(API_TransactionsResource.class.getName());

    @Context
    HttpServletRequest request;

//	private static int count_step;

//	private static boolean run_test = false;

//	protected String tt;

//	private static Thread thread;
//	private static int sleep;
//	private static BigDecimal amm;
//	private static byte[] mes;

    @GET
    public Response Default() {

        Map<String, String> help = new LinkedHashMap<String, String>();

        help.put("apirecords/getbyaddress?address={address}&asset={asset}&recordType={recordType}",
                Lang.getInstance().translate("Get all Records for Address & Asset Key by record type. recordType is option parameter"));
        help.put(
                "apirecords/getbyaddressfromtransactionlimit?address={address}&asset={asset}&start={start record}&end={end record}&type={type Transaction}&sort={des/asc}",
                Lang.getInstance().translate("Get all Records for Address & Asset Key from Start to End"));
        help.put("apirecords/getbyblock?block={block}", Lang.getInstance().translate("Get all Records from Block"));

        help.put("apirecords/getlastbyaddress?address={address}&timestamp={Timestamp}&limit={Limit}",
                "Get last Records from Unix Timestamp milisec(1512777600000)");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(help)).build();

    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("getbyaddress")
    public Response getByAddress(@QueryParam("address") String address, @QueryParam("asset") String asset, @QueryParam("recordType") String recordType) {
        List<Transaction> result;
        if (address == null || address.equals("")) {
            JSONObject ff = new JSONObject();
            ff.put("Error", "Invalid Address");
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(ff.toJSONString()).build();
        }

        result = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddress(address);

        if (result == null) {
            JSONObject ff = new JSONObject();
            ff.put("message", "null");
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(ff.toJSONString()).build();
        }
        JSONArray array = new JSONArray();
        for (Transaction transaction : result) {
            if (recordType != null) {
                if (transaction.viewTypeName().toUpperCase().equals(recordType.toUpperCase())) {
                    if (asset != null) {
                        if (transaction.getAbsKey() == new Long(asset))
                            array.add(transaction.toJson());
                    } else
                        array.add(transaction.toJson());
                }
            } else {

                if (asset != null) {
                    if (transaction.getAbsKey() == new Long(asset))
                        array.add(transaction.toJson());

                } else
                    array.add(transaction.toJson());

            }

        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("getbyaddressfromtransactionlimit")
    public Response getByAddressLimit(@QueryParam("address") String address, @QueryParam("asset") String asset,
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
        // TransactionsTableModel a = new TransactionsTableModel();
        // a.Find_Transactions_from_Address(address);
        // result =a.getTransactions();
        Integer type;
        try {
            type = Integer.valueOf(type1);
            result = DCSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress(address, type, 0);

        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            result = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddress(address);
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
                if (transaction.getAbsKey() == new Long(asset)) {
                    rec.put(library.getBlockSegToBigInteger(transaction), transaction);
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

    @SuppressWarnings("unchecked")
    @GET
    @Path("getbyblock")
    public Response getByBlock(@QueryParam("block") String block) {
        JSONObject ff = new JSONObject();
        List<Transaction> result;

        TransactionsTableModel a = new TransactionsTableModel();
        a.setBlockNumber(block);
        result = a.getTransactions();
        if (result == null || result.isEmpty()) {

            ff.put("message", "null");
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(ff.toJSONString()).build();
        }

        JSONArray array = new JSONArray();
        for (Transaction trans : result) {

            array.add(trans.toJson());
        }
        // json.put("transactions", array);
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();

    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("find")
    public Response getTransactionsFind(@QueryParam("address") String address, @QueryParam("sender") String sender,
                                        @QueryParam("recipient") String recipient, @QueryParam("startblock") String s_minHeight,
                                        @QueryParam("endblock") String s_maxHeight, @QueryParam("type") String s_type,
                                        @QueryParam("service") String s_service, @QueryParam("desc") String s_desc,
                                        @QueryParam("offset") String s_offset, @QueryParam("limit") String s_limit

    ) {

        int maxHeight;
        try {
            maxHeight = new Integer(s_maxHeight);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            maxHeight = 0;
        }
        int minHeight;
        try {
            minHeight = new Integer(s_minHeight);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            minHeight = 0;
        }
        int type;
        try {
            type = new Integer(s_type);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            type = 0;
        }
        int service;
        try {
            service = new Integer(s_service);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            service = 0;
        }
        int offset;
        try {
            offset = new Integer(s_offset);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            offset = 0;
        }
        int limit;
        try {
            limit = new Integer(s_limit);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            limit = 0;
        }

        List<Transaction> result = DCSet.getInstance().getTransactionFinalMap().findTransactions(address, sender,
                recipient, minHeight, maxHeight, type, service, false, offset, limit);

        JSONArray array = new JSONArray();
        for (Transaction trans : result) {

            array.add(trans.toJson());
        }
        // json.put("transactions", array);
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();
    }

    // "apirecords/getlastbyaddress?address={address}&timestamp={Timestamp}&limit={Limit}"
    @GET
    @Path("getlastbyaddress")
    public Response getLastByAddress(@QueryParam("address") String address, @QueryParam("timestamp") Long timestamp,
                                     @QueryParam("limit") Integer limit) {
        JSONObject out = new JSONObject();
        if (timestamp == null)
            timestamp = new Date().getTime();
        if (limit == null)
            limit = 20;
        List<Transaction> transs = new ArrayList<Transaction>();
        List<Transaction> trans = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddress(address);
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
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(out)).build();
    }

}