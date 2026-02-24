package com.example.bms.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolicyBooking {

    @Indexed
    private String bookingId;
    private Integer productId;


   // @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate startDate;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate endDate;


    private String policyType;

    private Map<String,Object> bookingData;

    private String status;


}
