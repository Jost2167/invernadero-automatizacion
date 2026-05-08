package com.jost.invernadero.automatizacion.service;

public enum AuthMessage {

    LOGIN_SUCCESS,
    LOGIN_CANCELLED,
    TOKEN_INVALID,
    USER_INACTIVE,
    USER_NOT_REGISTERED,
    USER_NO_ROLES,
    EMAIL_INVALID,
    GENERIC_ERROR;

    private static final String OAUTH2_ACCESS_DENIED = "access_denied";
    private static final String KEY_PREFIX = "auth.errors.";

    public String code() {
        return name();
    }

    public String defaultKey() {
        return KEY_PREFIX + name();
    }

    public static AuthMessage fromCode(String code) {
        if (code == null) {
            return GENERIC_ERROR;
        }
        if (OAUTH2_ACCESS_DENIED.equals(code)) {
            return LOGIN_CANCELLED;
        }
        try {
            return AuthMessage.valueOf(code);
        } catch (IllegalArgumentException ignored) {
            return GENERIC_ERROR;
        }
    }
}
