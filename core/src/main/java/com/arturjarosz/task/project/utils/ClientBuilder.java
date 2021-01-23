package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.BuilderUtils;

public class ClientBuilder extends AbstractBuilder<Client, ClientBuilder> {

    public ClientBuilder() {
        super(Client.class);
    }

    public ClientBuilder withId(Long id) {
        BuilderUtils.setFieldForObject(this.object, "id", id);
        return this;
    }
}
