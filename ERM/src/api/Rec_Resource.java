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
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import com.google.common.primitives.Bytes;

import controller.Controller;
import core.TransactionCreator;
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
import core.transaction.TransactionFactory;
import core.transaction.UpdateNameTransaction;
import core.transaction.VoteOnPollTransaction;
import database.DBSet;
import lang.Lang;
import network.Peer;
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
	public String getRecord()
	{
		String text = "";
		JSONArray help = new JSONArray();
		JSONObject item;
		
		text = "SEND_ASSET_TRANSACTION: ";
		text += "type: " + Transaction.SEND_ASSET_TRANSACTION;
		text += ", recipient: Account in Base58";
		text += ", key: AssetKey - long";
		text += ", amount: BigDecimal";
		text += ", head: String UTF-8";
		text += ", data: String in Base58";
		text += ", isText: =0 - not as a TEXT";
		text += ", encryptMessage: =0 - not encrypt";
		item = new JSONObject();
		item.put(Transaction.SEND_ASSET_TRANSACTION, text);
		
		
		help.add(item);
		
		return help.toJSONString();
	}

	// short data without Signature
	@GET
	@Path("/parseshort/")
	public String parseShort() // throws JSONException
	{

		if (uriInfo == null) {
			return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON).toString());
		}
		
		// see http://ru.tmsoftstudio.com/file/page/web-services-java/javax_ws_rs_core.html
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		
		//CREATE TRANSACTION FROM RAW
		Transaction transaction;
		try {
			if (!queryParameters.containsKey("data"))
				return APIUtils.errorMess(-1, "Parameter [data] not found");
			
			String dataStr = queryParameters.get("data").get(0);
			byte[] data = Base58.decode(dataStr);
			data = Bytes.concat(data, new byte[64]);
			transaction = TransactionFactory.getInstance().parse(data, null);
		} catch (Exception e) {
			return APIUtils.errorMess(-1, e.toString());
		}

		return transaction.toJson().toJSONString();
	}

	@GET
	@Path("/parse/")
	public String parse() // throws JSONException
	{

		if (uriInfo == null) {
			return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON).toString());
		}
		
		// see http://ru.tmsoftstudio.com/file/page/web-services-java/javax_ws_rs_core.html
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		
		//CREATE TRANSACTION FROM RAW
		Transaction transaction;
		try {
			if (!queryParameters.containsKey("data"))
				return APIUtils.errorMess(-1, "Parameter [data] not found");
			
			String dataStr = queryParameters.get("data").get(0);
			transaction = TransactionFactory.getInstance().parse(Base58.decode(dataStr), null);
		} catch (Exception e) {
			return APIUtils.errorMess(-1, e.toString());
		}

		return transaction.toJson().toJSONString();
	}

	@GET
	@Path("/getraw/{type}/{creator}")
	//@Consumes(MediaType.APPLICATION_JSON)
	//@Produces("application/json")
	//
	// get record/getraw/31/5mgpEGqUGpfme4W2tHJmG7Ew21Te2zNY7Ju3e9JfUmRF?feePow=2&timestamp=123123243&version=3&recipient=7JS4ywtcqrcVpRyBxfqyToS2XBDeVrdqZL&amount=123.0000123&key=12
	//
	public String toBytes(@PathParam("type") int record_type,
			@PathParam("creator") String creator) // throws JSONException
	{

		if (uriInfo == null) {
			return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON).toString());
		}
		
		// see http://ru.tmsoftstudio.com/file/page/web-services-java/javax_ws_rs_core.html
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(queryParameters);
		} catch (NullPointerException | ClassCastException e) {
			// JSON EXCEPTION
			//LOGGER.info(e);
			return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON).toString());
		} 

		int feePow;
		int version;
		long timestamp;
		long reference;
		
		int steep = 0;
		try {			
			feePow = jsonObject.containsKey("feePow")?Integer.parseInt(((List<String>)jsonObject.get("feePow")).get(0)):0;
			
			steep++;
			version = jsonObject.containsKey("version")?Integer.parseInt(((List<String>)jsonObject.get("version")).get(0)):0;
			
			steep++;
			timestamp = jsonObject.containsKey("timestamp")?Long.parseLong(((List<String>)jsonObject.get("timestamp")).get(0)):NTP.getTime();
			
			steep++;
			reference = jsonObject.containsKey("reference")?Long.parseLong(((List<String>)jsonObject.get("reference")).get(0)):0l;
		} catch (Exception e1) {
			//LOGGER.info(e1);
			return APIUtils.errorMess(-steep, e1.toString() + " on steep: " + steep);
		} 
		

		return toBytes(record_type,	version, feePow, timestamp, creator, reference, queryParameters);

	}

	@GET
	@Path("/getraw/{type}/{version}/{creator}/{timestamp}/{feePow}")
	public String toBytes(@PathParam("type") int record_type,
			@PathParam("version") int version,
			@PathParam("creator") String creator,
			@PathParam("timestamp") long timestamp,
			@PathParam("feePow") int feePow,
			@PathParam("reference") long reference
			) // throws JSONException
	{

		if (uriInfo == null) {
			return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON).toString());
		}
		
		// see http://ru.tmsoftstudio.com/file/page/web-services-java/javax_ws_rs_core.html
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		
		return toBytes(record_type,	version, feePow, timestamp, creator, reference, queryParameters);

	}

	public String toBytes(int record_type,	int version, int feePow, long timestamp, String creator, long reference,
			MultivaluedMap<String, String> queryParameters) // throws JSONException
	{

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(queryParameters);
		} catch (NullPointerException | ClassCastException e) {
			// JSON EXCEPTION
			//LOGGER.info(e);
			//return Response.status(500).entity(ApiErrorFactory.getInstance().createError(
			//		ApiErrorFactory.ERROR_JSON)).build();
			return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON).toString());
		} 

		PublicKeyAccount creatorPK;
		if (!PublicKeyAccount.isValidPublicKey(creator)) {
			return APIUtils.errorMess(Transaction.INVALID_CREATOR,
					ApiErrorFactory.getInstance().createErrorJSON(Transaction.INVALID_CREATOR).toJSONString());				
		}
		creatorPK = new PublicKeyAccount(Base58.decode(creator));

		int steep = 0;

		if (reference == 0) {
			reference = Controller.getInstance().getTransactionCreator().getReference(creatorPK);
		}

		
		try {
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
	
				Account recipient = null;
				long key = 0;
				BigDecimal amount = null;
				String head = null;
				byte[] data = null;
				byte[] isText = null;
				byte[] encryptMessage = null;
				try {
					steep++;
					if (!jsonObject.containsKey("recipient"))
						return ApiErrorFactory.getInstance().createErrorJSON(Transaction.INVALID_ADDRESS).toJSONString();
					String recipientStr = ((List<String>)jsonObject.get("recipient")).get(0);
					Tuple2<Account, String> recipientRes = Account.tryMakeAccount(recipientStr);
					if (recipientRes.b != null) {
						return APIUtils.errorMess(Transaction.INVALID_ADDRESS,
								ApiErrorFactory.getInstance().createErrorJSON(recipientRes.b).toJSONString());					
					}
					recipient = recipientRes.a;
					
					steep++;
					if (jsonObject.containsKey("key"))
						key = Long.parseLong(((List<String>)jsonObject.get("key")).get(0));
					
					steep++;
					if (jsonObject.containsKey("amount"))
						amount = new BigDecimal(((List<String>)jsonObject.get("amount")).get(0)).setScale(8);
					
					steep++;
					if (jsonObject.containsKey("head"))
						head = ((List<String>)jsonObject.get("head")).get(0);
					
					steep++;
					if (jsonObject.containsKey("data"))
						data = Base58.decode(((List<String>)jsonObject.get("data")).get(0));
					
					steep++;
					if (jsonObject.containsKey("isText")
							&& Integer.parseInt(((List<String>)jsonObject.get("isText")).get(0))==0)
						isText = new byte[]{0};
					else
						isText = new byte[]{1};
	
					steep++;
					if (jsonObject.containsKey("encryptMessage")
							&& Integer.parseInt(((List<String>)jsonObject.get("encryptMessage")).get(0)) == 0)
						encryptMessage = new byte[]{0};
					else
						encryptMessage = new byte[]{1};
					
				} catch (Exception e1) {
					//LOGGER.info(e1);
					return APIUtils.errorMess(-steep, e1.toString() + " on steep: " + steep);
				} 
				R_Send record = new R_Send((byte)version, creatorPK,
						(byte)feePow, recipient, key, amount, head,
						data, isText, encryptMessage, timestamp, reference);
	
				/*
				int error = Controller.getInstance().getTransactionCreator().afterCreate(record, false);
				if (error != Transaction.VALIDATE_OK)
					return APIUtils.errorMess(error, gui.transaction.OnDealClick.resultMess(error));
					*/
				
				return Base58.encode(record.toBytes(false, null));
				
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
		} catch (Exception e) {
			//LOGGER.info(e);
			return APIUtils.errorMess(-steep, e.toString() + " on steep: " + steep);
		} 

		return APIUtils.errorMess(Transaction.INVALID_TRANSACTION_TYPE, "Invalid transaction type: " + record_type);
			
	}
	
	@POST
	@Path("/broadcast")
	public String processRecordFromRaw(String rawDataBase58)
	{
		byte[] transactionBytes = Base58.decode(rawDataBase58);
		
		Pair<Transaction, Integer> result = Controller.getInstance().createTransactionFromRaw(transactionBytes);
		if(result.getB() == Transaction.VALIDATE_OK) {
			return result.getA().toJson().toJSONString();
		} else {
			return APIUtils.errorMess(result.getB(), gui.transaction.OnDealClick.resultMess(result.getB()));
		}
	}

}
