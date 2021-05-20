package org.erachain.webserver;

import org.erachain.core.item.ItemCls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PreviewVideo {

    static Logger LOGGER = LoggerFactory.getLogger(PreviewVideo.class.getSimpleName());

    public static byte[] getPreview(ItemCls item) {

        try {
            File file = makePreview(item);
            if (file == null)
                return null;
            if (file.canWrite())
                return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public static File makePreview(ItemCls item) {

        String outputName = item.getItemTypeName() + item.getKey();
        String path = "dataPreviews" + File.separator + outputName;
        File fileOut = new File(path + ".mp4");
        if (fileOut.exists()) {
            if (fileOut.canWrite())
                return fileOut;
            // он еще записывается
            return null;
        }

        ProcessBuilder builder = new ProcessBuilder("makeVPreview.bat",
                "dataPreviews/demo1.mp4", fileOut.toPath().toString());
        // указываем перенаправление stderr в stdout, чтобы проще было отлаживать
        builder.redirectErrorStream(true);

        String output = path + ".txt";
        builder.redirectOutput(new File(output));
        try {
            Process process = builder.start();
            process.waitFor();
            return fileOut;
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;

    }

}
