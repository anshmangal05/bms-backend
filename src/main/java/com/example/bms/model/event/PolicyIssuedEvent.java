package com.example.bms.model.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyIssuedEvent {
    private String customerId;
    private String bookingId;
    private String policyType;
    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDateTime issuedAt;

}
