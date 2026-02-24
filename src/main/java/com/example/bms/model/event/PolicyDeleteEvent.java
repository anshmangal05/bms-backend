package com.example.bms.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDeleteEvent {
    private String customerId;
    private String bookingId;
    private LocalDateTime deletedAt;
}
