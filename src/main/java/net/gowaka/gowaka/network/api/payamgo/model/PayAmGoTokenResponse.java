package net.gowaka.gowaka.network.api.payamgo.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 10:32 PM <br/>
 */
@Data
public class PayAmGoTokenResponse {

    private String header;
    private String issuer;
    private String accessToken;
    private String type;

}
