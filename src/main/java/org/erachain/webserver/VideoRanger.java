package org.erachain.webserver;

import org.erachain.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.Objects;

/**
 * for issue https://lab.erachain.org/erachain/Erachain/-/issues/1721
 */
public class VideoRanger {

    static Logger LOGGER = LoggerFactory.getLogger(VideoRanger.class.getSimpleName());

    /**
     * for CACHE
     */
    String url;
    byte[] data;

    static Controller cnt = Controller.getInstance();

    VideoRanger(String url, byte[] data) {
        this.url = url;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoRanger)) return false;
        VideoRanger that = (VideoRanger) o;
        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    // Jetty does not need to be configured to use keep alive.
    // If the client offers a request that can be kept alive (HTTP/2, HTTP/1.1 or HTTP/1.0 with a keep-alive header),
    // then jetty will respond automatically to keep the connection alive
    // - unless there is an error or a filter/servlet/handler explicitly sets Connection:close on the response.

    static int RANGE_LEN = 1 << 17;

    public static Response getRange(HttpServletRequest request, byte[] data) {
        String headerSince = request.getHeader("If-Modified-Since");
        // сервер шлет запрос мол поменялись данные? мы отвечаем - НЕТ
        if (false && headerSince != null && !headerSince.isEmpty())
            return Response.status(304)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Timing-Allow-Origin", "*")
                    .header("Last-Modified", cnt.blockChain.getGenesisTimestamp())
                    .build();
        if (false)
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Timing-Allow-Origin", "*")
                    .header("Last-Modified", cnt.blockChain.getGenesisTimestamp())
                    .header("Content-Type", "video/mp4")
                    .entity(new ByteArrayInputStream(data))
                    .build();


        int maxEND = data.length - 1;
        int rangeStart;
        int rangeEnd;
        String rangeStr = request.getHeader("Range");
        LOGGER.debug("Range: [" + (rangeStr == null ? "null" : rangeStr) + "]");
        Enumeration<String> headersKeys = request.getHeaderNames();
        while (headersKeys.hasMoreElements()) {
            String key = headersKeys.nextElement();
            LOGGER.debug(key + ": " + request.getHeader(key));
        }


        if (rangeStr == null || rangeStr.isEmpty() || !rangeStr.startsWith("bytes=")) {
            rangeStart = 0;
            rangeEnd = RANGE_LEN;
        } else {
            // Range: bytes=0-1000  // bytes=301867-
            String[] tmp = rangeStr.substring(6).split("-");
            rangeStart = Integer.parseInt(tmp[0]);
            if (tmp.length == 1) {
                rangeEnd = rangeStart + RANGE_LEN; //maxEND;
                //rangeEnd = maxEND - 1;
            } else {
                try {
                    rangeEnd = Integer.parseInt(tmp[1]);
                } catch (Exception e) {
                    rangeEnd = maxEND;
                }
            }
        }

        if (rangeEnd > maxEND)
            rangeEnd = maxEND;

        int status = rangeEnd == maxEND ? 200 : 206;
        rangeStr = "bytes " + rangeStart + "-" + rangeEnd + "/" + maxEND;
        LOGGER.debug(status + ": " + rangeStr);

        byte[] rangeBytes = new byte[rangeEnd - rangeStart + 1];
        System.arraycopy(data, rangeStart, rangeBytes, 0, rangeBytes.length - 1);

        Response.ResponseBuilder responce = Response.status(status)
                .header("Access-Control-Allow-Origin", "*")
                .header("Timing-Allow-Origin", "*")
                .header("Last-Modified", cnt.blockChain.getGenesisTimestamp())
                //.header("Last-Modified", System.currentTimeMillis())
                .header("Content-Type", "video/mp4");

        if (status == 200) {
        } else {
            responce.header("Accept-Range", "bytes")
                    .header("Content-Length", rangeBytes.length)
                    .header("Content-Range", rangeStr);
        }

        return responce
                .entity(new ByteArrayInputStream(rangeBytes))
                .build();
    }

    public Response getRange(HttpServletRequest request) {
        return getRange(request, data);
    }

}
