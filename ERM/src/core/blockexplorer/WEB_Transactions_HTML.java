package core.blockexplorer;

import java.math.BigDecimal;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.json.simple.JSONObject;

import com.github.rjeschke.txtmark.Processor;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.IssueAssetTransaction;
import core.transaction.IssuePersonRecord;
import core.transaction.R_Send;
import core.transaction.Transaction;
import lang.Lang;
import utils.MenuPopupUtil;

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
			case Transaction.ISSUE_ASSET_TRANSACTION:
				return issue_Asset_HTML(transaction, langObj);
			case Transaction.ISSUE_PERSON_TRANSACTION:
				return issue_Person_HTML(transaction, langObj);
			
			
			
		}
		
		
		return null;
		
	}

	private String issue_Person_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		IssuePersonRecord personIssue = (IssuePersonRecord)transaction;
		PersonCls person = (PersonCls)personIssue.getItem();
		String out = "";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> <a href=?person=" + person.getKey()+ get_Lang(langObj)+ ">" + personIssue.getItem().getName() +"</a><br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Birthday", langObj) + ":</b> " + person.getBirthdayStr() +"<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Gender", langObj) + ":</b> ";
		if(person.getGender() == 0) out += Lang.getInstance().translate_from_langObj("Male", langObj);
		if(person.getGender() == 1) out += Lang.getInstance().translate_from_langObj("Female", langObj);
		out +="<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> " + personIssue.getItem().getDescription() +"<br>";
		if (person.getOwner().getPerson() != null){
			out += "<b>" + Lang.getInstance().translate_from_langObj("Owner", langObj) + ":</b> <a href=?person=" +person.getOwner().getPerson().b.getKey()+ get_Lang(langObj) + ">" + person.getOwner().viewPerson() +"</a><br>";
		}else {
		out += "<b>" +Lang.getInstance().translate_from_langObj("Owner", langObj) + ":</b> <a href=?addr=" + person.getOwner().getAddress() + get_Lang(langObj) + ">"  + person.getOwner().getAddress() +"</a><br>";
		}
		out += "<b>" + Lang.getInstance().translate_from_langObj("Public Key", langObj) + ":</b> " + person.getOwner().getBase58() +"<br>";
		return out;
	}

	private String issue_Asset_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		IssueAssetTransaction tr = (IssueAssetTransaction)transaction;
		String out = "";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> <a href=?asset=" + tr.getAssetKey()+ get_Lang(langObj)+ ">" + tr.getItem().getName() +"</a><br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> " + tr.getItem().getDescription() +"<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Quantity", langObj) + ":</b> " + ((AssetCls)tr.getItem()).getQuantity().toString() +"<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Divisible", langObj) + ":</b> " + Lang.getInstance().translate_from_langObj(((AssetCls)tr.getItem()).isDivisible() +"", langObj)+"<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Movable", langObj) + ":</b> " + Lang.getInstance().translate_from_langObj(((AssetCls)tr.getItem()).isMovable() + "", langObj) +"<br>";
		
		return out;
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
