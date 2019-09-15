package net.gowaka.gowaka.network.api.apisecurity.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 7:57 PM <br/>
 */
@Data
public class ApiSecurityUser {

    private String id;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String roles;

}
