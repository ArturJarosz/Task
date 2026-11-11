package com.arturjarosz.task.user.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("java:S2160")
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "user_sequence", allocationSize = 1)
@Table(name = "LOCAL_USER")
public class User extends AbstractAggregateRoot {

    @Serial
    private static final long serialVersionUID = -4721849012345678901L;
    // Lengths of the columns, in characters
    private static final int USERNAME_MAX_LENGTH = 100;
    private static final int EMAIL_MAX_LENGTH = 255;

    // Profile values come from Auth0; columns are not null, so a value Auth0 has not provided yet stays empty and the
    // FE falls back to the Auth0 profile for it
    @Getter
    @Column(name = "USERNAME", nullable = false)
    private String username = "";

    @Getter
    @Column(name = "EMAIL", nullable = false)
    private String email = "";

    @Getter
    @Column(name = "AUTH_ID", nullable = false, unique = true)
    private String authId;

    // Newest release note the user has seen; null until the user closes "What's new" for the first time
    @Getter
    @Column(name = "LAST_SEEN_RELEASE_NOTE_ID")
    private Long lastSeenReleaseNoteId;

    // Settings the user has defined; one row per setting in USER_CONFIGURATION
    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Set<UserConfiguration> configurations = new HashSet<>();

    // Ids of the roles of the user; what they allow is resolved by the PermissionProvider. Loaded in batches, as user
    // summaries listing several users carry them
    @ElementCollection
    @CollectionTable(name = "USER_ROLE", joinColumns = @JoinColumn(name = "USER_ID"))
    @Column(name = "ROLE_ID", nullable = false)
    @BatchSize(size = 50)
    private Set<Long> roleIds = new HashSet<>();

    protected User() {
        // needed by JPA
    }

    /**
     * Creates the user wired to given Auth0 id from its profile, using the email as username when there is no name.
     */
    public User(String username, String email, String authId) {
        this.authId = authId;
        this.updateProfile(StringUtils.defaultIfBlank(username, email), email);
    }

    /**
     * Takes the profile values that can be stored and keeps the current ones for the rest: blank values are ignored,
     * the username is cut to the column length and an email too long for its column is ignored, as cutting it would
     * store a wrong address.
     */
    public void updateProfile(String username, String email) {
        if (StringUtils.isNotBlank(username)) {
            this.username = truncate(username, USERNAME_MAX_LENGTH);
        }
        if (StringUtils.isNotBlank(email) && email.codePointCount(0, email.length()) <= EMAIL_MAX_LENGTH) {
            this.email = email;
        }
    }

    public Set<Long> getRoleIds() {
        return Collections.unmodifiableSet(this.roleIds);
    }

    /**
     * Replaces the roles of the user with given ones.
     */
    public void assignRoles(Collection<Long> roleIds) {
        this.roleIds.clear();
        this.roleIds.addAll(roleIds);
    }

    public Optional<UserConfiguration> getConfiguration(UserConfigurationName name) {
        return this.configurations.stream().filter(configuration -> configuration.getName() == name).findFirst();
    }

    /**
     * Sets value of the configuration with given name, creating it when the user has not defined it yet.
     */
    public UserConfiguration setConfiguration(UserConfigurationName name, String value) {
        var maybeConfiguration = this.getConfiguration(name);
        if (maybeConfiguration.isPresent()) {
            maybeConfiguration.get().update(value);
            return maybeConfiguration.get();
        }
        var configuration = new UserConfiguration(name, value);
        this.configurations.add(configuration);
        return configuration;
    }

    /**
     * Marks release notes up to the one with given id as seen. Never goes back to an older note, so a request sent
     * from an outdated browser tab cannot show the user notes again.
     */
    public void markReleaseNotesSeen(long releaseNoteId) {
        if (!this.hasSeenReleaseNote(releaseNoteId)) {
            this.lastSeenReleaseNoteId = releaseNoteId;
        }
    }

    /**
     * Tells whether the release note with given id is not newer than the last one the user has seen.
     */
    public boolean hasSeenReleaseNote(long releaseNoteId) {
        return this.lastSeenReleaseNoteId != null && releaseNoteId <= this.lastSeenReleaseNoteId;
    }

    // Counts characters (code points) like the database does, so a character outside the BMP is never cut in half
    private static String truncate(String value, int maxLength) {
        if (value.codePointCount(0, value.length()) <= maxLength) {
            return value;
        }
        return value.substring(0, value.offsetByCodePoints(0, maxLength));
    }
}
