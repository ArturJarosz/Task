package com.arturjarosz.task.notification.application;

import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.notification.domain.DueItem;
import com.arturjarosz.task.user.infrastructure.repository.UserRepository;
import com.arturjarosz.task.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Decides who is notified about a due item. Users see only their own work, so an item whose architect has no linked
 * user goes to no one.
 */
@RequiredArgsConstructor
@Component
public class NotificationRecipientResolver {

    private final ArchitectRepository architectRepository;
    private final UserRepository userRepository;

    public Set<Long> resolve(DueItem dueItem) {
        if (dueItem.forEveryone()) {
            return this.userRepository.findAll().stream().map(User::getId).collect(Collectors.toSet());
        }
        if (dueItem.architectId() == null) {
            return Set.of();
        }
        return this.architectRepository.findById(dueItem.architectId()).map(Architect::getUserId)
                .map(Set::of).orElseGet(Set::of);
    }
}
