package net.gowaka.gowaka.dto;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/15/19 7:40 AM <br/>
 */
@Data
public class ChangePasswordDTO {

    private String email;
    private String oldPassword;
    private String password;

}
