package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.Embeddable;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 6:52 AM <br/>
 */
@Data
@Embeddable
public class Location {

    private String country;
    private String state;
    private String city;
    private String address;

    private String tlaCountry;
    private String tlaState;
    private String tlaCity;
    private String tlaAddress;

//    private Integer longitude;
//    private Integer latitude;
}
