package com.samsung.whatsapp.model;

public class User {
    private String uid, phone, name, status, image, token;

    public User() {
    }

    public User(String uid, String phone, String name, String status, String image, String token) {
        this.uid = uid;
        this.phone = phone;
        this.name = name;
        this.status = status;
        this.image = image;
        this.token = token;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
