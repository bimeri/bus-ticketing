package net.gogroups.gowaka.dto;

import lombok.Data;

@Data
public class PassengerDTO {

    private String passengerName;
    private String passengerIdNumber;
    private Integer passengerSeatNumber;
    private String passengerPhoneNumber;
    private String passengerEmail;
    private String checkedInCode;
    private boolean checkedIn;
    private String qRCheckedInImage;
    private String qRCheckedInImageUrl;

}
