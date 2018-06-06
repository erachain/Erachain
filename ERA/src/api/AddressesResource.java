package api;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.transaction.Transaction;
import datachain.DCSet;
import datachain.SortableList;
import utils.APIUtils;
import utils.Pair;

@Path("addresses")
@Produces(MediaType.APPLICATION_JSON)
public class AddressesResource {

    @Context
    HttpServletRequest request;

    @GET
    @Path("balance/{address}")
    public static String getGeneratingBalance(
            @PathParam("address") String address) {
        return getGeneratingBalance(address, 1);
    }

    @GET
    @Path("balance/{address}/{confirmations}")
    public static String getGeneratingBalance(
            @PathParam("address") String address,
            @PathParam("confirmations") int confirmations) {
        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);
        }

        return new Account(address).getConfBalance3(confirmations, Transaction.FEE_KEY).toString();
    }

    @SuppressWarnings("unchecked")
    @GET
    public String getAddresses(@QueryParam("password") String password) {

        APIUtils.askAPICallAllowed(password, "GET addresses", request);

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        // GET ACCOUNTS
        List<Account> accounts = Controller.getInstance().getAccounts();

        // CONVERT TO LIST OF ADDRESSES
        JSONArray addresses = new JSONArray();
        for (Account account : accounts) {
            addresses.add(account.getAddress());
        }

        // RETURN
        return addresses.toJSONString();
    }

    @GET
    @Path("/lastreference/{address}")
    public String getLastReference(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);
        }

        // GET ACCOUNT
        Account account = new Account(address);

        Long lastTimestamp = account.getLastTimestamp();

        // RETURN

        if (lastTimestamp == null) {
            return "false";
        } else {
            return "" + lastTimestamp;
        }
    }

    @GET
    @Path("/lastreference/{address}/unconfirmed")
    public String getLastReferenceUnconfirmed(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);
        }

        // GET ACCOUNT
        Account account = new Account(address);

        HashSet<byte[]> isSomeoneReference = new HashSet<byte[]>();

        Controller cntrl = Controller.getInstance();

        List<Transaction> transactions = Controller.getInstance().getUnconfirmedTransactions(0, 10, true);

        DCSet db = DCSet.getInstance();
        Long lastTimestamp = account.getLastTimestamp(db);
        byte[] signature;
        if (!(lastTimestamp == null)) {
            signature = cntrl.getSignatureByAddrTime(db, address, lastTimestamp);
            transactions.add(cntrl.getTransaction(signature));
        }

        for (Transaction tx : transactions) {
            if (tx.getCreator().equals(account)) {
                for (Transaction tx2 : transactions) {
                    if (tx.getTimestamp() > tx2.getTimestamp()
                            & tx.getCreator().getAddress().equals(tx2.getCreator().getAddress())) {
                        // if same address and parent timestamp
                        isSomeoneReference.add(tx.getSignature());
                        break;
                    }
                }
            }
        }

        if (isSomeoneReference.isEmpty()) {
            return getLastReference(address);
        }

        for (Transaction tx : cntrl.getUnconfirmedTransactions(0, 10, true)) {
            if (tx.getCreator().equals(account)) {
                if (!isSomeoneReference.contains(tx.getSignature())) {
                    //return Base58.encode(tx.getSignature());
                    return "" + tx.getTimestamp();
                }
            }
        }

        return "false";
    }

    @GET
    @Path("/validate/{address}")
    public String validate(@PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        return String.valueOf(Crypto.getInstance().isValidAddress(address));
    }

    @GET
    @Path("/seed/{address}")
    public String getSeed(@PathParam("address") String address, @QueryParam("password") String password) {

        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);
        }

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        APIUtils.askAPICallAllowed(password, "GET addresses/seed/" + address + "\nWARNING, your seed will be revealed to the caller!", request);

        // CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        // CHECK ACCOUNT IN WALLET
        Account account = Controller.getInstance().getAccountByAddress(address);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
        }

        byte[] seed = Controller.getInstance().exportAccountSeed(address);
        return Base58.encode(seed);
    }

    @GET
    @Path("/new")
    public String generateNewAccount(@QueryParam("password") String password) {
        APIUtils.askAPICallAllowed(password, "GET addresses/new", request);

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        // CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        return Controller.getInstance().generateNewAccount();
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    public String createNewAddress(String x) {
        // CHECK IF CONTENT IS EMPTY
	String password = null;
	
        if (x.isEmpty()) {
            
            APIUtils.askAPICallAllowed(password, "POST addresses new\nGenerates a new account", request);

            // CHECK IF WALLET EXISTS
            if (!Controller.getInstance().doesWalletExists()) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
            }

            // CHECK WALLET UNLOCKED
            if (!Controller.getInstance().isWalletUnlocked()) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_WALLET_LOCKED);
            }

            return Controller.getInstance().generateNewAccount();
        } else {
            APIUtils.askAPICallAllowed(password, "POST addresses import seed\n " + x, request);

            String seed = x;

            // CHECK IF WALLET EXISTS
            if (!Controller.getInstance().doesWalletExists()) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
            }

            // CHECK WALLET UNLOCKED
            if (!Controller.getInstance().isWalletUnlocked()) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_WALLET_LOCKED);
            }

            // DECODE SEED
            byte[] seedBytes;
            try {
                seedBytes = Base58.decode(seed);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_INVALID_SEED);
            }

            // CHECK SEED LENGTH
            if (seedBytes == null || seedBytes.length != 32) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_INVALID_SEED);

            }

            // CONVERT TO BYTE
            return Controller.getInstance().importAccountSeed(seedBytes);
        }
    }

    @DELETE
    @Path("/{address}")
    public String deleteAddress(@PathParam("address") String address, @QueryParam("password") String password) {

        APIUtils.askAPICallAllowed(password, "DELETE addresses/" + address, request);

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        // CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        // DELETE
        PrivateKeyAccount account = Controller.getInstance()
                .getPrivateKeyAccountByAddress(address);
        return String.valueOf(Controller.getInstance().deleteAccount(account));
    }

    @GET
    @Path("/generatingbalance/{address}")
    public String getGeneratingBalanceOfAddress(
            @PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        Account account = new Account(address);
        return "" + BlockChain.calcWinValue(DCSet.getInstance(),
                account, Controller.getInstance().getBlockChain().getHeight(DCSet.getInstance()),
                account.getBalanceUSE(Transaction.RIGHTS_KEY, DCSet.getInstance()).intValue());
    }

    public JSONArray tuple5_toJson(Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance) {
        JSONArray item1 = new JSONArray();
        JSONArray item2 = new JSONArray();
        JSONArray item3 = new JSONArray();
        JSONArray item4 = new JSONArray();
        JSONArray item5 = new JSONArray();

        item1.add(balance.a.a.toPlainString());
        item1.add(balance.a.b.toPlainString());

        item2.add(balance.b.a.toPlainString());
        item2.add(balance.b.b.toPlainString());

        item3.add(balance.c.a.toPlainString());
        item3.add(balance.c.b.toPlainString());

        item4.add(balance.d.a.toPlainString());
        item4.add(balance.d.b.toPlainString());

        item5.add(balance.e.a.toPlainString());
        item5.add(balance.e.b.toPlainString());

        JSONArray result = new JSONArray();
        result.add(item1);
        result.add(item2);
        result.add(item3);
        result.add(item4);
        result.add(item5);

        return result;

    }

    ///  get addresses/assetbalance/1/7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC
    @GET
    @Path("assetbalance/{assetid}/{address}")
    public String getAssetBalance(@PathParam("assetid") String assetid,
                                  @PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        Long assetAsLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            assetAsLong = Long.valueOf(assetid);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemAssetMap().contains(assetAsLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);

        }

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = DCSet.getInstance().getAssetBalanceMap().get(address, assetAsLong);

        return tuple5_toJson(balance).toJSONString();
    }

    @GET
    @Path("assetbalanceown/{assetid}/{address}")
    public String getAssetBalanceOwn(@PathParam("assetid") String assetid,
                                     @PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        Long assetAsLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            assetAsLong = Long.valueOf(assetid);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemAssetMap().contains(assetAsLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);

        }

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = DCSet.getInstance().getAssetBalanceMap().get(address, assetAsLong);

        return balance.a.b.toPlainString();
    }

    @GET
    @Path("assetbalanceincomedown/{assetid}/{address}")
    public String getAssetBalanceIncomedOwn(@PathParam("assetid") String assetid,
                                            @PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        Long assetAsLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            assetAsLong = Long.valueOf(assetid);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemAssetMap().contains(assetAsLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);

        }

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = DCSet.getInstance().getAssetBalanceMap().get(address, assetAsLong);

        return balance.a.a.toPlainString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("assets/{address}")
    public String getAssetBalance(@PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetsBalances = DCSet.getInstance().getAssetBalanceMap().getBalancesSortableList(new Account(address));

        JSONObject assetsBalancesJSON = new JSONObject();

        for (Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetsBalance : assetsBalances) {
            assetsBalancesJSON.put(assetsBalance.getA().b, tuple5_toJson(assetsBalance.getB()));
        }

        return assetsBalancesJSON.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("sign/{address}")
    public String sign(String x, @PathParam("address") String address, @QueryParam("password") String password) {

        //String password = null;
        APIUtils.askAPICallAllowed(password, "POST addresses/sign/" + address, request);

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        // CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);
        }

        // GET OWNER
        PrivateKeyAccount account = Controller.getInstance()
                .getPrivateKeyAccountByAddress(address);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
        }

        JSONObject signatureJSON = new JSONObject();
        signatureJSON.put("message", x);
        signatureJSON.put("publickey", Base58.encode(account.getPublicKey()));
        signatureJSON.put(
                "signature",
                Base58.encode(Crypto.getInstance().sign(account,
                        x.getBytes(StandardCharsets.UTF_8))));

        return signatureJSON.toJSONString();
    }

    @POST
    @Path("verify/{address}")
    public String verify(String x, @PathParam("address") String address) {
        try {
            // READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String message = (String) jsonObject.get("message");
            String signature = (String) jsonObject.get("signature");
            String publicKey = (String) jsonObject.get("publickey");

            // CHECK IF VALID ADDRESS
            if (!Crypto.getInstance().isValidAddress(address)) {
                throw ApiErrorFactory.getInstance().createError(
                        //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                        Transaction.INVALID_ADDRESS);
            }

            // DECODE SIGNATURE
            byte[] signatureBytes;
            try {
                signatureBytes = Base58.decode(signature);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_SIGNATURE);

            }

            // DECODE PUBLICKEY
            byte[] publicKeyBytes;
            try {
                publicKeyBytes = Base58.decode(publicKey);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        //ApiErrorFactory.ERROR_INVALID_PUBLIC_KEY);
                        Transaction.INVALID_PUBLIC_KEY);

            }

            PublicKeyAccount account = new PublicKeyAccount(publicKeyBytes);

            // CHECK IF ADDRESS MATCHES
            if (!account.getAddress().equals(address)) {
                return String.valueOf(false);
            }

            return String.valueOf(Crypto.getInstance().verify(publicKeyBytes,
                    signatureBytes, message.getBytes(StandardCharsets.UTF_8)));
        } catch (NullPointerException e) {
            // JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON);
        } catch (ClassCastException e) {
            // JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON);
        }
    }

    @GET
    @Path("/publickey/{address}")
    public String getPublicKey(@PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        byte[] publicKey = Controller.getInstance().getPublicKeyByAddress(address);

        if (publicKey == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_PUBLIC_KEY);
        } else {
            return Base58.encode(publicKey);
        }
    }

    @GET
    @Path("/publickeygetaddress/{publickey}")
    public String getAddressByPublicKey(@PathParam("publickey") String publickey) {

        byte[] publicKeyBytes;
        try {
            publicKeyBytes = Base58.decode(publickey);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_PUBLIC_KEY);
                    Transaction.INVALID_PUBLIC_KEY);

        }

        PublicKeyAccount publicKey = new PublicKeyAccount(publicKeyBytes);

        return publicKey.getAddress();
    }

}
