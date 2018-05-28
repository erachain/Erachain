package api;

import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.transaction.Transaction;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;
import utils.APIUtils;
import utils.Converter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;

@Path("rec_statement")
@Produces(MediaType.APPLICATION_JSON)
public class Rec_StatementResource {


    private static final Logger LOGGER = Logger
            .getLogger(Rec_StatementResource.class);

    @Context
    HttpServletRequest request;

    @POST
    @Consumes(MediaType.WILDCARD)
    public String signNote(String x) {
        try {

            // READ JSON and TRY UNLOCK WALLET
            Tuple3<JSONObject, PrivateKeyAccount, Integer> resultRequet = APIUtils.postPars(request, x);

            JSONObject jsonObject = resultRequet.a;
            PrivateKeyAccount maker = resultRequet.b;
            int feePow = resultRequet.c;

            String templateKeyString = (String) jsonObject.get("template");
            String message = (String) jsonObject.get("message");
            String isTextMessageString = (String) jsonObject.get("istextmessage");
            String encryptString = (String) jsonObject.get("encrypt");

            if (maker == null)
                return "use parameters: template=12&maker=ADDRESS&message=MESS&istextmessage=true&encrypt=true&password=PASSWORD";

            boolean isTextMessage = true;
            if (isTextMessageString != null) {
                isTextMessage = Boolean.valueOf(isTextMessageString);
            }

            long templateKey = 0l;
            if (templateKeyString != null) {
                templateKey = Long.valueOf(templateKeyString);
            }


            boolean encrypt = true;
            if (encryptString != null) {
                encrypt = Boolean.valueOf(encryptString);
            }

            // TODO this is duplicate code -> Send money Panel, we should add
            // that to a common place later
            byte[] messageBytes = null;
            if (message != null) {
                if (isTextMessage) {
                    messageBytes = message.getBytes(StandardCharsets.UTF_8);
                } else {
                    try {
                        messageBytes = Converter.parseHexString(message);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        throw ApiErrorFactory.getInstance().createError(
                                ApiErrorFactory.ERROR_MESSAGE_FORMAT_NOT_HEX);
                    }
                }

                if (messageBytes.length > 4000) {
                    throw ApiErrorFactory.getInstance().createError(
                            Transaction.INVALID_DATA_LENGTH);
                }

                // TODO duplicate code -> SendMoneyPanel
                if (encrypt) {
                    // sender
                    byte[] privateKey = maker.getPrivateKey();

                    messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey,
                            maker.getPublicKey());
                }
            }

            byte[] encrypted = (encrypt) ? new byte[]{1} : new byte[]{0};
            byte[] isTextByte = (isTextMessage) ? new byte[]{1}
                    : new byte[]{0};

		/*	Pair<Transaction, Integer> result = Controller.getInstance()
					.signNote(false,
							maker,
							feePow, templateKey, messageBytes,
							isTextByte, encrypted);
	
			if (result.getB() == Transaction.VALIDATE_OK)
				return result.getA().toJson().toJSONString();
			else
				throw ApiErrorFactory.getInstance().createError(result.getB());
*/
            return "";
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            LOGGER.info(e);
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }
    }
}
