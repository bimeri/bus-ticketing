package net.gowaka.gowaka.network.api.apisecurity.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 5:23 PM <br/>
 */
@Data
public class ApiSecurityAccessToken {
    private String header;
    private String type;
    private String issuer;
    private String version;
    private String token;
    private String refreshToken;
    private Long expiredIn;

}
