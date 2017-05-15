package webserver;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

		help.put("height", "height");
		help.put("BLOCK", "");
		help.put("First Block", "firstblock");
		help.put("Last Block", "lastblock");
		help.put("Block", "block/{signature}");
		help.put("Block by Height", "blockbyheight/{height}");
		help.put("Child Block Signature", "childblocksignature/{signature}");
		help.put("Child Block", "childblock/{signature}");
		help.put("BLOCKS", "");
		help.put("Blocks from Height by Limit (end:1 if END is reached)", "blocksfromheight/{height}/{limit}");
		help.put("Blocks Signatures from Height by Limit (end:1 if END id reached)", "/blockssignaturesfromheight/{height}/{limit}");		
		help.put("RECORD", "");
		help.put("Record", "record/{signature}");
		help.put("Record by Height and Sequence", "recordbynumber/{height-sequence}");
		
		
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

}
