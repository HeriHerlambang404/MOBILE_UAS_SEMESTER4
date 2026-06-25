package com.apk.catatkeuanganku.database;

public class ChatMessage {
    public static final String ROLE_USER = "user";
    public static final String ROLE_AI = "ai";

    private String text;
    private String role;

    public ChatMessage(String text, String role) {
        this.text = text;
        this.role = role;
    }

    public String getText() { return text; }
    public String getRole() { return role; }
}