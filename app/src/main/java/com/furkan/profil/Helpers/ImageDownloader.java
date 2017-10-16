package com.furkan.profil.Helpers;

import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

/**
 * Created by oyken on 6/22/2017.
 */

public class ImageDownloader extends AppCompatActivity {
    private StorageReference mStorageRef;
    static String appPath;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        // Create a reference of the image
        StorageReference imageRefer = storageRef.child("images/"+"pic.jpg");
        File localFile = null;

        String root ;
        root=Environment.getExternalStorageDirectory().toString();

        Log.i("root",root);
        File dir = new File(root , "Android/data/com.furkan.profil/files/images/");
        Log.i("dir",dir.toString());
        try {
            if(!dir.exists()) {

                dir.mkdirs();
                Log.i("dir",dir.toString());
            }
            localFile = File.createTempFile("pic",".jpg",dir);

        } catch (IOException e) {
            Toast.makeText(getBaseContext(),"Exception occurred ",Toast.LENGTH_LONG).show();

            e.printStackTrace();
        }
        if (localFile != null) {
            Toast.makeText(getBaseContext(),"Downloading to "+localFile.getAbsolutePath(),Toast.LENGTH_LONG).show();
            imageRefer.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getBaseContext(), "Downloaded !!", Toast.LENGTH_LONG).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getBaseContext(), "Failed !!" + exception, Toast.LENGTH_LONG).show();

                }
            });
    }
}}