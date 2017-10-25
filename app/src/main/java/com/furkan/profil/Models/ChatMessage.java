package com.furkan.profil.Models;

import android.text.format.DateUtils;
import android.util.Log;

import com.google.firebase.database.Exclude;
public class ChatMessage {

    private String message;
    private String sMessage;
    private String sender;
    private String recipient;
    private long timestap;
    private int mRecipientOrSenderStatus;

    public ChatMessage(String message, String sMessage, String sender, String recipient, Long timestap) {
        this.message = message;
        this.sMessage = sMessage;
        this.sender = sender;
        this.recipient = recipient;
        this.timestap=timestap;
    }

    public ChatMessage() {
        //For Firebase.
    }

    public void setRecipientOrSenderStatus(int recipientOrSenderStatus) {
        this.mRecipientOrSenderStatus = recipientOrSenderStatus;
    }

    @Exclude
    public int getRecipientOrSenderStatus() {
        return mRecipientOrSenderStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getsMessage() {
        return sMessage;
    }

    public void setsMessage(String sMessage) {
        this.sMessage = sMessage;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public long getTimestap() {
        return timestap;
    }

    public void setTimestap(long timestap) {
        this.timestap = timestap;
    }
}