package com.example.online_quiz_system.service;

import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract boolean isEmailVerified();
    public String getImageUrl() {
        Object pic = attributes.get("picture");
        return pic != null ? pic.toString() : null;
    }
}

