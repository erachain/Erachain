package api;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple3;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.naming.Name;
import core.transaction.Transaction;
import database.DBSet;
import lang.Lang;
import ntp.NTP;
import utils.APIUtils;
import utils.Converter;
import utils.Pair;

@Path("rec_hashes")
@Produces(MediaType.APPLICATION_JSON)
public class Rec_HashesResource {

	
	
	private static final Logger LOGGER = Logger
			.getLogger(Rec_HashesResource.class);
	
	@Context
	HttpServletRequest request;

	//@GET // from browser - not work X parameters
	@POST // from curl only
	@Consumes(MediaType.WILDCARD)
	
	// http://127.0.0.1:9085/rec_hashes?sender=78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5&feePow=0&password=1&url=123
	public String hashes(String x) {
		try {

			// READ JSON
			Tuple3<JSONObject, PrivateKeyAccount, Integer> resultRequet = APIUtils.postPars(request, x);
			
			JSONObject jsonObject = resultRequet.a;
			PrivateKeyAccount maker = resultRequet.b;
			int feePow = resultRequet.c;

			String url = (String) jsonObject.get("url");
			String data = (String) jsonObject.get("data");
			String hashes = (String) jsonObject.get("hashes"); // :"12312 12123 234234"
			
			/*
			String isTextMessageString = (String) jsonObject
					.get("istextmessage");
			String encryptString = (String) jsonObject.get("encrypt");

			boolean isTextMessage = true;
			if (isTextMessageString != null) {
				isTextMessage = Boolean.valueOf(isTextMessageString);
			}
						
			boolean encrypt = true;
			if (encryptString != null) {
				encrypt = Boolean.valueOf(encryptString);
			}
			*/
			
			// TODO this is duplicate code -> Send money Panel, we should add
			// check this up here to avoid leaking wallet information to remote user
			// full check is later to prompt user with calculated fee
			APIUtils.disallowRemote(request);

			// CHECK IF WALLET EXISTS
			if (!Controller.getInstance().doesWalletExists()) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
			}

			// CHECK WALLET UNLOCKED
			if (!Controller.getInstance().isWalletUnlocked()) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_WALLET_LOCKED);
			}

			if (true) {
				return hashes;
			}
			Pair<Transaction, Integer> result = Controller.getInstance()
					.r_Hashes(maker, feePow,
							url, data, hashes);

			if (result.getB() == Transaction.VALIDATE_OK)
				return result.getA().toJson().toJSONString();
			else
				throw ApiErrorFactory.getInstance().createError(result.getB());

		} catch (NullPointerException | ClassCastException e) {
			// JSON EXCEPTION
			LOGGER.info(e);
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		} 
	}
}
