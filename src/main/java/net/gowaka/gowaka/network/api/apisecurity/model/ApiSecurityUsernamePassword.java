package net.gowaka.gowaka.network.api.apisecurity.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/15/19 12:13 AM <br/>
 */
@Data
public class ApiSecurityUsernamePassword {

    private String username;
    private String password;
    private String appName;
}
