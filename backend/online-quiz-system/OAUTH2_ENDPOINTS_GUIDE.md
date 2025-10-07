# OAuth2 Endpoints & Redirect URIs Guide

## Overview
This guide documents all OAuth2 endpoints and redirect URIs for the Online Quiz System.

## Backend Endpoints

### OAuth2 Authorization Endpoints
- **Google Authorization**: `http://localhost:8080/oauth2/authorization/google`
- **Facebook Authorization**: `http://localhost:8080/oauth2/authorization/facebook`

### OAuth2 Callback Endpoints
- **Google Callback**: `http://localhost:8080/login/oauth2/code/google`
- **Facebook Callback**: `http://localhost:8080/login/oauth2/code/facebook`

### OAuth2 Test Endpoints
- **Configuration Test**: `GET /api/oauth2/test/config`
- **Validation Test**: `POST /api/oauth2/test/validate`
- **Provider Support Test**: `GET /api/oauth2/test/providers/{provider}`
- **URL Generation Test**: `GET /api/oauth2/test/urls`
- **Email Validation Test**: `POST /api/oauth2/test/validate-email`
- **Name Validation Test**: `POST /api/oauth2/test/validate-name`
- **Input Sanitization Test**: `POST /api/oauth2/test/sanitize`

## Frontend Routes

### OAuth2 Success/Error Routes
- **Success Route**: `http://localhost:3000/oauth2/success`
- **Error Route**: `http://localhost:3000/oauth2/error`

## Configuration Properties

### Application Properties
```properties
# OAuth2 Redirect URIs & Endpoints
app.oauth2.google.auth.url=http://localhost:8080/oauth2/authorization/google
app.oauth2.facebook.auth.url=http://localhost:8080/oauth2/authorization/facebook
app.oauth2.google.callback.url=http://localhost:8080/login/oauth2/code/google
app.oauth2.facebook.callback.url=http://localhost:8080/login/oauth2/code/facebook
app.oauth2.frontend.success.url=http://localhost:3000/oauth2/success
app.oauth2.frontend.error.url=http://localhost:3000/oauth2/error
```

### Spring Security OAuth2 Configuration
```properties
# OAuth2 Client Registration
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

spring.security.oauth2.client.registration.facebook.client-id=${FACEBOOK_CLIENT_ID}
spring.security.oauth2.client.registration.facebook.client-secret=${FACEBOOK_CLIENT_SECRET}
spring.security.oauth2.client.registration.facebook.scope=public_profile,email
spring.security.oauth2.client.registration.facebook.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
```

## OAuth2 Flow

### 1. Authorization Flow
1. User clicks "Login with Google/Facebook"
2. Redirect to: `http://localhost:8080/oauth2/authorization/{provider}`
3. User authenticates with provider
4. Provider redirects to: `http://localhost:8080/login/oauth2/code/{provider}`
5. Backend processes OAuth2 callback
6. Redirect to frontend: `http://localhost:3000/oauth2/success` or `http://localhost:3000/oauth2/error`

### 2. Error Handling
- Authentication failures redirect to: `http://localhost:3000/oauth2/error`
- Error parameters: `?error={errorCode}&message={errorMessage}`

## Testing Endpoints

### Test Configuration
```bash
GET http://localhost:8080/api/oauth2/test/config
```

### Test URL Generation
```bash
GET http://localhost:8080/api/oauth2/test/urls
```

### Test Email Validation
```bash
POST http://localhost:8080/api/oauth2/test/validate-email
Content-Type: application/json

{
  "email": "user@gmail.com"
}
```

## Environment Variables

### Required Environment Variables
```bash
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
FACEBOOK_CLIENT_ID=your-facebook-client-id
FACEBOOK_CLIENT_SECRET=your-facebook-client-secret
FRONTEND_URL=http://localhost:3000
```

## Security Considerations

### CORS Configuration
```properties
spring.web.cors.allowed-origins=http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS,PATCH
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true
```

### OAuth2 Security
- All OAuth2 endpoints are protected by Spring Security
- Test endpoints should be disabled in production
- Client secrets are masked in test responses
- Input validation and sanitization are enforced

## Production Deployment

### Production URLs
- Replace `localhost:8080` with your production backend URL
- Replace `localhost:3000` with your production frontend URL
- Update OAuth2 provider redirect URIs in provider dashboards
- Ensure HTTPS is used in production

### OAuth2 Provider Configuration
1. **Google Cloud Console**:
   - Add redirect URI: `https://yourdomain.com/login/oauth2/code/google`
   
2. **Facebook Developers**:
   - Add redirect URI: `https://yourdomain.com/login/oauth2/code/facebook`

## Troubleshooting

### Common Issues
1. **CORS Errors**: Check CORS configuration
2. **Redirect URI Mismatch**: Verify provider configuration
3. **Client Secret Issues**: Check environment variables
4. **Frontend Route Issues**: Ensure frontend routes exist

### Debugging
- Use test endpoints to verify configuration
- Check logs for OAuth2 authentication flow
- Verify provider redirect URIs match exactly
- Test with different browsers/incognito mode

