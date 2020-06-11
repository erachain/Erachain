package org.erachain.core.blockexplorer;

import org.apache.commons.net.util.Base64;
import org.erachain.controller.Controller;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.*;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMapImpl;
import org.erachain.lang.Lang;
import org.erachain.utils.Converter;
import org.erachain.utils.DateTimeFormat;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class WebTransactionsHTML {
    private static WebTransactionsHTML instance;
    JSONObject langObj;


    public static WebTransactionsHTML getInstance() {
        if (instance == null) {
            instance = new WebTransactionsHTML();
        }

        return instance;
    }

    public HashMap get_HTML(Transaction transaction, JSONObject langObj) {
        // TODO: надо переделать тут так чтобы на строне клиента HTML собиралось с его локальным временм из timestamp

        transaction.setDC(DCSet.getInstance());

        this.langObj = langObj;
        List<Transaction> tt = new ArrayList<Transaction>();
        boolean wiped = transaction.isWiped();
        tt.add(transaction);
        LinkedHashMap json = new LinkedHashMap();
        BlockExplorer.getInstance().transactionsJSON(json, null, tt, 0, BlockExplorer.pageSize, "tx");
        LinkedHashMap tras_json = (LinkedHashMap) ((LinkedHashMap) ((LinkedHashMap) json.get("Transactions"))
                .get("transactions")).get(0);

        HashMap output = new HashMap();

        String out = "<font size='+1'> <b>" + Lang.getInstance().translateFromLangObj("Transaction", langObj) + ": </b>" + tras_json.get("type");
        out += " (" + Lang.getInstance().translateFromLangObj("Block", langObj) + ": </b><a href=?block=" + tras_json.get("block") + get_Lang() + ">" + tras_json.get("block") + "</a>";
        out += ", " + Lang.getInstance().translateFromLangObj("seqNo", langObj) + ": </b><a href=?tx=" + tras_json.get("block") + "-" + tras_json.get("seqNo") + get_Lang() + ">" + tras_json.get("block") + "-" + tras_json.get("seqNo") + "</a> ) </font><br>";

        // она и так в заголовке будет
        //out += "<br><b>" + Lang.getInstance().translateFromLangObj("Type", langObj) + ": </b>" + tras_json.get("type_name");
        out += "<br><b>" + Lang.getInstance().translateFromLangObj("Confirmations", langObj) + ": </b>" + transaction.getConfirmations(DCSet.getInstance());

        if (!(transaction instanceof RCalculated)) {
            out += "<br><b>" + Lang.getInstance().translateFromLangObj("Size", langObj) + ": </b>" + tras_json.get("size");
            out += "<br><b>" + Lang.getInstance().translateFromLangObj("Publick Key", langObj) + ": </b>" + tras_json.get("publickey");
            out += "<br><b>" + Lang.getInstance().translateFromLangObj("Signature", langObj) + ": </b>" + tras_json.get("signature");
            out += "<BR><b>" + Lang.getInstance().translateFromLangObj("Fee", langObj) + ": </b>" + tras_json.get("fee");
            if (wiped) {
                out += "<BR><b>" + Lang.getInstance().translateFromLangObj("WIPED", langObj) + ": </b>" + "true";
            }
            out += "<br> ";
            out += "<b>" + Lang.getInstance().translateFromLangObj("Creator", langObj)
                    + ": </b><a href=?address=" + tras_json.get("creator_addr") + get_Lang() + ">" + tras_json.get("creator") + "</a>";
        }

        output.put("head", out);
        output.put("timestampLabel", Lang.getInstance().translateFromLangObj("Date", langObj));
        output.put("timestamp", transaction.getTimestamp());

        if (wiped)
            return output;

        int type = transaction.getType();
        switch (type) {
            case Transaction.CALCULATED_TRANSACTION:
                output.put("body", r_Calculated_HTML(transaction));
                break;
            case Transaction.SEND_ASSET_TRANSACTION:
                output.put("body", r_Send_HTML(transaction));
                output.put("message", ((RSend) transaction).viewData());
                break;
            case Transaction.ISSUE_ASSET_TRANSACTION:
                output.put("body", issue_Asset_HTML(transaction));
                output.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.ISSUE_PERSON_TRANSACTION:
                output.put("body", issue_Person_HTML(transaction));
                output.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.ISSUE_POLL_TRANSACTION:
                output.put("body", issue_Poll_HTML(transaction));
                output.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.ISSUE_IMPRINT_TRANSACTION:
                output.put("body", issue_Imprint_HTML(transaction));
                output.put("message", ((Itemable) transaction));
                break;
            case Transaction.ISSUE_TEMPLATE_TRANSACTION:
                output.put("body", issue_Template_HTML(transaction));
                output.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.ISSUE_STATUS_TRANSACTION:
                output.put("body", issue_Status_HTML(transaction));
                output.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.ISSUE_UNION_TRANSACTION:
                output.put("body", issue_Union_HTML(transaction));
                output.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.VOUCH_TRANSACTION:
                output.put("body", vouch_HTML(transaction));
                break;
            case Transaction.SIGN_NOTE_TRANSACTION:
                output.put("body", sign_Note_HTML(transaction));
                break;
            case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:
                output.put("body", sertify_Pub_Key_HTML(transaction));
                break;
            case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:
                output.put("body", set_Status_HTML(transaction));
                break;
            case Transaction.HASHES_RECORD:
                output.put("body", hash_Record_HTML(transaction));
                break;
            case Transaction.CREATE_ORDER_TRANSACTION:
                output.put("body", create_Order_HTML(transaction));
                break;
            case Transaction.CANCEL_ORDER_TRANSACTION:
                output.put("body", cancel_Order_HTML(transaction));
                break;
            case Transaction.VOTE_ON_ITEM_POLL_TRANSACTION:
                output.put("body", vote_On_Item_Poll_HTML(transaction));
                break;
            case Transaction.GENESIS_CERTIFY_PERSON_TRANSACTION:
                output.put("body", genesis_Certify_Person_HTML(transaction));
                break;
            case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:
                output.put("body", genesis_Issue_Asset_HTML(transaction));
                output.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.GENESIS_ISSUE_TEMPLATE_TRANSACTION:
                output.put("body", genesis_Issue_Template_HTML(transaction));
                output.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:
                output.put("body", genesisIssue_Person_HTML(transaction));
                output.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.GENESIS_SEND_ASSET_TRANSACTION:
                output.put("body", genesis_Send_Asset_HTML(transaction));
                break;
            default:
                output.put("body", transaction.toJson());
        }

        output.put("vouches", get_Vouches(transaction));

        return output;
    }

    private String get_Lang() {
        if (langObj == null)
            return "&lang=en";
        return "&lang=" + langObj.get("_lang_ISO_");

    }

    public String itemNameHTML(ItemCls item) {
        String out = "<a href=?" + item.getItemTypeName() + "=" + item.getKey(DCSet.getInstance()) + get_Lang() + ">";
        if (item.getKey() >= item.getStartKey()) {
            out += "[" + item.getKey() + "] ";
        }
        if (item.getIcon() != null && item.getIcon().length > 0) {
            out += "<img src='data:image/gif;base64," + Base64.encodeBase64String(item.getIcon()) + "' style='width:1.8em;'/> ";
        }
        out += item.viewName() + "</a>";

        return out;
    }

    private String getItemDescription(Itemable itemIssueTx) {
        ItemCls item = itemIssueTx.getItem();

        if (item.getKey() < item.getStartKey()) {
            return Lang.getInstance().translateFromLangObj(item.viewDescription(), langObj);
        }

        return item.viewDescription();
    }

    private String genesis_Send_Asset_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisTransferAssetTransaction assetTransfer = (GenesisTransferAssetTransaction) transaction;
        if (assetTransfer.getCreator() != null) {
            out += Lang.getInstance().translateFromLangObj("Creator", langObj) + ": <a href=?address="
                    + assetTransfer.getCreator().getAddress() + get_Lang() + "><b>" + assetTransfer.getCreator().getPersonAsString()
                    + "</b></a>";
        } else {
            out += "<b>" + Lang.getInstance().translateFromLangObj("Creator", langObj) + ": GENESIS";
        }

        out += "<br>" + Lang.getInstance().translateFromLangObj("Recipient", langObj) + ": <a href=?address="
                + assetTransfer.getRecipient().getAddress() + get_Lang() + "><b>" + assetTransfer.getRecipient().getPersonAsString()
                + "</b></a><br>";

        out += "<br>" + Lang.getInstance().translateFromLangObj(assetTransfer.viewActionTypeWas(), langObj)
                + ": <b>" + assetTransfer.getAmount().toPlainString() + " x "
                + itemNameHTML(Controller.getInstance().getAsset(assetTransfer.getAbsKey())) + "</b>";


        return out;
    }

    private String genesis_Issue_Template_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisIssueTemplateRecord templateIssue = (GenesisIssueTemplateRecord) transaction;
        TemplateCls template = (TemplateCls) templateIssue.getItem();
        out += "<br>" + Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>" + itemNameHTML(template) + "</b>";
        return out;
    }

    private String genesis_Issue_Asset_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisIssueAssetTransaction assetIssue = (GenesisIssueAssetTransaction) transaction;
        AssetCls asset = (AssetCls) assetIssue.getItem();
        out += "<BR>" + Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>" + itemNameHTML(asset) + "</b>";

        out += "<BR><b>" + Lang.getInstance().translateFromLangObj("Quantity", langObj) + ": </b>" + asset.getQuantity();
        out += "<BR><b>" + Lang.getInstance().translateFromLangObj("Scale", langObj) + ": </b>" + Lang.getInstance().translateFromLangObj(asset.getScale() + "", langObj);
        out += "<BR><b>" + Lang.getInstance().translateFromLangObj("Asset Type", langObj) + ": </b>" + Lang.getInstance().translateFromLangObj(asset.viewAssetType() + "", langObj);

        return out;
    }

    private String genesisIssue_Person_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        IssuePersonRecord personIssue = (IssuePersonRecord) transaction;
        PersonCls person = (PersonCls) personIssue.getItem();

        String out = "";
        out += "<BR>" + Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>" + itemNameHTML(person) + "</b><br>";

        out += "<b>" + Lang.getInstance().translateFromLangObj("Birthday", langObj) + ":</b> "
                + person.getBirthdayStr() + "<br>";
        out += "<b>" + Lang.getInstance().translateFromLangObj("Gender", langObj) + ":</b> ";
        if (person.getGender() == 0)
            out += Lang.getInstance().translateFromLangObj("Male", langObj);
        if (person.getGender() == 1)
            out += Lang.getInstance().translateFromLangObj("Female", langObj);
        out += "<br>";
        //out += "<b>" + Lang.getInstance().translateFromLangObj("Description", langObj) + ":</b> "
        //        + person.getDescription() + "<br>";
        if (person.getOwner().getPerson() != null) {
            // out += "<b>" + Lang.getInstance().translateFromLangObj("Owner",
            // langObj) + ":</b> <a href=?person="
            // +person.getOwner().getPerson().b.getKey()+ get_Lang(langObj) +
            // ">" + person.getOwner().viewPerson() +"</a><br>";
        } else {
            // out += "<b>" +Lang.getInstance().translateFromLangObj("Owner",
            // langObj) + ":</b> <a href=?address=" +
            // person.getOwner().getAddress() + get_Lang(langObj) + ">" +
            // person.getOwner().getAddress() +"</a><br>";
        }
        // out += "<b>" + Lang.getInstance().translateFromLangObj("Public
        // Key", langObj) + ":</b> " + person.getOwner().getBase58() +"<br>";
        return out;
    }

    private String genesis_Certify_Person_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisCertifyPersonRecord record = (GenesisCertifyPersonRecord) transaction;
        out += "<b>" + Lang.getInstance().translateFromLangObj("Recipient", langObj) + ":</b> <a href=?address="
                + record.getRecipient().getAddress() + get_Lang() + ">" + record.getRecipient().getPersonAsString()
                + "</a><br>";

        out += "<BR>" + Lang.getInstance().translateFromLangObj("Person", langObj) + ": <b>"
                + itemNameHTML(Controller.getInstance().getPerson(record.getKey())) + "</b>";
        return out;
    }

    private String vote_On_Item_Poll_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        VoteOnItemPollTransaction pollVote = (VoteOnItemPollTransaction) transaction;
        out += "<BR>" + Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>" + itemNameHTML(pollVote.getItem()) + "</b>";
        out += "<BR>" + Lang.getInstance().translateFromLangObj("Option", langObj) + ": <b>" + pollVote.viewOption() + "</b>";
        return out;
    }

    private String cancel_Order_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        CancelOrderTransaction cancelOrder = (CancelOrderTransaction) transaction;
        Long key = DCSet.getInstance().getTransactionFinalMapSigns().get(cancelOrder.getorderSignature());
        if (key == null) {
            out += cancelOrder.toJson();
        }

        CreateOrderTransaction createOrder = (CreateOrderTransaction) DCSet.getInstance().getTransactionFinalMap().get(key);

        out += "</br><h3>" + Lang.getInstance().translateFromLangObj("Order to Cancel", langObj) + "</h3>";
        if (createOrder == null) {
            out += "not found" + " : " + cancelOrder.viewSignature();
        } else {
            out += create_Order_HTML(createOrder);
        }

        return out;
    }

    private String create_Order_HTML(Transaction transaction) {
        // TODO Auto-generated method stub

        String out = "";

        CreateOrderTransaction orderCreation = (CreateOrderTransaction) transaction;

        Long refDB = orderCreation.getDBRef();
        Order order = null;
        String status;
        if (DCSet.getInstance().getOrderMap().contains(refDB)) {
            order = DCSet.getInstance().getOrderMap().get(refDB);
            status = "Active";
        } else if (DCSet.getInstance().getCompletedOrderMap().contains(refDB)) {
            order = DCSet.getInstance().getCompletedOrderMap().get(refDB);
            if (order.isFulfilled()) {
                status = "Completed";
            } else {
                status = "Canceled";
            }
        } else {
            status = "Unknown";
        }

        out += "<h4><a href='?order=" + Transaction.viewDBRef(refDB) + get_Lang() + "'>" + Lang.getInstance().translateFromLangObj(status, langObj) + "</a></h4>";

        out += Lang.getInstance().translateFromLangObj("Have", langObj) + ": <b>"
                + orderCreation.getAmountHave().toPlainString() + " x "
                + itemNameHTML(orderCreation.getHaveAsset()) + "</b>"
                + (order != null ? "<br>" + Lang.getInstance().translateFromLangObj("Fulfilled", langObj)
                + ": <b>" + order.getFulfilledHave().toPlainString() + "</b>" : "")
                + "<br>";
        out += Lang.getInstance().translateFromLangObj("Want", langObj) + ": <b>"
                + orderCreation.getAmountWant().toPlainString() + " x "
                + itemNameHTML(orderCreation.getWantAsset()) + "</b>"
                + (order != null ? "<br>" + Lang.getInstance().translateFromLangObj("Fulfilled", langObj)
                + ": <b>" + order.getFulfilledWant().toPlainString() + "</b>" : "")
                + "<br>";
        out += Lang.getInstance().translateFromLangObj("Price", langObj) + ": <b>"
                + orderCreation.makeOrder().calcPrice().toPlainString()
                + " / " + orderCreation.makeOrder().calcPriceReverse().toPlainString() + "</b><br>";

        return out;
    }

    private String hash_Record_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        RHashes hashesTx = (RHashes) transaction;
        String url = new String(hashesTx.getURL(), StandardCharsets.UTF_8);
        URL linkURL;
        boolean urlForUse = false;
        try {
            linkURL = new URL(url);

            if (!url.isEmpty()) {
                if (url.charAt(url.length() - 1) == '=' || // as query parameter
                        url.charAt(url.length() - 1) == '/' || // af path parameter
                        url.charAt(url.length() - 1) == '#') { // as  anchor
                    urlForUse = true;
                } else {
                    out += "<b>" + Lang.getInstance().translateFromLangObj("URL", langObj) + ":</b> "
                            + "<a href='" + linkURL.toString() + "'>" + url + "</a><br>";
                }
            }
        } catch (Exception e) {
            linkURL = null;
            out += "<b>" + Lang.getInstance().translateFromLangObj("Title", langObj) + ":</b> "
                    + url + "<br>";
        }

        out += "<b>" + Lang.getInstance().translateFromLangObj("HASHES", langObj) + ":</b> ";
        int count = 0;

        for (byte[] hash : hashesTx.getHashes()) {
            String hash58 = Base58.encode(hash);
            out += "<br>" + ++count + " <a href=?q=" + hash58 + BlockExplorer.get_Lang(langObj) + "&search=transactions><b>" + hash58 + "</b></a>";
            if (urlForUse) {
                out += " - <a href='" + linkURL.toString() + hash58 + "' class='button ll-blue-bgc'>" + Lang.getInstance().translateFromLangObj("Open", langObj) + "</a>";
            }
        }

        out += "<br><b>" + Lang.getInstance().translateFromLangObj("Description", langObj) + ":</b><br>"
                + new String(hashesTx.getData(), StandardCharsets.UTF_8) + "<br>";

        return out;
    }

    private String set_Status_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        RSetStatusToItem setStatusToItem = (RSetStatusToItem) transaction;
        ItemCls item = Controller.getInstance().getItem(setStatusToItem.getItemType(), setStatusToItem.getItemKey());
        long status_key = setStatusToItem.getKey();
        StatusCls status = Controller.getInstance().getItemStatus(status_key);
        out += "<br>" + Lang.getInstance().translateFromLangObj("Status Name", langObj) + ":<b> "
                + itemNameHTML(status) + "</b><br>";
        long beginDate = setStatusToItem.getBeginDate();
        long endDate = setStatusToItem.getEndDate();
        out += "<b>" + Lang.getInstance().translateFromLangObj("From - To", langObj) + ":</b> "
                + (beginDate == Long.MIN_VALUE ? "?" : DateTimeFormat.timestamptoString(beginDate))
                + " - " + (endDate == Long.MAX_VALUE ? "?" : DateTimeFormat.timestamptoString(endDate)) + "<br>";
        if (setStatusToItem.getValue1() != 0) {
            out += "<b>" + Lang.getInstance().translateFromLangObj("Value", langObj) + " 1:</b> "
                    + setStatusToItem.getValue1() + "<br>";
        }
        if (setStatusToItem.getValue2() != 0) {
            out += "<b>" + Lang.getInstance().translateFromLangObj("Value", langObj) + " 2:</b> "
                    + setStatusToItem.getValue2() + "<br>";
        }
        if (setStatusToItem.getData1() != null) {
            out += "<b>" + Lang.getInstance().translateFromLangObj("DATA", langObj) + " 1:</b> "
                    + new String(setStatusToItem.getData1(), StandardCharsets.UTF_8) + "<br>";
        }
        if (setStatusToItem.getData2() != null) {
            out += "<b>" + Lang.getInstance().translateFromLangObj("DATA", langObj) + " 2:</b> "
                    + new String(setStatusToItem.getData2(), StandardCharsets.UTF_8) + "<br>";
        }
        if (setStatusToItem.getRefParent() != 0l) {
            out += "<b>" + Lang.getInstance().translateFromLangObj("Parent", langObj) + ":</b> "
                    + setStatusToItem.viewRefParent() + "<br>";
        }
        out += Lang.getInstance().translateFromLangObj("Item Name", langObj) + ":</b> "
                + item.getItemTypeName() + " - " + item.getItemSubType()
                + ": " + itemNameHTML(item) + "<br>";
        return out;
    }

    private String sertify_Pub_Key_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        RSertifyPubKeys record = (RSertifyPubKeys) transaction;
        PersonCls person;
        person = Controller.getInstance().getPerson(record.getKey());
        out += Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>"
                + itemNameHTML(person) + "</b><br>";
        out += "<b>" + Lang.getInstance().translateFromLangObj("End Days", langObj) + ":</b> "
                + record.getAddDay() + "<br>";
        int i = 0;
        for (String address : record.getSertifiedPublicKeysB58()) {
            out += "<b>   " + Lang.getInstance().translateFromLangObj("Key", langObj) + " " + ++i + ":</b> " + address + "<br>";
        }
        return out;
    }

    private String sign_Note_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        RSignNote r_Statement = (RSignNote) transaction;
        if (r_Statement.getKey() > 0) {
            out += Lang.getInstance().translateFromLangObj("Key", langObj) + ": <b>"
                    + itemNameHTML(Controller.getInstance().getTemplate(r_Statement.getKey())) + "</b><br>";
        }
        if (r_Statement.getData() != null) {
            String ss = ((r_Statement.isText()) ? new String(r_Statement.getData(), StandardCharsets.UTF_8) : Converter.toHex(r_Statement.getData()));
            ss = "<div  style='word-wrap: break-word;'>" + ss;
            out += "<b>" + Lang.getInstance().translateFromLangObj("Message", langObj) + ":</b> "
                    + ss + "<br>";
        }
        return out;
    }

    private String vouch_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        RVouch vouchRecord = (RVouch) transaction;
        Transaction record = DCSet.getInstance().getTransactionFinalMap().get(vouchRecord.getVouchHeight(),
                vouchRecord.getVouchSeqNo());
		/*out += "<b>" + Lang.getInstance().translateFromLangObj("height-seqNo", langObj) + ":</b> <a href=?tx="
				+  Base58.encode(record.getSignature()) + get_Lang(langObj) + ">" + vouchRecord.getVouchHeight() + "-"
				+ vouchRecord.getVouchSeqNo() + "</a><br>"; */
        //out += "<b>" + Lang.getInstance().translateFromLangObj("Description", langObj) + ":</b>";
        //  out += "<b>" + Lang.getInstance().translateFromLangObj("Vouch Record", langObj) + ":</b> ";
        out += "<b>" + Lang.getInstance().translateFromLangObj("Vouch Record", langObj) + ": </b> <a href='?tx=" + record.viewSignature() + get_Lang() + "'> ";
        out += record.getBlockHeight() + "-" + record.getSeqNo() + "</a> <br>";
        // LABEL DESCRIPTION

        return out;
    }

    private String issue_Union_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        IssueUnionRecord unionIssue = (IssueUnionRecord) transaction;
        UnionCls union = (UnionCls) unionIssue.getItem();
        out += "<BR>" + Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>" + itemNameHTML(union) + "</b>";

        out += Lang.getInstance().translateFromLangObj("Birthday", langObj) + ": <b>"
                + union.getBirthdayStr() + "</b><br>";
        out += Lang.getInstance().translateFromLangObj("Parent", langObj) + ": <b>"
                + String.valueOf(union.getParent()) + "</b><br>";

        return out;
    }

    private String issue_Status_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        IssueStatusRecord statusIssue = (IssueStatusRecord) transaction;
        StatusCls status = (StatusCls) statusIssue.getItem();
        out += "<BR>" + Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>" + itemNameHTML(status) + "</b>";

        return out;
    }

    private String issue_Template_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        IssueTemplateRecord templateIssue = (IssueTemplateRecord) transaction;
        TemplateCls template = (TemplateCls) templateIssue.getItem();
        out += "<BR>" + Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>" + itemNameHTML(template) + "</b>";

        return out;
    }

    private String issue_Imprint_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        IssueImprintRecord imprintIssue = (IssueImprintRecord) transaction;
        ImprintCls imprint = (ImprintCls) imprintIssue.getItem();
        out += "<BR>" + Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>" + itemNameHTML(imprint) + "</b>";

        return out;
    }

    private String issue_Person_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        IssuePersonRecord personIssue = (IssuePersonRecord) transaction;
        PersonCls person = (PersonCls) personIssue.getItem();

        String out = "";
        out += "<BR>" + Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>" + itemNameHTML(person) + "</b><br>";

        out += Lang.getInstance().translateFromLangObj("Birthday", langObj) + ": <b>"
                + person.getBirthdayStr() + "</b><br>";
        out += Lang.getInstance().translateFromLangObj("Gender", langObj) + ": <b>";
        if (person.getGender() == 0)
            out += Lang.getInstance().translateFromLangObj("Male", langObj);
        if (person.getGender() == 1)
            out += Lang.getInstance().translateFromLangObj("Female", langObj);
        out += "</b><br>";
        //out += "<b>" + Lang.getInstance().translateFromLangObj("Description", langObj) + ":</b> "
        //        + person.getDescription() + "<br>";
        if (person.getOwner().getPerson() != null) {
            // out += "<b>" + Lang.getInstance().translateFromLangObj("Owner",
            // langObj) + ":</b> <a href=?person="
            // +person.getOwner().getPerson().b.getKey()+ get_Lang(langObj) +
            // ">" + person.getOwner().viewPerson() +"</a><br>";
        } else {
            // out += "<b>" +Lang.getInstance().translateFromLangObj("Owner",
            // langObj) + ":</b> <a href=?address=" +
            // person.getOwner().getAddress() + get_Lang(langObj) + ">" +
            // person.getOwner().getAddress() +"</a><br>";
        }
        // out += "<b>" + Lang.getInstance().translateFromLangObj("Public
        // Key", langObj) + ":</b> " + person.getOwner().getBase58() +"<br>";
        return out;
    }

    private String issue_Asset_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        IssueAssetTransaction tr = (IssueAssetTransaction) transaction;
        String out = "";
        AssetCls asset = (AssetCls) tr.getItem();
        out += "<br>" + Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>" + itemNameHTML(asset) + "</b><br>";
        out += Lang.getInstance().translateFromLangObj("Quantity", langObj) + ": <b>"
                + asset.getQuantity() + "</b><br>";
        out += Lang.getInstance().translateFromLangObj("Scale", langObj) + ": <b>"
                + Lang.getInstance().translateFromLangObj(asset.getScale() + "", langObj)
                + "</b><br>";
        out += Lang.getInstance().translateFromLangObj("Asset Type", langObj) + ": <b>"
                + Lang.getInstance().translateFromLangObj(asset.viewAssetType() + "", langObj)
                + "</b><br>";

        return out;
    }

    private String issue_Poll_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        IssuePollRecord tr = (IssuePollRecord) transaction;
        String out = "";
        PollCls poll = (PollCls) tr.getItem();
        out += "<BR>" + Lang.getInstance().translateFromLangObj("Name", langObj) + ": <b>" + itemNameHTML(poll) + "</b>";

        return out;
    }

    private String r_Send_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        RSend rSend = (RSend) transaction;
        String out = "";

        out += Lang.getInstance().translateFromLangObj("Recipient", langObj) + ": <a href=?address="
                + rSend.getRecipient().getAddress() + get_Lang() + "><b>" + rSend.getRecipient().getPersonAsString()
                + "</b></a>";

        if (rSend.getAmount() != null) {
            out += "<br>" + Lang.getInstance().translateFromLangObj(rSend.viewActionTypeWas(), langObj)
                    + ": <b>" + rSend.getAmount().toPlainString() + " х "
                    + itemNameHTML(Controller.getInstance().getAsset(rSend.getAbsKey())) + "</b>";
        }

        if (!rSend.getHead().equals(""))
            out += "<BR>" + Lang.getInstance().translateFromLangObj("Title", langObj) + ": <b>" + rSend.getHead() + "</b>";

        return out;

    }

    private String r_Calculated_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        RCalculated tr = (RCalculated) transaction;
        String out = "";

        out += Lang.getInstance().translateFromLangObj("Recipient", langObj) + ": <a href=?address="
                + tr.getRecipient().getAddress() + get_Lang() + "><b>" + tr.getRecipient().getPersonAsString()
                + "</b></a><br>";

        if (!tr.getMessage().equals(""))
            out += "<h4>" + tr.getMessage() + "</h4>";

        if (tr.getAmount() != null) {
            out += "<br>" + Lang.getInstance().translateFromLangObj("Amount", langObj) + ": <b>"
                    + tr.getAmount().toPlainString()
                    + itemNameHTML(Controller.getInstance().getAsset(tr.getAbsKey())) + "</b></a>";
        }

        return out;

    }

    public String get_Vouches(Transaction transaction) {

        Fun.Tuple2<BigDecimal, List<Long>> vouchesItem = DCSet.getInstance().getVouchRecordMap().get(transaction.getDBRef());
        if (vouchesItem == null || vouchesItem.b.isEmpty())
            return "";

        TransactionFinalMapImpl map = DCSet.getInstance().getTransactionFinalMap();

        String out = "<b>" + Lang.getInstance().translateFromLangObj("Certified", langObj) + ":</b> ";

        out += "<table id=statuses BORDER=0 cellpadding=15 cellspacing=0 width='800'  class='table table-striped' style='border: 1px solid #ddd; word-wrap: break-word;'><tr><td>" + Lang.getInstance().translateFromLangObj("Transaction", langObj) + "<td>" + Lang.getInstance().translateFromLangObj("Date", langObj) + "<td>" + Lang.getInstance().translateFromLangObj("Creator", langObj) + "</tr>";
        for (Long txKey : vouchesItem.b) {

            transaction = map.get(txKey);

            out += "<tr>"
                    + "<td><a href=?tx=" + Base58.encode(transaction.getSignature()) + get_Lang() + ">" + transaction.getBlockHeight()
                    + "-" + transaction.getSeqNo() + "</a>"
                    + "<td>" + DateTimeFormat.timestamptoString(transaction.getTimestamp());
            out += "<td>";

            Fun.Tuple2<Integer, PersonCls> itemPerson = transaction.getCreator().getPerson();
            if (itemPerson != null) {
                out += "<a href=?person=" + itemPerson.b.getKey() + get_Lang() + "><b>"
                        + itemPerson.b.viewName() + "</b></a> ("
                        + Lang.getInstance().translateFromLangObj("Public Key", langObj) + ": "
                        + Base58.encode(transaction.getCreator().getPublicKey()) + ")<br>";
            } else {
                out += "<a href=?address=" + transaction.getCreator().getAddress() + get_Lang() + ">" + transaction.getCreator().getAddress()
                        + "</a> ("
                        + Lang.getInstance().translateFromLangObj("Public Key", langObj) + ": "
                        + Base58.encode(transaction.getCreator().getPublicKey()) + ")<br>";
            }

            out += Lang.getInstance().translateFromLangObj("Signature", langObj) + " : "
                    + "<a href=?tx=" + Base58.encode(transaction.getSignature()) + ">" + transaction.getSignature() + "</a><br>";

        }
        out += "</table>";

        return out;
    }

    public String htmlSignifier(long timestamp, Long personKey, String personName, PublicKeyAccount publicKey, byte[] signature) {

        String out = DateTimeFormat.timestamptoString(timestamp) + " ";
        if (personKey != null) {
            out += "<a href=?person=" + personKey + get_Lang() + "><b>"
                    + personName + "</b></a> ("
                    + Lang.getInstance().translateFromLangObj("Public key", langObj) + ": "
                    + Base58.encode(publicKey.getPublicKey()) + ")<br>";
        } else {
            out += "<a href=?address=" + publicKey.getAddress() + get_Lang() + ">" + publicKey.getAddress()
                    + "</a> ("
                    + Lang.getInstance().translateFromLangObj("Public key", langObj) + ": "
                    + Base58.encode(publicKey.getPublicKey()) + ")<br>";
        }

        out += Lang.getInstance().translateFromLangObj("Signature", langObj) + ": "
                + "<a href=?tx=" + Base58.encode(signature) + ">" + Base58.encode(signature) + "</a><br>";

        return out;
    }

    public String htmlSignifier(Transaction transaction) {

        Fun.Tuple2<Integer, PersonCls> itemPerson = transaction.getCreator().getPerson();
        if (itemPerson == null) {
            return htmlSignifier(0, null, null, transaction.getCreator(), transaction.getSignature());
        }

        return htmlSignifier(transaction.getTimestamp(), itemPerson.b.getKey(), itemPerson.b.viewName(),
                transaction.getCreator(), transaction.getSignature());
    }


    public String getVouchesNew(Fun.Tuple2<Integer, PersonCls> creatorPersonItem,
                                Transaction transaction, JSONObject langObj) {

        String personSign;
        if (creatorPersonItem != null) {
            personSign = htmlSignifier(transaction.getTimestamp(), creatorPersonItem.b.getKey(),
                    creatorPersonItem.b.viewName(), transaction.getCreator(), transaction.getSignature());
            ;
        } else {
            personSign = htmlSignifier(0, null, null, transaction.getCreator(), transaction.getSignature());
            ;
        }

        Fun.Tuple2<BigDecimal, List<Long>> vouchesItem = DCSet.getInstance().getVouchRecordMap().get(transaction.getDBRef());

        if (vouchesItem == null || vouchesItem.b.isEmpty()) {
            String out = "<b><center>" + Lang.getInstance().translateFromLangObj("Signifier", langObj) + "</center></b> ";
            out += personSign;
            return out;
        }


        TransactionFinalMapImpl map = DCSet.getInstance().getTransactionFinalMap();
        String out;

        if (vouchesItem.b.size() == 1) {
            out = "<b><center>" + Lang.getInstance().translateFromLangObj("Signatures of the parties", langObj) + "</center></b> "
                    + "<b>" + Lang.getInstance().translateFromLangObj("Side", langObj) + " 1:<br>" + personSign;

            Transaction signTransaction = map.get(vouchesItem.b.get(0));
            out += "<b>" + Lang.getInstance().translateFromLangObj("Side", langObj) + " 2:<br>"
                    + htmlSignifier(signTransaction);

        } else {
            out = "<b><center>" + Lang.getInstance().translateFromLangObj("Signatories", langObj) + "</center></b> "
                    + "<b>" + Lang.getInstance().translateFromLangObj("Side", langObj) + " 1:<br>" + personSign;

            int count = 1;
            for (Long txKey : vouchesItem.b) {

                Transaction signTransaction = map.get(txKey);
                out += "<b>" + Lang.getInstance().translateFromLangObj("Side", langObj) + " " + ++count
                        + ":<br>" + htmlSignifier(signTransaction);

            }
        }

        out += "</table>";

        return out;
    }
}
