package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.payment.Payment;
import org.erachain.core.transaction.Transaction;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("arbitrarytransactions")
@Produces(MediaType.APPLICATION_JSON)
public class ArbitraryTransactionsResource {

    private static final Logger LOGGER = LoggerFactory            .getLogger(ArbitraryTransactionsResource.class);
    @Context
    HttpServletRequest request;

    public static String checkArbitraryTransaction(Pair<Transaction, Integer> result) {


        if (result.getB() == Transaction.VALIDATE_OK)
            return result.getA().toJson().toJSONString();

        throw ApiErrorFactory.getInstance().createError(result.getB());
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    public String createArbitraryTransaction(String x) {
        try {
            String password = null;
            APIUtils.askAPICallAllowed(password, "POST arbitrarytransactions\n" + x, request, true);

            //READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            int service = ((Long) jsonObject.get("service")).intValue();
            String data = (String) jsonObject.get("data");
            String feePowStr = (String) jsonObject.get("feePow");
            String creator = (String) jsonObject.get("creator");

            long lgAsset = 0L;
            if (jsonObject.containsKey("asset")) {
                lgAsset = ((Long) jsonObject.get("asset")).intValue();
            }

            AssetCls defaultAsset;

            try {
                defaultAsset = Controller.getInstance().getAsset(new Long(lgAsset));
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                        Transaction.ITEM_ASSET_NOT_EXIST);

            }

            List<Payment> payments = MultiPaymentResource.jsonPaymentParser((JSONArray) jsonObject.get("payments"), defaultAsset);

            //PARSE DATA
            byte[] dataBytes;
            try {
                dataBytes = Base58.decode(data);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_DATA);

            }

            //PARSE FEE

            int feePow = 0;
            if (feePowStr != null) {
                try {
                    feePow = Integer.parseInt(feePowStr);
                } catch (Exception e) {
                    throw ApiErrorFactory.getInstance().createError(
                            Transaction.INVALID_FEE_POWER);

                }
            }

            //CHECK ADDRESS
            if (!Crypto.getInstance().isValidAddress(creator)) {
                throw ApiErrorFactory.getInstance().createError(
                        //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                        Transaction.INVALID_ADDRESS);

            }

            //CHECK IF WALLET EXISTS
            if (!Controller.getInstance().doesWalletKeysExists()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
            }


            //CHECK WALLET UNLOCKED
            if (!Controller.getInstance().isWalletUnlocked()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
            }

            //GET ACCOUNT
            PrivateKeyAccount account = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creator);
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(
                        //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                        Transaction.INVALID_ADDRESS);

            }

            //SEND PAYMENT
            Pair<Transaction, Integer> result = Controller.getInstance().createArbitraryTransaction(account, payments, service, dataBytes, feePow);

            return checkArbitraryTransaction(result);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            LOGGER.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }
    }
}
