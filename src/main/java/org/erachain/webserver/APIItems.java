package org.erachain.webserver;

//import com.google.common.collect.Iterables;
//import com.google.gson.internal.LinkedHashTreeMap;
//import com.sun.org.apache.xpath.internal.operations.Or;
//import javafx.print.Collation;

import org.erachain.core.item.ItemCls;
import org.erachain.datachain.ItemMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;

//import com.google.gson.Gson;
//import org.mapdb.Fun;

public class APIItems {

    public static Response getImage(ItemMap map, long key) {

        ItemCls item = map.get(key);

        if (item.getImage() == null) {
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("")
                    .build();

        }

        Response.ResponseBuilder response = Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ByteArrayInputStream(item.getImage()));

        if (item.getImageType() == ItemCls.MEDIA_TYPE_IMG) {
            response.type(new MediaType("image", "gif"));
            response.type(new MediaType("image", "jpeg"));
        } else if (item.getImageType() == ItemCls.MEDIA_TYPE_VIDEO) {
            response.type(new MediaType("video", "mp4"));
        }

        return response.build();

    }

    public static Response getIcon(ItemMap map, long key) {

        ItemCls item = map.get(key);

        if (item.getIcon() == null) {
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("")
                    .build();

        }

        Response.ResponseBuilder response = Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ByteArrayInputStream(item.getIcon()));

        if (item.getIconType() == ItemCls.MEDIA_TYPE_IMG) {
            response.type(new MediaType("image", "gif"));
            response.type(new MediaType("image", "jpeg"));
        } else if (item.getIconType() == ItemCls.MEDIA_TYPE_VIDEO) {
            response.type(new MediaType("video", "mp4"));
        }

        return response.build();

    }

}