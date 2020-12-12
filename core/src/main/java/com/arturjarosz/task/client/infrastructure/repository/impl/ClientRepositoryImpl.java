package com.arturjarosz.task.client.infrastructure.repository.impl;

import com.arturjarosz.task.client.infrastructure.repository.ClientRepository;
import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.client.model.QClient;
import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ClientRepositoryImpl extends GenericJpaRepositoryImpl<Client, QClient>
        implements ClientRepository {

    private final static QClient PRIVATE_CLIENT = QClient.client;

    public ClientRepositoryImpl() {
        super(PRIVATE_CLIENT);
    }
}
