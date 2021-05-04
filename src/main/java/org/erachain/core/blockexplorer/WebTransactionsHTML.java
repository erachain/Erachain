package org.erachain.core.blockexplorer;

import org.apache.commons.net.util.Base64;
import org.erachain.controller.Controller;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.exdata.exLink.ExLink;
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
import org.erachain.dbs.IteratorCloseable;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WebTransactionsHTML {

    JSONObject langObj;
    DCSet dcSet;
    public JSONObject outTX;

    WebTransactionsHTML() {
    }

    public WebTransactionsHTML(DCSet dcSet, JSONObject langObj) {
        this.dcSet = dcSet;
        this.langObj = langObj;
    }

    public JSONObject get_HTML_Body(Transaction transaction, String out) {

        //String out = "";

        ExLink exLink = transaction.getExLink();
        if (exLink != null) {
            out += "<h4>";
            Object parent = exLink.getParent(dcSet);
            String parentString;
            if (parent instanceof PersonCls) {
                parentString = ((ItemCls) parent).toString(dcSet);
                out += Lang.T("Author to", langObj)
                        + ": <a href=?person="
                        + exLink.getRef() + get_Lang() + ">" + parentString + "</a>";
            } else {
                parentString = ((Transaction) parent).toStringShort();
                out += Lang.T("Appendix to", langObj)
                        + ": <a href=?tx="
                        + Transaction.viewDBRef(exLink.getRef()) + get_Lang() + ">" + parentString + "</a>";
            }
            out += "</h4>";

        }

        JSONObject outTX = new JSONObject();

        outTX.put("head", out);
        outTX.put("timestampLabel", Lang.T("Date", langObj));
        outTX.put("timestamp", transaction.getTimestamp());

        if (transaction.isWiped())
            return outTX;

        int type = transaction.getType();
        switch (type) {
            case Transaction.CALCULATED_TRANSACTION:
                outTX.put("body", r_Calculated_HTML(transaction));
                break;
            case Transaction.SEND_ASSET_TRANSACTION:
                outTX.put("body", r_Send_HTML(transaction));
                outTX.put("message", ((RSend) transaction).viewData());
                break;
            case Transaction.ISSUE_ASSET_TRANSACTION:
                outTX.put("body", issue_Asset_HTML(transaction));
                outTX.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.ISSUE_PERSON_TRANSACTION:
                outTX.put("body", issue_Person_HTML(transaction));
                outTX.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.ISSUE_POLL_TRANSACTION:
                outTX.put("body", issue_Poll_HTML(transaction));
                outTX.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.ISSUE_IMPRINT_TRANSACTION:
                outTX.put("body", issue_Imprint_HTML(transaction));
                outTX.put("message", ((Itemable) transaction));
                break;
            case Transaction.ISSUE_TEMPLATE_TRANSACTION:
                outTX.put("body", issue_Template_HTML(transaction));
                outTX.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.ISSUE_STATUS_TRANSACTION:
                outTX.put("body", issue_Status_HTML(transaction));
                outTX.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.ISSUE_UNION_TRANSACTION:
                outTX.put("body", issue_Union_HTML(transaction));
                outTX.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.SIGN_TRANSACTION:
                outTX.put("body", sign_HTML(transaction));
                break;
            case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:
                outTX.put("body", certify_Pub_Key_HTML(transaction));
                break;
            case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:
                outTX.put("body", set_Status_HTML(transaction));
                break;
            case Transaction.HASHES_RECORD:
                outTX.put("body", hash_Record_HTML(transaction));
                break;
            case Transaction.CREATE_ORDER_TRANSACTION:
                outTX.put("body", create_Order_HTML(transaction));
                break;
            case Transaction.CANCEL_ORDER_TRANSACTION:
                outTX.put("body", cancel_Order_HTML(transaction));
                break;
            case Transaction.VOTE_ON_ITEM_POLL_TRANSACTION:
                outTX.put("body", vote_On_Item_Poll_HTML(transaction));
                break;
            case Transaction.GENESIS_CERTIFY_PERSON_TRANSACTION:
                outTX.put("body", genesis_Certify_Person_HTML(transaction));
                break;
            case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:
                outTX.put("body", genesis_Issue_Asset_HTML(transaction));
                outTX.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.GENESIS_ISSUE_TEMPLATE_TRANSACTION:
                outTX.put("body", genesis_Issue_Template_HTML(transaction));
                outTX.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:
                outTX.put("body", genesisIssue_Person_HTML(transaction));
                outTX.put("message", getItemDescription((Itemable) transaction));
                break;
            case Transaction.GENESIS_SEND_ASSET_TRANSACTION:
                outTX.put("body", genesis_Send_Asset_HTML(transaction));
                break;
            default:
                outTX.put("body", transaction.toJson());
        }

        getApps(outTX, transaction, langObj);

        return outTX;

    }

    public void get_HTML(BlockExplorer explorer, Transaction transaction) {
        // TODO: надо переделать тут так чтобы на строне клиента HTML собиралось с его локальным временм из timestamp

        if (explorer != null) {
            this.dcSet = explorer.dcSet;
            this.langObj = explorer.langObj;
        }

        transaction.setDC(dcSet, true);

        List<Transaction> tt = new ArrayList<Transaction>();
        tt.add(transaction);
        //explorer.transactionsJSON(null, tt, 0, BlockExplorer.pageSize, "tx");
        //JSONObject tras_json = (JSONObject) ((LinkedHashMap) ((LinkedHashMap) explorer.output.get("Transactions"))
        //        .get("transactions")).get(0);

        JSONObject tras_json = transaction.toJson();

        String out = "<font size='+1'> <b>" + Lang.T("Transaction", langObj) + ":</b>";
        out += " [" + tras_json.get("type") + "]" + tras_json.get("type_name");
        out += " (" + Lang.T("Block", langObj) + ": </b><a href=?block=" + tras_json.get("height") + get_Lang() + ">" + tras_json.get("height") + "</a>";
        out += ", " + Lang.T("seqNo", langObj) + ": </b><a href=?tx=" + tras_json.get("seqNo") + get_Lang() + ">" + tras_json.get("seqNo") + "</a> ) </font><br>";

        // она и так в заголовке будет
        //out += "<br><b>" + Lang.TFromLangObj("Type", langObj) + ": </b>" + tras_json.get("type_name");
        out += "<br><b>" + Lang.T("Confirmations", langObj) + ": </b>" + transaction.getConfirmations(dcSet);

        if (!(transaction instanceof RCalculated)) {
            out += "<br><b>" + Lang.T("Size", langObj) + ": </b>" + tras_json.get("size");
            out += "<br><b>" + Lang.T("Signature", langObj) + ": </b>" + tras_json.get("signature") + "<br>";
            if (transaction.getCreator() == null) {
                // GENESIS
                out += "<b>" + Lang.T("Creator", langObj) + ":</b> GENESIS";
            } else {
                out += "<b>" + Lang.T("Creator", langObj)
                        + ":</b> <a href=?address=" + transaction.getCreator().getAddress() + get_Lang() + ">"
                        + transaction.getCreator().getPersonAsString() + "</a>";
                out += "<br><b>" + Lang.T("Public Key", langObj) + ": </b><a href=?address="
                        + tras_json.get("publickey") + get_Lang() + ">" + tras_json.get("publickey") + "</a>";
                out += "<BR><b>" + Lang.T("Fee", langObj) + ": </b>" + tras_json.get("fee");
            }
            if (transaction.isWiped()) {
                out += "<BR><b>" + Lang.T("WIPED", langObj) + ": </b>" + "true";
            }
        }

        JSONObject outTX = get_HTML_Body(transaction, out);

        if (explorer == null) {
            this.outTX = outTX;
        } else {
            explorer.output.put("tx", outTX);
        }

    }

    private String get_Lang() {
        if (langObj == null)
            return "&lang=en";
        return "&lang=" + langObj.get("_lang_ISO_");

    }

    public String itemNameHTML(ItemCls item) {
        String out = "<a href=?" + item.getItemTypeName() + "=" + item.getKey(dcSet) + get_Lang() + ">";
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
            return Lang.T(item.viewDescription(), langObj);
        }

        return item.viewDescription();
    }

    private String genesis_Send_Asset_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisTransferAssetTransaction assetTransfer = (GenesisTransferAssetTransaction) transaction;

        if (assetTransfer.getSender() != null) {
            out += "<br>" + Lang.T("Sender", langObj) + ": <a href=?address="
                    + assetTransfer.getSender().getAddress() + get_Lang() + "><b>" + assetTransfer.getSender().getPersonAsString()
                    + "</b></a>";
        }

        out += "<br>" + Lang.T("Recipient", langObj) + ": <a href=?address="
                + assetTransfer.getRecipient().getAddress() + get_Lang() + "><b>" + assetTransfer.getRecipient().getPersonAsString()
                + "</b></a>";

        out += "<br>" + Lang.T(assetTransfer.viewActionType(), langObj)
                + ": <b>" + assetTransfer.getAmount().toPlainString() + " x "
                + itemNameHTML(Controller.getInstance().getAsset(assetTransfer.getAbsKey())) + "</b>";


        return out;
    }

    private String genesis_Issue_Template_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisIssueTemplateRecord templateIssue = (GenesisIssueTemplateRecord) transaction;
        TemplateCls template = (TemplateCls) templateIssue.getItem();
        out += "<br>" + Lang.T("Name", langObj) + ": <b>" + itemNameHTML(template) + "</b>";
        return out;
    }

    private String genesis_Issue_Asset_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisIssueAssetTransaction assetIssue = (GenesisIssueAssetTransaction) transaction;
        AssetCls asset = (AssetCls) assetIssue.getItem();
        out += "<BR>" + Lang.T("Name", langObj) + ": <b>" + itemNameHTML(asset) + "</b>";

        out += "<BR><b>" + Lang.T("Quantity", langObj) + ": </b>" + asset.getQuantity();
        out += "<BR><b>" + Lang.T("Scale", langObj) + ": </b>" + Lang.T(asset.getScale() + "", langObj);
        out += "<BR><b>" + Lang.T("Asset Type", langObj) + ": </b>" + Lang.T(asset.viewAssetType() + "", langObj);

        return out;
    }

    private String genesisIssue_Person_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        IssuePersonRecord personIssue = (IssuePersonRecord) transaction;
        PersonCls person = (PersonCls) personIssue.getItem();

        String out = "";
        out += "<BR>" + Lang.T("Name", langObj) + ": <b>" + itemNameHTML(person) + "</b><br>";

        out += "<b>" + Lang.T("Birthday", langObj) + ":</b> "
                + person.getBirthdayStr() + "<br>";
        out += "<b>" + Lang.T("Gender", langObj) + ":</b> ";
        if (person.getGender() == 0)
            out += Lang.T("Male", langObj);
        if (person.getGender() == 1)
            out += Lang.T("Female", langObj);
        out += "<br>";
        //out += "<b>" + Lang.TFromLangObj("Description", langObj) + ":</b> "
        //        + person.getDescription() + "<br>";
        if (person.getMaker().getPerson() != null) {
            // out += "<b>" + Lang.TFromLangObj("Maker",
            // langObj) + ":</b> <a href=?person="
            // +person.getMaker().getPerson().b.getKey()+ get_Lang(langObj) +
            // ">" + person.getMaker().viewPerson() +"</a><br>";
        } else {
            // out += "<b>" +Lang.TFromLangObj("Maker",
            // langObj) + ":</b> <a href=?address=" +
            // person.getMaker().getAddress() + get_Lang(langObj) + ">" +
            // person.getMaker().getAddress() +"</a><br>";
        }
        // out += "<b>" + Lang.TFromLangObj("Public
        // Key", langObj) + ":</b> " + person.getMaker().getBase58() +"<br>";
        return out;
    }

    private String genesis_Certify_Person_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        GenesisCertifyPersonRecord record = (GenesisCertifyPersonRecord) transaction;
        out += "<b>" + Lang.T("Recipient", langObj) + ":</b> <a href=?address="
                + record.getRecipient().getAddress() + get_Lang() + ">" + record.getRecipient().getPersonAsString()
                + "</a><br>";

        out += "<BR>" + Lang.T("Person", langObj) + ": <b>"
                + itemNameHTML(Controller.getInstance().getPerson(record.getKey())) + "</b>";
        return out;
    }

    private String vote_On_Item_Poll_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        VoteOnItemPollTransaction pollVote = (VoteOnItemPollTransaction) transaction;
        out += "<BR>" + Lang.T("Name", langObj) + ": <b>" + itemNameHTML(pollVote.getItem()) + "</b>";
        out += "<BR>" + Lang.T("Option", langObj) + ": <b>" + pollVote.viewOption() + "</b>";
        return out;
    }

    private String cancel_Order_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        CancelOrderTransaction cancelOrder = (CancelOrderTransaction) transaction;
        Long key = dcSet.getTransactionFinalMapSigns().get(cancelOrder.getorderSignature());
        if (key == null) {
            out += cancelOrder.toJson();
        }

        CreateOrderTransaction createOrder = (CreateOrderTransaction) dcSet.getTransactionFinalMap().get(key);

        out += "</br><h3>" + Lang.T("Order to Cancel", langObj) + "</h3>";
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
        if (dcSet.getOrderMap().contains(refDB)) {
            order = dcSet.getOrderMap().get(refDB);
            status = "Active";
        } else if (dcSet.getCompletedOrderMap().contains(refDB)) {
            order = dcSet.getCompletedOrderMap().get(refDB);
            if (order.isCompleted()) {
                status = "Completed";
            } else {
                status = "Canceled";
            }
        } else {
            status = "Unknown";
        }

        out += "<h4><a href='?order=" + Transaction.viewDBRef(refDB) + get_Lang() + "'>" + Lang.T(status, langObj) + "</a></h4>";

        out += Lang.T("Have", langObj) + ": <b>"
                + orderCreation.getAmountHave().toPlainString() + " x "
                + itemNameHTML(orderCreation.getHaveAsset()) + "</b>"
                + (order != null ? "<br>" + Lang.T("Fulfilled", langObj)
                + ": <b>" + order.getFulfilledHave().toPlainString() + "</b>" : "")
                + "<br>";
        out += Lang.T("Want", langObj) + ": <b>"
                + orderCreation.getAmountWant().toPlainString() + " x "
                + itemNameHTML(orderCreation.getWantAsset()) + "</b>"
                + (order != null ? "<br>" + Lang.T("Fulfilled", langObj)
                + ": <b>" + order.getFulfilledWant().toPlainString() + "</b>" : "")
                + "<br>";
        out += Lang.T("Price", langObj) + ": <b>"
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
                    out += "<b>" + Lang.T("URL", langObj) + ":</b> "
                            + "<a href='" + linkURL.toString() + "'>" + url + "</a><br>";
                }
            }
        } catch (Exception e) {
            linkURL = null;
            out += "<b>" + Lang.T("Title", langObj) + ":</b> "
                    + url + "<br>";
        }

        out += "<b>" + Lang.T("HASHES", langObj) + ":</b> ";
        int count = 0;

        for (byte[] hash : hashesTx.getHashes()) {
            String hash58 = Base58.encode(hash);
            out += "<br>" + ++count + " <a href=?q=" + hash58 + BlockExplorer.get_Lang(langObj) + "&search=transactions><b>" + hash58 + "</b></a>";
            if (urlForUse) {
                out += " - <a href='" + linkURL.toString() + hash58 + "' class='button ll-blue-bgc'>" + Lang.T("Open", langObj) + "</a>";
            }
        }

        out += "<br><b>" + Lang.T("Description", langObj) + ":</b><br>"
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
        out += "<br>" + Lang.T("Status Name", langObj) + ":<b> "
                + itemNameHTML(status) + "</b><br>";
        long beginDate = setStatusToItem.getBeginDate();
        long endDate = setStatusToItem.getEndDate();
        out += "<b>" + Lang.T("From - To", langObj) + ":</b> "
                + (beginDate == Long.MIN_VALUE ? "?" : DateTimeFormat.timestamptoString(beginDate))
                + " - " + (endDate == Long.MAX_VALUE ? "?" : DateTimeFormat.timestamptoString(endDate)) + "<br>";
        if (setStatusToItem.getValue1() != 0) {
            out += "<b>" + Lang.T("Value", langObj) + " 1:</b> "
                    + setStatusToItem.getValue1() + "<br>";
        }
        if (setStatusToItem.getValue2() != 0) {
            out += "<b>" + Lang.T("Value", langObj) + " 2:</b> "
                    + setStatusToItem.getValue2() + "<br>";
        }
        if (setStatusToItem.getData1() != null) {
            out += "<b>" + Lang.T("DATA", langObj) + " 1:</b> "
                    + new String(setStatusToItem.getData1(), StandardCharsets.UTF_8) + "<br>";
        }
        if (setStatusToItem.getData2() != null) {
            out += "<b>" + Lang.T("DATA", langObj) + " 2:</b> "
                    + new String(setStatusToItem.getData2(), StandardCharsets.UTF_8) + "<br>";
        }
        if (setStatusToItem.getRefParent() != 0l) {
            out += "<b>" + Lang.T("Parent", langObj) + ":</b> "
                    + setStatusToItem.viewRefParent() + "<br>";
        }
        out += Lang.T("Item Name", langObj) + ": <b>"
                + item.getItemTypeName() + " - " + item.getItemSubType()
                + ": " + itemNameHTML(item) + "</b><br>";

        out += Lang.T("Result", langObj) + ": <b>"
                + setStatusToItem.getResultText() + "</b><br>";


        return out;
    }

    private String certify_Pub_Key_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        RCertifyPubKeys record = (RCertifyPubKeys) transaction;
        PersonCls person;
        person = Controller.getInstance().getPerson(record.getKey());
        out += Lang.T("Name", langObj) + ": <b>"
                + itemNameHTML(person) + "</b><br>";
        out += "<b>" + Lang.T("End Days", langObj) + ":</b> "
                + record.getAddDay() + "<br>";
        int i = 0;
        for (String address : record.getCertifiedPublicKeysB58()) {
            out += "<b>   " + Lang.T("Key", langObj) + " " + ++i + ":</b> " + address + "<br>";
        }
        return out;
    }

    private String sign_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        RVouch vouchRecord = (RVouch) transaction;
        Transaction record = dcSet.getTransactionFinalMap().get(vouchRecord.getRefHeight(),
                vouchRecord.getRefSeqNo());
		/*out += "<b>" + Lang.TFromLangObj("height-seqNo", langObj) + ":</b> <a href=?tx="
				+  Base58.encode(record.getSignature()) + get_Lang(langObj) + ">" + vouchRecord.getVouchHeight() + "-"
				+ vouchRecord.getVouchSeqNo() + "</a><br>"; */
        //out += "<b>" + Lang.TFromLangObj("Description", langObj) + ":</b>";
        //  out += "<b>" + Lang.TFromLangObj("Vouch Record", langObj) + ":</b> ";
        out += "<b>" + Lang.T("Signed Transaction", langObj) + ": </b> <a href='?tx=" + record.viewSignature() + get_Lang() + "'> ";
        out += record.getBlockHeight() + "-" + record.getSeqNo() + "</a> <br>";
        // LABEL DESCRIPTION

        return out;
    }

    private String issue_Union_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        IssueUnionRecord unionIssue = (IssueUnionRecord) transaction;
        UnionCls union = (UnionCls) unionIssue.getItem();
        out += "<BR>" + Lang.T("Name", langObj) + ": <b>" + itemNameHTML(union) + "</b>";

        out += Lang.T("Birthday", langObj) + ": <b>"
                + union.getBirthdayStr() + "</b><br>";
        out += Lang.T("Parent", langObj) + ": <b>"
                + String.valueOf(union.getParent()) + "</b><br>";

        return out;
    }

    private String issue_Status_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        IssueStatusRecord statusIssue = (IssueStatusRecord) transaction;
        StatusCls status = (StatusCls) statusIssue.getItem();
        out += "<BR>" + Lang.T("Name", langObj) + ": <b>" + itemNameHTML(status) + "</b>";

        return out;
    }

    private String issue_Template_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        IssueTemplateRecord templateIssue = (IssueTemplateRecord) transaction;
        TemplateCls template = (TemplateCls) templateIssue.getItem();
        out += "<BR>" + Lang.T("Name", langObj) + ": <b>" + itemNameHTML(template) + "</b>";

        return out;
    }

    private String issue_Imprint_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        String out = "";
        IssueImprintRecord imprintIssue = (IssueImprintRecord) transaction;
        ImprintCls imprint = (ImprintCls) imprintIssue.getItem();
        out += "<BR>" + Lang.T("Name", langObj) + ": <b>" + itemNameHTML(imprint) + "</b>";

        return out;
    }

    private String issue_Person_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        IssuePersonRecord personIssue = (IssuePersonRecord) transaction;
        PersonCls person = (PersonCls) personIssue.getItem();

        String out = "";
        out += "<BR>" + Lang.T("Name", langObj) + ": <b>" + itemNameHTML(person) + "</b><br>";

        out += Lang.T("Birthday", langObj) + ": <b>"
                + person.getBirthdayStr() + "</b><br>";
        out += Lang.T("Gender", langObj) + ": <b>";
        if (person.getGender() == 0)
            out += Lang.T("Male", langObj);
        if (person.getGender() == 1)
            out += Lang.T("Female", langObj);
        out += "</b><br>";
        //out += "<b>" + Lang.TFromLangObj("Description", langObj) + ":</b> "
        //        + person.getDescription() + "<br>";
        if (person.getMaker().getPerson() != null) {
            // out += "<b>" + Lang.TFromLangObj("Maker",
            // langObj) + ":</b> <a href=?person="
            // +person.getMaker().getPerson().b.getKey()+ get_Lang(langObj) +
            // ">" + person.getMaker().viewPerson() +"</a><br>";
        } else {
            // out += "<b>" +Lang.TFromLangObj("Maker",
            // langObj) + ":</b> <a href=?address=" +
            // person.getMaker().getAddress() + get_Lang(langObj) + ">" +
            // person.getMaker().getAddress() +"</a><br>";
        }
        // out += "<b>" + Lang.TFromLangObj("Public
        // Key", langObj) + ":</b> " + person.getMaker().getBase58() +"<br>";
        return out;
    }

    private String issue_Asset_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        IssueAssetTransaction tr = (IssueAssetTransaction) transaction;
        String out = "";
        AssetCls asset = (AssetCls) tr.getItem();
        out += "<br>" + Lang.T("Name", langObj) + ": <b>" + itemNameHTML(asset) + "</b><br>";
        out += Lang.T("Quantity", langObj) + ": <b>"
                + asset.getQuantity() + "</b><br>";
        out += Lang.T("Scale", langObj) + ": <b>"
                + Lang.T(asset.getScale() + "", langObj)
                + "</b><br>";
        out += Lang.T("Asset Type", langObj) + ": <b>"
                + Lang.T(asset.viewAssetType() + "", langObj)
                + "</b><br>";

        return out;
    }

    private String issue_Poll_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        IssuePollRecord tr = (IssuePollRecord) transaction;
        String out = "";
        PollCls poll = (PollCls) tr.getItem();
        out += "<BR>" + Lang.T("Name", langObj) + ": <b>" + itemNameHTML(poll) + "</b>";

        return out;
    }

    private String r_Send_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        RSend rSend = (RSend) transaction;
        String out = "";

        out += Lang.T("Recipient", langObj) + ": <a href=?address="
                + rSend.getRecipient().getAddress() + get_Lang() + "><b>" + rSend.getRecipient().getPersonAsString()
                + "</b></a>";

        if (rSend.getAmount() != null) {
            out += "<br>" + Lang.T(rSend.viewActionType(), langObj)
                    + ": <b>" + rSend.getAmount().toPlainString() + " х "
                    + itemNameHTML(Controller.getInstance().getAsset(rSend.getAbsKey())) + "</b>";
        }

        if (!rSend.getTitle().equals(""))
            out += "<BR>" + Lang.T("Title", langObj) + ": <b>" + rSend.getTitle() + "</b>";

        return out;

    }

    private String r_Calculated_HTML(Transaction transaction) {
        // TODO Auto-generated method stub
        RCalculated tr = (RCalculated) transaction;
        String out = "";

        out += Lang.T("Recipient", langObj) + ": <a href=?address="
                + tr.getRecipient().getAddress() + get_Lang() + "><b>" + tr.getRecipient().getPersonAsString()
                + "</b></a><br>";

        out += "<h4>" + tr.getTitle(langObj) + "</h4>";

        if (tr.getAmount() != null) {
            out += "<br>" + Lang.T("Amount", langObj) + ": <b>"
                    + tr.getAmount().toPlainString()
                    + itemNameHTML(Controller.getInstance().getAsset(tr.getAbsKey())) + "</b></a>";
        }

        return out;

    }

    public String getVouches_old(Transaction transaction) {

        Fun.Tuple2<BigDecimal, List<Long>> vouchesItem = dcSet.getVouchRecordMap().get(transaction.getDBRef());
        if (vouchesItem == null || vouchesItem.b.isEmpty())
            return "";

        TransactionFinalMapImpl map = dcSet.getTransactionFinalMap();

        String out = "<b>" + Lang.T("Certified", this.langObj) + ":</b> ";

        out += "<table id=statuses BORDER=0 cellpadding=15 cellspacing=0 width='800'  class='table table-striped' style='border: 1px solid #ddd; word-wrap: break-word;'><tr><td>" + Lang.T("Transaction", this.langObj) + "<td>" + Lang.T("Date", this.langObj) + "<td>" + Lang.T("Creator", this.langObj) + "</tr>";
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
                        + Lang.T("Public Key", this.langObj) + ": "
                        + Base58.encode(transaction.getCreator().getPublicKey()) + ")<br>";
            } else {
                out += "<a href=?address=" + transaction.getCreator().getAddress() + get_Lang() + ">" + transaction.getCreator().getAddress()
                        + "</a> ("
                        + Lang.T("Public Key", this.langObj) + ": "
                        + Base58.encode(transaction.getCreator().getPublicKey()) + ")<br>";
            }

            out += Lang.T("Signature", this.langObj) + " : "
                    + "<a href=?tx=" + Base58.encode(transaction.getSignature()) + ">" + transaction.getSignature() + "</a><br>";

        }
        out += "</table>";

        return out;
    }

    public static String htmlSignifier(long timestamp, Long personKey, String personName, PublicKeyAccount publicKey, byte[] signature, JSONObject langObj) {

        String out = DateTimeFormat.timestamptoString(timestamp) + " ";
        if (personKey != null) {
            out += "<a href=?person=" + personKey + BlockExplorer.get_Lang(langObj) + "><b>"
                    + personName + "</b></a> ("
                    + Lang.T("Public key", langObj) + ": "
                    + Base58.encode(publicKey.getPublicKey()) + ")<br>";
        } else {
            out += "<a href=?address=" + publicKey.getAddress() + BlockExplorer.get_Lang(langObj) + ">" + publicKey.getAddress()
                    + "</a> ("
                    + Lang.T("Public key", langObj) + ": "
                    + Base58.encode(publicKey.getPublicKey()) + ")<br>";
        }

        if (signature != null) {
            out += Lang.T("Signature", langObj) + ": "
                    + "<a href=?tx=" + Base58.encode(signature) + ">" + Base58.encode(signature) + BlockExplorer.get_Lang(langObj) + "</a><br>";
        }

        return out;
    }

    public static String htmlSignifier(Transaction transaction, JSONObject langObj) {

        Fun.Tuple2<Integer, PersonCls> itemPerson = transaction.getCreator().getPerson();
        if (itemPerson == null) {
            return htmlSignifier(0, null, null, transaction.getCreator(), transaction.getSignature(), langObj);
        }

        return htmlSignifier(transaction.getTimestamp(), itemPerson.b.getKey(), itemPerson.b.viewName(),
                transaction.getCreator(), transaction.getSignature(), langObj);
    }


    public static void getVouches(HashMap output, Transaction transaction, JSONObject langObj) {

        DCSet dcSet = DCSet.getInstance();

        Fun.Tuple2<BigDecimal, List<Long>> vouchesItem = dcSet.getVouchRecordMap().get(transaction.getDBRef());

        if (vouchesItem != null && !vouchesItem.b.isEmpty()) {

            TransactionFinalMapImpl map = dcSet.getTransactionFinalMap();

            String out = "<b><center>" + Lang.T("Vouchers", langObj) + "</center></b><br>";

            int count = 0;
            for (Long txKey : vouchesItem.b) {

                Transaction signTransaction = map.get(txKey);
                out += "<b>" + Lang.T("Voucher", langObj) + " " + ++count
                        + ":<br>" + htmlSignifier(signTransaction, langObj);

            }
            output.put("vouches", out);
        }

    }

    public static void getSigns(HashMap output, Transaction transaction, JSONObject langObj) {

        DCSet dcSet = DCSet.getInstance();

        PublicKeyAccount creator = transaction.getCreator();
        if (creator == null) {
            return;
        }

        Fun.Tuple2<Integer, PersonCls> creatorPersonItem = creator.getPerson();
        String out;

        String personSign;
        if (creatorPersonItem != null) {
            personSign = htmlSignifier(transaction.getTimestamp(), creatorPersonItem.b.getKey(),
                    creatorPersonItem.b.viewName(), transaction.getCreator(), transaction.getSignature(), langObj);
        } else {
            personSign = htmlSignifier(0, null, null, transaction.getCreator(), transaction.getSignature(), langObj);
        }

        Fun.Tuple2<BigDecimal, List<Long>> signsItem = dcSet.getVouchRecordMap().get(transaction.getDBRef());

        if (signsItem == null || signsItem.b.isEmpty()) {
            out = "<b><center>" + Lang.T("Signifier", langObj) + "</center></b> ";
            out += personSign;
        } else {

            TransactionFinalMapImpl map = dcSet.getTransactionFinalMap();

            if (signsItem.b.size() == 1) {
                out = "<b><center>" + Lang.T("Signatures of the parties", langObj) + "</center></b> "
                        + "<b>" + Lang.T("Side", langObj) + " 1:<br>" + personSign;

                Transaction signTransaction = map.get(signsItem.b.get(0));
                out += "<b>" + Lang.T("Side", langObj) + " 2:<br>"
                        + htmlSignifier(signTransaction, langObj);

            } else {
                out = "<b><center>" + Lang.T("Signatories", langObj) + "</center></b> "
                        + "<b>" + Lang.T("Side", langObj) + " 1:<br>" + personSign;

                int count = 1;
                for (Long txKey : signsItem.b) {

                    Transaction signTransaction = map.get(txKey);
                    out += "<b>" + Lang.T("Side", langObj) + " " + ++count
                            + ":<br>" + htmlSignifier(signTransaction, langObj);

                }
            }
        }

        ///out += "</table>";
        output.put("signs", out);

    }

    public static void getLinks(HashMap output, Transaction parentTx, JSONObject langObj) {

        DCSet dcSet = DCSet.getInstance();

        String out = "<hr>";

        try (IteratorCloseable<Fun.Tuple3<Long, Byte, Long>> appendixListIterator = dcSet.getExLinksMap()
                .getTXLinksIterator(parentTx.getDBRef(), ExData.LINK_APPENDIX_TYPE, false)) {
            List<Fun.Tuple3<Long, Byte, Long>> appendixes = new ArrayList<>();
            while (appendixListIterator.hasNext()) {
                appendixes.add(appendixListIterator.next());
            }
            if (!appendixes.isEmpty()) {
                TransactionFinalMapImpl map = dcSet.getTransactionFinalMap();

                if (appendixes.size() == 1) {
                    Transaction childTx = map.get(appendixes.get(0).c);

                    out += "<h2>" + Lang.T("Appendix", langObj)
                            + "</h2><h3>" + childTx.getTitle() + "</h3>";
                    out += "<a href=?tx=" + childTx.viewHeightSeq() + BlockExplorer.get_Lang(langObj) + ">"
                            + Lang.T(childTx.viewFullTypeName(), langObj) + " " + childTx.viewHeightSeq() + "</a> "
                            + " " + DateTimeFormat.timestamptoString(childTx.getTimestamp()) + " ";
                    out += "<a href=?address="
                            + childTx.getCreator().getAddress() + BlockExplorer.get_Lang(langObj) + "><b>" + childTx.getCreator().getPersonAsString()
                            + "</b></a><br>";

                } else {

                    int count = 0;
                    for (Fun.Tuple3<Long, Byte, Long> txKey : appendixes) {

                        Transaction childTx = map.get(txKey.c);
                        out += "<h2>" + Lang.T("Appendix", langObj) + " " + ++count
                                + "</h2><h3>" + childTx.getTitle() + "</h3>";
                        out += "<a href=?tx=" + childTx.viewHeightSeq() + BlockExplorer.get_Lang(langObj) + ">"
                                + Lang.T(childTx.viewFullTypeName(), langObj) + " " + childTx.viewHeightSeq() + "</a> "
                                + " " + DateTimeFormat.timestamptoString(childTx.getTimestamp()) + " ";
                        out += "<a href=?address="
                                + childTx.getCreator().getAddress() + BlockExplorer.get_Lang(langObj) + "><b>" + childTx.getCreator().getPersonAsString()
                                + "</b></a><br>";

                    }
                }

                ///out += "</table>";

            }
        } catch (IOException e) {
            output.put("error", e.getMessage());
        }

        try (IteratorCloseable<Fun.Tuple3<Long, Byte, Long>> appendixListIterator = dcSet.getExLinksMap()
                .getTXLinksIterator(parentTx.getDBRef(), ExData.LINK_AUTHOR_TYPE, false)) {
            List<Fun.Tuple3<Long, Byte, Long>> appendixes = new ArrayList<>();
            while (appendixListIterator.hasNext()) {
                appendixes.add(appendixListIterator.next());
            }
            if (!appendixes.isEmpty()) {
                TransactionFinalMapImpl map = dcSet.getTransactionFinalMap();

                out += "<h2>" + Lang.T("Issues", langObj)
                        + "</h2>";

                int count = 0;
                for (Fun.Tuple3<Long, Byte, Long> txKey : appendixes) {

                    Transaction childTx = map.get(txKey.c);
                    if (childTx == null)
                        continue;

                    out += "<h3>" + childTx.getTitle() + "</h3>";
                    out += "<a href=?tx=" + childTx.viewHeightSeq() + BlockExplorer.get_Lang(langObj) + ">"
                            + childTx.viewHeightSeq() + "</a> "
                            + " " + DateTimeFormat.timestamptoString(childTx.getTimestamp()) + " ";
                    out += "<a href=?address="
                            + childTx.getCreator().getAddress() + BlockExplorer.get_Lang(langObj) + "><b>" + childTx.getCreator().getPersonAsString()
                            + "</b></a><br>";

                }

                ///out += "</table>";

            }
        } catch (IOException e) {
            output.put("error", e.getMessage());
        }

        try (IteratorCloseable<Fun.Tuple3<Long, Byte, Long>> appendixListIterator = dcSet.getExLinksMap()
                .getTXLinksIterator(parentTx.getDBRef(), ExData.LINK_SOURCE_TYPE, false)) {
            List<Fun.Tuple3<Long, Byte, Long>> appendixes = new ArrayList<>();
            while (appendixListIterator.hasNext()) {
                appendixes.add(appendixListIterator.next());
            }
            if (!appendixes.isEmpty()) {
                TransactionFinalMapImpl map = dcSet.getTransactionFinalMap();

                out += "<h2>" + Lang.T("Usage", langObj)
                        + "</h2>";

                int count = 0;
                for (Fun.Tuple3<Long, Byte, Long> txKey : appendixes) {

                    Transaction childTx = map.get(txKey.c);
                    out += "<h3>" + childTx.getTitle() + "</h3>";
                    out += "<a href=?tx=" + childTx.viewHeightSeq() + BlockExplorer.get_Lang(langObj) + ">"
                            + childTx.viewHeightSeq() + "</a> "
                            + " " + DateTimeFormat.timestamptoString(childTx.getTimestamp()) + " ";
                    out += "<a href=?address="
                            + childTx.getCreator().getAddress() + BlockExplorer.get_Lang(langObj) + "><b>" + childTx.getCreator().getPersonAsString()
                            + "</b></a><br>";

                }

                ///out += "</table>";

            }
        } catch (IOException e) {
            output.put("error", e.getMessage());
        }

        try (IteratorCloseable<Fun.Tuple3<Long, Byte, Long>> appendixListIterator = dcSet.getExLinksMap()
                .getTXLinksIterator(parentTx.getDBRef(), ExData.LINK_REPLY_COMMENT_TYPE, false)) {
            List<Fun.Tuple3<Long, Byte, Long>> appendixes = new ArrayList<>();
            while (appendixListIterator.hasNext()) {
                appendixes.add(appendixListIterator.next());
            }
            if (!appendixes.isEmpty()) {
                TransactionFinalMapImpl map = dcSet.getTransactionFinalMap();

                out += "<h2>" + Lang.T("Replays and Comments", langObj)
                        + "</h2>";

                int count = 0;
                for (Fun.Tuple3<Long, Byte, Long> txKey : appendixes) {

                    Transaction childTx = map.get(txKey.c);
                    out += "<h3>" + childTx.getTitle() + "</h3>";
                    out += "<a href=?tx=" + childTx.viewHeightSeq() + BlockExplorer.get_Lang(langObj) + ">"
                            + childTx.viewHeightSeq() + "</a> "
                            + " " + DateTimeFormat.timestamptoString(childTx.getTimestamp()) + " ";
                    out += "<a href=?address="
                            + childTx.getCreator().getAddress() + BlockExplorer.get_Lang(langObj) + "><b>" + childTx.getCreator().getPersonAsString()
                            + "</b></a><br>";

                }

                ///out += "</table>";

            }
        } catch (IOException e) {
            output.put("error", e.getMessage());
        }

        output.put("links", out);

    }

    public static void getAppLink(HashMap output, Transaction transaction, JSONObject langObj) {

        ExLink exLink = transaction.getExLink();
        if (exLink != null) {
            output.put("Label_LinkType", Lang.T("Link Type", langObj));
            output.put("exLink_Name", Lang.T(exLink.viewTypeName(false), langObj));
            output.put("exLink", exLink.makeJSONforHTML(false, langObj));
            output.put("Label_Parent", Lang.T("for # для", langObj));
        }

    }

    public static void getApps(HashMap output, Transaction transaction, JSONObject langObj) {
        getSigns(output, transaction, langObj);
        getLinks(output, transaction, langObj);
    }

}
