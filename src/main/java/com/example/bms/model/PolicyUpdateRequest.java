package com.example.bms.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PolicyUpdateRequest {
    private LocalDate endDate;
    private Map<String,Object> bookingData;
}
