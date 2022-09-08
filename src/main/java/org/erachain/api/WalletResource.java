package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.utils.APIUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("wallet")
@Produces(MediaType.APPLICATION_JSON)
public class WalletResource {


    private static final Logger LOGGER = LoggerFactory.getLogger(WalletResource.class);

    @Context
    HttpServletRequest request;

    @SuppressWarnings("unchecked")
    @GET
    public String getWallet() {
        String password = null;
        APIUtils.askAPICallAllowed(password, "GET wallet", request, true);

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("exists", Controller.getInstance().doesWalletKeysExists());
        jsonObject.put("isunlocked", Controller.getInstance().isWalletUnlocked());

        return jsonObject.toJSONString();
    }

    @GET
    @Path("/seed")
    public String getSeed() {

        String password = null;
        APIUtils.askAPICallAllowed(password, "GET wallet/seed", request, true);

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletKeysExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        //CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        byte[] seed = Controller.getInstance().exportSeed();
        return Base58.encode(seed);
    }

    @GET
    @Path("/synchronize")
    public String synchronize() {
        String password = null;
        APIUtils.askAPICallAllowed(password, "GET wallet/synchronize", request, true);

        //CHECK IF WALLET EXISTSашч
        if (!Controller.getInstance().doesWalletKeysExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        if (!Controller.getInstance().getWallet().synchronizeBodyUsed) {

            // TODO: was
            //Controller.getInstance().synchronizeWallet();
            Controller.getInstance().getWallet().synchronizeFull();

            return String.valueOf(true);
        } else {
            return String.valueOf(false);
        }
    }

    @GET
    @Path("/lock")
    public String lock() {
        String password = null;
        //APIUtils.askAPICallAllowed(password, "GET wallet/lock", request);

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletKeysExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        return String.valueOf(Controller.getInstance().lockWallet());
    }

    /**
     * POST wallet {"seed":"6iqp9e", "password":"1",  "amount":5, "dir":<wallet dir>}
     *
     * @param x
     * @return
     */
    @POST
    @Consumes(MediaType.WILDCARD)
    public String createWallet(String x) {
        try {
            //READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);

            APIUtils.askAPICallAllowed(null, "POST wallet " + x, request, true);

            String password = (String) jsonObject.get("password");
            String seed = (String) jsonObject.get("seed");
            int amount = ((Long) jsonObject.getOrDefault("amount", 1L)).intValue();
            String path = (String) jsonObject.get("dir");

            //CHECK IF WALLET EXISTS
            if (Controller.getInstance().doesWalletKeysExists()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ALREADY_EXISTS);
            }

            //DECODE SEED
            byte[] seedBytes;
            try {
                seedBytes = Base58.decode(seed, Crypto.HASH_LENGTH);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_SEED);
            }

            //CHECK SEED LENGTH
            if (seedBytes.length != 32) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_SEED);
            }

            //CHECK AMOUNT
            if (amount < 1) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_AMOUNT);
            }

            //CREATE WALLET
            return String.valueOf(Controller.getInstance().recoverWallet(seedBytes, password, amount, path));

        } catch (NullPointerException | ClassCastException e) {
            LOGGER.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }
    }

    @POST
    @Path("/unlock")
    @Consumes(MediaType.WILDCARD)
    public String unlock(String x) {

        APIUtils.askAPICallAllowed(x, "POST wallet/unlock " + x, request, false);

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletKeysExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        return String.valueOf(Controller.getInstance().isWalletUnlocked());
    }

}
