package com.fraction.payments.respository;

import com.fraction.payments.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    //List<Account> findByOwner(UUID owner);
}

