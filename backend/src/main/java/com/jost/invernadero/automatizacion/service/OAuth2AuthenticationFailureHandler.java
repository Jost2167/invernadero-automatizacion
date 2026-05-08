package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.config.OAuth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2Properties properties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        AuthMessage message = resolveMessage(exception);

        String targetUrl = UriComponentsBuilder.fromUriString(properties.redirectUri())
                .queryParam("error", message.code())
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private AuthMessage resolveMessage(AuthenticationException ex) {
        if (ex instanceof OAuth2AuthenticationException oauthEx) {
            return AuthMessage.fromCode(oauthEx.getError().getErrorCode());
        }
        return AuthMessage.GENERIC_ERROR;
    }
}
