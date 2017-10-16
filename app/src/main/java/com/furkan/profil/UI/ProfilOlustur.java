package com.furkan.profil.UI;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.furkan.profil.R;
import com.furkan.profil.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.Calendar;
import javax.security.auth.x500.X500Principal;

public class ProfilOlustur extends AppCompatActivity {
    KeyStore keyStore;
    EditText name, email, cell, workphone, address, job;
    Button next;
    User u;
    String TAG = "PRIVATEKEY UPLOAD";
    private static String publicKey = "";
    private static String privateKey = "";
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_olustur);

        name = (EditText) findViewById(R.id.editText);
        email = (EditText) findViewById(R.id.editText2);
        cell = (EditText) findViewById(R.id.editText3);
        workphone = (EditText) findViewById(R.id.editText4);
        address = (EditText) findViewById(R.id.editText5);
        job = (EditText) findViewById(R.id.editText6);
        next = (Button) findViewById(R.id.button2);


        try{
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (Exception ignored) {
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealWithKeys();
                startActivity(new Intent(ProfilOlustur.this, UploadPicture.class));
                finish();
            }
        });

    }


    public void yaz() {
        if (name.getText() != null) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
            FirebaseUser userr = FirebaseAuth.getInstance().getCurrentUser();
            // Creating new user node, which returns the unique key value
            // new user node would be /users/$userid/

            String uID = userr.getUid();
            i++;
            // creating user object
            User user = new User(name.getText().toString(),
                    email.getText().toString(),
                    cell.getText().toString(),
                    workphone.getText().toString(),
                    address.getText().toString(),
                    job.getText().toString(),
                    uID,
                    "online",
                    i);

            // pushing user to 'users' node using the userId
            mDatabase.child(uID).setValue(user);
        } else {
            Snackbar.make(getCurrentFocus(), "Burası Konuşma Ekranına Gidecek", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void dealWithKeys() {
        new requestTask().execute();
    }

    public class requestTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("publicKeys");
            FirebaseUser userr = FirebaseAuth.getInstance().getCurrentUser();
            String uID = userr.getUid();
            String alias = uID;

            try {
                // Create new key if needed
                if (!keyStore.containsAlias(alias)) {
                    Calendar start = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    end.add(Calendar.YEAR, 25);
                    KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(getApplicationContext())
                            .setAlias(alias)
                            .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
                            .setSerialNumber(BigInteger.ONE)
                            .setStartDate(start.getTime())
                            .setEndDate(end.getTime())
                            .build();
                    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                    generator.initialize(spec);

                    KeyPair keyPair = generator.generateKeyPair();
                    byte[] publicKeyBytes = Base64.encode(keyPair.getPublic().getEncoded(),0);
                    String pubKey = new String(publicKeyBytes);
                    mDatabase.child(uID).child("pubkey").setValue(pubKey);
                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            yaz();
            return;
        }
    }
}
