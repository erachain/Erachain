package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.utils.StrJSonFine;
import org.erachain.utils.ZipBytes;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;

/**
 * Poll class (Create, vote by poll, get poll, )
 */
@Path("apidocuments")
@Produces(MediaType.APPLICATION_JSON)
public class APIDocuments {

    @Context
    HttpServletRequest request;

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("apidocuments/message/{seqNo}", "Get message from transaction");
        help.put("apidocuments/getFiles?block={block}&seqNo={seqNo}", "get files from transaction");
        help.put("apidocuments/getFile?download={true/false}block={block}&seqNo={seqNo}&name={name]", "get file (name) from transaction");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    @GET
    @Path("message/{seqNo}")
    public Response getMessage(@PathParam("seqNo") String seqNo) {
        String out;
        Transaction tx = DCSet.getInstance().getTransactionFinalMap().getRecord(seqNo);
        if (tx == null) {
            JSONObject result = new JSONObject();
            result.put("code", 2);
            result.put("message", "Transaction not exist");
            out = result.toJSONString();
        } else {
            out = ((RSignNote) tx).getMessage();
        }

        return Response.status(200).header("Content-Type", "text/plain; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out)
                .build();
    }

    /**
     * Get files from Transaction.
     * <br>
     * <h3>example request:</h3>
     * apidocuments/getFiles?blockl=1&txt=1
     *
     * @param block is number Block
     * @param seqNo is num Transaction from Block
     * @return JSOM format
     * 
     */

    @GET
    @Path("getFiles")
    public Response getFiles(@QueryParam("block") int block, @QueryParam("seqNo") int seqNo,
                             @QueryParam("txt") int seqNo_old) {
        JSONObject result = new JSONObject();
        try {
            //READ TXT
            if (seqNo == 0 && seqNo_old > 0) {
                seqNo = seqNo_old;
            }
            Transaction tx = DCSet.getInstance().getTransactionFinalMap().get(block, seqNo);
            if (tx instanceof RSignNote) {
                RSignNote statement = (RSignNote) tx;
                statement.parseDataFull();
                ExData exData = statement.getExData();

                HashMap<String, Tuple3<byte[], Boolean, byte[]>> files = exData.getFiles();
                if (files != null) {
                    Iterator<Entry<String, Tuple3<byte[], Boolean, byte[]>>> it_Files = files.entrySet().iterator();
                    int i = 0;
                    while (it_Files.hasNext()) {
                        Entry<String, Tuple3<byte[], Boolean, byte[]>> file = it_Files.next();
                        JSONObject jsonFile = new JSONObject();
                        jsonFile.put("filename", file.getKey());
                        jsonFile.put("hash", file.getValue().a);
                        jsonFile.put("ZIP", file.getValue().b);
                        result.put(i++, jsonFile);
                    }
                } else {
                    result.put("code", 4);
                    result.put("message", "Document not include files");
                }

            } else {
                result.put("code", 2);
                result.put("message", "Transaction is not Document");
            }


        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);

        }
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(result.toJSONString()).build();
    }

    @GET
    @Path("getFile")
    @Produces("application/zip")
    public Response getFile(@Context UriInfo info, @QueryParam("block") int block, @QueryParam("seqNo") int seqNo,
                            @QueryParam("txt") int seqNo_old,
                            @QueryParam("name") String name, @QueryParam("download") String downloadParam) {
        JSONObject result = new JSONObject();
        byte[] resultByte = null;
        try {
            if (seqNo == 0 && seqNo_old > 0) {
                seqNo = seqNo_old;
            }

            //READ TXT
           Transaction tx = DCSet.getInstance().getTransactionFinalMap().get(block, seqNo);
           if (tx instanceof RSignNote) {
               RSignNote statement = (RSignNote) tx;
               statement.parseDataFull();
               ExData exData = statement.getExData();

               HashMap<String, Tuple3<byte[], Boolean, byte[]>> files = exData.getFiles();
               if (files != null) {
                   Iterator<Entry<String, Tuple3<byte[], Boolean, byte[]>>> it_Files = files.entrySet().iterator();
                   int i = 0;
                   while (it_Files.hasNext()) {
                       Entry<String, Tuple3<byte[], Boolean, byte[]>> file = it_Files.next();
                       if (name.equals(file.getKey())) {
                           i++;

                           // if ZIP
                           if (file.getValue().b) {
                               // надо сделать

                               try {
                                   resultByte = ZipBytes.decompress(file.getValue().c);
                               } catch (DataFormatException e1) {
                                   // TODO Auto-generated catch block
                                   e1.printStackTrace();
                               } catch (IOException e) {
                                   // TODO Auto-generated catch block
                                   e.printStackTrace();
                               }
                           } else {
                               resultByte = file.getValue().c;
                           }
                           // if download Param

                           //  mime TYPE
                           InputStream is = new BufferedInputStream(new ByteArrayInputStream(resultByte));
                           String mm = null;
                           try {
                               mm = URLConnection.guessContentTypeFromStream(is);
                           } catch (IOException e) {
                               // TODO Auto-generated catch block
                               e.printStackTrace();
                           }

                           //if downloadParam
                           if (downloadParam != null) {

                               if (downloadParam.equals("true")) {
                                   String nameEncode = name.replace(" ", "_");
                                   try {
                                       nameEncode = URLEncoder.encode(nameEncode, "UTF-8");
                                   } catch (Exception e) {
                                       e.printStackTrace();
                                   }
                                   return Response.status(200).header("Content-Type", mm)
                                           .header("Access-Control-Allow-Origin", "*")
                                           .header("Content-disposition", "attachment; filename=" + nameEncode)
                                           .entity(new ByteArrayInputStream(resultByte))
                                           .build();
                               }
                           }

                           return Response.status(200).header("Content-Type", mm)
                                   .header("Access-Control-Allow-Origin", "*")
                                   //    .header("Content-disposition", "attachment; filename=" + new String(outputData).replace(" ", "_"))
                                   .entity(new ByteArrayInputStream(resultByte))
                                   .build();
                       }
                   }
               } else {
                   // view version 1
                   result.put("code", 3);
                   result.put("message", "Document version 1 (not include files)");
               }
               
           } else{
               result.put("code", 2);
               result.put("message", "Transaction is not Document");
           }

           
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);

        }
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(result.toJSONString()).build();
    }

}