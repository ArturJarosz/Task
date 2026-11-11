package com.arturjarosz.task.security.permission;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Person behind the current request, as the authentication says: authId is the Auth0 user id (the token subject) and
 * claims are all claims of the token, for providers reading permissions from them.
 */
public record AuthenticatedPrincipal(String authId, Map<String, Object> claims) {

    public AuthenticatedPrincipal {
        // copied without Map.copyOf, which rejects null values
        claims = Collections.unmodifiableMap(new HashMap<>(claims));
    }
}
