package com.example.bms.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyAddEvent {
    private String customerId;
    private  String bookingId;
    private String policyType;
    private String status;
    private LocalDateTime policyAddedAt;
}
