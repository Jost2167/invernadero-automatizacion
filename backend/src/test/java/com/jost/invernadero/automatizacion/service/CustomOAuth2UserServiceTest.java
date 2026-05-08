package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.config.OAuth2Properties;
import com.jost.invernadero.automatizacion.entity.Role;
import com.jost.invernadero.automatizacion.entity.User;
import com.jost.invernadero.automatizacion.repository.RoleRepository;
import com.jost.invernadero.automatizacion.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomOAuth2UserServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private DefaultOAuth2UserService delegate;
    private CustomOAuth2UserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        delegate = mock(DefaultOAuth2UserService.class);
        OAuth2Properties properties = new OAuth2Properties(false, "http://localhost:5173/auth/callback");
        service = new CustomOAuth2UserService(userRepository, roleRepository, properties);
        ReflectionTestUtils.setField(service, "delegate", delegate);
    }

    private OAuth2User stubOAuth2User(String email, String name) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "g-12345");
        if (email != null) attributes.put("email", email);
        if (name != null) attributes.put("name", name);
        attributes.put("picture", "https://example.com/avatar.png");
        String nameKey = email != null ? "email" : "sub";
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")), attributes, nameKey);
    }

    @Test
    void activeUserWithRoles_loadsSuccessfully() {
        when(delegate.loadUser(any())).thenReturn(stubOAuth2User("alice@example.com", "Alice"));
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .id(1L).email("alice@example.com").name("Alice")
                .active(true).roles(new HashSet<>(Set.of(role)))
                .build();
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        OAuth2User result = service.loadUser(mock(OAuth2UserRequest.class));

        assertThat(result.<String>getAttribute("email")).isEqualTo("alice@example.com");
        assertThat(result.getName()).isEqualTo("alice@example.com");
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void inactiveUser_throwsUserInactive() {
        when(delegate.loadUser(any())).thenReturn(stubOAuth2User("inactive@example.com", "Inactive"));
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .email("inactive@example.com").active(false)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.loadUser(mock(OAuth2UserRequest.class)))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(ex -> assertThat(((OAuth2AuthenticationException) ex).getError().getErrorCode())
                        .isEqualTo(AuthMessage.USER_INACTIVE.code()));
    }

    @Test
    void notRegistered_autoRegisterDisabled_throwsUserNotRegistered() {
        when(delegate.loadUser(any())).thenReturn(stubOAuth2User("new@example.com", "New"));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUser(mock(OAuth2UserRequest.class)))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(ex -> assertThat(((OAuth2AuthenticationException) ex).getError().getErrorCode())
                        .isEqualTo(AuthMessage.USER_NOT_REGISTERED.code()));
    }

    @Test
    void notRegistered_autoRegisterEnabled_createsUserWithDefaultRole() {
        OAuth2Properties autoRegister = new OAuth2Properties(
                true, "http://localhost:5173/auth/callback");
        CustomOAuth2UserService autoService = new CustomOAuth2UserService(
                userRepository, roleRepository, autoRegister);
        ReflectionTestUtils.setField(autoService, "delegate", delegate);

        when(delegate.loadUser(any())).thenReturn(stubOAuth2User("new@example.com", "New User"));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        Role userRole = Role.builder().id(1L).name("ROLE_USER").build();
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });

        OAuth2User result = autoService.loadUser(mock(OAuth2UserRequest.class));

        assertThat(result.<String>getAttribute("email")).isEqualTo("new@example.com");
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void invalidEmailFormat_throwsEmailInvalid() {
        when(delegate.loadUser(any())).thenReturn(stubOAuth2User("not-an-email", "X"));

        assertThatThrownBy(() -> service.loadUser(mock(OAuth2UserRequest.class)))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(ex -> assertThat(((OAuth2AuthenticationException) ex).getError().getErrorCode())
                        .isEqualTo(AuthMessage.EMAIL_INVALID.code()));
    }

    @Test
    void emptyEmail_throwsEmailInvalid() {
        when(delegate.loadUser(any())).thenReturn(stubOAuth2User(null, "X"));

        assertThatThrownBy(() -> service.loadUser(mock(OAuth2UserRequest.class)))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(ex -> assertThat(((OAuth2AuthenticationException) ex).getError().getErrorCode())
                        .isEqualTo(AuthMessage.EMAIL_INVALID.code()));
    }
}
