package com.arturjarosz.task.user.infrastructure.repository;

import com.arturjarosz.task.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAuthId(String authId);

    /**
     * Takes a PostgreSQL advisory lock for given authId, held until the current transaction ends, so concurrent
     * requests creating the same user run one after another. Touches no table, so it needs no schema.
     */
    @Query(value = "SELECT 1 FROM (SELECT pg_advisory_xact_lock(hashtext(:authId))) AS advisory_lock",
            nativeQuery = true)
    Integer lockAuthId(@Param("authId") String authId);
}
