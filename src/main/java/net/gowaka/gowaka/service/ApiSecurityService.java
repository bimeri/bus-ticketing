package net.gowaka.gowaka.service;

import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityAccessToken;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityClientUser;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityUser;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityUsernamePassword;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 5:33 PM <br/>
 */
public interface ApiSecurityService {

    ApiSecurityAccessToken getClientToken(ApiSecurityClientUser apiSecurityClientUser);
    ApiSecurityAccessToken getUserToken(ApiSecurityUsernamePassword apiSecurityUsernamePassword);
    ApiSecurityUser registerUser(ApiSecurityUser apiSecurityUser, String clientToken);
}
