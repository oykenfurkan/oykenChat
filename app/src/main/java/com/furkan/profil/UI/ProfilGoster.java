package com.furkan.profil.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.furkan.profil.R;
import com.furkan.profil.Register.Register;
import com.furkan.profil.Register.Uyelik;
import com.furkan.profil.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfilGoster extends AppCompatActivity {

    private boolean isImageFitToScreen = false;
    TextView name, cell, workphone, address, mail, job;
    Button btn;
    User u;
    ImageView imageview;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference mDatabase;
    String uID;
    FloatingActionButton fab;
    StorageReference riversRef, storageRef;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_goster);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("name"));

        final Intent intent = getIntent();
        name = (TextView) findViewById(R.id.tvNumber1);
        cell = (TextView) findViewById(R.id.tvNumber2);
        workphone = (TextView) findViewById(R.id.tvNumber3);
        address = (TextView) findViewById(R.id.tvNumber4);
        mail = (TextView) findViewById(R.id.tvNumber5);
        job = (TextView) findViewById(R.id.tvNumber6);
        btn = (Button) findViewById(R.id.btn);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        uID=intent.getStringExtra("uID");

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        riversRef = storageRef.child("images/" + uID + "/pic.jpg");

        imageview = (ImageView) findViewById(R.id.imageView);
        if (!imageExist(intent.getStringExtra("uID"))) {
            try {
                getUserAvatar(intent.getStringExtra("uID"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String completePath = Environment.getExternalStorageDirectory() + "/kucuk/" + intent.getStringExtra("uID") + ".jpg";

        File file = new File(completePath);
        Log.i("Glide adresi", completePath);
        Uri imageUri = Uri.fromFile(file);
        imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this)
                .load(imageUri)
                .into(imageview);
        /////                                                       PROFİL FOTOĞRAFI GÖRÜNTÜLEME


        auth = FirebaseAuth.getInstance();




        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfilGoster.this, Uyelik.class));
            }
        });

        final Intent intent2 = new Intent(this, ChatActivity.class);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Burası Konuşma Ekranına Gidecek", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(intent2);

            }
        });

        imageview.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             Intent intent3=new Intent(ProfilGoster.this, ImageViewActivity.class);
                                             intent3.putExtra("name", getIntent().getStringExtra("name"));
                                             intent3.putExtra("uID", uID);
                                             startActivity(intent3);
                                         }
                                     });


                ////////////////////////////////////////////////////////RETRIEVING DATA

        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        String id = intent.getStringExtra("id");
        Query query = mDatabase.orderByChild("uID").equalTo(intent.getStringExtra("uID"));
        Query query2 = mDatabase.orderByChild("uID").equalTo(user.getUid());
        Log.i("helloo helloo", intent.getStringExtra("uID") + "");

        ChildEventListener chi = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                u = dataSnapshot.getValue(User.class);
                name.setText(u.name);
                cell.setText(u.cell);
                workphone.setText(u.workphone);
                job.setText(u.job);
                address.setText(u.address);
                mail.setText(u.email);
                Log.i("helloo helloo", u.address + u.cell + u.name + "");
                intent2.putExtra("Receiver", u.getuID());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        //  mDatabase.addChildEventListener(chi);
        query.addChildEventListener(chi);
        ////////////////////////////////////////////////////////RETRIEVING DATA-END

        ChildEventListener chi2 = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                u = dataSnapshot.getValue(User.class);
                intent2.putExtra("Sender", u.getuID());
                Log.i("helloo helloo", u.address + u.cell + u.name + "");

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        //  mDatabase.addChildEventListener(chi);
        query2.addChildEventListener(chi2);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            final Intent intent = new Intent(this, AnaEkran.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void getUserAvatar(final String id) throws IOException {
        riversRef = storageRef.child("images/" + id + "/pic.jpg");
        Log.i("TestID", riversRef.toString());
        if (!imageExist(id)) {
            final File rootPath = new File(Environment.getExternalStorageDirectory(), "/kucuk");
            Log.i("Adres", rootPath.toString());
            if (!rootPath.exists()) {
                rootPath.mkdir();
            }
            riversRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Use the bytes to display the image
                    String path = rootPath.getPath();
                    Log.i("helal", path);
                    try {
                        FileOutputStream fos = new FileOutputStream(path + "/" + id + ".jpg");
                        fos.write(bytes);
                        fos.close();
                        Log.i("helal", "süper");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.i("helal", e.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i("helal", e.toString());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
    }

    public boolean imageExist(String id) {
        final File rootPath = new File(Environment.getExternalStorageDirectory() + "/kucuk", "/" + id + ".jpg");
        if (rootPath.exists()) {
            return true;
        } else {
            return false;
        }
    }


}

