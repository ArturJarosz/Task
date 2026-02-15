package com.arturjarosz.task.user.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

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

    protected User() {
        // needed by JPA
    }

    public User(String username, String email, String authId) {
        this.username = username;
        this.email = email;
        this.authId = authId;
    }
}
