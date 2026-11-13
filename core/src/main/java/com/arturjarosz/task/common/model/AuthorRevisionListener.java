package com.arturjarosz.task.common.model;

import com.arturjarosz.task.user.application.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.RevisionListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Sets the author of each new revision. Hibernate takes the listener from Spring, so it gets its dependencies
 * injected. The author is kept as the Auth0 id of the token: finding the local user would run a query while Hibernate
 * writes the revision.
 */
@RequiredArgsConstructor
@Component
public class AuthorRevisionListener implements RevisionListener {

    // Hibernate creates the listener while it starts, before the repositories CurrentUserProvider depends on exist
    private final ObjectProvider<CurrentUserProvider> currentUserProvider;

    @Override
    public void newRevision(Object revisionEntity) {
        this.currentUserProvider.getObject().findCurrentPrincipal()
                .ifPresent(principal -> ((RevInfo) revisionEntity).setAuthorAuthId(principal.authId()));
    }
}
