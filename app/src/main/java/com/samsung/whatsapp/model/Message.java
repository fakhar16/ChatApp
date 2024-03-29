package com.samsung.whatsapp.model;

public class Message {
    private String message, type, from, to, messageId, starred, filename, caption, fileSize;
    private long time;
    private int feeling;
    private boolean isUnread;

    public Message() {

    }

    public Message(String messageId, String message, String type, String from, String to, long time, int feeling, String starred, boolean isUnread) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageId = messageId;
        this.time = time;
        this.feeling = feeling;
        this.starred = starred;
        this.isUnread = isUnread;
    }

    public Message(String messageId, String message, String type, String from, String to, long time, int feeling, String starred, String filename, String fileSize, boolean isUnread) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageId = messageId;
        this.time = time;
        this.feeling = feeling;
        this.starred = starred;
        this.filename = filename;
        this.fileSize = fileSize;
        this.isUnread = isUnread;
    }

    public Message(String messageId, String message, String caption, String type, String from, String to, long time, int feeling, String starred, boolean isUnread) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageId = messageId;
        this.time = time;
        this.feeling = feeling;
        this.starred = starred;
        this.caption = caption;
        this.isUnread = isUnread;
    }

    public Message(String messageId, String message, String caption, String type, String from, String to, long time, int feeling, String starred, String filename, String fileSize, boolean isUnread) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageId = messageId;
        this.time = time;
        this.feeling = feeling;
        this.starred = starred;
        this.filename = filename;
        this.fileSize = fileSize;
        this.caption = caption;
        this.isUnread = isUnread;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
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
