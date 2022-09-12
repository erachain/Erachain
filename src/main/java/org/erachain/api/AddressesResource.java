package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.CreditAddressesMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.utils.APIUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

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

        APIUtils.askAPICallAllowed(password, "GET addresses", request, true);

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletKeysExists()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        // GET ACCOUNTS
        List<Account> accounts = Controller.getInstance().getWalletAccounts();

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

        long[] lastTimestamp = account.getLastTimestamp();

        // RETURN

        if (lastTimestamp == null) {
            return "false";
        } else {
            return "" + lastTimestamp[0];
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

        DCSet db = DCSet.getInstance();
        long[] lastTimestamp = account.getLastTimestamp(db);
        if (lastTimestamp != null) {
            return "" + lastTimestamp[0];
        }

        byte[] signature;

        if (isSomeoneReference.isEmpty()) {
            return getLastReference(address);
        }

        // TODO: тут надо скан взять сразу для заданного адреса и последний
        // а вообще для чего нафиг это нужно?
        List<Transaction> items = DCSet.getInstance().getTransactionTab().getTransactionsByAddressFast100(address);
        if (items.isEmpty())
            return "false";

        return "" + items.get(items.size()).getTimestamp();
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
        if (!Controller.getInstance().doesWalletKeysExists()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        APIUtils.askAPICallAllowed(password, "GET addresses/seed/" + address + "\nWARNING, your seed will be revealed to the caller!", request, true);

        // CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        // CHECK ACCOUNT IN WALLET
        Account account = Controller.getInstance().getWalletAccountByAddress(address);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
        }

        byte[] seed = Controller.getInstance().exportAccountSeed(address);
        return Base58.encode(seed);
    }

    @GET
    @Path("/private/{address}")
    public Response getPrivate(@PathParam("address") String address, @QueryParam("password") String password) {

        // CHECK IF VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);
        }

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletKeysExists()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        APIUtils.askAPICallAllowed(password, "GET addresses/private/" + address + "\nWARNING, your private key will be revealed to the caller!", request, true);

        // CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        // CHECK ACCOUNT IN WALLET
        Account account = Controller.getInstance().getWalletAccountByAddress(address);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
        }

        PrivateKeyAccount privateKey = Controller.getInstance().getWalletPrivateKeyAccountByAddress(address);

        JSONObject json = new JSONObject();
        json.put("pubKey", Base58.encode(privateKey.getPublicKey()));
        json.put("privateKey", Base58.encode(privateKey.getPrivateKey()));
        json.put("seed", Base58.encode(privateKey.getSeed()));
        json.put("address", privateKey.getAddress());
        return Response.status(200).header("Content-Type", "text/html; charset=utf-8")
                //.header("Access-Control-Allow-Origin", "*")
                .entity(json.toJSONString()).build();

    }

    @GET
    @Path("/new")
    public String generateNewAccount(@QueryParam("password") String password) {
        APIUtils.askAPICallAllowed(password, "GET addresses/new", request, true);

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletKeysExists()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        // CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        return Controller.getInstance().generateNewWalletAccount();
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    public String createNewAddress(String x) {
        // CHECK IF CONTENT IS EMPTY
	String password = null;
	
        if (x.isEmpty()) {
            
            APIUtils.askAPICallAllowed(password, "POST addresses new\nGenerates a new account", request, true);

            // CHECK IF WALLET EXISTS
            if (!Controller.getInstance().doesWalletKeysExists()) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
            }

            // CHECK WALLET UNLOCKED
            if (!Controller.getInstance().isWalletUnlocked()) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_WALLET_LOCKED);
            }

            return Controller.getInstance().generateNewWalletAccount();
        } else {
            return importAccount(x, Crypto.HASH_LENGTH);
        }
    }

    @Deprecated
    @POST
    @Path("makepairbyseed")
    public String makePair_Old(String seed) {
        return makePair(seed);
    }

    @POST
    @Path("makepairbyaccountseed")
    public String makePair(String seed) {

        // DECODE SEED
        byte[] seedBytes;
        try {
            seedBytes = Base58.decode(seed, Crypto.HASH_LENGTH);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_INVALID_SEED);
        }

        // CHECK SEED LENGTH
        if (seedBytes == null || seedBytes.length != 32) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_INVALID_SEED);

        }

        // CREATE ACCOUNT
        PrivateKeyAccount account = new PrivateKeyAccount(seedBytes);
        JSONObject json = new JSONObject();
        json.put("pubKey", Base58.encode(account.getPublicKey()));
        json.put("privateKey", Base58.encode(account.getPrivateKey()));
        json.put("seed", Base58.encode(account.getSeed()));
        json.put("address", account.getAddress());
        return json.toJSONString();

    }

    /**
     * Make address pair by text phrase without insert in wallet
     *
     * @param phrase
     * @return
     */
    @POST
    @Path("makepairbyphrase")
    public String makePairByPhrase(String phrase) {

        // MAKE SEED
        byte[] seedBytes = Crypto.getInstance().doubleDigest(phrase.getBytes(StandardCharsets.UTF_8));

        // CREATE ACCOUNT
        PrivateKeyAccount account = new PrivateKeyAccount(seedBytes);
        JSONObject json = new JSONObject();
        json.put("pubKey", Base58.encode(account.getPublicKey()));
        json.put("privateKey", Base58.encode(account.getPrivateKey()));
        json.put("seed", Base58.encode(account.getSeed()));
        json.put("address", account.getAddress());
        return json.toJSONString();

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
                account.getBalanceUSE(Transaction.RIGHTS_KEY, DCSet.getInstance()).intValue(), null);
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
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        Long assetAsLong;

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

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance
                = result.a.getBalance(assetAsLong);

        return tuple5_toJson(balance).toJSONString();
    }

    @GET
    @Path("assetbalanceown/{assetid}/{address}")
    public String getAssetBalanceOwn(@PathParam("assetid") String assetid,
                                     @PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
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

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance
                = DCSet.getInstance().getAssetBalanceMap().get(Account.makeShortBytes(address), assetAsLong);

        return balance.a.b.toPlainString();
    }

    @GET
    @Path("assetbalanceincomedown/{assetid}/{address}")
    public String getAssetBalanceIncomedOwn(@PathParam("assetid") String assetid,
                                            @PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
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

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance
                = DCSet.getInstance().getAssetBalanceMap().get(Account.makeShortBytes(address), assetAsLong);

        return balance.a.a.toPlainString();
    }

    @GET
    @Path("creditors/{address}/{assetid}")
    public String getAssetCreditors(@PathParam("address") String address,
                                    @PathParam("assetid") Long assetKey) {
        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemAssetMap().contains(assetKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);

        }

        CreditAddressesMap map = DCSet.getInstance().getCreditAddressesMap();
        JSONArray out = new JSONArray();
        try (IteratorCloseable<Fun.Tuple3<String, Long, String>> iterator = map.getCreditorsIterator(address, assetKey)) {
            Fun.Tuple3<String, Long, String> key;
            BigDecimal credit;
            JSONArray item;
            while (iterator.hasNext()) {
                key = iterator.next();
                credit = map.get(key);

                item = new JSONArray();
                item.add(key.a);
                item.add(credit);

                out.add(item);


            }
        } catch (IOException e) {
        }

        return out.toJSONString();
    }

    @GET
    @Path("debitors/{address}/{assetid}")
    public String getAssetDebitors(@PathParam("address") String address,
                                   @PathParam("assetid") Long assetKey) {
        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);

        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemAssetMap().contains(assetKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);

        }

        CreditAddressesMap map = DCSet.getInstance().getCreditAddressesMap();
        JSONArray out = new JSONArray();
        try (IteratorCloseable<Fun.Tuple3<String, Long, String>> iterator = map.getDebitorsIterator(address, assetKey)) {
            Fun.Tuple3<String, Long, String> key;
            BigDecimal credit;
            JSONArray item;
            while (iterator.hasNext()) {
                key = iterator.next();
                credit = map.get(key);

                item = new JSONArray();
                item.add(key.c);
                item.add(credit);

                out.add(item);


            }
        } catch (IOException e) {
        }

        return out.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("assets/{address}")
    public String getAssetBalance(@PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                    Transaction.INVALID_ADDRESS);
        }

        ItemAssetBalanceMap map = DCSet.getInstance().getAssetBalanceMap();
        List<Tuple2<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> assetsBalances
                = map.getBalancesList(result.a);

        JSONObject assetsBalancesJSON = new JSONObject();

        for (Tuple2<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> assetsBalance : assetsBalances) {
            assetsBalancesJSON.put(ItemAssetBalanceMap.getAssetKeyFromKey(assetsBalance.a), tuple5_toJson(assetsBalance.b));
        }

        return assetsBalancesJSON.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("sign/{address}")
    public String sign(String x, @PathParam("address") String address) {

        //String password = null;
        APIUtils.askAPICallAllowed(null, "POST addresses/sign/" + address, request, true);

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletKeysExists()) {
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
                .getWalletPrivateKeyAccountByAddress(address);
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

    public String importAccount(String accountSeed, int base_len) {
        // CHECK IF CONTENT IS EMPTY
        String password = null;

        APIUtils.askAPICallAllowed(password, "GET addresses import Account", request, true);

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletKeysExists()) {
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
            seedBytes = Base58.decode(accountSeed, base_len);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_INVALID_SEED);
        }

        // CHECK SEED LENGTH
        if (seedBytes == null || seedBytes.length != base_len) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_INVALID_SEED);
        }

        Fun.Tuple3<String, Integer, String> result = Controller.getInstance().importAccountSeed(seedBytes);
        if (result.a == null)
            throw ApiErrorFactory.getInstance().createError(result.c);

        return result.a;
    }

    @Deprecated
    @GET
    @Path("/importaccountseed/{accountseed}")
    public String importAccountSeed_old(@PathParam("accountseed") String accountSeed) {
        return importAccount(accountSeed, Crypto.HASH_LENGTH);
    }

    @POST
    @Path("/importaccountseed")
    public String importAccountSeed(String accountSeed) {
        return importAccount(accountSeed, Crypto.HASH_LENGTH);
    }

    @Deprecated
    @GET
    @Path("/importprivatekey/{privatekey}")
    public String importPrivate_old(@PathParam("privatekey") String privateKey) {
        return importAccount(privateKey, Crypto.SIGNATURE_LENGTH);
    }

    @POST
    @Path("/importprivatekey")
    public String importPrivate(String privateKey) {
        return importAccount(privateKey, Crypto.SIGNATURE_LENGTH);
    }

}
