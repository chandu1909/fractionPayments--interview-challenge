package com.fraction.payments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

/**
 * @Author: Chandra sekhar polavarapi
 * @Description: Entity class for the Accounts Object
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {

    @Column
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID primaryAccountNumber;
    @Column
    private double creditLimit;
    @Column
    private double accountBalance = 0.0;
    @ManyToOne
    @JoinColumn(name = "owner")
    private User user;

    // @OneToMany(targetEntity = Transaction.class, cascade =CascadeType.ALL, fetch = FetchType.EAGER)
    // @JoinColumn(name = "accountID", referencedColumnName = "primaryAccountNumber")

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Transaction> transactions;

    public Account(double creditLimit, double accountBalance, User user) {
        this.creditLimit = creditLimit;
        this.accountBalance = accountBalance;
        this.user = user;
    }
}
