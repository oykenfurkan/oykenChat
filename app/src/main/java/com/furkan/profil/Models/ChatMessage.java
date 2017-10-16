package com.furkan.profil.Models;

import android.os.Handler;
import android.widget.EditText;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Date;

public class ChatMessage {

    private String message;
    private String sMessage;
    private String sender;
    private String recipient;

    private int mRecipientOrSenderStatus;

    public ChatMessage() {
    }



    public ChatMessage(String message, String sMessage, String sender, String recipient) {
        this.message = message;
        this.sMessage = sMessage;
        this.recipient = recipient;
        this.sender = sender;
    }

    public String getsMessage() {
        return sMessage;
    }

    public void setsMessage(String sMessage) {
        this.sMessage = sMessage;
    }

    public void setRecipientOrSenderStatus(int recipientOrSenderStatus) {
        this.mRecipientOrSenderStatus = recipientOrSenderStatus;
    }


    public String getMessage() {
        return message;
    }

    public String getRecipient(){
        return recipient;
    }

    public String getSender(){
        return sender;
    }

    @Exclude
    public int getRecipientOrSenderStatus() {
        return mRecipientOrSenderStatus;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}