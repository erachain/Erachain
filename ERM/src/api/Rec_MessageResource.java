package api;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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

@Path("rec_message")
@Produces(MediaType.APPLICATION_JSON)
public class Rec_MessageResource {

	
	
	private static final Logger LOGGER = Logger
			.getLogger(Rec_MessageResource.class);
	
	@Context
	HttpServletRequest request;

	@POST
	@Consumes(MediaType.WILDCARD)
	public String sendMessage(String x) {
		try {
			// READ JSON
			Tuple3<JSONObject, PrivateKeyAccount, Integer> resultRequet = APIUtils.postPars(request, x);
			
			JSONObject jsonObject = resultRequet.a;
			PrivateKeyAccount sender = resultRequet.b;
			int feePow = resultRequet.c;

			String amount = (String) jsonObject.get("amount");
			String assetKeyString = (String) jsonObject.get("asset");
			String recipient = (String) jsonObject.get("recipient");
			String message = (String) jsonObject.get("message");
			String isTextMessageString = (String) jsonObject
					.get("istextmessage");
			String encryptString = (String) jsonObject.get("encrypt");

			boolean isTextMessage = true;
			if (isTextMessageString != null) {
				isTextMessage = Boolean.valueOf(isTextMessageString);
			}

			long assetKey = 0l;
			if (assetKeyString != null) {
				assetKey = Long.valueOf(assetKeyString);
			}
						
			boolean encrypt = true;
			if (encryptString != null) {
				encrypt = Boolean.valueOf(encryptString);
			}
			
			Name recipientObj = DBSet.getInstance().getNameMap().get(recipient);
			
			if (recipientObj != null) {
				recipient = recipientObj.getOwner().getAddress();
			}
			
			Account recipientAccount = new Account(recipient);

			// PARSE AMOUNT
			BigDecimal bdAmount;
			try {
				if(amount != null) {	
					bdAmount = new BigDecimal(amount);
					bdAmount = bdAmount.setScale(8);
				} else {
					bdAmount = BigDecimal.ZERO.setScale(8);
				}
			} catch (Exception e) {
				throw ApiErrorFactory.getInstance().createError(
						Transaction.INVALID_AMOUNT);
			}

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

			// TODO this is duplicate code -> Send money Panel, we should add
			// that to a common place later
			byte[] messageBytes = null;
			
			if (message != null && message.length() > 0)
			{
				if ( isTextMessage )
				{
					messageBytes = message.getBytes( Charset.forName("UTF-8") );
				}
				else
				{
					try
					{
						messageBytes = Converter.parseHexString( message );
					}
					catch (Exception g)
					{
						try
						{
							messageBytes = Base58.decode(message);
						}
						catch (Exception e)
						{
							LOGGER.error(e.getMessage(),e);
							throw ApiErrorFactory.getInstance().createError(
									ApiErrorFactory.ERROR_MESSAGE_FORMAT_NOT_HEX);
						}
					}
				}
			}

			if (messageBytes.length > 4000) {
				throw ApiErrorFactory.getInstance().createError(
						Transaction.INVALID_DATA_LENGTH);
			}

			// TODO duplicate code -> SendMoneyPanel
			// gui.Send_Panel
			
			if (encrypt) {
				// sender
				byte[] privateKey = sender.getPrivateKey();

				// recipient
				byte[] publicKey = Controller.getInstance()
						.getPublicKeyByAddress(recipient);
				if (publicKey == null) {
					throw ApiErrorFactory.getInstance().createError(
							ApiErrorFactory.ERROR_NO_PUBLIC_KEY);
				}

				messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey,
						publicKey);
			}
			

			byte[] encrypted = (encrypt) ? new byte[] { 1 } : new byte[] { 0 };
			byte[] isTextByte = (isTextMessage) ? new byte[] { 1 }
					: new byte[] { 0 };

			Pair<Transaction, Integer> result = Controller.getInstance()
					.r_Send(sender, feePow,
							recipientAccount, assetKey, bdAmount, messageBytes,
							isTextByte, encrypted);

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
