package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.R_Hashes;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("rec_hashes")
@Produces(MediaType.APPLICATION_JSON)
public class Rec_HashesResource {


    private static final Logger LOGGER = LoggerFactory            .getLogger(Rec_HashesResource.class);

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
            APIUtils.askAPICallAllowed(password, "POST payment\n " + x, request, true);


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

			/*
			if (true) {
				return hashes;
			}
			*/

            String beginStr = hashesStr.substring(0, 100);
            List<String> twins;
            String[] hashes;
            if (beginStr.contains("-")) {
                hashes = hashesStr.split("-");
            } else {
                hashes = hashesStr.split(" ");
            }
            twins = R_Hashes.findTwins(DCSet.getInstance(), hashes);
            if (!twins.isEmpty()) {
                JSONObject json_result = new JSONObject();
                json_result.put("error", "twin hashes");
                JSONArray twins_array = new JSONArray();
                twins_array.addAll(twins);
                json_result.put("twins", twins_array);

                return json_result.toJSONString();
            }

            Pair<Transaction, Integer> result = Controller.getInstance()
                    .r_Hashes(maker, feePow,
                            url, message, hashes);

            if (result.getB() == Transaction.VALIDATE_OK) {
                //return result.getA().toJson().toJSONString();
                JSONObject json_result = new JSONObject();
                String b58 = Base58.encode(result.getA().getSignature());
                json_result.put("signature", b58);

                return json_result.toJSONString();
            } else
                throw ApiErrorFactory.getInstance().createError(result.getB());

        } catch (NullPointerException | ClassCastException e) {
            // JSON EXCEPTION
            LOGGER.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON);
        }
    }
}
