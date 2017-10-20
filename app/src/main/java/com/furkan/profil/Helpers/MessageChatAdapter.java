package com.furkan.profil.Helpers;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.furkan.profil.Models.ChatMessage;
import com.furkan.profil.Models.User;
import com.furkan.profil.R;
import com.furkan.profil.UI.ChatActivity;
import com.furkan.profil.UI.NonFriendProfile;
import com.furkan.profil.UI.ProfilGoster;

import java.util.List;

public class MessageChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> mChatList;
    public static final int SENDER = 0;
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

    private String converteTimestamp(Long millisecond){
        return DateUtils.getRelativeTimeSpanString(millisecond,System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }


    public void refillAdapter(ChatMessage newFireChatMessage) {

        /*add new message chat to list*/
        mChatList.add(newFireChatMessage);

        /*refresh view*/
        notifyItemInserted(getItemCount() - 1);
    }


    public void cleanUp() {
        mChatList.clear();
    }


    /*==============ViewHolder===========*/

    /*ViewHolder for Sender*/

    public class ViewHolderSender extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mSenderMessageTextView;
        private TextView mTimeStamp;

        public ViewHolderSender(View itemView) {
            super(itemView);
            mSenderMessageTextView = (TextView) itemView.findViewById(R.id.text_view_sender_message);
            mTimeStamp = (TextView) itemView.findViewById(R.id.textView2);
            itemView.setOnClickListener(this);
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
    }


    /*ViewHolder for Recipient*/
    public class ViewHolderRecipient extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mRecipientMessageTextView;
        private TextView mTimeStamp;

        public ViewHolderRecipient(View itemView) {
            super(itemView);
            mRecipientMessageTextView = (TextView) itemView.findViewById(R.id.text_view_recipient_message);
            mTimeStamp = (TextView) itemView.findViewById(R.id.textView2);
            itemView.setOnClickListener(this);
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
    }
}