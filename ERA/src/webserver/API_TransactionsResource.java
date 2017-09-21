package webserver;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.mapdb.Fun.Tuple2;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import core.transaction.Transaction;
import database.DBSet;
import gui.library.library;
import gui.models.TransactionsTableModel;
import lang.Lang;
import utils.StrJSonFine;

@Path("apirecords")
@Produces(MediaType.APPLICATION_JSON)
public class API_TransactionsResource {

	
	@Context
	HttpServletRequest request;

	@GET
	public Response Default() {
		
		Map<String, String> help = new LinkedHashMap<String, String>();

		help.put("apirecords/getbyaddress?address={address}&asset={asset}", Lang.getInstance().translate("Get all Records for Address & Asset Key"));
		help.put("apirecords/getbyaddressfromtransactionlimit?address={address}&asset={asset}&start={start record}&end={end record}&type={type Transaction}&sort={des/asc}",Lang.getInstance().translate("Get all Records for Address & Asset Key from Start to End"));
		help.put("apirecords/getbyblock?block={block}", Lang.getInstance().translate("Get all Records from Block"));
		 
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.header("Access-Control-Allow-Origin", "*")
				.entity(StrJSonFine.convert(help))
				.build();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("getbyaddress")
	public String getByAddress(@QueryParam("address") String address, @QueryParam("asset") String asset )
	{
		List<Transaction> result;
		if (address ==null || address.equals("")) {
			JSONObject ff = new JSONObject();
			ff.put("Error", "Invalid Address");
			return  ff.toJSONString();
		}
		//TransactionsTableModel a = new TransactionsTableModel();
		//a.Find_Transactions_from_Address(address);
		//result =a.getTransactions();
		result = DBSet.getInstance().getTransactionFinalMap().getTransactionsByAddress(address);
		
		if (result == null){
				JSONObject ff = new JSONObject();
				ff.put("message", "null");
				return  ff.toJSONString();
		};
		
		
		JSONArray array = new JSONArray();
		for(Transaction transaction: result)
		{
			if(asset !=null){
				if(transaction.getAbsKey() == new Long (asset)){
					array.add(transaction.toJson());
				}
			}else{
			array.add(transaction.toJson());
			}
		}
		//json.put("transactions", array);
		return array.toJSONString();
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("getbyaddressfromtransactionlimit")
	public String getByAddressLimit(@QueryParam("address") String address, @QueryParam("asset") String asset, @QueryParam("start") long start, @QueryParam("end") long end,  @QueryParam("type") String type1, @QueryParam("sort") String sort)
	{
		List<Transaction> result;
		
		if (address ==null || address.equals("")) {
			JSONObject ff = new JSONObject();
			ff.put("Error", "Invalid Address");
			return  ff.toJSONString();
		}
		//TransactionsTableModel a = new TransactionsTableModel();
		//a.Find_Transactions_from_Address(address);
		//result =a.getTransactions();
		Integer type;
		try {
			type = Integer.valueOf(type1);
			result = DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress(address, type,0);
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			result = DBSet.getInstance().getTransactionFinalMap().getTransactionsByAddress(address);
			//e.printStackTrace();
		}
		
		if (result == null){
				JSONObject ff = new JSONObject();
				ff.put("message", "null");
				return  ff.toJSONString();
		};
		
		// 7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC
		
		// read transactions from treeMap
		TreeMap<BigDecimal, Transaction> rec = new TreeMap<BigDecimal, Transaction>();
		for(Transaction transaction: result)
		{
			if(asset !=null){
				if(transaction.getAbsKey() == new Long (asset) ){
					rec.put(library.getBlockSegToBigInteger(transaction), transaction);
				}
			
			}
		}
		// read tree map from 1...n
		TreeMap<Long, JSONObject> k_Map = new TreeMap<Long, JSONObject>();
		// if descending = 1 sort descending
		NavigableMap<BigDecimal, Transaction> rec1;
		if (sort == null || !sort.equals("des")){
			rec1 = rec;
		}
		else {
		rec1 = rec.descendingMap();
		}
		long i=0;
		for( Entry<BigDecimal, Transaction> transaction: rec1.entrySet())
		{
			k_Map.put(i++, transaction.getValue().toJson());
		}
		
		
		
		//json.put("transactions", array);
		return new JSONObject(k_Map.subMap(start, end)).toJSONString();
		
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("getbyblock")
	public String getByBlock(@QueryParam("block") String block )
	{
		JSONObject ff = new JSONObject();
		List<Transaction> result;
		
		TransactionsTableModel a = new TransactionsTableModel();
		a.setBlockNumber(block);
		result =a.getTransactions();
		if (result == null || result.size()==0){
				
				ff.put("message", "null");
				return  ff.toJSONString();
		};
		 
		 
			JSONArray array = new JSONArray();
			for(Transaction trans: result)
			{
				
				array.add(trans.toJson());
			}
			//json.put("transactions", array);
			return array.toJSONString();
		
	}

	
	@SuppressWarnings("unchecked")
	@GET
	@Path("find")
	public String getTransactionsFind(@QueryParam("address") String address,
			@QueryParam("sender") String sender,
			@QueryParam("recipient") String recipient,
			@QueryParam("startblock") String s_minHeight,
			@QueryParam("endblock") String s_maxHeight,
			@QueryParam("type") String s_type,
			@QueryParam("service") String s_service,
			@QueryParam("desc") String s_desc,
			@QueryParam("offset") String s_offset,
			@QueryParam("limit") String s_limit
			
			)
	{
		
		int  maxHeight;
		try {
			maxHeight = new Integer(s_maxHeight);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			maxHeight=0;
		}
		int  minHeight;
		try {
			minHeight = new Integer(s_minHeight);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			minHeight=0;
		}
		int  type;
		try {
			type = new Integer(s_type);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			type=0;
		}
		int  service;
		try {
			service = new Integer(s_service);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			service=0;
		}
		int  offset;
		try {
			offset = new Integer(s_offset);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			offset=0;
		}
		int  limit;
		try {
			limit = new Integer(s_limit);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			limit=0;
		}
		
		
		List<Transaction> result = DBSet.getInstance().getTransactionFinalMap().findTransactions(address, sender, recipient, 
					minHeight, maxHeight,
					type, service,
					false, offset, limit);
		
				JSONArray array = new JSONArray();
		for(Transaction trans: result)
		{
			
			array.add(trans.toJson());
		}
		//json.put("transactions", array);
		return array.toJSONString();
	}
	
}