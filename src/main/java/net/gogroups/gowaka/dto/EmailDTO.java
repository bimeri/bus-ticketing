package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.Email;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/16/19 6:19 AM <br/>
 */
@Data
public class EmailDTO {

    @Email(message = "valid email is required.")
    private String email;
}
