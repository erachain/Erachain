package org.erachain.webserver;

import org.erachain.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
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

    /**
     * Первый запрос - выдаем что это тип Видео и размер
     * - дальше ждем запросы на кусочки и keep alive в запросе чтобы Jetty не рвал соединение.
     *
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    // Jetty does not need to be configured to use keep alive.
    // If the client offers a request that can be kept alive (HTTP/2, HTTP/1.1 or HTTP/1.0 with a keep-alive header),
    // then jetty will respond automatically to keep the connection alive
    // - unless there is an error or a filter/servlet/handler explicitly sets Connection:close on the response.

    static int RANGE_LEN = 25000;

    public static Response getRange(HttpServletRequest request, byte[] data) {

        long lastUpdated = cnt.blockChain.getGenesisTimestamp();
        String headerSince = request.getHeader("If-Modified-Since");
        // сервер шлет запрос мол поменялись данные? мы отвечаем - НЕТ
        if (false && headerSince != null && !headerSince.isEmpty()) {
            //LOGGER.debug(headerSince + " OK!!!");
            return Response.status(304) // not modified
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "video/mp4")
                    .header("Accept-Range", "bytes")
                    .build();
        }


        int maxEND = data.length - 1;
        int rangeStart;
        int rangeEnd;
        String rangeStr = request.getHeader("Range");

        if (rangeStr == null || rangeStr.isEmpty()) {
            // это первый запрос - ответим что тут Видео + его размер
            return Response.status(200) // set as first response
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Connection", "keep-alive")
                    //.header("Last-Modified", lastUpdated)
                    .header("Cache-Control", "public, max-age=31536000")
                    .header("Content-Transfer-Encoding", "binary")
                    .header("Content-Type", "video/mp4")
                    .header("Accept-Range", "bytes")
                    .header("Content-Length", data.length)
                    .header("Content-Range", "bytes 0-" + maxEND + "/" + data.length)
                    .entity(new ByteArrayInputStream(data))
                    .build();
        } else {
            // Range: bytes=0-1000  // bytes=301867-
            String[] tmp = rangeStr.substring(6).split("-");
            rangeStart = Integer.parseInt(tmp[0]);
            if (tmp.length == 1) {
                rangeEnd = rangeStart + RANGE_LEN - 1;
                if (rangeEnd > maxEND)
                    rangeEnd = maxEND;
            } else {
                try {
                    rangeEnd = Integer.parseInt(tmp[1]);
                    if (rangeEnd <= rangeStart) {
                        rangeEnd = rangeStart + RANGE_LEN - 1;
                        if (rangeEnd > maxEND)
                            rangeEnd = maxEND;
                    }
                } catch (Exception e) {
                    rangeEnd = rangeStart + RANGE_LEN - 1;
                    if (rangeEnd > maxEND)
                        rangeEnd = maxEND;
                }
            }
        }

        if (rangeStart < 0 || rangeStart > maxEND || rangeEnd > maxEND) {
            return Response.status(416) // out of range
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Connection", "keep-alive")
                    .header("Content-Transfer-Encoding", "binary")
                    .header("Content-Type", "video/mp4")
                    .header("Accept-Range", "bytes")
                    .header("Content-Length", 0)
                    .header("Content-Range", "bytes 0-0/" + data.length)
                    .build();
        }

        byte[] rangeBytes = new byte[rangeEnd - rangeStart + 1];
        System.arraycopy(data, rangeStart, rangeBytes, 0, rangeBytes.length);

        return Response.status(206)
                .header("Access-Control-Allow-Origin", "*")
                .header("Connection", "keep-alive")
                .header("Content-Type", "video/mp4")
                .header("Cache-Control", "public, max-age=31536000")
                .header("Content-Transfer-Encoding", "binary")
                .header("Accept-Range", "bytes")
                .header("Content-Length", rangeBytes.length)
                .header("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + data.length)
                .entity(new ByteArrayInputStream(rangeBytes))
                .build();
    }

    public Response getRange(HttpServletRequest request) {
        return getRange(request, data);
    }

}
