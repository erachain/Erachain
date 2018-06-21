package api;

import org.junit.Assert;
import org.junit.Test;
import api.ApiClient;

public class AddressesResourceTest {

    @Test
    public void getAddresses() {
        new ApiClient().executeCommand("POST wallet/unlock 1234567");

        String resultAddresses = new ApiClient().executeCommand("GET addresses");

        String[] parse = (resultAddresses.replace("\r\n", "").split(","));
        String address = parse[0].replace("[", "").replace("]", "");
        Assert.assertNotEquals(address, "");
        new ApiClient().executeCommand("GET wallet/lock");
    }
}