package com.example.bms.controller;



import com.example.bms.constant.AppConstant;
import com.example.bms.model.Customer;
import com.example.bms.model.PolicyBooking;
import com.example.bms.model.PolicyUpdateRequest;
import com.example.bms.response.ApiResponse;
import com.example.bms.service.CustomerService;
import com.example.bms.utility.ApiKeyCheck;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.Map;

import static com.example.bms.utility.ApiKeyCheck.inValidKey;
import static com.example.bms.utility.ApiKeyCheck.isValidApiKey;


@RestController
@RequestMapping("/api")
public class CustomerController {
    private final CustomerService service;
    private  final ApiKeyCheck apiKeyCheck;

    public CustomerController(CustomerService service, ApiKeyCheck apiKeyCheck) {
        this.service = service;
        this.apiKeyCheck = apiKeyCheck;
    }


    @PostMapping("/createCustomer")
    public ResponseEntity<ApiResponse> createCustomer(
            @RequestHeader Map<String, String> headers,
            @RequestBody Customer customer) {

        String apiKey = headers.get(AppConstant.API_KEY_HEADER);

        if (apiKey == null || !isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).body(inValidKey());
        }

        ApiResponse response = service.createCustomer(customer);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @GetMapping("/customer")
    public ResponseEntity<ApiResponse> getCustomer(
            @RequestHeader Map<String, String> headers) {

        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
        String customerId = headers.get("customer-id");

        if (apiKey == null || !isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).body(inValidKey());
        }

        ApiResponse response = service.getCustomerByIdService(customerId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // policy status for that booking id
    @GetMapping("/policyStatus")
    public ResponseEntity<ApiResponse> getPolicyStaus(
            @RequestHeader Map<String, String> headers
    ) {

        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
        String bookingId = headers.get("booking-id");
        String customerId = headers.get("customer-id");

        if (apiKey == null || !isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).body(inValidKey());
        }

        ApiResponse response = service.getPolicyStatusService(customerId, bookingId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

//    @PostMapping("/addPolicy")
//    public ResponseEntity<ApiResponse> addPolicy(
//            @RequestHeader Map<String, String> headers,
//            @RequestBody PolicyBooking booking) {
//
//        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
//        String customerId = headers.get("customer-id");
//
//        if (apiKey == null || !isValidApiKey(apiKey)) {
//            return ResponseEntity.status(401).body(inValidKey());
//        }
//
//        ApiResponse response = service.addPolicy(customerId, booking);
//
//        return ResponseEntity
//                .status(response.getStatusCode())
//                .body(response);
//    }


    @GetMapping("/policies")
    public ResponseEntity<ApiResponse> getAllPolicies(
            @RequestHeader Map<String, String> headers) {

        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
        String customerId = headers.get("customer-id");

        if (apiKey == null || !isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).body(inValidKey());
        }

        ApiResponse response = service.getAllPoliciesService(customerId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/issuePolicy")
    public ResponseEntity<ApiResponse> policyIssue(
            @RequestHeader Map<String, String> headers,
            @RequestBody Map<String, Object> body) {

        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
        String customerId = headers.get("customer-id");
        String bookingId = headers.get("booking-id");

        if (apiKey == null || !isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).body(inValidKey());
        }

        if (customerId == null || bookingId == null) {
            ApiResponse res = new ApiResponse();
            res.setStatusCode(400);
            res.setStatusMessage("HEADERS MISSING");
            res.setData("NO DATA");
            return ResponseEntity.badRequest().body(res);
        }

        Object startDateObj = body.get("startDate");

        if (startDateObj == null) {
            ApiResponse res = new ApiResponse();
            res.setStatusCode(400);
            res.setStatusMessage("startDate REQUIRED");
            res.setData("NO DATA");
            return ResponseEntity.badRequest().body(res);
        }

        LocalDate startDate = LocalDate.parse(startDateObj.toString());

        ApiResponse response =
                service.issuePolicy(customerId, bookingId, startDate);

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping("/insertPolicy")
    public ResponseEntity<ApiResponse> addGenericPolicy(
            @RequestHeader Map<String, String> headers,
            @RequestBody PolicyBooking booking) {

        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
        String customerId = headers.get("customer-id");

        if (apiKey == null || !isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).body(inValidKey());
        }

        ApiResponse response = service.addPolicyGeneric(customerId, booking);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/booking")
    public ResponseEntity<ApiResponse> getBookingByBookingId(@RequestHeader Map<String, String> headers) {
        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
        String customerId = headers.get("customer-id");
        String bookingId = headers.get("booking-id");
        if (apiKey == null || !isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).body(inValidKey());
        }

        ApiResponse response = service.getBookingsByBoookingId(bookingId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/deleteCustomer")
    public ResponseEntity<ApiResponse> deleteCustomer(@RequestHeader Map<String, String> headers) {
        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
        String customerId = headers.get("customer-id");

        if (apiKey == null || !isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).body(inValidKey());
        }
        ApiResponse response = service.deleteCustomer(customerId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/deletePolicy")
    public ResponseEntity<ApiResponse> deletePolicy(@RequestHeader Map<String, String> headers) {
        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
        String customerId = headers.get("customer-id");
        String bookingId = headers.get("booking-id");
        if (apiKey == null || !isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).body(inValidKey());
        }

        ApiResponse response = service.deletePolicy(customerId, bookingId);

        return ResponseEntity.status(response.getStatusCode()).body(response);

    }

    @PutMapping("/updatePolicy")
    public ResponseEntity<ApiResponse> updatePolicy(@RequestHeader Map<String, String> headers, @RequestBody PolicyUpdateRequest request) {
        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
        String customerId = headers.get("customer-id");
        String bookingId = headers.get("booking-id");
        if (apiKey == null || !isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).body(inValidKey());
        }
        ApiResponse response = service.updatePolicy(customerId, bookingId, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/renewPolicy")
    public ResponseEntity<ApiResponse> renewPolicy(@RequestHeader Map<String, String> headers, @RequestBody Map<String,Object> body) {
        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
        String customerId = headers.get("customer-id");
        String bookingId = headers.get("booking-id");
        LocalDate newEndDate = LocalDate.parse(body.get("newEndDate").toString());
        Map<String,Object> bookingData = (Map<String, Object>) body.get("bookingData");
        if (apiKey == null || !isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).body(inValidKey());
        }
        ApiResponse response = service.renewPolicy(customerId, bookingId,newEndDate,bookingData);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/health/policyStatus")
    public ResponseEntity <ApiResponse> healthPolicyStatus(@RequestHeader Map<String,String> headers){
        String apiKey = headers.get(AppConstant.API_KEY_HEADER);
        String customerId = headers.get("customer-id");
        String bookingId = headers.get("booking-id");
        if(apiKey==null || !isValidApiKey(apiKey)){
            return ResponseEntity.status(401).body(inValidKey());
        }
        ApiResponse response= service.healthPolicyStatus(customerId,bookingId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
