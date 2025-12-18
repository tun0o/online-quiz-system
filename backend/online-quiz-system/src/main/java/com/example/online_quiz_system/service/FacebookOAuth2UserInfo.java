package com.example.online_quiz_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class FacebookOAuth2UserInfo extends OAuth2UserInfo {
    private static final Logger logger = LoggerFactory.getLogger(FacebookOAuth2UserInfo.class);

    public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public boolean isEmailVerified() {
        return getEmail() != null;
    }

    @Override
    public String getImageUrl() {
        try {
            if (attributes.containsKey("picture")) {
                Object pictureObj = attributes.get("picture");
                if (pictureObj instanceof Map) {
                    Map<?,?> pictureMap = (Map<?,?>) pictureObj;
                    if (pictureMap.containsKey("data")) {
                        Object dataObj = pictureMap.get("data");
                        if (dataObj instanceof Map) {
                            Map<?,?> dataMap = (Map<?,?>) dataObj;
                            if (dataMap.containsKey("url")) {
                                return dataMap.get("url").toString();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to extract Facebook profile picture: {}", e.getMessage());
        }
        return null;
    }
}