package com.furkan.profil.Fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.furkan.profil.Helpers.UsersChatAdapter;
import com.furkan.profil.Helpers.FriendManager;
import com.furkan.profil.Register.Login;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Optional;

public class FriendListFragment extends Fragment {

    FloatingActionButton fab;
    RecyclerView mUsersRecyclerView;
    FriendManager f;
    Toolbar toolbar;
    View view;
    User u;
    FirebaseUser user;
    private String mCurrentUserUid;
    private List<String> mUsersKeyList;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mFriendRefDatabase, mUserRefDatabase;
    private ChildEventListener mChildEventListener;
    private UsersChatAdapter mUsersChatAdapter;

    @Optional
    private void bindButterKnife() {
        ButterKnife.bind(view);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        activity.getSupportActionBar().setTitle("Arkadaş Listesi");
        toolbar.inflateMenu(R.menu.menu_main);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        mUsersRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_users);
        bindButterKnife();
        setAuthInstance();
        setUserData();
        setAuthListener();
        setUsersDatabase();
        setUserRecyclerView();
        setUsersKeyList();
        f = new FriendManager();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_log_in, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.miProfile) {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return true;
        }
        if (id == android.R.id.home) {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                if (ft != null) {
                    ft.replace(R.id.content, new ListeFragment());
                    ft.commit();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void setAuthInstance() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setUsersDatabase() {
        mUserRefDatabase = FirebaseDatabase.getInstance().getReference("users");
        mFriendRefDatabase = FirebaseDatabase.getInstance().getReference("friends/" + mCurrentUserUid);
        Log.i("mFriendRefDatabase", mFriendRefDatabase.toString());
    }

    private void setUserRecyclerView() {
        mUsersChatAdapter = new UsersChatAdapter(getActivity(), new ArrayList<User>());
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
                    queryAllUsers();
                } else {
                    // User is signed out
                    goToLogin();
                }
            }
        };
    }

    private void setUserData() {
        mCurrentUserUid = mAuth.getCurrentUser().getUid();
    }

    private void queryAllUsers() {
        mChildEventListener = getChildEventListener();
        mFriendRefDatabase.limitToFirst(50).addChildEventListener(mChildEventListener);
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
        setUserOffline();
        clearCurrentUsers();

        if (mChildEventListener != null) {
            mFriendRefDatabase.removeEventListener(mChildEventListener);
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
   //         mFriendRefDatabase.child(userId).child("connection").setValue(UsersChatAdapter.OFFLINE);
        }
    }

    private ChildEventListener getChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.exists()) {
                    String recipient = dataSnapshot.getKey();
                    mUsersKeyList.add(recipient);
                    Log.i("Arkadaşları", recipient);
                    indir(recipient);
                  //  mUsersChatAdapter.refill(recipient);
                }
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    String userUid = dataSnapshot.getKey();
                    if (!userUid.equals(mCurrentUserUid)) {

                        User user = dataSnapshot.getValue(User.class);

                        int index = mUsersKeyList.indexOf(userUid);
                        if (index > -1) {
                            mUsersChatAdapter.changeUser(index, user);
                        }
                    }

                }
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


    public void indir(String uID) {
        mUsersChatAdapter.clear();
        Query query = mUserRefDatabase.orderByChild("uID").equalTo(uID);
        user=mAuth.getCurrentUser();

        ChildEventListener chi = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userUid = dataSnapshot.getKey();

                u = dataSnapshot.getValue(User.class);
                if (!user.getUid().equals(userUid)) {
                 //   mUsersKeyList.add(userUid);
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
