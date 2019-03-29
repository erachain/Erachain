package org.erachain.webserver;

import javax.ws.rs.core.Response;

import org.erachain.CallRemoteApi;
import org.erachain.SettingTests;
import org.junit.Assert;
import org.junit.Test;

//import test.CallRemoteApi;
//import test.SettingTests;


public class API_TelegramsResourceTest extends SettingTests {

    /**
     * check documentation is not null and status 200
     */
    @Test
    public void Default() {
        Response annotation = new APITelegramsResource().Default();
        Assert.assertEquals(annotation.getStatus(), 200);
        Assert.assertNotNull(annotation.getEntity());
    }

    @Test
    public void DefaultRemote() throws Exception {
        CallRemoteApi RemoteAPI = new CallRemoteApi();
        String resultStatus = RemoteAPI.ResponseCodeAPI(URL_REMOTE_NODE_API + "/apitelegrams", "get");
        Assert.assertEquals(Integer.parseInt(resultStatus), 200);

        String resultValue = RemoteAPI.ResponseValueAPI(URL_REMOTE_NODE_API + "/apitelegrams", "get", "");
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