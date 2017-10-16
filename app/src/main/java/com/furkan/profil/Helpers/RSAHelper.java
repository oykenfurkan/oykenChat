package com.furkan.profil.Helpers;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.security.KeyPairGeneratorSpec;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.furkan.profil.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

/**
 * Created by oyken on 5/24/2017.
 */

public class RSAHelper extends AppCompatActivity {
    static final String TAG = "SimpleKeystoreApp";
    static final String CIPHER_TYPE = "RSA/ECB/PKCS1Padding";
    static final String CIPHER_PROVIDER = "AndroidOpenSSL";
    Handler handle = null;
    Runnable runnable = null;
    EditText aliasText;
    EditText startText, decryptedText, encryptedText;
    List<String> keyAliases;
    ListView listView;
    KeyStore.Entry entry;
    static KeyStore keyStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new IllegalStateException("KeyStore could not be initialized", e);
        }
        refreshKeys();

        setContentView(R.layout.activity_main);

    }

    private void refreshKeys() {
        keyAliases = new ArrayList<>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                keyAliases.add(aliases.nextElement());
            }
        } catch (Exception e) {
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void createNewKeys(View view) {
        String alias = aliasText.getText().toString();
        try {
            // Create new key if needed
            if (!keyStore.containsAlias(alias)) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
                        .setAlias(alias)
                        .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);

                KeyPair keyPair = generator.generateKeyPair();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(e));
        }
        refreshKeys();
    }

    public void deleteKey(final String alias) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Delete Key")
                .setMessage("Do you want to delete the key \"" + alias + "\" from the keystore?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            keyStore.deleteEntry(alias);
                            refreshKeys();
                        } catch (KeyStoreException e) {
                            Toast.makeText(RSAHelper.this,
                                    "Exception " + e.getMessage() + " occured",
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    public static String encryptString(PublicKey publicK, String plain) {
        try {
          // KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
          // RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();
            RSAPublicKey publicKey = (RSAPublicKey) publicK;
            // HATA BURADAAA publickey convert olmuyor.
            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, inCipher);
            cipherOutputStream.write(plain.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte[] vals = outputStream.toByteArray();
            return (Base64.encodeToString(vals, Base64.DEFAULT));
        } catch (Exception e) {
//            Toast.makeText(null, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return "HATA";
    }

    public String decryptString(final String alias, String decrypted) {
        try {
     /*     KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();

            */
            try{
                keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
                throw new IllegalStateException("KeyStore could not be initialized", e);
            }


            entry = keyStore.getEntry(alias, null);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(decrypted, Base64.DEFAULT)), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i);
            }

            String finalText = new String(bytes, 0, bytes.length, "UTF-8");
            return finalText;

        } catch ( IOException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException  e) {
            e.printStackTrace();
            return "Hata";
        }

    }

}





/* EKSTRAS





 */