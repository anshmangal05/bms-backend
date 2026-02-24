package com.example.bms.service;

import com.example.bms.constant.ProductConstant;
import com.example.bms.model.Customer;
import com.example.bms.model.PolicyBooking;
import com.example.bms.model.PolicyUpdateRequest;
import com.example.bms.model.event.*;
import com.example.bms.repository.CustomerRepository;
import com.example.bms.response.ApiResponse;
import com.example.bms.service.kafka.PolicyEventProducer;
import com.example.bms.validation.BookingValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;




@Service
public class CustomerService {
    @Autowired
    private PolicyEventProducer policyEventProducer;

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public ApiResponse createCustomer(Customer customer) {
        ApiResponse response = new ApiResponse();

        try {
            if (customer == null) {
                response.setStatusCode(400);
                response.setStatusMessage("REQUEST BODY MISSING");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            //name should not be blank
            if (customer.getName() == null || customer.getName().isBlank()) {
                response.setStatusCode(400);
                response.setStatusMessage("NAME IS REQUIRED");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            if (customer.getName().length() < 2 || customer.getName().length() > 25) {
                response.setStatusCode(400);
                response.setStatusMessage("NAME SHOUD BE BETWEEN 3 TO 25 LETTERS");
                response.setData("NO DATA AVAILABLE");
                return response;
            }


            if (customer.getDob() == null) {
                response.setStatusCode(400);
                response.setStatusMessage("DOB IS REQUIRED");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            //dob should not be in future
            if (customer.getDob().isAfter(LocalDate.now())) {
                response.setStatusCode(400);
                response.setStatusMessage("INVALID DOB");
                response.setData("NO DATA AVAILABLE");
                return response;
            }

            //Customer saved = repository.save(customer);

            try{
                PolicyCreateCustomerEvent event = new PolicyCreateCustomerEvent(
                        customer.getName(), customer.getDob(),LocalDateTime.now()
                );
                policyEventProducer.sendCreateCustomerEvent(event);
                System.out.println("customer created!");
            } catch (Exception kafkaEx) {
                System.out.println("Kafka send failed: " + kafkaEx.getMessage());
            }

            response.setStatusCode(201);
            response.setStatusMessage("CUSTOMER CREATED");
            response.setData(customer);
            return response;

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setStatusMessage("DB ERROR -> FAILED TO SAVE CUSTOMER");
            response.setData("NO DATA AVAILABLE");
            return response;
        }
    }

    public ApiResponse getCustomerByIdService(String customerId) {
        ApiResponse response = new ApiResponse();
        try {
            if (customerId == null || customerId.isBlank()) {
                response.setStatusCode(400);
                response.setStatusMessage("CUSTOMER ID REQUIRED");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            Customer customer = repository.findById(customerId).orElse(null);

            if (customer == null) {
                response.setStatusCode(404);
                response.setStatusMessage("CUSTOMER NOT FOUND");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            response.setStatusCode(200);
            response.setStatusMessage("CUSTOMER FETCHED");
            response.setData(customer);
            return response;

        } catch (Exception e) {

            response.setStatusCode(500);
            response.setStatusMessage("INTERNAL SERVER ERROR");
            response.setData("NO DATA AVAILABLE");
            return response;
        }
    }

    public ApiResponse getPolicyStatusService(String customerId, String bookingId) {
        ApiResponse response = new ApiResponse();

        try {
            if (customerId == null || customerId.isBlank()) {
                response.setStatusCode(400);
                response.setStatusMessage("CUSTOMER ID REQUIRED");
                response.setData("NO DATA AVAILABLE");
                return response;
            }

            if (bookingId == null || bookingId.isBlank()) {
                response.setStatusCode(400);
                response.setStatusMessage("BOOKING ID HEADER IS REQUIRED");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            Customer customer = repository.findById(customerId).orElse(null);

            if (customer == null) {
                response.setStatusCode(404);
                response.setStatusMessage("CUSTOMER NOT FOUND");
                response.setData("NO DATA AVAILABLE");
                return response;
            }

            if (customer.getBookings() == null || customer.getBookings().isEmpty()) {
                response.setStatusCode(404);
                response.setStatusMessage("NO BOOKINGS FOUND");
                response.setData("NO DATA AVAILABLE");
                return response;
            }

            PolicyBooking booking = customer.getBookings().stream()
                    .filter(b -> bookingId.equals(b.getBookingId()))
                    .findFirst()
                    .orElse(null);

            if (booking == null) {
                response.setStatusCode(404);
                response.setStatusMessage("BOOKING ID NOT FOUND");
                response.setData("NO DATA AVAILABLE");
                return response;
            }

            // calculate runtime status
            String runtimeStatus = calculateStatus(booking);
            booking.setStatus(runtimeStatus);

            response.setStatusCode(200);
            response.setStatusMessage("POLICY STATUS FOUND");
            response.setData(booking);

            return response;

        } catch (Exception e) {

            response.setStatusCode(500);
            response.setStatusMessage("INTERNAL SERVER ERROR");
            response.setData("NO DATA AVAILABLE");
            return response;
        }
    }

    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    //add policy to existing customer
    public ApiResponse addPolicy(String customerId, PolicyBooking booking) {

        ApiResponse response = new ApiResponse();

        try {
            if (booking == null) {
                response.setStatusCode(400);
                response.setStatusMessage("BOOKING BODY MISSING");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            Customer customer = repository.findById(customerId).orElse(null);
            if (customer == null) {
                response.setStatusCode(404);
                response.setStatusMessage("CUSTOMER NOT FOUND");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            if (booking.getBookingId() == null || booking.getBookingId().isBlank()) {
                response.setStatusCode(400);
                response.setStatusMessage("BOOKING ID SHOULD NOT BE BLANK");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            // if customer exists and no bookings ... add an arraylist
            if (customer.getBookings() == null) {
                customer.setBookings(new ArrayList<>());
            }
            if (booking.getPolicyType() == null || booking.getPolicyType().isBlank()) {
                response.setStatusCode(400);
                response.setStatusMessage("POLICY TYPE REQUIRED");
                response.setData("NO DATA AVAILABLE");
                return response;
            }

            String type = booking.getPolicyType().toUpperCase();
            if (!List.of("HEALTH", "LIFE", "MOTOR", "TRAVEL").contains(type)) {
                response.setStatusCode(400);
                response.setStatusMessage("INVALID POLICY TYPE");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            if (booking.getStartDate() != null && booking.getEndDate() != null) {
                if (booking.getStartDate().isAfter(booking.getEndDate())) {
                    response.setStatusCode(400);
                    response.setStatusMessage("END DATE MUST BE AFTER START DATE");
                    response.setData("NO DATA AVAILABLE");
                    return response;
                }
            }
            booking.setPolicyType(type);
            String baseId = booking.getBookingId();

            if(baseId == null || baseId.isBlank()){
                baseId = "POL";
            }

            booking.setBookingId(generateUniqueBookingId(baseId));

            booking.setStatus("BOOKED");

//            customer.getBookings().add(booking);
//
//            Customer saved = repository.save(customer);

            try{
                PolicyAddEvent event = new PolicyAddEvent(
                        customerId,booking.getBookingId(), booking.getPolicyType(),booking.getStatus(),LocalDateTime.now()
                );
                policyEventProducer.sendPolicyAddEvent(event);
                System.out.println("kafka event sent");

            } catch (Exception kafkaEx) {
                System.out.println("Kafka send failed: " + kafkaEx.getMessage());
            }
            response.setStatusCode(201);
            response.setStatusMessage("POLICY ADDED");
            response.setData(booking);
            return response;
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setStatusMessage("INTERNAL SERVER ERROR");
            response.setData("NO DATA AVAILABLE");
            return response;
        }
    }

    public String calculateStatus(PolicyBooking booking) {
        LocalDate today = LocalDate.now();

        if (booking.getStartDate() == null || booking.getEndDate() == null) {
            return "BOOKED";
        }
        if (today.isBefore(booking.getStartDate())) {
            return "UPCOMING";
        } else if (today.isAfter(booking.getEndDate())) {
            return "EXPIRED";
        } else {
            return "ACTIVE";
        }
    }
    public ApiResponse issuePolicy(String customerId, String bookingId, LocalDate startDate) {
        ApiResponse response = new ApiResponse();

        try {
            if (startDate == null) {
                response.setStatusCode(400);
                response.setStatusMessage("START DATE REQUIRED");
               // response.setData("NO DATA AVAILABLE");
                return response;
            }
            Customer customer = repository.findById(customerId).orElse(null);

            if (customer == null || customer.getBookings() == null) {
                response.setStatusCode(400);
                response.setStatusMessage("CUSTOMER OR BOOKINGS NOT FOUND");
               // response.setData("NO DATA AVAILABLE");
                return response;
            }

            PolicyBooking booking = customer.getBookings().stream()
                    .filter(b -> b.getBookingId().equals(bookingId))
                    .findFirst()
                    .orElse(null);

            if (booking == null) {
                response.setStatusCode(404);
                response.setStatusMessage("BOOKING NOT FOUND");
                //response.setData("NO DATA AVAILABLE");
                return response;
            }
            // allow null or BOOKED
            if (booking.getStatus() != null &&
                    !"BOOKED".equalsIgnoreCase(booking.getStatus())) {

                response.setStatusCode(400);
                response.setStatusMessage("POLICY NOT IN BOOKED STATE");
                response.setData(booking);
                return response;
            }

            String type = booking.getPolicyType().toUpperCase();
            int termYears = POLICY_TERM_MAP.getOrDefault(type, 1);

            booking.setStartDate(startDate);
            booking.setEndDate(startDate.plusYears(termYears));
            booking.setStatus("ISSUED");
         //   response.setData("NO DATA AVAILABLE");

//            repository.save(customer);

            try {
                PolicyIssuedEvent event = new PolicyIssuedEvent(
                        customerId,
                        bookingId,
                        booking.getPolicyType(),
                        booking.getStartDate(),
                        booking.getEndDate(),
                        LocalDateTime.now()
                );

                policyEventProducer.sendPolicyIssuedEvent(event);
                System.out.println("Kafka event sent");

            } catch (Exception kafkaEx) {
                System.out.println("Kafka send failed: " + kafkaEx.getMessage());
            }
            response.setStatusCode(200);
            response.setStatusMessage("POLICY ISSUED");
            response.setData(booking);

            return response;


        } catch (Exception e) {
            response.setStatusCode(500);
            response.setStatusMessage("INTERNAL SERVER ERROR");
            response.setData("NO DATA AVAILABLE");
            return response;
        }
    }

    public ApiResponse getAllPoliciesService(String customerId) {
        ApiResponse response = new ApiResponse();
        try {
            if (customerId == null || customerId.isBlank()) {
                response.setStatusCode(400);
                response.setStatusMessage("CUSTOMER ID REQUIRED");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            Customer customer = repository.findById(customerId).orElse(null);

            if (customer == null) {
                response.setStatusCode(404);
                response.setStatusMessage("CUSTOMER NOT FOUND");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            if (customer.getBookings() == null || customer.getBookings().isEmpty()) {
                response.setStatusCode(404);
                response.setStatusMessage("NO POLICIES FOUND");
                response.setData("NO DATA AVAILABLE");
                return response;
            }

            customer.getBookings().forEach(b ->
                    b.setStatus(calculateStatus(b))
            );
            response.setStatusCode(200);
            response.setStatusMessage("POLICIES FETCHED");
            response.setData(customer.getBookings());
            return response;
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setStatusMessage("INTERNAL SERVER ERROR");
            response.setData("NO DATA AVAILABLE");
            return response;
        }
    }

    //for generating a unique id
    private String generateUniqueBookingId(String baseId) {
        String newId = baseId;
        while (repository.existsByBookingsBookingId(newId)) {
            newId = baseId + "-" + UUID.randomUUID()
                    .toString()
                    .substring(0, 4)
                    .toUpperCase();
        }
        return newId;
    }
    private static final Map<String, Integer> POLICY_TERM_MAP = Map.of("HEALTH", 10,
            "MOTOR", 5, "TERM", 25, "TRAVEL", 1 , "TW" ,10 , "INVESTMENT" ,15);

    public ApiResponse addPolicyGeneric(String customerId, PolicyBooking booking) {
        ApiResponse response = new ApiResponse();
        try {
            Customer customer = repository.findById(customerId).orElse(null);
            if (customer == null) {
                response.setStatusCode(404);
                response.setStatusMessage("CUSTOMER NOT FOUND");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            if (booking == null) {
                response.setStatusCode(400);
                response.setStatusMessage("BOOKING BODY MISSING");
                response.setData("NO DATA AVAILABLE");
                return response;
            }

            if (booking.getProductId() == null || !ProductConstant.PRODUCT_MAP.containsKey(booking.getProductId())) {
                response.setStatusCode(400);
                response.setStatusMessage("INVALID PRODUCT ID ");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            if (booking.getBookingData() == null || booking.getBookingData().isEmpty()) {
                response.setStatusCode(400);
                response.setStatusMessage("BOOKING BODY REQUIRED");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            if (customer.getBookings() == null) {
                customer.setBookings(new ArrayList<>());
            }

            String uniqueId = generateUniqueBookingId("POL");
            booking.setBookingId(uniqueId);

            // derive policyType from productId
            booking.setPolicyType(
                    ProductConstant.PRODUCT_MAP.get(booking.getProductId())
            );

            String validationError = BookingValidator.validateGenericBooking( booking);
            if (validationError != null ) {
                response.setStatusCode(400);
                response.setStatusMessage(validationError);
                return response;
            }
            booking.setStatus("BOOKED");

//            customer.getBookings().add(booking);
//            repository.save(customer);

            try{
                PolicyInsertEvent event = new PolicyInsertEvent(
                       customerId,booking, LocalDateTime.now()
                );
                policyEventProducer.sendInsertPolicyEvent(event);
                System.out.println("Policy Inserted!");
            } catch (Exception kafkaEx) {
                System.out.println("Kafka send failed: " + kafkaEx.getMessage());
            }

            response.setStatusCode(200);
            response.setStatusMessage("GENERIC POLICY ADDED");
            response.setData(booking);
            return response;

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setStatusMessage("INTERNAL SERVER ERROR");
            response.setData("NO DATA AVAILABLE");
            return response;
        }
    }
    public ApiResponse getBookingsByBoookingId (String bookingId){
        ApiResponse response = new ApiResponse();
        try {
            Customer customer = repository.findByBookingsBookingId(bookingId).orElse(null);

            if(customer == null){
                response.setStatusCode(404);
                response.setStatusMessage("booking  not found");
                response.setData("No data available");
                return response;
            }
            PolicyBooking found = null;
            for(PolicyBooking b: customer.getBookings()){
                if(bookingId.equals(b.getBookingId())){
                    found = b ;
                    break;
                }
            }
            if(found == null){
                response.setStatusCode(404);
                response.setStatusMessage("booking  not found");
                response.setData("No data available");
                return response;
            }
            response.setStatusCode(200);
            response.setStatusMessage("BOOKING FETCHED");
            response.setData(found);
            return response;

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setStatusMessage("INTERNAL SERVER ERROR");
            response.setData("NO DATA AVAILABLE");
            return response;
        }
    }
    public ApiResponse deleteCustomer(String customerId){
        ApiResponse  response= new ApiResponse();
        if(!repository.existsById(customerId)){
            response.setStatusCode(404);
            response.setStatusMessage("Customer Not Found");
            response.setData("No Data Available");
            return response;
        }
        try {
            CustomerDeleteEvent event = new CustomerDeleteEvent( customerId,LocalDateTime.now());
                policyEventProducer.sendCustomerDeleteEvent(event);

                response.setStatusCode(202);
                response.setStatusMessage("Delete Request Sent by Kafka");
                return response;

        } catch (Exception e) {
                response.setStatusCode(500);
                response.setStatusMessage("Failed to send event ");
                return response;
        }

    }
    public ApiResponse deletePolicy(String customerId,String bookingId){
            ApiResponse response = new ApiResponse();
            Customer customer = repository.findById(customerId).orElse(null);

            try {
                if (customer == null) {
                    response.setStatusCode(404);
                    response.setStatusMessage("CUSTOMER NOT FOUND");
                    return response;
                }
                PolicyBooking policy = customer.getBookings().stream().
                        filter(b->b.getBookingId().equals(bookingId))
                        .findFirst().orElse(null);
                if(policy==null){
                    response.setStatusCode(404);
                    response.setStatusMessage("Policy Not Found");
                    return response;
                }
                //check that it is expired or not
                    if(!policy.getEndDate().isBefore(LocalDate.now())){
                        //policy cannot be deleted
                        response.setStatusCode(400);
                        response.setStatusMessage("POLICY NOT EXPIRED -CANNOT DELETE");
                        return response;
                    }

                    policyEventProducer.sendPolicyDeleteEvent(new PolicyDeleteEvent(customerId,bookingId,LocalDateTime.now()));
                    response.setStatusCode(202);
                    response.setStatusMessage("Policy Deleted Successfully");
                    return response;


            } catch (Exception e) {
                response.setStatusCode(500);
                response.setStatusMessage("Failed to send event ");
                return response;
            }


    }
    public ApiResponse updatePolicy(String customerId, String bookingId, PolicyUpdateRequest request){
        ApiResponse response = new ApiResponse();
        try{
            if(request == null){
                response.setStatusCode(400);
                response.setStatusMessage("Request Body is missing");
                return  response;
            }
            Customer customer = repository.findById(customerId).orElse(null);
            if (customer == null){
                response.setStatusCode(404);
                response.setStatusMessage("Customer Not Found");
                return response;
            }
            PolicyBooking booking = customer.getBookings().stream()
                    .filter(b->b.getBookingId().equals(bookingId))
                    .findFirst()
                    .orElse(null);
            if(booking == null){
               response.setStatusCode(404);
               response.setStatusMessage("Policy Not Found");
               return response;
            }
            policyEventProducer.sendUpdatePolicyEvent(new PolicyUpdateEvent( customerId,bookingId,request,LocalDate.now()));
            response.setStatusCode(200);
            response.setStatusMessage("updated policy event ");
            return response;
        } catch (Exception e) {
           response.setStatusCode(500);
           response.setStatusMessage("Failed to update event");
           return response;
        }
    }
    public ApiResponse renewPolicy(String customerId, String bookingId, LocalDate newEndDate, Map<String,Object>bookingData){
        ApiResponse response = new ApiResponse();
        Customer customer = repository.findById(customerId).orElse(null);
        if(customer==null){
            response.setStatusCode(404);
            response.setStatusMessage("Customer Not Found");
            return response;
        }


        PolicyBooking booking = customer.getBookings().stream()
                .filter(b->b.getBookingId().equals(bookingId))
                .findFirst()
                .orElse(null);

        if(booking==null){
            response.setStatusCode(404);
            response.setStatusMessage("booking not found");
            return response;
        }
        policyEventProducer.sendRenewPolicyEvent(new PolicyRenewEvent(customerId,bookingId,newEndDate,bookingData));

        response.setStatusCode(200);
        response.setStatusMessage("Policy renewed!!");
        return response;
    }
    public ApiResponse healthPolicyStatus(String customerId, String bookingId){
        ApiResponse response = new ApiResponse();
        try {
            if (customerId == null || customerId.isBlank()) {
                response.setStatusCode(400);
                response.setStatusMessage("CUSTOMER ID REQUIRED");
                response.setData("NO DATA AVAILABLE");
                return response;
            }

            if (bookingId == null || bookingId.isBlank()) {
                response.setStatusCode(400);
                response.setStatusMessage("BOOKING ID HEADER IS REQUIRED");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            Customer customer = repository.findById(customerId).orElse(null);

            if (customer == null) {
                response.setStatusCode(404);
                response.setStatusMessage("CUSTOMER NOT FOUND");
                response.setData("NO DATA AVAILABLE");
                return response;
            }

            if (customer.getBookings() == null || customer.getBookings().isEmpty()) {
                response.setStatusCode(404);
                response.setStatusMessage("NO BOOKINGS FOUND");
                response.setData("NO DATA AVAILABLE");
                return response;
            }

            PolicyBooking booking = customer.getBookings().stream()
                    .filter(b -> bookingId.equals(b.getBookingId()))
                    .findFirst()
                    .orElse(null);

            if (booking == null) {
                response.setStatusCode(404);
                response.setStatusMessage("BOOKING ID NOT FOUND");
                response.setData("NO DATA AVAILABLE");
                return response;
            }
            if(!"HEALTH".equals(booking.getPolicyType())) {
                response.setStatusCode(400);
                response.setStatusMessage("THIS IS NOT A HEALTH POLICY");
                return response;
            }

            // calculate runtime status
            String runtimeStatus = calculateStatus(booking);
            booking.setStatus(runtimeStatus);

            response.setStatusCode(200);
            response.setStatusMessage("POLICY STATUS FOUND");
            response.setData(booking);

            return response;
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setStatusMessage("INTERNAL SERVER ERROR");
            response.setData("NO DATA AVAILABLE");
            return response;
        }

    }

}