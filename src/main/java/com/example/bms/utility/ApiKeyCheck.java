package com.example.bms.utility;


import com.example.bms.constant.AppConstant;
import com.example.bms.response.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyCheck {

    public static Boolean isValidApiKey(String apiKey) {
        return AppConstant.API_KEY_VALUE.equals(apiKey);
    }

    public static ApiResponse inValidKey() {
        ApiResponse response = new ApiResponse();
        response.setStatusCode(401);
        response.setStatusMessage("INVALID API KEY");
        response.setData("NO DATA AVAILABLE");
        return response;
    }
}
