package com.fraction.payments.responseBuilder;

import com.fraction.payments.appConfig.FractionConstants;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

/**
 * @Author: Chandra sekhar Polavarapu
 *
 * @Description: The Requirement says the responses will be collected by a frontend dashboard.
 * A consistent format, status codes and messages will help in frontend to recognize and process the responses and
 * reveals less information in the network as we have Notification service to send the direct details to customer over phone/email.
 */
@Component
public class ResponseBuilder {

    public JSONObject blanketTransactionFailed() {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(FractionConstants.STATUS, "FAILED");
        jsonResponse.put(FractionConstants.CODE, 1);
        jsonResponse.put(FractionConstants.REASON_FOR_FAILURE,"");
        return jsonResponse;
    }
    public JSONObject getInvalidInputResponse() {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(FractionConstants.STATUS, "FAILED");
        jsonResponse.put(FractionConstants.REASON_FOR_FAILURE, "INVALID INPUT");
        jsonResponse.put(FractionConstants.CODE, 1);
        return jsonResponse;
    }
    public JSONObject transactionSuccess() {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(FractionConstants.STATUS, "SUCCESS");
        jsonResponse.put(FractionConstants.CODE, 0);
        return jsonResponse;
    }
    public JSONObject getDuplicateResponse(){
        JSONObject duplicateResponse = new JSONObject();
        duplicateResponse.put(FractionConstants.STATUS, "FAILED");
        duplicateResponse.put(FractionConstants.CODE, 1);
        duplicateResponse.put(FractionConstants.REASON_FOR_FAILURE,"DUPLICATE TRANSACTION");
        return duplicateResponse;
    }

}
