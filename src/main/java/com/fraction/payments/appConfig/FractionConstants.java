package com.fraction.payments.appConfig;

/**
 * @author Chandra Sekhar Polavarapu
 */
public final class FractionConstants {
    public static final String PAN = "primary_account_number";
    public static final String ACCOUNT_BALANCE = "account_balance";
    public static final String AVAILABLE_CREDIT = "credit_available";
    public static final String CREDIT_LIMIT = "credit_limit";
    public static final String REASON_FOR_FAILURE = "reason_for_failure";
    public static final String STATUS = "status";
    public static final String CODE = "code";

    //custom incoming input fields
    public static final String USERID = "userID";
    public static final String DEBIT = "debit";
    public static final String TRANSACTION_TYPE = "transaction_type";
    public static final String CREDIT_TRANSACTION = "credit";
    public static final String DEBIT_TRANSACTION = "DEBIT";
    public static final String IDEMPOTENCY_KEY = "idempotency-key";

    private FractionConstants() {
    }
}
