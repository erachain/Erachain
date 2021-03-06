package org.erachain.webserver;

import org.erachain.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
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

    /**
     * Размер буфера для выдачи ответа - сейчас сети очень быстрые - задержка на запрос 42мс,
     * а ответ в 1МБ с загрузкой - задержка всего 200-300мс. То есть нет смысла бить на маленькие пакеты - тогда время на запросы съедается почем зря
     * Оптимально 250 к и выше. 250к - это 68мс задержка загрузки - т о есть полностью запрос обрабатывается за 120мс
     * Но зато если бить мельче то одновременная загрузка большого числа видео будет меньше грузить сервер
     */
    static int RANGE_LEN = 1 << 18;

    public static Response getRange(HttpServletRequest request, byte[] data, MediaType mediaType, boolean asPreview) {

        long lastUpdated = cnt.blockChain.getGenesisTimestamp();
        String headerSince = request.getHeader("If-Modified-Since");
        // сервер шлет запрос мол поменялись данные? мы отвечаем - НЕТ
        if (false && headerSince != null && !headerSince.isEmpty()) {
            //LOGGER.debug(headerSince + " OK!!!");
            return Response.status(304) // not modified
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", mediaType.toString())
                    .header("Accept-Range", "bytes")
                    .build();
        }

        int maxEND = data.length - 1;
        int rangeLen = asPreview ? RANGE_LEN >> 2 : RANGE_LEN;
        int rangeStart;
        int rangeEnd;
        String rangeStr = request.getHeader("Range");

        // Sec-Fetch-Dest: document
        if (rangeStr == null || rangeStr.isEmpty()) {
            // это первый запрос - ответим что тут Видео + его размер
            return Response.status(200) // set as first response
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Connection", "keep-alive")
                    //.header("Cache-Control", "public, max-age=31536000")
                    .header("Content-Transfer-Encoding", "binary")
                    .header("Content-Type", mediaType.toString())
                    .header("Accept-Range", "bytes")
                    // ****
                    // для плеера на андроиде надо именно так - все данные в буфер пихать
                    // на время обработки это не особо влияет так как копирования данных из DATA в буфер тут нет
                    // и остальные браузеры и плееры этот запрос игнорируют все равно и время не тратят на него
                    .header("Content-Length", data.length)
                    .header("Content-Range", "bytes 0-" + maxEND + "/" + data.length)
                    // ****
                    .entity(new ByteArrayInputStream(data))
                    .build();
        } else {
            // Range: bytes=0-1000  // bytes=301867-
            String[] tmp = rangeStr.substring(6).split("-");
            rangeStart = Integer.parseInt(tmp[0]);
            if (tmp.length == 1) {
                rangeEnd = rangeStart + rangeLen - 1;
                if (rangeEnd > maxEND)
                    rangeEnd = maxEND;
            } else {
                try {
                    rangeEnd = Integer.parseInt(tmp[1]);
                    if (rangeEnd <= rangeStart) {
                        rangeEnd = rangeStart + rangeLen - 1;
                        if (rangeEnd > maxEND)
                            rangeEnd = maxEND;
                    }
                } catch (Exception e) {
                    rangeEnd = rangeStart + rangeLen - 1;
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
                    .header("Content-Type", mediaType.toString())
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
                .header("Content-Type", mediaType.toString())
                //.header("Cache-Control", "public, max-age=31536000")
                .header("Content-Transfer-Encoding", "binary")
                .header("Accept-Range", "bytes")
                .header("Content-Length", rangeBytes.length)
                .header("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + data.length)
                .entity(new ByteArrayInputStream(rangeBytes))
                .build();
    }

    public Response getRange(HttpServletRequest request, MediaType mediaType, boolean asPreview) {
        return getRange(request, data, mediaType, asPreview);
    }

}
