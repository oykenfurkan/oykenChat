package com.furkan.profil.Helpers;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by oyken on 5/16/2017.
 */

public class FriendManager {

    DatabaseReference dFriends, dRequests, dNotifications, dUsers;
    FirebaseUser user;
    Boolean ret;
    String cuID;
    String cuName;
    private static String freq = "friendRequest";

    public FriendManager() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        dUsers = FirebaseDatabase.getInstance().getReference("users");
        dFriends = FirebaseDatabase.getInstance().getReference("friends");
        dNotifications = FirebaseDatabase.getInstance().getReference("notifications/friendRequestAccepted");
        dRequests = FirebaseDatabase.getInstance().getReference("requests/friendRequest");
        cuID=user.getUid();
        dUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cuName = (String) dataSnapshot.child(cuID).child("name").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    public void sendFriendRequest(String user2,String uName) {
        dRequests.child(user2).child(cuID).setValue(uName);
    }

    public void rejectFriendRequest(String receiver,String sender) {
        dRequests.child(receiver).child(sender).removeValue();
    }

    public void addFriend(String user2) {
        dFriends.child(cuID).child(user2).setValue(true);
        dFriends.child(user2).child(cuID).setValue(true);
        rejectFriendRequest(cuID,user2);
        addNotification(user2);
    }

    public void addNotification(String userID){
        dNotifications.child(userID).child(cuID).setValue(cuName);
    }
    public void removeNotification(String userID){
        dNotifications.child(cuID).child(userID).removeValue();
    }

    public void checkFriend(final String uID, final CheckFriendSuccessListener onCheckFriendSuccessListener) {
        dFriends.child(cuID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(uID)) {
                    //   Log.i("Database", dFriends.toString()+" +++ "+uID+" ++++ ");
                    ret = (Boolean) snapshot.child(uID).getValue();
                    onCheckFriendSuccessListener.onSuccess(ret);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void prepareList(final CheckFriendRequestListener onCheckFriendRequestListener) {

        dRequests.child(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                        String userUid = dataSnapshot.getKey();
                        Log.i("Database", userUid+ dataSnapshot.getValue().toString());
                    onCheckFriendRequestListener.onSuccess(userUid, dataSnapshot.getValue().toString(),0);
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
        });

        dNotifications.child(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    String userUid = dataSnapshot.getKey();
                    Log.i("Database", userUid+ dataSnapshot.getValue().toString());
                    onCheckFriendRequestListener.onSuccess(userUid, dataSnapshot.getValue().toString(),1);
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
        });

    }

    public interface CheckFriendSuccessListener {
        void onSuccess(boolean ret);
    }

    public interface CheckFriendRequestListener {
        void onSuccess(String uID, String name, int type);
    }
}
