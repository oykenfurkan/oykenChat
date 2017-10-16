package com.furkan.profil.Helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.furkan.profil.UI.ChatActivity;
import com.furkan.profil.R;
import com.furkan.profil.Models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.List;

public class UsersChatAdapter extends RecyclerView.Adapter<UsersChatAdapter.ViewHolderUsers> {

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    private List<User> mUsers;
    private Context mContext;
    private String mCurrentUserId;

    public UsersChatAdapter(Context context, List<User> fireChatUsers) {
        mUsers = fireChatUsers;
        mContext = context;
        mCurrentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderUsers(mContext, LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderUsers holder, int position) {

        User fireChatUser = mUsers.get(position);

   //     int userAvatarId= R.mipmap.ic_avatar_blue;//ChatHelper.getDrawableAvatarId(fireChatUser.getAvatarId()); // AVATAR GLIDE MUHABBETİ
  //      Drawable avatarDrawable = ContextCompat.getDrawable(mContext,userAvatarId);
        holder.getUserAvatar();
        holder.getUserDisplayName().setText(fireChatUser.getName());
        holder.getStatusConnection().setText(fireChatUser.getConnection());

        if(fireChatUser.getConnection().equals(ONLINE)) {

            holder.getStatusConnection().setTextColor(Color.parseColor("#00FF00"));
        }else {

            holder.getStatusConnection().setTextColor(Color.parseColor("#FF0000"));
        }

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void refill(User users) {
        mUsers.add(users);
        notifyDataSetChanged();
    }

    public void changeUser(int index, User user) {
        mUsers.set(index,user);
        notifyDataSetChanged();
    }

    public void setCurrentUserInfo(String userUid, int number) {
        mCurrentUserId = userUid;
        //mCurrentUserCreatedAt = createdAt;
    }

    public void clear() {
        mUsers.clear();
    }

    public class ViewHolderUsers extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView mUserAvatar;
        private TextView mUserDisplayName;
        private TextView mStatusConnection;
        private Context mContextViewHolder;


        public ViewHolderUsers(Context context, View itemView) {
            super(itemView);
            mUserAvatar = (ImageView)itemView.findViewById(R.id.img_avatar);
            mUserDisplayName = (TextView)itemView.findViewById(R.id.text_view_display_name);
            mStatusConnection = (TextView)itemView.findViewById(R.id.text_view_connection_status);
            mContextViewHolder = context;

            itemView.setOnClickListener(this);
        }

        public ImageView getUserAvatar()
        {
            String completePath = Environment.getExternalStorageDirectory() + "/kucuk/" + mUsers.get(getLayoutPosition()).getuID()+".jpg";
            File file = new File(completePath);
            Log.i("Glide adresi", completePath);
            Uri imageUri = Uri.fromFile(file);
            Glide.with(mContext)
                    .load(imageUri)
                    .into(mUserAvatar);
            return mUserAvatar;
        }

        public TextView getUserDisplayName() {
            return mUserDisplayName;
        }

        public TextView getStatusConnection() {
            return mStatusConnection;
        }

        @Override
        public void onClick(View view) {
            User user = mUsers.get(getLayoutPosition());

            Intent chatIntent = new Intent(mContextViewHolder, ChatActivity.class);
            chatIntent.putExtra("Sender", mCurrentUserId);
            chatIntent.putExtra("Receiver",user.getuID() );
            chatIntent.putExtra("Name",user.getName() );

            mContextViewHolder.startActivity(chatIntent);

        }
    }

}
