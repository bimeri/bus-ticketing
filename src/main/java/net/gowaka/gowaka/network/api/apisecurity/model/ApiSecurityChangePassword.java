package net.gowaka.gowaka.network.api.apisecurity.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/15/19 7:52 AM <br/>
 */
@Data
public class ApiSecurityChangePassword {

        private String oldPassword;
        private String password;
        private String username;

}
