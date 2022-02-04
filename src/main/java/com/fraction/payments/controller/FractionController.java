package com.fraction.payments.controller;

import com.fraction.payments.appConfig.FractionConstants;
import com.fraction.payments.appConfig.InputValidations;
import com.fraction.payments.coreService.UserService;
import com.fraction.payments.entities.User;
import com.fraction.payments.responseBuilder.ResponseBuilder;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/fraction")
public class FractionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FractionController.class);

    //@TODO  we need to implement cache cleaning mechanism based on expiry (30 seconds)! -->> Right now am NOT CLEANING THE CACHE
    Map<String, Integer> inMemoryCache = new HashMap<>();

    @Autowired
    UserService userService;

    @Autowired
    InputValidations inputValidations;

    @Autowired
    ResponseBuilder responseBuilder;


    /**
     * Expects USER ID in the request headers
     * @param headers
     * @return
     */
    @GetMapping("/get-balance")
    public ResponseEntity<JSONObject> getBalances(@RequestHeader Map<String, String> headers) {
        return userService.getBalance(headers.get("userid"));
    }

    /**
     * @param transaction
     * @return
     * @Description: This method validates the incoming input and then hand-over to service layer if success
     */
    @PostMapping("/draw-funds")
    public ResponseEntity<JSONObject> drawFunds(@RequestBody JSONObject transaction) {
        boolean duplicate = isDuplicate(transaction);
        if (inputValidations.isWithdrawValid(transaction) && !duplicate) {
            return userService.drawFundsFromAccount(transaction);
        }
        return duplicate ? new ResponseEntity<>(responseBuilder.getDuplicateResponse(), HttpStatus.NOT_ACCEPTABLE)
                : new ResponseEntity<>(responseBuilder.getInvalidInputResponse(), HttpStatus.NOT_ACCEPTABLE);


    }

    /**
     * Exposing end point to pay existing balance on the account
     * @param transaction
     * @return
     */
    @PostMapping("/pay-balance")
    public ResponseEntity<JSONObject> payBalance(@RequestBody JSONObject transaction) {

        boolean duplicate = isDuplicate(transaction);
        if (inputValidations.isCreditValid(transaction) && !duplicate) {
            return userService.payDownAccountBalance(transaction);
        }
        return duplicate ? new ResponseEntity<>(responseBuilder.getDuplicateResponse(), HttpStatus.NOT_ACCEPTABLE)
                : new ResponseEntity<>(responseBuilder.getInvalidInputResponse(), HttpStatus.NOT_ACCEPTABLE);
    }

    //Utility Endpoints for Admins
    @GetMapping("/get-all-users")
    public ResponseEntity<List<User>> getAllUsers(){

        return userService.getAllAccounts();
    }

    @PostMapping("/adduser")
    public User addUser(@RequestBody User user) throws Exception {

        LOGGER.info("calling the service class");
        return userService.execute(user);
    }

    //checks if the transaction is duplicate using the idempotency key
    private boolean isDuplicate(JSONObject transaction) {

        String idempotencyKey = transaction.get(FractionConstants.IDEMPOTENCY_KEY).toString();
        if (inMemoryCache.containsKey(idempotencyKey)) {
            LOGGER.error("Duplicate Transaction !");
            return true;
        }
        //Typical HTTP connection timeout happens in 30 seconds so we don't need to keep it in cache for more than 35 seconds
        inMemoryCache.put(idempotencyKey, 35);

        return false;
    }


}
