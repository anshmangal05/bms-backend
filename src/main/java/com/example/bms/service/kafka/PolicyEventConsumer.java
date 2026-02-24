package com.example.bms.service.kafka;


import com.example.bms.model.Customer;
import com.example.bms.model.PolicyBooking;
import com.example.bms.model.PolicyUpdateRequest;
import com.example.bms.model.event.*;
import com.example.bms.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class PolicyEventConsumer {
    @Autowired
    private CustomerRepository repository;

@KafkaListener(topics="policy-issued-topic",groupId = "policy-group")
    public void listen(PolicyIssuedEvent event ) {
    Customer customer = repository.findById(event.getCustomerId()).orElseThrow();
    if (customer.getBookings() != null) {
        customer.getBookings().forEach(b -> {
            if (b.getBookingId().equals(event.getBookingId())) {
                b.setStatus("ISSUED");
                b.setStartDate(event.getStartDate());
                b.setEndDate(event.getEndDate());
            }
        });
    }
    repository.save(customer);
    System.out.println("policy received from kafka(issue-policy) : " + event);
}

@KafkaListener(topics = "add-policy-topic",groupId = "policy-group")
    public void add(PolicyAddEvent event){
    Customer customer = repository.findById(event.getCustomerId()).orElseThrow();
    PolicyBooking booking = new PolicyBooking();
    booking.setBookingId(event.getBookingId());
    booking.setPolicyType(event.getPolicyType());
    booking.setStatus(event.getStatus());

    if(customer.getBookings()==null){
        customer.setBookings(new ArrayList<>());

    }
    customer.getBookings().add(booking);

    repository.save(customer);
    System.out.println("policy received from kafka(add-policy): " + event);

}
@KafkaListener(topics="create-customer-topic",groupId = "policy-group")
    public void add(PolicyCreateCustomerEvent event){
    Customer customer = new Customer();
    customer.setName(event.getName());
    customer.setDob(event.getDob());
    customer .setBookings(new ArrayList<>());
     repository.save(customer);
    System.out.println("customer added from kafka(create-customer) : "+ event );

}

@KafkaListener(topics="insert-policy-topic",groupId = "policy-group")
    public  void insert(PolicyInsertEvent event){
    Customer customer = repository.findById(event.getCustomerId()).orElseThrow();
    if(customer.getBookings()==null){
        customer.setBookings(new ArrayList<>());
    }
    boolean exists = customer.getBookings().stream()
            .anyMatch(b -> b.getBookingId()
                    .equals(event.getBooking().getBookingId()));

    if (exists) return;

    customer.getBookings().add(event.getBooking());

    repository.save(customer);
    System.out.println(" policy inserted from Kafka" );


}

@KafkaListener(topics = "customer-delete-topic",groupId = "policy-group")
    public  void deleteCustomer(CustomerDeleteEvent event){
    repository.deleteById(event.getCustomerId());

    System.out.println("customer deleted by kafka :" + event.getCustomerId());
}


@KafkaListener(topics = "delete-policy-topic",groupId = "policy-group")
    public void deletePolicy(PolicyDeleteEvent event){
    Customer customer = repository.findById(event.getCustomerId()).orElse(null);
    if(customer==null ) return ;
    customer.getBookings().removeIf(b ->b.getBookingId().equals(event.getBookingId()));

    repository.save(customer);
    System.out.println("policy deleted via kafka: " + event.getBookingId());
}
@KafkaListener(topics = "update-policy-topic",groupId = "policy-update-group")
    public void updatePolicy(PolicyUpdateEvent event){
    Customer customer = repository.findById(event.getCustomerId()).orElse(null);
    if(customer == null ) {
        System.out.println("customer not found");
        return;
    }
    for(PolicyBooking b : customer.getBookings()){
        if(b.getBookingId().equals(event.getBookingId())){
            PolicyUpdateRequest upd = event.getUpdate();

            if (upd.getEndDate() != null) {
                b.setEndDate(upd.getEndDate());
            }
            if (upd.getBookingData() != null && !upd.getBookingData().isEmpty()) {
                if (b.getBookingData() == null) {
                    b.setBookingData(new HashMap<>());
                }
                b.getBookingData().putAll(upd.getBookingData());
            }

        }
    }
    repository.save(customer);
    System.out.println("policy updated via kafka ");
       }

       @KafkaListener(topics = "renew-policy-topic" , groupId = "policy-group")
           public void renewPolicy(PolicyRenewEvent event){
        Customer customer = repository.findById(event.getCustomerId()).orElse(null);
        if(customer==null){
            System.out.println("Customer Not Found");
            return;
        }
           for (PolicyBooking b : customer.getBookings()) {
               if (b.getBookingId().equals(event.getBookingId())) {

                   b.setStartDate(LocalDate.now());

                   if(event.getNewEndDate() != null) {
                       b.setEndDate(event.getNewEndDate());
                   }
                   if(event.getBookingData() != null) {
                       Map<String,Object> existing = b.getBookingData();
                       if(existing == null) existing = new HashMap<>();
                       existing.putAll(event.getBookingData());
                       b.setBookingData(existing);
                   }
               }
           }
           repository.save(customer);
       }
}
