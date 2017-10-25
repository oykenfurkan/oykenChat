package com.furkan.profil.Helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.furkan.profil.Models.ChatMessage;
import com.furkan.profil.Models.User;
import com.furkan.profil.R;
import com.furkan.profil.UI.ChatActivity;
import com.furkan.profil.UI.MessageDetails;
import com.furkan.profil.UI.NonFriendProfile;
import com.furkan.profil.UI.ProfilGoster;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class MessageChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> mIds= new ArrayList<>();
    private List<ChatMessage> mChatList;
    public static final int SENDER = 0;
    private DatabaseReference messageChatDatabase;
    public static final int RECIPIENT = 1;

    public MessageChatAdapter(List<ChatMessage> listOfFireChats) {
        mChatList = listOfFireChats;
    }

    @Override
    public int getItemViewType(int position) {
        if (mChatList.get(position).getRecipientOrSenderStatus() == SENDER) {
            return SENDER;
        } else {
            return RECIPIENT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case SENDER:
                View viewSender = inflater.inflate(R.layout.layout_sender_message, viewGroup, false);
                viewHolder = new ViewHolderSender(viewSender);
                break;
            case RECIPIENT:
                View viewRecipient = inflater.inflate(R.layout.layout_recipient_message, viewGroup, false);
                viewHolder = new ViewHolderRecipient(viewRecipient);
                break;
            default:
                View viewSenderDefault = inflater.inflate(R.layout.layout_sender_message, viewGroup, false);
                viewHolder = new ViewHolderSender(viewSenderDefault);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        switch (viewHolder.getItemViewType()) {
            case SENDER:
                ViewHolderSender viewHolderSender = (ViewHolderSender) viewHolder;
                configureSenderView(viewHolderSender, position);
                break;
            case RECIPIENT:
                ViewHolderRecipient viewHolderRecipient = (ViewHolderRecipient) viewHolder;
                configureRecipientView(viewHolderRecipient, position);
                break;
        }
    }

    private void configureSenderView(ViewHolderSender viewHolderSender, int position) {
        ChatMessage senderFireMessage = mChatList.get(position);
        viewHolderSender.getSenderMessageTextView().setText(senderFireMessage.getMessage());
        viewHolderSender.getmTimeStamp().setText(converteTimestamp(senderFireMessage.getTimestap()));

    }

    private void configureRecipientView(ViewHolderRecipient viewHolderRecipient, int position) {
        ChatMessage recipientFireMessage = mChatList.get(position);
        viewHolderRecipient.getRecipientMessageTextView().setText(recipientFireMessage.getMessage());
        viewHolderRecipient.getmTimeStamp().setText(converteTimestamp(recipientFireMessage.getTimestap()));
    }

    private String converteTimestamp(Long millisecond) {
        return DateUtils.getRelativeTimeSpanString(millisecond, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }


    public void refillAdapter(ChatMessage newFireChatMessage, String key) {

        /*add new message chat to list*/
        mChatList.add(newFireChatMessage);
        mIds.add(key);
        /*refresh view*/
        notifyItemInserted(getItemCount() - 1);
    }


    public void cleanUp() {
        mChatList.clear();
    }


    /*==============ViewHolder===========*/

    /*ViewHolder for Sender*/

    public class ViewHolderSender extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView mSenderMessageTextView;
        private TextView mTimeStamp;

        public ViewHolderSender(View itemView) {
            super(itemView);
            mSenderMessageTextView = (TextView) itemView.findViewById(R.id.text_view_sender_message);
            mTimeStamp = (TextView) itemView.findViewById(R.id.textView2);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public TextView getSenderMessageTextView() {
            return mSenderMessageTextView;
        }

        public TextView getmTimeStamp() {
            return mTimeStamp;
        }

        public void onClick(View view) {
            Toast.makeText(view.getContext(), mSenderMessageTextView.getText(), Toast.LENGTH_LONG).show();

        }

        @Override
        public boolean onLongClick(final View v) {

            PopupMenu popup = new PopupMenu(v.getContext(), v, Gravity.RIGHT);
            //Inflating the Popup using xml file
            popup.getMenuInflater()
                    .inflate(R.menu.popup, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.delete) {
                        delete(mChatList.get(getLayoutPosition()), getLayoutPosition());
                    }else if(item.getItemId() == R.id.details){
                        //SHOW MESSAGE DETAILS.
                        Intent i=new Intent(v.getContext(), MessageDetails.class);
                        i.putExtra("timestamp",String.valueOf(mChatList.get(getLayoutPosition()).getTimestap()));
                        i.putExtra("message",mSenderMessageTextView.getText());
                        i.putExtra("receiverPubKey",ChatActivity.receiverPubkey);

                        v.getContext().startActivity(i);
                    }
                    return true;
                }
            });

            popup.show(); //showing popup menu
            return false;
        }
    }


    /*ViewHolder for Recipient*/
    public class ViewHolderRecipient extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView mRecipientMessageTextView;
        private TextView mTimeStamp;

        public ViewHolderRecipient(View itemView) {
            super(itemView);
            mRecipientMessageTextView = (TextView) itemView.findViewById(R.id.text_view_recipient_message);
            mTimeStamp = (TextView) itemView.findViewById(R.id.textView2);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public TextView getRecipientMessageTextView() {
            return mRecipientMessageTextView;
        }

        public TextView getmTimeStamp() {
            return mTimeStamp;
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), mRecipientMessageTextView.getText(), Toast.LENGTH_LONG).show();
        }

        @Override
        public boolean onLongClick(final View v) {

            PopupMenu popup = new PopupMenu(v.getContext(), v, Gravity.RIGHT);
            //Inflating the Popup using xml file
            popup.getMenuInflater()
                    .inflate(R.menu.popup, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.delete) {
                        delete(mChatList.get(getLayoutPosition()), getLayoutPosition());
                    }else if(item.getItemId() == R.id.details){
                        //SHOW MESSAGE DETAILS.
                        Intent i=new Intent(v.getContext(), MessageDetails.class);
                        i.putExtra("timestamp",String.valueOf(mChatList.get(getLayoutPosition()).getTimestap()));
                        i.putExtra("message",mRecipientMessageTextView.getText());
                        i.putExtra("receiverPubKey",ChatActivity.receiverPubkey);

                        v.getContext().startActivity(i);

                    }
                    return true;
                }
            });

            popup.show(); //showing popup menu
            return false;
        }
    }

    public void delete(ChatMessage message, int position) {
        messageChatDatabase = FirebaseDatabase.getInstance().getReference("chats");
        if (message.getRecipientOrSenderStatus() == 0) {
            setDatabaseName(message.getSender(), message.getRecipient());
        } else {
            setDatabaseName(message.getRecipient(), message.getSender());
        }
        messageChatDatabase.child(mIds.get(position)).removeValue();
        mIds.remove(position);
        mChatList.remove(position);
        notifyDataSetChanged();

    }

    private void setDatabaseName(String mCurrentUserId, String mRecipientId) {
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
}