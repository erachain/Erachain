package webserver;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.swing.ImageIcon;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import api.ApiErrorFactory;
import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.AEScrypto;
import core.crypto.Base32;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import database.BlockHeightsMap;
import database.BlockMap;
import database.DBSet;
import database.ItemAssetMap;
import database.ItemPersonMap;
import database.SortableList;
import gui.library.Images_Work;
import utils.APIUtils;
import utils.Pair;
import utils.StrJSonFine;

@SuppressWarnings({ "unchecked", "rawtypes" })

@Path("api")
public class API {
	
	@Context
    private UriInfo uriInfo;
	private HttpServletRequest request;
    
	private static final Logger LOGGER = Logger
			.getLogger(API.class);

	private DBSet dbSet = DBSet.getInstance();
	private Controller cntrl = Controller.getInstance();

	@GET
	public Response Default() {
		
		Map help = new LinkedHashMap();

		help.put("GET Height", "height");
		help.put("*** BLOCK ***", "");
		help.put("GET First Block", "firstblock");
		help.put("GET Last Block", "lastblock");
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
		help.put("GET Record RAW by Height and Sequence", "recordrawbynumber/{height-sequence}");
		
		help.put("*** ADDRESS ***", "");
		help.put("GET Address Validate", "addressvalidate/{address}");
		help.put("GET Address Last Reference", "addresslastreference/{address}");
		help.put("GET Address Unconfirmed Last Reference", "addressunconfirmedlastreference/{address}");
		help.put("GET Address Generating Balance", "addressgeneratingbalance/{address}");
		help.put("GET Address Asset Balance", "addressassetbalance/{address}/{assetid}");
		help.put("GET Address Assets", "addressassets/{address}");
		help.put("GET Address Public Key", "addresspublickey/{address}");
		
		help.put("*** ASSET ***", "");
		help.put("GET Asset Height", "assetheight");
		help.put("GET Asset", "asset/{key}");
		help.put("GET Asset Data", "assetdata/{key}");
		
		help.put("*** ASSETS ***", "");
		help.put("GET Assets", "assets");
		help.put("GET Assets Full", "assetsfull");
		help.put("GET Assets by Name Filter", "assetsfilter/{filter_name_string}");		

		help.put("*** PERSON ***", "");
		help.put("GET Person Height", "personheight");
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
		help.put("POST Verify Signature for JSON {\"message\": ..., \"signature\": Base58, \"publickey\": Base58)", "verifysignature");
		
		help.put("POST Broadcast" , "/broadcast JSON {raw=raw(BASE58)}");
		help.put("GET Broadcast" , "/broadcast/{raw(BASE58)}");
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(help))
				.build();
	}

	@GET
	@Path("height")
	public static String getHeight() 
	{
		return String.valueOf(Controller.getInstance().getMyHeight());
	}

	@GET
	@Path("firstblock")	
	public Response getFirstBlock()
	{
		Map out = new LinkedHashMap();

		out = Controller.getInstance().getBlockChain().getGenesisBlock().toJson();

		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(out))
				.build();
	}
	
	@GET
	@Path("lastblock")
	public Response lastBlock()
	{
		
		Map out = new LinkedHashMap();

		Block lastBlock = dbSet.getBlockMap().getLastBlock();
		out = lastBlock.toJson();
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(out))
				.build();
	}
	
	@GET
	@Path("/childblocksignature/{signature}")	
	public Response getChildBlockSignature(@PathParam("signature") String signature)
	{
		//DECODE SIGNATURE
		byte[] signatureBytes;
		Map out = new LinkedHashMap();
		
		int steep = 1;
		try
		{
			signatureBytes = Base58.decode(signature);

			++steep;
			byte[] childSign = dbSet.getChildMap().get(signatureBytes);
			out.put("child", Base58.encode(childSign));
		}
		catch(Exception e)
		{
			out.put("error", steep);
			if (steep == 1)
				out.put("message", "signature error, use Base58 value");
			else if (steep == 2)
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
	public Response getChildBlock(@PathParam("signature") String signature)
	{
		//DECODE SIGNATURE
		byte[] signatureBytes;
		Map out = new LinkedHashMap();
		
		int steep = 1;
		try
		{
			signatureBytes = Base58.decode(signature);

			++steep;
			byte[] childSign = dbSet.getChildMap().get(signatureBytes);
			out = dbSet.getBlockMap().get(childSign).toJson();
		}
		catch(Exception e)
		{
			out.put("error", steep);
			if (steep == 1)
				out.put("message", "signature error, use Base58 value");
			else if (steep == 2)
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
	public Response block(@PathParam("signature") String signature)
	{
		
		Map out = new LinkedHashMap();

		int steep = 1;

		try {
			byte[] key = Base58.decode(signature);

			++steep;
			Block block = dbSet.getBlockMap().get(key);			
			out.put("block", block.toJson());
			
			++steep;
			byte[] childSign = dbSet.getChildMap().get(block.getSignature());
			if (childSign != null)
				out.put("next", Base58.encode(childSign));

		} catch (Exception e) {
			
			out.put("error", steep);
			if (steep == 1)
				out.put("message", "signature error, use Base58 value");
			else if (steep == 2)
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
	public Response blockByHeight(@PathParam("height") String heightStr)
	{
		
		Map out = new LinkedHashMap();
		int steep = 1;

		try {
			int height = Integer.parseInt(heightStr);
			
			++steep;
			Block block = cntrl.getBlockByHeight(dbSet, height);
			out.put("block", block.toJson());
			
			++steep;
			byte[] childSign = dbSet.getChildMap().get(block.getSignature());
			if (childSign != null)
				out.put("next", Base58.encode(childSign));
			
		} catch (Exception e) {
			
			out.put("error", steep);
			if (steep == 1)
				out.put("message", "height error, use integer value");
			else if (steep == 2)
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
	@Path("/blocksfromheight/{height}/{limit}")
	public Response getBlocksFromHeight(@PathParam("height") int height,
			@PathParam("limit") int limit) 
	{
		
		if (limit > 30)
			limit = 30;

		Map out = new LinkedHashMap();
		int steep = 1;

		try {
			
			JSONArray array = new JSONArray();
			BlockHeightsMap blockHeightMap = dbSet.getBlockHeightsMap();
			BlockMap blockMap = dbSet.getBlockMap();
			for (int i = height; i < height + limit + 1; ++i) {
				byte[] signature = blockHeightMap.get((long)i);
				if (signature == null) {
					out.put("end", 1);
					break;
				} else {
					array.add(blockMap.get(signature).toJson());
				}
			}
			out.put("blocks", array);
			
		} catch (Exception e) {
			
			out.put("error", steep);
			if (steep == 1)
				out.put("message", "height error, use integer value");
			else if (steep == 2)
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
	@Path("/blockssignaturesfromheight/{height}/{limit}")
	public Response getBlocksSignsFromHeight(@PathParam("height") int height,
			@PathParam("limit") int limit) 
	{
		
		if (limit > 100)
			limit = 100;

		Map out = new LinkedHashMap();
		int steep = 1;

		try {
			
			JSONArray array = new JSONArray();
			BlockHeightsMap blockHeightMap = dbSet.getBlockHeightsMap();
			for (int i = height; i < height + limit + 1; ++i) {
				byte[] signature = blockHeightMap.get((long)i);
				if (signature == null) {
					out.put("end", 1);
					break;
				} else {
					array.add(Base58.encode(signature));
				}
			}
			out.put("signatures", array);
			
		} catch (Exception e) {
			
			out.put("error", steep);
			if (steep == 1)
				out.put("message", "height error, use integer value");
			else if (steep == 2)
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
	public Response recordParse(@QueryParam("raw") String raw) // throws JSONException
	{

		JSONObject out = new JSONObject();
		
		//CREATE TRANSACTION FROM RAW
		Transaction transaction;
		try {			
			transaction = TransactionFactory.getInstance().parse(Base58.decode(raw), null);
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
		return recordParse(raw);
	}

	@GET
	@Path("record/{signature}")
	public Response record(@PathParam("signature") String signature)
	{
		
		Map out = new LinkedHashMap();

		int steep = 1;

		try {
			byte[] key = Base58.decode(signature);

			++steep;
			Transaction record = cntrl.getTransaction(key, dbSet);		
			out = record.toJson();
			
		} catch (Exception e) {
			
			out.put("error", steep);
			if (steep == 1)
				out.put("message", "signature error, use Base58 value");
			else if (steep == 2)
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
	public Response recodByNumber(@PathParam("number") String numberStr)
	{
		
		Map out = new LinkedHashMap();
		int steep = 1;

		try {
			
			String[] strA = numberStr.split("\\-");
			int height = Integer.parseInt(strA[0]);
			int seq = Integer.parseInt(strA[1]);
			
			++steep;	
			Transaction record = dbSet.getTransactionFinalMap().getTransaction(height, seq);
			out = record.toJson();
						
		} catch (Exception e) {
			
			out.put("error", steep);
			if (steep == 1)
				out.put("message", "height-sequence error, use integer-integer value");
			else if (steep == 2)
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
	public Response recordRaw(@PathParam("signature") String signature)
	{
		
		Map out = new LinkedHashMap();

		int steep = 1;

		try {
			byte[] key = Base58.decode(signature);

			++steep;
			Transaction record = cntrl.getTransaction(key, dbSet);		
			out = record.rawToJson();
			
		} catch (Exception e) {
			
			out.put("error", steep);
			if (steep == 1)
				out.put("message", "signature error, use Base58 value");
			else if (steep == 2)
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
	@Path("recordrawbynumber/{number}")
	public Response recodRawByNumber(@PathParam("number") String numberStr)
	{
		
		Map out = new LinkedHashMap();
		int steep = 1;

		try {
			
			String[] strA = numberStr.split("\\-");
			int height = Integer.parseInt(strA[0]);
			int seq = Integer.parseInt(strA[1]);
			
			++steep;	
			Transaction record = dbSet.getTransactionFinalMap().getTransaction(height, seq);
			out = record.rawToJson();
						
		} catch (Exception e) {
			
			out.put("error", steep);
			if (steep == 1)
				out.put("message", "height-sequence error, use integer-integer value");
			else if (steep == 2)
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
	@Path("broadcast/{raw}")
	// http://127.0.0.1:9047/lightwallet/broadcast?data=DPDnFCNvPk4m8GMi2ZprirSgQDwxuQw4sWoJA3fmkKDrYwddTPtt1ucFV4i45BHhNEn1W1pxy3zhRfpxKy6fDb5vmvQwwJ3M3E12jyWLBJtHRYPLnRJnK7M2x5MnPbvnePGX1ahqt7PpFwwGiivP1t272YZ9VKWWNUB3Jg6zyt51fCuyDCinLx4awQPQJNHViux9xoGS2c3ph32oi56PKpiyM
	public Response broadcastRaw(@PathParam("raw") String raw)
	{
		
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(broadcastFromRawPost1(raw)))
				.build();
	}

	@POST
	@Path("broadcast")
	public Response broadcastFromRawPost (@Context HttpServletRequest request,
			MultivaluedMap<String, String> form){
		
		String raw = form.getFirst("raw");
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(broadcastFromRawPost1(raw)))
				.build();
		
	}
	
	// http://127.0.0.1:9047/lightwallet/broadcast?data=DPDnFCNvPk4m8GMi2ZprirSgQDwxuQw4sWoJA3fmkKDrYwddTPtt1ucFV4i45BHhNEn1W1pxy3zhRfpxKy6fDb5vmvQwwJ3M3E12jyWLBJtHRYPLnRJnK7M2x5MnPbvnePGX1ahqt7PpFwwGiivP1t272YZ9VKWWNUB3Jg6zyt51fCuyDCinLx4awQPQJNHViux9xoGS2c3ph32oi56PKpiyM
	public JSONObject broadcastFromRawPost1( String rawDataBase58)
	{
		int steep = 1;
		JSONObject out = new JSONObject();
		try {
		//	JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
		//	String rawDataBase58 = (String) jsonObject.get("raw");
			byte[] transactionBytes = Base58.decode(rawDataBase58);
		
	
			steep++;
			Pair<Transaction, Integer> result = Controller.getInstance().lightCreateTransactionFromRaw(transactionBytes);
			if(result.getB() == Transaction.VALIDATE_OK) {
				out.put("status", "ok");
				return out;
			} else {
				out.put("error",result.getB());
				out.put("message", gui.transaction.OnDealClick.resultMess(result.getB()));
				return out;
			}

		} catch (Exception e) {
			//LOGGER.info(e);
			out.put("error", APIUtils.errorMess(-1, e.toString() + " on steep: " + steep));
			return out;
		}
	}


	/*
	 * ********** ADDRESS **********
	 */
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

		Long lastTimestamp = account.getLastReference();
		
		String out;
		if(lastTimestamp == null) {
			out = "-"; 
		} else {
			out = ""+lastTimestamp;
		}
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(out)
				.build();

	}
	
	@GET
	@Path("addressunconfirmedlastreference/{address}/")
	public Response getUnconfirmedLastReferenceUnconfirmed(@PathParam("address") String address) {
		
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

		List<Transaction> transactions = Controller.getInstance().getUnconfirmedTransactions();
		
		DBSet db = DBSet.getInstance();
		Long lastTimestamp = account.getLastReference();
		byte[] signature;
		if(!(lastTimestamp == null)) 
		{
			signature = cntrl.getSignatureByAddrTime(db, address, lastTimestamp);
			transactions.add(cntrl.getTransaction(signature));
		}	
		
		for (Transaction item: transactions)
		{
			if (item.getCreator().equals(account))
			{
				for (Transaction item2 : transactions)
				{
					if (item.getTimestamp() == item2.getReference()
							& item.getCreator().getAddress().equals(item2.getCreator().getAddress())){
						// if same address and parent timestamp
						isSomeoneReference.add(item.getSignature());
						break;
					}
				}
			}	
		}
		
		String out = "-";
		if(isSomeoneReference.isEmpty())
		{
			return getAddressLastReference(address);
		}
		
		for (Transaction item : cntrl.getUnconfirmedTransactions())
		{
			if (item.getCreator().equals(account))
			{
				if(!isSomeoneReference.contains(item.getSignature()))
				{
					//return Base58.encode(tx.getSignature());
					out =  ""+item.getTimestamp();
					break;
				}
			}
		}
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(out)
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

		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity("" + Block.calcGeneratingBalance(DBSet.getInstance(),
						new Account(address), Controller.getInstance().getBlockChain().getHeight(DBSet.getInstance()) ))
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
		if (!DBSet.getInstance().getItemAssetMap().contains(assetAsLong)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ITEM_ASSET_NOT_EXIST);

		}
		
		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance = dbSet.getAssetBalanceMap().get(address, assetAsLong);
		JSONArray array = new JSONArray();
		array.add(balance.a);
		array.add(balance.b);
		array.add(balance.c);

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

		SortableList<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> assetsBalances = DBSet.getInstance().getAssetBalanceMap().getBalancesSortableList(new Account(address));

		JSONObject out = new JSONObject();
		
		for (Pair<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> assetsBalance : assetsBalances) 	
		{
			JSONArray array = new JSONArray();
			array.add(assetsBalance.getB().a);
			array.add(assetsBalance.getB().b);
			array.add(assetsBalance.getB().c);
			out.put(assetsBalance.getA().b, array);
		}
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(out))
				.build();
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

		Tuple4<Long, Integer, Integer, Integer> personItem = DBSet.getInstance().getAddressPersonMap().getItem(address);		
		
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
		ItemCls cls = DBSet.getInstance().getItemPersonMap().get(new Long(key));
		if (DBSet.getInstance().getItemPersonMap().get(new Long(key)) == null){
			out.put("error", "Person not Found");
		}
		else
		{
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses = DBSet.getInstance().getPersonAddressMap().getItems(new Long(key));
		if (addresses.size() == 0){
			out.put("null", "null");
		}else{
		Set<String> ad = addresses.keySet();
		int i = 0;
		for (String a:ad){
			out.put(i,a);
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
		
		long height = dbSet.getItemAssetMap().getSize();

		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity("" + height)
				.build();
		
	}

	@GET
	@Path("asset/{key}")
	public Response asset(@PathParam("key") long key) {
		
		ItemAssetMap map = DBSet.getInstance().getItemAssetMap();
		// DOES ASSETID EXIST
		if (!map.contains(key)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ITEM_ASSET_NOT_EXIST);
		}
		
		AssetCls asset = (AssetCls)map.get(key);
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(asset.toJson()))
				.build();
		
	}
	
	@GET
	@Path("assetdata/{key}")
	public Response assetData(@PathParam("key") long key) {
		
		ItemAssetMap map = DBSet.getInstance().getItemAssetMap();
		// DOES ASSETID EXIST
		if (!map.contains(key)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ITEM_ASSET_NOT_EXIST);
		}
		
		AssetCls asset = (AssetCls)map.get(key);
		
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
				.entity(StrJSonFine.convert(core.blockexplorer.BlockExplorer.getInstance().jsonQueryAssetsLite()))
				.build();
		
	}

	@Path("assetsfull")
	public Response assetsFull() {
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(core.blockexplorer.BlockExplorer.getInstance().jsonQueryAssets())
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
		
		ItemAssetMap map = DBSet.getInstance().getItemAssetMap();
		List<ItemCls> list = map.get_By_Name(filter, false);

		JSONArray array = new JSONArray();
		
		if (list != null) {
			for(ItemCls item: list)
			{
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
	 * ************* PERSON **************
	 */
	@GET
	@Path("personheight")
	public Response personHeight() {
		
		long height = dbSet.getItemPersonMap().getSize();

		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity("" + height)
				.build();
		
	}

	@GET
	@Path("person/{key}")
	public Response person(@PathParam("key") long key) {
		
		ItemPersonMap map = DBSet.getInstance().getItemPersonMap();
		// DOES EXIST
		if (!map.contains(key)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ITEM_PERSON_NOT_EXIST);
		}
		
		PersonCls person = (PersonCls)map.get(key);
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(person.toJson()))
				.build();
		
	}
	
	
	@Path("personimage/{key}")
	@GET
	@Produces({"image/png", "image/jpg"})
	public Response getFullImage(@PathParam("key") long key) throws IOException {
		
		int weight = 0;
	 if (key <=0) {
		 throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					"Error key");
	 }
		
	 ItemPersonMap map = DBSet.getInstance().getItemPersonMap();
		// DOES EXIST
		if (!map.contains(key)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ITEM_PERSON_NOT_EXIST);
		}
		
		PersonCls person = (PersonCls)map.get(key);
		
	// image to byte[] hot scale (param2 =0)
	//	byte[] b = Images_Work.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
		return Response.ok(new ByteArrayInputStream(person.getImage())).build();
	}
	
	
	@GET
	@Path("persondata/{key}")
	public Response personData(@PathParam("key") long key) {
		
		ItemPersonMap map = DBSet.getInstance().getItemPersonMap();
		// DOES ASSETID EXIST
		if (!map.contains(key)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ITEM_PERSON_NOT_EXIST);
		}
		
		PersonCls person = (PersonCls)map.get(key);
		
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

		Tuple4<Long, Integer, Integer, Integer> personItem = DBSet.getInstance().getAddressPersonMap().getItem(address);		
		
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
		if (!core.account.PublicKeyAccount.isValidPublicKey(publicKey)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ADDRESS);
					Transaction.INVALID_PUBLIC_KEY);

		}
		
		PublicKeyAccount publicKeyAccount = new PublicKeyAccount(publicKey);

		Tuple4<Long, Integer, Integer, Integer> personItem = DBSet.getInstance().getAddressPersonMap().getItem(publicKeyAccount.getAddress());		
		
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
	@Path("personbyaddress/{address}")
	public Response personByAddress(@PathParam("address") String address) {
		
		// CHECK IF VALID ADDRESS
		if (!Crypto.getInstance().isValidAddress(address)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ADDRESS);
					Transaction.INVALID_ADDRESS);

		}

		Tuple4<Long, Integer, Integer, Integer> personItem = DBSet.getInstance().getAddressPersonMap().getItem(address);		
		
		if (personItem == null) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ITEM_PERSON_NOT_EXIST);
		}

		long key = personItem.a;
		ItemPersonMap map = DBSet.getInstance().getItemPersonMap();
		// DOES EXIST
		if (!map.contains(key)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ITEM_PERSON_NOT_EXIST);
		}
		
		PersonCls person = (PersonCls)map.get(key);
		
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
		if (!core.account.PublicKeyAccount.isValidPublicKey(publicKey)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ADDRESS);
					Transaction.INVALID_PUBLIC_KEY);

		}
		
		PublicKeyAccount publicKeyAccount = new PublicKeyAccount(publicKey);

		Tuple4<Long, Integer, Integer, Integer> personItem = DBSet.getInstance().getAddressPersonMap().getItem(publicKeyAccount.getAddress());		
		
		if (personItem == null) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ITEM_PERSON_NOT_EXIST);
		}

		long key = personItem.a;
		ItemPersonMap map = DBSet.getInstance().getItemPersonMap();
		// DOES EXIST
		if (!map.contains(key)) {
			throw ApiErrorFactory.getInstance().createError(
					//ApiErrorFactory.ERROR_INVALID_ASSET_ID);
					Transaction.ITEM_PERSON_NOT_EXIST);
		}
		
		PersonCls person = (PersonCls)map.get(key);
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(person.toJson()))
				.build();
		
	}
	
	@GET
	@Path("personbypublickeybase32/{publickeybase}")
	public Response personsByBankKey(@PathParam("publickeybase") String bankkey) {

		JSONObject ansver;
		ansver = new JSONObject();	
		try {
			byte[] publicKey = Base32.decode(bankkey);
					
					   Iterator<Pair<Long, ItemCls>> tt = DBSet.getInstance().getItemPersonMap().getList().iterator();
					while(tt.hasNext())
					{
						  Pair<Long, ItemCls> s = tt.next();
						PersonCls p = (PersonCls) s.getB();
						PublicKeyAccount own = p.getOwner();
						 byte[] ow = own.getPublicKey();
						if (Arrays.equals(ow,publicKey)){
							 ansver = p.toJson();
							
						}
					}
					
				if (ansver.size()== 0) {
					
					ansver.put("Error", "Public Key Not Found");
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ansver.put("Error", "Invalid Base32 Key");
		}
		
		
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(ansver))
				.build();
		
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

		ItemPersonMap map = DBSet.getInstance().getItemPersonMap();
		// DOES ASSETID EXIST
		List<ItemCls> list = map.get_By_Name(filter, false);

		JSONArray array = new JSONArray();
		
		if (list != null) {
			for(ItemCls item: list)
			{
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
	
	@POST
	@Path("verifysignature")
	public String verifysignature(String x) {
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

			return String.valueOf(Crypto.getInstance().verify(publicKeyBytes,
					signatureBytes, message.getBytes(StandardCharsets.UTF_8)));
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


}
