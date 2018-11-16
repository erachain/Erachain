package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.R_Send;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.DCSet;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

// 30/03

@Path("transactions")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsResource.class);

    @Context
    HttpServletRequest request;

    @GET
    @Path("signature/{signature}")
    public static String getTransactionsBySignature(@PathParam("signature") String signature) throws Exception {
        // DECODE SIGNATURE
        byte[] signatureBytes;
        try {
            signatureBytes = Base58.decode(signature);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        // GET TRANSACTION
        Transaction transaction = Controller.getInstance().getTransaction(signatureBytes);

        // CHECK IF TRANSACTION EXISTS
        if (transaction == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.TRANSACTION_DOES_NOT_EXIST);
        }

        return transaction.toJson().toJSONString();
    }

	/*
	@GET
	@Path("/{address}")
	public String getTransactions(@PathParam("address") String address) {
		return this.getTransactionsLimited(address, 50);
	}
	 */

    @GET
    public String getTransactions() {
        return this.getTransactionsLimited(50);
    }

    @GET
    @Path("address/{address}")
    public String getTransactionsTwo(@PathParam("address") String address) {
        return this.getTransactionsLimited(address, 50);
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("limit/{limit}")
    public String getTransactionsLimited(@PathParam("limit") int limit) {
        String password = null;
        APIUtils.askAPICallAllowed(password, "GET transactions/limit/" + limit, request);

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        // GET TRANSACTIONS
        List<Pair<Account, Transaction>> transactions = Controller.getInstance().getLastTransactions(limit);

        // ORGANIZE TRANSACTIONS
        Map<Account, List<Transaction>> orderedTransactions = new HashMap<Account, List<Transaction>>();
        for (Pair<Account, Transaction> transaction : transactions) {
            if (!orderedTransactions.containsKey(transaction.getA())) {
                orderedTransactions.put(transaction.getA(), new ArrayList<Transaction>());
            }

            orderedTransactions.get(transaction.getA()).add(transaction.getB());
        }

        // CREATE JSON OBJECT
        JSONArray orderedTransactionsJSON = new JSONArray();

        for (Account account : orderedTransactions.keySet()) {
            JSONArray transactionsJSON = new JSONArray();
            for (Transaction transaction : orderedTransactions.get(account)) {
                transactionsJSON.add(transaction.toJson());
            }

            JSONObject accountTransactionsJSON = new JSONObject();
            accountTransactionsJSON.put("account", account.getAddress());
            accountTransactionsJSON.put("transactions", transactionsJSON);
            orderedTransactionsJSON.add(accountTransactionsJSON);
        }

        return orderedTransactionsJSON.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("address/{address}/limit/{limit}")
    public String getTransactionsLimited(@PathParam("address") String address, @PathParam("limit") int limit) {
        String password = null;
        APIUtils.askAPICallAllowed(password, "GET transactions/address/" + address + "/limit/" + limit, request);

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        // CHECK ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        // CHECK ACCOUNT IN WALLET
        Account account = Controller.getInstance().getAccountByAddress(address);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
        }

        JSONArray array = new JSONArray();
        for (Transaction transaction : Controller.getInstance().getLastTransactions(account, limit)) {
            array.add(transaction.toJson());
        }

        return array.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/network")
    public String getNetworkTransactions() {
        List<Transaction> transactions = Controller.getInstance().getUnconfirmedTransactions(0, 100, true);
        JSONArray array = new JSONArray();

        for (Transaction transaction : transactions) {
            // JSONArray peersList = new JSONArray<List<byte[]>>(transaction.a);
            array.add(transaction.toJson());
        }

        return array.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/unconfirmedof/{address}")
    public String getNetworkTransactions(@PathParam("address") String address) {
        List<Transaction> transactions = Controller.getInstance().getUnconfirmedTransactionsByAddressFast100(address);
        JSONArray array = new JSONArray();

        for (Transaction transaction : transactions) {
            array.add(transaction.toJson());
        }

        return array.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/unconfirmedincomes/{address}")
    // get transactions/unconfirmedincomes/7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7?from=123&count=13&descending=true
    public String getNetworkIncomesTransactions(@PathParam("address") String address,
            @QueryParam("from") int from, @QueryParam("count") int count,
            @QueryParam("type") int type, @QueryParam("descending") boolean descending) {

        JSONArray array = new JSONArray();

        DCSet dcSet = DCSet.getInstance();

        for (Transaction record : dcSet.getTransactionMap().getIncomedTransactions(address, type, from, count, descending)) {
            record.setDC(dcSet);
            array.add(record.toJson());
        }

        return array.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("/scan")
    public String scanTransactions(String x) {
        try {
            // READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);

            // GET BLOCK
            Block block = null;
            if (jsonObject.containsKey("start")) {
                byte[] signatureBytes;
                try {
                    String signature = (String) jsonObject.get("start");
                    signatureBytes = Base58.decode(signature);
                } catch (Exception e) {
                    throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
                }

                block = Controller.getInstance().getBlock(signatureBytes);

                // CHECK IF BLOCK EXISTS
                if (block == null) {
                    throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
                }
            }

            // CHECK FOR BLOCKLIMIT
            int blockLimit = -1;
            try {
                blockLimit = ((Long) jsonObject.get("blocklimit")).intValue();
            } catch (ClassCastException e) {
                // JSON EXCEPTION
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }

            if (blockLimit > 360) // 360 ensures at least six hours of
            // blocks can be queried at once
            {
                String ipAddress = ServletUtils.getRemoteAddress(request);
                if (!ServletUtils.isRemoteRequest(request, ipAddress))
                    throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }

            // CHECK FOR TRANSACTIONLIMIT
            int transactionLimit = -1;
            try {
                transactionLimit = ((Long) jsonObject.get("transactionlimit")).intValue();
            } catch (NullPointerException e) {
                // OPTION DOES NOT EXIST
            } catch (ClassCastException e) {
                // JSON EXCEPTION
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }

            // CHECK FOR TYPE
            int type = 0;
            try {
                type = ((Long) jsonObject.get("type")).intValue();
            } catch (NullPointerException e) {
                // OPTION DOES NOT EXIST
            } catch (ClassCastException e) {
                // JSON EXCEPTION
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }

            // CHECK FOR SERVICE
            int service = -1;
            try {
                service = ((Long) jsonObject.get("service")).intValue();
            } catch (NullPointerException e) {
                // OPTION DOES NOT EXIST
            } catch (ClassCastException e) {
                // JSON EXCEPTION
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }

            // CHECK FOR ACCOUNT
            Account account = null;
            try {
                if (jsonObject.containsKey("address")) {
                    String address = (String) jsonObject.get("address");

                    // CHECK ADDRESS
                    if (!Crypto.getInstance().isValidAddress(address)) {
                        throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
                    }

                    account = new Account(address);
                }

            } catch (NullPointerException e) {
                // OPTION DOES NOT EXIST
            } catch (ClassCastException e) {
                // JSON EXCEPTION
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }

            // SCAN
            Pair<Block, List<Transaction>> result = Controller.getInstance().scanTransactions(block, blockLimit,
                    transactionLimit, type, service, account);

            // CONVERT RESULT TO JSON
            JSONObject json = new JSONObject();

            json.put("lastscanned", Base58.encode(result.getA().getSignature()));

            if (block != null) {
                json.put("amount",
                        result.getA().getHeight() - block.getHeight() + 1);
            } else {
                json.put("amount", result.getA().getHeight());
            }

            JSONArray transactions = new JSONArray();
            for (Transaction transaction : result.getB()) {
                transactions.add(transaction.toJson());
            }
            json.put("transactions", transactions);

            // RETURN
            return json.toJSONString();
        } catch (NullPointerException | ClassCastException e) {
            // JSON EXCEPTION
            LOGGER.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("recipient/{address}/limit/{limit}")
    public String getTransactionsByRecipient(@PathParam("address") String address, @PathParam("limit") int limit) {
        JSONArray array = new JSONArray();
        List<Transaction> txs = DCSet.getInstance().getTransactionFinalMap().getTransactionsByRecipient(address, limit);
        for (Transaction transaction : txs) {
            array.add(transaction.toJson());
        }

        return array.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("find")
    public String getTransactionsFind(String x) {
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) JSONValue.parse(x);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

        String address = (String) jsonObject.get("address");

        // CHECK IF VALID ADDRESS
        if (address != null && !Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        String sender = (String) jsonObject.get("sender");

        // CHECK IF VALID ADDRESS
        if (sender != null && !Crypto.getInstance().isValidAddress(sender)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        String recipient = (String) jsonObject.get("recipient");

        // CHECK IF VALID ADDRESS
        if (recipient != null && !Crypto.getInstance().isValidAddress(recipient)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        boolean count = false;
        if (jsonObject.containsKey("count")) {
            try {
                count = (boolean) jsonObject.get("count");
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }
        }

        boolean desc = false;
        if (jsonObject.containsKey("desc")) {
            try {
                desc = (boolean) jsonObject.get("desc");
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }
        }

        int offset = 0;
        if (jsonObject.containsKey("offset")) {
            try {
                offset = ((Long) jsonObject.get("offset")).intValue();
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }
        }

        int limit = 0;
        if (jsonObject.containsKey("limit")) {
            try {
                limit = ((Long) jsonObject.get("limit")).intValue();
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }
        }

        int minHeight = 0;
        if (jsonObject.containsKey("minHeight")) {
            try {
                minHeight = ((Long) jsonObject.get("minHeight")).intValue();
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }
        }

        int maxHeight = 0;
        if (jsonObject.containsKey("maxHeight")) {
            try {
                maxHeight = ((Long) jsonObject.get("maxHeight")).intValue();
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }
        }

        int type = 0;
        if (jsonObject.containsKey("type")) {
            try {
                type = ((Long) jsonObject.get("type")).intValue();
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }
        }

        int service = -1;
        if ((type == Transaction.ARBITRARY_TRANSACTION || type == 0) && jsonObject.containsKey("service")) {
            try {
                service = ((Long) jsonObject.get("service")).intValue();
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }

            type = Transaction.ARBITRARY_TRANSACTION;
        }

        if (count) {
            return String.valueOf(DCSet.getInstance().getTransactionFinalMap().findTransactionsCount(address, sender,
                    recipient, minHeight, maxHeight, type, service, desc, offset, limit));
        }

        JSONArray array = new JSONArray();
        List<Transaction> txs = DCSet.getInstance().getTransactionFinalMap().findTransactions(address, sender,
                recipient, minHeight, maxHeight, type, service, desc, offset, limit);
        for (Transaction transaction : txs) {
            array.add(transaction.toJson());
        }

        return array.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("sender/{address}/limit/{limit}")
    public String getTransactionsBySender(@PathParam("address") String address, @PathParam("limit") int limit) {

        JSONArray array = new JSONArray();
        List<Transaction> txs = DCSet.getInstance().getTransactionFinalMap().getTransactionsBySender(address, limit);
        for (Transaction transaction : txs) {
            array.add(transaction.toJson());
        }

        return array.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("address/{address}/type/{type}/limit/{limit}")
    public String getTransactionsByTypeAndAddress(@PathParam("address") String address, @PathParam("type") int type,
                                                  @PathParam("limit") int limit) {

        JSONArray array = new JSONArray();
        List<Transaction> txs = DCSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress(address,
                type, limit);
        for (Transaction transaction : txs) {
            array.add(transaction.toJson());
        }

        return array.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("incoming/{height}")
    public String incoming(@PathParam("height") int height) {

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        Block block;
        try {
            block = Controller.getInstance().getBlockByHeight(height);
            if (block == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
            }
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }

        // GET ACCOUNTS
        List<Account> accounts = Controller.getInstance().getAccounts();

        JSONArray array = new JSONArray();
        DCSet dcSet = DCSet.getInstance();

        int seqNo = 0;
        for (Transaction transaction : block.getTransactions()) {
            // FOR ALL ACCOUNTS
            synchronized (accounts) {
                for (Account account : accounts) {
                    transaction.setBlock(block, dcSet, Transaction.FOR_NETWORK, ++seqNo);
                    // CHECK IF INVOLVED
                    if (!account.equals(transaction.getCreator()) && transaction.isInvolved(account)) {
                        array.add(transaction.toJson());
                        break;
                    }
                }
            }
        }

        return array.toJSONString();

    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("incoming/{height}/{address}")
    public String incomingRecipient(@PathParam("height") int height, @PathParam("address") String address) {

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        Block block;
        try {
            block = Controller.getInstance().getBlockByHeight(height);
            if (block == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
            }
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }

        JSONArray array = new JSONArray();
        DCSet dcSet = DCSet.getInstance();

        int seqNo = 0;
        for (Transaction transaction : block.getTransactions()) {
            transaction.setDC(dcSet);
            // TODO: тут наверное поиск быстрее по HsahSet будет
            HashSet<Account> recipients = transaction.getRecipientAccounts();
            for (Account recipient : recipients) {
                if (recipient.equals(address)) {
                    transaction.setBlock(block, dcSet, Transaction.FOR_NETWORK, ++seqNo);
                    array.add(transaction.toJson());
                    break;
                }
            }
        }

        return array.toJSONString();

    }

    /*
     * used in FACE2FACE
     * http://127.0.0.1:9048/transactions/incoming/148598/7KC2LXsD6h29XQqqEa7EpwRhfv89i8imGK/decrypt/***
     * 
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("incoming/{height}/{address}/decrypt/{password}")
    public String incomingRecipientDecrypt(@PathParam("height") int height, @PathParam("address") String address,
                                           @PathParam("password") String password) {

        boolean needPass = true;

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        Block block;
        try {
            block = Controller.getInstance().getBlockByHeight(height);
            if (block == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
            }
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }

        JSONArray array = new JSONArray();
        Controller cntr = Controller.getInstance();
        DCSet dcSet = DCSet.getInstance();

        int seqNo = 0;
        for (Transaction transaction : block.getTransactions()) {
            transaction.setDC(dcSet);
            HashSet<Account> recipients = transaction.getRecipientAccounts();
            for (Account recipient : recipients) {
                if (recipient.equals(address)) {

                    transaction.setBlock(block, dcSet, Transaction.FOR_NETWORK, ++seqNo);
                    JSONObject json = transaction.toJson();

                    if (transaction instanceof R_Send) {

                        R_Send r_Send = (R_Send) transaction;
                        byte[] r_data = null;
                        r_data = r_Send.getData();

                        if (r_Send.isEncrypted()) {

                            if (r_data != null && r_data.length > 0) {

                                if (needPass) {
                                    APIUtils.askAPICallAllowed(password, "GET incoming for [" + height + "] DECRYPT data - password: " + password + "\n", request);
                                    needPass = false;
                                }
                                r_data = cntr.decrypt(transaction.getCreator(), recipient, r_data);
                                if (r_data == null) {
                                    json.put("message", "error decryption");
                                } else {
                                    if (r_Send.isText()) {
                                        try {
                                            json.put("message", new String(r_data, "UTF-8"));
                                        } catch (UnsupportedEncodingException e) {
                                            json.put("message", "error UTF-8");
                                        }
                                    } else {
                                        json.put("message", Base58.encode(r_data));
                                    }
                                }
                            }
                        }
                    }

                    array.add(json);
                    break;
                }
            }
        }

        return array.toJSONString();

    }

    // check1 = 1 - check transaction. not save in chain. return fee
    @SuppressWarnings("unchecked")
    @GET
    @Path("sendAsset")
    public String sendAsset(@QueryParam("sender") String sender1, @QueryParam("recipient") String recipient1,
                            @QueryParam("amount") String amount1, @QueryParam("message") String message1,
                            @QueryParam("title") String title1, @QueryParam("asset") int asset1, @QueryParam("password") String pass,
                            @QueryParam("check") String check1) {

        JSONObject out = new JSONObject();

        if (pass == null || pass.length() == 0
                || !Controller.getInstance().unlockOnceWallet(pass)) {
            out.put("status_code", 0);
            out.put("status", "Invalid Password");
            return out.toJSONString();
        }

        // READ SENDER
        Account sender;
        try {
            sender = new Account(sender1);
            if (sender.getAddress() == null)
                throw new Exception("");
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            out.put("status_code", Transaction.INVALID_CREATOR);
            out.put("status", "Invalid Sender");
            return out.toJSONString();
        }

        // READ RECIPIENT
        Account recip;
        try {
            recip = new Account(recipient1);
            if (recip.getAddress() == null)
                throw new Exception("");
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            // e1.printStackTrace();
            out.put("status_code", Transaction.INVALID_ADDRESS);
            out.put("status", "Invalid Recipient Address");
            return out.toJSONString();
        }
        BigDecimal amount;
        // READ AMOUNT
        try {
            amount = new BigDecimal(amount1);
            if (amount.equals(new BigDecimal(0)))
                throw new Exception("");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            out.put("status_code", Transaction.INVALID_AMOUNT);
            out.put("status", "Invalid Amount");
            return out.toJSONString();
        }
        String message;
        try {
            message = message1;
            if (message != null) {
                if (message.length() > BlockChain.MAX_REC_DATA_BYTES)
                    throw new Exception("");
            } else {
                message = "";
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            // e.printStackTrace();
            out.put("status_code", Transaction.INVALID_DESCRIPTION_LENGTH);
            out.put("status", "Invalid message");
            return out.toJSONString();
        }
        byte[] encrypted = new byte[]{0};
        byte[] isTextByte = new byte[]{1};

        // asset
        try {
            AssetCls asset = Controller.getInstance().getAsset(asset1);
            if (asset == null)
                throw new Exception("");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            out.put("status_code", 1000000);
            out.put("status", "Invalid asset");
            return out.toJSONString();
        }
        // title
        String head;
        try {
            head = title1;
            if (head == null)
                head = "";
            if (head.getBytes(StandardCharsets.UTF_8).length > 256)
                throw new Exception("");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            out.put("status_code", Transaction.INVALID_HEAD_LENGTH);
            out.put("status", "Invalid Title");
            return out.toJSONString();
        }

        // CREATE TX MESSAGE
        Transaction transaction;
        try {
            transaction = Controller.getInstance().r_Send(
                    Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), 0, recip, asset1, amount,
                    head, message.getBytes(Charset.forName("UTF-8")), isTextByte, encrypted);
            // test result = new Pair<Transaction, Integer>(null,
            // Transaction.VALIDATE_OK);
            if (transaction == null)
                throw new Exception("");
        } catch (Exception e) {
            out.put("status_code", Transaction.INVALID_TRANSACTION_TYPE);
            out.put("status", "Invalid Transaction");
            return out.toJSONString();
        }

        if (check1 != null || check1 == "1") {
            out.put("fee", transaction.getFee());
            out.put("status_code", Transaction.VALIDATE_OK);
            out.put("status", "ok");
            return out.toJSONString();

        }

        Integer result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

        // CHECK VALIDATE MESSAGE
        if (result != Transaction.VALIDATE_OK) {
            out.put("status_code", result);
            out.put("status", "error");
            return out.toJSONString();

        }
        out.put("status", "ok");
        out.put("status_code", Transaction.VALIDATE_OK);
        out.put("signature", Base58.encode(transaction.getSignature()));
        return out.toJSONString();
    }


    // GET transactions/datadecrypt/GerrwwEJ9Ja8gZnzLrx8zdU53b7jhQjeUfVKoUAp1StCDSFP9wuyyqYSkoUhXNa8ysoTdUuFHvwiCbwarKhhBg5?password=1
    @GET
    //@Produces("text/plain")
    @Path("datadecrypt/{signature}")
    public String dataDecrypt(@PathParam("signature") String signature, @QueryParam("password") String password) {

        byte[] signatureBytes;
        try {
            signatureBytes = Base58.decode(signature);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        // GET TRANSACTION
        Transaction transaction = Controller.getInstance().getTransaction(signatureBytes);

        // CHECK IF TRANSACTION EXISTS
        if (transaction == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.TRANSACTION_DOES_NOT_EXIST);
        }

        JSONObject out = new JSONObject();

        R_Send r_Send = (R_Send) transaction;
        Account account = Controller.getInstance().getAccountByAddress(r_Send.getCreator().getAddress());
        byte[] r_data = r_Send.getData();
        if (r_data == null || r_data.length == 0)
            return null;

        APIUtils.askAPICallAllowed(password, "POST decrypt data\n " + signature, request);

        byte[] ddd = Controller.getInstance().decrypt(r_Send.getCreator(), r_Send.getRecipient(), r_data);
        if (ddd == null) {
            return "wrong decryption";
        }

        if (r_Send.isText()) {
            try {
                String str = (new String(ddd, "UTF-8"));
                return str;
            } catch (UnsupportedEncodingException e) {
                return "error UTF-8";
            }
        } else {
            String str = Base58.encode(ddd);
            return str;
        }
    }
}
