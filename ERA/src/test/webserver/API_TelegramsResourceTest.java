package test.webserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import api.ApiClient;
import test.CallRemoteApi;
import test.SettingTests;
import webserver.API_TelegramsResource;

public class API_TelegramsResourceTest extends SettingTests {

    /**
     * check documentation is not null and status 200
     */
    @Test
    public void Default() {
        Response annotation = new API_TelegramsResource().Default();
        Assert.assertEquals(annotation.getStatus(), 200);
        Assert.assertNotNull(annotation.getEntity());
    }

    @Test
    public void DefaultRemote() throws Exception {
        CallRemoteApi RemoteAPI = new CallRemoteApi();
        String resultStatus = RemoteAPI.ResponseCodeAPI(SettingTests.URL_REMOTE_NODE_API + "/apitelegrams", "get");
        Assert.assertEquals(Integer.parseInt(resultStatus), 200);

        String resultValue = RemoteAPI.ResponseValueAPI(SettingTests.URL_REMOTE_NODE_API + "/apitelegrams", "get", "");
        Assert.assertNotNull(resultValue);
        Assert.assertNotEquals(resultValue, "");

    }

    @Test
    public void getTelegramBySignature() {

    }

    @Test
    public void getTelegramsTimestamp() {

    }
}