package com.fraction.payments.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

/**
 * @Author : Chandra sekhar Polavarapu
 * @Description: Entity class for the Transaction object
 */

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
    @Column
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID transactionId;
    @Column
    private double credit;
    @Column
    private double debit;

    @ManyToOne
    @JoinColumn(name = "accountID")
    private Account account;

    public Transaction(double credit, double debit, Account account) {
        this.credit = credit;
        this.debit = debit;
        this.account = account;
    }
}
