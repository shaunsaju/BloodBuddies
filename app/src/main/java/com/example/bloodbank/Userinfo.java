package com.example.bloodbank;

public class Userinfo {
    public String name1;
    public String name2;
    public String name3;
    public String email;
    public String phone;
    public String bldgrp;
    public String gender;
    public String mImageUrl;
    public String userid;

    public Userinfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Userinfo(String userid, String name1, String name2, String name3, String email, String phone, String bldgrp, String gender, String mImageUrl) {
        this.userid = userid;
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
        this.email = email;
        this.phone = phone;
        this.bldgrp = bldgrp;
        this.gender = gender;
        this.mImageUrl = mImageUrl;

    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name) {
        name1 = name;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name) {
        name2 = name;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name) {
        name3 = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String name) {
        email = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String name) {
        phone = name;
    }

    public String getBldgrp() {
        return bldgrp;
    }

    public void setBldgrp(String name) {
        bldgrp = name;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String name) {
        mImageUrl = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String name) {
        gender = name;
    }
}
