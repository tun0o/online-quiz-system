package com.example.online_quiz_system.security;

import com.example.online_quiz_system.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class UserPrincipal implements UserDetails, OAuth2User {

    private Long id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean verified;

    // Dùng cho OAuth2 (Google, Facebook)
    private Map<String, Object> attributes;

    public UserPrincipal(Long id,
                         String email,
                         String password,
                         Collection<? extends GrantedAuthority> authorities,
                         boolean verified) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities == null
                ? Collections.emptyList()
                : Collections.unmodifiableCollection(authorities);
        this.verified = verified;
    }

    // Factory method cho local login
    public static UserPrincipal create(User user) {
        GrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(authority),
                user.isVerified()
        );
    }

    // Factory method cho OAuth2 login
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal principal = create(user);
        principal.attributes = attributes;
        return principal;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return verified;
    }

    // ---- Implement OAuth2User ----
    @Override
    public Map<String, Object> getAttributes() {
        return attributes != null ? attributes : Collections.emptyMap();
    }

    @Override
    public String getName() {
        return String.valueOf(id); // hoặc return email;
    }

    // Helper method kiểm tra role nhanh
    public boolean hasRole(String roleWithoutPrefix) {
        if (roleWithoutPrefix == null) return false;
        String expected = "ROLE_" + roleWithoutPrefix;
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(a -> a.equals(expected));
    }

    // Helper method to get authorities as a list of strings
    public List<String> getAuthoritiesAsString() {
        if (authorities == null) {
            return Collections.emptyList();
        }
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
