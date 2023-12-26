package com.arturjarosz.task.client.infrastructure.repository;

import com.arturjarosz.task.client.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

}
