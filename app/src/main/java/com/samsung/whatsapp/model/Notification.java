package com.samsung.whatsapp.model;

public class Notification {
    String title;
    String message;
    String type;
    String icon;
    String to_token;
    String sender_id;
    String receiver_id;

    public Notification(String title, String message, String type, String icon, String to_token, String sender_id, String receiver_id) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.icon = icon;
        this.to_token = to_token;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTo_token() {
        return to_token;
    }

    public void setTo_token(String to_token) {
        this.to_token = to_token;
    }
}
