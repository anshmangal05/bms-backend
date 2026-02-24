package com.example.bms.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRenewEvent {
    private String customerId;
    private String bookingId;
    private LocalDate newEndDate;

    private Map<String, Object> bookingData;



}
