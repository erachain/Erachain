package api;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Crypto;
import core.naming.Name;
import core.transaction.Transaction;
import database.DBSet;
import ntp.NTP;
import utils.APIUtils;
import utils.Converter;
import utils.Pair;

@Path("statement")
@Produces(MediaType.APPLICATION_JSON)
public class StatementResource {

	
	
	private static final Logger LOGGER = Logger
			.getLogger(StatementResource.class);
	
	@Context
	HttpServletRequest request;

	@POST
	@Consumes(MediaType.WILDCARD)
	public String signNote(String x) {
		try {
			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String password = (String) jsonObject.get("password");
			String noteKeyString = (String) jsonObject.get("note");
			String sender = (String) jsonObject.get("sender");
			String message = (String) jsonObject.get("message");
			String isTextMessageString = (String) jsonObject
					.get("istextmessage");
			String encryptString = (String) jsonObject.get("encrypt");

			boolean isTextMessage = true;
			if (isTextMessageString != null) {
				isTextMessage = Boolean.valueOf(isTextMessageString);
			}

			long noteKey = 0l;
			if (noteKeyString != null) {
				noteKey = Long.valueOf(noteKeyString);
			}
			
			
			boolean encrypt = true;
			if (encryptString != null) {
				encrypt = Boolean.valueOf(encryptString);
			}

			// CHECK ADDRESS
			if (!Crypto.getInstance().isValidAddress(sender)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_SENDER);
			}

			// check this up here to avoid leaking wallet information to remote user
			// full check is later to prompt user with calculated fee
			//APIUtils.disallowRemote(request);
			APIUtils.askAPICallAllowed(password, "POST payment\n" + x, request);


			// CHECK IF WALLET EXISTS
			if (!Controller.getInstance().doesWalletExists()) {
				return ApiErrorFactory.getInstance().createErrorJSON(
						ApiErrorFactory.ERROR_WALLET_NO_EXISTS).toJSONString();
			}

			// CHECK WALLET UNLOCKED
			if (!Controller.getInstance().isWalletUnlocked()) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_WALLET_LOCKED);
			}

			// GET ACCOUNT
			PrivateKeyAccount account = Controller.getInstance()
					.getPrivateKeyAccountByAddress(sender);
			if (account == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_SENDER);
			}

			// TODO this is duplicate code -> Send money Panel, we should add
			// that to a common place later
			byte[] messageBytes;
			if (isTextMessage) {
				messageBytes = message.getBytes(StandardCharsets.UTF_8);
			} else {
				try {
					messageBytes = Converter.parseHexString(message);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(),e);
					throw ApiErrorFactory.getInstance().createError(
							ApiErrorFactory.ERROR_MESSAGE_FORMAT_NOT_HEX);
				}
			}

			if (messageBytes.length > 4000) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_MESSAGESIZE_EXCEEDED);
			}

			// TODO duplicate code -> SendMoneyPanel
			if (encrypt) {
				// sender
				PrivateKeyAccount pkAccount = Controller.getInstance()
						.getPrivateKeyAccountByAddress(sender);
				byte[] privateKey = pkAccount.getPrivateKey();

				messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey,
						pkAccount.getPublicKey());
			}
			

			//APIUtils.askAPICallAllowed("POST message\n" + x + "\n Fee Power: "+ bdFee.toPlainString(), request);

			byte[] encrypted = (encrypt) ? new byte[] { 1 } : new byte[] { 0 };
			byte[] isTextByte = (isTextMessage) ? new byte[] { 1 }
					: new byte[] { 0 };

			Pair<Transaction, Integer> result = Controller.getInstance()
					.signNote(false,
							Controller.getInstance()
									.getPrivateKeyAccountByAddress(sender),
							0, noteKey, messageBytes,
							isTextByte, encrypted);

			if (result.getB() == Transaction.VALIDATE_OK)
				return result.getA().toJson().toJSONString();
			else {

				throw ApiErrorFactory.getInstance().createError(result.getB());

			}
		}
		catch(NullPointerException| ClassCastException e)
		{
			//JSON EXCEPTION
			LOGGER.info(e);
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}
}
