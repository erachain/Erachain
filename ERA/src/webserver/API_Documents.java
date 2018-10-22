package webserver;

import api.ApiErrorFactory;
import core.exdata.ExData;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import datachain.DCSet;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple4;

import utils.StrJSonFine;
import utils.Zip_Bytes;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;

/**
 * Poll class (Create, vote by poll, get poll, )
 */
@Path("apidocuments")
@Produces(MediaType.APPLICATION_JSON)
public class API_Documents {

    @Context
    HttpServletRequest request;

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("apidocuments/getFiles?block={block}&txt={transaction}", "get files from transaction");
        help.put("apidocuments/getFile?download={true/false}block={block}&txt={transaction}&name={name]", "get file (name) from transaction");
               
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    /**
     * Get files from Transaction.
     * <br>
     * <h3>example request:</h3>
     * apidocuments/getFiles?blockl=1&txt=1
     *
     * @param block is number Block
     * @param txt is num Transaction from Block
     * @return JSOM format
     * 
     */

    @GET
    @Path("getFiles")
    public Response getFiles(@QueryParam("block") int blockN, @QueryParam("txt") int txtN ) {
       JSONObject result = new JSONObject();
        try {
            //READ TXT
           Transaction tx = DCSet.getInstance().getTransactionFinalMap().get(blockN, txtN);
           if (tx instanceof R_SignNote){
               R_SignNote statement = (R_SignNote)tx; 
               if (statement.getVersion() == 2) {
                   byte[] data = statement.getData();
                   Tuple4<String, String, JSONObject, HashMap<String, Tuple2<Boolean, byte[]>>> map;
                try {
                    map = ExData.parse_Data_V2(data);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
                }

                   JSONObject jSON = map.c;

                   HashMap<String, Tuple2<Boolean, byte[]>> files = map.d;
                   if (files != null) {
                       Iterator<Entry<String, Tuple2<Boolean, byte[]>>> it_Files = files.entrySet().iterator();
                       int i = 0;
                       while (it_Files.hasNext()) {
                           Entry<String, Tuple2<Boolean, byte[]>> file = it_Files.next();
                           JSONObject jsonFile = new JSONObject();
                           jsonFile.put("filename", (String) file.getKey());
                           jsonFile.put("ZIP", file.getValue().a);
                           result.put(i++,jsonFile);
                       }
                   }else{
                       result.put("code", 4);
                       result.put("message", "Document not include files");
                   }
                       
                   
               }else{
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

   

    @GET
    @Path("getFile")
    @Produces("application/zip")
    public Response getFile(@QueryParam("block") int blockN, @QueryParam("txt") int txtN, @QueryParam("name") String name, @QueryParam("download") String downloadParam ) {
       JSONObject result = new JSONObject();
       byte[] resultByte = null;
        try {
            //READ TXT
           Transaction tx = DCSet.getInstance().getTransactionFinalMap().get(blockN, txtN);
           if (tx instanceof R_SignNote){
               R_SignNote statement = (R_SignNote)tx; 
               if (statement.getVersion() == 2) {
                   byte[] data = statement.getData();
                   Tuple4<String, String, JSONObject, HashMap<String, Tuple2<Boolean, byte[]>>> map;
                try {
                    map = ExData.parse_Data_V2(data);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
                }

                   JSONObject jSON = map.c;

                   HashMap<String, Tuple2<Boolean, byte[]>> files = map.d;
                   if (files != null) {
                       Iterator<Entry<String, Tuple2<Boolean, byte[]>>> it_Files = files.entrySet().iterator();
                       int i = 0;
                       while (it_Files.hasNext()) {
                           Entry<String, Tuple2<Boolean, byte[]>> file = it_Files.next();
                           JSONObject jsonFile = new JSONObject();
                           if (name.equals((String) file.getKey())){
                               i++;
                              
                               // if ZIP
                              if(file.getValue().a){
                                  // надо сделать
                                 
                                  try {
                                      resultByte = Zip_Bytes.decompress(file.getValue().b);
                                  } catch (DataFormatException e1) {
                                      // TODO Auto-generated catch block
                                      e1.printStackTrace();
                                  } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                              }else{
                                  resultByte = file.getValue().b;
                              }
                  // ifdownloadParam
                              
                              // convert shar to ANSI 
                              Charset utf8charset = Charset.forName("UTF-8");
                              Charset iso88591charset = Charset.forName("ISO-8859-1");
                              ByteBuffer inputBuffer = ByteBuffer.wrap(name.getBytes());
                              // decode UTF-8
                              CharBuffer data1 = utf8charset.decode(inputBuffer);
                              // encode ISO-8559-1
                              ByteBuffer outputBuffer = iso88591charset.encode(data1);
                              byte[] outputData = outputBuffer.array();
                              String ss = new String(outputData);
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
                            if(downloadParam!=null){
                                
                               if (downloadParam.equals("true")){
                                    return Response.status(200).header("Content-Type", mm)
                                            .header("Access-Control-Allow-Origin", "*")
                                            .header("Content-disposition", "attachment; filename=" + new String(outputData).replace(" ", "_"))
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
                          
                   }else{
                       result.put("code", 4);
                       result.put("message", "Document not include files");
                   }
                       
                   
               }
               else{
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