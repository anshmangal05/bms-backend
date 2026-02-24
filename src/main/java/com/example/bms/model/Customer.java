package com.example.bms.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "customers")
//@CompoundIndex(
//        name = "customer_booking_idx",
//        def = "{'id': 1, 'bookings.bookingId': 1}"
//)
@Data //lombok -> no need to generate getter and setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Customer {
    @Id
    private String id;


    private String name ;

    private LocalDate dob;
    private List<PolicyBooking> bookings;

    }

