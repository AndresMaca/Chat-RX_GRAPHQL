package com.coders.dope.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity
public class ChatMessage {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("user")
    @Expose
    private Boolean user;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("image_url")
    @Expose
    private String imageUrl;
    @SerializedName("content_type")
    @Expose
    private String contentType;

    public ChatMessage(@NonNull String id, Boolean user, String message, String author, String date, String imageUrl, String contentType) {
        this.id = id;
        this.user = user;
        this.message = message;
        this.author = author;
        this.date = date;
        this.imageUrl = imageUrl;
        this.contentType = contentType;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public Boolean getUser() {
        return user;
    }

    public void setUser(Boolean user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return "Chat Message, Message: " + message + " Author: " + author + "id: " + id;
    }
}
