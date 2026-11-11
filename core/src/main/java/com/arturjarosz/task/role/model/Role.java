package com.arturjarosz.task.role.model;

import com.arturjarosz.task.security.permission.Permission;
import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntry;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Named set of permissions given to users, configured by users. The administrator role has every permission, also
 * the ones added in later versions, so it stores none and cannot be changed. A role given to any user cannot be
 * removed.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "role_sequence", allocationSize = 1)
@Table(name = "ROLE")
public class Role extends AbstractNamedEntry {
    @Serial
    private static final long serialVersionUID = -3305729475091744186L;

    @Getter
    @Column(name = "ADMINISTRATOR", nullable = false, updatable = false)
    private boolean administrator;

    @ElementCollection
    @CollectionTable(name = "ROLE_PERMISSION", joinColumns = @JoinColumn(name = "ROLE_ID"))
    private Set<Permission> permissions = new HashSet<>();

    protected Role() {
        // needed by JPA
    }

    public Role(String name, Collection<Permission> permissions) {
        super(name);
        this.changePermissions(permissions);
    }

    /**
     * Returns permissions of the role in {@link Permission#ORDER}; all of them for the administrator role.
     */
    public Set<Permission> getPermissions() {
        if (this.administrator) {
            return Permission.all();
        }
        var result = Permission.newSortedSet();
        result.addAll(this.permissions);
        return result;
    }

    @Override
    public void rename(String name) {
        this.assertNotAdministrator();
        super.rename(name);
    }

    public void changePermissions(Collection<Permission> permissions) {
        this.assertNotAdministrator();
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }

    // RoleValidator.validateNotAdministrator tells users; this keeps any other caller from changing it
    private void assertNotAdministrator() {
        if (this.administrator) {
            throw new IllegalStateException("The administrator role cannot be changed");
        }
    }
}
