package org.erachain.webserver;

import org.erachain.core.BlockChain;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.library.ImagesTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

public class PreviewMaker {

    public String errorMess;

    static final int VIDEO_USE_ORIG_LEN = 1 << 17;
    static final int IMAGE_USE_ORIG_LEN = 1 << 16;

    static Logger LOGGER = LoggerFactory.getLogger(PreviewMaker.class.getSimpleName());

    public static boolean notNeedPreview(ItemCls item, byte[] image) {

        // так как даже маленькие картинки будут обрамлены в теге ВИДЕО на сайте то все IMG
        // и только большие ВИДЕО
        return item.getImageType() == AssetCls.MEDIA_TYPE_AUDIO
                || item.getImageType() == AssetCls.MEDIA_TYPE_VIDEO && image.length < VIDEO_USE_ORIG_LEN
                || item.getImageType() == AssetCls.MEDIA_TYPE_IMG && image.length < IMAGE_USE_ORIG_LEN;

    }

    public byte[] getPreview(ItemCls item, byte[] image) {

        if (notNeedPreview(item, image))
            return image;

        try {
            File file = makePreview(item, image);
            if (file == null)
                return null;
            if (file.canRead())
                return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            errorMess = e.getMessage();
        }

        return null;
    }

    public static String getItemName(ItemCls item) {
        return item.getItemTypeName() + item.getKey();
    }

    /**
     * Convert all media (JPG, GIF, animated-GIF, MPEG etc. to MP4
     *
     * @param item
     * @param image
     * @return
     */
    public File makePreview(ItemCls item, byte[] image) {

        if (notNeedPreview(item, image))
            return null;

        String outputName = getItemName(item);
        String mainFolder = "dataPreviews" + (BlockChain.DEMO_MODE ? "_demo" : BlockChain.TEST_MODE ? "_test" : "");
        String path = mainFolder + File.separator + outputName;
        String pathIn = mainFolder + File.separator + "orig" + File.separator + outputName;
        String mediaExt = item.getImageTypeExt();
        File fileOut = new File(path + "." + mediaExt);

        fileOut.getParentFile().mkdirs();

        if (fileOut.exists()) {
            if (fileOut.canRead())
                return fileOut;
            // он еще записывается
            return null;
        }

        String output = pathIn + ".log";
        File outLog = new File(output);
        outLog.getParentFile().mkdirs();

        if (!outLog.exists()) {
            // иначе некоторые картинки дают сбой при конвертации в ffmpeg
            pathIn += "." + mediaExt;
            File fileIn = new File(pathIn);
            try (FileOutputStream fos = new FileOutputStream(fileIn)) {
                fos.write(image);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                errorMess = e.getMessage();
                return null;
            }

            ImageIcon imageIcon = null;
            if (false) {
                try {
                    imageIcon = new ImageIcon(new URL("file", "", pathIn));
                } catch (MalformedURLException e) {
                    LOGGER.error(e.getMessage(), e);
                    errorMess = e.getMessage();
                }
            } else {
                imageIcon = new ImageIcon(pathIn);
            }

            BufferedImage bufferedImage = ImagesTools.imageToBufferedImage(imageIcon.getImage());
            int imageSize;
            try {
                byte[] imageBytes = ImagesTools.bufferedImageToBytes(bufferedImage, mediaExt);
                imageSize = imageBytes.length;
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                errorMess = e.getMessage();
                return null;
            }
            float coefficient = (float) Math.sqrt((double) IMAGE_USE_ORIG_LEN / (double) imageSize);
            int width = (int) (imageIcon.getIconWidth() * coefficient);
            int height = (int) (imageIcon.getIconHeight() * coefficient);

            try {
                switch (mediaExt) {
                    case "mp3":
                        return null;
                    case "jpg":
                        imageIcon.setImage(imageIcon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
                        bufferedImage = ImagesTools.imageToBufferedImage(imageIcon.getImage());
                        try {
                            ImageIO.write(bufferedImage, "jpg", fileOut);
                            return fileOut;
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage(), e);
                            errorMess = e.getMessage();
                            return null;
                        }

                    case "mp4":
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

                        boolean isWindows = System.getProperty("os.name").startsWith("Windows");
                        ProcessBuilder builder;
                        // -i %1 -y -fs 512k -vcodec h264 -s 256x256 -q:v %2 -r:v %3 %4
                        if (isWindows) {
                            builder = new ProcessBuilder("makePreview.bat",
                                    fileIn.toPath().toString(),
                                    parQV, parRV,
                                    fileOut.toPath().toString());
                        } else {
                            // в Unix через bash makePreview.bash - вызывает ошибку "Unable to find a suitable output format for"
                            // - последний параметр как-то криво передается
                            // -vf scale=256:-2,setsar=1:1
                            builder = new ProcessBuilder("ffmpeg",
                                    "-i", fileIn.toPath().toString(),
                                    "-y", "-fs", "512k",
                                    "-pix_fmt", "yuv420p", // for FireFox (neg error NS_ERROR_DOM_MEDIA_FATAL_ERR see https://github.com/ccrisan/motioneye/issues/1067
                                    "-vcodec", "h264", "-an",
                                    "-vf", "scale=256:-2,setsar=1:1", // "-s", "256x256",
                                    "-q:v", parQV, "-r:v", parRV, fileOut.toPath().toString()
                            );
                        }
                        // указываем перенаправление stderr в stdout, чтобы проще было отлаживать
                        builder.redirectErrorStream(true);

                        builder.redirectOutput(outLog);
                        try {
                            Process process = builder.start();
                            process.waitFor();
                            return fileOut;
                        } catch (IOException | InterruptedException e) {
                            LOGGER.error(e.getMessage(), e);
                            errorMess = e.getMessage();
                            return null;
                        }
                }
            } finally {
                if (fileOut.exists()) {
                    fileIn.delete();
                }

            }
        }

        // some errors was happen
        return null;

    }

}
