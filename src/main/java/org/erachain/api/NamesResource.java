package org.erachain.api;
// 30/03 ++ FeePOOWER

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.naming.Name;
import org.erachain.core.transaction.BuyNameTransaction;
import org.erachain.core.transaction.RegisterNameTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.UpdateNameTransaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Corekeys;
import org.erachain.utils.GZIP;
import org.erachain.utils.Pair;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.List;

@Path("names")
@Produces(MediaType.APPLICATION_JSON)
public class NamesResource {

    @Context
    HttpServletRequest request;

    @GET
    @Path("/{name}")
    public static String getName(@PathParam("name") String nameName) {
        Name name = Controller.getInstance().getName(nameName);

        // CHECK IF NAME EXISTS
        if (name == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.NAME_DOES_NOT_EXIST);
        }

        return name.toJson().toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    public String getNames() {
        String password = null;
        APIUtils.askAPICallAllowed(password, "GET names", request, true);

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        List<Pair<Account, Name>> names = Controller.getInstance().getNames();
        JSONArray array = new JSONArray();

        for (Pair<Account, Name> name : names) {
            array.add(name.getB().toJson());
        }

        return array.toJSONString();
    }

    @GET
    @Path("/address/{address}/values")
    public String getNamesValues(@PathParam("address") String address) {
        return getNames(address, true);
    }

    @GET
    @Path("/address/{address}")
    public String getNames(@PathParam("address") String address) {
        return getNames(address, false);
    }

    @SuppressWarnings("unchecked")
    public String getNames(String address, boolean values) {

        // CHECK ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);
        }

        JSONArray array = new JSONArray();

        // CHECK ACCOUNT IN WALLET
        Account account = Controller.getInstance().getAccountByAddress(address);
        if (account == null) {
            //SLOW METHOD FOR FOREIGN ADDRESSES

            HashSet<Name> names = new HashSet<>();

            for (Transaction transaction : DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressAndType(account.getShortAddressBytes(), Transaction.REGISTER_NAME_TRANSACTION, 0, 0)) {
                if (((RegisterNameTransaction) transaction).getName().getOwner().getAddress().equals(address)) {
                    names.add(((RegisterNameTransaction) transaction).getName());
                }
            }

            for (Transaction transaction : DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressAndType(account.getShortAddressBytes(), Transaction.UPDATE_NAME_TRANSACTION, 0, 0)) {
                if (((UpdateNameTransaction) transaction).getName().getOwner().getAddress().equals(address)) {
                    names.add(((UpdateNameTransaction) transaction).getName());
                }
            }

            for (Transaction transaction : DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressAndType(account.getShortAddressBytes(), Transaction.BUY_NAME_TRANSACTION, 0, 0)) {
                if (((BuyNameTransaction) transaction).getNameSale().getName().getOwner().getAddress().equals(address)) {
                    names.add(((BuyNameTransaction) transaction).getNameSale().getName());
                }
            }

            for (Name name : names) {
                if (values) {
                    array.add(name.toJson());
                } else {
                    array.add(name.getName());
                }
            }
        } else {
            //FAST METHOD FOR OWN ADDRESS
            for (Name name : Controller.getInstance().getNames(account)) {
                if (values) {
                    array.add(name.toJson());
                } else {
                    array.add(name.getName());
                }
            }
        }

        return array.toJSONString();
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    public String createName(String x) {
        try {
            // READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String fee = (String) jsonObject.get("fee");
            String registrant = (String) jsonObject.get("registrant");
            String name = (String) jsonObject.get("name");
            String value = (String) jsonObject.get("value");

            // PARSE FEE
            int feePow;
            try {
                feePow = Integer.parseInt(fee);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_FEE_POWER);
            }

            // CHECK ADDRESS
            if (!Crypto.getInstance().isValidAddress(registrant)) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_ADDRESS);
            }

            String password = null;
            APIUtils.askAPICallAllowed(password, "POST names " + x, request, true);

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

            // GET ACCOUNT
            PrivateKeyAccount account = Controller.getInstance()
                    .getPrivateKeyAccountByAddress(registrant);
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_ADDRESS);
            }

            // CREATE NAME
            Pair<Transaction, Integer> result = Controller.getInstance()
                    .registerName(account, account, name, value, feePow);

            if (result.getB() == Transaction.VALIDATE_OK)
                return result.getA().toJson().toJSONString();
            else
                throw ApiErrorFactory.getInstance().createError(result.getB());

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

    @SuppressWarnings("unchecked")
    @DELETE
    @Path("/key/{name}")
    public String removeKey(String x, @PathParam("name") String nameName) {
        try {
            // READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String fee = (String) jsonObject.get("fee");
            String key = (String) jsonObject.get("key");

            // keys are always lowercase!
            key = key.toLowerCase();

            // PARSE FEE
            int feePow;
            try {
                feePow = Integer.parseInt(fee);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_FEE_POWER);
            }

            String password = null;
            APIUtils.askAPICallAllowed(password, "DELETE names/key/" + nameName + "\n"
                    + x, request, true);

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


            // GET NAME
            Name name = Controller.getInstance().getName(nameName);
            if (name == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.NAME_DOES_NOT_EXIST);
            }

            // GET ACCOUNT
            PrivateKeyAccount account = Controller
                    .getInstance()
                    .getPrivateKeyAccountByAddress(name.getOwner().getAddress());
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.CREATOR_NOT_OWNER);

            }

            String oldValue = GZIP.webDecompress(name.getValue());
            JSONObject resultJson = null;
            try {
                resultJson = (JSONObject) JSONValue.parse(oldValue);
            } catch (Exception e) {
                // looks like no json
            }

            if (resultJson != null) {
                if (!resultJson.containsKey(key)) {

                    throw ApiErrorFactory.getInstance().createError(
                            Transaction.NAME_DOES_NOT_EXIST);

                } else {


                    if (resultJson.size() == 1) {
                        //THERE MUST BE at least one KEY always, so we convert it to the defaultkey
                        String oldVal = (String) resultJson.get(key);
                        resultJson.put(Corekeys.DEFAULT.toString(), oldVal);
                    }

                    resultJson.remove(key);

                }

            } else {

                //SPECIAL CASE USER TRIES TO DELETE LAST KEY
                if (key.equals(Corekeys.DEFAULT.toString())) {
                    throw ApiErrorFactory.getInstance().createError(
                            Transaction.LAST_KEY_IS_DEFAULT_KEY);
                }
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.NAME_DOES_NOT_EXIST);

            }

            String resultString = resultJson.toJSONString();

            // UPDATE NAME
            Pair<Transaction, Integer> result = Controller.getInstance()
                    .updateName(account, account, nameName,
                            GZIP.compress(resultString), feePow);

            return checkNameTransaction(result);

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

    @SuppressWarnings("unchecked")
    @POST
    @Path("/key/{name}")
    public String updateKey(String x, @PathParam("name") String nameName) {
        try {

            // READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String fee = (String) jsonObject.get("fee");
            String key = (String) jsonObject.get("key");
            String value = (String) jsonObject.get("value");
            String updateString = (String) jsonObject.get("update");

            // keys are always lowercase!
            key = key.toLowerCase();

            boolean update = true;

            if (updateString != null) {
                if (updateString.equalsIgnoreCase("true")
                        || updateString.equalsIgnoreCase("false")) {
                    update = Boolean.parseBoolean(updateString);
                } else {
                    throw ApiErrorFactory.getInstance().createError(
                            Transaction.INVALID_UPDATE_VALUE);
                }
            }

            // PARSE FEE
            int feePow;
            try {
                feePow = Integer.parseInt(fee);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_FEE_POWER);
            }

            String password = null;
            APIUtils.askAPICallAllowed(password,
                    "POST names/key/" + nameName + "\n" + x, request, true);

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

            // GET NAME
            Name name = Controller.getInstance().getName(nameName);
            if (name == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.NAME_DOES_NOT_EXIST);
            }

            // GET ACCOUNT
            PrivateKeyAccount account = Controller
                    .getInstance()
                    .getPrivateKeyAccountByAddress(name.getOwner().getAddress());
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.CREATOR_NOT_OWNER);
            }

            String oldValue = GZIP.webDecompress(name.getValue());
            JSONObject resultJson = null;
            try {
                resultJson = (JSONObject) JSONValue.parse(oldValue);
            } catch (Exception e) {
                // looks like no json
            }

            if (resultJson != null) {
                if (resultJson.containsKey(key)) {
                    if (!update) {
                        throw ApiErrorFactory.getInstance().createError(
                                Transaction.NAME_KEY_ALREADY_EXISTS);
                    }

                }
                resultJson.put(key, value);

            } else {
                // CONVERTING TO CORRECT FORMAT
                resultJson = new JSONObject();
                resultJson.put(Corekeys.DEFAULT.toString(), oldValue);
                resultJson.put(key, value);
            }

            String resultString = resultJson.toJSONString();

            // UPDATE NAME
            Pair<Transaction, Integer> result = Controller.getInstance()
                    .updateName(account, account, nameName,
                            GZIP.compress(resultString), feePow);

            return checkNameTransaction(result);

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

    @POST
    @Path("/{name}")
    @Consumes(MediaType.WILDCARD)
    public String updateName(String x, @PathParam("name") String nameName) {
        try {
            // READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String fee = (String) jsonObject.get("fee");
            String newOwner = (String) jsonObject.get("newowner");
            String newValue = (String) jsonObject.get("newvalue");

            // PARSE FEE
            int feePow;
            try {
                feePow = Integer.parseInt(fee);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_FEE_POWER);
            }

            // CHECK ADDRESS
            if (!Crypto.getInstance().isValidAddress(newOwner)) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_ADDRESS);
            }

            String password = null;
            APIUtils.askAPICallAllowed(password,
                    "POST names/" + nameName + "\n" + x, request, true);

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

            // GET NAME
            Name name = Controller.getInstance().getName(nameName);
            if (name == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.CREATOR_NOT_OWNER);
            }

            // GET ACCOUNT
            PrivateKeyAccount account = Controller
                    .getInstance()
                    .getPrivateKeyAccountByAddress(name.getOwner().getAddress());
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.CREATOR_NOT_OWNER);
            }

            // UPDATE NAME
            Pair<Transaction, Integer> result = Controller.getInstance()
                    .updateName(account, new Account(newOwner), nameName,
                            newValue, feePow);

            return checkNameTransaction(result);
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

    public String checkNameTransaction(Pair<Transaction, Integer> result) {
        if (result.getB() == Transaction.VALIDATE_OK)
            return result.getA().toJson().toJSONString();
        else
            throw ApiErrorFactory.getInstance().createError(result.getB());
    }
}
