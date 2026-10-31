package com.arturjarosz.task.user.application;

import com.arturjarosz.task.user.infrastructure.repository.UserRepository;
import com.arturjarosz.task.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Single place that resolves the local {@link User} wired to the Auth0 user of the current JWT token. Anything acting
 * on behalf of the current user should get it from here, so the user is provisioned no matter which request comes
 * first after the first login.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CurrentUserProvider {

    // Auth0 sets it on tokens issued to machine-to-machine applications, which are not people
    private static final String GRANT_TYPE_CLAIM = "gty";
    private static final String CLIENT_CREDENTIALS_GRANT = "client-credentials";
    // Auth0 access tokens carry only the subject; the Post-Login Action adding user claims adds these
    // (see documentation/auth0/add-user-claims.js)
    private static final String CLAIMS_NAMESPACE = "https://task.arturjarosz.com/";
    public static final String EMAIL_CLAIM = CLAIMS_NAMESPACE + "email";
    public static final String NAME_CLAIM = CLAIMS_NAMESPACE + "name";

    private final UserRepository userRepository;

    /**
     * Returns the local user of the current JWT token without creating it.
     */
    public Optional<User> findCurrentUser() {
        return this.currentTokenUser().flatMap(tokenUser -> this.userRepository.findByAuthId(tokenUser.authId()));
    }

    /**
     * Returns the local user of the current JWT token, creating it on the first login. Username and email are
     * refreshed from the token claims, so a change made in Auth0 reaches the local user on next login. Returns an
     * empty optional when the token does not belong to a person. Joins the caller's transaction, so the user it
     * returns stays managed there.
     */
    @Transactional
    public Optional<User> findOrCreateCurrentUser() {
        var maybeTokenUser = this.currentTokenUser();
        if (maybeTokenUser.isEmpty()) {
            return Optional.empty();
        }
        var tokenUser = maybeTokenUser.get();

        var maybeUser = this.userRepository.findByAuthId(tokenUser.authId());
        if (maybeUser.isEmpty()) {
            // A concurrent first request (another tab) waits here until this transaction commits the user, then finds
            // it below instead of inserting a duplicate
            this.userRepository.lockAuthId(tokenUser.authId());
            maybeUser = this.userRepository.findByAuthId(tokenUser.authId());
        }
        if (maybeUser.isPresent()) {
            maybeUser.get().updateProfile(tokenUser.name(), tokenUser.email());
            return maybeUser;
        }

        var user = this.userRepository.save(new User(tokenUser.name(), tokenUser.email(), tokenUser.authId()));
        LOG.info("Local user created for authId {}.", tokenUser.authId());
        return Optional.of(user);
    }

    // Jwt stays out of every method signature (lambda parameters included): sample-data runs this context without
    // the OAuth2 resource server on the classpath, and Spring reads the declared methods when creating the bean
    private Optional<TokenUser> currentTokenUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            var jwt = jwtAuth.getToken();
            if (jwt.getSubject() != null && !CLIENT_CREDENTIALS_GRANT.equals(jwt.getClaimAsString(GRANT_TYPE_CLAIM))) {
                return Optional.of(new TokenUser(jwt.getSubject(), jwt.getClaimAsString(NAME_CLAIM),
                        jwt.getClaimAsString(EMAIL_CLAIM)));
            }
        }
        return Optional.empty();
    }

    // Person behind the current token, as the claims say; name and email are null when the claim is missing
    private record TokenUser(String authId, String name, String email) {
    }
}
