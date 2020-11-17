package com.arturjarosz.task.client.infrastructure.repository.impl;

import com.arturjarosz.task.client.infrastructure.repository.ClientRepository;
import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.sharedkernel.infrastructure.impl.AbstractBaseRepositoryImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class ClientRepositoryImpl extends AbstractBaseRepositoryImpl<Client>
        implements ClientRepository {

    @PersistenceContext
    private EntityManager em;

    public Client load(Long id) {
        //TODO implement with QClass
        return null;
    }

    public List<Client> loadAll() {
        //TODO implement with QClass
        return null;
    }

    public void saveAll(Collection<Client> aggregates) {
        //TODO implement with QClass

    }

    public void remove(Long id) {
        //TODO implement with QClass
    }
}
