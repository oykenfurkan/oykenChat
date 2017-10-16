package com.furkan.profil.UI;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.furkan.profil.Fragments.ListeFragment;
import com.furkan.profil.Models.User;
import com.furkan.profil.R;
import com.furkan.profil.Register.Login;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class PpUpdate extends AppCompatActivity {
    EditText name, email, cell, workphone, address, job;
    Button btn;
    ImageView imageview;
    FirebaseUser currentUser;
    FirebaseStorage storage;
    StorageReference storageRef, riversRef;
    DatabaseReference mDatabase;
    String mCurrentuID;
    User user;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pp_update);

        name = (EditText) findViewById(R.id.editText);
        email = (EditText) findViewById(R.id.editText2);
        cell = (EditText) findViewById(R.id.editText3);
        workphone = (EditText) findViewById(R.id.editText4);
        address = (EditText) findViewById(R.id.editText5);
        job = (EditText) findViewById(R.id.editText6);
        btn = (Button) findViewById(R.id.button);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(ProfilDuzenle.this, Login.class));
            finish();
        }else{
            currentUser=FirebaseAuth.getInstance().getCurrentUser();
            mCurrentuID=currentUser.getUid();
        }

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

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });
    }

    public void update(){
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

    }

}
