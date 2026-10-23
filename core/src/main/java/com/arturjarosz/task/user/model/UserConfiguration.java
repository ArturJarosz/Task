package com.arturjarosz.task.user.model;

import com.arturjarosz.task.sharedkernel.model.AbstractHistoryAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "user_configuration_sequence", allocationSize = 1)
@Table(name = "USER_CONFIGURATION")
public class UserConfiguration extends AbstractHistoryAwareEntity {

    @Serial
    private static final long serialVersionUID = 6302841947625830167L;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "NAME", nullable = false)
    private UserConfigurationName name;

    @Getter
    @Column(name = "VALUE", nullable = false)
    private String value;

    protected UserConfiguration() {
        // needed by JPA
    }

    public UserConfiguration(UserConfigurationName name, String value) {
        this.name = name;
        this.value = value;
    }

    public void update(String value) {
        this.value = value;
    }
}
