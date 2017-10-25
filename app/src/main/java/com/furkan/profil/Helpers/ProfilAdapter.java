package com.furkan.profil.Helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.furkan.profil.UI.AnaEkran;
import com.furkan.profil.UI.NonFriendProfile;
import com.furkan.profil.UI.ProfilGoster;
import com.furkan.profil.R;
import com.furkan.profil.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ProfilAdapter extends RecyclerView.Adapter<ProfilAdapter.ViewHolderUsers> {

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    private List<User> mUsers;
    private Context mContext;
    private String mCurrentUserId;
    private int sendNum;
    public static Bus bus;

    public ProfilAdapter(Context context, List<User> fireChatUsers) {
        mUsers = fireChatUsers;
        mContext = context;
    }

    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderUsers(mContext, LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderUsers holder, int position) {

        User fireChatUser = mUsers.get(position);
        try {
            holder.getUserAvatar(mUsers.get(position).getuID());
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.getUserDisplayName().setText(fireChatUser.getName());
        holder.getStatusConnection().setText(fireChatUser.getConnection());

        if (fireChatUser.getConnection().equals(ONLINE)) {
            holder.getStatusConnection().setTextColor(Color.parseColor("#00FF00"));
        } else {
            holder.getStatusConnection().setTextColor(Color.parseColor("#FF0000"));
        }

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void refill(final User users) {

        final FriendManager f = new FriendManager();
        mUsers.add(users);
        notifyDataSetChanged();
        f.checkFriend(users.getuID(), new FriendManager.CheckFriendSuccessListener() {
            @Override
            public void onSuccess(boolean value) {
                mUsers.remove(users);
                notifyDataSetChanged();

            }


        });

    }

    public void changeUser(int index, User user) {
        mUsers.set(index, user);
        notifyDataSetChanged();
    }

    public void setCurrentUserInfo(String userUid, int number) {
        mCurrentUserId = userUid;
        sendNum = number;
        //mCurrentUserCreatedAt = createdAt;
    }

    public void clear() {
        mUsers.clear();
    }

    public class ViewHolderUsers extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mUserAvatar;
        private TextView mUserDisplayName;
        private TextView mStatusConnection;
        private Context mContextViewHolder;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference riversRef = storageRef.child("images/" + "pic.jpg");


        public ViewHolderUsers(Context context, View itemView) {
            super(itemView);
            mUserAvatar = (ImageView) itemView.findViewById(R.id.img_avatar);
            mUserDisplayName = (TextView) itemView.findViewById(R.id.text_view_display_name);
            mStatusConnection = (TextView) itemView.findViewById(R.id.text_view_connection_status);
            mContextViewHolder = context;
            bus = new Bus();
            bus.register(this);
            itemView.setOnClickListener(this);
        }


        @Subscribe
        public void setUserAvatar(String id) {
            String completePath = Environment.getExternalStorageDirectory() + "/kucuk/" + id + ".jpg";

            File file = new File(completePath);
            Log.i("Glide adresi", completePath);
            Uri imageUri = Uri.fromFile(file);
            Glide.with(mContext)
                    .load(imageUri)
                    .into(mUserAvatar);
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
                            bus.post(id);
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
            } else {
                Log.i("helal", "süper");
                bus.post(id);
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


        public TextView getUserDisplayName() {
            return mUserDisplayName;
        }

        public TextView getStatusConnection() {
            return mStatusConnection;
        }

        @Override
        public void onClick(View view) {
            final User user = mUsers.get(getLayoutPosition());

            final Intent[] chatIntent = {new Intent(mContextViewHolder, NonFriendProfile.class)};
            final FriendManager f = new FriendManager();
            chatIntent[0].putExtra("uID", user.getuID());

            f.checkFriend(user.getuID(), new FriendManager.CheckFriendSuccessListener() {
                @Override
                public void onSuccess(boolean value) {
                    chatIntent[0] = new Intent(mContextViewHolder, ProfilGoster.class);
                    mContextViewHolder.startActivity(chatIntent[0]);
                }
            });

            mContextViewHolder.startActivity(chatIntent[0]);
        }
    }

}
