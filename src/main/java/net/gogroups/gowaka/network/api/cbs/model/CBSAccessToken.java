package net.gogroups.gowaka.network.api.cbs.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/21/20 4:57 PM <br/>
 */
@Data
public class CBSAccessToken {

    private String accessToken;
    private String issuer;

}
