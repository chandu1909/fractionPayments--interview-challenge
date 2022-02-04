package com.fraction.payments.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author : Chandra sekhar Polavarapu
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransactionStatus {

    private Transaction transaction;
    private String status;
    private String message;
}
