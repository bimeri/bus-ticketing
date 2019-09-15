package net.gowaka.gowaka.network.api.apisecurity.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/14/19 10:01 AM <br/>
 */
@Data
public class ApiSecurityErrorResponse {

    private String code;
    private String message;
    private String path;

}
