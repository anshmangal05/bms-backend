package com.example.bms.validation;

import com.example.bms.model.PolicyBooking;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class BookingValidator {
@JsonInclude(JsonInclude.Include.NON_NULL)
    private static final Map<Integer, List<String>> PRODUCT_FIELDS = Map.of(
            2, List.of("members","sumInsured"),
            7, List.of("coverAmount","termYears","smoker"),
            115, List.of("investmentAmount","paymentFrequency","riskProfile"),
            3, List.of("tripStartDate","tripEndDate","destination","travellers","visaRequired"),
            114, List.of("idv","twAge"),
            117, List.of("idv","vehicleAge","fuelType")
    );
    public static String validateGenericBooking(PolicyBooking booking) {

        if (booking == null)
            return "booking body missing";

        if (booking.getProductId() == null)
            return "productId is required";

        if (booking.getBookingData() == null)
            return "bookingData required";

        String fieldError = validateRequiredFields(
                booking.getProductId(),
                booking.getBookingData());

        if (fieldError != null)
            return fieldError;

        return validateBusinessRules(
                booking.getProductId(),
                booking.getBookingData());
    }
    private static String validateRequiredFields(int productId, Map<String,Object> bookingData) {

        List<String> required = PRODUCT_FIELDS.get(productId);

        if (required == null)
            return "unsupported productId";

        for (String f : required) {
            if (!bookingData.containsKey(f) || bookingData.get(f) == null)
                return f + " is required";
        }

        return null;
    }

    private static String validateBusinessRules(int productId, Map<String,Object> bookingData) {

        try {
            // HEALTH
            if (productId == 2) {
                int members = Integer.parseInt(bookingData.get("members").toString());
                long sum = Long.parseLong(bookingData.get("sumInsured").toString());

                if (members < 1 || members > 20)  return "members must be between 1 and 20";

                if (sum < 10000 || sum > 10000000)  return "invalid sumInsured";
            }
            // TRAVEL
            if (productId == 3) {
                LocalDate start = LocalDate.parse(bookingData.get("tripStartDate").toString());
                LocalDate end = LocalDate.parse(bookingData.get("tripEndDate").toString());
                int travellers = Integer.parseInt(bookingData.get("travellers").toString());

                if (end.isBefore(start)) return "tripEndDate before start";

                if (ChronoUnit.DAYS.between(start,end)>180) return "trip too long";
                if(travellers <1 || travellers >20) return "too many travellers!";

            }

            // MOTOR
            if (productId == 117 ) {
                long idv = Long.parseLong(bookingData.get("idv").toString());
                int vehicleAge = Integer.parseInt(bookingData.get("vehicleAge").toString());
                String fuelType =bookingData.get("fuelType").toString();
                if (idv <= 0)
                    return "invalid idv";
                if(vehicleAge<0 || vehicleAge >20 ){
                    return "invalid vehicleAge";
                }
                if(!List.of("PETROL","DIESEL","CNG").contains(fuelType.toUpperCase())){
                    return "invalid fuelType";
                }
            }
            //TW
            if(productId ==114){
                long idv = Long.parseLong(bookingData.get("idv").toString());
                int twAge = Integer.parseInt(bookingData.get("twAge").toString());

                if(idv<=0){
                    return "invalid idv";
                }
                if(twAge<0 || twAge>20){
                    return "invalid twAge";
                }
            }
            // TERM
            if (productId == 7) {
                long cover = Long.parseLong(bookingData.get("coverAmount").toString());
                int years = Integer.parseInt(bookingData.get("termYears").toString());
                boolean smoker =Boolean.parseBoolean(bookingData.get("smoker").toString());
                if (cover < 10000)  return "cover too low";
                if(years < 5 || years >40) return "termYears must be 5-40 years";
            }

            // INVESTMENT
            if (productId == 115) {
                long amount = Long.parseLong(bookingData.get("investmentAmount").toString());
                String freq = bookingData.get("paymentFrequency").toString();
                String risk = bookingData.get("riskProfile").toString();
                if (amount < 1000) return "investment too low";

                if(!List.of("MONTHLY","YEARLY","QUARTERLY").contains(freq.toUpperCase())){
                    return "invalid paymentFrequency";
                }
                if(!List.of("LOW","MEDIUM","HIGH").contains(risk.toUpperCase())){
                    return "invalid riskProfile";
                }
            }

        } catch (Exception e) {
            return "invalid bookingData format";
        }

        return null;
    }
}
