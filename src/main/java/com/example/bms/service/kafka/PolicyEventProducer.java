package com.example.bms.service.kafka;

import com.example.bms.model.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PolicyEventProducer {
    @Autowired
    private final KafkaTemplate<String,Object>kafkaTemplate;


    public PolicyEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    @Async
    public void sendPolicyIssuedEvent(PolicyIssuedEvent event){
        kafkaTemplate.send("policy-issued-topic",event);
        System.out.println("sent policy issued event : "+event.getBookingId());
    }
    @Async
    public void sendPolicyAddEvent(PolicyAddEvent event){
        kafkaTemplate.send("add-policy-topic",event);
        System.out.println("sent policy add event : " + event.getBookingId());
    }
    @Async
    public void sendCreateCustomerEvent(PolicyCreateCustomerEvent event){
        kafkaTemplate.send("create-customer-topic",event);
        System.out.println("create customer event name :  " + event.getName());
    }
    @Async
    public void sendInsertPolicyEvent(PolicyInsertEvent event){
        kafkaTemplate.send("insert-policy-topic",event);
        System.out.println("insert policy event name " + event.getBooking());
    }

    @Async
    public void sendCustomerDeleteEvent(CustomerDeleteEvent event){
        kafkaTemplate.send("customer-delete-topic",event);
        System.out.println("customer delete event name " + event.getCustomerId());
    }

    @Async
    public void sendPolicyDeleteEvent(PolicyDeleteEvent event){
        kafkaTemplate.send("delete-policy-topic",event);
        System.out.println("delete policy event name " + event.getBookingId());
    }
    @Async
    public void sendUpdatePolicyEvent(PolicyUpdateEvent event){
        kafkaTemplate.send("update-policy-topic",event);
        System.out.println("update policy event name " + event.getBookingId() );
    }
    @Async
    public void sendRenewPolicyEvent(PolicyRenewEvent event){
        kafkaTemplate.send("renew-policy-topic" ,event);
        System.out.println("policy renew event name " + event.getBookingId());
    }
}
