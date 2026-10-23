package com.arturjarosz.task.user.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
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

    @Getter
    @Setter
    @Column(name = "USERNAME", nullable = false)
    private String username;

    @Getter
    @Setter
    @Column(name = "EMAIL", nullable = false)
    private String email;

    @Getter
    @Setter
    @Column(name = "AUTH_ID", nullable = false, unique = true)
    private String authId;

    // Settings the user has defined; one row per setting in USER_CONFIGURATION
    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Set<UserConfiguration> configurations = new HashSet<>();

    protected User() {
        // needed by JPA
    }

    public User(String username, String email, String authId) {
        this.username = username;
        this.email = email;
        this.authId = authId;
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
}
