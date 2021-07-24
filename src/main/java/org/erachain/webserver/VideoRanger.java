package org.erachain.webserver;

import org.erachain.controller.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.Objects;

/**
 * for issue https://lab.erachain.org/erachain/Erachain/-/issues/1721
 */
public class VideoRanger {

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

    public Response get(HttpServletRequest request) {
        int rangeStart = -1;
        int rangeEnd = -1;
        String rangeStr = request.getHeader("Range");
        if (rangeStr == null)
            rangeStr = request.getHeader("range");

        if (rangeStr != null) {
            // Range: bytes=0-1000
            if (rangeStr.startsWith("bytes=")) {
                String[] tmp = rangeStr.substring(6).split("-");
                try {
                    rangeStart = Integer.parseInt(tmp[0]);
                    rangeEnd = Integer.parseInt(tmp[1]);
                } catch (Exception e) {
                }
            }
        }

        if (rangeEnd < 0) {
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Content-length", data.length)
                    .header("Last-Modified", cnt.blockChain.getGenesisTimestamp())
                    .header("Content-Type", "video/mp4")
                    .header("Timing-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(data))
                    .build();

        }

        if (rangeEnd > data.length)
            rangeEnd = data.length;

        byte[] rangeBytes = new byte[rangeEnd - rangeStart];
        System.arraycopy(data, rangeStart, rangeBytes, 0, rangeBytes.length);

        return Response.status(rangeEnd == data.length ? 200 : 206)
                .header("Access-Control-Allow-Origin", "*")
                .header("Timing-Allow-Origin", "*")
                .header("Last-Modified", cnt.blockChain.getGenesisTimestamp())
                .header("Content-Type", "video/mp4")
                .header("Accept-Range", "bytes")
                .header("Content-Length", rangeBytes.length)
                .header("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + data.length)
                .entity(new ByteArrayInputStream(rangeBytes))
                .build();
    }

}
