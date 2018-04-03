package webserver;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.mapdb.Fun.Tuple2;

import com.twitter.Extractor.Entity;

import api.ApiErrorFactory;
import controller.Controller;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import core.BlockChain;
import core.BlockGenerator;
import core.BlockGenerator.ForgingStatus;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.crypto.Base64;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.transaction.R_Send;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.PasswordPane;
import gui.library.Issue_Confirm_Dialog;
import gui.library.library;
import gui.models.TransactionsTableModel;
import gui.transaction.OnDealClick;
import gui.transaction.Send_RecordDetailsFrame;
import lang.Lang;
import network.message.TelegramMessage;
import utils.APIUtils;
import utils.Converter;
import utils.Pair;
import utils.StrJSonFine;
import utils.TransactionTimestampComparator;

@Path("apitelegrams")
@Produces(MediaType.APPLICATION_JSON)
public class API_TelegramsResource {

	static Logger LOGGER = Logger.getLogger(API_TelegramsResource.class.getName());

	@Context
	HttpServletRequest request;

	private static int count_step;

	private static boolean run_test = false;

	protected String tt;

	private static Thread thread;
	private static int sleep;
	private static BigDecimal amm;
	private static byte[] mes;

	@GET
	public Response Default() {

		Map<String, String> help = new LinkedHashMap<String, String>();

		help.put("apitelegrams/getbyaddress?address={address}&asset={asset}",
				Lang.getInstance().translate("Get all Records for Address & Asset Key"));
		help.put(
				"apitelegrams/getbyaddressfromtransactionlimit?address={address}&asset={asset}&start={start record}&end={end record}&type={type Transaction}&sort={des/asc}",
				Lang.getInstance().translate("Get all Records for Address & Asset Key from Start to End"));
		help.put("apitelegrams/getbyblock?block={block}", Lang.getInstance().translate("Get all Records from Block"));

		help.put("apitelegrams/getlastbyaddress?address={address}&timestamp={Timestamp}&limit={Limit}",
				"Get last Records from Unix Timestamp milisec(1512777600000)");
		
		help.put("apitelegrams/getTelegramslimited?timestamp={timestamp}", "Get last message by timestamp");
		
		help.put("apitelegrams/getTelegramBySignature?signature={signature}", "Get Telegramm signature");

		help.put("apitelegrams/getTelegramsTimestamp?address={adress}&timestamp={timestamp}", "Get last message by assress and timestamp");
		help.put("apitelegrams/send?sender={sender}&recipient={recipient}", "Send message from sender to recipient");
		
		help.put("apitelegrams/timestamp?timestamp={timestamp}", "Get last message");
		
		help.put("apitelegrams/datadecrypt?signature={signature}", "Decypt data by signature");
				
		return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("timestamp/{timestamp}")
	public Response getTelegramsLimited(@PathParam("timestamp") int timestamp) {
		///String password = null;
		/// APIUtils.askAPICallAllowed(password, "GET telegrams/timestamp/" +
		/// timestamp, request);

		// CHECK IF WALLET EXISTS
		if (!Controller.getInstance().doesWalletExists()) {
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}

		// CREATE JSON OBJECT
		JSONArray array = new JSONArray();

		for (TelegramMessage telegram : Controller.getInstance().getLastTelegrams(timestamp)) {
			array.add(telegram.toJson());
		}

		return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(array.toJSONString())).build();

	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("getbyaddress")
	public Response getByAddress(@QueryParam("address") String address, @QueryParam("asset") String asset) {
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
			if (asset != null) {
				if (transaction.getAbsKey() == new Long(asset)) {
					array.add(transaction.toJson());
				}
			} else {
				array.add(transaction.toJson());
			}
		}
		// json.put("transactions", array);
		///return array.toJSONString();
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

	// "apitelegrams/getlastbyaddress?address={address}&timestamp={Timestamp}&limit={Limit}"
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

	@GET
	@Path("get/{signature}")
	// GET
	// telegrams/get/6kdJgbiTxtqFt2zQDz9Lb29Z11Fa1TSwfZvjU21j6Cn9umSUEK4jXmNU19Ww4RcXpFyQiJTCaSz6Lc5YKn26hsR
	public Response getTelegramBySignature(@PathParam("signature") String signature) throws Exception {

		/// String password = null;
		/// APIUtils.askAPICallAllowed(password, "GET telegrams/get/"
		/// +signature, request);

		// DECODE SIGNATURE
		byte[] signatureBytes;
		try {
			signatureBytes = Base58.decode(signature);
		} catch (Exception e) {
			throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
		}

		// GET TELEGRAM\
		TelegramMessage telegram = Controller.getInstance().getTelegram(signature);

		// CHECK IF TELEGRAM EXISTS
		if (telegram == null) {
			throw ApiErrorFactory.getInstance().createError(Transaction.TELEGRAM_DOES_NOT_EXIST);
		}

		// return telegram.toJson().toJSONString();

		return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(telegram.toJson().toJSONString())).build();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("address/{address}/timestamp/{timestamp}")
	public Response getTelegramsTimestamp(@PathParam("address") String address, @PathParam("timestamp") int timestamp) {
		//String password = null;
		// APIUtils.askAPICallAllowed(password, "GET telegrams/address/" +
		// address + "/timestamp/" + timestamp, request);

		// CHECK IF WALLET EXISTS
		if (!Controller.getInstance().doesWalletExists()) {
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}

		// CHECK ADDRESS
		if (!Crypto.getInstance().isValidAddress(address)) {
			throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
		}

		// CHECK ACCOUNT IN WALLET
		Account account = Controller.getInstance().getAccountByAddress(address);
		if (account == null) {
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
		}

		JSONArray array = new JSONArray();
		for (TelegramMessage telegram : Controller.getInstance().getLastTelegrams(account, timestamp)) {
			array.add(telegram.toJson());
		}

		return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(array.toJSONString())).build();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("send")
	public Response send(@QueryParam("sender") String sender1, @QueryParam("recipient") String recipient1,
			@QueryParam("asset") long asset1, @QueryParam("amount") String amount1, @QueryParam("title") String title1,
			@QueryParam("message") String message1, @QueryParam("istextmessage") boolean istextmessage,
			@QueryParam("encrypt") boolean encrypt, @QueryParam("callback") String callback,
			@QueryParam("password") String password) {

		// APIUtils.askAPICallAllowed(password, "POST telegrams/send", request);

		JSONObject out = new JSONObject();
		Controller cntr = Controller.getInstance();

		// READ SENDER
		Account sender;
		try {
			sender = new Account(sender1);
			if (sender.getAddress() == null)
				throw new Exception("");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			out.put("status_code", Transaction.INVALID_CREATOR);
			out.put("status", "Invalid Senser");
			// return out.toJSONString();

			return Response.status(200).header("Context-Type", "application/json; charset=utf-8")
					.header("Access-Conrol-Allow-Origin", "*").entity(StrJSonFine.convert(out.toJSONString())).build();
		}

		// READ RECIPIENT
		Account recip;
		try {
			recip = new Account(recipient1);
			if (recip.getAddress() == null)
				throw new Exception("");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
			out.put("status_code", Transaction.INVALID_ADDRESS);
			out.put("status", "Invalid Recipient Address");
			// return out.toJSONString();
			return Response.status(200).header("Context-Type", "application/json; charset=utf-8")
					.header("Access-Conrol-Allow-Origin", "*").entity(StrJSonFine.convert(out.toJSONString())).build();

		}
		BigDecimal amount;
		// READ AMOUNT
		try {
			amount = new BigDecimal(amount1).setScale(8);
			if (amount.equals(new BigDecimal("0.0").setScale(8)))
				throw new Exception("");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			out.put("status_code", Transaction.INVALID_AMOUNT);
			out.put("status", "Invalid Amount");
			// return out.toJSONString();
			return Response.status(200).header("Context-Type", "application/json; charset=utf-8")
					.header("Access-Conrol-Allow-Origin", "*").entity(StrJSonFine.convert(out.toJSONString())).build();

		}
		String message;
		try {
			message = message1;
			if (message != null) {
				if (message.length() > BlockChain.MAX_REC_DATA_BYTES)
					throw new Exception("");
			} else {
				message = "";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// e.printStackTrace();
			out.put("status_code", Transaction.INVALID_DESCRIPTION_LENGTH);
			out.put("status", "Invalid message");
			// return out.toJSONString();
			return Response.status(200).header("Context-Type", "application/json; charset=utf-8")
					.header("Access-Conrol-Allow-Origin", "*").entity(StrJSonFine.convert(out.toJSONString())).build();

		}
		byte[] encrypted = new byte[] { 0 };
		byte[] isTextByte = new byte[] { 1 };

		// asset
		try {
			AssetCls asset = cntr.getAsset(asset1);
			if (asset == null)
				throw new Exception("");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			out.put("status_code", 1000000);
			out.put("status", "Invalid asset");
			// return out.toJSONString();
			return Response.status(200).header("Context-Type", "application/json; charset=utf-8")
					.header("Access-Conrol-Allow-Origin", "*").entity(StrJSonFine.convert(out.toJSONString())).build();

		}
		// title
		String head;
		try {
			head = title1;
			if (head == null)
				head = "";
			if (head.getBytes(StandardCharsets.UTF_8).length > 256)
				throw new Exception("");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			out.put("status_code", Transaction.INVALID_HEAD_LENGTH);
			out.put("status", "Invalid Title");
			// return out.toJSONString();
			return Response.status(200).header("Context-Type", "application/json; charset=utf-8")
					.header("Access-Conrol-Allow-Origin", "*").entity(StrJSonFine.convert(out.toJSONString())).build();
		}

		// CREATE TX MESSAGE
		Transaction transaction;
		PrivateKeyAccount account = cntr.getPrivateKeyAccountByAddress(sender.getAddress());
		if (account == null) {
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
		}

		try {
			transaction = cntr.r_Send(account, 0, recip, asset1, amount, head,
					message.getBytes(Charset.forName("UTF-8")), isTextByte, encrypted);
			if (transaction == null)
				throw new Exception("transaction == null");
		} catch (Exception e) {
			out.put("status_code", Transaction.INVALID_TRANSACTION_TYPE);
			out.put("status", "Invalid Transaction");
			// return out.toJSONString();
			return Response.status(200).header("Context-Type", "application/json; charset=utf-8")
					.header("Access-Conrol-Allow-Origin", "*").entity(StrJSonFine.convert(out.toJSONString())).build();
		}

		cntr.broadcastTelegram(transaction, callback, true);

		out.put("signature", Base58.encode(transaction.getSignature()));
		// return out.toJSONString();
		return Response.status(200).header("Context-Type", "application/json; charset=utf-8")
				.header("Access-Conrol-Allow-Origin", "*").entity(StrJSonFine.convert(out.toJSONString())).build();

		// return Response.status(200).header("Content-Type", "application/json;
		// charset=utf-8")
		// .header("Access-Control-Allow-Origin",
		// "*").entity(StrJSonFine.convert(array.toJSONString())).build();
	}

	// GET
	// telegrams/datadecrypt/GerrwwEJ9Ja8gZnzLrx8zdU53b7jhQjeUfVKoUAp1StCDSFP9wuyyqYSkoUhXNa8ysoTdUuFHvwiCbwarKhhBg5?password=1
	@GET
	// @Produces("text/plain")
	@Path("datadecrypt/{signature}")
	public Response dataDecrypt(@PathParam("signature") String signature, @QueryParam("password") String password) {

		byte[] signatureBytes;
		try {
			signatureBytes = Base58.decode(signature);
		} catch (Exception e) {
			throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
		}

		// GET TELEGRAM
		TelegramMessage telegram = Controller.getInstance().getTelegram(signature);

		// CHECK IF TELEGRAM EXISTS
		if (telegram == null) {
			throw ApiErrorFactory.getInstance().createError(Transaction.TRANSACTION_DOES_NOT_EXIST);
		}

		R_Send r_Send = (R_Send) telegram.getTransaction();
		byte[] r_data = r_Send.getData();
		if (r_data == null || r_data.length == 0)
			return null;

		// APIUtils.askAPICallAllowed(password, "POST decrypt telegram data\n "
		// + signature, request);

		byte[] ddd = Controller.getInstance().decrypt(r_Send.getCreator(), r_Send.getRecipient(), r_data);
		if (ddd == null) {
			// return "wrong decryption";
			return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert("wrong decryption")).build();
		}

		if (r_Send.isText()) {
			try {
				String str = (new String(ddd, "UTF-8"));
				// return str;
				return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
						.header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(str)).build();

			} catch (UnsupportedEncodingException e) {
				// return "error UTF-8";

				return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
						.header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert("error UTF-8")).build();
			}
		} else {
			String str = Base58.encode(ddd);
			// return str;
			return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(str)).build();
		}
	}

}