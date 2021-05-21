package org.erachain.webserver;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class PreviewVideo {

    static final int VIDEO_USE_ORIG_LEN = 1 << 19;
    static final int IMAGE_USE_ORIG_LEN = 1 << 18;

    static Logger LOGGER = LoggerFactory.getLogger(PreviewVideo.class.getSimpleName());

    public static byte[] getPreview(ItemCls item, byte[] image) {

        if (item.getImageType() == AssetCls.MEDIA_TYPE_VIDEO && image.length < VIDEO_USE_ORIG_LEN
                || item.getImageType() == AssetCls.MEDIA_TYPE_IMG //&& image.length < IMAGE_USE_ORIG_LEN
        )
            return image;

        try {
            File file = makePreview(item, image);
            if (file == null)
                return image;
            if (file.canRead())
                return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return image;
    }

    public static File makePreview(ItemCls item, byte[] image) {

        if (image.length < VIDEO_USE_ORIG_LEN)
            return null;

        if (item.getImageType() == AssetCls.MEDIA_TYPE_IMG) {

        }

        String outputName = item.getItemTypeName() + item.getKey();
        String path = "dataPreviews\\" + outputName;
        File fileOut = new File(path + ".mp4");
        if (fileOut.exists()) {
            if (fileOut.canRead())
                return fileOut;
            // он еще записывается
            return null;
        }

        String parQV;
        String parRV;
        if (image.length > 4000000) {
            parQV = "20";
            parRV = "10";
        } else if (image.length > 1500000) {
            parQV = "16";
            parRV = "12";
        } else if (image.length > 500000) {
            parQV = "14";
            parRV = "14";
        } else {
            parQV = "12";
            parRV = "15";
        }

        File fileIn = new File(path + "_in.mp4");
        try (FileOutputStream fos = new FileOutputStream(fileIn)) {
            fos.write(image);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        ProcessBuilder builder = new ProcessBuilder("makeVPreview.bat",
                fileIn.toPath().toString(), "-q:v " + parQV + " -r:v " + parRV, fileOut.toPath().toString());
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
