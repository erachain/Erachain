package org.erachain.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PreviewVideo {

    static Logger LOGGER = LoggerFactory.getLogger(PreviewVideo.class.getSimpleName());

    public static byte[] getPreview(String itemType, long key) {

        try {
            File file = makePreview(itemType, key);
            if (file == null)
                return null;
            if (file.canWrite())
                return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public static File makePreview(String itemType, long key) {

        String path = "dataPreviews" + File.separator + itemType + key + ".mp4";
        File file = new File(path);
        if (file.exists()) {
            if (file.canWrite())
                return file;
            // он еще записывается
            return null;
        }

        ProcessBuilder builder = new ProcessBuilder("makeVPreview.bat", path);
        // указываем перенаправление stderr в stdout, чтобы проще было отлаживать
        builder.redirectErrorStream(true);

        String output = "dataPreviews" + File.separator + itemType + key + ".txt";
        builder.redirectOutput(new File(output));
        try {
            Process process = builder.start();
            process.waitFor();
            return file;
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;

    }

}
