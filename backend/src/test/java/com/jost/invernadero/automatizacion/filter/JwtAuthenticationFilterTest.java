package com.jost.invernadero.automatizacion.filter;

import com.jost.invernadero.automatizacion.service.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtTokenProvider tokenProvider;
    private UserDetailsService userDetailsService;
    private FilterChain chain;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        tokenProvider = mock(JwtTokenProvider.class);
        userDetailsService = mock(UserDetailsService.class);
        chain = mock(FilterChain.class);
        filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noHeader_doesNotAuthenticateAndContinuesChain() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
        verifyNoInteractions(tokenProvider, userDetailsService);
    }

    @Test
    void invalidToken_doesNotAuthenticate() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer invalid");
        when(tokenProvider.validateToken("invalid")).thenReturn(false);

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider).validateToken("invalid");
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void validToken_authenticatesUser() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer good-token");
        when(tokenProvider.validateToken("good-token")).thenReturn(true);
        when(tokenProvider.getEmailFromToken("good-token")).thenReturn("alice@example.com");
        UserDetails userDetails = User.withUsername("alice@example.com")
                .password("")
                .authorities("ROLE_USER")
                .build();
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(userDetails);

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("alice@example.com");
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void validTokenButUserDisabled_doesNotAuthenticate() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer good-token");
        when(tokenProvider.validateToken("good-token")).thenReturn(true);
        when(tokenProvider.getEmailFromToken("good-token")).thenReturn("inactive@example.com");
        UserDetails disabled = User.withUsername("inactive@example.com")
                .password("")
                .authorities("ROLE_USER")
                .disabled(true)
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(disabled);

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void userNotFound_doesNotAuthenticate() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer good-token");
        when(tokenProvider.validateToken("good-token")).thenReturn(true);
        when(tokenProvider.getEmailFromToken("good-token")).thenReturn("ghost@example.com");
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenThrow(new UsernameNotFoundException("not found"));

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void headerWithoutBearerPrefix_isIgnored() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Token foo");

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenProvider, userDetailsService);
    }
}
