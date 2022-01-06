package org.erachain.utils;

import org.erachain.core.crypto.Base58;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;

/**
 * Быстрый ХЭШ больших файлов через буфер 1 МБ.
 * Используется только для создания хэшей их файлов как утилита для пользователя при создании ХЭША для JSON ExData - то есть тут вообще любой может быть алгоритм.
 * А для создания ХЭША на лету из вложенного в ExData файла используется прямой алгоритм. Поэтому важно их не путать: то что задает сам пользователь, даже с помощью этой утилиты - это его дело,
 * а то что внутри ноды файлы хэшироуются - это другое.
 * См. https://lab.erachain.org/erachain/Erachain/-/issues/1433
 */
public class FileHash {
    String partOfName;
    String partOfContent;
    String algorithm = "SHA-256";
    String hash = "";
    long offset = 0;
    private int buff;


    public FileHash(File file) {
        try {
            init(file, "SHA-256", 1048576);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public FileHash(File file, String algorithm) {
        try {
            init(file, algorithm, 1048576);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public FileHash(File file, String algorithm, int buff) {
        try {
            init(file, algorithm, buff);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String getHash() {
        return hash;
    }

    private void init(File file1, String algorithm, int buff) throws IOException {
        this.algorithm = algorithm;
        String file_name = file1.getPath();
        byte[] partialHash = null;
        this.buff = buff;

        if (!file1.exists() || file1.isDirectory()) {
            JOptionPane.showMessageDialog(null, Lang.T("File not exist"),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            RandomAccessFile file2 = new RandomAccessFile(file_name, "r");

            long startTime = System.nanoTime();
            MessageDigest hashSum = null;
            hashSum = MessageDigest.getInstance(algorithm);

            byte[] buffer = new byte[buff];

            long read = 0;

            // calculate the hash of the hole file for the test

            offset = file2.length();
            int unitsize;
            while (read < offset) {
                unitsize = (int) (((offset - read) >= buff) ? buff : (offset - read));
                file2.read(buffer, 0, unitsize);

                hashSum.update(buffer, 0, unitsize);

                read += unitsize;
            }

            file2.close();
            partialHash = new byte[hashSum.getDigestLength()];
            partialHash = hashSum.digest();

            long endTime = System.nanoTime();

            System.out.println(endTime - startTime);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
        }


        hash = Base58.encode(partialHash);
    }

    public int getBufferSize() {
        return buff;
    }
}