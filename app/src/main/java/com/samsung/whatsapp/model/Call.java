package com.samsung.whatsapp.model;

public class Call {
    private String userId;
    private String userName;
    private String date;
    private String userImageUrl;
    private String callType;

    private String callChannel;

    public Call() {

    }

    public Call(String userId, String userName, String date, String userImageUrl, String callType, String callChannel) {
        this.userId = userId;
        this.userName = userName;
        this.date = date;
        this.userImageUrl = userImageUrl;
        this.callType = callType;
        this.callChannel = callChannel;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallChannel() {
        return callChannel;
    }

    public void setCallChannel(String callChannel) {
        this.callChannel = callChannel;
    }
}
