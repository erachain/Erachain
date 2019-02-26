package org.erachain.core.blockexplorer;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.*;
import org.erachain.datachain.DCSet;
import org.erachain.gui.Gui;
import org.erachain.gui.items.statement.Statements_Vouch_Table_Model;
import org.erachain.gui.models.PollOptionsTableModel;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;
import org.erachain.utils.BigDecimalStringComparator;
import org.erachain.utils.Converter;
import org.erachain.utils.DateTimeFormat;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.nio.charset.Charset;
import java.util.*;

public class WEB_Transactions_HTML {
    private static WEB_Transactions_HTML instance;
    JSONObject langObj;


    public static WEB_Transactions_HTML getInstance() {
        if (instance == null) {
            instance = new WEB_Transactions_HTML();
        }

        return instance;
    }

    public HashMap get_HTML(Transaction transaction, JSONObject langObj) {
        this.langObj = langObj;
        List<Transaction> tt = new ArrayList<Transaction>();
        tt.add(transaction);
        LinkedHashMap json = BlockExplorer.getInstance().Transactions_JSON(null, tt);
        LinkedHashMap tras_json = (LinkedHashMap) ((LinkedHashMap) json.get("transactions")).get(0);

        HashMap output = new HashMap();

        String out = "<font size='+1'> <b>" + Lang.getInstance().translate_from_langObj("Transaction", langObj) + ": </b>" + tras_json.get("type");
        out += " (" + Lang.getInstance().translate_from_langObj("Block", langObj) + ": </b><a href=?block=" + tras_json.get("block") + get_Lang(langObj) + ">" + tras_json.get("block") + "</a>";
        out += ", " + Lang.getInstance().translate_from_langObj("seqNo", langObj) + ": </b><a href=?tx=" + tras_json.get("block") + "-" + tras_json.get("seqNo") + get_Lang(langObj) + ">" + tras_json.get("block") + "-" + tras_json.get("seqNo") + "</a> ) </font><br>";

        // она и так в заголовке будет
        //out += "<br><b>" + Lang.getInstance().translate_from_langObj("Type", langObj) + ": </b>" + tras_json.get("type_name");
        out += "<br><b>" + Lang.getInstance().translate_from_langObj("Confirmations", langObj) + ": </b>" + tras_json.get("confirmations");
        out += "<br><b>" + Lang.getInstance().translate_from_langObj("Date", langObj) + ": </b>" + tras_json.get("date");
        out += "<br><b>" + Lang.getInstance().translate_from_langObj("Size", langObj) + ": </b>" + tras_json.get("size");
        out += "<br><b>" + Lang.getInstance().translate_from_langObj("Publick Key", langObj) + ": </b>" + tras_json.get("publickey");
        out += "<br><b>" + Lang.getInstance().translate_from_langObj("Signature", langObj) + ": </b>" + tras_json.get("signature");
        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Fee", langObj) + ": </b>" + tras_json.get("fee");
        out += "<br> ";
        out += "<b>" + Lang.getInstance().translate_from_langObj("Creator", langObj) + ": </b><a href=?addr=" + tras_json.get("creator_addr") + get_Lang(langObj) + ">" + tras_json.get("creator") + "</a>";

        output.put("head", out);

        int type = transaction.getType();
        switch (type) {
            case Transaction.SEND_ASSET_TRANSACTION:
                output.put("body", r_Send_HTML(transaction, langObj));
                output.put("message", ((R_Send)transaction).viewData());

                break;
            case Transaction.ISSUE_ASSET_TRANSACTION:
                output.put("body", issue_Asset_HTML(transaction, langObj));
                output.put("message", ((Issue_ItemRecord)transaction).getItemDescription());
                break;
            case Transaction.ISSUE_PERSON_TRANSACTION:
                output.put("body", issue_Person_HTML(transaction, langObj));
                output.put("message", ((Issue_ItemRecord)transaction).getItemDescription());
                break;
            case Transaction.ISSUE_POLL_TRANSACTION:
                output.put("body", issue_Poll_HTML(transaction, langObj));
                output.put("message", ((Issue_ItemRecord)transaction).getItemDescription());
                break;
            case Transaction.ISSUE_IMPRINT_TRANSACTION:
                output.put("body", issue_Imprint_HTML(transaction, langObj));
                output.put("message", ((Issue_ItemRecord)transaction).getItemDescription());
                break;
            case Transaction.ISSUE_TEMPLATE_TRANSACTION:
                output.put("body", issue_Template_HTML(transaction, langObj));
                output.put("message", ((Issue_ItemRecord)transaction).getItemDescription());
                break;
            case Transaction.ISSUE_STATUS_TRANSACTION:
                output.put("body", issue_Status_HTML(transaction, langObj));
                output.put("message", ((Issue_ItemRecord)transaction).getItemDescription());
                break;
            case Transaction.ISSUE_UNION_TRANSACTION:
                output.put("body", issue_Union_HTML(transaction, langObj));
                output.put("message", ((Issue_ItemRecord)transaction).getItemDescription());
                break;
            case Transaction.VOUCH_TRANSACTION:
                output.put("body", vouch_HTML(transaction, langObj));
                break;
            case Transaction.SIGN_NOTE_TRANSACTION:
                output.put("body", sign_Note_HTML(transaction, langObj));
                break;
            case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:
                output.put("body", serttify_Pub_Key_HTML(transaction, langObj));
                break;
            case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:
                output.put("body", set_Status_HTML(transaction, langObj));
                break;
            case Transaction.HASHES_RECORD:
                output.put("body", hash_Record_HTML(transaction, langObj));
                break;
            case Transaction.CREATE_ORDER_TRANSACTION:
                output.put("body", create_Order_HTML(transaction, langObj));
                break;
            case Transaction.CANCEL_ORDER_TRANSACTION:
                output.put("body", cancel_Order_HTML(transaction, langObj));
                break;
            case Transaction.CREATE_POLL_TRANSACTION:
                output.put("body", create_Poll_HTML(transaction, langObj));
                break;
            case Transaction.VOTE_ON_POLL_TRANSACTION:
                output.put("body", vote_On_Poll_HTML(transaction, langObj));
                break;
            case Transaction.GENESIS_CERTIFY_PERSON_TRANSACTION:
                output.put("body", genesis_Certify_Person_HTML(transaction, langObj));
                break;
            case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:
                output.put("body", genesis_Issue_Asset_HTML(transaction, langObj));
                break;
            case Transaction.GENESIS_ISSUE_TEMPLATE_TRANSACTION:
                output.put("body", genesis_Issue_Template_HTML(transaction, langObj));
                break;
            case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:
                output.put("body", genesis_Certify_Person_HTML(transaction, langObj));
                break;
            case Transaction.GENESIS_SEND_ASSET_TRANSACTION:
                output.put("body", genesis_Send_Asset_HTML(transaction, langObj));
                break;
            default:
                output.put("body", transaction.toJson());
        }

        output.put("vouches", get_Vouches(transaction,langObj));

        return output;
    }

    private String genesis_Send_Asset_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisTransferAssetTransaction assetTransfer = (GenesisTransferAssetTransaction) transaction;
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


        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Asset", langObj) + ": </b>" + String.valueOf(Controller.getInstance()
                .getAsset(assetTransfer.getAbsKey()).toString());
        out += "<BR><b>" + Lang.getInstance().translate_from_langObj(isCredit ? "Credit" : "Amount", langObj) + ": </b>" + assetTransfer.getAmount().toPlainString();
        return out;
    }

    private String genesis_Issue_Template_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisIssueTemplateRecord templateIssue = (GenesisIssueTemplateRecord) transaction;
        TemplateCls template = (TemplateCls) templateIssue.getItem();
        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ": </b>" + template.getName();
        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ": </b>" + template.getDescription();
        return out;
    }

    private String genesis_Issue_Asset_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisIssueAssetTransaction assetIssue = (GenesisIssueAssetTransaction) transaction;
        AssetCls asset = (AssetCls) assetIssue.getItem();
        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ": </b>" + asset.getName();
        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ": </b>"
                + Lang.getInstance().translate_from_langObj(asset.viewDescription(), langObj);
        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Quantity", langObj) + ": </b>" + asset.getQuantity().toString();
        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Scale", langObj) + ": </b>" + Lang.getInstance().translate_from_langObj(asset.getScale() + "", langObj);
        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Asset Type", langObj) + ": </b>" + Lang.getInstance().translate_from_langObj(asset.viewAssetType() + "", langObj);
        return out;
    }

    private String genesis_Certify_Person_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisCertifyPersonRecord record = (GenesisCertifyPersonRecord) transaction;
        if (record.getRecipient().getPerson() != null) {
            out += "<b>" + Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?person="
                    + record.getRecipient().getPerson().b.getKey() + get_Lang(langObj) + ">"
                    + record.getRecipient().viewPerson() + "</a><br>";

        } else {

            out += "<b>" + Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?addr="
                    + record.getRecipient().getAddress() + get_Lang(langObj) + ">" + record.getRecipient().getAddress()
                    + "</a><br>";
        }


        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Person", langObj) + ": </b>" + String.valueOf(Controller.getInstance().getPerson(record.getKey()).toString());
        return out;
    }

    private String vote_On_Poll_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        String out = "";
        VoteOnPollTransaction pollVote = (VoteOnPollTransaction) transaction;
        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ": </b>" + pollVote.getPoll();
        out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Option", langObj) + ": </b>" + String.valueOf(pollVote.getOption());
        return out;
    }

    private String create_Poll_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        String out = "";
        CreatePollTransaction pollCreation = (CreatePollTransaction) transaction;
        out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> "
                + pollCreation.getPoll().getName() + "<br>";
        out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
                + pollCreation.getPoll().getDescription() + "<br>";
        out += "<b>" + Lang.getInstance().translate_from_langObj("Options", langObj) + ":</b><br>";

        //OPTIONS

        PollOptionsTableModel pollOptionsTableModel = new PollOptionsTableModel(pollCreation.getPoll(),
                Controller.getInstance().getAsset(AssetCls.FEE_KEY));
        JTable table = Gui.createSortableTable(pollOptionsTableModel, 0);

        TableRowSorter<PollOptionsTableModel> sorter = (TableRowSorter<PollOptionsTableModel>) table.getRowSorter();
        sorter.setComparator(PollOptionsTableModel.COLUMN_VOTES, new BigDecimalStringComparator());
        out += "<Table><tr><td>" + table.getColumnName(0) + "<td>" + table.getColumnName(1) + "<td>" + table.getColumnName(2) + "</tr> ";
        int row_Count = table.getRowCount();
        for (int i = 0; i < row_Count; i++) {
            out += "<Table><tr><td>" + table.getValueAt(i, 0) + "<td>" + table.getValueAt(i, 1) + "<td>" + table.getValueAt(i, 2) + "</tr> ";
        }

        return out;
    }

    private String cancel_Order_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        String out = "";
        CancelOrderTransaction orderCreation = (CancelOrderTransaction) transaction;
        out += orderCreation.toJson();
        return out;
    }

    private String create_Order_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        String out = "";
        CreateOrderTransaction orderCreation = (CreateOrderTransaction) transaction;
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
        R_Hashes r_Hashes = (R_Hashes) transaction;
        out += "<b>" + Lang.getInstance().translate_from_langObj("URL", langObj) + ":</b> "
                + new String(r_Hashes.getURL(), Charset.forName("UTF-8")) + "<br>";
        out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b> "
                + new String(r_Hashes.getData(), Charset.forName("UTF-8")) + "<br>";
        out += "<b>" + Lang.getInstance().translate_from_langObj("HASHES", langObj) + ":</b> "
                + String.join("<br />", r_Hashes.getHashesB58()) + "<br>";
        return out;
    }

    private String set_Status_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        String out = "";
        R_SetStatusToItem setStatusToItem = (R_SetStatusToItem) transaction;
        ItemCls item = Controller.getInstance().getItem(setStatusToItem.getItemType(), setStatusToItem.getItemKey());
        long status_key = setStatusToItem.getKey();
        StatusCls status = Controller.getInstance().getItemStatus(status_key);
        out += "<b>" + Lang.getInstance().translate_from_langObj("Status Name", langObj) + ":</b> "
                + status.getName() + "<br>";
        out += "<b>" + Lang.getInstance().translate_from_langObj("Status Description", langObj) + ":</b> " + status.getDescription() + "<br>";
        long beginDate = setStatusToItem.getBeginDate();
        long endDate = setStatusToItem.getEndDate();
        out += "<b>" + Lang.getInstance().translate_from_langObj("From - To", langObj) + ":</b> "
                + (beginDate == Long.MIN_VALUE ? "?" : DateTimeFormat.timestamptoString(beginDate))
                + " - " + (endDate == Long.MAX_VALUE ? "?" : DateTimeFormat.timestamptoString(endDate)) + "<br>";
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
        person = Controller.getInstance().getPerson(record.getKey());
        out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj) + ":</b> <a href=?person="
                + person.getKey() + get_Lang(langObj) + ">" + person.getName() + "</a><br>";
        out += "<b>" + Lang.getInstance().translate_from_langObj("End Days", langObj) + ":</b> "
                + record.getAddDay() + "<br>";
        int i = 0;
        for (String address : record.getSertifiedPublicKeysB58()) {
            out += "<b>   " + Lang.getInstance().translate_from_langObj("Account", langObj) + " " + ++i + ":</b> " + address + "<br>";
        }
        return out;
    }

    private String sign_Note_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        String out = "";
        R_SignNote r_Statement = (R_SignNote) transaction;
        if (r_Statement.getKey() > 0) {
            out += "<b>" + Lang.getInstance().translate_from_langObj("Key", langObj) + ":</b> "
                    + Controller.getInstance().getTemplate(r_Statement.getKey()).toString() + "<br>";
        }
        if (r_Statement.getData() != null) {
            String ss = ((r_Statement.isText()) ? new String(r_Statement.getData(), Charset.forName("UTF-8")) : Converter.toHex(r_Statement.getData()));
            ss = "<div  style='word-wrap: break-word;'>" + ss;
            out += "<b>" + Lang.getInstance().translate_from_langObj("Message", langObj) + ":</b> "
                    + ss + "<br>";
        }
        return out;
    }

    private String vouch_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        String out = "";
        R_Vouch vouchRecord = (R_Vouch) transaction;
        Transaction record = DCSet.getInstance().getTransactionFinalMap().get(vouchRecord.getVouchHeight(),
                vouchRecord.getVouchSeqNo());
		/*out += "<b>" + Lang.getInstance().translate_from_langObj("height-seqNo", langObj) + ":</b> <a href=?tx="
				+  Base58.encode(record.getSignature()) + get_Lang(langObj) + ">" + vouchRecord.getVouchHeight() + "-"
				+ vouchRecord.getVouchSeqNo() + "</a><br>"; */
        //out += "<b>" + Lang.getInstance().translate_from_langObj("Description", langObj) + ":</b>";
      //  out += "<b>" + Lang.getInstance().translate_from_langObj("Vouch Record", langObj) + ":</b> ";
        out += "<b>"+ Lang.getInstance().translate_from_langObj("Vouch Record", langObj) + ": </b> <a href='?tx=" + record.viewSignature() + get_Lang(langObj) + "'> " ;
        out += record.getBlockHeight()+ "-" + record.getSeqNo() +"</a> <br>";
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
                + person.getDescription() + "<br>";
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
        AssetCls asset = (AssetCls)tr.getItem();
        out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj)
                + ":</b> <a href=?asset=" + asset.getKey()
                + get_Lang(langObj) + ">" + asset.viewName() + "</a><br>";
        out += "<b>" + Lang.getInstance().translate_from_langObj("Quantity", langObj) + ":</b> "
                + asset.getQuantity().toString() + "<br>";
        out += "<b>" + Lang.getInstance().translate_from_langObj("Scale", langObj) + ":</b> "
                + Lang.getInstance().translate_from_langObj(asset.getScale() + "", langObj)
                + "<br>";
        out += "<b>" + Lang.getInstance().translate_from_langObj("Asset Type", langObj) + ":</b> "
                + Lang.getInstance().translate_from_langObj(asset.viewAssetType() + "", langObj)
                + "<br>";

        return out;
    }

    private String issue_Poll_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        IssuePollRecord tr = (IssuePollRecord) transaction;
        String out = "";
        ItemCls item = tr.getItem();
        out += "<b>" + Lang.getInstance().translate_from_langObj("Name", langObj)
                + ":</b> <a href=?poll=" + item.getKey()
                + get_Lang(langObj) + ">" + item.viewName() + "</a><br>";

        return out;
    }

    private String r_Send_HTML(Transaction transaction, JSONObject langObj) {
        // TODO Auto-generated method stub
        R_Send tr = (R_Send) transaction;
        String out = "";

        out += "<b>" + Lang.getInstance().translate_from_langObj("Recipient", langObj) + ":</b> <a href=?addr="
                + tr.getRecipient().getAddress() + get_Lang(langObj) + ">" + tr.getRecipient().getPersonAsString()
                + "</a><br>";

        if (tr.getAmount() != null) {
            out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Amount", langObj) + ":</b> "
                    + tr.getAmount().toPlainString() + " ("
                    + Controller.getInstance().getAsset(tr.getAbsKey()).getName() + ")";
        }

        if (!tr.getHead().equals(""))
            out += "<BR><b>" + Lang.getInstance().translate_from_langObj("Title", langObj) + ":</b> " + tr.getHead();

        return out;

    }

    private String get_Lang(JSONObject langObj) {
        if (langObj == null)
            return "&lang=en";
        return "&lang=" + langObj.get("_lang_ISO_");

    }

    public String get_Vouches(Transaction transaction, JSONObject langObj) {


        Statements_Vouch_Table_Model model = new Statements_Vouch_Table_Model(transaction);
        int row_count = model.getRowCount();
        if (row_count == 0) return "";
        String out = "<b>" + Lang.getInstance().translate_from_langObj("Certified", langObj) + ":</b> ";
        //langObj

        out += "<table id=statuses BORDER=0 cellpadding=15 cellspacing=0 width='800'  class='table table-striped' style='border: 1px solid #ddd; word-wrap: break-word;'><tr><td>" + Lang.getInstance().translate_from_langObj("Transaction", langObj) + "<td>" + Lang.getInstance().translate_from_langObj("Date", langObj) + "<td>" + Lang.getInstance().translate_from_langObj("Creator", langObj) + "</tr>";
        for (int i = 0; i < row_count; i++) {
            out += "<tr>";
            out += "<td><a href=?tx=" + Base58.encode(model.getTrancaction(i).getSignature()) + get_Lang(langObj) + ">" + model.getTrancaction(i).getBlockHeight() + "-" + model.getTrancaction(i).getSeqNo() + "</a>";
            out += "<td>" + model.getValueAt(i, 0);
            out += "<td>";
            Transaction tr = model.getTrancaction(i);
            if (tr.getCreator().getPerson() != null) {
                out += "<a href=?person=" + tr.getCreator().getPerson().b.getKey() + get_Lang(langObj) + ">"
                        + tr.getCreator().viewPerson() + "</a><br>";
            } else {
                out += "<a href=?addr=" + tr.getCreator().getAddress() + get_Lang(langObj) + ">" + tr.getCreator().getAddress()
                        + "</a><br>";
            }


        }
        out += "</table>";
        return out;
    }


}
