package com.fraction.payments.respository;

import com.fraction.payments.entities.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.beans.Transient;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRespository extends JpaRepository<Account, UUID> {

    @Modifying
    @Transactional
    @Query("Update Account account set account.accountBalance = :newBalance where account.primaryAccountNumber = :pan")
    int updateBalance(double newBalance, UUID pan);

}
