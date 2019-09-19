package net.gowaka.gowaka.service;

import net.gowaka.gowaka.network.api.apisecurity.model.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 5:33 PM <br/>
 */
public interface ApiSecurityService {

    ApiSecurityAccessToken getClientToken(ApiSecurityClientUser apiSecurityClientUser);
    ApiSecurityAccessToken getUserToken(ApiSecurityUsernamePassword apiSecurityUsernamePassword);
    ApiSecurityUser registerUser(ApiSecurityUser apiSecurityUser, String clientToken);
    void changePassword(ApiSecurityChangePassword apiSecurityChangePassword);
    void forgotPassword(ApiSecurityForgotPassword apiSecurityForgotPassword);
    ApiSecurityUser getUserByUsername(String username, String clientToken);
    void updateUserInfo(String userId, String field, String value, String clientToken);

}
