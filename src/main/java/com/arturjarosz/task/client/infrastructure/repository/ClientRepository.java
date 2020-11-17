package com.arturjarosz.task.client.infrastructure.repository;

import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractBaseRepository;

import java.util.Collection;
import java.util.List;

public interface ClientRepository extends AbstractBaseRepository<Client> {

    //TODO implement with QClass
    public Client load(Long id);

    public List<Client> loadAll();

    public void saveAll(Collection<Client> aggregates);

    public void remove(Long id);
}
