package com.furkan.profil.UI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.furkan.profil.Models.User;
import com.furkan.profil.R;
import com.furkan.profil.Register.Login;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditProfile extends AppCompatActivity {
    EditText name, email, cell, workphone, address, job;
    Button btn;
    FirebaseUser currentUser;
    DatabaseReference mDatabase;
    String mCurrentuID;
    User user;
    ImageView imageview;
    ImageButton imagebutton;
    String imageurl;
    int i;
    Uri selectedImageUri;
    ProgressDialog progressDialog;
    StorageReference riversRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        name = (EditText) findViewById(R.id.editText);
        email = (EditText) findViewById(R.id.editText2);
        cell = (EditText) findViewById(R.id.editText3);
        workphone = (EditText) findViewById(R.id.editText4);
        address = (EditText) findViewById(R.id.editText5);
        job = (EditText) findViewById(R.id.editText6);
        btn = (Button) findViewById(R.id.button);
        imageview = (ImageView) findViewById(R.id.imageView);
        imagebutton = (ImageButton) findViewById(R.id.imageButton);
        btn = (Button) findViewById(R.id.button);
        progressDialog = new ProgressDialog(this);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(EditProfile.this, Login.class));
            finish();
        } else {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            mCurrentuID = currentUser.getUid();
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        riversRef = storageReference.child("images/" + mCurrentuID + "/pic.jpg");

        String completePath = Environment.getExternalStorageDirectory() + "/kucuk/" + mCurrentuID + ".jpg";

        File file = new File(completePath);
        // Log.i("Glide adresi", completePath);
        Uri imageUri = Uri.fromFile(file);
        Glide.with(this)
                .load(imageUri)
                .into(imageview);


        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        Query query = mDatabase.orderByChild("uID").equalTo(mCurrentuID);
        ChildEventListener chi = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                user = dataSnapshot.getValue(User.class);
                name.setText(user.name);
                cell.setText(user.cell);
                workphone.setText(user.workphone);
                job.setText(user.job);
                address.setText(user.address);
                email.setText(user.email);
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
        query.addChildEventListener(chi);


        imagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_LONG);
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    update();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void update() throws IOException {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser userr = FirebaseAuth.getInstance().getCurrentUser();
        // Creating new user node, which returns the unique key value
        // new user node would be /users/$userid/
        uploadFile();
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


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == 100) {
                    // Get the url from data
                    selectedImageUri = data.getData();
                    if (null != selectedImageUri) {
                        // Set the image in ImageView
                        imageview.setImageURI(selectedImageUri);
                        imageurl = selectedImageUri.toString();

                    }

                }
            }
        } catch (Exception e) {
            Log.d("test", e.getMessage());
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void uploadFile() throws IOException {
        //if there is a file to upload

        if (selectedImageUri != null) {
            //displaying a progress dialog while upload is going on
            savePicture();
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            riversRef.putFile(selectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();
                            startActivity(new Intent(EditProfile.this, AnaEkran.class));
                            finish();
                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });

        }
    }

    public void savePicture() throws IOException {
        imageview.setDrawingCacheEnabled(true);
        imageview.buildDrawingCache();
        Bitmap bitmap = imageview.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final File rootPath = new File(Environment.getExternalStorageDirectory(), "/kucuk");
        if (!rootPath.exists()) {
            rootPath.mkdir();
        }

        // Use the bytes to display the image
        String path = rootPath.getPath();
        try {
            FileOutputStream fos = new FileOutputStream(path + "/" + id + ".jpg");
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

