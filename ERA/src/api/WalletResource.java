package api;

import controller.Controller;
import core.crypto.Base58;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import utils.APIUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("wallet")
@Produces(MediaType.APPLICATION_JSON)
public class WalletResource {


    private static final Logger LOGGER = Logger.getLogger(WalletResource.class);

    @Context
    HttpServletRequest request;

    @SuppressWarnings("unchecked")
    @GET
    public String getWallet() {
        String password = null;
        APIUtils.askAPICallAllowed(password, "GET wallet", request);

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("exists", Controller.getInstance().doesWalletExists());
        jsonObject.put("isunlocked", Controller.getInstance().isWalletUnlocked());

        return jsonObject.toJSONString();
    }

    @GET
    @Path("/seed")
    public String getSeed() {

        String password = null;
        APIUtils.askAPICallAllowed(password, "GET wallet/seed", request);

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
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
        APIUtils.askAPICallAllowed(password, "GET wallet/synchronize", request);

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        if (!Controller.getInstance().isProcessingWalletSynchronize()) {

            // TODO: was
            Controller.getInstance().synchronizeWallet();
            Controller.getInstance().setNeedSyncWallet(true);

            return String.valueOf(true);
        } else {
            return String.valueOf(false);
        }
    }

    @GET
    @Path("/lock")
    public String lock() {
        String password = null;
        APIUtils.askAPICallAllowed(password, "GET wallet/lock", request);

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        return String.valueOf(Controller.getInstance().lockWallet());
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    public String createWallet(String x) {
        try {
            //READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String password = (String) jsonObject.get("password");

            password = null;
            APIUtils.askAPICallAllowed(password, "POST wallet " + x, request);

            boolean recover = (boolean) jsonObject.get("recover");
            String seed = (String) jsonObject.get("seed");
            int amount = ((Long) jsonObject.get("amount")).intValue();
            String path = (String) jsonObject.get("dir");

            //CHECK IF WALLET EXISTS
            if (Controller.getInstance().doesWalletExists()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ALREADY_EXISTS);
            }

            //DECODE SEED
            byte[] seedBytes;
            try {
                seedBytes = Base58.decode(seed);
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
            if (recover) {
                return String.valueOf(Controller.getInstance().recoverWallet(seedBytes, password, amount, path));
            } else {
                return String.valueOf(Controller.getInstance().createWallet(Controller.getInstance().getWalletLicense(), seedBytes, password, amount, path));
            }
        } catch (NullPointerException | ClassCastException e) {
            LOGGER.info(e);
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }
    }

    @POST
    @Path("/unlock")
    @Consumes(MediaType.WILDCARD)
    public String unlock(String x) {
        //String password = null;
        APIUtils.askAPICallAllowed(x, "POST wallet/unlock " + x, request);

        //JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
        //String password = (String) jsonObject.get("password");


        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        //return String.valueOf(Controller.getInstance().unlockWallet(password));
        return String.valueOf(Controller.getInstance().isWalletUnlocked());
    }

}
