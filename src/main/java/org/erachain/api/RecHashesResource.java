package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.transaction.Transaction;
import org.erachain.utils.APIUtils;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Deprecated
@Path("rec_hashes")
@Produces(MediaType.APPLICATION_JSON)
public class RecHashesResource {


    private static final Logger LOGGER = LoggerFactory.getLogger(RecHashesResource.class);

    @Context
    HttpServletRequest request;

    //@GET // from browser - not work X parameters
    @POST // from curl only
    @Consumes(MediaType.WILDCARD)

    // http://127.0.0.1:9085/rec_hashes?sender=78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5&feePow=0&password=1&url=123
    @SuppressWarnings("unchecked")
    public String hashes(String x) {
        try {

            // READ JSON
            Tuple3<JSONObject, PrivateKeyAccount, Integer> resultRequet = APIUtils.postPars(request, x);

            JSONObject jsonObject = resultRequet.a;
            PrivateKeyAccount maker = resultRequet.b;
            int feePow = resultRequet.c;

            String url = (String) jsonObject.get("url");
            String message = (String) jsonObject.get("message");
            String hashesStr = (String) jsonObject.get("hashes"); // :"12312 12123 234234"

            String password = (String) jsonObject.get("password");

			/*
			String isTextMessageString = (String) jsonObject
					.get("istext");
			String encryptString = (String) jsonObject.get("encrypt");

			boolean isTextMessage = true;
			if (isTextMessageString != null) {
				isTextMessage = Boolean.valueOf(isTextMessageString);
			}
						
			boolean encrypt = true;
			if (encryptString != null) {
				encrypt = Boolean.valueOf(encryptString);
			}
			*/

            // TODO this is duplicate code -> Send money Panel, we should add
            // check this up here to avoid leaking wallet information to remote user
            // full check is later to prompt user with calculated fee
            //APIUtils.disallowRemote(request);
            password = null;
            APIUtils.askAPICallAllowed(password, "POST rec_hashes\n " + x, request, true);


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

			/*
			if (true) {
				return hashes;
			}
			*/

            String[] hashes = hashesStr.split("[-, ]");

            Transaction transaction = Controller.getInstance()
                    .r_Hashes(maker, null, feePow,
                            url, message, hashes);

            int validate = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

            if (validate == Transaction.VALIDATE_OK)
                return transaction.toJson().toJSONString();
            else {
                JSONObject out = new JSONObject();
                Transaction.updateMapByErrorSimple(validate, out);
                return out.toJSONString();
            }

        } catch (NullPointerException | ClassCastException e) {
            // JSON EXCEPTION
            LOGGER.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON);
        }
    }
}
