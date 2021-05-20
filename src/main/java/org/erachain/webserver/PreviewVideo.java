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
            return Files.readAllBytes(makePreview(itemType, key).toPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public static File makePreview(String itemType, long key) {

        String path = "previews" + File.separator + itemType + key;
        File file = new File(path);
        if (file.exists()) {
            if (file.canWrite())
                return file;
            // он еще записывается
            return null;
        }

        ProcessBuilder builder = new ProcessBuilder(path + ".mp4", "previews" + File.separator + "make");
        // задаем переменную окружения руками
        // builder.environment().put( "COWPATH", "e:/cowsay-inst/share/cows/" );
        // указываем перенаправление stderr в stdout, чтобы проще было отлаживать
        builder.redirectErrorStream(true);
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
