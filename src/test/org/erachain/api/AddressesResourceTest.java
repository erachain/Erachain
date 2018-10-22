package org.erachain.api;

import org.junit.Assert;
import org.junit.Test;

import org.erachain.api.ApiClient;
import test.SettingTests;

public class AddressesResourceTest extends SettingTests {

    @Test
    public void getAddresses() {
        new ApiClient().executeCommand("POST wallet/unlock " + WALLET_PASSWORD);

        String resultAddresses = new ApiClient().executeCommand("GET addresses");

        String[] parse = (resultAddresses.replace("\r\n", "").split(","));
        String address = parse[0].replace("[", "").replace("]", "");
        Assert.assertNotEquals(address, "");
        new ApiClient().executeCommand("GET wallet/lock");
    }
}