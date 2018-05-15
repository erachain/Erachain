package core.blockexplorer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import org.json.simple.JSONObject;

import controller.Controller;
import core.crypto.Base58;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.item.templates.TemplateCls;
import core.item.unions.UnionCls;
import core.transaction.CancelOrderTransaction;
import core.transaction.CreateOrderTransaction;
import core.transaction.CreatePollTransaction;
import core.transaction.GenesisCertifyPersonRecord;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.GenesisIssueTemplateRecord;
import core.transaction.GenesisTransferAssetTransaction;
import core.transaction.IssueAssetTransaction;
import core.transaction.IssueImprintRecord;
import core.transaction.IssuePersonRecord;
import core.transaction.IssueStatusRecord;
import core.transaction.IssueTemplateRecord;
import core.transaction.IssueUnionRecord;
import core.transaction.R_Hashes;
import core.transaction.R_Send;
import core.transaction.R_SertifyPubKeys;
import core.transaction.R_SetStatusToItem;
import core.transaction.R_SignNote;
import core.transaction.R_Vouch;
import core.transaction.Transaction;
import core.transaction.VoteOnPollTransaction;
import datachain.DCSet;
import gui.Gui;
import gui.items.statement.Statements_Vouch_Table_Model;
import gui.models.PollOptionsTableModel;
import lang.Lang;
import utils.BigDecimalStringComparator;
import utils.Converter;
import utils.DateTimeFormat;

public class WEB_Transactions_HTML {
	private static WEB_Transactions_HTML instance;
	JSONObject langObj;


	public static WEB_Transactions_HTML getInstance() {
		if (instance == null) {
			instance = new WEB_Transactions_HTML();
		}

		return instance;
	}

	public String get_HTML(Transaction transaction, JSONObject langObj) {
		this.langObj = langObj;
		List<Transaction>tt = new ArrayList<Transaction>();
		tt.add(transaction);
		LinkedHashMap json = BlockExplorer.getInstance().Transactions_JSON(tt);
		LinkedHashMap tras_json = (LinkedHashMap)((LinkedHashMap)json.get("transactions")).get(0);

		String out = "<font size='+1'> <b>"+Lang.getInstance().translate_from_langObj("Transaction", langObj) + ": </b>"+ tras_json.get("type");
		out += "       ("  + Lang.getInstance().translate_from_langObj("Block", langObj) +": </b><a href=?block=" + tras_json.get("block") + get_Lang(langObj) + ">" +  tras_json.get("block") + "</a>";
		out += " - " +  tras_json.get("seq")  +") </font><br>";
		out += "<br><b>"  +  Lang.getInstance().translate_from_langObj("Confirmations", langObj)       + ": </b>" +  tras_json.get("confirmations");
		out += "<br><b>"  +  Lang.getInstance().translate_from_langObj("Date", langObj)       + ": </b>" + tras_json.get("date");
		out += "<br><b>"  +  Lang.getInstance().translate_from_langObj("Size", langObj)        + ": </b>" + tras_json.get("size");
		out += "<br><b>" +  Lang.getInstance().translate_from_langObj("Publick Key", langObj)   + ": </b>" + tras_json.get("pub_key");
		out += "<br><b>" +  Lang.getInstance().translate_from_langObj("Signature", langObj)   + ": </b>" + tras_json.get("signature");
		out += "<br><b>"  +  Lang.getInstance().translate_from_langObj("Reference", langObj) + ": </b>" +  tras_json.get("reference");
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Fee", langObj) + ": </b>" + tras_json.get("fee");
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
			out = out + r_Send_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.ISSUE_ASSET_TRANSACTION:
			out= out+ issue_Asset_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.ISSUE_PERSON_TRANSACTION:
			out= out+ issue_Person_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.ISSUE_IMPRINT_TRANSACTION:
			out= out+ issue_Imprint_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.ISSUE_TEMPLATE_TRANSACTION:
			out= out+ issue_Template_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.ISSUE_STATUS_TRANSACTION:
			out= out+ issue_Status_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.ISSUE_UNION_TRANSACTION:
			out= out+ issue_Union_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.VOUCH_TRANSACTION:
			out = out+ vouch_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.SIGN_NOTE_TRANSACTION:
			out= out+ sign_Note_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:
			out= out+ serttify_Pub_Key_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:
			out= out+ set_Status_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.HASHES_RECORD:
			out= out+ hash_Record_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.CREATE_ORDER_TRANSACTION:
			out= out+ create_Order_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.CANCEL_ORDER_TRANSACTION:
			out= out+ cancel_Order_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.CREATE_POLL_TRANSACTION:
			out= out+ create_Poll_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.VOTE_ON_POLL_TRANSACTION:
			out= out+ vate_On_Poll_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.GENESIS_CERTIFY_PERSON_TRANSACTION:
			out= out+ genesis_Certify_Person_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:
			out= out+ genesis_Issue_Asset_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.GENESIS_ISSUE_TEMPLATE_TRANSACTION:
			out= out+ genesis_Issue_Template_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:
			out= out+ genesis_Certify_Person_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		case Transaction.GENESIS_SEND_ASSET_TRANSACTION:
			out= out+ genesis_Send_Asset_HTML(transaction, langObj) + get_Vouches(transaction);
			break;
		default:	out += "<br>" +transaction.toJson();
		}
		//		out += "<br>" +transaction.toJson();
		out += "<br><a href ='/api/recordrawbynumber/" + tras_json.get("block") + "-" +  tras_json.get("seq") + "'> RAW </a>";
		return out;
	}

	private String genesis_Send_Asset_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		GenesisTransferAssetTransaction assetTransfer = (GenesisTransferAssetTransaction)transaction;
		boolean isCredit = false;
		if (assetTransfer.getOwner() != null) {
			if (assetTransfer.getOwner().getPerson() != null) {
				out += "<b>" + Lang.getInstance().translate_from_langObj("Creditor", langObj) + ":</b> <a href=?person="
						+ assetTransfer.getOwner().getPerson().b.getKey() + get_Lang(langObj) + ">"
						+ assetTransfer.getOwner().viewPerson() + "</a><br>";
			} else {
				out += "<b>" + Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?addr="
						+ assetTransfer.getOwner().getAddress() + get_Lang(langObj) + ">" + assetTransfer.getOwner().getAddress()
						+ "</a><br>";
			}
		}

		if (assetTransfer.getRecipient().getPerson() != null) {
			out += "<b>" + Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?person="
					+ assetTransfer.getRecipient().getPerson().b.getKey() + get_Lang(langObj) + ">"
					+ assetTransfer.getRecipient().viewPerson() + "</a><br>";

		} else {

			out += "<b>" + Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?addr="
					+ assetTransfer.getRecipient().getAddress() + get_Lang(langObj) + ">" + assetTransfer.getRecipient().getAddress()
					+ "</a><br>";
		}


		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Asset", langObj) + ": </b>" +String.valueOf(Controller.getInstance()
				.getAsset(assetTransfer.getAbsKey()).toString());
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj(isCredit?"Credit":"Amount", langObj) + ": </b>" +assetTransfer.getAmount().toPlainString();
		return out;
	}

	private String genesis_Issue_Template_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		GenesisIssueTemplateRecord templateIssue =(GenesisIssueTemplateRecord)transaction;
		TemplateCls template = (TemplateCls)templateIssue.getItem();
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ": </b>" +template.getName();
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ": </b>" + template.getDescription();
		return out;
	}

	private String genesis_Issue_Asset_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		GenesisIssueAssetTransaction assetIssue =(GenesisIssueAssetTransaction)transaction;
		AssetCls asset = (AssetCls)assetIssue.getItem();
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ": </b>" +asset.getName();
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ": </b>" +asset.getDescription();
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Quantity", langObj) + ": </b>" +asset.getQuantity().toString();
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Scale", langObj) + ": </b>" + Lang.getInstance().translate_from_langObj(asset.getScale()+"", langObj) ;
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Asset Type", langObj) + ": </b>" + Lang.getInstance().translate_from_langObj(asset.viewAssetType()+"", langObj) ;
		return out;
	}

	private String genesis_Certify_Person_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		GenesisCertifyPersonRecord record = (GenesisCertifyPersonRecord)transaction;
		if (record.getRecipient().getPerson() != null) {
			out += "<b>" + Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?person="
					+ record.getRecipient().getPerson().b.getKey() + get_Lang(langObj) + ">"
					+ record.getRecipient().viewPerson() + "</a><br>";

		} else {

			out += "<b>" + Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?addr="
					+ record.getRecipient().getAddress() + get_Lang(langObj) + ">" + record.getRecipient().getAddress()
					+ "</a><br>";
		}



		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Person", langObj) + ": </b>" +String.valueOf(Controller.getInstance().getPerson(record.getKey()).toString());
		return out;
	}

	private String vate_On_Poll_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		VoteOnPollTransaction pollVote = (VoteOnPollTransaction)transaction;
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ": </b>" +pollVote.getPoll();
		out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Option", langObj) + ": </b>" + String.valueOf(pollVote.getOption());
		return out;
	}

	private String create_Poll_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		CreatePollTransaction pollCreation = (CreatePollTransaction)transaction;
		out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> "
				+ pollCreation.getPoll().getName() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
				+ pollCreation.getPoll().getDescription() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Options", langObj) + ":</b><br>";

		//OPTIONS

		PollOptionsTableModel pollOptionsTableModel = new PollOptionsTableModel(pollCreation.getPoll(),
				Controller.getInstance().getAsset(AssetCls.FEE_KEY));
		JTable table = Gui.createSortableTable(pollOptionsTableModel, 0);

		TableRowSorter<PollOptionsTableModel> sorter =  (TableRowSorter<PollOptionsTableModel>) table.getRowSorter();
		sorter.setComparator(PollOptionsTableModel.COLUMN_VOTES, new BigDecimalStringComparator());
		out += "<Table><tr><td>" + table.getColumnName(0) + "<td>" + table.getColumnName(1) + "<td>" + table.getColumnName(2) + "</tr> ";
		int row_Count = table.getRowCount();
		for (int i = 0; i<row_Count; i++){
			out += "<Table><tr><td>" + table.getValueAt(i, 0) + "<td>" + table.getValueAt(i, 1) + "<td>" + table.getValueAt(i, 2) + "</tr> ";
		}

		return out;
	}

	private String cancel_Order_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		CancelOrderTransaction orderCreation = (CancelOrderTransaction)transaction;
		out += orderCreation.toJson();
		return out;
	}

	private String create_Order_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		CreateOrderTransaction orderCreation = (CreateOrderTransaction)transaction;
		//tradeMap.getTradesByOrderID(new BigInteger(transaction.getSignature());
		//Order order = orderCreation.getOrder();
		out += "<b>" + Lang.getInstance().translate_from_langObj("Have", langObj) + ":</b> "
				+ orderCreation.getAmountHave().toPlainString() + " x "
				+ String.valueOf(orderCreation.getHaveAsset().toString()) + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Want", langObj) + ":</b> "
				+ orderCreation.getAmountWant().toPlainString() + " x "
				+ String.valueOf(orderCreation.getWantAsset().toString()) + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Price", langObj) + ":</b> "
				+ orderCreation.getPriceCalc().toPlainString() + " / " + orderCreation.getPriceCalcReverse().toPlainString() + "<br>";
		return out;
	}

	private String hash_Record_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		R_Hashes r_Hashes = (R_Hashes)transaction;
		out += "<b>" + Lang.getInstance().translate_from_langObj("URL", langObj) + ":</b> "
				+ new String(r_Hashes.getURL(), Charset.forName("UTF-8")) + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
				+ new String(r_Hashes.getData(), Charset.forName("UTF-8")) + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("HASHES", langObj) + ":</b> "
				+  String.join("<br />", r_Hashes.getHashesB58()) + "<br>";
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
					+  new String(setStatusToItem.getDescription(), Charset.forName("UTF-8")) + "<br>";
		}
		out += "<b>" + Lang.getInstance().translate_from_langObj("Item Name", langObj) + ":</b> "
				+ item.getItemTypeStr() + " - " + item.getItemSubType()
				+ ": " + item.getName() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Item Description", langObj) + ":</b> "
				+  item.getDescription() + "<br>";
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
					+ Controller.getInstance().getTemplate( r_Statement.getKey()).toString() + "<br>";
		}
		if (r_Statement.getData() != null) {
			String ss = (( r_Statement.isText() ) ? new String(r_Statement.getData(), Charset.forName("UTF-8")) : Converter.toHex(r_Statement.getData()));
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
		Transaction record = DCSet.getInstance().getTransactionFinalMap().getTransaction(vouchRecord.getVouchHeight(),
				vouchRecord.getVouchSeq());
		/*out += "<b>" + Lang.getInstance().translate_from_langObj("height-seq.", langObj) + ":</b> <a href=?tx="
				+  Base58.encode(record.getSignature()) + get_Lang(langObj) + ">" + vouchRecord.getVouchHeight() + "-"
				+ vouchRecord.getVouchSeq() + "</a><br>"; */
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> ";

		out += "<table id=statuses BORDER=0 cellpadding=15 cellspacing=0 width='800'  class='table table-striped' style='border: 1px solid #ddd; word-wrap: break-word;'><tr><td><td>";
		out += "<td>"+ get_HTML(record, langObj) + "</table><br>";
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

	private String issue_Template_HTML(Transaction transaction, JSONObject langObj) {
		// TODO Auto-generated method stub
		String out = "";
		IssueTemplateRecord templateIssue = (IssueTemplateRecord) transaction;
		out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> "
				+ templateIssue.getItem().getName() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
				+ templateIssue.getItem().getDescription() + "<br>";

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
				+ tr.getAssetKey() + get_Lang(langObj) + ">" + tr.getItem().viewName() + "</a><br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
				+ tr.getItem().getDescription() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Quantity", langObj) + ":</b> "
				+ ((AssetCls) tr.getItem()).getQuantity().toString() + "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Scale", langObj) + ":</b> "
				+ Lang.getInstance().translate_from_langObj(((AssetCls) tr.getItem()).getScale() + "", langObj)
				+ "<br>";
		out += "<b>" + Lang.getInstance().translate_from_langObj("Asset Type", langObj) + ":</b> "
				+ Lang.getInstance().translate_from_langObj(((AssetCls) tr.getItem()).viewAssetType() + "", langObj)
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
		if (tr.getAmount() != null) {
			out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Amount", langObj) + ":</b> "
					+ tr.getAmount().toPlainString() + " ("
					+ Controller.getInstance().getAsset(tr.getAbsKey()).getName() + ")";
		}

		if (!tr.getHead().equals(""))
			out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Title", langObj) + ":</b> " + tr.getHead();

		if (!tr.viewData().equals(""))
			out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Message", langObj) + ":</b> "
					+ tr.viewData();


		return out;

	}

	private String get_Lang(JSONObject langObj) {
		if (langObj == null)
			return "&lang=en";
		return "&lang=" + langObj.get("_lang_ISO_");

	}

	private String get_Vouches(Transaction transaction ){


		Statements_Vouch_Table_Model model = new Statements_Vouch_Table_Model(transaction);
		int row_count = model.getRowCount();
		if(row_count == 0) return "";
		String out = "<b>" + Lang.getInstance().translate_from_langObj("Certified", langObj) + ":</b> ";
		//langObj

		out += "<table id=statuses BORDER=0 cellpadding=15 cellspacing=0 width='800'  class='table table-striped' style='border: 1px solid #ddd; word-wrap: break-word;'><tr><td>"+ Lang.getInstance().translate_from_langObj("Transaction", langObj)+"<td>"+ Lang.getInstance().translate_from_langObj("Date", langObj)+"<td>"+ Lang.getInstance().translate_from_langObj("Creator", langObj)+"</tr>";
		for (int i = 0; i<row_count; i++){
			out += "<tr>";
			out += "<td><a href=?tx="+  Base58.encode(model.getTrancaction(i).getSignature())+ get_Lang(langObj) +  ">" + model.getTrancaction(i).getBlockHeight(DCSet.getInstance())+ "-" + model.getTrancaction(i).getSeqNo(DCSet.getInstance()) + "</a>";
			out +="<td>" + model.getValueAt(i, 0);
			out +="<td>";
			Transaction tr = model.getTrancaction(i);
			if (tr.getCreator().getPerson() != null) {
				out += "<a href=?person="	+ tr.getCreator().getPerson().b.getKey() + get_Lang(langObj) + ">"
						+ tr.getCreator().viewPerson() + "</a><br>";
			} else {
				out += "<a href=?addr="	+ tr.getCreator().getAddress() + get_Lang(langObj) + ">" + tr.getCreator().getAddress()
						+ "</a><br>";
			}


		}
		out += "</table>";
		return out;
	}


}
