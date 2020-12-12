package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 9:07 PM <br/>
 */
@Data
public class CreateUserRequest {

    @NotBlank(message = "full name is required.")
    @Size(max = 50, message = "Full name can not be more than 50 characters")
    private String fullName;

    @NotBlank(message = "email is required.")
    @Email(message = "valid email required.")
    private String email;

    @NotBlank(message = "password is required.")
    @Size(max = 255, min = 6, message = "Password must be more the 6 characters but less than 255 characters")
    private String password;

}
