package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.api.CoreResource;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.core.crypto.Base32;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.datachain.*;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;




@SuppressWarnings({"unchecked", "rawtypes"})

@Path("api")
public class API {

    private static final Logger LOGGER = LoggerFactory            .getLogger(API.class);
    @Context
    private UriInfo uriInfo;
    private HttpServletRequest request;
    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {

        Map help = new LinkedHashMap();

        help.put("see /apiasset", "Help for assets API");
        help.put("see /apipoll", "Help for polls API");
        help.put("see /apiperson", "Help for persons API");
        help.put("see /apitemplate", "Help for templates API");
        help.put("see /apistatus", "Help for statuses API");

        help.put("see /apitelegrams", "Help for telegrams API");
        help.put("see /apiexchange", "Help for exchange API");
        help.put("see /apirecords", "Help for transactions API");
        help.put("see /apidocuments", "Help for documents API");

        help.put("*** BALANCE SYSTEM ***", "");
        help.put("bal 1", "Balance Components - {Total_Income, Remaining_Balance]");
        help.put("bal 2", "Balances Array - [Balance in Own, Balance in Debt, Balance on Hold, Balance of Consumer]");

        help.put("*** CHAIN ***", "");
        help.put("GET Height", "height");
        help.put("GET First Block", "firstblock");
        help.put("GET Last Block", "lastblock");
        help.put("GET Last Block Head", "lastblockhead");

        help.put("*** BLOCK ***", "");
        help.put("GET Block", "block/{signature}");
        help.put("GET Block by Height", "blockbyheight/{height}");
        help.put("GET Child Block Signature", "childblocksignature/{signature}");
        help.put("GET Child Block", "childblock/{signature}");

        help.put("*** BLOCKS ***", "");
        help.put("GET Blocks from Height by Limit (end:1 if END is reached)", "blocksfromheight/{height}/{limit}");
        help.put("GET Blocks Signatures from Height by Limit (end:1 if END id reached)", "/blockssignaturesfromheight/{height}/{limit}");

        help.put("*** RECORD ***", "");
        help.put("GET Record Parse from RAW", "recordparse/{raw}");
        help.put("POST Record Parse from RAW", "recordparse?raw=...");
        help.put("GET Record", "record/{signature}");
        help.put("GET Record by Height and Sequence", "recordbynumber/{height-sequence}");
        help.put("GET Record RAW", "recordraw/{signature}");
        help.put("GET Record RAW by Height and Sequence", "recordrawbynumber/{block-seqNo]");
        help.put("GET Record RAW by Height and Sequence 2", "recordrawbynumber/{block]/[seqNo]");

        help.put("*** ADDRESS ***", "");
        help.put("GET Address Validate", "addressvalidate/{address}");
        help.put("GET Address Last Reference", "addresslastreference/{address}");
        help.put("GET Address Unconfirmed Last Reference", "addressunconfirmedlastreference/{address}/{from}/{count}");
        help.put("GET Address Generating Balance", "addressgeneratingbalance/{address}");
        help.put("GET Address Asset Balance", "addressassetbalance/{address}/{assetid}");
        help.put("GET Address Assets", "addressassets/{address}");
        help.put("GET Address Public Key", "addresspublickey/{address}");

        help.put("*** ASSET ***", "");
        help.put("GET Asset Height", "assetheight");
        help.put("GET Asset", "asset/{key}");
        help.put("GET Asset Data", "assetdata/{key}");
        help.put("GET Asset Image", "assetimage/{key}");
        help.put("GET Asset Icon", "asseticon/{key}");

        help.put("*** ASSETS ***", "");
        help.put("GET Assets", "assets");
        help.put("GET Assets Full", "assetsfull");
        help.put("GET Assets by Name Filter", "assetsfilter/{filter_name_string}");

        help.put("*** EXCHANGE ***", "");
        help.put("GET Exchange Orders", "exchangeorders/{have}/{want}");

        help.put("*** PERSON ***", "");
        help.put("GET Person Height", "personheight");
        help.put("GET Person Key by PubKey of Owner", "personkeybyownerpublickey/{publickey}");
        help.put("GET Person", "person/{key}");
        help.put("GET Person Data", "persondata/{key}");
        help.put("GET Person Key by Address", "personkeybyaddress/{address}");
        help.put("GET Person by Address", "personbyaddress/{address}");
        help.put("GET Person Key by Public Key", "personkeybypublickey/{publickey}");
        help.put("GET Person by Public Key", "personbypublickey/{publickey}");
        help.put("GET Person by Public Key Base32", "personbypublickeybase32/{publickeybase32}");
        help.put("GET Accounts From Person", "getaccountsfromperson/{key}");
        help.put("Get Person Image", "personimage/{key}");

        help.put("*** PERSONS ***", "");
        help.put("GET Persons by Name Filter", "personsfilter/{filter_name_string}");

        help.put("*** TOOLS ***", "");
        help.put("POST Verify Signature for JSON {'message': ..., 'signature': Base58, 'publickey': Base58)", "verifysignature");
        help.put("GET info by node", " GET api/info");
        help.put("GET benchmark info by node", " GET api/bench");

        help.put("POST Broadcast", "/broadcast JSON {raw=raw(BASE58)}");
        help.put("GET Broadcast", "/broadcast/{raw(BASE58)}");

        help.put("POST Broadcasttelegram", "/broadcasttelegram JSON {'raw': raw(BASE58)}");
        help.put("GET Broadcasttelegram", "/broadcasttelegram/ {raw(BASE58)}");

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(help))
                .build();
    }


    @GET
    @Path("height")
    public static Response getHeight() {
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(String.valueOf(Controller.getInstance().getMyHeight()))
                .build();
    }

    @GET
    @Path("firstblock")
    public Response getFirstBlock() {

        JSONObject out = dcSet.getBlockMap().get(1).toJson();

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }

    @GET
    @Path("lastblock")
    public Response lastBlock() {

        Block lastBlock = dcSet.getBlockMap().last();
        Map out = lastBlock.toJson();

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }

    @GET
    @Path("lastblockhead")
    public Response lastBlockHead() {

        Block.BlockHead lastBlock = dcSet.getBlocksHeadsMap().last();
        Map out = lastBlock.toJson();

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }

    @GET
    @Path("/childblocksignature/{signature}")
    public Response getChildBlockSignature(@PathParam("signature") String signature) {
        //DECODE SIGNATURE
        byte[] signatureBytes;
        Map out = new LinkedHashMap();

        int step = 1;
        try {
            signatureBytes = Base58.decode(signature);

            ++step;
            Integer heightWT = dcSet.getBlockSignsMap().get(signatureBytes);
            if (heightWT != null && heightWT > 0) {
                byte[] childSign = dcSet.getBlocksHeadsMap().get(heightWT + 1).signature;
                out.put("child", Base58.encode(childSign));
            } else {
                out.put("message", "signature not found");
            }
        } catch (Exception e) {
            out.put("error", step);
            if (step == 1)
                out.put("message", "signature error, use Base58 value");
            else if (step == 2)
                out.put("message", "child not found");
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
    @Path("/childblock/{signature}")
    public Response getChildBlock(@PathParam("signature") String signature) {
        //DECODE SIGNATURE
        byte[] signatureBytes;
        Map out = new LinkedHashMap();

        int step = 1;
        try {
            signatureBytes = Base58.decode(signature);

            ++step;
            Integer heightWT = dcSet.getBlockSignsMap().get(signatureBytes);
            if (heightWT != null && heightWT > 0) {
                out = dcSet.getBlockMap().getAndProcess(heightWT + 1).toJson();
            } else {
                out.put("message", "signature not found");
            }
        } catch (Exception e) {
            out.put("error", step);
            if (step == 1)
                out.put("message", "signature error, use Base58 value");
            else if (step == 2)
                out.put("message", "child not found");
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
    @Path("block/{signature}")
    public Response block(@PathParam("signature") String signature) {

        Map out = new LinkedHashMap();

        int step = 1;

        try {
            byte[] key = Base58.decode(signature);

            ++step;
            Block block = dcSet.getBlockSignsMap().getBlock(key);
            out.put("block", block.toJson());

            ++step;
            byte[] childSign = dcSet.getBlocksHeadsMap().get(block.getHeight() + 1).signature;
            if (childSign != null)
                out.put("next", Base58.encode(childSign));

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "signature error, use Base58 value");
            else if (step == 2)
                out.put("message", "block not found");
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
    @Path("blockbyheight/{height}")
    public Response blockByHeight(@PathParam("height") String heightStr) {

        Map out = new LinkedHashMap();
        int step = 1;

        try {
            int height = Integer.parseInt(heightStr);

            ++step;
            Block block = cntrl.getBlockByHeight(dcSet, height);
            out.put("block", block.toJson());

            ++step;
            byte[] childSign = dcSet.getBlocksHeadsMap().get(block.getHeight() + 1).signature;
            if (childSign != null)
                out.put("next", Base58.encode(childSign));

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "height error, use integer value");
            else if (step == 2)
                out.put("message", "block not found");
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
    @Path("blockbyheight2/{height}")
    public Response blockByHeight2(@PathParam("height") String heightStr) {

        Map out = new LinkedHashMap();
        int step = 1;

        try {
            int height = Integer.parseInt(heightStr);

            ++step;
            Block block;
            LinkedList eee = null;
            //LinkedList eee = ((LinkedList) dcSet.getBlocksHeadMap().map);//.keySet());
            //LinkedList eee = ((LinkedList) dcSet.getBlocksHeadMap().map.keySet());
            //LinkedList eee = ((LinkedList) dcSet.getBlocksHeadMap().map.values());
            //LinkedList eee = ((LinkedList) dcSet.getBlocksHeadMap().map.entrySet());
            ListIterator listIterator = eee.listIterator(height);
            block = (Block) listIterator.next();

            //block = dcSet.getBlocksHeadMap().get(iterator.next());
            out.put("block", block.toJson());

            ++step;
            byte[] childSign = dcSet.getBlocksHeadsMap().get(block.getHeight() + 1).signature;
            if (childSign != null)
                out.put("next", Base58.encode(childSign));

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "height error, use integer value " + e.getMessage());
            else if (step == 2)
                out.put("message", "block not found " + e.getMessage());
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
    @Path("/blocksfromheight/{height}/{limit}")
    public Response getBlocksFromHeight(@PathParam("height") int height,
                                        @PathParam("limit") int limit) {

        if (limit > 30)
            limit = 30;

        //Map out = new LinkedHashMap();
        JSONObject out = new JSONObject();
        int step = 1;

        try {

            JSONArray array = new JSONArray();
            BlockMap blockMap = dcSet.getBlockMap();
            int max = blockMap.size();
            for (int i = height; i < height + limit; i++) {
                if (i > max) {
                    out.put("end", 1);
                    break;
                }
                array.add(blockMap.getAndProcess(i));
            }
            out.put("blocks", array);

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "height error, use integer value");
            else if (step == 2)
                out.put("message", "block not found");
            else
                out.put("message", e.getMessage());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toString())
                .build();
    }

    @GET
    @Path("/blockssignaturesfromheight/{height}/{limit}")
    public Response getBlocksSignsFromHeight(@PathParam("height") int height,
                                             @PathParam("limit") int limit) {

        if (limit > 100)
            limit = 100;

        Map out = new LinkedHashMap();
        int step = 1;

        try {

            JSONArray array = new JSONArray();
            BlocksHeadsMap blocksHeadsMap = dcSet.getBlocksHeadsMap();
            int max = dcSet.getBlockMap().size();
            for (int i = height; i < height + limit; i++) {
                if (i > max) {
                    out.put("end", 1);
                    break;
                }
                array.add(Base58.encode(blocksHeadsMap.get(i).signature));
            }
            out.put("signatures", array);

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "height error, use integer value");
            else if (step == 2)
                out.put("message", "block not found");
            else
                out.put("message", e.getMessage());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }


    /*
     * ************** RECORDS **********
     */

    @POST
    @Path("recordparse")
    public Response recordParse(String raw) // throws JSONException
    {

        JSONObject out = new JSONObject();

        //CREATE TRANSACTION FROM RAW
        Transaction transaction;
        try {
            transaction = TransactionFactory.getInstance().parse(Base58.decode(raw), Transaction.FOR_NETWORK);

            if (transaction instanceof RSignNote) {
                ((RSignNote) transaction).parseDataFull();
            }

            out = transaction.toJson();
        } catch (Exception e) {
            out.put("error", -1);
            out.put("message", APIUtils.errorMess(-1, e.toString()));
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }

    @GET
    @Path("recordparse/{raw}")
    public Response recordParseGET(@PathParam("raw") String raw) // throws JSONException
    {
        //raw = "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";
        return recordParse(raw);
    }

    @GET
    @Path("record/{signature}")
    public Response record(@PathParam("signature") String signature) {

        Map out = new LinkedHashMap();

        int step = 1;

        try {
            byte[] key = Base58.decode(signature);

            ++step;
            Transaction record = cntrl.getTransaction(key, dcSet);
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
                .entity(StrJSonFine.convert(out))
                .build();
    }

    @GET
    @Path("recordbynumber/{number}")
    public Response recodByNumber(@PathParam("number") String numberStr) {

        Map out = new LinkedHashMap();
        int step = 1;

        try {

            String[] strA = numberStr.split("\\-");
            int height = Integer.parseInt(strA[0]);
            int seq = Integer.parseInt(strA[1]);

            ++step;
            Transaction record = dcSet.getTransactionFinalMap().get(height, seq);
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
                .entity(StrJSonFine.convert(out))
                .build();
    }

    @GET
    @Path("recordraw/{signature}")
    public Response recordRaw(@PathParam("signature") String signature) {

        Map out = new LinkedHashMap();

        int step = 1;

        byte[] key;
        Transaction record = null;
        try {
            key = Base58.decode(signature);
            ++step;
            record = cntrl.getTransaction(key, dcSet);

            ++step;
            if (record == null) {
                out.put("error", step);
                out.put("message", "record not found");
            } else {
                ++step;
                try {
                    out = record.rawToJson();
                } catch (Exception e) {
                    out.put("error", step);
                    out.put("message", e.getMessage());
                }
            }

        } catch (Exception e) {
            out.put("error", step);
            if (step == 1)
                out.put("message", "signature error, use Base58 value");
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
    @Path("recordrawbynumber/{number}")
    public Response recodRawByNumber(@PathParam("number") String numberStr) {

        Map out = new LinkedHashMap();
        int step = 1;

        try {

            String[] strA = numberStr.split("\\-");
            int height = Integer.parseInt(strA[0]);
            int seqNo = Integer.parseInt(strA[1]);

            ++step;
            Transaction record = dcSet.getTransactionFinalMap().get(height, seqNo);

            ++step;
            out = record.rawToJson();

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
                .entity(StrJSonFine.convert(out))
                .build();
    }

    @GET
    @Path("recordrawbynumber/{block}/{seqNo}")
    public Response recodRawBySeqNo(@PathParam("block") int block, @PathParam("seqNo") int seqNo) {

        Map out = new LinkedHashMap();

        try {

            Transaction record = dcSet.getTransactionFinalMap().get(block, seqNo);
            out = record.rawToJson();

        } catch (Exception e) {
            out.put("message", e.getMessage());
        }


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }

    @GET
    @Path("broadcast/{raw}")
    // http://127.0.0.1:9047/api/broadcast/DPDnFCNvPk4m8GMi2ZprirSgQDwxuQw4sWoJA3fmkKDrYwddTPtt1ucFV4i45BHhNEn1W1pxy3zhRfpxKy6fDb5vmvQwwJ3M3E12jyWLBJtHRYPLnRJnK7M2x5MnPbvnePGX1ahqt7PpFwwGiivP1t272YZ9VKWWNUB3Jg6zyt51fCuyDCinLx4awQPQJNHViux9xoGS2c3ph32oi56PKpiyM
    public Response broadcastRaw(@PathParam("raw") String raw) {


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(broadcastFromRaw_1(raw)))
                .build();
    }

    @POST
    @Path("broadcastjson")
    public Response broadcastFromRawPost(@Context HttpServletRequest request,
                                         MultivaluedMap<String, String> form) {

        String raw = form.getFirst("raw");

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(broadcastFromRaw_1(raw)))
                .build();

    }

    @POST
    @Path("broadcast")
    public Response broadcastFromRawPost(@Context HttpServletRequest request,
                                         String raw) {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(broadcastFromRaw_1(raw)))
                .build();

    }

    // http://127.0.0.1:9047/api/broadcast?data=DPDnFCNvPk4m8GMi2ZprirSgQDwxuQw4sWoJA3fmkKDrYwddTPtt1ucFV4i45BHhNEn1W1pxy3zhRfpxKy6fDb5vmvQwwJ3M3E12jyWLBJtHRYPLnRJnK7M2x5MnPbvnePGX1ahqt7PpFwwGiivP1t272YZ9VKWWNUB3Jg6zyt51fCuyDCinLx4awQPQJNHViux9xoGS2c3ph32oi56PKpiyM
    public JSONObject broadcastFromRaw_1(String rawDataBase58) {
        int step = 1;
        JSONObject out = new JSONObject();
        try {
            //	JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            //	String rawDataBase58 = (String) jsonObject.get("raw");
            byte[] transactionBytes = Base58.decode(rawDataBase58);


            step++;
            Pair<Transaction, Integer> result = Controller.getInstance().lightCreateTransactionFromRaw(transactionBytes);
            if (result.getB() == Transaction.VALIDATE_OK) {
                out.put("status", "ok");
                return out;
            } else {
                out.put("error", result.getB());
                out.put("message", OnDealClick.resultMess(result.getB()));
                return out;
            }

        } catch (Exception e) {
            //logger.error(e.getMessage());
            out.put("error", APIUtils.errorMess(-1, e.toString() + " on step: " + step));
            return out;
        }
    }

    public JSONObject broadcastTelegram_1(String rawDataBase58) {
        JSONObject out = new JSONObject();
        byte[] transactionBytes;
        Transaction transaction;

        try {
            transactionBytes = Base58.decode(rawDataBase58);
        } catch (Exception e) {
            //logger.error(e.getMessage());
            out.put("error", APIUtils.errorMess(-1, e.toString() + " INVALID_RAW_DATA"));
            return out;
        }

        try {
            transaction = TransactionFactory.getInstance().parse(transactionBytes, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            out.put("error", APIUtils.errorMess(-1, e.toString() + " parse ERROR"));
            return out;
        }

        // CHECK IF RECORD VALID
        if (!transaction.isSignatureValid(DCSet.getInstance())) {
            out.put("error", APIUtils.errorMess(-1, " INVALID_SIGNATURE"));
            return out;
        }

        int status = Controller.getInstance().broadcastTelegram(transaction, true);
        if (status == 0) {
            out.put("status", "ok");
        } else {
            out.put("status", "error");
            out.put("error", OnDealClick.resultMess(status));
        }
        out.put("signature", Base58.encode(transaction.getSignature()));
        return out;
    }

    @GET
    //@Path("broadcasttelegram/{raw}")
    @Path("broadcasttelegram/{raw}")
    // http://127.0.0.1:9047/broadcasttelegram/DPDnFCNvPk4m8GMi2ZprirSgQDwxuQw4sWoJA3fmkKDrYwddTPtt1ucFV4i45BHhNEn1W1pxy3zhRfpxKy6fDb5vmvQwwJ3M3E12jyWLBJtHRYPLnRJnK7M2x5MnPbvnePGX1ahqt7PpFwwGiivP1t272YZ9VKWWNUB3Jg6zyt51fCuyDCinLx4awQPQJNHViux9xoGS2c3ph32oi56PKpiyM
    public Response broadcastTelegram(@PathParam("raw") String raw) {


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(broadcastTelegram_1(raw)))
                .build();
    }

    @POST
    @Path("broadcasttelegramjson")
    public Response broadcastTelegramPost(@Context HttpServletRequest request,
                                          MultivaluedMap<String, String> form) {

        String raw = form.getFirst("raw");

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(broadcastTelegram_1(raw)))
                .build();

    }

    @POST
    @Path("broadcasttelegram")
    public Response broadcastTelegramPost(@Context HttpServletRequest request,
                                          String raw) {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(broadcastTelegram_1(raw)))
                .build();

    }

    /*
     * ********** ADDRESS **********
     */
    // TODO перименовать бы LastTimestamp - так более понятно
    @GET
    @Path("addresslastreference/{address}")
    public Response getAddressLastReference(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);
        }

        // GET ACCOUNT
        Account account = new Account(address);

        long[] lastTimestamp = account.getLastTimestamp();

        String out;
        if (lastTimestamp == null) {
            out = "-";
        } else {
            out = "" + lastTimestamp[0];
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out)
                .build();

    }

    @GET
    @Path("addressunconfirmedlastreference/{address}/{from}/{count}")
    public Response getUnconfirmedLastReferenceUnconfirmed(@PathParam("address") String address,
                                                           @PathParam("from") int from, @PathParam("count") int count) {

        // сейчас этот поиск делается по другому и он не нужен вообще для создания транзакций а следовательно закроем его
        /*
        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);
        }

        // GET ACCOUNT
        Account account = new Account(address);

        HashSet<byte[]> isSomeoneReference = new HashSet<byte[]>();

        Controller cntrl = Controller.getInstance();

        List<Transaction> transactions = Controller.getInstance().getUnconfirmedTransactions(from, count, true);

        DCSet db = DCSet.getInstance();
        Long lastTimestamp = account.getLastTimestamp();
        byte[] signature;
        if (!(lastTimestamp == null)) {
            signature = cntrl.getSignatureByAddrTime(db, address, lastTimestamp);
            transactions.add(cntrl.get(signature));
        }

        for (Transaction item : transactions) {
            if (item.getCreator().equals(account)) {
                for (Transaction item2 : transactions) {
                    if (item.getTimestamp() == item2.getTimestamp()
                            & item.getCreator().getAddress().equals(item2.getCreator().getAddress())) {
                        // if same address and parent timestamp
                        isSomeoneReference.add(item.getSignature());
                        break;
                    }
                }
            }
        }

        String out = "-";
        if (isSomeoneReference.isEmpty()) {
            return getAddressLastReference(address);
        }

        for (Transaction item : cntrl.getUnconfirmedTransactions(from, count, true)) {
            if (item.getCreator().equals(account)) {
                if (!isSomeoneReference.contains(item.getSignature())) {
                    //return Base58.encode(tx.getSignature());
                    out = "" + item.getTimestamp();
                    break;
                }
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out)
                .build();
        */
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("---nope")
                .build();

    }


    @GET
    @Path("addressvalidate/{address}")
    public Response validate(@PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(String.valueOf(Crypto.getInstance().isValidAddress(address)))
                .build();
    }

    @GET
    @Path("addressgeneratingbalance/{address}")
    public Response getAddressGeneratingBalanceOfAddress(
            @PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        Account account = new Account(address);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("" + BlockChain.calcWinValue(DCSet.getInstance(),
                        account, Controller.getInstance().getBlockChain().getHeight(DCSet.getInstance()),
                        account.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue(), null))
                .build();
    }

    @GET
    @Path("addressassetbalance/{address}/{assetid}")
    public Response getAddressAssetBalance(@PathParam("address") String address,
                                           @PathParam("assetid") String assetid) {
        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        Long assetAsLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            assetAsLong = Long.valueOf(assetid);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemAssetMap().contains(assetAsLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);

        }

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance
                = dcSet.getAssetBalanceMap().get(Account.makeShortBytes(address), assetAsLong);
        JSONArray array = new JSONArray();

        array.add(setJSONArray(balance.a));
        array.add(setJSONArray(balance.b));
        array.add(setJSONArray(balance.c));
        array.add(setJSONArray(balance.d));
        array.add(setJSONArray(balance.e));

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(array))
                .build();
    }

    @GET
    @Path("addressassets/{address}")
    public Response getAddressAssetBalance(@PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        Account account = new Account(address);
        ItemAssetBalanceMap map = DCSet.getInstance().getAssetBalanceMap();
        List<Tuple2<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> assetsBalances
                = map.getBalancesList(account);

        JSONObject out = new JSONObject();

        for (Tuple2<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                assetsBalance : assetsBalances) {
            JSONArray array = new JSONArray();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(assetsBalance.a);

            if (BlockChain.ERA_COMPU_ALL_UP) {
                array.add(setJSONArray(account.balAaddDEVAmount(assetKey, assetsBalance.b.a)));
            } else {
                array.add(setJSONArray(assetsBalance.b.a));
            }
            array.add(setJSONArray(assetsBalance.b.b));
            array.add(setJSONArray(assetsBalance.b.c));
            array.add(setJSONArray(assetsBalance.b.d));
            array.add(setJSONArray(assetsBalance.b.e));
            out.put(assetKey, array);
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }

    private JSONArray setJSONArray(Tuple2 t) {
        JSONArray array = new JSONArray();
        array.add(t.a);
        array.add(t.b);
        return array;
    }

    @GET
    @Path("addresspublickey/{address}")
    public Response getPublicKey(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        byte[] publicKey = Controller.getInstance().getPublicKeyByAddress(address);

        if (publicKey == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_PUBLIC_KEY);
        } else {
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(Base58.encode(publicKey))
                    .build();
        }
    }

    @GET
    @Path("addresspersonkey/{address}")
    public Response getPersonKey(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap().getItem(address);

        if (personItem == null) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.CREATOR_NOT_PERSONALIZED);
        } else {
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("" + personItem.a)
                    .build();
        }
    }

    @GET
    @Path("getaccountsfromperson/{key}")
    public Response getAccountsFromPerson(@PathParam("key") String key) {
        JSONObject out = new JSONObject();
        ItemCls cls = DCSet.getInstance().getItemPersonMap().get(new Long(key));
        if (DCSet.getInstance().getItemPersonMap().get(new Long(key)) == null) {
            out.put("error", "Person not Found");
        } else {
            TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses = DCSet.getInstance().getPersonAddressMap().getItems(new Long(key));
            if (addresses.isEmpty()) {
                out.put("null", "null");
            } else {
                Set<String> ad = addresses.keySet();
                int i = 0;
                for (String a : ad) {
                    out.put(i, a);
                    i++;
                }
            }
        }
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(out))
                .build();
    }

    /*
     * ************* ASSET **************
     */
    @GET
    @Path("assetheight")
    public Response assetHeight() {

        long height = dcSet.getItemAssetMap().getLastKey();

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("" + height)
                .build();

    }

    @GET
    @Path("asset/{key}")
    public Response asset(@PathParam("key") long key) {

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();

        AssetCls asset = (AssetCls) map.get(key);
        if (asset == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(asset.toJson()))
                .build();

    }

    @GET
    @Path("assetdata/{key}")
    public Response assetData(@PathParam("key") long key) {

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();

        AssetCls asset = (AssetCls) map.get(key);
        if (asset == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(asset.toJsonData()))
                .build();

    }

    /*
     * ************* ASSETS **************
     */

    @GET
    @Path("assets")
    public Response assets() {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(BlockExplorer.getInstance().jsonQueryAssetsLite()))
                .build();

    }

    @Path("assetsfull")
    public Response assetsFull() {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(BlockExplorer.getInstance().jsonQueryAssets())
                .build();

    }

    @GET
    @Path("assetsfilter/{filter_name_string}")
    public Response assetsFilter(@PathParam("filter_name_string") String filter) {


        if (filter == null || filter.length() < 3) {
            return Response.status(501)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("error - so small filter length")
                    .build();
        }

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        List<ItemCls> list = map.getByFilterAsArray(filter, 0, 100);

        JSONArray array = new JSONArray();

        if (list != null) {
            for (ItemCls item : list) {
                array.add(item.toJson());
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(array))
                .build();

    }

    /*
     * ************* EXCHANGE **************
     */
    @GET
    @Path("exchangeorders/{have}/{want}")
    public Response exchangeOrders(@PathParam("have") long have, @PathParam("want") long want) {

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        // DOES ASSETID EXIST
        if (!map.contains(have)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        if (!map.contains(want)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        /* OLD
        SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> ordersA = this.dcSet.getOrderMap().getOrdersSortableList(have, want, true);

        JSONArray arrayA = new JSONArray();

        if (!ordersA.isEmpty()) {
            for (Pair<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                    Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> pair : ordersA) {
                Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                        Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = pair.getB();
                JSONArray itemJson = new JSONArray();
                itemJson.add(order.b.b.subtract(order.b.c)); // getAmountHaveLeft());
                itemJson.add(Order.calcPrice(order.b.b, order.c.b));

                arrayA.add(itemJson);
            }
        }

        SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> ordersB = this.dcSet.getOrderMap().getOrdersSortableList(want, have, true);

        JSONArray arrayB = new JSONArray();

        if (!ordersA.isEmpty()) {
            for (Pair<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                    Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> pair : ordersB) {
                Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                        Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = pair.getB();
                JSONArray itemJson = new JSONArray();
                itemJson.add(order.b.b.subtract(order.b.c)); // getAmountHaveLeft());
                itemJson.add(Order.calcPrice(order.b.b, order.c.b));

                arrayB.add(itemJson);
            }
        }
        */

        JSONArray arraySell = new JSONArray();
        List<Order> orders = this.dcSet.getOrderMap().getOrdersForTrade(have, want, false);
        for (Order order : orders) {
            JSONArray itemJson = new JSONArray();
            itemJson.add(order.getAmountHaveLeft());
            itemJson.add(order.calcLeftPrice());
            itemJson.add(order.getAmountWantLeft());

            arraySell.add(itemJson);

        }

        JSONArray arrayBuy = new JSONArray();
        orders = this.dcSet.getOrderMap().getOrdersForTrade(want, have, false);
        for (Order order : orders) {
            JSONArray itemJson = new JSONArray();
            itemJson.add(order.getAmountHaveLeft());
            itemJson.add(order.calcLeftPriceReverse()); // REVERSE
            itemJson.add(order.getAmountWantLeft());

            arrayBuy.add(itemJson);

        }

        JSONObject itemJSON = new JSONObject();

        // ADD DATA
        itemJSON.put("buy", arrayBuy);
        itemJSON.put("sell", arraySell);
        itemJSON.put("pair", have + ":" + want);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                //.entity(StrJSonFine.convert(itemJSON))
                .entity(itemJSON.toJSONString())
                //.entity(itemJSON.toString())
                .build();

    }


    /*
     * ************* PERSON **************
     */
    @GET
    @Path("personheight")
    public Response personHeight() {

        long height = dcSet.getItemPersonMap().getLastKey();

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("" + height)
                .build();

    }

    @GET
    @Path("person/{key}")
    public Response person(@PathParam("key") long key) {

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(person.toJson()))
                .build();

    }

    @Path("assetimage/{key}")
    @GET
    @Produces({"image/png", "image/jpg"})
    public Response assetImage(@PathParam("key") long key) throws IOException {

        int weight = 0;
        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        AssetCls asset = (AssetCls) map.get(key);

        if (asset.getImage() != null) {
            // image to byte[] hot scale (param2 =0)
            //	byte[] b = Images_Work.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
            ///return Response.ok(new ByteArrayInputStream(asset.getImage())).build();
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(asset.getImage()))
                    .build();
        }
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity("")
                .build();

    }

    @Path("asseticon/{key}")
    @GET
    @Produces({"image/png", "image/jpg"})
    public Response assetIcon(@PathParam("key") long key) throws IOException {

        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        AssetCls asset = (AssetCls) map.get(key);

        if (asset.getIcon() != null) {
            // image to byte[] hot scale (param2 =0)
            //	byte[] b = Images_Work.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
            //return Response.ok(new ByteArrayInputStream(asset.getIcon())).build();
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(asset.getIcon()))
                    .build();
        }
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity("")
                .build();
    }

    @Path("personimage/{key}")
    @GET
    @Produces({"image/png", "image/jpg"})
    public Response getFullImage(@PathParam("key") long key) throws IOException {

        int weight = 0;
        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        // image to byte[] hot scale (param2 =0)
        //	byte[] b = Images_Work.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
        //return Response.ok(new ByteArrayInputStream(person.getImage())).build();
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ByteArrayInputStream(person.getImage()))
                .build();
    }


    @GET
    @Path("persondata/{key}")
    public Response personData(@PathParam("key") long key) {

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES ASSETID EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(person.toJsonData()))
                .build();

    }

    @GET
    @Path("personkeybyaddress/{address}")
    public Response getPersonKeyByAddres(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap().getItem(address);

        if (personItem == null) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.CREATOR_NOT_PERSONALIZED);
        } else {
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("" + personItem.a)
                    .build();
        }
    }

    @GET
    @Path("personkeybypublickey/{publickey}")
    public Response getPersonKeyByPublicKey(@PathParam("publickey") String publicKey) {

        // CHECK IF VALID ADDRESS
        if (!PublicKeyAccount.isValidPublicKey(publicKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_PUBLIC_KEY);

        }

        PublicKeyAccount publicKeyAccount = new PublicKeyAccount(publicKey);

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap().getItem(publicKeyAccount.getShortAddressBytes());

        if (personItem == null) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.CREATOR_NOT_PERSONALIZED);
        } else {
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("" + personItem.a)
                    .build();
        }
    }

    @GET
    @Path("personkeybyownerpublickey/{publickey}")
    public Response getPersonKeyByOwnerPublicKey(@PathParam("publickey") String publicKey) {

        // CHECK IF VALID ADDRESS
        if (!PublicKeyAccount.isValidPublicKey(publicKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_PUBLIC_KEY);

        }

        PublicKeyAccount publicKeyAccount = new PublicKeyAccount(publicKey);
        byte[] pkBytes = publicKeyAccount.getPublicKey();
        if (!DCSet.getInstance().getIssuePersonMap().contains(pkBytes)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        } else {
            Long key = DCSet.getInstance().getIssuePersonMap().get(pkBytes);
            if (key == null || key == 0) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.ITEM_PERSON_NOT_EXIST);
            }

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("" + key)
                    .build();
        }
    }

    @GET
    @Path("personkeybyownerpublickey32/{publickey}")
    public Response getPersonKeyByOwnerPublicKey32(@PathParam("publickey") String publicKey32) {

        JSONObject answer = new JSONObject();
        try {
            byte[] publicKey = Base32.decode(publicKey32);
            return getPersonKeyByOwnerPublicKey(Base58.encode(publicKey));
        } catch (Exception e) {
            answer.put("Error", "Invalid Base32 Key");
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(StrJSonFine.convert(answer))
                    .build();
        }
    }


    @GET
    @Path("personbyaddress/{address}")
    public Response personByAddress(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap().getItem(address);

        if (personItem == null) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        long key = personItem.a;
        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(person.toJson()))
                .build();

    }

    @GET
    @Path("personbypublickey/{publickey}")
    public Response personByPublicKey(@PathParam("publickey") String publicKey) {

        // CHECK IF VALID ADDRESS
        if (!PublicKeyAccount.isValidPublicKey(publicKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_PUBLIC_KEY);

        }

        PublicKeyAccount publicKeyAccount = new PublicKeyAccount(publicKey);

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap().getItem(publicKeyAccount.getShortAddressBytes());
        //Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap().getItem(publicKeyAccount.getAddress());

        if (personItem == null) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        long key = personItem.a;
        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(person.toJson()))
                .build();
    }

    @GET
    @Path("personbyownerpublickey/{publickey}")
    public Response getPersonByOwnerPublicKey(@PathParam("publickey") String publicKey) {

        // CHECK IF VALID ADDRESS
        if (!PublicKeyAccount.isValidPublicKey(publicKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_PUBLIC_KEY);

        }

        PublicKeyAccount publicKeyAccount = new PublicKeyAccount(publicKey);
        byte[] pkBytes = publicKeyAccount.getPublicKey();
        if (!DCSet.getInstance().getIssuePersonMap().contains(pkBytes)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        } else {
            Long key = DCSet.getInstance().getIssuePersonMap().get(pkBytes);
            if (key == null || key == 0) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.ITEM_PERSON_NOT_EXIST);
            }

            PersonCls person = (PersonCls) DCSet.getInstance().getItemPersonMap().get(key);
            if (person == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.ITEM_PERSON_NOT_EXIST);
            }

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(StrJSonFine.convert(person.toJson()))
                    .build();
        }
    }

    @GET
    @Path("personbyownerpublickey32/{publickey}")
    public Response getPersonByOwnerPublicKey32(@PathParam("publickey") String publicKey32) {

        JSONObject answer = new JSONObject();
        try {
            byte[] publicKey = Base32.decode(publicKey32);
            return getPersonByOwnerPublicKey(Base58.encode(publicKey));
        } catch (Exception e) {
            answer.put("Error", "Invalid Base32 Key");
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(StrJSonFine.convert(answer))
                    .build();
        }
    }

    @GET
    @Path("personbypublickeybase32/{publickeybase}")
    @Deprecated
    public Response personsByBankKey(@PathParam("publickeybase") String publicKey32) {

        return getPersonByOwnerPublicKey32(publicKey32);

    }

    /*
     * ************* PERSONS **************
     */

    @GET
    @Path("personsfilter/{filter_name_string}")
    public Response personsFilter(@PathParam("filter_name_string") String filter) {

        if (filter == null || filter.length() < 3) {
            return Response.status(501)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("error - so small filter length")
                    .build();
        }

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES ASSETID EXIST
        List<ItemCls> list = map.getByFilterAsArray(filter, 0, 100);

        JSONArray array = new JSONArray();

        if (list != null) {
            for (ItemCls item : list) {
                array.add(item.toJson());
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(array))
                .build();

    }


    /*
     * ************* TOOLS **************
     */

    /**
     * wiury2876rw7yer8923y63riyrf9287y6r87wyr9737yriwuyr3yr978ry48732y3rsiouyvbkshefiuweyriuwer
     * {"trtr": 293847}
     * @param x
     * @return
     */
    @POST
    @Path("verifysignature")
    public Response verifysignature(String x) {
        try {
            // READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String message = (String) jsonObject.get("message");
            String signature = (String) jsonObject.get("signature");
            String publicKey = (String) jsonObject.get("publickey");

            // DECODE SIGNATURE
            byte[] signatureBytes;
            try {
                signatureBytes = Base58.decode(signature);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_SIGNATURE);

            }

            // DECODE PUBLICKEY
            byte[] publicKeyBytes;
            try {
                publicKeyBytes = Base58.decode(publicKey);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        //ApiErrorFactory.ERROR_INVALID_PUBLIC_KEY);
                        Transaction.INVALID_PUBLIC_KEY);

            }

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(String.valueOf(Crypto.getInstance().verify(publicKeyBytes,
                            signatureBytes, message.getBytes(StandardCharsets.UTF_8))))
                    .build();

        } catch (NullPointerException e) {
            // JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON);
        } catch (ClassCastException e) {
            // JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON);
        }
    }

    @GET
    @Path("info")
    public Response getInformation() throws NoSuchFieldException, IllegalAccessException {
        JSONObject jsonObject = CoreResource.infoJson();

        if (false) {
            Object f = Controller.class.getDeclaredField("version");
            ((Field) f).setAccessible(true);
            String version = ((Field) f).get(Controller.getInstance()).toString();
            ((Field) f).setAccessible(false);
            jsonObject.put("version2", version);
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(jsonObject.toJSONString())
                .build();
    }

    @GET
    @Path("bench")
    public Response getSpeedInfo() {
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(Controller.getInstance().getBenchmarks().toJSONString())
                .build();
    }

}
