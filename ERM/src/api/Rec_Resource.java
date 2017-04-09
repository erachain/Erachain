package api;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
import org.mapdb.Fun.Tuple3;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
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
import core.transaction.UpdateNameTransaction;
import core.transaction.VoteOnPollTransaction;
import database.DBSet;
import lang.Lang;
import ntp.NTP;
import utils.APIUtils;
import utils.Converter;
import utils.Pair;

@Path("record")
@Produces(MediaType.APPLICATION_JSON)
public class Rec_Resource {
	
    @Context
    private UriInfo uriInfo;
    
	private static final Logger LOGGER = Logger
			.getLogger(Rec_Resource.class);
	
	@Context
	HttpServletRequest request;

	/*@GET here defines, this method will process HTTP GET requests. */
	@GET
	@Path("/tobytes/{type}/{creator}/")
	//@Consumes(MediaType.APPLICATION_JSON)
	//@Produces("application/json")
	public String toBytes(@PathParam("type") int record_type,
			@PathParam("creator") String creatorStr) // throws JSONException
	{

		if (uriInfo == null) {
			return ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON).toString();
		}
		
		// see http://ru.tmsoftstudio.com/file/page/web-services-java/javax_ws_rs_core.html
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(queryParameters);
		} catch (NullPointerException | ClassCastException e) {
			// JSON EXCEPTION
			LOGGER.info(e);
			//return Response.status(500).entity(ApiErrorFactory.getInstance().createError(
			//		ApiErrorFactory.ERROR_JSON)).build();
			return ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON).toString();
		} 

		byte feePow;
		byte version;
		long timestamp;
		PublicKeyAccount creator;
		
		try {
			feePow = jsonObject.containsKey("version")?(byte)jsonObject.get("version"):(byte)0;
			version = jsonObject.containsKey("version")?(byte)jsonObject.get("version"):(byte)0;
			timestamp = jsonObject.containsKey("timestamp")?(long)jsonObject.get("timestamp"):NTP.getTime();
			creator = new PublicKeyAccount(Base58.decode(creatorStr));
		} catch (Exception e1) {
			LOGGER.info(e1);
			return e1.toString();
		} 
		
		DBSet dbSet = DBSet.getInstance();
		long releaserReference = creator.getLastReference(dbSet);
				
		switch(record_type)
		{
		case Transaction.SIGN_NOTE_TRANSACTION:
			
			//PARSE PAYMENT TRANSACTION
			//return R_SignNote.Parse(data, releaserReference);
		
		case Transaction.REGISTER_NAME_TRANSACTION:
			
			//PARSE REGISTER NAME TRANSACTION
			//return RegisterNameTransaction.Parse(data);
			
		case Transaction.UPDATE_NAME_TRANSACTION:
			
			//PARSE UPDATE NAME TRANSACTION
			//return UpdateNameTransaction.Parse(data);
			
		case Transaction.SELL_NAME_TRANSACTION:
			
			//PARSE SELL NAME TRANSACTION
			//return SellNameTransaction.Parse(data);
			
		case Transaction.CANCEL_SELL_NAME_TRANSACTION:
			
			//PARSE CANCEL SELL NAME TRANSACTION
			//return CancelSellNameTransaction.Parse(data);
			
		case Transaction.BUY_NAME_TRANSACTION:
			
			//PARSE CANCEL SELL NAME TRANSACTION
			//return BuyNameTransaction.Parse(data);	
			
		case Transaction.CREATE_POLL_TRANSACTION:
			
			//PARSE CREATE POLL TRANSACTION
			//return CreatePollTransaction.Parse(data);	
			
		case Transaction.VOTE_ON_POLL_TRANSACTION:
			
			//PARSE CREATE POLL VOTE
			//return VoteOnPollTransaction.Parse(data, releaserReference);		
			
		case Transaction.ARBITRARY_TRANSACTION:
			
			//PARSE ARBITRARY TRANSACTION
			//return ArbitraryTransaction.Parse(data);			
					
		case Transaction.CREATE_ORDER_TRANSACTION:
			
			//PARSE ORDER CREATION TRANSACTION
			//return CreateOrderTransaction.Parse(data, releaserReference);	
			
		case Transaction.CANCEL_ORDER_TRANSACTION:
			
			//PARSE ORDER CANCEL
			//return CancelOrderTransaction.Parse(data, releaserReference);	
			
		case Transaction.MULTI_PAYMENT_TRANSACTION:
			
			//PARSE MULTI PAYMENT
			//return MultiPaymentTransaction.Parse(data, releaserReference);		
		
		case Transaction.DEPLOY_AT_TRANSACTION:
			//return DeployATTransaction.Parse(data);

		case Transaction.SEND_ASSET_TRANSACTION:

			Account recipient;
			long key;
			BigDecimal amount;
			String head;
			byte[] data;
			byte[] isText;
			byte[] encryptMessage;
			try {
				recipient = new Account((String)jsonObject.get("recipient"));
				key = (long)jsonObject.get("key");
				amount = (BigDecimal)jsonObject.get("amount");
				head = (String)jsonObject.get("head");
				data = Base58.decode((String)jsonObject.get("data"));
				if (jsonObject.containsKey("isText") && (int)jsonObject.get("isText")==0)
					isText = new byte[]{0};
				else
					isText = new byte[]{1};
				if (jsonObject.containsKey("encryptMessage") && (int)jsonObject.get("encryptMessage") == 0)
					encryptMessage = new byte[]{0};
				else
					encryptMessage = new byte[]{1};
				
			} catch (Exception e1) {
				LOGGER.info(e1);
				return e1.toString();
			} 
			R_Send record = new R_Send(version, creator,
					(byte)feePow, recipient, key, amount, head,
					data, isText, encryptMessage, timestamp, releaserReference);

			int error = record.isValid(dbSet, releaserReference);
			if (error > 0)
				return "error - " + error;
			
			return Base58.encode(record.toBytes(false, releaserReference));
			
		case Transaction.HASHES_RECORD:

			// PARSE ACCOUNTING TRANSACTION V3
			//return R_Hashes.Parse(data, releaserReference);
				
		case Transaction.VOUCH_TRANSACTION:
			
			//PARSE CERTIFY PERSON TRANSACTION
			//return R_Vouch.Parse(data, releaserReference);

		case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:
			
			//PARSE CERTIFY PERSON TRANSACTION
			//return R_SetStatusToItem.Parse(data, releaserReference);
			
		case Transaction.SET_UNION_TO_ITEM_TRANSACTION:
			
			//PARSE CERTIFY PERSON TRANSACTION
			//return R_SetUnionToItem.Parse(data, releaserReference);

		case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:
			
			//PARSE CERTIFY PERSON TRANSACTION
			//return R_SertifyPubKeys.Parse(data, releaserReference);			
			
		case Transaction.ISSUE_ASSET_TRANSACTION:
			
			//PARSE ISSUE ASSET TRANSACTION
			//return IssueAssetTransaction.Parse(data, releaserReference);
			
		case Transaction.ISSUE_IMPRINT_TRANSACTION:
			
			//PARSE ISSUE IMPRINT TRANSACTION
			//return IssueImprintRecord.Parse(data, releaserReference);

		case Transaction.ISSUE_NOTE_TRANSACTION:
			
			//PARSE ISSUE NOTE TRANSACTION
			//return IssueNoteRecord.Parse(data, releaserReference);

		case Transaction.ISSUE_PERSON_TRANSACTION:
			
			//PARSE ISSUE PERSON TRANSACTION
			//return IssuePersonRecord.Parse(data, releaserReference);

		case Transaction.ISSUE_STATUS_TRANSACTION:
			
			//PARSE ISSUE NOTE TRANSACTION
			//return IssueStatusRecord.Parse(data, releaserReference);

		case Transaction.ISSUE_UNION_TRANSACTION:
			
			//PARSE ISSUE NOTE TRANSACTION
			//return IssueUnionRecord.Parse(data, releaserReference);
			
		case Transaction.GENESIS_SEND_ASSET_TRANSACTION:
			
			//PARSE TRANSFER ASSET TRANSACTION
			//return GenesisTransferAssetTransaction.Parse(data);	
		
		case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:
			
			//PARSE ISSUE PERSON TRANSACTION
			//return GenesisIssuePersonRecord.Parse(data);

		case Transaction.GENESIS_ISSUE_NOTE_TRANSACTION:
			
			//PARSE ISSUE NOTE TRANSACTION
			//return GenesisIssueNoteRecord.Parse(data);

		case Transaction.GENESIS_ISSUE_STATUS_TRANSACTION:
			
			//PARSE ISSUE STATUS TRANSACTION
			//return GenesisIssueStatusRecord.Parse(data);

		case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:
			
			//PARSE GENESIS TRANSACTION
			//return GenesisIssueAssetTransaction.Parse(data);
			
		}

		return "{ \"error\": \"Invalid transaction type: " + record_type + "\" }";
			
	}
	
	//@GET // from browser - not work X parameters
	@POST // from curl only
	@Consumes(MediaType.WILDCARD)
	
	// http://127.0.0.1:9085/rec_hashes?sender=78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5&feePow=0&password=1&url=123
	@SuppressWarnings("unchecked")
	public String toBytes1(String x) {
		try {

			// READ JSON
			Tuple3<JSONObject, PrivateKeyAccount, Integer> resultRequet = APIUtils.postPars(request, x);
			
			JSONObject jsonObject =  (JSONObject) JSONValue.parse(x);
			PrivateKeyAccount maker = resultRequet.b;
			int feePow = resultRequet.c;

			String url = (String) jsonObject.get("url");
			String data = (String) jsonObject.get("data");
			String hashesStr = (String) jsonObject.get("hashes"); // :"12312 12123 234234"
			
			
			if (false) {
				JSONObject json_result = new JSONObject();
				json_result.put("error", "twin hashes");
				JSONArray twins_array = new JSONArray();
				//twins_array.addAll(twins);
				json_result.put("twins", twins_array);
				
				return json_result.toJSONString();
			}

			String hashes = "";
			Pair<Transaction, Integer> result = Controller.getInstance()
					.r_Hashes(maker, feePow,
							url, data, hashes);

			if (result.getB() == Transaction.VALIDATE_OK) {
				//return result.getA().toJson().toJSONString();
				JSONObject json_result = new JSONObject();
				String b58 = Base58.encode(result.getA().getSignature());
				json_result.put("signature", b58);
				
				return json_result.toJSONString();
			}
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
