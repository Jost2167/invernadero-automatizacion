package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.config.OAuth2Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2AuthenticationFailureHandlerTest {

    private static final String REDIRECT = "http://localhost:5173/auth/callback";

    private OAuth2AuthenticationFailureHandler handler;

    @BeforeEach
    void setUp() {
        OAuth2Properties properties = new OAuth2Properties(false, REDIRECT);
        handler = new OAuth2AuthenticationFailureHandler(properties);
    }

    @Test
    void redirectsWithUserInactiveCode() throws Exception {
        OAuth2AuthenticationException ex = new OAuth2AuthenticationException(
                new OAuth2Error("USER_INACTIVE", "auth.errors.USER_INACTIVE", null));
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        handler.onAuthenticationFailure(req, res, ex);

        assertThat(res.getStatus()).isEqualTo(302);
        assertThat(res.getRedirectedUrl())
                .isEqualTo(REDIRECT + "?error=USER_INACTIVE");
    }

    @Test
    void mapsAccessDeniedToLoginCancelled() throws Exception {
        OAuth2AuthenticationException ex = new OAuth2AuthenticationException(
                new OAuth2Error("access_denied", null, null));
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        handler.onAuthenticationFailure(req, res, ex);

        assertThat(res.getRedirectedUrl())
                .isEqualTo(REDIRECT + "?error=LOGIN_CANCELLED");
    }

    @Test
    void unknownOAuth2CodeFallsBackToGenericError() throws Exception {
        OAuth2AuthenticationException ex = new OAuth2AuthenticationException(
                new OAuth2Error("totally_unknown_code", null, null));
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        handler.onAuthenticationFailure(req, res, ex);

        assertThat(res.getRedirectedUrl())
                .isEqualTo(REDIRECT + "?error=GENERIC_ERROR");
    }

    @Test
    void nonOAuth2ExceptionFallsBackToGenericError() throws Exception {
        BadCredentialsException ex = new BadCredentialsException("creds");
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        handler.onAuthenticationFailure(req, res, ex);

        assertThat(res.getRedirectedUrl())
                .isEqualTo(REDIRECT + "?error=GENERIC_ERROR");
    }
}
