package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author Nnouka Stephen
 * @date 07 Oct 2019
 */
@Data
public class LocationDTO {
    @NotBlank(message = "country is required")
    private String country;
    @NotBlank(message = "state is required")
    private String state;
    @NotBlank(message = "city is required")
    private String city;
    @NotBlank(message = "address is required")
    private String address;
}
