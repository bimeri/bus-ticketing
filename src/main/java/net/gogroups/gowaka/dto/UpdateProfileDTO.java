package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/16/20 12:28 PM <br/>
 */
@Data
public class UpdateProfileDTO {

    @NotBlank(message = "full name is required.")
    @Size(max = 50, message = "full name can not be more than 50 characters")
    private String fullName;

    @NotBlank(message = "phone number is required.")
    @Size(max = 15, message = "phone number can not be more than 15 characters")
    private String phoneNumber;

    @NotBlank(message = "id card number is required.")
    @Size(max = 50, message = "id card number can not be more than 15 characters")
    private String idCardNumber;

}
