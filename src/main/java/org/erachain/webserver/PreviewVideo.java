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
            if (file.canRead())
                return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public static File getPreviewAsFile(ItemCls item) {

        File file = makePreview(item);
        if (file == null)
            return null;
        if (file.canRead())
            return file;

        return null;
    }

    public static File makePreview(ItemCls item) {

        String outputName = item.getItemTypeName() + item.getKey();
        String path = "dataPreviews" + File.separator + outputName;
        File fileOut = new File(path + ".mp4");
        if (fileOut.exists()) {
            if (fileOut.canRead())
                return fileOut;
            // он еще записывается
            return null;
        }

        byte[] image = item.getImage();
        String parQV;
        String parRV;
        if (image.length > 4000000) {
            parQV = "20";
            parRV = "10";
        } else if (image.length > 1000000) {
            parQV = "16";
            parRV = "12";
        } else if (image.length > 500000) {
            parQV = "14";
            parRV = "14";
        } else {
            parQV = "12";
            parRV = "15";
        }

        ProcessBuilder builder = new ProcessBuilder("makeVPreview.bat",
                "dataPreviews/demo1.mp4", "-q:v " + parQV + " -r:v " + parRV, fileOut.toPath().toString());
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
