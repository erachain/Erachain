package org.erachain.webserver;
// 30/03

import com.google.common.base.Charsets;
import com.mitchellbosecke.pebble.error.PebbleException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.erachain.api.ApiErrorFactory;
import org.erachain.api.BlogPostResource;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.core.blockexplorer.WrongSearchException;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Base64;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.payment.Payment;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.*;
import org.erachain.core.web.blog.BlogEntry;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.datachain.ItemPersonMap;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.*;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/")
public class WebResource {

    private static final Logger logger = LoggerFactory.getLogger(WebResource.class);
    @Context
    HttpServletRequest request;
    String[] imgsArray = {"Erachain.org.png", "logo_header.png", "Erachain.org-user.png",
            "logo_bottom.png", "banner_01.png", "loading.gif",
            "00_generating.png", "01_genesis.jpg", "02_payment_in.png",
            "02_payment_out.png", "03_name_registration.png",
            "04_name_update.png", "05_name_sale.png",
            "06_cancel_name_sale.png", "07_name_purchase_in.png",
            "07_name_purchase_out.png", "08_poll_creation.jpg",
            "09_poll_vote.jpg", "10_arbitrary_transaction.png",
            "11_asset_issue.png", "12_asset_transfer_in.png",
            "12_asset_transfer_out.png", "13_order_creation.png",
            "14_cancel_order.png", "15_multi_payment_in.png", "check-yes.png", "check-no.png",
            "parentTx.png",
            "15_multi_payment_out.png", "16_deploy_at.png",
            "17_message_in.png", "17_message_out.png", "asset_trade.png",
            "at_tx_in.png", "at_tx_out.png", "grleft.png", "grright.png",
            "redleft.png", "redright.png", "bar.gif", "bar_left.gif",
            "bar_right.gif", "locked.png", "unlocked.png", "exchange.png"};

    public static String selectTitleOpt(Document htmlDoc) {
        String title = selectFirstElementOpt(htmlDoc, "title");

        return title;
    }

    public static String selectFirstElementOpt(Document htmlDoc, String tag) {
        Elements titleElements = htmlDoc.select(tag);
        String title = null;
        if (!titleElements.isEmpty()) {
            title = titleElements.get(0).text();
        }
        return title;
    }

    public static String selectDescriptionOpt(Document htmlDoc) {
        String result = "";
        Elements descriptions = htmlDoc.select("meta[name=\"description\"]");
        if (!descriptions.isEmpty()) {
            Element descr = descriptions.get(0);
            if (descr.hasAttr("content")) {
                result = descr.attr("content");
            }
        }

        return result;
    }

    public static String getFileExtention(String filename) {
        int dotPos = filename.lastIndexOf(".") + 1;
        return filename.substring(dotPos);
    }

    public static void addSharingAndLiking(BlogEntry blogEntry, String signature) {
        List<String> list = DCSet.getInstance().getSharedPostsMap()
                .get(Base58.decode(blogEntry.getSignature()));
        if (list != null) {
            for (String name : list) {
                blogEntry.addSharedUser(name);
            }
        }

        NameStorageMap nameStorageMap = DCSet.getInstance().getNameStorageMap();
        Set<String> keys = nameStorageMap.keySet();

        for (String name : keys) {
            Profile profileOpt = Profile.getProfileOpt(name);
            if (profileOpt != null) {
                if (profileOpt.getLikedPosts().contains(signature)) {
                    blogEntry.addLikingUser(profileOpt.getName().getAddress());
                }

            }
        }
    }

    public static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static String injectValues(String value) {
        // PROCESSING TAG INJ
        Pattern pattern = Pattern.compile("(?i)(<inj.*>(.*?)</inj>)");
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            Document doc = Jsoup.parse(matcher.group(1));
            Elements inj = doc.select("inj");
            Element element = inj.get(0);

            NameStorageMap nameMap = DCSet.getInstance().getNameStorageMap();
            String name = matcher.group(2);
            String result = "";
            if (nameMap.contains(name)) {

                if (element.hasAttr("key")) {
                    String key = element.attr("key");
                    String opt = nameMap.getOpt(name, key);
                    result = opt != null ? opt : "";

                }
            }
            value = value.replace(matcher.group(), result);

        }
        return value;
    }

    @GET
    public Response Default() {

        // REDIRECT
        return Response.status(302).header("Location", "index/main.html")
                .build();
    }

    public Response handleDefault() {
        try {

            String searchValue = request.getParameter("search");

            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/main.mini.html", request);

            if (searchValue == null) {

                return Response.ok(
                        PebbleHelper.getPebbleHelper("web/main.html", request)
                                .evaluate(), "text/html; charset=utf-8")
                        .build();
            }

            if (StringUtils.isBlank(searchValue)) {
                return Response.ok(

                        pebbleHelper.evaluate(), "text/html; charset=utf-8").build();
            }

            List<Pair<String, String>> searchResults;
            searchResults = null; //NameUtils.getWebsitesByValue(searchValue);

            List<HTMLSearchResult> results = generateHTMLSearchresults(searchResults);

            pebbleHelper.getContextMap().put("searchresults", results);

            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }
    }

    public List<HTMLSearchResult> generateHTMLSearchresults(
            List<Pair<String, String>> searchResults) throws IOException,
            PebbleException {
        List<HTMLSearchResult> results = new ArrayList<>();
        for (Pair<String, String> result : searchResults) {
            String name = result.getA();
            String websitecontent = result.getB();
            Document htmlDoc = Jsoup.parse(websitecontent);
            String title = selectTitleOpt(htmlDoc);
            title = title == null ? "" : title;
            String description = selectDescriptionOpt(htmlDoc);
            description = description == null ? "" : description;
            description = StringUtils.abbreviate(description, 150);
            results.add(new HTMLSearchResult(title, description, name, "/"
                    + name, "/" + name, "/namestorage:" + name, null));

        }
        return results;
    }

    @SuppressWarnings("rawtypes")
    @Path("index/blockexplorer.json")
    @GET
    public Response jsonQueryMain(@Context UriInfo info) {
        Map output;
        try {
            output = BlockExplorer.getInstance().
                    jsonQueryMain(info);
        } catch (WrongSearchException e) {
            logger.info(e.getMessage(), e);
            output = BlockExplorer.getInstance().getOutput();
            output.put("noSearchedElements", "true");
            output.put("error", e.getMessage());
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(StrJSonFine.convert(output))
                    .build();
        } catch (Exception ee) {
            //ee.printStackTrace();
            logger.error(ee.getMessage(), ee);
            StringBuilder ss = new StringBuilder();
            for (StackTraceElement item : ee.getStackTrace()) {
                ss.append(item.toString()).append("<br>");
            }
            return Response.status(501)
                    .header("Content-Type", "text/html; charset=utf-8")
                    .entity(ss + "<br>" + ee.getMessage())
                    .build();
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(output))
                .build();
    }

    @Path("index/blockexplorer")
    @GET
    public Response blockexplorer() {
        return blockexplorerhtml();
    }

    @GET
    @Path("blockexplorer")
    public Response newBlockExplorerIndex() {
        File file = new File("web/blockexplorer/index.html");
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            String type = URLConnection.guessContentTypeFromStream(is);
            return Response.ok(file, type).build();
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(e.getMessage(), e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("blockexplorer/{address: .+}")
    public Response newBlockExplorer(@PathParam("address") String address) {
        String addr = "web/blockexplorer/" + address;
        File file = new File(addr);

        if (!file.exists()) {
            file = new File("web/blockexplorer/index.html");
        }

        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            String type = URLConnection.guessContentTypeFromStream(is);
            return Response.ok(file, type).build();
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(e.getMessage(), e);
            return Response.status(500).build();
        }
    }

    @Path("index/blockexplorer.html")
    @GET
    public Response blockexplorerhtml() {
        String content;
        JSONObject langObj;
        String lang = request.getParameter("lang");
        try {
            content = readFile("web/blockexplorer.html",
                    StandardCharsets.UTF_8);


        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }

        Document doc = null;

        doc = Jsoup.parse(content);

        //	Element element = doc.getElementById("menu_top_100_");
        //	if (element != null)	element.text("werwfyrtyryrtyrtyrtyrtyrtyrtyrtytyrerwer");

        //

        if (lang != null) {

            //logger.info("try lang file: " + lang + ".json for " + request.getRemoteUser() + " " + request.getRequestURL());
            langObj = Lang.openLangFile(lang + ".json");

     /*   // translate select
            Elements el = doc.getElementsByTag("option");//.select("translate");
            for (Element e : el) {
                e.text(Lang.TFromLangObj(e.text(), langObj));
          }
      */        // translate links
            Elements el = doc.getElementsByAttributeValueContaining("translate", "true");//.select("translate");
            for (Element e : el) {
                e.text(Lang.T(e.text(), langObj));
            }
        }


        return Response.ok(doc.toString(), "text/html; charset=utf-8").build();

    }

    @Path("index/blogsearch.html")
    @GET
    public Response doBlogSearch() {

        String searchValue = request.getParameter("search");
        try {
            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/main.mini.html", request);
            if (StringUtil.isBlank(searchValue)) {

                return Response.ok(pebbleHelper.evaluate(),
                        "text/html; charset=utf-8").build();
            }

            List<HTMLSearchResult> results = handleBlogSearch(searchValue);
            pebbleHelper.getContextMap().put("searchresults", results);
            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }

    }

    @Path("index/test.html")
    @GET
    public Response test() {
        String content;
        JSONObject langObj;
        String lang = request.getParameter("lang");
        try {
            content = readFile("web/test.html",
                    StandardCharsets.UTF_8);


        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }

        Document doc = null;

        doc = Jsoup.parse(content);

        //	Element element = doc.getElementById("menu_top_100_");
        //	if (element != null)	element.text("werwfyrtyryrtyrtyrtyrtyrtyrtyrtytyrerwer");

        //

        if (lang != null) {

            logger.error("try lang file: " + lang + ".json");
            langObj = Lang.openLangFile(lang + ".json");


            Elements el = doc.select("translate");
            for (Element e : el) {
                e.text(Lang.T(e.text(), langObj));
            }
        }

        return Response.ok(doc.toString(), "text/html; charset=utf-8").build();

    }

    @Path("index/nsrepopulate.html")
    @GET
    public Response doNsRepopulate() {

        UpdateUtil.repopulateNameStorage(70000);
        return error404(request, "Namestorage repopulated!");

    }

    @Path("index/deleteunconfirmed.html")
    @GET
    public Response doDeleteUnconfirmedTxs() {

        DCSet dcSet = DCSet.getInstance();
        Collection<Transaction> values = dcSet.getTransactionTab().values();

        List<Account> myAccounts = Controller.getInstance().getWalletAccounts();

        for (Transaction transaction : values) {
            if (myAccounts.contains(transaction.getCreator())) {
                dcSet.getTransactionTab().delete(transaction);
            }
        }

        return error404(request, "Unconfirmed transactions removed.");

    }

    @Path("index/blogdirectory.html")
    @GET
    public Response doBlogdirectory() {

        try {
            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/main.mini.html", request);

            List<HTMLSearchResult> results = handleBlogSearch(null);
            pebbleHelper.getContextMap().put("searchresults", results);
            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }

    }

    @Path("index/messaging.html")
    @GET
    public Response getMessaging() {

        try {
            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/messaging.html", request);
            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }

    }

    @POST
    @Path("index/websitepreview.html")
    @Consumes("application/x-www-form-urlencoded")
    public Response previewWebsite(@Context HttpServletRequest request,
                                   MultivaluedMap<String, String> form) {

        try {
            String website = form.getFirst("website");

            website = website == null ? "" : website;

            return enhanceAndShowWebsite(website);

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }

    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("index/api.html")
    @Consumes("application/x-www-form-urlencoded")
    public Response createApiCall(@Context HttpServletRequest request,
                                  MultivaluedMap<String, String> form) throws IOException {

        String type = form.getFirst("type");
        String apiurl = form.getFirst("apiurl");

        String jsonContent = form.getFirst("json");
        JSONObject jsonanswer = new JSONObject();

        if (StringUtils.isBlank(type)
                || (!type.equalsIgnoreCase("get")
                && !type.equalsIgnoreCase("post") && !type
                .equalsIgnoreCase("delete"))) {

            jsonanswer.put("type", "apicallerror");
            jsonanswer.put("errordetail",
                    "type parameter must be post, get or delete");

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(jsonanswer.toJSONString()).build();
        }

        if (StringUtils.isBlank(apiurl)) {
            jsonanswer.put("type", "apicallerror");
            jsonanswer.put("errordetail",
                    "apiurl parameter must be correct set");

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(jsonanswer.toJSONString()).build();
        }

        // CREATE CONNECTION

        apiurl = apiurl.startsWith("/") ? apiurl.substring(1) : apiurl;

        URL urlToCall = new URL("http://127.0.0.1:"
                + Settings.getInstance().getRpcPort() + "/" + apiurl);
        HttpURLConnection connection = (HttpURLConnection) urlToCall
                .openConnection();
        connection.setRequestProperty("X-FORWARDED-FOR", ServletUtils.getRemoteAddress(request));

        // EXECUTE
        connection.setRequestMethod(type.toUpperCase());

        if (type.equalsIgnoreCase("POST")) {
            connection.setDoOutput(true);
            connection.getOutputStream().write(
                    jsonContent.getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
        }

        // READ RESULT
        InputStream stream;
        if (connection.getResponseCode() == 400) {
            stream = connection.getErrorStream();
        } else {
            stream = connection.getInputStream();
        }

        InputStreamReader isReader = new InputStreamReader(stream, "UTF-8");
        BufferedReader br = new BufferedReader(isReader);
        String result = br.readLine();

        if (result.contains("message") && result.contains("error")) {
            jsonanswer.put("type", "apicallerror");
            jsonanswer.put("errordetail", result);
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(jsonanswer.toJSONString()).build();
        } else {
            jsonanswer.put("type", "success");
            jsonanswer.put("result", result);
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(jsonanswer.toJSONString()).build();
        }

    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("index/encodefile.html")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadMultipart(@FormDataParam("file") FormDataBodyPart is)
            throws IOException {
        try {
            InputStream valueAs = is.getValueAs(InputStream.class);
            byte[] byteArray = IOUtils.toByteArray(valueAs);
            String encode = Base64.encode(byteArray);
            MediaType mediaType = is.getMediaType();
            String result = "data:" + mediaType.getType() + "/"
                    + mediaType.getSubtype() + ";base64, ";
            result += encode;

            JSONObject json = new JSONObject();
            if (StringUtils.isEmpty(encode)) {
                json.put("type", "error");
                json.put("result", "You did not choose a file or the file was empty!");
            } else if (checkPlainTypes(mediaType)) {
                json.put("type", "success");
                json.put("result", new String(byteArray));
            } else {
                json.put("type", "success");
                json.put("result", result);
            }

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .entity(json.toJSONString()).build();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
        // prepare the response
    }

    public boolean checkPlainTypes(MediaType mediaType) {

        List<Pair<String, String>> pairsToCheck = new ArrayList<Pair<String, String>>();
        pairsToCheck.add(new Pair<String, String>("text", "html"));
        pairsToCheck.add(new Pair<String, String>("text", "plain"));

        for (Pair<String, String> pair : pairsToCheck) {
            if (pair.getA().equalsIgnoreCase(mediaType.getType()) && pair.getB().equalsIgnoreCase(mediaType.getSubtype())) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("index/websitesave.html")
    @Consumes("application/x-www-form-urlencoded")
    public Response saveWebsite(@Context HttpServletRequest request,
                                MultivaluedMap<String, String> form) {

        String name = form.getFirst("name");
        String website = form.getFirst("website");
        String key = form.getFirst("key");

        JSONObject json = new JSONObject();

        if (key != null && !key.equalsIgnoreCase(Corekeys.WEBSITE.toString())) {
            if (Corekeys.isPartOf(key)) {
                json.put("type", "badKey");

                return Response
                        .status(200)
                        .header("Content-Type",
                                "application/json; charset=utf-8")
                        .entity(json.toJSONString()).build();
            }
        }

        if (StringUtils.isBlank(name)) {
            json.put("type", "parametersMissing");

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .entity(json.toJSONString()).build();
        }

        // TODO
        Pair<String, String> websitepair;
        if (StringUtils.isNotBlank(key)) {
            websitepair = new Pair<String, String>(key, website);
        } else {
            websitepair = new Pair<String, String>(Corekeys.WEBSITE.toString(),
                    website);
        }

        JSONObject storageJsonObject = null;
        if (website == null || website.isEmpty()) {

            storageJsonObject = StorageUtils
                    .getStorageJsonObject(
                            null,
                            Collections.singletonList(StringUtils.isBlank(key) ? Corekeys.WEBSITE
                                    .toString() : key), null, null, null, null);
        } else {

            try {
                String source = DCSet.getInstance().getNameStorageMap()
                        .getOpt(name, websitepair.getA());

                if (StringUtils.isNotBlank(source)) {
                    String diff = DiffHelper.getDiff(source, website);

                    if (website.length() > diff.length()
                            && diff.length() < 3500) {
                        websitepair.setB(diff);
                        storageJsonObject = StorageUtils.getStorageJsonObject(
                                null, null, null, null, null,
                                Collections.singletonList(websitepair));
                    }
                }

            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }

            if (storageJsonObject == null) {
                storageJsonObject = StorageUtils.getStorageJsonObject(
                        Collections.singletonList(websitepair), null, null,
                        null, null, null);
            }
        }

        //new NameStorageResource().updateEntry(storageJsonObject.toString(), name);

        json.put("type", "settingsSuccessfullySaved");
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .entity(json.toJSONString()).build();

    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("index/settingssave.html")
    @Consumes("application/x-www-form-urlencoded")
    public Response saveProfileSettings(@Context HttpServletRequest request,
                                        MultivaluedMap<String, String> form) {

        JSONObject json = new JSONObject();

        try {

            String profileName = form.getFirst("profilename");

            if (StringUtils.isBlank(profileName)) {
                json.put("type", "parametersMissing");

                return Response
                        .status(200)
                        .header("Content-Type",
                                "application/json; charset=utf-8")
                        .entity(json.toJSONString()).build();
            }

            Account name = null;
            //name = Controller.getInstance().getName(profileName);

            if (name == null || !Profile.isAllowedProfileName(profileName)) {

                json.put("type", "profileNameisnotAllowed");
                return Response
                        .status(200)
                        .header("Content-Type",
                                "application/json; charset=utf-8")
                        .entity(json.toJSONString()).build();
            }

            boolean blogenable = Boolean.valueOf(form
                    .getFirst(Corekeys.BLOGENABLE.toString()));
            boolean blockComments = Boolean.valueOf(form
                    .getFirst(Corekeys.BLOGBLOCKCOMMENTS.toString()));
            boolean profileenable = Boolean.valueOf(form
                    .getFirst(Corekeys.PROFILEENABLE.toString()));
            String titleOpt = form.getFirst(Corekeys.BLOGTITLE.toString());
            titleOpt = decodeIfNotNull(titleOpt);
            String blogDescrOpt = form.getFirst(Corekeys.BLOGDESCRIPTION
                    .toString());
            blogDescrOpt = decodeIfNotNull(blogDescrOpt);
            String profileAvatarOpt = form.getFirst(Corekeys.PROFILEAVATAR
                    .toString());
            String profileBannerOpt = form.getFirst(Corekeys.PROFILEMAINGRAPHIC
                    .toString());

            String bwlistkind = form.getFirst("bwlistkind");
            String blackwhitelist = form.getFirst("blackwhitelist");
            blackwhitelist = URLDecoder.decode(blackwhitelist, "UTF-8");

            profileAvatarOpt = decodeIfNotNull(profileAvatarOpt);
            profileBannerOpt = decodeIfNotNull(profileBannerOpt);

            Profile profile = Profile.getProfileOpt(name);
            profile.saveAvatarTitle(profileAvatarOpt);
            profile.saveProfileMainGraphicOpt(profileBannerOpt);
            profile.saveBlogDescription(blogDescrOpt);
            profile.saveBlogTitle(titleOpt);
            profile.setBlogEnabled(blogenable);
            profile.setBlockComments(blockComments);
            profile.setProfileEnabled(profileenable);

            profile.getBlogBlackWhiteList().clearList();
            profile.getBlogBlackWhiteList().setWhitelist(
                    !bwlistkind.equals("black"));
            String[] bwList = StringUtils.split(blackwhitelist, ";");
            for (String listentry : bwList) {
                profile.getBlogBlackWhiteList().addAddressOrName(listentry);
            }

            try {

                profile.saveProfile(null);

                json.put("type", "settingsSuccessfullySaved");
                return Response
                        .status(200)
                        .header("Content-Type",
                                "application/json; charset=utf-8")
                        .entity(json.toJSONString()).build();

            } catch (WebApplicationException e) {
                logger.error(e.getMessage(), e);

                json = new JSONObject();
                json.put("type", "error");
                json.put("error", e.getResponse().getEntity());

                return Response
                        .status(200)
                        .header("Content-Type",
                                "application/json; charset=utf-8")
                        .entity(json.toJSONString()).build();
            }

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);

            json.put("type", "error");
            json.put("error", e.getMessage());

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .entity(json.toJSONString()).build();
        }

    }

    @Path("index/settings.html")
    @GET
    public Response doProfileSettings() {

        try {

            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/settings.html", request);

            String ipAddress = ServletUtils.getRemoteAddress(request);
            if (ServletUtils.isRemoteRequest(request, ipAddress)) {
                return error404(request,
                        "This page is disabled for remote usage");
            }

            String profileName = request.getParameter("profilename");

            List<Account> namesAsList = null; //new CopyOnWriteArrayList<Name>(Controller.getInstance().getWalletNamesAsList());

            for (Account name : namesAsList) {
                if (!Profile.isAllowedProfileName(name.getAddress())) {
                    namesAsList.remove(name);
                }
            }

            pebbleHelper.getContextMap().put("names", namesAsList);

            handleSelectNameAndProfile(pebbleHelper, profileName, namesAsList);

            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }

    }

    public void handleSelectNameAndProfile(PebbleHelper pebbleHelper,
                                           String profileName, List<Account> namesAsList) {
        Account name = null;
        if (profileName != null) {
            //name = Controller.getInstance().getName(profileName);
        }

        if (!namesAsList.isEmpty()) {

            if (name == null) {
                Profile activeProfileOpt = ProfileHelper.getInstance()
                        .getActiveProfileOpt(request);
                if (activeProfileOpt != null) {
                    name = activeProfileOpt.getName();
                } else {
                    name = namesAsList.get(0);
                }
            }

            // WE HAVE HERE ONLY ALLOWED NAMES SO PROFILE CAN'T BE NULL HERE
            Profile profile = Profile.getProfileOpt(name);

            pebbleHelper.getContextMap().put("profile", profile);
            pebbleHelper.getContextMap().put("name", name);

        } else {
            pebbleHelper
                    .getContextMap()
                    .put("result",
                            "<div class=\"alert alert-danger translate\" role=\"alert\">"
                                    + "You need to register a name to create a profile.<br>"
                                    + "</div>");
        }
    }

    public String decodeIfNotNull(String parameter)
            throws UnsupportedEncodingException {
        return parameter != null ? URLDecoder.decode(parameter, "UTF-8") : null;
    }

    @Path("index/webdirectory.html")
    @GET
    public Response doWebdirectory() {

        try {
            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/main.mini.html", request);

            List<Pair<String, String>> websitesByValue = null; //NameUtils.getWebsitesByValue(null);
            List<HTMLSearchResult> results = generateHTMLSearchresults(websitesByValue);

            pebbleHelper.getContextMap().put("searchresults", results);

            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }

    }

    @Path("index/status.html")
    @GET
    public Response getStatus() {

        try {
            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/status.html", request);

            pebbleHelper.getContextMap().put(
                    "walletstatus",
                    Controller.getInstance().isWalletUnlocked() ? "<img src=\"/index/img/unlocked.png\" /> <span class=\"translate\">Wallet is unlocked</span>" : "<img src=\"/index/img/locked.png\" /> <span class=\"translate\">Wallet is locked</span>");
            pebbleHelper.getContextMap().put(
                    "forgestatus",
                    Controller.getInstance().getForgingStatus().getName());
            pebbleHelper.getContextMap().put(
                    "version",
                    Controller.getInstance().getApplicationName(true));

            int status = Controller.getInstance().getStatus();
            String statustext = "";
            //TODO this needs to be moved to another place

            Tuple2<Integer, Long> HWeight = Controller.getInstance().getBlockChain().getHWeightFull(DCSet.getInstance());
            if (Controller.getInstance().getWalletSyncHeight() > 0) {
                statustext = "<span class=\"translate\">Wallet Synchronizing</span> ";
                statustext += 100 * Controller.getInstance().getWalletSyncHeight() / HWeight.a + "%<br>";
                statustext += "<span class=\"translate\">Height</span>: " + Controller.getInstance().getWalletSyncHeight() + "/" + HWeight.a + "/" + Controller.getInstance().getMaxPeerHWeight(0, false, false).a;
            } else if (status == Controller.STATUS_OK) {
                statustext = "OK<br>";
                statustext += "<span class=\"translate\">Height</span>: " + HWeight.a;
                statustext += " <span class=\"translate\">Weight</span>: " + HWeight.b;
            } else if (status == Controller.STATUS_NO_CONNECTIONS) {
                statustext = "<span class=\"translate\">No connections</span><br>";
                statustext += "<span class=\"translate\">Height</span>: " + HWeight.a;
                statustext += " <span class=\"translate\">Weight</span>: " + HWeight.b;
            } else if (status == Controller.STATUS_SYNCHRONIZING) {
                statustext = "<span class=\"translate\">Synchronizing</span> ";
                statustext += 100 * HWeight.a / Controller.getInstance().getMaxPeerHWeight(0, false, false).a + "%<br>";
                statustext += "<span class=\"translate\">Height</span>: " + HWeight.a + "/" + Controller.getInstance().getMaxPeerHWeight(0, false, false).a;
            }

            pebbleHelper.getContextMap().put(
                    "status",
                    statustext);

            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();
        } catch (PebbleException e) {
            logger.error(e.getMessage(), e);
            return Response.status(404).build();
        }
    }

    private List<HTMLSearchResult> handleBlogSearch(String blogSearchOpt) {
        List<HTMLSearchResult> results = new ArrayList<>();
        List<BlogProfile> allEnabledBlogs = BlogUtils
                .getEnabledBlogs(blogSearchOpt);
        for (BlogProfile blogProfile : allEnabledBlogs) {
            String name = blogProfile.getProfile().getName().getAddress();
            String title = blogProfile.getProfile().getBlogTitleOpt();
            String description = blogProfile.getProfile()
                    .getBlogDescriptionOpt();

            results.add(new HTMLSearchResult(title, description, name,
                    "/index/blog.html?blogname=" + name,
                    "/index/blog.html?blogname=" + name,
                    "/namestorage:" + name, blogProfile.getFollower()));
        }

        return results;
    }

    @Path("index/main.html")
    @GET
    public Response handleIndex() {
        return handleDefault();
    }

    @Path("favicon.ico")
    @GET
    public Response favicon() {
        File file = new File("web/favicon.ico");

        if (file.exists()) {
            return Response.ok(file, "image/png").build();
            //return Response.ok(file, "image/vnd.microsoft.icon").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/favicon.ico")
    @GET
    public Response indexfavicon() {
        File file = new File("web/favicon.ico");

        if (file.exists()) {
            return Response.ok(file, "image/png").build();
            //return Response.ok(file, "image/vnd.microsoft.icon").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/img/{filename}")
    @GET
    public Response image(@PathParam("filename") String filename) {
        ArrayList<String> imgs = new ArrayList<String>();

        imgs.addAll(Arrays.asList(imgsArray));

        int imgnum = imgs.indexOf(filename);

        if (imgnum == -1) {
            return error404(request, null);
        }

        File file = new File("web/img/" + imgs.get(imgnum));
        String type = "";

        switch (getFileExtention(imgs.get(imgnum))) {
            case "png":
                type = "image/png";
                break;
            case "gif":
                type = "image/gif";
                break;
            case "jpg":
                type = "image/jpeg";
                break;
            case "svg":
                type = "image/svg+xml";
        }

        if (file.exists()) {
            return Response.ok(file, type).build();
        } else {
            return error404(request, null);
        }
    }

    @Deprecated
    @Path("index/personimage")
    @GET
    @Produces({"image/png", "image/jpg"})
    public Response getFullImage() {

        long key = new Long(request.getParameter("key"));
        if (key <= 0) {
            return error404(request, null);
        }

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);
        JSONObject jj = new JSONObject();
        byte[] b = person.getImage();
        if (b.length <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Invalid Image");


        }

        return Response.ok(new ByteArrayInputStream(b)).build();
        //	return Response.ok(file, "image/png").build();

    }

    @Deprecated
    @Path("index/assetimage")
    @GET
    @Produces({"image/png", "image/jpg"})
    public Response getAssetImage() {

        long key = new Long(request.getParameter("key"));
        if (key <= 0) {
            return error404(request, null);
        }

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        AssetCls person = (AssetCls) map.get(key);
        JSONObject jj = new JSONObject();
        byte[] b = person.getImage();
        if (b.length <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Invalid Image");


        }

        return Response.ok(new ByteArrayInputStream(b)).build();
        //	return Response.ok(file, "image/png").build();

    }

    @Path("index/libs/css/style.css")
    @GET
    public Response style() {
        File file = new File("web/libs/css/style.css");

        if (file.exists()) {
            return Response.ok(file, "text/css").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/css/sidebar.css")
    @GET
    public Response sidebarcss() {
        File file = new File("web/libs/css/sidebar.css");

        if (file.exists()) {
            return Response.ok(file, "text/css").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/css/timeline.css")
    @GET
    public Response timelinecss() {
        File file = new File("web/libs/css/timeline.css");

        if (file.exists()) {
            return Response.ok(file, "text/css").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/sidebar.js")
    @GET
    public Response sidebarjs() {
        File file = new File("web/libs/js/sidebar.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/utils.js")
    @GET
    public Response utilsjs() {
        File file = new File("web/libs/js/utils.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/marked.js")
    @GET
    public Response markedjs() {
        File file = new File("web/libs/js/marked.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/third-party/highlight.pack.js")
    @GET
    public Response highlightpackjs() {
        File file = new File("web/libs/js/third-party/highlight.pack.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/third-party/github.css")
    @GET
    public Response highgitcss() {
        File file = new File("web/libs/js/third-party/github.css");

        if (file.exists()) {
            return Response.ok(file, "text/css").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/clipboard.js")
    @GET
    public Response clipboard() {
        File file = new File("web/libs/js/clipboard.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/explorer.js")
    @GET
    public Response explorer() {
        File file = new File("web/libs/js/explorer.js");

        if (file.exists()) {
            return Response.ok(file, "text/explorer").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/explorerItems.js")
    @GET
    public Response explorerItems() {
        File file = new File("web/libs/js/explorerItems.js");

        if (file.exists()) {
            return Response.ok(file, "text/explorerItems").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/explorerAssets.js")
    @GET
    public Response explorerAssets() {
        File file = new File("web/libs/js/explorerAssets.js");

        if (file.exists()) {
            return Response.ok(file, "text/explorerAssets").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/explorerPersons.js")
    @GET
    public Response explorerPersons() {
        File file = new File("web/libs/js/explorerPersons.js");

        if (file.exists()) {
            return Response.ok(file, "text/explorerPersons").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/explorerStatements.js")
    @GET
    public Response explorerStatements() {
        File file = new File("web/libs/js/explorerStatements.js");

        if (file.exists()) {
            return Response.ok(file, "text/explorerStatements").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/explorerTemplates.js")
    @GET
    public Response explorerTemplates() {
        File file = new File("web/libs/js/explorerTemplates.js");

        if (file.exists()) {
            return Response.ok(file, "text/explorerTemplates").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/explorerStatuses.js")
    @GET
    public Response explorerStatuses() {
        File file = new File("web/libs/js/explorerStatuses.js");

        if (file.exists()) {
            return Response.ok(file, "text/explorerStatuses").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/explorerTransactions.js")
    @GET
    public Response explorerTransactions() {
        File file = new File("web/libs/js/explorerTransactions.js");

        if (file.exists()) {
            return Response.ok(file, "text/explorerTransactions").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/explorerExchange.js")
    @GET
    public Response explorerExchange() {
        File file = new File("web/libs/js/explorerExchange.js");

        if (file.exists()) {
            return Response.ok(file, "text/explorerExchange").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/explorerPolls.js")
    @GET
    public Response explorerPolls() {
        File file = new File("web/libs/js/explorerPolls.js");

        if (file.exists()) {
            return Response.ok(file, "text/explorerPolls").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/explorerTransactionsTable.js")
    @GET
    public Response explorerTransactionsTable() {
        File file = new File("web/libs/js/explorerTransactionsTable.js");

        if (file.exists()) {
            return Response.ok(file, "text/explorerTransactionsTable").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/third-party/ZeroClipboard.min.js")
    @GET
    public Response ZeroClipboardmin() {
        File file = new File("web/libs/js/third-party/ZeroClipboard.min.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/third-party/ZeroClipboard.swf")
    @GET
    public Response ZeroClipboard() {
        File file = new File("web/libs/js/third-party/ZeroClipboard.swf");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/biginteger.js")
    @GET
    public Response biginteger() {
        File file = new File("web/libs/js/biginteger.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/converters.js")
    @GET
    public Response converters() {
        File file = new File("web/libs/js/converters.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/crypto/curve25519.js")
    @GET
    public Response curve25519() {
        File file = new File("web/libs/js/crypto/curve25519.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/crypto/curve25519_.js")
    @GET
    public Response curve25519_() {
        File file = new File("web/libs/js/crypto/curve25519_.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/crypto/3rdparty/cryptojs/sha256.js")
    @GET
    public Response sha256() {
        File file = new File("web/libs/js/crypto/3rdparty/cryptojs/sha256.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("index/postblogprocessing.html")
    @Consumes("application/x-www-form-urlencoded")
    public Response postBlogProcessing(@Context HttpServletRequest request,
                                       MultivaluedMap<String, String> form) {
        JSONObject json = new JSONObject();

        String title = form.getFirst(BlogPostResource.TITLE_KEY);
        String creator = form.getFirst("creator");
        String contentparam = form.getFirst("content");
        String preview = form.getFirst("preview");


        String blogname = form.getFirst(BlogPostResource.BLOGNAME_KEY);
        String postid = form.getFirst(BlogPostResource.COMMENT_POSTID_KEY);

        if (StringUtil.isNotBlank(creator)
                && StringUtil.isNotBlank(contentparam)) {

            JSONObject jsonBlogPost = new JSONObject();

            /*
            Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(creator);

            String authorOpt = null;
            if (nameToAdress.getB() == NameResult.OK) {
                authorOpt = creator;
                jsonBlogPost.put(BlogPostResource.AUTHOR, authorOpt);
                jsonBlogPost.put("creator", nameToAdress.getA().getAddress());
            } else {
                jsonBlogPost.put("creator", creator);
            }

             */

            jsonBlogPost.put("title", title);
            jsonBlogPost.put("body", contentparam);


            if (StringUtils.isNotBlank(preview) && preview.equals("true")) {
                json.put("type", "preview");

                BlogEntry entry = new BlogEntry(title, contentparam, null, //authorOpt,
                        new Date().getTime(), creator, "", blogname);

                json.put("previewBlogpost", entry.toJson());

                return Response
                        .status(200)
                        .header("Content-Type",
                                "application/json; charset=utf-8")
                        .entity(json.toJSONString()).build();
            }

            try {

                jsonBlogPost.put(
                        "fee",
						/*
						Controller
								.getInstance()
								.calcRecommendedFeeForArbitraryTransaction(
										jsonBlogPost.toJSONString().getBytes(StandardCharsets.UTF_8), null)
								.getA().toPlainString()
						 */
                        0);

                String result;
                //COMMENT OR REAL BLOGPOST?
                if (postid != null) {
                    jsonBlogPost.put(BlogPostResource.COMMENT_POSTID_KEY, postid);
                    result = new BlogPostResource().commentBlogEntry(
                            jsonBlogPost.toJSONString());
                } else {
                    result = new BlogPostResource().addBlogEntry(
                            jsonBlogPost.toJSONString(), blogname);
                }

                json.put("type", "postSuccessful");
                json.put("result", result);

                return Response
                        .status(200)
                        .header("Content-Type",
                                "application/json; charset=utf-8")
                        .entity(json.toJSONString()).build();

            } catch (WebApplicationException e) {
                logger.error(e.getMessage(), e);

                json = new JSONObject();
                json.put("type", "error");
                json.put("error", e.getResponse().getEntity());

                return Response
                        .status(200)
                        .header("Content-Type",
                                "application/json; charset=utf-8")
                        .entity(json.toJSONString()).build();
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .entity(json.toJSONString()).build();
    }

    @Path("index/postcomment.html")
    @GET
    public Response postComment() {

        try {

            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/postblog.html", request);

            pebbleHelper.getContextMap().put("errormessage", "");
            pebbleHelper.getContextMap().put("font", "");
            pebbleHelper.getContextMap().put("content", "");
            pebbleHelper.getContextMap().put("option", "");
            pebbleHelper.getContextMap().put("oldtitle", "");
            pebbleHelper.getContextMap().put("oldcreator", "");
            pebbleHelper.getContextMap().put("oldcontent", "");
            pebbleHelper.getContextMap().put("oldfee", "");
            pebbleHelper.getContextMap().put("preview", "");

            List<Account> resultingAccounts;


            /**
             * Currently we allow all names and accounts, that needs to be restricted later
             */
            if (Controller.getInstance().doesWalletDatabaseExists()) {
                resultingAccounts = new ArrayList<Account>(Controller.getInstance()
                        .getWalletAccounts());
            } else {
                resultingAccounts = new ArrayList<Account>();
            }
            List<Account> resultingNames = null; //new ArrayList<Name>(Controller.getInstance().getWalletNamesAsList());

            for (Account name : resultingNames) {
                // No balance account not shown
                if (name.getConfBalance3(0, Transaction.FEE_KEY).a.compareTo(BigDecimal.ZERO) <= 0) {
                    resultingNames.remove(name);
                }
            }

            for (Account account : resultingAccounts) {
                if (account.getConfBalance3(0, Transaction.FEE_KEY).a.compareTo(BigDecimal.ZERO) <= 0) {
                    resultingAccounts.remove(account);
                }
            }

            //		 Pair<List<Account>, List<Name>> accountdAndNames = new Pair<List<Account>, List<Name>>(resultingAccounts,
            //				resultingNames);


            Collections.sort(resultingAccounts, new AccountBalanceComparator());

            String accountStrings = "";

            for (Account name : resultingNames) {
                accountStrings += "<option value=" + name.getFromFavorites() + ">"
                        + name.getBalanceUSE(1L) + "</option>";
            }

            for (Account account : resultingAccounts) {
                accountStrings += "<option value=" + account.getAddress() + ">"
                        + account + "</option>";
            }

            // are we allowed to post
            if (resultingNames.isEmpty() && resultingAccounts.isEmpty()) {

                pebbleHelper
                        .getContextMap()
                        .put("errormessage",
                                "<div id=\"result\"><div class=\"alert alert-dismissible alert-danger\" role=\"alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">x</button>You can't post to this blog! None of your accounts has balance or the blog owner did not allow your accounts to post!<br></div></div>");

            }

            Profile activeProfileOpt = ProfileHelper.getInstance()
                    .getActiveProfileOpt(request);

            if (activeProfileOpt != null
                    && resultingNames.contains(activeProfileOpt.getName())) {
                pebbleHelper.getContextMap().put("primaryname",
                        activeProfileOpt.getName().getFromFavorites());
            }

            pebbleHelper.getContextMap().put("option", accountStrings);

            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }
    }

    @Path("index/postblog.html")
    @GET
    public Response postBlog() {

        try {

            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/postblog.html", request);

            pebbleHelper.getContextMap().put("errormessage", "");
            pebbleHelper.getContextMap().put("font", "");
            pebbleHelper.getContextMap().put("content", "");
            pebbleHelper.getContextMap().put("option", "");
            pebbleHelper.getContextMap().put("oldtitle", "");
            pebbleHelper.getContextMap().put("oldcreator", "");
            pebbleHelper.getContextMap().put("oldcontent", "");
            pebbleHelper.getContextMap().put("oldfee", "");
            pebbleHelper.getContextMap().put("preview", "");

            String blogname = request
                    .getParameter(BlogPostResource.BLOGNAME_KEY);

            BlogBlackWhiteList blogBlackWhiteList = BlogBlackWhiteList
                    .getBlogBlackWhiteList(blogname);

            Pair<List<Account>, List<Account>> ownAllowedElements = blogBlackWhiteList
                    .getOwnAllowedElements(true);

            List<Account> resultingAccounts = new ArrayList<Account>(
                    ownAllowedElements.getA());
            List<Account> resultingNames = ownAllowedElements.getB();

            Collections.sort(resultingAccounts, new AccountBalanceComparator());

            String accountStrings = "";

            for (Account name : resultingNames) {
                accountStrings += "<option value=" + name.getFromFavorites() + ">"
                        + name.getBalanceUSE(1L) + "</option>";
            }

            for (Account account : resultingAccounts) {
                accountStrings += "<option value=" + account.getAddress() + ">"
                        + account + "</option>";
            }

            // are we allowed to post
            if (resultingNames.isEmpty() && resultingAccounts.isEmpty()) {

                pebbleHelper
                        .getContextMap()
                        .put("errormessage",
                                "<div id=\"result\"><div class=\"alert alert-dismissible alert-danger\" role=\"alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">x</button>You can't post to this blog! None of your accounts has balance or the blog owner did not allow your accounts to post!<br></div></div>");

            }

            Profile activeProfileOpt = ProfileHelper.getInstance()
                    .getActiveProfileOpt(request);

            if (activeProfileOpt != null
                    && resultingNames.contains(activeProfileOpt.getName())) {
                pebbleHelper.getContextMap().put("primaryname",
                        activeProfileOpt.getName().getFromFavorites());
            }

            pebbleHelper.getContextMap().put("option", accountStrings);

            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("index/followblog.html")
    @Consumes("application/x-www-form-urlencoded")
    public Response followBlog(@Context HttpServletRequest request,
                               MultivaluedMap<String, String> form) {
        try {

            JSONObject json = new JSONObject();

            String blogname = form.getFirst(BlogPostResource.BLOGNAME_KEY);
            String followString = form.getFirst("follow");

            Profile activeProfileOpt = ProfileHelper.getInstance()
                    .getActiveProfileOpt(request);

            if (followString != null && activeProfileOpt != null
                    && blogname != null //&& nameMap.contains(blogname)
            ) {
                boolean follow = Boolean.valueOf(followString);
                Account name = null; //nameMap.get(blogname);
                Profile profile = Profile.getProfileOpt(name);
                if (activeProfileOpt.isProfileEnabled()) {

                    if (follow) {
                        if (profile != null && profile.isProfileEnabled()
                                && profile.isBlogEnabled()) {
                            String result;

                            if (activeProfileOpt.getFollowedBlogs().contains(
                                    blogname)) {
                                result = "<center><div class=\"alert alert-danger\" role=\"alert\">Blog follow not successful<br>"
                                        + "You already follow this blog"
                                        + "</div></center>";

                                json.put("type", "youAlreadyFollowThisBlog");
                                json.put("follower", profile.getFollower()
                                        .size());

                                json.put("isFollowing", activeProfileOpt
                                        .getFollowedBlogs().contains(blogname));

                                return Response
                                        .status(200)
                                        .header("Content-Type",
                                                "application/json; charset=utf-8")
                                        .entity(json.toJSONString()).build();
                            }

                            // Prevent following of own profiles
                            if (false
                                //Controller.getInstance().getWalletNamesAsListAsString().contains(blogname)
                            ) {
                                result = "<center><div class=\"alert alert-danger\" role=\"alert\">Blog follow not successful<br>"
                                        + "You can't follow your own profiles"
                                        + "</div></center>";

                                json.put("type", "youCantFollowYourOwnProfiles");
                                json.put("follower", profile.getFollower()
                                        .size());

                                json.put("isFollowing", activeProfileOpt
                                        .getFollowedBlogs().contains(blogname));

                                return Response
                                        .status(200)
                                        .header("Content-Type",
                                                "application/json; charset=utf-8")
                                        .entity(json.toJSONString()).build();
                            }

                            boolean isFollowing = activeProfileOpt
                                    .getFollowedBlogs().contains(blogname);

                            try {

                                activeProfileOpt.addFollowedBlog(blogname);
                                result = activeProfileOpt.saveProfile(null);
                                result = "<div class=\"alert alert-success\" role=\"alert\">You follow this blog now<br>"
                                        + result + "</div>";

                                json.put("type", "YouFollowThisBlogNow");
                                json.put("result", result);
                                json.put("follower", profile.getFollower()
                                        .size());
                                json.put("isFollowing", activeProfileOpt
                                        .getFollowedBlogs().contains(blogname));

                            } catch (WebApplicationException e) {
                                logger.error(e.getMessage(), e);

                                result = "<center><div class=\"alert alert-danger\" role=\"alert\">Blog follow not successful<br>"
                                        + e.getResponse().getEntity()
                                        + "</div></center>";

                                json.put("type", "BlogFollowNotSuccessful");
                                json.put("result", e.getResponse().getEntity());
                                json.put("follower", profile.getFollower()
                                        .size());
                                json.put("isFollowing", isFollowing);

                            }

                            return Response
                                    .status(200)
                                    .header("Content-Type",
                                            "application/json; charset=utf-8")
                                    .entity(json.toJSONString()).build();
                        }

                    } else {

                        boolean isFollowing = activeProfileOpt
                                .getFollowedBlogs().contains(blogname);

                        if (activeProfileOpt.getFollowedBlogs().contains(
                                blogname)) {
                            activeProfileOpt.removeFollowedBlog(blogname);
                            String result;
                            try {
                                result = activeProfileOpt.saveProfile(null);
                                result = "<div class=\"alert alert-success\" role=\"alert\">Unfollow successful<br>"
                                        + result + "</div>";

                                json.put("type", "unfollowSuccessful");
                                json.put("result", result);
                                json.put("follower", profile.getFollower()
                                        .size());
                                json.put("isFollowing", activeProfileOpt
                                        .getFollowedBlogs().contains(blogname));
                            } catch (WebApplicationException e) {
                                logger.error(e.getMessage(), e);

                                result = "<center><div class=\"alert alert-danger\" role=\"alert\">Blog unfollow not successful<br>"
                                        + e.getResponse().getEntity()
                                        + "</div></center>";

                                json.put("type", "blogUnfollowNotSuccessful");
                                json.put("result", e.getResponse().getEntity());
                                json.put("follower", profile.getFollower()
                                        .size());
                                json.put("isFollowing", isFollowing);
                            }

                            return Response
                                    .status(200)
                                    .header("Content-Type",
                                            "application/json; charset=utf-8")
                                    .entity(json.toJSONString()).build();

                        }
                    }

                }
            }

            return getBlog(null);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .entity("{}").build();
        }

    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("index/deletecomment.html")
    @Consumes("application/x-www-form-urlencoded")
    public Response deleteComment(@Context HttpServletRequest request,
                                  MultivaluedMap<String, String> form) {

        JSONObject jsonanswer = new JSONObject();
        try {

            String signature = form.getFirst("signature");

            if (signature != null) {

                BlogEntry blogEntryOpt = BlogUtils.getCommentBlogEntryOpt(signature);

                if (blogEntryOpt == null) {
                    // TODO put this snippet in method
                    jsonanswer.put("type", "deleteError");
                    jsonanswer
                            .put("errordetail",
                                    "The comment you are trying to delete does not exist!");

                    return Response
                            .status(200)
                            .header("Content-Type",
                                    "application/json; charset=utf-8")
                            .entity(jsonanswer.toJSONString()).build();
                }

                if (!Controller.getInstance().doesWalletDatabaseExists()) {
                    jsonanswer.put("type", "deleteError");
                    jsonanswer.put("errordetail", "You don't have a wallet!");

                    return Response
                            .status(200)
                            .header("Content-Type",
                                    "application/json; charset=utf-8")
                            .entity(jsonanswer.toJSONString()).build();
                }

                String creator = BlogUtils.getCreatorOrBlogOwnerOpt(blogEntryOpt);

                if (creator == null) {
                    jsonanswer.put("type", "deleteError");
                    jsonanswer
                            .put("errordetail",
                                    "You are not allowed to delete this comment! You need to be the owner of the blog or author of the comment!");

                    return Response
                            .status(200)
                            .header("Content-Type",
                                    "application/json; charset=utf-8")
                            .entity(jsonanswer.toJSONString()).build();
                }

                try {

                    String result = new BlogPostResource().deleteCommentEntry(
                            signature);

                    jsonanswer.put("type", "deleteSuccessful");
                    jsonanswer.put("result", result);

                    return Response
                            .status(200)
                            .header("Content-Type",
                                    "application/json; charset=utf-8")
                            .entity(jsonanswer.toJSONString()).build();
                } catch (WebApplicationException e) {
                    logger.error(e.getMessage(), e);

                    jsonanswer.put("type", "deleteError");
                    jsonanswer.put("errordetail", e.getResponse().getEntity());

                    return Response
                            .status(200)
                            .header("Content-Type",
                                    "application/json; charset=utf-8")
                            .entity(jsonanswer.toJSONString()).build();

                }

            }

            jsonanswer.put("type", "deleteError");
            jsonanswer.put("errordetail",
                    "the signature parameter must be set!");

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .entity(jsonanswer.toJSONString()).build();

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);

            jsonanswer.put("type", "deleteError");
            jsonanswer.put("errordetail", e.getMessage());

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .entity(jsonanswer.toJSONString()).build();
        }

    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("index/deletepost.html")
    @Consumes("application/x-www-form-urlencoded")
    public Response deletePost(@Context HttpServletRequest request,
                               MultivaluedMap<String, String> form) {

        JSONObject jsonanswer = new JSONObject();
        try {

            String signature = form.getFirst("signature");

            if (signature != null) {

                BlogEntry blogEntryOpt = BlogUtils.getBlogEntryOpt(signature);

                if (blogEntryOpt == null) {
                    // TODO put this snippet in method
                    jsonanswer.put("type", "deleteError");
                    jsonanswer
                            .put("errordetail",
                                    "The blog entry you are trying to delete does not exist!");

                    return Response
                            .status(200)
                            .header("Content-Type",
                                    "application/json; charset=utf-8")
                            .entity(jsonanswer.toJSONString()).build();
                }

                if (!Controller.getInstance().doesWalletDatabaseExists()) {
                    jsonanswer.put("type", "deleteError");
                    jsonanswer.put("errordetail", "You don't have a wallet!");

                    return Response
                            .status(200)
                            .header("Content-Type",
                                    "application/json; charset=utf-8")
                            .entity(jsonanswer.toJSONString()).build();
                }

                String creator = blogEntryOpt.getCreator();

                Account accountByAddress = Controller.getInstance()
                        .getWalletAccountByAddress(creator);
                String blognameOpt = blogEntryOpt.getBlognameOpt();
                // Did I create that blogpost?
                JSONObject jsonBlogPost = new JSONObject();
                jsonBlogPost.put(BlogPostResource.DELETE_KEY, signature);
                jsonBlogPost.put("body", "delete");
                if (accountByAddress != null) {
					/*
					// TODO create blogpost json in method --> move to BlogUtils
					// (for every kind delete/share and so on)
					jsonBlogPost.put("creator", creator);
					Pair<BigDecimal, Integer> fee = Controller.getInstance()
							.calcRecommendedFeeForArbitraryTransaction(
									jsonBlogPost.toJSONString().getBytes(StandardCharsets.UTF_8), null
									);
					jsonBlogPost.put("fee", fee.getA().toPlainString());
					 */
                    jsonBlogPost.put("fee", 0);
                    // I am not author, but am I the owner of the blog?
                } else if (blognameOpt != null
                    //&& Controller.getInstance().getWalletNamesAsListAsString().contains(blognameOpt)
                ) {
                    Account name = null; //DCSet.getInstance().getNameMap().get(blognameOpt);
                    jsonBlogPost.put("creator", name.getAddress());
                    jsonBlogPost.put(BlogPostResource.AUTHOR, blognameOpt);
                } else {
                    jsonanswer.put("type", "deleteError");
                    jsonanswer
                            .put("errordetail",
                                    "You are not allowed to delete this post! You need to be owner of the blog or author of the blogpost!");

                    return Response
                            .status(200)
                            .header("Content-Type",
                                    "application/json; charset=utf-8")
                            .entity(jsonanswer.toJSONString()).build();
                }

                try {

                    String result = new BlogPostResource().addBlogEntry(
                            jsonBlogPost.toJSONString(), null);

                    jsonanswer.put("type", "deleteSuccessful");
                    jsonanswer.put("result", result);

                    return Response
                            .status(200)
                            .header("Content-Type",
                                    "application/json; charset=utf-8")
                            .entity(jsonanswer.toJSONString()).build();
                } catch (WebApplicationException e) {
                    logger.error(e.getMessage(), e);

                    jsonanswer.put("type", "deleteError");
                    jsonanswer.put("errordetail", e.getResponse().getEntity());

                    return Response
                            .status(200)
                            .header("Content-Type",
                                    "application/json; charset=utf-8")
                            .entity(jsonanswer.toJSONString()).build();

                }

            }

            jsonanswer.put("type", "deleteError");
            jsonanswer.put("errordetail",
                    "the signature parameter must be set!");

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .entity(jsonanswer.toJSONString()).build();

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);

            jsonanswer.put("type", "deleteError");
            jsonanswer.put("errordetail", e.getMessage());

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .entity(jsonanswer.toJSONString()).build();
        }

    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("index/sharepost.html")
    @Consumes("application/x-www-form-urlencoded")
    public Response sharePost(@Context HttpServletRequest request,
                              MultivaluedMap<String, String> form) {

        JSONObject json = new JSONObject();

        // TODO CHANGE ERROR RETURNING --> less html code! see delete post and
        // also processlike!
        try {

            String signature = form.getFirst("signature");
            String sourceBlog = form.getFirst("blogname");

            Profile activeProfileOpt = ProfileHelper.getInstance()
                    .getActiveProfileOpt(request);

            if (activeProfileOpt != null && signature != null
                    && sourceBlog != null) {
                if (activeProfileOpt.isProfileEnabled()) {

                    if (!activeProfileOpt.isBlogEnabled()) {
                        json.put("type", "BlogIsDisabled");
                        return Response
                                .status(200)
                                .header("Content-Type",
                                        "application/json; charset=utf-8")
                                .entity(json.toJSONString()).build();
                    }

                    List<String> list = DCSet.getInstance().getSharedPostsMap()
                            .get(Base58.decode(signature));
                    if (list != null
                            && list.contains(activeProfileOpt.getName()
                            .getFromFavorites())) {
                        json.put("type", "YouAlreadySharedThisPost");

                        return Response
                                .status(200)
                                .header("Content-Type",
                                        "application/json; charset=utf-8")
                                .entity(json.toJSONString()).build();
                    }

                    if (activeProfileOpt.getName().getFromFavorites().equals(sourceBlog)) {
                        json.put("type", "YouCantShareYourOwnPosts");

                        return Response
                                .status(200)
                                .header("Content-Type",
                                        "application/json; charset=utf-8")
                                .entity(json.toJSONString()).build();
                    }

                    JSONObject jsonBlogPost = new JSONObject();
                    String profileName = activeProfileOpt.getName().getAddress();
                    jsonBlogPost.put(BlogPostResource.AUTHOR, profileName);
                    jsonBlogPost.put("creator", activeProfileOpt.getName()
                            .getAddress());
                    jsonBlogPost.put(BlogPostResource.SHARE_KEY, signature);
                    jsonBlogPost.put("body", "share");

					/*
					Pair<BigDecimal, Integer> fee = Controller.getInstance()
							.calcRecommendedFeeForArbitraryTransaction(
									jsonBlogPost.toJSONString().getBytes(StandardCharsets.UTF_8), null);
					jsonBlogPost.put("fee", fee.getA().toPlainString());
					 */
                    jsonBlogPost.put("fee", "0.0");

                    try {

                        String result = new BlogPostResource().addBlogEntry(
                                jsonBlogPost.toJSONString(), profileName);

                        json.put("type", "ShareSuccessful");
                        json.put("result", result);

                    } catch (WebApplicationException e) {
                        logger.error(e.getMessage(), e);

                        json.put("type", "ShareNotSuccessful");
                        json.put("result", e.getResponse().getEntity());
                    }

                    return Response
                            .status(200)
                            .header("Content-Type",
                                    "application/json; charset=utf-8")
                            .entity(json.toJSONString()).build();

                }
            }

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);

            json.put("type", "error");
            json.put("error", e.getMessage());

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .entity(json.toJSONString()).build();
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .entity("{}").build();
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("index/likepost.html")
    @Consumes("application/x-www-form-urlencoded")
    public Response likePost(@Context HttpServletRequest request,
                             MultivaluedMap<String, String> form) {

        JSONObject json = new JSONObject();

        try {

            String signature = form.getFirst("signature");
            String likeString = form.getFirst("like");

            Profile activeProfileOpt = ProfileHelper.getInstance()
                    .getActiveProfileOpt(request);

            if (likeString != null && activeProfileOpt != null) {
                boolean like = Boolean.valueOf(likeString);
                if (activeProfileOpt.isProfileEnabled()) {

                    if (like) {
                        String result;

                        if (activeProfileOpt.getLikedPosts()
                                .contains(signature)) {

                            json.put("type", "YouAlreadyLikeThisPost");

                            return Response
                                    .status(200)
                                    .header("Content-Type",
                                            "application/json; charset=utf-8")
                                    .entity(json.toJSONString()).build();
                        }

                        BlogEntry blogEntryOpt = BlogUtils
                                .getBlogEntryOpt((ArbitraryTransaction) Controller
                                        .getInstance().getTransaction(
                                                Base58.decode(signature)));

                        boolean ownPost = false;
                        if (blogEntryOpt != null) {
                            if (Controller.getInstance().getWalletAccountByAddress(
                                    blogEntryOpt.getCreator()) != null) {
                                ownPost = true;
                            }
                        }

                        if (ownPost) {

                            json.put("type", "YouCantLikeYourOwnPosts");

                            return Response
                                    .status(200)
                                    .header("Content-Type",
                                            "application/json; charset=utf-8")
                                    .entity(json.toJSONString()).build();

                        }

                        activeProfileOpt.addLikePost(signature);
                        try {

                            String creator = blogEntryOpt.getCreator();
                            List<Payment> payments = new ArrayList<>();
                            if (creator != null) {
                                BigDecimal amount = BigDecimal.TEN;
                                amount = amount;
                                payments.add(new Payment(new Account(creator), AssetCls.FEE_KEY, amount));
                            }
                            result = activeProfileOpt.saveProfile(payments);

                            json.put("type", "LikeSuccessful");
                            json.put("result", result);

                        } catch (WebApplicationException e) {
                            logger.error(e.getMessage(), e);

                            json.put("type", "LikeNotSuccessful");
                            json.put("result", e.getResponse().getEntity());
                        }

                        return Response
                                .status(200)
                                .header("Content-Type",
                                        "application/json; charset=utf-8")
                                .entity(json.toJSONString()).build();
                    } else {
                        if (activeProfileOpt.getLikedPosts()
                                .contains(signature)) {

                            activeProfileOpt.removeLikeProfile(signature);
                            String result;
                            try {
                                result = activeProfileOpt.saveProfile(null);

                                json.put("type", "LikeRemovedSuccessful");
                                json.put("result", result);

                            } catch (WebApplicationException e) {
                                logger.error(e.getMessage(), e);

                                json.put("type", "LikeRemovedNotSuccessful");
                                json.put("result", e.getResponse().getEntity());

                            }

                            return Response
                                    .status(200)
                                    .header("Content-Type",
                                            "application/json; charset=utf-8")
                                    .entity(json.toJSONString()).build();
                        }
                    }

                }
            }

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);

            json.put("type", "error");
            json.put("error", e.getMessage());

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .entity(json.toJSONString()).build();
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .entity("{}").build();
    }

    @Path("index/showpost.html")
    @GET
    public Response showPost() {
        try {
            String msg = request.getParameter("msg");

            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/blog.html", request, NavbarElements.NoNavbar);
            pebbleHelper.getContextMap().put("hideprofile", true);
            pebbleHelper.getContextMap().put("blogenabled", true);

            if (StringUtils.isEmpty(msg)) {
                return Response.ok(pebbleHelper.evaluate(),
                        "text/html; charset=utf-8").build();
            }

            if (msg != null) {
                pebbleHelper.getContextMap().put("msg", msg);
            }

            BlogEntry blogEntryOpt = BlogUtils.getBlogEntryOpt(Base58
                    .decode(msg));

            if (blogEntryOpt == null) {
                // TODO SHOW NOT FOUND MESSAGE
                return Response.ok(pebbleHelper.evaluate(),
                        "text/html; charset=utf-8").build();
            }
            Profile activeProfileOpt = ProfileHelper.getInstance()
                    .getActiveProfileOpt(request);

            String signature = blogEntryOpt.getSignature();

            addSharingAndLiking(blogEntryOpt, signature);
            if (activeProfileOpt != null) {
                blogEntryOpt.setLiking(activeProfileOpt.getLikedPosts()
                        .contains(signature));
            }

            pebbleHelper.getContextMap().put("blogposts",
                    Arrays.asList(blogEntryOpt));

            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }
    }

    @Path("index/mergedblog.html")
    @GET
    public Response mergedBlog() {

        try {
            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/blog.html", request, NavbarElements.BlogNavbar);

            String blogname = request
                    .getParameter(BlogPostResource.BLOGNAME_KEY);

            String msg = request.getParameter("msg");

            if (msg != null) {
                pebbleHelper.getContextMap().put("msg", msg);
            }

            Profile profile = null;
            if (blogname == null) {
                Profile activeProfileOpt = ProfileHelper.getInstance()
                        .getActiveProfileOpt(request);

                profile = activeProfileOpt;
            } else {
                profile = Profile.getProfileOpt(blogname);
            }

            if (profile == null || !profile.isProfileEnabled()) {

                pebbleHelper = PebbleHelper.getPebbleHelper(
                        "web/profiledisabled.html", request);
                return Response.ok(pebbleHelper.evaluate(),
                        "text/html; charset=utf-8").build();
            }

            pebbleHelper.getContextMap().put("postblogurl",
                    "postblog.html?blogname=" + blogname);

            pebbleHelper.getContextMap().put("blogprofile", profile);
            pebbleHelper.getContextMap().put("blogenabled", true);
            pebbleHelper.getContextMap().put("hideprofile", true);

            List<String> followedBlogs = new ArrayList<String>(
                    profile.getFollowedBlogs());
            followedBlogs.add(profile.getName().getAddress());

            List<BlogEntry> blogPosts = BlogUtils.getBlogPosts(followedBlogs);

            Profile activeProfileOpt = ProfileHelper.getInstance()
                    .getActiveProfileOpt(request);
            for (BlogEntry blogEntry : blogPosts) {
                String signature = blogEntry.getSignature();

                addSharingAndLiking(blogEntry, signature);
                if (activeProfileOpt != null) {
                    blogEntry.setLiking(activeProfileOpt.getLikedPosts()
                            .contains(signature));
                }
            }

            pebbleHelper.getContextMap().put("blogposts", blogPosts);

            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }

    }

    @Path("index/hashtag.html")
    @GET
    public Response getHashTagPosts() {
        try {
            String hashtag = request.getParameter("hashtag");
            String msg = request.getParameter("msg");

            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/blog.html", request, NavbarElements.Searchnavbar);
            pebbleHelper.getContextMap().put("hideprofile", true);
            pebbleHelper.getContextMap().put("blogenabled", true);
            hashtag = hashtag == null ? "" : hashtag;

            if (StringUtils.isEmpty(hashtag)) {
                return Response.ok(pebbleHelper.evaluate(),
                        "text/html; charset=utf-8").build();
            }
            hashtag = hashtag.toLowerCase();

            hashtag = "#" + hashtag;

            if (msg != null) {
                pebbleHelper.getContextMap().put("msg", msg);
            }

            List<BlogEntry> blogPosts = BlogUtils.getHashTagPosts(hashtag);

            Profile activeProfileOpt = ProfileHelper.getInstance()
                    .getActiveProfileOpt(request);

            for (BlogEntry blogEntry : blogPosts) {
                String signature = blogEntry.getSignature();

                addSharingAndLiking(blogEntry, signature);
                if (activeProfileOpt != null) {
                    blogEntry.setLiking(activeProfileOpt.getLikedPosts()
                            .contains(signature));
                }
            }

            pebbleHelper.getContextMap().put("blogposts", blogPosts);

            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }
    }

    @Path("index/blog.html")
    @GET
    public Response getBlog(@PathParam("messageOpt") String messageOpt) {

        try {
            String blogname = request
                    .getParameter(BlogPostResource.BLOGNAME_KEY);
            String switchprofile = request.getParameter("switchprofile");
            String disconnect = request.getParameter("disconnect");
            String msg = request.getParameter("msg");

            if (StringUtils.isNotBlank(disconnect)) {
                ProfileHelper.getInstance().disconnect();
            } else {
                ProfileHelper.getInstance().switchProfileOpt(switchprofile);
            }

            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/blog.html", request, NavbarElements.BlogNavbar);
            //pebbleHelper.getContextMap().put("namestoragemap",
            //        NameStorageWebResource.getInstance());
            pebbleHelper.getContextMap().put("postblogurl", "postblog.html");
            pebbleHelper.getContextMap().put("apimessage", messageOpt);

            if (msg != null) {
                pebbleHelper.getContextMap().put("msg", msg);
            }

            //NameMap nameMap = DCSet.getInstance().getNameMap();
            if (blogname != null) {
                if (false  //!nameMap.contains(blogname)
                ) {
                    return Response.ok(
                            PebbleHelper.getPebbleHelper(
                                    "web/profiledisabled.html", request)
                                    .evaluate(), "text/html; charset=utf-8")
                            .build();
                }

                Account name = null; //nameMap.get(blogname);
                Profile profile = Profile.getProfileOpt(name);

                if (profile == null || !profile.isProfileEnabled()) {
                    pebbleHelper = PebbleHelper.getPebbleHelper(
                            "web/profiledisabled.html", request);
                    if (Controller.getInstance().getWalletAccountByAddress(
                            name.getAddress()) != null) {
                        pebbleHelper.getContextMap().put("ownProfileName",
                                blogname);
                    }
                    return Response.ok(pebbleHelper.evaluate(),
                            "text/html; charset=utf-8").build();
                }

                pebbleHelper.getContextMap().put("postblogurl",
                        "postblog.html?blogname=" + blogname);

                pebbleHelper.getContextMap().put("blogprofile", profile);
                pebbleHelper.getContextMap().put("blogenabled",
                        profile.isBlogEnabled());
                if (Controller.getInstance().doesWalletDatabaseExists()) {
                    if (Controller.getInstance().getWalletAccountByAddress(
                            name.getAddress()) != null) {
                        pebbleHelper.getContextMap().put("ownProfileName",
                                blogname);
                    }
                }
                pebbleHelper.getContextMap().put("follower",
                        profile.getFollower());

            } else {
                pebbleHelper.getContextMap().put("hideprofile", true);
                pebbleHelper.getContextMap().put("blogenabled", true);
            }

            Profile activeProfileOpt = ProfileHelper.getInstance()
                    .getActiveProfileOpt(request);
            pebbleHelper.getContextMap().put(
                    "isFollowing",
                    activeProfileOpt != null
                            && activeProfileOpt.getFollowedBlogs().contains(
                            blogname));

            pebbleHelper.getContextMap().put(
                    "isLikeing",
                    activeProfileOpt != null
                            && activeProfileOpt.getLikedPosts().contains(
                            blogname));

            List<BlogEntry> blogPosts = BlogUtils.getBlogPosts(blogname);

            for (BlogEntry blogEntry : blogPosts) {
                String signature = blogEntry.getSignature();

                addSharingAndLiking(blogEntry, signature);
                if (activeProfileOpt != null) {
                    blogEntry.setLiking(activeProfileOpt.getLikedPosts()
                            .contains(signature));
                }
            }

            pebbleHelper.getContextMap().put("blogposts", blogPosts);

            return Response.ok(pebbleHelper.evaluate(),
                    "text/html; charset=utf-8").build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }
    }

    @Path("index/libs/js/Base58.js")
    @GET
    public Response Base58js() {
        File file = new File("web/libs/js/Base58.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/common.js")
    @GET
    public Response commonjs() {
        File file = new File("web/libs/js/common.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("/index/libs/third-party/jquery.form.min.js")
    @GET
    public Response getFormMin() {
        File file = new File("web/libs/js/third-party/jquery.form.min.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/jquery/jquery.{version}.js")
    @GET
    public Response jquery(@PathParam("version") String version) {
        File file;
        if (version.equals("1")) {
            file = new File("web/libs/jquery/jquery-1.11.3.min.js");
        } else if (version.equals("2")) {
            file = new File("web/libs/jquery/jquery-2.1.4.min.js");
        } else {
            file = new File("web/libs/jquery/jquery-2.1.4.min.js");
        }

        if (file.exists()) {
            return Response.ok(file, "text/javascript; charset=utf-8").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/angular/angular.{version}.js")
    @GET
    public Response angular(@PathParam("version") String version) {
        File file;
        if (version.equals("1.3")) {
            file = new File("web/libs/angular/angular.min.1.3.15.js");
        } else if (version.equals("1.4")) {
            file = new File("web/libs/angular/angular.min.1.4.0.js");
        } else {
            file = new File("web/libs/angular/angular.min.1.3.15.js");
        }

        if (file.exists()) {
            return Response.ok(file, "text/javascript; charset=utf-8").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/bootstrap/{version}/{folder}/{filename}")
    @GET
    public Response bootstrap(@PathParam("version") String version,
                              @PathParam("folder") String folder,
                              @PathParam("filename") String filename) {
        String fullname = "web/libs/bootstrap-3.3.4-dist/";
        String type = "text/html; charset=utf-8";

        switch (folder) {
            case "css": {
                fullname += "css/";
                type = "text/css";
                switch (filename) {
                    case "bootstrap.css":

                        fullname += "bootstrap.css";
                        break;

                    case "theme.css":

                        fullname += "theme.css";
                        break;

                    case "bootstrap.css.map":

                        fullname += "bootstrap.css.map";
                        break;

                    case "bootstrap.min.css":

                        fullname += "bootstrap.min.css";
                        break;

                    case "bootstrap-theme.css":

                        fullname += "bootstrap-theme.css";
                        break;

                    case "bootstrap-theme.css.map":

                        fullname += "bootstrap-theme.css.mapp";
                        break;

                    case "bootstrap-theme.min.css":

                        fullname += "bootstrap-theme.min.css";
                        break;
                }
                break;
            }
            case "fonts": {
                fullname += "fonts/";
                switch (filename) {
                    case "glyphicons-halflings-regular.eot":

                        fullname += "glyphicons-halflings-regular.eot";
                        type = "application/vnd.ms-fontobject";
                        break;

                    case "glyphicons-halflings-regular.svg":

                        fullname += "glyphicons-halflings-regular.svg";
                        type = "image/svg+xml";
                        break;

                    case "glyphicons-halflings-regular.ttf":

                        fullname += "glyphicons-halflings-regular.ttf";
                        type = "application/x-font-ttf";
                        break;

                    case "glyphicons-halflings-regular.woff":

                        fullname += "glyphicons-halflings-regular.woff";
                        type = "application/font-woff";
                        break;

                    case "glyphicons-halflings-regular.woff2":

                        fullname += "glyphicons-halflings-regular.woff2";
                        type = "application/font-woff";
                        break;
                }
                break;
            }
            case "js": {
                fullname += "js/";
                type = "text/javascript";
                switch (filename) {
                    case "bootstrap.js":

                        fullname += "bootstrap.js";
                        break;

                    case "bootstrap.min.js":

                        fullname += "bootstrap.js";
                        break;

                    case "npm.js":

                        fullname += "npm.js";
                        break;

                }
                break;
            }
        }

        File file = new File(fullname);

        if (file.exists()) {
            return Response.ok(file, type).build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/ckeditor/{folder : .+}")
    @GET
    public Response ckeditor(@PathParam("folder") String folder) {

        String[] files =
                {
                        "adapters/jquery.js",
                        "README.md",
                        "CHANGES.md",
                        "styles.js",
                        "lang/sk.js",
                        "lang/fi.js",
                        "lang/it.js",
                        "lang/he.js",
                        "lang/uk.js",
                        "lang/sv.js",
                        "lang/en-ca.js",
                        "lang/sr-latn.js",
                        "lang/ru.js",
                        "lang/zh-cn.js",
                        "lang/no.js",
                        "lang/fr.js",
                        "lang/fa.js",
                        "lang/da.js",
                        "lang/mk.js",
                        "lang/ko.js",
                        "lang/ro.js",
                        "lang/mn.js",
                        "lang/tr.js",
                        "lang/bg.js",
                        "lang/ka.js",
                        "lang/de.js",
                        "lang/el.js",
                        "lang/pt.js",
                        "lang/af.js",
                        "lang/eu.js",
                        "lang/cy.js",
                        "lang/en-au.js",
                        "lang/hi.js",
                        "lang/en.js",
                        "lang/fr-ca.js",
                        "lang/nb.js",
                        "lang/sr.js",
                        "lang/en-gb.js",
                        "lang/ms.js",
                        "lang/pl.js",
                        "lang/is.js",
                        "lang/lv.js",
                        "lang/km.js",
                        "lang/tt.js",
                        "lang/th.js",
                        "lang/hu.js",
                        "lang/bn.js",
                        "lang/zh.js",
                        "lang/ja.js",
                        "lang/et.js",
                        "lang/nl.js",
                        "lang/ar.js",
                        "lang/eo.js",
                        "lang/lt.js",
                        "lang/gl.js",
                        "lang/ku.js",
                        "lang/cs.js",
                        "lang/vi.js",
                        "lang/ca.js",
                        "lang/ug.js",
                        "lang/fo.js",
                        "lang/id.js",
                        "lang/si.js",
                        "lang/sl.js",
                        "lang/pt-br.js",
                        "lang/es.js",
                        "lang/hr.js",
                        "lang/sq.js",
                        "lang/bs.js",
                        "lang/gu.js",
                        "skins/moono/dialog_ie7.css",
                        "skins/moono/dialog_ie.css",
                        "skins/moono/editor_iequirks.css",
                        "skins/moono/icons_hidpi.png",
                        "skins/moono/editor.css",
                        "skins/moono/readme.md",
                        "skins/moono/dialog_ie8.css",
                        "skins/moono/editor_ie.css",
                        "skins/moono/dialog.css",
                        "skins/moono/icons.png",
                        "skins/moono/dialog_iequirks.css",
                        "skins/moono/editor_ie7.css",
                        "skins/moono/editor_gecko.css",
                        "skins/moono/editor_ie8.css",
                        "skins/moono/images/spinner.gif",
                        "skins/moono/images/arrow.png",
                        "skins/moono/images/lock-open.png",
                        "skins/moono/images/lock.png",
                        "skins/moono/images/close.png",
                        "skins/moono/images/refresh.png",
                        "skins/moono/images/hidpi/lock-open.png",
                        "skins/moono/images/hidpi/lock.png",
                        "skins/moono/images/hidpi/close.png",
                        "skins/moono/images/hidpi/refresh.png",
                        "build-config.js",
                        "config.js",
                        "ckeditor.js",
                        "LICENSE.md",
                        "plugins/preview/preview.html",
                        "plugins/templates/templates/default.js",
                        "plugins/templates/templates/images/template3.gif",
                        "plugins/templates/templates/images/template1.gif",
                        "plugins/templates/templates/images/template2.gif",
                        "plugins/templates/dialogs/templates.css",
                        "plugins/templates/dialogs/templates.js",
                        "plugins/tabletools/dialogs/tableCell.js",
                        "plugins/icons_hidpi.png",
                        "plugins/dialog/dialogDefinition.js",
                        "plugins/iframe/dialogs/iframe.js",
                        "plugins/iframe/images/placeholder.png",
                        "plugins/liststyle/dialogs/liststyle.js",
                        "plugins/magicline/images/icon-rtl.png",
                        "plugins/magicline/images/icon.png",
                        "plugins/magicline/images/hidpi/icon-rtl.png",
                        "plugins/magicline/images/hidpi/icon.png",
                        "plugins/image/dialogs/image.js",
                        "plugins/image/images/noimage.png",
                        "plugins/link/dialogs/link.js",
                        "plugins/link/dialogs/anchor.js",
                        "plugins/link/images/anchor.png",
                        "plugins/link/images/hidpi/anchor.png",
                        "plugins/flash/dialogs/flash.js",
                        "plugins/flash/images/placeholder.png",
                        "plugins/about/dialogs/logo_ckeditor.png",
                        "plugins/about/dialogs/about.js",
                        "plugins/about/dialogs/hidpi/logo_ckeditor.png",
                        "plugins/icons.png",
                        "plugins/div/dialogs/div.js",
                        "plugins/specialchar/dialogs/lang/sk.js",
                        "plugins/specialchar/dialogs/lang/fi.js",
                        "plugins/specialchar/dialogs/lang/it.js",
                        "plugins/specialchar/dialogs/lang/he.js",
                        "plugins/specialchar/dialogs/lang/uk.js",
                        "plugins/specialchar/dialogs/lang/_translationstatus.txt",
                        "plugins/specialchar/dialogs/lang/sv.js",
                        "plugins/specialchar/dialogs/lang/ru.js",
                        "plugins/specialchar/dialogs/lang/zh-cn.js",
                        "plugins/specialchar/dialogs/lang/no.js",
                        "plugins/specialchar/dialogs/lang/fr.js",
                        "plugins/specialchar/dialogs/lang/fa.js",
                        "plugins/specialchar/dialogs/lang/da.js",
                        "plugins/specialchar/dialogs/lang/ko.js",
                        "plugins/specialchar/dialogs/lang/tr.js",
                        "plugins/specialchar/dialogs/lang/bg.js",
                        "plugins/specialchar/dialogs/lang/de.js",
                        "plugins/specialchar/dialogs/lang/el.js",
                        "plugins/specialchar/dialogs/lang/pt.js",
                        "plugins/specialchar/dialogs/lang/af.js",
                        "plugins/specialchar/dialogs/lang/eu.js",
                        "plugins/specialchar/dialogs/lang/cy.js",
                        "plugins/specialchar/dialogs/lang/en.js",
                        "plugins/specialchar/dialogs/lang/fr-ca.js",
                        "plugins/specialchar/dialogs/lang/nb.js",
                        "plugins/specialchar/dialogs/lang/en-gb.js",
                        "plugins/specialchar/dialogs/lang/pl.js",
                        "plugins/specialchar/dialogs/lang/lv.js",
                        "plugins/specialchar/dialogs/lang/km.js",
                        "plugins/specialchar/dialogs/lang/tt.js",
                        "plugins/specialchar/dialogs/lang/th.js",
                        "plugins/specialchar/dialogs/lang/hu.js",
                        "plugins/specialchar/dialogs/lang/zh.js",
                        "plugins/specialchar/dialogs/lang/ja.js",
                        "plugins/specialchar/dialogs/lang/et.js",
                        "plugins/specialchar/dialogs/lang/nl.js",
                        "plugins/specialchar/dialogs/lang/ar.js",
                        "plugins/specialchar/dialogs/lang/eo.js",
                        "plugins/specialchar/dialogs/lang/lt.js",
                        "plugins/specialchar/dialogs/lang/gl.js",
                        "plugins/specialchar/dialogs/lang/ku.js",
                        "plugins/specialchar/dialogs/lang/cs.js",
                        "plugins/specialchar/dialogs/lang/vi.js",
                        "plugins/specialchar/dialogs/lang/ca.js",
                        "plugins/specialchar/dialogs/lang/ug.js",
                        "plugins/specialchar/dialogs/lang/id.js",
                        "plugins/specialchar/dialogs/lang/si.js",
                        "plugins/specialchar/dialogs/lang/sl.js",
                        "plugins/specialchar/dialogs/lang/pt-br.js",
                        "plugins/specialchar/dialogs/lang/es.js",
                        "plugins/specialchar/dialogs/lang/hr.js",
                        "plugins/specialchar/dialogs/lang/sq.js",
                        "plugins/specialchar/dialogs/specialchar.js",
                        "plugins/table/dialogs/table.js",
                        "plugins/showblocks/images/block_address.png",
                        "plugins/showblocks/images/block_blockquote.png",
                        "plugins/showblocks/images/block_pre.png",
                        "plugins/showblocks/images/block_h2.png",
                        "plugins/showblocks/images/block_h3.png",
                        "plugins/showblocks/images/block_h1.png",
                        "plugins/showblocks/images/block_h4.png",
                        "plugins/showblocks/images/block_h6.png",
                        "plugins/showblocks/images/block_div.png",
                        "plugins/showblocks/images/block_p.png",
                        "plugins/showblocks/images/block_h5.png",
                        "plugins/find/dialogs/find.js",
                        "plugins/smiley/dialogs/smiley.js",
                        "plugins/smiley/images/lightbulb.gif",
                        "plugins/smiley/images/cry_smile.png",
                        "plugins/smiley/images/heart.gif",
                        "plugins/smiley/images/thumbs_up.png",
                        "plugins/smiley/images/wink_smile.png",
                        "plugins/smiley/images/teeth_smile.gif",
                        "plugins/smiley/images/teeth_smile.png",
                        "plugins/smiley/images/heart.png",
                        "plugins/smiley/images/regular_smile.gif",
                        "plugins/smiley/images/cry_smile.gif",
                        "plugins/smiley/images/shades_smile.gif",
                        "plugins/smiley/images/embarrassed_smile.png",
                        "plugins/smiley/images/broken_heart.gif",
                        "plugins/smiley/images/shades_smile.png",
                        "plugins/smiley/images/sad_smile.gif",
                        "plugins/smiley/images/omg_smile.gif",
                        "plugins/smiley/images/regular_smile.png",
                        "plugins/smiley/images/angel_smile.png",
                        "plugins/smiley/images/devil_smile.png",
                        "plugins/smiley/images/kiss.gif",
                        "plugins/smiley/images/whatchutalkingabout_smile.gif",
                        "plugins/smiley/images/omg_smile.png",
                        "plugins/smiley/images/envelope.gif",
                        "plugins/smiley/images/confused_smile.png",
                        "plugins/smiley/images/envelope.png",
                        "plugins/smiley/images/tongue_smile.gif",
                        "plugins/smiley/images/embarrassed_smile.gif",
                        "plugins/smiley/images/confused_smile.gif",
                        "plugins/smiley/images/angel_smile.gif",
                        "plugins/smiley/images/tounge_smile.gif",
                        "plugins/smiley/images/thumbs_down.png",
                        "plugins/smiley/images/thumbs_up.gif",
                        "plugins/smiley/images/lightbulb.png",
                        "plugins/smiley/images/tongue_smile.png",
                        "plugins/smiley/images/sad_smile.png",
                        "plugins/smiley/images/angry_smile.gif",
                        "plugins/smiley/images/angry_smile.png",
                        "plugins/smiley/images/devil_smile.gif",
                        "plugins/smiley/images/thumbs_down.gif",
                        "plugins/smiley/images/kiss.png",
                        "plugins/smiley/images/whatchutalkingabout_smile.png",
                        "plugins/smiley/images/wink_smile.gif",
                        "plugins/smiley/images/broken_heart.png",
                        "plugins/smiley/images/embaressed_smile.gif",
                        "plugins/forms/dialogs/textfield.js",
                        "plugins/forms/dialogs/select.js",
                        "plugins/forms/dialogs/hiddenfield.js",
                        "plugins/forms/dialogs/button.js",
                        "plugins/forms/dialogs/checkbox.js",
                        "plugins/forms/dialogs/textarea.js",
                        "plugins/forms/dialogs/form.js",
                        "plugins/forms/dialogs/radio.js",
                        "plugins/forms/images/hiddenfield.gif",
                        "plugins/pastefromword/filter/default.js",
                        "plugins/pagebreak/images/pagebreak.gif",
                        "plugins/wsc/README.md",
                        "plugins/wsc/dialogs/ciframe.html",
                        "plugins/wsc/dialogs/wsc.css",
                        "plugins/wsc/dialogs/wsc_ie.js",
                        "plugins/wsc/dialogs/wsc.js",
                        "plugins/wsc/dialogs/tmpFrameset.html",
                        "plugins/wsc/LICENSE.md",
                        "plugins/scayt/README.md",
                        "plugins/scayt/dialogs/toolbar.css",
                        "plugins/scayt/dialogs/options.js",
                        "plugins/scayt/CHANGELOG.md",
                        "plugins/scayt/LICENSE.md",
                        "plugins/colordialog/dialogs/colordialog.js",
                        "plugins/clipboard/dialogs/paste.js",
                        "plugins/a11yhelp/dialogs/a11yhelp.js",
                        "plugins/a11yhelp/dialogs/lang/sk.js",
                        "plugins/a11yhelp/dialogs/lang/fi.js",
                        "plugins/a11yhelp/dialogs/lang/it.js",
                        "plugins/a11yhelp/dialogs/lang/he.js",
                        "plugins/a11yhelp/dialogs/lang/uk.js",
                        "plugins/a11yhelp/dialogs/lang/_translationstatus.txt",
                        "plugins/a11yhelp/dialogs/lang/sv.js",
                        "plugins/a11yhelp/dialogs/lang/sr-latn.js",
                        "plugins/a11yhelp/dialogs/lang/ru.js",
                        "plugins/a11yhelp/dialogs/lang/zh-cn.js",
                        "plugins/a11yhelp/dialogs/lang/no.js",
                        "plugins/a11yhelp/dialogs/lang/fr.js",
                        "plugins/a11yhelp/dialogs/lang/fa.js",
                        "plugins/a11yhelp/dialogs/lang/da.js",
                        "plugins/a11yhelp/dialogs/lang/mk.js",
                        "plugins/a11yhelp/dialogs/lang/ko.js",
                        "plugins/a11yhelp/dialogs/lang/ro.js",
                        "plugins/a11yhelp/dialogs/lang/mn.js",
                        "plugins/a11yhelp/dialogs/lang/tr.js",
                        "plugins/a11yhelp/dialogs/lang/bg.js",
                        "plugins/a11yhelp/dialogs/lang/de.js",
                        "plugins/a11yhelp/dialogs/lang/el.js",
                        "plugins/a11yhelp/dialogs/lang/pt.js",
                        "plugins/a11yhelp/dialogs/lang/af.js",
                        "plugins/a11yhelp/dialogs/lang/eu.js",
                        "plugins/a11yhelp/dialogs/lang/cy.js",
                        "plugins/a11yhelp/dialogs/lang/hi.js",
                        "plugins/a11yhelp/dialogs/lang/en.js",
                        "plugins/a11yhelp/dialogs/lang/fr-ca.js",
                        "plugins/a11yhelp/dialogs/lang/nb.js",
                        "plugins/a11yhelp/dialogs/lang/sr.js",
                        "plugins/a11yhelp/dialogs/lang/en-gb.js",
                        "plugins/a11yhelp/dialogs/lang/pl.js",
                        "plugins/a11yhelp/dialogs/lang/lv.js",
                        "plugins/a11yhelp/dialogs/lang/km.js",
                        "plugins/a11yhelp/dialogs/lang/tt.js",
                        "plugins/a11yhelp/dialogs/lang/th.js",
                        "plugins/a11yhelp/dialogs/lang/hu.js",
                        "plugins/a11yhelp/dialogs/lang/zh.js",
                        "plugins/a11yhelp/dialogs/lang/ja.js",
                        "plugins/a11yhelp/dialogs/lang/et.js",
                        "plugins/a11yhelp/dialogs/lang/nl.js",
                        "plugins/a11yhelp/dialogs/lang/ar.js",
                        "plugins/a11yhelp/dialogs/lang/eo.js",
                        "plugins/a11yhelp/dialogs/lang/lt.js",
                        "plugins/a11yhelp/dialogs/lang/gl.js",
                        "plugins/a11yhelp/dialogs/lang/ku.js",
                        "plugins/a11yhelp/dialogs/lang/cs.js",
                        "plugins/a11yhelp/dialogs/lang/vi.js",
                        "plugins/a11yhelp/dialogs/lang/ca.js",
                        "plugins/a11yhelp/dialogs/lang/ug.js",
                        "plugins/a11yhelp/dialogs/lang/fo.js",
                        "plugins/a11yhelp/dialogs/lang/id.js",
                        "plugins/a11yhelp/dialogs/lang/si.js",
                        "plugins/a11yhelp/dialogs/lang/sl.js",
                        "plugins/a11yhelp/dialogs/lang/pt-br.js",
                        "plugins/a11yhelp/dialogs/lang/es.js",
                        "plugins/a11yhelp/dialogs/lang/hr.js",
                        "plugins/a11yhelp/dialogs/lang/sq.js",
                        "plugins/a11yhelp/dialogs/lang/gu.js",
                        "contents.css"
                };


        String fullname = "";
        String type = "text/plain";
        File file;

        for (String filename : files) {
            if (filename.equals(folder)) {
                fullname = "web/libs/ckeditor/" + filename;

                switch (filename.substring(filename.lastIndexOf(".") + 1)) {
                    case "js":
                        type = "text/javascript";
                        break;
                    case "css":
                        type = "text/css";
                        break;
                    case "html":
                        type = "text/html";
                        break;
                    case "txt":
                    case "md":
                        type = "text/plain";
                        break;
                    case "png":
                        type = "image/png";
                        break;
                    case "gif":
                        type = "image/gif";
                        break;
                    case "svg":
                        type = "image/svg+xml";
                }
            }
        }

        file = new File(fullname);

        if (file.exists()) {
            return Response.ok(file, type).build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/translation.json")
    @GET
    public Response translationjson() {

        File file = new File("languages/" + Settings.getInstance().getLangFileName());

        if (file.exists()) {
            return Response.ok(file, "application/json").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/translation.js")
    @GET
    public Response translationjs() {
        File file = new File("web/libs/js/translation.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    @Path("index/libs/js/third-party/qrcode.min.js")
    @GET
    public Response qrcodejs() {
        File file = new File("web/libs/js/third-party/qrcode.min.js");

        if (file.exists()) {
            return Response.ok(file, "text/javascript").build();
        } else {
            return error404(request, null);
        }
    }

    public Response error404(HttpServletRequest request, String titleOpt) {

        try {
            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/404.html", request);

            pebbleHelper.getContextMap().put(
                    "title",
                    titleOpt == null ? "Sorry, that page does not exist!"
                            : titleOpt);

            return Response.status(404)
                    .header("Content-Type", "text/html; charset=utf-8")
                    .entity(pebbleHelper.evaluate()).build();
        } catch (PebbleException e) {
            logger.error(e.getMessage(), e);
            return Response.status(404).build();
        }
    }

    @SuppressWarnings("unchecked")
    @Path("namestorage:{name}")
    @GET
    public Response showNamestorage(@PathParam("name") String name) {

        try {
            PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                    "web/main.mini.html", request);

            NameStorageMap nameStorageMap = DCSet.getInstance()
                    .getNameStorageMap();
            Map<String, String> map = nameStorageMap.get(name);

            if (map != null) {
                Set<String> keySet = map.keySet();
                JSONObject resultJson = new JSONObject();
                for (String key : keySet) {
                    String value = map.get(key);
                    resultJson.put(key, value);
                }

                pebbleHelper.getContextMap().put("keyvaluepairs", resultJson);
                pebbleHelper.getContextMap().put("dataname", name);

                return Response.status(200)
                        .header("Content-Type", "text/html; charset=utf-8")
                        .entity(pebbleHelper.evaluate()).build();

            } else {
                return error404(request,
                        "This namestorage does not contain any entries");
            }

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }

    }

    public String miniIndex() {
        try {
            return readFile("web/main.mini.html", StandardCharsets.UTF_8);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return "ERROR";
        }
    }

    @Path("/index/{html}")
    @GET
    public Response getHtml(@PathParam("html") String html) {
        return error404(request, null);
    }

    @Path("{name}/{key}")
    @GET
    public Response getKeyAsWebsite(@PathParam("name") String nameName,
                                    @PathParam("key") String key) {

        try {


            String website = DCSet.getInstance().getNameStorageMap()
                    .getOpt(nameName, key);

            if (website == null) {
                try {
                    return error404(request, "This key is empty");
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    return error404(request, null);
                }

            }

            return enhanceAndShowWebsite(website);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }
    }

    @Path("{name}")
    @GET
    public Response getNames(@PathParam("name") String nameName) {
        Account name = new Account(nameName);

        try {

            // CHECK IF NAME EXISTS
            if (name == null) {
                return error404(request, null);
            }

            String website = DCSet.getInstance().getNameStorageMap()
                    .getOpt(nameName, Corekeys.WEBSITE.toString());

            if (website == null) {
                try {
                    return error404(
                            request,
                            "This name has currently no <a href=\"/index/namestorage.html\">website<a/>!");
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    return error404(request, null);
                }

            }

            return enhanceAndShowWebsite(website);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return error404(request, null);
        }
    }

    private Response enhanceAndShowWebsite(String website) throws IOException,
            PebbleException {
        website = injectValues(website);

        File tmpFile = File.createTempFile("web", ".site");
        FileUtils.writeStringToFile(tmpFile, website, Charsets.UTF_8);
        PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
                tmpFile.getAbsolutePath(), request);
        //pebbleHelper.getContextMap().put("namestoragemap",
        //        NameStorageWebResource.getInstance());
        // pebbleHelper.getContextMap().put("atmap",DLSet.getInstance().getATMap());
        // pebbleHelper.getContextMap().put("attxsmap",DLSet.getInstance().getATTransactionMap());
        pebbleHelper.getContextMap().put("ats", ATWebResource.getInstance());
        pebbleHelper.getContextMap().put("controller",
                ControllerWebResource.getInstance());
        pebbleHelper.getContextMap().put("request", this.request);
        tmpFile.delete();

        // SHOW WEB-PAGE
        String evaluate = pebbleHelper.evaluate();

        String pictureRegex = "data.([a-zA-Z]+).([a-zA-Z]+);base64, (.+)";
        if (!evaluate.isEmpty()) {
            if (evaluate.matches(pictureRegex)) {

                String type = evaluate.replaceAll(pictureRegex, "$1");
                String subtype = evaluate.replaceAll(pictureRegex, "$2");
                byte[] dataOfImage = Base64.decode(evaluate.replaceAll(
                        pictureRegex, "$3"));
                Response build = Response
                        .ok(dataOfImage,
                                type + "/" + subtype + "; charset=utf-8")
                        .header("X-XSS-Protection", "0").build();
                return build;
            }
        }

        Response build = Response.ok(evaluate, "text/html; charset=utf-8")
                .header("X-XSS-Protection", "0").build();
        return build;
    }
}
