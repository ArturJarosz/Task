package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.data.model.UserSample;
import com.arturjarosz.task.user.infrastructure.repository.UserRepository;
import com.arturjarosz.task.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads the sample architects as users, the authors of the changes in the history of sample Projects. Users are
 * created on their first login, so there is no application service creating them.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class UserDataInitializer implements DataInitializer {
    static final String FILE_NAME = "sampleData/usersSample.json";

    private final UserRepository userRepository;

    @Override
    public void initializeData() {
        LOG.info("Starting importing users.");
        readUsers().forEach(userSample -> this.userRepository.save(
                new User(userSample.getUsername(), userSample.getEmail(), userSample.getAuthId())));
        LOG.info("Users added to the database.");
    }

    static List<UserSample> readUsers() {
        return SampleDataFileReader.readList(FILE_NAME, UserSample.class);
    }
}
