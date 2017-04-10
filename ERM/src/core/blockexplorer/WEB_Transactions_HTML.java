package core.blockexplorer;

import java.math.BigDecimal;

import org.json.simple.JSONObject;

import com.github.rjeschke.txtmark.Processor;

import controller.Controller;
import core.transaction.R_Send;
import core.transaction.Transaction;
import lang.Lang;

public class WEB_Transactions_HTML {
	private static WEB_Transactions_HTML instance;
	
	public static WEB_Transactions_HTML getInstance()
	{
		if(instance == null)
		{
			instance = new WEB_Transactions_HTML();
		}
		
		return instance;
	}
	
	public String get_HTML(Transaction transaction, JSONObject langObj){
		
		int type = transaction.getType();
		switch (type){
			case Transaction.SEND_ASSET_TRANSACTION:
				return r_Send_HTML(transaction, langObj);
			
			
			
			
			
		}
		
		
		return null;
		
	}

	private String r_Send_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		R_Send tr = (R_Send)transaction;
		String out = "";
		
		if (tr.getRecipient().getPerson() != null){
			out += "<b>" + Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?person=" +tr.getRecipient().getPerson().b.getKey()+ get_Lang(langObj) + ">" + tr.getRecipient().viewPerson() +"</a><br>";
			
		}else {
		
		out += "<b>" +Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?addr=" +tr.getRecipient().getAddress() + get_Lang(langObj) + ">"  + tr.getRecipient().getAddress() +"</a><br>";
		}
		if (!tr.getHead().equals("") ) out += "<b>" + Lang.getInstance().translate_from_langObj("Title", langObj) + ":</b> " + tr.getHead() + "<BR>";
		if (!tr.viewData().equals("") ) out += "<b>" +Lang.getInstance().translate_from_langObj("Message", langObj) + ":</b> " + tr.viewData();
		
		if (tr.getAmount() != null){
		out += "<BR><b>"  + Lang.getInstance().translate_from_langObj("Amount", langObj) + ":</b> " + tr.getAmount().toPlainString() + " (" + Controller.getInstance().getAsset( tr.getAbsKey()).getName()+ ")";
		}
		
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Fee", langObj) + ": </b>" + tr.viewFee();
		return out;
		
		
	}
	
	private String get_Lang(JSONObject langObj){
		if (langObj == null) return "&lang=en";
		return "&lang="+langObj.get("_lang_ISO_");
		
		
	}

}
