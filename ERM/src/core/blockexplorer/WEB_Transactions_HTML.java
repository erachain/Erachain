package core.blockexplorer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.json.simple.JSONObject;

import com.github.rjeschke.txtmark.Processor;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.item.unions.UnionCls;
import core.transaction.IssueAssetTransaction;
import core.transaction.IssueImprintRecord;
import core.transaction.IssueNoteRecord;
import core.transaction.IssuePersonRecord;
import core.transaction.IssueStatusRecord;
import core.transaction.IssueUnionRecord;
import core.transaction.R_Send;
import core.transaction.R_SertifyPubKeys;
import core.transaction.R_SetStatusToItem;
import core.transaction.R_SignNote;
import core.transaction.R_Vouch;
import core.transaction.Transaction;
import database.DBSet;
import gui.MainFrame;
import gui.PasswordPane;
import lang.Lang;
import utils.Converter;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

public class WEB_Transactions_HTML {
	private static WEB_Transactions_HTML instance;

	public static WEB_Transactions_HTML getInstance() {
		if (instance == null) {
			instance = new WEB_Transactions_HTML();
		}

		return instance;
	}

	public String get_HTML(Transaction transaction, JSONObject langObj) {
		
		List<Transaction>tt = new ArrayList<Transaction>();
		tt.add(transaction);
		LinkedHashMap json = BlockExplorer.getInstance().Transactions_JSON(tt);
		LinkedHashMap aa = (LinkedHashMap)json.get("transactions");
		LinkedHashMap tras_json = (LinkedHashMap) aa.get(0);
		
			String out = "<font size='+1'> <b>"+Lang.getInstance().translate_from_langObj("Transaction", langObj) + ": </b>"+ tras_json.get("type");
			out += "       ("  + Lang.getInstance().translate_from_langObj("Block", langObj) +": </b><a href=?block=" + tras_json.get("block") + get_Lang(langObj) + ">" +  tras_json.get("block") + "</a>"; 
			out += " - " +  tras_json.get("seq")  +") </font><br>";
			out += "<br><b>"  +  Lang.getInstance().translate_from_langObj("Confirmations", langObj)       + ": </b>" +  tras_json.get("confirmations"); 
			out += "<br><b>"  +  Lang.getInstance().translate_from_langObj("Date", langObj)       + ": </b>" + tras_json.get("date");
			out += "<br><b>"  +  Lang.getInstance().translate_from_langObj("Size", langObj)        + ": </b>" + tras_json.get("size"); 
			out += "<br><b>" +  Lang.getInstance().translate_from_langObj("Signature", langObj)   + ": </b>" + tras_json.get("signature");
			out += "<br><b>"  +  Lang.getInstance().translate_from_langObj("Reference", langObj) + ": </b>" +  tras_json.get("reference");
			out += "<br> ";
			if ( tras_json.get("creator_key") != "-")
			{
				if ( tras_json.get("creator_key") == "+"){
					out += "<b>" + Lang.getInstance().translate_from_langObj("Creator", langObj) +": </b><a href=?addr="+  tras_json.get("creator_addr") +  get_Lang(langObj)+ ">" + tras_json.get("creator") + "</a>";
				}
				else{
					out += "<b>" +  Lang.getInstance().translate_from_langObj("Creator", langObj) +": </b><a href=?person="+  tras_json.get("creator_key") + get_Lang(langObj)+ ">" + tras_json.get("creator") + "</a>";
				}
			}
			//output += '<br>'  +  data.transaction_Header.label_recipient       + ': ' + data.transaction_Header.transactions[0].recipient; 
			out += "<br>";	
		
		
		
		
		int type = transaction.getType();
		switch (type) {
		case Transaction.SEND_ASSET_TRANSACTION:
			return out+ r_Send_HTML(transaction, langObj);
		case Transaction.ISSUE_ASSET_TRANSACTION:
			return out+ issue_Asset_HTML(transaction, langObj);
		case Transaction.ISSUE_PERSON_TRANSACTION:
			return out+ issue_Person_HTML(transaction, langObj);
		case Transaction.ISSUE_IMPRINT_TRANSACTION:
			return out+ issue_Imprint_HTML(transaction, langObj);
		case Transaction.ISSUE_NOTE_TRANSACTION:
			return out+ issue_Note_HTML(transaction, langObj);
		case Transaction.ISSUE_STATUS_TRANSACTION:
			return out+ issue_Status_HTML(transaction, langObj);
		case Transaction.ISSUE_UNION_TRANSACTION:
			return out+ issue_Union_HTML(transaction, langObj);
		case Transaction.VOUCH_TRANSACTION:
			return out+ vouch_HTML(transaction, langObj);
		case Transaction.SIGN_NOTE_TRANSACTION:
			return out+ sign_Note_HTML(transaction, langObj);
		case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:
			return out+ serttify_Pub_Key_HTML(transaction, langObj);
		case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:
			return out+ set_Status_HTML(transaction, langObj);
/*
			public static final int GENESIS_ISSUE_ASSET_TRANSACTION = 1;
			public static final int GENESIS_ISSUE_NOTE_TRANSACTION = 2;
			public static final int GENESIS_ISSUE_PERSON_TRANSACTION = 3;
			public static final int GENESIS_ISSUE_STATUS_TRANSACTION = 4;
			public static final int GENESIS_ISSUE_UNION_TRANSACTION = 5; //
			public static final int GENESIS_SEND_ASSET_TRANSACTION = 6;
			public static final int GENESIS_SIGN_NOTE_TRANSACTION = 7; //
			public static final int GENESIS_CERTIFY_PERSON_TRANSACTION = 8; // нет в гуи
			public static final int GENESIS_ASSIGN_STATUS_TRANSACTION = 9;//
			public static final int GENESIS_ADOPT_UNION_TRANSACTION = 10;//
			// ISSUE ITEMS
			public static final int ISSUE_STATEMENT_TRANSACTION = 27; // not in gui
			// RENT ASSET
			public static final int RENT_ASSET_TRANSACTION = 32; //
			// HOLD ASSET
			public static final int HOLD_ASSET_TRANSACTION = 33; // not in gui
			
			// OTHER
			public static final int SET_UNION_TO_ITEM_TRANSACTION = 38;
			public static final int SET_UNION_STATUS_TO_ITEM_TRANSACTION = 39; // not in gui

			public static final int HASHES_RECORD = 41;
			
			// exchange of assets
			public static final int CREATE_ORDER_TRANSACTION = 50;
			public static final int CANCEL_ORDER_TRANSACTION = 51;
			// voting
			public static final int CREATE_POLL_TRANSACTION = 61;
			public static final int VOTE_ON_POLL_TRANSACTION = 62;
			
			public static final int RELEASE_PACK = 70;

			// old
			public static final int REGISTER_NAME_TRANSACTION = 6 + 130;
			public static final int UPDATE_NAME_TRANSACTION = 7 + 130;
			public static final int SELL_NAME_TRANSACTION = 8 + 130;
			public static final int CANCEL_SELL_NAME_TRANSACTION = 9 + 130;
			public static final int BUY_NAME_TRANSACTION = 10 + 130;
			public static final int ARBITRARY_TRANSACTION = 12 + 130;
			public static final int MULTI_PAYMENT_TRANSACTION = 13 + 130;
			public static final int DEPLOY_AT_TRANSACTION = 14 + 130;
*/	
		}
		out += "<br>" +transaction.toJson();
		return out;
	}

	private String set_Status_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		R_SetStatusToItem setStatusToItem = (R_SetStatusToItem)transaction;
		ItemCls item = Controller.getInstance().getItem(setStatusToItem.getItemType(), setStatusToItem.getItemKey());
		long status_key = setStatusToItem.getKey();
		StatusCls status = Controller.getInstance().getItemStatus(status_key);
		out += "<b>" + Lang.getInstance().translate_from_langObj("Status Name", langObj) + ":</b> "
					+ status.getName()+ "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Status Description", langObj) + ":</b> " + status.getDescription()+ "<br>";
		long beginDate = setStatusToItem.getBeginDate();
		long endDate = setStatusToItem.getEndDate();
		out += "<b>" + Lang.getInstance().translate_from_langObj("From - To", langObj) + ":</b> "
					+ (beginDate == Long.MIN_VALUE?"?":DateTimeFormat.timestamptoString(beginDate))
					+ " - " + (endDate == Long.MAX_VALUE? "?":DateTimeFormat.timestamptoString(endDate)) + "<br>";
		if (setStatusToItem.getValue1() != 0) {
			out += "<b>" + Lang.getInstance().translate_from_langObj("Value", langObj) + " 1:</b> "
						+ setStatusToItem.getValue1() + "<br>";
		}
		if (setStatusToItem.getValue2() != 0) {
				out += "<b>" + Lang.getInstance().translate_from_langObj("Value", langObj) + " 2:</b> "
						+ setStatusToItem.getValue2() + "<br>";
		}
		if (setStatusToItem.getData1() != null) {
			out += "<b>" + Lang.getInstance().translate_from_langObj("DATA", langObj) + " 1:</b> "
						+ new String(setStatusToItem.getData1(), Charset.forName("UTF-8")) + "<br>";
		}
		if (setStatusToItem.getData2() != null) {
			out += "<b>" + Lang.getInstance().translate_from_langObj("DATA", langObj) + " 2:</b> "
						+ new String(setStatusToItem.getData2(), Charset.forName("UTF-8")) + "<br>";
		}
		if (setStatusToItem.getRefParent() != 0l) {
				out += "<b>" + Lang.getInstance().translate_from_langObj("Parent", langObj) + ":</b> "
						+ setStatusToItem.viewRefParent() + "<br>";
		}
		if (setStatusToItem.getDescription() != null) {
				out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
						+ new String(setStatusToItem.getDescription(), Charset.forName("UTF-8")) + "<br>";
		}
		out += "<b>" + Lang.getInstance().translate_from_langObj("Item Name", langObj) + ":</b> "
					+ item.getItemTypeStr() + " - " + item.getItemSubType()
					+ ": " + item.getName() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Item Description", langObj) + ":</b> "
					+ item.getDescription() + "<br>";
		
		return out;
	}

	private String serttify_Pub_Key_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		 R_SertifyPubKeys record = (R_SertifyPubKeys) transaction;
		PersonCls person;
		person = Controller.getInstance().getPerson( record.getKey());
		out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> <a href=?person="
					+ person.getKey() + get_Lang(langObj) + ">" + person.getName() + "</a><br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("End Days", langObj) + ":</b> "
					+ record.getAddDay() + "<br>";
			int i = 0;
			for (String address: record.getSertifiedPublicKeysB58())
			{
				out += "<b>   " + Lang.getInstance().translate_from_langObj("Account", langObj)  + " " + ++i + ":</b> "	+ address + "<br>";
			}
		return out;
	}

	private String sign_Note_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		R_SignNote r_Statement = (R_SignNote)transaction;
			if (r_Statement.getKey() > 0) {
				out += "<b>" + Lang.getInstance().translate_from_langObj("Key", langObj) + ":</b> "
						+ Controller.getInstance().getNote( r_Statement.getKey()).toString() + "<br>";
			}
			if (r_Statement.getData() != null) {
				String ss = (( r_Statement.isText() ) ? Processor.process(new String(r_Statement.getData(), Charset.forName("UTF-8"))) : Processor.process(Converter.toHex(r_Statement.getData())));
				ss = "<div  style='word-wrap: break-word;'>" +ss;
				out += "<b>" + Lang.getInstance().translate_from_langObj("Message", langObj) + ":</b> "
						+ ss + "<br>";
			}	
		return out;
	}

	private String vouch_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		R_Vouch vouchRecord = (R_Vouch) transaction;
		Transaction record = DBSet.getInstance().getTransactionFinalMap().getTransaction(vouchRecord.getVouchHeight(),
				vouchRecord.getVouchSeq());
		out += "<b>" + Lang.getInstance().translate_from_langObj("height-seq.", langObj) + ":</b> <a href=?tx="
				+  Base58.encode(record.getSignature()) + get_Lang(langObj) + ">" + vouchRecord.getVouchHeight() + "-"
				+ vouchRecord.getVouchSeq() + "</a><br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "+ get_HTML(record, langObj) + "<br>";
		// LABEL DESCRIPTION

		return out;
	}

	private String issue_Union_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		IssueUnionRecord unionIssue = (IssueUnionRecord) transaction;
		UnionCls union = (UnionCls) unionIssue.getItem();
		out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> "
				+ unionIssue.getItem().getName() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
				+ unionIssue.getItem().getDescription() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Birthday", langObj) + ":</b> "
				+ union.getBirthdayStr() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Parent", langObj) + ":</b> "
				+ String.valueOf(union.getParent()) + "<br>";
		return out;
	}

	private String issue_Status_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		IssueStatusRecord statusIssue = (IssueStatusRecord) transaction;
		out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> "
				+ statusIssue.getItem().getName() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
				+ statusIssue.getItem().getDescription() + "<br>";
		return out;
	}

	private String issue_Note_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		IssueNoteRecord noteIssue = (IssueNoteRecord) transaction;
		out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> "
				+ noteIssue.getItem().getName() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
				+ noteIssue.getItem().getDescription() + "<br>";
		return out;
	}

	private String issue_Imprint_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		IssueImprintRecord imprintIssue = (IssueImprintRecord) transaction;
		out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> "
				+ imprintIssue.getItem().getName() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
				+ imprintIssue.getItem().getDescription() + "<br>";
		return out;
	}

	private String issue_Person_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		IssuePersonRecord personIssue = (IssuePersonRecord) transaction;
		PersonCls person = (PersonCls) personIssue.getItem();
		String out = "";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> <a href=?person="
				+ person.getKey() + get_Lang(langObj) + ">" + personIssue.getItem().getName() + "</a><br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Birthday", langObj) + ":</b> "
				+ person.getBirthdayStr() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Gender", langObj) + ":</b> ";
		if (person.getGender() == 0)
			out += Lang.getInstance().translate_from_langObj("Male", langObj);
		if (person.getGender() == 1)
			out += Lang.getInstance().translate_from_langObj("Female", langObj);
		out += "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
				+ personIssue.getItem().getDescription() + "<br>";
		if (person.getOwner().getPerson() != null) {
			// out += "<b>" + Lang.getInstance().translate_from_langObj("Owner",
			// langObj) + ":</b> <a href=?person="
			// +person.getOwner().getPerson().b.getKey()+ get_Lang(langObj) +
			// ">" + person.getOwner().viewPerson() +"</a><br>";
		} else {
			// out += "<b>" +Lang.getInstance().translate_from_langObj("Owner",
			// langObj) + ":</b> <a href=?addr=" +
			// person.getOwner().getAddress() + get_Lang(langObj) + ">" +
			// person.getOwner().getAddress() +"</a><br>";
		}
		// out += "<b>" + Lang.getInstance().translate_from_langObj("Public
		// Key", langObj) + ":</b> " + person.getOwner().getBase58() +"<br>";
		return out;
	}

	private String issue_Asset_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		IssueAssetTransaction tr = (IssueAssetTransaction) transaction;
		String out = "";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> <a href=?asset="
				+ tr.getAssetKey() + get_Lang(langObj) + ">" + tr.getItem().getName() + "</a><br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
				+ tr.getItem().getDescription() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Quantity", langObj) + ":</b> "
				+ ((AssetCls) tr.getItem()).getQuantity().toString() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Divisible", langObj) + ":</b> "
				+ Lang.getInstance().translate_from_langObj(((AssetCls) tr.getItem()).isDivisible() + "", langObj)
				+ "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Movable", langObj) + ":</b> "
				+ Lang.getInstance().translate_from_langObj(((AssetCls) tr.getItem()).isMovable() + "", langObj)
				+ "<br>";

		return out;
	}

	private String r_Send_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		R_Send tr = (R_Send) transaction;
		String out = "";

		if (tr.getRecipient().getPerson() != null) {
			out += "<b>" + Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?person="
					+ tr.getRecipient().getPerson().b.getKey() + get_Lang(langObj) + ">"
					+ tr.getRecipient().viewPerson() + "</a><br>";

		} else {

			out += "<b>" + Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?addr="
					+ tr.getRecipient().getAddress() + get_Lang(langObj) + ">" + tr.getRecipient().getAddress()
					+ "</a><br>";
		}
		if (!tr.getHead().equals(""))
			out += "<b>" + Lang.getInstance().translate_from_langObj("Title", langObj) + ":</b> " + tr.getHead()
					+ "<BR>";
		if (!tr.viewData().equals(""))
			out += "<b>" + Lang.getInstance().translate_from_langObj("Message", langObj) + ":</b> " + tr.viewData();

		if (tr.getAmount() != null) {
			out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Amount", langObj) + ":</b> "
					+ tr.getAmount().toPlainString() + " ("
					+ Controller.getInstance().getAsset(tr.getAbsKey()).getName() + ")";
		}

		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Fee", langObj) + ": </b>" + tr.viewFee();
		return out;

	}

	private String get_Lang(JSONObject langObj) {
		if (langObj == null)
			return "&lang=en";
		return "&lang=" + langObj.get("_lang_ISO_");

	}

}
