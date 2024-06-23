package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.exdata.ExData;
import org.erachain.core.exdata.exActions.ExAction;
import org.erachain.core.exdata.exLink.*;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.DCSet;
import org.erachain.utils.APIUtils;
import org.erachain.utils.FileUtils;
import org.erachain.utils.StrJSonFine;
import org.erachain.utils.ZipBytes;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Path("r_note")
@Produces(MediaType.APPLICATION_JSON)
public class RSignNoteResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSignNoteResource.class);
    @Context
    HttpServletRequest request;

    @GET
    public String help() {
        Map<String, String> help = new LinkedHashMap<String, String>();
        help.put("POST r_note/make {" +
                        "creator - maker account, " +
                        "title, " +
                        "feePow:0..6, " +
                        "tryFree, " +
                        "tags:WORDS, " +
                        "linkType:0..4, " +
                        "linkTo:SeqNo, " +
                        "test:true, " +
                        "recipients: { onlyRecipients:false, list:[Addresses] }, " +

                        "accruals: { "
                        + "assetKey:long - asset key for Action, "
                        + "position=1 - balance position (1 - OWN, 2 - DEBT, 3 - HOLD, 4 - SPEND, 5 - PLEDGE), "
                        + "backward:false, "
                        + "method:0..2 - by TOTAL (0), by coefficient (1), by same Value (2), "
                        + "methodValue:BigDecimal, "
                        + "amountMin:BigDecimal or Null - minimum to accrual, "
                        + "amountMax:BigDecimal or Null - maximum to accrual, "
                        + "filterAssetKey:long - file by this asset balances, "
                        + "filterBalPos:1 - check balance position (1 - OWN, 2 - DEBT, 3 - HOLD, 4 - SPEND, 5 - PLEDGE), "
                        + "filterBalSide:1 - check balance side (0 - Debit, 1 - Left, 2 - Credit), "
                        + "filterTXType:0, - If 0 - for all transactions, "
                        + "filterGreatEqual:BigDecimal or Null, "
                        + "filterLessEqual:BigDecimal or Null, "
                        + "activeAfter=date - yyyy-MM-dd hh:mm:00 or Seq-No (3214-2) or timestamp[sec], "
                        + "activeBefore=date - yyyy-MM-dd hh:mm:00 or Seq-No (3214-2) or timestamp[sec], "
                        + "filterPerson:0..3 - 0 all, 1 - only for persons, 2 - only for man, 3 - only for woman, "
                        + "selfUse:false - Use creator address too. Default = false, "
                        + "}, " +
                        "authors: { list: { ref:No, name:Name, share:Share }, " +
                        "sources: { list: { ref:SeqNo, name:Name, share:Share }, " +
                        "encrypt:false, " +
                        "message:MESSAGE, " +
                        "messageUnique:false, " +
                        "templateKey:long, " +
                        "templateParams: { param:Value, ..}, " +
                        "templateUnique:false," +
                        "hashes: { path:HASH, ..}, " +
                        "hashesUnique:false," +
                        "files: [ { name:Path, zip:false, file:FILE or data:bytes.UTF-8 }, ..]" +
                        "filesUnique:false," +

                        "ai:, ",
                "accruals - make 'muli-send' action from creator Address the asset [assetKey] by filter, If 'test' = false it will be make real sends. In 'files' - if set 'file' then 'data' ignored (for example: 'file`:'resources/r_note_test.json'"
        );

        //

        return StrJSonFine.convert(help);
    }


    /**
     * Multi send scrip for send asset for many addresses or persons filtered by some parameters.
     * This command will run as test for calculate FEE and total AMOUNT by default. For run real send set parameter `test=false`.
     * Unlock wallet.
     * <br>
     * fromAddress     my address in Wallet
     * assetKey        asset Key that send
     * * @param forAssetKey     asset key of holders test
     * * @param amount          absolute amount to send
     * * @param onlyPerson      Default: false. Use only person accounts
     * * @param gender          Filter by gender. -1 = all, 0 - man, 1 - woman. Default: -1.
     * * @param position        test balance position. 1 - Own, 2 - Credit, 3 - Hold, 4 - Spend, 5 - Other
     * * @param greatEqual      test balance is great or equal
     * * @param selfPay         if set - pay to self address too. Default = true
     * * @param test            default - true. test=false - real send
     * * @param feePow
     * * @param activeAfterStr  timestamp after that is filter - yyyy-MM-dd hh:mm or timestamp(sec)
     * * @param activeBeforeStr timestamp before that is filter - yyyy-MM-dd hh:mm or timestamp(sec) activetypetx
     * * @param activeTypeTX    if set - test only that type transactions
     * * @param coeff          coefficient for amount in balance position of forAssetKey
     * * @param title
     * * @param password
     *
     * @return
     */
    @POST
    @Path("make")
    public String makeNote(String x) {

        JSONObject jsonObject;
        try {
            //READ JSON
            jsonObject = (JSONObject) JSONValue.parse(x);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            ///logger.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

        if (jsonObject == null)
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);

        String password = (String) jsonObject.getOrDefault("password", null);

        int step = 0;

        /////// COMMON
        String creatorStr = (String) jsonObject.getOrDefault("creator", null);
        Fun.Tuple2<Account, String> resultCreator = Account.tryMakeAccount(creatorStr);
        if (resultCreator.b != null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_MAKER_ADDRESS);
        }

        String title = (String) jsonObject.get("title");

        int feePow = Integer.valueOf(jsonObject.getOrDefault("feePow", 0).toString());
        boolean tryFree = Boolean.valueOf((boolean) jsonObject.getOrDefault("tryFree", false));

        String tags = (String) jsonObject.get("tags");

        boolean test = Boolean.valueOf((boolean) jsonObject.getOrDefault("test", true));

        //////////// LINK TO
        step++;
        Long exLinkType = (Long) jsonObject.get("linkType");
        ExLink exLink = null;
        if (exLinkType != null && exLinkType > ExData.LINK_SIMPLE_TYPE) {
            String linkToRefStr = jsonObject.get("linkTo").toString();
            if (linkToRefStr == null) {
                JSONObject out = new JSONObject();
                Transaction.updateMapByErrorSimple(Transaction.INVALID_EX_LINK_REF, out);
                return out.toJSONString();
            } else {
                Transaction parent = DCSet.getInstance().getTransactionFinalMap().getRecord(linkToRefStr);
                if (parent == null) {
                    JSONObject out = new JSONObject();
                    Transaction.updateMapByErrorSimple(Transaction.INVALID_EX_LINK_REF, out);
                    return out.toJSONString();
                }
                int linkType = (int) (long) exLinkType;
                if (parent == null || linkType == ExData.LINK_SIMPLE_TYPE) {
                    exLink = null;
                } else {
                    switch (linkType) {
                        case ExData.LINK_APPENDIX_TYPE:
                            exLink = new ExLinkAppendix(parent.getDBRef());
                            break;
                        case ExData.LINK_REPLY_COMMENT_TYPE:
                            exLink = new ExLinkReply(parent.getDBRef());
                            break;
                        case ExData.LINK_COMMENT_TYPE_FOR_VIEW:
                            APPENDIX_TYPE:
                            exLink = new ExLinkReply(parent.getDBRef());
                            break;
                        default:
                            exLink = null;
                    }
                }

                if (exLink == null) {
                    throw ApiErrorFactory.getInstance().createError(
                            Transaction.INVALID_EX_LINK_REF);
                }
            }
        }

        /////////// RECIPIENTS
        step++;
        JSONObject recipientsJson = (JSONObject) jsonObject.get("recipients");
        boolean onlyRecipients = false;
        Account[] recipients;
        if (recipientsJson == null) {
            recipients = null;
        } else {
            onlyRecipients = Boolean.valueOf((boolean) recipientsJson.getOrDefault("onlyRecipients", false));
            JSONArray recipientsArray = (JSONArray) recipientsJson.get("list");
            if (recipientsArray == null) {
                JSONObject out = new JSONObject();
                Transaction.updateMapByErrorSimple(Transaction.INVALID_RECEIVERS_LIST, out);
                return out.toJSONString();
            }

            recipients = new Account[recipientsArray.size()];
            for (int index = 0; index < recipientsArray.size(); index++) {
                String recipientAddress = (String) recipientsArray.get(index);
                //ORDINARY RECIPIENT
                Fun.Tuple2<Account, String> result = Account.tryMakeAccount(recipientAddress);
                if (result.a == null) {
                    JSONObject out = new JSONObject();
                    Transaction.updateMapByErrorSimple(Transaction.INVALID_RECEIVERS_LIST, recipientAddress, out);
                    return out.toJSONString();
                }
                recipients[index] = result.a;
            }
        }

        /////////// Accruals
        JSONObject actionJson = (JSONObject) jsonObject.get("action");
        ExAction action;
        if (actionJson == null) {
            action = null;
        } else {

            Fun.Tuple2<ExAction, String> accrualsResult = ExAction.parseJSON(jsonObject);

            if (accrualsResult.a == null) {
                action = null;
            } else {
                action = accrualsResult.a;
            }

        }

        if (!test && !BlockChain.TEST_MODE
                && ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            return "not LOCAL && not testnet";
        }

        ////////// AUTHORS
        ExLinkAuthor[] authors;
        JSONObject authorsJson = (JSONObject) jsonObject.get("authors");
        if (authorsJson == null) {
            authors = null;
        } else {
            JSONArray authorsArray = (JSONArray) authorsJson.get("list");
            authors = new ExLinkAuthor[authorsArray.size()];
            for (int index = 0; index < authorsArray.size(); index++) {
                JSONObject author = (JSONObject) authorsArray.get(index);
                authors[index] = new ExLinkAuthor((Long) author.get("ref"),
                        (int) (long) (Long) author.get("share"),
                        (String) author.get("memo"));
            }
        }

        ///// SOURCES
        ExLinkSource[] sources = null;
        JSONObject sourcesJson = (JSONObject) jsonObject.get("sources");
        if (sourcesJson == null) {
            sources = null;
        } else {
            JSONArray sourcesArray = (JSONArray) sourcesJson.get("list");
            sources = new ExLinkSource[sourcesArray.size()];
            for (int index = 0; index < sourcesArray.size(); index++) {
                JSONObject source = (JSONObject) sourcesArray.get(index);
                sources[index] = new ExLinkSource((Long) source.get("ref"),
                        (int) (long) (Long) source.get("share"),
                        (String) source.get("memo"));
            }
        }

        ////////// BODY THAT MAY BE ENCRYPTED

        boolean isEncrypted = Boolean.valueOf((boolean) jsonObject.getOrDefault("encrypt", false));

        //////// MESSAGE
        String message = (String) jsonObject.get("message");
        boolean messageUnique = Boolean.valueOf((boolean) jsonObject.getOrDefault("messageUnique", false));

        //////// TEMPLATE
        Long templateKey = (Long) jsonObject.get("templateKey");
        boolean templateUnique = Boolean.valueOf((boolean) jsonObject.getOrDefault("templateUnique", false));
        HashMap<String, String> templateParams;
        if (templateKey == null) {
            templateParams = null;
        } else {
            templateParams = (HashMap) jsonObject.get("templateParams");
        }

        /// HASHES
        HashMap<String, String> hashes = (HashMap) jsonObject.get("hashes");
        boolean hashesUnique = Boolean.valueOf((boolean) jsonObject.getOrDefault("hashesUnique", false));

        //// FILES
        Set<Fun.Tuple3<String, Boolean, byte[]>> files = new HashSet<Fun.Tuple3<String, Boolean, byte[]>>();
        JSONArray filesArray = (JSONArray) jsonObject.get("files");
        if (filesArray != null) {
            for (Object fileObj : filesArray) {
                JSONObject file = (JSONObject) fileObj;
                String filePath = (String) file.remove("file");
                byte[] dataBytes;
                if (filePath != null) {
                    try {
                        dataBytes = FileUtils.getBytesFromFile(new File(filePath));
                    } catch (FileNotFoundException e) {
                        JSONObject out = new JSONObject();
                        Transaction.updateMapByErrorSimple(Transaction.INVALID_DATA, "File not found: [" + filePath + "] - " + e.getMessage(), out);
                        return out.toJSONString();
                    } catch (Exception e) {
                        JSONObject out = new JSONObject();
                        Transaction.updateMapByErrorSimple(Transaction.INVALID_DATA, "File error: [" + filePath + "] - " + e.getMessage(), out);
                        return out.toJSONString();
                    }
                } else {
                    dataBytes = file.get("data").toString().getBytes(StandardCharsets.UTF_8);
                }
                Boolean zip = (Boolean) file.get("zip");
                if (zip) {
                    try {
                        dataBytes = ZipBytes.compress(dataBytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                files.add(new Fun.Tuple3<>((String) file.get("name"), zip, dataBytes));
            }
        }
        boolean filesUnique = Boolean.valueOf((boolean) jsonObject.getOrDefault("filesUnique", false));

        PrivateKeyAccount privateKeyAccount;
        if (!test) {
            // так как тут может очень долго работать то откроем на долго
            APIUtils.askAPICallAllowed(password, "GET RSignNote\n ", request, false);

        }

        Controller cntr = Controller.getInstance();
        BlockChain chain = cntr.getBlockChain();

        privateKeyAccount = cntr.getWalletPrivateKeyAccountByAddress(resultCreator.a.getAddress());
        if (privateKeyAccount == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_WALLET_ADDRESS, resultCreator.a.getAddress());
        }

        try {

            JSONObject out = new JSONObject();

            byte[] exDataBytes;
            try {
                exDataBytes = ExData.make(exLink, action, privateKeyAccount, title,
                        onlyRecipients, recipients, authors, sources, tags, isEncrypted,
                        templateKey, templateParams, templateUnique,
                        message, messageUnique,
                        hashes, hashesUnique,
                        files, filesUnique, true);
            } catch (Exception e) {
                Transaction.updateMapByErrorSimple(Transaction.INVALID_DATA, e.getMessage(), out);
                return out.toJSONString();
            }

            if (exDataBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
                Transaction.updateMapByErrorSimple(Transaction.INVALID_DATA_LENGTH, "Message size exceeded %1 kB"
                        .replace("%1", "" + (BlockChain.MAX_REC_DATA_BYTES >> 10)), out);
                return out.toJSONString();
            }

            // CREATE TX MESSAGE
            byte property1 = (byte) 0;
            byte property2 = (byte) 0;
            long key = 0L; // not need for 3 version

            RSignNote issueDoc = (RSignNote) Controller.getInstance().r_SignNote(RSignNote.CURRENT_VERS, property1, property2,
                    privateKeyAccount, feePow, key, exDataBytes);

            int validate = cntr.getTransactionCreator().afterCreate(issueDoc, Transaction.FOR_NETWORK, tryFree, test);

            if (validate == Transaction.VALIDATE_OK) {
                if (test)
                    out.put("status", "TEST");
                else
                    out.put("status", "OK");
                return out.toJSONString();
            } else {
                issueDoc.updateMapByError(validate, out);
                return out.toJSONString();
            }

        } finally {
            Controller.getInstance().lockWallet();
        }
    }

}
