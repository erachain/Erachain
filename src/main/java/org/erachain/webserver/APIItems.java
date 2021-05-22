package org.erachain.webserver;

//import com.google.common.collect.Iterables;
//import com.google.gson.internal.LinkedHashTreeMap;
//import com.sun.org.apache.xpath.internal.operations.Or;
//import javafx.print.Collation;

import org.erachain.api.ApiErrorFactory;
import org.erachain.core.item.ItemCls;
import org.erachain.datachain.ItemMap;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;

//import com.google.gson.Gson;
//import org.mapdb.Fun;

public class APIItems {

    public static Response getImage(ItemMap map, long key, boolean preView) {

        ItemCls item = map.get(key);

        byte[] image = item.getImage();
        if (image == null || image.length == 0) {
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("")
                    .build();
        }


        PreviewMaker preViewMaker = new PreviewMaker();
        preViewMaker.makePreview(item, image);
        if (preView) {
            image = preViewMaker.getPreview((item), image);
            if (image == null) {
                if (preViewMaker.errorMess == null) {
                    throw ApiErrorFactory.getInstance().createError(
                            "previewmaker parameter is empty, see z_START_EXAMPLES/readme-commands.txt");
                } else {
                    throw ApiErrorFactory.getInstance().createError(
                            preViewMaker.errorMess);
                }
            }
        }

        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ByteArrayInputStream(image))
                .type(item.getImageMediaType())
                .build();

    }

    public static Response getIcon(ItemMap map, long key) {

        ItemCls item = map.get(key);

        byte[] icon = item.getIcon();
        if (icon == null || icon.length == 0) {
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("")
                    .build();
        }

        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ByteArrayInputStream(icon))
                .type(item.getIconMediaType())
                .build();

    }

}