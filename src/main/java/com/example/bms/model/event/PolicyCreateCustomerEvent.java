package com.example.bms.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyCreateCustomerEvent {
   // private String customerId;
    private String name;
    private LocalDate dob;
    private LocalDateTime createdAt;
}
