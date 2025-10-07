package com.example.online_quiz_system.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Custom OIDC User implementation that ensures 'sub' field is always present.
 */
public class CustomOidcUser implements OidcUser {
    
    private final OidcUser delegate;
    private final Map<String, Object> fixedClaims;
    
    public CustomOidcUser(OidcUser delegate, Map<String, Object> fixedClaims) {
        this.delegate = delegate;
        this.fixedClaims = fixedClaims;
    }

    @Override
    public Map<String, Object> getClaims() {
        return fixedClaims;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate != null ? delegate.getUserInfo() : null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate != null ? delegate.getIdToken() : null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return fixedClaims;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate != null ? delegate.getAuthorities() : new ArrayList<>();
    }

    @Override
    public String getName() {
        return (String) fixedClaims.get("sub");
    }
}
