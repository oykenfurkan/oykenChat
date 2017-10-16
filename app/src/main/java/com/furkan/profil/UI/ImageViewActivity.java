package com.furkan.profil.UI;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.furkan.profil.R;

import java.io.File;
import java.util.NoSuchElementException;

public class ImageViewActivity extends AppCompatActivity {

    ImageView imageview;
    String name, uID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);


        imageview=(ImageView)findViewById(R.id.imageView2);
        name=getIntent().getStringExtra("name");
        uID=getIntent().getStringExtra("uID");
        Log.i("name", name);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String completePath = Environment.getExternalStorageDirectory() + "/kucuk/" + uID  + ".jpg";

        File file = new File(completePath);
     // Log.i("Glide adresi", completePath);
        Uri imageUri = Uri.fromFile(file);
        Glide.with(this)
                .load(imageUri)
                .into(imageview);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
