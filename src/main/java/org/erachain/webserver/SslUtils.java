package org.erachain.webserver;

import org.erachain.settings.Settings;
import org.mapdb.Fun;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;

public class SslUtils { // get web keystore
    public static Fun.Tuple2<KeyStore, String> GetWebKeystore(String keyStorePath, String keyStorePassword, String AliasPassword) throws FileNotFoundException {

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("JKS");//"PKCS12");
        } catch (KeyStoreException e) {
            e.printStackTrace();
            return new Fun.Tuple2<KeyStore, String>(null, e.getMessage());
        }
        InputStream keyStoreData;
        try {
             keyStoreData = new FileInputStream(keyStorePath);
        } catch (IOException e) {
            e.printStackTrace();
            return new Fun.Tuple2<KeyStore, String>(null, e.getMessage());
        }
        try {
            keyStore.load(keyStoreData, keyStorePassword.toCharArray());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new Fun.Tuple2<KeyStore, String>(null, e.getMessage());
        } catch (CertificateException e) {
            e.printStackTrace();
            return new Fun.Tuple2<KeyStore, String>(null, e.getMessage());
        } catch (IOException e) {
        e.printStackTrace();
        return new Fun.Tuple2<KeyStore, String>(null, e.getMessage());
    }
    // confirm password source
        Enumeration<String> result;
    try {
            result = keyStore.aliases();
        } catch (KeyStoreException e) {
            e.printStackTrace();
            return new Fun.Tuple2<KeyStore, String>(null, e.getMessage());
        }
        Key key =null;
        while ( result.hasMoreElements()){
            String ss = result.nextElement();
            try {
                key = keyStore.getKey(ss, AliasPassword.toCharArray());
            } catch (KeyStoreException e) {
                e.printStackTrace();
                key=null;
                continue;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                key=null;
                continue;
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
                key=null;
                continue;
            }
        }
       if(key == null)  return new Fun.Tuple2<KeyStore, String>(null, "Invalid password Source or Source not found");


        return new Fun.Tuple2<KeyStore, String>(keyStore, "Ok");
    }
}