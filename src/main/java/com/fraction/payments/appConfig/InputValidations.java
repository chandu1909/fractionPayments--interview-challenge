package com.fraction.payments.appConfig;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Chandra sekhar Polavarapu
 *
 * @Description: This class is responsible for validate he inputs of incoming requests before
 * handing the transactions over to service layer
 *
 * @Consideration: In real world the validation would be more specific to issuer and the network such as origin of transaction, country code,
 * payment model code, ACH etc. for
 * interview purpose this class is representing only basic validation.
 */
@Component
public class InputValidations {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputValidations.class);
    private static final Set<String> depositInputKeySet = new HashSet<>();
    private static final Set<String> payBalanceInputKeySet = new HashSet<>();

    //load the map at beginning of application
    static {
        depositInputKeySet.add(FractionConstants.USERID);
        depositInputKeySet.add(FractionConstants.DEBIT);
        depositInputKeySet.add(FractionConstants.PAN);
        depositInputKeySet.add(FractionConstants.IDEMPOTENCY_KEY);

        payBalanceInputKeySet.add(FractionConstants.USERID);
        payBalanceInputKeySet.add(FractionConstants.CREDIT_TRANSACTION);
        payBalanceInputKeySet.add(FractionConstants.PAN);
        payBalanceInputKeySet.add(FractionConstants.IDEMPOTENCY_KEY);
    }

    //validateWithdrawInput
    public boolean isWithdrawValid(JSONObject inputObject) {

        AtomicBoolean result = new AtomicBoolean(true);

        depositInputKeySet.stream().forEach(element -> {
            if (!inputObject.containsKey(element)) {
                result.set(false);
                LOGGER.error("Missing required input field: {}", element);
            }
        });
        return result.get();
    }

    //validate credit input
    public boolean isCreditValid(JSONObject inputObject) {

        AtomicBoolean result = new AtomicBoolean(true);

        payBalanceInputKeySet.stream().forEach(element -> {
            if (!inputObject.containsKey(element)) {
                result.set(false);
                LOGGER.error("Missing required input field: {}", element);
            }
        });
        return result.get();
    }


}
