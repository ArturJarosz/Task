package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;

import static com.arturjarosz.task.sharedkernel.utils.TestUtils.setFieldForObject;

public class ClientBuilder extends AbstractBuilder<Client, ClientBuilder> {

    private static final String ID = "id";

    public ClientBuilder() {
        super(Client.class);
    }

    public ClientBuilder withId(Long id) {
        setFieldForObject(this.object, ID, id);
        return this;
    }
}
