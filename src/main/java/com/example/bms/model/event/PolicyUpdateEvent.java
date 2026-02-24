package com.example.bms.model.event;

import com.example.bms.model.PolicyUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyUpdateEvent {
    private String customerId;
    private String bookingId;
    private PolicyUpdateRequest update;
    private LocalDate updatedAt;
}
