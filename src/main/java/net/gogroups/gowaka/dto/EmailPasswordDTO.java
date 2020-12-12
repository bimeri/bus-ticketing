package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/15/19 12:35 AM <br/>
 */
@Data
public class EmailPasswordDTO {

    @NotBlank(message = "email is required.")
    @Email(message = "valid email is required.")
    private String email;

    @NotBlank(message = "password is required.")
    @Size(max = 255, min = 2, message = "valid password required")
    private String password;

}
