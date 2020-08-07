package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.Transaction;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("persons")
@Produces(MediaType.APPLICATION_JSON)
public class PersonsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonsResource.class);

    @Context
    HttpServletRequest request;

    @SuppressWarnings("unchecked")
    @GET
    public String getPersons() {

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        Collection<ItemCls> persons = Controller.getInstance().getAllItems(ItemCls.PERSON_TYPE);
        JSONArray array = new JSONArray();

        for (ItemCls person : persons) {
            array.add(((PersonCls) person).toJson());
        }

        return array.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/address/{address}")
    public String getPersons(@PathParam("address") String address) {

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        //CHECK ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        //CHECK ACCOUNT IN WALLET
        Account account = Controller.getInstance().getWalletAccountByAddress(address);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
        }

        JSONArray array = new JSONArray();
        for (ItemCls person : Controller.getInstance().getAllItems(ItemCls.PERSON_TYPE, account)) {
            array.add(((PersonCls) person).toJson());
        }

        return array.toJSONString();
    }

}
