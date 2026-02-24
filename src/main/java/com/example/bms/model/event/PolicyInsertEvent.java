package com.example.bms.model.event;

import com.example.bms.model.PolicyBooking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyInsertEvent {
    private String customerId;
    private PolicyBooking booking;
    private LocalDateTime insertedAt;
}
