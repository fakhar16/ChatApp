package com.samsung.whatsapp.model;

public class Message {
    private String message, type, from, to, messageId, starred, filename, caption;
    private long time;
    private int feeling;

    public Message() {

    }

    public Message(String messageId, String message, String type, String from, String to, long time, int feeling, String starred) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageId = messageId;
        this.time = time;
        this.feeling = feeling;
        this.starred = starred;
    }

    public Message(String messageId, String message, String type, String from, String to, long time, int feeling, String starred, String filename) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageId = messageId;
        this.time = time;
        this.feeling = feeling;
        this.starred = starred;
        this.filename = filename;
    }

    public Message(String messageId, String message, String caption, String type, String from, String to, long time, int feeling, String starred) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageId = messageId;
        this.time = time;
        this.feeling = feeling;
        this.starred = starred;
        this.caption = caption;
    }

    public Message(String messageId, String message, String caption, String type, String from, String to, long time, int feeling, String starred, String filename) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageId = messageId;
        this.time = time;
        this.feeling = feeling;
        this.starred = starred;
        this.filename = filename;
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStarred() {
        return starred;
    }

    public void setStarred(String starred) {
        this.starred = starred;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
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

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
