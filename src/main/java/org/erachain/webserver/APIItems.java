package org.erachain.webserver;

//import com.google.common.collect.Iterables;
//import com.google.gson.internal.LinkedHashTreeMap;
//import com.sun.org.apache.xpath.internal.operations.Or;
//import javafx.print.Collation;

import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.datachain.ItemMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;

//import com.google.gson.Gson;
//import org.mapdb.Fun;

public class APIItems {

    public static Response getImage(ItemMap map, long key, boolean preView) {

        ItemCls item = map.get(key);

        byte[] image;
        MediaType mediaType;

        if (item.getImageType() == ItemCls.MEDIA_TYPE_AUDIO) {
            if (preView) {
                image = item.getIcon();
                mediaType = item.getIconMediaType();
            } else {
                image = item.getImage();
                mediaType = item.getImageMediaType();
            }
        } else {
            image = item.getImage();
            if (image == null || image.length == 0) {
                return Response.status(200)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity("")
                        .build();
            }
            if (PreviewMaker.notNeedPreview(item, image)) {
                mediaType = item.getImageMediaType();
            } else {
                PreviewMaker preViewMaker = new PreviewMaker();
                preViewMaker.makePreview(item, image);
                if (preView) {
                    image = preViewMaker.getPreview((item), image);
                    if (image == null) {
                        if (preViewMaker.errorMess == null) {
                            throw ApiErrorFactory.getInstance().createError(
                                    "Some error - see in dataPreviews" + File.separator + "orig" + File.separator + PreviewMaker.getItemName(item) + ".log");
                        } else {
                            throw ApiErrorFactory.getInstance().createError(
                                    preViewMaker.errorMess);
                        }
                    }
                    mediaType = new MediaType("video", "mp4");
                } else {
                    mediaType = item.getImageMediaType();
                }
            }
        }

        Controller cnt = Controller.getInstance();

        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Content-length", image.length)
                .header("Last-Modified", cnt.blockChain.getTimestamp(1000))
                .header("Timing-Allow-Origin", "*")
                .entity(new ByteArrayInputStream(image))
                .type(mediaType)
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