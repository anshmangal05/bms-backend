package com.example.bms.repository;

import com.example.bms.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer,String> {
    boolean existsByBookingsBookingId(String bookingId);
    Optional<Customer> findByBookingsBookingId(String bookingId);


}
