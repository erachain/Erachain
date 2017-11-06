package webserver;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.mapdb.Fun.Tuple2;

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
import utils.Converter;
import utils.StrJSonFine;

@Path("apirecords")
@Produces(MediaType.APPLICATION_JSON)
public class API_Test {

	static Logger LOGGER = Logger.getLogger(API_Test.class.getName());
	
	@Context
	HttpServletRequest request;

	private static int count_step;

	private static boolean run_test = false;

	protected String tt;

	private static Thread thread;
	private static int sleep;
	private static  BigDecimal amm;
	private static byte[] mes;

	@GET
	public Response Default() {

		Map<String, String> help = new LinkedHashMap<String, String>();

		help.put("apirecords/getbyaddress?address={address}&asset={asset}",
				Lang.getInstance().translate("Get all Records for Address & Asset Key"));
		help.put(
				"apirecords/getbyaddressfromtransactionlimit?address={address}&asset={asset}&start={start record}&end={end record}&type={type Transaction}&sort={des/asc}",
				Lang.getInstance().translate("Get all Records for Address & Asset Key from Start to End"));
		help.put("apirecords/getbyblock?block={block}", Lang.getInstance().translate("Get all Records from Block"));

		if (BlockChain.DEVELOP_USE) {
			help.put("apirecords/setnd_assets_era?sender={address}&recipient={address}&ammount=0.0001&message=sadasdasdsda&count=10&sleep=200", "sends ERA");
			
			help.put("apirecords/setnd_mails?sender={address}&recipient={address}&message=sadasdasdsda&count=10&sleep=200", "sends Mails");
			
			help.put("apirecords/getstatus", "GET Nenwork Status");
			
			help.put("apirecords/runtransactions?message={message}&sleep={1000}&ammount={0.0001}", "Start send transaction to 1 сек");
			
			help.put("apirecords/stoptransaction", "Stop to Start comand");
			
			help.put("apirecords/getcount", "get count Transactions");
			help.put("apirecords/erasecount", "erase count Transactions");
			
			help.put("apirecords/forgingstart","Forging Start");
			help.put("apirecords/forgingend", "Forging End");
		}
		
		
		
		
		return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
	
		
	
		
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("getbyaddress")
	public String getByAddress(@QueryParam("address") String address, @QueryParam("asset") String asset) {
		List<Transaction> result;
		if (address == null || address.equals("")) {
			JSONObject ff = new JSONObject();
			ff.put("Error", "Invalid Address");
			return ff.toJSONString();
		}
		// TransactionsTableModel a = new TransactionsTableModel();
		// a.Find_Transactions_from_Address(address);
		// result =a.getTransactions();
		result = DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddress(address);

		if (result == null) {
			JSONObject ff = new JSONObject();
			ff.put("message", "null");
			return ff.toJSONString();
		}
		;

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
		return array.toJSONString();

	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("getbyaddressfromtransactionlimit")
	public String getByAddressLimit(@QueryParam("address") String address, @QueryParam("asset") String asset,
			@QueryParam("start") long start, @QueryParam("end") long end, @QueryParam("type") String type1,
			@QueryParam("sort") String sort) {
		List<Transaction> result;

		if (address == null || address.equals("")) {
			JSONObject ff = new JSONObject();
			ff.put("Error", "Invalid Address");
			return ff.toJSONString();
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
			return ff.toJSONString();
		}
		;

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
		return new JSONObject(k_Map.subMap(start, end)).toJSONString();

	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("getbyblock")
	public String getByBlock(@QueryParam("block") String block) {
		JSONObject ff = new JSONObject();
		List<Transaction> result;

		TransactionsTableModel a = new TransactionsTableModel();
		a.setBlockNumber(block);
		result = a.getTransactions();
		if (result == null || result.size() == 0) {

			ff.put("message", "null");
			return ff.toJSONString();
		}
		;

		JSONArray array = new JSONArray();
		for (Transaction trans : result) {

			array.add(trans.toJson());
		}
		// json.put("transactions", array);
		return array.toJSONString();

	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("find")
	public String getTransactionsFind(@QueryParam("address") String address, @QueryParam("sender") String sender,
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
		return array.toJSONString();
	}

	
	
	//createtransactions?recipient={address}&ammount=0.0001&asset=1&message=sadasdasdsda&count=10000&sleep=200
	@SuppressWarnings("unchecked")
	@GET
	@Path("setnd_assets_era")
	public Response createTransactions(
			// @QueryParam("sender") String sender,
			@QueryParam("sender") String sender_url,
			@QueryParam("recipient") String recipient_url,
			@QueryParam("ammount") String ammount_url, // fofmat 100.00
			@QueryParam("message") String message_url, 
			@QueryParam("asset") String asset_url,
			@QueryParam("count") String count_url,
			@QueryParam("sleep") String sleep_url
			

	){
		
		if (!BlockChain.DEVELOP_USE) return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity("CLOSED")
				.build();
		
		return send( sender_url, recipient_url,
				 ammount_url, // fofmat 100.00
				  message_url, 
				 count_url,
				 sleep_url);
		
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("setnd_mails")
	public Response createmails(
			// @QueryParam("sender") String sender,
			@QueryParam("sender") String sender_url,
			@QueryParam("recipient") String recipient_url,
			@QueryParam("message") String message_url, 
			@QueryParam("count") String count_url,
			@QueryParam("sleep") String sleep_url
			

	){
		
		if (!BlockChain.DEVELOP_USE) return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity("CLOSED")
				.build();

		return send( sender_url, recipient_url,
				 "0.0", // fofmat 100.00
				 message_url, 
				 count_url,
				 sleep_url);
		
	}
	
	// is run 
	public static boolean isRunTest() {
		return run_test;
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("runtransactions")
	public Response start(
			@QueryParam("message") String message_url, 
			@QueryParam("sleep") String sleep_url,
			@QueryParam("ammount") String ammount_url
			

	){
		
		if (!BlockChain.DEVELOP_USE) return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity("CLOSED")
				.build();

		JSONObject ff = new JSONObject();
	//	if(proces_Start) return "Allredy Start";
	//	proces_Start = true;
		if (run_test) {
			run_test = false;
			ff.put("status","Alredy Run... try STOPING");
			ff.put("code", "1000");
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*")
					.entity(StrJSonFine.convert(ff))
					.build();
		}

		run_test = true;
		
		try {
			sleep = Integer.valueOf(sleep_url);
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			sleep = 10000;
		}
		
		
		
		try {
			amm = new BigDecimal(ammount_url);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			amm = new BigDecimal("0.0001");
		}
		
		try {
			 mes = message_url.getBytes();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			 mes = "".getBytes();
		}

		
		if (thread==null) {
			thread = new Thread(){
	
				public void run(){					
					
					Account sender = (Account) Controller.getInstance().getAccounts().get(0);
					Controller ctrl = Controller.getInstance();
					DCSet dcSet = DCSet.getInstance();
					do {
						if(Thread.interrupted())return;
						if(dcSet.isStoped()) return;
						if (ctrl.isOnStopping() || dcSet.isStoped())return;
	
						try {
							Thread.sleep(sleep);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							return;
						}
	
						if (dcSet.getBlockMap().isProcessing()
								|| !ctrl.checkStatus()
								|| !API_Test.isRunTest())
							continue;
											
						count_step++;
					//	LOGGER.info("\n count =" + count_step + "  ");
						tt = returnDeal( null,
								sender.getAddress(), amm, 
								mes, new byte[]{1},	new byte[]{0});
				
				//		LOGGER.info("Result send Transaction " +tt);
						
					} while (true);
			}
			};
			thread.start();
		}
		
		
		ff.put("status","Started");
		ff.put("code", "0000");
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(ff))
				.build();
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@GET
	@Path("stoptransaction")
	public Response stop1(){
		
		if (!BlockChain.DEVELOP_USE) return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity("CLOSED")
				.build();

		
		run_test = false;
		
		JSONObject ff = new JSONObject();
		ff.put("status","Stopped");
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(ff))
				.build();
	}
	
	public Response send(String sender_url,String recipient_url,
		String ammount_url, // fofmat 100.00
		 String message_url, 
		String count_url,
		String sleep_url)
	{

		if (!BlockChain.DEVELOP_USE) return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity("CLOSED")
				.build();


		JSONObject ff = new JSONObject();
		if (run_test) {
			run_test = false;
			ff.put("status","Alredy Run... try STOPING");
			ff.put("code", "1000");
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*")
					.entity(StrJSonFine.convert(ff))
					.build();
		}
		run_test = true;
		String Status_text = "";
	
		
		// READ SENDER
		Account sender = (Account) Controller.getInstance().getAccounts().get(0);
		if (sender == null){
		//	return "Sender not found";
			run_test = false;
			ff.put("status","Sender not found");
			ff.put("code", "2000");
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*")
					.entity(StrJSonFine.convert(ff))
					.build();
			
		}
		String sender_account = null;
		if (sender_url == null) sender_account = sender.getAddress();
		
		String recipient_account;
		Double ran = null;
		

		int parsing = 0;
		int feePow = 0;
		int count =0;
		
		BigDecimal amount = null;
		
			// READ AMOUNT
			
			try {
				amount = new BigDecimal(ammount_url).setScale(8);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				amount = new BigDecimal("0.0001").setScale(8);
			}

			// READ FEEparsing = 2;
			feePow = Integer.parseInt("1");
			
			
			
			
		
			try {
				sleep = Integer.parseInt(sleep_url);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				sleep = 1000;
			}
			
			

			// CHECK WHERE PARSING ERROR HAPPENED
			

				

		
				
				

			
		
		
		
	//	if (amount.equals(new BigDecimal("0.0").setScale(8))) {
	//		return "Amount must be greater 0.0";
	//	}
		String message = "";
		if (message_url != null)  message = message_url;

		boolean isTextB = true;

		byte[] messageBytes = null;

		if (message != null && message.length() > 0) {
			if (isTextB) {
				messageBytes = message.getBytes(Charset.forName("UTF-8"));
			} else {
				try {
					messageBytes = Converter.parseHexString(message);
				} catch (Exception g) {
					try {
						messageBytes = Base58.decode(message);
					} catch (Exception e1) {
					//	return "Message format is not base58 or hex!";
						ff.put("status","Message format is not base58 or hex!");
						ff.put("code", "3000");
						run_test = false;
						return Response.status(200)
								.header("Content-Type", "application/json; charset=utf-8")
								.header("Access-Control-Allow-Origin", "*")
								.entity(StrJSonFine.convert(ff))
								.build();
					}
				}
			}
		}

		// if no TEXT - set null
		if (messageBytes != null && messageBytes.length == 0)
			messageBytes = null;
		// if amount = 0 - set null
		if (amount.compareTo(BigDecimal.ZERO) == 0)
			amount = null;

		boolean encryptMessage = false;

		byte[] encrypted = new byte[] { 0 };
		byte[] isTextByte = new byte[] { 1 };

		AssetCls asset;
		long key = 1l;
		

		Integer result;

		if (messageBytes != null) {
			if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
				//return ("Message size exceeded!") + " <= MAX";
				ff.put("status",("Message size exceeded!") + " <= MAX");
				ff.put("code", "4000");
				run_test = false;
				return Response.status(200)
						.header("Content-Type", "application/json; charset=utf-8")
						.header("Access-Control-Allow-Origin", "*")
						.entity(StrJSonFine.convert(ff))
						.build();
			}

			
		}
		
		try {
			count = Integer.parseInt(count_url);
			for (count_step = 0; count > count_step; count_step++) {
				
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					//return "Stop";
					ff.put("status","Stop");
					ff.put("code", "5000");
					run_test = false;
					return Response.status(200)
							.header("Content-Type", "application/json; charset=utf-8")
							.header("Access-Control-Allow-Origin", "*")
							.entity(StrJSonFine.convert(ff))
							.build();
					
					
				}
				tt = returnDeal( recipient_url,
						sender_account, amount, 
						messageBytes, isTextByte,encrypted );
			//	LOGGER.info("Result send Transaction " +tt);
			}
		
		} catch (NumberFormatException e) {
			
		}
		
		
		Status_text +="\n Message and/or payment has been sent!";
		//return Status_text;
		
		ff.put("status","Message  payment has been sent!");
		ff.put("code", "0000");
		run_test = false;
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(ff))
				.build();
				//Response.status(200)
				//.header("Content-Type", "application/json; charset=utf-8")
				//.header("Access-Control-Allow-Origin", "*")
				//.entity(Status_text)
				//.build();
		
		
		
	}
	@SuppressWarnings("unchecked")
	@GET
	@Path("getstatus")
	public Response getstatus() {
		
			JSONObject ff = new JSONObject();
			int st = Controller.getInstance().getStatus();
			String descr = null;
			descr = " No Connection";		
			if (st == Controller.getInstance().STATUS_OK) descr = "Status Ok";
			if (st == Controller.getInstance().STATUS_SYNCHRONIZING)descr = "Sinchronizing";
			ff.put("network_code", st);
			ff.put("network_name",descr);
			
			//ForgingStatus forg = Controller.getInstance().getForgingStatus();
			ForgingStatus forg = Status.getinstance().getForgingStatus();
			if (forg == null){
				ff.put("forging_code", -100);
				ff.put("forging_name", "Not Found");
			}else{
			
			ff.put("forging_code", forg.getStatuscode());
			ff.put("forging_name", forg.getName());
			}
			
			Block block = Controller.getInstance().getLastBlock();
			ff.put("block_height", block.getHeight(DCSet.getInstance()));
			ff.put("block_timestamp", block.getTimestamp(DCSet.getInstance()));
			ff.put("block_vin_value", block.calcWinValue(DCSet.getInstance()));
			ff.put("block_value_target", block.calcWinValueTargeted(DCSet.getInstance()));
			ff.put("block_creator_address", block.getCreator().getAddress());
			ff.put("block_transaction_count", block.getTransactionCount());
			
			ff.put("unconfirmed_transactions",  DCSet.getInstance().getTransactionMap().size());
			ff.put("count",count_step);
			ff.put("transactions_sleep",sleep);
			ff.put("transaction_status", run_test?"Выполняется":"Остановлено");
			ff.put("transaction_status_cod", run_test?"1":"0");
			ff.put("block_generator_status",BlockGenerator.viewStatus());
			
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*")
					.entity(StrJSonFine.convert(ff))
					.build();
		
		

	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("getcount")
	public Response getcount() {
		
			JSONObject ff = new JSONObject();
			ff.put("Count",count_step);
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*")
					.entity(StrJSonFine.convert(ff))
					.build();
		
		

	}
	@SuppressWarnings("unchecked")
	@GET
	@Path("erasecount")
	public Response erasecount() {
		
		count_step=0;
			JSONObject ff = new JSONObject();
			ff.put("Erase Count",count_step);
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*")
					.entity(StrJSonFine.convert(ff))
					.build();
		
		

	}
	
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("forgingend")
	public Response forgig() {
		
		if (!BlockChain.DEVELOP_USE) return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity("CLOSED")
				.build();

		JSONObject ff = new JSONObject();
		 
			Controller.getInstance().forgingStatusChanged(ForgingStatus.FORGING_DISABLED);
			ff.put("forging","Stop");
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*")
					.entity(StrJSonFine.convert(ff))
					.build();
		
			

	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("forgingstart")
	public Response forgigStart() {
		
		if (!BlockChain.DEVELOP_USE) return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity("CLOSED")
				.build();

		JSONObject ff = new JSONObject();
			Controller.getInstance().forgingStatusChanged(ForgingStatus.FORGING);
			ff.put("forging","Start");
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.header("Access-Control-Allow-Origin", "*")
					.entity(StrJSonFine.convert(ff))
					.build();
		}
	
	
	private String returnDeal( String recipient_url,
			String sender_account, BigDecimal amount, 
			byte[] messageBytes, byte[] isTextByte,
			byte[] encrypted ) {
	
		// REcipient 
		String recipient_account;
		if (recipient_url == null){
			int k = (int) (GenesisBlock.generalGenesisUsers.size()* Math.random());
			
			List<Object> ss = GenesisBlock.generalGenesisUsers.get(k);
			String kk = (String) ss.get(0);
			recipient_account = (String) ss.get(0);
		}
		else{
			recipient_account = recipient_url;
		}
		// READ RECIPIENT
		Tuple2<Account, String> resultRecipient = Account.tryMakeAccount(recipient_account);
		if (resultRecipient.b != null) {
			JOptionPane.showMessageDialog(null, Lang.getInstance().translate(resultRecipient.b),
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			// ENABLE

			return "ERROR recipient";
		}
		Account recipient = resultRecipient.a;
		
		
		
		
		String head = "transacnion " + count_step;

		if (head.getBytes(StandardCharsets.UTF_8).length > 256) {

			return "Title size exceeded!" + " <= 256";
		}

		// CREATE TX MESSAGE
		
		Transaction transaction = Controller.getInstance().r_Send(
				Controller.getInstance().getPrivateKeyAccountByAddress(sender_account),
				0,
				recipient,
				new Long(1),
				amount,
				head,
				messageBytes,
				isTextByte,
				encrypted
				);
		// test result = new Pair<Transaction, Integer>(null,
		// Transaction.VALIDATE_OK);

	//	LOGGER.info(" sender:" +sender_account + "   reciever:" + recipient_account );

		Integer result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, false);
		if (result != 1)
			LOGGER.error(" CODE: " + result +" sender:" +sender_account + "   reciever:" + recipient_account );
		
	
//		Status_text += "\n" + head  + "res:" + result;
	return ""+ result;
	}

}