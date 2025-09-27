package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.AuthProvider;
import com.example.online_quiz_system.entity.Role;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.repository.UserRepository;
import com.example.online_quiz_system.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class OAuth2Service extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = getEmailFromAttributes(provider, attributes);
        String name = getNameFromAttributes(provider, attributes);
        String providerId = oAuth2User.getName();

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getProvider().equals(AuthProvider.valueOf(provider.toUpperCase()))) {
                throw new OAuth2AuthenticationException("Email already registered with " + user.getProvider() + " provider");
            }
        } else {
            user = new User();
            user.setEmail(email);
            user.setProvider(AuthProvider.GOOGLE.name());
            user.setProviderId(providerId);
            user.setVerified(true);
            user.setRole(Role.USER);
            user = userRepository.save(user);
        }

        return UserPrincipal.create(user, attributes);
    }

    private String getEmailFromAttributes(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("email");
        } else if ("facebook".equals(provider)) {
            return (String) attributes.get("email");
        }
        throw new OAuth2AuthenticationException("Provider not supported: " + provider);
    }

    private String getNameFromAttributes(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("name");
        } else if ("facebook".equals(provider)) {
            return (String) attributes.get("name");
        }
        return "";
    }
}
