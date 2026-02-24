package com.example.bms.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    public int statusCode;
    public String statusMessage;
    public Object data;


}
