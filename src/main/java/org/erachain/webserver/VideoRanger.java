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

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    static int RANGE_LEN = 1 << 14;
    public static Response getRange(HttpServletRequest request, byte[] data) {
        String headerSince = request.getHeader("If-Modified-Since");
        if (false && headerSince != null && !headerSince.isEmpty())
            return Response.status(304)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Timing-Allow-Origin", "*")
                    .header("Last-Modified", cnt.blockChain.getGenesisTimestamp())
                    .build();


        int rangeStart;
        int rangeEnd;
        String rangeStr = request.getHeader("Range");
        LOGGER.debug("Range: " + rangeStr == null ? "null" : rangeStr);

        if (rangeStr == null || rangeStr.isEmpty() || !rangeStr.startsWith("bytes=")) {
            rangeStart = 0;
            rangeEnd = RANGE_LEN;
        } else {
            // Range: bytes=0-1000  // bytes=301867-
            String[] tmp = rangeStr.substring(6).split("-");
            rangeStart = Integer.parseInt(tmp[0]);
            if (tmp.length == 1) {
                rangeEnd = data.length;
            } else {
                try {
                    rangeEnd = Integer.parseInt(tmp[1]);
                } catch (Exception e) {
                    rangeEnd = data.length;
                }
            }
        }

        if (rangeEnd > data.length)
            rangeEnd = data.length;

        LOGGER.debug("bytes " + rangeStart + "-" + rangeEnd + "/" + data.length);

        byte[] rangeBytes = new byte[rangeEnd - rangeStart];
        System.arraycopy(data, rangeStart, rangeBytes, 0, rangeBytes.length);

        return Response.status(rangeEnd == data.length ? 200 : 206)
                .header("Access-Control-Allow-Origin", "*")
                .header("Timing-Allow-Origin", "*")
                .header("Last-Modified", cnt.blockChain.getGenesisTimestamp())
                .header("Content-Type", "video/mp4")
                .header("Accept-Range", "bytes")
                .header("Content-Length", data.length)
                .header("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + data.length)
                .entity(new ByteArrayInputStream(rangeBytes))
                .build();
    }

    public Response getRange(HttpServletRequest request) {
        return getRange(request, data);
    }

}
