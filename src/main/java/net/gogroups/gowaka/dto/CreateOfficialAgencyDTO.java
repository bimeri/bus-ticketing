package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:35 PM <br/>
 */
@Data
public class CreateOfficialAgencyDTO {

    @NotBlank(message = "agency name required.")
    @Size(max = 50, message = "agency name can not be more than 50 characters")
    private String agencyName;

    @Size(max = 50, message = "agency registration number can not be more than 50 characters")
    @NotBlank(message = "agency registration number required.")
    private String agencyRegistrationNumber;

    @NotBlank(message = "agency admin email required.")
    @Email(message = "Valid agency admin email required.")
    private String agencyAdminEmail;

    @NotBlank(message = "agency policy required.")
    private String policy;

    @Size(max = 50, message = "agencyAddress can not be more than 50 characters")
    @NotBlank(message = "agencyAddress policy required.")
    private String agencyAddress;

    @Size(max = 50, message = "agencyPhoneNumber can not be more than 50 characters")
    @NotBlank(message = "agencyPhoneNumber policy required.")
    private String agencyPhoneNumber;

    @Size(max = 50, message = "agency code can not be more than 5 characters")
    @NotBlank(message = "agency agency code is required.")
    private String code;

}
