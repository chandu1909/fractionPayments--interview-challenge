package com.fraction.payments.coreService;

import com.fraction.payments.entities.User;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * A layer of abstraction to the real service class and we can also use this interface as generic class create different implementations
 */
@Service
public interface UserService {
    User execute(User user) throws Exception;

    ResponseEntity<JSONObject> getBalance(String userID);

    ResponseEntity<JSONObject> drawFundsFromAccount(JSONObject transaction);

    ResponseEntity<JSONObject> payDownAccountBalance(JSONObject transaction);

    ResponseEntity<List<User>> getAllAccounts();

}
