package com.arturjarosz.task.user.application;

import com.arturjarosz.task.user.model.User;

/**
 * Lets other modules prepare a user created on the first login. All beans implementing it are called right after the
 * user is created, in the same transaction.
 */
public interface UserCreationListener {

    /**
     * Called after given user was created; the user is managed, so changes made to it are saved.
     */
    void onUserCreated(User user);
}
