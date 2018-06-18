package test.webserver;

import org.junit.Assert;
import org.junit.Test;
import test.CallRemoteApi;
import test.SettingTests;
import webserver.API_TelegramsResource;

import javax.ws.rs.core.Response;

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
        String resultStatus = RemoteAPI.ResponseCodeAPI(SettingTests.URL_REMOTE_NODE + "/apitelegrams", "get");
        Assert.assertEquals(Integer.parseInt(resultStatus), 200);

        String resultValue = RemoteAPI.ResponseValueAPI(SettingTests.URL_REMOTE_NODE + "/apitelegrams", "get");
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

