package net.gowaka.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/16/20 12:28 PM <br/>
 */
@Data
public class UpdateProfileDTO {

    @NotBlank
    private String fullName;
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String idCardNumber;

}
