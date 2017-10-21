package com.furkan.profil.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.furkan.profil.Helpers.MessageChatAdapter;
import com.furkan.profil.Models.ChatMessage;
import com.furkan.profil.R;
import com.furkan.profil.Helpers.RSAHelper;
import com.furkan.profil.Register.Register;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();

    @BindView(R.id.recycler_view_chat)
    RecyclerView mChatRecyclerView;
    @BindView(R.id.edit_text_message)
    EditText mUserMessageChatText;

    ImageView img;
    private FirebaseAuth mAuth;
    private static FirebaseUser user;
    private String mRecipientId;
    private String mCurrentUserId;

    private MessageChatAdapter messageChatAdapter;
    private DatabaseReference messageChatDatabase;
    private DatabaseReference keyDatabase;
    private ChildEventListener messageChatListener;
    public String receiverPubkey;
    public String senderPubkey;
    RSAPublicKey receiverPublicKey;
    RSAPublicKey senderPublicKey;
    RSAHelper rsa = new RSAHelper();
    TextView userName;
    String name = "TEST";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konusma);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_chats);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        img=(ImageView)findViewById(R.id.conversation_contact_photo);
        userName = (TextView) findViewById(R.id.action_bar_title_1);

        bindButterKnife();
        setDatabaseInstances();
        setFirebaseUser();
        userName.setText(name);
        setPicture();
        getReceiverPublicKey();
        getSenderPublicKey();
        setDatabaseName();
        setChatRecyclerView();
        queryMessages();

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(ChatActivity.this, ProfilGoster.class);
                intent.putExtra("uID", mRecipientId);
                intent.putExtra("name", name);
                startActivity(intent);
                finish();
            }
        });

    }

    private void setPicture() {
        String completePath = Environment.getExternalStorageDirectory() + "/kucuk/" + mRecipientId +".jpg";
        File file = new File(completePath);
        Log.i("Glide adresi", completePath);
        Uri imageUri = Uri.fromFile(file);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this)
                .load(imageUri)
                .into(img);

    }

    private void setFirebaseUser() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(ChatActivity.this, Register.class));
            finish();
        }
        mRecipientId = getIntent().getStringExtra("Receiver");
        mCurrentUserId = user.getUid();
        name = getIntent().getStringExtra("Name");
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

    public void setDatabaseInstances(){
        messageChatDatabase = FirebaseDatabase.getInstance().getReference("chats");
        keyDatabase = FirebaseDatabase.getInstance().getReference("publicKeys");
    }

    private void setDatabaseName() {
        int rec = 0;
        int sen = 0;
        for (int i = 0; i < mCurrentUserId.length(); i++) {
            sen = sen + mCurrentUserId.codePointAt(i);
        }
        for (int i = 0; i < mRecipientId.length(); i++) {
            rec = rec + mRecipientId.codePointAt(i);
        }
        if (sen > rec) {
            messageChatDatabase = messageChatDatabase.child(mRecipientId + "-" + mCurrentUserId);
        } else {
            messageChatDatabase = messageChatDatabase.child(mCurrentUserId + "-" + mRecipientId);
        }
    }

    private void getReceiverPublicKey() {

        keyDatabase.child(mRecipientId).child("pubkey").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                receiverPubkey = (String) snapshot.getValue();
                strtoPub1(receiverPubkey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getSenderPublicKey() {

        keyDatabase.child(mCurrentUserId).child("pubkey").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                senderPubkey = (String) snapshot.getValue();
                strtoPub2(senderPubkey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void strtoPub1(String pubkey) {

        try {
            //Create the PublicKey object from the String encoded in Base64.
            Log.i("Test", pubkey);
            byte[] publicBytes = Base64.decode(pubkey, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            receiverPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public void strtoPub2(String pubkey) {

        try {
            //Create the PublicKey object from the String encoded in Base64.
            Log.i("Test", pubkey);
            byte[] publicBytes = Base64.decode(pubkey, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            senderPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }

    private void setChatRecyclerView() {
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChatRecyclerView.setHasFixedSize(true);
        messageChatAdapter = new MessageChatAdapter(new ArrayList<ChatMessage>());
        mChatRecyclerView.setAdapter(messageChatAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (messageChatListener != null) {
            messageChatDatabase.removeEventListener(messageChatListener);
        }
        messageChatAdapter.cleanUp();
     //   setUserOffline();
    }

    private void setUserOffline() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
       //     mUserRefDatabase.child(userId).child("connection").setValue(UsersChatAdapter.OFFLINE);
        }
    }

    @OnClick(R.id.btn_send_message)
    public void btnSendMsgListener(View sendButton) {

        String encrypted;
        String sencrypted;
        String senderMessage = mUserMessageChatText.getText().toString().trim();


        if (!senderMessage.isEmpty()) {

            encrypted = rsa.encryptString(receiverPublicKey, senderMessage);
            sencrypted = rsa.encryptString(senderPublicKey, senderMessage);
            ChatMessage newMessage = new ChatMessage(encrypted, sencrypted, mCurrentUserId, mRecipientId, System.currentTimeMillis());
            messageChatDatabase.push().setValue(newMessage);
            mUserMessageChatText.setText("");
        }
    }

    public void queryMessages(){
        messageChatListener = messageChatDatabase.limitToFirst(50).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {


                ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                if (newMessage != null) {
                    Log.i("Test",  messageChatDatabase.toString());
                    Log.i("newMessage içeriği", newMessage.getRecipient()+"///"+ newMessage.getSender());
                    //newMessage.setMessage(rsa.decryptString(user.getUid(),newMessage+.getMessage()));

                    if (newMessage.getSender().equals(mCurrentUserId)) {
                        newMessage.setMessage(rsa.decryptString(mCurrentUserId, newMessage.getsMessage()));
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.SENDER);
                    } else {
                        newMessage.setMessage(rsa.decryptString(user.getUid(), newMessage.getMessage()));
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.RECIPIENT);
                    }
                    messageChatAdapter.refillAdapter(newMessage,dataSnapshot.getKey());
                    mChatRecyclerView.scrollToPosition(messageChatAdapter.getItemCount() - 1);

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
}