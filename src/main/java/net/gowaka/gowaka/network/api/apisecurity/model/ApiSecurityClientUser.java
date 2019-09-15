package net.gowaka.gowaka.network.api.apisecurity.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 5:35 PM <br/>
 */
@Data
public class ApiSecurityClientUser {

    private String clientId;
    private String clientSecret;
}
