package com.arturjarosz.task.security.permission;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Allows a user to take an action on a resource, such as editing clients. Permissions come from the roles of the user
 * (see {@link PermissionProvider}); only the actions available for the resource can be given.
 */
@Embeddable
public record Permission(
        @Enumerated(EnumType.STRING) @Column(name = "RESOURCE", nullable = false) Resource resource,
        @Enumerated(EnumType.STRING) @Column(name = "ACTION", nullable = false) Action action)
        implements Serializable {

    // Resources in declaration order, and the actions of each in declaration order
    public static final Comparator<Permission> ORDER = Comparator.comparing(Permission::resource)
            .thenComparing(Permission::action);

    public Permission {
        if (resource == null || action == null || !resource.allows(action)) {
            throw new IllegalArgumentException("Action %s is not available for resource %s".formatted(action, resource));
        }
    }

    /**
     * Returns the permissions this one does not work without: reading the resource, for any other action on it, and
     * reading the resource it is part of.
     */
    public Set<Permission> getRequiredPermissions() {
        var required = newSortedSet();
        if (this.action != Action.READ && this.resource.allows(Action.READ)) {
            required.add(new Permission(this.resource, Action.READ));
        }
        this.resource.getRequiredResource()
                .ifPresent(requiredResource -> required.add(new Permission(requiredResource, Action.READ)));
        return required;
    }

    /**
     * Returns a sorted set to collect permissions into, so they keep the same order everywhere.
     */
    public static Set<Permission> newSortedSet() {
        return new TreeSet<>(ORDER);
    }

    /**
     * Returns every permission that can be given, in order.
     */
    public static Set<Permission> all() {
        var permissions = newSortedSet();
        Arrays.stream(Resource.values()).forEach(resource ->
                resource.getActions().forEach(action -> permissions.add(new Permission(resource, action))));
        return permissions;
    }
}
