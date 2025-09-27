package com.example.online_quiz_system.security;

import com.example.online_quiz_system.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean verified;

    

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

    

    // Helper method kiểm tra role nhanh
    public boolean hasRole(String roleWithoutPrefix) {
        if (roleWithoutPrefix == null) return false;
        String expected = "ROLE_" + roleWithoutPrefix;
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(a -> a.equals(expected));
    }
}
