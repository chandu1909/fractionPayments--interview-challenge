package com.fraction.payments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

/**
 * @Author: Chandra sekhar Polavarapu
 * @Description: This is the entity class for the user object and its attributes in the database. Lombok has been used
 * to avoid boilerplate code and UUID has been used to generate the random userID.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Column
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID userID;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Account> accounts;

    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
