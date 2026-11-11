package com.arturjarosz.task.security.permission;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static com.arturjarosz.task.security.permission.Action.EDIT;
import static com.arturjarosz.task.security.permission.Action.READ;

/**
 * What a permission is about, with the actions that make sense for it. Names are stored in the database, so a resource
 * must not be renamed without a migration.
 */
public enum Resource {
    // Projects with their stages and tasks, without any money (see PROJECT_FINANCE)
    PROJECT,
    // A contract comes with its project, so it is never created or deleted on its own
    CONTRACT(READ, EDIT),
    TIME_ENTRY,
    CLIENT,
    ARCHITECT,
    CONTRACTOR,
    SUPPLIER,
    CALENDAR,
    // Money of a project: its costs, installments, supplies, contractor jobs, supervision and financial summary. Only
    // for projects the user can read
    PROJECT_FINANCE(PROJECT),
    COMPANY_COST,
    // The financial report page, which covers all projects and company costs, so it can only be read
    FINANCIAL_REPORT(READ),
    // Types and categories, system parameters and sharing of the Google calendar
    SETTINGS,
    // Users come from their first login, so they are never created or deleted; editing gives them roles
    USER(READ, EDIT),
    ROLE;

    private final Set<Action> actions;
    // Resource this one is part of, so none of its actions works without reading that one; null for none
    private final Resource requiredResource;

    Resource() {
        this.actions = EnumSet.allOf(Action.class);
        this.requiredResource = null;
    }

    Resource(Resource requiredResource) {
        this.actions = EnumSet.allOf(Action.class);
        this.requiredResource = requiredResource;
    }

    Resource(Action first, Action... rest) {
        this.actions = EnumSet.of(first, rest);
        this.requiredResource = null;
    }

    /**
     * Returns the actions available for this resource, in declaration order.
     */
    public Set<Action> getActions() {
        return EnumSet.copyOf(this.actions);
    }

    public boolean allows(Action action) {
        return this.actions.contains(action);
    }

    /**
     * Returns the resource this one is part of, whose reading every action on this one needs.
     */
    public Optional<Resource> getRequiredResource() {
        return Optional.ofNullable(this.requiredResource);
    }
}
