package net.gowaka.gowaka.network.api.apisecurity.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/16/19 6:23 AM <br/>
 */
@Data
public class ApiSecurityForgotPassword {

    private String username;
    private String applicationName;

}
