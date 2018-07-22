package api;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import datachain.DCSet;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.mapdb.Fun;
import utils.APIUtils;
import utils.Pair;
import utils.StrJSonFine;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("trade")
@Produces(MediaType.APPLICATION_JSON)
public class TradeResource {

    private static final Logger LOGGER = Logger.getLogger(TradeResource.class);
    @Context
    HttpServletRequest request;

    @GET
    public String help() {
        Map<String, String> help = new LinkedHashMap<String, String>();
        help.put("GET trade/rater/{start/stop}",
                "Start Rater: 1 - start, 0 - stop");
        help.put("GET trade/create/{creator}/{haveKey}/{wantKey}/{haveAmount}/{wantAmount}?feePow={feePow}&password={password}",
                "make and broadcast CreateOrder ");
        help.put("GET trade/cancel/{creator}/{signature}?password={password}",
                "Cancel Order by orderID");
        help.put("GET trade/cancelbyid/{creator}/{orderID}?password={password}",
                "Cancel Order by orderID");

        return StrJSonFine.convert(help);
    }

    @GET
    @Path("rater/{status}")
    public String rater(@PathParam("status") Long status) {


        return "+";
    }
        /**
         * send and broadcast GET
         *
         * @param creatorStr   address in wallet
         * @param haveKey      haveKey
         * @param wantKey      wantKey
         * @param haveAmount   haveAmount or head
         * @param wantAmount   wantAmount
         * @param feePower     fee Power
         * @param password     password
         * @return JSON row
         *
         * <h2>Example request</h2>
         * GET create/7GvWSpPr4Jbv683KFB5WtrCCJJa6M36QEP/2/1/1.0/100.0?password=123456789
         * <h2>Example response</h2>
         * {}
         */
    @GET
    @Path("create/{creator}/{haveKey}/{wantKey}/{haveAmount}/{wantAmount}")
    public String sendGet(@PathParam("creator") String creatorStr,
                          @PathParam("haveKey") Long haveKey, @PathParam("wantKey") Long wantKey,
                          @PathParam("haveAmount") Double haveAmount, @PathParam("wantAmount") Double wantAmount,
                          @DefaultValue("0") @QueryParam("feePow") Long feePower, @QueryParam("password") String password) {

        if (!BlockChain.DEVELOP_USE)
            APIUtils.askAPICallAllowed(password, "GET create Order\n ", request);

        JSONObject out = new JSONObject();
        Controller cntr = Controller.getInstance();

        // READ CREATOR
        Fun.Tuple2<Account, String> resultCreator = Account.tryMakeAccount(creatorStr);
        if (resultCreator.b != null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_MAKER_ADDRESS);
        }

        AssetCls haveAsset = cntr.getAsset(haveKey);
        if (haveAsset == null)
            throw ApiErrorFactory.getInstance().createError(Transaction.ITEM_ASSET_NOT_EXIST);

        AssetCls wantAsset = cntr.getAsset(wantKey);
        if (wantAsset == null)
            throw ApiErrorFactory.getInstance().createError(Transaction.ITEM_ASSET_NOT_EXIST);

        PrivateKeyAccount privateKeyAccount = cntr.getPrivateKeyAccountByAddress(resultCreator.a.getAddress());
        if (privateKeyAccount == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_WALLET_ADDRESS);
        }

        Transaction transaction = cntr.createOrder(privateKeyAccount, haveAsset, wantAsset,
                new BigDecimal(haveAmount), new BigDecimal(wantAmount), feePower.intValue());

        int validate = cntr.getTransactionCreator().afterCreate(transaction, false);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            out.put("error", validate);
            out.put("error_message", gui.transaction.OnDealClick.resultMess(validate));
            return out.toJSONString();
        }

    }

    /**
     * send and broadcast GET
     *
     * @param creatorStr   address in wallet
     * @param signatureStr    signature
     * @param feePower     fee Power
     * @param password     password
     * @return JSON row
     *
     * <h2>Example request</h2>
     * GET cancelbyid/7GvWSpPr4Jbv683KFB5WtrCCJJa6M36QEP/1234567898765432134567899876545678?password=123456789
     * <h2>Example response</h2>
     * {}
     */
    @GET
    @Path("cancel/{creator}/{signature}")
    public String cancel(@PathParam("creator") String creatorStr,
                             @PathParam("signature") String signatureStr,
                             @DefaultValue("0") @QueryParam("feePow") Long feePower, @QueryParam("password") String password) {

        byte[] signature;
        try {
            signature = Base58.decode(signatureStr);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        return this.cancelByID(creatorStr, new BigInteger(signature), feePower, password);
    }

    /**
     * send and broadcast GET
     *
     * @param creatorStr   address in wallet
     * @param orderID      orderID
     * @param feePower     fee Power
     * @param password     password
     * @return JSON row
     *
     * <h2>Example request</h2>
     * GET cancelbyid/7GvWSpPr4Jbv683KFB5WtrCCJJa6M36QEP/1234567898765432134567899876545678?password=123456789
     * <h2>Example response</h2>
     * {}
     */
    @GET
    @Path("cancelbyid/{creator}/{orderID}")
    public String cancelByID(@PathParam("creator") String creatorStr,
                          @PathParam("orderID") BigInteger orderID,
                          @DefaultValue("0") @QueryParam("feePow") Long feePower, @QueryParam("password") String password) {

        if (!BlockChain.DEVELOP_USE)
            APIUtils.askAPICallAllowed(password, "GET cancel Order\n ", request);

        JSONObject out = new JSONObject();
        Controller cntr = Controller.getInstance();

        // READ CREATOR
        Fun.Tuple2<Account, String> resultCreator = Account.tryMakeAccount(creatorStr);
        if (resultCreator.b != null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_MAKER_ADDRESS);
        }

        if(!DCSet.getInstance().getOrderMap().contains(orderID)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.ORDER_DOES_NOT_EXIST);
        }

        PrivateKeyAccount privateKeyAccount = cntr.getPrivateKeyAccountByAddress(resultCreator.a.getAddress());
        if (privateKeyAccount == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_WALLET_ADDRESS);
        }

        Transaction transaction = cntr.cancelOrder2(privateKeyAccount, orderID,
                feePower.intValue());

        int validate = cntr.getTransactionCreator().afterCreate(transaction, false);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            out.put("error", validate);
            out.put("error_message", gui.transaction.OnDealClick.resultMess(validate));
            return out.toJSONString();
        }

    }

}
