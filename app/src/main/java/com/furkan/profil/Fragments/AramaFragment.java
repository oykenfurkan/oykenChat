package com.furkan.profil.Fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.furkan.profil.Helpers.UsersChatAdapter;
import com.furkan.profil.Helpers.FriendManager;
import com.furkan.profil.Helpers.ProfilAdapter;
import com.furkan.profil.R;
import com.furkan.profil.Register.Login;
import com.furkan.profil.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AramaFragment extends Fragment {

    EditText arama;
    FirebaseUser user;
    User u;
    ArrayList<User> list = new ArrayList<>();
    ArrayList<String> mobileArray = new ArrayList<>();
    RecyclerView mUsersRecyclerView;
    View view;

    private String mCurrentUserUid;
    private List<String> mUsersKeyList;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserRefDatabase;
    private ChildEventListener mChildEventListener;
    private ProfilAdapter mUsersChatAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_arama, container, false);
        mUsersRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_users);

        setAuthInstance();
        setUsersDatabase();
        setUsersKeyList();
        setAuthListener();

        arama = (EditText) view.findViewById(R.id.editText);
        mUsersRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_users);
        setUserRecyclerView();

        user = mAuth.getCurrentUser();

        arama.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.equals("")) {
                    indir();
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    private void setAuthInstance() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setUsersDatabase() {
        mUserRefDatabase = FirebaseDatabase.getInstance().getReference("users");
    }

    private void setUserRecyclerView() {
        mUsersChatAdapter = new ProfilAdapter(getActivity(), new ArrayList<User>());
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUsersRecyclerView.setHasFixedSize(true);
        mUsersRecyclerView.setAdapter(mUsersChatAdapter);
    }

    private void setUsersKeyList() {
        mUsersKeyList = new ArrayList<String>();
    }

    private void setAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    setUserData(user);
                    queryAllUsers();
                } else {
                    // User is signed out
                    goToLogin();
                }
            }
        };
    }

    private void setUserData(FirebaseUser user) {
        mCurrentUserUid = user.getUid();
    }

    private void queryAllUsers() {
        mChildEventListener = getChildEventListener();
        mUserRefDatabase.limitToFirst(50).addChildEventListener(mChildEventListener);
    }

    private void goToLogin() {
        Intent intent = new Intent(getContext(), Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // LoginActivity is a New Task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // The old task when coming back to this activity should be cleared so we cannot come back to it.
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        clearCurrentUsers();

        if (mChildEventListener != null) {
            mUserRefDatabase.removeEventListener(mChildEventListener);
        }

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }

    private void clearCurrentUsers() {
        mUsersChatAdapter.clear();
        mUsersKeyList.clear();
    }

    private void logout() {
        setUserOffline();
        mAuth.signOut();
    }

    private void setUserOffline() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
       //     mUserRefDatabase.child(userId).child("connection").setValue(UsersChatAdapter.OFFLINE);
        }
    }

    private ChildEventListener getChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.exists()) {

                    String userUid = dataSnapshot.getKey();

                    if (!userUid.equals(mCurrentUserUid)) {
                        User recipient = dataSnapshot.getValue(User.class);
                        Log.i("user", recipient.getuID());
                        mUsersKeyList.add(userUid);
                        mUsersChatAdapter.refill(recipient);
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
               /* if (dataSnapshot.exists()) {
                    String userUid = dataSnapshot.getKey();
                    if (!userUid.equals(mCurrentUserUid)) {

                        User user = dataSnapshot.getValue(User.class);

                        int index = mUsersKeyList.indexOf(userUid);
                        if (index > -1) {
                            mUsersChatAdapter.changeUser(index, user);
                        }
                    }

                }*/
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
    }

    public void indir() {
        mUsersChatAdapter.clear();
        list.clear();
        mobileArray.clear();
        Query query = mUserRefDatabase.orderByChild("name")
                .startAt(arama.getText().toString())
                .endAt(arama.getText().toString() + "\uf8ff");


        ChildEventListener chi = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userUid = dataSnapshot.getKey();
                u = dataSnapshot.getValue(User.class);
                if (!user.getUid().equals(userUid)) {
                    mUsersKeyList.add(userUid);
                    mUsersChatAdapter.refill(u);
                }
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


    }

}
