package api;

import at.AT_Constants;
import at.AT_Transaction;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import controller.Controller;
import core.account.PrivateKeyAccount;
import core.crypto.Crypto;
import core.transaction.Transaction;
import datachain.DCSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import utils.APIUtils;
import utils.Converter;
import utils.Pair;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

@Path("at")
@Produces(MediaType.APPLICATION_JSON)
public class ATResource {
    @Context
    HttpServletRequest request;

    @GET
    @Path("id/{id}")
    public static String getAT(@PathParam("id") String id) {
        return DCSet.getInstance().getATMap().getAT(id).toJSON().toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/transactions/id/{id}")
    public static String getATTransactionsBySender(@PathParam("id") String id) {
        List<AT_Transaction> txs = DCSet.getInstance().getATTransactionMap().getATTransactionsBySender(id);

        JSONArray json = new JSONArray();
        for (AT_Transaction tx : txs) {
            json.add(tx.toJSON());
        }
        return json.toJSONString();

    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/creator/{creator}")
    public static String getATsByCreator(@PathParam("creator") String creator) {
        Iterable<String> ats = DCSet.getInstance().getATMap().getATsByCreator(creator);
        Iterator<String> iter = ats.iterator();

        JSONArray json = new JSONArray();
        while (iter.hasNext()) {
            json.add(iter.next());
        }
        return json.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/transactions/recipient/{id}")
    public static String getATTransactionsByRecipient(@PathParam("id") String id) {
        List<AT_Transaction> txs = DCSet.getInstance().getATTransactionMap().getATTransactionsByRecipient(id);

        JSONArray json = new JSONArray();
        for (AT_Transaction tx : txs) {
            json.add(tx.toJSON());
        }
        return json.toJSONString();

    }

    @GET
    public String getATs() {
        return this.getATsLimited(50);
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/type/{type}")
    public String getATsByType(@PathParam("type") String type) {
        Iterable<String> ats = DCSet.getInstance().getATMap().getTypeATs(type);
        Iterator<String> iter = ats.iterator();

        JSONArray json = new JSONArray();
        while (iter.hasNext()) {
            json.add(iter.next());
        }
        return json.toJSONString();

    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("limit/{limit}")
    public String getATsLimited(@PathParam("limit") int limit) {
        Iterable<String> ids = DCSet.getInstance().getATMap().getATsLimited(limit);
        JSONArray jsonArray = new JSONArray();
        Iterator<String> iter = ids.iterator();
        while (iter.hasNext()) {
            jsonArray.add(iter.next());
        }

        return jsonArray.toJSONString();

    }


    @POST
    @Consumes(MediaType.WILDCARD)
    public String deployAT(String x) {
        //READ JSON
        JSONObject jsonObject = (JSONObject) JSONValue.parse(x);

        //GET CREATOR
        String creator = (String) jsonObject.get("creator");


        //CHECK ADDRESS
        if (!Crypto.getInstance().isValidAddress(creator)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_SENDER);
                    Transaction.INVALID_ADDRESS);
        }

        String password = null;
        APIUtils.askAPICallAllowed(password, "POST at " + x, request);

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        //CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        //GET ACCOUNT
        PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(creator);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_CREATOR);
        }

        String name = (String) jsonObject.get("name");

        if (name.getBytes(StandardCharsets.UTF_8).length > AT_Constants.NAME_MAX_LENGTH || name.length() < 1) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_NAME_LENGTH);
        }

        String desc = (String) jsonObject.get("description");

        if (desc.getBytes(StandardCharsets.UTF_8).length > AT_Constants.DESC_MAX_LENGTH || desc.length() < 1) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_DESC_LENGTH);
        }

        String type = (String) jsonObject.get("type");

        if (type.getBytes(StandardCharsets.UTF_8).length > AT_Constants.TYPE_MAX_LENGTH || type.length() < 1) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_TYPE_LENGTH);
        }

        String tags = (String) jsonObject.get("tags");

        if (tags.getBytes(StandardCharsets.UTF_8).length > AT_Constants.TAGS_MAX_LENGTH || tags.length() < 1) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_TAGS_LENGTH);
        }
        int feePow;
        try {
            String feeString = (String) jsonObject.get("feePow");
            feePow = Integer.parseInt(feeString);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_FEE_POWER);

        }

        BigDecimal quantity;
        try {
            String quantityString = (String) jsonObject.get("quantity");
            quantity = new BigDecimal(quantityString);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_AMOUNT);

        }

        String code = (String) jsonObject.get("code");
        if (code.length() == 0) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_EMPTY_CODE);
        }

        String data = (String) jsonObject.get("data");

        if (data == null)
            data = "";
        if ((data.length() & 1) != 0) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_DATA_LENGTH);
        }

        int cpages = (code.length() / 2 / 256) + (((code.length() / 2) % 256) != 0 ? 1 : 0);

        String dPages = (String) jsonObject.get("dpages");
        String csPages = (String) jsonObject.get("cspages");
        String usPages = (String) jsonObject.get("uspages");

        int dpages = Integer.parseInt(dPages);
        int cspages = Integer.parseInt(csPages);
        int uspages = Integer.parseInt(usPages);

        if (dpages < 0 || cspages < 0 || uspages < 0) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NULL_PAGES);

        }

		/*
			byte[] balanceBytes = feePow.toByteArray();
			byte[] fill = new byte[8 - balanceBytes.length];
			balanceBytes = Bytes.concat(fill, balanceBytes);
		 */
        byte[] feePowBytes = new byte[1];
        feePowBytes[0] = (byte) feePow;
        //data = Bytes.concat(data, feePowBytes);


        long lFee = Longs.fromByteArray(feePowBytes);


        if ((cpages + dpages + cspages + uspages) * AT_Constants.getInstance().COST_PER_PAGE(DCSet.getInstance().getBlockMap().last()
                .getHeight(DCSet.getInstance())) > lFee) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_FEE_POWER);
        }
        BigDecimal minActivationAmountB = new BigDecimal((String) jsonObject.get("minActivationAmount"));

        byte[] minActivationAmountBytes = minActivationAmountB.unscaledValue().toByteArray();
        byte[] fillActivation = new byte[8 - minActivationAmountBytes.length];
        minActivationAmountBytes = Bytes.concat(fillActivation, minActivationAmountBytes);

        long minActivationAmount = Longs.fromByteArray(minActivationAmountBytes);

        int creationLength = 4;
        creationLength += 8; //pages
        creationLength += 8; //minActivationAmount
        creationLength += cpages * 256 <= 256 ? 1 : (cpages * 256 <= 32767 ? 2 : 4);
        creationLength += code.length() / 2;

        creationLength += dpages * 256 <= 256 ? 1 : (dpages * 256 <= 32767 ? 2 : 4); // data size
        creationLength += data.length() / 2;

        ByteBuffer creation = ByteBuffer.allocate(creationLength);
        creation.order(ByteOrder.LITTLE_ENDIAN);

        creation.putShort(AT_Constants.getInstance().AT_VERSION(DCSet.getInstance().getBlockMap().last()
                .getHeight(DCSet.getInstance())));
        creation.putShort((short) 0);
        creation.putShort((short) cpages);
        creation.putShort((short) dpages);
        creation.putShort((short) cspages);
        creation.putShort((short) uspages);
        creation.putLong(minActivationAmount);
        if (cpages * 256 <= 256)
            creation.put((byte) (code.length() / 2));
        else if (cpages * 256 <= 32767)
            creation.putShort((short) (code.length() / 2));
        else
            creation.putInt(code.length() / 2);
        byte[] codeBytes = Converter.parseHexString(code);
        if (codeBytes != null)
            creation.put(codeBytes);
        if (dpages * 256 <= 256)
            creation.put((byte) (data.length() / 2));
        else if (dpages * 256 <= 32767)
            creation.putShort((short) (data.length() / 2));
        else
            creation.putInt(data.length() / 2);
        byte[] dataBytes = Converter.parseHexString(data);
        if (dataBytes != null)
            creation.put(dataBytes);
        byte[] creationBytes = null;
        creationBytes = creation.array();

        PrivateKeyAccount sender = Controller.getInstance().getPrivateKeyAccountByAddress(creator);

        Pair<Transaction, Integer> result = Controller.getInstance().deployAT(sender, name, desc, type, tags, creationBytes, quantity, feePow);

        if (result.getB() == Transaction.VALIDATE_OK)
            return result.getA().toJson().toJSONString();
        else
            throw ApiErrorFactory.getInstance().createError(result.getB());

    }


}
