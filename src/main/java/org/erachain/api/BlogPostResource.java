package org.erachain.api;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.Profile;
import org.erachain.core.web.blog.BlogEntry;
import org.erachain.datachain.DCSet;
import org.erachain.utils.APIUtils;
import org.erachain.utils.BlogUtils;
import org.erachain.utils.Corekeys;
import org.erachain.utils.Pair;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;

@Path("blogpost")
@Produces(MediaType.APPLICATION_JSON)
public class BlogPostResource {

    public static final String AUTHOR = "author";
    public static final String BLOGNAME_KEY = "blogname";
    public static final String TITLE_KEY = "title";
    public static final String SHARE_KEY = "share";
    public static final String DELETE_KEY = "delete";
    public static final String POST_KEY = "post";
    // THIS IS ONLY NEEDED FOR COMMENTS -> id of the post to comment!
    public static final String COMMENT_POSTID_KEY = "postid";

    @Context
    HttpServletRequest request;

    static void isPostAllowed(String blogname) {

        // MAINBLOG allows posting always
        if (blogname == null) {
            return;
        }

        String blogenable = DCSet.getInstance().getNameStorageMap()
                .getOpt(blogname, Corekeys.BLOGENABLE.toString());

        if (blogenable == null) {

            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_BLOG_DISABLED);
        }

    }

    @SuppressWarnings("unchecked")
    @DELETE
    @Path("/comment/{signature}")
    public String deleteCommentEntry(
            @PathParam("signature") String signatureOfComment) {
        try {

            BlogEntry commentEntryOpt = BlogUtils
                    .getCommentBlogEntryOpt(signatureOfComment);

            if (commentEntryOpt == null) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_COMMENT_NOT_EXISTING);
            }

            String creator = BlogUtils
                    .getCreatorOrBlogOwnerOpt(commentEntryOpt);

            // CHECK ACCOUNT IN WALLET

            if (Controller.getInstance().getWalletAccountByAddress(creator) == null) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_INVALID_COMMENT_OWNER);
            }

            String blognameOpt = commentEntryOpt.getBlognameOpt();

            // CHECK ADDRESS
            if (!Crypto.getInstance().isValidAddress(creator)) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_ADDRESS);
            }

            String password = null;
            APIUtils.askAPICallAllowed(password, "POST blogpost/comment/"
                    + signatureOfComment, request, true);

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

            // GET ACCOUNT
            PrivateKeyAccount account = Controller.getInstance()
                    .getWalletPrivateKeyAccountByAddress(creator);
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_ADDRESS);
            }

            JSONObject dataStructure = new JSONObject();

            dataStructure.put(DELETE_KEY, signatureOfComment);

            if (blognameOpt != null) {
                dataStructure.put(BLOGNAME_KEY, blognameOpt);
            }

            byte[] resultbyteArray = dataStructure.toJSONString().getBytes(
                    StandardCharsets.UTF_8);
            int feePow = 0;

            // SEND PAYMENT
            Pair<Transaction, Integer> result = Controller.getInstance()
                    .createArbitraryTransaction(
                            account,
                            null,
                            BlogUtils.COMMENT_SERVICE_ID,
                            dataStructure.toJSONString().getBytes(
                                    StandardCharsets.UTF_8), feePow);

            return ArbitraryTransactionsResource
                    .checkArbitraryTransaction(result);

        } catch (NullPointerException | ClassCastException e) {
            // JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON);
        }
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("/comment")
    public String commentBlogEntry(String x) {
        try {

            // READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String feePowStr = (String) jsonObject.get("feePow");
            String creator = (String) jsonObject.get("creator");
            String authorOpt = (String) jsonObject.get(BlogPostResource.AUTHOR);
            String title = (String) jsonObject.get("title");
            String body = (String) jsonObject.get("body");
            // this is the post we are commenting
            String postid = (String) jsonObject.get("postid");
            String password = (String) jsonObject.get("password");

            if (StringUtil.isBlank(body)) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_BODY_EMPTY);
            }

            if (StringUtil.isBlank(postid)) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_POSTID_EMPTY);
            }

            BlogEntry blogEntryOpt = BlogUtils.getBlogEntryOpt(postid);

            if (blogEntryOpt == null) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_POST_NOT_EXISTING);
            }

            String blognameOpt = blogEntryOpt.getBlognameOpt();

            // PARSE FEE
            int feePow = 0;
            try {
                feePow = Integer.parseInt(feePowStr);
            } catch (Exception e) {
            }

            // CHECK ADDRESS
            if (!Crypto.getInstance().isValidAddress(creator)) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_ADDRESS);
            }

            Profile profileOpt = Profile.getProfileOpt(blognameOpt);
            if (profileOpt != null && profileOpt.isCommentingDisabled()) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_COMMENTING_DISABLED);
            }

            APIUtils.askAPICallAllowed(password,
                    "POST blogpost/comment" + "\n" + x, request, true);

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

            if (authorOpt != null) {
                Account name = new Account(authorOpt);

                // Name is not owned by creator!
                if (name == null
                        || !name.getAddress().equals(creator)) {
                    throw ApiErrorFactory.getInstance().createError(
                            ApiErrorFactory.ERROR_NAME_NOT_OWNER);
                }

            }

            // CHECK ACCOUNT IN WALLET

            if (Controller.getInstance().getWalletAccountByAddress(creator) == null) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
            }

            // GET ACCOUNT
            PrivateKeyAccount account = Controller.getInstance()
                    .getWalletPrivateKeyAccountByAddress(creator);
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_ADDRESS);
            }

            JSONObject dataStructure = new JSONObject();

            dataStructure.put(TITLE_KEY, title);
            dataStructure.put(POST_KEY, body);
            dataStructure.put(COMMENT_POSTID_KEY, postid);

            if (blognameOpt != null) {
                dataStructure.put(BLOGNAME_KEY, blognameOpt);
            }

            if (authorOpt != null) {
                dataStructure.put(AUTHOR, authorOpt);
            }

            // SEND PAYMENT
            Pair<Transaction, Integer> result = Controller.getInstance()
                    .createArbitraryTransaction(
                            account,
                            null,
                            BlogUtils.COMMENT_SERVICE_ID,
                            dataStructure.toJSONString().getBytes(
                                    StandardCharsets.UTF_8), feePow);

            return ArbitraryTransactionsResource
                    .checkArbitraryTransaction(result);

        } catch (NullPointerException | ClassCastException e) {
            // JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON);
        }
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("/{blogname}")
    public String addBlogEntry(String x, @PathParam("blogname") String blogname) {
        try {

            // READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String feePowStr = (String) jsonObject.get("feePow");
            String creator = (String) jsonObject.get("creator");
            String authorOpt = (String) jsonObject.get(BlogPostResource.AUTHOR);
            String title = (String) jsonObject.get("title");
            String body = (String) jsonObject.get("body");
            String share = (String) jsonObject.get(BlogPostResource.SHARE_KEY);
            String delete = (String) jsonObject
                    .get(BlogPostResource.DELETE_KEY);
            String password = (String) jsonObject.get("password");


            if (StringUtil.isBlank(body)) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_BODY_EMPTY);
            }

            // PARSE FEE POW
            int feePow = 0;
            try {
                feePow = Integer.parseInt(feePowStr);
            } catch (Exception e) {
            }

            // CHECK ADDRESS
            if (!Crypto.getInstance().isValidAddress(creator)) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_ADDRESS);
            }

            isPostAllowed(blogname);

            APIUtils.askAPICallAllowed(password,
                    "POST blogpost/" + blogname + "\n" + x, request, true);

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

            if (authorOpt != null) {
                Account name = null; //DCSet.getInstance().getNameMap().get(authorOpt);

                // Name is not owned by creator!
                if (name == null
                        || !name.getAddress().equals(creator)) {
                    throw ApiErrorFactory.getInstance().createError(
                            ApiErrorFactory.ERROR_NAME_NOT_OWNER);
                }

            }

            // CHECK ACCOUNT IN WALLET

            if (Controller.getInstance().getWalletAccountByAddress(creator) == null) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
            }

            // GET ACCOUNT
            PrivateKeyAccount account = Controller.getInstance()
                    .getWalletPrivateKeyAccountByAddress(creator);
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_ADDRESS);
            }

            JSONObject dataStructure = new JSONObject();

            dataStructure.put(TITLE_KEY, title);
            dataStructure.put(POST_KEY, body);
            if (StringUtils.isNotBlank(share)) {
                dataStructure.put(BlogPostResource.SHARE_KEY, share);
            }

            // TODO add delete logic including errors here!
            if (StringUtils.isNotBlank(delete)) {
                dataStructure.put(BlogPostResource.DELETE_KEY, delete);
            }

            if (blogname != null) {
                dataStructure.put(BLOGNAME_KEY, blogname);
            }

            if (authorOpt != null) {
                dataStructure.put(AUTHOR, authorOpt);
            }

            // SEND PAYMENT
            Pair<Transaction, Integer> result = Controller.getInstance()
                    .createArbitraryTransaction(
                            account,
                            null,
                            777,
                            dataStructure.toJSONString().getBytes(
                                    StandardCharsets.UTF_8), feePow);

            return ArbitraryTransactionsResource
                    .checkArbitraryTransaction(result);

        } catch (NullPointerException | ClassCastException e) {
            // JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON);
        }
    }

}
