package org.erachain.api;

import org.erachain.at.ATConstants;
import org.erachain.at.ATError;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class ApiErrorFactory {
    //COMMON

    //VALIDATION
    private static final int ERROR = Transaction.AT_ERROR + 10000;
    public static final int ERROR_UNKNOWN = ERROR;
    public static final int ERROR_JSON = ERROR + 1;

    //public static final int ERROR_INVALID_SIGNATURE = ERROR + 101;
    //public static final int ERROR_INVALID_ADDRESS = 102;
    public static final int ERROR_INVALID_SEED = ERROR + 1033;
    public static final int ERROR_INVALID_AMOUNT = Transaction.INVALID_AMOUNT;
    //public static final int Екфтыфсешщт = 2;
    //public static final int ERROR_NOT_YET_RELEASED = ERROR + 3;
    //public static final int ERROR_INVALID_FEE_POW = ERROR + 105;
    //public static final int ERROR_INVALID_SENDER = 106;
    //public static final int ERROR_INVALID_RECIPIENT = 107;
    //public static final int ERROR_INVALID_NAME_LENGTH = 108;
    //public static final int ERROR_INVALID_VALUE_LENGTH = 109;
    //public static final int ERROR_INVALID_NAME_OWNER = 110;
    //public static final int ERROR_INVALID_BUYER = 111;
    //public static final int ERROR_INVALID_PUBLIC_KEY = 112;
    //public static final int ERROR_INVALID_OPTIONS_LENGTH = 113;
    //public static final int ERROR_INVALID_OPTION_LENGTH = 114;
    //public static final int ERROR_INVALID_DATA = ERROR + 115;
    //public static final int ERROR_INVALID_DATA_LENGTH = 116;
    //public static final int ERROR_INVALID_UPDATE_VALUE = ERROR + 117;
    //public static final int ERROR_NAME_KEY_ALREADY_EXISTS = ERROR + 118;
    //public static final int ERROR_NAME_KEY_NOT_EXISTS = ERROR + 119;
    //public static final int ERROR_LAST_KEY_IS_DEFAULT_KEY_ERROR = ERROR + 120;
    //public static final int ERROR_FEE_LESS_REQUIRED = 121;
    public static final int ERROR_WALLET_NOT_IN_SYNC = ERROR + 122;
    public static final int ERROR_INVALID_NETWORK_ADDRESS = ERROR + 123;

    //WALLET
    public static final int ERROR_WALLET_NO_EXISTS = ERROR + 201;
    public static final int ERROR_WALLET_ADDRESS_NO_EXISTS = ERROR + 202;
    public static final int ERROR_WALLET_LOCKED = ERROR + 203;
    public static final int ERROR_WALLET_ALREADY_EXISTS = ERROR + 204;
    public static final int ERROR_WALLET_API_CALL_FORBIDDEN_BY_USER = ERROR + 205;
    public static final int ERROR_WALLET_PASSWORD_SO_SHORT = ERROR + 206;

    //BLOCKS
    //public static final int ERROR_BLOCK_NO_EXISTS = ERROR + 301;

    //TRANSACTIONS
    //public static final int ERROR_TRANSACTION_NO_EXISTS = ERROR + 311;
    //public static final int ERROR_PUBLIC_KEY_NOT_FOUND = ERROR + 304;

    //NAMING
    //public static final int ERROR_NAME_NO_EXISTS = 401;
    //public static final int ERROR_NAME_ALREADY_EXISTS = 402;
    //public static final int ERROR_NAME_ALREADY_FOR_SALE = 403;
    //public static final int ERROR_NAME_NOT_LOWER_CASE = 404;
    //public static final int ERROR_NAME_SALE_NO_EXISTS = 410;
    //public static final int ERROR_BUYER_ALREADY_OWNER = 411;

    //POLLS
    //public static final int ERROR_POLL_NO_EXISTS = 501;
    //public static final int ERROR_POLL_ALREADY_EXISTS = 502;
    //public static final int ERROR_DUPLICATE_OPTION = 503;
    //public static final int ERROR_POLL_OPTION_NO_EXISTS = 504;
    //public static final int ERROR_ALREADY_VOTED_FOR_THAT_OPTION = 505;

    //ASSET
    //public static final int ERROR_INVALID_ASSET_ID = 601;

    //NAME PAYMENTS
    //public static final int ERROR_NAME_NOT_REGISTERED = 701;
    //public static final int ERROR_NAME_FOR_SALE = 702;
    //public static final int ERROR_NAME_WITH_SPACE = 703;

    //ATs
    public static final int ERROR_INVALID_DESC_LENGTH = ERROR + 801;
    public static final int ERROR_EMPTY_CODE = ERROR + 802;
    //public static final int ERROR_DATA_SIZE = ERROR + 803;
    public static final int ERROR_NULL_PAGES = ERROR + 804;
    public static final int ERROR_INVALID_TYPE_LENGTH = ERROR + 805;
    public static final int ERROR_INVALID_TAGS_LENGTH = ERROR + 806;
    public static final int ERROR_INVALID_CREATION_BYTES = ERROR + 809;

    //BLOG/Namestorage
    public static final int ERROR_BODY_EMPTY = ERROR + 901;
    public static final int ERROR_BLOG_DISABLED = ERROR + 902;
    public static final int ERROR_NAME_NOT_OWNER = ERROR + 903;
    public static final int ERROR_TX_AMOUNT = ERROR + 904;
    public static final int ERROR_BLOG_ENTRY_NO_EXISTS = ERROR + 905;
    public static final int ERROR_BLOG_EMPTY = ERROR + 906;
    public static final int ERROR_POSTID_EMPTY = ERROR + 907;
    public static final int ERROR_POST_NOT_EXISTING = ERROR + 908;
    public static final int ERROR_COMMENTING_DISABLED = ERROR + 909;
    public static final int ERROR_COMMENT_NOT_EXISTING = ERROR + 910;
    public static final int ERROR_INVALID_COMMENT_OWNER = ERROR + 911;

    //Messages
    public static final int ERROR_MESSAGE_FORMAT_NOT_HEX = ERROR + 1001;
    //public static final int ERROR_MESSAGE_BLANK = 1002;
    public static final int ERROR_NO_PUBLIC_KEY = ERROR + 1003;
    //public static final int ERROR_MESSAGESIZE_EXCEEDED = 1004;


    public static int BATCH_TX_AMOUNT = 50;


    private static ApiErrorFactory instance;
    private Map<Integer, String> errorMessages;

    public ApiErrorFactory() {
        this.errorMessages = new HashMap<Integer, String>();

        //COMMON
        this.errorMessages.put(ERROR_UNKNOWN, Lang.T("unknown error"));
        this.errorMessages.put(ERROR_JSON, Lang.T("failed to parse json message"));
        //this.errorMessages.put(ERROR_NO_BALANCE, Lang.T("not enough balance"));
        //this.errorMessages.put(ERROR_NOT_YET_RELEASED, Lang.T("that feature is not yet released"));

        //VALIDATION
        //this.errorMessages.put(ERROR_INVALID_SIGNATURE, Lang.T("invalid signature"));
        //this.errorMessages.put(ERROR_INVALID_ADDRESS, Lang.T("invalid address"));
        this.errorMessages.put(ERROR_INVALID_SEED, Lang.T("invalid seed"));
        //this.errorMessages.put(ERROR_INVALID_AMOUNT, Lang.T("invalid amount"));
        //this.errorMessages.put(ERROR_INVALID_FEE_POW, Lang.T("invalid fee"));
        //this.errorMessages.put(ERROR_INVALID_SENDER, Lang.T("invalid sender"));
        //this.errorMessages.put(ERROR_INVALID_RECIPIENT, Lang.T("invalid recipient"));
        //this.errorMessages.put(ERROR_INVALID_NAME_LENGTH, Lang.T("invalid name length"));
        //this.errorMessages.put(ERROR_INVALID_VALUE_LENGTH, Lang.T("invalid value length"));
        //this.errorMessages.put(ERROR_INVALID_NAME_OWNER, Lang.T("invalid name owner"));
        //this.errorMessages.put(ERROR_INVALID_BUYER, Lang.T("invalid buyer"));
        //this.errorMessages.put(ERROR_INVALID_PUBLIC_KEY, Lang.T("invalid public key"));
        //this.errorMessages.put(ERROR_INVALID_OPTIONS_LENGTH, Lang.T("invalid options length"));
        //this.errorMessages.put(ERROR_INVALID_OPTION_LENGTH, Lang.T("invalid option length"));
        //this.errorMessages.put(ERROR_INVALID_DATA, Lang.T("invalid data"));
        //this.errorMessages.put(ERROR_INVALID_DATA_LENGTH, Lang.T("invalid data length"));
        //this.errorMessages.put(ERROR_INVALID_UPDATE_VALUE, Lang.T("invalid update value"));
        //this.errorMessages.put(ERROR_KEY_ALREADY_EXISTS, Lang.T("key already exists, edit is false"));
        //this.errorMessages.put(ERROR_KEY_NOT_EXISTS, Lang.T("the key does not exist"));
        //this.errorMessages.put(ERROR_LAST_KEY_IS_DEFAULT_KEY_ERROR, Lang.T(("you can't delete the key \"%key%\" if it is the only key")).replace("%key%", Corekeys.DEFAULT.toString()));
        //this.errorMessages.put(ERROR_FEE_LESS_REQUIRED, Lang.T("fee less required"));
        this.errorMessages.put(ERROR_WALLET_NOT_IN_SYNC, Lang.T("wallet needs to be synchronized"));
        this.errorMessages.put(ERROR_INVALID_NETWORK_ADDRESS, Lang.T("invalid network address"));

        //WALLET
        this.errorMessages.put(ERROR_WALLET_NO_EXISTS, Lang.T("wallet does not exist"));
        this.errorMessages.put(ERROR_WALLET_ADDRESS_NO_EXISTS, Lang.T("address does not exist in wallet"));
        this.errorMessages.put(ERROR_WALLET_LOCKED, Lang.T("wallet is locked"));
        this.errorMessages.put(ERROR_WALLET_ALREADY_EXISTS, Lang.T("wallet already exists"));
        this.errorMessages.put(ERROR_WALLET_API_CALL_FORBIDDEN_BY_USER, Lang.T("user denied api call"));
        this.errorMessages.put(ERROR_WALLET_PASSWORD_SO_SHORT, Lang.T("password is too short"));

        //BLOCK
        //this.errorMessages.put(ERROR_BLOCK_NO_EXISTS, Lang.T("block does not exist"));

        //TRANSACTIONS
        //this.errorMessages.put(ERROR_TRANSACTION_NO_EXISTS, Lang.T("transactions does not exist"));
        //this.errorMessages.put(ERROR_PUBLIC_KEY_NOT_FOUND, Lang.T("public key not found"));

        //NAMING
        //this.errorMessages.put(ERROR_NAME_NO_EXISTS, Lang.T("name does not exist"));
        //this.errorMessages.put(ERROR_NAME_ALREADY_EXISTS, Lang.T("name already exists"));
        //this.errorMessages.put(ERROR_NAME_ALREADY_FOR_SALE, Lang.T("name already for sale"));
        //this.errorMessages.put(ERROR_NAME_NOT_LOWER_CASE, Lang.T("name must be lower case"));
        //this.errorMessages.put(ERROR_NAME_SALE_NO_EXISTS, Lang.T("namesale does not exist"));
        //this.errorMessages.put(ERROR_BUYER_ALREADY_OWNER, Lang.T("buyer is already owner"));

        //POLLS
        //this.errorMessages.put(ERROR_POLL_NO_EXISTS, Lang.T("poll does not exist"));
        //this.errorMessages.put(ERROR_POLL_ALREADY_EXISTS, Lang.T("poll already exists"));
        //this.errorMessages.put(ERROR_DUPLICATE_OPTION, Lang.T("not all options are unique"));
        //this.errorMessages.put(ERROR_POLL_OPTION_NO_EXISTS, Lang.T("option does not exist"));
        //this.errorMessages.put(ERROR_ALREADY_VOTED_FOR_THAT_OPTION, Lang.T("already voted for that option"));

        //ASSETS
        //this.errorMessages.put(ERROR_INVALID_ASSET_ID, Lang.T("invalid asset id"));

        //NAME PAYMENTS
        //this.errorMessages.put(ERROR_NAME_NOT_REGISTERED, Lang.T(NameResult.NAME_NOT_REGISTERED.getStatusMessage()));
        //this.errorMessages.put(ERROR_NAME_FOR_SALE, Lang.T(NameResult.NAME_FOR_SALE.getStatusMessage()));
        //this.errorMessages.put(ERROR_NAME_WITH_SPACE, Lang.T(NameResult.NAME_WITH_SPACE.getStatusMessage()));

        //AT
        this.errorMessages.put(ERROR_INVALID_CREATION_BYTES, Lang.T("error in creation bytes"));
        this.errorMessages.put(ERROR_INVALID_DESC_LENGTH, Lang.T("invalid description length. max length ") + ATConstants.DESC_MAX_LENGTH);
        this.errorMessages.put(ERROR_EMPTY_CODE, Lang.T("code is empty"));
        //this.errorMessages.put(ERROR_DATA_SIZE, Lang.T("invalid data length"));
        this.errorMessages.put(ERROR_INVALID_TYPE_LENGTH, Lang.T("invalid type length"));
        this.errorMessages.put(ERROR_INVALID_TAGS_LENGTH, Lang.T("invalid tags length"));
        this.errorMessages.put(ERROR_NULL_PAGES, Lang.T("invalid pages"));

        //BLOG
        this.errorMessages.put(ERROR_BODY_EMPTY, Lang.T("invalid body it must not be empty"));
        this.errorMessages.put(ERROR_BLOG_DISABLED, Lang.T("this blog is disabled"));
        this.errorMessages.put(ERROR_NAME_NOT_OWNER, Lang.T("the creator address does not own the author name"));
        this.errorMessages.put(ERROR_TX_AMOUNT, Lang.T("the data size is too large - currently only %BATCH_TX_AMOUNT% arbitrary transactions are allowed at once!").replace("%BATCH_TX_AMOUNT%", String.valueOf(BATCH_TX_AMOUNT)));
        this.errorMessages.put(ERROR_BLOG_ENTRY_NO_EXISTS, Lang.T("transaction with this signature contains no entries!"));
        this.errorMessages.put(ERROR_BLOG_EMPTY, Lang.T("this blog is empty"));
        this.errorMessages.put(ERROR_POSTID_EMPTY, Lang.T("the attribute postid is empty! this is the signature of the post you want to comment"));
        this.errorMessages.put(ERROR_POST_NOT_EXISTING, Lang.T("for the given postid no blogpost to comment was found"));
        this.errorMessages.put(ERROR_COMMENTING_DISABLED, Lang.T("commenting is for this blog disabled"));
        this.errorMessages.put(ERROR_COMMENT_NOT_EXISTING, Lang.T("for the given signature no comment was found"));
        this.errorMessages.put(ERROR_INVALID_COMMENT_OWNER, Lang.T("invalid comment owner"));


        //MESSAGES
        this.errorMessages.put(ERROR_MESSAGE_FORMAT_NOT_HEX, Lang.T("the Message format is not hex and not Base58 - correct the text or use isTextMessage = true"));
        //this.errorMessages.put(ERROR_MESSAGE_BLANK, Lang.T("The message attribute is missing or content is blank"));
        this.errorMessages.put(ERROR_NO_PUBLIC_KEY, Lang.T("ERROR_NO_PUBLIC_KEY"));
        //this.errorMessages.put(ERROR_MESSAGESIZE_EXCEEDED, Lang.T("Message size exceeded!"));

    }

    public static ApiErrorFactory getInstance() {
        if (instance == null) {
            instance = new ApiErrorFactory();
        }

        return instance;
    }

    public String messageError(int error) {
        return this.errorMessages.get(error);
    }


    @SuppressWarnings("unchecked")
    public JSONObject createErrorJSON(int error) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", error);

        if (error > ERROR) {
            // errors for API
            jsonObject.put("message", this.errorMessages.get(error));

        } else if (error > Transaction.AT_ERROR) {
            // AT errors
            jsonObject.put("message", ATError.getATError(error - Transaction.AT_ERROR));
        } else {
            // errors for Transaction
            //jsonObject.put("message", this.errorMessages.get(error));
            jsonObject.put("message", OnDealClick.resultMess(error));
        }

        return jsonObject;
    }

    public JSONObject createErrorJSON(int error, String value) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", error);
        jsonObject.put("value", value);

        if (error > ERROR) {
            // errors for API
            jsonObject.put("message", this.errorMessages.get(error));

        } else if (error > Transaction.AT_ERROR) {
            // AT errors
            jsonObject.put("message", ATError.getATError(error - Transaction.AT_ERROR));
        } else {
            // errors for Transaction
            //jsonObject.put("message", this.errorMessages.get(error));
            jsonObject.put("message", OnDealClick.resultMess(error));
        }

        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public JSONObject createErrorJSON(String error) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", error);

        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public WebApplicationException createError(int error) {
        //return new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
        return new WebApplicationException(Response.status(Response.Status.OK)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(createErrorJSON(error).toJSONString()).build());
    }

    @SuppressWarnings("unchecked")
    public WebApplicationException createError(String error) {
        //return new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
        return new WebApplicationException(Response.status(Response.Status.OK)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(createErrorJSON(error).toJSONString()).build());
    }

    public WebApplicationException createError(int error, String value) {
        return new WebApplicationException(Response.status(Response.Status.OK)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(createErrorJSON(error, value).toJSONString()).build());
    }

}
