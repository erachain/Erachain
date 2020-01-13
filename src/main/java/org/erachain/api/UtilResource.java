package org.erachain.api;

import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.datachain.DCSet;
import org.erachain.utils.Chain;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("util")
@Produces(MediaType.APPLICATION_JSON)
public class UtilResource {
    @Context
    HttpServletRequest request;

    @GET
    @Path("hw_test")
    public String hw_Test() {
        return String.valueOf(Chain.hw_Test());
    }

    @GET
    @Path("parse/{seqNo}")
    public String parse(@PathParam("seqNo") String seqNo) {
        Transaction transaction = DCSet.getInstance().getTransactionFinalMap().getRecord(seqNo);
        if (transaction == null)
            return "null";

        byte[] bytes = transaction.toBytes(Transaction.FOR_NETWORK, true);
        try {
            transaction = TransactionFactory.getInstance().parse(bytes, Transaction.FOR_NETWORK);
        } catch (Exception e) {
        }

        return bytes.toString();
    }

}
