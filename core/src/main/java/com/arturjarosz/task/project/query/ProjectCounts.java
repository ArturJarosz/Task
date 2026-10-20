package com.arturjarosz.task.project.query;

/**
 * Number of all projects and of projects in an active status, for one architect or client.
 */
public record ProjectCounts(long all, long active) {
    public static final ProjectCounts NONE = new ProjectCounts(0, 0);
}
