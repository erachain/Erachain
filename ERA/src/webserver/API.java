package webserver;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import api.ApiErrorFactory;
import controller.Controller;
import core.TransactionCreator;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.blockexplorer.BlockExplorer;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.naming.Name;
import core.transaction.ArbitraryTransaction;
import core.transaction.BuyNameTransaction;
import core.transaction.CancelOrderTransaction;
import core.transaction.CancelSellNameTransaction;
import core.transaction.CreateOrderTransaction;
import core.transaction.CreatePollTransaction;
import core.transaction.DeployATTransaction;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.GenesisIssueNoteRecord;
import core.transaction.GenesisIssuePersonRecord;
import core.transaction.GenesisIssueStatusRecord;
import core.transaction.GenesisTransferAssetTransaction;
import core.transaction.IssueAssetTransaction;
import core.transaction.IssueImprintRecord;
import core.transaction.IssueNoteRecord;
import core.transaction.IssuePersonRecord;
import core.transaction.IssueStatusRecord;
import core.transaction.IssueUnionRecord;
import core.transaction.MultiPaymentTransaction;
import core.transaction.R_Hashes;
import core.transaction.R_Send;
import core.transaction.R_SertifyPubKeys;
import core.transaction.R_SetStatusToItem;
import core.transaction.R_SetUnionToItem;
import core.transaction.R_SignNote;
import core.transaction.R_Vouch;
import core.transaction.RegisterNameTransaction;
import core.transaction.SellNameTransaction;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import core.transaction.UpdateNameTransaction;
import core.transaction.VoteOnPollTransaction;
import database.BlockHeightsMap;
import database.BlockMap;
import database.DBSet;
import database.ItemAssetMap;
import database.ItemPersonMap;
import database.SortableList;
import lang.Lang;
import network.Peer;
import ntp.NTP;
import utils.APIUtils;
import utils.Converter;
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
		help.put("GET Address Person Key", "addresspersonkey/{address}");
		
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
		help.put("GET Person by Address", "personbyaddress/{address}");
		help.put("GET Person Data", "persondata/{key}");

		help.put("*** PERSONS ***", "");
		help.put("GET Persons by Name Filter", "personsfilter/{filter_name_string}");

		help.put("*** TOOLS ***", "");
		help.put("POST Verify Signature for JSON {\"message\": ..., \"signature\": Base58, \"publickey\": Base58)", "verifysignature");
		
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

	@POST
	@Path("broadcast")
	// http://127.0.0.1:9047/lightwallet/broadcast?data=DPDnFCNvPk4m8GMi2ZprirSgQDwxuQw4sWoJA3fmkKDrYwddTPtt1ucFV4i45BHhNEn1W1pxy3zhRfpxKy6fDb5vmvQwwJ3M3E12jyWLBJtHRYPLnRJnK7M2x5MnPbvnePGX1ahqt7PpFwwGiivP1t272YZ9VKWWNUB3Jg6zyt51fCuyDCinLx4awQPQJNHViux9xoGS2c3ph32oi56PKpiyM
	public Response broadcastFromRawPost(@QueryParam("raw") String raw58)
	{
		byte[] transactionBytes = Base58.decode(raw58);
		
		Pair<Transaction, Integer> result = Controller.getInstance().createTransactionFromRaw(transactionBytes);
		if(result.getB() == Transaction.VALIDATE_OK) {
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*")
					.entity(StrJSonFine.convert(result.getA().toJson()))
					.build();
		} else {
			
			Map out = new LinkedHashMap();
			out.put("error", result.getB());
			out.put("message", gui.transaction.OnDealClick.resultMess(result.getB()));
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*")
					.entity(StrJSonFine.convert(result.getA().toJson()))
					.build();
		}
	}

	@GET
	@Path("broadcast/{raw}")
	// http://127.0.0.1:9047/lightwallet/broadcast?data=DPDnFCNvPk4m8GMi2ZprirSgQDwxuQw4sWoJA3fmkKDrYwddTPtt1ucFV4i45BHhNEn1W1pxy3zhRfpxKy6fDb5vmvQwwJ3M3E12jyWLBJtHRYPLnRJnK7M2x5MnPbvnePGX1ahqt7PpFwwGiivP1t272YZ9VKWWNUB3Jg6zyt51fCuyDCinLx4awQPQJNHViux9xoGS2c3ph32oi56PKpiyM
	public Response broadcastRaw(@PathParam("raw") String raw)
	{
		
		return broadcastFromRawPost(raw);
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
		List<ItemCls> list = map.get_By_Name(filter);

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
	@Path("personbyaddress/{address}")
	public Response personbyaddress(@PathParam("address") String address) {
		
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
		List<ItemCls> list = map.get_By_Name(filter);

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
