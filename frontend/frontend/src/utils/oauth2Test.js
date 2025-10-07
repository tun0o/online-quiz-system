// OAuth2 Test Utilities
// This file contains utilities for testing OAuth2 functionality

import { OAUTH2_URLS } from '@/services/api.js';

/**
 * Test OAuth2 URL generation
 */
export const testOAuth2Urls = () => {
    console.log('OAuth2 URLs Test:');
    console.log('Google OAuth2 URL:', OAUTH2_URLS.GOOGLE);
    console.log('Facebook OAuth2 URL:', OAUTH2_URLS.FACEBOOK);
    console.log('Success Route:', OAUTH2_URLS.SUCCESS);
    console.log('Error Route:', OAUTH2_URLS.ERROR);
    
    return {
        google: OAUTH2_URLS.GOOGLE,
        facebook: OAUTH2_URLS.FACEBOOK,
        success: OAUTH2_URLS.SUCCESS,
        error: OAUTH2_URLS.ERROR
    };
};

/**
 * Validate OAuth2 URLs format
 */
export const validateOAuth2Urls = () => {
    const errors = [];
    
    // Check Google URL
    if (!OAUTH2_URLS.GOOGLE.includes('/oauth2/authorization/google')) {
        errors.push('Google OAuth2 URL format is incorrect');
    }
    
    // Check Facebook URL
    if (!OAUTH2_URLS.FACEBOOK.includes('/oauth2/authorization/facebook')) {
        errors.push('Facebook OAuth2 URL format is incorrect');
    }
    
    // Check Success route
    if (OAUTH2_URLS.SUCCESS !== '/oauth2/success') {
        errors.push('Success route format is incorrect');
    }
    
    // Check Error route
    if (OAUTH2_URLS.ERROR !== '/oauth2/error') {
        errors.push('Error route format is incorrect');
    }
    
    return {
        isValid: errors.length === 0,
        errors: errors
    };
};

/**
 * Test OAuth2 redirect functionality
 */
export const testOAuth2Redirect = (provider) => {
    const url = provider === 'google' ? OAUTH2_URLS.GOOGLE : OAUTH2_URLS.FACEBOOK;
    
    console.log(`Testing OAuth2 redirect for ${provider}:`, url);
    
    // In a real test, you would navigate to the URL
    // For now, just log it
    return url;
};

// Export for use in browser console
if (typeof window !== 'undefined') {
    window.testOAuth2Urls = testOAuth2Urls;
    window.validateOAuth2Urls = validateOAuth2Urls;
    window.testOAuth2Redirect = testOAuth2Redirect;
}

