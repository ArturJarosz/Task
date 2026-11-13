package com.arturjarosz.task.data.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSample {
    // the same as the name of an architect when the user is one; the changes to their projects are made by them
    private String username;
    private String email;
    // made up, so nobody can log in as a sample user
    private String authId;
}
