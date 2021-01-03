package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.exdata.ExData;
import org.erachain.core.exdata.ExPays;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.utils.APIUtils;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("r_note")
@Produces(MediaType.APPLICATION_JSON)
public class RSignNoteResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSignNoteResource.class);
    @Context
    HttpServletRequest request;

    @GET
    public String help() {
        Map<String, String> help = new LinkedHashMap<String, String>();
        help.put("POST make/{fromAddress}/{assetKey}/{forAssetKey}?position=1&amount=0&test=true&feePow=0&activeafter=[date]&activebefore=[date]&greatequal=[amount]&koeff=1&title=&onlyperson=false&selfpay=false&password=",
                "Muli-send from Address [fromAddress] the asset [assetKey] by filter: Who has positive balance by asset [forAssetKey] where "
                        + " position - balance position for test, amount and koeff: sensed AMOUNT = amount + koeff * BALANCE, test - set false for real send or true for statistics, activeafter and activebefore - check activity for address in format: [timestamp_in_sec | YYYY-MM-DD HH:MM],"
                        + " greatequal=0 - if set balance in position must be great or equal this amount, activeTypeTX=0 - if set test activity on this type transactions,"
                        + "selfPay=true - if set pay to self address too. Default = true"
                        + " title=, onlyperson - get only personalized addresses, password=");
        //

        return StrJSonFine.convert(help);
    }


    /**
     * Multi send scrip for send asset for many addresses or persons filtered by some parameters.
     * This command will run as test for calculate FEE and total AMOUNT by default. For run real send set parameter `test=false`.
     * Unlock wallet.
     * <br>
     * fromAddress     my address in Wallet
     * assetKey        asset Key that send
     * * @param forAssetKey     asset key of holders test
     * * @param amount          absolute amount to send
     * * @param onlyPerson      Default: false. Use only person accounts
     * * @param gender          Filter by gender. -1 = all, 0 - man, 1 - woman. Default: -1.
     * * @param position        test balance position. 1 - Own, 2 - Credit, 3 - Hold, 4 - Spend, 5 - Other
     * * @param greatEqual      test balance is great or equal
     * * @param selfPay         if set - pay to self address too. Default = true
     * * @param test            default - true. test=false - real send
     * * @param feePow
     * * @param activeAfterStr  timestamp after that is filter - yyyy-MM-dd hh:mm or timestamp(sec)
     * * @param activeBeforeStr timestamp before that is filter - yyyy-MM-dd hh:mm or timestamp(sec) activetypetx
     * * @param activeTypeTX    if set - test only that type transactions
     * * @param coeff          coefficient for amount in balance position of forAssetKey
     * * @param title
     * * @param password
     *
     * @return
     */
    @POST
    @Path("make")
    public String multiSend(String x) {

        JSONObject jsonObject;
        try {
            //READ JSON
            jsonObject = (JSONObject) JSONValue.parse(x);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            ///logger.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

        if (jsonObject == null)
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);

        String password = (String) jsonObject.getOrDefault("password", null);

        int step = 0;

        String creator = (String) jsonObject.getOrDefault("creator", null);
        String recipient = (String) jsonObject.getOrDefault("recipient", null);
        String linkToRefStr = jsonObject.get("linkTo").toString();
        Long linkToRef;
        if (linkToRefStr == null)
            linkToRef = null;
        else {
            linkToRef = Transaction.parseDBRef(linkToRefStr);
            if (linkToRef == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR);
            }
        }

        boolean test = Boolean.valueOf((boolean) jsonObject.getOrDefault("test", true));

        String title = (String) jsonObject.getOrDefault("title", null);
        String message = (String) jsonObject.getOrDefault("message", null);

        int feePow = Integer.valueOf(jsonObject.getOrDefault("feePow", 0).toString());

        JSONObject payoutsJson = (JSONObject) jsonObject.get("payouts");
        ExPays payouts;
        if (payoutsJson == null) {
            payouts = null;
        } else {
            long assetKey = Long.valueOf(jsonObject.getOrDefault("assetKey", 0l).toString());
            int position = Integer.valueOf(jsonObject.getOrDefault("position", 1).toString());
            boolean backward = Boolean.valueOf((boolean) jsonObject.getOrDefault("backward", false));

            step++;
            int payMethod = Integer.valueOf(jsonObject.getOrDefault("method", 1).toString());
            String value = (String) jsonObject.get("methodValue");
            String amountMin = (String) jsonObject.get("amountMin");
            String amountMax = (String) jsonObject.get("amountMax");

            long filterAssetKey = Long.valueOf(jsonObject.getOrDefault("filterAssetKey", 0l).toString());
            int filterPos = Integer.valueOf(jsonObject.getOrDefault("filterBalPos", 1).toString());
            int filterSide = Integer.valueOf(jsonObject.getOrDefault("filterBalSide", 1).toString());
            String filterGreatEqual = (String) jsonObject.get("filterGreatEqual");
            String filterLessEqual = (String) jsonObject.get("filterLessEqual");

            int filterTXType = Integer.valueOf(jsonObject.getOrDefault("filterTXType", 1).toString());

            int encoding = Integer.valueOf(jsonObject.getOrDefault("encoding", 0).toString());
            boolean encrypt = Boolean.valueOf((boolean) jsonObject.getOrDefault("encrypt", false));

            int filterPerson = Integer.valueOf(jsonObject.getOrDefault("filterPerson", 0).toString());
            boolean selfPay = Boolean.valueOf((boolean) jsonObject.getOrDefault("selfPay", true));

            String filterTXStart = (String) jsonObject.get("activeAfter");
            String filterTXEnd = (String) jsonObject.get("activeBefore");

            Fun.Tuple2<ExPays, String> payoutsResult = ExPays.make(assetKey, position, backward, payMethod, value,
                    amountMin, amountMax, filterAssetKey, filterPos, filterSide,
                    filterGreatEqual, filterLessEqual,
                    filterTXType, filterTXStart, filterTXEnd,
                    filterPerson, selfPay);

            if (payoutsResult.a == null) {
                payouts = null;
            } else {
                payouts = payoutsResult.a;
            }

        }

        if (!test && !BlockChain.TEST_MODE
                && ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))
        )
            return "not LOCAL && not testnet";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:00");

        Controller cntr = Controller.getInstance();
        BlockChain chain = cntr.getBlockChain();


        if (!test) {
            // так как тут может очень долго работать то откроем на долго
            APIUtils.askAPICallAllowed(password, "GET multisend\n ", request, false);
        }
        try {

            JSONObject out = new JSONObject();
            JSONArray outResult = new JSONArray();

            byte[] exDataResult = ExData.make(exLink, exPayoutsResult.a, creator, jTextField_Title_Message.getText(),
                    signCanOnlyRecipients, recipients, authors, sources, tags, isEncrypted,
                    (TemplateCls) fill_Template_Panel.sel_Template, fill_Template_Panel.get_Params(),
                    fill_Template_Panel.checkBoxMakeHashAndCheckUniqueTemplate.isSelected(),
                    jTextPane_Message.getText(), checkBoxMakeHashAndCheckUniqueText.isSelected(),
                    hashes_Map, checkBoxMakeHashAndCheckUniqueHashes.isSelected(),
                    files_1, checkBoxMakeHashAndCheckUniqueAttachedFiles.isSelected());

            if (payouts != null) {
                out.put("_results", outResult);
                out.put("count", payouts.getTotalFeeBytes());
                out.put("totalFee", payouts.getTotalFeeBytes());
                out.put("totalSendAmount", payouts.getTotalPay().toPlainString());
            }

            if (test)
                out.put("status", "TEST");

            return out.toJSONString();

        } finally {
            Controller.getInstance().lockWallet();
        }
    }

}
