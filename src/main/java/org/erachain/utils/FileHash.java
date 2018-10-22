package org.erachain.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.erachain.core.crypto.Base58;


public class FileHash  {
    String partOfName;
    String partOfContent;
    String algorithm = "SHA-256";
    String hash ="";
    long offset = 0;
    private int buff;


    public FileHash (File file){
        try {
            init(file, "SHA-256", 1048576);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public FileHash (File file, String algorithm){
        try {
            init(file, algorithm, 1048576);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    public FileHash (File file, String algorithm, int buff )  {
        try {
            init(file, algorithm, buff);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
   
    }
    public String getHash (){
       return hash; 
    }
    private void init(File file1, String algorithm, int buff) throws IOException
    {this.algorithm = algorithm;
    String file_name = file1.getPath();
    byte[] partialHash = null;
    this.buff = buff;
  
       
        try {
            RandomAccessFile file2 = new RandomAccessFile(file_name, "r");

            long startTime = System.nanoTime();
            MessageDigest hashSum = null;
            try {
                hashSum = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            byte[] buffer = new byte[buff];
           

            long read = 0;

            // calculate the hash of the hole file for the test
            
            try {
                offset = file2.length();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            int unitsize;
            while (read < offset) {
                unitsize = (int) (((offset - read) >= buff) ? buff : (offset - read));
                try {
                    file2.read(buffer, 0, unitsize);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                hashSum.update(buffer, 0, unitsize);

                read += unitsize;
            }

            try {
                file2.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            partialHash = new byte[hashSum.getDigestLength()];
            partialHash = hashSum.digest();

            long endTime = System.nanoTime();

            System.out.println(endTime - startTime);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    
 
    hash = Base58.encode(partialHash);
    }
    public int getBufferSize(){
        return buff;
    }
}