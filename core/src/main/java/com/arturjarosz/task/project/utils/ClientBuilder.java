package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.sharedkernel.testhelpers.AbstractBuilder;

import static com.arturjarosz.task.sharedkernel.testhelpers.TestUtils.setFieldForObject;

public class ClientBuilder extends AbstractBuilder<Client, ClientBuilder> {

    private static final String FIRST_NAME = "firstName";
    private static final String ID = "id";
    private static final String LAST_NAME = "lastName";

    public ClientBuilder() {
        super(Client.class);
    }

    public ClientBuilder withId(Long id) {
        setFieldForObject(this.object, ID, id);
        return this;
    }

    public ClientBuilder withFirstName(String firstName) {
        setFieldForObject(this.object, FIRST_NAME, firstName);
        return this;
    }

    public ClientBuilder withLastName(String lastName) {
        setFieldForObject(this.object, LAST_NAME, lastName);
        return this;
    }
}
