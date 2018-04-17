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
import javax.ws.rs.POST;
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
import org.json.simple.JSONValue;

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
		help.put("apitelegrams/getbysignature/{signature}", "Get Telegramm by signature");
		help.put("apitelegrams/get?address={address}&timestamp={timestamp}&filter={filter}",
				"Get messages by filter. Filter is title.");

		return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
	}

	/**
	 * @author Ruslan
	 * @param signature
	 *            is signature message
	 * @return telegram
	 */
	@GET
	@Path("getbysignature/{signature}")
	// GET
	// telegrams/getbysignature/6kdJgbiTxtqFt2zQDz9Lb29Z11Fa1TSwfZvjU21j6Cn9umSUEK4jXmNU19Ww4RcXpFyQiJTCaSz6Lc5YKn26hsR
	public Response getTelegramBySignature(@PathParam("signature") String signature) throws Exception {

		// DECODE SIGNATURE
		@SuppressWarnings("unused")
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

		return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(telegram.toJson().toJSONString())).build();
	}

	/**
	 * @author Ruslan
	 * @return json string all find message by filter
	 * @param address
	 *            account user
	 * @param timestamp
	 *            value time
	 * @param filter
	 *            is title message.
	 */
	@SuppressWarnings("unchecked")
	@GET
	@Path("get")
	public Response getTelegramsTimestamp(@QueryParam("address") String address, @QueryParam("timestamp") int timestamp, @QueryParam("filter") String filter) {

		// CHECK ADDRESS
		if (!Crypto.getInstance().isValidAddress(address)) {
			throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
		}

		JSONArray array = new JSONArray();
		for (TelegramMessage telegram : Controller.getInstance().getLastTelegrams(address, timestamp, filter)) {
			array.add(telegram.toJson());
		}

		return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(array.toJSONString())).build();
	}

}