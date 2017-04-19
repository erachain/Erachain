package api;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
	@Path("/parsetest/")
	// for test raw without Signature
	// http://127.0.0.1:9068/record/parsetest?data=3RKre8zCEarLNq4CQ6njRmvjGURz7KFWhec3H9H3tebEeKQEGDTsvAFizKnFpJAGDAoRQCKH9pygBQsrWfbxwgfcuEAKbARh5p6Yk2ZvfJDReFzBJbUSUwUgtxsKm2ZXHR
	//
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
			int cut = 53;
			byte[] dataTail = Arrays.copyOfRange(data, cut, data.length);
			data = Bytes.concat(Arrays.copyOfRange(data, 0, cut), new byte[64], dataTail);
			transaction = TransactionFactory.getInstance().parse(data, null);
			JSONObject json = transaction.toJson();
			json.put("raw", Base58.encode(data));
			return json.toJSONString();
		} catch (Exception e) {
			return APIUtils.errorMess(-1, e.toString());
		}

	}

	@GET
	@Path("/parse/")
	// http://127.0.0.1:9068/record/parse?data=DPDnFCNvPk4kLMQcyEp8wTmzT53vcFpVPVhBA8VuHDH6ekAWJAEgZvtjtKGcXwsAKyNs5k2aCpziAmqEDjTigbnDjMeXRfbUDUJNmEJHwB2uPdboSszwsy3fckANUgPV8Ep9CN1fdTdq3QfYE7bbpeYWS2rsTNHb3a7nEV6jg2XJguavqhNSzVeyM6UrRtbiVciMvHFayUAMrE4L3CPjZjPEf
	//
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
	// http://127.0.0.1:9068/record/getraw/31/5mgpEGqUGpfme4W2tHJmG7Ew21Te2zNY7Ju3e9JfUmRF?feePow=2&recipient=77QnJnSbS9EeGBa2LPZFZKVwjPwzeAxjmy&amount=123.0000123&key=1
	//
	public String getRaw(@PathParam("type") int record_type,
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
		

		return webserver.LightWallet.toBytes(record_type,	version, feePow, timestamp, creator, reference, queryParameters);

	}

	@GET
	@Path("/getraw/{type}/{version}/{creator}/{timestamp}/{feePow}")
	public String getRaw(@PathParam("type") int record_type,
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
		
		return webserver.LightWallet.toBytes(record_type, version, feePow, timestamp, creator, reference, queryParameters);

	}
	
	@POST
	@Path("/broadcast")
	public String broadcastFromRaw(String rawDataBase58)
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
