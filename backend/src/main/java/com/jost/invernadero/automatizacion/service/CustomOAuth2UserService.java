package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.config.OAuth2Properties;
import com.jost.invernadero.automatizacion.entity.Role;
import com.jost.invernadero.automatizacion.entity.User;
import com.jost.invernadero.automatizacion.repository.RoleRepository;
import com.jost.invernadero.automatizacion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    public static final String DEFAULT_ROLE = "ROLE_USER";

    private static final String ATTR_EMAIL = "email";
    private static final String ATTR_NAME = "name";
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OAuth2Properties properties;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        String email = oAuth2User.getAttribute(ATTR_EMAIL);
        String name = oAuth2User.getAttribute(ATTR_NAME);

        if (!StringUtils.hasText(email) || !EMAIL_REGEX.matcher(email).matches()) {
            throw oauth2Error(AuthMessage.EMAIL_INVALID);
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> autoRegisterIfAllowed(email, name));

        if (!user.isActive()) {
            throw oauth2Error(AuthMessage.USER_INACTIVE);
        }

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw oauth2Error(AuthMessage.USER_NO_ROLES);
        }

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), ATTR_EMAIL);
    }

    private User autoRegisterIfAllowed(String email, String name) {
        if (!properties.autoRegister()) {
            throw oauth2Error(AuthMessage.USER_NOT_REGISTERED);
        }
        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> oauth2Error(AuthMessage.USER_NO_ROLES));
        User newUser = User.builder()
                .email(email)
                .name(name)
                .active(true)
                .roles(new HashSet<>(Set.of(defaultRole)))
                .build();
        return userRepository.save(newUser);
    }

    private OAuth2AuthenticationException oauth2Error(AuthMessage message) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(message.code(), message.defaultKey(), null));
    }
}
